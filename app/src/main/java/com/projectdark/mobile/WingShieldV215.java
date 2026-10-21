package com.projectdark.mobile;
final class WingShieldV215 {
 static void install(RpgProgressionState r){
  r.installVisibleAccessory("USER_ITEM_20261018_R00813","날개","mg261");
  r.installVisibleAccessory("IT_NATIVE_V210_84BD5A9CA00577FD","날개","mg332");

  r.installVisibleAccessory("USER_ITEM_20261018_R00774","날개","mg198");
  r.installVisibleAccessory("USER_ITEM_20261018_R00689","날개","mg087");
  r.installVisibleAccessory("USER_ITEM_20261018_R00688","날개","mg285");
  r.installVisibleAccessory("USER_ITEM_20261018_R00687","날개","mg252");
  r.installVisibleAccessory("USER_ITEM_20261018_R00685","날개","mg253");
  r.installVisibleAccessory("USER_ITEM_20261018_R00678","날개","mg266");
  r.installVisibleAccessory("IT_NATIVE_V210_1ABBEF8A45CF9935","날개","mg142");
  r.installVisibleAccessory("USER_ITEM_20261018_R03576","날개","mg090");
  r.installVisibleAccessory("USER_ITEM_20261018_R00472","날개","mg046");
  r.installVisibleAccessory("USER_ITEM_20261018_R00456","날개","mg045");
  r.installVisibleAccessory("USER_ITEM_20261018_R00453","날개","mg239");
  r.installVisibleAccessory("USER_ITEM_20261018_R00372","날개","mg047");
  r.installVisibleAccessory("USER_ITEM_20261018_R00206","날개","mg165");
  r.installVisibleAccessory("USER_ITEM_20261018_R00146","날개","mg216");
  r.installVisibleAccessory("USER_ITEM_20261018_R00055","날개","mg164");
  r.installVisibleAccessory("IT_NATIVE_V210_3F2BEB27CE663113","날개","wg142");
  r.installVisibleAccessory("IT_SHIELD_WOOD","방패","ms001");
  r.installVisibleAccessory("IT_SHIELD_SILVER","방패","ms002");
  r.installVisibleAccessory("IT_SHIELD_GOLD","방패","ms003");
  r.installVisibleAccessory("IT_SHIELD_PLANUM","방패","ms004");
  r.installVisibleAccessory("IT_SHIELD_LEATHER","방패","ms005");
  r.installVisibleAccessory("IT_SHIELD_COPPER","방패","ms006");
  r.installVisibleAccessory("IT_SHIELD_IRON","방패","ms007");
  r.installVisibleAccessory("IT_SHIELD_KNIGHT","방패","ms008");
  r.installVisibleAccessory("IT_SHIELD_SKULL","방패","ms009");
  r.installVisibleAccessory("IT_SHIELD_EXORCISM","방패","ms010");
  r.installVisibleAccessory("IT_SHIELD_BROKEN_PHOBOS","방패","ms014");
  r.installVisibleAccessory("IT_SHIELD_PHOBOS","방패","ms015");
  r.installVisibleAccessory("IT_SHIELD_DEATHKNIGHT","방패","ms016");
  r.installVisibleAccessory("IT_SHIELD_PAPAYA","방패","ms018");
 }
 static final java.util.Map<String,String> FRONTS=new java.util.HashMap<>();static {
  FRONTS.put("mg261","mc261");
  FRONTS.put("mg332","mc332");

  FRONTS.put("mg045","mc045");
  FRONTS.put("mg046","mc046");
  FRONTS.put("mg047","mc047");
  FRONTS.put("mg087","mc087");
  FRONTS.put("mg090","mc090");
  FRONTS.put("mg142","mc142");
  FRONTS.put("mg164","mc164");
  FRONTS.put("mg165","mc165");
  FRONTS.put("mg198","mc198");
  FRONTS.put("mg216","mc216");
  FRONTS.put("mg239","mc239");
  FRONTS.put("mg252","mc252");
  FRONTS.put("mg253","mc253");
  FRONTS.put("mg266","mc266");
  FRONTS.put("mg285","mc285");
  FRONTS.put("wg090","wc090");
  FRONTS.put("wg142","wc142");
}
 static boolean wing(String id){return id!=null&&(id.startsWith("mg")||id.startsWith("wg"));}
 static String front(String id){return FRONTS.get(id);}
 static boolean layer(String id){return wing(id)||FRONTS.containsValue(id);}
 static boolean inFront(String id,CharacterRenderer.Direction d){return FRONTS.containsValue(id)||(!FRONTS.containsKey(id)&&(d==CharacterRenderer.Direction.NW||d==CharacterRenderer.Direction.NE));}
 static int actionFrame(String id,int bodyFrame){return "mg332".equals(id)||"mc332".equals(id)?bodyFrame*4:bodyFrame;}
 private WingShieldV215(){}
}
