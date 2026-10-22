package com.projectdark.mobile;

/** User decisions. Retired IDs remain readable for transactional old-save migration. */
final class SkillPolicyV227 {
  static final String HOLY_BLOW="SK_성직자_020",SEMELIA="SK_마법사_034",RAGNAROK="SK_마법사_037";
  static boolean retired(String id){return HOLY_BLOW.equals(id)||"SK_도적_031".equals(id)||"SK_마법사_057".equals(id);}
  static String effectSource(String id){
    if(SEMELIA.equals(id))return RAGNAROK;
    // User correction V240: circle4 screen attacks reuse circle3 single-element effects.
    switch(id){case "SK_마법사_030":return "SK_마법사_013";case "SK_마법사_031":return "SK_마법사_014";case "SK_마법사_032":return "SK_마법사_015";case "SK_마법사_033":return "SK_마법사_016";default:return id;}
  }
  private SkillPolicyV227(){}
}
