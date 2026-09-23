package com.projectdark.mobile;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** Game-owned direct-click shop session for the classic Milles reagent shop. */
public final class ReagentShopController {
  public static final class Offer {
    public final String itemId,name; public final Long price;
    Offer(String itemId,String name,Long price){this.itemId=itemId;this.name=name;this.price=price;}
  }
  public enum BuyResult { BOUGHT, PRICE_UNRESOLVED, INSUFFICIENT_GOLD, INVENTORY_REJECTED, SHOP_CLOSED }

  private static final List<Offer> OFFERS=Collections.unmodifiableList(Arrays.asList(
      new Offer(RpgProgressionState.REAGENT_KOMADIUM_ITEM_ID,"코마디움",null),
      new Offer(RpgProgressionState.REAGENT_DIVENOUM_ITEM_ID,"디베노움",null),
      new Offer(RpgProgressionState.REAGENT_CURANUM_ITEM_ID,"쿠라눔",null)));
  private boolean open;

  public List<Offer> offers(){return OFFERS;}
  public boolean open(){return open;}
  public void openDirect(){open=true;}
  public void close(){open=false;}
  public BuyResult buy(int index,RpgProgressionState rpg){
    if(!open)return BuyResult.SHOP_CLOSED;if(index<0||index>=OFFERS.size())return BuyResult.INVENTORY_REJECTED;
    Offer o=OFFERS.get(index);if(o.price==null)return BuyResult.PRICE_UNRESOLVED;
    if(rpg.gold()==null||rpg.gold()<o.price)return BuyResult.INSUFFICIENT_GOLD;
    return rpg.buyItem(o.itemId,o.price)?BuyResult.BOUGHT:BuyResult.INVENTORY_REJECTED;
  }
}
