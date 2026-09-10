package com.projectdark.mobile;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Deterministic regression audit for action identity, validation, hit timing and feedback lifecycle. */
public final class CombatResolverAudit {
  private CombatResolverAudit(){}

  private static final class FakePort implements CombatResolver.Port {
    boolean actorAlive=true,targetAlive=true,los=true;
    float distance=20f;
    int resource=100,hp=30,damageCalls=0;
    final Set<String> learned=new HashSet<>();
    final Map<String,Float> cooldowns=new HashMap<>();
    public boolean actorAlive(String actorId){return actorAlive;}
    public boolean targetAlive(String targetId){return targetAlive;}
    public boolean learned(String actorId,String actionId){return learned.contains(actionId);}
    public boolean cooldownReady(String actorId,String actionId){Float v=cooldowns.get(actionId);return v==null||v<=0f;}
    public boolean hasResource(String actorId,int amount){return resource>=amount;}
    public float distance(String actorId,String targetId){return distance;}
    public boolean hasLineOfSight(String actorId,String targetId){return los;}
    public void consumeResource(String actorId,int amount){resource-=amount;}
    public void commitCooldown(String actorId,String actionId,float cooldown){cooldowns.put(actionId,cooldown);}
    public CombatResolver.EffectResult applyDamage(String actorId,String targetId,String actionId,int amount){
      damageCalls++;int before=hp;hp=Math.max(0,hp-amount);targetAlive=hp>0;return new CombatResolver.EffectResult(before-hp,before>0&&hp==0);
    }
  }

  public static boolean verify(){
    CombatResolver.Definition attack=new CombatResolver.Definition("attack_a",CombatResolver.ActionKind.ATTACK,CombatResolver.ActionState.ATTACK,
        CombatResolver.EffectType.PHYSICAL_HIT,false,0,.4f,80f,.2f,8);
    CombatResolver.Definition skill=new CombatResolver.Definition("skill_a",CombatResolver.ActionKind.SKILL,CombatResolver.ActionState.SKILL,
        CombatResolver.EffectType.SKILL_HIT,true,0,1f,90f,.15f,10);
    CombatResolver.Definition magic=new CombatResolver.Definition("magic_a",CombatResolver.ActionKind.MAGIC,CombatResolver.ActionState.MAGIC,
        CombatResolver.EffectType.MAGIC_HIT,true,8,1.2f,150f,.24f,12);
    if(attack.actionId.equals(skill.actionId)||skill.actionId.equals(magic.actionId)||attack.state==skill.state||skill.state==magic.state)return false;

    FakePort p=new FakePort();p.learned.add("skill_a");p.learned.add("magic_a");
    CombatResolver r=new CombatResolver(p);
    CombatResolver.BeginResult manual=r.begin(magic,"player","monster",CombatResolver.InputMode.MANUAL);
    if(!manual.accepted||p.resource!=92||p.damageCalls!=0)return false;
    r.tick(.12f);if(p.damageCalls!=0)return false;
    r.tick(.12f);if(p.damageCalls!=1||p.hp!=18)return false;
    r.tick(1f);if(p.damageCalls!=1)return false;
    List<CombatResolver.Event> first=r.drainEvents();
    if(!has(first,CombatResolver.EventType.ACTION_STARTED,"magic_a",CombatResolver.ActionState.MAGIC,CombatResolver.EffectType.MAGIC_HIT,CombatResolver.InputMode.MANUAL))return false;
    if(!has(first,CombatResolver.EventType.EFFECT_APPLIED,"magic_a",CombatResolver.ActionState.MAGIC,CombatResolver.EffectType.MAGIC_HIT,CombatResolver.InputMode.MANUAL))return false;

    p.cooldowns.put("skill_a",0f);
    CombatResolver.BeginResult auto=r.begin(skill,"player","monster",CombatResolver.InputMode.AUTO);
    if(!auto.accepted)return false;r.tick(.15f);if(p.damageCalls!=2||p.hp!=8)return false;
    if(!has(r.drainEvents(),CombatResolver.EventType.EFFECT_APPLIED,"skill_a",CombatResolver.ActionState.SKILL,CombatResolver.EffectType.SKILL_HIT,CombatResolver.InputMode.AUTO))return false;

    p.cooldowns.put("attack_a",0f);
    if(!r.begin(attack,"player","monster",CombatResolver.InputMode.MANUAL).accepted)return false;r.tick(.2f);
    if(p.hp!=0||p.damageCalls!=3)return false;
    List<CombatResolver.Event> death=r.drainEvents();
    if(count(death,CombatResolver.EventType.MONSTER_DEFEATED)!=1)return false;
    if(r.begin(attack,"player","monster",CombatResolver.InputMode.AUTO).accepted)return false;
    if(count(r.drainEvents(),CombatResolver.EventType.MONSTER_DEFEATED)!=0)return false;

    // Validation gates fail closed before resource/effect mutation.
    FakePort q=new FakePort();CombatResolver rq=new CombatResolver(q);
    if(rq.begin(magic,"p","m",CombatResolver.InputMode.MANUAL).rejectReason!=CombatResolver.RejectReason.NOT_LEARNED)return false;
    q.learned.add("magic_a");q.resource=2;
    if(rq.begin(magic,"p","m",CombatResolver.InputMode.MANUAL).rejectReason!=CombatResolver.RejectReason.RESOURCE)return false;
    q.resource=100;q.distance=999f;
    if(rq.begin(magic,"p","m",CombatResolver.InputMode.MANUAL).rejectReason!=CombatResolver.RejectReason.RANGE)return false;
    q.distance=20f;q.los=false;
    if(rq.begin(magic,"p","m",CombatResolver.InputMode.MANUAL).rejectReason!=CombatResolver.RejectReason.LOS)return false;
    if(q.damageCalls!=0||q.resource!=100)return false;

    DamageNumberModel numbers=new DamageNumberModel();
    numbers.spawn("m",DamageNumberModel.SemanticType.DAMAGE,8,10,10);
    numbers.spawn("m",DamageNumberModel.SemanticType.CRIT,16,10,10);
    numbers.spawn("m",DamageNumberModel.SemanticType.MISS,99,10,10);
    numbers.spawn("p",DamageNumberModel.SemanticType.HEAL,7,10,10);
    if(numbers.size()!=4)return false;
    List<DamageNumberModel.Snapshot> snapshots=numbers.snapshot();
    if(!"-8".equals(snapshots.get(0).displayText())||!"MISS".equals(snapshots.get(2).displayText())||!"+7".equals(snapshots.get(3).displayText()))return false;
    numbers.tick(.5f);snapshots=numbers.snapshot();if(snapshots.isEmpty()||snapshots.get(0).offsetY>=0f||snapshots.get(0).alpha>=1f)return false;
    numbers.tick(1f);return numbers.size()==0;
  }

  private static boolean has(List<CombatResolver.Event> events,CombatResolver.EventType type,String id,CombatResolver.ActionState state,
      CombatResolver.EffectType effect,CombatResolver.InputMode mode){for(CombatResolver.Event e:events)if(e.type==type&&id.equals(e.actionId)&&e.state==state&&e.effectType==effect&&e.inputMode==mode)return true;return false;}
  private static int count(List<CombatResolver.Event> events,CombatResolver.EventType type){int n=0;for(CombatResolver.Event e:events)if(e.type==type)n++;return n;}
  public static void main(String[] args){if(!verify())throw new IllegalStateException("Combat resolver audit failed");}
}
