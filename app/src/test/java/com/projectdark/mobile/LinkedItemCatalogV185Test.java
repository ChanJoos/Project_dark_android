package com.projectdark.mobile;
import static org.junit.Assert.*;
import android.content.Context;
import java.util.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.*;
@RunWith(RobolectricTestRunner.class) @Config(sdk=34,manifest=Config.NONE) @GraphicsMode(GraphicsMode.Mode.NATIVE)
public class LinkedItemCatalogV185Test {
 @Test public void everyReviewedItemHasSourceModifiersIconAndRetainedWeaponFrames() throws Exception {
  Context c=RuntimeEnvironment.getApplication();RpgProgressionState r=new RpgProgressionState();r.enableEquipmentSandbox(true);r.bindTwoHandLearning(()->true);
  SourceItemIconRegistry icons=new SourceItemIconRegistry(c);EquipmentVisualRegistry visuals=new EquipmentVisualRegistry(c);
  org.json.JSONArray rows=new org.json.JSONObject(java.nio.file.Files.readString(java.nio.file.Path.of("../master/source/items/naver_bundle_20261019/runtime_catalog.json"))).getJSONArray("admitted");
  assertEquals(30,rows.length());assertEquals(rows.length(),LinkedItemCatalogV185.IDS.length);
  for(int i=0;i<rows.length();i++){
   org.json.JSONObject source=rows.getJSONObject(i);String id=source.getString("itemId");RpgProgressionState.ItemDefinition d=r.itemDefinitions().get(id);assertNotNull(id,d);assertNotNull(id,icons.get(id));
   org.json.JSONObject stats=source.getJSONObject("stats");for(Iterator<String> it=stats.keys();it.hasNext();){String key=it.next();assertEquals(id+key,Integer.valueOf(stats.getInt(key)),d.statModifiers.get(key));}
   r.unequip(d.equipSlot);assertEquals(id,RpgProgressionState.EquipResult.EQUIPPED,r.equip(id));
   if(d.appearanceId!=null){EquipmentVisualRegistry.Visual v=visuals.get(d.appearanceId);assertNotNull(id,v);assertNotNull(id,v.registration);for(CharacterRenderer.Direction dir:CharacterRenderer.Direction.values())for(int frame=0;frame<5;frame++)assertNotNull(id,v.registration.idle(dir,frame));for(int frame=0;frame<4;frame++)assertNotNull(id,v.registration.action(frame));}
  }
 }
 @Test public void articleMigrationIsOneTimeBankAwareAndRemovalSurvivesRestart() throws Exception {
  Context c=RuntimeEnvironment.getApplication();android.content.SharedPreferences p=c.getSharedPreferences("project_dark_f5m_v1",0);p.edit().clear().commit();RpgProgressionState old=new RpgProgressionState();String id=LinkedItemCatalogV185.IDS[0];assertTrue(old.restoreOwnedItems(Collections.emptyMap(),Collections.emptyMap()));assertTrue(old.restoreBank(0,Collections.singletonMap(id,1)));F5mSaveStore.install(c);F5mSaveStore.saveRewardsActive(old);p.edit().remove("linked_items_granted_v185").commit();
  RpgProgressionState migrated=new RpgProgressionState();F5mSaveStore.restoreRewardsActive(migrated);assertTrue(F5mSaveStore.writable());assertFalse(migrated.inventory().containsKey(id));for(String item:LinkedItemCatalogV185.IDS)if(!item.equals(id))assertEquals(Integer.valueOf(1),migrated.inventory().get(item));
  assertTrue(migrated.restoreOwnedItems(Collections.emptyMap(),Collections.emptyMap()));F5mSaveStore.saveRewardsActive(migrated);RpgProgressionState reopened=new RpgProgressionState();F5mSaveStore.restoreRewardsActive(reopened);assertTrue(reopened.inventory().isEmpty());assertEquals(Integer.valueOf(1),reopened.bankInventory().get(id));p.edit().clear().commit();
 }
 @Test public void rapidTextStatsReachBothOrnamentSlotsWithoutClaimingMissingBodySprite(){
  RpgProgressionState r=new RpgProgressionState();r.enableEquipmentSandbox(true);String id="USER_ITEM_20261018_R05143";RpgProgressionState.ItemDefinition d=r.itemDefinitions().get(id);assertNull(d.appearanceId);assertEquals(Integer.valueOf(1),d.statModifiers.get("MAGIC_DEFENSE"));assertEquals(Integer.valueOf(10),d.statModifiers.get("REGEN"));r.unequip("장식");r.unequip(RpgProgressionState.SECOND_ORNAMENT_SLOT);int hp=r.finalStats().maxHp;assertEquals(RpgProgressionState.EquipResult.EQUIPPED,r.equip(id));assertEquals(RpgProgressionState.EquipResult.EQUIPPED,r.equipToSlot(id,RpgProgressionState.SECOND_ORNAMENT_SLOT));assertEquals(hp+600,r.finalStats().maxHp);
 }
}
