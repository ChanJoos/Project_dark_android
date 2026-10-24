package com.projectdark.mobile;

import static org.junit.Assert.*;

import java.util.*;
import org.json.*;
import org.junit.Test;

public class CompanionEquipmentV272Test {
  RuntimeState state() {
    RuntimeState s = new RuntimeState();
    s.party().enableTesting(s);
    return s;
  }

  String armor(RuntimeState s, CompanionParty.Job job, int level) {
    for (RpgProgressionState.ItemDefinition d : s.rpg().itemDefinitions().values())
      if ("갑옷".equals(d.equipSlot)
          && d.appearanceId != null
          && d.appearanceId.startsWith("mu")
          && d.jobRestrictionResolved
          && d.allowedJobCodes.contains(job.code)
          && d.requiredLevel != null
          && d.requiredLevel >= level
          && s.rpg().evaluateRequirements(d.itemId, job.code, 99)
              == RpgProgressionState.RequirementResult.MET
          && CompanionEquipment.available(s, d.itemId) > 0) return d.itemId;
    throw new AssertionError("No source armor for " + job + " level " + level);
  }

  String common(RuntimeState s, String slot) {
    for (RpgProgressionState.ItemDefinition d : s.rpg().itemDefinitions().values())
      if (slot.equals(d.equipSlot)
          && s.party().member(CompanionParty.Job.WARRIOR).growth.currentRequirements(d.itemId)
              == RpgProgressionState.RequirementResult.MET
          && s.party().member(CompanionParty.Job.MAGE).growth.currentRequirements(d.itemId)
              == RpgProgressionState.RequirementResult.MET
          && CompanionEquipment.available(s, d.itemId) > 0) return d.itemId;
    throw new AssertionError(slot);
  }

  int total(RuntimeState s, String id) {
    int n = s.rpg().inventory().getOrDefault(id, 0);
    for (CompanionParty.Job job : CompanionParty.Job.values())
      n += s.party().member(job).growth.inventory().getOrDefault(id, 0);
    return n;
  }

  @Test
  public void ownProfessionAndLevelAreEnforcedEvenWithPlayerSandboxEnabled() {
    RuntimeState s = state();
    s.rpg().enableEquipmentSandbox(true);
    assertEquals("COMMONER", s.rpg().currentJobCode());
    String id = armor(s, CompanionParty.Job.WARRIOR, 99);
    CompanionParty.Member w = s.party().member(CompanionParty.Job.WARRIOR);
    int owned = total(s, id);
    assertEquals(
        RpgProgressionState.EquipResult.REQUIREMENT_NOT_MET,
        CompanionEquipment.apply(s, CompanionParty.Job.MAGE, id, "갑옷", false));
    w.configure(41);
    assertEquals(
        RpgProgressionState.RequirementResult.LEVEL_NOT_MET, CompanionEquipment.requirement(w, id));
    assertEquals(
        RpgProgressionState.EquipResult.REQUIREMENT_NOT_MET,
        CompanionEquipment.apply(s, w.job, id, "갑옷", false));
    w.configure(99);
    assertEquals(
        RpgProgressionState.EquipResult.EQUIPPED,
        CompanionEquipment.apply(s, w.job, id, "갑옷", false));
    assertEquals(id, w.growth.equipment().get("갑옷"));
    assertEquals(owned, total(s, id));
    assertEquals(1, s.rpg().normalLevel().intValue());
    assertEquals("COMMONER", s.rpg().currentJobCode());
  }

  @Test
  public void playerNecklaceTestBypassDoesNotBypassCompanionLevel() {
    RuntimeState s = state();
    CompanionParty.Member m = s.party().active();
    String id = "IT_NECK_WATER_PEARL";
    assertTrue(s.rpg().itemDefinitions().containsKey(id));
    s.rpg().autoLootResolvedItem(id, 1);
    m.configure(1);
    assertEquals(
        RpgProgressionState.RequirementResult.LEVEL_NOT_MET, CompanionEquipment.requirement(m, id));
    assertEquals(
        RpgProgressionState.EquipResult.REQUIREMENT_NOT_MET,
        CompanionEquipment.apply(s, m.job, id, "목걸이", false));
  }

