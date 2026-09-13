package com.projectdark.mobile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Read-only projection of RPG equipment into paper-doll visual coverage. */
public final class CharacterVisualBinding {
  public static final String ASSET_STATUS="PENDING_CROP";
  public static final String RESOLVED_ROBE_APPEARANCE_ID="mu0000058";
  public static final String RESOLVED_WEAPON_APPEARANCE_ID="mw001";
  public static final String WEAPON_SLOT="무기";

  public enum GarmentCoverage { FULL_BODY, UPPER, LOWER }
  public enum VisualSlot { FULL_BODY, UPPER, LOWER, HEAD, HANDS, FEET, WEAPON, SHIELD }

  public static final class EquipmentAppearance {
    public final String appearanceId;
    public final VisualSlot visualSlot;
    public final GarmentCoverage garmentCoverage;
    EquipmentAppearance(String appearanceId,VisualSlot visualSlot,GarmentCoverage garmentCoverage){
      this.appearanceId=appearanceId;this.visualSlot=visualSlot;this.garmentCoverage=garmentCoverage;
    }
  }

  private final Map<String,String> equippedBySlot;
  private final String weaponItemId;
  private final String equipmentVisualRef;
  private final String weaponVisualRef;
  private final List<EquipmentAppearance> appearances;
  private final boolean valid;
  private final boolean resolved;

  private CharacterVisualBinding(Map<String,String> equippedBySlot,String weaponItemId,
      String equipmentVisualRef,String weaponVisualRef,List<EquipmentAppearance> appearances,
      boolean valid,boolean resolved){
    this.equippedBySlot=Collections.unmodifiableMap(new LinkedHashMap<>(equippedBySlot));
    this.weaponItemId=weaponItemId;
    this.equipmentVisualRef=equipmentVisualRef;
    this.weaponVisualRef=weaponVisualRef;
    this.appearances=Collections.unmodifiableList(new ArrayList<>(appearances));
    this.valid=valid;this.resolved=resolved;
  }

  public static GarmentCoverage garmentCoverageForAppearance(String appearanceId){
    if(RESOLVED_ROBE_APPEARANCE_ID.equals(appearanceId))return GarmentCoverage.FULL_BODY;
    return null;
  }

  public static VisualSlot visualSlotForAppearance(String appearanceId){
    if(RESOLVED_WEAPON_APPEARANCE_ID.equals(appearanceId))return VisualSlot.WEAPON;
    GarmentCoverage coverage=garmentCoverageForAppearance(appearanceId);
    if(coverage==GarmentCoverage.FULL_BODY)return VisualSlot.FULL_BODY;
    if(coverage==GarmentCoverage.UPPER)return VisualSlot.UPPER;
    if(coverage==GarmentCoverage.LOWER)return VisualSlot.LOWER;
    return null;
  }

  public static boolean isFullBodyAppearance(String appearanceId){
    return garmentCoverageForAppearance(appearanceId)==GarmentCoverage.FULL_BODY;
  }

  public static CharacterVisualBinding from(RpgProgressionState rpg){
    if(rpg==null)return empty();
    Map<String,String> equipped=new LinkedHashMap<>(rpg.equipment());
    Map<String,RpgProgressionState.ItemDefinition> defs=rpg.itemDefinitions();
    boolean ok=true,resolved=false;
    String weapon=null;
    List<String> equipmentTokens=new ArrayList<>();
    List<EquipmentAppearance> appearances=new ArrayList<>();

    for(Map.Entry<String,String> e:equipped.entrySet()){
      String slot=e.getKey(),itemId=e.getValue();
      RpgProgressionState.ItemDefinition def=defs.get(itemId);
      if(def==null||def.equipSlot==null||!slot.equals(def.equipSlot))ok=false;
      if(WEAPON_SLOT.equals(slot)){
        weapon=itemId;
        if(def!=null&&def.appearanceId!=null){
          resolved=true;
          appearances.add(new EquipmentAppearance(def.appearanceId,VisualSlot.WEAPON,null));
        }
      }else if(def!=null&&def.appearanceId!=null){
        equipmentTokens.add(def.appearanceId);resolved=true;
        GarmentCoverage coverage=garmentCoverageForAppearance(def.appearanceId);
        appearances.add(new EquipmentAppearance(def.appearanceId,visualSlotForAppearance(def.appearanceId),coverage));
      }else equipmentTokens.add(ASSET_STATUS+":"+itemId);
    }

    String equipmentRef=equipmentTokens.isEmpty()?ASSET_STATUS:join(equipmentTokens);
    RpgProgressionState.ItemDefinition weaponDef=weapon==null?null:defs.get(weapon);
    String weaponRef=weaponDef!=null&&weaponDef.appearanceId!=null
        ?weaponDef.appearanceId
        :weapon==null?ASSET_STATUS:ASSET_STATUS+"|ITEM_ID:"+weapon;
    return new CharacterVisualBinding(equipped,weapon,equipmentRef,weaponRef,appearances,ok,resolved);
  }

  public static CharacterVisualBinding empty(){
    return new CharacterVisualBinding(Collections.<String,String>emptyMap(),null,
        ASSET_STATUS,ASSET_STATUS,Collections.<EquipmentAppearance>emptyList(),true,false);
  }

  public Map<String,String> equippedBySlot(){return equippedBySlot;}
  public String weaponItemId(){return weaponItemId;}
  public String equipmentVisualRef(){return equipmentVisualRef;}
  public String weaponVisualRef(){return weaponVisualRef;}
  public List<EquipmentAppearance> equipmentAppearances(){return appearances;}
  public boolean isDefinitionConsistent(){return valid;}
  public boolean hasResolvedSpriteAssets(){return resolved;}

  public boolean hasCoverage(GarmentCoverage coverage){
    if(coverage==null)return false;
    for(EquipmentAppearance appearance:appearances)if(appearance.garmentCoverage==coverage)return true;
    return false;
  }

  private static String join(List<String> values){
    StringBuilder b=new StringBuilder();
    for(int i=0;i<values.size();i++){if(i>0)b.append(',');b.append(values.get(i));}
    return b.toString();
  }
}
