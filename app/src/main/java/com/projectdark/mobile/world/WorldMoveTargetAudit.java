package com.projectdark.mobile.world;

/** Compatibility entry point for the canonical tile-locked movement audit. */
public final class WorldMoveTargetAudit {
  private WorldMoveTargetAudit(){}
  public static boolean verify(){return IsometricTileMovementAudit.verify();}
  public static void main(String[] args){if(!verify())throw new AssertionError("WorldMoveTargetAudit failed");}
}
