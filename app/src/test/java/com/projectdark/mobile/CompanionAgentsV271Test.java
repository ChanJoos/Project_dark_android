package com.projectdark.mobile;

import static org.junit.Assert.*;

import java.util.*;
import org.json.*;
import org.junit.Test;

/** Real controller -> profession strategy -> shared Resolver, not mocked strategy answers. */
public class CompanionAgentsV271Test {
  final CompanionPartyV269Test setup = new CompanionPartyV269Test();
  final CompanionControllerV269Test paths = new CompanionControllerV269Test();

  static class Fixture {
    RuntimeState s;
    CompanionParty.Member m;
    RuntimeState.Monster enemy;
    RuntimeCombatSession combat;
    CompanionController controller;
    CompanionController.Navigation nav;

    void decide() {
      controller.tick(.3f, nav, enemy, true);
    }

    void finish() {
      combat.tick(3.2f);
    }
  }

  Fixture fixture(CompanionParty.Job job) {
    Fixture f = new Fixture();
    f.s = setup.state(3, "MAGE");
    f.s.party().enableTesting(f.s);
    assertTrue(f.s.party().recruit(f.s, job));
    f.m = f.s.party().active();
    f.m.map = f.s.currentMapId();
    f.m.body.x = f.s.player().x;
    f.m.body.y = f.s.player().y;
    f.enemy = f.s.monsters().get(0);
    f.enemy.x = f.m.body.x + 32;
    f.enemy.y = f.m.body.y + 16;
    f.enemy.hp = 200000;
    f.enemy.alive = true;
    f.enemy.provoked = true;
    f.enemy.attackTarget = "player";
    for (RuntimeState.Monster x : f.s.monsters())
      if (x != f.enemy) {
        x.alive = false;
        x.hp = 0;
      }
    f.combat = setup.session(f.s);
    f.controller = new CompanionController(f.s, f.combat);
    f.nav = paths.nav(f.s);
    return f;
  }

  @Test
  public void fiveFullyLearnedProfessionsHaveDistinctAgentsAndCorrectStats() {
    Set<Class<?>> types = new HashSet<>();
    for (CompanionParty.Job job : CompanionParty.Job.values()) {
      Fixture f = fixture(job);
      types.add(f.controller.agent(job).getClass());
      assertEquals(job.code, f.m.growth.currentJobCode());
      assertEquals(99, f.m.level);
      for (SkillAbilityCatalog.Ability a : SkillAbilityCatalog.all())
        assertEquals(
            a.id, a.job.equals(job.label) && !SkillPolicyV227.retired(a.id), f.m.learned(a.id));
      if (job == CompanionParty.Job.MONK) assertEquals(f.m.stats().str, f.m.stats().con);
      if (job == CompanionParty.Job.ROGUE) assertTrue(f.m.stats().dex > f.m.stats().str);
      if (job == CompanionParty.Job.MAGE) assertTrue(f.m.stats().intel > f.m.stats().str);
    }
    assertEquals(5, types.size());
  }

  @Test
  public void rogueActuallyHidesThenAmbushesWithoutResettingPlayersOtherEnemy() {
    Fixture f = fixture(CompanionParty.Job.ROGUE);
    RuntimeState.Monster other = f.s.monsters().get(1);
    other.alive = true;
    other.hp = other.maxHp;
    other.provoked = true;
    other.x = f.enemy.x + 128;
    other.y = f.enemy.y + 64;
    other.attackTarget = "player";
    f.decide();
    assertEquals("SK_도적_011", f.m.actionId);
    f.finish();
    assertTrue(f.s.skillEffects().has(f.m.id, "STEALTH"));
    assertTrue(other.provoked);
    int hp = f.enemy.hp, own = f.s.player().hp;
    f.decide();
    assertEquals("SK_도적_023", f.m.actionId);
    f.finish();
    assertTrue(f.enemy.hp < hp);
    assertFalse(f.s.skillEffects().has(f.m.id, "STEALTH"));
    assertEquals(own, f.s.player().hp);
  }

