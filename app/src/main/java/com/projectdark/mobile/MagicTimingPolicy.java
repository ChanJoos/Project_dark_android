package com.projectdark.mobile;

/** V137 early-spell correction. Explicit project timings, never a claim of current server numbers. */
final class MagicTimingPolicy {
  static final float SPELL_INTERVAL = 1f / 3f;
  private MagicTimingPolicy() {}
  static boolean managed(String id) {
    SkillAbilityCatalog.Ability a = SkillAbilityCatalog.get(id);
    return a != null && ItemEffects.magic(id)
        && (a.name.equals("쿠로토") || a.job.equals("마법사") && a.circle <= 2);
  }
  static float baseCast(String id, float fallback) {
    SkillAbilityCatalog.Ability a = SkillAbilityCatalog.get(id);
    if (a == null) return fallback;
    if (a.name.equals("쿠로토")) return 0;
    // One-second elementary cast is ADAPTED pending a labelled original timing capture.
    switch (id) {
      case "SK_마법사_001": case "SK_마법사_003":
      case "SK_마법사_004": case "SK_마법사_005": return 1;
      default: return fallback;
    }
  }
}
