package com.projectdark.mobile;

import static org.junit.Assert.*;
import android.content.*;
import android.graphics.*;
import java.io.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.*;

@RunWith(RobolectricTestRunner.class) @Config(sdk=34,manifest=Config.NONE) @GraphicsMode(GraphicsMode.Mode.NATIVE)
public class ReferenceWarriorTest {
 Context c;
 @Before public void setup(){c=RuntimeEnvironment.getApplication();c.getSharedPreferences("project_dark_f5m_v1",0).edit().clear().commit();F5mSaveStore.install(c);}
 @Test public void referenceOutfitIsInitiallyOwnedAndEquipsAsFullSprites()throws Exception{
  GameView v=new GameView(c);RuntimeState s=TownInteriorTest.field(v,"state");
  assertEquals(Integer.valueOf(1),s.rpg().inventory().get(RpgProgressionState.REFERENCE_LEOPARD_ITEM_ID));
  assertEquals(Integer.valueOf(1),s.rpg().inventory().get(RpgProgressionState.REFERENCE_HELM_ITEM_ID));
  assertEquals(RpgProgressionState.EquipResult.EQUIPPED,s.rpg().equip(RpgProgressionState.REFERENCE_LEOPARD_ITEM_ID));
  assertEquals(RpgProgressionState.EquipResult.EQUIPPED,s.rpg().equip(RpgProgressionState.REFERENCE_HELM_ITEM_ID));
  s.rpg().equip(ChungryongWeaponRenderer.ITEM);
  EquipmentVisualRegistry gear=new EquipmentVisualRegistry(c);
  assertNotNull(gear.get("mu0000180").registration.idle(CharacterRenderer.Direction.SW,0));
  assertNotNull(gear.get("mh168").registration.idle(CharacterRenderer.Direction.SW,0));
  CharacterRenderer renderer=new CharacterRenderer(c);SkillBodyRenderer body=new SkillBodyRenderer(c,new SkillPresentationCatalog(c));
  Bitmap sheet=Bitmap.createBitmap(640,720,Bitmap.Config.ARGB_8888);Canvas out=new Canvas(sheet);out.drawColor(0xff403b34);
  for(CharacterRenderer.Direction d:CharacterRenderer.Direction.values())for(int frame=0;frame<5;frame++){
   Bitmap image=Bitmap.createBitmap(160,140,Bitmap.Config.ARGB_8888);Canvas canvas=new Canvas(image);
   CharacterRenderer.Pose pose=new CharacterRenderer.Pose(80,120,d,frame==0?CharacterRenderer.State.IDLE:CharacterRenderer.State.WALK,0,0,1,false,"mu0000180,mh168",ChungryongWeaponRenderer.APPEARANCE,null,CharacterRenderer.EffectFamily.NONE);
   CharacterRenderer.setPresentationWalkClock(Math.max(0,frame-1)*CharacterRenderer.WALK_FRAME_SECONDS);renderer.draw(canvas,pose);
   out.drawBitmap(image,d.ordinal()*160,frame*140,null);image.recycle();
  }
  CharacterRenderer.setPresentationWalkClock(0);
  File file=new File("build/reports/device-review/v107-reference-outfit-idle-walk.png");file.getParentFile().mkdirs();try(FileOutputStream stream=new FileOutputStream(file)){sheet.compress(Bitmap.CompressFormat.PNG,100,stream);}sheet.recycle();
 }
 @Test public void legacySaveReceivesEachReferenceItemOnceWithoutReplacingExistingEquipment()throws Exception{
  SharedPreferences prefs=c.getSharedPreferences("project_dark_f5m_v1",0);
  prefs.edit().putString("inventory_v2","{\"IT_TEST_WEAPON_MW002\":2}").putString("equipment_v2","{\"무기\":\"IT_TEST_WEAPON_MW002\"}").putLong("gold",456).commit();
  GameView v=new GameView(c);RuntimeState s=TownInteriorTest.field(v,"state");
  assertEquals(Integer.valueOf(1),s.rpg().inventory().get(RpgProgressionState.REFERENCE_LEOPARD_ITEM_ID));
  assertEquals(Integer.valueOf(1),s.rpg().inventory().get(RpgProgressionState.REFERENCE_HELM_ITEM_ID));
  assertEquals("IT_TEST_WEAPON_MW002",s.rpg().equipment().get("무기"));assertEquals(Long.valueOf(456),s.rpg().gold());
  assertTrue(prefs.getBoolean("reference_warrior_granted_v107",false));
  prefs.edit().putString("inventory_v2","{\"IT_TEST_WEAPON_MW002\":2}").commit();F5mSaveStore.install(c);
  v=new GameView(c);s=TownInteriorTest.field(v,"state");assertFalse(s.rpg().inventory().containsKey(RpgProgressionState.REFERENCE_LEOPARD_ITEM_ID));
 }
 @Test public void invalidSaveKeepsOriginalBytesAndDoesNotMarkReferenceGrant(){
  SharedPreferences prefs=c.getSharedPreferences("project_dark_f5m_v1",0);String raw="{\"unknown_saved_identity\":1}";prefs.edit().putString("inventory_v2",raw).commit();new GameView(c);
  assertFalse(F5mSaveStore.writable());assertEquals(raw,prefs.getString("inventory_v2",null));assertFalse(prefs.getBoolean("reference_warrior_granted_v107",false));
 }
}
