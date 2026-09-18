package com.projectdark.mobile;
/** Single combat-v3 stat pipeline. Unknown original HIT/MDEF formulas remain fail-neutral until evidenced. */
public final class CombatStatPipeline {
 public enum Channel { PHYSICAL, MAGIC }
 public static final class Result {public final int raw,afterElement,applied;public final boolean resisted;Result(int r,int e,int a,boolean x){raw=r;afterElement=e;applied=a;resisted=x;}}
 private CombatStatPipeline(){}
 public static Result resolve(int actionBase,Channel channel,FinalStats attacker,FinalStats defender){
   int raw=Math.max(1,actionBase);
   if(channel==Channel.PHYSICAL&&attacker!=null)raw=Math.max(raw,attacker.prototypePhysicalAttack());
   int elemental=ElementalDamageFormula.apply(raw,attacker==null?"NONE":attacker.attackElement,defender==null?"NONE":defender.defenseElement);
   if(channel==Channel.PHYSICAL)return new Result(raw,elemental,CombatDefenseFormula.physical(elemental,attacker,defender),false);
   // MDEF exact probability/applicability is PENDING. Keep channel separate and neutral rather than invent a roll.
   int v=defender==null?elemental:Math.max(1,(int)Math.round(elemental*Math.max(0,100-defender.damageReductionPct)/100d)-defender.flatMitigation);
   return new Result(raw,elemental,v,false);
 }
}