package com.projectdark.mobile;

import java.util.EnumSet;
import com.projectdark.mobile.world.WorldMoveTargetController;

/** Focused deterministic audit; deliberately not executed as an app-startup gate. */
public final class CanonicalActorFacingAudit {
  private CanonicalActorFacingAudit(){}

  public static boolean passes(){
    EnumSet<CharacterRenderer.Direction> seen=EnumSet.noneOf(CharacterRenderer.Direction.class);
    float[] values={-128f,-64f,-32f,-16f,-1f,-.0002f,0f,.0002f,1f,16f,32f,64f,128f};
    for(float dx:values)for(float dy:values){
      CharacterRenderer.Direction player=CanonicalActorFacing.quantize(dx,dy,CharacterRenderer.Direction.NW);
      CharacterRenderer.Direction monster=CanonicalActorFacing.quantize(dx,dy,CharacterRenderer.Direction.NW);
      if(player==null||monster==null||player!=monster)return false;
      seen.add(player);
      if(Math.abs(dx)>.0001f||Math.abs(dy)>.0001f){
        CharacterRenderer.Direction reverse=CanonicalActorFacing.quantize(-dx,-dy,CharacterRenderer.Direction.SE);
        if(reverse!=CanonicalActorFacing.reciprocal(player))return false;
      }
    }
    if(seen.size()!=4)return false;

    if(CanonicalActorFacing.quantize(WorldMoveTargetController.Direction.NW.dx,WorldMoveTargetController.Direction.NW.dy,null)!=CharacterRenderer.Direction.NW)return false;
    if(CanonicalActorFacing.quantize(WorldMoveTargetController.Direction.NE.dx,WorldMoveTargetController.Direction.NE.dy,null)!=CharacterRenderer.Direction.NE)return false;
    if(CanonicalActorFacing.quantize(WorldMoveTargetController.Direction.SW.dx,WorldMoveTargetController.Direction.SW.dy,null)!=CharacterRenderer.Direction.SW)return false;
    if(CanonicalActorFacing.quantize(WorldMoveTargetController.Direction.SE.dx,WorldMoveTargetController.Direction.SE.dy,null)!=CharacterRenderer.Direction.SE)return false;

    // Screen-axis ties are ambiguous in an isometric world, but must remain reciprocal.
    if(CanonicalActorFacing.quantize(0f,-64f,null)!=CharacterRenderer.Direction.NE)return false;
    if(CanonicalActorFacing.quantize(0f,64f,null)!=CharacterRenderer.Direction.SW)return false;
    if(CanonicalActorFacing.quantize(64f,0f,null)!=CharacterRenderer.Direction.NE)return false;
    if(CanonicalActorFacing.quantize(-64f,0f,null)!=CharacterRenderer.Direction.SW)return false;

    // Player and monster take one stable attack-facing snapshot for the full action.
    CanonicalActorFacing playerActor=new CanonicalActorFacing(CharacterRenderer.Direction.SW);
    playerActor.beginAttack(96f,-16f);
    CharacterRenderer.Direction playerSnapshot=playerActor.presentation();
    playerActor.updateLocomotion(-96f,48f);
    if(!playerActor.attackLocked()||playerActor.presentation()!=playerSnapshot)return false;

    CanonicalActorFacing monsterActor=new CanonicalActorFacing(CharacterRenderer.Direction.NE);
    monsterActor.beginAttack(-96f,16f);
    CharacterRenderer.Direction monsterSnapshot=monsterActor.presentation();
    monsterActor.updateLocomotion(96f,-48f);
    if(!monsterActor.attackLocked()||monsterActor.presentation()!=monsterSnapshot)return false;
    if(monsterSnapshot!=CanonicalActorFacing.reciprocal(playerSnapshot))return false;

    playerActor.endAttack();
    monsterActor.endAttack();
    return !playerActor.attackLocked()&&!monsterActor.attackLocked();
  }

  public static void main(String[] args){
    if(!passes())throw new AssertionError("CanonicalActorFacingAudit failed");
    System.out.println("CanonicalActorFacingAudit PASS");
  }
}
