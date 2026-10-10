package com.projectdark.mobile;

import static org.junit.Assert.*;
import android.content.Context;
import java.lang.reflect.Method;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.GraphicsMode;
import com.projectdark.mobile.world.WorldRuntimeAdapter;

/** Exercises the user's quest controls through GameView and the production combat event path. */
@RunWith(RobolectricTestRunner.class)
@Config(sdk=34,manifest=Config.NONE)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public final class CampaignQuickQuestIntegrationTest {
  private Context context;
  @Before public void setup(){
    context=RuntimeEnvironment.getApplication();
    for(String name:new String[]{"project_dark_f5m_v1","project_dark_journal_v1","project_dark_skill_test_v1"})
      context.getSharedPreferences(name,0).edit().clear().commit();
    F5mSaveStore.install(context);
  }
  @Test public void quickQuestStartsAutoAttackAndKeepsKillsOnObjectiveSpecies()throws Exception{
    GameView view=new GameView(context);view.layout(0,0,960,540);
    RuntimeState runtime=TownInteriorTest.field(view,"state");RpgProgressionState r=runtime.rpg();
    r.grantAdaptedReward(5_000_000,0);assertTrue(r.chooseInitialJob("WARRIOR"));
    CampaignProgress campaign=r.campaign();
    org.json.JSONObject state=campaign.snapshot();
    state.put("version",1);state.put("complete",new org.json.JSONArray().put("M01").put("M02").put("M03").put("J01_WARRIOR"));
    state.put("active","M04");assertTrue(campaign.restore(state));
    ((F5mAdaptedPrologueQuest)TownInteriorTest.field(view,"f5mQuest")).restore(F5mAdaptedPrologueQuest.State.COMPLETED,1);
    ((GrowthQuest2)TownInteriorTest.field(view,"quest2")).restore(GrowthQuest2.State.COMPLETED,3);
    assertEquals("campaign hunt is the visible quick quest","CAMPAIGN_M04",((QuestJournalModel)TownInteriorTest.field(view,"questJournalModel")).current(TownInteriorTest.field(view,"f5mQuest"),TownInteriorTest.field(view,"quest2"),false,r).id);
    RuntimeState.Monster target=runtime.monsters().get(0);
    for(RuntimeState.Monster m:runtime.monsters())if(m!=target)m.alive=false;
    target.x=runtime.player().x+32;target.y=runtime.player().y+16;target.hp=1;
    TownInteriorTest.tap(view,790,104);
    assertTrue("quick hunt turns AUTO on",(Boolean)TownInteriorTest.field(view,"autoAttackEnabled"));
    CombatController combat=TownInteriorTest.field(view,"combat");
    assertNotNull("quick hunt selects an objective target",combat.target());
    assertTrue(campaign.wanted(CampaignProgress.find("M04"),combat.target().id));
    assertSame(target,combat.target());
    TownInteriorTest.tick(view,30);
    assertEquals("the tap-driven quick hunt defeats its selected quest target",1,campaign.count(CampaignProgress.find("M04"),r));
  }

  @Test public void killQuickQuestTravelsToForestTargetsAndAutoAttacksQuestMonster()throws Exception{
    GameView view=new GameView(context);view.layout(0,0,960,540);
    RuntimeState runtime=TownInteriorTest.field(view,"state");RpgProgressionState r=runtime.rpg();
    r.grantAdaptedReward(5_000_000,0);assertTrue(r.chooseInitialJob("WARRIOR"));
    CampaignProgress campaign=r.campaign();org.json.JSONObject saved=campaign.snapshot();
    saved.put("version",1);saved.put("complete",new org.json.JSONArray().put("M01").put("M02").put("M03").put("J01_WARRIOR").put("M04").put("M05").put("M06"));
    saved.put("active","M07");assertTrue(campaign.restore(saved));
    TownInteriorTest.tap(view,790,104);
    assertTrue("AUTO is visibly armed as soon as a kill quest is selected",(Boolean)TownInteriorTest.field(view,"autoAttackEnabled"));
    assertEquals("quick-quest route begins toward the forest travel guide","pote_travel_guide",((WorldRuntimeAdapter)TownInteriorTest.field(view,"worldAdapter")).movement().snapshot().targetEntityId);
    CombatController combat=TownInteriorTest.field(view,"combat");RuntimeState.Monster objectiveTarget=null;
    // Exercise the production GameView/update path through the guide approach,
    // map transition, objective selection, monster approach, and first attack.
    Method update=GameView.class.getDeclaredMethod("update",float.class);update.setAccessible(true);
    for(int frame=0;frame<1800&&campaign.count(CampaignProgress.find("M07"),r)==0;frame++){
      update.invoke(view,.05f);
      if((Boolean)TownInteriorTest.field(view,"inPoteField")){
        assertEquals("quickquest enters its objective map","MAP_POTE_01",runtime.currentMapId());
        assertTrue("AUTO remains armed after the Milles-to-forest transition",(Boolean)TownInteriorTest.field(view,"autoAttackEnabled"));
        if(objectiveTarget==null&&combat.target()!=null){objectiveTarget=combat.target();assertTrue("selected mob matches M07's red/green objective",campaign.wanted(CampaignProgress.find("M07"),objectiveTarget.campaignRewardProfileId));objectiveTarget.hp=1;}
      }
    }
    assertTrue("the route selected an objective monster",objectiveTarget!=null);
    assertTrue("AUTO defeated a target and advanced the live campaign count",campaign.count(CampaignProgress.find("M07"),r)>0);
    assertTrue("AUTO stays enabled after an objective kill",(Boolean)TownInteriorTest.field(view,"autoAttackEnabled"));
  }

  @Test public void warriorShortbladePracticeCountsThreeAcceptedVisibleQuickslotUses()throws Exception{
    GameView view=new GameView(context);view.layout(0,0,960,540);
    RuntimeState runtime=TownInteriorTest.field(view,"state");RpgProgressionState r=runtime.rpg();
    r.grantAdaptedReward(5_000_000,0);assertTrue(r.chooseInitialJob("WARRIOR"));
    SkillBook book=TownInteriorTest.field(view,"skillBook");assertFalse("quest input uses production skill rules, not skill-test mode",book.testAccess());CampaignProgress campaign=r.campaign();campaign.syncOpening(true,true);
    assertTrue(campaign.accept("M03",r,book));assertTrue(campaign.claim("M03",r));assertTrue(campaign.restore(campaign.snapshot().put("version",1)));
    assertTrue(campaign.accept("J01_WARRIOR",r,book));
    RuntimeState.Monster target=null;for(RuntimeState.Monster m:runtime.monsters())if(m.alive){target=m;break;}
    assertNotNull("test needs an actual spawned target",target);
    target.x=runtime.player().x+32;target.y=runtime.player().y+16;
    target.hp=10000;
    ((CombatController)TownInteriorTest.field(view,"combat")).selectTarget(target);
    SkillBook.Entry shortblade=book.get("SK_전사_001");assertNotNull(shortblade);assertTrue(book.usable(shortblade.id));
    int previousProficiency=book.proficiency("SK_전사_001");
    for(int use=0;use<3;use++){
      TownInteriorTest.tap(view,672,429);
      for(int frame=0;frame<120&&(campaign.count(CampaignProgress.find("J01_WARRIOR"),r)==use||book.proficiency("SK_전사_001")==previousProficiency);frame++)TownInteriorTest.tick(view,1);
      assertTrue("tap starts and resolves the visible Shortblade quickslot action #"+(use+1),book.proficiency("SK_전사_001")>previousProficiency);
      previousProficiency=book.proficiency("SK_전사_001");
      assertEquals("each accepted Shortblade action increments the active quest immediately",use+1,campaign.count(CampaignProgress.find("J01_WARRIOR"),r));
      if(use<2){
        RuntimeCombatSession session=TownInteriorTest.field(view,"combatSession");
        for(int frame=0;frame<240&&session.cooldownRemaining(RuntimeCombatSession.PLAYER_ID,"SK_전사_001")>0f;frame++)TownInteriorTest.tick(view,1);
        assertEquals("wait for the real Shortblade cooldown before the next quickslot tap",0f,session.cooldownRemaining(RuntimeCombatSession.PLAYER_ID,"SK_전사_001"),.01f);
        TownInteriorTest.tick(view,15); // Let the live skill animation settle before the next physical slot tap.
      }
    }
    CampaignProgress.Def quest=CampaignProgress.find("J01_WARRIOR");
    assertEquals("quest copy matches the requested action count","숏블레이드 사용 3회",campaign.objective(quest));
    assertEquals("three actual quickslot uses advance the live warrior quest",3,campaign.count(quest,r));
    assertEquals(CampaignProgress.Status.REPORT,campaign.status(quest,r));
    assertTrue(campaign.claim("J01_WARRIOR",r));
  }
}
