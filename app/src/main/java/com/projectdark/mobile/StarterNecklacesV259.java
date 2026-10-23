package com.projectdark.mobile;
/** Explicit user starter kit; source item levels/stats remain unchanged. */
final class StarterNecklacesV259 {
 static final String FIRE="IT_SOURCE_197_HICRYSTAL_A94FCE06",EARTH="IT_SOURCE_197_TICRYSTAL_A90E7D50",WIND="IT_SOURCE_197_KAZECRYSTAL_2C35EA1B",WATER="IT_SOURCE_197_UMICRYSTAL_21B5EDA7";
 static String id(String value){return RpgProgressionState.canonicalItemId(value);}
 static boolean allowed(String value){return value!=null&&(value.equals(id(FIRE))||value.equals(id(EARTH))||value.equals(id(WIND))||value.equals(id(WATER)));}
 static boolean seed(RuntimeState s,SkillBook book,QuickItemBarV254 bar){
  if(!F5mSaveStore.writable())return false;
  boolean ok=F5mSaveStore.transactActive(s.rpg(),book,()->{
   for(String alias:new String[]{FIRE,EARTH,WIND,WATER}){String item=id(alias);if(s.rpg().inventory().getOrDefault(item,0)<=0&&s.rpg().autoLootResolvedItem(item,1)!=RpgProgressionState.AutoLootResult.LOOTED)return false;}
   String water=id(WATER);return water.equals(s.rpg().equipment().get("목걸이"))||s.rpg().equipToSlot(water,"목걸이")==RpgProgressionState.EquipResult.EQUIPPED;
  });
  if(ok)ok=bar.seedFresh(new String[]{id(FIRE),id(EARTH),id(WIND),null},s.rpg());s.applyDerivedGrowth();return ok;
 }
 private StarterNecklacesV259(){}
}
