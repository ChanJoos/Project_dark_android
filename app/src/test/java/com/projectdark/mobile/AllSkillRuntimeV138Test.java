package com.projectdark.mobile;
import static org.junit.Assert.*;
import java.util.*;
import android.graphics.RectF;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.*;
@RunWith(RobolectricTestRunner.class) @Config(sdk=34,manifest=Config.NONE)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public class AllSkillRuntimeV138Test extends MagicRuntimeV137Test {
 @Test public void all221RuntimeRoutesAreExercisedWithoutTestModeOrImaginaryResources(){
  int count=0,spells=0;
  for(SkillAbilityCatalog.Ability a:SkillAbilityCatalog.all()){
   count++;SkillActionContract.Rule r=SkillActionContract.get(a.id);assertNotNull(a.id,r);
   RuntimeState s=new RuntimeState();s.rpg().restoreBaseResources(200000,200000);s.applyDerivedGrowth();s.player().hp=1000;s.player().mp=100000;
   RuntimeState.Monster m=s.monsters().get(0);m.x=s.player().x+32*Math.max(1,r.minReach);m.y=s.player().y+16*Math.max(1,r.minReach);m.hp=1;
   s.skillEffects().put("player","qa-hidden","STEALTH",1,60);
   RuntimeCombatSession session=new RuntimeCombatSession(s,(x,y)->true,(x,id)->true,x->true);
   boolean spell=ItemEffects.magic(a.id);if(spell){spells++;assertTrue(a.id,MagicTimingPolicy.managed(a.id));}
   if(a.id.equals("SK_공통_001")){assertTrue(session.submitPlayerBasicAttack(m.id).combat.accepted());session.tick(.3f);continue;}
   if(!a.supported()){int mp=s.player().mp;assertFalse(a.id,session.useUtility(a.id));assertEquals(a.id,mp,s.player().mp);continue;}
   if(a.status.equals("TRANSFER")){assertFalse(session.submitPlayer("player",a.id).accepted());continue;}
   if(!r.presentationAllowed()){
    if(r.mode.equals("LINKED")||r.pattern==SkillActionContract.Pattern.PASSIVE){assertFalse(a.id,session.submitPlayer(m.id,a.id).accepted());continue;}
    assertTrue(a.id,session.useUtility(a.id));continue;
   }
   String target=r.selfAnchored()?"player":m.id;int mp=s.player().mp;
   CombatActionOrchestrator.Submission use=session.submitPlayer(target,a.id);assertTrue(a.id+" "+use.rejectReason,use.accepted());
   assertEquals(a.id,mp-session.effectiveMpCost(a.id),s.player().mp);
   RuntimeCombatSession.FrameResult frame=session.tick(session.effectiveCastSeconds(a.id)+.001f);
   assertTrue(a.id+" contact",frame.events.stream().anyMatch(e->e.type==CombatResolver.EventType.HIT_FEEDBACK&&e.actionId.equals(a.id)));
  }
  assertEquals(221,count);assertEquals(138,spells);
 }
 @Test public void allSpellDefinitionsSeparateSharedCadenceFromExplicitReuse(){
  RuntimeCombatSession s=new RuntimeCombatSession(new RuntimeState(),(a,b)->true,(a,id)->true,a->true);
  for(SkillAbilityCatalog.Ability a:SkillAbilityCatalog.all())if(ItemEffects.magic(a.id)){
   float expected=a.name.equals("이모탈")||a.name.equals("리플렉토")?5:a.name.equals("나르콜리")?1:a.name.equals("다라밀공")?12:0;
   assertEquals(a.id,expected,a.cooldown,0);
   if(a.supported()&&SkillActionContract.get(a.id).presentationAllowed())assertEquals(a.id,expected,s.actionDefinitions().get(a.id).cooldown,0);
  }
 }
 @Test public void upperCircleTargetCrossCastsAtMovingRangeAndAutoNeverFallsIntoMelee()throws Exception{
  GameView v=mage(true);RuntimeState s=field(v,"state");SkillBook book=field(v,"skillBook");String ex="SK_마법사_018";assertTrue(book.learn(ex,50));book.clearSlot(0);book.clearSlot(1);book.clearSlot(2);assertTrue(book.assign(0,ex));RuntimeState.Monster m=distantTarget(v);m.x+=.2f;m.y+=.1f;m.isMoving=true;
  float x=s.player().x,y=s.player().y;int hp=m.hp;RectF slot=(RectF)invoke(v,"slotRect",int.class,0);tap(v,slot.centerX(),slot.centerY());invoke(v,"tickSkillCombat",float.class,.001f);assertTrue(m.hp<hp);assertEquals(0,((RuntimeCombatSession)field(v,"combatSession")).cooldownRemaining("player",ex),0);
  advance(v,.4f);tap(v,926,507);advance(v,1.4f);assertEquals(x,s.player().x,0);assertEquals(y,s.player().y,0);
 }
 @Test public void upperSpellAndHealShareCadenceAndPanaPoseUsesEffectiveClock()throws Exception{
  GameView v=mage(true);RuntimeState s=field(v,"state");SkillBook b=field(v,"skillBook");String upper="SK_마법사_013";assertTrue(b.learn(upper,50));RuntimeState.Monster m=distantTarget(v);RuntimeCombatSession c=field(v,"combatSession");assertTrue(c.submitPlayer(m.id,upper).accepted());c.tick(.001f);assertEquals(CombatResolver.RejectReason.ACTION_BUSY,c.submitPlayer("player",HEAL).rejectReason);c.tick(.334f);assertTrue(c.submitPlayer("player",HEAL).accepted());
  s.rpg().autoLootResolvedItem("IT_WARDROBE_MW062",1);assertEquals(0,c.effectiveCastSeconds(upper),0);
 }
 @Test public void utilityRejectsInsufficientMpDeadPlayerAndControlWithoutCharging(){
  RuntimeState s=new RuntimeState();RuntimeCombatSession c=new RuntimeCombatSession(s,(a,b)->true,(a,id)->true,a->true);String id="SK_도적_012";s.player().mp=0;assertFalse(c.useUtility(id));assertEquals(0,s.player().mp);s.player().mp=100;s.player().alive=false;assertFalse(c.useUtility(id));assertEquals(100,s.player().mp);s.player().alive=true;s.skillEffects().put("player","qa-sleep","SLEEP",1,30);assertFalse(c.useUtility(id));assertEquals(100,s.player().mp);
 }
}
