package com.projectdark.mobile;

import com.projectdark.mobile.world.WorldMoveTargetController;

/** Android-free gate proving melee reach/facing use the same four adjacent tile endpoints. */
public final class CanonicalMeleeTileContractAudit {
  private static final float X=640f,Y=480f;
  private CanonicalMeleeTileContractAudit(){}

  public static boolean passes(){
    if(Math.abs(CanonicalMeleeTileContract.STEP_X-Math.abs(WorldMoveTargetController.Direction.NE.dx))>.001f)return false;
    if(Math.abs(CanonicalMeleeTileContract.STEP_Y-Math.abs(WorldMoveTargetController.Direction.NE.dy))>.001f)return false;
    if(Math.abs(CanonicalMeleeTileContract.REACH_DISTANCE-WorldMoveTargetController.ADJACENT_TILE_DISTANCE)>.001f)return false;
    for(WorldMoveTargetController.Direction d:WorldMoveTargetController.Direction.values()){
      float tx=X+d.dx,ty=Y+d.dy;
      if(!CanonicalMeleeTileContract.reachable(X,Y,tx,ty))return false;
      if(CanonicalMeleeTileContract.direction(X,Y,tx,ty)!=d)return false;
      if(CanonicalActorFacing.quantize(d.dx,d.dy,null)!=CanonicalMeleeTileContract.facing(d))return false;
      WorldMoveTargetController.Direction reverse=CanonicalMeleeTileContract.direction(tx,ty,X,Y);
      if(reverse==null||CanonicalActorFacing.reciprocal(CanonicalMeleeTileContract.facing(d))!=CanonicalMeleeTileContract.facing(reverse))return false;
    }
    float[][] rejected={{0f,32f},{0f,-32f},{32f,0f},{-32f,0f},{16f,16f},{0f,-35f},{20f,20f},{64f,32f},{0f,0f}};
    for(float[] delta:rejected)if(CanonicalMeleeTileContract.reachable(X,Y,X+delta[0],Y+delta[1]))return false;
    if(!CanonicalMeleeTileContract.isMelee(AttackDef.Kind.SWING)||!CanonicalMeleeTileContract.isMelee(AttackDef.Kind.THRUST)||!CanonicalMeleeTileContract.isMelee(AttackDef.Kind.PUNCH))return false;
    if(CanonicalMeleeTileContract.isMelee(AttackDef.Kind.THROW))return false;
    return true;
  }

  public static void main(String[] args){
    if(!passes())throw new AssertionError("CanonicalMeleeTileContractAudit failed");
    System.out.println("CanonicalMeleeTileContractAudit PASS");
  }
}
