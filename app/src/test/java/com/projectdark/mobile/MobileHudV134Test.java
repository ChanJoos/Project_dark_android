package com.projectdark.mobile;
import static org.junit.Assert.*;
import android.content.Context;
import android.graphics.*;
import android.view.MotionEvent;
import com.projectdark.mobile.world.WorldRuntimeAdapter;
import java.io.*;
import java.lang.reflect.*;
import org.json.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.*;
@RunWith(RobolectricTestRunner.class) @Config(sdk=34,manifest=Config.NONE) @GraphicsMode(GraphicsMode.Mode.NATIVE)
public final class MobileHudV134Test {
 Context c;
 @Before public void setup(){c=RuntimeEnvironment.getApplication();reset();}
 void reset(){c.getSharedPreferences("project_dark_f5m_v1",0).edit().clear().commit();c.getSharedPreferences("project_dark_skill_test_v1",0).edit().clear().commit();F5mSaveStore.install(c);}
 GameView view(){GameView v=new GameView(c);v.layout(0,0,960,540);return v;}
 @Test public void legacyEightSlotsMigrateWithoutGrantingOrLosingSkills()throws Exception{
   SkillBook b=SkillBook.load(c);assertTrue(b.learn("cast_proto",12));assertTrue(b.assign(7,"cast_proto"));JSONObject legacy=b.snapshot();JSONArray eight=new JSONArray();for(int i=0;i<8;i++)eight.put(legacy.getJSONArray("slots").get(i));legacy.put("slots",eight);
   SkillBook restored=SkillBook.load(c);assertTrue(restored.restore(legacy));assertEquals("cast_proto",restored.slot(7));assertNull(restored.slot(8));assertNull(restored.slot(9));assertEquals(12,restored.proficiency("cast_proto"));assertEquals(10,restored.snapshot().getJSONArray("slots").length());
   JSONObject corrupt=restored.snapshot();corrupt.getJSONArray("slots").put(9,"unknown");assertFalse(restored.restore(corrupt));assertEquals("cast_proto",restored.slot(7));
 }
 @Test public void ninthAndTenthSkillsRegisterCastAndPersistThroughRealInput()throws Exception{
   for(int index=8;index<10;index++){reset();GameView v=view();RuntimeState r=field(v,"state");r.rpg().restoreProgression(3,0);assertTrue(r.rpg().restoreJobCode("MAGE"));SkillBook b=field(v,"skillBook");assertTrue(b.learn("SK_마법사_001",0));SkillWindow w=field(v,"skillWindow");w.open=true;w.selectedId="SK_마법사_001";
     tap(v,REGISTER_CENTER_X,400);RectF cell=SkillWindow.bookSlot(index);tap(v,cell.centerX(),cell.centerY());assertEquals("SK_마법사_001",b.slot(index));w.close();
     RuntimeState.Monster m=r.monsters().get(0);r.player().x=m.x-32;r.player().y=m.y-16;r.player().mp=20;((CombatController)field(v,"combat")).selectTarget(m);((WorldRuntimeAdapter)field(v,"worldAdapter")).snapCameraToPlayer();
     RectF slot=slot(v,index);tap(v,slot.centerX(),slot.centerY());RuntimeCombatSession session=field(v,"combatSession");assertTrue("slot "+(index+1)+" starts the real shared action",session.playerActionActive());int mp=r.player().mp;tap(v,slot.centerX(),slot.centerY());assertEquals(mp,r.player().mp);
     v.pause();F5mSaveStore.install(c);GameView restarted=view();SkillBook saved=field(restarted,"skillBook");assertEquals("SK_마법사_001",saved.slot(index));
   }
 }
 static final float REGISTER_CENTER_X=850;
 @Test public void centralPotionConsumesOnlyWhenNeededAndPersists()throws Exception{
   GameView v=view();RuntimeState r=field(v,"state");assertTrue(r.rpg().grantB5SmallHpPotion(5));r.player().hp=Math.max(1,r.player().maxHp-10);int before=r.rpg().inventory().get(RpgProgressionState.B_SMALL_POTION_ITEM_ID);RectF p=MobileHudLayout.potion(0,0);tap(v,p.centerX(),p.centerY());assertEquals(before-1,r.rpg().inventory().get(RpgProgressionState.B_SMALL_POTION_ITEM_ID).intValue());assertEquals(r.player().maxHp,r.player().hp);tap(v,p.centerX(),p.centerY());assertEquals(before-1,r.rpg().inventory().get(RpgProgressionState.B_SMALL_POTION_ITEM_ID).intValue());
   v.pause();F5mSaveStore.install(c);RuntimeState saved=field(view(),"state");assertEquals(before-1,saved.rpg().inventory().get(RpgProgressionState.B_SMALL_POTION_ITEM_ID).intValue());
 }
 @Test public void boundsTrackVisibleHudInNormalAndWideViewports()throws Exception{
   for(float d:new float[]{0,210}){assertTrue(GameView.blocksWorldTapForHud(68,73,false,d));assertTrue(GameView.blocksWorldTapForHud(790+d,104,false,d));assertTrue(GameView.blocksWorldTapForHud(456+d/2,515,false,d));assertFalse("old quest no longer intercepts the world",GameView.blocksWorldTapForHud(140,150,false,d));assertFalse("old status no longer intercepts the world",GameView.blocksWorldTapForHud(200,60,false,d));assertFalse(GameView.blocksWorldTapForHud(400+d/2,350,false,d));GameView v=view();for(int i=0;i<10;i++){RectF r=slot(v,i);assertTrue(GameView.blocksWorldTapForHud(r.centerX()+d,r.centerY(),false,d));}}
 }
 @Test public void renderTenOriginalSkillIconsTownForestAndWideHud()throws Exception{
   for(int[] size:new int[][]{{960,540},{2340,1080}}){reset();GameView v=new GameView(c);v.layout(0,0,size[0],size[1]);v.setSkillTestMode(true);render(v,"town",size[0],size[1]);Method enter=GameView.class.getDeclaredMethod("enterPoteField");enter.setAccessible(true);enter.invoke(v);render(v,"forest",size[0],size[1]);}
 }
 static void render(GameView v,String scene,int w,int h)throws Exception{for(String name:new String[]{"feedbackClock","rewardClock"}){Field clock=GameView.class.getDeclaredField(name);clock.setAccessible(true);clock.setFloat(v,0);}Bitmap b=Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888);v.draw(new Canvas(b));File dir=new File("build/reports/mobile-hud-v134");dir.mkdirs();try(FileOutputStream f=new FileOutputStream(new File(dir,scene+"-"+w+"x"+h+".png"))){assertTrue(b.compress(Bitmap.CompressFormat.PNG,100,f));}b.recycle();}
 static RectF slot(GameView v,int i)throws Exception{Method m=GameView.class.getDeclaredMethod("slotRect",int.class);m.setAccessible(true);return (RectF)m.invoke(v,i);}
 static void tap(GameView v,float x,float y){MotionEvent e=MotionEvent.obtain(0,1,MotionEvent.ACTION_DOWN,x,y,0);v.onTouchEvent(e);e.recycle();}
 @SuppressWarnings("unchecked") static <T>T field(Object o,String name)throws Exception{Field f=o.getClass().getDeclaredField(name);f.setAccessible(true);return (T)f.get(o);}
}
