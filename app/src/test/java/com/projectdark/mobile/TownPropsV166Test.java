package com.projectdark.mobile;

import android.content.Context;
import android.graphics.*;
import com.projectdark.mobile.world.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk=34,manifest=Config.NONE)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public class TownPropsV166Test {
 Context c;
 @Before public void reset(){
  c=RuntimeEnvironment.getApplication();
  for(String p:new String[]{"project_dark_f5m_v1","project_dark_skill_book_v1","project_dark_skill_test_v1"})c.getSharedPreferences(p,0).edit().clear().commit();
  F5mSaveStore.install(c);
 }
 @Test public void sharedFlowerbedHasNoCapturedBrightNeutralPavingAndRetainsNativeSize()throws Exception{
  int[] previous=null;
  for(String town:new String[]{"abel-video-v147","rucesion-v154"}){
   Bitmap b=BitmapFactory.decodeStream(c.getAssets().open(town+"/flowerbed_orange.png"));
   assertEquals(182,b.getWidth());assertEquals(109,b.getHeight());
   int[] pixels=new int[182*109];b.getPixels(pixels,0,182,0,0,182,109);
   int solid=0,neutralPaving=0;
   for(int pixel:pixels)if(Color.alpha(pixel)>128){
    solid++;int r=Color.red(pixel),g=Color.green(pixel),blue=Color.blue(pixel);
    if(Math.max(r,Math.max(g,blue))-Math.min(r,Math.min(g,blue))<35&&Math.min(r,Math.min(g,blue))>90)neutralPaving++;
   }
   // Old registered PNG had 1,745 bright low-chroma pavement pixels (16% of solid area).
   assertTrue("flower coverage retained",solid>9000);
   assertEquals("no bright neutral sidewalk fragments",0,neutralPaving);
   for(int x=0;x<182;x++){assertEquals(0,Color.alpha(b.getPixel(x,0)));assertEquals(0,Color.alpha(b.getPixel(x,108)));}
   if(previous!=null)assertArrayEquals("both towns consume the same repair",previous,pixels);
   previous=pixels;b.recycle();
  }
 }
 @Test public void captureAllTwentyOneFlowerbedsInActualTownRenderer()throws Exception{
  TownQualityV164Test helper=new TownQualityV164Test();helper.c=c;
  List<String> receipt=new ArrayList<>();int count=0;
  for(String map:new String[]{AbelTownGeometry.ID,RucesionTownGeometry.ID}){
   GameView v=new GameView(c);v.layout(0,0,960,540);helper.travel(v,map);
   WorldRuntimeAdapter w=TownInteriorTest.field(v,"poteFieldAdapter");
   List<float[]> spots=new ArrayList<>();
   if(AbelTownGeometry.ID.equals(map)){
    for(AbelTownGeometry.Prop p:AbelTownGeometry.PROPS)if(p.asset.equals("flowerbed_orange")){assertEquals(182*AbelVideoLayout.SCALE,p.width,.001f);assertEquals(92*AbelVideoLayout.SCALE,p.anchorX,.001f);spots.add(new float[]{p.x,p.y});}
   }else{
    for(RucesionTownGeometry.Prop p:RucesionTownGeometry.PROPS)if(p.asset.equals("flowerbed_orange")){assertEquals(109*AbelVideoLayout.SCALE,p.height,.001f);assertEquals(98*AbelVideoLayout.SCALE,p.anchorY,.001f);spots.add(new float[]{p.x,p.y});}
   }
   assertEquals(map,AbelTownGeometry.ID.equals(map)?16:5,spots.size());
   for(int i=0;i<spots.size();i++){
    float[] xy=spots.get(i);w.camera().snapTo(xy[0],xy[1]-70);
    Bitmap shot=Bitmap.createBitmap(960,540,Bitmap.Config.ARGB_8888);v.draw(new Canvas(shot));
    Path f=Path.of("build/reports/town-v166/"+map+"-flowerbed-"+i+".png");Files.createDirectories(f.getParent());
    try(java.io.OutputStream out=Files.newOutputStream(f)){assertTrue(shot.compress(Bitmap.CompressFormat.PNG,100,out));}
    shot.recycle();receipt.add(map+" flowerbed="+i+" x="+xy[0]+" y="+xy[1]);count++;
   }
  }
  assertEquals(11,count);Files.write(Path.of("build/reports/town-v166/placements.txt"),receipt);
 }
}
