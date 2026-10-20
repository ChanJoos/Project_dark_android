package com.projectdark.mobile;

/** Whole-catalog Spell cadence; original unknown timings stay explicit adaptations. */
final class MagicTimingPolicy {
  static final float SPELL_INTERVAL = 1f / 3f;
  private MagicTimingPolicy() {}
  static boolean managed(String id) {
    SkillAbilityCatalog.Ability a = SkillAbilityCatalog.get(id);
    return a != null && ItemEffects.magic(id);
  }
  static float baseCast(String id, float fallback) {
    SkillAbilityCatalog.Ability a = SkillAbilityCatalog.get(id);
    if (a == null) return fallback;
    if (a.name.equals("쿠로토") || id.equals("SK_무도가_016")) return 0;
    if (id.equals("SK_성직자_055") || id.equals("SK_공통_004")) return 5;
    // One-second elementary cast is ADAPTED pending a labelled original timing capture.
    switch (id) {
      case "SK_마법사_043": return 2; // User original-client screenshot1000060433.
      case "SK_마법사_001": case "SK_마법사_003":
      case "SK_마법사_004": case "SK_마법사_005": return 1;
      default: return fallback;
    }
  }
}
