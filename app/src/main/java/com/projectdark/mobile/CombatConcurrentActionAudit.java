package com.projectdark.mobile;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Deterministic audit for actor-scoped action slots and shared MANUAL/AUTO orchestration. */
public final class CombatConcurrentActionAudit {
  private static final class Port implements CombatResolver.Port {
    final Map<String,Integer> hp=new HashMap<>();
    int effects;
    Port(){hp.put("player",100);hp.put("monster_a",20);hp.put("monster_b",20);}
    public boolean actorAlive(String id){return hp.getOrDefault(id,0)>0;}
    public boolean targetAlive(String id){return actorAlive(id);}
    public boolean learned(String actorId,String actionId){return true;}
    public boolean cooldownReady(String actorId,String actionId){return true;}
    public boolean hasResource(String actorId,int amount){return true;}
    public float distance(String actorId,String targetId){return 10f;}
    public boolean hasLineOfSight(String actorId,String targetId){return true;}
    public void consumeResource(String actorId,int amount){}
    public void commitCooldown(String actorId,String actionId,float cooldown){}
    public CombatResolver.EffectResult applyDamage(String actorId,String targetId,String actionId,int amount){
      effects++;
      int before=hp.get(targetId),after=Math.max(0,before-amount);
      hp.put(targetId,after);
      CombatResolver.DefeatedTargetKind kind=targetId.startsWith("monster")
          ?CombatResolver.DefeatedTargetKind.MONSTER:CombatResolver.DefeatedTargetKind.PLAYER;
      return new CombatResolver.EffectResult(before-after,before>0&&after==0,
          CombatResolver.DefeatPublication.RESOLVER_OWNS,kind,CombatResolver.HitSemantic.DAMAGE);
    }
  }
  private static final class AiPort implements MonsterAIController.Port {
    final CombatActionOrchestrator actions;
    int submissions;
    AiPort(CombatActionOrchestrator actions){this.actions=actions;}
    public boolean monsterAlive(String id){return true;}
    public boolean targetAlive(String id){return true;}
    public float distance(String monsterId,String targetId){return 10f;}
    public boolean hasLineOfSight(String monsterId,String targetId){return true;}
    public boolean tryMoveToward(String monsterId,String targetId,float maxDistance){return true;}
    public boolean submitAttack(MonsterAIController.AttackRequest request){
      submissions++;
      return actions.submitMonsterAttack(request);
    }
  }

  public static void main(String[] args){run();System.out.println("CombatConcurrentActionAudit PASS");}
  public static void run(){
    Port port=new Port();
    CombatResolver resolver=new CombatResolver(port);
    CombatResolver.Definition playerAttack=definition("player_attack",6,.20f);
    CombatResolver.Definition monsterAttack=definition("monster_attack",7,.20f);
    CombatActionOrchestrator actions=new CombatActionOrchestrator(resolver,Arrays.asList(playerAttack,monsterAttack));

    require(actions.submitManual("player","monster_a","player_attack").accepted(),"manual player action must start");
    require(actions.submit("monster_a","player","monster_attack",CombatResolver.InputMode.AUTO).accepted(),"monster A AUTO action must coexist");
    require(actions.submit("monster_b","player","monster_attack",CombatResolver.InputMode.AUTO).accepted(),"monster B AUTO action must coexist");
    CombatActionOrchestrator.Submission busy=actions.submitManual("player","monster_b","player_attack");
    require(busy.outcome==CombatActionOrchestrator.Outcome.REJECTED&&busy.rejectReason==CombatResolver.RejectReason.ACTION_BUSY,
        "only the same actor must be busy");
    require(actions.activeActionCount()==3,"three actors must own independent slots");
    actions.tick(.20f);
    require(port.effects==3&&port.hp.get("monster_a")==14&&port.hp.get("player")==86,"all concurrent hit frames must resolve once");
    require(actions.activeActionCount()==0,"resolved slots must be released");
    List<CombatResolver.Event> events=actions.drainEvents();
    require(count(events,CombatResolver.EventType.EFFECT_APPLIED)==3,"three effects required");
    require(countMode(events,CombatResolver.EventType.EFFECT_APPLIED,CombatResolver.InputMode.MANUAL)==1,"manual effect identity");
    require(countMode(events,CombatResolver.EventType.EFFECT_APPLIED,CombatResolver.InputMode.AUTO)==2,"AUTO effect identity");

    require(actions.submitManual("player","monster_a","missing").outcome==CombatActionOrchestrator.Outcome.ACTION_UNRESOLVED,
        "unknown actionId must fail closed before resolver mutation");

    // MonsterAI's private AttackRequest DTO must cross the orchestrator into the same resolver.
    AiPort aiPort=new AiPort(actions);
    MonsterAIController.Config config=new MonsterAIController.Config(30,20,40,20,.1f,.1f,.1f,.2f);
    MonsterAIController ai=new MonsterAIController("monster_a","monster_attack",config,aiPort);
    ai.acquireTarget("player");
    ai.tick(.01f);
    require(ai.tick(.1f).state==MonsterAIController.State.ATTACK,"AI windup must submit through orchestrator");
    require(aiPort.submissions==1&&actions.actionActive("monster_a"),"AI request must own monster actor slot");
    int before=port.effects;
    actions.tick(.2f);
    require(port.effects==before+1,"AI-orchestrated effect must resolve exactly once");
  }

  private static CombatResolver.Definition definition(String id,int damage,float hitTime){
    return new CombatResolver.Definition(id,CombatResolver.ActionKind.ATTACK,CombatResolver.ActionState.ATTACK,
        CombatResolver.EffectType.PHYSICAL_HIT,false,0,.5f,40f,hitTime,damage);
  }
  private static int count(List<CombatResolver.Event> events,CombatResolver.EventType type){
    int count=0;for(CombatResolver.Event event:events)if(event.type==type)count++;return count;
  }
  private static int countMode(List<CombatResolver.Event> events,CombatResolver.EventType type,CombatResolver.InputMode mode){
    int count=0;for(CombatResolver.Event event:events)if(event.type==type&&event.inputMode==mode)count++;return count;
  }
  private static void require(boolean condition,String message){if(!condition)throw new IllegalStateException(message);}
}
