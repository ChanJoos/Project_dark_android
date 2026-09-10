package com.projectdark.mobile;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Proves Resolver and legacy Port publication modes cannot both emit MONSTER_DEFEATED. */
public final class CombatDefeatAuthorityAudit {
  private CombatDefeatAuthorityAudit(){}
  private static final class Port implements CombatResolver.Port {
    boolean targetAlive=true;
    CombatResolver.DefeatPublication publication;
    Port(CombatResolver.DefeatPublication publication){this.publication=publication;}
    public boolean actorAlive(String id){return true;}
    public boolean targetAlive(String id){return targetAlive;}
    public boolean learned(String a,String b){return true;}
    public boolean cooldownReady(String a,String b){return true;}
    public boolean hasResource(String a,int n){return true;}
    public float distance(String a,String b){return 1f;}
    public boolean hasLineOfSight(String a,String b){return true;}
    public void consumeResource(String a,int n){}
    public void commitCooldown(String a,String b,float n){}
    public CombatResolver.EffectResult applyDamage(String a,String b,String c,int n){
      targetAlive=false;return new CombatResolver.EffectResult(n,true,publication);
    }
  }
  public static boolean verify(){
    CombatResolver.Definition attack=new CombatResolver.Definition("a",CombatResolver.ActionKind.ATTACK,
        CombatResolver.ActionState.ATTACK,CombatResolver.EffectType.PHYSICAL_HIT,false,0,0f,10f,0f,1);
    Port resolverPort=new Port(CombatResolver.DefeatPublication.RESOLVER_OWNS);
    CombatResolver resolver=new CombatResolver(resolverPort);
    resolver.begin(attack,"p","m",CombatResolver.InputMode.MANUAL);resolver.tick(.01f);
    if(count(resolver.drainEvents())!=1)return false;
    Port legacyPort=new Port(CombatResolver.DefeatPublication.PORT_ALREADY_PUBLISHED);
    CombatResolver adapted=new CombatResolver(legacyPort);
    adapted.begin(attack,"p","m",CombatResolver.InputMode.AUTO);adapted.tick(.01f);
    return count(adapted.drainEvents())==0;
  }
  private static int count(List<CombatResolver.Event> events){
    int count=0;for(CombatResolver.Event event:events)if(event.type==CombatResolver.EventType.MONSTER_DEFEATED)count++;return count;
  }
  public static void main(String[] args){
    if(!verify())throw new AssertionError("CombatDefeatAuthorityAudit failed");
    System.out.println("CombatDefeatAuthorityAudit PASS");
  }
}
