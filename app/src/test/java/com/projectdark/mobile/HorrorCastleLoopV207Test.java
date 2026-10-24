package com.projectdark.mobile;
import static org.junit.Assert.*;
import android.graphics.*;
import com.projectdark.mobile.world.*;
import java.util.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.*;
@RunWith(RobolectricTestRunner.class) @Config(sdk=34,manifest=Config.NONE) @GraphicsMode(GraphicsMode.Mode.NATIVE)
public class HorrorCastleLoopV207Test {
 HorrorDoorsV200Test h=new HorrorDoorsV200Test();
 @Before public void setup(){h.setup();}
 @Test public void exactHudCoordinatesMarkersAndRealGateDestinationsMatchAllTwelveRequestedCells()throws Exception{
  GameView v=h.view(960);RuntimeState s=ItemWindowReferenceTest.field(v,"state");h.enter(v,HorrorCastleGeometry.LOBBY);WorldRuntimeAdapter w=ItemWindowReferenceTest.field(v,"poteFieldAdapter");
  Set<String> expected=new HashSet<>();for(int y=21;y<=25;y++){expected.add("0,"+y+",MILLES");expected.add("9,"+y+","+HorrorCastleGeometry.FIRST);}expected.add("16,45,"+HorrorCastleGeometry.FIRST);expected.add("18,17,"+HorrorCastleGeometry.SECOND);
  Set<String> actual=new HashSet<>();for(HorrorCastleGeometry.Door d:HorrorCastleGeometry.doors(HorrorCastleGeometry.LOBBY)){
   actual.add(w.tileCoordinateX(d.x,d.y)+","+w.tileCoordinateY(d.x,d.y)+","+d.destination);assertEquals(d.x,d.markerX,0);assertEquals(d.y,d.markerY,0);
   h.enter(v,HorrorCastleGeometry.LOBBY);w=ItemWindowReferenceTest.field(v,"poteFieldAdapter");assertTrue(CampaignNavigationTest.walk(w,w.requestGroundWorld(d.x,d.y)));s.player().x=d.x;s.player().y=d.y;assertTrue(h.gate(v));assertEquals("MILLES".equals(d.destination)?WorldDef.ID:d.destination,s.currentMapId());
  }assertEquals(expected,actual);
  for(int[] old:new int[][]{{1,21},{6,21},{15,45},{17,18}}){h.enter(v,HorrorCastleGeometry.LOBBY);s.player().x=HorrorCastleGeometry.x(old[1]+1,19-old[0]);s.player().y=HorrorCastleGeometry.y(old[1]+1,19-old[0]);assertFalse("former portal cell "+Arrays.toString(old),h.gate(v));}
 }
 @Test public void twoCompleteLapsHaveFreshLiveMonstersAndRestartRestoresTheSecondLap()throws Exception{
  HorrorCastleMobileV183Test fixture=new HorrorCastleMobileV183Test();fixture.c=h.c;GameView v=fixture.view();RuntimeState s=ItemWindowReferenceTest.field(v,"state");fixture.enter(v,1);
  for(int lap=0;lap<2;lap++){long serial=s.horror().snapshot().getLong("serial");for(int n=1;n<=15;n++){
   assertEquals(HorrorCastleGeometry.roomId(n),s.currentMapId());assertEquals(15,s.monsters().stream().filter(m->m.alive).count());
   for(int i=0;i<14;i++)s.damage(s.monsters().get(i),Integer.MAX_VALUE);fixture.call(v,"updatePoteField",new Class<?>[]{float.class},0f);assertEquals(HorrorCastleGeometry.roomId(n),s.currentMapId());
   s.damage(s.monsters().get(14),Integer.MAX_VALUE);fixture.call(v,"updatePoteField",new Class<?>[]{float.class},0f);assertEquals(HorrorCastleGeometry.roomId(n==15?1:n+1),s.currentMapId());
  }assertEquals(serial+1,s.horror().snapshot().getLong("serial"));assertEquals(0,s.horror().clearedMask());assertEquals(15,s.monsters().stream().filter(m->m.alive).count());}
  s.damage(s.monsters().get(0),111);String id=s.monsters().get(0).id;int hp=s.monsters().get(0).hp;assertTrue(F5mSaveStore.checkpointActive());F5mSaveStore.install(h.c);GameView restarted=new GameView(h.c);RuntimeState restored=ItemWindowReferenceTest.field(restarted,"state");assertEquals(HorrorCastleGeometry.roomId(1),restored.currentMapId());assertEquals(s.horror().seed(),restored.horror().seed());assertEquals(hp,restored.monsters().stream().filter(m->m.id.equals(id)).findFirst().get().hp);
 }
 @Test public void referenceRoomsRenderWithoutLegacyAndRemainBoundedAcrossTwelveTrips()throws Exception{
  HorrorCastleRenderer renderer=new HorrorCastleRenderer(h.c);GameView v=h.view(960);RuntimeState s=ItemWindowReferenceTest.field(v,"state");
  for(int trip=0;trip<12;trip++){int n=12+trip%4;String id=HorrorCastleGeometry.roomId(n);h.enter(v,id);assertEquals(0,HorrorCastleGeometry.original(id).sourceId);assertEquals(15,s.monsters().size());assertTrue(s.npcs().isEmpty());WorldRuntimeAdapter w=ItemWindowReferenceTest.field(v,"poteFieldAdapter");
   renderer.prepare(id);int builds=renderer.backgroundBuilds();Bitmap b=Bitmap.createBitmap(960,540,Bitmap.Config.ARGB_8888);renderer.draw(new Canvas(b),w,Collections.emptyList());assertTrue(renderer.backgroundBytes()<=8652800);renderer.draw(new Canvas(b),w,Collections.emptyList());renderer.prepare(id);assertEquals(builds,renderer.backgroundBuilds());
   if(trip<4)h.save(v,"v207-room-"+n,960);renderer.releaseFloor();assertEquals(0,renderer.backgroundBytes());
  }
  h.enter(v,HorrorCastleGeometry.LOBBY);h.save(v,"v207-lobby",960);
 }
}
