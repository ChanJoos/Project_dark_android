package com.projectdark.mobile;

import android.content.Context;
import android.graphics.*;
import java.util.*;

/** A companion's own paper doll and stats, with an explicit owned-equipment picker. */
final class CompanionEquipmentWindow {
  interface Actions {
    void change(CompanionParty.Job job, String item, String slot, boolean remove);
  }

  boolean open, stats;
  CompanionParty.Job job = CompanionParty.Job.WARRIOR;
  final ItemWindow equipment;
  String selected;
  int page;
  private List<RpgInventoryPresentation.ItemRow> cached = Collections.emptyList();
  private long inventoryRevision = -1, equipmentRevision = -1;
  private CompanionParty.Member cachedMember;
  private String cachedSlot;
  private int cachedLevel;
  private final RpgInventoryPresentation presentation = new RpgInventoryPresentation();

  CompanionEquipmentWindow(Context context) {
    equipment = new ItemWindow(context);
    equipment.pairedPanels = true;
    equipment.selectedSlot = "갑옷";
  }

  void open(CompanionParty.Job selectedJob) {
    open = true;
    job = selectedJob;
    stats = false;
    selected = null;
    page = 0;
    equipment.selectedSlot = "갑옷";
  }

  static RectF tab(int index) {
    return new RectF(16 + index * 186, 5, 194 + index * 186, 31);
  }

  static RectF row(int index) {
    return new RectF(440, 94 + index * 45, 936, 136 + index * 45);
  }

  static RectF equipButton() {
    return new RectF(760, 488, 936, 522);
  }

  List<RpgInventoryPresentation.ItemRow> rows(RuntimeState state) {
    CompanionParty.Member m = state.party().member(job);
    if (cachedMember == m
        && inventoryRevision == state.rpg().inventoryRevision()
        && equipmentRevision == m.growth.equipmentRevision()
        && Objects.equals(cachedSlot, equipment.selectedSlot)
        && cachedLevel == m.level) return cached;
    cachedMember = m;
    inventoryRevision = state.rpg().inventoryRevision();
    equipmentRevision = m.growth.equipmentRevision();
    cachedSlot = equipment.selectedSlot;
    cachedLevel = m.level;
    List<RpgInventoryPresentation.ItemRow> result = new ArrayList<>();
    for (Map.Entry<String, Integer> e : state.rpg().inventory().entrySet()) {
      RpgProgressionState.ItemDefinition d = state.rpg().itemDefinitions().get(e.getKey());
      int quantity = e.getValue() - state.rpg().equippedCount(e.getKey());
      if (d != null
          && d.equippable()
          && RpgProgressionState.acceptsSlot(equipment.selectedSlot, d)
          && quantity > 0)
        result.add(
            new RpgInventoryPresentation.ItemRow(
                d, quantity, false, CompanionEquipment.requirement(m, d.itemId)));
    }
    result.sort(
        Comparator.comparingInt(
                (RpgInventoryPresentation.ItemRow r) ->
                    r.requirementResult == RpgProgressionState.RequirementResult.MET ? 0 : 1)
            .thenComparing(r -> r.name));
    cached = Collections.unmodifiableList(result);
    return cached;
  }

  void draw(Canvas c, RuntimeState state, float offset, ItemWindow.Visuals visuals) {
    if (!open) return;
    c.save();
    c.translate(offset, 0);
    UiTheme.scrim(c);
    CompanionParty.Member m = state.party().member(job);
    for (int i = 0; i < CompanionParty.Job.values().length; i++) {
      CompanionParty.Job j = CompanionParty.Job.values()[i];
      UiTheme.button(c, tab(i), j.label + " 동료", true, j == job);
    }
    UiTheme.text(c, m.name + " · Lv" + m.level, 18, 42, 10, UiTheme.GOLD, true);
    c.save();
    InventoryPairV262.equipment(c);
    equipment.equipment(c, m.growth, null, visuals);
    c.restore();
    UiTheme.text(
        c,
        "HP " + m.body.hp + " / " + m.body.maxHp + " · MP " + m.body.mp + " / " + m.body.maxMp,
        18,
        526,
        10,
        UiTheme.TEXT,
        false);
    UiTheme.panel(
        c,
        new RectF(424, 45, 950, 532),
        stats ? m.name + " 능력치" : "동료 장비 선택 · " + equipment.selectedSlot);
    UiTheme.button(c, new RectF(876, 51, 938, 79), stats ? "돌아가기" : "닫기", true, false);
    if (stats) {
      drawStats(c, m);
      c.restore();
      return;
    }
    List<RpgInventoryPresentation.ItemRow> rows = rows(state);
    int pages = Math.max(1, (rows.size() + 7) / 8);
    page = Math.max(0, Math.min(page, pages - 1));
    for (int i = 0; i < 8; i++) {
      int n = page * 8 + i;
      RectF b = row(i);
      UiTheme.surface(c, b, UiTheme.BG, UiTheme.LINE, 3);
      if (n >= rows.size()) continue;
      RpgInventoryPresentation.ItemRow r = rows.get(n);
      boolean picked = r.itemId.equals(selected);
      visuals.icon(c, r, b.left + 5, b.top + 3, 35);
      UiTheme.fit(
          c,
          r.name,
          b.left + 47,
          b.top + 16,
          b.width() - 60,
          11,
          picked ? UiTheme.GOLD : UiTheme.TEXT,
          true);
      UiTheme.text(
          c,
          "Lv"
              + (r.requiredLevel == null ? "?" : r.requiredLevel)
              + " · "
              + presentation.requirementLabel(r)
              + " · "
              + r.quantity
              + "개",
          b.left + 47,
          b.top + 33,
          9,
          r.requirementResult == RpgProgressionState.RequirementResult.MET
              ? UiTheme.MUTED
              : 0xffe9a594,
          false);
      if (picked) UiTheme.surface(c, b, 0, UiTheme.GOLD, 3);
    }
    if (rows.isEmpty()) UiTheme.text(c, "보유한 여분 장비가 없습니다", 457, 125, 12, UiTheme.MUTED, false);
    UiTheme.button(c, new RectF(440, 488, 493, 522), "‹", true, false);
    UiTheme.button(c, new RectF(681, 488, 734, 522), "›", true, false);
    UiTheme.center(c, (page + 1) + " / " + pages, 587, 509, 11, UiTheme.TEXT, true);
    RpgInventoryPresentation.ItemRow chosen = null;
    for (RpgInventoryPresentation.ItemRow r : rows) if (r.itemId.equals(selected)) chosen = r;
    boolean valid =
        chosen != null && chosen.requirementResult == RpgProgressionState.RequirementResult.MET;
    UiTheme.button(c, equipButton(), "동료에게 장착", valid, true);
    UiTheme.fit(c, "동료 자신의 직업·레벨 조건과 장비 능력치를 적용합니다", 440, 474, 493, 10, UiTheme.MUTED, false);
    c.restore();
  }

