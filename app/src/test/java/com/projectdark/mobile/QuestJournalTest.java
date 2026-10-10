package com.projectdark.mobile;

import static org.junit.Assert.*;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.MotionEvent;
import com.projectdark.mobile.world.*;
import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.*;

@RunWith(RobolectricTestRunner.class) @Config(sdk=34,manifest=Config.NONE) @GraphicsMode(GraphicsMode.Mode.NATIVE)
public class QuestJournalTest {
  Context c;
  @Before public void setup(){c=RuntimeEnvironment.getApplication();for(String name:new String[]{"project_dark_f5m_v1","project_dark_journal_v1","project_dark_skill_test_v1"})c.getSharedPreferences(name,0).edit().clear().commit();F5mSaveStore.install(c);}
  GameView start(){GameView v=new GameView(c);v.layout(0,0,960,540);return v;}
  List<QuestJournalModel.Row> rows(GameView v)throws Exception{RuntimeState s=TownInteriorTest.field(v,"state");return ((QuestJournalModel)TownInteriorTest.field(v,"questJournalModel")).rows(TownInteriorTest.field(v,"f5mQuest"),TownInteriorTest.field(v,"quest2"),TownInteriorTest.field(v,"inPoteField"),s.rpg());}
  static void gesture(GameView v,float x,float y){for(int action:new int[]{MotionEvent.ACTION_DOWN,MotionEvent.ACTION_UP}){MotionEvent e=MotionEvent.obtain(0,1,action,x,y,0);v.onTouchEvent(e);e.recycle();}}
  void capture(GameView v,String name)throws Exception{Bitmap b=Bitmap.createBitmap(v.getWidth(),v.getHeight(),Bitmap.Config.ARGB_8888);v.draw(new Canvas(b));File f=new File("build/reports/device-review/v97-"+name+".png");f.getParentFile().mkdirs();try(FileOutputStream out=new FileOutputStream(f)){assertTrue(b.compress(Bitmap.CompressFormat.PNG,100,out));}b.recycle();}
  void completeOpening(GameView v)throws Exception{F5mAdaptedPrologueQuest q=TownInteriorTest.field(v,"f5mQuest");q.restore(F5mAdaptedPrologueQuest.State.COMPLETED,1);((GrowthQuest2)TownInteriorTest.field(v,"quest2")).unlockIfPrologueCompleted(q);}
  @Test public void rightIconListsEveryQuestAndAvailableFilterOnlyShowsReceivableQuest()throws Exception{
    GameView v=start();assertEquals(43,rows(v).size());assertEquals(1,((QuestJournalModel)TownInteriorTest.field(v,"questJournalModel")).available(rows(v)));
    gesture(v,918,28);assertTrue((Boolean)TownInteriorTest.field(v,"questJournalOpen"));QuestJournalWindow w=TownInteriorTest.field(v,"questJournalWindow");assertEquals(43,w.visible(rows(v)).size());capture(v,"all-available");
    gesture(v,365,124);assertEquals(QuestJournalWindow.Filter.AVAILABLE,w.filter);assertEquals(1,w.visible(rows(v)).size());assertEquals(F5mAdaptedPrologueQuest.QUEST_ID,w.selected(rows(v)).id);capture(v,"available-only");
    gesture(v,540,124);assertTrue(w.visible(rows(v)).isEmpty());capture(v,"empty-progress");gesture(v,818,74);assertFalse((Boolean)TownInteriorTest.field(v,"questJournalOpen"));
  }
  @Test public void lockedConditionsAndLastQuestStayReachableWithoutNavigatingOrRewarding()throws Exception{
    GameView v=start();RuntimeState s=TownInteriorTest.field(v,"state");float x=s.player().x,y=s.player().y;long gold=s.rpg().gold();gesture(v,918,28);QuestJournalWindow w=TownInteriorTest.field(v,"questJournalWindow");
    gesture(v,250,230);assertEquals(GrowthQuest2.QUEST_ID,w.selected(rows(v)).id);assertEquals(QuestJournalModel.Status.LOCKED,w.selected(rows(v)).status);capture(v,"locked-prerequisite");gesture(v,680,443);assertTrue((Boolean)TownInteriorTest.field(v,"questJournalOpen"));
    List<QuestJournalModel.Row> all=rows(v);for(int i=0;i<(all.size()+3)/4;i++)gesture(v,355,450);assertEquals("journal can scroll to its final five rows",Math.max(0,all.size()-5),w.offset);gesture(v,250,394);assertEquals("CAMPAIGN_D04",w.selected(rows(v)).id);capture(v,"last-planned-quest");gesture(v,680,443);
    assertEquals(x,s.player().x,0);assertEquals(y,s.player().y,0);assertEquals(gold,s.rpg().gold().longValue());assertTrue((Boolean)TownInteriorTest.field(v,"questJournalOpen"));
    gesture(v,720,124);assertTrue(w.visible(rows(v)).isEmpty());capture(v,"empty-completed");
  }
  @Test public void compactQuestFirstTouchActuallyReachesJamesAndAcceptsHisQuest()throws Exception{
    GameView v=start();RuntimeState s=TownInteriorTest.field(v,"state");s.rpg().restoreBaseResources(20000,20000);s.applyDerivedGrowth();s.player().hp=s.player().maxHp;capture(v,"compact-initial");
    gesture(v,100,155);assertFalse((Boolean)TownInteriorTest.field(v,"questJournalOpen"));TownInteriorTest.tick(v,2200);
    InteractionController i=TownInteriorTest.field(v,"interaction");assertTrue("first compact touch drives actual NPC approach",i.dialogOpen());assertEquals("milles_guide_proto",i.dialogNpc().id);capture(v,"quick-arrived-james");gesture(v,550,426);assertEquals(F5mAdaptedPrologueQuest.State.ACTIVE,((F5mAdaptedPrologueQuest)TownInteriorTest.field(v,"f5mQuest")).state());
    gesture(v,918,28);gesture(v,540,124);assertEquals(1,((QuestJournalWindow)TownInteriorTest.field(v,"questJournalWindow")).visible(rows(v)).size());capture(v,"active-opening");
  }
  @Test public void firstTouchCrossesInnDoorTargetsMouseAndReportsToMary()throws Exception{
    GameView v=start();RuntimeState s=TownInteriorTest.field(v,"state");s.rpg().restoreBaseResources(20000,20000);s.applyDerivedGrowth();s.player().hp=s.player().maxHp;
    gesture(v,100,155);TownInteriorTest.tick(v,2200);assertTrue(((InteractionController)TownInteriorTest.field(v,"interaction")).dialogOpen());gesture(v,550,426);
    gesture(v,100,155);for(int frame=0;frame<2400&&!s.currentMapId().equals("milles_interior_inn");frame++)TownInteriorTest.tick(v,1);assertEquals("milles_interior_inn",s.currentMapId());CombatController combat=TownInteriorTest.field(v,"combat");assertNotNull("entry automatically continues to actual objective",combat.target());assertEquals(F5mAdaptedPrologueQuest.OPENING_MONSTER_ID,combat.target().id);capture(v,"quick-inn-target");
    assertTrue("objective arms actual automatic combat",(Boolean)TownInteriorTest.field(v,"autoAttackEnabled"));F5mAdaptedPrologueQuest opening=TownInteriorTest.field(v,"f5mQuest");for(int frame=0;frame<2400&&opening.state()==F5mAdaptedPrologueQuest.State.ACTIVE;frame++)TownInteriorTest.tick(v,1);assertEquals("AUTO completes the hunt without artificial damage",F5mAdaptedPrologueQuest.State.RETURN_READY,opening.state());gesture(v,100,155);TownInteriorTest.tick(v,600);assertTrue("report navigation actually opens Mary's dialogue",(Boolean)TownInteriorTest.field(v,"innDialogueOpen"));capture(v,"quick-mary-report");gesture(v,570,426);assertEquals(F5mAdaptedPrologueQuest.State.COMPLETED,((F5mAdaptedPrologueQuest)TownInteriorTest.field(v,"f5mQuest")).state());assertEquals(GrowthQuest2.State.AVAILABLE,((GrowthQuest2)TownInteriorTest.field(v,"quest2")).state());capture(v,"quick-next-growth");
  }
  @Test public void growthReportCompletionAndForestVisitReflectImmediatelyAndRestore()throws Exception{
    GameView v=start();completeOpening(v);((RuntimeState)TownInteriorTest.field(v,"state")).rpg().grantAdaptedReward(10000,100);GrowthQuest2 g=TownInteriorTest.field(v,"quest2");assertTrue(g.accept());RuntimeState s=TownInteriorTest.field(v,"state");
    for(String id:new String[]{"combat_dummy_01","combat_dummy_02","combat_dummy_03"})for(RuntimeState.Monster m:s.monsters())if(id.equals(m.id))s.damage(m,m.hp);
    TownInteriorTest.tick(v,1);assertEquals(GrowthQuest2.State.RETURN_READY,g.state());assertEquals(QuestJournalModel.Status.REPORT,rows(v).get(1).status);gesture(v,918,28);capture(v,"growth-report");gesture(v,818,74);
    assertTrue(g.turnIn(s.rpg()));long rewardGold=s.rpg().gold();assertFalse(g.turnIn(s.rpg()));assertEquals(rewardGold,s.rpg().gold().longValue());assertEquals(QuestJournalModel.Status.AVAILABLE,rows(v).get(2).status);gesture(v,918,28);gesture(v,720,124);assertEquals(2,((QuestJournalWindow)TownInteriorTest.field(v,"questJournalWindow")).visible(rows(v)).size());capture(v,"completed-chain");gesture(v,818,74);
    TownInteriorTest.call(v,"enterPoteField");assertEquals(QuestJournalModel.JOB_CHOICE,rows(v).get(2).id);assertEquals(QuestJournalModel.Status.AVAILABLE,rows(v).get(2).status);TownInteriorTest.call(v,"leavePoteField");assertTrue(F5mSaveStore.checkpointActive());F5mSaveStore.install(c);GameView restored=start();assertEquals(QuestJournalModel.Status.AVAILABLE,rows(restored).get(2).status);assertEquals(rewardGold,((RuntimeState)TownInteriorTest.field(restored,"state")).rpg().gold().longValue());gesture(restored,918,28);capture(restored,"restored-completed");
  }
  @Test public void quickQuestLeavesAnyServiceInteriorBeforeContinuingToTheNextNpc()throws Exception{
    GameView v=start();completeOpening(v);TownInteriorTest.enter(v,TownInteriorDef.forMap("milles_interior_inn"));RuntimeState s=TownInteriorTest.field(v,"state");s.rpg().restoreBaseResources(20000,20000);s.applyDerivedGrowth();s.player().hp=s.player().maxHp;
    capture(v,"compact-growth-inn");gesture(v,100,155);TownInteriorTest.tick(v,2400);assertEquals(WorldDef.ID,s.currentMapId());InteractionController i=TownInteriorTest.field(v,"interaction");assertTrue(i.dialogOpen());assertEquals("milles_guide_proto",i.dialogNpc().id);gesture(v,550,426);assertEquals(GrowthQuest2.State.ACTIVE,((GrowthQuest2)TownInteriorTest.field(v,"quest2")).state());capture(v,"continued-growth");
  }
  @Test public void wideViewportMovesQuestBesideSkillsAndJournalToTheCenter()throws Exception{
    GameView v=start();v.layout(0,0,2340,1080);float d=GameView.rightHudOffsetForView(2340,1080);assertEquals(1018,GameView.questIconX(d),0);gesture(v,GameView.questIconX(d)*2,56);assertTrue((Boolean)TownInteriorTest.field(v,"questJournalOpen"));capture(v,"wide-all");gesture(v,(818+d/2)*2,148);assertFalse((Boolean)TownInteriorTest.field(v,"questJournalOpen"));capture(v,"wide-hud");assertFalse(GameView.blocksWorldTapForHud(294,28,false));
  }
  @Test public void manualWorldTapCancelsPendingQuestDoorContinuation()throws Exception{
    GameView v=start();((F5mAdaptedPrologueQuest)TownInteriorTest.field(v,"f5mQuest")).accept();gesture(v,100,155);assertTrue((Boolean)TownInteriorTest.field(v,"questEntryPending"));gesture(v,310,190);assertFalse("manual target replaces pending quest continuation",(Boolean)TownInteriorTest.field(v,"questEntryPending"));assertFalse((Boolean)TownInteriorTest.field(v,"questExitPending"));
  }
  @Test public void journalDragScrollDoesNotStartWorldMovement()throws Exception{
    GameView v=start();gesture(v,918,28);RuntimeState s=TownInteriorTest.field(v,"state");float x=s.player().x,y=s.player().y;
    for(int[] event:new int[][]{{0,380},{2,320},{2,240},{2,180},{1,180}}){MotionEvent e=MotionEvent.obtain(0,1,event[0],240,event[1],0);v.onTouchEvent(e);e.recycle();}
    assertTrue(((QuestJournalWindow)TownInteriorTest.field(v,"questJournalWindow")).offset>=3);assertEquals(x,s.player().x,0);assertEquals(y,s.player().y,0);capture(v,"dragged-list");
  }
  @Test public void growthCompletionOpensFirstJobQuestAndRoutesToMichael()throws Exception{
    GameView v=start();F5mAdaptedPrologueQuest q=TownInteriorTest.field(v,"f5mQuest");GrowthQuest2 g=TownInteriorTest.field(v,"quest2");RuntimeState runtime=TownInteriorTest.field(v,"state");
    q.restore(F5mAdaptedPrologueQuest.State.COMPLETED,1);g.restore(GrowthQuest2.State.COMPLETED,3);runtime.rpg().grantAdaptedReward(22800,0);
    QuestJournalModel model=TownInteriorTest.field(v,"questJournalModel");List<QuestJournalModel.Row> actual=model.rows(q,g,false,runtime.rpg());
    QuestJournalModel.Row job=null;for(QuestJournalModel.Row row:actual)if(QuestJournalModel.JOB_CHOICE.equals(row.id))job=row;
    assertNotNull(job);assertEquals(QuestJournalModel.Status.AVAILABLE,job.status);
    assertEquals(QuestJournalModel.JOB_CHOICE,model.current(q,g,false,runtime.rpg()).id);
    assertTrue(runtime.npcs().stream().anyMatch(n->"milles_job_counselor".equals(n.id)));
  }
}
