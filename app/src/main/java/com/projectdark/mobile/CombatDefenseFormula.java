package com.projectdark.mobile;
/** Evidence-gated defense pipeline. AC curve is [V-CANDIDATE]; remaining ordering is [B] and replaceable. */
public final class CombatDefenseFormula {
 private CombatDefenseFormula(){}
 public static int physical(int raw,FinalStats attacker,FinalStats defender){
   if(raw<=0)return 0;int effectiveAc=defender.ac+Math.max(0,attacker==null?0:attacker.acIgnore);
   double mult=effectiveAc>=-98?(100d+effectiveAc)/100d:.02d*Math.pow(.99d,-98-effectiveAc);
   double v=raw*Math.max(0d,mult);v*=Math.max(0,100-defender.damageReductionPct)/100d;v-=defender.flatMitigation;return Math.max(1,(int)Math.round(v));
 }
}