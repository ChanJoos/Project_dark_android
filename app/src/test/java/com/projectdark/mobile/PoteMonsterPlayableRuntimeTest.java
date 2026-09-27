package com.projectdark.mobile;

import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import com.projectdark.mobile.world.PoteFieldDef;
import com.projectdark.mobile.world.WorldMoveTargetController;
import java.util.List;

/** Exercises the same AI and shared combat route wired into GameView for the Pote actor. */
@RunWith(RobolectricTestRunner.class)
@Config(sdk=34,manifest=Config.NONE)
public final class PoteMonsterPlayableRuntimeTest {
  @Test public void poteMonsterMovesAndAttacksThroughSharedCombatSession(){
    RuntimeState state=new RuntimeState(RuntimeState.BootMode.POTE_01_PROTOTYPE,true);
    state.enterPoteField();
    RuntimeState.Monster monster=state.monsters().get(0);
    MonsterDefinition definition=new MonsterDefinitionRegistry().resolve("POTE_PURPLE");
    assertEquals(MonsterDefinition.Status.PROTOTYPE_PENDING,definition.status);
    assertEquals(MonsterDefinition.Evidence.B,definition.evidence);
    assertFalse("prototype placement must not invent canonical rewards",definition.hasCanonicalReward());

    RuntimeCombatSession combat=new RuntimeCombatSession(state,(a,t)->true,
        RuntimeCombatSession.startingCommonerLearnedActions(),a->true);
    MonsterAIController ai=new MonsterAIController(
        new MonsterAIController.SharedResolverAttackRouter(combat.monsterAutoBridge()));
    List<WorldMoveTargetController.TileCenter> tiles=PoteFieldDef.navigationTiles();
    WorldMoveTargetController.TileCenter start=tiles.stream().filter(t->Math.abs(t.x-PotePrototypeWorldDef.MONSTER_X_B)<.1f
        &&Math.abs(t.y-PotePrototypeWorldDef.MONSTER_Y_B)<.1f).findFirst().orElse(null);
    assertNotNull("prototype monster spawn must land on a clear authored tile",start);
    WorldMoveTargetController.TileCenter target=null;
    for(WorldMoveTargetController.TileCenter goal:tiles){
      float dx=goal.x-start.x,dy=goal.y-start.y,d=(float)Math.hypot(dx,dy);
      if(d<80f||d>=180f)continue; // established Pote prototype chase radius [B]
      WorldMoveTargetController.Direction direction=MonsterTileCenterLocomotion.toward(dx,dy,WorldMoveTargetController.Direction.SE);
      float nx=start.x+direction.dx,ny=start.y+direction.dy;
      if(Math.hypot(nx-goal.x,ny-goal.y)<40f)continue;
      if(tiles.stream().anyMatch(t->Math.abs(t.x-nx)<.1f&&Math.abs(t.y-ny)<.1f)){target=goal;break;}
    }
    assertNotNull("authored spawn pocket must have a walkable one-tile pursuit step",target);
    assertNotNull(target);
    monster.x=start.x;monster.y=start.y;state.player().x=target.x;state.player().y=target.y;
    float beforeX=monster.x,beforeY=monster.y;
    ai.tick(state,MonsterAIController.MONSTER_STEP_SECONDS_B+0.01f);
    assertTrue("POTE_PURPLE moves on an authored clear tile",MonsterTileCenterLocomotion.isAdjacentEndpoint(beforeX,beforeY,monster.x,monster.y));
    assertTrue(state.isMonsterTileCenter(monster.x,monster.y));
    assertEquals(MonsterAIController.AttackRoute.SHARED_RESOLVER,ai.attackRoute());

    // Put both actors on legal adjacent centers and drive the actual monster attack windup,
    // shared resolver submission and hit frame; no direct RuntimeState damage shortcut.
    WorldMoveTargetController.TileCenter attackMonster=null,attackPlayer=null;
    for(WorldMoveTargetController.TileCenter candidate:tiles){
      for(WorldMoveTargetController.TileCenter goal:tiles){
        if(PoteFieldDef.areAdjacentGroundTiles(candidate.x,candidate.y,goal.x,goal.y)){
          attackMonster=candidate;attackPlayer=goal;break;
        }
      }
      if(attackMonster!=null)break;
    }
    assertNotNull(attackMonster);assertNotNull(attackPlayer);
    monster.x=attackMonster.x;monster.y=attackMonster.y;state.player().x=attackPlayer.x;state.player().y=attackPlayer.y;
    int hp=state.player().hp;
    ai.tick(state,0f);
    assertTrue(monster.attackPrimed);
    state.tick(0.25f);
    ai.tick(state,0f);
    assertEquals(MonsterAIController.SubmissionOutcome.ACCEPTED,ai.lastAttackSubmission().outcome);
    combat.tick(0.24f);
    assertTrue("shared combat resolver applies the B damage on hit frame",state.player().hp<hp);
  }
}
