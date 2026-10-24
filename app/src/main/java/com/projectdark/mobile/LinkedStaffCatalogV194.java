package com.projectdark.mobile;
import java.util.*;
/**3 exact named staff weapons. Historical article conflicts retain current U values. */
final class LinkedStaffCatalogV194 {
 static void install(RpgProgressionState r){
  r.registerSourceAccessory(new RpgProgressionState.ItemDefinition("USER_ITEM_20261018_R01401","홀리완드","무기","mw162",AnimationAction.SWING,3,Collections.<String>emptySet(),false,null,null,stats("AC",0,"HIT",0,"DAM",0,"HP",0,"MP",0,"STR",0,"DEX",0,"INT",0,"WIS",0,"CON",0,"REGEN",0,"MAGIC_DEFENSE",0,"MinATK",83,"MaxATK",83,"LMinATK",5,"LMaxATK",13),RpgProgressionState.Evidence.U));
  r.registerSourceAccessory(new RpgProgressionState.ItemDefinition("USER_ITEM_20261018_R05275","다크디아나","무기","mw094",AnimationAction.SWING,99,Collections.<String>emptySet(),false,null,null,stats("AC",0,"HIT",0,"DAM",0,"HP",-200,"MP",800,"STR",3,"DEX",-1,"INT",-2,"WIS",-2,"CON",0,"REGEN",0,"MAGIC_DEFENSE",2,"MinATK",34,"MaxATK",50,"LMinATK",3,"LMaxATK",7),RpgProgressionState.Evidence.U));
  r.registerSourceAccessory(new RpgProgressionState.ItemDefinition("USER_ITEM_20261018_R03696","블로우웨이브","무기","mw276",AnimationAction.SWING,99,Collections.<String>emptySet(),false,null,null,stats("AC",-1,"HIT",0,"DAM",0,"HP",500,"MP",1300,"STR",3,"DEX",-1,"INT",-1,"WIS",-1,"CON",0,"REGEN",0,"MAGIC_DEFENSE",2,"MinATK",40,"MaxATK",56,"LMinATK",3,"LMaxATK",7),RpgProgressionState.Evidence.U));
 }
 static final String[] IDS={"USER_ITEM_20261018_R01401","USER_ITEM_20261018_R05275","USER_ITEM_20261018_R03696"};
 private static Map<String,Integer> stats(Object... pairs){Map<String,Integer> out=new LinkedHashMap<>();for(int i=0;i<pairs.length;i+=2)out.put((String)pairs[i],(Integer)pairs[i+1]);return out;}
}
