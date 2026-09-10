package com.projectdark.mobile;

import java.util.List;

/**
 * Regression audit for the combat -> RPG reward boundary.
 * Replaying the same combat ledger snapshot must not resolve one MONSTER_DEFEATED sequence twice.
 * This does not fabricate drop probability/quantity and does not create ground loot.
 */
public final class MonsterDefeatIdempotencyAudit {
  private MonsterDefeatIdempotencyAudit(){}

  public static boolean verify(){
    RpgProgressionState probe=new RpgProgressionState();
    CombatLedger ledger=new CombatLedger();

    // POTE_SPIRIT has a verified EXP reward entry. Its item drop remains unresolved, so this audit
    // checks exactly-once reward resolution without asserting a deterministic item emission.
    ledger.add(CombatLedger.Type.MONSTER_DEFEATED,"player","POTE_SPIRIT",0);
    List<CombatLedger.Event> snapshot=ledger.snapshot();

    probe.consumeCombat(snapshot,null);
    if(probe.rewardHistory().size()!=1)return false;
    RpgProgressionState.RewardResolution first=probe.rewardHistory().get(0);
    if(first.status!=RpgProgressionState.RewardStatus.RESOLVED)return false;
    if(first.exp==null||first.exp.intValue()!=308950)return false;
    if(!first.autoLootedItems.isEmpty())return false;

    // RuntimeState feeds a bounded snapshot every tick. Re-consuming the same snapshot must be a no-op.
    probe.consumeCombat(snapshot,null);
    if(probe.rewardHistory().size()!=1)return false;

    // A distinct later defeat sequence is a distinct event and may resolve once.
    ledger.add(CombatLedger.Type.MONSTER_DEFEATED,"player","POTE_SPIRIT",0);
    probe.consumeCombat(ledger.snapshot(),null);
    if(probe.rewardHistory().size()!=2)return false;
    if(probe.rewardHistory().get(1).combatSequence<=first.combatSequence)return false;

    // No authoritative drop quantity/rate exists yet, so neither defeat may auto-loot an item.
    return probe.inventory().isEmpty();
  }
}
