package com.projectdark.mobile;

import static org.junit.Assert.assertTrue;
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
  Field cf=GameView.class.getDeclaredField("camera"); cf.setAccessible(true); WorldCameraTransform camera=(WorldCameraTransform)cf.get(view);
  float[][] spots={{480f,416f},{384f,256f},{640f,224f}};
  String[] names={"pote-entry-clearing.png","pote-central-grove.png","pote-northeast-water.png"};
  for(int i=0;i<spots.length;i++){
   camera.snapTo(spots[i][0],spots[i][1]); Bitmap frame=Bitmap.createBitmap(1536,704,Bitmap.Config.ARGB_8888); view.draw(new Canvas(frame));
   File file=new File("build/reports/device-review/"+names[i]); File parent=file.getParentFile(); if(parent!=null) parent.mkdirs();
   try(FileOutputStream out=new FileOutputStream(file)){assertTrue(frame.compress(Bitmap.CompressFormat.PNG,100,out));}
   assertTrue(file.isFile()&&file.length()>0); frame.recycle();
  }
 }
}
