package com.projectdark.mobile;

/** User decisions. Retired IDs remain readable for transactional old-save migration. */
final class SkillPolicyV227 {
  static final String HOLY_BLOW="SK_성직자_020",SEMELIA="SK_마법사_034",RAGNAROK="SK_마법사_037";
  static boolean retired(String id){return "SK_마법사_038".equals(id)||HOLY_BLOW.equals(id)||"SK_도적_031".equals(id)||"SK_마법사_057".equals(id)||"SK_마법사_054".equals(id)||"SK_성직자_059".equals(id)||"SK_마법사_022".equals(id);}
  static String effectSource(String id){
    if("SK_마법사_023".equals(id))return "SK_무도가_010";
    if("SK_마법사_025".equals(id))return "SK_무도가_018";
    if(SEMELIA.equals(id))return RAGNAROK;
    // V243 caster-cross attacks reuse the corresponding circle's single recipient FX.
    switch(id){case "SK_마법사_007":return "SK_마법사_001";case "SK_마법사_008":return "SK_마법사_003";case "SK_마법사_009":return "SK_마법사_004";case "SK_마법사_010":return "SK_마법사_005";case "SK_마법사_018":return "SK_마법사_013";case "SK_마법사_019":return "SK_마법사_014";case "SK_마법사_020":return "SK_마법사_015";case "SK_마법사_021":return "SK_마법사_016";default:break;}
    // User V235: each screen element casts its third-tier source on every recipient.
    switch(id){case "SK_마법사_030":return "SK_마법사_026";case "SK_마법사_031":return "SK_마법사_027";case "SK_마법사_032":return "SK_마법사_028";case "SK_마법사_033":return "SK_마법사_029";default:return id;}
  }
  private SkillPolicyV227(){}
}
