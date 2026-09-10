package com.projectdark.mobile;

import java.util.HashMap;
import java.util.Map;

/**
 * Combat-owned adapter over current RuntimeState facts.
 * It deliberately does not own collision/pathfinding, UI, rewards or progression mutation.
 */
public final class RuntimeCombatPortAdapter implements CombatResolver.Port {
  public interface LineOfSightPort { boolean hasLineOfSight(String actorId,String targetId); }

  private static final String PLAYER_ID="player";
  private final RuntimeState state;
  private final LineOfSightPort los;
  private final Map<String,Float> cooldowns=new HashMap<>();

  public RuntimeCombatPortAdapter(RuntimeState state,LineOfSightPort los){
    if(state==null||los==null)throw new IllegalArgumentException("state and LOS port are required");
    this.state=state;this.los=los;
  }

  public void tick(float dt){
    if(dt<=0f)return;
    for(Map.Entry<String,Float> e:cooldowns.entrySet())e.setValue(Math.max(0f,e.getValue()-dt));
  }

  public boolean actorAlive(String actorId){return entityAlive(actorId);}
  public boolean targetAlive(String targetId){return entityAlive(targetId);}

  public boolean learned(String actorId,String actionId){
    if(!PLAYER_ID.equals(actorId))return false;
    return state.rpg().learnedActionIds().contains(actionId);
  }

  public boolean cooldownReady(String actorId,String actionId){
    Float value=cooldowns.get(key(actorId,actionId));return value==null||value<=0f;
  }

  public boolean hasResource(String actorId,int amount){
    if(amount<=0)return true;
    return PLAYER_ID.equals(actorId)&&state.player().alive&&state.player().mp>=amount;
  }

  public float distance(String actorId,String targetId){
    RuntimeState.Monster actorMonster=findMonster(actorId),targetMonster=findMonster(targetId);
    if(PLAYER_ID.equals(actorId)&&targetMonster!=null)return state.distanceTo(targetMonster);
    if(actorMonster!=null&&PLAYER_ID.equals(targetId))return state.distanceTo(actorMonster);
    if(actorMonster!=null&&targetMonster!=null){float dx=actorMonster.x-targetMonster.x,dy=actorMonster.y-targetMonster.y;return(float)Math.sqrt(dx*dx+dy*dy);}
    return Float.POSITIVE_INFINITY;
  }

  public boolean hasLineOfSight(String actorId,String targetId){return los.hasLineOfSight(actorId,targetId);}

  public void consumeResource(String actorId,int amount){
    if(amount<=0)return;
    if(!PLAYER_ID.equals(actorId)||state.player().mp<amount)throw new IllegalStateException("resource gate violated");
    state.player().mp-=amount;
  }

  public void commitCooldown(String actorId,String actionId,float cooldown){cooldowns.put(key(actorId,actionId),Math.max(0f,cooldown));}

  public CombatResolver.EffectResult applyDamage(String actorId,String targetId,String actionId,int amount){
    RuntimeState.Monster targetMonster=findMonster(targetId);
    if(targetMonster!=null){
      int before=targetMonster.hp;
      state.damage(targetMonster,amount);
      int applied=Math.max(0,before-targetMonster.hp);
      boolean defeated=before>0&&!targetMonster.alive;
      return new CombatResolver.EffectResult(applied,defeated,CombatResolver.DefeatPublication.PORT_ALREADY_PUBLISHED,
          CombatResolver.DefeatedTargetKind.MONSTER,CombatResolver.HitSemantic.DAMAGE);
    }
    if(PLAYER_ID.equals(targetId)){
      int before=state.player().hp;
      state.damagePlayer(amount);
      int applied=Math.max(0,before-state.player().hp);
      boolean defeated=before>0&&!state.player().alive;
      return new CombatResolver.EffectResult(applied,defeated,CombatResolver.DefeatPublication.PORT_ALREADY_PUBLISHED,
          CombatResolver.DefeatedTargetKind.PLAYER,CombatResolver.HitSemantic.DAMAGE);
    }
    return new CombatResolver.EffectResult(0,false,CombatResolver.DefeatPublication.PORT_ALREADY_PUBLISHED,
        CombatResolver.DefeatedTargetKind.OTHER,CombatResolver.HitSemantic.MISS);
  }

  private boolean entityAlive(String id){if(PLAYER_ID.equals(id))return state.player().alive;RuntimeState.Monster m=findMonster(id);return m!=null&&m.alive;}
  private RuntimeState.Monster findMonster(String id){if(id==null)return null;for(RuntimeState.Monster m:state.monsters())if(id.equals(m.id))return m;return null;}
  private static String key(String actorId,String actionId){return actorId+"\u0000"+actionId;}
}
