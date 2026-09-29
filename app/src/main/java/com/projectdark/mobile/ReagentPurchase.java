package com.projectdark.mobile;
import java.util.*;
/** Atomic shop purchase using the same checkpoint boundary as skill learning. */
final class ReagentPurchase {
  enum Result {PURCHASED,UNAVAILABLE,INSUFFICIENT_GOLD,INVENTORY_FULL,SAVE_FAILED}
  static Result buy(String id,RpgProgressionState r,SkillAcquisition.Durability save){
    ReagentShopCatalog.Offer offer=null;for(ReagentShopCatalog.Offer o:ReagentShopCatalog.offers())if(o.itemId.equals(id))offer=o;
    if(offer==null||!offer.purchasable())return Result.UNAVAILABLE;
    if(r.gold()<offer.price)return Result.INSUFFICIENT_GOLD;
    long gold=r.gold();Map<String,Integer> items=new LinkedHashMap<>(r.inventory());Map<String,String> gear=new LinkedHashMap<>(r.equipment());
    if(!r.buyItem(id,offer.price))return Result.INVENTORY_FULL;
    boolean committed=false;try{committed=save!=null&&save.checkpoint();return committed?Result.PURCHASED:Result.SAVE_FAILED;}
    finally{if(!committed){r.restoreGold(gold);if(!r.restoreOwnedItems(items,gear))throw new IllegalStateException("purchase rollback failed");}}
  }
}
