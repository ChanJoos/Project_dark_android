package com.projectdark.mobile;

/** Taunt, blades and source-cost soul/crash; no manufactured low HP. */
final class WarriorCompanionAgent implements CompanionAgent {
  public void decide(CompanionAgentContext c, RuntimeState.Monster target) {
    CompanionParty.Member m = c.member;
    RuntimeState state = c.state;
    CompanionController.Navigation nav = c.nav;
    if (target == null) {
      c.follow();
      return;
    }
    if (state.party().holdAttack(m.id, target.id) || state.skillEffects().has(target.id, "SLEEP")) {
      // A sleeping target is reserved for the strongest available warrior hit, never a stray basic
      // attack.
      if (state.skillEffects().has(target.id, "SLEEP")
          && canBurst(c)
          && !state.party().holdAttack(m.id, target.id)) {
        if (c.use(target.id, burst(m))) {
          state.party().reserve(m.id, target.id, burst(m), .7f);
          return;
        }
      }
      m.notice = "연계 대기";
      return;
    }
    if (!CanonicalMeleeTileContract.reachable(m.body.x, m.body.y, target.x, target.y)
        || !nav.sight(m.body.x, m.body.y, target.x, target.y)) {
      m.notice = "접근";
      c.approach(target, 1);
      return;
    }
    m.facing = CanonicalActorFacing.quantize(target.x - m.body.x, target.y - m.body.y, m.facing);
    if ("player".equals(target.attackTarget)
        && m.body.hp > m.body.maxHp * .3f
        && c.use(target.id, "SK_전사_012")) return;
    if (m.body.hp <= m.body.maxHp * .02f && c.use(target.id, crash(m))) return;
    if (m.body.hp < m.body.maxHp * .25f) {
      m.notice = "회복 대기";
      return;
    }
    boolean support =
        state.skillEffects().has(m.id, "PROTECT")
            || state.skillEffects().has(m.id, "PHYSICAL_GUARD")
            || "CLERIC".equals(state.rpg().currentJobCode());
    if (target.hp > m.stats().prototypePhysicalAttack() * 5
        && support
        && m.body.hp > m.body.maxHp * .8f
        && c.use(target.id, soul(m))) return;
    for (String id : new String[] {"SK_전사_006", "SK_전사_003", "SK_전사_001"})
      if (c.use(target.id, id)) return;
    c.use(target.id, RuntimeCombatSession.playerAttackActionId(0));
  }

  private String crash(CompanionParty.Member m) {
    return m.learned("SK_전사_023") ? "SK_전사_023" : "SK_전사_015";
  }

  private String soul(CompanionParty.Member m) {
    return m.learned("SK_전사_022") ? "SK_전사_022" : "SK_전사_013";
  }

  private String burst(CompanionParty.Member m) {
    return m.body.hp <= m.body.maxHp * .02f ? crash(m) : soul(m);
  }

  private boolean canBurst(CompanionAgentContext c) {
    CompanionParty.Member m = c.member;
    RuntimeState state = c.state;
    String id = burst(m);
    return m.learned(id)
        && m.cooldown(id) <= 0
        && m.body.hp > 1
        && (SkillAbilityCatalog.get(id).formula.equals("CRASH")
            || m.body.hp > m.body.maxHp * .8f
                && ("CLERIC".equals(state.rpg().currentJobCode())
                    || state.skillEffects().has(m.id, "PROTECT")));
  }
}
