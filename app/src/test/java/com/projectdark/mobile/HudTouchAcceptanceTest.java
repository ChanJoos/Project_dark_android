package com.projectdark.mobile;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

/** Keeps visible HUD bounds from consuming representative empty-world tap-to-move regions. */
public final class HudTouchAcceptanceTest {
  @Test public void tenWorldTapRegionsRemainReachableBeforeAndAfterCameraMove() {
    UxTapAcceptanceAudit.Result result = UxTapAcceptanceAudit.run();
    assertTrue("initial camera accepts " + result.initialCameraAccepted + "/10",
        result.initialCameraAccepted == UxTapAcceptanceAudit.REQUIRED_POINTS);
    assertTrue("moved camera accepts " + result.movedCameraAccepted + "/10",
        result.movedCameraAccepted == UxTapAcceptanceAudit.REQUIRED_POINTS);
  }
}
