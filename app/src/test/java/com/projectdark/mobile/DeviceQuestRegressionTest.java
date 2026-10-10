package com.projectdark.mobile;

import static org.junit.Assert.*;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import com.projectdark.mobile.world.*;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.*;

/** Phone startup defaults and persisted saves, not a bare GameView with preview disabled. */
@RunWith(RobolectricTestRunner.class) @Config(sdk=34,manifest=Config.NONE)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public class DeviceQuestRegressionTest {
  Context context;
  final List<ActivityController<MainActivity>> activities=new ArrayList<>();
  @Before public void before(){
    context=RuntimeEnvironment.getApplication();
    for(String name:new String[]{"project_dark_f5m_v1","project_dark_journal_v1","project_dark_skill_test_v1"})
      context.getSharedPreferences(name,0).edit().clear().commit();
  }
  @After public void after(){for(ActivityController<MainActivity> a:activities)a.destroy();}
  GameView launch()throws Exception{
    ActivityController<MainActivity> a=Robolectric.buildActivity(MainActivity.class).create();activities.add(a);
    GameView v=TownInteriorTest.field(a.get(),"game");assertNotNull("MainActivity starts without crashing",v);v.layout(0,0,960,540);
    assertTrue("exercise the shipped default skill-preview mode",((SkillBook)TownInteriorTest.field(v,"skillBook")).testAccess());return v;
  }
  static void capture(GameView v,String name)throws Exception{
    Bitmap b=Bitmap.createBitmap(960,540,Bitmap.Config.ARGB_8888);v.draw(new Canvas(b));
    File f=new File("build/reports/device-review/v109-"+name+".png");f.getParentFile().mkdirs();
    try(FileOutputStream out=new FileOutputStream(f)){assertTrue(b.compress(Bitmap.CompressFormat.PNG,100,out));}b.recycle();
  }
  @Test public void openingQuickQuestFromTownEntersInnAndKillsWithoutAnAutoButtonTap()throws Exception{
    GameView v=launch();RuntimeState s=TownInteriorTest.field(v,"state");
    F5mAdaptedPrologueQuest q=TownInteriorTest.field(v,"f5mQuest");assertEquals(F5mAdaptedPrologueQuest.AcceptResult.ACTIVATED,q.accept());
    TownInteriorTest.tap(v,790,104);assertTrue("opening mouse hunt arms AUTO",(Boolean)TownInteriorTest.field(v,"autoAttackEnabled"));
    boolean entered=false;
    for(int frame=0;frame<2400&&q.state()==F5mAdaptedPrologueQuest.State.ACTIVE;frame++){
      TownInteriorTest.tick(v,1);
      if(!entered&&s.currentMapId().equals("milles_interior_inn")){
        entered=true;assertTrue("AUTO survives the inn transition",(Boolean)TownInteriorTest.field(v,"autoAttackEnabled"));capture(v,"opening-inn-entry");
      }
    }
    assertTrue("the real door route reached the inn",entered);
    assertEquals("only the quickquest tap must complete the mouse hunt",F5mAdaptedPrologueQuest.State.RETURN_READY,q.state());
    assertEquals(1,q.currentCount());assertTrue("the shared resolver dealt real damage",s.metrics().damageDealt()>0);capture(v,"opening-auto-kill");
  }
  @Test public void savedGrowthQuickQuestKillsAllThreeWithStartupDefaults()throws Exception{
    GameView v=launch();RuntimeState s=TownInteriorTest.field(v,"state");
    F5mAdaptedPrologueQuest q=TownInteriorTest.field(v,"f5mQuest");q.restore(F5mAdaptedPrologueQuest.State.COMPLETED,1);
    s.rpg().grantAdaptedReward(7500,100);s.applyDerivedGrowth();
    GrowthQuest2 growth=TownInteriorTest.field(v,"quest2");growth.unlockIfPrologueCompleted(q);assertTrue(growth.accept());v.pause();
    v=launch();growth=TownInteriorTest.field(v,"quest2");assertEquals(GrowthQuest2.State.ACTIVE,growth.state());
    TownInteriorTest.tap(v,790,104);assertTrue("growth hunt arms AUTO after restart",(Boolean)TownInteriorTest.field(v,"autoAttackEnabled"));
    for(int frame=0;frame<2400&&growth.state()==GrowthQuest2.State.ACTIVE;frame++)TownInteriorTest.tick(v,1);
    assertEquals("growth hunt retargets the remaining two mice",GrowthQuest2.State.RETURN_READY,growth.state());assertEquals(3,growth.currentCount());capture(v,"growth-auto-three-kills");
  }
  @Test public void startupPreviewShortbladeCountsThreeUsesAcrossSavedRestart()throws Exception{
    GameView v=launch();RuntimeState s=TownInteriorTest.field(v,"state");RpgProgressionState r=s.rpg();
    ((F5mAdaptedPrologueQuest)TownInteriorTest.field(v,"f5mQuest")).restore(F5mAdaptedPrologueQuest.State.COMPLETED,1);
    ((GrowthQuest2)TownInteriorTest.field(v,"quest2")).restore(GrowthQuest2.State.COMPLETED,3);
    r.grantAdaptedReward(22800,350);assertTrue(r.chooseInitialJob("WARRIOR"));
    SkillBook book=TownInteriorTest.field(v,"skillBook");CampaignProgress c=r.campaign();c.syncOpening(true,true);
    assertTrue(c.accept("M03",r,book));assertTrue(c.claim("M03",r));assertTrue(c.restore(c.snapshot().put("version",1)));assertTrue(c.accept("J01_WARRIOR",r,book));
    assertTrue(book.assign(0,"SK_전사_001"));TownInteriorTest.call(v,"saveSkillSlots");
    for(int use=0;use<3;use++){
      s=TownInteriorTest.field(v,"state");r=s.rpg();c=r.campaign();RuntimeState.Monster target=s.monsters().get(0);
      for(RuntimeState.Monster m:s.monsters())if(m!=target){m.alive=false;m.hp=0;m.respawnClock=30f;}
      target.x=s.player().x+32;target.y=s.player().y+16;target.hp=target.maxHp;target.alive=true;
      ((CombatController)TownInteriorTest.field(v,"combat")).selectTarget(target);
      TownInteriorTest.tap(v,672,429);
      for(int frame=0;frame<120&&c.count(CampaignProgress.find("J01_WARRIOR"),r)==use;frame++)TownInteriorTest.tick(v,1);
      assertEquals("accepted visible Shortblade slot tap counts in the actual app mode",use+1,c.count(CampaignProgress.find("J01_WARRIOR"),r));
      RuntimeCombatSession session=TownInteriorTest.field(v,"combatSession");
      for(int frame=0;frame<240&&session.cooldownRemaining("player","SK_전사_001")>0;frame++)TownInteriorTest.tick(v,1);
      TownInteriorTest.tick(v,15);
      if(use==0){v.pause();v=launch();s=TownInteriorTest.field(v,"state");assertEquals("one use survives upgrade-style process restart",1,s.rpg().campaign().count(CampaignProgress.find("J01_WARRIOR"),s.rpg()));}
    }
    s=TownInteriorTest.field(v,"state");assertEquals(CampaignProgress.Status.REPORT,s.rpg().campaign().status(CampaignProgress.find("J01_WARRIOR"),s.rpg()));capture(v,"shortblade-three-report");
  }
  @Test public void rescueQuickslotApproachesAndActuallyForcesAggroWithoutDamage()throws Exception{
    GameView v=launch();RuntimeState s=TownInteriorTest.field(v,"state");SkillBook book=TownInteriorTest.field(v,"skillBook");
    RuntimeState.Monster target=s.monsters().get(0);for(RuntimeState.Monster m:s.monsters())if(m!=target)m.alive=false;
    SkillActionContract.Rule rule=SkillActionContract.get("SK_전사_012");
    assertFalse("Rescue is not a screen-distance attack",SkillActionContract.canStart(rule,0,0,128,64,true));
    WorldRuntimeAdapter world=TownInteriorTest.field(v,"worldAdapter");boolean placed=false;
    for(WorldMoveTargetController.TileCenter tile:world.navigationTiles()){
      float distance=(float)Math.hypot(tile.x-s.player().x,tile.y-s.player().y);if(distance<90||distance>150)continue;
      target.x=tile.x;target.y=tile.y;
      if(world.monsterApproachPathSteps(target.id,CanonicalMeleeTileContract.REACH_DISTANCE)>=2){placed=true;break;}
    }
    assertTrue("fixture has a reachable distant mouse",placed);world.snapCameraToPlayer();
    s.skillEffects().put("player","SK_도적_004","STEALTH",1,30);s.skillEffects().put(target.id,"SK_도적_009","AGGRO_RESET",1,30);
    assertTrue(book.assign(0,"SK_전사_012"));((CombatController)TownInteriorTest.field(v,"combat")).selectTarget(target);int hp=target.hp;
    TownInteriorTest.tap(v,672,429);assertFalse("no remote taunt is applied before walking adjacent",s.skillEffects().has(target.id,"TAUNT"));
    for(int frame=0;frame<600&&!s.skillEffects().has(target.id,"TAUNT");frame++)TownInteriorTest.tick(v,1);
    assertTrue("accepted Rescue changes live monster aggro",s.skillEffects().has(target.id,"TAUNT"));
    assertTrue("Rescue was applied only from the adjacent tile",CanonicalMeleeTileContract.reachable(s.player().x,s.player().y,target.x,target.y));
    assertFalse(s.skillEffects().has(target.id,"AGGRO_RESET"));assertEquals("Rescue never deals monster damage",hp,target.hp);
    int playerHp=s.player().hp;for(int frame=0;frame<120&&s.player().hp==playerHp;frame++)TownInteriorTest.tick(v,1);
    assertTrue("taunted mouse attacks its caster despite ordinary stealth suppression",s.player().hp<playerHp);assertEquals(hp,target.hp);capture(v,"rescue-live-taunt");
  }
}
