package com.projectdark.mobile;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Combat-owned shared MANUAL/AUTO action resolver. */
public final class CombatResolver {
  public enum ActionKind { ATTACK, SKILL, MAGIC, KICK }
  public enum ActionState { ATTACK, SKILL, MAGIC, KICK }
  public enum EffectType { PHYSICAL_HIT, SKILL_HIT, MAGIC_HIT, KICK_HIT }
  public enum InputMode { MANUAL, AUTO }
  public enum RejectReason { ACTOR_DEAD, TARGET_DEAD, NOT_LEARNED, COOLDOWN, RESOURCE, RANGE, LOS, ACTION_BUSY }
  public enum EventType { ACTION_STARTED, ACTION_REJECTED, ACTION_CANCELLED, EFFECT_APPLIED, HIT_FEEDBACK, MONSTER_DEFEATED }
  public enum DefeatPublication { RESOLVER_OWNS, PORT_ALREADY_PUBLISHED }
  public enum DefeatedTargetKind { MONSTER, PLAYER, OTHER }
  public enum HitSemantic { DAMAGE, CRIT, MISS, HEAL }

  public static final class Definition {
    public final String actionId; public final ActionKind kind; public final ActionState state; public final EffectType effectType;
    public final boolean requiresLearned; public final int resourceCost,damage; public final float cooldown,range,hitTime;
    public Definition(String id,ActionKind k,ActionState s,EffectType e,boolean l,int r,float c,float range,float h,int d){
      if(id==null||id.isEmpty()||k==null||s==null||e==null||r<0||c<0||range<0||h<0||d<0)throw new IllegalArgumentException("action definition");
      actionId=id;kind=k;state=s;effectType=e;requiresLearned=l;resourceCost=r;cooldown=c;this.range=range;hitTime=h;damage=d;
    }
  }
  public static final class EffectResult {
    public final int appliedAmount; public final boolean defeatedNow; public final DefeatPublication defeatPublication;
    public final DefeatedTargetKind defeatedTargetKind; public final HitSemantic hitSemantic;
    public EffectResult(int a,boolean d,DefeatPublication p,DefeatedTargetKind k,HitSemantic h){appliedAmount=Math.max(0,a);defeatedNow=d;defeatPublication=p==null?DefeatPublication.RESOLVER_OWNS:p;defeatedTargetKind=k==null?DefeatedTargetKind.OTHER:k;hitSemantic=h==null?HitSemantic.DAMAGE:h;}
    public EffectResult(int a,boolean d){this(a,d,DefeatPublication.RESOLVER_OWNS,DefeatedTargetKind.MONSTER,HitSemantic.DAMAGE);}
  }
  public interface Port { boolean actorAlive(String id); boolean targetAlive(String id); boolean learned(String a,String id); boolean cooldownReady(String a,String id); boolean hasResource(String a,int n); float distance(String a,String b); boolean hasLineOfSight(String a,String b); void consumeResource(String a,int n); void commitCooldown(String a,String id,float c); EffectResult applyDamage(String a,String b,String id,int n); }
  public static final class Event {
    public final long sequence,actionSequence; public final EventType type; public final String actorId,targetId,actionId; public final ActionState state; public final EffectType effectType; public final InputMode inputMode; public final RejectReason rejectReason; public final HitSemantic hitSemantic; public final int amount;
    Event(long s,long as,EventType t,String a,String target,Definition d,InputMode m,RejectReason r,HitSemantic h,int n){sequence=s;actionSequence=as;type=t;actorId=a;targetId=target;actionId=d.actionId;state=d.state;effectType=d.effectType;inputMode=m;rejectReason=r;hitSemantic=h;amount=n;}
  }
  public static final class BeginResult { public final boolean accepted; public final long actionSequence; public final RejectReason rejectReason; BeginResult(boolean a,long s,RejectReason r){accepted=a;actionSequence=s;rejectReason=r;} }
  public static final class ActionSnapshot {
    public final long actionSequence; public final String actorId,targetId,actionId; public final ActionState state; public final EffectType effectType; public final InputMode inputMode; public final float elapsed,hitTime,progress;
    ActionSnapshot(Pending p){actionSequence=p.seq;actorId=p.a;targetId=p.t;actionId=p.d.actionId;state=p.d.state;effectType=p.d.effectType;inputMode=p.m;elapsed=p.elapsed;hitTime=p.d.hitTime;progress=p.d.hitTime<=0?1f:Math.max(0f,Math.min(1f,p.elapsed/p.d.hitTime));}
  }
  private static final class Pending { final long seq; final Definition d; final String a,t; final InputMode m; float elapsed; Pending(long s,Definition d,String a,String t,InputMode m){seq=s;this.d=d;this.a=a;this.t=t;this.m=m;} }

  private final Port port; private final Deque<Event> events=new ArrayDeque<>(); private final Set<String> defeated=new HashSet<>();
  private final Map<String,Pending> activeByActor=new LinkedHashMap<>(); private long es,as;
  public CombatResolver(Port p){if(p==null)throw new IllegalArgumentException("port");port=p;}

