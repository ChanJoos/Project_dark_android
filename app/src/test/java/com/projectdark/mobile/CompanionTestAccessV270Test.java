package com.projectdark.mobile;

import static org.junit.Assert.*;

import android.graphics.*;
import java.io.*;
import java.lang.reflect.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34, manifest = Config.NONE)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public class CompanionTestAccessV270Test {
  final MobileHudV134Test h = new MobileHudV134Test();

  @Before
  public void setup() {
    h.setup();
  }

  GameView testView() {
    GameView v = new GameView(h.c, false, true);
    v.layout(0, 0, 960, 540);
    return v;
  }

  void allSkills(CompanionParty.Member m) {
    assertEquals(99, m.level);
    assertEquals(7, m.circle());
    int learned = 0;
    for (SkillAbilityCatalog.Ability a : SkillAbilityCatalog.all())
      if (a.job.equals(m.job.label)
          && !SkillPolicyV227.retired(a.id)) {
        assertTrue(a.id, m.learned(a.id));
        learned++;
      }
    assertTrue(learned > 20);
  }

  void render(GameView v, String name) throws Exception {
    Bitmap b = Bitmap.createBitmap(960, 540, Bitmap.Config.ARGB_8888);
    v.draw(new Canvas(b));
    File out = new File("build/reports/companion-v270/" + name + ".png");
    out.getParentFile().mkdirs();
    try (FileOutputStream f = new FileOutputStream(out)) {
      b.compress(Bitmap.CompressFormat.PNG, 100, f);
    }
    b.recycle();
  }

  @Test
  public void realApkEntryStartsWith99CompanionBeforeAnyQuestOrJob() throws Exception {
    assertEquals(
        BuildConfig.APPLICATION_ID.matches("com[.]projectdark[.]mobile[.]v[0-9]+test"), BuildConfig.COMPANION_TEST_ACCESS);
    MainActivity a = Robolectric.buildActivity(MainActivity.class).create().get();
    assertFalse(h.field(a, "startupFailed"));
    GameView v = h.field(a, "game");
    v.layout(0, 0, 960, 540);
    RuntimeState s = h.field(v, "state");
    assertEquals("COMMONER", s.rpg().currentJobCode());
    assertEquals(1, s.rpg().normalLevel().intValue());
    if (!BuildConfig.COMPANION_TEST_ACCESS) {
      assertNull(s.party().active());
      return;
    }
    assertNotNull(s.party().active());
    assertEquals(CompanionParty.Job.WARRIOR, s.party().active().job);
    for (CompanionParty.Job j : CompanionParty.Job.values()) allSkills(s.party().member(j));
    assertEquals(s.party().active().body.maxHp, s.party().active().body.hp);
    render(v, "fresh-start-level99");
  }

  @Test
  public void hudCanSelectPriestWithoutProgressAndShowsTestAccess() throws Exception {
    GameView v = testView();
    RuntimeState s = h.field(v, "state");
    long exp = s.rpg().normalExp();
    String quest = s.rpg().campaign().snapshot().toString();
    EquipmentHudV262Test.tap(v, 60, 213);
    EquipmentHudV262Test.tap(v, 470, 205);
    assertEquals(CompanionParty.Job.PRIEST, s.party().active().job);
    allSkills(s.party().active());
    assertEquals(1, s.rpg().normalLevel().intValue());
    assertEquals("COMMONER", s.rpg().currentJobCode());
    assertEquals(exp, s.rpg().normalExp().longValue());
    assertEquals(quest, s.rpg().campaign().snapshot().toString());
    render(v, "priest-all-skills-test-menu");
  }

  @Test
  public void restartAndSkillTestToggleKeep99CompanionWithoutRefillingResources() throws Exception {
    GameView v = testView();
    RuntimeState s = h.field(v, "state");
    assertTrue(s.party().recruit(s, CompanionParty.Job.PRIEST));
    s.party().active().body.hp = 123;
    s.party().active().body.mp = 45;
    s.party().active().cooldown("SK_성직자_029", 3);
    v.setSkillTestMode(true);
    v.setSkillTestMode(false);
    allSkills(s.party().active());
    v.pause();
    F5mSaveStore.install(h.c);
    GameView next = testView();
    RuntimeState restored = h.field(next, "state");
    allSkills(restored.party().active());
    assertEquals(123, restored.party().active().body.hp);
    assertEquals(45, restored.party().active().body.mp);
    assertEquals(3, restored.party().active().cooldown("SK_성직자_029"), .001);
    assertEquals("COMMONER", restored.rpg().currentJobCode());
  }

  @Test
  public void testRecruitmentWorksInFieldAndNormalGameStaysLocked() {
    RuntimeState normal = new RuntimeState();
    assertFalse(normal.party().unlocked(normal));
    assertFalse(normal.party().recruit(normal, CompanionParty.Job.WARRIOR));
    assertNull(normal.party().active());
    RuntimeState field = new RuntimeState(RuntimeState.BootMode.POTE_01_PROTOTYPE);
    field.party().enableTesting(field);
    assertFalse(field.party().town(field));
    assertTrue(field.party().recruit(field, CompanionParty.Job.PRIEST));
    allSkills(field.party().active());
  }

  @Test
  public void learnedTriplePassiveActuallyAddsThreeCompanionBasicHits() {
    RuntimeState s = new RuntimeState();
    s.party().enableTesting(s);
    CompanionParty.Member m = s.party().active();
    RuntimeCombatPortAdapter port =
        new RuntimeCombatPortAdapter(s, (a, t) -> true, (a, id) -> true);
    assertEquals(3, port.hitCount(m.id, RuntimeCombatSession.playerAttackActionId(0)));
    assertEquals(1, port.hitCount("player", RuntimeCombatSession.playerAttackActionId(0)));
    assertTrue(m.learned("SK_전사_023"));
    assertTrue(s.party().recruit(s, CompanionParty.Job.PRIEST));
    assertTrue(s.party().active().learned("SK_성직자_056"));
  }

  @Test
  public void levelDependentHealingUsesCompanion99WhilePlayerStaysOne() {
    RuntimeState s = new RuntimeState();
    s.party().enableTesting(s);
    assertTrue(s.party().recruit(s, CompanionParty.Job.PRIEST));
    CompanionParty.Member m = s.party().active();
    assertEquals(99, m.growth.normalLevel().intValue());
    m.body.x = s.player().x;
    m.body.y = s.player().y;
    m.body.hp = 1;
    SkillAbilityCatalog.Ability a = SkillAbilityCatalog.get("SK_성직자_001");
    int expected =
        SkillAbilityCatalog.amount(
            a,
            new SkillAbilityCatalog.Snapshot(m.stats(), m.body.hp, m.body.mp, m.level, 100, false));
    RuntimeCombatSession c =
        new RuntimeCombatSession(s, (actor, target) -> true, (actor, id) -> true, actor -> true);
    assertTrue(c.submitCompanion(m.id, a.id).accepted());
    c.tick(.3f);
    assertEquals(1 + expected, m.body.hp);
    assertEquals(1, s.rpg().normalLevel().intValue());
  }
}
