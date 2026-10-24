package com.projectdark.mobile;import java.util.*;
/** Playable provisional profile, not canonical original maximumHP/unsensed stats. */
public final class RucesionCombatProfiles {
 private static final Map<String,AbelCombatProfile> ALL=new LinkedHashMap<>();
 private static void add(String id,int level,int hp,int damage,String element,String spell,int exp,long gold){ALL.put(id,AbelCombatProfile.regional(id,level,hp,damage,-12,12,element,spell,exp,gold,2.2f));}
 static {
  add("LYK_BLUE_HACON",82,20163,42,"FIRE","MONSTER_FIRE",196250,240);
  add("LYK_RED_HACON",84,22000,44,"FIRE","",200000,250);
  add("LYK_ARKPUS",75,16000,35,"WATER","MONSTER_WATER",170000,210);
  add("LYK_PLANT_ARKPUS",80,18000,38,"EARTH","MONSTER_ROOT",185000,220);
  add("LYK_KAKAPO",90,23000,46,"EARTH","",215000,270);
  add("LYK_POISON_KAKAPO",92,24000,45,"EARTH","MONSTER_VENOM",230000,280);
  add("LYK_KOATL",98,22000,50,"WATER","",240000,300);
  add("LYK_ELECT_KOATL",98,22345,52,"WATER","MONSTER_ROOT",248570,310);
  add("LYK_TYRANT",98,87801,72,"WATER","MONSTER_WATER",1280575,500);
  // Explicit test-only encounter: no invented original party ritual/rewards.
  add("LYK_ARIEL",99,120000,84,"WATER","MONSTER_WATER",0,0);
 }
 public static AbelCombatProfile forId(String id){return ALL.get(PoteForestMonsterShowcase.species(id));}public static Collection<AbelCombatProfile> all(){return Collections.unmodifiableCollection(ALL.values());}private RucesionCombatProfiles(){}
}
