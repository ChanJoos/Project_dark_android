package com.projectdark.mobile.world;

import static org.junit.Assert.*;
import android.graphics.*;
import android.content.res.AssetManager;
import java.io.*;
import java.nio.file.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk=34,manifest=Config.NONE)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public final class MillesSourceStyleTest {
  @Test public void bothOriginalBenchOrientationsRetainIronBelowTheSeat()throws Exception{
    AssetManager assets=RuntimeEnvironment.getApplication().getAssets();
    for(int orientation=1;orientation<=2;orientation++){
      String p="video_reference/objects/bench_video_cutout_0"+orientation+".png";
      Bitmap raw;try(InputStream in=assets.open(p)){BitmapFactory.Options options=new BitmapFactory.Options();options.inPremultiplied=false;raw=BitmapFactory.decodeStream(in,null,options);}
      Bitmap fixed=MillesSourceStyle.bench(raw,p);
      int restored=0,lowerLeg=0;
      for(int y=0;y<raw.getHeight();y++)for(int x=0;x<raw.getWidth();x++){
        int before=raw.getPixel(x,y),after=fixed.getPixel(x,y);
        if(Color.alpha(before)==0&&Color.alpha(after)>0){
          restored++;assertEquals("recover source metal RGB, never fabricate feet",before&0xffffff,after&0xffffff);
          if(y>110)lowerLeg++;
        }
      }
      assertTrue("the old key deleted metal supports",restored>70);
      assertTrue("visible leg continues below the seat, not merely the armrest",lowerLeg>15);
      assertEquals("background gutter remains transparent",0,Color.alpha(fixed.getPixel(0,0)));
      Bitmap actual=Bitmap.createBitmap(800,660,Bitmap.Config.ARGB_8888);Canvas c=new Canvas(actual);
      c.drawColor(0xff355127);Paint pixel=new Paint();pixel.setFilterBitmap(false);
      float scale=.43f,anchor=orientation==1?128:123;
      c.drawBitmap(fixed,null,new RectF(120,150-anchor*scale,120+fixed.getWidth()*scale,150+(fixed.getHeight()-anchor)*scale),pixel);
      c.translate(80,220);c.scale(3,3);c.drawBitmap(fixed,0,0,pixel);
      java.nio.file.Path out=Paths.get("build/reports/device-review/v101-bench-"+orientation+".png");Files.createDirectories(out.getParent());
      try(OutputStream stream=Files.newOutputStream(out)){assertTrue(actual.compress(Bitmap.CompressFormat.PNG,100,stream));}
    }
  }
  @Test public void scenePalettesAffectSceneryButNeverCapturePixelsOrCharacters()throws Exception{
    AssetManager assets=RuntimeEnvironment.getApplication().getAssets();MillesSourceStyle style=new MillesSourceStyle(assets);
    Bitmap captured;try(InputStream in=assets.open("video_reference/terrain/grass_texture.png")){captured=BitmapFactory.decodeStream(in);}
    assertSame(captured,style.scenery(captured,"video_reference/terrain/grass_texture.png"));
    assertSame(captured,style.scenery(captured,"assets/characters/player.png"));
    Bitmap authored;try(InputStream in=assets.open("buildings/BLD_003_weapon_shop.png")){authored=BitmapFactory.decodeStream(in);}
    Bitmap styled=style.scenery(authored,"buildings/BLD_003_weapon_shop.png");
    int changed=0;for(int y=0;y<authored.getHeight();y++)for(int x=0;x<authored.getWidth();x++)if(authored.getPixel(x,y)!=styled.getPixel(x,y))changed++;
    assertTrue("palette conversion is wired into actual generated scenery",changed>1000);
    assertEquals(authored.getWidth(),styled.getWidth());assertEquals(authored.getHeight(),styled.getHeight());
  }
}
