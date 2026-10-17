package com.projectdark.mobile;
import android.content.Context;
import android.graphics.*;
import com.projectdark.mobile.world.*;
import org.junit.*;import org.junit.runner.RunWith;import org.robolectric.*;import org.robolectric.annotation.*;
import java.nio.file.Files;import java.nio.file.Path;import java.util.*;import java.lang.reflect.Method;
import static org.junit.Assert.*;
@RunWith(RobolectricTestRunner.class) @Config(sdk=34,manifest=Config.NONE) @GraphicsMode(GraphicsMode.Mode.NATIVE)
public class TownQualityV164Test {
 Context c;
 @Before public void reset(){c=RuntimeEnvironment.getApplication();for(String p:new String[]{"project_dark_f5m_v1","project_dark_skill_book_v1","project_dark_skill_test_v1"})c.getSharedPreferences(p,0).edit().clear().commit();F5mSaveStore.install(c);}
 void travel(GameView v,String map)throws Exception{Method m=GameView.class.getDeclaredMethod("travelTown",String.class);m.setAccessible(true);m.invoke(v,map);}
 void save(Bitmap b,String name)throws Exception{Path p=Path.of("build/reports/town-v164/"+name+".png");Files.createDirectories(p.getParent());try(java.io.OutputStream out=Files.newOutputStream(p)){assertTrue(b.compress(Bitmap.CompressFormat.PNG,100,out));}}
 void capture(GameView v,String name)throws Exception{Bitmap b=Bitmap.createBitmap(960,540,Bitmap.Config.ARGB_8888);v.draw(new Canvas(b));save(b,name);b.recycle();}
 @Test public void everyTownHidesItselfAndUsesOnlyVisibleCardsWithSafeGapsCloseAndForest()throws Exception{
  TownCampaignV163Test helper=new TownCampaignV163Test();helper.c=c;
  for(TownTravel.Destination from:TownTravel.TOWNS){GameView v=new GameView(c);v.layout(0,0,960,540);v.setSkillTestMode(false);travel(v,from.map);RuntimeState s=TownInteriorTest.field(v,"state");helper.approach(v,from.npc);assertEquals(WorldDef.ID.equals(from.map)?4:3,TownTravelWindow.destinations(from.map).size());assertFalse(TownTravelWindow.destinations(from.map).contains(from.map));assertNull(TownTravelWindow.cardFor(from.map,from.map));capture(v,"dialog-"+from.map);
   // Empty card gutters must not select a destination or dismiss the dialogue.
   TownInteriorTest.tap(v,480,258);assertEquals(from.map,s.currentMapId());InteractionController dialog=TownInteriorTest.field(v,"interaction");assertTrue(dialog.dialogOpen());TownInteriorTest.tap(v,679,162);assertFalse(dialog.dialogOpen());assertEquals(from.map,s.currentMapId());
   v.setSkillTestMode(true);helper.approach(v,from.npc);String hunt=TownTravelWindow.hunting(from.map);RectF forest=TownTravelWindow.cardFor(from.map,hunt);TownInteriorTest.tap(v,forest.centerX(),forest.centerY());assertEquals(hunt,s.currentMapId());assertEquals(1,s.rpg().normalLevel().intValue());
  }
 }
 @Test public void allLoadedPropsHaveAssetsAndNativeRepairPreservesCanvasGuttersAndContact()throws Exception{
  Set<String> files=new TreeSet<>();for(AbelTownGeometry.Prop p:AbelTownGeometry.PROPS)files.add("abel-video-v147/"+p.asset+".png");for(RucesionTownGeometry.Prop p:RucesionTownGeometry.PROPS)files.add("rucesion-v154/"+p.asset+".png");
  for(String file:files){Bitmap b=BitmapFactory.decodeStream(c.getAssets().open(file));assertNotNull(file,b);assertTrue(b.getWidth()>0&&b.getHeight()>0);b.recycle();}
  for(String file:new String[]{"abel-video-v147/tree_broadleaf.png","rucesion-v154/tree_broadleaf.png","rucesion-v154/pergola_garden.png","abel-video-v147/bench_front_up.png","abel-video-v147/bench_front_down.png","abel-video-v147/bench_back_up.png","abel-video-v147/bench_back_down.png"}){Bitmap b=BitmapFactory.decodeStream(c.getAssets().open(file));for(int x=0;x<b.getWidth();x++){assertEquals(file,0,Color.alpha(b.getPixel(x,0)));assertEquals(file,0,Color.alpha(b.getPixel(x,b.getHeight()-1)));}b.recycle();}
 }
 @Test public void actualNativeTownCamerasCoverGardensTreesBenchesAndPergolaWithBoundedCache()throws Exception{
  for(String map:new String[]{AbelTownGeometry.ID,RucesionTownGeometry.ID}){GameView v=new GameView(c);v.layout(0,0,960,540);travel(v,map);RuntimeState s=TownInteriorTest.field(v,"state");WorldRuntimeAdapter w=TownInteriorTest.field(v,"poteFieldAdapter");
   Set<String> seen=new HashSet<>();List<float[]> spots=new ArrayList<>();
   if(AbelTownGeometry.ID.equals(map)){for(AbelTownGeometry.Prop p:AbelTownGeometry.PROPS)if(seen.add(p.asset))spots.add(new float[]{p.x,p.y});}else for(RucesionTownGeometry.Prop p:RucesionTownGeometry.PROPS)if(seen.add(p.asset))spots.add(new float[]{p.x,p.y});
   int i=0;for(float[] spot:spots){w.camera().snapTo(spot[0],spot[1]-70);capture(v,map+"-family-"+(i++));}
   assertTrue(F5mSaveStore.checkpointActive());
  }
 }
}
