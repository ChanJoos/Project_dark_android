package com.projectdark.mobile;

import com.projectdark.mobile.world.WorldMoveTargetController;
import java.util.*;

/** One bounded decision per 0.25s; submits existing action IDs to the shared Resolver. */
public final class CompanionController {
  public interface Navigation {
    Object identity();

    List<WorldMoveTargetController.TileCenter> tiles();

    boolean stand(float x, float y);

    boolean traverse(float ax, float ay, float bx, float by);

    boolean sight(float ax, float ay, float bx, float by);
  }

  private final RuntimeState state;
  private final RuntimeCombatSession combat;
  private Object navIdentity;
  private final Map<Long, WorldMoveTargetController.TileCenter> cells = new HashMap<>();
  private float decisionClock;

  public CompanionController(RuntimeState s, RuntimeCombatSession session) {
    state = s;
    combat = session;
  }

  public void tick(float dt, Navigation nav, RuntimeState.Monster selected, boolean allowCombat) {
    CompanionParty.Member m = state.party().active();
    if (m == null) return;
    if (navIdentity != nav.identity()) {
      navIdentity = nav.identity();
      cells.clear();
      for (WorldMoveTargetController.TileCenter t : nav.tiles()) cells.put(key(t.x, t.y), t);
    }
    if (!state.currentMapId().equals(m.map)) {
      m.map = state.currentMapId();
      m.target = null;
      m.order = CompanionParty.Order.FOLLOW;
      m.moving = false;
      m.body.x = state.player().x;
      m.body.y = state.player().y;
      for (WorldMoveTargetController.Direction d : WorldMoveTargetController.Direction.values())
        if (cells.containsKey(key(m.body.x + d.dx, m.body.y + d.dy))
            && nav.stand(m.body.x + d.dx, m.body.y + d.dy)) {
          m.body.x += d.dx;
          m.body.y += d.dy;
          break;
        }
    }
    m.body.hitFlash = Math.max(0, m.body.hitFlash - dt);
    if (m.moving) {
      m.moveClock = Math.max(0, m.moveClock - dt);
      if (m.moveClock <= 0) m.moving = false;
      return;
    }
    decisionClock -= Math.max(0, dt);
    if (decisionClock > 0) return;
    decisionClock = .25f;
    if (!state.player().alive || !m.body.alive || state.skillEffects().disabled(m.id)) {
      m.notice = m.body.alive ? "대기" : "전투 불능 · 마을에서 회복";
      return;
    }
    if (combat.companionBusy()) return;
    if (m.order == CompanionParty.Order.WAIT) {
      m.notice = "대기";
      if (allowCombat && m.job == CompanionParty.Job.PRIEST) {
        support(m, nav);
        return;
      }
      if (allowCombat)
        for (RuntimeState.Monster enemy : state.monsters())
          if (enemy.alive
              && m.id.equals(enemy.attackTarget)
              && CanonicalMeleeTileContract.reachable(m.body.x, m.body.y, enemy.x, enemy.y)
              && !state.skillEffects().has(enemy.id, "SLEEP")
              && !state.party().holdAttack(m.id, enemy.id)) {
            use(m, enemy.id, RuntimeCombatSession.playerAttackActionId(0));
            break;
          }
      return;
    }
    if (!allowCombat) {
      m.notice = "동행";
      follow(m, nav);
      return;
    }
    if (m.job == CompanionParty.Job.PRIEST && support(m, nav)) return;
    RuntimeState.Monster target = choose(m, selected);
    if (target == null) {
      m.notice = "동행";
      follow(m, nav);
      return;
    }
    m.target = target.id;
    if (Math.hypot(target.x - state.player().x, target.y - state.player().y) > 224) {
      m.target = null;
      m.notice = "추격 중단";
      follow(m, nav);
      return;
    }
    if (m.job == CompanionParty.Job.PRIEST) {
      m.notice = "지원 대기";
      follow(m, nav);
      return;
    }
    if (state.party().holdAttack(m.id, target.id) || state.skillEffects().has(target.id, "SLEEP")) {
      // A sleeping target is reserved for the strongest available warrior hit, never a stray basic
      // attack.
      if (state.skillEffects().has(target.id, "SLEEP")
          && canBurst(m)
          && !state.party().holdAttack(m.id, target.id)) {
        if (use(m, target.id, burst(m))) {
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
      step(m, nav, target.x, target.y, 1);
      return;
    }
    m.facing = CanonicalActorFacing.quantize(target.x - m.body.x, target.y - m.body.y, m.facing);
    if ("player".equals(target.attackTarget)
        && m.body.hp > m.body.maxHp * .3f
        && use(m, target.id, "SK_전사_012")) return;
    if (m.body.hp <= m.body.maxHp * .02f && use(m, target.id, crash(m))) return;
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
        && use(m, target.id, soul(m))) return;
    for (String id : new String[] {"SK_전사_006", "SK_전사_003", "SK_전사_001"})
      if (use(m, target.id, id)) return;
    use(m, target.id, RuntimeCombatSession.playerAttackActionId(0));
  }

  private boolean support(CompanionParty.Member m, Navigation nav) {
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
        if (id != null && use(m, urgent, id)) return true;
      }
    }
    for (String target : allies) {
      RuntimeState.Player b = state.party().body(state, target);
      if (b == null
          || !b.alive
          || Math.hypot(b.x - m.body.x, b.y - m.body.y) > 192
          || !nav.sight(m.body.x, m.body.y, b.x, b.y)) continue;
      String id = CompanionPartyRules.dispel(m, state.skillEffects(), target);
      if (id != null && use(m, target, id)) return true;
    }
    if (urgent != null && health < .8f && !state.party().holdHeal(urgent)) {
      String id = CompanionPartyRules.heal(m, state.party().body(state, urgent));
      if (id != null && use(m, urgent, id)) return true;
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
      if (m.learned(id) && !state.skillEffects().has(m.id, a.status) && use(m, m.id, id))
        return true;
    }
    for (String target : allies) {
      String id = CompanionPartyRules.protection(m, state.skillEffects(), target);
      if (id != null && use(m, target, id)) return true;
    }
    return false;
  }

  private boolean use(CompanionParty.Member m, String target, String id) {
    if (id == null || m.cooldown(id) > 0 || !id.startsWith("attack_proto_") && !m.learned(id))
      return false;
    if (combat.submitCompanion(target, id).accepted()) {
      m.notice = SkillAbilityCatalog.get(id) == null ? "기본공격" : SkillAbilityCatalog.get(id).name;
      m.actionClock = 0;
      m.actionId = id;
      return true;
    }
    return false;
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

  private boolean canBurst(CompanionParty.Member m) {
    String id = burst(m);
    return m.learned(id)
        && m.cooldown(id) <= 0
        && m.body.hp > 1
        && (SkillAbilityCatalog.get(id).formula.equals("CRASH")
            || m.body.hp > m.body.maxHp * .8f
                && ("CLERIC".equals(state.rpg().currentJobCode())
                    || state.skillEffects().has(m.id, "PROTECT")));
  }

  private RuntimeState.Monster choose(CompanionParty.Member m, RuntimeState.Monster selected) {
    if (m.order == CompanionParty.Order.FOCUS) {
      for (RuntimeState.Monster x : state.monsters())
        if (x.alive && x.id.equals(m.target)) return x;
      m.order = CompanionParty.Order.FOLLOW;
      m.target = null;
    }
    for (RuntimeState.Monster x : state.monsters())
      if (x.alive && x.id.equals(m.target) && (x.provoked || m.id.equals(x.attackTarget))) return x;
    if (selected != null && selected.alive && selected.provoked) return selected;
    for (RuntimeState.Monster x : state.monsters())
      if (x.alive
          && (x.provoked || x.attackPrimed || x.attackVisualRemaining > 0)
          && Math.hypot(x.x - state.player().x, x.y - state.player().y) < 120) return x;
    return null;
  }

  private void follow(CompanionParty.Member m, Navigation nav) {
    if (Math.hypot(m.body.x - state.player().x, m.body.y - state.player().y) > 48)
      step(m, nav, state.player().x, state.player().y, 1);
  }

  private static long key(float x, float y) {
    return ((long) Math.round(x) << 32) ^ (Math.round(y) & 0xffffffffL);
  }

  private void step(CompanionParty.Member m, Navigation nav, float tx, float ty, int reach) {
    if (state.skillEffects().rooted(m.id)) return;
    WorldMoveTargetController.TileCenter start = cells.get(key(m.body.x, m.body.y));
    if (start == null) {
      m.notice = "이동 경로 대기";
      return;
    }
    ArrayDeque<WorldMoveTargetController.TileCenter> queue = new ArrayDeque<>();
    Map<Long, WorldMoveTargetController.TileCenter> first = new HashMap<>();
    queue.add(start);
    first.put(key(start.x, start.y), start);
    int visited = 0;
    while (!queue.isEmpty() && visited++ < 2048) {
      WorldMoveTargetController.TileCenter at = queue.removeFirst();
      if (at != start && SkillActionContract.distance(at.x, at.y, tx, ty) <= reach) {
        WorldMoveTargetController.TileCenter next = first.get(key(at.x, at.y));
        m.fromX = m.body.x;
        m.fromY = m.body.y;
        m.body.x = next.x;
        m.body.y = next.y;
        m.facing = CanonicalActorFacing.quantize(next.x - m.fromX, next.y - m.fromY, m.facing);
        m.moving = true;
        m.moveClock = WorldMoveTargetController.TILE_STEP_SECONDS;
        return;
      }
      for (WorldMoveTargetController.Direction d : WorldMoveTargetController.Direction.values()) {
        long k = key(at.x + d.dx, at.y + d.dy);
        WorldMoveTargetController.TileCenter next = cells.get(k);
        if (next != null
            && !first.containsKey(k)
            && nav.stand(next.x, next.y)
            && nav.traverse(at.x, at.y, next.x, next.y)) {
          first.put(k, at == start ? next : first.get(key(at.x, at.y)));
          queue.addLast(next);
        }
      }
    }
  }
}
