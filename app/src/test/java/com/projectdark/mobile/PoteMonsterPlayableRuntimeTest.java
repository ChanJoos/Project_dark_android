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
    assertTrue("monster starts a visible tile interpolation",monster.isMoving);
    state.tick(MonsterAIController.MONSTER_STEP_SECONDS_B*.5f);
    assertTrue("position moves smoothly between tiles",Math.hypot(monster.x-beforeX,monster.y-beforeY)>0f);
    assertTrue("monster is still interpolating halfway",monster.isMoving);
    state.tick(MonsterAIController.MONSTER_STEP_SECONDS_B);
    assertTrue("POTE_PURPLE completes one authored clear tile",MonsterTileCenterLocomotion.isAdjacentEndpoint(beforeX,beforeY,monster.x,monster.y));
    assertFalse("monster must approach before beginning its adjacent-tile attack",monster.attackPrimed);
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
  @Test public void everyCandidateMovesWithItsAppliedFacingAndAttacksOnlyInFourDiagonalDirections(){
    List<WorldMoveTargetController.TileCenter> tiles=PoteFieldDef.navigationTiles();
    for(String id:PoteForestMonsterShowcase.monsterIds()){
      RuntimeState movementState=new RuntimeState(RuntimeState.BootMode.POTE_01_PROTOTYPE,true);
      movementState.enterPoteField();
      RuntimeState.Monster moving=find(movementState,id);
      for(RuntimeState.Monster other:movementState.monsters())if(other!=moving)other.alive=false;
      WorldMoveTargetController.TileCenter goal=null;
      for(WorldMoveTargetController.TileCenter tile:tiles){
        float dx=tile.x-moving.x,dy=tile.y-moving.y,d=(float)Math.hypot(dx,dy);
        if(d<80f||d>=180f)continue;
        WorldMoveTargetController.Direction direction=MonsterTileCenterLocomotion.toward(dx,dy,WorldMoveTargetController.Direction.SE);
        float nextX=moving.x+direction.dx,nextY=moving.y+direction.dy;
        boolean nextIsTile=tiles.stream().anyMatch(t->Math.abs(t.x-nextX)<.1f&&Math.abs(t.y-nextY)<.1f);
        if(nextIsTile&&!movementState.blocked(nextX,nextY)&&Math.hypot(nextX-tile.x,nextY-tile.y)>=40f){goal=tile;break;}
      }
      assertNotNull("nearby pursuit goal for "+id,goal);
      movementState.player().x=goal.x;movementState.player().y=goal.y;
      float beforeX=moving.x,beforeY=moving.y;
      RuntimeCombatSession movementCombat=new RuntimeCombatSession(movementState,(a,t)->true,
          RuntimeCombatSession.startingCommonerLearnedActions(),a->true);
      MonsterAIController movementAi=new MonsterAIController(
          new MonsterAIController.SharedResolverAttackRouter(movementCombat.monsterAutoBridge()));
      movementAi.tick(movementState,MonsterAIController.MONSTER_STEP_SECONDS_B);
      movementState.tick(MonsterAIController.MONSTER_STEP_SECONDS_B*.5f);
      assertTrue("monster interpolation must be visible before reaching the next tile: "+id,moving.isMoving);
      assertEquals("walking artwork is active during interpolation", "walk",PoteForestMonsterShowcase.poseFor(moving));
      assertTrue("interpolated position changes continuously",Math.hypot(moving.x-beforeX,moving.y-beforeY)>0f);
      assertNotNull("walk art uses the current locomotion facing: "+id,
          PoteForestMonsterShowcase.assetPath(id,"walk",moving.visualFacing.presentation()));
      movementState.tick(MonsterAIController.MONSTER_STEP_SECONDS_B);
      assertFalse("movement completes at the target tile: "+id,moving.isMoving);
      WorldMoveTargetController.Direction actual=WorldMoveTargetController.Direction.between(beforeX,beforeY,moving.x,moving.y);
      assertNotNull("candidate must complete one diagonal tile step: "+id,actual);
      assertEquals("walk image facing must match applied movement: "+id,
          MonsterTileCenterLocomotion.facing(actual),moving.visualFacing.presentation());
      assertEquals("monster returns to idle between tile steps", "idle",PoteForestMonsterShowcase.poseFor(moving));
    }

    assertEquals("no cardinal/eight-way facings are part of this contract",4,CharacterRenderer.Direction.values().length);
    for(String id:PoteForestMonsterShowcase.monsterIds())for(CharacterRenderer.Direction facing:CharacterRenderer.Direction.values()){
      RuntimeState state=new RuntimeState(RuntimeState.BootMode.POTE_01_PROTOTYPE,true);
      state.enterPoteField();
      RuntimeState.Monster monster=find(state,id);
      for(RuntimeState.Monster other:state.monsters())if(other!=monster)other.alive=false;
      RuntimeCombatSession combat=new RuntimeCombatSession(state,(a,t)->true,
          RuntimeCombatSession.startingCommonerLearnedActions(),a->true);
      MonsterAIController ai=new MonsterAIController(
          new MonsterAIController.SharedResolverAttackRouter(combat.monsterAutoBridge()));
      WorldMoveTargetController.TileCenter attacker=null,target=null;
      for(WorldMoveTargetController.TileCenter from:tiles){
        for(WorldMoveTargetController.TileCenter to:tiles){
          if(PoteFieldDef.areAdjacentGroundTiles(from.x,from.y,to.x,to.y)
              &&facing==CanonicalMeleeTileContract.facing(from.x,from.y,to.x,to.y)){
            attacker=from;target=to;break;
          }
        }
        if(attacker!=null)break;
      }
      assertNotNull("legal adjacent player tile "+facing,attacker);
      monster.x=attacker.x;monster.y=attacker.y;
      state.player().x=target.x;state.player().y=target.y;
      int hp=state.player().hp;
      ai.tick(state,0f);
      assertTrue(id+" winds up in "+facing,monster.attackPrimed);
      assertEquals(id+" locks the attack facing",facing,monster.visualFacing.presentation());
      state.tick(.25f);ai.tick(state,0f);
      assertEquals(MonsterAIController.SubmissionOutcome.ACCEPTED,ai.lastAttackSubmission().outcome);
      assertTrue("shared resolver hit must expose an attack pose for "+id,monster.attackVisualRemaining>0f);
      assertEquals("the actual hit frame must choose the attack art", "attack", PoteForestMonsterShowcase.poseFor(monster));
      assertTrue(PoteForestMonsterShowcase.assetPath(id,"attack",facing).endsWith("attack_"+facing.name().toLowerCase()+".png"));
      combat.tick(.24f);
      assertTrue(id+" deals the hit-frame damage",state.player().hp<hp);
      assertTrue("attack facing remains locked only during attack animation",monster.visualFacing.attackLocked());
      state.tick(.37f);
      assertFalse("movement facing is released after the attack pose",monster.visualFacing.attackLocked());
    }
  }

  private static RuntimeState.Monster find(RuntimeState state,String id){
    for(RuntimeState.Monster monster:state.monsters())if(id.equals(monster.id))return monster;
    throw new AssertionError("missing candidate "+id);
  }
}
