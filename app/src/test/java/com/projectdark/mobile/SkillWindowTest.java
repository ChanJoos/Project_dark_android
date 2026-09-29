package com.projectdark.mobile;

import static org.junit.Assert.*;
import android.content.*;
import android.graphics.*;
import android.view.MotionEvent;
import java.lang.reflect.*;
import java.io.*;
import org.json.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk=34,manifest=Config.NONE)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public class SkillWindowTest {
  Context context;
  @Before public void setup(){context=RuntimeEnvironment.getApplication();context.getSharedPreferences("project_dark_f5m_v1",0).edit().clear().commit();F5mSaveStore.install(context);}
  @Test public void catalogPreservesMasterAndCommonerCannotUseOrRegister(){
    SkillBook b=SkillBook.load(context);assertEquals(222,b.entries().size());assertEquals("숏블레이드",b.get("SK_전사_001").name);
    assertEquals(0,b.list(false,true).size());assertFalse(b.usable("cast_proto"));assertFalse(b.assign(0,"cast_proto"));
    assertTrue(b.learn("SK_전사_001",0));assertTrue(b.usable("SK_전사_001"));assertTrue(b.assign(0,"SK_전사_001"));
    assertFalse(b.learn("unknown",10));assertFalse(b.learn("cast_proto",101));
  }
  @Test public void checkpointRestoresLearnedActionsAndSlotsAtomically() throws Exception {
    SkillBook b=SkillBook.load(context);RuntimeState r=new RuntimeState();F5mSaveStore.bindRuntime(r,F5mAdaptedPrologueQuest.openingFixture(),new GrowthQuest2());F5mSaveStore.restoreAndBindSkillsActive(b);
    assertTrue(b.learn("cast_proto",12));assertTrue(b.assign(7,"cast_proto"));assertTrue(F5mSaveStore.checkpointActive());
    F5mSaveStore.install(context);SkillBook restored=SkillBook.load(context);F5mSaveStore.restoreAndBindSkillsActive(restored);
    assertEquals(12,restored.proficiency("cast_proto"));assertEquals("cast_proto",restored.slot(7));assertTrue(restored.usable("cast_proto"));
    JSONObject invalid=b.snapshot();invalid.getJSONArray("slots").put(0,"removed_id");assertFalse(restored.restore(invalid));assertEquals("cast_proto",restored.slot(7));
    SharedPreferences prefs=context.getSharedPreferences("project_dark_f5m_v1",0);prefs.edit().putString("skill_book_v1",invalid.toString()).commit();F5mSaveStore.install(context);F5mSaveStore.restoreAndBindSkillsActive(SkillBook.load(context));assertFalse(F5mSaveStore.writable());assertFalse(F5mSaveStore.checkpointActive());assertEquals(invalid.toString(),prefs.getString("skill_book_v1",""));
  }
  @Test public void selectedSkillAndQuickSlotUseTheSharedCombatGate() throws Exception {
    GameView v=new GameView(context);v.layout(0,0,960,540);SkillBook b=field(v,"skillBook");SkillWindow w=field(v,"skillWindow");RuntimeState r=field(v,"state");CombatController combat=field(v,"combat");RuntimeCombatSession session=field(v,"combatSession");
    RuntimeState.Monster m=r.monsters().get(0);r.player().x=m.x-20;r.player().y=m.y;r.player().mp=20;combat.selectTarget(m);
    assertTrue(b.learn("cast_proto",0));w.open=true;w.magic=true;w.selectedId="cast_proto";
    tap(v,860,432);tap(v,324,485);assertEquals("cast_proto",b.slot(0));w.close();
    Method slotRect=GameView.class.getDeclaredMethod("slotRect",int.class);slotRect.setAccessible(true);RectF slot=(RectF)slotRect.invoke(v,0);tap(v,slot.centerX(),slot.centerY());
    assertTrue("one shared Resolver action",session.playerActionActive());int mp=r.player().mp;tap(v,slot.centerX(),slot.centerY());assertEquals("repeated input does not charge twice",mp,r.player().mp);
  }
  @Test public void modalSelectionLeavesMovementAndTickActiveAndRenders() throws Exception {
    GameView v=new GameView(context);v.layout(0,0,960,540);SkillWindow w=field(v,"skillWindow");tap(v,770,28);assertTrue("fourth utility opens the real window",w.open);
    tap(v,340,180);assertNotNull(w.selectedId);assertEquals(0,w.detailPage);tap(v,780,261);assertEquals(1,w.detailPage);tap(v,840,115);assertTrue(w.learnedOnly);assertNull(w.selectedId);
    RuntimeState r=field(v,"state");RuntimeState.Monster ticking=r.monsters().get(0);ticking.attackCooldown=1f;Method update=GameView.class.getDeclaredMethod("update",float.class);update.setAccessible(true);update.invoke(v,.05f);assertTrue(w.open);assertTrue("world timers continue behind modal",ticking.attackCooldown<1f);
    tap(v,700,115);tap(v,340,180);Bitmap image=Bitmap.createBitmap(960,540,Bitmap.Config.ARGB_8888);v.draw(new Canvas(image));File dir=new File("build/reports/device-review");dir.mkdirs();try(FileOutputStream out=new FileOutputStream(new File(dir,"skill-window.png"))){image.compress(Bitmap.CompressFormat.PNG,100,out);}
    render(v,"skill-window-overview.png");
    tap(v,560,144);assertEquals("도적",w.job);tap(v,340,180);assertNotNull(w.selectedId);assertEquals("도적",bEntry(v,w.selectedId).job);render(v,"skill-window-rogue.png");
    tap(v,540,110);tap(v,870,144);tap(v,340,180);assertTrue(w.magic);assertEquals("성직자",w.job);assertEquals("성직자",bEntry(v,w.selectedId).job);render(v,"skill-window-cleric.png");
    tap(v,840,110);assertTrue(w.learnedOnly);assertNull(w.selectedId);render(v,"skill-window-empty.png");
    tap(v,907,67);assertFalse(w.open);
  }
  @Test public void referenceIconsUseExactIdsAndUnknownDoesNotBorrowArt(){
    SkillIconCatalog icons=new SkillIconCatalog(context);assertTrue(icons.has("SK_마법사_001"));assertTrue(icons.has("SK_도적_003"));assertFalse(icons.has("SK_전사_001"));assertTrue(icons.has("SK_전사_012"));assertTrue(icons.has("SK_공통_001"));assertTrue(icons.has("SK_무도가_002"));assertTrue(icons.has("SK_무도가_032"));assertFalse(icons.has("missing"));
    Bitmap bitmap=Bitmap.createBitmap(60,60,Bitmap.Config.ARGB_8888);assertTrue(icons.draw(new Canvas(bitmap),"SK_마법사_001",new RectF(4,4,44,44)));assertNotEquals(0,bitmap.getPixel(20,20));
  }
  @Test public void captureAcquisitionRequiresJobStatsAndPrerequisiteAndDoesNotCycleDescription() throws Exception {
    GameView v=new GameView(context);v.layout(0,0,960,540);SkillBook b=field(v,"skillBook");SkillWindow w=field(v,"skillWindow");RuntimeState r=field(v,"state");
    SkillAcquisition a=new SkillAcquisition(b);assertFalse(a.learn("SK_무도가_002",r.rpg()));assertFalse(b.learned("SK_무도가_002"));
    Field job=RpgProgressionState.class.getDeclaredField("currentJobCode");job.setAccessible(true);job.set(r.rpg(),"MARTIAL_ARTIST");
    r.rpg().restoreStats(6,3,3,4,3,0);assertFalse(a.learn("SK_무도가_002",r.rpg()));
    r.rpg().restoreStats(23,3,3,19,3,0);w.open=true;w.job="무도가";w.selectedId="SK_무도가_002";
    tap(v,700,432);assertTrue("real learn button grants once",b.learned("SK_무도가_002"));assertEquals(0,b.proficiency("SK_무도가_002"));
    tap(v,860,432);tap(v,324,485);assertEquals("SK_무도가_002",b.slot(0));
    assertFalse("prerequisite proficiency gate",a.learn("SK_무도가_007",r.rpg()));for(int i=0;i<60;i++)b.practiced("SK_무도가_002");assertTrue(a.learn("SK_무도가_007",r.rpg()));
    int page=w.detailPage,offset=w.detailOffset;tap(v,700,320);tap(v,700,320);assertEquals(page,w.detailPage);assertEquals(offset,w.detailOffset);render(v,"skill-window-acquired.png");
    w.selectedId="SK_무도가_007";tap(v,860,432);tap(v,400,485);assertEquals("SK_무도가_007",b.slot(1));tap(v,860,432);tap(v,400,485);assertNull("register same skill explicitly clears",b.slot(1));
  }
  @Test public void realSkillCooldownSharedBetweenDuplicateSlotsAndExpiresWithoutDoubleCharge() throws Exception {
    GameView v=new GameView(context);v.layout(0,0,960,540);SkillBook b=field(v,"skillBook");RuntimeState r=field(v,"state");RuntimeCombatSession session=field(v,"combatSession");
    b.learn("SK_무도가_032",0);b.assign(0,"SK_무도가_032");b.assign(1,"SK_무도가_032");r.player().hp=40;r.player().mp=50;
    Method rect=GameView.class.getDeclaredMethod("slotRect",int.class);rect.setAccessible(true);RectF slot=(RectF)rect.invoke(v,0),duplicate=(RectF)rect.invoke(v,1);
    tap(v,slot.centerX(),slot.centerY());assertEquals(44,r.player().mp);assertEquals(40,r.player().hp);assertTrue(session.cooldownRemaining("player","SK_무도가_032")>0);
    session.tick(.3f);assertEquals(58,r.player().hp);int hp=r.player().hp;tap(v,duplicate.centerX(),duplicate.centerY());assertEquals(44,r.player().mp);assertEquals(hp,r.player().hp);
    SkillWindow w=field(v,"skillWindow");w.open=true;w.magic=true;w.job="무도가";w.selectedId="SK_무도가_032";render(v,"skill-window-cooldown.png");w.close();
    session.tick(1.7f);assertEquals(0,session.cooldownRemaining("player","SK_무도가_032"),.0001f);assertTrue(session.submitPlayer("player","SK_무도가_032").accepted());assertEquals(38,r.player().mp);
    session.tick(.3f);assertEquals(76,r.player().hp);
  }
  @Test public void learnedUnimplementedSkillRegistersAndRestoresWithoutPretendingCombatSupport() throws Exception {
    SkillBook b=SkillBook.load(context);assertTrue(b.learn("SK_무도가_003",0));assertTrue(b.assign(2,"SK_무도가_003"));assertFalse(b.usable("SK_무도가_003"));SkillBook restored=SkillBook.load(context);assertTrue(restored.restore(b.snapshot()));assertEquals("SK_무도가_003",restored.slot(2));
    for(SkillDef d:SkillRuntimeCatalog.definitions())assertNotNull("all adaptation IDs must be real Master IDs",b.get(d.id));
  }
  @Test public void commonerCanActuallyLearnRegisterAndUseBasicAttackThroughProductionInput() throws Exception {
    GameView v=new GameView(context);v.layout(0,0,960,540);SkillBook b=field(v,"skillBook");SkillWindow w=field(v,"skillWindow");RuntimeState r=field(v,"state");CombatController combat=field(v,"combat");RuntimeCombatSession session=field(v,"combatSession");
    w.open=true;w.selectedId="SK_공통_001";tap(v,700,432);assertTrue(b.learned("SK_공통_001"));tap(v,860,432);tap(v,324,485);assertEquals("SK_공통_001",b.slot(0));render(v,"skill-window-commoner-learned.png");w.close();
    RuntimeState.Monster m=r.monsters().get(0);r.player().x=m.x-CanonicalMeleeTileContract.STEP_X;r.player().y=m.y-CanonicalMeleeTileContract.STEP_Y;combat.selectTarget(m);
    Method rect=GameView.class.getDeclaredMethod("slotRect",int.class);rect.setAccessible(true);RectF slot=(RectF)rect.invoke(v,0);tap(v,slot.centerX(),slot.centerY());
    assertTrue(session.playerActionActive());assertTrue(session.cooldownRemaining("player",RuntimeCombatSession.playerAttackActionId(new EquipmentActionResolver().resolveBasicAttack(r.rpg()).animationAction))>0);
    int hp=m.hp;session.tick(.3f);assertTrue(m.hp<hp);assertEquals(90,r.player().mp);
  }
  @Test public void latestCaptureCorrectsStaffIntWisAndWarriorPrerequisitePair() {
    SkillBook b=SkillBook.load(context);RuntimeState r=new RuntimeState();SkillAcquisition a=new SkillAcquisition(b);
    r.rpg().restoreStats(3,3,6,3,3,0);assertFalse(a.learn("SK_공통_014",r.rpg()));
    r.rpg().restoreStats(3,6,3,3,3,0);assertTrue(a.learn("SK_공통_014",r.rpg()));
    assertTrue(b.captureConditions("SK_전사_009").contains("Required_Prerequisite_Level 90;90"));
  }
  private static SkillBook.Entry bEntry(GameView v,String id)throws Exception{return ((SkillBook)field(v,"skillBook")).get(id);}
  private static void render(GameView v,String name)throws Exception{Bitmap image=Bitmap.createBitmap(960,540,Bitmap.Config.ARGB_8888);v.draw(new Canvas(image));File dir=new File("build/reports/device-review");dir.mkdirs();try(FileOutputStream out=new FileOutputStream(new File(dir,name))){image.compress(Bitmap.CompressFormat.PNG,100,out);}}
  @SuppressWarnings("unchecked") private static <T>T field(Object o,String name)throws Exception{Field f=o.getClass().getDeclaredField(name);f.setAccessible(true);return(T)f.get(o);}
  private static void tap(GameView v,float x,float y){MotionEvent e=MotionEvent.obtain(0,0,MotionEvent.ACTION_DOWN,x,y,0);v.onTouchEvent(e);e.recycle();}
}