  @Test
  public void shurikenUsesCompanionRayAndCannotPierceFirstMonster() {
    Fixture f = fixture(CompanionParty.Job.ROGUE);
    f.enemy.x = f.m.body.x + 96;
    f.enemy.y = f.m.body.y + 48;
    f.s.player().x -= 64;
    f.s.player().y += 32; // player's ray differs
    f.decide();
    assertEquals("SK_도적_003", f.m.actionId);
    int hp = f.enemy.hp;
    f.finish();
    assertTrue(f.enemy.hp < hp);
    Fixture g = fixture(CompanionParty.Job.ROGUE);
    g.enemy.x = g.m.body.x + 96;
    g.enemy.y = g.m.body.y + 48;
    RuntimeState.Monster blocker = g.s.monsters().get(1);
    blocker.alive = true;
    blocker.hp = blocker.maxHp;
    blocker.x = g.m.body.x + 32;
    blocker.y = g.m.body.y + 16;
    g.m.facing = CanonicalActorFacing.quantize(32, 16, g.m.facing);
    assertFalse(g.combat.submitCompanion(g.enemy.id, "SK_도적_003").accepted());
  }

  @Test
  public void endangeredMageFreezesOwnAttackerBeforeSmallSelfHeal() {
    Fixture f = fixture(CompanionParty.Job.MAGE);
    f.m.body.hp = f.m.body.maxHp / 4;
    f.enemy.attackTarget = f.m.id;
    f.decide();
    assertEquals("SK_마법사_025", f.m.actionId);
    f.finish();
    assertTrue(f.s.skillEffects().has(f.enemy.id, "FREEZE"));
  }

  @Test
  public void mageSelectsAdvantageousElementAndPaysItsOwnMp() {
    Fixture f = fixture(CompanionParty.Job.MAGE);
    f.enemy.hp = 2000;
    f.enemy.defenseElement = "FIRE";
    int mp = f.m.body.mp, playerMp = f.s.player().mp;
    f.decide();
    SkillAbilityCatalog.Ability a = SkillAbilityCatalog.get(f.m.actionId);
    assertNotNull(a);
    assertTrue(a.damage());
    assertEquals("WATER", RuntimeCombatPortAdapter.skillElement(a));
    f.finish();
    assertTrue(f.enemy.hp < 2000);
    assertEquals(mp - a.mpCost, f.m.body.mp);
    assertEquals(playerMp, f.s.player().mp);
  }

  @Test
  public void mageControlsAdditionalEnemyWithoutSleepingDamageTarget() {
    Fixture f = fixture(CompanionParty.Job.MAGE);
    RuntimeState.Monster add = f.s.monsters().get(1);
    add.alive = true;
    add.hp = add.maxHp;
    add.provoked = true;
    add.x = f.m.body.x - 32;
    add.y = f.m.body.y - 16;
    f.decide();
    assertEquals("SK_마법사_017", f.m.actionId);
    f.finish();
    assertTrue(f.s.skillEffects().has(add.id, "SLEEP"));
    assertFalse(f.s.skillEffects().has(f.enemy.id, "SLEEP"));
    f.enemy.hp = 2000;
    f.decide();
    assertTrue(SkillAbilityCatalog.get(f.m.actionId).damage());
    f.finish();
    assertTrue(f.s.skillEffects().has(add.id, "SLEEP"));
  }

  @Test
  public void mageAreaChoiceDoesNotWakeSleepingOrIdleBystanders() {
    Fixture f = fixture(CompanionParty.Job.MAGE);
    f.enemy.hp = 2000;
    RuntimeState.Monster add = f.s.monsters().get(1);
    add.alive = true;
    add.hp = add.maxHp;
    add.provoked = false;
    add.x = f.m.body.x - 32;
    add.y = f.m.body.y - 16;
    int hp = add.hp;
    f.decide();
    assertEquals(SkillActionContract.Pattern.SINGLE, SkillActionContract.get(f.m.actionId).pattern);
    f.finish();
    assertEquals(hp, add.hp);
  }

