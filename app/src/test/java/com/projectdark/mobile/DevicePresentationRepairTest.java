package com.projectdark.mobile;

import static org.junit.Assert.*;
import android.content.Context;
import android.graphics.*;
import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import org.junit.*;
import org.json.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.*;

/** Reproduces the shipped V105 missing registrations, not merely NPC wardrobe coverage. */
@RunWith(RobolectricTestRunner.class) @Config(sdk=34,manifest=Config.NONE)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public class DevicePresentationRepairTest {
 Context context;
 @Before public void setup(){context=RuntimeEnvironment.getApplication();for(String n:new String[]{"project_dark_f5m_v1","project_dark_journal_v1","project_dark_skill_test_v1"})context.getSharedPreferences(n,0).edit().clear().commit();F5mSaveStore.install(context);}
 @Test public void retainedStarterAndShopEquipmentHaveRealMatchingFrames()throws Exception{
  EquipmentVisualRegistry registry=new EquipmentVisualRegistry(context);
  String[] ids={"mu0000001","mu0000002","mu0000003","ml228","ml229","ml230","mh172","mh173","mh174","ms001","ms002","ms003","mw001","mw002","mw003","mw004","mw005","mw006","mw007","mw008","mw009","mw010"};
  for(String id:ids){EquipmentVisualRegistry.Visual v=registry.get(id);assertNotNull(id,v);assertNotNull("V105 omitted "+id,v.registration);
   JSONObject sprites;try(InputStream input=context.getAssets().open("source-registration/"+id+".json")){sprites=new JSONObject(new String(input.readAllBytes(),java.nio.charset.StandardCharsets.UTF_8)).getJSONObject("sprites");}
   // mw006 original has nine idle/walk and three action frames. Preserve its explicit
   // missing final source poses; never manufacture art just to satisfy a test.
   for(CharacterRenderer.Direction d:CharacterRenderer.Direction.values())for(int col=0;col<5;col++){int index=ShirtSourceRegistration.idleFrame(d,col);SourceEquipmentRegistration.Frame f=v.registration.idle(d,col);if(index<sprites.getJSONArray("01").length())assertInside(id,v.atlas,f);else assertNull("unavailable original frame "+id,f);}
   for(int i=0;i<4;i++){SourceEquipmentRegistration.Frame f=v.registration.action(i);if(i<sprites.getJSONArray("02").length())assertInside(id,v.atlas,f);else assertNull("unavailable original action "+id,f);}
  }
 }
 private void assertInside(String id,Bitmap b,SourceEquipmentRegistration.Frame f){assertNotNull(id,f);assertTrue(id,f.src.width()>0&&f.src.height()>0&&f.src.left>=0&&f.src.top>=0&&f.src.right<=b.getWidth()&&f.src.bottom<=b.getHeight());}
 @Test public void npcNeverTurnsOrAnimatesAndEveryIdentityIsKorean(){
  for(NpcIdentity.Profile p:NpcIdentity.ALL){assertFalse(p.name.matches(".*[A-Za-z].*"));Bitmap baseline=npc(p,CharacterRenderer.Direction.SW,CharacterRenderer.State.IDLE,0);
   for(CharacterRenderer.Direction d:CharacterRenderer.Direction.values())for(CharacterRenderer.State state:new CharacterRenderer.State[]{CharacterRenderer.State.IDLE,CharacterRenderer.State.WALK})assertTrue(p.id,baseline.sameAs(npc(p,d,state,1.37f)));
  }
  assertEquals("제임스와 벤저민에게 보고",NpcIdentity.text("James와 Benjamin에게 보고"));
 }
 private Bitmap npc(NpcIdentity.Profile p,CharacterRenderer.Direction d,CharacterRenderer.State state,float clock){Bitmap b=Bitmap.createBitmap(128,128,Bitmap.Config.ARGB_8888);new NpcActorRenderer(context).draw(new Canvas(b),p.id,64,112,d,state,clock);return b;}
 @Test public void actualPhoneAspectHudInventoryEquipmentAndMovementAreCaptured()throws Exception{
  GameView view=new GameView(context);view.layout(0,0,1536,709);
  capture(view,"hud");
  Field inv=GameView.class.getDeclaredField("inventoryOpen");inv.setAccessible(true);inv.setBoolean(view,true);capture(view,"inventory");inv.setBoolean(view,false);
  Field gear=GameView.class.getDeclaredField("equipmentOpen");gear.setAccessible(true);gear.setBoolean(view,true);capture(view,"equipment");gear.setBoolean(view,false);
  RuntimeState state=ItemWindowReferenceTest.field(view,"state");Map<String,String> equipment=new LinkedHashMap<>(state.rpg().equipment());
  for(CharacterRenderer.Direction d:CharacterRenderer.Direction.values()){
   Bitmap b=Bitmap.createBitmap(320,200,Bitmap.Config.ARGB_8888);Canvas canvas=new Canvas(b);canvas.drawColor(0xff30482e);String outfit=CharacterVisualBinding.from(state.rpg()).equipmentVisualRef();
   // Use production GameView captures for the complete HUD; registered-frame bounds above
   // cover every actual direction and all walk/action cells without inferred source geometry.
   new CharacterRenderer(context).draw(canvas,new CharacterRenderer.Pose(160,160,d,CharacterRenderer.State.WALK,.25f,0,1,false,outfit,"mw001",CharacterRenderer.ASSET_STATUS,CharacterRenderer.EffectFamily.NONE));write(b,"walk-"+d);
  }
  assertEquals(equipment,state.rpg().equipment());
 }
 private void capture(GameView view,String name)throws Exception{Bitmap b=Bitmap.createBitmap(view.getWidth(),view.getHeight(),Bitmap.Config.ARGB_8888);view.draw(new Canvas(b));write(b,name);}
 private void write(Bitmap b,String name)throws Exception{File f=new File("build/reports/device-review/v106-"+name+".png");f.getParentFile().mkdirs();try(FileOutputStream out=new FileOutputStream(f)){assertTrue(b.compress(Bitmap.CompressFormat.PNG,100,out));}}
}
