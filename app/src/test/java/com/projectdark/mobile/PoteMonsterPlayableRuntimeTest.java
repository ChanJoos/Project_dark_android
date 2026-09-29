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
  @Test public void monsterAttackCompletesBeforeItTurnsAndWalksTowardANewTarget(){
    List<WorldMoveTargetController.TileCenter> tiles=PoteFieldDef.navigationTiles();
    RuntimeState state=new RuntimeState(RuntimeState.BootMode.POTE_01_PROTOTYPE,true);
    state.enterPoteField();
    RuntimeState.Monster monster=find(state,"POTE_PURPLE");
    for(RuntimeState.Monster other:state.monsters())if(other!=monster)other.alive=false;
    RuntimeCombatSession combat=new RuntimeCombatSession(state,(a,t)->true,
        RuntimeCombatSession.startingCommonerLearnedActions(),a->true);
    MonsterAIController ai=new MonsterAIController(
        new MonsterAIController.SharedResolverAttackRouter(combat.monsterAutoBridge()));
    WorldMoveTargetController.TileCenter attacker=null,target=null;
    for(WorldMoveTargetController.TileCenter from:tiles){
      for(WorldMoveTargetController.TileCenter to:tiles){
        if(PoteFieldDef.areAdjacentGroundTiles(from.x,from.y,to.x,to.y)){attacker=from;target=to;break;}
      }
      if(attacker!=null)break;
    }
    assertNotNull(attacker);assertNotNull(target);
    monster.x=attacker.x;monster.y=attacker.y;state.player().x=target.x;state.player().y=target.y;
    ai.tick(state,0f);assertTrue(monster.attackPrimed);
    state.tick(.25f);ai.tick(state,0f);
    assertEquals(MonsterAIController.SubmissionOutcome.ACCEPTED,ai.lastAttackSubmission().outcome);
    assertTrue(monster.visualFacing.attackLocked());
    assertEquals(.5f,PoteForestMonsterShowcase.attackProgress(monster),.001f);

    WorldMoveTargetController.TileCenter goal=null;
    WorldMoveTargetController.Direction expected=null;
    for(WorldMoveTargetController.TileCenter candidate:tiles){
      float d=(float)Math.hypot(candidate.x-monster.x,candidate.y-monster.y);
      if(d<80f||d>=180f)continue;
      WorldMoveTargetController.Direction direction=MonsterTileCenterLocomotion.toward(
          candidate.x-monster.x,candidate.y-monster.y,WorldMoveTargetController.Direction.SE);
      float nx=monster.x+direction.dx,ny=monster.y+direction.dy;
      boolean nextIsTile=tiles.stream().anyMatch(t->Math.abs(t.x-nx)<.1f&&Math.abs(t.y-ny)<.1f);
      if(nextIsTile&&Math.hypot(nx-candidate.x,ny-candidate.y)>=40f){goal=candidate;expected=direction;break;}
    }
    assertNotNull("a clear new pursuit target must be available",goal);
    state.player().x=goal.x;state.player().y=goal.y;
    CharacterRenderer.Direction locked=monster.visualFacing.presentation();
    ai.tick(state,MonsterAIController.MONSTER_STEP_SECONDS_B+.01f);
    assertFalse("pursuit waits while the attack recovery is visible",monster.isMoving);
    assertEquals("attack-facing remains locked through recovery",locked,monster.visualFacing.presentation());

    state.tick(.37f);
    assertFalse(monster.visualFacing.attackLocked());
    ai.tick(state,MonsterAIController.MONSTER_STEP_SECONDS_B+.01f);
    assertTrue("monster starts moving after attack recovery",monster.isMoving);
    assertEquals("walk pose faces the newly applied step",
        MonsterTileCenterLocomotion.facing(expected),monster.visualFacing.presentation());
    assertEquals("walk",PoteForestMonsterShowcase.poseFor(monster));
  }

  @Test public void attackProgressIsContinuousAndPeaksOnceAtContact(){
    RuntimeState state=new RuntimeState(RuntimeState.BootMode.POTE_01_PROTOTYPE,true);
    RuntimeState.Monster monster=find(state,"POTE_PURPLE");
    monster.attackPrimed=true;monster.attackWindup=.24f;
    assertEquals(0f,PoteForestMonsterShowcase.attackProgress(monster),.001f);
    monster.attackWindup=.12f;
    assertEquals(.25f,PoteForestMonsterShowcase.attackProgress(monster),.001f);
    monster.attackWindup=0f;
    assertEquals(.5f,PoteForestMonsterShowcase.attackProgress(monster),.001f);
    monster.attackPrimed=false;monster.attackVisualRemaining=.36f;
    assertEquals(.5f,PoteForestMonsterShowcase.attackProgress(monster),.001f);
    monster.attackVisualRemaining=.18f;
    assertEquals(.75f,PoteForestMonsterShowcase.attackProgress(monster),.001f);
    monster.attackVisualRemaining=0f;
    assertEquals(1f,PoteForestMonsterShowcase.attackProgress(monster),.001f);
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
      CharacterRenderer.Direction committed=MonsterTileCenterLocomotion.facing(
          WorldMoveTargetController.Direction.between(moving.moveStartX,moving.moveStartY,
              moving.moveTargetX,moving.moveTargetY));
      assertEquals("render direction follows the committed movement vector: "+id,committed,
          PoteForestMonsterShowcase.presentationFacing(moving));
      moving.visualFacing.beginAttack(opposite(committed));
      assertEquals("a stale attack lock cannot turn a walking sprite: "+id,committed,
          PoteForestMonsterShowcase.presentationFacing(moving));
      assertEquals("walk pose wins during interpolation: "+id,"walk",PoteForestMonsterShowcase.poseFor(moving));
      moving.visualFacing.endAttack();
      assertNotNull("walk art uses the current locomotion facing: "+id,
          PoteForestMonsterShowcase.assetPath(id,"walk",PoteForestMonsterShowcase.presentationFacing(moving)));
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

  @Test public void monstersReserveDestinationsAndDoNotOverlapDuringConcurrentSteps(){
    List<WorldMoveTargetController.TileCenter> tiles=PoteFieldDef.navigationTiles();
    RuntimeState state=new RuntimeState(RuntimeState.BootMode.POTE_01_PROTOTYPE,true);
    state.enterPoteField();
    RuntimeState.Monster first=find(state,"POTE_PURPLE");
    RuntimeState.Monster second=find(state,"POTE_RED");
    for(RuntimeState.Monster other:state.monsters())if(other!=first&&other!=second)other.alive=false;
    WorldMoveTargetController.TileCenter a=null,b=null,c=null,playerTile=null;
    outer:for(WorldMoveTargetController.TileCenter center:tiles){
      for(WorldMoveTargetController.TileCenter shared:tiles){
        if(!PoteFieldDef.areAdjacentGroundTiles(center.x,center.y,shared.x,shared.y))continue;
        for(WorldMoveTargetController.TileCenter other:tiles){
          if((Math.abs(other.x-center.x)>.1f||Math.abs(other.y-center.y)>.1f)
              &&PoteFieldDef.areAdjacentGroundTiles(other.x,other.y,shared.x,shared.y)){
            a=center;b=shared;c=other;break outer;
          }
        }
      }
    }
    assertNotNull("find two approach tiles sharing one destination",a);
    for(WorldMoveTargetController.TileCenter tile:tiles){
      if(Math.hypot(tile.x-a.x,tile.y-a.y)>220f&&Math.hypot(tile.x-c.x,tile.y-c.y)>220f){playerTile=tile;break;}
    }
    assertNotNull("keep player outside the collision fixture",playerTile);
    first.x=a.x;first.y=a.y;second.x=c.x;second.y=c.y;
    state.player().x=playerTile.x;state.player().y=playerTile.y;
    assertTrue(state.tryMoveMonster(first,b.x-a.x,b.y-a.y,MonsterTileCenterLocomotion.STEP_DISTANCE));
    assertTrue("first monster reserves the shared destination",first.isMoving);
    boolean secondStarted=state.tryMoveMonster(second,b.x-c.x,b.y-c.y,MonsterTileCenterLocomotion.STEP_DISTANCE);
    if(secondStarted){
      float required=RuntimeState.MONSTER_RADIUS*2f+5f;
      assertTrue("second monster cannot reserve the same destination",
          Math.hypot(second.moveTargetX-first.moveTargetX,second.moveTargetY-first.moveTargetY)>=required);
      state.tick(MonsterAIController.MONSTER_STEP_SECONDS_B*.5f);
      assertTrue("simultaneous paths retain collision clearance",
          Math.hypot(second.x-first.x,second.y-first.y)>=required);
    }
  }

  @Test public void monsterCannotMoveOntoPlayerTile(){
    List<WorldMoveTargetController.TileCenter> tiles=PoteFieldDef.navigationTiles();
    RuntimeState state=new RuntimeState(RuntimeState.BootMode.POTE_01_PROTOTYPE,true);
    state.enterPoteField();
    RuntimeState.Monster monster=find(state,"POTE_PURPLE");
    WorldMoveTargetController.TileCenter from=null,to=null;
    outer:for(WorldMoveTargetController.TileCenter a:tiles){
      for(WorldMoveTargetController.TileCenter b:tiles){
        if(PoteFieldDef.areAdjacentGroundTiles(a.x,a.y,b.x,b.y)){from=a;to=b;break outer;}
      }
    }
    assertNotNull(from);assertNotNull(to);
    monster.x=from.x;monster.y=from.y;state.player().x=to.x;state.player().y=to.y;
    state.tryMoveMonster(monster,to.x-from.x,to.y-from.y,MonsterTileCenterLocomotion.STEP_DISTANCE);
    if(monster.isMoving){
      assertTrue("monster route keeps the player collision radius",
          Math.hypot(monster.moveTargetX-state.player().x,monster.moveTargetY-state.player().y)
              >=RuntimeState.MONSTER_RADIUS+RuntimeState.PLAYER_RADIUS+5f);
    }
  }

  private static CharacterRenderer.Direction opposite(CharacterRenderer.Direction direction){
    switch(direction){
      case NW:return CharacterRenderer.Direction.SE;
      case NE:return CharacterRenderer.Direction.SW;
      case SW:return CharacterRenderer.Direction.NE;
      default:return CharacterRenderer.Direction.NW;
    }
  }

  private static RuntimeState.Monster find(RuntimeState state,String id){
    for(RuntimeState.Monster monster:state.monsters())if(id.equals(monster.id))return monster;
    throw new AssertionError("missing candidate "+id);
  }
}
