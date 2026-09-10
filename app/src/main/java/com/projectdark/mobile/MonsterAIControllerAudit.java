package com.projectdark.mobile;

/** Deterministic audit for AI states, World movement delegation, AUTO requests and windup edge cases. */
public final class MonsterAIControllerAudit {
  private MonsterAIControllerAudit(){}
  private static final class Fixture implements MonsterAIController.Port {
    boolean monsterAlive=true,targetAlive=true,los=true,move=true,acceptAttack=true;
    float distance=100f;
    int moveCalls,attackCalls;
    CombatResolver.InputMode lastMode;
    public boolean monsterAlive(String id){return monsterAlive;}
    public boolean targetAlive(String id){return targetAlive;}
    public float distance(String a,String b){return distance;}
    public boolean hasLineOfSight(String a,String b){return los;}
    public boolean tryMoveToward(String a,String b,float maxDistance){moveCalls++;if(move)distance=Math.max(0f,distance-maxDistance);return move;}
    public boolean submitAttack(MonsterAIController.AttackRequest request){attackCalls++;lastMode=request.inputMode;return acceptAttack;}
  }
  public static boolean verify(){
    MonsterAIController.Config c=new MonsterAIController.Config(90f,30f,140f,40f,.2f,.1f,.3f,.25f);

    Fixture f=new Fixture();
    MonsterAIController ai=new MonsterAIController("monster","monster_basic_attack",c,f);
    if(ai.acquireTarget("player").state!=MonsterAIController.State.IDLE)return false;
    f.distance=80f;
    if(ai.tick(.01f).state!=MonsterAIController.State.DETECT)return false;
    if(ai.tick(.01f).state!=MonsterAIController.State.CHASE)return false;
    ai.tick(1f);if(f.moveCalls!=1)return false;
    f.distance=25f;
    if(ai.tick(.01f).state!=MonsterAIController.State.WINDUP)return false;
    ai.tick(.1f);if(f.attackCalls!=0)return false;
    if(ai.tick(.1f).state!=MonsterAIController.State.ATTACK||f.attackCalls!=1)return false;
    if(f.lastMode!=CombatResolver.InputMode.AUTO)return false;
    ai.tick(.1f);if(ai.snapshot().state!=MonsterAIController.State.RECOVER)return false;
    ai.tick(.3f);if(ai.snapshot().state!=MonsterAIController.State.IDLE)return false;
    f.distance=200f;
    if(ai.tick(.01f).cancelReason!=MonsterAIController.CancelReason.OUT_OF_CANCEL_RANGE)return false;

    f.monsterAlive=false;
    if(ai.tick(.01f).state!=MonsterAIController.State.DEAD)return false;
    f.monsterAlive=true;ai.onRespawn();f.distance=60f;f.move=false;ai.acquireTarget("player");ai.tick(.01f);
    ai.tick(.13f);ai.tick(.13f);
    if(ai.snapshot().state!=MonsterAIController.State.CANCELLED||ai.snapshot().cancelReason!=MonsterAIController.CancelReason.MOVE_BLOCKED)return false;

    // Target death during windup cancels before an AUTO request can escape.
    Fixture death=new Fixture();death.distance=20f;
    MonsterAIController deathAi=new MonsterAIController("m2","attack",c,death);
    deathAi.acquireTarget("player");deathAi.tick(.01f);
    if(deathAi.snapshot().state!=MonsterAIController.State.WINDUP)return false;
    death.targetAlive=false;
    if(deathAi.tick(.2f).cancelReason!=MonsterAIController.CancelReason.TARGET_DEAD||death.attackCalls!=0)return false;

    // LOS loss during windup also cancels before submission.
    Fixture los=new Fixture();los.distance=20f;
    MonsterAIController losAi=new MonsterAIController("m3","attack",c,los);
    losAi.acquireTarget("player");losAi.tick(.01f);los.los=false;
    if(losAi.tick(.2f).cancelReason!=MonsterAIController.CancelReason.LOS_BLOCKED||los.attackCalls!=0)return false;

    // Shared resolver rejection is a recoverable combat outcome, not a second damage path.
    Fixture reject=new Fixture();reject.distance=20f;reject.acceptAttack=false;
    MonsterAIController rejectAi=new MonsterAIController("m4","attack",c,reject);
    rejectAi.acquireTarget("player");rejectAi.tick(.01f);
    if(rejectAi.tick(.2f).state!=MonsterAIController.State.RECOVER)return false;
    return reject.attackCalls==1&&rejectAi.snapshot().lastEvent==MonsterAIController.EventType.ATTACK_REJECTED;
  }
  public static void main(String[] args){if(!verify())throw new AssertionError("MonsterAIControllerAudit failed");System.out.println("MonsterAIControllerAudit PASS");}
}
