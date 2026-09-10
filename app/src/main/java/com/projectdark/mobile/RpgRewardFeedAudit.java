package com.projectdark.mobile;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

/** Deterministic audit for direct-grant outcomes, feed idempotency and visible lifetime. */
public final class RpgRewardFeedAudit {
  private RpgRewardFeedAudit(){}
  public static boolean verify(){
    RpgProgressionState state=new RpgProgressionState();
    String item="IT_RING_THREELINEGOLD";
    if(state.grantResolvedRewardItem(null,1,RpgProgressionState.Evidence.PENDING).status!=RpgProgressionState.RewardGrantStatus.INVALID_ITEM)return false;
    if(state.grantResolvedRewardItem(item,0,RpgProgressionState.Evidence.V).status!=RpgProgressionState.RewardGrantStatus.INVALID_QUANTITY)return false;
    if(state.grantResolvedRewardItem(item,null,RpgProgressionState.Evidence.V).status!=RpgProgressionState.RewardGrantStatus.UNRESOLVED_REWARD)return false;
    if(state.grantResolvedRewardItem(item,999999,RpgProgressionState.Evidence.V).status!=RpgProgressionState.RewardGrantStatus.GRANTED)return false;
    if(state.grantResolvedRewardItem(item,1,RpgProgressionState.Evidence.V).status!=RpgProgressionState.RewardGrantStatus.INVENTORY_FULL)return false;

    RpgProgressionState.RewardGrantOutcome granted=new RpgProgressionState.RewardGrantOutcome(
        item,1,RpgProgressionState.RewardGrantStatus.GRANTED,RpgProgressionState.Evidence.V);
    LinkedHashMap<String,Integer> items=new LinkedHashMap<>();items.put(item,1);
    RpgProgressionState.RewardResolution reward=new RpgProgressionState.RewardResolution(
        7L,"POTE_SPIRIT",RpgProgressionState.RewardStatus.RESOLVED,308950,items,Arrays.asList(granted));
    RpgRewardFeedPresentation feed=new RpgRewardFeedPresentation();
    feed.sync(Arrays.asList(reward),state.itemDefinitions());
    if(feed.size()!=2)return false;
    feed.sync(Arrays.asList(reward),state.itemDefinitions());
    if(feed.size()!=2)return false;
    List<RpgRewardFeedPresentation.Snapshot> visible=feed.snapshot();
    if(visible.get(0).semantic!=RpgRewardFeedPresentation.Semantic.EXP_GAINED||
        visible.get(1).semantic!=RpgRewardFeedPresentation.Semantic.ITEM_GRANTED)return false;
    feed.tick(2f);if(feed.snapshot().get(0).alpha>=1f)return false;
    feed.tick(1f);if(feed.size()!=0)return false;

    RpgProgressionState.RewardGrantOutcome unresolved=new RpgProgressionState.RewardGrantOutcome(
        item,null,RpgProgressionState.RewardGrantStatus.UNRESOLVED_REWARD,RpgProgressionState.Evidence.V);
    RpgProgressionState.RewardResolution pendingDrop=new RpgProgressionState.RewardResolution(
        8L,"POTE_SPIRIT",RpgProgressionState.RewardStatus.RESOLVED,308950,Collections.<String,Integer>emptyMap(),
        Arrays.asList(unresolved));
    feed.sync(Arrays.asList(pendingDrop),state.itemDefinitions());
    return feed.size()==2&&feed.snapshot().get(1).semantic==RpgRewardFeedPresentation.Semantic.UNRESOLVED_REWARD;
  }
  public static void main(String[] args){
    if(!verify())throw new AssertionError("RpgRewardFeedAudit failed");
    System.out.println("RpgRewardFeedAudit PASS");
  }
}
