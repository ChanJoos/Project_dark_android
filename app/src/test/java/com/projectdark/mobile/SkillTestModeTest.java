package com.projectdark.mobile;

import static org.junit.Assert.*;
import android.content.*;
import android.graphics.*;
import android.view.MotionEvent;
import java.io.*;
import java.lang.reflect.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.*;

@RunWith(RobolectricTestRunner.class) @Config(sdk=34,manifest=Config.NONE) @GraphicsMode(GraphicsMode.Mode.NATIVE)
public class SkillTestModeTest {
  Context c;
  @Before public void setup(){c=RuntimeEnvironment.getApplication();c.getSharedPreferences("project_dark_f5m_v1",0).edit().clear().commit();c.getSharedPreferences("project_dark_skill_test_v1",0).edit().clear().commit();F5mSaveStore.install(c);}
  @Test public void realActivityDefaultsToAllLearnedAndPreferenceSurvivesRestart()throws Exception{
    org.robolectric.android.controller.ActivityController<MainActivity> a=Robolectric.buildActivity(MainActivity.class).create();GameView v=field(a.get(),"game");SkillBook b=field(v,"skillBook");assertTrue(b.testAccess());int count=0;for(SkillBook.Entry e:b.entries())if(b.previewable(e.id)){count++;assertTrue(e.id,b.learned(e.id));assertTrue(e.id,b.usable(e.id));}assertEquals(221,count);
    assertEquals(0,b.snapshot().getJSONObject("learned").length());v.setSkillTestMode(false);a.destroy();org.robolectric.android.controller.ActivityController<MainActivity> restarted=Robolectric.buildActivity(MainActivity.class).create();SkillBook restored=field(field(restarted.get(),"game"),"skillBook");assertFalse(restored.testAccess());restarted.destroy();
  }
  @Test public void normalCommonerCannotLearnOrUseMartialAndOverlayPreservesNormalBook()throws Exception{
    GameView v=new GameView(c);SkillBook b=field(v,"skillBook");RuntimeState s=field(v,"state");s.rpg().restoreStats(99,99,99,99,99,0);s.rpg().restoreGold(100000);
    assertFalse(new SkillAcquisition(b).learn("SK_무도가_002",s.rpg()));b.learn("SK_무도가_002",12);b.assign(0,"SK_무도가_002");String before=b.snapshot().toString();assertFalse(b.usable("SK_무도가_002"));use(v,b.get("SK_무도가_002"));assertNull(field(v,"activeSkillVisualId"));
    v.setSkillTestMode(true);assertTrue(b.usable("SK_무도가_002"));b.assign(0,"SK_마법사_043");b.practiced("SK_마법사_043");assertEquals(before,b.snapshot().toString());v.setSkillTestMode(false);assertEquals(before,b.snapshot().toString());assertEquals("SK_무도가_002",b.slot(0));assertFalse(b.learned("SK_마법사_043"));
  }
  @Test public void lowMpActualMagicAndAllUnsupportedMageClericVisualsAreReachable()throws Exception{
    GameView v=new GameView(c);v.layout(0,0,960,540);v.setSkillTestMode(true);SkillBook b=field(v,"skillBook");RuntimeState s=field(v,"state");CombatController combat=field(v,"combat");RuntimeState.Monster m=s.monsters().get(0);s.player().x=m.x-32;s.player().y=m.y-16;s.player().mp=0;combat.selectTarget(m);((com.projectdark.mobile.world.WorldRuntimeAdapter)field(v,"worldAdapter")).snapCameraToPlayer();
    use(v,b.get("SK_마법사_005"));assertEquals(0,s.player().mp);tick(v,.27f);assertTrue(((SkillVfxRenderer)field(v,"skillVfx")).pulses.stream().anyMatch(p->!p.caster));assertFalse(((RuntimeCombatSession)field(v,"combatSession")).playerActionActive());
    Method update=GameView.class.getDeclaredMethod("update",float.class);update.setAccessible(true);update.invoke(v,.7f);int tested=0;SkillPresentationCatalog catalog=field(v,"skillPresentation");
    for(SkillBook.Entry e:b.entries())if((e.job.equals("마법사")||e.job.equals("성직자"))&&e.runtime==null&&SkillActionContract.get(e.id).presentationAllowed()){
      int hp=m.hp,playerHp=s.player().hp,mp=s.player().mp;use(v,e);assertEquals(e.id,field(v,"activeSkillVisualId"));assertTrue(((RuntimeCombatSession)field(v,"combatSession")).playerActionActive());tick(v,.11f);
      if(e.id.equals("SK_마법사_012")){time(v,.11f);render(v,"skill-test-poison-caster.png");}tick(v,.15f);
      assertEquals(hp,m.hp);assertEquals(playerHp,s.player().hp);assertEquals(mp,s.player().mp);assertEquals("CAST",catalog.get(e.id).motion);
      if(e.id.equals("SK_마법사_012")){time(v,.26f);render(v,"skill-test-poison-target.png");}tick(v,.8f);idle(v);tested++;
    }assertTrue(tested>50);assertEquals("f",catalog.profiles.getJSONObject("CAST").getString("group"));
    SkillWindow w=field(v,"skillWindow");w.open=true;w.magic=true;w.job="성직자";w.selectedId="SK_성직자_029";render(v,"skill-test-cleric-all.png");
  }
  @Test public void productionSlotRegistrationPersistsOutsideNormalSaveAndToggleRestoresNormal()throws Exception{
    GameView v=new GameView(c);v.layout(0,0,960,540);v.setSkillTestMode(true);SkillWindow w=field(v,"skillWindow");SkillBook b=field(v,"skillBook");w.open=true;w.magic=true;w.selectedId="SK_마법사_043";tap(v,760,425);tap(v,155,485);assertEquals("SK_마법사_043",b.slot(0));assertEquals(0,b.snapshot().getJSONObject("learned").length());
    GameView restarted=new GameView(c);restarted.setSkillTestMode(true);SkillBook rb=field(restarted,"skillBook");assertEquals("SK_마법사_043",rb.slot(0));tap(v,420,50);assertFalse(b.testAccess());assertNull(b.slot(0));assertFalse(c.getSharedPreferences("project_dark_skill_test_v1",0).getBoolean("enabled",true));
  }
  private static void time(GameView v,float t)throws Exception{Field f=GameView.class.getDeclaredField("actionClock");f.setAccessible(true);f.setFloat(v,t);}
  @SuppressWarnings({"unchecked","rawtypes"}) private static void idle(GameView v)throws Exception{Field f=GameView.class.getDeclaredField("action");f.setAccessible(true);f.set(v,Enum.valueOf((Class)f.getType(),"IDLE"));time(v,0);}
  private static void use(GameView v,SkillBook.Entry e)throws Exception{Method m=GameView.class.getDeclaredMethod("useBookSkill",SkillBook.Entry.class);m.setAccessible(true);m.invoke(v,e);}
  private static void tick(GameView v,float t)throws Exception{Method m=GameView.class.getDeclaredMethod("tickSkillCombat",float.class);m.setAccessible(true);m.invoke(v,t);}
  private static void tap(GameView v,float x,float y){MotionEvent e=MotionEvent.obtain(0,0,MotionEvent.ACTION_DOWN,x,y,0);v.onTouchEvent(e);e.recycle();}
  @SuppressWarnings("unchecked") private static <T>T field(Object o,String name)throws Exception{Field f=o.getClass().getDeclaredField(name);f.setAccessible(true);return(T)f.get(o);}
  private static void render(GameView v,String name)throws Exception{Bitmap b=Bitmap.createBitmap(960,540,Bitmap.Config.ARGB_8888);v.draw(new Canvas(b));File d=new File("build/reports/device-review");d.mkdirs();try(FileOutputStream out=new FileOutputStream(new File(d,name))){b.compress(Bitmap.CompressFormat.PNG,100,out);}}
}
