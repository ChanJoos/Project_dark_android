package com.projectdark.mobile;
import java.util.Map;
/** Read-only combat/stat projection. Base progression and equipment remain separate authorities. */
public final class FinalStats {
 public final int str,intel,wis,con,dex,maxHp,maxMp,ac,magicDefense,hit,dam,damageReductionPct,flatMitigation,acIgnore;
 public final String attackElement,defenseElement;
 private FinalStats(int s,int i,int w,int c,int d,int hp,int mp,int ac,int md,int hit,int dam,String ae,String de,int dr,int flat,int ignore){this.str=s;intel=i;wis=w;con=c;dex=d;maxHp=hp;maxMp=mp;this.ac=ac;magicDefense=md;this.hit=hit;this.dam=dam;attackElement=ae;defenseElement=de;damageReductionPct=dr;flatMitigation=flat;acIgnore=ignore;}
 public static FinalStats from(RpgProgressionState r){RpgProgressionState.StatSnapshot x=r.recomputeStats();Map<String,Integer> e=x.equipment;
   return new FinalStats(r.str()+v(e,"STR"),r.intel()+v(e,"INT"),r.wis()+v(e,"WIS"),r.con()+v(e,"CON"),r.dex()+v(e,"DEX"),
    r.baseMaxHp()+v(e,"HP"),r.baseMaxMp()+v(e,"MP"),v(e,"AC"),v(e,"MAGIC_DEFENSE"),v(e,"HIT"),v(e,"DAM"),r.attackElement(),r.defenseElement(),v(e,"DAMAGE_REDUCTION_PCT"),v(e,"FLAT_MITIGATION"),v(e,"AC_IGNORE"));
 }
 public static FinalStats neutral(int hp,int mp){return new FinalStats(0,0,0,0,0,hp,mp,0,0,0,0,"NONE","NONE",0,0,0);}
 public int prototypePhysicalAttack(){return Math.max(1,8+str*3+dam);}
 private static int v(Map<String,Integer> m,String k){Integer x=m.get(k);return x==null?0:x;}
}