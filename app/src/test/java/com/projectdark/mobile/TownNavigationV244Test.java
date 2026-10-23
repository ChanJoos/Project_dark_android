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
public class TownNavigationV244Test {
 MineTownV203Test f;Context c;
 @Before public void setup(){f=new MineTownV203Test();f.reset();c=RuntimeEnvironment.getApplication();}
 GameView view(){GameView v=new GameView(c);v.layout(0,0,960,540);return v;}
 RuntimeState state(GameView v)throws Exception{return f.field(v,"state");}
 WorldRuntimeAdapter world(GameView v)throws Exception{return (WorldRuntimeAdapter)f.call(v,"activeWorld",new Class<?>[]{});}
 void travel(GameView v,String map)throws Exception{f.call(v,"travelTown",new Class<?>[]{String.class},map);}
 void field(GameView v,String id)throws Exception{f.call(v,"enterPoteField",new Class<?>[]{});f.call(v,"changeCampaignMap",new Class<?>[]{String.class,boolean.class},id,false);}
 WorldMapWindow open(GameView v)throws Exception{f.tap(v,68,73);WorldMapWindow map=f.field(v,"worldMap");assertTrue(state(v).currentMapId(),map.open);return map;}
 @Test public void freshStartAndEveryMillesTownReturnAreExactly46_39AndSaveRestoresPosition()throws Exception{
  GameView v=view();RuntimeState s=state(v);WorldRuntimeAdapter w=world(v);
  assertEquals(46,w.tileCoordinateX(s.player().x,s.player().y));assertEquals(39,w.tileCoordinateY(s.player().x,s.player().y));assertTrue(w.canPlayerOccupy(s.player().x,s.player().y));
  for(TownTravel.Destination d:TownTravel.TOWNS){travel(v,d.map);travel(v,WorldDef.ID);assertEquals(WorldDef.PLAYER_SPAWN_X,s.player().x,0);assertEquals(WorldDef.PLAYER_SPAWN_Y,s.player().y,0);assertEquals(46,world(v).tileCoordinateX(s.player().x,s.player().y));assertEquals(39,world(v).tileCoordinateY(s.player().x,s.player().y));}
  WorldMoveTargetController.Snapshot move=world(v).step(WorldMoveTargetController.Direction.SE);assertTrue(CampaignNavigationTest.walk(world(v),move));world(v).tickNavigation(.7f);float x=s.player().x,y=s.player().y;assertTrue(F5mSaveStore.checkpointActive());F5mSaveStore.install(c);GameView restored=view();assertEquals(x,state(restored).player().x,0);assertEquals(y,state(restored).player().y,0);
 }
 @Test public void allSixActualInventoryRecallsShareNpcArrivalConsumeOneAndPersist()throws Exception{
  GameView v=view();RuntimeState s=state(v);
  for(String id:new String[]{"IT_RECALL_MILLES","IT_RECALL_ABEL","IT_RECALL_RUCESION","IT_RECALL_MINE","IT_RECALL_PIET","IT_RECALL_DEATH"}){
   travel(v,AbelTownGeometry.ID);String map=s.rpg().recallDestination(id);assertNotNull(id,map);WorldMoveTargetController.TileCenter expected=TownTravel.arrival(map);
   assertEquals(RpgProgressionState.AutoLootResult.LOOTED,s.rpg().autoLootResolvedItem(id,2));int owned=s.rpg().inventory().get(id);RpgInteractionController ui=f.field(v,"rpgInteraction");ui.selectInventoryItem(s.rpg(),id);f.call(v,"activateInventoryItem",new Class<?>[]{});
   assertEquals(id,map,s.currentMapId());assertEquals(expected.x,s.player().x,0);assertEquals(expected.y,s.player().y,0);assertEquals(owned-1,s.rpg().inventory().get(id).intValue());assertFalse((Boolean)f.field(v,"inventoryOpen"));
  }
  assertTrue(F5mSaveStore.checkpointActive());F5mSaveStore.install(c);GameView restored=view();assertEquals(s.currentMapId(),state(restored).currentMapId());assertEquals(s.player().x,state(restored).player().x,0);assertEquals(s.rpg().inventory(),state(restored).rpg().inventory());
 }
 @Test public void recallFromDeepTempleLeavesAllInteriorLayersAndDeadOrUnknownCannotConsume()throws Exception{
  GameView v=view();RuntimeState s=state(v);f.call(v,"enterTownInterior",new Class<?>[]{TownInteriorDef.class},TownInteriorDef.forMap("milles_interior_temple_seo"));
  String id="IT_RECALL_MILLES";s.rpg().autoLootResolvedItem(id,2);int before=s.rpg().inventory().get(id);s.player().alive=false;assertEquals(RpgProgressionState.UseResult.NO_EFFECT,s.rpg().useRecall(id,s));assertEquals(before,s.rpg().inventory().get(id).intValue());assertFalse(s.rpg().isRecall("unknown"));s.player().alive=true;
  RpgInteractionController ui=f.field(v,"rpgInteraction");ui.selectInventoryItem(s.rpg(),id);f.call(v,"activateInventoryItem",new Class<?>[]{});assertEquals(WorldDef.ID,s.currentMapId());assertFalse((Boolean)f.field(v,"inReagentShop"));assertFalse((Boolean)f.field(v,"inPoteField"));assertEquals(46,world(v).tileCoordinateX(s.player().x,s.player().y));assertEquals(39,world(v).tileCoordinateY(s.player().x,s.player().y));
 }
 @Test public void everyAuthoredMapFamilyExposesItsRealDoorOrRoutePins(){
  List<String> ids=new ArrayList<>(Arrays.asList(CampaignWorld.ROUTE));ids.addAll(Arrays.asList(RucesionCoastGeometry.ROUTE));ids.addAll(DeathVillageData.ids());ids.add(WorldDef.ID);ids.add(MineTownGeometry.ID);ids.add(MineHuntGeometry.ID);ids.add(RucesionTownGeometry.ID);ids.add(HorrorCastleGeometry.LOBBY);ids.add(HorrorCastleGeometry.FIRST);ids.add(HorrorCastleGeometry.SECOND);for(int i=1;i<=HorrorCastleGeometry.ROOM_COUNT;i++)ids.add(HorrorCastleGeometry.roomId(i));
  for(List<TownInteriorDef> rooms:Arrays.asList(TownInteriorDef.ALL,TownInteriorDef.ABEL,TownInteriorDef.RUCESION,TownInteriorDef.MINE,TownInteriorDef.TEMPLE))for(TownInteriorDef d:rooms)ids.add(d.mapId);
  for(String id:ids){List<WorldMapWindow.Pin> pins=WorldMapWindow.entrances(id);assertFalse(id,pins.isEmpty());for(WorldMapWindow.Pin pin:pins){assertTrue(id,Float.isFinite(pin.targetX)&&Float.isFinite(pin.targetY));assertNotNull(pin.label);}}
 }
 @Test public void forestAndInteriorMinimapTouchesWalkRealExitWithoutTeleportAndCacheOverview()throws Exception{
  for(boolean interior:new boolean[]{false,true}){
   setup();GameView v=view();if(interior)f.call(v,"enterTownInterior",new Class<?>[]{TownInteriorDef.class},TownInteriorDef.ALL.get(0));else field(v,"MAP_POTE_01");RuntimeState s=state(v);String id=s.currentMapId();WorldRuntimeAdapter w=world(v);float x=s.player().x,y=s.player().y;WorldMapWindow map=open(v);int builds=map.builds();WorldMapWindow.Pin pin=map.pins().get(0);f.tap(v,map.screenX(pin,0),map.screenY(pin));assertFalse(map.open);assertEquals(x,s.player().x,0);assertEquals(y,s.player().y,0);assertTrue(id,CampaignNavigationTest.walk(w,w.snapshot().movement));w.tickNavigation(.7f);
   if(interior)f.tick(v,2);else assertTrue((Boolean)f.call(v,"checkCampaignGate",new Class<?>[]{}));assertNotEquals(id,s.currentMapId());assertTrue(map.bytes()<=2097152);assertEquals(builds,map.builds());
  }
 }
 @Test public void mineHorrorCoastsAndTempleOverviewOpenThroughActualHudAndRetainLiveCamera()throws Exception{
  GameView v=view();for(String id:new String[]{MineHuntGeometry.ID,HorrorCastleGeometry.LOBBY,AbelCoastGeometry.ENTRY,RucesionCoastGeometry.ROUTE[0]}){
   if(WorldDef.ID.equals(state(v).currentMapId()))f.call(v,"enterPoteField",new Class<?>[]{});f.call(v,"changeCampaignMap",new Class<?>[]{String.class,boolean.class},id,false);WorldRuntimeAdapter w=world(v);WorldCameraTransform camera=w.camera();float x=state(v).player().x,y=state(v).player().y;WorldMapWindow map=open(v);assertFalse(map.pins().isEmpty());assertSame(camera,w.camera());assertEquals(x,state(v).player().x,0);assertEquals(y,state(v).player().y,0);f.tap(v,907,36);int builds=map.builds();open(v);assertEquals(builds,map.builds());f.tap(v,907,36);
  }
  travel(v,WorldDef.ID);f.call(v,"enterTownInterior",new Class<?>[]{TownInteriorDef.class},TownInteriorDef.TEMPLE.get(0));WorldMapWindow map=open(v);assertFalse(map.pins().isEmpty());f.shot(v,"v244-temple-map");
 }
}
