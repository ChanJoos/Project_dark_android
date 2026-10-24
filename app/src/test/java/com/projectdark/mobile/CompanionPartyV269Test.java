package com.projectdark.mobile;

import static org.junit.Assert.*;

import org.json.*;
import org.junit.Test;

/** Domain integration using real RuntimeState, shared Resolver, formulas and ledger. */
public class CompanionPartyV269Test {
  RuntimeState state(int level, String job) {
    RuntimeState s = new RuntimeState();
    s.rpg().restoreProgression(level, 0);
    assertTrue(s.rpg().restoreJobCode(job));
    s.applyDerivedGrowth();
    return s;
  }

  RuntimeCombatSession session(RuntimeState s) {
    return new RuntimeCombatSession(s, (a, t) -> true, (a, id) -> a.equals("player"), a -> true);
  }

  CompanionParty.Member recruit(RuntimeState s, CompanionParty.Job job) {
    assertTrue(s.party().recruit(s, job));
    CompanionParty.Member m = s.party().active();
    m.body.x = s.player().x;
    m.body.y = s.player().y;
    return m;
  }

  @Test
  public void startingCommonerHasNoImplicitPartyAndRecruitmentRequiresJob() {
    RuntimeState s = new RuntimeState();
    assertNull(s.party().active());
    assertFalse(s.party().recruit(s, CompanionParty.Job.WARRIOR));
  }

  @Test
  public void allyHealConsumesCasterMpAndUsesRecipientHp() {
    RuntimeState s = state(41, "CLERIC");
    CompanionParty.Member m = recruit(s, CompanionParty.Job.WARRIOR);
    m.body.hp = 1;
    int own = s.player().hp, mp = s.player().mp;
    RuntimeCombatSession c = session(s);
    assertTrue(c.submitPlayer(m.id, "SK_성직자_001").accepted());
    c.tick(.3f);
    assertTrue(m.body.hp > 1);
    assertEquals(own, s.player().hp);
    assertEquals(mp - SkillAbilityCatalog.get("SK_성직자_001").mpCost, s.player().mp);
  }

  @Test
  public void priestCompanionHealsPlayerWithoutSpendingPlayerMp() {
    RuntimeState s = state(41, "WARRIOR");
    CompanionParty.Member m = recruit(s, CompanionParty.Job.PRIEST);
    s.player().hp = 1;
    int own = s.player().mp, mp = m.body.mp;
    RuntimeCombatSession c = session(s);
    assertTrue(c.submitCompanion("player", "SK_성직자_005").accepted());
    c.tick(.3f);
    assertTrue(s.player().hp > 1);
    assertEquals(own, s.player().mp);
    assertEquals(mp - SkillAbilityCatalog.get("SK_성직자_005").mpCost, m.body.mp);
  }

  @Test
  public void companionCannotUseUnlearnedOrWrongJobSkills() {
    RuntimeState s = state(11, "MAGE");
    recruit(s, CompanionParty.Job.PRIEST);
    RuntimeCombatSession c = session(s);
    assertFalse(c.submitCompanion("player", "SK_성직자_032").accepted());
    assertFalse(c.submitCompanion("player", "SK_마법사_002").accepted());
    assertFalse(c.submitCompanion("player", "SK_성직자_002").accepted());
    assertFalse(c.submitCompanion("player", "SK_성직자_012").accepted());
  }

  @Test
  public void friendlyDamageAndEnemyHealingAreRejected() {
    RuntimeState s = state(41, "CLERIC");
    CompanionParty.Member m = recruit(s, CompanionParty.Job.WARRIOR);
    RuntimeCombatSession c = session(s);
    assertFalse(c.submitPlayer(m.id, "SK_마법사_001").accepted());
    assertFalse(c.submitPlayer(s.monsters().get(0).id, "SK_성직자_001").accepted());
  }

  @Test
  public void warriorSoulUsesCompanionHpAndCostsCompanionHp() {
    RuntimeState s = state(99, "CLERIC");
    CompanionParty.Member m = recruit(s, CompanionParty.Job.WARRIOR);
    RuntimeState.Monster target = s.monsters().get(0);
    m.body.x = target.x - 32;
    m.body.y = target.y - 16;
    int hp = m.body.hp, player = s.player().hp;
    RuntimeCombatSession c = session(s);
    assertTrue(c.submitCompanion(target.id, "SK_전사_013").accepted());
    c.tick(.2f);
    assertEquals(hp - (int) Math.floor(hp * .9), m.body.hp);
    assertEquals(player, s.player().hp);
    assertFalse(target.alive);
    long defeats =
        s.ledger().snapshot().stream()
            .filter(
                e -> e.type == CombatLedger.Type.MONSTER_DEFEATED && e.targetId.equals(target.id))
            .count();
    assertEquals(1, defeats);
    c.tick(.2f);
    assertEquals(
        1,
        s.ledger().snapshot().stream()
            .filter(
                e -> e.type == CombatLedger.Type.MONSTER_DEFEATED && e.targetId.equals(target.id))
            .count());
  }

  @Test
  public void crashConditionIsNotInventedByMadSoul() {
    RuntimeState s = state(99, "CLERIC");
    CompanionParty.Member m = recruit(s, CompanionParty.Job.WARRIOR);
    RuntimeState.Monster target = s.monsters().get(0);
    m.body.x = target.x - 32;
    m.body.y = target.y - 16;
    m.body.hp = m.body.maxHp / 10;
    RuntimeCombatSession c = session(s);
    assertFalse(c.submitCompanion(target.id, "SK_전사_015").accepted());
    m.body.hp = Math.max(1, m.body.maxHp / 100);
    assertTrue(c.submitCompanion(target.id, "SK_전사_015").accepted());
  }

