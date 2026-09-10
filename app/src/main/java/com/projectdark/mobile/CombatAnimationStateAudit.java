package com.projectdark.mobile;

import java.util.Arrays;
import java.util.List;

/** Regression audit for Director D002 combat animation acceptance contract. */
public final class CombatAnimationStateAudit {
  private static final class Port implements CombatResolver.Port {
    int hp=50;
    public boolean actorAlive(String id){return true;}
    public boolean targetAlive(String id){return hp>0;}
    public boolean learned(String a,String id){return true;}
    public boolean cooldownReady(String a,String id){return true;}
    public boolean hasResource(String a,int n){return true;}
    public float distance(String a,String b){return 10f;}
    public boolean hasLineOfSight(String a,String b){return true;}
    public void consumeResource(String a,int n){}
    public void commitCooldown(String a,String id,float c){}
    public CombatResolver.EffectResult applyDamage(String a,String b,String id,int n){int before=hp;hp=Math.max(0,hp-n);return new CombatResolver.EffectResult(before-hp,before>0&&hp==0,CombatResolver.DefeatPublication.RESOLVER_OWNS,CombatResolver.DefeatedTargetKind.MONSTER,CombatResolver.HitSemantic.DAMAGE);}
  }

  public static boolean verify(){
    Port port=new Port();CombatResolver resolver=new CombatResolver(port);
    CombatAnimationStateModel model=new CombatAnimationStateModel();
    CombatResolver.Definition attack=new CombatResolver.Definition("attack",CombatResolver.ActionKind.ATTACK,CombatResolver.ActionState.ATTACK,CombatResolver.EffectType.PHYSICAL_HIT,false,0,0,50,.2f,5);
    CombatResolver.Definition cast=new CombatResolver.Definition("cast",CombatResolver.ActionKind.MAGIC,CombatResolver.ActionState.MAGIC,CombatResolver.EffectType.MAGIC_HIT,true,0,0,50,.2f,5);
    CombatResolver.Definition kick=new CombatResolver.Definition("kick",CombatResolver.ActionKind.KICK,CombatResolver.ActionState.KICK,CombatResolver.EffectType.KICK_HIT,true,0,0,50,.2f,5);

    if(!resolver.begin(attack,"player","monster",CombatResolver.InputMode.MANUAL).accepted)return false;
    List<CombatResolver.Event> events=resolver.drainEvents();model.consume(events);model.syncActions(resolver.actionSnapshots());
    if(model.snapshot("player").state!=CombatAnimationStateModel.VisualState.ATTACK)return false;
    resolver.tick(.1f);model.syncActions(resolver.actionSnapshots());
    if(model.snapshot("player").progress<=0f||model.snapshot("player").progress>=1f)return false;
    resolver.tick(.1f);events=resolver.drainEvents();model.consume(events);model.syncActions(resolver.actionSnapshots());
    if(model.snapshot("player").state!=CombatAnimationStateModel.VisualState.IDLE||port.hp!=45)return false;

    if(!resolver.begin(cast,"player","monster",CombatResolver.InputMode.MANUAL).accepted)return false;
    model.consume(resolver.drainEvents());model.syncActions(resolver.actionSnapshots());
    if(model.snapshot("player").state!=CombatAnimationStateModel.VisualState.CAST)return false;
    resolver.tick(.2f);model.consume(resolver.drainEvents());model.syncActions(resolver.actionSnapshots());

    if(!resolver.begin(kick,"player","monster",CombatResolver.InputMode.MANUAL).accepted)return false;
    model.consume(resolver.drainEvents());model.syncActions(resolver.actionSnapshots());
    if(model.snapshot("player").state!=CombatAnimationStateModel.VisualState.SKILL_KICK)return false;
    resolver.tick(.2f);model.consume(resolver.drainEvents());model.syncActions(resolver.actionSnapshots());
    model.setLocomotion("player",true);
    return model.snapshot("player").state==CombatAnimationStateModel.VisualState.WALK&&port.hp==35;
  }

  public static void main(String[] args){if(!verify())throw new AssertionError("CombatAnimationStateAudit failed");System.out.println("CombatAnimationStateAudit PASS");}
}
