package com.projectdark.mobile;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Combat-owned action validator / delayed effect resolver.
 * Manual and AUTO requests enter the same begin()/tick() path.
 * Numeric definitions may remain [B]; this class does not promote them to original-game facts.
 */
public final class CombatResolver {
  public enum ActionKind { ATTACK, SKILL, MAGIC, KICK }
  public enum ActionState { ATTACK, SKILL, MAGIC, KICK }
  public enum EffectType { PHYSICAL_HIT, SKILL_HIT, MAGIC_HIT, KICK_HIT }
  public enum InputMode { MANUAL, AUTO }
  public enum RejectReason { ACTOR_DEAD, TARGET_DEAD, NOT_LEARNED, COOLDOWN, RESOURCE, RANGE, LOS, ACTION_BUSY }
  public enum EventType { ACTION_STARTED, ACTION_REJECTED, EFFECT_APPLIED, HIT_FEEDBACK, MONSTER_DEFEATED }
  public enum DefeatPublication { RESOLVER_OWNS, PORT_ALREADY_PUBLISHED }

  public static final class Definition {
    public final String actionId;
    public final ActionKind kind;
    public final ActionState state;
    public final EffectType effectType;
    public final boolean requiresLearned;
    public final int resourceCost,damage;
    public final float cooldown,range,hitTime;
    public Definition(String actionId,ActionKind kind,ActionState state,EffectType effectType,boolean requiresLearned,
        int resourceCost,float cooldown,float range,float hitTime,int damage){
      if(actionId==null||actionId.isEmpty()||kind==null||state==null||effectType==null)throw new IllegalArgumentException("action definition");
      if(resourceCost<0||cooldown<0f||range<0f||hitTime<0f||damage<0)throw new IllegalArgumentException("negative action value");
      this.actionId=actionId;this.kind=kind;this.state=state;this.effectType=effectType;this.requiresLearned=requiresLearned;
      this.resourceCost=resourceCost;this.cooldown=cooldown;this.range=range;this.hitTime=hitTime;this.damage=damage;
    }
  }

  public static final class EffectResult {
    public final int appliedAmount;
    public final boolean defeatedNow;
    public final DefeatPublication defeatPublication;
    public EffectResult(int appliedAmount,boolean defeatedNow){
      this(appliedAmount,defeatedNow,DefeatPublication.RESOLVER_OWNS);
    }
    public EffectResult(int appliedAmount,boolean defeatedNow,DefeatPublication defeatPublication){
      this.appliedAmount=Math.max(0,appliedAmount);this.defeatedNow=defeatedNow;
      this.defeatPublication=defeatPublication==null?DefeatPublication.RESOLVER_OWNS:defeatPublication;
    }
  }

  /** Adapter boundary. RPG/world/UI owners provide facts; resolver owns validation ordering and effect timing. */
  public interface Port {
    boolean actorAlive(String actorId);
    boolean targetAlive(String targetId);
    boolean learned(String actorId,String actionId);
    boolean cooldownReady(String actorId,String actionId);
    boolean hasResource(String actorId,int amount);
    float distance(String actorId,String targetId);
    boolean hasLineOfSight(String actorId,String targetId);
    void consumeResource(String actorId,int amount);
    void commitCooldown(String actorId,String actionId,float cooldown);
    EffectResult applyDamage(String actorId,String targetId,String actionId,int amount);
  }

  public static final class Event {
    public final long sequence,actionSequence;
    public final EventType type;
    public final String actorId,targetId,actionId;
    public final ActionState state;
    public final EffectType effectType;
    public final InputMode inputMode;
    public final RejectReason rejectReason;
    public final int amount;
    private Event(long sequence,long actionSequence,EventType type,String actorId,String targetId,String actionId,
        ActionState state,EffectType effectType,InputMode inputMode,RejectReason rejectReason,int amount){
      this.sequence=sequence;this.actionSequence=actionSequence;this.type=type;this.actorId=actorId;this.targetId=targetId;this.actionId=actionId;
      this.state=state;this.effectType=effectType;this.inputMode=inputMode;this.rejectReason=rejectReason;this.amount=amount;
    }
  }

  public static final class BeginResult {
    public final boolean accepted;
    public final long actionSequence;
    public final RejectReason rejectReason;
    private BeginResult(boolean accepted,long actionSequence,RejectReason rejectReason){this.accepted=accepted;this.actionSequence=actionSequence;this.rejectReason=rejectReason;}
  }

  private static final class PendingAction {
    final long sequence;
    final Definition def;
    final String actorId,targetId;
    final InputMode inputMode;
    float elapsed;
    boolean effectApplied;
    PendingAction(long sequence,Definition def,String actorId,String targetId,InputMode inputMode){
      this.sequence=sequence;this.def=def;this.actorId=actorId;this.targetId=targetId;this.inputMode=inputMode;
    }
  }

  private static final int MAX_EVENTS=128;
  private final Port port;
  private final Deque<Event> events=new ArrayDeque<>();
  private final Set<String> defeatPublishedForLife=new HashSet<>();
  private long eventSequence=0,actionSequence=0;
  private PendingAction active;

  public CombatResolver(Port port){if(port==null)throw new IllegalArgumentException("port");this.port=port;}

