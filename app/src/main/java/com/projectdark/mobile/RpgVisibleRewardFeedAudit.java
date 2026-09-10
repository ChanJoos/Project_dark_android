package com.projectdark.mobile;

/** Regression audit for explicit direct-inventory grant outcomes and player-visible reward-feed mapping. */
public final class RpgVisibleRewardFeedAudit {
  private RpgVisibleRewardFeedAudit(){}

  public static boolean verify(){
    RpgProgressionState rpg=new RpgProgressionState();
    RpgVisibleProgressionPresentation visible=new RpgVisibleProgressionPresentation();
    String known="IT_RING_THREELINEGOLD";

    RpgProgressionState.RewardGrantOutcome granted=rpg.grantResolvedRewardItem(known,2,RpgProgressionState.Evidence.V);
    if(granted.status!=RpgProgressionState.RewardGrantStatus.GRANTED)return false;
    if(rpg.inventory().get(known)==null||rpg.inventory().get(known)!=2)return false;
    if(visible.grantOutcomeLine(rpg,granted).kind!=RpgVisibleProgressionPresentation.RewardLineKind.ITEM_GRANTED)return false;

    RpgProgressionState.RewardGrantOutcome fill=rpg.grantResolvedRewardItem(known,999997,RpgProgressionState.Evidence.V);
    if(fill.status!=RpgProgressionState.RewardGrantStatus.GRANTED)return false;
    RpgProgressionState.RewardGrantOutcome full=rpg.grantResolvedRewardItem(known,1,RpgProgressionState.Evidence.V);
    if(full.status!=RpgProgressionState.RewardGrantStatus.INVENTORY_FULL)return false;
    if(visible.grantOutcomeLine(rpg,full).kind!=RpgVisibleProgressionPresentation.RewardLineKind.INVENTORY_FULL)return false;

    RpgProgressionState.RewardGrantOutcome invalidItem=rpg.grantResolvedRewardItem("IT_UNKNOWN",1,RpgProgressionState.Evidence.U);
    if(invalidItem.status!=RpgProgressionState.RewardGrantStatus.INVALID_ITEM)return false;
    if(visible.grantOutcomeLine(rpg,invalidItem).kind!=RpgVisibleProgressionPresentation.RewardLineKind.INVALID_ITEM)return false;

    RpgProgressionState.RewardGrantOutcome invalidQuantity=rpg.grantResolvedRewardItem(known,0,RpgProgressionState.Evidence.V);
    if(invalidQuantity.status!=RpgProgressionState.RewardGrantStatus.INVALID_QUANTITY)return false;
    if(visible.grantOutcomeLine(rpg,invalidQuantity).kind!=RpgVisibleProgressionPresentation.RewardLineKind.INVALID_QUANTITY)return false;

    RpgProgressionState.RewardGrantOutcome unresolved=rpg.grantResolvedRewardItem(known,null,RpgProgressionState.Evidence.PENDING);
    if(unresolved.status!=RpgProgressionState.RewardGrantStatus.UNRESOLVED_REWARD)return false;
    if(visible.grantOutcomeLine(rpg,unresolved).kind!=RpgVisibleProgressionPresentation.RewardLineKind.UNRESOLVED)return false;

    return true;
  }
}
