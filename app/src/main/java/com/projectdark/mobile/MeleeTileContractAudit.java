package com.projectdark.mobile;

import com.projectdark.mobile.world.WorldMoveTargetController;

/** Regression gate for shared player/monster melee adjacency. */
public final class MeleeTileContractAudit {
  private MeleeTileContractAudit(){}

  public static boolean passes(){
    float ax=320f,ay=240f;
    for(WorldMoveTargetController.Direction d:WorldMoveTargetController.Direction.values()){
      float tx=ax+d.dx,ty=ay+d.dy;
      if(!MeleeTileContract.canAttack(ax,ay,tx,ty))return false;
      if(MeleeTileContract.adjacentDirection(ax,ay,tx,ty)!=d)return false;
      CharacterRenderer.Direction forward=MeleeTileContract.visualDirection(d);
      WorldMoveTargetController.Direction reverse=MeleeTileContract.adjacentDirection(tx,ty,ax,ay);
      if(reverse==null)return false;
      CharacterRenderer.Direction backward=MeleeTileContract.visualDirection(reverse);
      if(CanonicalActorFacing.reciprocal(forward)!=backward)return false;
    }

    // Old radius-valid positions that must now be rejected.
    if(MeleeTileContract.canAttack(ax,ay,ax,ay-35f))return false;
    if(MeleeTileContract.canAttack(ax,ay,ax+20f,ay+20f))return false;
    if(MeleeTileContract.canAttack(ax,ay,ax+32f,ay))return false;
    if(MeleeTileContract.canAttack(ax,ay,ax+64f,ay+32f))return false;
    if(MeleeTileContract.canAttack(ax,ay,ax,ay))return false;

    float expected=(float)Math.sqrt(32f*32f+16f*16f);
    return Math.abs(MeleeTileContract.ADJACENT_CENTER_DISTANCE-expected)<.001f;
  }

  public static void main(String[] args){
    if(!passes())throw new AssertionError("MeleeTileContractAudit failed");
    System.out.println("MeleeTileContractAudit PASS");
  }
}
