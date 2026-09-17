package com.projectdark.mobile;

import com.projectdark.mobile.world.WorldMoveTargetController;

/** Compatibility facade; canonical spatial semantics live in UnifiedRuntimeContract. */
public final class CanonicalMeleeTileContract {
  public static final float STEP_X=Math.abs(WorldMoveTargetController.Direction.NE.dx);
  public static final float STEP_Y=Math.abs(WorldMoveTargetController.Direction.NE.dy);
  public static final float REACH_DISTANCE=(float)Math.sqrt(STEP_X*STEP_X+STEP_Y*STEP_Y);
  private CanonicalMeleeTileContract(){}
  public static WorldMoveTargetController.Direction direction(float ax,float ay,float tx,float ty){return UnifiedRuntimeContract.adjacentDirection(ax,ay,tx,ty);}
  public static boolean reachable(float ax,float ay,float tx,float ty){return UnifiedRuntimeContract.meleeReachable(ax,ay,tx,ty);}
  public static CharacterRenderer.Direction facing(float ax,float ay,float tx,float ty){return UnifiedRuntimeContract.facing(direction(ax,ay,tx,ty));}
  public static CharacterRenderer.Direction facing(WorldMoveTargetController.Direction direction){return UnifiedRuntimeContract.facing(direction);}
  public static boolean isMelee(AttackDef.Kind kind){return kind!=null&&kind!=AttackDef.Kind.THROW;}
}
