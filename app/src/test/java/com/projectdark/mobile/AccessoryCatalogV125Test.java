package com.projectdark.mobile;
import static org.junit.Assert.*;
import java.util.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.Config;
import android.content.Context;
import android.graphics.*;

@RunWith(RobolectricTestRunner.class) @Config(sdk=34,manifest=Config.NONE)
@org.robolectric.annotation.GraphicsMode(org.robolectric.annotation.GraphicsMode.Mode.NATIVE)
public class AccessoryCatalogV125Test {
 Context c;
 @Before public void setup(){c=RuntimeEnvironment.getApplication();c.getSharedPreferences("project_dark_f5m_v1",0).edit().clear().commit();F5mSaveStore.install(c);}
 static RpgProgressionState.ItemDefinition named(RpgProgressionState r,String name){for(RpgProgressionState.ItemDefinition d:r.itemDefinitions().values())if(d.name.equals(name))return d;throw new AssertionError(name);}
 @Test public void allEquipmentStartsOwnedInActualGameAndEveryIconDecodes()throws Exception{
  GameView view=new GameView(c);RpgProgressionState r=((RuntimeState)ItemWindowReferenceTest.field(view,"state")).rpg();
  SourceItemIconRegistry registry=new SourceItemIconRegistry(c);int connected=0;
  assertEquals(Integer.valueOf(1),r.normalLevel());assertEquals("COMMONER",r.currentJobCode());assertTrue(r.equipmentSandbox());
  assertEquals(r.itemDefinitions().keySet(),r.inventory().keySet());
  for(RpgProgressionState.ItemDefinition d:r.itemDefinitions().values())if(d.equippable()){
   assertEquals(d.itemId,Integer.valueOf(RpgProgressionState.pairedSlot(d.equipSlot)?2:1),r.inventory().get(d.itemId));assertEquals(RpgProgressionState.RequirementResult.MET,r.currentRequirements(d.itemId));
   if(d.itemId.startsWith("IT_SOURCE_")&&!d.name.startsWith("흑요석워리어")){assertNotNull(d.itemId,registry.get(d));connected++;}
  }
  for(String element:new String[]{"WATER","EARTH","WIND","FIRE"}){assertNotNull(element,registry.get(r.itemDefinitions().get("IT_NECK_"+element+"_PEARL")));assertNotNull(element,registry.get(r.itemDefinitions().get("IT_BELT_"+element+"_LEATHER")));}
  assertTrue(connected>=150);
  java.io.File dir=new java.io.File("build/reports/device-review");dir.mkdirs();
  java.nio.file.Files.write(new java.io.File(dir,"accessory-v125-coverage.json").toPath(),new org.json.JSONObject().put("ownedItems",r.inventory().size()).put("definitions",r.itemDefinitions().size()).put("sourceIconsLoaded",connected).put("job",r.currentJobCode()).put("level",r.normalLevel()).put("testRequirementsBypass",r.equipmentSandbox()).toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));
 }
 @Test public void fourCircleLeggingsAreSeparateAndChangeRealStats(){
  RuntimeState s=new RuntimeState();RpgProgressionState r=s.rpg();r.enableEquipmentSandbox(true);int base=r.finalStats().ac;
  for(Object[] row:new Object[][]{{"가죽각반",11,-2},{"동각반",41,-4},{"은각반",71,-6},{"금각반",99,-8}}){
   RpgProgressionState.ItemDefinition d=named(r,(String)row[0]);assertEquals(row[1],d.requiredLevel);
   assertEquals(RpgProgressionState.RequirementResult.LEVEL_NOT_MET,r.evaluateRequirements(d.itemId,"COMMONER",1));
   assertEquals(RpgProgressionState.EquipResult.EQUIPPED,r.equip(d.itemId));assertEquals(base+(Integer)row[2],r.finalStats().ac);
  }
  s.applyDerivedGrowth();assertTrue(s.player().maxHp>0);
 }
 @Test public void originalModifiersElementsAndSaveRestoreAreConnected()throws Exception{
  RpgProgressionState r=new RpgProgressionState();r.enableEquipmentSandbox(true);
  RpgProgressionState.ItemDefinition necklace=named(r,"숲의잼스톤목걸이"),belt=named(r,"다크그레이벨트(수)+1");
  int mp=r.finalStats().maxMp,hit=r.finalStats().hit,dam=r.finalStats().dam;
  assertEquals(RpgProgressionState.EquipResult.EQUIPPED,r.equip(necklace.itemId));assertEquals(RpgProgressionState.EquipResult.EQUIPPED,r.equip(belt.itemId));
  assertEquals(hit+5,r.finalStats().hit);assertEquals(dam+10,r.finalStats().dam);assertTrue(r.finalStats().maxMp>=mp+50);
  assertEquals("숲",r.equippedDefinition("목걸이").attackElement);assertEquals("바다",r.equippedDefinition("벨트").defenseElement);
  F5mSaveStore.saveRewardsActive(r);F5mSaveStore.install(c);RpgProgressionState restored=new RpgProgressionState();F5mSaveStore.restoreRewardsActive(restored);restored.enableEquipmentSandbox(false);
  assertEquals(r.inventory(),restored.inventory());assertEquals(r.equipment(),restored.equipment());assertEquals(r.finalStats().dam,restored.finalStats().dam);
  restored.restoreOwnedItems(Collections.emptyMap(),Collections.emptyMap());F5mSaveStore.saveRewardsActive(restored);
  GameView reopened=new GameView(c);RpgProgressionState after=((RuntimeState)ItemWindowReferenceTest.field(reopened,"state")).rpg();assertTrue(after.inventory().isEmpty()); // never regrant removed/banked gear on restart
 }
 @Test public void elementalEquipmentFeedsTheCombatPipeline(){
  RpgProgressionState a=new RpgProgressionState(),d=new RpgProgressionState();a.enableEquipmentSandbox(true);d.enableEquipmentSandbox(true);
  a.equip("IT_NECK_FIRE_PEARL");assertEquals(Integer.valueOf(5),a.equippedDefinition("목걸이").statModifiers.get("HIT"));d.equip("IT_BELT_WATER_LEATHER");
  assertEquals(38,ElementalDamageFormula.apply(100,a.finalStats().attackElement,d.finalStats().defenseElement));
  assertEquals(175,ElementalDamageFormula.apply(100,"생명","암흑"));assertEquals(100,ElementalDamageFormula.apply(100,"숲","숲"));
 }
 @Test public void realPagedInventoryCanReachAndEquipLastAccessoryAndRingSlot()throws Exception{
  GameView view=new GameView(c);view.layout(0,0,960,540);RuntimeState state=ItemWindowReferenceTest.field(view,"state");ItemWindow w=ItemWindowReferenceTest.field(view,"itemWindow");
  ItemWindowReferenceTest.tap(view,742,28);List<RpgInventoryPresentation.ItemRow> rows=w.rows(state.rpg());int last=rows.size()-1;
  for(int p=0;p<last/50;p++)ItemWindowReferenceTest.tap(view,894,480);
  assertEquals(last/50,w.page);RectF b=ItemWindow.cell(last%50);ItemWindowReferenceTest.tap(view,b.centerX(),b.centerY());
  assertEquals(rows.get(last).itemId,w.hitItem);ItemWindowReferenceTest.render(view,"accessory-v125-last-page-details.png");
  ItemWindowReferenceTest.tap(view,805,490);assertEquals(rows.get(last).itemId,state.rpg().equipment().get(rows.get(last).equipSlot));
  assertTrue(Arrays.asList(ItemWindow.SLOTS).contains("반지"));
 }
}
