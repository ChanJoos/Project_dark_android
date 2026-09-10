package com.projectdark.mobile;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Combat-owned adapter exposing current RuntimeState facts/mutations to CombatResolver.
 *
 * RuntimeState remains the HP/MP/ledger authority. This adapter owns only per-action cooldown clocks
 * required by the resolver contract. LOS and learned-action truth are injected by their owning domains.
 */
public final class RuntimeCombatPortAdapter implements CombatResolver.Port {
  public interface LineOfSightPort {
    boolean hasLineOfSight(String actorId,String targetId);
  }

  public interface LearnedActionPort {
    boolean learned(String actorId,String actionId);
  }

  private final RuntimeState state;
  private final LineOfSightPort lineOfSight;
  private final LearnedActionPort learnedActions;
  private final Map<String,Float> cooldowns=new HashMap<>();

  public RuntimeCombatPortAdapter(RuntimeState state,LineOfSightPort lineOfSight,LearnedActionPort learnedActions){
    if(state==null||lineOfSight==null||learnedActions==null)throw new IllegalArgumentException("runtime combat dependencies");
    this.state=state;
    this.lineOfSight=lineOfSight;
    this.learnedActions=learnedActions;
  }

  public void tick(float dt){
    if(dt<=0f||cooldowns.isEmpty())return;
    Iterator<Map.Entry<String,Float>> it=cooldowns.entrySet().iterator();
    while(it.hasNext()){
      Map.Entry<String,Float> entry=it.next();
      float next=Math.max(0f,entry.getValue()-dt);
      if(next<=0f)it.remove(); else entry.setValue(next);
    }
  }

  public boolean actorAlive(String actorId){return entityAlive(actorId);}
  public boolean targetAlive(String targetId){return entityAlive(targetId);}
  public boolean learned(String actorId,String actionId){return learnedActions.learned(actorId,actionId);}
  public boolean cooldownReady(String actorId,String actionId){return cooldownRemaining(actorId,actionId)<=0f;}

  public boolean hasResource(String actorId,int amount){
    if(amount<=0)return true;
    if("player".equals(actorId))return state.player().alive&&state.player().mp>=amount;
    // Current monster runtime has no canonical MP/resource pool. Resource-costing monster actions fail closed.
    return false;
  }

  public float distance(String actorId,String targetId){
    Position a=position(actorId),b=position(targetId);
    if(a==null||b==null)return Float.POSITIVE_INFINITY;
    float dx=a.x-b.x,dy=a.y-b.y;
    return (float)Math.sqrt(dx*dx+dy*dy);
  }

  public boolean hasLineOfSight(String actorId,String targetId){return lineOfSight.hasLineOfSight(actorId,targetId);}

  public void consumeResource(String actorId,int amount){
    if(amount<=0)return;
    if(!"player".equals(actorId))throw new IllegalStateException("monster resource pool unresolved");
    RuntimeState.Player player=state.player();
    if(!player.alive||player.mp<amount)throw new IllegalStateException("resource commit without validation");
    player.mp-=amount;
  }

  public void commitCooldown(String actorId,String actionId,float cooldown){
    if(actorId==null||actionId==null)throw new IllegalArgumentException("cooldown identity");
    if(cooldown<=0f){cooldowns.remove(key(actorId,actionId));return;}
    cooldowns.put(key(actorId,actionId),cooldown);
  }

  public float cooldownRemaining(String actorId,String actionId){
    Float value=cooldowns.get(key(actorId,actionId));
    return value==null?0f:Math.max(0f,value);
  }

  public CombatResolver.EffectResult applyDamage(String actorId,String targetId,String actionId,int amount){
    if(amount<=0||!entityAlive(actorId)||!entityAlive(targetId))
      return new CombatResolver.EffectResult(0,false,CombatResolver.DefeatPublication.PORT_ALREADY_PUBLISHED,
          targetKind(targetId),CombatResolver.HitSemantic.DAMAGE);

    if("player".equals(targetId)){
      int before=state.player().hp;
      state.damagePlayer(amount);
      int applied=Math.max(0,before-state.player().hp);
      return new CombatResolver.EffectResult(applied,before>0&&!state.player().alive,
          CombatResolver.DefeatPublication.PORT_ALREADY_PUBLISHED,CombatResolver.DefeatedTargetKind.PLAYER,
          CombatResolver.HitSemantic.DAMAGE);
    }

    RuntimeState.Monster monster=findMonster(targetId);
    if(monster!=null){
      int before=monster.hp;
      state.damage(monster,amount);
      int applied=Math.max(0,before-monster.hp);
      return new CombatResolver.EffectResult(applied,before>0&&!monster.alive,
          CombatResolver.DefeatPublication.PORT_ALREADY_PUBLISHED,CombatResolver.DefeatedTargetKind.MONSTER,
          CombatResolver.HitSemantic.DAMAGE);
    }

    return new CombatResolver.EffectResult(0,false,CombatResolver.DefeatPublication.PORT_ALREADY_PUBLISHED,
        CombatResolver.DefeatedTargetKind.OTHER,CombatResolver.HitSemantic.DAMAGE);
  }

  private boolean entityAlive(String id){
    if("player".equals(id))return state.player().alive;
    RuntimeState.Monster monster=findMonster(id);
    return monster!=null&&monster.alive;
  }

  private Position position(String id){
    if("player".equals(id))return new Position(state.player().x,state.player().y);
    RuntimeState.Monster monster=findMonster(id);
    return monster==null?null:new Position(monster.x,monster.y);
  }

  private RuntimeState.Monster findMonster(String id){
    if(id==null)return null;
    for(RuntimeState.Monster monster:state.monsters())if(id.equals(monster.id))return monster;
    return null;
  }

  private CombatResolver.DefeatedTargetKind targetKind(String targetId){
    if("player".equals(targetId))return CombatResolver.DefeatedTargetKind.PLAYER;
    return findMonster(targetId)==null?CombatResolver.DefeatedTargetKind.OTHER:CombatResolver.DefeatedTargetKind.MONSTER;
  }

  private static String key(String actorId,String actionId){return String.valueOf(actorId)+'\u0000'+String.valueOf(actionId);}
  private static final class Position { final float x,y; Position(float x,float y){this.x=x;this.y=y;} }
}
