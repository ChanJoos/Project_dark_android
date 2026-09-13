package com.projectdark.mobile;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Combat-owned animation state projection for renderer/UX integration.
 * Consumes resolver events/snapshots and exposes only state/timing; it performs no drawing.
 * Director D002 acceptance: ATTACK / CAST / SKILL_KICK must be observable and return to movement/idle.
 */
public final class CombatAnimationStateModel {
  public enum VisualState { IDLE, WALK, ATTACK, CAST, SKILL_KICK, HIT, DEAD }

  public static final class Snapshot {
    public final String actorId,targetId,actionId;
    public final VisualState state;
    public final CombatResolver.ActionState actionState;
    public final CombatResolver.InputMode inputMode;
    public final float progress;
    public final long actionSequence;
    Snapshot(String actorId,String targetId,String actionId,VisualState state,
        CombatResolver.ActionState actionState,CombatResolver.InputMode inputMode,float progress,long actionSequence){
      this.actorId=actorId;this.targetId=targetId;this.actionId=actionId;this.state=state;
      this.actionState=actionState;this.inputMode=inputMode;this.progress=progress;this.actionSequence=actionSequence;
    }
  }

  private static final class Entry {
    String targetId,actionId;
    VisualState state=VisualState.IDLE;
    CombatResolver.ActionState actionState;
    CombatResolver.InputMode inputMode;
    float progress;
    long actionSequence;
  }

  private final Map<String,Entry> byActor=new LinkedHashMap<>();

  public void syncActions(Iterable<CombatResolver.ActionSnapshot> actions){
    Map<String,Boolean> active=new LinkedHashMap<>();
    if(actions!=null)for(CombatResolver.ActionSnapshot a:actions){
      active.put(a.actorId,Boolean.TRUE);
      Entry e=entry(a.actorId);
      e.targetId=a.targetId;e.actionId=a.actionId;e.actionState=a.state;e.inputMode=a.inputMode;
      e.progress=a.progress;e.actionSequence=a.actionSequence;e.state=visualFor(a.state);
    }
    for(Map.Entry<String,Entry> item:byActor.entrySet()){
      Entry e=item.getValue();
      if(!active.containsKey(item.getKey())&&isCombatAction(e.state)){
        e.state=VisualState.IDLE;e.progress=0f;e.actionState=null;e.actionId=null;e.targetId=null;
      }
    }
  }

  public void consume(Iterable<CombatResolver.Event> events){
    if(events==null)return;
    for(CombatResolver.Event event:events){
      Entry actor=entry(event.actorId);
      switch(event.type){
        case ACTION_STARTED:
          actor.state=visualFor(event.state);actor.actionState=event.state;actor.actionId=event.actionId;
          actor.targetId=event.targetId;actor.inputMode=event.inputMode;actor.actionSequence=event.actionSequence;actor.progress=0f;
          break;
        case ACTION_REJECTED:
        case ACTION_CANCELLED:
          if(actor.actionSequence==event.actionSequence||event.actionSequence==0){actor.state=VisualState.IDLE;actor.progress=0f;}
          break;
        case HIT_FEEDBACK:
          entry(event.targetId).state=VisualState.HIT;
          break;
        case MONSTER_DEFEATED:
          entry(event.targetId).state=VisualState.DEAD;
          break;
        case EFFECT_APPLIED:
        default:
          break;
      }
    }
  }

  /** World/UX owner may explicitly project movement when no combat action is active. */
  public void setLocomotion(String actorId,boolean moving){
    Entry e=entry(actorId);
    if(!isCombatAction(e.state)&&e.state!=VisualState.DEAD)e.state=moving?VisualState.WALK:VisualState.IDLE;
  }

  public void clearHit(String actorId,boolean moving){
    Entry e=entry(actorId);
    if(e.state==VisualState.HIT)e.state=moving?VisualState.WALK:VisualState.IDLE;
  }

  public void revive(String actorId){Entry e=entry(actorId);e.state=VisualState.IDLE;e.progress=0f;}

  public Snapshot snapshot(String actorId){
    Entry e=entry(actorId);
    return new Snapshot(actorId,e.targetId,e.actionId,e.state,e.actionState,e.inputMode,e.progress,e.actionSequence);
  }

  public Map<String,Snapshot> snapshots(){
    Map<String,Snapshot> out=new LinkedHashMap<>();
    for(String actorId:byActor.keySet())out.put(actorId,snapshot(actorId));
    return Collections.unmodifiableMap(out);
  }

  private Entry entry(String actorId){
    if(actorId==null||actorId.trim().isEmpty())throw new IllegalArgumentException("actorId");
    Entry e=byActor.get(actorId);if(e==null){e=new Entry();byActor.put(actorId,e);}return e;
  }

  private static VisualState visualFor(CombatResolver.ActionState state){
    if(state==null)return VisualState.IDLE;
    switch(state){
      case MAGIC:return VisualState.CAST;
      case SKILL:
      case KICK:return VisualState.SKILL_KICK;
      case ATTACK:
      default:return VisualState.ATTACK;
    }
  }
  private static boolean isCombatAction(VisualState state){
    return state==VisualState.ATTACK||state==VisualState.CAST||state==VisualState.SKILL_KICK;
  }
}
