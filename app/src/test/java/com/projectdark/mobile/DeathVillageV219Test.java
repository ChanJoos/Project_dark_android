package com.projectdark.mobile;
import android.content.Context;import android.graphics.*;
import com.projectdark.mobile.world.*;
import org.junit.*;import org.junit.runner.RunWith;import org.robolectric.*;import org.robolectric.annotation.*;
import static org.junit.Assert.*;
@RunWith(RobolectricTestRunner.class) @Config(sdk=34,manifest=Config.NONE) @GraphicsMode(GraphicsMode.Mode.NATIVE)
public class DeathVillageV219Test {
 MineTownV203Test f;Context c;
 @Before public void setup(){f=new MineTownV203Test();f.reset();c=RuntimeEnvironment.getApplication();}
 @Test public void realActivityMigratesOldTenSlotsToRequestedFourAndKeepsLaterEditsOnRestart()throws Exception{
  org.json.JSONArray previous=new org.json.JSONArray();for(int i=0;i<10;i++)previous.put("SK_마법사_001");c.getSharedPreferences("project_dark_skill_test_v1",0).edit().putBoolean("enabled",true).putString("slots",previous.toString()).commit();
  org.robolectric.android.controller.ActivityController<MainActivity> a=Robolectric.buildActivity(MainActivity.class).create();GameView v=f.field(a.get(),"game");v.layout(0,0,960,540);SkillBook b=f.field(v,"skillBook");String[] names={"프라베라","속성강화","메테오","홀리드래곤"};for(int i=0;i<10;i++){if(i<4){assertEquals(names[i],b.get(b.slot(i)).name);assertTrue(b.usable(b.slot(i)));}else assertNull(b.slot(i));}assertEquals(0,b.snapshot().getJSONObject("learned").length());f.shot(v,"v219-four-startup-skills");a.destroy();
  a=Robolectric.buildActivity(MainActivity.class).create();v=f.field(a.get(),"game");b=f.field(v,"skillBook");for(int i=0;i<4;i++)assertEquals(names[i],b.get(b.slot(i)).name);for(int i=4;i<10;i++)assertNull(b.slot(i));assertTrue(b.assign(0,"SK_마법사_001"));assertTrue((Boolean)f.call(v,"saveSkillSlots",new Class<?>[]{}));a.destroy();a=Robolectric.buildActivity(MainActivity.class).create();b=f.field(f.field(a.get(),"game"),"skillBook");assertEquals("SK_마법사_001",b.slot(0));a.destroy();
 }
}
