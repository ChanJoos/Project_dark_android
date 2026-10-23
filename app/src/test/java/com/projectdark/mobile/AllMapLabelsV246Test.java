package com.projectdark.mobile;
import android.content.Context;
import android.graphics.*;
import com.projectdark.mobile.world.*;
import java.util.*;
import java.io.*;
import org.json.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.*;
import static org.junit.Assert.*;
@RunWith(RobolectricTestRunner.class) @Config(sdk=34,manifest=Config.NONE) @GraphicsMode(GraphicsMode.Mode.NATIVE)
public class AllMapLabelsV246Test {
 MineTownV203Test f;Context c;
 @Before public void setup(){f=new MineTownV203Test();f.reset();c=RuntimeEnvironment.getApplication();}
 static List<String> maps(){
  Set<String> ids=new LinkedHashSet<>();ids.add(WorldDef.ID);for(TownTravel.Destination d:TownTravel.TOWNS)ids.add(d.map);ids.addAll(Arrays.asList(CampaignWorld.ROUTE));ids.add("MAP_POTE_04");ids.addAll(Arrays.asList(AbelCoastGeometry.ROUTE));ids.addAll(Arrays.asList(RucesionCoastGeometry.ROUTE));ids.add(MineHuntGeometry.ID);ids.addAll(DeathVillageData.ids());ids.add(HorrorCastleGeometry.LOBBY);ids.add(HorrorCastleGeometry.FIRST);ids.add(HorrorCastleGeometry.SECOND);for(int i=1;i<=HorrorCastleGeometry.ROOM_COUNT;i++)ids.add(HorrorCastleGeometry.roomId(i));for(List<TownInteriorDef> rooms:Arrays.asList(TownInteriorDef.ALL,TownInteriorDef.TEMPLE,TownInteriorDef.ABEL,TownInteriorDef.RUCESION,TownInteriorDef.MINE))for(TownInteriorDef d:rooms)ids.add(d.mapId);return new ArrayList<>(ids);
 }
 static List<String> signature(String id){List<String> out=new ArrayList<>();for(WorldMapWindow.Pin pin:WorldMapWindow.entrances(id))out.add(pin.destination+":"+pin.x+":"+pin.y+":"+pin.targetX+":"+pin.targetY);return out;}
 @Test public void everyMapCoversEveryRawGateExactlyOnceAndOnlyAdjacentSameDestinationMembersMerge()throws Exception{
  List<String> ids=maps();assertTrue("complete live/restorable map inventory",ids.size()>=60);JSONArray audit=new JSONArray();int totalRaw=0,totalShown=0;
  for(String id:ids){
   List<WorldMapWindow.Pin> raw=WorldMapWindow.rawEntrances(id),shown=WorldMapWindow.entrances(id);assertFalse(id,raw.isEmpty());assertFalse(id,shown.isEmpty());int covered=0;
   Map<String,Integer> counts=new HashMap<>();for(WorldMapWindow.Pin p:raw)counts.merge(p.destination+":"+p.targetX+":"+p.targetY,1,Integer::sum);
   Map<String,Integer> grouped=new HashMap<>();JSONArray pins=new JSONArray();
   for(WorldMapWindow.Pin p:shown){
    assertFalse(id,p.members.isEmpty());assertTrue(id,p.members.stream().anyMatch(m->m.targetX==p.targetX&&m.targetY==p.targetY));
    for(WorldMapWindow.Pin member:p.members){assertEquals(id,p.destination,member.destination);covered++;grouped.merge(member.destination+":"+member.targetX+":"+member.targetY,1,Integer::sum);}
    for(WorldMapWindow.Pin other:shown)if(p!=other&&p.destination.equals(other.destination))for(WorldMapWindow.Pin a:p.members)for(WorldMapWindow.Pin b:other.members)assertTrue(id+" adjacent entrance split",Math.hypot(a.targetX-b.targetX,a.targetY-b.targetY)>72);
    pins.put(new JSONObject().put("label",p.label).put("destination",p.destination).put("sourceThresholds",p.members.size()).put("targetX",p.targetX).put("targetY",p.targetY));
   }
   assertEquals(id,raw.size(),covered);assertEquals(id,counts,grouped);totalRaw+=raw.size();totalShown+=shown.size();audit.put(new JSONObject().put("map",id).put("raw",raw.size()).put("shown",shown.size()).put("pins",pins));
  }
  assertEquals(signature(AbelTownGeometry.ID),signature(AbelTownGeometry.EAST_ID));assertEquals(signature(RucesionTownGeometry.ID),signature(RucesionTownGeometry.EAST_ID));assertEquals(signature("MAP_ABEL_COAST_1A"),signature("MAP_ABEL_COAST_1B"));
  write("pin-audit.json",new JSONObject().put("maps",ids.size()).put("rawThresholds",totalRaw).put("displayedEntrances",totalShown).put("records",audit).toString(2));
 }
 @Test public void commonGroupingMergesAllSameDestinationNeighborsButKeepsDisconnectedAndDifferentDestinations(){
  List<WorldMapWindow.Pin> raw=Arrays.asList(new WorldMapWindow.Pin("같은 이름","A",0,0,0,0),new WorldMapWindow.Pin("같은 이름","A",64,32,64,32),new WorldMapWindow.Pin("같은 이름","A",128,64,128,64),new WorldMapWindow.Pin("같은 이름","A",500,500,500,500),new WorldMapWindow.Pin("같은 이름","B",0,0,0,0));
  List<WorldMapWindow.Pin> grouped=WorldMapWindow.groupEntrances(raw);assertEquals(3,grouped.size());assertEquals(3,grouped.get(0).members.size());assertEquals(64,grouped.get(0).targetX,0);assertEquals(500,grouped.get(1).targetX,0);assertEquals("B",grouped.get(2).destination);
  assertTrue(WorldMapWindow.entrances(HorrorCastleGeometry.LOBBY).size()<WorldMapWindow.rawEntrances(HorrorCastleGeometry.LOBBY).size());
 }
 void enter(GameView v,String id)throws Exception{
  while((Boolean)f.field(v,"inReagentShop"))f.call(v,"leaveReagentShop",new Class<?>[]{});
  TownInteriorDef room=TownInteriorDef.forMap(id);
  if(room!=null){String parent=room.mapId.startsWith("abel_")?AbelTownGeometry.ID:room.mapId.startsWith("rucesion_")?RucesionTownGeometry.ID:room.mapId.startsWith("mine_")?MineTownGeometry.ID:WorldDef.ID;f.call(v,"travelTown",new Class<?>[]{String.class},parent);f.call(v,"enterTownInterior",new Class<?>[]{TownInteriorDef.class},room);}
  else if(WorldDef.ID.equals(id))f.call(v,"travelTown",new Class<?>[]{String.class},id);
  else{if(!(Boolean)f.field(v,"inPoteField"))f.call(v,"enterPoteField",new Class<?>[]{});f.call(v,"changeCampaignMap",new Class<?>[]{String.class,boolean.class},id,false);}
 }
 @Test public void actualHudOverviewAndEveryGroupedPinPathAreInspectedOnEveryMap()throws Exception{
  GameView v=new GameView(c);v.layout(0,0,960,540);JSONArray checked=new JSONArray();int targets=0;
  for(String id:maps()){
   enter(v,id);RuntimeState state=f.field(v,"state");assertEquals(id,state.currentMapId());WorldRuntimeAdapter world=(WorldRuntimeAdapter)f.call(v,"activeWorld",new Class<?>[]{});WorldCameraTransform camera=world.camera();
   float x=state.player().x,y=state.player().y;f.tap(v,68,73);WorldMapWindow map=f.field(v,"worldMap");assertTrue(id+" HUD opens map",map.open);assertSame(camera,world.camera());assertEquals(x,state.player().x,0);assertEquals(y,state.player().y,0);assertEquals(WorldMapWindow.entrances(id).size(),map.pins().size());assertTrue(map.bytes()<=2097152);f.shot(v,"v246/"+id);
   int builds=map.builds();List<WorldMapWindow.Pin> pins=new ArrayList<>(map.pins());int routes=0;
   for(WorldMapWindow.Pin pin:pins){
    if(!map.open){f.tap(v,68,73);assertTrue(id,map.open);}assertEquals(id,builds,map.builds());float beforeX=state.player().x,beforeY=state.player().y;
    f.tap(v,map.screenX(pin,0),map.screenY(pin));assertFalse(id+" "+pin.label,map.open);assertEquals(id,beforeX,state.player().x,0);assertEquals(id,beforeY,state.player().y,0);
    assertEquals(id+" "+pin.label,WorldMoveTargetController.RequestKind.GROUND,world.snapshot().movement.kind);assertTrue(id+" "+pin.label+" path",CampaignNavigationTest.walk(world,world.snapshot().movement));world.tickNavigation(.7f);assertTrue(id+" target proximity",Math.hypot(state.player().x-pin.targetX,state.player().y-pin.targetY)<40);routes++;targets++;
   }
   checked.put(new JSONObject().put("map",id).put("pins",pins.size()).put("actualHudPaths",routes).put("overviewBytes",map.bytes()));
  }
  write("native-audit.json",new JSONObject().put("maps",checked.length()).put("actualHudPaths",targets).put("records",checked).toString(2));
 }
 static void write(String name,String data)throws Exception{File file=new File("build/reports/horror-v179/v246/"+name);file.getParentFile().mkdirs();try(FileOutputStream out=new FileOutputStream(file)){out.write(data.getBytes(java.nio.charset.StandardCharsets.UTF_8));}}
}
