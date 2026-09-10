package com.projectdark.mobile;

import java.util.HashSet;
import java.util.Set;

/** Regression audit for the read-only 포테의숲 monster roster projection. */
public final class PoteMonsterRosterAudit {
  private PoteMonsterRosterAudit(){}

  public static boolean verify(){
    PoteMonsterRoster roster=new PoteMonsterRoster();
    if(roster.entries().size()!=16)return false;

    Set<String> names=new HashSet<>();
    for(PoteMonsterRoster.Entry entry:roster.entries().values()){
      if(!entry.hasStableIdentity())return false;
      if(!"포테의숲".equals(entry.region))return false;
      if(entry.sourceEvidence==null||entry.sourceEvidence.isEmpty())return false;
      if(entry.validationStatus==null||entry.validationStatus.isEmpty())return false;
      if(!names.add(entry.name))return false;

      // Only POTE_SPIRIT currently carries a fully resolved Lv/HP/EXP tuple in the projected rows.
      if(!"POTE_SPIRIT".equals(entry.monsterId)&&entry.hasResolvedCombatStats())return false;
    }

    PoteMonsterRoster.Entry spirit=roster.find("POTE_SPIRIT");
    if(spirit==null||!spirit.hasResolvedCombatStats())return false;
    if(spirit.level.intValue()!=48||spirit.hp.intValue()!=29201||spirit.exp.intValue()!=308950)return false;
    if(!"V".equals(spirit.sourceEvidence)||!"STAT_VERIFIED".equals(spirit.validationStatus))return false;

    // Guard against accidentally treating mixed evidence as fully stat-verified.
    PoteMonsterRoster.Entry red=roster.find("POTE_RED");
    if(red==null||red.level!=null||red.hp!=null||red.exp!=null)return false;
    if(!"V/FAN".equals(red.sourceEvidence))return false;

    return true;
  }
}
