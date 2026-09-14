package com.projectdark.mobile;

import com.projectdark.mobile.world.IsometricTileMovementAudit;
import com.projectdark.mobile.world.MillesProductionCollisionAudit;

/** Composite World release gate for player-parity tile locomotion + canonical melee spatial contract. */
public final class MonsterCanonicalRuntimeGateAudit {
  private MonsterCanonicalRuntimeGateAudit(){}

  public static boolean passes(){
    if(!IsometricTileMovementAudit.verify())return false;
    if(!MonsterPlayerTileContractAudit.passes())return false;
    if(!CanonicalMeleeTileContractAudit.passes())return false;
    if(!MillesProductionCollisionAudit.verify())return false;
    if(!CanonicalActorFacingAudit.passes())return false;

    for(com.projectdark.mobile.world.WorldMoveTargetController.Direction d:
        com.projectdark.mobile.world.WorldMoveTargetController.Direction.values()){
      CharacterRenderer.Direction expected=MonsterTileCenterLocomotion.facing(d);
      if(CanonicalActorFacing.quantize(d.dx,d.dy,null)!=expected)return false;
      if(CanonicalMeleeTileContract.facing(d)!=expected)return false;
    }

    CanonicalActorFacing facing=new CanonicalActorFacing(CharacterRenderer.Direction.SE);
    CharacterRenderer.Direction expected=CanonicalMeleeTileContract.facing(0f,0f,-32f,-16f);
    facing.beginAttack(-32f,-16f);
    if(expected==null||!facing.attackLocked()||facing.attack()!=expected||facing.presentation()!=expected)return false;
    facing.setLocomotion(CharacterRenderer.Direction.SE);
    if(facing.presentation()!=expected)return false;
    facing.endAttack();
    return !facing.attackLocked();
  }

  public static void main(String[] args){
    if(!passes())throw new AssertionError("MonsterCanonicalRuntimeGateAudit failed");
    System.out.println("MonsterCanonicalRuntimeGateAudit PASS: tile-center movement, exact adjacent melee reach, facing parity, collision, zero drift, attack lock");
  }
}
