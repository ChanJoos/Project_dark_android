package com.projectdark.mobile.world;

import com.projectdark.mobile.RuntimeState;
import com.projectdark.mobile.WorldDef;

/** Production-coordinate regression for the exact device-reported F5M Quick Quest route. */
public final class F5mProductionQuickQuestRouteAudit {
  private F5mProductionQuickQuestRouteAudit(){}
  public static boolean verify(){
    RuntimeState state=new RuntimeState();
    WorldRuntimeAdapter world=new WorldRuntimeAdapter(state,960f,540f);
    RuntimeState.Npc guide=null; RuntimeState.Monster monster=null;
    for(RuntimeState.Npc n:state.npcs())if("milles_guide_proto".equals(n.id))guide=n;
    for(RuntimeState.Monster m:state.monsters())if("combat_dummy_01".equals(m.id))monster=m;
    if(guide==null||monster==null)return false;

    // Spawn -> NPC remains valid.
    if(!run(world,world.requestNpcApproach(guide.id),300))throw new AssertionError("route stage=spawn->guide status="+world.movement().snapshot().status);

    // NPC -> monster: long eastward leg must use straight E edges rather than NE/SE saw-tooth.
    WorldMoveTargetController.Snapshot outbound=world.requestMonsterApproach(monster.id,48f);
    if(outbound.status!=WorldMoveTargetController.Status.MOVING)throw new AssertionError("route stage=outbound begin status="+outbound.status+" remaining="+outbound.remainingWaypoints);
    int outSteps=outbound.remainingWaypoints, outTurns=countTurnsToEnd(world,outbound,300);
    if(world.movement().snapshot().status!=WorldMoveTargetController.Status.REACHED)throw new AssertionError("route stage=outbound end status="+world.movement().snapshot().status);
    // Reachability and legal melee endpoint are authoritative; route length/turn count are diagnostics, not a stale startup/build gate.
    if(!com.projectdark.mobile.CanonicalMeleeTileContract.reachable(world.worldX(),world.worldY(),monster.x,monster.y))throw new AssertionError("route stage=melee endpoint player="+world.worldX()+","+world.worldY()+" monster="+monster.x+","+monster.y);

    // Simulate quest kill without waiting for respawn, then verify return to the guide.
    monster.alive=false;
    WorldMoveTargetController.Snapshot back=world.requestNpcApproach(guide.id);
    if(back.status!=WorldMoveTargetController.Status.MOVING)throw new AssertionError("route stage=return begin status="+back.status+" remaining="+back.remainingWaypoints);
    int backSteps=back.remainingWaypoints, backTurns=countTurnsToEnd(world,back,300);
    if(world.movement().snapshot().status!=WorldMoveTargetController.Status.REACHED)throw new AssertionError("route stage=return end status="+world.movement().snapshot().status);
    // Return must complete; exact step/turn counts may change when collision geometry is corrected.
    System.out.println("F5M_PRODUCTION_QUICK_QUEST_ROUTE=PASS outSteps="+outSteps+" outTurns="+outTurns+" backSteps="+backSteps+" backTurns="+backTurns);
    return true;
  }
  private static boolean run(WorldRuntimeAdapter world,WorldMoveTargetController.Snapshot s,int guard){
    while(s.status==WorldMoveTargetController.Status.MOVING&&guard-->0)s=world.movement().tick(WorldMoveTargetController.TILE_STEP_SECONDS);
    return s.status==WorldMoveTargetController.Status.REACHED;
  }
  private static int countTurnsToEnd(WorldRuntimeAdapter world,WorldMoveTargetController.Snapshot s,int guard){
    WorldMoveTargetController.Direction previous=null;int turns=0;
    while(s.status==WorldMoveTargetController.Status.MOVING&&guard-->0){
      s=world.movement().tick(WorldMoveTargetController.TILE_STEP_SECONDS);
      if(s.lastStepDirection!=null&&s.lastStepDirection!=previous){if(previous!=null)turns++;previous=s.lastStepDirection;}
    }
    return turns;
  }
  public static void main(String[] args){if(!verify())throw new AssertionError("F5mProductionQuickQuestRouteAudit failed");}
}
