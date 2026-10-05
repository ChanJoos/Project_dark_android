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
    state.put("complete",new org.json.JSONArray().put("M01").put("M02").put("M03").put("J01_WARRIOR"));
    state.put("active","M04");assertTrue(campaign.restore(state));
    ((F5mAdaptedPrologueQuest)TownInteriorTest.field(view,"f5mQuest")).restore(F5mAdaptedPrologueQuest.State.COMPLETED,1);
    ((GrowthQuest2)TownInteriorTest.field(view,"quest2")).restore(GrowthQuest2.State.COMPLETED,3);
    assertEquals("campaign hunt is the visible quick quest","CAMPAIGN_M04",((QuestJournalModel)TownInteriorTest.field(view,"questJournalModel")).current(TownInteriorTest.field(view,"f5mQuest"),TownInteriorTest.field(view,"quest2"),false,r).id);
    TownInteriorTest.tap(view,80,150);
    assertTrue("quick hunt turns AUTO on",(Boolean)TownInteriorTest.field(view,"autoAttackEnabled"));
    CombatController combat=TownInteriorTest.field(view,"combat");
    assertNotNull("quick hunt selects an objective target",combat.target());
    assertTrue(campaign.wanted(CampaignProgress.find("M04"),combat.target().id));
  }

  @Test public void warriorShortbladePracticeCountsARealGameViewEffect()throws Exception{
    GameView view=new GameView(context);view.layout(0,0,960,540);
    RuntimeState runtime=TownInteriorTest.field(view,"state");RpgProgressionState r=runtime.rpg();
    r.grantAdaptedReward(5_000_000,0);assertTrue(r.chooseInitialJob("WARRIOR"));
    SkillBook book=TownInteriorTest.field(view,"skillBook");CampaignProgress campaign=r.campaign();campaign.syncOpening(true,true);
    assertTrue(campaign.accept("M03",r,book));assertTrue(campaign.claim("M03",r));
    assertTrue(campaign.accept("J01_WARRIOR",r,book));
    RuntimeState.Monster target=null;for(RuntimeState.Monster m:runtime.monsters())if(m.alive){target=m;break;}
    assertNotNull("test needs an actual spawned target",target);
    target.x=runtime.player().x+32;target.y=runtime.player().y+16;
    ((CombatController)TownInteriorTest.field(view,"combat")).selectTarget(target);
    SkillBook.Entry shortblade=book.get("SK_전사_001");assertNotNull(shortblade);assertTrue(book.usable(shortblade.id));
    Method use=GameView.class.getDeclaredMethod("useBookSkillNow",SkillBook.Entry.class);use.setAccessible(true);use.invoke(view,shortblade);
    assertEquals("GameView submits the selected warrior ability",shortblade.id,TownInteriorTest.field(view,"activeSkillVisualId"));
    TownInteriorTest.tick(view,5);
    CampaignProgress.Def quest=CampaignProgress.find("J01_WARRIOR");
    assertEquals("the GameView EFFECT_APPLIED event increments the live quest",1,campaign.count(quest,r));
    assertEquals(CampaignProgress.Status.ACTIVE,campaign.status(quest,r));
  }
}