  @Test
  public void mageUsesAreaForSeveralControlledTargetsAndNoBlindAllMpDump() {
    Fixture f = fixture(CompanionParty.Job.MAGE);
    f.enemy.hp = 2000;
    RuntimeState.Monster add = f.s.monsters().get(1);
    add.alive = true;
    add.hp = 2000;
    add.provoked = true;
    add.x = f.m.body.x - 32;
    add.y = f.m.body.y - 16;
    f.s.skillEffects().put(add.id, "SK_마법사_025", "FREEZE", 1, 8);
    f.decide();
    SkillAbilityCatalog.Ability a = SkillAbilityCatalog.get(f.m.actionId);
    assertTrue(a.damage());
    assertFalse(a.allMp);
    assertNotEquals(SkillActionContract.Pattern.SINGLE, SkillActionContract.get(a.id).pattern);
    f.finish();
    assertTrue(add.hp < 2000);
  }

  @Test
  public void monkDispelsPoisonAndHealsSelfWithoutHealingPlayerOrChangingItsMp() {
    Fixture f = fixture(CompanionParty.Job.MONK);
    f.m.body.hp = f.m.body.maxHp / 3;
    f.s.skillEffects().put(f.m.id, "SK_마법사_012", "POISON", 10, 8);
    int player = f.s.player().hp, mp = f.s.player().mp;
    f.decide();
    assertEquals("SK_무도가_031", f.m.actionId);
    f.finish();
    assertFalse(f.s.skillEffects().has(f.m.id, "POISON"));
    int hp = f.m.body.hp;
    f.decide();
    assertTrue(SkillAbilityCatalog.get(f.m.actionId).heal());
    f.finish();
    assertTrue(f.m.body.hp > hp);
    assertEquals(player, f.s.player().hp);
    assertEquals(mp, f.s.player().mp);
    f.combat.tick(1);
    assertFalse(f.combat.submitCompanion("player", "SK_무도가_017").accepted());
  }

  @Test
  public void monkUsesDefenseBeforeResourceBurstAndReallyPaysDaraResources() {
    Fixture f = fixture(CompanionParty.Job.MONK);
    f.s.skillEffects().put(f.m.id, "SK_무도가_013", "ARMOR", 10, 12);
    f.s.skillEffects().put(f.m.id, "SK_무도가_029", "PROTECT", 30, 12);
    f.decide();
    assertEquals("SK_무도가_016", f.m.actionId);
    f.finish();
    assertTrue(f.s.skillEffects().has(f.m.id, "INVINCIBLE"));
    f.decide();
    assertEquals("SK_무도가_020", f.m.actionId);
    int hp = f.s.player().hp;
    f.finish();
    assertEquals(1, f.m.body.hp);
    assertEquals(1, f.m.body.mp);
    assertEquals(hp, f.s.player().hp);
  }

  @Test
  public void allAgentsRespectWaitMenusDeadActorsAndSleepingReservation() {
    for (CompanionParty.Job job : CompanionParty.Job.values()) {
      Fixture f = fixture(job);
      f.s.party().order(CompanionParty.Order.WAIT, null);
      float x = f.m.body.x, y = f.m.body.y;
      f.controller.tick(.3f, f.nav, f.enemy, false);
      assertEquals("", f.m.actionId);
      assertEquals(x, f.m.body.x, 0);
      assertEquals(y, f.m.body.y, 0);
      f.s.party().order(CompanionParty.Order.FOLLOW, null);
      f.s.party().reserve("player", f.enemy.id, "SK_전사_015", 2);
      f.s.skillEffects().put(f.enemy.id, "SK_마법사_017", "SLEEP", 1, 8);
      int hp = f.enemy.hp;
      f.decide();
      f.finish();
      assertEquals(job.name(), hp, f.enemy.hp);
      f.m.body.hp = 0;
      f.m.body.alive = false;
      String last = f.m.actionId;
      f.decide();
      assertEquals(last, f.m.actionId);
    }
  }

