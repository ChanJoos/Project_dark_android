package com.projectdark.mobile;

import com.projectdark.mobile.world.WorldMoveTargetController;

/**
 * Canonical melee spatial contract shared by player and monster combat.
 * A melee target is reachable iff it occupies exactly one authored adjacent 64x32 tile endpoint.
 * Geometry is derived from WorldMoveTargetController so combat cannot fork its own tile constants.
 */
public final class CanonicalMeleeTileContract {
  public static final float STEP_X=Math.abs(WorldMoveTargetController.Direction.NE.dx);
  public static final float STEP_Y=Math.abs(WorldMoveTargetController.Direction.NE.dy);
  public static final float REACH_DISTANCE=WorldMoveTargetController.ADJACENT_TILE_DISTANCE;

  private CanonicalMeleeTileContract(){}

  public static WorldMoveTargetController.Direction direction(float attackerX,float attackerY,float targetX,float targetY){
    return WorldMoveTargetController.Direction.between(attackerX,attackerY,targetX,targetY);
  }

  public static boolean reachable(float attackerX,float attackerY,float targetX,float targetY){
    return direction(attackerX,attackerY,targetX,targetY)!=null;
  }

  public static CharacterRenderer.Direction facing(float attackerX,float attackerY,float targetX,float targetY){
    return facing(direction(attackerX,attackerY,targetX,targetY));
  }

  public static CharacterRenderer.Direction facing(WorldMoveTargetController.Direction direction){
    if(direction==null)return null;
    switch(direction){
      case NW:return CharacterRenderer.Direction.NW;
      case NE:return CharacterRenderer.Direction.NE;
      case SW:return CharacterRenderer.Direction.SW;
      default:return CharacterRenderer.Direction.SE;
    }
  }

  public static boolean isMelee(AttackDef.Kind kind){return kind!=null&&kind!=AttackDef.Kind.THROW;}
}
