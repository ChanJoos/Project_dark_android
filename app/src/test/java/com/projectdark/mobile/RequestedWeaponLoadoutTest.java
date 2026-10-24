package com.projectdark.mobile;
import android.content.Context;
import android.graphics.Color;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.*;
import static org.junit.Assert.*;
@RunWith(RobolectricTestRunner.class) @Config(sdk=34,manifest=Config.NONE) @GraphicsMode(GraphicsMode.Mode.NATIVE)
public class RequestedWeaponLoadoutTest {
 Context context;
 @Before public void setup(){context=RuntimeEnvironment.getApplication();context.getSharedPreferences("project_dark_f5m_v1",0).edit().clear().commit();F5mSaveStore.install(context);}
 @Test public void uniqueNameStatsUseUserSourceWithoutInventingPower(){
  RpgProgressionState r=new RpgProgressionState();RpgProgressionState.ItemDefinition d=r.itemDefinitions().get(RequestedWeaponSourceFacts.SHINWOLDO);
  assertEquals("신월도",d.name);assertEquals(Integer.valueOf(99),d.requiredLevel);assertEquals(RpgProgressionState.Evidence.U,d.evidence);
  assertEquals(Integer.valueOf(165),d.statModifiers.get("MinATK"));assertEquals(Integer.valueOf(170),d.statModifiers.get("MaxATK"));
  assertEquals(Integer.valueOf(16),d.statModifiers.get("LMinATK"));assertEquals(Integer.valueOf(17),d.statModifiers.get("LMaxATK"));assertEquals(Integer.valueOf(0),d.statModifiers.get("HP"));
  assertEquals(Integer.valueOf(-500),r.itemDefinitions().get(RequestedWeaponSourceFacts.ROO_GRACE).statModifiers.get("HP"));
 }
 @Test public void generatedBindingsActuallyEquipWithExactFourDirectionSourcePixels(){
  RpgProgressionState r=new RpgProgressionState();r.enableEquipmentSandbox(true);r.bindTwoHandLearning(()->true);
  String[] ids={RequestedWeaponSourceFacts.SHINWOLDO,RequestedWeaponSourceFacts.ROO_GRACE,"IT_SHIELD_KNIGHT"};String[] appearances={"mw103","mw078","ms008"};
  EquipmentVisualRegistry registry=new EquipmentVisualRegistry(context);
  for(int i=0;i<ids.length;i++){
   RpgProgressionState.ItemDefinition d=r.itemDefinitions().get(ids[i]);assertEquals(appearances[i],d.appearanceId);assertFalse(r.inventoryOnlyItem(ids[i]));
   assertEquals(RpgProgressionState.EquipResult.EQUIPPED,r.equip(ids[i]));assertEquals(ids[i],r.equippedDefinition(d.equipSlot).itemId);
   EquipmentVisualRegistry.Visual v=registry.get(d.appearanceId);assertNotNull(v);assertNotNull(v.registration);
   for(CharacterRenderer.Direction dir:CharacterRenderer.Direction.values()){
    SourceEquipmentRegistration.Frame f=v.registration.idle(dir,0);assertNotNull(ids[i]+dir,f);assertTrue(f.src.left>=0&&f.src.top>=0&&f.src.right<=v.atlas.getWidth()&&f.src.bottom<=v.atlas.getHeight());
    boolean visible=false;for(int y=f.src.top;y<f.src.bottom&&!visible;y++)for(int x=f.src.left;x<f.src.right;x++)if(Color.alpha(v.atlas.getPixel(x,y))>0){visible=true;break;}assertTrue(ids[i]+dir,visible);
   }
  }
 }
}