  @Test
  public void rogueAndMonkBasicTripleProducesThreeRealContacts() {
    for (CompanionParty.Job job :
        new CompanionParty.Job[] {CompanionParty.Job.ROGUE, CompanionParty.Job.MONK}) {
      Fixture f = fixture(job);
      String action = RuntimeCombatSession.playerAttackActionId(0);
      assertTrue(f.combat.submitCompanion(f.enemy.id, action).accepted());
      RuntimeCombatSession.FrameResult frame = f.combat.tick(.4f);
      assertEquals(
          3,
          frame.events.stream()
              .filter(
                  e -> e.type == CombatResolver.EventType.HIT_FEEDBACK && e.actorId.equals(f.m.id))
              .count());
    }
  }

  @Test
  public void legacyTwoMemberSaveAndEffectsMigrateAtomicallyAndFiveJobsPersist() throws Exception {
    Fixture f = fixture(CompanionParty.Job.WARRIOR);
    f.m.body.hp = 123;
    f.m.body.mp = 45;
    f.m.cooldown("SK_전사_013", 3);
    JSONObject legacy = f.s.party().snapshot();
    JSONArray rows = legacy.getJSONArray("members"), two = new JSONArray();
    two.put(rows.get(0));
    two.put(rows.get(1));
    legacy.put("version", 1).put("members", two);
    CompanionParty restored = new CompanionParty();
    assertTrue(restored.restore(legacy));
    assertEquals(123, restored.active().body.hp);
    assertEquals(45, restored.active().body.mp);
    assertEquals(3, restored.active().cooldown("SK_전사_013"), 0);
    for (CompanionParty.Job job : CompanionParty.Job.values()) assertNotNull(restored.member(job));
    JSONObject fx =
        new JSONObject()
            .put("companion_warrior", new JSONArray())
            .put("companion_priest", new JSONArray());
    assertTrue(f.s.skillEffects().restoreCompanions(f.s, fx));
    for (CompanionParty.Job job : CompanionParty.Job.values()) {
      CompanionParty.Member m = f.s.party().member(job);
      m.body.hp = 20 + job.ordinal();
      m.body.mp = 10 + job.ordinal();
      m.cooldown("agent_retreat", 2);
    }
    assertTrue(restored.restore(f.s.party().snapshot()));
    for (CompanionParty.Job job : CompanionParty.Job.values()) {
      assertEquals(20 + job.ordinal(), restored.member(job).body.hp);
      assertEquals(10 + job.ordinal(), restored.member(job).body.mp);
    }
    JSONObject bad = f.s.party().snapshot();
    bad.getJSONArray("members").getJSONObject(4).put("hp", -1);
    CompanionParty.Member before = restored.active();
    assertFalse(restored.restore(bad));
    assertSame(before, restored.active());
  }

  @Test
  public void hiddenCompanionCannotBeHitAndItsInactiveEffectsSurviveSwitching() throws Exception {
    Fixture f = fixture(CompanionParty.Job.ROGUE);
    f.s.skillEffects().put(f.m.id, "SK_도적_011", "STEALTH", 1, 12);
    assertNotEquals(
        MonsterAutoCombatBridge.Outcome.ACCEPTED,
        f.combat.monsterAutoBridge().submit(f.enemy.id, f.m.id).outcome);
    f.enemy.provoked = false;
    String id = f.m.id;
    assertTrue(f.s.party().recruit(f.s, CompanionParty.Job.MAGE));
    f.s.skillEffects().tick(f.s, 1);
    assertTrue(f.s.skillEffects().has(id, "STEALTH"));
    assertTrue(
        f.s.skillEffects().restoreCompanions(f.s, f.s.skillEffects().companionSnapshot(f.s)));
  }
}
