package com.projectdark.mobile;

import java.util.*;
import org.json.*;

/**
 * User-approved V269 single-player party. Growth and AI numbers are PROJECT_ADAPTED, not original
 * server data.
 */
public final class CompanionParty {
  public enum Job {
    WARRIOR("전사", "WARRIOR"),
    PRIEST("성직자", "CLERIC"),
    ROGUE("도적", "ROGUE"),
    MAGE("마법사", "MAGE"),
    MONK("무도가", "MARTIAL_ARTIST");
    public final String label, code;

    Job(String label, String code) {
      this.label = label;
      this.code = code;
    }
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
    private boolean testAccess;
    private float equipmentRegenClock;
    private int equipmentRegenBonus;
    public float clock, actionClock, moveClock, fromX, fromY;
    public boolean moving;
    public CharacterRenderer.Direction facing = CharacterRenderer.Direction.SW;
    private final Map<String, Float> cooldowns = new HashMap<>();

    Member(Job j) {
      job = j;
      id = "companion_" + j.name().toLowerCase(Locale.ROOT);
      name = j.label + " 동료";
      growth.restoreProgression(3, 0);
      growth.chooseInitialJob(j.code);
      configure(1);
      growth.bindTwoHandLearning(() -> learned("SK_전사_008"));
      // Only existing worn starter pieces belong to a companion; no hidden sandbox item grants.
      growth.restoreOwnedItems(CompanionEquipment.counts(growth.equipment()), growth.equipment());
      body.hp = body.maxHp;
      body.mp = body.maxMp;
    }

    void configure(int lv) {
      level = lv;
      boolean physical = job == Job.WARRIOR || job == Job.ROGUE || job == Job.MONK;
      int str = job == Job.WARRIOR || job == Job.MONK ? 2 : job == Job.ROGUE ? 1 : 0;
      int dex = job == Job.ROGUE ? 3 : 1;
      int intel = job == Job.MAGE ? 3 : job == Job.PRIEST ? 1 : 0;
      int wis = job == Job.PRIEST ? 2 : job == Job.MAGE ? 1 : 0;
      int con = job == Job.MONK ? 2 : 1;
      growth.restoreProgression(lv, 0);
      growth.restoreStats(
          3 + lv * str,
          3 + lv * intel,
          3 + lv * wis,
          3 + lv * con,
          3 + (job == Job.WARRIOR || job == Job.PRIEST ? lv / 2 : lv * dex),
          0);
      growth.restoreBaseResources(
          120 + lv * lv * (physical ? 2 : 1), 100 + lv * lv * (physical ? 1 : 2));
      body.maxHp = growth.finalStats().maxHp;
      body.maxMp = growth.finalStats().maxMp;
    }

    void syncEquipment() {
      FinalStats f = growth.finalStats();
      body.maxHp = f.maxHp;
      body.maxMp = f.maxMp;
      body.hp = body.alive ? Math.min(body.hp, body.maxHp) : 0;
      body.mp = Math.min(body.mp, body.maxMp);
    }

    void regenerateEquipment(float dt) {
      int bonus = ItemEffects.regeneration(growth);
      if (bonus != equipmentRegenBonus) {
        equipmentRegenClock = 0;
        equipmentRegenBonus = bonus;
      }
      if (!body.alive || bonus == 0) {
        equipmentRegenClock = 0;
        return;
      }
      equipmentRegenClock += dt;
      int n = (int) (equipmentRegenClock / 25f);
      if (n > 0) {
        equipmentRegenClock -= n * 25f;
        body.hp =
            (int)
                Math.min(
                    body.maxHp,
                    (long) body.hp + (long) n * ItemEffects.additionalRegeneration(growth));
      }
    }

    public FinalStats stats() {
      return growth.finalStats();
    }

    public int circle() {
      if (testAccess) return 7;
      return level >= 99 ? 5 : level >= 71 ? 4 : level >= 41 ? 3 : level >= 11 ? 2 : 1;
    }

