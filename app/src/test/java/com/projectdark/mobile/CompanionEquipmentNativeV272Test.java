package com.projectdark.mobile;

import static org.junit.Assert.*;

import android.graphics.*;
import java.io.*;
import java.lang.reflect.Method;
import java.util.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34, manifest = Config.NONE)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public class CompanionEquipmentNativeV272Test {
  final MobileHudV134Test h = new MobileHudV134Test();
  final CompanionNativeV269Test frames = new CompanionNativeV269Test();
  final CompanionEquipmentV272Test domain = new CompanionEquipmentV272Test();

  @Before
  public void setup() {
    h.setup();
  }

  GameView view(int w, int height) throws Exception {
    GameView v = new GameView(h.c, false, true);
    v.layout(0, 0, w, height);
    frames.tick(v, .1f);
    return v;
  }

  void tap(GameView v, float x, float y, float scale, float offset) {
    EquipmentHudV262Test.tap(v, (x + offset) * scale, y * scale);
  }

  void open(GameView v, float scale, float offset) throws Exception {
    EquipmentHudV262Test.tap(v, 60 * scale, 213 * scale);
    tap(v, 630, 395, scale, offset);
    CompanionEquipmentWindow w = h.field(v, "companionEquipment");
    assertTrue(w.open);
    assertFalse(((CompanionWindow) h.field(v, "companionWindow")).open);
  }

  void chooseSlot(GameView v, CompanionEquipmentWindow w, String slot, float scale, float offset)
      throws Exception {
    int index = Arrays.asList(ItemWindow.SLOTS).indexOf(slot);
    assertTrue(index >= 0);
    RectF r = ItemWindow.slot(index);
    tap(
        v,
        InventoryPairV262.EQUIP_X + r.centerX() * InventoryPairV262.EQUIP_SCALE,
        InventoryPairV262.EQUIP_Y + r.centerY() * InventoryPairV262.EQUIP_SCALE,
        scale,
        offset);
    assertEquals(slot, w.equipment.selectedSlot);
  }

  String firstAvailable(RuntimeState s, CompanionEquipmentWindow w) {
    for (RpgInventoryPresentation.ItemRow r : w.rows(s))
      if (r.requirementResult == RpgProgressionState.RequirementResult.MET
          && !r.itemId.equals(
              s.party().member(w.job).growth.equipment().get(w.equipment.selectedSlot)))
        return r.itemId;
    throw new AssertionError(w.job + " " + w.equipment.selectedSlot);
  }

  void equip(
      GameView v,
      RuntimeState s,
      CompanionEquipmentWindow w,
      String id,
      float scale,
      float offset) {
    int index = -1;
    List<RpgInventoryPresentation.ItemRow> rows = w.rows(s);
    for (int i = 0; i < rows.size(); i++) if (rows.get(i).itemId.equals(id)) index = i;
    assertTrue(index >= 0);
    w.page = index / 8;
    RectF row = CompanionEquipmentWindow.row(index % 8);
    tap(v, row.centerX(), row.centerY(), scale, offset);
    assertEquals(id, w.selected);
    RectF action = CompanionEquipmentWindow.equipButton();
    tap(v, action.centerX(), action.centerY(), scale, offset);
    assertEquals(id, s.party().member(w.job).growth.equipment().get(w.equipment.selectedSlot));
  }

  Bitmap render(GameView v, int width, int height, String name) throws Exception {
    Bitmap b = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
    v.draw(new Canvas(b));
    save(b, name);
    return b;
  }

  void save(Bitmap b, String name) throws Exception {
    File out = new File("build/reports/companion-v272/" + name + ".png");
    out.getParentFile().mkdirs();
    try (FileOutputStream stream = new FileOutputStream(out)) {
      b.compress(Bitmap.CompressFormat.PNG, 100, stream);
    }
  }

  Bitmap worldActor(GameView v, CompanionParty.Member m) throws Exception {
    Bitmap b = Bitmap.createBitmap(400, 300, Bitmap.Config.ARGB_8888);
    Canvas c = new Canvas(b);
    c.translate(200 - m.body.x, 200 - m.body.y);
    Method draw = GameView.class.getDeclaredMethod("drawCompanion", Canvas.class);
    draw.setAccessible(true);
    draw.invoke(v, c);
    return b;
  }

  int difference(Bitmap a, Bitmap b, int left, int top, int right, int bottom) {
    int n = 0;
    for (int y = top; y < bottom; y++)
      for (int x = left; x < right; x++) if (a.getPixel(x, y) != b.getPixel(x, y)) n++;
    return n;
  }

  @Test
  public void realHudOpensOwnEquipmentAndAppliesGearAtBothWidths() throws Exception {
    for (int[] size : new int[][] {{960, 540}, {2340, 1080}}) {
      h.reset();
      GameView v = view(size[0], size[1]);
      RuntimeState s = h.field(v, "state");
      CompanionParty.Member m = s.party().active();
      float scale = size[1] / 540f, offset = (size[0] / scale - 960) / 2;
      Map<String, String> player = new LinkedHashMap<>(s.rpg().equipment());
      String quests = s.rpg().campaign().snapshot().toString();
      open(v, scale, offset);
      CompanionEquipmentWindow w = h.field(v, "companionEquipment");
      chooseSlot(v, w, "갑옷", scale, offset);
      String id = domain.armor(s, CompanionParty.Job.WARRIOR, 99);
      Bitmap before = render(v, size[0], size[1], "before-" + size[0]);
      Bitmap oldActor = worldActor(v, m);
      equip(v, s, w, id, scale, offset);
      assertEquals(player, s.rpg().equipment());
      assertEquals(quests, s.rpg().campaign().snapshot().toString());
      assertEquals(1, s.rpg().normalLevel().intValue());
      Bitmap after = render(v, size[0], size[1], "equipped-" + size[0]);
      int left = (int) ((180 + offset) * scale),
          right = (int) ((245 + offset) * scale),
          top = (int) (190 * scale),
          bottom = (int) (285 * scale);
      assertTrue(
          "actual companion paper doll changes",
          difference(before, after, left, top, right, bottom) > 0);
      Bitmap actor = worldActor(v, m);
      assertTrue(
          "same equipment changes actual field actor",
          difference(oldActor, actor, 155, 150, 245, 205) > 0);
      save(actor, "field-armor-" + size[0]);
      before.recycle();
      after.recycle();
      oldActor.recycle();
      actor.recycle();
      // The source stat button opens this companion's live stats, not the player stat page.
      RectF stats = ItemWindow.equipmentStats();
      tap(
          v,
          InventoryPairV262.EQUIP_X + stats.centerX() * InventoryPairV262.EQUIP_SCALE,
          InventoryPairV262.EQUIP_Y + stats.centerY() * InventoryPairV262.EQUIP_SCALE,
          scale,
          offset);
      assertTrue(w.stats);
      render(v, size[0], size[1], "stats-" + size[0]).recycle();
    }
  }

  @Test
  public void equippedArmorKeepsOriginalActionLayersInAllFourDirections() throws Exception {
    GameView v = view(960, 540);
    RuntimeState s = h.field(v, "state");
    CompanionParty.Member m = s.party().active();
    m.actionId = "SK_전사_001";
    m.actionClock = .2f;
    Map<CharacterRenderer.Direction, Bitmap> before =
        new EnumMap<>(CharacterRenderer.Direction.class);
    for (CharacterRenderer.Direction direction : CharacterRenderer.Direction.values()) {
      m.facing = direction;
      before.put(direction, worldActor(v, m));
    }
    open(v, 1, 0);
    CompanionEquipmentWindow w = h.field(v, "companionEquipment");
    equip(v, s, w, domain.armor(s, CompanionParty.Job.WARRIOR, 99), 1, 0);
    for (CharacterRenderer.Direction direction : CharacterRenderer.Direction.values()) {
      m.facing = direction;
      Bitmap after = worldActor(v, m);
      assertTrue(
          "worn source action layer " + direction,
          difference(before.get(direction), after, 155, 150, 245, 205) > 0);
      save(before.get(direction), "action-before-" + direction);
      save(after, "action-equipped-" + direction);
      before.get(direction).recycle();
      after.recycle();
    }
  }

  @Test
  public void fiveJobTabsEditInactiveMembersAndActualRestartKeepsEquipment() throws Exception {
    GameView v = view(960, 540);
    RuntimeState s = h.field(v, "state");
    open(v, 1, 0);
    CompanionEquipmentWindow w = h.field(v, "companionEquipment");
    Map<CompanionParty.Job, Map<String, String>> gear = new EnumMap<>(CompanionParty.Job.class);
    for (int i = 0; i < CompanionParty.Job.values().length; i++) {
      CompanionParty.Job job = CompanionParty.Job.values()[i];
      RectF tab = CompanionEquipmentWindow.tab(i);
      tap(v, tab.centerX(), tab.centerY(), 1, 0);
      assertEquals(job, w.job);
      String slot = job == CompanionParty.Job.PRIEST ? "모자" : "갑옷";
      chooseSlot(v, w, slot, 1, 0);
      String id = firstAvailable(s, w);
      equip(v, s, w, id, 1, 0);
      gear.put(job, new LinkedHashMap<>(s.party().member(job).growth.equipment()));
      render(v, 960, 540, "job-" + job).recycle();
      assertEquals(CompanionParty.Job.WARRIOR, s.party().active().job);
    }
    s.party().active().body.hp = 123;
    s.party().active().body.mp = 45;
    v.pause();
    F5mSaveStore.install(h.c);
    GameView next = new GameView(h.c, false, true);
    RuntimeState restored = h.field(next, "state");
    assertTrue(F5mSaveStore.writable());
    for (CompanionParty.Job job : CompanionParty.Job.values())
      assertEquals(gear.get(job), restored.party().member(job).growth.equipment());
    assertEquals(123, restored.party().active().body.hp);
    assertEquals(45, restored.party().active().body.mp);
  }

  @Test
  public void unknownAndWrongJobItemsFailClosedAndFemaleGearUsesSourceBody() throws Exception {
    GameView v = view(960, 540);
    RuntimeState s = h.field(v, "state");
    open(v, 1, 0);
    CompanionEquipmentWindow w = h.field(v, "companionEquipment");
    RectF mage = CompanionEquipmentWindow.tab(3);
    tap(v, mage.centerX(), mage.centerY(), 1, 0);
    String warrior = domain.armor(s, CompanionParty.Job.WARRIOR, 99);
    assertEquals(
        RpgProgressionState.RequirementResult.JOB_NOT_MET,
        CompanionEquipment.requirement(s.party().member(w.job), warrior));
    Map<String, String> before = new LinkedHashMap<>(s.party().member(w.job).growth.equipment());
    equipAttempt(v, s, w, warrior);
    assertEquals(before, s.party().member(w.job).growth.equipment());
    String female = null;
    for (RpgProgressionState.ItemDefinition d : s.rpg().itemDefinitions().values())
      if (d.appearanceId != null
          && d.appearanceId.startsWith("wu")
          && "갑옷".equals(d.equipSlot)
          && CompanionEquipment.requirement(s.party().member(w.job), d.itemId)
              == RpgProgressionState.RequirementResult.MET
          && CompanionEquipment.available(s, d.itemId) > 0) {
        female = d.itemId;
        break;
      }
    if (female != null) {
      equip(v, s, w, female, 1, 0);
      assertEquals(
          "wm001",
          CharacterVisualBinding.from(s.party().member(w.job).growth).sourceBodyIdentity("mm001"));
      render(v, 960, 540, "female-source-body").recycle();
    }
    tap(v, 906, 65, 1, 0);
    assertFalse(w.open);
    render(v, 960, 540, "closed-clean").recycle();
  }

  void equipAttempt(GameView v, RuntimeState s, CompanionEquipmentWindow w, String id) {
    int index = -1;
    List<RpgInventoryPresentation.ItemRow> rows = w.rows(s);
    for (int i = 0; i < rows.size(); i++) if (rows.get(i).itemId.equals(id)) index = i;
    assertTrue(index >= 0);
    w.page = index / 8;
    RectF r = CompanionEquipmentWindow.row(index % 8);
    tap(v, r.centerX(), r.centerY(), 1, 0);
    tap(v, 850, 505, 1, 0);
  }
}
