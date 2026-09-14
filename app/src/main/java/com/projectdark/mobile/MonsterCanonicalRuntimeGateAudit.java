package com.projectdark.mobile;

import com.projectdark.mobile.world.IsometricTileMovementAudit;
import com.projectdark.mobile.world.MillesProductionCollisionAudit;

/** Composite World release gate for player-parity monster tile locomotion + attack-facing stability. */
public final class MonsterCanonicalRuntimeGateAudit {
  private MonsterCanonicalRuntimeGateAudit(){}

  public static boolean passes(){
    // Player contract is the source of truth; monster endpoints must be identical to it.
    if(!IsometricTileMovementAudit.verify())return false;
    if(!MonsterPlayerTileContractAudit.passes())return false;
    if(!MillesProductionCollisionAudit.verify())return false;
    if(!CanonicalActorFacingAudit.passes())return false;

    // Legacy segment-length audits are intentionally not release gates anymore: device acceptance
    // rejected the 8x4/16x8 tuning model. Runtime now consumes full authored tile endpoints only.
    for(com.projectdark.mobile.world.WorldMoveTargetController.Direction d:
        com.projectdark.mobile.world.WorldMoveTargetController.Direction.values()){
      CharacterRenderer.Direction expected=MonsterTileCenterLocomotion.facing(d);
      if(CanonicalActorFacing.quantize(d.dx,d.dy,null)!=expected)return false;
    }

    // Attack uses the same centered actor delta and stays snapshotted for the whole action.
    CanonicalActorFacing facing=new CanonicalActorFacing(CharacterRenderer.Direction.SE);
    CharacterRenderer.Direction expected=CanonicalActorFacing.quantize(-32f,-16f,CharacterRenderer.Direction.SE);
    facing.beginAttack(-32f,-16f);
    if(!facing.attackLocked()||facing.attack()!=expected||facing.presentation()!=expected)return false;
    facing.setLocomotion(CharacterRenderer.Direction.SE);
    if(facing.presentation()!=expected)return false;
    facing.endAttack();
    if(facing.attackLocked())return false;
    return true;
  }

  public static void main(String[] args){
    if(!passes())throw new AssertionError("MonsterCanonicalRuntimeGateAudit failed");
    System.out.println("MonsterCanonicalRuntimeGateAudit PASS: player/monster tile-center parity, ±32/±16 endpoints, collision, zero drift, attack lock");
  }
}
