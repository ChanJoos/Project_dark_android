package com.projectdark.mobile;
import static org.junit.Assert.*;
import java.util.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.*;
@RunWith(RobolectricTestRunner.class) @Config(sdk=34,manifest=Config.NONE) @GraphicsMode(GraphicsMode.Mode.NATIVE)
public class CombatFacingV262Test {
 final MartialHudAutoRegressionTest f=new MartialHudAutoRegressionTest();
 @Before public void setup(){f.setup();}
 @Test public void rangedMonsterTurnsAtAcceptedAttackEvenWhenCrowdKeepsOldWalkHeading()throws Exception{
  for(float[] direction:new float[][]{{64,32},{-64,32},{64,-32},{-64,-32}}){RuntimeState s=new RuntimeState();AbelCoastV139Test.restoreD04(s.rpg(),"WARRIOR");s.enterCampaignMap("MAP_ABEL_COAST_1B",false);assertTrue(s.monsters().size()>5);RuntimeState.Monster m=s.monsters().stream().filter(x->"ABEL_ARKPUS".equals(x.campaignRewardProfileId)).findFirst().get();s.player().x=m.x+direction[0];s.player().y=m.y+direction[1];CharacterRenderer.Direction expected=CanonicalActorFacing.quantize(direction[0],direction[1],null);m.visualFacing.setLocomotion(expected==CharacterRenderer.Direction.SE?CharacterRenderer.Direction.NW:CharacterRenderer.Direction.SE);
   RuntimeCombatSession combat=new RuntimeCombatSession(s,(a,t)->true,(a,id)->false,a->true);MonsterAIController.SharedResolverAttackRouter router=new MonsterAIController.SharedResolverAttackRouter(combat.monsterAutoBridge());assertEquals(MonsterAIController.SubmissionOutcome.ACCEPTED,router.submitAction(s,m,1,"MONSTER_WATER").outcome);assertEquals(expected,m.visualFacing.presentation());m.visualFacing.setLocomotion(CharacterRenderer.Direction.SW);assertEquals("accepted attack lock survives unrelated heading",expected,m.visualFacing.presentation());
  }
 }
 @Test public void basicContactFacesItsActualRecipientAfterItMovesAcrossThePlayer()throws Exception{
  GameView v=f.directed(CharacterRenderer.Direction.SE);RuntimeState s=f.field(v,"state");RuntimeState.Monster target=((CombatController)f.field(v,"combat")).target();target.hp=1000000;
  java.lang.reflect.Method attack=GameView.class.getDeclaredMethod("attack");attack.setAccessible(true);attack.invoke(v);assertEquals(CharacterRenderer.Direction.SE,((CanonicalActorFacing)f.field(v,"playerFacing")).presentation());
  target.x=s.player().x-32;target.y=s.player().y-16;f.advance(v,.19f);assertTrue("real hit follows moved selected recipient",target.hp<1000000);assertEquals("contact pose must face damaged recipient",CharacterRenderer.Direction.NW,((CanonicalActorFacing)f.field(v,"playerFacing")).presentation());
 }
 @Test public void denseTargetsCannotStealPlayerBasicAttackFacingDuringContact()throws Exception{
  for(CharacterRenderer.Direction direction:CharacterRenderer.Direction.values()){GameView v=f.directed(direction);RuntimeState s=f.field(v,"state");CombatController c=f.field(v,"combat");RuntimeState.Monster target=c.target();target.hp=1000000;List<RuntimeState.Monster> population=f.field(s,"monsters");for(int i=0;i<24;i++){RuntimeState.Monster other=new RuntimeState.Monster("qa_crowd_"+i,"crowd",s.player().x+(i%2==0?32:-32),s.player().y+(i%3==0?16:-16),1000000,"B");other.attackCooldown=10000;population.add(other);s.skillEffects().put(other.id,"qa","ROOT",1,10000);}
   java.lang.reflect.Method attack=GameView.class.getDeclaredMethod("attack");attack.setAccessible(true);attack.invoke(v);assertEquals(1L,((Long)f.field(v,"acceptedPlayerActions")).longValue());CanonicalActorFacing facing=f.field(v,"playerFacing");assertEquals(direction,facing.presentation());c.selectTarget(population.get(population.size()-1));f.advance(v,.19f);assertEquals(direction,facing.presentation());assertTrue("locked recipient received contact",target.hp<1000000);EquipmentHudV262Test.render(v,"crowd-"+direction,960,540);
  }
 }
}