  public BeginResult begin(Definition def,String actorId,String targetId,InputMode inputMode){
    if(def==null||actorId==null||targetId==null)throw new IllegalArgumentException("action request");
    InputMode mode=inputMode==null?InputMode.MANUAL:inputMode;
    RejectReason reason=validate(def,actorId,targetId,true);
    if(reason==null&&active!=null)reason=RejectReason.ACTION_BUSY;
    if(reason!=null){emit(EventType.ACTION_REJECTED,0,actorId,targetId,def,mode,reason,0);return new BeginResult(false,0,reason);}
    if(def.resourceCost>0)port.consumeResource(actorId,def.resourceCost);
    port.commitCooldown(actorId,def.actionId,def.cooldown);
    long seq=++actionSequence;
    active=new PendingAction(seq,def,actorId,targetId,mode);
    emit(EventType.ACTION_STARTED,seq,actorId,targetId,def,mode,null,0);
    return new BeginResult(true,seq,null);
  }

  /** Applies the accepted effect at most once, only when hitTime has elapsed. */
  public void tick(float dt){
    if(active==null||dt<=0f)return;
    PendingAction a=active;
    a.elapsed+=dt;
    if(a.effectApplied||a.elapsed<a.def.hitTime)return;
    RejectReason effectGate=validate(a.def,a.actorId,a.targetId,false);
    if(effectGate!=null){emit(EventType.ACTION_REJECTED,a.sequence,a.actorId,a.targetId,a.def,a.inputMode,effectGate,0);active=null;return;}
    a.effectApplied=true;
    EffectResult result=port.applyDamage(a.actorId,a.targetId,a.def.actionId,a.def.damage);
    emit(EventType.EFFECT_APPLIED,a.sequence,a.actorId,a.targetId,a.def,a.inputMode,null,result.appliedAmount);
    emit(EventType.HIT_FEEDBACK,a.sequence,a.actorId,a.targetId,a.def,a.inputMode,null,result.appliedAmount);
    if(result.defeatedNow&&result.defeatPublication==DefeatPublication.RESOLVER_OWNS&&defeatPublishedForLife.add(a.targetId)){
      emit(EventType.MONSTER_DEFEATED,a.sequence,a.actorId,a.targetId,a.def,a.inputMode,null,0);
    }
    active=null;
  }

  /** Call when the owning monster runtime respawns/reincarnates a target so a future death may publish once again. */
  public void onTargetRespawned(String targetId){if(targetId!=null)defeatPublishedForLife.remove(targetId);}
  public boolean actionActive(){return active!=null;}
  public long activeActionSequence(){return active==null?0:active.sequence;}

  public List<Event> events(){return Collections.unmodifiableList(new ArrayList<>(events));}
  public List<Event> drainEvents(){List<Event> out=new ArrayList<>(events);events.clear();return Collections.unmodifiableList(out);}

  private RejectReason validate(Definition def,String actorId,String targetId,boolean includeCommitGates){
    if(!port.actorAlive(actorId))return RejectReason.ACTOR_DEAD;
    if(!port.targetAlive(targetId))return RejectReason.TARGET_DEAD;
    if(def.requiresLearned&&!port.learned(actorId,def.actionId))return RejectReason.NOT_LEARNED;
    if(includeCommitGates&&!port.cooldownReady(actorId,def.actionId))return RejectReason.COOLDOWN;
    if(includeCommitGates&&def.resourceCost>0&&!port.hasResource(actorId,def.resourceCost))return RejectReason.RESOURCE;
    if(port.distance(actorId,targetId)>def.range)return RejectReason.RANGE;
    if(!port.hasLineOfSight(actorId,targetId))return RejectReason.LOS;
    return null;
  }

  private void emit(EventType type,long actionSeq,String actorId,String targetId,Definition def,InputMode mode,RejectReason reject,int amount){
    events.addLast(new Event(++eventSequence,actionSeq,type,actorId,targetId,def.actionId,def.state,def.effectType,mode,reject,amount));
    while(events.size()>MAX_EVENTS)events.removeFirst();
  }

  public static Definition attackPrototype(AttackDef def,int modeIndex){
    if(def==null)throw new IllegalArgumentException("def");
    return new Definition("attack_proto_"+modeIndex+"_"+def.kind.name().toLowerCase(),ActionKind.ATTACK,ActionState.ATTACK,
        EffectType.PHYSICAL_HIT,false,0,def.cooldown,def.range,Math.min(.18f,def.cooldown*.5f),def.damage);
  }
  public static Definition magicPrototype(){SkillDef d=SkillDef.CAST_PROTO;return new Definition(d.id,ActionKind.MAGIC,ActionState.MAGIC,EffectType.MAGIC_HIT,true,d.mpCost,d.cooldown,d.range,.24f,d.damage);}
  public static Definition skillPrototype(){SkillDef d=SkillDef.SKILL_PROTO;return new Definition(d.id,ActionKind.SKILL,ActionState.SKILL,EffectType.SKILL_HIT,true,d.mpCost,d.cooldown,d.range,.18f,d.damage);}
  public static Definition kickPrototype(){SkillDef d=SkillDef.KICK_PROTO;return new Definition(d.id,ActionKind.KICK,ActionState.KICK,EffectType.KICK_HIT,true,d.mpCost,d.cooldown,d.range,.14f,d.damage);}
}
