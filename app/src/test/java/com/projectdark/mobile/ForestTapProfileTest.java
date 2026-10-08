package com.projectdark.mobile;
import android.view.MotionEvent;
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
/** Exact same DOWN/UP callbacks and actor/collision state on V116 and V117. */
@RunWith(RobolectricTestRunner.class) @Config(sdk=34,manifest=Config.NONE)
public class ForestTapProfileTest {
 @Test public void realForestTouchesAndLongRoutesAreProfiled()throws Exception{
  for(String n:new String[]{"project_dark_f5m_v1","project_dark_journal_v1","project_dark_skill_test_v1"})RuntimeEnvironment.getApplication().getSharedPreferences(n,0).edit().clear().commit();
  F5mSaveStore.install(RuntimeEnvironment.getApplication());GameView v=new GameView(RuntimeEnvironment.getApplication());v.layout(0,0,960,540);
  Method enter=GameView.class.getDeclaredMethod("enterPoteField"),change=GameView.class.getDeclaredMethod("changeCampaignMap",String.class,boolean.class),update=GameView.class.getDeclaredMethod("update",float.class),hud=GameView.class.getDeclaredMethod("isHudSurface",float.class,float.class);
  for(Method m:new Method[]{enter,change,update,hud})m.setAccessible(true);enter.invoke(v);JSONArray scenes=new JSONArray();
  for(String map:new String[]{"MAP_POTE_01","MAP_POTE_02","MAP_POTE_03","MAP_POTE_D_BOSS"}){
   change.invoke(v,map,false);RuntimeState state=TownInteriorTest.field(v,"state");WorldRuntimeAdapter w=TownInteriorTest.field(v,"poteFieldAdapter");ArrayList<WorldMoveTargetController.TileCenter> visible=new ArrayList<>();
   for(WorldMoveTargetController.TileCenter t:w.navigationTiles()){WorldCameraTransform.Point p=w.worldToScreen(t.x,t.y);if(p.x<310||p.x>650||p.y<210||p.y>360||(Boolean)hud.invoke(v,p.x,p.y)||state.hitNpc(t.x,t.y,34)!=null||state.hitMonster(t.x,t.y,34)!=null||!w.canPlayerOccupy(t.x,t.y))continue;visible.add(t);}
   assertTrue(map+" visible empty ground",visible.size()>8);JSONArray taps=new JSONArray();
   for(int i=0;i<8;i++){WorldMoveTargetController.TileCenter t=visible.get(i*(visible.size()-1)/7);WorldCameraTransform.Point p=w.worldToScreen(t.x,t.y);long before=w.movement().snapshot().requestId;long a=System.nanoTime();MotionEvent down=MotionEvent.obtain(0,0,MotionEvent.ACTION_DOWN,p.x,p.y,0),up=MotionEvent.obtain(0,1,MotionEvent.ACTION_UP,p.x,p.y,0);v.onTouchEvent(down);v.onTouchEvent(up);down.recycle();up.recycle();long ns=System.nanoTime()-a;WorldMoveTargetController.Snapshot s=w.movement().snapshot();assertTrue("real ground callback must create movement "+map,s.requestId>before);assertTrue(s.kind==WorldMoveTargetController.RequestKind.GROUND);taps.put(new JSONObject().put("ms",ns/1e6).put("x",s.targetX).put("y",s.targetY).put("status",s.status.name()).put("steps",s.remainingWaypoints));}
   final int[] calls={0,0};WorldMoveTargetController.NavigationWorld counted=new WorldMoveTargetController.NavigationWorld(){public List<WorldMoveTargetController.TileCenter> navigationTiles(){return w.navigationTiles();}public boolean canPlayerOccupy(float x,float y){calls[0]++;return w.canPlayerOccupy(x,y);}public boolean canPlayerTraverse(float ax,float ay,float bx,float by){calls[1]++;return w.canPlayerTraverse(ax,ay,bx,by);}};
   WorldMoveTargetController planner=new WorldMoveTargetController(counted,w);JSONArray routes=new JSONArray();List<WorldMoveTargetController.TileCenter> cells=w.navigationTiles();
   for(int i:new int[]{cells.size()/4,cells.size()/2,3*cells.size()/4,cells.size()-1}){WorldMoveTargetController.TileCenter t=cells.get(i);calls[0]=calls[1]=0;long a=System.nanoTime();WorldMoveTargetController.Snapshot s=planner.requestGroundMove(t.x,t.y);routes.put(new JSONObject().put("ms",(System.nanoTime()-a)/1e6).put("occupancy",calls[0]).put("edges",calls[1]).put("x",s.targetX).put("y",s.targetY).put("steps",s.remainingWaypoints).put("status",s.status.name()));}
   WorldMoveTargetController.TileCenter next=null;for(WorldMoveTargetController.Direction d:WorldMoveTargetController.Direction.values()){float x=state.player().x+d.dx,y=state.player().y+d.dy;if(w.canPlayerTraverse(state.player().x,state.player().y,x,y))for(WorldMoveTargetController.TileCenter t:cells)if(t.x==x&&t.y==y){next=t;break;}if(next!=null)break;}assertNotNull(next);w.requestGroundWorld(next.x,next.y);float oldX=w.presentationPlayerX(),oldY=w.presentationPlayerY();for(int f=0;f<15;f++)update.invoke(v,.016f);assertTrue(Math.hypot(w.presentationPlayerX()-oldX,w.presentationPlayerY()-oldY)>1);
   scenes.put(new JSONObject().put("map",map).put("tiles",cells.size()).put("monsters",state.monsters().size()).put("taps",taps).put("routes",routes));
  }
  JSONObject out=new JSONObject().put("source",System.getenv("PROJECT_DARK_SOURCE_SHA")).put("environment","Robolectric CPU; physical phone pending").put("scenes",scenes);File file=new File("build/reports/forest-performance/TAPS.json");file.getParentFile().mkdirs();try(Writer wr=new FileWriter(file)){wr.write(out.toString(2));}System.out.println(out);
 }
}
