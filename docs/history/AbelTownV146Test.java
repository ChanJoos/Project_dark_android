package com.projectdark.mobile;
import android.content.Context;import android.graphics.Bitmap;import android.graphics.Canvas;import com.projectdark.mobile.world.*;import org.junit.*;import org.junit.runner.RunWith;import org.robolectric.*;import org.robolectric.annotation.*;import java.nio.file.Files;import java.nio.file.Paths;import java.io.OutputStream;import java.lang.reflect.Method;import java.util.*;import static org.junit.Assert.*;
@RunWith(RobolectricTestRunner.class) @Config(sdk=34,manifest=Config.NONE) @GraphicsMode(GraphicsMode.Mode.NATIVE)
public class AbelTownV146Test {
 Context c;
 @Before public void reset(){c=RuntimeEnvironment.getApplication();for(String key:new String[]{"project_dark_f5m_v1","project_dark_skill_book_v1","project_dark_skill_test_v1"})c.getSharedPreferences(key,0).edit().clear().commit();F5mSaveStore.install(c);}
 Object call(GameView v,String name,Class<?>[] types,Object...args)throws Exception{Method m=GameView.class.getDeclaredMethod(name,types);m.setAccessible(true);return m.invoke(v,args);}
 Object call(GameView v,String name)throws Exception{return call(v,name,new Class<?>[]{});}
 RuntimeState state(GameView v)throws Exception{return TownInteriorTest.field(v,"state");}
 GameView town()throws Exception{GameView v=new GameView(c);v.layout(0,0,960,540);AbelCoastV139Test.restoreD04(state(v).rpg(),"MARTIAL_ARTIST");call(v,"enterPoteField");call(v,"changeCampaignMap",new Class<?>[]{String.class,boolean.class},AbelTownGeometry.ID,false);return v;}
 void save(Bitmap b,String name)throws Exception{java.nio.file.Path p=Paths.get("build/reports/abel-v146/"+name+".png");Files.createDirectories(p.getParent());try(OutputStream out=Files.newOutputStream(p)){assertTrue(b.compress(Bitmap.CompressFormat.PNG,100,out));}}
 void capture(GameView v,String name)throws Exception{Bitmap b=Bitmap.createBitmap(960,540,Bitmap.Config.ARGB_8888);v.draw(new Canvas(b));save(b,name);}
 @Test public void completeBenchSupportsRemainVisibleBelowSeatAndFeetHaveTransparentGroundGutter()throws Exception{
  AbelTownRenderer r=new AbelTownRenderer(c);Method image=AbelTownRenderer.class.getDeclaredMethod("image",String.class);image.setAccessible(true);Set<String> views=new HashSet<>();
  for(AbelTownGeometry.Prop p:AbelTownGeometry.PROPS)if(p.asset.startsWith("bench_")){
   assertFalse(p.mirror);assertTrue(AbelTownGeometry.benchCell(p.u,p.v));int du=p.asset.equals("bench_se")?1:p.asset.equals("bench_nw")?-1:0,dv=p.asset.equals("bench_sw")?1:p.asset.equals("bench_ne")?-1:0;assertTrue(AbelTownGeometry.pool(p.u+du,p.v+dv));
   if(!views.add(p.asset))continue;Bitmap b=(Bitmap)image.invoke(r,p.asset);int dark=0;for(int y=48;y<b.getHeight();y++)for(int x=0;x<b.getWidth();x++){int color=b.getPixel(x,y);if((color>>>24)>0&&((color>>16)&255)<100&&((color>>8)&255)<110)dark++;}assertTrue(p.asset+" connected support pixels below seat: "+dark,dark>=45);for(int x=0;x<b.getWidth();x++)assertEquals("foot must leave a transparent ground gutter",0,b.getPixel(x,b.getHeight()-1)>>>24);save(b,p.asset);
  }assertEquals(4,views.size());
 }
 @Test public void allServiceDoorsHaveActualEntryExitRestartAndPreserveProgress()throws Exception{
  for(TownInteriorDef room:TownInteriorDef.ABEL){reset();GameView v=town();RuntimeState s=state(v);long exp=CampaignProgress.earnedExp(s.rpg()),gold=s.rpg().gold();AbelTownGeometry.Building b=AbelTownGeometry.forInterior(room.mapId);WorldRuntimeAdapter w=TownInteriorTest.field(v,"poteFieldAdapter");assertTrue(b.id,CampaignNavigationTest.walk(w,w.requestGroundWorld(b.doorX(),b.doorY())));capture(v,b.id);assertEquals(true,call(v,"checkCampaignGate"));assertEquals(room.mapId,s.currentMapId());capture(v,room.mapId);assertTrue(F5mSaveStore.checkpointActive());F5mSaveStore.install(c);GameView restored=new GameView(c);restored.layout(0,0,960,540);assertEquals(room.mapId,state(restored).currentMapId());call(restored,"leaveReagentShop");RuntimeState other=state(restored);assertEquals(AbelTownGeometry.ID,other.currentMapId());assertNull(AbelTownGeometry.doorAt(other.player().x,other.player().y));assertFalse(other.blocked(other.player().x,other.player().y));assertEquals(exp,CampaignProgress.earnedExp(other.rpg()));assertEquals(gold,other.rpg().gold().longValue());}
 }
 @Test public void allDistrictsAndWholeNativeSceneryUseTheSameGeometryAndBoundedCache()throws Exception{
  GameView v=town();RuntimeState s=state(v);assertTrue(s.monsters().isEmpty());WorldRuntimeAdapter w=TownInteriorTest.field(v,"poteFieldAdapter");AbelTownRenderer r=TownInteriorTest.field(v,"abelTownRenderer");
  int i=0;for(int u:new int[]{7,18,28,38})for(int vv:new int[]{7,18,25,33,39}){WorldMoveTargetController.TileCenter t=CampaignWorld.nearestConnectedTile(AbelTownGeometry.ID,AbelTownGeometry.x(u,vv),AbelTownGeometry.y(u,vv));s.player().x=t.x;s.player().y=t.y;w.snapCameraToPlayer();capture(v,"district-"+(i++));assertTrue(r.floorCacheEntries()<=32);assertTrue(r.floorCachePixelBytes()<=8652800);}
  PoteCampaignMapDef d=PoteCampaignMapDef.forId(AbelTownGeometry.ID);WorldRuntimeAdapter full=new WorldRuntimeAdapter(s,3008,1536,d.minX,d.maxX,d.minY,d.maxY,d.navigationTiles(),d.obstacles(),true);Bitmap overview=Bitmap.createBitmap(3008,1536,Bitmap.Config.ARGB_8888);r.draw(new Canvas(overview),full,Collections.emptyList());save(overview,"whole-native-scenery");assertTrue(r.floorCacheEntries()<=32);
 }
 @Test public void twelveTownCoastRoundTripsAndTownRestartRetainProgress()throws Exception{
  GameView v=town();RuntimeState s=state(v);long exp=CampaignProgress.earnedExp(s.rpg());for(int i=0;i<12;i++){WorldMoveTargetController.TileCenter t=CampaignWorld.forward(AbelTownGeometry.ID);s.player().x=t.x;s.player().y=t.y;assertEquals(true,call(v,"checkCampaignGate"));assertEquals(AbelCoastGeometry.ENTRY,s.currentMapId());t=CampaignWorld.backward(s.currentMapId());s.player().x=t.x;s.player().y=t.y;assertEquals(true,call(v,"checkCampaignGate"));assertEquals(AbelTownGeometry.ID,s.currentMapId());}assertTrue(F5mSaveStore.checkpointActive());F5mSaveStore.install(c);GameView restored=new GameView(c);assertEquals(AbelTownGeometry.ID,state(restored).currentMapId());assertEquals(exp,CampaignProgress.earnedExp(state(restored).rpg()));
 }
}
