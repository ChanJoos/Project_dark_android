package com.projectdark.mobile;

import com.projectdark.mobile.world.WorldMoveTargetController;

/** Shared player/monster melee geometry on the authored 64x32 tile-center lattice. */
public final class MeleeTileContract {
  private static final float EPSILON=.01f;
  public static final float ADJACENT_CENTER_DISTANCE=(float)Math.sqrt(
      WorldMoveTargetController.Direction.NE.dx*WorldMoveTargetController.Direction.NE.dx+
      WorldMoveTargetController.Direction.NE.dy*WorldMoveTargetController.Direction.NE.dy);

  private MeleeTileContract(){}

  public static WorldMoveTargetController.Direction adjacentDirection(float attackerX,float attackerY,float targetX,float targetY){
    float dx=targetX-attackerX,dy=targetY-attackerY;
    for(WorldMoveTargetController.Direction d:WorldMoveTargetController.Direction.values())
      if(close(dx,d.dx)&&close(dy,d.dy))return d;
    return null;
  }

  public static boolean canAttack(float attackerX,float attackerY,float targetX,float targetY){
    return adjacentDirection(attackerX,attackerY,targetX,targetY)!=null;
  }

  public static CharacterRenderer.Direction visualDirection(WorldMoveTargetController.Direction direction){
    if(direction==null)return null;
    switch(direction){
      case NW:return CharacterRenderer.Direction.NW;
      case NE:return CharacterRenderer.Direction.NE;
      case SW:return CharacterRenderer.Direction.SW;
      case SE:return CharacterRenderer.Direction.SE;
      default:throw new IllegalStateException("Unhandled melee direction "+direction);
    }
  }

  private static boolean close(float a,float b){return Math.abs(a-b)<=EPSILON;}
}