  public BeginResult begin(Definition d,String a,String t,InputMode m){
    if(d==null||a==null||t==null)throw new IllegalArgumentException("action request");
    InputMode mode=m==null?InputMode.MANUAL:m; RejectReason r=validate(d,a,t,true);
    if(r==null&&activeByActor.containsKey(a))r=RejectReason.ACTION_BUSY;
    if(r!=null){emit(EventType.ACTION_REJECTED,0,a,t,d,mode,r,null,0);return new BeginResult(false,0,r);}
    if(d.resourceCost>0)port.consumeResource(a,d.resourceCost); port.commitCooldown(a,d.actionId,d.cooldown);
    Pending p=new Pending(++as,d,a,t,mode); activeByActor.put(a,p); emit(EventType.ACTION_STARTED,p.seq,a,t,d,mode,null,null,0); return new BeginResult(true,p.seq,null);
  }

  public void tick(float dt){
    if(activeByActor.isEmpty()||dt<=0)return;
    List<Pending> pending=new ArrayList<>(activeByActor.values());
    for(Pending p:pending){
      if(activeByActor.get(p.a)!=p)continue;
      p.elapsed+=dt; if(p.elapsed<p.d.hitTime)continue;
      RejectReason r=validate(p.d,p.a,p.t,false);
      if(r!=null){cancel(p,r);continue;}
      EffectResult x=port.applyDamage(p.a,p.t,p.d.actionId,p.d.damage);
      emit(EventType.EFFECT_APPLIED,p.seq,p.a,p.t,p.d,p.m,null,x.hitSemantic,x.appliedAmount);
      emit(EventType.HIT_FEEDBACK,p.seq,p.a,p.t,p.d,p.m,null,x.hitSemantic,x.appliedAmount);
      activeByActor.remove(p.a);
      if(x.defeatedNow){
        interruptDeath(p.t,p.a);
        if(x.defeatedTargetKind==DefeatedTargetKind.MONSTER&&x.defeatPublication==DefeatPublication.RESOLVER_OWNS&&defeated.add(p.t))
          emit(EventType.MONSTER_DEFEATED,p.seq,p.a,p.t,p.d,p.m,null,x.hitSemantic,0);
      }
    }
  }

  private void interruptDeath(String deadId,String sourceActor){
    List<Pending> pending=new ArrayList<>(activeByActor.values());
    for(Pending q:pending){
      if(q.a.equals(sourceActor))continue;
      if(q.a.equals(deadId))cancel(q,RejectReason.ACTOR_DEAD);
      else if(q.t.equals(deadId))cancel(q,RejectReason.TARGET_DEAD);
    }
  }
  private void cancel(Pending p,RejectReason reason){emit(EventType.ACTION_CANCELLED,p.seq,p.a,p.t,p.d,p.m,reason,null,0);activeByActor.remove(p.a);}

  public void onTargetRespawned(String id){if(id!=null)defeated.remove(id);} public boolean actionActive(){return !activeByActor.isEmpty();}
  public boolean actionActive(String actorId){return actorId!=null&&activeByActor.containsKey(actorId);} public int activeActionCount(){return activeByActor.size();}
  public List<ActionSnapshot> actionSnapshots(){List<ActionSnapshot> out=new ArrayList<>();for(Pending p:activeByActor.values())out.add(new ActionSnapshot(p));return Collections.unmodifiableList(out);}
  public List<Event> drainEvents(){List<Event> x=new ArrayList<>(events);events.clear();return Collections.unmodifiableList(x);} public List<Event> events(){return Collections.unmodifiableList(new ArrayList<>(events));}

  private RejectReason validate(Definition d,String a,String t,boolean commit){if(!port.actorAlive(a))return RejectReason.ACTOR_DEAD;if(!port.targetAlive(t))return RejectReason.TARGET_DEAD;if(d.requiresLearned&&!port.learned(a,d.actionId))return RejectReason.NOT_LEARNED;if(commit&&!port.cooldownReady(a,d.actionId))return RejectReason.COOLDOWN;if(commit&&d.resourceCost>0&&!port.hasResource(a,d.resourceCost))return RejectReason.RESOURCE;if(port.distance(a,t)>d.range)return RejectReason.RANGE;if(!port.hasLineOfSight(a,t))return RejectReason.LOS;return null;}
  private void emit(EventType t,long seq,String a,String target,Definition d,InputMode m,RejectReason r,HitSemantic h,int n){events.add(new Event(++es,seq,t,a,target,d,m==null?InputMode.MANUAL:m,r,h,n));while(events.size()>128)events.removeFirst();}

  public static Definition attackPrototype(AttackDef d,int i){return new Definition("attack_proto_"+i+"_"+d.kind.name().toLowerCase(),ActionKind.ATTACK,ActionState.ATTACK,EffectType.PHYSICAL_HIT,false,0,d.cooldown,d.range,Math.min(.18f,d.cooldown*.5f),d.damage);}
  public static Definition magicPrototype(){SkillDef d=SkillDef.CAST_PROTO;return new Definition(d.id,ActionKind.MAGIC,ActionState.MAGIC,EffectType.MAGIC_HIT,true,d.mpCost,d.cooldown,d.range,.24f,d.damage);} public static Definition skillPrototype(){SkillDef d=SkillDef.SKILL_PROTO;return new Definition(d.id,ActionKind.SKILL,ActionState.SKILL,EffectType.SKILL_HIT,true,d.mpCost,d.cooldown,d.range,.18f,d.damage);} public static Definition kickPrototype(){SkillDef d=SkillDef.KICK_PROTO;return new Definition(d.id,ActionKind.KICK,ActionState.KICK,EffectType.KICK_HIT,true,d.mpCost,d.cooldown,d.range,.14f,d.damage);}
}
