package com.projectdark.mobile;
import android.content.Context;
import com.projectdark.mobile.world.*;
import java.util.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.*;
import static org.junit.Assert.*;
@RunWith(RobolectricTestRunner.class) @Config(sdk=34,manifest=Config.NONE) @GraphicsMode(GraphicsMode.Mode.NATIVE)
public class MapLabelsV245Test {
 MineTownV203Test f;Context c;
 @Before public void setup(){f=new MineTownV203Test();f.reset();c=RuntimeEnvironment.getApplication();}
 @Test public void millesTwelveThresholdsShowSevenUniqueEntranceNamesWithRealTargets(){
  List<WorldDef.PortalSpawn> portals=new WorldDef().portalSpawns();List<WorldMapWindow.Pin> pins=WorldMapWindow.entrances(WorldDef.ID);
  assertEquals(12,portals.size());assertEquals(7,pins.size());Set<String> labels=new HashSet<>();Set<String> destinations=new HashSet<>();
  for(WorldMapWindow.Pin pin:pins){assertTrue("one label per entrance: "+pin.label,labels.add(pin.label));WorldDef.PortalSpawn target=portals.stream().filter(p->p.x==pin.targetX&&p.y==pin.targetY).findFirst().orElseThrow(AssertionError::new);assertTrue(destinations.add(target.targetMapId));}
  assertEquals(7,destinations.size());
 }
 WorldDef.PortalSpawn portal(String id,String target,float x,float y){return new WorldDef.PortalSpawn(id,target,x,y,18,"USER_TEST","TEST_ONLY");}
 @Test public void adjacentChainsGroupButDistantAndDifferentDestinationsStaySeparate(){
  String room=TownInteriorDef.ALL.get(0).mapId,other=TownInteriorDef.ALL.get(1).mapId;
  List<WorldMapWindow.Pin> pins=WorldMapWindow.millesEntrances(Arrays.asList(portal("a",room,0,0),portal("b",room,64,32),portal("c",room,128,64),portal("distant",room,500,500),portal("other",other,0,0)));
  assertEquals(3,pins.size());WorldMapWindow.Pin group=pins.get(0);assertEquals(64,group.x,0);assertEquals(32,group.y,0);assertEquals(64,group.targetX,0);assertEquals(32,group.targetY,0);assertEquals(500,pins.get(1).targetX,0);assertEquals(0,pins.get(2).targetX,0);
 }
 @Test public void everyGroupedNameUsesNormalHudPathThenTheExistingDoorAndMapCache()throws Exception{
  List<WorldMapWindow.Pin> expected=WorldMapWindow.entrances(WorldDef.ID);
  for(int i=0;i<expected.size();i++){
   setup();GameView v=new GameView(c);v.layout(0,0,960,540);RuntimeState s=f.field(v,"state");WorldRuntimeAdapter w=f.field(v,"worldAdapter");float x=s.player().x,y=s.player().y;
   f.tap(v,68,73);WorldMapWindow map=f.field(v,"worldMap");assertTrue(map.open);assertEquals(7,map.pins().size());WorldMapWindow.Pin pin=map.pins().get(i);if(i==0)f.shot(v,"v245-milles-grouped-map");
   int builds=map.builds();f.tap(v,907,36);f.tap(v,68,73);assertEquals(builds,map.builds());
   f.tap(v,map.screenX(pin,0),map.screenY(pin));assertFalse(pin.label,map.open);assertEquals(x,s.player().x,0);assertEquals(y,s.player().y,0);assertTrue(pin.label,CampaignNavigationTest.walk(w,w.snapshot().movement));w.tickNavigation(.7f);f.tick(v,2);
   WorldDef.PortalSpawn target=s.world().portalSpawns().stream().filter(p->p.x==pin.targetX&&p.y==pin.targetY).findFirst().orElseThrow(AssertionError::new);assertEquals(pin.label,target.targetMapId,s.currentMapId());
  }
 }
}
