package com.projectdark.mobile;
import android.content.Context;import android.graphics.*;
import com.projectdark.mobile.world.*;
import org.junit.*;import org.junit.runner.RunWith;import org.robolectric.*;import org.robolectric.annotation.*;
import static org.junit.Assert.*;
@RunWith(RobolectricTestRunner.class) @Config(sdk=34,manifest=Config.NONE) @GraphicsMode(GraphicsMode.Mode.NATIVE)
public class DeathVillageV219Test {
 MineTownV203Test f;Context c;
 @Before public void setup(){f=new MineTownV203Test();f.reset();c=RuntimeEnvironment.getApplication();}
 void select(GameView v,String to)throws Exception{RuntimeState s=f.field(v,"state");java.util.List<DeathVillageData.Gate> gates=DeathVillageGeometry.gates(s.currentMapId());int index=-1;for(int i=0;i<gates.size();i++)if(to.equals(gates.get(i).to))index=i;assertTrue(index>=0);float offset=f.field(v,"hudCenterOffset");RectF r=new RectF(new DeathVillageGuide().button(index,gates.size(),offset));f.tap(v,r.centerX(),r.centerY());}
 @Test public void realLobbySpawnButtonWalksIntoVisibleOutdoorHouseAndBackWithoutTeleport()throws Exception{
  DeathVillageV217Test old=new DeathVillageV217Test();old.f=f;old.c=c;GameView v=old.lobby();RuntimeState s=f.field(v,"state");int[] spawn=DeathVillageData.spawn(DeathVillageGeometry.LOBBY);assertEquals(DeathVillageGeometry.x(spawn[0],spawn[1]),s.player().x,0);assertEquals(DeathVillageGeometry.y(spawn[0],spawn[1]),s.player().y,0);f.shot(v,"v219-01-lobby-real-spawn");float before=s.player().x;select(v,DeathVillageGeometry.FIELD);assertEquals("button requests walking",before,s.player().x,0);
  for(int i=0;i<500&&DeathVillageGeometry.LOBBY.equals(s.currentMapId());i++)f.tick(v,1);
  assertEquals(DeathVillageGeometry.FIELD,s.currentMapId());assertEquals(72,s.monsters().size());assertEquals(DeathVillageGeometry.x(28,10),s.player().x,0);assertEquals(DeathVillageGeometry.y(28,10),s.player().y,0);assertFalse((Boolean)f.call(v,"checkCampaignGate",new Class<?>[]{}));
  WorldRuntimeAdapter world=f.field(v,"poteFieldAdapter");WorldCameraTransform.Point door=world.worldToScreen(DeathVillageGeometry.x(28,3),DeathVillageGeometry.y(28,3));assertTrue("first original building door visible on arrival",door.x>285&&door.x<710&&door.y>120&&door.y<430);f.shot(v,"v219-02-outdoor-on-arrival");select(v,"MAP_DEATH_HOUSE_1");for(int i=0;i<250&&DeathVillageGeometry.FIELD.equals(s.currentMapId());i++)f.tick(v,1);assertEquals("walk to actual original doorway", "MAP_DEATH_HOUSE_1",s.currentMapId());f.shot(v,"v219-03-house1-arrival");
  select(v,DeathVillageGeometry.FIELD);f.tick(v,2);assertEquals("explicit return allowed at latched arrival",DeathVillageGeometry.FIELD,s.currentMapId());assertFalse("no automatic return bounce",(Boolean)f.call(v,"checkCampaignGate",new Class<?>[]{}));
 }
 @Test public void outdoorButtonsWalkToAllThreeSourceBuildingsWithLiveCollisionAndWideHud()throws Exception{
  for(int house=1;house<=3;house++){setup();GameView v=new GameView(c);v.layout(0,0,1280,540);f.call(v,"travelTown",new Class<?>[]{String.class},DeathVillageGeometry.LOBBY);f.call(v,"changeCampaignMap",new Class<?>[]{String.class,boolean.class},DeathVillageGeometry.FIELD,false);RuntimeState s=f.field(v,"state");assertEquals(72,s.monsters().size());select(v,"MAP_DEATH_HOUSE_"+house);WorldRuntimeAdapter world=f.field(v,"poteFieldAdapter");assertTrue("route uses original source terrain and live 72 actors",CampaignNavigationTest.walk(world,world.movement().snapshot()));world.tickNavigation(.7f);f.shot(v,"v219-outdoor-door-"+house+"-wide");assertTrue((Boolean)f.call(v,"checkCampaignGate",new Class<?>[]{}));assertEquals("MAP_DEATH_HOUSE_"+house,s.currentMapId());assertEquals(15,s.monsters().size());}
 }
 @Test public void realActivityMigratesOldTenSlotsToRequestedFourAndKeepsLaterEditsOnRestart()throws Exception{
  org.json.JSONArray previous=new org.json.JSONArray();for(int i=0;i<10;i++)previous.put("SK_마법사_001");c.getSharedPreferences("project_dark_skill_test_v1",0).edit().putBoolean("enabled",true).putString("slots",previous.toString()).commit();
  org.robolectric.android.controller.ActivityController<MainActivity> a=Robolectric.buildActivity(MainActivity.class).create();GameView v=f.field(a.get(),"game");v.layout(0,0,960,540);SkillBook b=f.field(v,"skillBook");String[] names={"프라베라","속성강화","메테오","홀리드래곤"};for(int i=0;i<10;i++){if(i<4){assertEquals(names[i],b.get(b.slot(i)).name);assertTrue(b.usable(b.slot(i)));}else assertNull(b.slot(i));}assertEquals(0,b.snapshot().getJSONObject("learned").length());f.shot(v,"v219-four-startup-skills");a.destroy();
  a=Robolectric.buildActivity(MainActivity.class).create();v=f.field(a.get(),"game");b=f.field(v,"skillBook");for(int i=0;i<4;i++)assertEquals(names[i],b.get(b.slot(i)).name);for(int i=4;i<10;i++)assertNull(b.slot(i));assertTrue(b.assign(0,"SK_마법사_001"));assertTrue((Boolean)f.call(v,"saveSkillSlots",new Class<?>[]{}));a.destroy();a=Robolectric.buildActivity(MainActivity.class).create();b=f.field(f.field(a.get(),"game"),"skillBook");assertEquals("SK_마법사_001",b.slot(0));a.destroy();
 }
}
