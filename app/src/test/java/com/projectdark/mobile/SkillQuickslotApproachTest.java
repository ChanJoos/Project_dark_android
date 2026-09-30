package com.projectdark.mobile;

import static org.junit.Assert.*;
import android.content.Context;
import android.view.MotionEvent;
import com.projectdark.mobile.world.*;
import java.lang.reflect.*;
import java.util.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.*;

@RunWith(RobolectricTestRunner.class) @Config(sdk=34,manifest=Config.NONE)
public class SkillQuickslotApproachTest {
  Context c;
  @Before public void setup(){c=RuntimeEnvironment.getApplication();c.getSharedPreferences("project_dark_f5m_v1",0).edit().clear().commit();c.getSharedPreferences("project_dark_skill_test_v1",0).edit().clear().commit();F5mSaveStore.install(c);}
  @Test public void oneTapWalksToLegalTileAndChargesExactlyOnce()throws Exception{
    GameView view=new GameView(c);view.layout(0,0,960,540);view.setSkillTestMode(true);
    SkillBook book=field(view,"skillBook");RuntimeState s=field(view,"state");WorldRuntimeAdapter world=field(view,"worldAdapter");
    RuntimeState.Monster target=s.monsters().get(0);((CombatController)field(view,"combat")).selectTarget(target);
    // An authored, reachable caster tile at least two steps from the monster.
    SkillActionContract.Rule rule=SkillActionContract.get("SK_전사_001");
    WorldMoveTargetController.TileCenter start=null;
    for(WorldMoveTargetController.TileCenter tile:world.navigationTiles())if(world.canPlayerOccupy(tile.x,tile.y)&&!SkillApproachController.legal(rule,tile.x,tile.y,target.x,target.y)){
      s.player().x=tile.x;s.player().y=tile.y;List<WorldMoveTargetController.TileCenter> path=SkillApproachController.path(world,rule,target,960,540);
      if(path!=null&&path.size()>=2&&path.size()<10){start=tile;break;}
    }assertNotNull(start);s.player().x=start.x;s.player().y=start.y;world.snapCameraToPlayer();book.assign(0,rule.id);
    tap(view,671,395);SkillApproachController pending=field(view,"skillApproach");assertEquals(rule.id,pending.skillId);
    RuntimeCombatSession session=field(view,"combatSession");assertFalse(session.playerActionActive());assertEquals(0,session.cooldownRemaining("player",rule.id),.001f);
    for(int n=0;n<200&&pending.skillId!=null;n++){world.tickNavigation(.1f);tickPending(view,.1f);}
    assertNull(pending.skillId);assertEquals(rule.id,field(view,"activeSkillVisualId"));assertTrue(session.playerActionActive());
    assertTrue(SkillActionContract.canStart(rule,s.player().x,s.player().y,target.x,target.y,true));
    List<CombatResolver.Event> events=session.tick(.2f).events;assertEquals(1,events.stream().filter(e->e.type==CombatResolver.EventType.HIT_FEEDBACK&&e.actionId.equals(rule.id)).count());
    tickPending(view,.1f);assertTrue(session.tick(.2f).events.isEmpty());
  }
  @Test public void shurikenRetreatsRatherThanCastingAdjacentAndDirectInputCancels()throws Exception{
    GameView view=new GameView(c);view.layout(0,0,960,540);view.setSkillTestMode(true);RuntimeState s=field(view,"state");WorldRuntimeAdapter world=field(view,"worldAdapter");
    RuntimeState.Monster target=s.monsters().get(0);s.player().x=target.x-32;s.player().y=target.y-16;world.snapCameraToPlayer();((CombatController)field(view,"combat")).selectTarget(target);
    SkillBook book=field(view,"skillBook");use(view,book.get("SK_도적_003"));SkillApproachController pending=field(view,"skillApproach");assertEquals("SK_도적_003",pending.skillId);
    assertFalse(((RuntimeCombatSession)field(view,"combatSession")).playerActionActive());
    for(int n=0;n<100&&pending.skillId!=null;n++){world.tickNavigation(.1f);tickPending(view,.1f);}
    assertNull(pending.skillId);assertEquals("SK_도적_003",field(view,"activeSkillVisualId"));assertTrue(SkillActionContract.canStart(SkillActionContract.get("SK_도적_003"),s.player().x,s.player().y,target.x,target.y,true));
    GameView other=new GameView(c);other.layout(0,0,960,540);other.setSkillTestMode(true);SkillApproachController intent=field(other,"skillApproach");intent.begin("SK_전사_001",target.id);tap(other,92,454);assertNull(intent.skillId);
  }
  @Test public void shortestRouteRejectsBlockedGoalAndRetainsFourDirections(){
    List<WorldMoveTargetController.TileCenter> tiles=new ArrayList<>();for(int u=-4;u<=4;u++)for(int v=-4;v<=4;v++)tiles.add(new WorldMoveTargetController.TileCenter((u-v)*32,(u+v)*16));
    class Fixture implements WorldMoveTargetController.NavigationWorld,WorldMoveTargetController.Walker{float x,y;public List<WorldMoveTargetController.TileCenter> navigationTiles(){return tiles;}public boolean canPlayerOccupy(float x,float y){return !(x==32&&y==16);}public boolean canPlayerTraverse(float ax,float ay,float x,float y){return canPlayerOccupy(x,y);}public float worldX(){return x;}public float worldY(){return y;}public boolean moveToAdjacentTile(float x,float y,WorldMoveTargetController.Direction d){this.x=x;this.y=y;return true;}}
    Fixture f=new Fixture();WorldMoveTargetController nav=new WorldMoveTargetController(f,f);
    List<WorldMoveTargetController.TileCenter> path=nav.skillApproachPath(t->t.x==64&&t.y==32);assertNotNull(path);assertTrue(path.size()>2);assertNull(nav.skillApproachPath(t->t.x==9999));
    nav.requestSkillApproach("target",path);for(int n=0;n<path.size();n++){WorldMoveTargetController.Snapshot step=nav.tick(.6f);assertNotNull(step.lastStepDirection);}assertEquals(64,f.x,.01f);assertEquals(32,f.y,.01f);
  }
  @Test public void deadTargetAndPauseCancelWithoutCharging()throws Exception{
    GameView view=new GameView(c);view.layout(0,0,960,540);view.setSkillTestMode(true);
    RuntimeState s=field(view,"state");SkillApproachController intent=field(view,"skillApproach");
    RuntimeState.Monster target=s.monsters().get(0);intent.begin("SK_전사_001",target.id);target.alive=false;
    tickPending(view,.1f);assertNull(intent.skillId);
    RuntimeCombatSession session=field(view,"combatSession");assertFalse(session.playerActionActive());assertEquals(0,session.cooldownRemaining("player","SK_전사_001"),.001f);
    intent.begin("SK_전사_001",target.id);view.pause();assertNull(intent.skillId);
  }
  @Test public void movingTargetReplansItsReservedTileBeforeCasting()throws Exception{
    GameView view=new GameView(c);view.layout(0,0,960,540);view.setSkillTestMode(true);
    RuntimeState s=field(view,"state");WorldRuntimeAdapter world=field(view,"worldAdapter");RuntimeState.Monster target=s.monsters().get(0);
    SkillApproachController intent=field(view,"skillApproach");intent.begin("SK_전사_001",target.id);
    target.isMoving=true;target.moveTargetX=target.x+32;target.moveTargetY=target.y+16;
    tickPending(view,.1f);assertNotNull(intent.skillId);assertEquals(target.moveTargetX,intent.plannedX,.01f);assertEquals(target.moveTargetY,intent.plannedY,.01f);
    assertFalse(((RuntimeCombatSession)field(view,"combatSession")).playerActionActive());
    target.x=target.moveTargetX;target.y=target.moveTargetY;target.isMoving=false;
    for(int n=0;n<300&&intent.skillId!=null;n++){world.tickNavigation(.1f);tickPending(view,.1f);}
    assertNull(intent.skillId);assertEquals("SK_전사_001",field(view,"activeSkillVisualId"));
  }
  @Test public void productionFrameLoopApproachesAndCastsInBothMaps()throws Exception{
    for(boolean forest:new boolean[]{false,true}){
      GameView view=new GameView(c);view.layout(0,0,960,540);view.setSkillTestMode(true);
      if(forest){Method enter=GameView.class.getDeclaredMethod("enterPoteField");enter.setAccessible(true);enter.invoke(view);}
      RuntimeState state=field(view,"state");WorldRuntimeAdapter world=field(view,forest?"poteFieldAdapter":"worldAdapter");
      RuntimeState.Monster target=state.monsters().get(0);SkillActionContract.Rule rule=SkillActionContract.get("SK_전사_001");
      boolean placed=false;for(WorldMoveTargetController.TileCenter tile:world.navigationTiles()){
        if(!world.canPlayerOccupy(tile.x,tile.y)||SkillApproachController.legal(rule,tile.x,tile.y,target.x,target.y))continue;
        state.player().x=tile.x;state.player().y=tile.y;
        List<WorldMoveTargetController.TileCenter> route=SkillApproachController.path(world,rule,target,960,540);
        if(route!=null&&route.size()>=2&&route.size()<5){placed=true;break;}
      }
      assertTrue("reachable scene "+forest,placed);world.snapCameraToPlayer();((CombatController)field(view,"combat")).selectTarget(target);
      SkillBook book=field(view,"skillBook");book.assign(0,rule.id);tap(view,671,395);
      SkillApproachController pending=field(view,"skillApproach");assertNotNull(pending.skillId);
      Method update=GameView.class.getDeclaredMethod("update",float.class);update.setAccessible(true);
      for(int frame=0;frame<600&&pending.skillId!=null;frame++)update.invoke(view,.05f);
      assertNull(pending.skillId);assertEquals("actual frame loop cast "+forest,rule.id,field(view,"activeSkillVisualId"));
      assertTrue(((RuntimeCombatSession)field(view,"combatSession")).playerActionActive());
    }
  }
  private static void tickPending(GameView v,float dt)throws Exception{Method m=GameView.class.getDeclaredMethod("tickSkillApproach",float.class);m.setAccessible(true);m.invoke(v,dt);}
  private static void use(GameView v,SkillBook.Entry e)throws Exception{Method m=GameView.class.getDeclaredMethod("useBookSkill",SkillBook.Entry.class);m.setAccessible(true);m.invoke(v,e);}
  private static void tap(GameView v,float x,float y){MotionEvent e=MotionEvent.obtain(0,0,MotionEvent.ACTION_DOWN,x,y,0);v.onTouchEvent(e);e.recycle();}
  @SuppressWarnings("unchecked") private static <T>T field(Object o,String n)throws Exception{Field f=o.getClass().getDeclaredField(n);f.setAccessible(true);return(T)f.get(o);}
}
