package com.projectdark.mobile;
import android.content.Context;
import android.graphics.*;
import com.projectdark.mobile.world.*;
import org.junit.*;import org.junit.runner.RunWith;import org.robolectric.*;import org.robolectric.annotation.*;
import org.json.*;import java.nio.file.Path;import java.nio.file.Files;import java.util.*;import java.lang.reflect.*;import static org.junit.Assert.*;
@RunWith(RobolectricTestRunner.class) @Config(sdk=34,manifest=Config.NONE) @GraphicsMode(GraphicsMode.Mode.NATIVE)
public class TownCampaignV163Test {
 Context c;
 @Before public void reset(){c=RuntimeEnvironment.getApplication();for(String p:new String[]{"project_dark_f5m_v1","project_dark_skill_book_v1","project_dark_skill_test_v1"})c.getSharedPreferences(p,0).edit().clear().commit();F5mSaveStore.install(c);}
 void call(GameView v,String method,String arg)throws Exception{Method m=GameView.class.getDeclaredMethod(method,String.class);m.setAccessible(true);m.invoke(v,arg);}
 void screenshot(GameView v,String name)throws Exception{Bitmap b=Bitmap.createBitmap(960,540,Bitmap.Config.ARGB_8888);v.draw(new Canvas(b));Path p=Path.of("build/reports/town-v163/"+name+".png");Files.createDirectories(p.getParent());try(java.io.OutputStream out=Files.newOutputStream(p)){assertTrue(b.compress(Bitmap.CompressFormat.PNG,100,out));}b.recycle();}
 RuntimeState.Npc approach(GameView v,String id)throws Exception{RuntimeState s=TownInteriorTest.field(v,"state");WorldRuntimeAdapter w=TownInteriorTest.field(v,WorldDef.ID.equals(s.currentMapId())?"worldAdapter":"poteFieldAdapter");assertTrue(s.currentMapId()+" approach "+id,CampaignNavigationTest.walk(w,w.requestNpcApproach(id)));RuntimeState.Npc n=s.npcs().stream().filter(a->a.id.equals(id)).findFirst().get();InteractionController interaction=TownInteriorTest.field(v,"interaction");assertEquals(InteractionController.TickResult.DIALOG_OPENED,interaction.request(s,n));return n;}
 @Test public void allTwelveTownPairsUseRealManagerDialogAndPersistWithoutQuestOrLevelGrant()throws Exception{
  GameView v=new GameView(c);v.layout(0,0,960,540);v.setSkillTestMode(false);RuntimeState s=TownInteriorTest.field(v,"state");
  for(TownTravel.Destination from:TownTravel.TOWNS)for(int target=0;target<4;target++){TownTravel.Destination to=TownTravel.TOWNS.get(target);if(from==to)continue;call(v,"travelTown",from.map);String quests=s.rpg().campaign().snapshot().getJSONArray("complete").toString();long exp=CampaignProgress.earnedExp(s.rpg()),gold=s.rpg().gold();approach(v,from.npc);screenshot(v,"travel-"+from.map+"-to-"+target);RectF destination=TownTravelWindow.cardFor(from.map,to.map);assertNotNull(destination);assertNull(TownTravelWindow.cardFor(from.map,from.map));TownInteriorTest.tap(v,destination.centerX(),destination.centerY());assertEquals(to.map,s.currentMapId());assertEquals(exp,CampaignProgress.earnedExp(s.rpg()));assertEquals(gold,s.rpg().gold().longValue());assertEquals(quests,s.rpg().campaign().snapshot().getJSONArray("complete").toString());assertFalse(s.blocked(s.player().x,s.player().y));assertTrue(F5mSaveStore.checkpointActive());
   F5mSaveStore.install(c);GameView restored=new GameView(c);assertEquals(to.map,((RuntimeState)TownInteriorTest.field(restored,"state")).currentMapId());F5mSaveStore.bindRuntime(s,TownInteriorTest.field(v,"f5mQuest"),TownInteriorTest.field(v,"quest2"));
  }
 }
 @Test public void retainedMillesNpcHasQuestServiceOrTravelAndCiviliansHaveNoWeaponsOrArmor(){
  RuntimeState s=new RuntimeState();Set<String> ids=new HashSet<>();for(RuntimeState.Npc n:s.npcs())ids.add(n.id);assertFalse(ids.contains("milles_west_proto"));assertFalse(ids.contains("milles_gate_proto"));assertTrue(ids.contains("milles_market_proto"));
  for(String id:ids)assertTrue(id,TownTravel.isManager(id)||CampaignProgress.definitions().stream().anyMatch(d->d.npc.equals(id)));
  EquipmentVisualRegistry art=new EquipmentVisualRegistry(c);for(NpcIdentity.Profile p:NpcIdentity.ALL)if(!NpcIdentity.special(p.id)){assertNull(p.id,p.weapon);assertEquals("mu0000001",p.outfit.split(",")[0]);for(String a:p.outfit.split(","))assertNotNull(a,art.get(a));}
 }
 @Test public void objectiveJourneyFindsBothCoastsAndTownReturnsWithoutSequentialTownDetour(){
  assertEquals(RucesionTownGeometry.ID,CampaignJourney.next(WorldDef.ID,"MAP_RUCESION_COAST_3B"));assertEquals(AbelTownGeometry.ID,CampaignJourney.next(RucesionTownGeometry.ID,"MAP_ABEL_COAST_2A"));assertEquals(RucesionTownGeometry.ID,CampaignJourney.next(AbelTownGeometry.ID,RucesionTownGeometry.ID));assertEquals("MAP_RUCESION_COAST_2C",CampaignJourney.next("MAP_RUCESION_COAST_1C","MAP_RUCESION_COAST_3B"));assertNull(CampaignJourney.next(RucesionTownGeometry.ID,RucesionCoastGeometry.BOSS));
 }
 static long toLevel(int level){long total=0;for(int n=1;n<level;n++)total+=LevelExpCurve.requiredForNext(n);return total;}
 void afterE18(RuntimeState s,String job)throws Exception{
  AbelCoastV139Test.restoreD04(s.rpg(),job);CampaignProgress p=s.rpg().campaign();JSONArray done=p.snapshot().getJSONArray("complete");for(int i=1;i<=18;i++)done.put(String.format(Locale.ROOT,"E%02d",i));assertTrue(p.restore(p.snapshot().put("complete",done).put("active",JSONObject.NULL)));long needed=toLevel(70)-CampaignProgress.earnedExp(s.rpg());if(needed>0)s.rpg().grantAdaptedReward(needed,0);assertEquals("F01",p.next(s.rpg()).id);
 }
 @Test public void fiveJobsFinishAll18WithRealDefeatLedgerThenExactlyOnceFinal99AndRestart()throws Exception{
  JSONArray receipt=new JSONArray();
  for(String job:CampaignProgress.JOBS){reset();RuntimeState s=new RuntimeState();afterE18(s,job);RpgProgressionState r=s.rpg();SkillBook book=SkillBook.load(c);book.bindJob(r::currentJobCode);RuntimeCombatSession combat=new RuntimeCombatSession(s,(a,t)->true,(a,k)->book.usable(k),a->true);F5mSaveStore.bindRuntime(s,F5mAdaptedPrologueQuest.openingFixture(),new GrowthQuest2());int kills=0;
   while(r.campaign().next(r)!=null){CampaignProgress.Def d=r.campaign().next(r);assertTrue(d.id,d.id.startsWith("F"));assertTrue(d.id+" gate",r.normalLevel()>=d.level);s.enterCampaignMap(d.map,false);assertTrue(d.id+" has reachable giver",s.npcs().stream().anyMatch(n->n.id.equals(d.npc)));WorldRuntimeAdapter world=CampaignNavigationTest.adapter(s);assertTrue(d.id+" giver path",CampaignNavigationTest.walk(world,world.requestNpcApproach(d.npc)));assertTrue(r.campaign().accept(d.id,r,book));r.campaign().visit(d.map);
    if(d.kind.equals("KILL")||d.kind.equals("PAIR")){int guard=0;while(r.campaign().status(d,r)!=CampaignProgress.Status.REPORT&&guard++<40){RuntimeState.Monster m=s.monsters().stream().filter(a->a.alive&&r.campaign().wanted(d,a.campaignRewardProfileId)).findFirst().orElse(null);if(m==null){s.tick(50);combat.tick(50);continue;}m.hp=1; // Final-contact fixture: tests real Resolver/event/reward plumbing, not fullHP combat balance.
      CampaignResources.freeRest(s);CampaignProgressTest.position(s,m);for(int i=0;i<20&&m.alive;i++){combat.submitPlayerBasicAttack(m.id);CampaignProgressTest.tick(s,combat,book,.6f);}assertFalse(d.id+" actual resolver defeat",m.alive);for(CombatLedger.Event e:s.ledger().snapshot())r.campaign().consume(e,s);kills++;}
    }
    assertEquals(d.id,CampaignProgress.Status.REPORT,r.campaign().status(d,r));assertTrue(F5mSaveStore.transactActive(r,book,()->r.campaign().claim(d.id,r)));long exp=CampaignProgress.earnedExp(r),gold=r.gold();assertFalse(r.campaign().claim(d.id,r));assertEquals(exp,CampaignProgress.earnedExp(r));assertEquals(gold,r.gold().longValue());F5mSaveStore.install(c);RpgProgressionState restored=new RpgProgressionState();F5mSaveStore.restoreRewardsActive(restored);assertEquals(r.campaign().snapshot().getJSONArray("complete").toString(),restored.campaign().snapshot().getJSONArray("complete").toString());assertEquals(exp,CampaignProgress.earnedExp(restored));F5mSaveStore.bindRuntime(s,F5mAdaptedPrologueQuest.openingFixture(),new GrowthQuest2());
   }
   assertEquals(99,r.normalLevel().intValue());assertTrue(r.campaign().finished());assertTrue(kills>=70);receipt.put(new JSONObject().put("job",job).put("level",r.normalLevel()).put("quests",18).put("realFinalContactDefeats",kills).put("fullHpBalanceVerified",false));
  }
  Path out=Path.of("build/reports/town-v163/progression.json");Files.createDirectories(out.getParent());Files.write(out,receipt.toString(2).getBytes(java.nio.charset.StandardCharsets.UTF_8));
 }
 @Test public void finalReportUsesActualTownNpcButtonAndJournalShowsLevel99Endpoint()throws Exception{
  GameView v=new GameView(c);v.layout(0,0,960,540);v.setSkillTestMode(false);RuntimeState s=TownInteriorTest.field(v,"state");afterE18(s,"WARRIOR");JSONArray done=s.rpg().campaign().snapshot().getJSONArray("complete");for(int i=1;i<18;i++)done.put(String.format(Locale.ROOT,"F%02d",i));assertTrue(s.rpg().campaign().restore(s.rpg().campaign().snapshot().put("complete",done)));ProgressionLeveling.restore(s.rpg(),98,0);call(v,"travelTown",RucesionTownGeometry.ID);approach(v,"rucesion_beach_keeper");screenshot(v,"final-report-before");TownInteriorTest.tap(v,667,429);assertEquals("F18",s.rpg().campaign().activeId());approach(v,"rucesion_beach_keeper");screenshot(v,"final-report-ready");TownInteriorTest.tap(v,667,429);assertEquals(99,s.rpg().normalLevel().intValue());assertTrue(s.rpg().campaign().finished());screenshot(v,"final-level99");assertEquals("뤼케시온 최종 보고",CampaignProgress.find("F18").title);assertFalse(CampaignWorld.canEnter(RucesionCoastGeometry.BOSS,99,true));
 }
}
