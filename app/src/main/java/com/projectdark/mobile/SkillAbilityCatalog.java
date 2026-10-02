package com.projectdark.mobile;
import java.util.*;
/** V83 abilities: preserved classic anchors and explicitly authored circle/stat balance. */
public final class SkillAbilityCatalog {
 public static final class Ability {
  public final String id,name,job,kind,formula,status,evidence,description; public final int circle,mpCost;public final float cooldown,duration;public final double coefficient;public final boolean allMp;
  Ability(String[] r){id=r[0];name=r[1];job=r[2];circle=Integer.parseInt(r[3]);kind=r[4];formula=r[5];coefficient=Double.parseDouble(r[6]);status=r[7];mpCost=Integer.parseInt(r[8]);cooldown=Float.parseFloat(r[9]);duration=Float.parseFloat(r[10]);allMp=Boolean.parseBoolean(r[11]);evidence=r[12];description=r[13];}
  public boolean damage(){return kind.equals("DAMAGE");}public boolean heal(){return kind.equals("HEAL");}public boolean supported(){return !kind.equals("SERVICE");}
  public boolean fixed(){return formula.equals("DALMA");}
  public boolean resourceBurst(){return allMp||Arrays.asList("DARA","CRASH","SOUL","ASSASSIN","ASSASSIN_PLUS","DALMA","HP_BURST","MP_BURST").contains(formula);}
 }
 public static final class Snapshot {
  public final FinalStats stats;public final int hp,mp,level,proficiency;public final boolean critical;
  public Snapshot(FinalStats s,int hp,int mp,int lv,int proficiency,boolean critical){stats=s;this.hp=hp;this.mp=mp;level=lv;this.proficiency=proficiency;this.critical=critical;}
 }
 private static final Map<String,Ability> ALL=new LinkedHashMap<>();
 static{for(String[] r:SkillAbilityData.ROWS){Ability a=new Ability(r);if(ALL.put(a.id,a)!=null)throw new IllegalStateException(a.id);}}
 public static Ability get(String id){return ALL.get(id);}public static Collection<Ability> all(){return Collections.unmodifiableCollection(ALL.values());}
 /** Input resources are captured BEFORE charging, once for all recipients. No global attack floor. */
 public static int amount(Ability a,Snapshot x){
  FinalStats s=x.stats;double raw;
  switch(a.formula){
   case "BASIC":raw=s.prototypePhysicalAttack();break;
   case "PHYSICAL":raw=(8+3d*s.str+s.dam)*a.coefficient;break;
   case "ROGUE":raw=(8+1.8*s.str+2.2*s.dex+s.dam)*a.coefficient;break;
   case "MARTIAL":raw=(8+4*Math.sqrt(Math.max(0,(double)s.str*s.con))+s.dam)*a.coefficient;break;
   case "MAGIC":raw=(8+4d*s.intel+s.wis+1.5*x.level)*a.coefficient;break;
   case "HEAL":raw=(5d*s.wis+1.5*s.intel+2d*x.level)*a.coefficient;break;
   case "CRASH":raw=s.maxHp*a.coefficient;break;
   case "SOUL":case "HP_BURST":case "DALMA":raw=x.hp*a.coefficient;break;
   case "ASSASSIN":raw=x.hp*(x.critical?1.69:a.coefficient);break;
   case "ASSASSIN_PLUS":raw=x.hp*(x.critical?3.5:a.coefficient);break;
   case "SEMELIA":raw=Math.max(0,x.mp-3240)*a.coefficient;break;
   case "METEOR":raw=Math.max(0,x.mp-12960)*a.coefficient;break;
   case "RAGNAROK":case "MP_BURST":raw=x.mp*a.coefficient;break;
   case "DARA":raw=Math.max(0,(double)x.hp+x.mp-1440)*a.coefficient;break;
   default:return 0;
  }
  // Supplied finishers preserve their exact base coefficients; learned ordinary attacks improve with practice.
  if(!a.resourceBurst())raw*=.8+.2*Math.max(0,Math.min(100,x.proficiency))/100d;
  return Math.max(0,(int)Math.min(Integer.MAX_VALUE,Math.round(raw)));
 }
 public static String resources(Ability a){if(a==null)return "";if(a.formula.equals("DARA"))return "HP·MP → 1";if(a.allMp)return "MP 전량";if(a.formula.equals("DALMA"))return "HP 60%";if(Arrays.asList("SOUL","HP_BURST","ASSASSIN","ASSASSIN_PLUS").contains(a.formula))return "HP 90%";if(a.formula.equals("CRASH"))return "HP 2% 이하";return "MP "+a.mpCost;}
}
