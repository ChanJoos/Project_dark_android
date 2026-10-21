package com.projectdark.mobile;
import android.content.*;import android.graphics.*;
import com.projectdark.mobile.world.*;import org.junit.*;import org.junit.runner.RunWith;import org.robolectric.*;import org.robolectric.annotation.*;
import java.util.*;import java.io.*;import org.json.*;import static org.junit.Assert.*;
@RunWith(RobolectricTestRunner.class) @Config(sdk=34,manifest=Config.NONE) @GraphicsMode(GraphicsMode.Mode.NATIVE)
public class ItemCatalogV210Test {
 Context c;MineTownV203Test f;
 @Before public void reset(){c=RuntimeEnvironment.getApplication();f=new MineTownV203Test();f.reset();}
 void save(Bitmap b,String n)throws Exception{File p=new File("build/reports/horror-v179/v210/"+n+".png");p.getParentFile().mkdirs();try(FileOutputStream out=new FileOutputStream(p)){assertTrue(b.compress(Bitmap.CompressFormat.PNG,100,out));}}
 @Test public void allSixtyInventedTrainingIdsResolveToOneOriginalDefinitionAndCannotReappear(){
  RpgProgressionState r=new RpgProgressionState();r.enableEquipmentSandbox(true);int synthetic=0;
  for(Map.Entry<String,String> e:EquipmentIdentityV210.ALIASES.entrySet()){
   assertFalse(e.getValue(),EquipmentIdentityV210.alias(e.getValue()));RpgProgressionState.ItemDefinition old=r.itemDefinitions().get(e.getKey()),canonical=r.itemDefinitions().get(e.getValue());assertNotNull(old);assertNotNull(canonical);assertEquals(canonical.name,old.name);assertEquals(canonical.statModifiers,old.statModifiers);assertEquals(canonical.requiredLevel,old.requiredLevel);assertEquals(canonical.appearanceId,old.appearanceId);assertFalse(e.getKey(),r.inventory().containsKey(e.getKey()));
   if(e.getKey().startsWith("IT_B_")){synthetic++;assertFalse(old.name.contains("수련"));}
  }assertEquals(60,synthetic);assertEquals("USER_ITEM_20261018_R05003",RpgProgressionState.canonicalItemId("IT_B_CAMPAIGN_TOOL_WARRIOR_65"));assertEquals("USER_ITEM_20261018_R05003",RpgProgressionState.canonicalItemId("IT_B_JOB_WARRIOR_WEAPON"));
  int before=r.inventory().get("USER_ITEM_20261018_R05003");assertEquals(RpgProgressionState.AutoLootResult.LOOTED,r.autoLootResolvedItem("IT_B_CAMPAIGN_TOOL_WARRIOR_65",1));assertEquals(Integer.valueOf(before+1),r.inventory().get("USER_ITEM_20261018_R05003"));assertFalse(r.inventory().containsKey("IT_B_CAMPAIGN_TOOL_WARRIOR_65"));
 }
 @Test public void knownLegacyRequirementsSurviveCanonicalJoinButUnknownNewWearablesStayPending(){
  RpgProgressionState r=new RpgProgressionState();
  assertEquals(RpgProgressionState.RequirementResult.MET,r.currentRequirements("IT_TEST_WEAPON_MW002"));
  assertEquals(RpgProgressionState.EquipResult.EQUIPPED,r.equip("IT_TEST_WEAPON_MW002"));
  assertEquals(RpgProgressionState.canonicalItemId("IT_TEST_WEAPON_MW002"),r.equipment().get("무기"));
  r.restoreProgression(41,0L);
  for(String id:new String[]{"IT_NECK_FIRE_PEARL","IT_BELT_FIRE_LEATHER"}){
   assertEquals(id,RpgProgressionState.RequirementResult.MET,r.currentRequirements(id));
   assertEquals(RpgProgressionState.EquipResult.EQUIPPED,r.equip(id));
  }
  r.enableEquipmentSandbox(true);
  for(String id:UnifiedItemCatalogV210.IDS)if(r.inventoryOnlyItem(id))assertEquals(id,RpgProgressionState.RequirementResult.PENDING,r.currentRequirements(id));
 }
 @Test public void originalDeityVariantsKeepSeparateNamesStatsRowsAndBothRingSlots()throws Exception{
  RpgProgressionState r=new RpgProgressionState();r.enableEquipmentSandbox(true);String base="USER_ITEM_20261018_R05424",setoa="USER_ITEM_20261018_R03440";Map<String,Integer> own=new LinkedHashMap<>();own.put(base,1);own.put(setoa,1);assertTrue(r.restoreOwnedItems(own,Collections.emptyMap()));assertNotEquals(RpgProgressionState.canonicalItemId(base),RpgProgressionState.canonicalItemId(setoa));assertFalse(InventoryGroupsV199.equivalent(r,base,setoa));assertEquals(2,new ItemWindow(c).rows(r).size());assertNotEquals(r.itemDefinitions().get(base).statModifiers,r.itemDefinitions().get(setoa).statModifiers);
  assertEquals(RpgProgressionState.EquipResult.EQUIPPED,r.equipToSlot(base,"반지"));assertEquals(RpgProgressionState.EquipResult.EQUIPPED,r.equipToSlot(setoa,RpgProgressionState.RIGHT_RING_SLOT));assertEquals(base,r.equipment().get("반지"));assertEquals(setoa,r.equipment().get(RpgProgressionState.RIGHT_RING_SLOT));assertEquals(2,new ItemWindow(c).rows(r).size());
  SourceItemIconRegistry art=new SourceItemIconRegistry(c);Bitmap out=Bitmap.createBitmap(480,140,Bitmap.Config.ARGB_8888);Canvas canvas=new Canvas(out);canvas.drawColor(0xff302820);Paint p=new Paint();int n=0;for(String id:own.keySet()){RpgProgressionState.ItemDefinition d=r.itemDefinitions().get(id);SourceItemIconRegistry.draw(canvas,art.get(d),new RectF(n*240+10,12,n*240+68,70),p);UiTheme.text(canvas,d.name,n*240+10,94,13,UiTheme.TEXT,true);UiTheme.text(canvas,"STR "+d.statModifiers.getOrDefault("STR",0)+" / MP "+d.statModifiers.getOrDefault("MP",0),n*240+10,119,12,UiTheme.MUTED,false);n++;}save(out,"original-variants-kept-separate");
 }
 @Test public void everyCanonicalItemHasPackedOriginalArtworkAndEveryNewIdentityIsInInventory()throws Exception{
  RpgProgressionState r=new RpgProgressionState();r.enableEquipmentSandbox(true);SourceItemIconRegistry art=new SourceItemIconRegistry(c);ItemAtlasV210 atlas=new ItemAtlasV210(c);int size=r.canonicalItemDefinitions().size(),count=0,weapons=0;Bitmap out=Bitmap.createBitmap(960,((size+19)/20)*48,Bitmap.Config.ARGB_8888);Canvas canvas=new Canvas(out);canvas.drawColor(0xff302820);Paint p=new Paint();
  for(RpgProgressionState.ItemDefinition d:r.canonicalItemDefinitions().values()){
   Bitmap icon=art.get(d);assertNotNull(d.itemId+" "+d.name,icon);assertNotNull(d.itemId,atlas.entry(d.itemId));assertTrue(d.itemId,r.inventory().containsKey(d.itemId));assertTrue(SourceItemIconRegistry.visibleEquipmentBounds(icon).width()>0);
   if("무기".equals(d.equipSlot)){assertEquals(32,icon.getWidth());if(WeaponInventoryArtworkV209.elongation(icon)>3)assertTrue(d.name+" axis "+WeaponInventoryArtworkV209.axisDegrees(icon),Math.abs(WeaponInventoryArtworkV209.axisDegrees(icon)+45)<9);weapons++;}
   SourceItemIconRegistry.draw(canvas,icon,new RectF(count%20*48,count/20*48,count%20*48+44,count/20*48+44),p);atlas.get(d.itemId);assertTrue(atlas.pageCacheCount()<=2);count++;
  }assertTrue(size>=1770);assertTrue(weapons>250);for(String id:UnifiedItemCatalogV210.IDS)assertTrue(id,r.inventory().containsKey(id));save(out,"all-canonical-item-artwork");
  File audit=new File("build/reports/horror-v179/v210/census.json");try(FileWriter w=new FileWriter(audit)){w.write(new JSONObject().put("canonicalItems",size).put("compatibilityDefinitions",r.itemDefinitions().size()).put("retiredAliases",EquipmentIdentityV210.ALIASES.size()).put("sourceNamedIdentities",UnifiedItemCatalogV210.IDS.length).put("weaponIcons",weapons).put("atlasTextures",atlas.manifest.getInt("textureCount")).put("allInventoryArtPresent",true).toString(2));}
 }
 @Test public void oldSaveMigratesAliasesInInventoryEquipmentAndBankWithoutLosingQuantityAndNewGrantRunsOnce()throws Exception{
  RpgProgressionState old=new RpgProgressionState();assertTrue(old.restoreOwnedItems(Collections.emptyMap(),Collections.emptyMap()));F5mSaveStore.saveRewardsActive(old);SharedPreferences prefs=c.getSharedPreferences("project_dark_f5m_v1",0);String legacy="IT_TEST_WEAPON_MW002",canonical=RpgProgressionState.canonicalItemId(legacy),newId=null;
  for(String id:UnifiedItemCatalogV210.IDS)if(old.inventoryOnlyItem(id)){newId=id;break;}assertNotNull(newId);
  JSONObject inv=new JSONObject().put(legacy,2).put(canonical,3);JSONObject eq=new JSONObject().put("무기",legacy);JSONObject bank=new JSONObject().put("IT_B_CAMPAIGN_TOOL_WARRIOR_65",2).put("USER_ITEM_20261018_R05003",3).put(newId,1);
  prefs.edit().putString("inventory_v2",inv.toString()).putString("equipment_v2",eq.toString()).putString("bank_inventory_v84",bank.toString()).remove("unified_items_granted_v210").commit();F5mSaveStore.install(c);RpgProgressionState r=new RpgProgressionState();F5mSaveStore.restoreRewardsActive(r);assertTrue(F5mSaveStore.writable());assertEquals(Integer.valueOf(5),r.inventory().get(canonical));assertFalse(r.inventory().containsKey(legacy));assertEquals(canonical,r.equipment().get("무기"));assertEquals(Integer.valueOf(5),r.bankInventory().get("USER_ITEM_20261018_R05003"));assertFalse(r.inventory().containsKey(newId));assertEquals(Integer.valueOf(1),r.bankInventory().get(newId));assertTrue(prefs.getBoolean("unified_items_granted_v210",false));
  Map<String,Integer> owned=new LinkedHashMap<>(r.inventory());String remove=UnifiedItemCatalogV210.IDS[4];if(remove.equals(canonical))remove=UnifiedItemCatalogV210.IDS[5];owned.remove(remove);assertTrue(r.restoreOwnedItems(owned,r.equipment()));F5mSaveStore.saveRewardsActive(r);F5mSaveStore.install(c);RpgProgressionState reopened=new RpgProgressionState();F5mSaveStore.restoreRewardsActive(reopened);assertTrue(F5mSaveStore.writable());assertEquals(owned,reopened.inventory());assertEquals(r.equipment(),reopened.equipment());assertEquals(r.bankInventory(),reopened.bankInventory());assertFalse(reopened.inventory().containsKey(remove));
 }
 @Test public void aliasOverflowRejectsAtomicallyAndOriginalSourceOnlyItemsNeverInventWearableMotions(){
  RpgProgressionState r=new RpgProgressionState();Map<String,Integer> before=new LinkedHashMap<>(r.inventory());Map<String,Integer> overflow=new LinkedHashMap<>();overflow.put("IT_TEST_WEAPON_MW002",999999);overflow.put("USER_ITEM_20261018_R02857",1);assertFalse(r.restoreOwnedItems(overflow,Collections.emptyMap()));assertEquals(before,r.inventory());r.enableEquipmentSandbox(true);int pending=0;for(String id:UnifiedItemCatalogV210.IDS)if(r.inventoryOnlyItem(id)){assertEquals(RpgProgressionState.RequirementResult.PENDING,r.currentRequirements(id));assertEquals(RpgProgressionState.EquipResult.REQUIREMENT_PENDING,r.equip(id));pending++;}assertTrue(pending>1000);
 }
 @Test public void actualGameViewInventoryEquipmentAndMerchantsUseCanonicalIdentities()throws Exception{
  GameView v=new GameView(c);v.layout(0,0,960,540);RuntimeState s=f.field(v,"state");RpgProgressionState r=s.rpg();r.enableEquipmentSandbox(true);for(String id:r.inventory().keySet())assertFalse(EquipmentIdentityV210.alias(id));
  Set<String> offers=new HashSet<>();for(TownCommerce.Offer offer:TownCommerce.offers(TownInteriorDef.Kind.EQUIPMENT)){assertFalse(EquipmentIdentityV210.alias(offer.id));assertTrue(offer.id,offers.add(offer.id));assertFalse(offer.name.contains("수련"));assertFalse(offer.description.contains("DEX 2"));}
  r.restoreGold(10000);String id=RpgProgressionState.canonicalItemId("IT_TEST_WEAPON_MW002");int before=r.inventory().get(id);assertEquals(TownCommerce.Result.OK,TownCommerce.transact(s,TownInteriorDef.Kind.EQUIPMENT,TownCommerce.Operation.BUY,"IT_TEST_WEAPON_MW002",1,()->true));assertEquals(Integer.valueOf(before+1),r.inventory().get(id));
  ItemWindowReferenceTest.tap(v,742,28);Bitmap frame=Bitmap.createBitmap(960,540,Bitmap.Config.ARGB_8888);v.draw(new Canvas(frame));save(frame,"actual-inventory");ItemWindow window=f.field(v,"itemWindow");int n=window.rows(r).size()-1;window.page=n/50;v.draw(new Canvas(frame));save(frame,"actual-last-inventory-page");
  ItemWindowReferenceTest.tap(v,916,66);assertEquals(RpgProgressionState.EquipResult.EQUIPPED,r.equipToSlot(id,"무기"));ItemWindowReferenceTest.tap(v,830,28);v.draw(new Canvas(frame));save(frame,"actual-equipment");v.pause();F5mSaveStore.install(c);GameView resumed=new GameView(c);RuntimeState restored=f.field(resumed,"state");assertEquals(id,restored.rpg().equipment().get("무기"));assertEquals(r.inventory(),restored.rpg().inventory());
 }
}
