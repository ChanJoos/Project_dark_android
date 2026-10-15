package com.projectdark.mobile;
import static org.junit.Assert.*;
import android.content.Context;
import android.graphics.*;
import java.io.*;
import java.util.*;
import org.json.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.*;
import com.projectdark.mobile.world.*;
@RunWith(RobolectricTestRunner.class) @Config(sdk=34,manifest=Config.NONE) @GraphicsMode(GraphicsMode.Mode.NATIVE)
public class CombatTownV167Test {
 Context c;
 @Before public void setup(){c=RuntimeEnvironment.getApplication();for(String p:new String[]{"project_dark_f5m_v1","project_dark_skill_test_v1","project_dark_skill_book_v1"})c.getSharedPreferences(p,0).edit().clear().commit();F5mSaveStore.install(c);UiTheme.install(c);}
 RuntimeState state(){RuntimeState s=new RuntimeState();s.rpg().enableEquipmentSandbox(true);return s;}
 int hit(RuntimeState s){RuntimeState.Monster m=s.monsters().get(0);m.hp=100000;m.alive=true;new RuntimeCombatPortAdapter(s,(a,b)->true,(a,b)->true).applyDamage("player",m.id,"attack_proto_swing",8);return 100000-m.hp;}
 @Test public void sourceEquipmentAndStrengthReachActualHpAndRestore()throws Exception{
  RuntimeState s=state();RpgProgressionState r=s.rpg();r.unequip("무기");r.equip("IT_TEST_WEAPON_MW002");int weak=hit(s);r.unequip("무기");r.equip("IT_WARDROBE_MW023");int strong=hit(s);assertTrue(strong>weak+100);
  r.restoreStats(50,3,3,3,3,0);assertTrue(hit(s)>strong);int before=hit(s);F5mSaveStore.saveRewardsActive(r);F5mSaveStore.install(c);RuntimeState restored=state();F5mSaveStore.restoreRewardsActive(restored.rpg());assertEquals(before,hit(restored));
 }
 @Test public void conWisAllocationChangesOnlyFutureLevelGainsAndSurvivesSave(){
  RuntimeState low=state(),high=state();for(RuntimeState s:Arrays.asList(low,high)){s.rpg().restoreProgression(30,0);s.rpg().restoreBaseResources(1000,1000);s.applyDerivedGrowth();}
  high.rpg().restoreStats(3,3,30,30,3,0);assertEquals(1000,high.rpg().baseMaxHp());assertEquals(1000,high.rpg().baseMaxMp());
  int hp=high.rpg().nextHpGain(),mp=high.rpg().nextMpGain();long exp=LevelExpCurve.requiredForNext(30);
  for(RuntimeState s:Arrays.asList(low,high)){assertEquals(1,s.rpg().grantAdaptedReward(exp,0));s.applyDerivedGrowth();}
  assertEquals(1000+hp,high.rpg().baseMaxHp());assertEquals(1000+mp,high.rpg().baseMaxMp());assertTrue(high.player().maxHp>low.player().maxHp);assertTrue(high.player().maxMp>low.player().maxMp);
  F5mSaveStore.saveRewardsActive(high.rpg());F5mSaveStore.install(c);RpgProgressionState restored=new RpgProgressionState();F5mSaveStore.restoreRewardsActive(restored);assertEquals(high.rpg().baseMaxHp(),restored.baseMaxHp());assertEquals(high.rpg().baseMaxMp(),restored.baseMaxMp());
 }
 @Test public void intAndDexScaleTheirActualSkillFamilies(){
  RuntimeState s=state();FinalStats low=s.rpg().finalStats();s.rpg().restoreStats(3,80,3,3,80,0);FinalStats high=s.rpg().finalStats();
  for(String id:new String[]{"SK_마법사_001","SK_도적_001"}){SkillAbilityCatalog.Ability a=SkillAbilityCatalog.get(id);assertTrue(id,SkillAbilityCatalog.amount(a,new SkillAbilityCatalog.Snapshot(high,100,100,30,100,false))>SkillAbilityCatalog.amount(a,new SkillAbilityCatalog.Snapshot(low,100,100,30,100,false)));}
 }
 @Test public void twoHandedOccupancyAndAllFourSourceFacingsAreExplicit()throws Exception{
  RuntimeState s=state();RpgProgressionState r=s.rpg();r.equip("IT_ADAPTED_STARTER_SHIELD");r.equip("IT_WARDROBE_MW016");assertNull(r.equippedDefinition("방패"));assertEquals(WeaponCombatProfile.Grip.TWO_HAND,WeaponCombatProfile.grip(r.equippedDefinition("무기")));assertEquals("TWO_HAND",WeaponCombatProfile.motion(r,"SWING"));
  r.equip("IT_ADAPTED_STARTER_SHIELD");assertNull(r.equippedDefinition("무기"));
  SkillPresentationCatalog catalog=new SkillPresentationCatalog(c);for(CharacterRenderer.Direction d:CharacterRenderer.Direction.values())for(String body:new String[]{"mm001","wm001"})assertEquals(body+"/02/"+CharacterRenderer.actionSourceIndex(d),catalog.frameKey(body,"SWING",d,.5f));
 }
 @Test public void daraCannotSilentlyPreviewZeroAndRealResourcesDamageExactlyOnce()throws Exception{
  SkillAbilityRuntimeTest h=new SkillAbilityRuntimeTest();h.context=c;RuntimeState s=h.state();RuntimeCombatSession session=h.session(s);session.setSkillTestMode(true);s.player().mp=0;
  CombatActionOrchestrator.Submission rejected=session.submitPlayer("m0","SK_무도가_020");assertFalse(rejected.accepted());assertEquals(CombatResolver.RejectReason.RESOURCE,rejected.rejectReason);assertEquals(1000000,s.monsters().get(0).hp);
  s.player().mp=7000;assertTrue(session.submitPlayer("m0","SK_무도가_020").accepted());List<CombatResolver.Event> e=session.tick(session.effectiveCastSeconds("SK_무도가_020")+.01f).events;assertTrue(e.stream().anyMatch(x->x.type==CombatResolver.EventType.HIT_FEEDBACK&&x.amount>0));int hp=s.monsters().get(0).hp;session.tick(3);assertEquals(hp,s.monsters().get(0).hp);
 }
 @Test public void eachTownShowsOnlyItsLocalHuntAndArrivalIsNearManager()throws Exception{
  for(TownTravel.Destination town:TownTravel.TOWNS){String hunt=TownTravelWindow.hunting(town.map);assertTrue(TownTravelWindow.destinations(town.map).contains(hunt));if(!WorldDef.ID.equals(town.map))assertFalse(TownTravelWindow.destinations(town.map).contains(TownTravelWindow.FOREST));if(WorldDef.ID.equals(town.map))continue;
   WorldMoveTargetController.TileCenter a=CampaignWorld.arrival(town.map,false),b=TownTravel.center(town.map);assertTrue(town.map,Math.hypot(a.x-b.x,a.y-b.y)<120);
   GameView v=new GameView(c);v.layout(0,0,960,540);v.setSkillTestMode(true);TownQualityV164Test h=new TownQualityV164Test();h.c=c;h.travel(v,town.map);h.capture(v,"v167-arrival-"+town.map);
  }
 }
 @Test public void inventoryAuditRecordsPendingPerformanceRatherThanPassingEmptyOptions()throws Exception{
  RpgProgressionState r=new RpgProgressionState();JSONArray rows=new JSONArray();for(RpgProgressionState.ItemDefinition d:r.itemDefinitions().values())if(d.equippable())rows.put(new JSONObject().put("id",d.itemId).put("name",d.name).put("stats",new JSONObject(d.statModifiers)).put("jobRestrictionResolved",d.jobRestrictionResolved).put("grip",WeaponCombatProfile.grip(d).name()).put("evidence",d.evidence.name()).put("performancePending",d.statModifiers.isEmpty()));
  File dir=new File("build/reports/combat-town-v167");dir.mkdirs();java.nio.file.Files.write(new File(dir,"all-equipment-audit.json").toPath(),rows.toString(2).getBytes("UTF-8"));assertTrue(rows.length()>100);
 }
}
