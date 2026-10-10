package com.projectdark.mobile;
import static org.junit.Assert.*;
import java.util.*;
import java.io.*;
import java.lang.reflect.*;
import org.json.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.Config;
import android.content.Context;
import android.graphics.*;
@RunWith(RobolectricTestRunner.class) @Config(sdk=34,manifest=Config.NONE)
@org.robolectric.annotation.GraphicsMode(org.robolectric.annotation.GraphicsMode.Mode.NATIVE)
public class ItemFunctionsV129Test {
 Context c;
 @Before public void setup(){c=RuntimeEnvironment.getApplication();c.getSharedPreferences("project_dark_f5m_v1",0).edit().clear().commit();F5mSaveStore.install(c);}
 RuntimeState state(){RuntimeState s=new RuntimeState();s.rpg().enableEquipmentSandbox(true);s.rpg().restoreBaseResources(5000,5000);s.applyDerivedGrowth();s.player().hp=3000;s.player().mp=3000;return s;}
 String skill(String name){for(SkillAbilityCatalog.Ability a:SkillAbilityCatalog.all())if(a.name.equals(name))return a.id;throw new AssertionError(name);}
 @Test public void actualResolverSnapshotsOneSecondReductionAndManaChargeWithoutChangingCooldown(){
  RuntimeState s=state();s.rpg().equipToSlot("IT_WARDROBE_MW062","무기");String id=skill("홀리쇼크");RuntimeCombatPortAdapter p=new RuntimeCombatPortAdapter(s,(a,t)->true,(a,i)->true);
  CombatResolver r=new CombatResolver(p);CombatResolver.Definition d=new CombatResolver.Definition(id,CombatResolver.ActionKind.MAGIC,CombatResolver.ActionState.MAGIC,CombatResolver.EffectType.MAGIC_HIT,true,120,7,999,3,1);
  int mp=s.player().mp;assertTrue(r.begin(d,"player","player",CombatResolver.InputMode.MANUAL).accepted);assertEquals(mp-17,s.player().mp);assertEquals(2,r.actionSnapshots().get(0).hitTime,.001f);assertEquals(7,p.cooldownRemaining("player",id),.001f);
  s.rpg().unequip("무기");r.tick(1.99f);assertEquals(1,r.activeActionCount());r.tick(.02f);assertEquals(0,r.activeActionCount());assertEquals(3,p.resolveDefinition("player",d).hitTime,.001f);
 }
 @Test public void realSessionAndDescriptionReadTheSameEquippedEffect(){
  RuntimeState s=state();RuntimeCombatSession session=new RuntimeCombatSession(s,(a,t)->true,(a,id)->true,a->true);String id=skill("홀리쇼크");assertEquals(3,session.effectiveCastSeconds(id),.001f);
  s.rpg().equipToSlot("IT_WARDROBE_MW062","무기");assertEquals(2,session.effectiveCastSeconds(id),.001f);assertEquals(17,session.effectiveMpCost(id));assertTrue(ItemEffects.description(s.rpg().itemDefinitions().get("IT_WARDROBE_MW062")).toString().contains("-1초"));
  F5mSaveStore.saveRewardsActive(s.rpg());F5mSaveStore.install(c);RpgProgressionState restored=new RpgProgressionState();F5mSaveStore.restoreRewardsActive(restored);assertEquals("매직파나",ItemEffects.weaponName(restored));assertEquals(Integer.valueOf(11),restored.itemDefinitions().get("IT_WARDROBE_MW062").requiredLevel);assertEquals(2,ItemEffects.castSeconds(restored,id,3),.001f);
 }
 @Test public void staffConditionalProfilesDoNotTurnIntoGenericCooldownReduction(){
  assertEquals(1,ItemEffects.castFor("매직새티아",2,false,false),0);assertEquals(3,ItemEffects.castFor("매직새티아",3,false,false),0);
  assertEquals(2,ItemEffects.castFor("매직쥬피티아",4,false,false),0);assertEquals(3,ItemEffects.castFor("매직쥬피티아",3,false,false),0);
  assertEquals(4,ItemEffects.castFor("매직가이아",6,false,false),0);assertEquals(3,ItemEffects.castFor("매직솔라",1,false,false),0);
  assertEquals(0,ItemEffects.castFor("매직파나",5,true,false),0);assertEquals(1,ItemEffects.castFor("매직파나",5,false,true),0);
  assertEquals(0,ItemEffects.castFor("홀리머큐리아",5,true,false),0);
  RuntimeState s=state();s.rpg().equipToSlot("IT_WARDROBE_MW062","무기");String physical=skill("숏블레이드");assertEquals(.14f,ItemEffects.castSeconds(s.rpg(),physical,.14f),0);assertEquals(20,ItemEffects.mpCost(s.rpg(),physical,20));
 }
 @Test public void manaReductionAlsoChangesValidationAtTheBoundary(){
  RuntimeState s=state();s.rpg().equipToSlot("IT_WARDROBE_MW062","무기");String id=skill("홀리쇼크");RuntimeCombatPortAdapter p=new RuntimeCombatPortAdapter(s,(a,t)->true,(a,i)->true);CombatResolver r=new CombatResolver(p);
  CombatResolver.Definition d=new CombatResolver.Definition(id,CombatResolver.ActionKind.MAGIC,CombatResolver.ActionState.MAGIC,CombatResolver.EffectType.MAGIC_HIT,true,120,7,999,3,1);
  s.player().mp=16;assertFalse(r.begin(d,"player","player",CombatResolver.InputMode.MANUAL).accepted);assertEquals(16,s.player().mp);
  s.player().mp=17;assertTrue(r.begin(d,"player","player",CombatResolver.InputMode.MANUAL).accepted);assertEquals(0,s.player().mp);
 }
 @Test public void healingReagentsConsumeExactlyOneAndNeverConsumeOnFullOrDead(){
  for(Object[] row:new Object[][]{{"IT_REAGENT_CURANUM",1000},{"IT_REAGENT_CURUM",200},{"IT_REAGENT_EXCURANUM",10000},{"IT_B_SMALL_POTION",35}}){
   RuntimeState s=state();String id=(String)row[0];s.player().hp=1;assertTrue(s.rpg().isConsumable(id));assertEquals(RpgProgressionState.UseResult.USED,s.rpg().useConsumable(id,s));assertEquals(Math.min(5000,1+(Integer)row[1]),s.player().hp);assertFalse(s.rpg().inventory().containsKey(id));
   s.rpg().autoLootResolvedItem(id,2);s.player().hp=s.player().maxHp;assertEquals(RpgProgressionState.UseResult.NO_EFFECT,s.rpg().useConsumable(id,s));assertEquals(Integer.valueOf(2),s.rpg().inventory().get(id));
   s.player().alive=false;s.player().hp=0;assertEquals(RpgProgressionState.UseResult.NO_EFFECT,s.rpg().useConsumable(id,s));assertEquals(Integer.valueOf(2),s.rpg().inventory().get(id));
  }
 }
 @Test public void antidoteHasARealStatusTargetAndOtherReagentsCannotMasqueradeAsHealing(){
  RuntimeState s=state();String id=RpgProgressionState.REAGENT_DIBENOMUM_ITEM_ID;assertEquals(RpgProgressionState.UseResult.NO_EFFECT,s.rpg().useConsumable(id,s));
  s.skillEffects().put("player","fixture","POISON",10,30);assertEquals(RpgProgressionState.UseResult.USED,s.rpg().useConsumable(id,s));assertFalse(s.skillEffects().has("player","POISON"));
  assertEquals(RpgProgressionState.UseResult.NOT_CONSUMABLE,s.rpg().useConsumable(RpgProgressionState.REAGENT_KOMADIUM_ITEM_ID,s));assertEquals(Integer.valueOf(1),s.rpg().inventory().get(RpgProgressionState.REAGENT_KOMADIUM_ITEM_ID));
  assertTrue(ItemEffects.description(s.rpg().itemDefinitions().get(RpgProgressionState.REAGENT_KOMADIUM_ITEM_ID)).toString().contains("미구현"));
 }
 @Test public void bothGlovesRegenerateHpOnlyAndRemovalStopsTheEffect(){
  RuntimeState s=state();String id=null;for(RpgProgressionState.ItemDefinition d:s.rpg().itemDefinitions().values())if(d.name.equals("버팔로가죽장갑"))id=d.itemId;assertNotNull(id);
  s.rpg().equipToSlot(id,"장갑");s.rpg().equipToSlot(id,RpgProgressionState.RIGHT_GLOVE_SLOT);assertEquals(20,ItemEffects.regeneration(s.rpg()));int mp=s.player().mp,hp=s.player().hp;
  s.tick(24);assertEquals(hp,s.player().hp);s.tick(1);assertEquals(hp+100,s.player().hp);assertEquals(mp,s.player().mp);
  s.rpg().unequip("장갑");s.rpg().unequip(RpgProgressionState.RIGHT_GLOVE_SLOT);hp=s.player().hp;s.tick(25);assertEquals(hp,s.player().hp);
 }
 @Test public void sourceArmorOptionsActuallyChangeStatsWhileUnresolvedArmorStaysHonest(){
  RuntimeState s=state();s.rpg().unequip("갑옷");int ac=s.rpg().finalStats().ac;s.rpg().equipToSlot("IT_WARDROBE_MU0000012","갑옷");assertEquals(ac-24,s.rpg().finalStats().ac);
  assertTrue(ItemEffects.description(s.rpg().itemDefinitions().get("IT_WARDROBE_MU0000044")).toString().contains("미확정"));
 }
 @Test public void all408RegisteredItemsHaveAnActualDescriptionAndACompleteAuditRow()throws Exception{
  RuntimeState s=state();JSONArray rows=new JSONArray();Set<String> allowed=new HashSet<>(Arrays.asList("MinATK","MaxATK","AC","HIT","DAM","MAGIC_DEFENSE","HP","MP","STR","INT","WIS","CON","DEX","REGEN","DAMAGE_REDUCTION_PCT","FLAT_MITIGATION","AC_IGNORE"));int pending=0;
  for(RpgProgressionState.ItemDefinition d:s.rpg().itemDefinitions().values()){
   assertFalse(d.itemId,ItemEffects.description(d).isEmpty());for(String k:d.statModifiers.keySet())assertTrue(d.itemId+":"+k,allowed.contains(k));
   if(d.evidence==RpgProgressionState.Evidence.PENDING)pending++;
   rows.put(new JSONObject().put("id",d.itemId).put("name",d.name).put("slot",d.equipSlot==null?JSONObject.NULL:d.equipSlot).put("stats",new JSONObject(d.statModifiers)).put("evidence",d.evidence.name()).put("usable",s.rpg().isConsumable(d.itemId)||s.rpg().isRecall(d.itemId)).put("description",new JSONArray(ItemEffects.description(d))).put("originalFullFunctionVerified",false));
  }
  assertEquals(408,rows.length());assertTrue(pending>0);File dir=new File("build/reports/item-functions-v129");dir.mkdirs();java.nio.file.Files.write(new File(dir,"all408-runtime-function-audit.json").toPath(),rows.toString(2).getBytes(java.nio.charset.StandardCharsets.UTF_8));
 }
 @Test public void curanumActualInventoryUseButtonRestoresHpConsumesOneAndSurvivesRestart()throws Exception{
  GameView view=new GameView(c);view.layout(0,0,960,540);RuntimeState s=ItemWindowReferenceTest.field(view,"state");ItemWindow w=ItemWindowReferenceTest.field(view,"itemWindow");
  String id=RpgProgressionState.REAGENT_CURANUM_ITEM_ID;s.rpg().restoreBaseResources(5000,5000);s.applyDerivedGrowth();s.player().hp=1;
  int owned=s.rpg().inventory().getOrDefault(id,0);assertTrue(owned>0);float x=s.player().x,y=s.player().y;
  ItemWindowReferenceTest.tap(view,608,28);ItemWindowReferenceTest.tap(view,509,93);assertEquals(2,w.filter);
  int index=-1;List<RpgInventoryPresentation.ItemRow> rows=w.rows(s.rpg());for(int i=0;i<rows.size();i++)if(id.equals(rows.get(i).itemId))index=i;assertTrue(index>=0);
  w.page=index/50;android.graphics.RectF cell=ItemWindow.cell(index%50);ItemWindowReferenceTest.tap(view,cell.centerX(),cell.centerY());assertTrue(w.details);
  ItemWindowReferenceTest.tap(view,805,490);assertEquals(1001,s.player().hp);assertEquals(owned-1,s.rpg().inventory().getOrDefault(id,0).intValue());assertEquals(x,s.player().x,0);assertEquals(y,s.player().y,0);
  view.pause();GameView restarted=new GameView(c);RuntimeState restored=ItemWindowReferenceTest.field(restarted,"state");assertEquals(owned-1,restored.rpg().inventory().getOrDefault(id,0).intValue());
 }
 @Test public void actualInventoryAndSkillWindowsShowEffectsAndUseTheExistingTouchRoute()throws Exception{
  GameView view=new GameView(c);RuntimeState s=(RuntimeState)ItemWindowReferenceTest.field(view,"state");ItemWindow w=(ItemWindow)ItemWindowReferenceTest.field(view,"itemWindow");
  ItemWindow.Visuals visuals=(ItemWindow.Visuals)ItemWindowReferenceTest.field(view,"itemWindowVisuals");w.details=true;assertEquals(ItemWindow.Hit.CONSUMED,w.inventoryTouch(650,365,s.rpg()));assertTrue(w.effectDetails);
  File dir=new File("build/reports/item-functions-v129");dir.mkdirs();
  for(String id:new String[]{"IT_WARDROBE_MW062","IT_REAGENT_CURANUM",RpgProgressionState.REAGENT_KOMADIUM_ITEM_ID}){
   Bitmap b=Bitmap.createBitmap(960,540,Bitmap.Config.ARGB_8888);Canvas canvas=new Canvas(b);w.inventory(canvas,s.rpg(),id,visuals);try(FileOutputStream out=new FileOutputStream(new File(dir,id+"-details.png"))){b.compress(Bitmap.CompressFormat.PNG,100,out);}b.recycle();
  }
 }
}
