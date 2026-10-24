package com.projectdark.mobile;

import java.util.*;

/** Shared observations and legal actions, without profession-specific policy. */
final class CompanionAgentContext {
  final RuntimeState state;
  final RuntimeCombatSession combat;
  final CompanionParty.Member member;
  final CompanionController.Navigation nav;
  private final CompanionController controller;

  CompanionAgentContext(
      RuntimeState s,
      RuntimeCombatSession combat,
      CompanionParty.Member m,
      CompanionController.Navigation nav,
      CompanionController controller) {
    state = s;
    this.combat = combat;
    member = m;
    this.nav = nav;
    this.controller = controller;
  }

  float health() {
    return member.body.hp / (float) member.body.maxHp;
  }

  boolean has(String kind) {
    return state.skillEffects().has(member.id, kind);
  }

  boolean engaged(RuntimeState.Monster m) {
    return m != null
        && m.alive
        && (m.provoked || m.attackPrimed || m.attackVisualRemaining > 0)
        && Math.hypot(m.x - state.player().x, m.y - state.player().y) <= 224;
  }

  RuntimeState.Monster attacker() {
    for (RuntimeState.Monster m : state.monsters())
      if (engaged(m) && member.id.equals(m.attackTarget)) return m;
    return null;
  }

  boolean anyCombat() {
    for (RuntimeState.Monster m : state.monsters()) if (engaged(m)) return true;
    return false;
  }

  boolean safeTarget(RuntimeState.Monster target) {
    return target != null
        && target.alive
        && !state.skillEffects().has(target.id, "SLEEP")
        && !state.party().holdAttack(member.id, target.id);
  }

  boolean melee(RuntimeState.Monster target) {
    return CanonicalMeleeTileContract.reachable(member.body.x, member.body.y, target.x, target.y)
        && nav.sight(member.body.x, member.body.y, target.x, target.y);
  }

  void face(RuntimeState.Monster target) {
    member.facing =
        CanonicalActorFacing.quantize(
            target.x - member.body.x, target.y - member.body.y, member.facing);
  }

  void follow() {
    if (member.order != CompanionParty.Order.WAIT) controller.follow(member, nav);
  }

  void approach(RuntimeState.Monster target, int reach) {
    if (member.order == CompanionParty.Order.WAIT) return;
    member.notice = "접근";
    controller.step(member, nav, target.x, target.y, reach);
  }

  boolean retreat(RuntimeState.Monster target) {
    return controller.retreat(member, nav, target);
  }

  boolean ready(String id) {
    SkillAbilityCatalog.Ability a = SkillAbilityCatalog.get(id);
    return a != null
        && a.supported()
        && member.learned(id)
        && member.cooldown(id) <= 0
        && member.body.mp >= a.mpCost;
  }

  boolean use(String target, String id) {
    if (id == null || member.cooldown(id) > 0 || !id.startsWith("attack_proto_") && !ready(id))
      return false;
    if (combat.submitCompanion(target, id).accepted()) {
      SkillAbilityCatalog.Ability a = SkillAbilityCatalog.get(id);
      member.notice = a == null ? "기본공격" : a.name;
      member.actionClock = 0;
      member.actionId = id;
      return true;
    }
    return false;
  }

  boolean self(String id) {
    return use(member.id, id);
  }

  void basic(RuntimeState.Monster target) {
    face(target);
    use(target.id, RuntimeCombatSession.playerAttackActionId(0));
  }

  boolean buff(String id, String family) {
    return !has(family) && self(id);
  }

  int damage(SkillAbilityCatalog.Ability a, RuntimeState.Monster target) {
    int raw =
        SkillAbilityCatalog.amount(
            a,
            new SkillAbilityCatalog.Snapshot(
                member.stats().withEffects(state.skillEffects(), member.id),
                member.body.hp,
                member.body.mp,
                member.level,
                100,
                false,
                a.mpCost));
    String element = RuntimeCombatPortAdapter.skillElement(a);
    return ElementalDamageFormula.apply(
        raw,
        element.isEmpty() ? member.stats().attackElement : element,
        state.skillEffects().has(target.id, "CHANGE_ELEMENT") ? "FIRE" : target.defenseElement);
  }

  List<RuntimeState.Monster> victims(String id, RuntimeState.Monster target) {
    SkillActionContract.Rule r = SkillActionContract.get(id);
    if (r == null) return Collections.emptyList();
    String anchor = r.selfAnchored() ? member.id : target.id;
    List<String> ids = combat.companionRecipients(anchor, id);
    List<RuntimeState.Monster> out = new ArrayList<>();
    for (RuntimeState.Monster m : state.monsters()) if (ids.contains(m.id)) out.add(m);
    return out;
  }

  boolean attack(String id, RuntimeState.Monster target, int minimumTargets) {
    if (!ready(id) || !safeTarget(target)) return false;
    face(target);
    List<RuntimeState.Monster> victims = victims(id, target);
    if (victims.size() < minimumTargets) return false;
    for (RuntimeState.Monster m : victims)
      if (!safeTarget(m) || !engaged(m) && !m.id.equals(target.id)) return false;
    SkillActionContract.Rule r = SkillActionContract.get(id);
    return use(r.selfAnchored() ? member.id : target.id, id);
  }

  boolean selfHeal() {
    if (health() >= .65f || state.party().holdHeal(member.id)) return false;
    String best = null;
    int cost = Integer.MAX_VALUE, largest = 0;
    String fallback = null;
    int missing = member.body.maxHp - member.body.hp;
    for (SkillAbilityCatalog.Ability a : SkillAbilityCatalog.all())
      if (a.heal()
          && ready(a.id)
          && SkillActionContract.get(a.id).pattern == SkillActionContract.Pattern.SELF) {
        int amount =
            SkillAbilityCatalog.amount(
                a,
                new SkillAbilityCatalog.Snapshot(
                    member.stats(), member.body.hp, member.body.mp, member.level, 100, false));
        if (amount >= missing && a.mpCost < cost) {
          best = a.id;
          cost = a.mpCost;
        }
        if (amount > largest) {
          largest = amount;
          fallback = a.id;
        }
      }
    return self(best == null ? fallback : best);
  }
}
