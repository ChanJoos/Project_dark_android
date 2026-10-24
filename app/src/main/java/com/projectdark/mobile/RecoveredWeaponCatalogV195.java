package com.projectdark.mobile;
import java.util.*;
/**6 exact named source weapons, sparse frame omissions retained. Historical article conflicts retain current U values. */
final class RecoveredWeaponCatalogV195 {
 static void install(RpgProgressionState r){
  r.registerSourceAccessory(new RpgProgressionState.ItemDefinition("USER_ITEM_20261018_R05262","단검","무기","mw006",AnimationAction.SWING,1,Collections.<String>emptySet(),false,null,null,stats("AC",0,"HIT",0,"DAM",0,"HP",0,"MP",0,"STR",0,"DEX",0,"INT",0,"WIS",0,"CON",0,"REGEN",0,"MAGIC_DEFENSE",0,"MinATK",5,"MaxATK",15,"LMinATK",1,"LMaxATK",2),RpgProgressionState.Evidence.U));
  r.registerSourceAccessory(new RpgProgressionState.ItemDefinition("USER_ITEM_20261018_R01478","혈단검","무기","mw032",AnimationAction.SWING,99,Collections.<String>emptySet(),false,null,null,stats("AC",0,"HIT",0,"DAM",0,"HP",0,"MP",0,"STR",0,"DEX",0,"INT",0,"WIS",0,"CON",0,"REGEN",0,"MAGIC_DEFENSE",0,"MinATK",95,"MaxATK",125,"LMinATK",9,"LMaxATK",12),RpgProgressionState.Evidence.U));
  r.registerSourceAccessory(new RpgProgressionState.ItemDefinition("USER_ITEM_20261018_R03682","비설표","무기","mw028",AnimationAction.SWING,1,Collections.<String>emptySet(),false,null,null,stats("AC",0,"HIT",0,"DAM",0,"HP",0,"MP",0,"STR",0,"DEX",0,"INT",0,"WIS",0,"CON",0,"REGEN",0,"MAGIC_DEFENSE",0,"MinATK",10,"MaxATK",20,"LMinATK",1,"LMaxATK",2),RpgProgressionState.Evidence.U));
  r.registerSourceAccessory(new RpgProgressionState.ItemDefinition("USER_ITEM_20261018_R05484","골드베리하프","무기","mw136",AnimationAction.SWING,99,Collections.<String>emptySet(),false,null,null,stats("AC",1,"HIT",0,"DAM",0,"HP",0,"MP",500,"STR",2,"DEX",0,"INT",-1,"WIS",1,"CON",0,"REGEN",0,"MAGIC_DEFENSE",2,"MinATK",24,"MaxATK",32,"LMinATK",86,"LMaxATK",100),RpgProgressionState.Evidence.U));
  r.registerSourceAccessory(new RpgProgressionState.ItemDefinition("USER_ITEM_20261018_R03653","비파","무기","mw173",AnimationAction.SWING,99,Collections.<String>emptySet(),false,null,null,stats("AC",2,"HIT",3,"DAM",3,"HP",1000,"MP",3000,"STR",2,"DEX",0,"INT",-2,"WIS",-2,"CON",0,"REGEN",0,"MAGIC_DEFENSE",2,"MinATK",44,"MaxATK",60,"LMinATK",100,"LMaxATK",110),RpgProgressionState.Evidence.U));
  r.registerSourceAccessory(new RpgProgressionState.ItemDefinition("USER_ITEM_20261018_R01435","홀리가디언스태프","무기","mw188",AnimationAction.SWING,41,Collections.<String>emptySet(),false,null,null,stats("AC",0,"HIT",0,"DAM",0,"HP",0,"MP",0,"STR",0,"DEX",0,"INT",0,"WIS",0,"CON",0,"REGEN",0,"MAGIC_DEFENSE",0,"MinATK",4,"MaxATK",8,"LMinATK",6,"LMaxATK",8),RpgProgressionState.Evidence.U));
 }
 static final String[] IDS={"USER_ITEM_20261018_R05262","USER_ITEM_20261018_R01478","USER_ITEM_20261018_R03682","USER_ITEM_20261018_R05484","USER_ITEM_20261018_R03653","USER_ITEM_20261018_R01435"};
 private static Map<String,Integer> stats(Object... pairs){Map<String,Integer> out=new LinkedHashMap<>();for(int i=0;i<pairs.length;i+=2)out.put((String)pairs[i],(Integer)pairs[i+1]);return out;}
}
