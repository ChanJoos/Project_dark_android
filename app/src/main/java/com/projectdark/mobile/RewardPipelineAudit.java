package com.projectdark.mobile;

import java.util.Map;

/**
 * Runtime contract audit for the retired-ground-loot / direct-auto-loot architecture.
 * Uses an isolated RPG state, so validation never mutates the live player's inventory.
 */
public final class RewardPipelineAudit {
  private RewardPipelineAudit(){}

  public static boolean verify(){
    RpgProgressionState probe=new RpgProgressionState();
    CanonicalMonsterRewardCatalog rewards=new CanonicalMonsterRewardCatalog();

    // Every reward item identity already referenced by canonical reward data must be registered
    // before a future resolved quantity can enter the direct-inventory endpoint.
    Map<String,RpgProgressionState.ItemDefinition> definitions=probe.itemDefinitions();
    for(CanonicalMonsterRewardCatalog.RewardEntry entry:rewards.entries().values()){
      for(CanonicalMonsterRewardCatalog.DropHint hint:entry.dropHints){
        if(!definitions.containsKey(hint.itemId))return false;
      }
    }

    final String canonicalProbeItem="IT_RING_THREELINEGOLD";
    int before=quantity(probe,canonicalProbeItem);
    if(probe.autoLootResolvedItem(canonicalProbeItem,1)!=RpgProgressionState.AutoLootResult.LOOTED)return false;
    if(quantity(probe,canonicalProbeItem)!=before+1)return false;

    // Invalid reward inputs must fail closed and must not mutate inventory.
    int afterValid=quantity(probe,canonicalProbeItem);
    if(probe.autoLootResolvedItem(canonicalProbeItem,0)!=RpgProgressionState.AutoLootResult.INVALID_QUANTITY)return false;
    if(quantity(probe,canonicalProbeItem)!=afterValid)return false;
    if(probe.autoLootResolvedItem("PENDING_UNKNOWN_ITEM",1)!=RpgProgressionState.AutoLootResult.INVALID_ITEM)return false;

    // Reward catalog currently has no authoritative quantity/probability, so no existing hint may
    // silently become deterministic merely because auto-loot exists.
    return rewards.hasNoInventedDropEmission();
  }

  private static int quantity(RpgProgressionState state,String itemId){
    Integer value=state.inventory().get(itemId);
    return value==null?0:value;
  }
}
