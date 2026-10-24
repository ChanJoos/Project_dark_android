package com.projectdark.mobile;

import java.util.*;
import org.json.*;

/**
 * User-approved V269 single-player party. Growth and AI numbers are PROJECT_ADAPTED, not original
 * server data.
 */
public final class CompanionParty {
  public enum Job {
    WARRIOR,
    PRIEST
  }

  public enum Order {
    FOLLOW,
    FOCUS,
    WAIT
  }

  public static final class Member {
    public final Job job;
    public final String id, name;
    public final RuntimeState.Player body = new RuntimeState.Player(0, 0);
    public final RpgProgressionState growth = new RpgProgressionState();
    public Order order = Order.FOLLOW;
    public String target, actionId = "", map = "", notice = "동행";
    public int level = 1;
    public float clock, actionClock, moveClock, fromX, fromY;
    public boolean moving;
    public CharacterRenderer.Direction facing = CharacterRenderer.Direction.SW;
    private final Map<String, Float> cooldowns = new HashMap<>();

    Member(Job j) {
      job = j;
      id = "companion_" + j.name().toLowerCase(Locale.ROOT);
      name = j == Job.WARRIOR ? "전사 동료" : "성직자 동료";
      growth.restoreProgression(3, 0);
      growth.chooseInitialJob(j == Job.WARRIOR ? "WARRIOR" : "CLERIC");
      configure(1);
      body.hp = body.maxHp;
      body.mp = body.maxMp;
    }

    void configure(int lv) {
      level = lv;
      boolean w = job == Job.WARRIOR;
      growth.restoreStats(
          3 + lv * (w ? 2 : 0), 3 + lv * (w ? 0 : 1), 3 + lv * (w ? 0 : 2), 3 + lv, 3 + lv / 2, 0);
      growth.restoreBaseResources(120 + lv * lv * (w ? 2 : 1), 100 + lv * lv * (w ? 1 : 2));
      body.maxHp = growth.finalStats().maxHp;
      body.maxMp = growth.finalStats().maxMp;
    }

    public FinalStats stats() {
      return growth.finalStats();
    }

    public int circle() {
      return level >= 99 ? 5 : level >= 71 ? 4 : level >= 41 ? 3 : level >= 11 ? 2 : 1;
    }

    public boolean learned(String id) {
      SkillAbilityCatalog.Ability a = SkillAbilityCatalog.get(id);
      return a != null
          && a.supported()
          && a.circle <= circle()
          && (job == Job.WARRIOR ? a.job.equals("전사") : a.job.equals("성직자"))
          && !a.kind.equals("SERVICE")
          && !Arrays.asList("SK_성직자_002", "SK_성직자_012", "SK_성직자_031").contains(id);
    }

    void tick(float dt) {
      clock += dt;
      actionClock += dt;
      cooldowns.replaceAll((k, v) -> Math.max(0, v - dt));
    }

    public float cooldown(String id) {
      return cooldowns.getOrDefault(id, 0f);
    }

    void cooldown(String id, float seconds) {
      if (seconds > 0) cooldowns.put(id, seconds);
    }
  }

  private final EnumMap<Job, Member> roster = new EnumMap<>(Job.class);
  private Job selected;
  private String allyTarget;
  private String reservedTarget, reservedActor, reservedSkill;
  private float reservation;

  public CompanionParty() {
    for (Job j : Job.values()) roster.put(j, new Member(j));
  }

  public Member active() {
    return selected == null ? null : roster.get(selected);
  }

  public Member member(Job j) {
    return roster.get(j);
  }

  public Member find(String id) {
    Member m = active();
    return m != null && m.id.equals(id) ? m : null;
  }

  public boolean friendly(String id) {
    return "player".equals(id) || find(id) != null;
  }

  public RuntimeState.Player body(RuntimeState state, String id) {
    if ("player".equals(id)) return state.player();
    Member m = find(id);
    return m == null ? null : m.body;
  }

  public FinalStats stats(RuntimeState state, String id) {
    Member m = find(id);
    return m == null ? state.rpg().finalStats() : m.stats();
  }

  public String allyTarget() {
    Member m = find(allyTarget);
    return m != null && m.body.alive ? m.id : "player";
  }

  public void selectAlly() {
    Member m = active();
    allyTarget = m != null && m.body.alive ? m.id : null;
  }

  public void selectSelf() {
    allyTarget = null;
  }

  public boolean unlocked(RuntimeState s) {
    return !"COMMONER".equals(s.rpg().currentJobCode())
        && s.rpg().normalLevel() != null
        && s.rpg().normalLevel() >= 3;
  }

  public boolean town(RuntimeState s) {
    return TownTravel.isTown(s.currentMapId())
        || WorldDef.ID.equals(s.currentMapId())
        || com.projectdark.mobile.world.TownInteriorDef.forMap(s.currentMapId()) != null;
  }

  private boolean threatened(RuntimeState s) {
    for (RuntimeState.Monster m : s.monsters())
      if (m.alive
          && (m.provoked || m.attackPrimed || m.attackVisualRemaining > 0)
          && Math.hypot(m.x - s.player().x, m.y - s.player().y) < 224) return true;
    return false;
  }

