package com.projectdark.mobile;

import com.projectdark.mobile.world.WorldMoveTargetController;

/**
 * Shared player/monster melee geometry. A basic melee attack is legal only when the target occupies
 * one authored adjacent 64x32 isometric tile center. This same match also owns attack-facing output.
 */
public final class MeleeTileContract {
  private static final float EPSILON=.01f;

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

  public static CharacterRenderer.Direction reciprocal(CharacterRenderer.Direction direction){
    if(direction==null)return null;
    switch(direction){
      case NW:return CharacterRenderer.Direction.SE;
      case NE:return CharacterRenderer.Direction.SW;
      case SW:return CharacterRenderer.Direction.NE;
      case SE:return CharacterRenderer.Direction.NW;
      default:throw new IllegalStateException("Unhandled facing "+direction);
    }
  }

  private static boolean close(float a,float b){return Math.abs(a-b)<=EPSILON;}
}
