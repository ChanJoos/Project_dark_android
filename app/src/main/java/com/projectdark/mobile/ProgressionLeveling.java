package com.projectdark.mobile;
/** Canonical Level_EXP_Curve projection. State mutation is owned by RpgProgressionState. */
public final class ProgressionLeveling{
 private ProgressionLeveling(){}
 public static int normalize(RpgProgressionState r){return r==null?0:r.normalizeCanonicalLevel();}
 public static void restore(RpgProgressionState r,int level,long exp){if(r!=null)r.restoreProgression(level,exp);}
}
