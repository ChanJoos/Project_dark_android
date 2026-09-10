package com.projectdark.mobile.world;

/** Legacy audit name retained; free-pixel exploration semantics are superseded. */
public final class WorldMoveTargetExplorationAudit {
  private WorldMoveTargetExplorationAudit(){}
  public static boolean verify(){return IsometricTileMovementAudit.verify();}
  public static void main(String[] args){if(!verify())throw new AssertionError("WorldMoveTargetExplorationAudit failed");}
}