    public boolean learned(String id) {
      SkillAbilityCatalog.Ability a = SkillAbilityCatalog.get(id);
      if (testAccess) return a != null && !SkillPolicyV227.retired(id) && a.job.equals(job.label);
      return a != null
          && a.supported()
          && a.circle <= circle()
          && a.job.equals(job.label)
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
  private boolean testing;

  public boolean testing() {
    return testing;
  }

  /** Temporary APK-only access: independent of quest and player skill-test toggles. */
  public void enableTesting(RuntimeState state) {
    testing = true;
    for (Member m : roster.values()) {
      m.testAccess = true;
      m.growth.restoreProgression(99, 0);
      if (m.level == 99) continue;
      float hp = m.body.hp / (float) m.body.maxHp,
          mp = m.body.mp / (float) Math.max(1, m.body.maxMp);
      m.testAccess = true;
      m.configure(99);
      m.body.hp = m.body.alive ? Math.max(1, (int) (m.body.maxHp * hp)) : 0;
      m.body.mp = (int) (m.body.maxMp * mp);
    }
    if (selected == null) selected = Job.WARRIOR;
  }

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
    return testing
        || !"COMMONER".equals(s.rpg().currentJobCode())
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
    if (!unlocked(s)
        || !testing && !town(s)
        || !s.player().alive
        || reservation > 0
        || threatened(s)) return false;
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
    if (!testing && !town(s) || !s.player().alive || threatened(s)) return false;
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
        && ("SK_전사_015".equals(reservedSkill) || "SK_전사_023".equals(reservedSkill));
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
    if (m != null) {
      grow(s, m);
      m.regenerateEquipment(safe);
    }
  }

  private void grow(RuntimeState s, Member m) {
    if (testing) return;
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
                .put("cooldowns", cds)
                .put("equipment", new JSONObject(m.growth.equipment())));
      }
      return new JSONObject()
          .put("version", 3)
          .put("selected", selected == null ? JSONObject.NULL : selected.name())
          .put("members", rows);
    } catch (JSONException e) {
      throw new IllegalStateException(e);
    }
  }

  public boolean restore(JSONObject json) {
    try {
      int version = json.getInt("version");
      if (version != 1 && version != 2 && version != 3) return false;
      EnumMap<Job, Member> staged = new EnumMap<>(Job.class);
      JSONArray rows = json.getJSONArray("members");
      if (rows.length() != (version == 1 ? 2 : Job.values().length)) return false;
      for (int i = 0; i < rows.length(); i++) {
        JSONObject row = rows.getJSONObject(i);
        Job j = Job.valueOf(row.getString("job"));
        if (staged.containsKey(j) || version == 1 && j != Job.WARRIOR && j != Job.PRIEST)
          return false;
        Member m = new Member(j);
        int lv = row.getInt("level");
        if (lv < 1 || lv > 99) return false;
        m.configure(lv);
        if (version >= 3) {
          Map<String, String> starterEquipment = new LinkedHashMap<>(m.growth.equipment());
          JSONObject equipment = row.getJSONObject("equipment");
          Map<String, String> equipped = new LinkedHashMap<>();
          Iterator<String> slots = equipment.keys();
          while (slots.hasNext()) {
            String slot = slots.next();
            equipped.put(slot, equipment.getString(slot));
          }
          if (!m.growth.restoreOwnedItems(CompanionEquipment.counts(equipped), equipped))
            return false;
          if (!m.growth.equipment().equals(equipped)) return false;
          for (Map.Entry<String, String> e : equipped.entrySet()) {
            RpgProgressionState.RequirementResult requirement =
                CompanionEquipment.requirement(m, e.getValue());
            if (requirement != RpgProgressionState.RequirementResult.MET
                && !Objects.equals(e.getValue(), starterEquipment.get(e.getKey()))) return false;
          }
          m.syncEquipment();
        }
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
      if (version == 1)
        for (Job j : Job.values()) if (!staged.containsKey(j)) staged.put(j, new Member(j));
      if (next != null && !staged.containsKey(next)) return false;
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
