package com.projectdark.mobile;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import java.io.InputStream;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk=34,manifest=Config.NONE)
public final class PoteCreekWaterAssetTest {
  @Test public void creekSpritesDecodeAsPackagedAssets() throws Exception {
    for(int i=1;i<=6;i++){
      String name=String.format("POTE_WATER_%02d.png",i);
      Bitmap bitmap;
      try(InputStream in=RuntimeEnvironment.getApplication().getAssets().open(name)){
        BitmapFactory.Options options=new BitmapFactory.Options();options.inScaled=false;
        bitmap=BitmapFactory.decodeStream(in,null,options);
      }
      assertNotNull("missing or undecodable creek runtime asset "+name,bitmap);
      assertTrue("creek sprite must have nonzero dimensions: "+name,bitmap.getWidth()>0&&bitmap.getHeight()>0);
      bitmap.recycle();
    }
  }
}
