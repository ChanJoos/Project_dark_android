package com.projectdark.mobile;
import static org.junit.Assert.*;
import android.content.Context;
import android.graphics.*;
import java.util.*;
import java.io.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.*;
@RunWith(RobolectricTestRunner.class) @Config(sdk=34,manifest=Config.NONE) @GraphicsMode(GraphicsMode.Mode.NATIVE)
public class LinkedAccessoryV193Test {
 Context c;
 @Before public void setup(){c=RuntimeEnvironment.getApplication();c.getSharedPreferences("project_dark_f5m_v1",0).edit().clear().commit();F5mSaveStore.install(c);UiTheme.install(c);}
 @Test public void allThirtyOneRowsRetainUserMasterStatsAndReviewedIcons()throws Exception{
  RpgProgressionState r=new RpgProgressionState();SourceItemIconRegistry icons=new SourceItemIconRegistry(c);
  org.json.JSONArray source=new org.json.JSONObject(new String(java.nio.file.Files.readAllBytes(java.nio.file.Path.of("../master/canonical/User_Item_Master_20261018.json")),java.nio.charset.StandardCharsets.UTF_8)).getJSONArray("records");Map<String,org.json.JSONObject> rows=new HashMap<>();for(int i=0;i<source.length();i++)rows.put(source.getJSONObject(i).getString("itemId"),source.getJSONObject(i));assertEquals(31,LinkedAccessoryCatalogV193.IDS.length);
  String[] keys={"AC","HIT","DAM","HP","MP","STR","DEX","INT","WIS","CON"};
  for(String id:LinkedAccessoryCatalogV193.IDS){RpgProgressionState.ItemDefinition d=r.itemDefinitions().get(id);org.json.JSONArray cells=rows.get(id).getJSONArray("cells");for(int k=0;k<keys.length;k++)assertEquals(id+keys[k],Integer.valueOf(cells.getInt(9+k)),d.statModifiers.get(keys[k]));assertEquals(cells.getInt(21),(int)d.requiredLevel);assertNotNull(id,icons.get(id));assertNull(id,d.appearanceId);}
 }
 @Test public void nativeEquipmentWindowShowsEveryAccessoryAndRestoresStats()throws Exception{
  GameView view=new GameView(c);view.layout(0,0,960,540);RuntimeState state=ItemWindowReferenceTest.field(view,"state");RpgProgressionState r=state.rpg();r.enableEquipmentSandbox(true);ItemWindowReferenceTest.tap(view,830,28);File out=new File("build/reports/horror-v179/linked-accessories-v193");out.mkdirs();for(String id:LinkedAccessoryCatalogV193.IDS){RpgProgressionState.ItemDefinition d=r.itemDefinitions().get(id);r.unequip(d.equipSlot);int ac=r.finalStats().ac;assertEquals(id,RpgProgressionState.EquipResult.EQUIPPED,r.equipToSlot(id,d.equipSlot));assertEquals(ac+d.statModifiers.get("AC"),r.finalStats().ac);Bitmap image=Bitmap.createBitmap(960,540,Bitmap.Config.ARGB_8888);view.draw(new Canvas(image));try(FileOutputStream stream=new FileOutputStream(new File(out,id+".png"))){image.compress(Bitmap.CompressFormat.PNG,100,stream);}image.recycle();}F5mSaveStore.saveRewardsActive(r);GameView restart=new GameView(c);RuntimeState restored=ItemWindowReferenceTest.field(restart,"state");assertEquals(r.equipment(),restored.rpg().equipment());assertEquals(r.finalStats().ac,restored.rpg().finalStats().ac);
 }
 @Test public void accessoriesMigrateOnceWithoutBankDuplication()throws Exception{
  android.content.SharedPreferences p=c.getSharedPreferences("project_dark_f5m_v1",0);RpgProgressionState old=new RpgProgressionState();String banked=LinkedAccessoryCatalogV193.IDS[0];assertTrue(old.restoreOwnedItems(Collections.emptyMap(),Collections.emptyMap()));assertTrue(old.restoreBank(0,Collections.singletonMap(banked,1)));F5mSaveStore.saveRewardsActive(old);p.edit().remove("linked_accessories_granted_v193").commit();assertTrue(p.getBoolean("linked_items_granted_v185",false));RpgProgressionState current=new RpgProgressionState();F5mSaveStore.restoreRewardsActive(current);assertTrue(F5mSaveStore.writable());assertFalse(current.inventory().containsKey(banked));for(String id:LinkedAccessoryCatalogV193.IDS)if(!id.equals(banked))assertEquals(Integer.valueOf(RpgProgressionState.pairedSlot(current.itemDefinitions().get(id).equipSlot)?2:1),current.inventory().get(id));for(String id:LinkedItemCatalogV185.IDS)assertFalse(current.inventory().containsKey(id));assertTrue(current.restoreOwnedItems(Collections.emptyMap(),Collections.emptyMap()));F5mSaveStore.saveRewardsActive(current);RpgProgressionState restart=new RpgProgressionState();F5mSaveStore.restoreRewardsActive(restart);assertTrue(restart.inventory().isEmpty());assertEquals(Integer.valueOf(1),restart.bankInventory().get(banked));
 }
}
