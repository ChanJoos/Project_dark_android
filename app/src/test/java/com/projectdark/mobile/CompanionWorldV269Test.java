package com.projectdark.mobile;

import com.projectdark.mobile.world.*;
import java.util.*;

public class CompanionWorldV269Test {
  @org.junit.Test
  @SuppressWarnings("unchecked")
  public void rescueRetargetsLiveMonsterWithoutLeavingPrimedWindupStuck() throws Exception {
    RuntimeState s = new RuntimeState();
    s.rpg().restoreProgression(71, 0);
    if (!s.rpg().restoreJobCode("MAGE")) throw new AssertionError();
    s.applyDerivedGrowth();
    RuntimeState.Monster old = s.monsters().get(0);
    RuntimeState.Monster enemy =
        new RuntimeState.Monster(old.id, old.name, old.x, old.y, 5000, old.assetStatus);
    java.lang.reflect.Field field = RuntimeState.class.getDeclaredField("monsters");
    field.setAccessible(true);
    List<RuntimeState.Monster> monsters = (List<RuntimeState.Monster>) field.get(s);
    monsters.clear();
    monsters.add(enemy);
    WorldRuntimeAdapter world = new WorldRuntimeAdapter(s, 960, 540);
    List<WorldMoveTargetController.TileCenter> neighbors = new ArrayList<>();
    for (WorldMoveTargetController.TileCenter t : world.navigationTiles())
      if (CanonicalMeleeTileContract.reachable(t.x, t.y, enemy.x, enemy.y)
          && world.canPartyStandAt(t.x, t.y)) neighbors.add(t);
    if (neighbors.size() < 1) throw new AssertionError("no legal adjacent tiles");
    s.player().x = neighbors.get(0).x;
    s.player().y = neighbors.get(0).y;
    if (!s.party().recruit(s, CompanionParty.Job.WARRIOR)) throw new AssertionError("recruit");
    CompanionParty.Member ally = s.party().active();
    ally.map = s.currentMapId();
    ally.body.x = s.player().x;
    ally.body.y = s.player().y;
    s.party().order(CompanionParty.Order.FOCUS, enemy.id);
    RuntimeCombatSession combat =
        new RuntimeCombatSession(s, world::hasCombatLineOfSight, (a, id) -> true, a -> true);
    CompanionController controller = new CompanionController(s, combat);
    MonsterAIController ai =
        new MonsterAIController(
            new MonsterAIController.SharedResolverAttackRouter(combat.monsterAutoBridge()));
    CompanionController.Navigation nav =
        new CompanionController.Navigation() {
          public Object identity() {
            return world;
          }

          public List<WorldMoveTargetController.TileCenter> tiles() {
            return world.navigationTiles();
          }

          public boolean stand(float x, float y) {
            return world.canPartyStandAt(x, y);
          }

          public boolean traverse(float ax, float ay, float bx, float by) {
            return world.canPlayerTraverse(ax, ay, bx, by);
          }

          public boolean sight(float ax, float ay, float bx, float by) {
            return world.hasLineOfSight(ax, ay, bx, by);
          }
        };
    System.out.println(
        "FIXTURE "
            + enemy.id
            + " at="
            + enemy.x
            + ","
            + enemy.y
            + " ally="
            + ally.body.x
            + ","
            + ally.body.y
            + " los="
            + world.hasCombatLineOfSight(enemy.id, ally.id));
    int hp = ally.body.hp, playerHp = s.player().hp;
    int starts = 0;
    for (int i = 0; i < 200; i++) {
      controller.tick(.05f, nav, enemy, true);
      RuntimeCombatSession.FrameResult result = combat.tick(.05f);
      for (CombatResolver.Event e : result.events)
        if (e.actorId.equals(ally.id) && e.type == CombatResolver.EventType.ACTION_STARTED)
          starts++;
      s.tick(.05f);
      ai.tick(s, .05f);
    }
    System.out.println(
        "AI "
            + ai.lastAttackSubmission().outcome
            + " "
            + ai.lastAttackSubmission().rejectReason
            + " state="
            + enemy.state
            + " prime="
            + enemy.attackPrimed
            + " wind="
            + enemy.attackWindup
            + " coords="
            + enemy.x
            + ","
            + enemy.y
            + " ally="
            + ally.body.x
            + ","
            + ally.body.y);
    if (enemy.hp >= 5000 || ally.body.hp >= hp || s.player().hp != playerHp)
      throw new AssertionError(
          "world combat enemy="
              + enemy.hp
              + " ally="
              + ally.body.hp
              + "/"
              + hp
              + " player="
              + s.player().hp
              + "/"
              + playerHp
              + " action="
              + ally.actionId
              + " notice="
              + ally.notice
              + " target="
              + enemy.attackTarget);
    System.out.println(
        "REAL_WORLD_PARTY_SMOKE PASS actions="
            + starts
            + " enemyHp="
            + enemy.hp
            + " companionHp="
            + ally.body.hp
            + " playerHpUnchanged="
            + playerHp);
  }
}
