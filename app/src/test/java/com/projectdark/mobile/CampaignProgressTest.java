package com.projectdark.mobile;

import static org.junit.Assert.*;
import android.content.Context;
import com.projectdark.mobile.world.*;
import java.util.*;
import org.json.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.Config;

/** Normal resource/combat progression; spatial navigation is tested separately. No EXP injection. */
@RunWith(RobolectricTestRunner.class) @Config(sdk=34,manifest=Config.NONE)
public class CampaignProgressTest {
 Context context;
 @Before public void reset(){context=RuntimeEnvironment.getApplication();context.getSharedPreferences("project_dark_f5m_v1",0).edit().clear().commit();F5mSaveStore.install(context);}
 @Test public void definitionsKeepOriginalCurveAndAddMapLocalForestRoute(){
  assertEquals(73,CampaignProgress.definitions().size());assertEquals(Long.valueOf(78000),LevelExpCurve.requiredForNext(10));
  for(String job:CampaignProgress.JOBS){CampaignProgress c=new CampaignProgress();assertTrue(c.restore(c.snapshot().put("version",3)));assertEquals(32,c.route(job).size());for(int tier=1;tier<=3;tier++){SkillAbilityCatalog.Ability a=SkillAbilityCatalog.get(CampaignProgress.skill(job,tier));assertNotNull(a);assertTrue("practice must resolve an actual effect "+a.id,a.damage()||a.heal());}}
 }
 @Test public void newForestRouteHasOrderedAtoDObjectivesAndSafeV3Migration(){
  CampaignProgress c=new CampaignProgress();List<CampaignProgress.Def> route=c.route("WARRIOR");assertEquals(44,route.size());int forestQuests=0;for(CampaignProgress.Def d:route)if(d.id.matches("[ABCD]\\d\\d"))forestQuests++;assertEquals(28,forestQuests);assertEquals("A01",findRoute(route,"A01").id);assertEquals(11,findRoute(route,"A01").level);RpgProgressionState atForestRegistration=new RpgProgressionState();atForestRegistration.grantAdaptedReward(378000,0);assertEquals("A01 is accepted only after the Lv11 handoff",0,c.rewardExp(findRoute(route,"A01"),atForestRegistration));assertEquals("M06 remains the V110 Lv10→11 handoff",10,findRoute(route,"M06").level);assertEquals("B01",findRoute(route,"B01").id);assertEquals("C01",findRoute(route,"C01").id);assertEquals("D03",findRoute(route,"D03").id);assertEquals("POTE_MANTIS",CampaignProgress.find("D03").targets);long to41=0;for(int lv=1;lv<=40;lv++)to41+=LevelExpCurve.requiredForNext(lv);assertEquals(to41,c.rewardExp(CampaignProgress.find("D03"),new RpgProgressionState()));
  assertTrue(c.snapshot().optBoolean("forestRoute"));JSONObject v3=c.snapshot().put("version",3).put("complete",new JSONArray().put("M01").put("M02").put("M03").put("M06").put("M07")).put("visited",new JSONArray().put("MAP_POTE_04"));CampaignProgress restored=new CampaignProgress();assertTrue(restored.restore(v3));assertTrue("completed V110 intro migrates to the new forest route",restored.snapshot().optBoolean("forestRoute"));JSONArray completed=restored.snapshot().getJSONArray("complete");assertTrue(contains(completed,"M06"));assertTrue(contains(completed,"A01"));assertTrue(contains(completed,"A02"));assertTrue(contains(completed,"A03"));assertTrue(contains(restored.snapshot().getJSONArray("visited"),"MAP_POTE_04"));assertNotEquals(com.projectdark.mobile.world.CampaignWorld.BOSS_D,"MAP_POTE_04");
 }
 private CampaignProgress.Def findRoute(List<CampaignProgress.Def> route,String id){for(CampaignProgress.Def d:route)if(d.id.equals(id))return d;throw new AssertionError(id);}
 private boolean contains(JSONArray values,String id)throws Exception{for(int i=0;i<values.length();i++)if(id.equals(values.getString(i)))return true;return false;}
 @Test public void eachClassReachesFortyOneThroughResolverKillsAndQuestTurnIns()throws Exception{
  JSONObject report=new JSONObject();
  for(String job:CampaignProgress.JOBS){
   reset();RuntimeState state=new RuntimeState();RpgProgressionState r=state.rpg();SkillBook book=SkillBook.load(context);book.bindJob(r::currentJobCode);
   RuntimeCombatSession session=new RuntimeCombatSession(state,(a,t)->true,(a,id)->book.usable(id),(a)->true);
   F5mAdaptedPrologueQuest opening=F5mAdaptedPrologueQuest.openingFixture();GrowthQuest2 growth=new GrowthQuest2();F5mSaveStore.bindRuntime(state,opening,growth);F5mSaveStore.restoreAndBindSkillsActive(book);
   opening.accept();state.enterTownInterior(TownInteriorDef.forMap("milles_interior_inn"));state.ensureAdaptedMillesMouse();kill(state,session,book,state.monsters().get(0),null);
   for(CombatLedger.Event e:state.ledger().snapshot())opening.consume(e);assertEquals(F5mAdaptedPrologueQuest.State.RETURN_READY,opening.state());assertTrue(F5mSaveStore.commitTurnInActive(opening,r));
   state.enterMillesFromField(WorldDef.PLAYER_SPAWN_X,WorldDef.PLAYER_SPAWN_Y);growth.unlockIfPrologueCompleted(opening);assertTrue(growth.accept());
   for(RuntimeState.Monster m:new ArrayList<>(state.monsters()))if(m.id.startsWith("combat_dummy_")){kill(state,session,book,m,null);for(CombatLedger.Event e:state.ledger().snapshot())growth.consume(e);}
   assertTrue(growth.turnIn(r));r.campaign().syncOpening(true,true);assertTrue(r.chooseInitialJob(job));
   assertTrue("exercise old route fixture for existing compatibility regression",r.campaign().restore(r.campaign().snapshot().put("version",3)));
   int completed=2,kills=4;long skillActions=0;
   for(int guard=0;guard<1500&&!r.campaign().finished();guard++){
    CampaignProgress.Def d=r.campaign().next(r);assertNotNull(job,d);if(!d.id.startsWith("T")&&!d.id.startsWith("J01")&&!d.id.equals("M03"))allocate(r,job);state.applyDerivedGrowth();
    if(r.normalLevel()<d.level){String map=huntMap(r.normalLevel());enter(state,map);RuntimeState.Monster m=state.monsters().get(0);rest(state);kill(state,session,book,m,null);kills++;continue;}
      enter(state,d.map);assertTrue(job+" accept "+d.id,r.campaign().accept(d.id,r,book));
      switch(d.kind){
     case "KILL":case "PAIR":
      enter(state,d.id.equals("D03")?CampaignWorld.BOSS_D:d.id.equals("M04")?WorldDef.ID:d.level<20?"MAP_POTE_01":d.level<30?"MAP_POTE_02":"MAP_POTE_03");
      for(int guardKill=0;r.campaign().status(d,r)!=CampaignProgress.Status.REPORT&&guardKill<100;guardKill++){
       RuntimeState.Monster pick=null;for(RuntimeState.Monster m:state.monsters())if(Arrays.asList(d.targets.split(",")).contains(m.campaignRewardProfileId==null?m.id:m.campaignRewardProfileId)){if(d.kind.equals("PAIR")&&r.campaign().snapshot().getJSONObject("counts").optInt(m.campaignRewardProfileId,0)>=d.goal/2)continue;pick=m;break;}
       assertNotNull(d.id,pick);rest(state);kill(state,session,book,pick,null);kills++;
      }break;
     case "SKILL":
      enter(state,d.level<11?WorldDef.ID:d.level<20?"MAP_POTE_01":"MAP_POTE_02");String skill=r.campaign().practiceSkill(d,r);assertTrue(book.usable(skill));
      for(int practice=0;r.campaign().status(d,r)!=CampaignProgress.Status.REPORT&&practice<10;practice++){
       rest(state);RuntimeState.Monster target=state.monsters().get(0);if(!target.alive){state.tick(25);session.tick(25);}
       position(state,target);if(SkillRuntimeCatalog.healing(skill)){takeHit(state,session,book,target);}
       long before=r.campaign().count(d,r);cast(state,session,book,target,skill);skillActions+=r.campaign().count(d,r)-before;
      }break;
     case "STAT":assertTrue(r.spendStat(job.equals("MAGE")||job.equals("CLERIC")?"WIS":"STR"));break;
     case "LEARN":String first=CampaignProgress.beginnerSkill(job);SkillAcquisition acquisition=new SkillAcquisition(book);SkillAcquisition.Quote quote=acquisition.quote(book.get(first),r);for(int i=0;i<5;i++)for(int add=quote.current[i];add<quote.required[i];add++)assertTrue(r.spendStat(SkillAcquisition.STATS[i]));assertTrue(acquisition.learn(first,r,()->F5mSaveStore.checkpointActive()));r.campaign().observeBasics(r,book);break;
     case "SLOT":assertTrue(book.assign(0,CampaignProgress.beginnerSkill(job)));r.campaign().observeBasics(r,book);break;
     case "BUY":assertEquals(TownCommerce.Result.OK,TownCommerce.transact(state,d.id.equals("T04")?TownInteriorDef.Kind.EQUIPMENT:TownInteriorDef.Kind.REAGENT,TownCommerce.Operation.BUY,d.targets,1,()->F5mSaveStore.checkpointActive()));break;
     case "SELL":assertEquals(TownCommerce.Result.OK,TownCommerce.transact(state,TownInteriorDef.Kind.REAGENT,TownCommerce.Operation.SELL,d.targets,1,()->F5mSaveStore.checkpointActive()));break;
     case "EQUIP":assertEquals(RpgProgressionState.EquipResult.EQUIPPED,r.equip(d.targets));break;
     case "USE":case "QUICK_USE":
      enter(state,WorldDef.ID);rest(state);RuntimeState.Monster drill=state.monsters().get(0);if(!drill.alive){state.tick(25);session.tick(25);}position(state,drill);
      boolean mana=d.kind.equals("QUICK_USE")&&(job.equals("MAGE")||job.equals("CLERIC"));
      if(mana){if(job.equals("CLERIC")){takeHit(state,session,book,drill);}cast(state,session,book,drill,CampaignProgress.beginnerSkill(job));assertTrue(job+" "+d.id+" must actually consume MP before recovery",state.player().mp<state.player().maxMp);}else{takeHit(state,session,book,drill);assertTrue(job+" "+d.id+" must actually lose HP before recovery",state.player().hp<state.player().maxHp);}
      assertEquals(RpgProgressionState.UseResult.USED,d.kind.equals("QUICK_USE")?r.useQuickConsumable(mana?"IT_B_MP_POTION":RpgProgressionState.B_SMALL_POTION_ITEM_ID,state):r.useConsumable(d.targets,state));break;
     case "SUPPLY":
      assertTrue(r.buyItem(RpgProgressionState.B_SMALL_POTION_ITEM_ID,20));assertEquals(TownCommerce.Result.OK,TownCommerce.transact(state,TownInteriorDef.Kind.REAGENT,TownCommerce.Operation.BUY,"IT_B_MP_POTION",1,()->F5mSaveStore.checkpointActive()));assertEquals(TownCommerce.Result.OK,TownCommerce.transact(state,TownInteriorDef.Kind.REAGENT,TownCommerce.Operation.SELL,"IT_B_MP_POTION",1,()->F5mSaveStore.checkpointActive()));RuntimeState.Monster target;
      enter(state,WorldDef.ID);target=state.monsters().get(0);if(!target.alive){state.tick(25);session.tick(25);}position(state,target);takeHit(state,session,book,target);
      assertEquals(RpgProgressionState.UseResult.USED,r.useConsumable(RpgProgressionState.B_SMALL_POTION_ITEM_ID,state));break;
     case "ALTAR":enter(state,"MAP_POTE_03");for(int i=0;i<3;i++)assertTrue(r.campaign().purify("campaign_altar_"+i,r));break;
     default:break;
    }
    assertEquals(job+" report "+d.id,CampaignProgress.Status.REPORT,r.campaign().status(d,r));enter(state,d.map);assertTrue(r.campaign().claim(d.id,r));assertFalse(r.campaign().claim(d.id,r));if(d.id.equals("D03"))assertEquals("first Mantis clear directly awards the recorded ring once",1,r.inventory().getOrDefault("IT_RING_THREELINEGOLD",0).intValue());completed++;if(d.id.equals("M06")){assertEquals("the Milles handoff reaches Lv11 before entering Pote",Integer.valueOf(11),r.normalLevel());assertEquals(378000,earnedExp(r));assertEquals(16,completed);}if(d.id.equals("A01")){assertEquals("forest entry is registered at Lv11",Integer.valueOf(11),r.normalLevel());assertEquals(17,completed);}if(d.id.startsWith("J02_")||d.id.startsWith("J03_")){int tier=d.id.startsWith("J02_")?11:26;String tool="IT_B_CAMPAIGN_TOOL_"+job+"_"+tier;assertEquals(TownCommerce.Result.OK,TownCommerce.transact(state,TownInteriorDef.Kind.EQUIPMENT,TownCommerce.Operation.BUY,tool,1,()->F5mSaveStore.checkpointActive()));assertEquals(RpgProgressionState.EquipResult.EQUIPPED,r.equip(tool));}
    assertTrue(F5mSaveStore.checkpointActive());RpgProgressionState restored=new RpgProgressionState();F5mSaveStore.restoreRewardsActive(restored);assertEquals(r.normalExp(),restored.normalExp());assertEquals(r.campaign().snapshot().getJSONArray("complete").toString(),restored.campaign().snapshot().getJSONArray("complete").toString());assertEquals(r.campaign().activeId(),restored.campaign().activeId());if(d.id.equals("D03"))assertEquals("the first-clear reward survives process-style restore",1,restored.inventory().getOrDefault("IT_RING_THREELINEGOLD",0).intValue());
   }
   assertTrue(job+" ended level="+r.normalLevel()+" next="+(r.campaign().next(r)==null?"done":r.campaign().next(r).id)+" exp="+r.normalExp(),r.campaign().finished());assertEquals(44,completed);assertEquals("the D03 one-time turn-in advances the character to Lv41",Integer.valueOf(41),r.normalLevel());assertTrue(earnedExp(r)>=9000000);assertTrue(r.gold()>=0);assertFalse(book.testAccess());assertEquals(9,skillActions);
   report.put(job,new JSONObject().put("level",r.normalLevel()).put("earnedExp",earnedExp(r)).put("expIntoLevel",r.normalExp()).put("gold",r.gold()).put("kills",kills).put("completedQuests",completed).put("validSkillActions",skillActions).put("scope","normal Resolver/resource domain; placement fixture; phone/time acceptance pending"));
  }
  java.nio.file.Path out=java.nio.file.Path.of("build/reports/campaign/normal-five-classes.json");java.nio.file.Files.createDirectories(out.getParent());java.nio.file.Files.write(out,report.toString(2).getBytes(java.nio.charset.StandardCharsets.UTF_8));
 }
 @Test public void successfulWarriorTechniqueUseCountsEvenWhenMitigationProducesZeroDamage()throws Exception{
  RpgProgressionState r=new RpgProgressionState();r.grantAdaptedReward(1_000_000,0);assertTrue(r.chooseInitialJob("WARRIOR"));
  CampaignProgress c=r.campaign();c.syncOpening(true,true);SkillBook b=SkillBook.load(context);
  assertTrue(c.accept("M03",r,b));assertTrue(c.claim("M03",r));assertTrue(c.restore(c.snapshot().put("version",1)));assertTrue(c.accept("J01_WARRIOR",r,b));
  c.acceptedSkillUse("SK_전사_001",1,false,r);
  assertEquals("accepted use counts without requiring contact damage",1,c.count(CampaignProgress.find("J01_WARRIOR"),r));
  c.acceptedSkillUse("SK_전사_001",1,false,r);
  assertEquals("duplicate resolver sequence is ignored",1,c.count(CampaignProgress.find("J01_WARRIOR"),r));
  c.acceptedSkillUse("SK_전사_002",2,false,r);
  assertEquals("wrong technique does not count",1,c.count(CampaignProgress.find("J01_WARRIOR"),r));
 }

