package com.projectdark.mobile;

/**
 * Self dispel/heal, protected resource finishers, freeze strike, shape-aware martial combinations.
 */
final class MonkCompanionAgent implements CompanionAgent {
  @Override
  public void defend(CompanionAgentContext c) {
    decide(c, c.attacker());
  }

  public void decide(CompanionAgentContext c, RuntimeState.Monster target) {
    if (c.has("POISON") && c.self("SK_무도가_031")) return;
    if (c.has("BLIND") && c.self("SK_무도가_030")) return;
    if (c.selfHeal()) return;
    if (target == null) {
      c.follow();
      return;
    }
    if (c.health() < .3f && c.buff("SK_무도가_016", "INVINCIBLE")) return;
    if (!c.safeTarget(target)) {
      c.member.notice = "연계 대기";
      return;
    }
    c.face(target);
    if (!c.melee(target)) {
      if (c.attack("SK_무도가_009", target, 1)) return;
      c.approach(target, 1);
      return;
    }
    if (c.state.skillEffects().power(c.member.id, "ARMOR") < 10 && c.self("SK_무도가_013")) return;
    if (c.buff("SK_무도가_029", "PROTECT")) return;
    if (c.health() < .85f && c.buff("SK_무도가_012", "REGEN")) return;
    if (c.health() > .8f
        && c.member.body.mp >= 1440
        && target.hp > c.member.stats().prototypePhysicalAttack() * 20
        && c.ready("SK_무도가_020")) {
      if (!c.has("INVINCIBLE") && c.self("SK_무도가_016")) return;
      if (c.has("INVINCIBLE") && c.attack("SK_무도가_020", target, 1)) return;
    }
    for (String id : new String[] {"SK_무도가_025", "SK_무도가_026", "SK_무도가_014"})
      if (c.attack(id, target, 2)) return;
    if (c.attacker() != null
        && !c.state.skillEffects().has(target.id, "FREEZE")
        && c.attack("SK_무도가_018", target, 1)) return;
    for (String id : new String[] {"SK_무도가_024", "SK_무도가_007", "SK_무도가_002", "SK_무도가_001"})
      if (c.attack(id, target, 1)) return;
    c.basic(target);
  }
}
