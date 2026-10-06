package com.projectdark.mobile;
/** Free safe-point recovery keeps every class recoverable without invented original prices. */
public final class CampaignResources {
 public static boolean freeRest(RuntimeState state){if(state==null||!state.player().alive)return false;state.applyDerivedGrowth();state.player().hp=state.player().maxHp;state.player().mp=state.player().maxMp;return true;}
 private CampaignResources(){}
}
