package com.projectdark.mobile;
import android.content.Context;import android.graphics.*;import com.projectdark.mobile.world.*;
import java.io.*;import java.util.*;import java.lang.reflect.Method;
import org.json.*;import org.junit.*;import org.junit.runner.RunWith;import org.robolectric.*;import org.robolectric.annotation.*;import static org.junit.Assert.*;
@RunWith(RobolectricTestRunner.class) @Config(sdk=34,manifest=Config.NONE) @GraphicsMode(GraphicsMode.Mode.NATIVE)
public class MonsterDensityV222Test {
 static float radius(RuntimeState.Monster m){String s=m.id.split("#",2)[0];if(s.startsWith("LYK_"))return s.equals("LYK_ARIEL")||s.equals("LYK_TYRANT")||s.endsWith("KOATL")?18:14;return m.id.equals("milles_mouse_proto")?7:s.equals("POTE_MANTIS")?18:s.equals("POTE_LYCAN")?16:12;}
 static boolean pointOracle(RuntimeState s,float x,float y){for(RuntimeState.Monster m:s.monsters())if(m.alive){float r=10+radius(m)+5;if(Math.hypot(x-m.x,y-m.y)<r||m.isMoving&&Math.hypot(x-m.moveTargetX,y-m.moveTargetY)<r)return false;}for(RuntimeState.Npc n:s.npcs())if(Math.hypot(x-n.x,y-n.y)<25)return false;return true;}
 @Test public void liveActorPointSegmentAndReservationResultsMatchIndependentOracle(){
  RuntimeState s=new RuntimeState();s.enterCampaignMap("MAP_DEATH_UNDER_C",false);Random random=new Random(222);
  for(RuntimeState.Monster m:s.monsters()){m.isMoving=random.nextBoolean();m.moveTargetX=m.x+32;m.moveTargetY=m.y+16;}
  for(int i=0;i<500;i++){
   RuntimeState.Monster near=s.monsters().get(i%s.monsters().size());float ax=near.x+random.nextInt(181)-90,ay=near.y+random.nextInt(181)-90,bx=ax+32,by=ay+16;
   assertEquals(pointOracle(s,ax,ay),s.canPlayerOccupyActors(ax,ay));if(!pointOracle(s,ax,ay))continue;
   boolean clear=pointOracle(s,bx,by);
   for(RuntimeState.Monster m:s.monsters())if(m.alive){double distance=m.isMoving?segmentDistance(ax,ay,bx,by,m.x,m.y,m.moveTargetX,m.moveTargetY):pointSegment(ax,ay,bx,by,m.x,m.y);float r=10+radius(m)+5;if(distance<r*r)clear=false;}
   assertEquals("stationary/moving/crossing live actors "+i,clear,s.canPlayerTraverseActors(ax,ay,bx,by));
  }
 }
 static final class HostOracle {
  static final Method POINT,INTERSECT;
  static{try{Class<?> c=Class.forName("java.awt.geom.Line2D");POINT=c.getMethod("ptSegDistSq",double.class,double.class,double.class,double.class,double.class,double.class);INTERSECT=c.getMethod("linesIntersect",double.class,double.class,double.class,double.class,double.class,double.class,double.class,double.class);}catch(Exception e){throw new ExceptionInInitializerError(e);}}
 }
 static double pointSegment(double ax,double ay,double bx,double by,double px,double py){try{return (Double)HostOracle.POINT.invoke(null,ax,ay,bx,by,px,py);}catch(Exception e){throw new AssertionError(e);}}
 static double segmentDistance(float ax,float ay,float bx,float by,float cx,float cy,float dx,float dy){try{if((Boolean)HostOracle.INTERSECT.invoke(null,(double)ax,(double)ay,(double)bx,(double)by,(double)cx,(double)cy,(double)dx,(double)dy))return 0;}catch(Exception e){throw new AssertionError(e);}return Math.min(Math.min(pointSegment(ax,ay,bx,by,cx,cy),pointSegment(ax,ay,bx,by,dx,dy)),Math.min(pointSegment(cx,cy,dx,dy,ax,ay),pointSegment(cx,cy,dx,dy,bx,by)));}
 static final class HostAllocation {
  final Object bean;final Method bytes;final boolean supported;
  HostAllocation()throws Exception{Class<?> factory=Class.forName("java.lang.management.ManagementFactory"),api=Class.forName("com.sun.management.ThreadMXBean");bean=factory.getMethod("getThreadMXBean").invoke(null);supported=(Boolean)api.getMethod("isThreadAllocatedMemorySupported").invoke(bean);if(supported)api.getMethod("setThreadAllocatedMemoryEnabled",boolean.class).invoke(bean,true);bytes=api.getMethod("getThreadAllocatedBytes",long.class);}
  long read()throws Exception{return supported?(Long)bytes.invoke(bean,Thread.currentThread().getId()):-1;}
 }
 @Test public void denseLiveFramesAndActorQueriesRetainAll72_36_100ActorsAndReportCpuAndAllocation()throws Exception{
  MineTownV203Test f=new MineTownV203Test();f.reset();Context c=RuntimeEnvironment.getApplication();GameView v=new GameView(c);v.layout(0,0,960,540);RuntimeState s=f.field(v,"state");f.call(v,"travelTown",new Class<?>[]{String.class},DeathVillageGeometry.LOBBY);
  Bitmap frame=Bitmap.createBitmap(960,540,Bitmap.Config.ARGB_8888);Canvas canvas=new Canvas(frame);JSONArray results=new JSONArray();
  HostAllocation allocation=new HostAllocation();String phase=System.getenv("PROJECT_DARK_PROFILE_PHASE");if(phase==null)phase="after";
  for(String id:new String[]{DeathVillageGeometry.FIELD,"MAP_DEATH_UNDER_C",MineHuntGeometry.ID}){
   if(DeathVillageGeometry.contains(id)){DeathVillageData.Gate inbound=DeathVillageData.GATES.stream().filter(g->g.to.equals(id)).findFirst().get();f.call(v,"changeCampaignMap",new Class<?>[]{String.class,boolean.class,DeathVillageData.Gate.class},id,false,inbound);}else f.call(v,"changeCampaignMap",new Class<?>[]{String.class,boolean.class},id,false);WorldRuntimeAdapter w=f.field(v,"poteFieldAdapter");int population=s.monsters().size();assertEquals(id,DeathVillageGeometry.FIELD.equals(id)?72:MineHuntGeometry.ID.equals(id)?100:36,population);
   // Gather a legal dense group without deleting actors or overlapping their starting footprints.
   List<WorldMoveTargetController.TileCenter> tiles=new ArrayList<>(w.navigationTiles());float px=s.player().x,py=s.player().y;tiles.sort(Comparator.comparingDouble(t->Math.hypot(t.x-px,t.y-py)));List<WorldMoveTargetController.TileCenter> assigned=new ArrayList<>();
   for(WorldMoveTargetController.TileCenter t:tiles){if(Math.hypot(t.x-px,t.y-py)<60)continue;boolean free=true;for(WorldMoveTargetController.TileCenter p:assigned)if(Math.hypot(t.x-p.x,t.y-p.y)<58){free=false;break;}if(free)assigned.add(t);if(assigned.size()==population)break;}
   assertEquals(population,assigned.size());for(int i=0;i<population;i++){RuntimeState.Monster m=s.monsters().get(i);m.x=assigned.get(i).x;m.y=assigned.get(i).y;m.isMoving=false;m.provoked=true;}
   long allocStart=allocation.read();long queryStart=System.nanoTime();int accepted=0;
   for(int i=0;i<6000;i++){RuntimeState.Monster m=s.monsters().get(i%population);if(s.canPlayerOccupyActors(m.x+80+(i%7),m.y+40))accepted++;}
   double queryMs=(System.nanoTime()-queryStart)/1e6;long queryAlloc=allocation.supported?allocation.read()-allocStart:-1;
   int frames=phase.equals("before")?0:90;long[] update=new long[frames],draw=new long[frames];for(int i=0;i<frames;i++){
    s.player().alive=true;s.player().hp=s.player().maxHp;long start=System.nanoTime();F5mSaveStore.beginFrame();try{f.call(v,"update",new Class<?>[]{float.class},.016f);}finally{F5mSaveStore.endFrame();}long middle=System.nanoTime();assertEquals("real inbound gate must preserve dense scene",id,s.currentMapId());v.draw(canvas);update[i]=middle-start;draw[i]=System.nanoTime()-middle;
   }
   assertEquals(population,s.monsters().size());JSONObject row=new JSONObject().put("map",id).put("population",population).put("acceptedQueries",accepted).put("queries6000ms",queryMs).put("queryAllocatedBytes",queryAlloc).put("fullFrames",frames).put("update90IncludingCold",frames==0?JSONObject.NULL:HudInventoryPerformanceV218Test.stats(update)).put("draw90IncludingCold",frames==0?JSONObject.NULL:HudInventoryPerformanceV218Test.stats(draw));results.put(row);
  }
  frame.recycle();JSONObject report=new JSONObject().put("phase",phase).put("scope","same current map/spawn/HP; only RuntimeState and species parsing compared to V221; paired queries, full AI frames after only; host CPU, not phone FPS").put("scenes",results);File out=new File("build/reports/forest-performance/v222/"+phase+"-density.json");out.getParentFile().mkdirs();try(FileWriter writer=new FileWriter(out)){writer.write(report.toString(2));}System.out.println("V222_DENSITY "+report);
 }
}
