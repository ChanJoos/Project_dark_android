package com.projectdark.mobile;

/** User decisions. Retired IDs remain readable for transactional old-save migration. */
final class SkillPolicyV227 {
  static final String HOLY_BLOW="SK_성직자_020",SEMELIA="SK_마법사_034",RAGNAROK="SK_마법사_037";
  static boolean retired(String id){return HOLY_BLOW.equals(id)||"SK_도적_031".equals(id);}
  static String effectSource(String id){return SEMELIA.equals(id)?RAGNAROK:id;}
  private SkillPolicyV227(){}
}
