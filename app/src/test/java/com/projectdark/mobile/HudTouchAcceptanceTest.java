package com.projectdark.mobile;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/** Keeps visible HUD bounds from consuming representative empty-world tap-to-move regions. */
@RunWith(RobolectricTestRunner.class)
@Config(sdk=34,manifest=Config.NONE)
public final class HudTouchAcceptanceTest {
  @Test public void tenWorldTapRegionsRemainReachableBeforeAndAfterCameraMove() {
    UxTapAcceptanceAudit.Result result = UxTapAcceptanceAudit.run();
    assertTrue("initial camera accepts " + result.initialCameraAccepted + "/10",
        result.initialCameraAccepted == UxTapAcceptanceAudit.REQUIRED_POINTS);
    assertTrue("moved camera accepts " + result.movedCameraAccepted + "/10",
        result.movedCameraAccepted == UxTapAcceptanceAudit.REQUIRED_POINTS);
  }
}
