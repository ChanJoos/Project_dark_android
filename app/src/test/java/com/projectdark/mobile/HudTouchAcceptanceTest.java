package com.projectdark.mobile;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import android.view.MotionEvent;
import java.lang.reflect.Field;

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

  @Test public void statusQuestAndUtilitySurfacesAreProtectedButQuestCardCanFold() throws Exception {
    assertTrue(GameView.blocksWorldTapForHud(140, 110, false));
    assertTrue(GameView.blocksWorldTapForHud(140, 190, false));
    assertTrue(GameView.blocksWorldTapForHud(666, 28, false));
    assertTrue("attack control is anchored at the right edge without clipping",
        GameView.blocksWorldTapForHud(955, 498, true));
    assertTrue("empty edge beyond the attack remains available to the world",
        !GameView.blocksWorldTapForHud(960, 498, true));
    assertTrue("utility rail shifted right while its former position returns to the world",
        !GameView.blocksWorldTapForHud(632, 28, false));
    assertTrue("world outside the left cards and icon rail stays tappable",
        !GameView.blocksWorldTapForHud(310, 190, false));

    GameView view = new GameView(RuntimeEnvironment.getApplication());
    view.layout(0, 0, 960, 540);
    assertTrue("quest starts folded to preserve map space", questCollapsed(view));
    tap(view, 80, 176); // open the compact tracker
    assertTrue(!questCollapsed(view));
    tap(view, 258, 177); // fold chevron on the expanded card
    assertTrue(questCollapsed(view));
    tap(view, 80, 176); // collapsed card opens without routing a move
    assertTrue(!questCollapsed(view));
    assertTrue(!chatExpanded(view));
    tap(view, 420, 480); // compact chat opens
    assertTrue(chatExpanded(view));
    tap(view, 620, 432); // expanded chat folds
    assertTrue(!chatExpanded(view));
  }

  private static void tap(GameView view, float x, float y) {
    MotionEvent event = MotionEvent.obtain(0, 1, MotionEvent.ACTION_DOWN, x, y, 0);
    view.onTouchEvent(event);
    event.recycle();
  }

  private static boolean questCollapsed(GameView view) throws Exception {
    Field field = GameView.class.getDeclaredField("questCollapsed");
    field.setAccessible(true);
    return field.getBoolean(view);
  }

  private static boolean chatExpanded(GameView view) throws Exception {
    Field field = GameView.class.getDeclaredField("chatExpanded");
    field.setAccessible(true);
    return field.getBoolean(view);
  }
}