 @Test public void fullInventoryRejectsQuestWithoutPartialRewardsAndBadSnapshotsFailClosed()throws Exception{
  RpgProgressionState r=new RpgProgressionState();r.grantAdaptedReward(22800,0);r.chooseInitialJob("WARRIOR");r.campaign().syncOpening(true,true);assertTrue(r.campaign().accept("M03",r,null));r.autoLootResolvedItem("IT_B_MP_POTION",999999);long exp=r.normalExp(),gold=r.gold();assertFalse(r.campaign().claim("M03",r));assertEquals(exp,r.normalExp().longValue());assertEquals(gold,r.gold().longValue());assertEquals("M03",r.campaign().activeId());
  JSONObject invalid=r.campaign().snapshot().put("sequence",-1);assertFalse(r.campaign().restore(invalid));
 }
 @Test public void changedLv20to39TargetsResetOnlyActiveLegacyProgress()throws Exception{
  CampaignProgress campaign=new CampaignProgress();JSONObject old=new JSONObject().put("version",2).put("legacy",false).put("complete",new JSONArray().put("M01").put("M02").put("M03")).put("visited",new JSONArray()).put("altars",new JSONArray()).put("counts",new JSONObject().put("POTE_STRONG_GNOLL",8)).put("active","M12").put("sequence",4).put("skillSequence",0).put("bought",false).put("healed",false).put("sold",false);
  assertTrue(campaign.restore(old));assertNull("old objective progress must not auto-complete a different species objective",campaign.activeId());assertEquals(0,campaign.count(CampaignProgress.find("M12"),new RpgProgressionState()));assertEquals("already claimed quest rewards remain recorded",3,campaign.snapshot().getJSONArray("complete").length());
 }
 static long earnedExp(RpgProgressionState r){long total=r.normalExp();for(int i=1;i<r.normalLevel();i++)total+=LevelExpCurve.requiredForNext(i);return total;}
 static String huntMap(int lv){return lv<10?WorldDef.ID:lv<20?"MAP_POTE_01":lv<30?"MAP_POTE_02":"MAP_POTE_03";}
 static void enter(RuntimeState s,String map){if(map.equals(s.currentMapId()))return;if(WorldDef.ID.equals(map))s.enterMillesFromField(WorldDef.PLAYER_SPAWN_X,WorldDef.PLAYER_SPAWN_Y);else if(CampaignWorld.contains(map))s.enterCampaignMap(map,false);else throw new AssertionError(map);s.rpg().campaign().visit(map);}
 static void allocate(RpgProgressionState r,String job){int n=0;while(r.statPoints()>0)assertTrue(r.spendStat((n++%4==0)?("MAGE".equals(job)||"CLERIC".equals(job)?"WIS":"CON"):("MAGE".equals(job)||"CLERIC".equals(job)?"INT":"STR")));}
 static void takeHit(RuntimeState s,RuntimeCombatSession session,SkillBook b,RuntimeState.Monster m){int hp=s.player().hp;MonsterAutoCombatBridge.Result last=null;for(int n=0;n<60&&s.player().hp==hp;n++){position(s,m);last=session.monsterAutoBridge().submit(m.id,"player");tick(s,session,b,.3f);}assertTrue("real monster hit must resolve; last="+(last==null?"none":last.outcome+"/"+last.rejectReason)+" hp="+hp+"/"+s.player().hp,s.player().hp<hp);}
 static void rest(RuntimeState s){CampaignResources.freeRest(s);}
 static void position(RuntimeState s,RuntimeState.Monster m){s.player().x=m.x-32;s.player().y=m.y-16;}
 static void cast(RuntimeState s,RuntimeCombatSession session,SkillBook b,RuntimeState.Monster m,String id){position(s,m);int reach=Math.max(1,SkillActionContract.get(id).minReach);s.player().x=m.x-32*reach;s.player().y=m.y-16*reach;assertTrue(id+" submission",session.submitPlayer(SkillActionContract.get(id).selfAnchored()?"player":m.id,id).accepted());tick(s,session,b,3);}
 static void kill(RuntimeState s,RuntimeCombatSession session,SkillBook b,RuntimeState.Monster m,String skill){if(!m.alive){s.tick(25);session.tick(25);}position(s,m);int turns=0;while(m.alive&&turns++<500){session.submitPlayerBasicAttack(m.id);session.monsterAutoBridge().submit(m.id,"player");tick(s,session,b,.6f);if(s.player().hp<s.player().maxHp/2){if(s.rpg().inventory().getOrDefault(RpgProgressionState.B_SMALL_POTION_ITEM_ID,0)==0)assertTrue(s.rpg().buySmallPotion());assertEquals(RpgProgressionState.UseResult.USED,s.rpg().useConsumable(RpgProgressionState.B_SMALL_POTION_ITEM_ID,s));}assertTrue("normal character survives",s.player().alive);}assertFalse("kill has real damage "+m.id,m.alive);}
 static void tick(RuntimeState s,RuntimeCombatSession session,SkillBook book,float dt){RuntimeCombatSession.FrameResult frame=session.tick(dt);for(CombatResolver.Event e:frame.events)if(e.actorId.equals("player")){if(e.type==CombatResolver.EventType.ACTION_STARTED)s.rpg().campaign().acceptedSkillUse(e.actionId,e.actionSequence,book.testAccess(),s.rpg());else if(e.type==CombatResolver.EventType.EFFECT_APPLIED)s.rpg().campaign().appliedHealingUse(e.actionId,e.actionSequence,e.amount,book.testAccess(),s.rpg());}s.tick(dt);s.applyDerivedGrowth();}
}
