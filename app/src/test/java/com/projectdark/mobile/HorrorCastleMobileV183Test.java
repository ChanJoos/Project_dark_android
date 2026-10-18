package com.projectdark.mobile;
import static org.junit.Assert.*;
import android.content.Context;
import com.projectdark.mobile.world.*;
import java.lang.reflect.*;
import java.util.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.*;
@RunWith(RobolectricTestRunner.class) @Config(sdk=34,manifest=Config.NONE) @GraphicsMode(GraphicsMode.Mode.NATIVE)
public class HorrorCastleMobileV183Test {
 Context c;
 @Before public void reset(){c=RuntimeEnvironment.getApplication();for(String n:new String[]{"project_dark_f5m_v1","project_dark_skill_book_v1","project_dark_skill_test_v1"})c.getSharedPreferences(n,0).edit().clear().commit();F5mSaveStore.install(c);UiTheme.install(c);}
 GameView view()throws Exception{GameView v=new GameView(c);v.layout(0,0,960,540);Field f=GameView.class.getDeclaredField("inPoteField");f.setAccessible(true);f.setBoolean(v,true);return v;}
 void call(GameView v,String name,Class<?>[] types,Object... args)throws Exception{Method m=GameView.class.getDeclaredMethod(name,types);m.setAccessible(true);m.invoke(v,args);}
 void enter(GameView v,int room)throws Exception{call(v,"changeCampaignMap",new Class<?>[]{String.class,boolean.class},HorrorCastleGeometry.roomId(room),false);}
 @Test public void lastKillAutomaticallyTraversesAllRoomsAndResetsFacing()throws Exception{
  GameView v=view();RuntimeState s=ItemWindowReferenceTest.field(v,"state");enter(v,1);
  for(int n=1;n<=13;n++){
   assertTrue(s.npcs().isEmpty());for(int i=0;i<14;i++)s.damage(s.monsters().get(i),Integer.MAX_VALUE);
   call(v,"updatePoteField",new Class<?>[]{float.class},0f);assertEquals(HorrorCastleGeometry.roomId(n),s.currentMapId());
   CanonicalActorFacing facing=ItemWindowReferenceTest.field(v,"playerFacing");facing.beginAttack(CharacterRenderer.Direction.NE);
   s.damage(s.monsters().get(14),Integer.MAX_VALUE);call(v,"updatePoteField",new Class<?>[]{float.class},0f);
   assertEquals(n==13?HorrorCastleGeometry.LOBBY:HorrorCastleGeometry.roomId(n+1),s.currentMapId());
   assertFalse(facing.attackLocked());assertEquals(CharacterRenderer.Direction.SW,facing.presentation());assertNull(ItemWindowReferenceTest.field(v,"activeSkillVisualId"));
   assertEquals(n==13?0:15,s.monsters().size());
  }
  assertEquals(8191,s.horror().clearedMask());
 }
 @Test public void everyRoomHasNoNpcAndPortalsReturnToMillesWithLivingMonsters()throws Exception{
  for(int n=1;n<=13;n++){
   GameView v=view();RuntimeState s=ItemWindowReferenceTest.field(v,"state");enter(v,n);assertTrue(s.npcs().isEmpty());
   for(HorrorCastleGeometry.Door d:HorrorCastleGeometry.doors(s.currentMapId())){
    assertEquals("MILLES",d.destination);assertTrue(HorrorCastleGeometry.inside(s.currentMapId(),d.x,d.y,0));
   }
   HorrorCastleGeometry.Door exit=HorrorCastleGeometry.doors(s.currentMapId()).get(0);s.player().x=exit.x;s.player().y=exit.y;
   call(v,"checkCampaignGate",new Class<?>[]{});assertEquals(WorldDef.ID,s.currentMapId());
  }
 }
 @Test public void multipleRecipientsKeepTheAcceptedSkillDirection()throws Exception{
  SkillVfxRenderer fx=new SkillVfxRenderer(c,new SkillPresentationCatalog(c));float[] target={168,184};
  SkillVfxRenderer.Anchors anchors=new SkillVfxRenderer.Anchors(){public float x(String id){return id.equals("player")?200:id.equals("primary")?target[0]:232;}public float y(String id){return id.equals("player")?200:id.equals("primary")?target[1]:216;}};
  CombatResolver.Definition d=new CombatResolver.Definition("SK_성직자_003",CombatResolver.ActionKind.MAGIC,CombatResolver.ActionState.MAGIC,CombatResolver.EffectType.MAGIC_HIT,true,8,1,150,.24f,12);
  fx.consume(Collections.singletonList(new CombatResolver.Event(1,81,CombatResolver.EventType.ACTION_STARTED,"player","primary",d,CombatResolver.InputMode.MANUAL,null,null,0)),anchors);
  target[0]=232;target[1]=216;
  for(String id:new String[]{"primary","secondary"})fx.consume(Collections.singletonList(new CombatResolver.Event(2,81,CombatResolver.EventType.HIT_FEEDBACK,"player",id,d,CombatResolver.InputMode.MANUAL,null,CombatResolver.HitSemantic.DAMAGE,12)),anchors);
  int checked=0;for(SkillVfxRenderer.Pulse p:fx.pulses)if(!p.sheet.equals("impact")){assertEquals(-32,p.directionX,.01);assertEquals(-16,p.directionY,.01);checked++;}assertTrue(checked>=2);
  fx.clear();assertTrue(fx.pulses.isEmpty());
 }
}
