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
  private final EnumMap<CompanionParty.Job, CompanionAgent> agents =
      new EnumMap<>(CompanionParty.Job.class);

  CompanionAgent agent(CompanionParty.Job job) {
    return agents.get(job);
  }

  public CompanionController(RuntimeState s, RuntimeCombatSession session) {
    state = s;
    combat = session;
    agents.put(CompanionParty.Job.WARRIOR, new WarriorCompanionAgent());
    agents.put(CompanionParty.Job.PRIEST, new PriestCompanionAgent());
    agents.put(CompanionParty.Job.ROGUE, new RogueCompanionAgent());
    agents.put(CompanionParty.Job.MAGE, new MageCompanionAgent());
    agents.put(CompanionParty.Job.MONK, new MonkCompanionAgent());
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
    CompanionAgentContext context = new CompanionAgentContext(state, combat, m, nav, this);
    if (m.order == CompanionParty.Order.WAIT) {
      m.notice = "대기";
      if (allowCombat) agents.get(m.job).defend(context);
      return;
    }
    if (!allowCombat) {
      m.notice = "동행";
      follow(m, nav);
      return;
    }
    RuntimeState.Monster target = choose(m, selected);
    if (target != null
        && Math.hypot(target.x - state.player().x, target.y - state.player().y) > 224) {
      m.target = null;
      target = null;
    }
    if (target != null) m.target = target.id;
    agents.get(m.job).decide(context, target);
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

  void follow(CompanionParty.Member m, Navigation nav) {
    if (Math.hypot(m.body.x - state.player().x, m.body.y - state.player().y) > 48)
      step(m, nav, state.player().x, state.player().y, 1);
  }

  boolean retreat(CompanionParty.Member m, Navigation nav, RuntimeState.Monster target) {
    if (m.order == CompanionParty.Order.WAIT || state.skillEffects().rooted(m.id)) return false;
    float best = (float) Math.hypot(target.x - m.body.x, target.y - m.body.y);
    WorldMoveTargetController.TileCenter chosen = null;
    for (WorldMoveTargetController.Direction d : WorldMoveTargetController.Direction.values()) {
      WorldMoveTargetController.TileCenter t = cells.get(key(m.body.x + d.dx, m.body.y + d.dy));
      if (t == null
          || Math.hypot(t.x - state.player().x, t.y - state.player().y) > 160
          || !nav.stand(t.x, t.y)
          || !nav.traverse(m.body.x, m.body.y, t.x, t.y)
          || !nav.sight(t.x, t.y, target.x, target.y)) continue;
      float distance = (float) Math.hypot(t.x - target.x, t.y - target.y);
      if (distance > best) {
        best = distance;
        chosen = t;
      }
    }
    if (chosen == null) return false;
    m.fromX = m.body.x;
    m.fromY = m.body.y;
    m.body.x = chosen.x;
    m.body.y = chosen.y;
    m.facing = CanonicalActorFacing.quantize(m.body.x - m.fromX, m.body.y - m.fromY, m.facing);
    m.moving = true;
    m.moveClock = WorldMoveTargetController.TILE_STEP_SECONDS;
    m.notice = "거리 확보";
    return true;
  }

  private static long key(float x, float y) {
    return ((long) Math.round(x) << 32) ^ (Math.round(y) & 0xffffffffL);
  }

  void step(CompanionParty.Member m, Navigation nav, float tx, float ty, int reach) {
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