  @Test
  public void supportPoliciesAvoidFullHpHealingAndPriestCurse() {
    RuntimeState s = state(99, "WARRIOR");
    CompanionParty.Member m = recruit(s, CompanionParty.Job.PRIEST);
    assertNull(CompanionPartyRules.heal(m, s.player()));
    s.skillEffects().put("player", "SK_마법사_025", "FREEZE", 1, 5);
    String dispel = CompanionPartyRules.dispel(m, s.skillEffects(), "player");
    assertNotNull(dispel);
    assertEquals("CLEAR_FREEZE", SkillAbilityCatalog.get(dispel).status);
    assertFalse(m.learned("SK_성직자_012"));
  }

  @Test
  public void partySnapshotPreservesHpMpDeathCooldownAndDoesNotHealOnRejoin() throws Exception {
    RuntimeState s = state(99, "MAGE");
    CompanionParty.Member m = recruit(s, CompanionParty.Job.WARRIOR);
    m.body.hp = 123;
    m.body.mp = 45;
    m.cooldown("SK_전사_013", 12);
    JSONObject saved = s.party().snapshot();
    CompanionParty other = new CompanionParty();
    assertTrue(other.restore(saved));
    assertEquals(123, other.active().body.hp);
    assertEquals(45, other.active().body.mp);
    assertEquals(12, other.active().cooldown("SK_전사_013"), .001);
    assertTrue(s.party().recruit(s, null));
    assertTrue(s.party().recruit(s, CompanionParty.Job.WARRIOR));
    assertEquals(123, s.party().active().body.hp);
    m = s.party().active();
    m.body.hp = 0;
    m.body.alive = false;
    assertTrue(other.restore(s.party().snapshot()));
    assertFalse(other.active().body.alive);
  }

  @Test
  public void invalidSnapshotDoesNotPartiallyReplaceRoster() throws Exception {
    RuntimeState s = state(41, "MAGE");
    CompanionParty.Member m = recruit(s, CompanionParty.Job.PRIEST);
    JSONObject bad = s.party().snapshot();
    bad.getJSONArray("members").getJSONObject(1).put("hp", -1);
    assertFalse(s.party().restore(bad));
    assertSame(m, s.party().active());
  }

  @Test
  public void reservationProtectsSleepAndCrashButExpiresAndEndsOnContact() {
    RuntimeState s = state(99, "WARRIOR");
    CompanionParty.Member m = recruit(s, CompanionParty.Job.PRIEST);
    RuntimeState.Monster target = s.monsters().get(0);
    s.party().reserve("player", target.id, "SK_전사_015", .5f);
    assertTrue(s.party().holdAttack(m.id, target.id));
    assertTrue(s.party().holdHeal("player"));
    s.party().finishReservation("player", "SK_전사_015");
    assertFalse(s.party().holdHeal("player"));
    s.party().reserve("player", target.id, "SK_전사_015", .1f);
    s.party().tick(s, .1f);
    assertFalse(s.party().holdAttack(m.id, target.id));
  }

  @Test
  public void monsterAttackCanHitCompanionAndNotPlayer() {
    RuntimeState s = state(41, "MAGE");
    CompanionParty.Member m = recruit(s, CompanionParty.Job.WARRIOR);
    RuntimeState.Monster monster = s.monsters().get(0);
    m.body.x = monster.x - 32;
    m.body.y = monster.y - 16;
    int hp = m.body.hp, player = s.player().hp;
    RuntimeCombatSession c = session(s);
    assertTrue(
        c.monsterAutoBridge().submit(monster.id, m.id).outcome
            == MonsterAutoCombatBridge.Outcome.ACCEPTED);
    c.tick(.3f);
    assertTrue(m.body.hp < hp);
    assertEquals(player, s.player().hp);
  }

  @Test
  public void companionEffectsKeepDurationAndAreRestoredWithoutTouchingPlayer() throws Exception {
    RuntimeState s = state(41, "CLERIC");
    CompanionParty.Member m = recruit(s, CompanionParty.Job.WARRIOR);
    s.skillEffects().put(m.id, "SK_성직자_028", "PROTECT", 30, 10);
    s.skillEffects().tick(s, 2);
    assertEquals(8, s.skillEffects().get(m.id, "PROTECT").remaining, .001);
    JSONObject saved = s.skillEffects().companionSnapshot(s);
    s.skillEffects().clear(m.id);
    assertTrue(s.skillEffects().restoreCompanions(s, saved));
    assertEquals(8, s.skillEffects().get(m.id, "PROTECT").remaining, .001);
  }

  @Test
  public void hiddenRogueCanStillReceiveFriendlyHealing() {
    RuntimeState s = state(41, "ROGUE");
    CompanionParty.Member m = recruit(s, CompanionParty.Job.PRIEST);
    s.player().hp = 1;
    s.skillEffects().put("player", "SK_도적_011", "STEALTH", 1, 12);
    RuntimeCombatSession c = session(s);
    assertTrue(c.submitCompanion("player", "SK_성직자_005").accepted());
    c.tick(.3f);
    assertTrue(s.player().hp > 1);
    assertTrue(s.skillEffects().hidden());
  }

  @Test
  public void playerSelfCenteredAttackStillUsesEnemyRecipients() {
    RuntimeState s = state(41, "MAGE");
    recruit(s, CompanionParty.Job.WARRIOR);
    RuntimeState.Monster enemy = s.monsters().get(0);
    s.player().x = enemy.x - 32;
    s.player().y = enemy.y - 16;
    RuntimeCombatSession c = session(s);
    int hp = enemy.hp;
    assertTrue(c.submitPlayer("player", "SK_마법사_007").accepted());
    c.tick(.3f);
    assertTrue(enemy.hp < hp);
  }
}
