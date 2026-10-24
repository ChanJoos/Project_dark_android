package com.projectdark.mobile;

/** Hide -> ambush, defense under threat, ranged shuriken on approach; preserve HP for finishers. */
final class RogueCompanionAgent implements CompanionAgent {
  public void decide(CompanionAgentContext c, RuntimeState.Monster target) {
    if (target == null) {
      c.follow();
      return;
    }
    if (!c.safeTarget(target)) {
      c.member.notice = "연계 대기";
      return;
    }
    if (c.health() < .3f) {
      if (c.attacker() != null && c.buff("SK_도적_011", "STEALTH")) return;
      if (c.attacker() != null && c.buff("SK_도적_013", "EVASION")) return;
      c.retreat(target);
      return;
    }
    c.face(target);
    if (!c.melee(target)) {
      if (c.attack("SK_도적_003", target, 1)) return;
      c.approach(target, 1);
      return;
    }
    if (c.has("STEALTH") || c.state.skillEffects().has(target.id, "AGGRO_RESET")) {
      for (String id : new String[] {"SK_도적_023", "SK_도적_028", "SK_도적_029", "SK_도적_015"})
        if (c.attack(id, target, 1)) return;
    } else if (c.ready("SK_도적_023") || c.ready("SK_도적_015")) {
      if (c.member.cooldown("agent_ambush_setup") <= 0 && c.buff("SK_도적_011", "STEALTH")) {
        c.member.cooldown("agent_ambush_setup", 8);
        return;
      }
    }
    if (c.attacker() != null && c.buff("SK_도적_013", "EVASION")) return;
    if (c.health() > .8f
        && c.has("PROTECT")
        && target.hp > c.member.stats().prototypePhysicalAttack() * 12)
      for (String id : new String[] {"SK_도적_027", "SK_도적_020"}) if (c.attack(id, target, 1)) return;
    for (String id : new String[] {"SK_도적_022", "SK_도적_021", "SK_도적_014", "SK_도적_007", "SK_도적_001"})
      if (c.attack(id, target, 1)) return;
    c.basic(target);
  }
}
