package com.projectdark.mobile;
import java.util.LinkedHashMap;
import java.util.Map;
/** Exact unique-name match omitted by the display-name-only V210 importer. */
final class RequestedWeaponSourceFacts {
 static final String SHINWOLDO="IT_NATIVE_V210_BE8DD6B8FCAD130F";
 static final String ROO_GRACE="USER_ITEM_20261018_R05020";
 static void install(RpgProgressionState r){
  // User_Item_Master_20261018 row5500: display 곡단도2, unique 신월도.
  // Preserve the existing item ID, art and source-recovery binding; no duplicate inventory item.
  Map<String,Integer> values=new LinkedHashMap<>();
  for(String key:new String[]{"AC","HIT","DAM","HP","MP","STR","DEX","INT","WIS","CON","REGEN","MAGIC_DEFENSE"})values.put(key,0);
  values.put("MinATK",165);values.put("MaxATK",170);values.put("LMinATK",16);values.put("LMaxATK",17);
  r.registerUnifiedItem(SHINWOLDO,"신월도",RpgProgressionState.WEAPON_SLOT,null,99,values,RpgProgressionState.Evidence.U,true);
 }
 private RequestedWeaponSourceFacts(){}
}