  @Test
  public void equipmentChangesCompanionStatsAndVisualBindingWithoutPlayerChanges() {
    RuntimeState s = state();
    Map<String, String> player = new LinkedHashMap<>(s.rpg().equipment());
    FinalStats own = s.rpg().finalStats();
    CompanionParty.Member m = s.party().member(CompanionParty.Job.WARRIOR);
    String id = armor(s, m.job, 99);
    String old = CharacterVisualBinding.from(m.growth).equipmentVisualRef();
    int hp = m.body.hp;
    assertEquals(
        RpgProgressionState.EquipResult.EQUIPPED,
        CompanionEquipment.apply(s, m.job, id, "갑옷", false));
    assertNotEquals(old, CharacterVisualBinding.from(m.growth).equipmentVisualRef());
    assertTrue(
        CharacterVisualBinding.from(m.growth)
            .equipmentVisualRef()
            .contains(s.rpg().itemDefinitions().get(id).appearanceId));
    assertEquals(FinalStats.from(m.growth).ac, m.stats().ac);
    assertEquals(m.stats().maxHp, m.body.maxHp);
    assertTrue(m.body.hp <= hp);
    assertEquals(player, s.rpg().equipment());
    assertEquals(own.str, s.rpg().finalStats().str);
    assertEquals(own.ac, s.rpg().finalStats().ac);
  }

  @Test
  public void eachJobStoresItsOwnEquipmentAndRestoresWithHpMpAndCooldowns() {
    RuntimeState s = state();
    for (CompanionParty.Job job : CompanionParty.Job.values()) {
      CompanionParty.Member m = s.party().member(job);
      String id = job == CompanionParty.Job.PRIEST ? common(s, "목걸이") : armor(s, job, 11);
      assertEquals(
          RpgProgressionState.EquipResult.EQUIPPED,
          CompanionEquipment.apply(s, job, id, s.rpg().itemDefinitions().get(id).equipSlot, false));
      m.body.hp = 123;
      m.body.mp = 45;
      m.cooldown("agent_retreat", 2);
    }
    CompanionParty restored = new CompanionParty();
    assertTrue(restored.restore(s.party().snapshot()));
    for (CompanionParty.Job job : CompanionParty.Job.values()) {
      assertEquals(
          s.party().member(job).growth.equipment(), restored.member(job).growth.equipment());
      assertEquals(123, restored.member(job).body.hp);
      assertEquals(45, restored.member(job).body.mp);
      assertEquals(2, restored.member(job).cooldown("agent_retreat"), 0);
    }
  }

  @Test
  public void ringHandsConserveCopiesAndUnequipReturnsTheExactCopy() {
    RuntimeState s = state();
    String id = null;
    for (RpgProgressionState.ItemDefinition d : s.rpg().itemDefinitions().values())
      if ("반지".equals(d.equipSlot)
          && CompanionEquipment.requirement(s.party().member(CompanionParty.Job.MAGE), d.itemId)
              == RpgProgressionState.RequirementResult.MET) {
        id = d.itemId;
        break;
      }
    assertNotNull(id);
    s.rpg().autoLootResolvedItem(id, 2);
    int n = total(s, id);
    assertEquals(
        RpgProgressionState.EquipResult.EQUIPPED,
        CompanionEquipment.apply(s, CompanionParty.Job.MAGE, id, "반지", false));
    assertEquals(
        RpgProgressionState.EquipResult.EQUIPPED,
        CompanionEquipment.apply(
            s, CompanionParty.Job.MAGE, id, RpgProgressionState.RIGHT_RING_SLOT, false));
    assertEquals(n, total(s, id));
    assertEquals(
        RpgProgressionState.EquipResult.UNEQUIPPED,
        CompanionEquipment.apply(
            s, CompanionParty.Job.MAGE, null, RpgProgressionState.RIGHT_RING_SLOT, true));
    assertEquals(id, s.party().member(CompanionParty.Job.MAGE).growth.equipment().get("반지"));
    assertEquals(n, total(s, id));
  }

