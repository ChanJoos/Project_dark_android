package com.projectdark.mobile;
import android.graphics.*;
import com.projectdark.mobile.world.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.*;
import java.nio.file.Path;
import java.nio.file.Files;
import java.util.*;
import static org.junit.Assert.*;
/** Independent same-frame original envelopes and missing opaque-jelly regression. */
@RunWith(RobolectricTestRunner.class) @Config(sdk=34,manifest=Config.NONE)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public class AbelSourceV161Test extends CoastRefinementV158Test {
 @Before public void output161(){out=Path.of("build/reports/abel-v161");}
 @Test public void allThirteenNativeSilhouettesUseRecordedOriginalPlayerRatios()throws Exception{
  PoteFieldRenderer renderer=new PoteFieldRenderer();CharacterRenderer player=new CharacterRenderer(c);
  CharacterVisualBinding v=CharacterVisualBinding.from(new RuntimeState().rpg());
  Bitmap p=Bitmap.createBitmap(260,240,Bitmap.Config.ARGB_8888);
  player.draw(new Canvas(p),new CharacterRenderer.Pose(90,190,CharacterRenderer.Direction.SW,CharacterRenderer.State.IDLE,0,0,1,false,v.equipmentVisualRef(),v.weaponVisualRef(),CharacterRenderer.ASSET_STATUS,CharacterRenderer.EffectFamily.NONE));
  float ph=bounds(p).height();assertTrue(ph>=45&&ph<=60);
  org.json.JSONArray rows=new org.json.JSONObject(Files.readString(Path.of("../master/source/abel_v161/calibration.json"))).getJSONArray("rows");
  assertEquals(13,rows.length());Set<String> ids=new HashSet<>();
  for(int i=0;i<rows.length();i++){
   org.json.JSONObject row=rows.getJSONObject(i);String id=row.getString("id");assertTrue(ids.add(id));
   Bitmap nativePose=Bitmap.createBitmap(300,240,Bitmap.Config.ARGB_8888);
   renderer.drawMonsterTestPose(new Canvas(nativePose),id,"idle",CharacterRenderer.Direction.SW,0,0,150,190);
   Rect b=bounds(nativePose);assertTrue(id,b.width()>15&&b.height()>15);
   if(!row.isNull("observation")){
    org.json.JSONObject observation=row.getJSONObject("observation");
    assertEquals(id+" visible width/source-player",observation.getDouble("envelopeWidthRatio"),b.width()/ph,.13);
    assertEquals(id+" visible height/source-player",observation.getDouble("envelopeHeightRatio"),b.height()/ph,.13);
   }
   System.out.println("V161_NATIVE_RATIO "+id+" "+b.width()/ph+" "+b.height()/ph);
   save(nativePose,id+"-native-idle");nativePose.recycle();
  }
  assertEquals(new HashSet<>(AbelCoastMonsters.ALL),ids);
 }
 @Test public void pongIsFilledAndAllSevenNewFamiliesHaveTwelveIndependentPaddedPoses()throws Exception{
  String[] families={"ABEL_TURTLE","ABEL_PONG","ABEL_CATERPILLAR","ABEL_ARKPUS","ABEL_OBS_SLATE","ABEL_OCTOPUS","ABEL_OBS_PURPLE_MAW"};
  for(String id:families){Set<Integer> hashes=new HashSet<>();
   for(String pose:new String[]{"idle","walk","attack"})for(CharacterRenderer.Direction direction:CharacterRenderer.Direction.values()){
    String path=PoteForestMonsterShowcase.assetPath(id,pose,direction);assertTrue(path,path.contains("/abel_v161/"));
    Bitmap b=BitmapFactory.decodeStream(c.getAssets().open(path));assertNotNull(path,b);assertEquals(192,b.getWidth());assertEquals(192,b.getHeight());
    Rect box=bounds(b);assertTrue(path,box.left>=7&&box.top>=7&&box.right<=185&&box.bottom<=185);
    int[] pixels=new int[192*192];b.getPixels(pixels,0,192,0,0,192,192);assertTrue(path,hashes.add(Arrays.hashCode(pixels)));
    if(id.equals("ABEL_PONG")){
     int filled=0,total=0;for(int y=box.top;y<box.bottom;y++)for(int x=box.left;x<box.right;x++){total++;if(Color.alpha(b.getPixel(x,y))>128)filled++;}
     assertTrue("opaque violet jelly rather than empty outline "+path,filled>total*.60f);
     assertTrue("filled centre "+path,Color.alpha(b.getPixel(box.centerX(),box.centerY()))>200);
    }
    b.recycle();
   }
   assertEquals(12,hashes.size());
  }
 }
}
