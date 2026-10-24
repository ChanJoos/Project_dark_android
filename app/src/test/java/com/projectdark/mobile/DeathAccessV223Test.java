package com.projectdark.mobile;
import android.graphics.RectF;
import com.projectdark.mobile.world.*;
import org.junit.*;import org.junit.runner.RunWith;import org.robolectric.*;import org.robolectric.annotation.*;
import static org.junit.Assert.*;
@RunWith(RobolectricTestRunner.class) @Config(sdk=34,manifest=Config.NONE) @GraphicsMode(GraphicsMode.Mode.NATIVE)
public class DeathAccessV223Test {
 @Test public void onlyMineManagerOffersDeathEntryAndOtherRoutesRemain(){
  for(TownTravel.Destination town:TownTravel.TOWNS){
   assertEquals(town.map,MineTownGeometry.ID.equals(town.map),TownTravelWindow.destinations(town.map).contains(DeathVillageGeometry.LOBBY));
   for(TownTravel.Destination to:TownTravel.TOWNS)if(!to.map.equals(town.map)&&!DeathVillageGeometry.LOBBY.equals(to.map))assertNotNull(TownTravelWindow.cardFor(town.map,to.map));
  }
  assertNull(TownTravelWindow.cardFor(CampaignWorld.PIET,DeathVillageGeometry.LOBBY));
  assertNotNull(TownTravelWindow.cardFor(WorldDef.ID,com.projectdark.mobile.world.HorrorCastleGeometry.LOBBY));
  assertNotNull(TownTravelWindow.cardFor(MineTownGeometry.ID,MineHuntGeometry.ID));
 }
 @Test public void actualMineNpcInputArrivesRestoresAndReturnsWithoutReward()throws Exception{
  DeathVillageV217Test flow=new DeathVillageV217Test();flow.setup();
  GameView v=flow.lobby();MineTownV203Test f=flow.f;RuntimeState s=f.field(v,"state");
  long gold=s.rpg().gold(),exp=CampaignProgress.earnedExp(s.rpg());
  f.shot(v,"v223-mine-npc-death-arrival");assertTrue(F5mSaveStore.checkpointActive());F5mSaveStore.install(flow.c);
  GameView restored=new GameView(flow.c);restored.layout(0,0,960,540);RuntimeState r=f.field(restored,"state");
  assertEquals(DeathVillageGeometry.LOBBY,r.currentMapId());assertEquals(s.player().x,r.player().x,0.01f);assertEquals(s.player().y,r.player().y,0.01f);
  f.approach(restored,"death_village_travel");RectF mine=TownTravelWindow.cardFor(r.currentMapId(),MineTownGeometry.ID);assertNotNull(mine);f.tap(restored,mine.centerX(),mine.centerY());
  assertEquals(MineTownGeometry.ID,r.currentMapId());assertEquals(gold,r.rpg().gold().longValue());assertEquals(exp,CampaignProgress.earnedExp(r.rpg()));
  f.approach(restored,"mine_town_travel");f.shot(restored,"v223-mine-death-travel-menu");
  RectF death=TownTravelWindow.cardFor(r.currentMapId(),DeathVillageGeometry.LOBBY);assertNotNull(death);f.tap(restored,death.centerX(),death.centerY());assertEquals(DeathVillageGeometry.LOBBY,r.currentMapId());
 }
}
