package com.projectdark.mobile;
import static org.junit.Assert.*;
import android.graphics.*;
import com.projectdark.mobile.world.*;
import java.lang.reflect.*;
import java.io.*;
import java.util.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.*;
@RunWith(RobolectricTestRunner.class) @Config(sdk=34,manifest=Config.NONE) @GraphicsMode(GraphicsMode.Mode.NATIVE)
public class AutoOrderTargetV256Test {
 final MartialHudAutoRegressionTest f=new MartialHudAutoRegressionTest();
 @Before public void setup(){f.setup();}
 GameView fixture()throws Exception{
  GameView v=f.directed(CharacterRenderer.Direction.SE);RuntimeState s=f.field(v,"state");
  RuntimeState.Monster m=s.monsters().get(0);m.hp=1000000000;
  for(RuntimeState.Monster x:s.monsters()){x.attackCooldown=10000;s.skillEffects().put(x.id,"qa","ROOT",1,10000);}
  SkillBook b=f.field(v,"skillBook");for(int i=0;i<12;i++)b.clearSlot(i);return v;
 }
 long accepted(GameView v)throws Exception{return ((Long)f.field(v,"acceptedPlayerActions")).longValue();}
 void start(GameView v)throws Exception{Method m=GameView.class.getDeclaredMethod("tickAutoAttack",WorldMoveTargetController.Snapshot.class);m.setAccessible(true);m.invoke(v,new Object[]{null});}
 void finish(GameView v)throws Exception{
  f.set(v,"autoAttackEnabled",false);String id=f.field(v,"activeSkillVisualId");RuntimeCombatSession session=f.field(v,"combatSession");float seconds=id==null?2:Math.max(2,session.effectiveCastSeconds(id)+1.5f);
  f.advance(v,seconds);assertFalse("accepted action completed",session.playerActionActive());f.set(v,"autoAttackEnabled",true);
 }
 void slots(GameView v,String... ids)throws Exception{SkillBook b=f.field(v,"skillBook");for(int i=0;i<ids.length;i++)if(ids[i]!=null)assertTrue(b.assign(i,ids[i]));}
 RuntimeState.Monster extra(GameView v,String id,float dx,float dy)throws Exception{
  RuntimeState s=f.field(v,"state");RuntimeState.Monster m=new RuntimeState.Monster(id,id,s.player().x+dx,s.player().y+dy,1000000,"B");m.attackCooldown=10000;((List<RuntimeState.Monster>)f.field(s,"monsters")).add(m);s.skillEffects().put(id,"qa","ROOT",1,10000);return m;
 }
 RuntimeState.Monster preferred(GameView v)throws Exception{Method m=GameView.class.getDeclaredMethod("bestReachableAutoTarget");m.setAccessible(true);return(RuntimeState.Monster)m.invoke(v);}
 @Test public void allTwelveAlwaysReadySkillsRunInRegisteredOrderThenWrapAndAutoEnableRestartsAtOne()throws Exception{
  GameView v=fixture();String[] ids={"SK_마법사_001","SK_마법사_003","SK_마법사_004","SK_마법사_005","SK_마법사_013","SK_마법사_014","SK_마법사_015","SK_마법사_016","SK_마법사_026","SK_마법사_027","SK_마법사_028","SK_마법사_029"};slots(v,ids);
  List<String> trace=new ArrayList<>();HudTestControlsV267.auto(v);for(int i=0;i<13;i++){long before=accepted(v);start(v);assertEquals("one accepted action per turn",before+1,accepted(v));assertEquals("registered slot "+(i%12+1),ids[i%12],f.field(v,"activeSkillVisualId"));trace.add((String)f.field(v,"activeSkillVisualId"));assertEquals((i+1)%12,((Integer)f.field(v,"autoSkillCursor")).intValue());finish(v);}
  File report=new File("build/reports/horror-v179/v256/auto_trace.json");report.getParentFile().mkdirs();try(FileWriter out=new FileWriter(report)){out.write(new org.json.JSONArray(trace).toString());}
  HudTestControlsV267.auto(v);HudTestControlsV267.auto(v);assertEquals(0,((Integer)f.field(v,"autoSkillCursor")).intValue());start(v);assertEquals(ids[0],f.field(v,"activeSkillVisualId"));
 }
 @Test public void afterOneThenFourFiveWinsEvenThoughOneHasAlreadyRecovered()throws Exception{
  GameView v=fixture();slots(v,"SK_마법사_001","SK_마법사_013","SK_마법사_014","SK_마법사_003","SK_마법사_004");
  RuntimeCombatSession session=f.field(v,"combatSession");RuntimeCombatPortAdapter port=f.field(session,"port");port.commitCooldown("player","SK_마법사_013",100);port.commitCooldown("player","SK_마법사_014",100);port.tick(1);
  HudTestControlsV267.auto(v);start(v);assertEquals("SK_마법사_001",f.field(v,"activeSkillVisualId"));finish(v);start(v);assertEquals("SK_마법사_003",f.field(v,"activeSkillVisualId"));assertEquals(4,((Integer)f.field(v,"autoSkillCursor")).intValue());finish(v);assertEquals(0,session.cooldownRemaining("player","SK_마법사_001"),0);start(v);assertEquals("slot5 before recovered slot1","SK_마법사_004",f.field(v,"activeSkillVisualId"));
 }
 @Test public void registeredBasicAttackParticipatesBeforeTheNextSkill()throws Exception{
  GameView v=fixture();slots(v,"SK_공통_001","SK_마법사_001");HudTestControlsV267.auto(v);start(v);assertEquals(1,accepted(v));assertEquals(1,((Integer)f.field(v,"autoSkillCursor")).intValue());finish(v);start(v);assertEquals("SK_마법사_001",f.field(v,"activeSkillVisualId"));assertEquals(2,accepted(v));
 }
 @Test public void acceptedNonVisualLeapAdvancesExactlyOneSlotWithoutNeedingAnAnimation()throws Exception{
  GameView v=fixture();RuntimeState s=f.field(v,"state");RuntimeCombatSession session=f.field(v,"combatSession");
  session.setSkillMovement(new SkillAbilityExecutor.Movement(){
   public boolean movePlayer(float x,float y){s.player().x=x;s.player().y=y;return true;}
   public boolean canLeapPlayer(float x,float y){return true;}
   public boolean leapPlayer(float x,float y){return movePlayer(x,y);}
   public boolean moveMonster(RuntimeState.Monster m,float x,float y){m.x=x;m.y=y;return true;}
  });
  slots(v,"SK_무도가_003","SK_마법사_001");HudTestControlsV267.auto(v);start(v);assertEquals(1,accepted(v));assertNull(f.field(v,"activeSkillVisualId"));assertEquals(1,((Integer)f.field(v,"autoSkillCursor")).intValue());finish(v);start(v);assertEquals("SK_마법사_001",f.field(v,"activeSkillVisualId"));
 }
 @Test public void enablingAndIncomingHitRetainTheAttackedMonsterThenFourAdjacentBeatsCloserDiagonal()throws Exception{
  GameView v=fixture();RuntimeState s=f.field(v,"state");CombatController combat=f.field(v,"combat");RuntimeState.Monster current=combat.target();current.x=s.player().x+64;current.y=s.player().y+32;RuntimeState.Monster adjacent=extra(v,"qa_incoming",-32,-16),diagonal=extra(v,"qa_diagonal",0,32);
  slots(v,"SK_마법사_001");assertSame(current,preferred(v));HudTestControlsV267.auto(v);assertSame(current,combat.target());start(v);
  RuntimeCombatSession session=f.field(v,"combatSession");int hp=s.player().hp;adjacent.attackCooldown=0;assertEquals(MonsterAutoCombatBridge.Outcome.ACCEPTED,session.monsterAutoBridge().submit(adjacent.id,"player").outcome);f.advance(v,.7f);assertTrue("real incoming damage",s.player().hp<hp);assertSame("incoming hit cannot steal target",current,combat.target());
  current.alive=false;combat.tick(0);assertSame("cardinal neighbor precedes nearer diagonal",adjacent,preferred(v));combat.selectTarget(diagonal);assertSame("explicit selection takes priority",diagonal,preferred(v));
  ((List<RuntimeState.Monster>)f.field(s,"monsters")).remove(diagonal);assertSame("removed target is not retained",adjacent,preferred(v));
 }
 @Test public void autoFacingAttacksSelectedMonsterInsteadOfNewcomerOnTheOtherSideWhileManualRayKeepsFacingRules()throws Exception{
  GameView v=fixture();RuntimeState s=f.field(v,"state");RuntimeState.Monster current=((CombatController)f.field(v,"combat")).target(),near=extra(v,"qa_wrong_side",-32,-16);((CanonicalActorFacing)f.field(v,"playerFacing")).setLocomotion(CharacterRenderer.Direction.NW);slots(v,"SK_도적_003");int currentHp=current.hp,nearHp=near.hp;HudTestControlsV267.auto(v);start(v);assertEquals("SK_도적_003",f.field(v,"activeSkillVisualId"));assertEquals(CharacterRenderer.Direction.SE,((CanonicalActorFacing)f.field(v,"playerFacing")).presentation());finish(v);assertTrue(current.hp<currentHp);assertEquals(nearHp,near.hp);
  GameView manual=fixture();RuntimeState ms=f.field(manual,"state");RuntimeState.Monster opposite=extra(manual,"qa_manual_side",-32,-16);((CanonicalActorFacing)f.field(manual,"playerFacing")).setLocomotion(CharacterRenderer.Direction.NW);SkillBook b=f.field(manual,"skillBook");f.call(manual,"useBookSkill",SkillBook.Entry.class,b.get("SK_도적_003"));int before=opposite.hp;f.set(manual,"autoAttackEnabled",false);f.advance(manual,.7f);assertTrue("manual ray still follows chosen facing",opposite.hp<before);
 }
}
