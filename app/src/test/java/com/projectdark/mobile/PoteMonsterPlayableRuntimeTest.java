package com.projectdark.mobile;

import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

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
    monster.x=PotePrototypeWorldDef.MONSTER_X_B;monster.y=PotePrototypeWorldDef.MONSTER_Y_B;
    state.player().x=768f;state.player().y=480f;
    float beforeX=monster.x,beforeY=monster.y;
    ai.tick(state,MonsterAIController.MONSTER_STEP_SECONDS_B+0.01f);
    assertTrue("POTE_PURPLE must advance by exactly one 4-way isometric tile; before="+beforeX+","+beforeY+" after="+monster.x+","+monster.y+" state="+monster.state,
        MonsterTileCenterLocomotion.isAdjacentEndpoint(beforeX,beforeY,monster.x,monster.y));
    assertTrue(MonsterTileCenterLocomotion.isAuthoredCenter(monster.x,monster.y));
    assertEquals(MonsterAIController.AttackRoute.SHARED_RESOLVER,ai.attackRoute());

    // Put both actors on legal adjacent centers and drive the actual monster attack windup,
    // shared resolver submission and hit frame; no direct RuntimeState damage shortcut.
    state.player().x=832f;state.player().y=480f;monster.x=800f;monster.y=464f;
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
