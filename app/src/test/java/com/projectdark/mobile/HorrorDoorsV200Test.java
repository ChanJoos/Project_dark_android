package com.projectdark.mobile;
import static org.junit.Assert.*;
import android.content.Context;
import android.graphics.*;
import com.projectdark.mobile.world.*;
import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.*;
@RunWith(RobolectricTestRunner.class) @Config(sdk=34,manifest=Config.NONE) @GraphicsMode(GraphicsMode.Mode.NATIVE)
public class HorrorDoorsV200Test {
 Context c;
 @Before public void setup(){c=RuntimeEnvironment.getApplication();for(String n:new String[]{"project_dark_f5m_v1","project_dark_skill_book_v1","project_dark_skill_test_v1"})c.getSharedPreferences(n,0).edit().clear().commit();F5mSaveStore.install(c);UiTheme.install(c);}
 GameView view(int width)throws Exception{GameView v=new GameView(c);v.layout(0,0,width,540);Field f=GameView.class.getDeclaredField("inPoteField");f.setAccessible(true);f.setBoolean(v,true);return v;}
 void enter(GameView v,String id)throws Exception{Method m=GameView.class.getDeclaredMethod("changeCampaignMap",String.class,boolean.class);m.setAccessible(true);m.invoke(v,id,false);}
 boolean gate(GameView v)throws Exception{Method m=GameView.class.getDeclaredMethod("checkCampaignGate");m.setAccessible(true);return (Boolean)m.invoke(v);}
 void save(GameView v,String name,int width)throws Exception{WorldRuntimeAdapter w=ItemWindowReferenceTest.field(v,"poteFieldAdapter");if(w!=null)w.snapCameraToPlayer();Bitmap b=Bitmap.createBitmap(width,540,Bitmap.Config.ARGB_8888);v.draw(new Canvas(b));File f=new File("build/reports/horror-v179/v200/"+name+".png");f.getParentFile().mkdirs();try(FileOutputStream o=new FileOutputStream(f)){assertTrue(b.compress(Bitmap.CompressFormat.PNG,100,o));}}
 @Test public void lobbyCountsAndCorridorReturnsMatchReferenceAndEachGateTransitions()throws Exception{
  GameView v=view(960);RuntimeState s=ItemWindowReferenceTest.field(v,"state");Map<String,Integer> expected=new LinkedHashMap<>();expected.put("밀레스",5);expected.put("1층 서쪽 복도",5);expected.put("1층 동쪽 복도",1);expected.put("2층 중앙 복도",1);
  for(Map.Entry<String,Integer> e:expected.entrySet())assertEquals(e.getKey(),e.getValue().longValue(),HorrorCastleGeometry.doors(HorrorCastleGeometry.LOBBY).stream().filter(d->e.getKey().equals(d.label)).count());
  int i=0;for(HorrorCastleGeometry.Door d:HorrorCastleGeometry.doors(HorrorCastleGeometry.LOBBY)){enter(v,HorrorCastleGeometry.LOBBY);s.player().x=d.x;s.player().y=d.y;assertTrue(d.visiblePortal);save(v,"lobby-"+(i++),960);assertTrue(gate(v));assertEquals("MILLES".equals(d.destination)?WorldDef.ID:d.destination,s.currentMapId());}
  for(String id:new String[]{HorrorCastleGeometry.FIRST,HorrorCastleGeometry.SECOND}){assertEquals(4,HorrorCastleGeometry.doors(id).stream().filter(d->d.visiblePortal).count());for(HorrorCastleGeometry.Door d:HorrorCastleGeometry.doors(id))if(d.visiblePortal){enter(v,id);s.player().x=d.x;s.player().y=d.y;assertTrue(gate(v));assertEquals(HorrorCastleGeometry.LOBBY,s.currentMapId());}}
 }
 @Test public void allFirstFloorDoorsUseNativePanelCellsInsteadOfAdjacentFloor()throws Exception{
  GameView v=view(960);RuntimeState s=ItemWindowReferenceTest.field(v,"state");int count=0;
  for(HorrorCastleGeometry.Door d:HorrorCastleGeometry.doors(HorrorCastleGeometry.FIRST))if(!d.visiblePortal){int u=Math.round((d.y-160)/32+(d.x-800)/64),w=Math.round((d.y-160)/32-(d.x-800)/64);assertTrue(u==0||u==9);assertTrue(w==6||w==12||w==18||w==24||w==30);enter(v,HorrorCastleGeometry.FIRST);s.player().x=d.x;s.player().y=d.y;save(v,"corridor-door-"+(count++),960);assertTrue(gate(v));assertEquals(HorrorCastleGeometry.roomId(1),s.currentMapId());}assertEquals(10,count);
 }
 @Test public void roomExitHasNoRingAndAdjacentFormerOffsetDoesNotActivate()throws Exception{
  GameView v=view(960);RuntimeState s=ItemWindowReferenceTest.field(v,"state");
  for(int n=1;n<=13;n++){String id=HorrorCastleGeometry.roomId(n);enter(v,id);for(HorrorCastleGeometry.Door d:HorrorCastleGeometry.doors(id)){assertFalse(d.visiblePortal);assertEquals("MILLES",d.destination);}HorrorCastleGeometry.Door d=HorrorCastleGeometry.doors(id).get(0);s.player().x=d.x+32;s.player().y=d.y+16;assertFalse("adjacent floor must not exit "+id,gate(v));s.player().x=d.x;s.player().y=d.y;save(v,"room-door-"+n,960);assertTrue(gate(v));assertEquals(WorldDef.ID,s.currentMapId());}
 }
 @Test public void realNavigationReachesDoorThenGameViewExitsWithoutRing()throws Exception{
  GameView v=view(960);enter(v,HorrorCastleGeometry.roomId(1));RuntimeState s=ItemWindowReferenceTest.field(v,"state");for(RuntimeState.Monster m:s.monsters())m.alive=false;WorldRuntimeAdapter w=ItemWindowReferenceTest.field(v,"poteFieldAdapter");HorrorCastleGeometry.Door d=HorrorCastleGeometry.doors(s.currentMapId()).get(0);assertTrue(CampaignNavigationTest.walk(w,w.requestGroundWorld(d.x,d.y)));assertTrue(gate(v));assertEquals(WorldDef.ID,s.currentMapId());
 }
 @Test public void monsterCounterStaysAtHorizontalViewportCenterOnWidePhones()throws Exception{
  for(int width:new int[]{960,1280}){RuntimeState s=new RuntimeState();s.enterCampaignMap(HorrorCastleGeometry.roomId(1),false);WorldRuntimeAdapter w=HorrorCastleV179Test.adapter(s);HorrorCastleRenderer r=new HorrorCastleRenderer(c);Method m=HorrorCastleRenderer.class.getDeclaredMethod("drawCounter",Canvas.class,WorldRuntimeAdapter.class);m.setAccessible(true);Bitmap b=Bitmap.createBitmap(width,540,Bitmap.Config.ARGB_8888);m.invoke(r,new Canvas(b),w);int left=width,right=-1;for(int y=0;y<110;y++)for(int x=0;x<width;x++)if(Color.alpha(b.getPixel(x,y))>0){left=Math.min(left,x);right=Math.max(right,x);}assertTrue(right>left);assertEquals(width/2f,(left+right)/2f,4f);GameView v=view(width);enter(v,HorrorCastleGeometry.roomId(1));save(v,"counter-"+width,width);}
 }
}
