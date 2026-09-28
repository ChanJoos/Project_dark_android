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
  @Test public void creekSpritesContainWaterPixelsWithoutRockOrBankPixels() throws Exception {
    for(int i=1;i<=6;i++){
      String name=String.format("POTE_WATER_%02d.png",i);
      Bitmap bitmap;
      try(InputStream in=RuntimeEnvironment.getApplication().getAssets().open(name)){
        BitmapFactory.Options options=new BitmapFactory.Options();options.inScaled=false;
        bitmap=BitmapFactory.decodeStream(in,null,options);
      }
      assertNotNull("missing water-only runtime asset "+name,bitmap);
      int visible=0;
      for(int y=0;y<bitmap.getHeight();y++)for(int x=0;x<bitmap.getWidth();x++){
        int color=bitmap.getPixel(x,y);if((color>>>24)<16)continue;
        visible++;
        int red=(color>>>16)&255,green=(color>>>8)&255,blue=color&255;
        assertTrue("dark rock/bank pixel remains in "+name,red+green+blue>=390);
      }
      assertTrue("water-only source must retain visible stream pixels (count=" +visible+ "): "+name,visible>0);
      bitmap.recycle();
    }
  }
}