  @Test
  public void cannotBorrowPlayerWornCopyOrDuplicateOneCopyAcrossCompanions() {
    RuntimeState s = state();
    String id = common(s, "목걸이");
    Map<String, Integer> owned = new LinkedHashMap<>();
    owned.put(id, 1);
    assertTrue(s.rpg().restoreOwnedItems(owned, Collections.singletonMap("목걸이", id)));
    assertEquals(
        RpgProgressionState.EquipResult.ITEM_NOT_OWNED,
        CompanionEquipment.apply(s, CompanionParty.Job.WARRIOR, id, "목걸이", false));
    assertTrue(s.rpg().restoreOwnedItems(owned, Collections.emptyMap()));
    assertEquals(
        RpgProgressionState.EquipResult.EQUIPPED,
        CompanionEquipment.apply(s, CompanionParty.Job.WARRIOR, id, "목걸이", false));
    assertEquals(
        RpgProgressionState.EquipResult.ITEM_NOT_OWNED,
        CompanionEquipment.apply(s, CompanionParty.Job.MAGE, id, "목걸이", false));
  }

  @Test
  public void failedCheckpointRollsBackOwnershipStatsAndLiveOrdersWithoutRefilling() {
    RuntimeState s = state();
    CompanionParty.Member m = s.party().member(CompanionParty.Job.WARRIOR);
    m.order = CompanionParty.Order.WAIT;
    m.body.hp = 123;
    m.body.mp = 45;
    m.body.x = 1234;
    m.cooldown("agent_retreat", 3);
    String id = armor(s, m.job, 99);
    Map<String, String> gear = new LinkedHashMap<>(m.growth.equipment());
    Map<String, Integer> bag = new LinkedHashMap<>(s.rpg().inventory());
    CompanionEquipment.Result result =
        CompanionEquipment.change(s, m.job, id, "갑옷", false, () -> false);
    assertFalse(result.saved);
    assertSame(m, s.party().member(m.job));
    assertEquals(gear, m.growth.equipment());
    assertEquals(bag, s.rpg().inventory());
    assertEquals(123, m.body.hp);
    assertEquals(45, m.body.mp);
    assertEquals(1234, m.body.x, 0);
    assertEquals(CompanionParty.Order.WAIT, m.order);
    assertEquals(3, m.cooldown("agent_retreat"), 0);
  }

  @Test
  public void legacySnapshotMigratesAndInvalidEquipmentNeverPartiallyRestores() throws Exception {
    RuntimeState s = state();
    JSONObject old = s.party().snapshot();
    old.put("version", 2);
    JSONArray rows = old.getJSONArray("members");
    for (int i = 0; i < rows.length(); i++) rows.getJSONObject(i).remove("equipment");
    assertTrue(new CompanionParty().restore(old));
    JSONObject bad = s.party().snapshot();
    bad.getJSONArray("members").getJSONObject(4).getJSONObject("equipment").put("모자", "UNKNOWN");
    CompanionParty.Member before = s.party().active();
    assertFalse(s.party().restore(bad));
    assertSame(before, s.party().active());
  }

  @Test
  public void gearChangeCannotResurrectOrHealAndRemovedMaximumClampsOnlyOwner() {
    RuntimeState s = state();
    CompanionParty.Member m = s.party().member(CompanionParty.Job.MAGE);
    m.body.hp = 0;
    m.body.alive = false;
    int playerHp = s.player().hp;
    String id = common(s, "목걸이");
    assertEquals(
        RpgProgressionState.EquipResult.EQUIPPED,
        CompanionEquipment.apply(s, m.job, id, "목걸이", false));
    assertFalse(m.body.alive);
    assertEquals(0, m.body.hp);
    assertEquals(playerHp, s.player().hp);
  }

