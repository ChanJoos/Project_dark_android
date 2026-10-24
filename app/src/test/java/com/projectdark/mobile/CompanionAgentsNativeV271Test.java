package com.projectdark.mobile;

import static org.junit.Assert.*;

import android.graphics.*;
import java.io.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34, manifest = Config.NONE)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public class CompanionAgentsNativeV271Test {
  final MobileHudV134Test h = new MobileHudV134Test();
  final CompanionNativeV269Test frames = new CompanionNativeV269Test();

  @Before
  public void setup() {
    h.setup();
  }

  void render(GameView v, String name, int w, int height) throws Exception {
    Bitmap b = Bitmap.createBitmap(w, height, Bitmap.Config.ARGB_8888);
    v.draw(new Canvas(b));
    File out = new File("build/reports/companion-v271/" + name + ".png");
    out.getParentFile().mkdirs();
    try (FileOutputStream stream = new FileOutputStream(out)) {
      b.compress(Bitmap.CompressFormat.PNG, 100, stream);
    }
    b.recycle();
  }

  @Test
  public void closedManagementDrawsOnlyWithinTheCompanionHud() {
    CompanionWindow window = new CompanionWindow();
    RuntimeState state = new RuntimeState();
    state.party().enableTesting(state);
    Bitmap bitmap = Bitmap.createBitmap(960, 540, Bitmap.Config.ARGB_8888);
    Canvas canvas = new Canvas(bitmap);
    window.drawHud(canvas, state);
    window.draw(canvas, state, 0);
    RectF hud = CompanionWindow.hud();
    for (int y = 0; y < bitmap.getHeight(); y++)
      for (int x = 0; x < bitmap.getWidth(); x++)
        if (x < hud.left - 2 || x > hud.right + 2 || y < hud.top - 2 || y > hud.bottom + 2)
          assertEquals(
              "Closed window leaked outside HUD at " + x + "," + y,
              0,
              Color.alpha(bitmap.getPixel(x, y)));
    bitmap.recycle();
  }

  @Test
  public void allFiveJobsAreReachableThroughRealTouchAtBothScreenWidths() throws Exception {
    CompanionParty.Job[] jobs = {
      CompanionParty.Job.WARRIOR,
      CompanionParty.Job.PRIEST,
      CompanionParty.Job.ROGUE,
      CompanionParty.Job.MAGE,
      CompanionParty.Job.MONK
    };
    int[][] buttons = {{320, 205}, {470, 205}, {320, 244}, {470, 244}, {620, 244}};
    for (int[] size : new int[][] {{960, 540}, {2340, 1080}}) {
      h.reset();
      GameView v = new GameView(h.c, false, true);
      v.layout(0, 0, size[0], size[1]);
      RuntimeState s = h.field(v, "state");
      String quests = s.rpg().campaign().snapshot().toString();
      float scale = size[1] / 540f, offset = (size[0] / scale - 960) / 2;
      EquipmentHudV262Test.tap(v, 60 * scale, 213 * scale);
      for (int i = 0; i < jobs.length; i++) {
        EquipmentHudV262Test.tap(v, (buttons[i][0] + offset) * scale, buttons[i][1] * scale);
        assertEquals(jobs[i], s.party().active().job);
        assertEquals(99, s.party().active().level);
        assertEquals("COMMONER", s.rpg().currentJobCode());
        assertEquals(1, s.rpg().normalLevel().intValue());
        assertEquals(quests, s.rpg().campaign().snapshot().toString());
        render(v, "menu-" + jobs[i] + "-" + size[0], size[0], size[1]);
        EquipmentHudV262Test.tap(v, (680 + offset) * scale, 145 * scale);
        frames.tick(v, .1f);
        assertEquals(s.currentMapId(), s.party().active().map);
        assertFalse(
            CharacterVisualBinding.from(s.party().active().growth).equipmentVisualRef().isEmpty());
        render(v, "world-" + jobs[i] + "-" + size[0], size[0], size[1]);
        EquipmentHudV262Test.tap(v, 60 * scale, 213 * scale);
      }
    }
  }

  @Test
  public void newProfessionsRestoreRealSaveWithoutRefillingOrCompletingQuests() throws Exception {
    for (CompanionParty.Job job :
        new CompanionParty.Job[] {
          CompanionParty.Job.ROGUE, CompanionParty.Job.MAGE, CompanionParty.Job.MONK
        }) {
      h.reset();
      GameView v = new GameView(h.c, false, true);
      v.layout(0, 0, 960, 540);
      RuntimeState s = h.field(v, "state");
      assertTrue(s.party().recruit(s, job));
      CompanionParty.Member m = s.party().active();
      m.body.hp = 123;
      m.body.mp = 45;
      m.cooldown("agent_retreat", 2);
      s.skillEffects().put(m.id, "SK_성직자_028", "PROTECT", 30, 8);
      v.pause();
      F5mSaveStore.install(h.c);
      GameView next = new GameView(h.c, false, true);
      RuntimeState restored = h.field(next, "state");
      assertEquals(job, restored.party().active().job);
      assertEquals(123, restored.party().active().body.hp);
      assertEquals(45, restored.party().active().body.mp);
      assertEquals(2, restored.party().active().cooldown("agent_retreat"), .001);
      assertTrue(restored.skillEffects().has(restored.party().active().id, "PROTECT"));
      assertEquals(1, restored.rpg().normalLevel().intValue());
    }
  }

  @Test
  public void actualViewControllerDrivesEachNewProfessionIntoItsOwnActions() throws Exception {
    for (CompanionParty.Job job :
        new CompanionParty.Job[] {
          CompanionParty.Job.ROGUE, CompanionParty.Job.MAGE, CompanionParty.Job.MONK
        }) {
      h.reset();
      GameView v = new GameView(h.c, false, true);
      v.layout(0, 0, 960, 540);
      RuntimeState s = h.field(v, "state");
      assertTrue(s.party().recruit(s, job));
      CompanionParty.Member m = s.party().active();
      frames.tick(v, .1f);
      RuntimeState.Monster enemy = s.monsters().get(0);
      enemy.x = m.body.x + 32;
      enemy.y = m.body.y + 16;
      enemy.hp = 200000;
      enemy.alive = true;
      enemy.provoked = true;
      s.party().order(CompanionParty.Order.FOCUS, enemy.id);
      for (int i = 0; i < 20 && m.actionId.isEmpty(); i++) frames.tick(v, .1f);
      assertFalse(job.name(), m.actionId.isEmpty());
      assertEquals(job.label, SkillAbilityCatalog.get(m.actionId).job);
      render(v, "active-agent-" + job, 960, 540);
    }
  }
}
