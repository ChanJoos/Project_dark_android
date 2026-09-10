package com.projectdark.mobile;

import java.util.List;

/**
 * End-to-end boundary regression for the isolated Pote prototype slice.
 * A canonical Monster_ID may be exercised with explicitly prototype placement/combat tuning,
 * but unresolved reward relationships must remain fail-closed and must never fabricate loot/EXP mutation.
 */
public final class PotePrototypeRewardBoundaryAudit {
  private PotePrototypeRewardBoundaryAudit(){}

  public static boolean verify(){
    PotePrototypeWorldDef.Spawn spawn=PotePrototypeWorldDef.primarySpawn();
    RuntimeState.Monster monster=spawn.instantiateRuntimeMonster();
    if(monster==null||!"POTE_PURPLE".equals(monster.id)||!monster.alive)return false;

    CombatLedger ledger=new CombatLedger();
    ledger.add(CombatLedger.Type.MONSTER_DEFEATED,"player",monster.id,0);

    RpgProgressionState rpg=new RpgProgressionState();
    rpg.consumeCombat(ledger.snapshot(),null);

    List<RpgProgressionState.RewardResolution> history=rpg.rewardHistory();
    if(history.size()!=1)return false;
    RpgProgressionState.RewardResolution reward=history.get(0);
    if(reward.status!=RpgProgressionState.RewardStatus.PENDING_NO_CANONICAL_MONSTER_REWARD)return false;
    if(!"POTE_PURPLE".equals(reward.monsterId))return false;
    if(reward.exp!=null)return false;
    if(!reward.autoLootedItems.isEmpty())return false;
    if(!rpg.inventory().isEmpty())return false;
    return rpg.normalExp()==null;
  }
}
