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
 // The two V262 correction-policy tests were withdrawn with the user-requested V263 rollback.
 @Test public void denseTargetsCannotStealPlayerBasicAttackFacingDuringContact()throws Exception{
  for(CharacterRenderer.Direction direction:CharacterRenderer.Direction.values()){GameView v=f.directed(direction);RuntimeState s=f.field(v,"state");CombatController c=f.field(v,"combat");RuntimeState.Monster target=c.target();target.hp=1000000;List<RuntimeState.Monster> population=f.field(s,"monsters");for(int i=0;i<24;i++){RuntimeState.Monster other=new RuntimeState.Monster("qa_crowd_"+i,"crowd",s.player().x+(i%2==0?32:-32),s.player().y+(i%3==0?16:-16),1000000,"B");other.attackCooldown=10000;population.add(other);s.skillEffects().put(other.id,"qa","ROOT",1,10000);}
   java.lang.reflect.Method attack=GameView.class.getDeclaredMethod("attack");attack.setAccessible(true);attack.invoke(v);assertEquals(1L,((Long)f.field(v,"acceptedPlayerActions")).longValue());CanonicalActorFacing facing=f.field(v,"playerFacing");assertEquals(direction,facing.presentation());c.selectTarget(population.get(population.size()-1));f.advance(v,.19f);assertEquals(direction,facing.presentation());assertTrue("locked recipient received contact",target.hp<1000000);EquipmentHudV262Test.render(v,"crowd-"+direction,960,540);
  }
 }
}
