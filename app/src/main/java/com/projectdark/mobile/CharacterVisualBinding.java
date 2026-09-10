package com.projectdark.mobile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * World/Character read-only projection of RPG-owned equipped Item IDs into character visual slots.
 *
 * This class does not own equip rules and never invents sprite assets. Equipped canonical item IDs
 * are preserved verbatim; unresolved artwork remains PENDING_CROP until positively identified.
 */
public final class CharacterVisualBinding {
  public static final String ASSET_STATUS="PENDING_CROP";
  public static final String WEAPON_SLOT="무기";

  private final Map<String,String> equippedBySlot;
  private final String weaponItemId;
  private final String equipmentVisualRef;
  private final String weaponVisualRef;
  private final boolean valid;

  private CharacterVisualBinding(Map<String,String> equippedBySlot,String weaponItemId,
      String equipmentVisualRef,String weaponVisualRef,boolean valid){
    this.equippedBySlot=Collections.unmodifiableMap(new LinkedHashMap<>(equippedBySlot));
    this.weaponItemId=weaponItemId;
    this.equipmentVisualRef=equipmentVisualRef;
    this.weaponVisualRef=weaponVisualRef;
    this.valid=valid;
  }

  /**
   * Builds a presentation projection from the existing RPG runtime boundary.
   * Unknown/mismatched item definitions fail closed: IDs remain inspectable but validity is false.
   */
  public static CharacterVisualBinding from(RpgProgressionState rpg){
    if(rpg==null)return empty();
    Map<String,String> equipped=new LinkedHashMap<>(rpg.equipment());
    Map<String,RpgProgressionState.ItemDefinition> defs=rpg.itemDefinitions();
    boolean ok=true;
    String weapon=null;
    List<String> equipmentTokens=new ArrayList<>();

    for(Map.Entry<String,String> e:equipped.entrySet()){
      String slot=e.getKey(),itemId=e.getValue();
      RpgProgressionState.ItemDefinition def=defs.get(itemId);
      if(def==null||def.equipSlot==null||!slot.equals(def.equipSlot))ok=false;
      if(WEAPON_SLOT.equals(slot))weapon=itemId;
      else equipmentTokens.add(slot+"="+itemId);
    }

    String equipmentRef=equipmentTokens.isEmpty()?ASSET_STATUS:
        ASSET_STATUS+"|ITEM_IDS:"+join(equipmentTokens);
    String weaponRef=weapon==null?ASSET_STATUS:ASSET_STATUS+"|ITEM_ID:"+weapon;
    return new CharacterVisualBinding(equipped,weapon,equipmentRef,weaponRef,ok);
  }

  public static CharacterVisualBinding empty(){
    return new CharacterVisualBinding(Collections.<String,String>emptyMap(),null,
        ASSET_STATUS,ASSET_STATUS,true);
  }

  public Map<String,String> equippedBySlot(){return equippedBySlot;}
  public String weaponItemId(){return weaponItemId;}
  public String equipmentVisualRef(){return equipmentVisualRef;}
  public String weaponVisualRef(){return weaponVisualRef;}
  public boolean isDefinitionConsistent(){return valid;}
  public boolean hasResolvedSpriteAssets(){return false;}

  private static String join(List<String> values){
    StringBuilder b=new StringBuilder();
    for(int i=0;i<values.size();i++){if(i>0)b.append(',');b.append(values.get(i));}
    return b.toString();
  }
}
