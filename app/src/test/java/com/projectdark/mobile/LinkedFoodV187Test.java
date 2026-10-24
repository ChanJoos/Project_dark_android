package com.projectdark.mobile;
import static org.junit.Assert.*;
import android.content.Context;
import android.graphics.Bitmap;
import java.util.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.*;
@RunWith(RobolectricTestRunner.class) @Config(sdk=34,manifest=Config.NONE) @GraphicsMode(GraphicsMode.Mode.NATIVE)
public class LinkedFoodV187Test {
 Context c;
 @Before public void setup(){c=RuntimeEnvironment.getApplication();c.getSharedPreferences("project_dark_f5m_v1",0).edit().clear().commit();F5mSaveStore.install(c);}
 org.json.JSONArray source()throws Exception{return new org.json.JSONArray(new String(java.nio.file.Files.readAllBytes(java.nio.file.Path.of("../master/source/items/naver_bundle_20261019/reviewed_foods.json")),java.nio.charset.StandardCharsets.UTF_8));}
 @Test public void allFiftyFourLabelledFoodsHaveNativeIconsAndOnlySixteenExplicitEffects()throws Exception{
  RpgProgressionState r=new RpgProgressionState();SourceItemIconRegistry icons=new SourceItemIconRegistry(c);org.json.JSONArray rows=source();assertEquals(54,rows.length());assertEquals(54,LinkedFoodCatalogV187.IDS.length);int usable=0;
  for(int i=0;i<rows.length();i++){org.json.JSONObject row=rows.getJSONObject(i);String id=row.getString("itemId");RpgProgressionState.ItemDefinition d=r.itemDefinitions().get(id);assertEquals(row.getString("name"),d.name);assertFalse(id,d.equippable());assertTrue(id,d.statModifiers.isEmpty());assertEquals(row.getInt("hp"),ItemEffects.healHp(id));assertEquals(row.getInt("mp"),ItemEffects.healMp(id));assertEquals(id,row.getInt("hp")>0||row.getInt("mp")>0,r.isConsumable(id));Bitmap icon=icons.get(id);assertNotNull(id,icon);org.json.JSONObject original=new OriginalInventoryV252(c).receipt(id);if(original==null){assertEquals(32,icon.getWidth());assertEquals(32,icon.getHeight());}else{assertEquals(id,original.getInt("w"),icon.getWidth());assertEquals(id,original.getInt("h"),icon.getHeight());Bitmap sheet=android.graphics.BitmapFactory.decodeStream(c.getAssets().open("inventory-v252/icons.png"));Bitmap expected=Bitmap.createBitmap(sheet,original.getInt("x"),original.getInt("y"),original.getInt("w"),original.getInt("h"));assertTrue(id,icon.sameAs(expected));}if(r.isConsumable(id))usable++;}
  assertEquals(16,usable);assertEquals(0,LinkedFoodCatalogV187.hp(null));assertEquals(0,LinkedFoodCatalogV187.mp(null));
 }
 @Test public void everyExplicitRecoveryUsesSourceAmountAndConsumesExactlyOnce()throws Exception{
  org.json.JSONArray rows=source();for(int i=0;i<rows.length();i++){org.json.JSONObject row=rows.getJSONObject(i);RuntimeState state=new RuntimeState();RpgProgressionState r=state.rpg();String id=row.getString("itemId");state.player().alive=true;state.player().hp=1;state.player().mp=1;state.player().maxHp=200001;state.player().maxMp=200001;
   if(row.getInt("hp")>0||row.getInt("mp")>0){assertEquals(id,RpgProgressionState.UseResult.USED,r.useConsumable(id,state));assertEquals(id,1+row.getInt("hp"),state.player().hp);assertEquals(id,1+row.getInt("mp"),state.player().mp);assertFalse(id,r.inventory().containsKey(id));}
   else{assertEquals(id,RpgProgressionState.UseResult.NOT_CONSUMABLE,r.useConsumable(id,state));assertEquals(Integer.valueOf(1),r.inventory().get(id));assertEquals(1,state.player().hp);assertEquals(1,state.player().mp);}
  }
 }
 @Test public void combinedFoodHealsWhicheverResourceIsMissingWithoutFullOrDeadConsumption(){
  RuntimeState state=new RuntimeState();RpgProgressionState r=state.rpg();String id="IT_NA_FOOD_293294_45";assertEquals(500,ItemEffects.healHp(id));assertEquals(150,ItemEffects.healMp(id));r.autoLootResolvedItem(id,2);state.player().maxHp=1000;state.player().maxMp=1000;state.player().hp=300;state.player().mp=1000;state.player().alive=true;
  assertEquals(RpgProgressionState.UseResult.USED,r.useConsumable(id,state));assertEquals(800,state.player().hp);assertEquals(1000,state.player().mp);state.player().hp=1000;state.player().mp=0;assertEquals(RpgProgressionState.UseResult.USED,r.useConsumable(id,state));assertEquals(1000,state.player().hp);assertEquals(150,state.player().mp);state.player().mp=1000;int owned=r.inventory().get(id);assertEquals(RpgProgressionState.UseResult.NO_EFFECT,r.useConsumable(id,state));assertEquals(owned,(int)r.inventory().get(id));state.player().alive=false;state.player().mp=0;assertEquals(RpgProgressionState.UseResult.NO_EFFECT,r.useConsumable(id,state));assertEquals(owned,(int)r.inventory().get(id));
 }
 @Test public void foodMigrationIsSeparateFromEquipmentAndNeverDuplicatesBankOrRestock(){
  android.content.SharedPreferences p=c.getSharedPreferences("project_dark_f5m_v1",0);RpgProgressionState old=new RpgProgressionState();String banked=LinkedFoodCatalogV187.IDS[0];assertTrue(old.restoreOwnedItems(Collections.emptyMap(),Collections.emptyMap()));assertTrue(old.restoreBank(0,Collections.singletonMap(banked,1)));F5mSaveStore.saveRewardsActive(old);p.edit().remove("linked_food_granted_v187").commit();RpgProgressionState current=new RpgProgressionState();F5mSaveStore.restoreRewardsActive(current);assertTrue(F5mSaveStore.writable());assertFalse(current.inventory().containsKey(banked));for(String id:LinkedFoodCatalogV187.IDS)if(!id.equals(banked))assertEquals(Integer.valueOf(1),current.inventory().get(id));for(String id:LinkedClothingCatalogV186.IDS)assertFalse(current.inventory().containsKey(id));assertTrue(current.restoreOwnedItems(Collections.emptyMap(),Collections.emptyMap()));F5mSaveStore.saveRewardsActive(current);RpgProgressionState restart=new RpgProgressionState();F5mSaveStore.restoreRewardsActive(restart);assertTrue(restart.inventory().isEmpty());assertEquals(Integer.valueOf(1),restart.bankInventory().get(banked));
 }
}
