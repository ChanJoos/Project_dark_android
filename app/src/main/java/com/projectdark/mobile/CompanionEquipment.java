package com.projectdark.mobile;

import java.util.*;
import java.util.function.BooleanSupplier;

/**
 * Companion equipment authority. Explicit transfers conserve ownership; no test kits or condition
 * bypass.
 */
final class CompanionEquipment {
  static Map<String, Integer> counts(Map<String, String> equipped) {
    Map<String, Integer> result = new LinkedHashMap<>();
    for (String id : equipped.values()) result.put(id, result.getOrDefault(id, 0) + 1);
    return result;
  }

  static RpgProgressionState.RequirementResult requirement(CompanionParty.Member m, String id) {
    id = RpgProgressionState.canonicalItemId(id);
    RpgProgressionState.RequirementResult current = m.growth.currentRequirements(id);
    if (current == RpgProgressionState.RequirementResult.PENDING) return current;
    return m.growth.evaluateRequirements(id, m.job.code, m.level);
  }

  static int available(RuntimeState state, String id) {
    return state.rpg().inventory().getOrDefault(id, 0) - state.rpg().equippedCount(id);
  }

  static final class Result {
    final RpgProgressionState.EquipResult equipment;
    final boolean saved;

    Result(RpgProgressionState.EquipResult equipment, boolean saved) {
      this.equipment = equipment;
      this.saved = saved;
    }
  }

  static Result change(
      RuntimeState state,
      CompanionParty.Job job,
      String item,
      String slot,
      boolean remove,
      BooleanSupplier checkpoint) {
    Map<String, Integer> owned = new LinkedHashMap<>(state.rpg().inventory());
    Map<String, String> worn = new LinkedHashMap<>(state.rpg().equipment());
    CompanionParty.Member member = state.party().member(job);
    Map<String, Integer> own = new LinkedHashMap<>(member.growth.inventory());
    Map<String, String> gear = new LinkedHashMap<>(member.growth.equipment());
    int hp = member.body.hp, mp = member.body.mp;
    RpgProgressionState.EquipResult result = apply(state, job, item, slot, remove);
    if (result != RpgProgressionState.EquipResult.EQUIPPED
        && result != RpgProgressionState.EquipResult.UNEQUIPPED) return new Result(result, true);
    if (checkpoint.getAsBoolean()) return new Result(result, true);
    if (!state.rpg().restoreOwnedItems(owned, worn) || !member.growth.restoreOwnedItems(own, gear))
      throw new IllegalStateException("companion equipment rollback");
    member.syncEquipment();
    member.body.hp = hp;
    member.body.mp = mp;
    return new Result(result, false);
  }

  static RpgProgressionState.EquipResult apply(
      RuntimeState state, CompanionParty.Job job, String id, String slot, boolean remove) {
    CompanionParty.Member m = state.party().member(job);
    if (m == null) return RpgProgressionState.EquipResult.UNKNOWN_ITEM;
    id = RpgProgressionState.canonicalItemId(id);
    if (!remove && Objects.equals(id, m.growth.equipment().get(slot)))
      return RpgProgressionState.EquipResult.INVALID_SLOT;
    if (!remove) {
      RpgProgressionState.RequirementResult requirement = requirement(m, id);
      if (requirement == RpgProgressionState.RequirementResult.PENDING)
        return RpgProgressionState.EquipResult.REQUIREMENT_PENDING;
      if (requirement != RpgProgressionState.RequirementResult.MET)
        return RpgProgressionState.EquipResult.REQUIREMENT_NOT_MET;
    }
    if (!remove && available(state, id) <= 0) return RpgProgressionState.EquipResult.ITEM_NOT_OWNED;
    RpgProgressionState staged = new RpgProgressionState();
    staged.restoreProgression(m.level, 0);
    staged.restoreJobCode(m.job.code);
    staged.restoreStats(
        m.growth.str(), m.growth.intel(), m.growth.wis(), m.growth.con(), m.growth.dex(), 0);
    staged.restoreBaseResources(m.growth.baseMaxHp(), m.growth.baseMaxMp());
    staged.bindTwoHandLearning(() -> m.learned("SK_전사_008"));
    Map<String, Integer> allocations = counts(m.growth.equipment());
    if (!remove) allocations.put(id, allocations.getOrDefault(id, 0) + 1);
    for (String key : allocations.keySet())
      if (!staged.itemDefinitions().containsKey(key)) {
        RpgProgressionState.ItemDefinition d = m.growth.itemDefinitions().get(key);
        if (d == null) d = state.rpg().itemDefinitions().get(key);
        if (d != null) staged.registerSourceAccessory(d);
      }
    if (!staged.restoreOwnedItems(allocations, m.growth.equipment()))
      return RpgProgressionState.EquipResult.UNKNOWN_ITEM;
    RpgProgressionState.EquipResult result =
        remove ? staged.unequip(slot) : staged.equipToSlot(id, slot);
    if (result != RpgProgressionState.EquipResult.EQUIPPED
        && result != RpgProgressionState.EquipResult.UNEQUIPPED) return result;
    Map<String, Integer> next = counts(staged.equipment());
    Map<String, Integer> bag = new LinkedHashMap<>(state.rpg().inventory());
    if (!remove) {
      int n = bag.get(id) - 1;
      if (n == 0) bag.remove(id);
      else bag.put(id, n);
    }
    for (Map.Entry<String, Integer> e : allocations.entrySet()) {
      int returned = e.getValue() - next.getOrDefault(e.getKey(), 0);
      if (returned > 0) {
        long n = (long) bag.getOrDefault(e.getKey(), 0) + returned;
        if (n > Integer.MAX_VALUE) return RpgProgressionState.EquipResult.ITEM_NOT_OWNED;
        bag.put(e.getKey(), (int) n);
      }
    }
    // Both maps were validated on staged authorities before either live owner changes.
    Map<String, Integer> before = new LinkedHashMap<>(state.rpg().inventory());
    Map<String, String> playerEquipment = new LinkedHashMap<>(state.rpg().equipment());
    if (!state.rpg().restoreOwnedItems(bag, playerEquipment))
      return RpgProgressionState.EquipResult.ITEM_NOT_OWNED;
    if (!m.growth.restoreOwnedItems(next, staged.equipment())) {
      state.rpg().restoreOwnedItems(before, playerEquipment);
      throw new IllegalStateException("validated companion equipment rejected");
    }
    m.syncEquipment();
    return result;
  }

  private CompanionEquipment() {}
}
