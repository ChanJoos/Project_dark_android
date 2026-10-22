package com.projectdark.mobile;

/** User decisions. Retired IDs remain readable for transactional old-save migration. */
final class SkillPolicyV227 {
  static final String HOLY_BLOW="SK_성직자_020",SEMELIA="SK_마법사_034",RAGNAROK="SK_마법사_037";
  static boolean retired(String id){return HOLY_BLOW.equals(id)||"SK_도적_031".equals(id);}
  static String effectSource(String id){
    if(SEMELIA.equals(id))return RAGNAROK;
    // User V235: each screen element casts its third-tier source on every recipient.
    switch(id){case "SK_마법사_030":return "SK_마법사_026";case "SK_마법사_031":return "SK_마법사_027";case "SK_마법사_032":return "SK_마법사_028";case "SK_마법사_033":return "SK_마법사_029";default:return id;}
  }
  private SkillPolicyV227(){}
}
