package com.projectdark.mobile;
import java.util.*;
/** Mobile solo 3-circle tuning. These are ADAPTED values, not original server statistics. */
public final class AbelCombatProfile {
 public final String id,element,spell;public final int level,hp,mp,damage,ac,mdef,exp;public final float cooldown,range;public final long gold;
 private AbelCombatProfile(String id,int lv,int hp,int damage,int ac,int md,String element,String spell,int exp,long gold,float cd){this.id=id;level=lv;this.hp=hp;this.damage=damage;this.ac=ac;mdef=md;this.element=element;this.spell=spell;this.exp=exp;this.gold=gold;cooldown=cd;range=spell.isEmpty()?0:spell.equals("MONSTER_VENOM_BITE")?48:144;mp=spell.isEmpty()?0:240;}
 private static final Map<String,AbelCombatProfile> ALL=new LinkedHashMap<>();
 private static void add(String id,int lv,int hp,int damage,int ac,int md,String element,String spell,int exp,long gold,float cd){ALL.put(id,new AbelCombatProfile(id,lv,hp,damage,ac,md,element,spell,exp,gold,cd));}
 static {
  add("ABEL_TURTLE",42,1000,12,-8,4,"EARTH","",45000,135,2.2f);
  add("ABEL_ARKPUS",44,1100,14,-3,8,"WATER","MONSTER_WATER",48000,165,2f);
  add("ABEL_PONG",43,850,10,0,5,"WIND","MONSTER_BLIND",43000,140,1.8f);
  add("ABEL_CATERPILLAR",42,900,11,-2,2,"EARTH","MONSTER_VENOM_BITE",44000,145,2f);
  add("ABEL_GRAB",49,1600,18,-15,5,"EARTH","",52000,155,2.3f);
  add("ABEL_OBS_SLATE",48,1300,15,-5,8,"EARTH","MONSTER_ROOT",50000,150,2f);
  add("ABEL_OCTOPUS",55,1900,21,-7,10,"WATER","MONSTER_WATER",58000,170,2f);
  add("ABEL_OBS_PURPLE_EGG",54,1400,17,-3,12,"NONE","MONSTER_CURSE",55000,160,2f);
  add("ABEL_GAS",57,1600,17,-4,12,"FIRE","MONSTER_VENOM",60000,175,2f);
  add("ABEL_OBS_PURPLE_MAW",58,2100,23,-8,8,"WIND","MONSTER_FREEZE",62000,180,2.1f);
  add("ABEL_BERSERKER",65,4500,34,-12,12,"FIRE","MONSTER_FIRE",95000,190,2f);
  add("ABEL_KINGARK_FAKE",67,6000,32,-10,15,"NONE","MONSTER_FIRE",110000,220,2.2f);
  add("ABEL_KINGARK",70,8500,38,-15,18,"NONE","MONSTER_WATER",150000,300,2.4f);
 }
 public static AbelCombatProfile forId(String id){if(id==null)return null;int n=id.indexOf('#');return ALL.get(n<0?id:id.substring(0,n));}
 public static Collection<AbelCombatProfile> all(){return Collections.unmodifiableCollection(ALL.values());}
 public static boolean spell(String id){return id!=null&&ACTIONS.contains(id);}
 public static final List<String> ACTIONS=Collections.unmodifiableList(Arrays.asList("MONSTER_WATER","MONSTER_FIRE","MONSTER_BLIND","MONSTER_ROOT","MONSTER_CURSE","MONSTER_VENOM","MONSTER_VENOM_BITE","MONSTER_FREEZE","MONSTER_RECOVER"));
 public static CombatResolver.Definition definition(String action){return new CombatResolver.Definition(action,CombatResolver.ActionKind.MAGIC,CombatResolver.ActionState.MAGIC,CombatResolver.EffectType.MAGIC_HIT,false,8,7,144,.65f,24);}
 public CombatResolver.Definition resolve(CombatResolver.Definition d){boolean magic=spell(d.actionId),heal=d.actionId.equals("MONSTER_RECOVER");return new CombatResolver.Definition(d.actionId,d.kind,d.state,d.effectType,false,magic?heal?16:8:0,magic?7:cooldown,heal?1:magic?range:48,heal?1.1f:magic?.65f:.24f,magic?damage+10:damage);}
 public FinalStats stats(){return FinalStats.monster(level,hp,mp,ac,mdef,damage,element);}
 public static String element(String action,AbelCombatProfile p){return action.equals("MONSTER_FIRE")?"FIRE":action.equals("MONSTER_WATER")?"WATER":action.equals("MONSTER_ROOT")?"EARTH":p.element;}
 private AbelCombatProfile(){throw new AssertionError();}
}
