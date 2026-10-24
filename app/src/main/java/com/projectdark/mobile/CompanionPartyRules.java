package com.projectdark.mobile;

/**
 * Explicit PvE skill choices; no transfusion, priest curse, blanket dispel, or invented resources.
 */
final class CompanionPartyRules {
  static boolean allySupport(String id) {
    SkillAbilityCatalog.Ability a = SkillAbilityCatalog.get(id);
    if (a == null) return false;
    return a.job.equals("성직자")
        && (a.heal()
            || a.status.startsWith("CLEAR_") && !a.status.equals("CLEAR_ALL")
            || a.status.startsWith("ARMOR")
            || a.status.startsWith("HIT")
            || a.status.startsWith("DAM")
            || a.status.equals("PROTECT")
            || a.status.equals("REGEN")
            || a.status.equals("BLESS_ALL"));
  }

  static String heal(CompanionParty.Member healer, RuntimeState.Player target) {
    int missing = target.maxHp - target.hp;
    if (missing <= 0) return null;
    String chosen = null;
    int best = Integer.MAX_VALUE, biggest = 0;
    String largest = null;
    for (SkillAbilityCatalog.Ability a : SkillAbilityCatalog.all())
      if (a.heal()
          && a.job.equals("성직자")
          && healer.learned(a.id)
          && healer.body.mp >= ItemEffects.mpCost(healer.growth, a.id, a.mpCost)
          && healer.cooldown(a.id) <= 0) {
        SkillActionContract.Rule r = SkillActionContract.get(a.id);
        if (r == null || r.pattern != SkillActionContract.Pattern.ALLY) continue;
        int n =
            SkillAbilityCatalog.amount(
                a,
                new SkillAbilityCatalog.Snapshot(
                    healer.stats(), healer.body.hp, healer.body.mp, healer.level, 100, false));
        if (n > biggest) {
          biggest = n;
          largest = a.id;
        }
        if (n >= missing && a.mpCost < best) {
          chosen = a.id;
          best = a.mpCost;
        }
      }
    return chosen != null ? chosen : largest;
  }

  static String dispel(CompanionParty.Member healer, SkillEffectState effects, String target) {
    for (String kind : new String[] {"FREEZE", "SLEEP", "POISON", "CURSE", "BLIND", "ROOT"})
      if (effects.has(target, kind)) {
        SkillAbilityCatalog.Ability best = null;
        for (SkillAbilityCatalog.Ability a : SkillAbilityCatalog.all())
          if (healer.learned(a.id)
              && a.status.startsWith("CLEAR_")
              && !a.status.equals("CLEAR_ALL")
              && healer.body.mp >= ItemEffects.mpCost(healer.growth, a.id, a.mpCost)
              && healer.cooldown(a.id) <= 0) {
            String clears = a.status.substring(6);
            boolean valid =
                kind.equals("CURSE")
                    ? clears.startsWith("CURSE")
                        && Integer.parseInt(clears.substring(5)) >= effects.power(target, kind)
                    : clears.equals(kind);
            if (valid && (best == null || a.mpCost < best.mpCost)) best = a;
          }
        if (best != null) return best.id;
      }
    return null;
  }

  static String protection(CompanionParty.Member healer, SkillEffectState effects, String target) {
    for (String id :
        new String[] {
          "SK_성직자_028",
          "SK_성직자_018",
          "SK_성직자_036",
          "SK_성직자_019",
          "SK_성직자_004",
          "SK_성직자_035",
          "SK_성직자_009"
        }) {
      SkillAbilityCatalog.Ability a = SkillAbilityCatalog.get(id);
      if (!healer.learned(id)
          || a == null
          || healer.body.mp < ItemEffects.mpCost(healer.growth, a.id, a.mpCost)
          || healer.cooldown(id) > 0) continue;
      String family =
          a.status.startsWith("ARMOR")
              ? "ARMOR"
              : a.status.startsWith("HIT") ? "HIT" : a.status.startsWith("DAM") ? "DAM" : a.status;
      int power =
          a.status.startsWith("ARMOR")
              ? 10
              : a.status.startsWith("HIT")
                  ? Integer.parseInt(a.status.substring(3))
                  : a.status.startsWith("DAM") ? Integer.parseInt(a.status.substring(3)) : 30;
      if (effects.power(target, family) < power) return id;
    }
    return null;
  }
}
