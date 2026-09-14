package com.projectdark.mobile;

import com.projectdark.mobile.world.WorldMoveTargetController;

/**
 * Canonical melee spatial contract shared by player and monster combat.
 * A melee target is reachable iff it occupies exactly one authored adjacent 64x32 tile endpoint.
 */
public final class CanonicalMeleeTileContract {
  public static final float STEP_X=32f;
  public static final float STEP_Y=16f;
  public static final float REACH_DISTANCE=(float)Math.sqrt(STEP_X*STEP_X+STEP_Y*STEP_Y);

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