  private void drawStats(Canvas c, CompanionParty.Member m) {
    FinalStats f = m.stats();
    String[] labels = {
      "직업", "레벨", "STR", "INT", "WIS", "CON", "DEX", "AC", "HIT", "DAM", "최대 HP", "최대 MP", "무기 공격",
      "마법 방어", "공격 속성", "방어 속성"
    };
    String[] values = {
      m.job.label,
      "" + m.level,
      "" + f.str,
      "" + f.intel,
      "" + f.wis,
      "" + f.con,
      "" + f.dex,
      "" + f.ac,
      "" + f.hit,
      "" + f.dam,
      "" + f.maxHp,
      "" + f.maxMp,
      f.weaponMinAttack + "~" + f.weaponMaxAttack,
      f.magicDefense + "%",
      f.attackElement,
      f.defenseElement
    };
    for (int i = 0; i < labels.length; i++) {
      float x = 442 + (i % 2) * 250, y = 112 + (i / 2) * 43;
      UiTheme.surface(c, new RectF(x, y, x + 240, y + 37), UiTheme.BG, UiTheme.LINE, 3);
      UiTheme.text(c, labels[i], x + 8, y + 23, 11, UiTheme.MUTED, false);
      UiTheme.right(c, values[i], x + 230, y + 23, 12, UiTheme.TEXT, true);
    }
    UiTheme.text(c, "장비창·필드 외형·전투가 같은 능력치를 사용합니다", 442, 496, 10, UiTheme.MUTED, false);
  }

  boolean touch(float x, float y, RuntimeState state, Actions actions) {
    if (!open) return false;
    for (int i = 0; i < CompanionParty.Job.values().length; i++)
      if (tab(i).contains(x, y)) {
        open(CompanionParty.Job.values()[i]);
        return true;
      }
    if (new RectF(876, 51, 938, 79).contains(x, y)) {
      if (stats) stats = false;
      else open = false;
      return true;
    }
    if (x < 418) {
      ItemWindow.Hit hit =
          equipment.equipmentTouch(
              InventoryPairV262.equipmentX(x),
              InventoryPairV262.equipmentY(y),
              state.party().member(job).growth);
      if (hit == ItemWindow.Hit.CLOSE) open = false;
      if (hit == ItemWindow.Hit.DETAILS) stats = !stats;
      if (hit == ItemWindow.Hit.SELECT || hit == ItemWindow.Hit.CONSUMED) {
        selected = null;
        page = 0;
      }
      if (hit == ItemWindow.Hit.ACTION) actions.change(job, null, equipment.selectedSlot, true);
      return true;
    }
    if (stats) return true;
    List<RpgInventoryPresentation.ItemRow> rows = rows(state);
    for (int i = 0; i < 8; i++)
      if (row(i).contains(x, y)) {
        int n = page * 8 + i;
        if (n < rows.size()) selected = rows.get(n).itemId;
        return true;
      }
    if (new RectF(440, 488, 493, 522).contains(x, y)) {
      page = Math.max(0, page - 1);
      selected = null;
    }
    if (new RectF(681, 488, 734, 522).contains(x, y)) {
      page = Math.min(Math.max(0, (rows.size() - 1) / 8), page + 1);
      selected = null;
    }
    if (equipButton().contains(x, y) && selected != null)
      actions.change(job, selected, equipment.selectedSlot, false);
    return true;
  }
}
