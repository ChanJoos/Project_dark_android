package com.projectdark.mobile;

import static org.junit.Assert.*;
import android.content.Context;
import android.graphics.*;
import android.view.MotionEvent;
import com.projectdark.mobile.world.*;
import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.*;

@RunWith(RobolectricTestRunner.class) @Config(sdk=34,manifest=Config.NONE)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public class MagicRuntimeV137Test {
  Context context;
  static final String MARENO="SK_마법사_001", EARTH="SK_마법사_003", HEAL="SK_마법사_056";
  @Before public void setup(){context=RuntimeEnvironment.getApplication();context.getSharedPreferences("project_dark_f5m_v1",0).edit().clear().commit();context.getSharedPreferences("project_dark_skill_test_v1",0).edit().clear().commit();F5mSaveStore.install(context);}
  static <T>T field(Object object,String name)throws Exception{Field f=object.getClass().getDeclaredField(name);f.setAccessible(true);return (T)f.get(object);}
  static Object invoke(GameView view,String name,Class<?> type,Object value)throws Exception{Method m=GameView.class.getDeclaredMethod(name,type);m.setAccessible(true);return m.invoke(view,value);}
  static void tap(GameView view,float x,float y){MotionEvent e=MotionEvent.obtain(0,1,MotionEvent.ACTION_DOWN,x,y,0);view.onTouchEvent(e);e.recycle();}
  static void advance(GameView view,float seconds)throws Exception{while(seconds>.049f){invoke(view,"update",float.class,.05f);seconds-=.05f;}if(seconds>0)invoke(view,"update",float.class,seconds);}
  GameView mage(boolean staff)throws Exception{
    GameView view=new GameView(context);view.layout(0,0,960,540);RuntimeState s=field(view,"state");s.rpg().restoreProgression(11,0);assertTrue(s.rpg().restoreJobCode("MAGE"));s.rpg().restoreBaseResources(20000,20000);s.applyDerivedGrowth();s.player().hp=15000;s.player().mp=15000;
    SkillBook book=field(view,"skillBook");assertFalse(book.testAccess());for(String id:new String[]{MARENO,EARTH,HEAL})assertTrue(book.learn(id,50));assertTrue(book.assign(0,MARENO));assertTrue(book.assign(1,EARTH));assertTrue(book.assign(2,HEAL));
    if(staff){assertEquals(RpgProgressionState.AutoLootResult.LOOTED,s.rpg().autoLootResolvedItem("IT_WARDROBE_MW062",1));assertEquals(RpgProgressionState.EquipResult.EQUIPPED,s.rpg().equipToSlot("IT_WARDROBE_MW062","무기"));assertEquals("매직파나",ItemEffects.weaponName(s.rpg()));}
    return view;
  }
  RuntimeState.Monster distantTarget(GameView view)throws Exception{
    RuntimeState s=field(view,"state");WorldRuntimeAdapter world=field(view,"worldAdapter");RuntimeState.Monster target=s.monsters().get(0);target.hp=50000;
    for(RuntimeState.Monster m:s.monsters()){if(m!=target)m.alive=false;m.attackCooldown=100;s.skillEffects().put(m.id,"qa-freeze","ROOT",1,60);}
    boolean found=false;
    for(WorldMoveTargetController.TileCenter tile:world.navigationTiles()){
      float d=(float)Math.hypot(tile.x-target.x,tile.y-target.y);
      if(d<100||d>220||!world.canPlayerOccupy(tile.x,tile.y)||!world.hasLineOfSight(tile.x,tile.y,target.x,target.y))continue;
      s.player().x=tile.x;s.player().y=tile.y;world.snapCameraToPlayer();
      if((Boolean)invoke(view,"skillTargetVisible",String.class,target.id)){found=true;break;}
    }
    assertTrue("distant authored visible LOS caster tile",found);assertFalse(CanonicalMeleeTileContract.reachable(s.player().x,s.player().y,target.x,target.y));((CombatController)field(view,"combat")).selectTarget(target);return target;
  }
  @Test public void normalMageQuickslotCastsAtRangeWhileTargetIsBetweenTiles()throws Exception{
    GameView v=mage(false);RuntimeState s=field(v,"state");RuntimeCombatSession session=field(v,"combatSession");RuntimeState.Monster m=distantTarget(v);float x=s.player().x,y=s.player().y;int hp=m.hp,mp=s.player().mp;
    m.x+=.2f;m.y+=.1f;m.isMoving=true;m.moveTargetX=m.x+32;m.moveTargetY=m.y+16;
    RectF slot=(RectF)invoke(v,"slotRect",int.class,0);tap(v,slot.centerX(),slot.centerY());
    assertEquals(MARENO,field(v,"activeSkillVisualId"));assertTrue(session.playerActionActive());assertNull(((SkillApproachController)field(v,"skillApproach")).skillId);assertEquals(x,s.player().x,0);assertEquals(y,s.player().y,0);assertEquals(mp-7,s.player().mp);
    invoke(v,"tickSkillCombat",float.class,.99f);assertEquals(hp,m.hp);invoke(v,"tickSkillCombat",float.class,.02f);assertTrue(m.hp<hp);assertEquals(0,session.cooldownRemaining("player",MARENO),0);assertEquals(x,s.player().x,0);render(v,"mareno-range-bare");
  }
  @Test public void equippedPanaReleasesElementarySpellOnNextFrameWithoutExtraCooldown()throws Exception{
    GameView v=mage(true);RuntimeState s=field(v,"state");RuntimeState.Monster m=distantTarget(v);RuntimeCombatSession session=field(v,"combatSession");assertEquals(0,session.effectiveCastSeconds(MARENO),0);assertEquals(0,session.effectiveCastSeconds(EARTH),0);assertEquals(0,session.effectiveCastSeconds(HEAL),0);
    int hp=m.hp,mp=s.player().mp;RectF slot=(RectF)invoke(v,"slotRect",int.class,0);tap(v,slot.centerX(),slot.centerY());invoke(v,"tickSkillCombat",float.class,.001f);assertTrue(m.hp<hp);assertEquals(mp-session.effectiveMpCost(MARENO),s.player().mp);assertEquals(0,session.cooldownRemaining("player",MARENO),0);render(v,"mareno-range-pana");
  }
  @Test public void twoInstantSlotsRotateInAutoWithoutAnyMeleeApproachOrWaitSeconds()throws Exception{
    GameView v=mage(true);RuntimeState s=field(v,"state");RuntimeState.Monster m=distantTarget(v);SkillBook book=field(v,"skillBook");book.clearSlot(2);float x=s.player().x,y=s.player().y;int hp=m.hp;
    tap(v,((Number)field(v,"AUTO_X")).floatValue()+((Number)field(v,"hudRightOffset")).floatValue(),((Number)field(v,"AUTO_Y")).floatValue());advance(v,1.55f);assertTrue((Boolean)field(v,"autoAttackEnabled"));assertTrue(m.hp<hp);assertTrue(book.proficiency(MARENO)>51);assertTrue(book.proficiency(EARTH)>51);assertEquals(x,s.player().x,0);assertEquals(y,s.player().y,0);assertNull(((SkillApproachController)field(v,"skillApproach")).skillId);assertFalse(((WorldRuntimeAdapter)field(v,"worldAdapter")).presentationMoving());render(v,"auto-two-elemental-slots");
  }
  @Test public void kurotoHasNoTwoSecondReuseDelayButSharesThreePerSecondSpellCadence()throws Exception{
    GameView v=mage(false);RuntimeState s=field(v,"state");RuntimeCombatSession session=field(v,"combatSession");s.player().hp=1000;int mp=s.player().mp;
    assertTrue(session.submitPlayer("player",HEAL).accepted());session.tick(.001f);assertTrue(s.player().hp>1000);assertEquals(mp-6,s.player().mp);assertEquals(0,session.cooldownRemaining("player",HEAL),0);
    assertEquals(CombatResolver.RejectReason.ACTION_BUSY,session.submitPlayer("player",HEAL).rejectReason);assertEquals(mp-6,s.player().mp);session.tick(.333f);assertTrue(session.submitPlayer("player",HEAL).accepted());session.tick(.001f);assertEquals(mp-12,s.player().mp);
  }
  @Test public void offscreenAndBlockedLosSpellsDoNotChargeAndPhysicalTileRulesRemainExact()throws Exception{
    RuntimeState s=new RuntimeState();s.rpg().restoreProgression(11,0);assertTrue(s.rpg().restoreJobCode("MAGE"));s.player().mp=100;RuntimeState.Monster m=s.monsters().get(0);m.x=s.player().x+160.2f;m.y=s.player().y+80.1f;
    RuntimeCombatSession hidden=new RuntimeCombatSession(s,(a,t)->true,(a,id)->true,a->true);hidden.setSkillVisibility(id->false);assertEquals(CombatResolver.RejectReason.RANGE,hidden.submitPlayer(m.id,MARENO).rejectReason);assertEquals(100,s.player().mp);
    RuntimeCombatSession blocked=new RuntimeCombatSession(s,(a,t)->false,(a,id)->true,a->true);assertEquals(CombatResolver.RejectReason.LOS,blocked.submitPlayer(m.id,MARENO).rejectReason);assertEquals(100,s.player().mp);
    assertFalse(SkillActionContract.canStart(SkillActionContract.get("SK_전사_001"),s.player().x,s.player().y,m.x,m.y,true));
  }
  @Test public void staffManaBoundaryUnlearnedAndDeadActorFailWithoutCharge()throws Exception{
    GameView v=mage(true);RuntimeState s=field(v,"state");RuntimeState.Monster m=distantTarget(v);RuntimeCombatSession session=field(v,"combatSession");int cost=session.effectiveMpCost(EARTH);s.player().mp=cost;
    assertTrue(cost<SkillAbilityCatalog.get(EARTH).mpCost);RectF slot=(RectF)invoke(v,"slotRect",int.class,1);tap(v,slot.centerX(),slot.centerY());assertEquals(EARTH,field(v,"activeSkillVisualId"));assertEquals(0,s.player().mp);session.tick(.001f);
    RuntimeCombatSession unlearned=new RuntimeCombatSession(s,(a,t)->true,(a,id)->false,a->true);s.player().mp=100;assertEquals(CombatResolver.RejectReason.NOT_LEARNED,unlearned.submitPlayer(m.id,MARENO).rejectReason);assertEquals(100,s.player().mp);s.player().alive=false;assertEquals(CombatResolver.RejectReason.ACTOR_DEAD,session.submitPlayer(m.id,MARENO).rejectReason);assertEquals(100,s.player().mp);
  }
  @Test public void learnedSlotsStaffAndTimingSurviveRestart()throws Exception{
    GameView v=mage(true);SkillBook b=field(v,"skillBook");assertTrue((Boolean)invoke(v,"saveSkillSlots",new Class<?>[]{},new Object[]{}));v.pause();F5mSaveStore.install(context);GameView resumed=new GameView(context);RuntimeState restored=field(resumed,"state");SkillBook rb=field(resumed,"skillBook");RuntimeCombatSession session=field(resumed,"combatSession");assertFalse(rb.testAccess());assertEquals("MAGE",restored.rpg().currentJobCode());assertEquals(MARENO,rb.slot(0));assertEquals(EARTH,rb.slot(1));assertEquals("매직파나",ItemEffects.weaponName(restored.rpg()));assertEquals(0,session.effectiveCastSeconds(MARENO),0);assertEquals(0,session.cooldownRemaining("player",MARENO),0);
  }
  static Object invoke(GameView v,String name,Class<?>[] types,Object[] args)throws Exception{Method m=GameView.class.getDeclaredMethod(name,types);m.setAccessible(true);return m.invoke(v,args);}
  static void render(GameView v,String name)throws Exception{Bitmap b=Bitmap.createBitmap(960,540,Bitmap.Config.ARGB_8888);v.draw(new Canvas(b));File dir=new File("build/reports/magic-runtime-v137");dir.mkdirs();try(FileOutputStream out=new FileOutputStream(new File(dir,name+".png"))){assertTrue(b.compress(Bitmap.CompressFormat.PNG,100,out));}b.recycle();}
}
