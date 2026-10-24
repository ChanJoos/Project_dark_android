package com.projectdark.mobile;

import com.projectdark.mobile.world.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.*;
import static org.junit.Assert.*;

/** Scoped user regression: actual doorway arrival, safe population, intentional exit/re-entry. */
@RunWith(RobolectricTestRunner.class)
@Config(sdk=34,manifest=Config.NONE)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public class MineEntryArrivalTest {
 private MineTownV203Test f;
 @Before public void setup(){f=new MineTownV203Test();f.reset();}
 @Test public void bothArrivalDirectionsUseWalkableAdjacentDoorwayTileAndPreservePopulation(){
  for(boolean fromNext:new boolean[]{false,true}){
   RuntimeState s=new RuntimeState();s.enterCampaignMap(MineHuntGeometry.ID,fromNext);
   assertEquals(12,MineHuntGeometry.sourceU(s.player().x,s.player().y));
   assertEquals(35,MineHuntGeometry.sourceV(s.player().x,s.player().y));
   assertTrue(MineHuntGeometry.inside(s.player().x,s.player().y));
   assertEquals(1,Math.abs(MineHuntGeometry.sourceU(s.player().x,s.player().y)-MineHuntGeometry.sourceU(MineHuntGeometry.exit().x,MineHuntGeometry.exit().y))+Math.abs(MineHuntGeometry.sourceV(s.player().x,s.player().y)-MineHuntGeometry.sourceV(MineHuntGeometry.exit().x,MineHuntGeometry.exit().y)));
   assertEquals(100,s.monsters().size());int[] species=new int[10];
   for(RuntimeState.Monster m:s.monsters()){
    species[MineHuntMonsters.type(m.id)]++;
    assertTrue(MineHuntGeometry.inside(m.x,m.y));
    assertTrue("initial/respawn doorway exclusion",MineHuntGeometry.allowsMonsterSpawn(m.spawnX,m.spawnY));
    assertTrue("outside initial aggro radius",Math.hypot(m.x-s.player().x,m.y-s.player().y)>180);
    for(RuntimeState.Monster other:s.monsters())if(m!=other)assertTrue(Math.hypot(m.x-other.x,m.y-other.y)>=90);
   }
   for(int count:species)assertEquals(10,count);
  }
 }
 @Test public void standingAtArrivalDoesNotStartCombatAndRespawnKeepsSafeHomes(){
  RuntimeState s=new RuntimeState();s.enterCampaignMap(MineHuntGeometry.ID,false);
  MonsterAIController ai=new MonsterAIController();int hp=s.player().hp;
  for(int i=0;i<200;i++){s.tick(.05f);ai.tick(s,.05f);}
  assertEquals(hp,s.player().hp);
  for(RuntimeState.Monster m:s.monsters()){
   assertEquals(m.spawnX,m.x,0);assertEquals(m.spawnY,m.y,0);
   s.damage(m,m.maxHp);
  }
  s.tick(30f);
  for(RuntimeState.Monster m:s.monsters()){
   assertTrue(m.alive);assertTrue(MineHuntGeometry.allowsMonsterSpawn(m.x,m.y));
  }
 }
 @Test public void actualGateAllowsImmediateDeliberateExitAndReentryWithoutBounce()throws Exception{
  GameView v=f.town();RuntimeState s=f.field(v,"state");
  for(int trip=0;trip<3;trip++){
   f.call(v,"changeCampaignMap",new Class<?>[]{String.class,boolean.class},MineHuntGeometry.ID,false);
   assertFalse("arrival must not auto-exit",(Boolean)f.call(v,"checkCampaignGate",new Class<?>[]{}));
   WorldRuntimeAdapter w=f.field(v,"poteFieldAdapter");
   WorldMoveTargetController.TileCenter exit=MineHuntGeometry.exit();
   assertTrue("one-step deliberate exit is reachable",CampaignNavigationTest.walk(w,w.requestGroundWorld(exit.x,exit.y)));
   assertTrue((Boolean)f.call(v,"checkCampaignGate",new Class<?>[]{}));
   assertEquals(MineTownGeometry.ID,s.currentMapId());
  }
 }
}
