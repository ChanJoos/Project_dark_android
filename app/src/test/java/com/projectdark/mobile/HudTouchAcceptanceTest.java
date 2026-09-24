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
    assertTrue(GameView.blocksWorldTapForHud(140, 150, false));
    assertTrue(GameView.blocksWorldTapForHud(608, 28, false));
    assertTrue("attack control is anchored at the right edge without clipping",
        GameView.blocksWorldTapForHud(955, 498, true));
    assertTrue("empty edge beyond the attack remains available to the world",
        !GameView.blocksWorldTapForHud(960, 498, true));
    assertTrue("utility rail shifted right while its former position returns to the world",
        !GameView.blocksWorldTapForHud(640, 28, false));
    assertTrue("world outside the left cards and icon rail stays tappable",
        !GameView.blocksWorldTapForHud(310, 190, false));

    GameView view = new GameView(RuntimeEnvironment.getApplication());
    view.layout(0, 0, 960, 540);
    assertTrue("quest starts folded to preserve map space", questCollapsed(view));
    tap(view, 80, 150); // open the compact tracker
    assertTrue(!questCollapsed(view));
    tap(view, 244, 151); // fold chevron on the expanded card
    assertTrue(questCollapsed(view));
    tap(view, 80, 150); // collapsed card opens without routing a move
    assertTrue(!questCollapsed(view));
    assertTrue(!chatExpanded(view));
    tap(view, 420, 480); // compact chat opens
    assertTrue(chatExpanded(view));
    tap(view, 620, 432); // expanded chat folds
    assertTrue(!chatExpanded(view));
  }

  @Test public void autoTargetSelectionChoosesNearestLivingMonster() {
    RuntimeState.Monster far=new RuntimeState.Monster("far","Far",40,0,10,"test");
    RuntimeState.Monster distant=new RuntimeState.Monster("distant","Distant",400,0,10,"test");
    RuntimeState.Monster dead=new RuntimeState.Monster("dead","Dead",1,0,10,"test");dead.alive=false;
    RuntimeState.Monster near=new RuntimeState.Monster("near","Near",-3,0,10,"test");
    assertTrue(GameView.nearestLivingMonster(java.util.Arrays.asList(distant,far,dead,near),0,0)==near);
    assertTrue("auto target acquisition is limited to nearby monsters",GameView.nearestLivingMonster(java.util.Collections.singletonList(distant),0,0)==null);
  }

  @Test public void rightHudUsesWideScreenSideMargin() throws Exception {
    assertTrue(Math.abs(GameView.logicalWidthForView(1920,1080)-960f)<.01f);
    assertTrue(Math.abs(GameView.logicalWidthForView(2340,1080)-1170f)<.01f);
    assertTrue(Math.abs(GameView.rightHudOffsetForView(1920,1080))<.01f);
    assertTrue(Math.abs(GameView.rightHudOffsetForView(2400,1080)-240f)<.01f);
    assertTrue(Math.abs(GameView.rightHudOffsetForView(1080,1920))<.01f);
    assertTrue("chat hit region follows its centered wide-screen panel",GameView.blocksWorldTapForHud(700,480,false,210f));
    assertTrue("old center-left chat position remains available to the world",!GameView.blocksWorldTapForHud(300,480,false,210f));
    GameView view=new GameView(RuntimeEnvironment.getApplication());
    view.layout(0,0,2340,1080);
    assertTrue("wide camera expands to the full game viewport",Math.abs(viewCameraWidth(view)-1170f)<.01f);
  }

  private static float viewCameraWidth(GameView view) throws Exception {
    Field field=GameView.class.getDeclaredField("camera");field.setAccessible(true);
    return ((com.projectdark.mobile.world.WorldCameraTransform)field.get(view)).viewportWidth();
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
