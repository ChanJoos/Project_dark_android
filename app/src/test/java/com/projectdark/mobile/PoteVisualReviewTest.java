package com.projectdark.mobile;

import static org.junit.Assert.*;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import com.projectdark.mobile.world.WorldCameraTransform;
import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.GraphicsMode;

@RunWith(RobolectricTestRunner.class)
@Config(sdk=34,manifest=Config.NONE)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public final class PoteVisualReviewTest {
 @Test public void renderPoteForestReferenceFrames() throws Exception {
  GameView view=new GameView(RuntimeEnvironment.getApplication()); view.layout(0,0,1536,704);
  Method enter=GameView.class.getDeclaredMethod("enterPoteField"); enter.setAccessible(true); enter.invoke(view);
  Field af=GameView.class.getDeclaredField("poteFieldAdapter"); af.setAccessible(true); com.projectdark.mobile.world.WorldRuntimeAdapter adapter=(com.projectdark.mobile.world.WorldRuntimeAdapter)af.get(view); WorldCameraTransform camera=adapter.camera();
  float[][] spots={{240f,760f},{640f,480f},{1324f,388f},{1550f,480f}};
  String[] names={"pote-entry-clearing.png","pote-central-clearing.png","pote-bridge-crossing.png","pote-east-bank-clearing.png"};
  for(int i=0;i<spots.length;i++){
   camera.snapTo(spots[i][0],spots[i][1]); Bitmap frame=Bitmap.createBitmap(1536,704,Bitmap.Config.ARGB_8888); view.draw(new Canvas(frame));
   File file=new File("build/reports/device-review/"+names[i]); File parent=file.getParentFile(); if(parent!=null) parent.mkdirs();
   try(FileOutputStream out=new FileOutputStream(file)){assertTrue(frame.compress(Bitmap.CompressFormat.PNG,100,out));}
   assertTrue(file.isFile()&&file.length()>0); frame.recycle();
  }
 }

 @Test public void fieldMovementAndRenderedCharacterUseTheSameWorldAdapter() throws Exception {
  GameView view=new GameView(RuntimeEnvironment.getApplication());view.layout(0,0,1536,704);
  Method enter=GameView.class.getDeclaredMethod("enterPoteField");enter.setAccessible(true);enter.invoke(view);
  Field af=GameView.class.getDeclaredField("poteFieldAdapter");af.setAccessible(true);
  com.projectdark.mobile.world.WorldRuntimeAdapter field=(com.projectdark.mobile.world.WorldRuntimeAdapter)af.get(view);
  float startX=field.runtime().player().x,startY=field.runtime().player().y;
  assertEquals(startX,view.renderedPlayerWorldX(),.01f);
  assertEquals(startY,view.renderedPlayerWorldY(),.01f);
  assertEquals(com.projectdark.mobile.world.WorldMoveTargetController.Status.MOVING,field.requestGroundWorld(1120f,224f).status);
  for(int i=0;i<240;i++)field.tickNavigation(.05f);
  assertTrue("tap-to-move must change the field player's world position",Math.abs(field.runtime().player().x-startX)>100f||Math.abs(field.runtime().player().y-startY)>100f);
  assertEquals(field.presentationPlayerX(),view.renderedPlayerWorldX(),.01f);
  assertEquals(field.presentationPlayerY(),view.renderedPlayerWorldY(),.01f);
 }

 @Test public void oneFourDirectionInputAdvancesExactlyOnePoteTile() throws Exception {
  GameView view=new GameView(RuntimeEnvironment.getApplication());view.layout(0,0,1536,704);
  Method enter=GameView.class.getDeclaredMethod("enterPoteField");enter.setAccessible(true);enter.invoke(view);
  Field af=GameView.class.getDeclaredField("poteFieldAdapter");af.setAccessible(true);
  com.projectdark.mobile.world.WorldRuntimeAdapter field=(com.projectdark.mobile.world.WorldRuntimeAdapter)af.get(view);
  float sx=field.runtime().player().x,sy=field.runtime().player().y;
  com.projectdark.mobile.world.WorldMoveTargetController.Snapshot step=field.step(com.projectdark.mobile.world.WorldMoveTargetController.Direction.NE);
  assertEquals(com.projectdark.mobile.world.WorldMoveTargetController.Status.REACHED,step.status);
  assertEquals(sx+32f,field.runtime().player().x,.01f);assertEquals(sy-16f,field.runtime().player().y,.01f);
  assertEquals("presentation starts on the old tile",sx,field.presentationPlayerX(),.01f);
  field.tickNavigation(.30f);
  assertEquals(sx+16f,field.presentationPlayerX(),.1f);
  field.tickNavigation(.30f);
  assertEquals(sx+32f,field.presentationPlayerX(),.01f);assertEquals(sy-16f,field.presentationPlayerY(),.01f);
  assertTrue("entry guide remains close to spawn",field.runtime().npcs().stream().anyMatch(n->Math.hypot(n.x-sx,n.y-sy)<100f));
 }
}
