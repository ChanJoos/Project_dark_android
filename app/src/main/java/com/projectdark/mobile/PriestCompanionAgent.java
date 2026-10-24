package com.projectdark.mobile;

/** Healing, matching dispels and protection; never curse or transfusion. */
final class PriestCompanionAgent implements CompanionAgent {
  public void decide(CompanionAgentContext c, RuntimeState.Monster target) {
    if (!support(c)) {
      c.member.notice = "지원 대기";
      c.follow();
    }
  }

  @Override
  public void defend(CompanionAgentContext c) {
    support(c);
  }

  public boolean support(CompanionAgentContext c) {
    RuntimeState state = c.state;
    CompanionParty.Member m = c.member;
    CompanionController.Navigation nav = c.nav;
    String[] allies = {"player", m.id};
    String urgent = null;
    float health = 1f;
    for (String id : allies) {
      RuntimeState.Player b = state.party().body(state, id);
      if (b != null
          && b.alive
          && Math.hypot(b.x - m.body.x, b.y - m.body.y) <= 192
          && nav.sight(m.body.x, m.body.y, b.x, b.y)) {
        float ratio = b.hp / (float) b.maxHp;
        if (ratio < health) {
          health = ratio;
          urgent = id;
        }
      }
    }
    if (urgent != null && health < .3f) {
      RuntimeState.Player b = state.party().body(state, urgent);
      if (!state.party().holdHeal(urgent) || health < .01f) {
        String id = CompanionPartyRules.heal(m, b);
        if (id != null && c.use(urgent, id)) return true;
      }
    }
    for (String target : allies) {
      RuntimeState.Player b = state.party().body(state, target);
      if (b == null
          || !b.alive
          || Math.hypot(b.x - m.body.x, b.y - m.body.y) > 192
          || !nav.sight(m.body.x, m.body.y, b.x, b.y)) continue;
      String id = CompanionPartyRules.dispel(m, state.skillEffects(), target);
      if (id != null && c.use(target, id)) return true;
    }
    if (urgent != null && health < .8f && !state.party().holdHeal(urgent)) {
      String id = CompanionPartyRules.heal(m, state.party().body(state, urgent));
      if (id != null && c.use(urgent, id)) return true;
    }
    boolean engaged = false;
    for (RuntimeState.Monster enemy : state.monsters())
      if (enemy.alive
          && (enemy.provoked || enemy.attackPrimed || enemy.attackVisualRemaining > 0)
          && Math.hypot(enemy.x - state.player().x, enemy.y - state.player().y) < 224)
        engaged = true;
    if (!engaged) return false;
    for (String id : new String[] {"SK_성직자_029", "SK_성직자_034"}) {
      SkillAbilityCatalog.Ability a = SkillAbilityCatalog.get(id);
      if (m.learned(id) && !state.skillEffects().has(m.id, a.status) && c.use(m.id, id))
        return true;
    }
    for (String target : allies) {
      String id = CompanionPartyRules.protection(m, state.skillEffects(), target);
      if (id != null && c.use(target, id)) return true;
    }
    return false;
  }
}
