package com.projectdark.mobile;
import android.view.MotionEvent;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.*;
import static org.junit.Assert.*;
@RunWith(RobolectricTestRunner.class) @Config(sdk=34,manifest=Config.NONE)
public class JoystickV119Test {
 @Test public void relocatedVisiblePadUsesTheSameTouchAndResetCenter()throws Exception{
  GameView view=new GameView(RuntimeEnvironment.getApplication());view.layout(0,0,960,540);
  view.onTouchEvent(MotionEvent.obtain(0,0,MotionEvent.ACTION_DOWN,84,398,0));
  assertTrue((Boolean)TownInteriorTest.field(view,"joy"));
  view.onTouchEvent(MotionEvent.obtain(0,1,MotionEvent.ACTION_MOVE,118,398,0));
  assertTrue((Float)TownInteriorTest.field(view,"vx")>0);
  view.onTouchEvent(MotionEvent.obtain(0,2,MotionEvent.ACTION_UP,118,398,0));
  assertFalse((Boolean)TownInteriorTest.field(view,"joy"));assertEquals(84f,(Float)TownInteriorTest.field(view,"knobX"),0);assertEquals(398f,(Float)TownInteriorTest.field(view,"knobY"),0);
 }
}
