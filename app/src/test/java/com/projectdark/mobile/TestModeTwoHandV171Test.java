package com.projectdark.mobile;
import static org.junit.Assert.*;
import android.content.Context;
import android.graphics.RectF;
import java.util.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.*;
@RunWith(RobolectricTestRunner.class) @Config(sdk=34,manifest=Config.NONE) @GraphicsMode(GraphicsMode.Mode.NATIVE)
public class TestModeTwoHandV171Test {
 Context c;final List<ActivityController<MainActivity>> activities=new ArrayList<>();
 @Before public void setup(){c=RuntimeEnvironment.getApplication();for(String key:new String[]{"project_dark_f5m_v1","project_dark_skill_test_v1","project_dark_skill_book_v1","project_dark_visual_v1"})c.getSharedPreferences(key,0).edit().clear().commit();F5mSaveStore.install(c);}
 @After public void cleanup(){for(ActivityController<MainActivity> a:activities)a.destroy();}
 GameView start()throws Exception{ActivityController<MainActivity> a=Robolectric.buildActivity(MainActivity.class).create();activities.add(a);GameView v=TownInteriorTest.field(a.get(),"game");assertNotNull(v);v.layout(0,0,960,540);return v;}
 RuntimeState state(GameView v)throws Exception{return TownInteriorTest.field(v,"state");}
 SkillBook book(GameView v)throws Exception{return TownInteriorTest.field(v,"skillBook");}
 void equipViaInventory(GameView v)throws Exception{ItemWindow w=TownInteriorTest.field(v,"itemWindow");TownInteriorTest.tap(v,742,28);List<RpgInventoryPresentation.ItemRow> rows=w.rows(state(v).rpg());int index=-1;for(int i=0;i<rows.size();i++)if(ChungryongWeaponRenderer.ITEM.equals(rows.get(i).itemId))index=i;assertTrue(index>=0);RectF cell=ItemWindow.cell(index);TownInteriorTest.tap(v,cell.centerX(),cell.centerY());TownInteriorTest.tap(v,805,490);assertEquals(ChungryongWeaponRenderer.ITEM,state(v).rpg().equipment().get("무기"));}
 @Test public void actualFreshActivityTestLearnedStateAllowsInventoryEquipAndKeepsShieldRule()throws Exception{GameView v=start();assertTrue(book(v).testAccess());assertTrue(book(v).learned("SK_전사_008"));assertFalse(book(v).owned("SK_전사_008"));equipViaInventory(v);assertNull(state(v).rpg().equippedDefinition("방패"));assertEquals(RpgProgressionState.EquipResult.TWO_HAND_CONFLICT,state(v).rpg().equip("IT_ADAPTED_STARTER_SHIELD"));assertFalse(book(v).snapshot().getJSONObject("learned").has("SK_전사_008"));}
 @Test public void testModeTwoHandSurvivesRestartBeforeEquipmentValidation()throws Exception{GameView v=start();equipViaInventory(v);v.pause();GameView restored=start();assertTrue(book(restored).testAccess());assertFalse(book(restored).owned("SK_전사_008"));assertEquals(ChungryongWeaponRenderer.ITEM,state(restored).rpg().equipment().get("무기"));}
 @Test public void disablingTestModeRemovesPreviewOnlyWeaponWithoutLosingInventory()throws Exception{GameView v=start();equipViaInventory(v);int owned=state(v).rpg().inventory().get(ChungryongWeaponRenderer.ITEM);v.setSkillTestMode(false);assertNull(state(v).rpg().equippedDefinition("무기"));assertEquals(owned,(int)state(v).rpg().inventory().get(ChungryongWeaponRenderer.ITEM));assertEquals(RpgProgressionState.EquipResult.TWO_HAND_SKILL_REQUIRED,state(v).rpg().equip(ChungryongWeaponRenderer.ITEM));}
 @Test public void normalSavedModeStillRequiresRealLearningAndRetainsLearnedEquipOnRestart()throws Exception{c.getSharedPreferences("project_dark_skill_test_v1",0).edit().putBoolean("enabled",false).commit();GameView v=start();assertFalse(book(v).testAccess());Map<String,String> before=new LinkedHashMap<>(state(v).rpg().equipment());assertEquals(RpgProgressionState.EquipResult.TWO_HAND_SKILL_REQUIRED,state(v).rpg().equip(ChungryongWeaponRenderer.ITEM));assertEquals(before,state(v).rpg().equipment());assertTrue(book(v).learn("SK_전사_008",0));assertEquals(RpgProgressionState.EquipResult.EQUIPPED,state(v).rpg().equip(ChungryongWeaponRenderer.ITEM));v.pause();GameView restored=start();assertFalse(book(restored).testAccess());assertTrue(book(restored).owned("SK_전사_008"));assertEquals(ChungryongWeaponRenderer.ITEM,state(restored).rpg().equipment().get("무기"));}
}
