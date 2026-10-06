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
  @Test public void productionBenchHasLongConnectedSupportsAndGroundRegisteredFeet()throws Exception{
    AssetManager a=RuntimeEnvironment.getApplication().getAssets();MillesSourceStyle style=new MillesSourceStyle(a);
    for(int orientation=1;orientation<=2;orientation++){
      String path="video_reference/objects/bench_video_cutout_0"+orientation+".png";
      java.lang.reflect.Method get=AdaptedMillesMapRenderer.class.getDeclaredMethod("bitmap",String.class);get.setAccessible(true);
      Bitmap b=(Bitmap)get.invoke(new AdaptedMillesMapRenderer(),path);
      assertEquals(180,b.getWidth());assertEquals(180,b.getHeight());
      int longest=0,legs=0;
      for(int x=0;x<180;x++){
        int run=0,columnRun=0;boolean foot=false;
        for(int y=125;y<173;y++){
          int c=b.getPixel(x,y),max=Math.max(Color.red(c),Math.max(Color.green(c),Color.blue(c)));
          if(Color.alpha(c)>0&&max<150){run++;longest=Math.max(longest,run);columnRun=Math.max(columnRun,run);if(y>=164)foot=true;}else run=0;
        }
        if(columnRun>=22&&foot)legs++;
      }
      assertTrue("actual renderer has a long continuous iron leg below the seat",longest>=22);
      assertTrue("flared ground foot remains visible at its registered bottom",legs>=3);
      int ground=0;for(int x=0;x<180;x++)if(Color.alpha(b.getPixel(x,170))>0)ground++;
      assertTrue("feet terminate on the ground, not the image edge",ground>3);
      for(int x=0;x<180;x++)assertEquals(0,Color.alpha(b.getPixel(x,179)));
      Bitmap review=Bitmap.createBitmap(900,700,Bitmap.Config.ARGB_8888);Canvas c=new Canvas(review);c.drawColor(0xff355127);Paint pixel=new Paint();pixel.setFilterBitmap(false);
      c.drawBitmap(b,null,new RectF(100-90*.43f,180-172*.43f,100+90*.43f,180+8*.43f),pixel);
      c.translate(80,210);c.scale(2.5f,2.5f);c.drawBitmap(b,0,0,pixel);
      java.nio.file.Path out=Paths.get("build/reports/device-review/v103-bench-supports-"+orientation+".png");Files.createDirectories(out.getParent());
      try(OutputStream stream=Files.newOutputStream(out)){assertTrue(review.compress(Bitmap.CompressFormat.PNG,100,stream));}
    }
    try(InputStream in=a.open("maps/milles_garden.json")){
      ByteArrayOutputStream data=new ByteArrayOutputStream();byte[] buf=new byte[4096];int n;while((n=in.read(buf))!=-1)data.write(buf,0,n);
      org.json.JSONArray rows=new org.json.JSONObject(data.toString("UTF-8")).getJSONArray("objects");
      for(int i=0;i<rows.length();i++){org.json.JSONObject row=rows.getJSONObject(i);if(row.getString("asset").contains("bench_video_cutout")){assertEquals(90,row.getInt("anchor_x"));assertEquals(172,row.getInt("anchor_y"));}}
    }
  }
  @Test public void churchKeepsItsEstablishedMultiSpireSilhouette()throws Exception{
    AssetManager a=RuntimeEnvironment.getApplication().getAssets();Bitmap original;
    try(InputStream in=a.open("landmarks/BLD_011_church.png")){original=BitmapFactory.decodeStream(in);}
    assertSame("do not substitute the established church with a small cabin-like single tower",original,new MillesSourceStyle(a).replacement(original,"landmarks/BLD_011_church.png"));
    java.lang.reflect.Method get=AdaptedMillesMapRenderer.class.getDeclaredMethod("bitmap",String.class);get.setAccessible(true);
    Bitmap rendered=(Bitmap)get.invoke(new AdaptedMillesMapRenderer(),"landmarks/BLD_011_church.png");
    int high=0;for(int y=0;y<original.getHeight()/4;y++)for(int x=0;x<original.getWidth();x++)if(Color.alpha(rendered.getPixel(x,y))>0)high++;
    assertTrue("spire survives the actual presentation pipeline",high>100);
  }
  @Test public void bothOriginalBenchOrientationsRetainIronBelowTheSeat()throws Exception{
    AssetManager assets=RuntimeEnvironment.getApplication().getAssets();
    for(int orientation=1;orientation<=2;orientation++){
      String p="video_reference/objects/bench_video_cutout_0"+orientation+".png";
      Bitmap raw;try(InputStream in=assets.open(p)){BitmapFactory.Options options=new BitmapFactory.Options();options.inPremultiplied=false;raw=BitmapFactory.decodeStream(in,null,options);}
      Bitmap fixed=MillesSourceStyle.bench(raw,p);
      int restored=0,lowerLeg=0,retainedSourceRgb=0;
      for(int y=0;y<raw.getHeight();y++)for(int x=0;x<raw.getWidth();x++){
        int before=raw.getPixel(x,y),after=fixed.getPixel(x,y);
        if(Color.alpha(before)==0&&Color.alpha(after)>0){
          restored++;if((before&0xffffff)!=0)retainedSourceRgb++;assertEquals("recover source metal RGB, never fabricate feet",before&0xffffff,after&0xffffff);
          if(y>110)lowerLeg++;
        }
      }
      assertTrue("the old key deleted metal supports",restored>70);
      assertTrue("PNG decoding retains real hidden RGB; black fabricated supports do not qualify",retainedSourceRgb>50);
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
  @Test public void replacementSpritesRetainDoorAndTrunkRegistrationWithoutGlow()throws Exception{
    AssetManager a=RuntimeEnvironment.getApplication().getAssets();MillesSourceStyle style=new MillesSourceStyle(a);
    Bitmap old;try(InputStream in=a.open("buildings/BLD_003_weapon_shop.png")){old=BitmapFactory.decodeStream(in);}
    Bitmap house=style.replacement(old,"buildings/BLD_003_weapon_shop.png");
    assertEquals(old.getWidth(),house.getWidth());assertEquals(old.getHeight(),house.getHeight());
    assertTrue("actual source-derived open threshold is opaque",Color.alpha(house.getPixel(97,214))>0);
    int c=house.getPixel(97,214);assertTrue("threshold is a dark open door",(Color.red(c)+Color.green(c)+Color.blue(c))/3<100);
    assertEquals("outside cabin is transparent",0,Color.alpha(house.getPixel(0,0)));
    for(int y=0;y<house.getHeight();y++)for(int x=0;x<house.getWidth();x++){int alpha=Color.alpha(house.getPixel(x,y));assertTrue("no generated glow alpha",alpha==0||alpha==255);}
    Bitmap treeOld;try(InputStream in=a.open("vegetation/trees/OBJ_tree_milles_willow.png")){treeOld=BitmapFactory.decodeStream(in);}
    Bitmap tree=style.replacement(treeOld,"vegetation/trees/OBJ_tree_milles_willow.png");
    assertEquals(0,Color.alpha(tree.getPixel(0,0)));int base=0;for(int y=tree.getHeight()-12;y<tree.getHeight();y++)for(int x=tree.getWidth()/2-12;x<tree.getWidth()/2+12;x++)if(Color.alpha(tree.getPixel(x,y))>0)base++;
    assertTrue("trunk remains at ground foot anchor",base>10);
  }

}
