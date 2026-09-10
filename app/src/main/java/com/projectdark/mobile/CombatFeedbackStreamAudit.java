package com.projectdark.mobile;

import java.util.List;

/** Deterministic audit for resolver event -> renderer feedback -> damage-number lifecycle. */
public final class CombatFeedbackStreamAudit {
  private CombatFeedbackStreamAudit(){}

  private static final class Port implements CombatResolver.Port {
    CombatResolver.HitSemantic semantic=CombatResolver.HitSemantic.DAMAGE;
    int hp=20;
    public boolean actorAlive(String id){return true;}
    public boolean targetAlive(String id){return hp>0;}
    public boolean learned(String a,String b){return true;}
    public boolean cooldownReady(String a,String b){return true;}
    public boolean hasResource(String a,int n){return true;}
    public float distance(String a,String b){return 1f;}
    public boolean hasLineOfSight(String a,String b){return true;}
    public void consumeResource(String a,int n){}
    public void commitCooldown(String a,String b,float n){}
    public CombatResolver.EffectResult applyDamage(String a,String b,String c,int n){
      if(semantic==CombatResolver.HitSemantic.MISS)return new CombatResolver.EffectResult(0,false,CombatResolver.DefeatPublication.RESOLVER_OWNS,CombatResolver.DefeatedTargetKind.MONSTER,semantic);
      int before=hp;hp=Math.max(0,hp-n);return new CombatResolver.EffectResult(before-hp,before>0&&hp==0,CombatResolver.DefeatPublication.RESOLVER_OWNS,CombatResolver.DefeatedTargetKind.MONSTER,semantic);
    }
  }

  public static boolean verify(){
    CombatResolver.Definition attack=new CombatResolver.Definition("attack",CombatResolver.ActionKind.ATTACK,
        CombatResolver.ActionState.ATTACK,CombatResolver.EffectType.PHYSICAL_HIT,false,0,0f,10f,.1f,5);
    Port port=new Port();CombatResolver resolver=new CombatResolver(port);CombatFeedbackStream feedback=new CombatFeedbackStream();
    CombatFeedbackStream.AnchorPort anchors=new CombatFeedbackStream.AnchorPort(){public float anchorX(String id){return 12f;}public float anchorY(String id){return 30f;}};

    resolver.begin(attack,"player","monster",CombatResolver.InputMode.MANUAL);resolver.tick(.1f);
    feedback.consume(resolver.drainEvents(),anchors);
    if(feedback.damageNumberCount()!=1)return false;
    List<DamageNumberModel.Snapshot> numbers=feedback.damageNumbers();
    if(numbers.get(0).type!=DamageNumberModel.SemanticType.DAMAGE||!"-5".equals(numbers.get(0).displayText()))return false;
    if(!has(feedback.events(),CombatFeedbackStream.Type.ACTION_STARTED)||!has(feedback.events(),CombatFeedbackStream.Type.HIT))return false;

    port.semantic=CombatResolver.HitSemantic.CRIT;resolver.begin(attack,"player","monster",CombatResolver.InputMode.AUTO);resolver.tick(.1f);
    feedback.consume(resolver.drainEvents(),anchors);
    if(!has(feedback.events(),CombatFeedbackStream.Type.CRIT))return false;

    port.semantic=CombatResolver.HitSemantic.MISS;resolver.begin(attack,"player","monster",CombatResolver.InputMode.MANUAL);resolver.tick(.1f);
    feedback.consume(resolver.drainEvents(),anchors);
    if(!has(feedback.events(),CombatFeedbackStream.Type.MISS))return false;
    numbers=feedback.damageNumbers();if(numbers.get(numbers.size()-1).type!=DamageNumberModel.SemanticType.MISS||!"MISS".equals(numbers.get(numbers.size()-1).displayText()))return false;

    feedback.tick(1f);return feedback.damageNumberCount()==0;
  }

  private static boolean has(List<CombatFeedbackStream.Event> events,CombatFeedbackStream.Type type){for(CombatFeedbackStream.Event e:events)if(e.type==type)return true;return false;}
  public static void main(String[] args){if(!verify())throw new AssertionError("CombatFeedbackStreamAudit failed");System.out.println("CombatFeedbackStreamAudit PASS");}
}
