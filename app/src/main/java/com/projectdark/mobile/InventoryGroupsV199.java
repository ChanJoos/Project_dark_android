package com.projectdark.mobile;
import java.util.*;
/** Inventory-window grouping only. Save IDs, bank rows and item authority remain intact. */
final class InventoryGroupsV199 {
 private static final Map<RpgProgressionState.ItemDefinition,List<Object>> KEYS=Collections.synchronizedMap(new WeakHashMap<>());
 static List<Object> key(RpgProgressionState.ItemDefinition d){
  List<Object> k=KEYS.get(d);if(k!=null)return k;
  k=Arrays.asList(RpgInventoryPresentation.displayName(d.name),d.equipSlot,d.appearanceId,d.basicAttackAction,d.requiredLevel,d.allowedJobCodes,d.jobRestrictionResolved,d.attackElement,d.defenseElement,d.statModifiers,ItemEffects.description(d));KEYS.put(d,k);return k;
 }
 static boolean equivalent(RpgProgressionState r,String a,String b){RpgProgressionState.ItemDefinition x=r.itemDefinitions().get(a),y=r.itemDefinitions().get(b);return x!=null&&y!=null&&x.equippable()&&y.equippable()&&key(x).equals(key(y));}
 static List<RpgInventoryPresentation.ItemRow> group(List<RpgInventoryPresentation.ItemRow> input,RpgProgressionState r){
  Map<Object,List<RpgInventoryPresentation.ItemRow>> groups=new LinkedHashMap<>();
  for(RpgInventoryPresentation.ItemRow row:input){RpgProgressionState.ItemDefinition d=r.itemDefinitions().get(row.itemId);Object key=d.equippable()?key(d):row.itemId;groups.computeIfAbsent(key,k->new ArrayList<>()).add(row);}
  List<RpgInventoryPresentation.ItemRow> out=new ArrayList<>();
  for(List<RpgInventoryPresentation.ItemRow> list:groups.values()){
   if(list.size()==1){out.add(list.get(0));continue;}
   RpgInventoryPresentation.ItemRow chosen=list.get(0);long quantity=0;
   for(RpgInventoryPresentation.ItemRow row:list){quantity+=row.quantity;if(row.equipped)chosen=row;}
   out.add(new RpgInventoryPresentation.ItemRow(r.itemDefinitions().get(chosen.itemId),(int)Math.min(Integer.MAX_VALUE,quantity),chosen.equipped,r.currentRequirements(chosen.itemId)));
  }return out;
 }
 static String actionId(RpgProgressionState r,String selected,String target){
  String equipped=r.equipment().get(target);if(equivalent(r,selected,equipped))return equipped;
  for(Map.Entry<String,Integer> e:r.inventory().entrySet())if(equivalent(r,selected,e.getKey())&&e.getValue()>r.equippedCount(e.getKey()))return e.getKey();
  return selected;
 }
}
