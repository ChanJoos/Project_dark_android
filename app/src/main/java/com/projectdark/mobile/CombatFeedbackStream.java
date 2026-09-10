package com.projectdark.mobile;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;

/**
 * Combat-owned presentation event projection. Renderer/UI consumes immutable snapshots.
 * No drawing or HUD coordinates are owned here.
 */
public final class CombatFeedbackStream {
  public enum Type { ACTION_STARTED, ACTION_REJECTED, ACTION_CANCELLED, EFFECT_APPLIED, HIT, CRIT, MISS, HEAL, MONSTER_DEFEATED }

  public interface AnchorPort {
    float anchorX(String entityId);
    float anchorY(String entityId);
  }

  public static final class Event {
    public final long sequence,resolverSequence,actionSequence;
    public final Type type;
    public final String actorId,targetId,actionId;
    public final CombatResolver.ActionState actionState;
    public final CombatResolver.EffectType effectType;
    public final CombatResolver.InputMode inputMode;
    public final CombatResolver.RejectReason rejectReason;
    public final int amount;
    private Event(long sequence,CombatResolver.Event source,Type type){
      this.sequence=sequence;this.resolverSequence=source.sequence;this.actionSequence=source.actionSequence;
      this.type=type;this.actorId=source.actorId;this.targetId=source.targetId;this.actionId=source.actionId;
      this.actionState=source.state;this.effectType=source.effectType;this.inputMode=source.inputMode;
      this.rejectReason=source.rejectReason;this.amount=source.amount;
    }
  }

  private static final int MAX_EVENTS=128;
  private final Deque<Event> events=new ArrayDeque<>();
  private final DamageNumberModel damageNumbers=new DamageNumberModel();
  private long sequence;

  public void consume(List<CombatResolver.Event> source,AnchorPort anchors){
    if(source==null)return;
    for(CombatResolver.Event e:source){
      Type type=map(e);
      if(type==null)continue;
      add(new Event(++sequence,e,type));
      if(e.type==CombatResolver.EventType.HIT_FEEDBACK&&anchors!=null){
        DamageNumberModel.SemanticType semantic=mapSemantic(e.hitSemantic);
        damageNumbers.spawn(e.targetId,semantic,e.amount,anchors.anchorX(e.targetId),anchors.anchorY(e.targetId));
      }
    }
  }

  public void tick(float dt){damageNumbers.tick(dt);}
  public List<Event> events(){return Collections.unmodifiableList(new ArrayList<>(events));}
  public List<Event> drainEvents(){List<Event> out=new ArrayList<>(events);events.clear();return Collections.unmodifiableList(out);}
  public List<DamageNumberModel.Snapshot> damageNumbers(){return damageNumbers.snapshot();}
  public int damageNumberCount(){return damageNumbers.size();}

  private void add(Event e){events.addLast(e);while(events.size()>MAX_EVENTS)events.removeFirst();}
  private static Type map(CombatResolver.Event e){
    switch(e.type){
      case ACTION_STARTED:return Type.ACTION_STARTED;
      case ACTION_REJECTED:return Type.ACTION_REJECTED;
      case ACTION_CANCELLED:return Type.ACTION_CANCELLED;
      case EFFECT_APPLIED:return Type.EFFECT_APPLIED;
      case MONSTER_DEFEATED:return Type.MONSTER_DEFEATED;
      case HIT_FEEDBACK:
        switch(e.hitSemantic){case CRIT:return Type.CRIT;case MISS:return Type.MISS;case HEAL:return Type.HEAL;default:return Type.HIT;}
      default:return null;
    }
  }
  private static DamageNumberModel.SemanticType mapSemantic(CombatResolver.HitSemantic semantic){
    if(semantic==null)return DamageNumberModel.SemanticType.DAMAGE;
    switch(semantic){case CRIT:return DamageNumberModel.SemanticType.CRIT;case MISS:return DamageNumberModel.SemanticType.MISS;case HEAL:return DamageNumberModel.SemanticType.HEAL;default:return DamageNumberModel.SemanticType.DAMAGE;}
  }
}