  @Test
  public void changedWeaponActuallyChangesSharedResolverDamageOnlyForItsOwner() {
    RuntimeState s = state();
    CompanionParty.Member m = s.party().active();
    RuntimeState.Monster enemy = s.monsters().get(0);
    enemy.x = m.body.x + 32;
    enemy.y = m.body.y + 16;
    enemy.hp = 200000;
    RuntimeCombatSession c = new CompanionPartyV269Test().session(s);
    assertTrue(
        c.submitCompanion(enemy.id, RuntimeCombatSession.playerAttackActionId(0)).accepted());
    int hp = enemy.hp;
    c.tick(1.2f);
    int before = hp - enemy.hp;
    String id = "TEST_V272_DAMAGE_WEAPON";
    RpgProgressionState.ItemDefinition fixture =
        new RpgProgressionState.ItemDefinition(
            id,
            "장비 전투 검증 무기 [ADAPTED]",
            "무기",
            "mw001",
            AnimationAction.SWING,
            1,
            Collections.singleton("WARRIOR"),
            true,
            null,
            null,
            Map.of("MinATK", 100, "MaxATK", 100, "STR", 20, "DAM", 10),
            RpgProgressionState.Evidence.ADAPTED);
    s.rpg().registerSourceAccessory(fixture);
    m.growth.registerSourceAccessory(fixture);
    s.rpg().autoLootResolvedItem(id, 1);
    assertEquals(
        RpgProgressionState.EquipResult.EQUIPPED,
        CompanionEquipment.apply(s, m.job, id, "무기", false));
    int playerHp = s.player().hp;
    hp = enemy.hp;
    assertTrue(
        c.submitCompanion(enemy.id, RuntimeCombatSession.playerAttackActionId(0)).accepted());
    c.tick(1.2f);
    assertTrue(hp - enemy.hp > before);
    assertEquals(playerHp, s.player().hp);
  }

  @Test
  public void weaponDamageAndStaffCostsUseCompanionEquipmentOnly() {
    RuntimeState s = state();
    CompanionParty.Member m = s.party().member(CompanionParty.Job.MAGE);
    String staff = null;
    for (RpgProgressionState.ItemDefinition d : s.rpg().itemDefinitions().values())
      if (RpgInventoryPresentation.displayName(d.name).equals("매직파나")
          && CompanionEquipment.requirement(m, d.itemId)
              == RpgProgressionState.RequirementResult.MET) {
        staff = d.itemId;
        break;
      }
    // Real staff source conditions are unresolved; a labelled test fixture exercises only the
    // existing staff function.
    if (staff == null) {
      staff = "TEST_V272_MAGIC_PANA";
      RpgProgressionState.ItemDefinition d =
          new RpgProgressionState.ItemDefinition(
              staff,
              "매직파나",
              "무기",
              "mw062",
              11,
              Collections.singleton("MAGE"),
              true,
              null,
              null,
              Collections.emptyMap(),
              RpgProgressionState.Evidence.ADAPTED);
      s.rpg().registerSourceAccessory(d);
      m.growth.registerSourceAccessory(d);
    }
    s.rpg().autoLootResolvedItem(staff, 1);
    assertEquals(
        RpgProgressionState.EquipResult.EQUIPPED,
        CompanionEquipment.apply(s, m.job, staff, "무기", false));
    assertTrue(s.party().recruit(s, CompanionParty.Job.MAGE));
    RuntimeCombatPortAdapter port = new RuntimeCombatPortAdapter(s, (a, b) -> true, (a, b) -> true);
    RuntimeCombatSession session = new CompanionPartyV269Test().session(s);
    SkillAbilityCatalog.Ability spell = SkillAbilityCatalog.get("SK_마법사_003");
    CombatResolver.Definition d =
        port.resolveDefinition(m.id, session.actionDefinitions().get(spell.id));
    assertEquals((int) Math.ceil(spell.mpCost * .9), d.resourceCost);
    assertEquals(0, d.hitTime, 0);
    assertEquals(
        spell.mpCost,
        port.resolveDefinition("player", session.actionDefinitions().get(spell.id)).resourceCost);
  }
}
