package com.projectdark.mobile;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Regression audit for MonsterAI AUTO -> orchestrator -> shared resolver submission. */
public final class MonsterAutoCombatBridgeAudit {
  private static final class Port implements CombatResolver.Port {
    final Map<String,Integer> hp=new HashMap<>(); int damageCalls;
    Port(){hp.put("player",100);hp.put("monster",20);hp.put("monster2",20);}
    public boolean actorAlive(String id){return hp.getOrDefault(id,0)>0;}
    public boolean targetAlive(String id){return actorAlive(id);}
    public boolean learned(String a,String id){return true;}
    public boolean cooldownReady(String a,String id){return true;}
    public boolean hasResource(String a,int n){return true;}
    public float distance(String a,String b){return 10f;}
    public boolean hasLineOfSight(String a,String b){return true;}
    public void consumeResource(String a,int n){}
    public void commitCooldown(String a,String id,float c){}
    public CombatResolver.EffectResult applyDamage(String actor,String target,String actionId,int amount){
      damageCalls++; int before=hp.get(target),after=Math.max(0,before-amount); hp.put(target,after);
      CombatResolver.DefeatedTargetKind kind=target.startsWith("monster")?CombatResolver.DefeatedTargetKind.MONSTER:CombatResolver.DefeatedTargetKind.PLAYER;
      return new CombatResolver.EffectResult(before-after,before>0&&after==0,CombatResolver.DefeatPublication.RESOLVER_OWNS,kind,CombatResolver.HitSemantic.DAMAGE);
    }
  }
  public static boolean verify(){
    Port port=new Port(); CombatResolver resolver=new CombatResolver(port);
    CombatResolver.Definition playerAttack=attack("player_attack",6,.10f);
    CombatResolver.Definition monsterAttack=attack("monster_attack",7,.24f);
    CombatActionOrchestrator actions=new CombatActionOrchestrator(resolver,Arrays.asList(playerAttack,monsterAttack));
    MonsterAutoCombatBridge bridge=new MonsterAutoCombatBridge(actions,"monster_attack");
    if(!actions.submitManual("player","monster","player_attack").accepted())return false;
    MonsterAutoCombatBridge.Result auto=bridge.submit("monster","player");
    if(auto.outcome!=MonsterAutoCombatBridge.Outcome.ACCEPTED||auto.actionSequence==0)return false;
    if(port.damageCalls!=0||actions.activeActionCount()!=2)return false;
    actions.tick(.10f);
    if(port.damageCalls!=1||port.hp.get("monster")!=14||port.hp.get("player")!=100)return false;
    actions.tick(.14f);
    if(port.damageCalls!=2||port.hp.get("player")!=93)return false;
    List<CombatResolver.Event> events=actions.drainEvents();
    if(!has(events,"monster","monster_attack",CombatResolver.InputMode.AUTO,CombatResolver.EventType.EFFECT_APPLIED))return false;
    if(!has(events,"player","player_attack",CombatResolver.InputMode.MANUAL,CombatResolver.EventType.EFFECT_APPLIED))return false;
    MonsterAutoCombatBridge missing=new MonsterAutoCombatBridge(actions,"missing_action");
    int before=port.damageCalls;
    MonsterAutoCombatBridge.Result unresolved=missing.submit("monster2","player");
    return unresolved.outcome==MonsterAutoCombatBridge.Outcome.ACTION_UNRESOLVED&&port.damageCalls==before&&!resolver.actionActive("monster2");
  }
  private static CombatResolver.Definition attack(String id,int damage,float hitTime){return new CombatResolver.Definition(id,CombatResolver.ActionKind.ATTACK,CombatResolver.ActionState.ATTACK,CombatResolver.EffectType.PHYSICAL_HIT,false,0,.5f,50f,hitTime,damage);}
  private static boolean has(List<CombatResolver.Event> events,String actor,String actionId,CombatResolver.InputMode mode,CombatResolver.EventType type){for(CombatResolver.Event e:events)if(type==e.type&&actor.equals(e.actorId)&&actionId.equals(e.actionId)&&mode==e.inputMode)return true;return false;}
  public static void main(String[] args){if(!verify())throw new AssertionError("MonsterAutoCombatBridgeAudit failed");System.out.println("MonsterAutoCombatBridgeAudit PASS");}
}
