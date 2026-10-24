package com.projectdark.mobile;
import android.view.MotionEvent;import java.lang.reflect.*;
/** Native input at the current drawn control center, including screen scaling. */
final class HudTestControlsV267 {
 static void auto(GameView v){try{Field f=GameView.class.getDeclaredField("hudRightOffset");f.setAccessible(true);float d=f.getFloat(v);Method offset=GameView.class.getDeclaredMethod("combatHudOffset",float.class);offset.setAccessible(true);float x=number("AUTO_X")+(Float)offset.invoke(null,d),y=number("AUTO_Y"),scale=v.getHeight()/540f;MotionEvent e=MotionEvent.obtain(0,0,MotionEvent.ACTION_DOWN,x*scale,y*scale,0);v.onTouchEvent(e);e.recycle();}catch(Exception e){throw new AssertionError(e);}}
 static float number(String name)throws Exception{Field f=GameView.class.getDeclaredField(name);f.setAccessible(true);return f.getFloat(null);}
}
