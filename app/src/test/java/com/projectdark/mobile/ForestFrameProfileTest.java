package com.projectdark.mobile;
import android.graphics.*;
import com.projectdark.mobile.world.*;
import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import org.json.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.*;
import static org.junit.Assert.*;
/** Identical harness is run against V115 and the repair, with actual update and native drawing. */
@RunWith(RobolectricTestRunner.class) @Config(sdk=34,manifest=Config.NONE) @GraphicsMode(GraphicsMode.Mode.NATIVE)
public class ForestFrameProfileTest {
 @Test public void movingNearCanopiesProfilesUpdateAndRenderSeparately()throws Exception{
  for(String n:new String[]{"project_dark_f5m_v1","project_dark_journal_v1","project_dark_skill_test_v1"})RuntimeEnvironment.getApplication().getSharedPreferences(n,0).edit().clear().commit();
  F5mSaveStore.install(RuntimeEnvironment.getApplication());GameView view=new GameView(RuntimeEnvironment.getApplication());view.layout(0,0,1536,864);
  Method enter=GameView.class.getDeclaredMethod("enterPoteField"),change=GameView.class.getDeclaredMethod("changeCampaignMap",String.class,boolean.class),update=GameView.class.getDeclaredMethod("update",float.class);
  enter.setAccessible(true);change.setAccessible(true);update.setAccessible(true);enter.invoke(view);
  Method placements=PoteFieldRenderer.class.getDeclaredMethod("placementsForMap",String.class);placements.setAccessible(true);
  JSONArray scenes=new JSONArray();
  for(String map:new String[]{"MAP_POTE_01","MAP_POTE_03"}){
   change.invoke(view,map,false);RuntimeState state=TownInteriorTest.field(view,"state");WorldRuntimeAdapter world=TownInteriorTest.field(view,"poteFieldAdapter");
   Object canopy=null;double best=Double.MAX_VALUE;
   for(Object p:(List<?>)placements.invoke(null,map)){
    Field role=p.getClass().getDeclaredField("role"),x=p.getClass().getDeclaredField("x"),y=p.getClass().getDeclaredField("y");role.setAccessible(true);x.setAccessible(true);y.setAccessible(true);
    if(!"canopy".equals(role.get(p)))continue;double d=Math.hypot(x.getFloat(p)-2000,y.getFloat(p)-1100);if(d<best){best=d;canopy=p;}
   }assertNotNull(canopy);
   Field x=canopy.getClass().getDeclaredField("x"),y=canopy.getClass().getDeclaredField("y");x.setAccessible(true);y.setAccessible(true);
   PoteCampaignMapDef def=PoteCampaignMapDef.forId(map);WorldMoveTargetController.TileCenter start=def.nearest(x.getFloat(canopy),y.getFloat(canopy)-80),end=def.nearest(start.x+192,start.y+96);
   state.player().x=start.x;state.player().y=start.y;world.cancelForAction();world.snapCameraToPlayer();
   assertEquals(WorldMoveTargetController.Status.MOVING,world.requestGroundWorld(end.x,end.y).status);
   Bitmap image=Bitmap.createBitmap(1536,864,Bitmap.Config.ARGB_8888);Canvas canvas=new Canvas(image);long[] updateNs=new long[100],renderNs=new long[100];
   float beforeX=world.presentationPlayerX(),beforeY=world.presentationPlayerY();int movingSamples=0;double sampledTravel=0;
   for(int frame=0;frame<140;frame++){
    float previousX=world.presentationPlayerX(),previousY=world.presentationPlayerY();
    long a=System.nanoTime();update.invoke(view,.016f);world.camera().follow(world.presentationPlayerX(),world.presentationPlayerY());long b=System.nanoTime();view.draw(canvas);long c=System.nanoTime();
    if(frame>=40){updateNs[frame-40]=b-a;renderNs[frame-40]=c-b;double travel=Math.hypot(world.presentationPlayerX()-previousX,world.presentationPlayerY()-previousY);sampledTravel+=travel;if(travel>.001)movingSamples++;}
   }
   double moved=Math.hypot(world.presentationPlayerX()-beforeX,world.presentationPlayerY()-beforeY);assertTrue("measured frames really move near canopy "+map,movingSamples>=75&&sampledTravel>10);
   Arrays.sort(updateNs);Arrays.sort(renderNs);
   JSONObject stages=new JSONObject();PoteFieldRenderer renderer=TownInteriorTest.field(view,"poteFieldRenderer");
   for(String methodName:new String[]{"drawFloor","drawCreekBed"}){
    Method stage=PoteFieldRenderer.class.getDeclaredMethod(methodName,Canvas.class,WorldRuntimeAdapter.class);stage.setAccessible(true);long[] samples=new long[30];
    canvas.save();float scale=1536/world.camera().viewportWidth();canvas.scale(scale,scale);
    for(int i=0;i<30;i++){long begin=System.nanoTime();stage.invoke(renderer,canvas,world);samples[i]=System.nanoTime()-begin;}canvas.restore();Arrays.sort(samples);stages.put(methodName+"MedianMs",samples[15]/1e6);
   }
   scenes.put(new JSONObject().put("stages",stages).put("map",map).put("monsters",state.monsters().size()).put("movedPixels",moved).put("movingSamples",movingSamples).put("sampledTravelPixels",sampledTravel).put("updateMedianMs",updateNs[50]/1e6).put("updateP95Ms",updateNs[95]/1e6).put("renderMedianMs",renderNs[50]/1e6).put("renderP95Ms",renderNs[95]/1e6));image.recycle();
  }
  JSONObject out=new JSONObject().put("source",System.getenv("PROJECT_DARK_SOURCE_SHA")).put("environment","Robolectric SDK34 native CPU, not phone FPS").put("warmFrames",40).put("samplesPerMap",100).put("scenes",scenes);
  File file=new File("build/reports/forest-performance/PROFILE.json");file.getParentFile().mkdirs();try(Writer writer=new FileWriter(file)){writer.write(out.toString(2));}System.out.println(out.toString());
 }
}
