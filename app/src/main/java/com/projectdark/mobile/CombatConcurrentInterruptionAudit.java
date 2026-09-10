package com.projectdark.mobile;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Regression audit: actor-scoped concurrency, visible windup progress, and immediate death interruption. */
public final class CombatConcurrentInterruptionAudit {
  private static final class Port implements CombatResolver.Port {
    final Map<String,Integer> hp=new HashMap<>(); int effects;
    Port(){hp.put("player",100);hp.put("m1",6);hp.put("m2",20);}
    public boolean actorAlive(String id){return hp.getOrDefault(id,0)>0;} public boolean targetAlive(String id){return actorAlive(id);}
    public boolean learned(String a,String id){return true;} public boolean cooldownReady(String a,String id){return true;} public boolean hasResource(String a,int n){return true;}
    public float distance(String a,String b){return 10;} public boolean hasLineOfSight(String a,String b){return true;} public void consumeResource(String a,int n){} public void commitCooldown(String a,String id,float c){}
    public CombatResolver.EffectResult applyDamage(String a,String t,String id,int n){effects++;int before=hp.get(t),after=Math.max(0,before-n);hp.put(t,after);CombatResolver.DefeatedTargetKind k=t.startsWith("m")?CombatResolver.DefeatedTargetKind.MONSTER:CombatResolver.DefeatedTargetKind.PLAYER;return new CombatResolver.EffectResult(before-after,before>0&&after==0,CombatResolver.DefeatPublication.RESOLVER_OWNS,k,CombatResolver.HitSemantic.DAMAGE);}
  }
  public static boolean verify(){
    Port p=new Port(); CombatResolver r=new CombatResolver(p);
    CombatResolver.Definition fast=new CombatResolver.Definition("fast",CombatResolver.ActionKind.ATTACK,CombatResolver.ActionState.ATTACK,CombatResolver.EffectType.PHYSICAL_HIT,false,0,0,50,.1f,6);
    CombatResolver.Definition slow=new CombatResolver.Definition("slow",CombatResolver.ActionKind.ATTACK,CombatResolver.ActionState.ATTACK,CombatResolver.EffectType.PHYSICAL_HIT,false,0,0,50,.5f,4);
    CombatActionOrchestrator o=new CombatActionOrchestrator(r,Arrays.asList(fast,slow));
    if(!o.submitManual("player","m1","fast").accepted())return false;
    if(!o.submitAuto("m2","player","slow").accepted())return false;
    if(!o.submitAuto("m1","player","slow").accepted())return false;
    if(o.activeActionCount()!=3)return false;
    o.tick(.05f); List<CombatResolver.ActionSnapshot> snapshots=o.actionSnapshots();
    if(snapshots.size()!=3||snapshots.get(0).progress<=0||snapshots.get(0).progress>=1)return false;
    o.tick(.05f); // player kills m1 here; m1's unfinished attack must cancel immediately.
    if(p.hp.get("m1")!=0||o.activeActionCount()!=1)return false;
    List<CombatResolver.Event> events=o.drainEvents();
    if(count(events,CombatResolver.EventType.MONSTER_DEFEATED)!=1)return false;
    if(!hasCancel(events,"m1",CombatResolver.RejectReason.ACTOR_DEAD))return false;
    if(p.effects!=1)return false;
    o.tick(.4f); if(p.effects!=2||p.hp.get("player")!=96)return false;
    return o.activeActionCount()==0;
  }
  private static int count(List<CombatResolver.Event> es,CombatResolver.EventType t){int n=0;for(CombatResolver.Event e:es)if(e.type==t)n++;return n;}
  private static boolean hasCancel(List<CombatResolver.Event> es,String actor,CombatResolver.RejectReason r){for(CombatResolver.Event e:es)if(e.type==CombatResolver.EventType.ACTION_CANCELLED&&actor.equals(e.actorId)&&e.rejectReason==r)return true;return false;}
  public static void main(String[] args){if(!verify())throw new AssertionError("CombatConcurrentInterruptionAudit failed");System.out.println("CombatConcurrentInterruptionAudit PASS");}
}
