package com.projectdark.mobile;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** Shop-domain projection for Merlin's classic Milles reagent counter. Prices remain unresolved until sourced. */
public final class ReagentShopCatalog {
  public static final class Offer {
    public final String itemId,name,iconAsset;
    public final Long price;
    Offer(String itemId,String name,String iconAsset,Long price){this.itemId=itemId;this.name=name;this.iconAsset=iconAsset;this.price=price;}
    public boolean purchasable(){return price!=null&&price>=0;}
  }
  private static final List<Offer> OFFERS=Collections.unmodifiableList(Arrays.asList(
      new Offer(RpgProgressionState.REAGENT_KOMADIUM_ITEM_ID,"코마디움","assets/items/consumable/it_reagent_komadium.webp",null),
      new Offer(RpgProgressionState.REAGENT_DIBENOMUM_ITEM_ID,"디베노뭄","assets/items/consumable/it_reagent_dibenomum.webp",null),
      new Offer(RpgProgressionState.REAGENT_CURANUM_ITEM_ID,"쿠라눔",null,null),
      new Offer(RpgProgressionState.RECALL_MILLES_ITEM_ID,"밀레스리콜","assets/items/consumable/it_recall_milles.webp",null)
  ));
  private ReagentShopCatalog(){}
  public static List<Offer> offers(){return OFFERS;}
}