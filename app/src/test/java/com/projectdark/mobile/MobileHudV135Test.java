package com.projectdark.mobile;
import static org.junit.Assert.*;
import android.content.Context;
import android.graphics.*;
import android.view.MotionEvent;
import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.*;
@RunWith(RobolectricTestRunner.class) @Config(sdk=34,manifest=Config.NONE) @GraphicsMode(GraphicsMode.Mode.NATIVE)
public final class MobileHudV135Test {
  Context c;
  @Before public void setup(){c=RuntimeEnvironment.getApplication();reset();}
  void reset(){c.getSharedPreferences("project_dark_f5m_v1",0).edit().clear().commit();c.getSharedPreferences("project_dark_skill_test_v1",0).edit().clear().commit();F5mSaveStore.install(c);}
  GameView view(int w,int h){GameView v=new GameView(c);v.layout(0,0,w,h);return v;}
  @Test public void liveTrackerShowsRealCurrentAndNextRowsAndSelectsTheirOwnDetails()throws Exception{
    GameView v=view(960,540);RuntimeState state=MobileHudV134Test.field(v,"state");QuestJournalModel m=MobileHudV134Test.field(v,"questJournalModel");F5mAdaptedPrologueQuest q=MobileHudV134Test.field(v,"f5mQuest");GrowthQuest2 g=MobileHudV134Test.field(v,"quest2");
    List<QuestJournalModel.Row> all=m.rows(q,g,false,state.rpg()),rows=MobileHudQuest.shown(all);assertEquals(3,rows.size());assertEquals(m.current(q,g,false,state.rpg()).id,rows.get(0).id);
    for(int i=0;i<3;i++){RectF r=MobileHudLayout.questRow(i,0);assertSame(rows.get(i),MobileHudQuest.hit(all,r.centerX(),r.centerY(),0));}
    RectF next=MobileHudLayout.questRow(1,0);MobileHudV134Test.tap(v,next.centerX(),next.centerY());assertTrue((Boolean)MobileHudV134Test.field(v,"questJournalOpen"));QuestJournalWindow journal=MobileHudV134Test.field(v,"questJournalWindow");assertEquals(rows.get(1).id,journal.selected(all).id);assertFalse("locked future details cannot grant or route",rows.get(1).navigable());
  }
  @Test public void bothPotionsAboveRestoredStatusConsumeHealAndRestartInWideViewport()throws Exception{
    GameView v=view(2340,1080);RuntimeState r=MobileHudV134Test.field(v,"state");r.rpg().grantB5SmallHpPotion(3);assertEquals(RpgProgressionState.AutoLootResult.LOOTED,r.rpg().autoLootResolvedItem("IT_B_MP_POTION",3));r.player().hp=r.player().maxHp-10;r.player().mp=r.player().maxMp-10;
    for(int i=0;i<2;i++){RectF p=MobileHudLayout.potion(i,210);assertTrue(p.bottom<MobileHudLayout.status(210).top);tapLogical(v,p.centerX(),p.centerY(),2);}
    assertEquals(r.player().maxHp,r.player().hp);assertEquals(r.player().maxMp,r.player().mp);
    assertEquals(Integer.valueOf(2),r.rpg().inventory().get(RpgProgressionState.B_SMALL_POTION_ITEM_ID));assertEquals(Integer.valueOf(2),r.rpg().inventory().get("IT_B_MP_POTION"));
    for(int i=0;i<2;i++){RectF p=MobileHudLayout.potion(i,210);tapLogical(v,p.centerX(),p.centerY(),2);}assertEquals(Integer.valueOf(2),r.rpg().inventory().get("IT_B_MP_POTION"));
    v.pause();F5mSaveStore.install(c);RuntimeState saved=MobileHudV134Test.field(view(960,540),"state");assertEquals(Integer.valueOf(2),saved.rpg().inventory().get("IT_B_MP_POTION"));assertEquals(r.player().mp,saved.player().mp);
  }
  @Test public void movedJoystickAndAllVisibleSurfacesLeaveOldUnusedEdgesAvailable(){
    for(float d:new float[]{0,210}){assertTrue(GameView.blocksWorldTapForHud(84,398,false,d));assertFalse(GameView.blocksWorldTapForHud(160,455,false,d));assertTrue(GameView.blocksWorldTapForHud(370+d/2,530,false,d));assertTrue(GameView.blocksWorldTapForHud(456+d/2,398,false,d));assertTrue(GameView.blocksWorldTapForHud(790+d,220,false,d));assertFalse(GameView.blocksWorldTapForHud(610+d/2,490,false,d));}
  }
  @Test public void renderTownForestBothAspectRatiosAndActualQuestStates()throws Exception{
    for(int[] size:new int[][]{{960,540},{2340,1080}}){reset();GameView v=view(size[0],size[1]);v.setSkillTestMode(true);render(v,"town",size[0],size[1]);Method enter=GameView.class.getDeclaredMethod("enterPoteField");enter.setAccessible(true);enter.invoke(v);render(v,"forest",size[0],size[1]);}
    reset();GameView v=view(2340,1080);F5mAdaptedPrologueQuest q=MobileHudV134Test.field(v,"f5mQuest");q.accept();render(v,"quest-active",2340,1080);q.restore(F5mAdaptedPrologueQuest.State.RETURN_READY,1);render(v,"quest-report",2340,1080);
  }
  static void tapLogical(GameView v,float x,float y,float scale){MotionEvent e=MotionEvent.obtain(0,1,MotionEvent.ACTION_DOWN,x*scale,y*scale,0);v.onTouchEvent(e);e.recycle();}
  static void render(GameView v,String scene,int w,int h)throws Exception{for(String name:new String[]{"feedbackClock","rewardClock"}){Field f=GameView.class.getDeclaredField(name);f.setAccessible(true);f.setFloat(v,0);}Bitmap b=Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888);v.draw(new Canvas(b));File dir=new File("build/reports/mobile-hud-v135");dir.mkdirs();try(FileOutputStream out=new FileOutputStream(new File(dir,scene+"-"+w+"x"+h+".png"))){assertTrue(b.compress(Bitmap.CompressFormat.PNG,100,out));}b.recycle();}
}
