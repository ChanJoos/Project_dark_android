package com.projectdark.mobile;

import static org.junit.Assert.assertTrue;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import com.projectdark.mobile.world.WorldCameraTransform;
import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.GraphicsMode;

/** Save native renders across the authored village instead of reviewing the spawn frame alone. */
@RunWith(RobolectricTestRunner.class)
@Config(sdk=34,manifest=Config.NONE)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public final class MillesDistrictReviewTest {
  @Test public void renderWestSouthAndWatersideDistricts() throws Exception {
    GameView view=new GameView(RuntimeEnvironment.getApplication());
    view.layout(0,0,1536,704);
    Field field=GameView.class.getDeclaredField("camera");field.setAccessible(true);
    WorldCameraTransform camera=(WorldCameraTransform)field.get(view);
    float[][] locations={{240f,780f},{800f,1130f},{1690f,810f}};
    String[] names={"milles-west-crafts.png","milles-south-residences.png","milles-east-waterside.png"};
    for(int i=0;i<locations.length;i++){
      camera.snapTo(locations[i][0],locations[i][1]);
      Bitmap frame=Bitmap.createBitmap(1536,704,Bitmap.Config.ARGB_8888);
      view.draw(new Canvas(frame));
      File file=new File("build/reports/device-review/"+names[i]);
      File parent=file.getParentFile();if(parent!=null)parent.mkdirs();
      try(FileOutputStream out=new FileOutputStream(file)){
        assertTrue("district image encoded",frame.compress(Bitmap.CompressFormat.PNG,100,out));
      }
      assertTrue("district image exists: "+names[i],file.isFile()&&file.length()>0);
      frame.recycle();
    }
  }
}