  public boolean recruit(RuntimeState s, Job job) {
    if (!unlocked(s) || !town(s) || !s.player().alive || reservation > 0 || threatened(s))
      return false;
    selected = job;
    allyTarget = null;
    clearReservation();
    Member m = active();
    if (m != null) {
      grow(s, m);
      m.order = Order.FOLLOW;
      m.target = null;
      m.map = "";
    }
    return true;
  }

  public boolean recover(RuntimeState s) {
    if (!town(s) || !s.player().alive || threatened(s)) return false;
    for (Member m : roster.values()) {
      m.body.hp = m.body.maxHp;
      m.body.mp = m.body.maxMp;
      m.body.alive = true;
      m.cooldowns.clear();
    }
    return true;
  }

  public void order(Order order, String target) {
    Member m = active();
    if (m == null) return;
    m.order = order;
    m.target = order == Order.FOCUS ? target : null;
    clearReservation();
  }

  public void reserve(String actor, String target, String skill, float seconds) {
    reservedActor = actor;
    reservedTarget = target;
    reservedSkill = skill;
    reservation = Math.min(4, Math.max(.1f, seconds));
  }

  public boolean holdAttack(String actor, String target) {
    return reservation > 0
        && Objects.equals(target, reservedTarget)
        && !Objects.equals(actor, reservedActor);
  }

  public boolean holdHeal(String target) {
    return reservation > 0
        && Objects.equals(target, reservedActor)
        && "SK_전사_015".equals(reservedSkill);
  }

  public void finishReservation(String actor, String skill) {
    if (Objects.equals(actor, reservedActor) && Objects.equals(skill, reservedSkill))
      clearReservation();
  }

  public void clearReservation() {
    reservation = 0;
    reservedTarget = reservedActor = reservedSkill = null;
  }

  public void tick(RuntimeState s, float dt) {
    float safe = Math.max(0, dt);
    reservation = Math.max(0, reservation - safe);
    if (reservation > 0) {
      RuntimeState.Player actor = body(s, reservedActor);
      RuntimeState.Monster target = null;
      for (RuntimeState.Monster x : s.monsters())
        if (x.id.equals(reservedTarget) && x.alive) target = x;
      if (actor == null || !actor.alive || target == null) clearReservation();
    }
    for (Member m : roster.values()) m.tick(safe);
    Member m = active();
    if (m != null) grow(s, m);
  }

  private void grow(RuntimeState s, Member m) {
    int lv = s.rpg().normalLevel() == null ? 1 : s.rpg().normalLevel();
    if (lv <= m.level) return;
    float hp = m.body.hp / (float) m.body.maxHp, mp = m.body.mp / (float) Math.max(1, m.body.maxMp);
    m.configure(Math.min(99, lv));
    m.body.hp = m.body.alive ? Math.max(1, (int) (m.body.maxHp * hp)) : 0;
    m.body.mp = (int) (m.body.maxMp * mp);
  }

  public JSONObject snapshot() {
    try {
      JSONArray rows = new JSONArray();
      for (Member m : roster.values()) {
        JSONObject cds = new JSONObject();
        for (Map.Entry<String, Float> e : m.cooldowns.entrySet())
          if (e.getValue() > 0) cds.put(e.getKey(), e.getValue());
        rows.put(
            new JSONObject()
                .put("job", m.job.name())
                .put("level", m.level)
                .put("hp", m.body.hp)
                .put("mp", m.body.mp)
                .put("order", m.order.name())
                .put("cooldowns", cds));
      }
      return new JSONObject()
          .put("version", 1)
          .put("selected", selected == null ? JSONObject.NULL : selected.name())
          .put("members", rows);
    } catch (JSONException e) {
      throw new IllegalStateException(e);
    }
  }

  public boolean restore(JSONObject json) {
    try {
      if (json.getInt("version") != 1) return false;
      EnumMap<Job, Member> staged = new EnumMap<>(Job.class);
      JSONArray rows = json.getJSONArray("members");
      if (rows.length() != 2) return false;
      for (int i = 0; i < rows.length(); i++) {
        JSONObject row = rows.getJSONObject(i);
        Job j = Job.valueOf(row.getString("job"));
        if (staged.containsKey(j)) return false;
        Member m = new Member(j);
        int lv = row.getInt("level");
        if (lv < 1 || lv > 99) return false;
        m.configure(lv);
        int hp = row.getInt("hp"), mp = row.getInt("mp");
        if (hp < 0 || hp > m.body.maxHp || mp < 0 || mp > m.body.maxMp) return false;
        m.body.hp = hp;
        m.body.mp = mp;
        m.body.alive = hp > 0;
        m.order = Order.valueOf(row.getString("order"));
        if (m.order == Order.FOCUS) m.order = Order.FOLLOW;
        JSONObject cds = row.getJSONObject("cooldowns");
        Iterator<String> it = cds.keys();
        while (it.hasNext()) {
          String id = it.next();
          float seconds = (float) cds.getDouble(id);
          if (!Float.isFinite(seconds) || seconds < 0 || seconds > 300) return false;
          m.cooldown(id, seconds);
        }
        staged.put(j, m);
      }
      Job next = json.isNull("selected") ? null : Job.valueOf(json.getString("selected"));
      roster.clear();
      roster.putAll(staged);
      selected = next;
      allyTarget = null;
      clearReservation();
      return true;
    } catch (Exception e) {
      return false;
    }
  }
}
