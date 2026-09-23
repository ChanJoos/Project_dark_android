package com.projectdark.mobile;

import com.projectdark.mobile.world.AdaptedMillesMapRenderer;

/** B5 integration gate for the playable Milles village -> hunt -> reward -> supply loop. */
public final class BundleB5Audit {
  private BundleB5Audit(){}
  public static boolean verify(){
    if(AdaptedMillesMapRenderer.B1_VISIBLE_BUILDING_SCALE<1.0f)return false;
    AdaptedPrototypeRewardCatalog rewards=new AdaptedPrototypeRewardCatalog();
    for(String id:new String[]{"combat_dummy_01","combat_dummy_02","combat_dummy_03"})if(rewards.find(id)==null)return false;
    RpgProgressionState r=new RpgProgressionState();
    r.restoreGold(40L);
    if(!r.buySmallPotion()||r.gold()!=20L)return false;
    Integer q=r.inventory().get(RpgProgressionState.B_SMALL_POTION_ITEM_ID);
    if(q==null||q!=1)return false;
    if(!r.isConsumable(RpgProgressionState.B_SMALL_POTION_ITEM_ID))return false;
    return true;
  }
  public static void main(String[] args){if(!verify())throw new AssertionError("BundleB5Audit failed");}
}
