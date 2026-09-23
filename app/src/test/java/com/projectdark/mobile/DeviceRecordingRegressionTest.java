package com.projectdark.mobile;

import static org.junit.Assert.*;
import android.content.Context;
import android.view.MotionEvent;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import com.projectdark.mobile.world.WorldRuntimeAdapter;

@RunWith(RobolectricTestRunner.class)
@Config(sdk=34,manifest=Config.NONE)
public class DeviceRecordingRegressionTest {
  private Context context;
  @Before public void reset(){context=RuntimeEnvironment.getApplication();context.getSharedPreferences("project_dark_f5m_v1",0).edit().clear().commit();F5mSaveStore.install(context);}
  private static Object field(Object o,String name)throws Exception{Field f=o.getClass().getDeclaredField(name);f.setAccessible(true);return f.get(o);}
  private static void attack(GameView v)throws Exception{Method m=GameView.class.getDeclaredMethod("attack");m.setAccessible(true);m.invoke(v);}
  @Test public void productionAttackDelaysDamageAndUsesEquippedWeapon()throws Exception{
    GameView view=new GameView(context);RuntimeState s=(RuntimeState)field(view,"state");
    RuntimeState.Monster target=s.monsters().get(0);s.player().x=target.x-32;s.player().y=target.y-16;
    assertEquals(RpgProgressionState.PLAYTEST_WEAPON_ITEM_ID,s.rpg().equipment().get(RpgProgressionState.WEAPON_SLOT));
    ((WorldRuntimeAdapter)field(view,"worldAdapter")).snapCameraToPlayer();
    ((CombatController)field(view,"combat")).selectTarget(target);
    int hp=target.hp;attack(view);assertEquals("damage must wait for contact",hp,target.hp);assertEquals("SWING",field(view,"action").toString());
    RuntimeCombatSession session=(RuntimeCombatSession)field(view,"combatSession");session.tick(.1f);assertEquals(hp,target.hp);session.tick(.1f);assertTrue(target.hp<hp);
    int after=target.hp;session.tick(1f);assertEquals("one hit per submitted attack",after,target.hp);
  }
  @Test public void movingOffLegalMeleeTileBeforeContactCancelsDamage(){
    RuntimeState s=new RuntimeState();RuntimeState.Monster target=s.monsters().get(0);s.player().x=target.x-32;s.player().y=target.y-16;
    RuntimeCombatSession session=new RuntimeCombatSession(s,(a,b)->true,(a,b)->false,a->true);
    assertTrue(session.submitPlayerBasicAttack(target.id).combat.accepted());int hp=target.hp;
    s.player().x=target.x;s.player().y=target.y-32;session.tick(.3f);assertEquals(hp,target.hp);
    assertFalse(CanonicalMeleeTileContract.reachable(0,0,64,0));assertTrue(CanonicalMeleeTileContract.reachable(0,0,32,16));
  }
  @Test public void acceptQuestClosesActualDialogue()throws Exception{
    GameView view=new GameView(context);RuntimeState s=(RuntimeState)field(view,"state");RuntimeState.Npc npc=s.npcs().get(0);
    s.player().x=npc.x-32;s.player().y=npc.y-16;InteractionController interaction=(InteractionController)field(view,"interaction");interaction.request(s,npc);assertTrue(interaction.dialogOpen());
    MotionEvent tap=MotionEvent.obtain(0,0,MotionEvent.ACTION_DOWN,550,425,0);view.onTouchEvent(tap);tap.recycle();
    assertEquals(F5mAdaptedPrologueQuest.State.ACTIVE,((F5mAdaptedPrologueQuest)field(view,"f5mQuest")).state());assertFalse(interaction.dialogOpen());
  }
  @Test public void inventoryNamesDoNotContainInternalEvidenceLabels(){
    for(RpgInventoryPresentation.ItemRow row:new RpgInventoryPresentation().inventoryRows(new RpgProgressionState()))assertFalse(row.name,row.name.contains("["));
  }
  @Test public void obstacleInteriorIsBlockedAndClearSpaceBelowItIsUsable(){
    RuntimeState s=new RuntimeState();WorldRuntimeAdapter world=new WorldRuntimeAdapter(s,960,540);
    for(android.graphics.RectF obstacle:s.obstacles())assertFalse(world.canPlayerOccupy(obstacle.centerX(),obstacle.centerY()));
    assertTrue(com.projectdark.mobile.world.F5mProductionQuickQuestRouteAudit.verify());
  }
  @Test public void attackControlEntireHitCircleFitsViewport()throws Exception{
    Field x=GameView.class.getDeclaredField("ATK_X"),radius=GameView.class.getDeclaredField("ATK_R");x.setAccessible(true);radius.setAccessible(true);assertTrue(x.getFloat(null)+radius.getFloat(null)+4<=960);
  }
}
