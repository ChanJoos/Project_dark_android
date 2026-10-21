package com.projectdark.mobile;

import android.content.Context;
import android.graphics.*;
import com.projectdark.mobile.world.*;
import java.util.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.*;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class) @Config(sdk=34,manifest=Config.NONE) @GraphicsMode(GraphicsMode.Mode.NATIVE)
public class DeathNavigationV221Test {
 MineTownV203Test f;Context c;
 @Before public void setup(){f=new MineTownV203Test();f.reset();c=RuntimeEnvironment.getApplication();}
 GameView scene(String id)throws Exception{GameView v=new GameView(c);v.layout(0,0,960,540);if(!WorldDef.ID.equals(id)){if(TownTravel.isTown(id))f.call(v,"travelTown",new Class<?>[]{String.class},id);else{f.call(v,"travelTown",new Class<?>[]{String.class},DeathVillageGeometry.LOBBY);f.call(v,"changeCampaignMap",new Class<?>[]{String.class,boolean.class},id,false);}}return v;}
 WorldRuntimeAdapter world(GameView v)throws Exception{RuntimeState s=f.field(v,"state");return f.field(v,WorldDef.ID.equals(s.currentMapId())?"worldAdapter":"poteFieldAdapter");}
 WorldMapWindow open(GameView v)throws Exception{f.tap(v,68,73);WorldMapWindow m=f.field(v,"worldMap");assertTrue(m.open);return m;}
 @Test public void everyTownPinTouchesUseLivePathThenExistingDoorOrPortalWithoutGrantingRewards()throws Exception{
  for(String id:new String[]{WorldDef.ID,AbelTownGeometry.ID,RucesionTownGeometry.ID,MineTownGeometry.ID,CampaignWorld.PIET,DeathVillageGeometry.LOBBY,DeathVillageGeometry.FIELD}){
   setup();GameView v=scene(id);v.layout(0,0,1280,540);RuntimeState s=f.field(v,"state");WorldRuntimeAdapter w=world(v);WorldCameraTransform camera=w.camera();float x=s.player().x,y=s.player().y;long exp=CampaignProgress.earnedExp(s.rpg()),gold=s.rpg().gold();WorldMapWindow map=open(v);WorldMapWindow.Pin pin=map.pins().get(0);float offset=f.field(v,"hudCenterOffset");
   f.tap(v,map.screenX(pin,offset),map.screenY(pin));assertFalse(id+" selection closes map",map.open);assertSame(camera,w.camera());assertEquals("touch never teleports",x,s.player().x,0);assertEquals(y,s.player().y,0);assertEquals(id,s.currentMapId());assertEquals(WorldMoveTargetController.RequestKind.GROUND,w.snapshot().movement.kind);
   assertTrue(id+" selected entrance path",CampaignNavigationTest.walk(w,w.snapshot().movement));w.tickNavigation(.7f);if(WorldDef.ID.equals(id))f.tick(v,1);else assertTrue(id+" actual gate",(Boolean)f.call(v,"checkCampaignGate",new Class<?>[]{}));assertNotEquals("ordinary door/portal enters destination",id,s.currentMapId());assertEquals(exp,CampaignProgress.earnedExp(s.rpg()));assertEquals(gold,s.rpg().gold().longValue());
  }
 }
 @Test public void actualUpdateWalksLobbyToOutdoorAndOutdoorToHouseWithLiveMonsters()throws Exception{
  GameView v=scene(DeathVillageGeometry.LOBBY);RuntimeState s=f.field(v,"state");WorldMapWindow map=open(v);WorldMapWindow.Pin pin=map.pins().get(0);f.tap(v,map.screenX(pin,0),map.screenY(pin));for(int i=0;i<500&&DeathVillageGeometry.LOBBY.equals(s.currentMapId());i++)f.tick(v,1);assertEquals(DeathVillageGeometry.FIELD,s.currentMapId());assertEquals(72,s.monsters().size());assertEquals(72,s.monsters().stream().filter(m->m.alive).count());
  map=open(v);pin=map.pins().stream().filter(p->p.label.equals("건물 1")).findFirst().get();f.tap(v,map.screenX(pin,0),map.screenY(pin));for(int i=0;i<240&&DeathVillageGeometry.FIELD.equals(s.currentMapId());i++)f.tick(v,1);assertEquals("MAP_DEATH_HOUSE_1",s.currentMapId());f.shot(v,"v221-map-selected-house1");assertFalse((Boolean)f.call(v,"checkCampaignGate",new Class<?>[]{}));
 }
 @Test public void groupedPinsSelectLegalGateAllDeathScenesAndBlankTouchCannotLeakOrRebake()throws Exception{
  DeathVillageData.Gate inbound=DeathVillageGeometry.gates(DeathVillageGeometry.LOBBY).get(0);
  DeathVillageData.Gate back=DeathVillageGeometry.gates(DeathVillageGeometry.FIELD).stream().filter(g->g.to.equals(DeathVillageGeometry.LOBBY)).findFirst().get();
  DeathVillageData.Gate house=DeathVillageGeometry.gates(DeathVillageGeometry.FIELD).stream().filter(g->g.to.equals("MAP_DEATH_HOUSE_1")).findFirst().get();
  DeathVillageTravel latch=new DeathVillageTravel();latch.arrived(inbound.to,inbound);float ax=DeathVillageGeometry.x(inbound.arrivalU,inbound.arrivalV),ay=DeathVillageGeometry.y(inbound.arrivalU,inbound.arrivalV);
  latch.select(inbound.to,DeathVillageGeometry.x(house.u,house.v),DeathVillageGeometry.y(house.u,house.v),ax,ay);assertNull("other destination preserves arrival bounce protection",latch.at(inbound.to,ax,ay));
  latch.select(inbound.to,DeathVillageGeometry.x(back.u,back.v),DeathVillageGeometry.y(back.u,back.v),ax,ay);assertNotNull("explicit return selection releases its own latch",latch.at(inbound.to,ax,ay));
  for(String id:DeathVillageData.ids()){
   for(WorldMapWindow.Pin pin:WorldMapWindow.entrances(id))assertTrue(id+" legal selected gate",DeathVillageGeometry.gates(id).stream().anyMatch(g->pin.targetX==DeathVillageGeometry.x(g.u,g.v)&&pin.targetY==DeathVillageGeometry.y(g.u,g.v)));
  }
  GameView v=scene(DeathVillageGeometry.LOBBY);RuntimeState s=f.field(v,"state");WorldMapWindow map=open(v);int builds=map.builds();float x=s.player().x,y=s.player().y;f.tap(v,200,480);assertTrue(map.open);assertEquals(x,s.player().x,0);assertEquals(y,s.player().y,0);for(int i=0;i<12;i++){f.tap(v,907,36);assertFalse(map.open);open(v);}assertEquals(builds,map.builds());assertTrue(map.bytes()<=2097152);f.shot(v,"v221-map-selectable-lobby");
 }
 @Test public void correctedPoolsSensedHpAndThreeSpecies750000RewardsPersistExactlyOnce()throws Exception{
  for(String id:DeathVillageData.ids()){
   Set<Integer> types=new HashSet<>();for(RuntimeState.Monster m:DeathVillageMonsters.spawn(id)){int t=DeathVillageMonsters.type(m.id);types.add(t);assertEquals(750000,DeathVillageMonsters.profile(m.id).exp);assertEquals(DeathVillageMonsters.profile(m.id).hp,m.maxHp);}
   assertEquals(id,DeathVillageGeometry.LOBBY.equals(id)?Set.of():DeathVillageGeometry.FIELD.equals(id)||DeathVillageGeometry.house(id)?Set.of(1,3):Set.of(1,2,3),types);
  }
  assertEquals(164926,DeathVillageMonsters.profile("DEATH_01").hp);assertEquals(192707,DeathVillageMonsters.profile("DEATH_03").hp);
  GameView v=scene("MAP_DEATH_UNDER_C");RuntimeState s=f.field(v,"state");long exp=CampaignProgress.earnedExp(s.rpg()),gold=s.rpg().gold(),rewardGold=0;
  for(int t=1;t<=3;t++){final int type=t;RuntimeState.Monster m=s.monsters().stream().filter(n->DeathVillageMonsters.type(n.id)==type).findFirst().get();rewardGold+=DeathVillageMonsters.profile(m.id).gold;s.damage(m,m.maxHp);s.rpg().consumeCombat(s.ledger().snapshot(),s);s.rpg().consumeCombat(s.ledger().snapshot(),s);assertEquals(exp+750000L*t,CampaignProgress.earnedExp(s.rpg()));assertEquals(gold+rewardGold,s.rpg().gold().longValue());}
  assertTrue(F5mSaveStore.checkpointActive());F5mSaveStore.install(c);GameView restored=new GameView(c);RuntimeState rs=f.field(restored,"state");assertEquals("MAP_DEATH_UNDER_C",rs.currentMapId());assertEquals(exp+2250000,CampaignProgress.earnedExp(rs.rpg()));assertEquals(gold+rewardGold,rs.rpg().gold().longValue());rs.rpg().consumeCombat(rs.ledger().snapshot(),rs);assertEquals(exp+2250000,CampaignProgress.earnedExp(rs.rpg()));
 }
}
