package com.projectdark.mobile;

import java.util.List;

/** Regression for 2026-09-10 19:38 device-playtest reward P0. */
public final class RpgPlaytestDummyRewardAudit {
  private RpgPlaytestDummyRewardAudit(){}

  public static boolean verify(){
    CanonicalMonsterRewardCatalog catalog=new CanonicalMonsterRewardCatalog();
    if(!catalog.hasNoInventedDropEmission()||!catalog.hasIsolatedDummyQaFixture())return false;

    RpgProgressionState rpg=new RpgProgressionState();
    CombatLedger ledger=new CombatLedger();
    ledger.add(CombatLedger.Type.MONSTER_DEFEATED,"player","combat_dummy_01",0);
    List<CombatLedger.Event> events=ledger.snapshot();

    List<RpgProgressionState.CombatConsumeOutcome> first=rpg.consumeCombatWithOutcomes(events,null);
    if(first.size()!=1||first.get(0).status!=RpgProgressionState.CombatConsumeStatus.PROCESSED_DEFEAT)return false;
    Integer firstQty=rpg.inventory().get("IT_GLOVE_LEATHER");
    if(firstQty==null||firstQty!=1)return false;

    RpgProgressionState.RewardResolution reward=rpg.rewardHistory().get(rpg.rewardHistory().size()-1);
    if(reward.autoLootedItems.get("IT_GLOVE_LEATHER")==null||reward.autoLootedItems.get("IT_GLOVE_LEATHER")!=1)return false;
    if(reward.grantOutcomes.size()!=1||reward.grantOutcomes.get(0).status!=RpgProgressionState.RewardGrantStatus.GRANTED)return false;
    if(reward.expOutcome!=null)return false;

    // Same ledger event consumed again must be stale/idempotent and must not grant a second item.
    List<RpgProgressionState.CombatConsumeOutcome> second=rpg.consumeCombatWithOutcomes(events,null);
    if(second.size()!=1||second.get(0).status!=RpgProgressionState.CombatConsumeStatus.DUPLICATE_OR_STALE)return false;
    Integer secondQty=rpg.inventory().get("IT_GLOVE_LEATHER");
    return secondQty!=null&&secondQty==1&&rpg.rewardHistory().size()==1;
  }
}
