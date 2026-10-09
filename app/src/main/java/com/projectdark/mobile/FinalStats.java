package com.projectdark.mobile;
import java.util.Map;
/** Read-only combat/stat projection. Base progression and equipment remain separate authorities. */
public final class FinalStats {
 public final int str,intel,wis,con,dex,maxHp,maxMp,ac,magicDefense,hit,dam,damageReductionPct,flatMitigation,acIgnore;
 public final String attackElement,defenseElement;
 public final int weaponMinAttack,weaponMaxAttack;
 private FinalStats(int s,int i,int w,int c,int d,int hp,int mp,int ac,int md,int hit,int dam,String ae,String de,int dr,int flat,int ignore,int minAttack,int maxAttack){weaponMinAttack=Math.max(0,minAttack);weaponMaxAttack=Math.max(weaponMinAttack,maxAttack);this.str=Math.max(0,s);intel=Math.max(0,i);wis=Math.max(0,w);con=Math.max(0,c);dex=Math.max(0,d);maxHp=Math.max(1,hp);maxMp=Math.max(0,mp);this.ac=ac;magicDefense=md;this.hit=hit;this.dam=dam;attackElement=ae;defenseElement=de;damageReductionPct=dr;flatMitigation=flat;acIgnore=ignore;}
 public static FinalStats from(RpgProgressionState r){RpgProgressionState.StatSnapshot x=r.recomputeStats();Map<String,Integer> e=x.equipment;
   return new FinalStats(r.str()+v(e,"STR"),r.intel()+v(e,"INT"),r.wis()+v(e,"WIS"),r.con()+v(e,"CON"),r.dex()+v(e,"DEX"),
    r.baseMaxHp()+v(e,"HP"),r.baseMaxMp()+v(e,"MP"),v(e,"AC"),v(e,"MAGIC_DEFENSE"),v(e,"HIT"),v(e,"DAM"),r.attackElement(),r.defenseElement(),v(e,"DAMAGE_REDUCTION_PCT"),v(e,"FLAT_MITIGATION"),v(e,"AC_IGNORE"),v(e,"MinATK"),v(e,"MaxATK"));
 }
 public static FinalStats neutral(int hp,int mp){return new FinalStats(0,0,0,0,0,hp,mp,0,0,0,0,"NONE","NONE",0,0,0,0,0);}
 public FinalStats withEffects(SkillEffectState e,String actor){
  int mode=e.power(actor,"PHOENIX")+e.power(actor,"DRAGON");int curse=e.power(actor,"CURSE");
  return new FinalStats(str,intel,wis,con,dex,maxHp,maxMp,ac-e.power(actor,"ARMOR")+curse+e.power(actor,"ARMOR_BREAK"),magicDefense-Math.max(0,curse-20)/2,
   hit+e.power(actor,"HIT")+(mode>0?(mode==8?20:10):0),dam+e.power(actor,"DAM")+mode,
   attackElement,e.has(actor,"CHANGE_ELEMENT")?"FIRE":defenseElement,damageReductionPct,flatMitigation,acIgnore,weaponMinAttack,weaponMaxAttack);
 }
 public FinalStats withAttackElement(String element){return new FinalStats(str,intel,wis,con,dex,maxHp,maxMp,ac,magicDefense,hit,dam,element,defenseElement,damageReductionPct,flatMitigation,acIgnore,weaponMinAttack,weaponMaxAttack);}
 /** Deterministic midpoint is project combat policy, not a claimed original random formula. */
 public int weaponBaseAttack(){return weaponMaxAttack>0?(int)Math.round((weaponMinAttack+(double)weaponMaxAttack)/2):8;}
 public int prototypePhysicalAttack(){return Math.max(1,weaponBaseAttack()+str*3+dam);}
 private static int v(Map<String,Integer> m,String k){Integer x=m.get(k);return x==null?0:x;}
}