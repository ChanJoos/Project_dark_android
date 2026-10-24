package com.projectdark.mobile;

/**
 * Control adds, preserve casting distance/MP, then choose actual element and shape by predicted
 * yield.
 */
final class MageCompanionAgent implements CompanionAgent {
  @Override
  public void defend(CompanionAgentContext c) {
    decide(c, c.attacker());
  }

  public void decide(CompanionAgentContext c, RuntimeState.Monster target) {
    if (target == null) {
      c.follow();
      return;
    }
    RuntimeState.Monster attacker = c.attacker();
    if (attacker != null && !c.state.skillEffects().disabled(attacker.id)) {
      if (c.health() < .45f && c.use(attacker.id, "SK_마법사_025")) return;
      if (c.health() < .45f && c.buff("SK_마법사_045", "ARMOR")) return;
    }
    if (c.selfHeal()) return;
    // Sleep only an additional attacker, never the selected damage target or a reserved burst
    // target.
    for (RuntimeState.Monster add : c.state.monsters())
      if (c.engaged(add)
          && add != target
          && !c.state.skillEffects().disabled(add.id)
          && !c.state.party().holdAttack(c.member.id, add.id)
          && Math.hypot(add.x - c.state.player().x, add.y - c.state.player().y) < 96
          && c.use(add.id, "SK_마법사_017")) return;
    if (!c.safeTarget(target)) {
      c.member.notice = "연계 대기";
      return;
    }
    if (attacker != null
        && c.melee(attacker)
        && c.member.cooldown("agent_retreat") <= 0
        && c.retreat(attacker)) {
      c.member.cooldown("agent_retreat", 1);
      return;
    }
    if (!c.nav.sight(c.member.body.x, c.member.body.y, target.x, target.y)) {
      c.approach(target, 3);
      return;
    }
    if (target.hp > c.member.stats().intel * 15) {
      for (String id :
          new String[] {"SK_마법사_046", "SK_마법사_053", "SK_마법사_024", "SK_마법사_011", "SK_마법사_002"}) {
        SkillAbilityCatalog.Ability a = SkillAbilityCatalog.get(id);
        if (c.ready(id)
            && c.state.skillEffects().power(target.id, "CURSE")
                < Integer.parseInt(a.status.substring(5))
            && c.use(target.id, id)) return;
      }
    }
    String best = null;
    double score = -1;
    for (SkillAbilityCatalog.Ability a : SkillAbilityCatalog.all()) {
      if (!a.job.equals("마법사") || !a.damage() || !c.ready(a.id)) continue;
      SkillActionContract.Rule r = SkillActionContract.get(a.id);
      if (r == null || !r.presentationAllowed()) continue;
      java.util.List<RuntimeState.Monster> victims = c.victims(a.id, target);
      if (victims.isEmpty() || !victims.contains(target)) continue;
      boolean safe = true;
      double yield = 0;
      for (RuntimeState.Monster m : victims) {
        if (!c.safeTarget(m) || !c.engaged(m) && m != target) {
          safe = false;
          break;
        }
        yield += Math.min(m.hp, c.damage(a, m));
      }
      if (!safe || r.pattern != SkillActionContract.Pattern.SINGLE && victims.size() < 2) continue;
      // All-MP finishers are reserved for durable targets with a healthy reserve and no immediate
      // attacker.
      if (a.allMp || a.formula.equals("METEOR")) {
        if (attacker != null
            || c.member.body.mp < c.member.body.maxMp * .65f
            || target.hp < c.member.stats().intel * 40) continue;
        yield *= .35;
      }
      double value = yield / (1 + Math.max(0, a.mpCost) * .01);
      if (value > score) {
        score = value;
        best = a.id;
      }
    }
    if (best != null && c.attack(best, target, 1)) return;
    c.member.notice = "마력·시전 대기";
  }
}
