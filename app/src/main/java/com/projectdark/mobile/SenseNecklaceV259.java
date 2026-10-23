package com.projectdark.mobile;
/** Select only owned, eligible, registered necklace shortcuts using the accepted element table. */
final class SenseNecklaceV259 {
 static String best(RuntimeState s,RuntimeState.Monster target,QuickItemBarV254 bar){
  if(s==null||target==null||!s.player().alive||!target.alive||target.hp<=0||!s.monsters().contains(target)||!F5mSaveStore.writable())return null;
  String defense=MonsterElementsV258.effectiveDefense(s,target);if(ElementalDamageFormula.index(defense)<0)return null;
  String current=s.rpg().equipment().get("목걸이");RpgProgressionState.ItemDefinition equipped=s.rpg().itemDefinitions().get(current);
  int best=score(equipped,defense);String selected=null;
  for(int i=0;i<4;i++){String id=bar.slot(i);RpgProgressionState.ItemDefinition d=s.rpg().itemDefinitions().get(id);
   if(d==null||!"목걸이".equals(d.equipSlot)||s.rpg().inventory().getOrDefault(id,0)<=0||s.rpg().currentRequirements(id)!=RpgProgressionState.RequirementResult.MET)continue;
   int value=score(d,defense);if(value>best){best=value;selected=id;}
  }
  return selected;
 }
 private static int score(RpgProgressionState.ItemDefinition d,String defense){return d==null||d.attackElement==null||ElementalDamageFormula.index(d.attackElement)<0?-1:ElementalDamageFormula.apply(18300,d.attackElement,defense);}
 private SenseNecklaceV259(){}
}
