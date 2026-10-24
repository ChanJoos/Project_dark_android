package com.projectdark.mobile;
import java.util.*;
/** User stats preserved including penalties; original source wearable associations are recorded separately. */
final class MineHuntEquipment {
static void install(RpgProgressionState r){
r.registerSourceAccessory(new RpgProgressionState.ItemDefinition("USER_ITEM_20261018_R01486","헬씨프아머","갑옷","mu0000037",null,99,Collections.<String>emptySet(),false,null,null,stats("AC",-57,"HIT",10,"DAM",1,"HP",-900,"MP",0,"STR",0,"DEX",-2,"INT",0,"WIS",0,"CON",0),RpgProgressionState.Evidence.U));
r.registerSourceAccessory(new RpgProgressionState.ItemDefinition("USER_ITEM_20261018_R01482","헬프리스트로브","갑옷","mu0000039",null,99,Collections.<String>emptySet(),false,null,null,stats("AC",-51,"HIT",0,"DAM",0,"HP",0,"MP",-500,"STR",0,"DEX",0,"INT",0,"WIS",-2,"CON",0),RpgProgressionState.Evidence.U));
}
private static Map<String,Integer> stats(Object... a){Map<String,Integer> m=new LinkedHashMap<>();for(int i=0;i<a.length;i+=2)m.put((String)a[i],(Integer)a[i+1]);return m;}
}
