package com.projectdark.mobile.world;

/** Legacy audit name retained; arbitrary taps now snap to authored isometric tile centers. */
public final class WorldArbitraryTapAcceptanceAudit {
  private WorldArbitraryTapAcceptanceAudit(){}
  public static boolean verify(){return IsometricTileMovementAudit.verify();}
  public static void main(String[] args){if(!verify())throw new AssertionError("WorldArbitraryTapAcceptanceAudit failed");}
}
