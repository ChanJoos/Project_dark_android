package com.projectdark.mobile;
import static org.junit.Assert.*;
import java.util.*;
import java.io.*;
import org.json.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.Config;
import android.content.Context;
import android.graphics.*;

@RunWith(RobolectricTestRunner.class) @Config(sdk=34,manifest=Config.NONE)
@org.robolectric.annotation.GraphicsMode(org.robolectric.annotation.GraphicsMode.Mode.NATIVE)
public class EquipmentPerformanceV130Test {
 Context c;
 @Before public void setup(){c=RuntimeEnvironment.getApplication();c.getSharedPreferences("project_dark_f5m_v1",0).edit().clear().commit();F5mSaveStore.install(c);UiTheme.install(c);}
 RuntimeState state(){RuntimeState s=new RuntimeState();s.rpg().enableEquipmentSandbox(true);return s;}
 int hit(RuntimeState s){RuntimeState.Monster m=s.monsters().get(0);m.hp=10000;m.alive=true;new RuntimeCombatPortAdapter(s,(a,b)->true,(a,b)->true).applyDamage("player",m.id,"attack_proto_swing",8);return 10000-m.hp;}
 @Test public void everyWeaponHasVisiblePerformanceAndEachRangeReachesActualDamage()throws Exception{
  RuntimeState s=state();RpgProgressionState r=s.rpg();JSONArray audit=new JSONArray();int count=0;
  for(RpgProgressionState.ItemDefinition d:r.itemDefinitions().values())if("무기".equals(d.equipSlot)){
   assertTrue(d.itemId,d.statModifiers.containsKey("MinATK"));assertTrue(d.itemId,d.statModifiers.get("MaxATK")>0);
   r.unequip("무기");assertEquals(d.itemId,RpgProgressionState.EquipResult.EQUIPPED,r.equip(d.itemId));
   assertEquals(d.itemId,(int)d.statModifiers.get("MinATK"),r.finalStats().weaponMinAttack);assertEquals(d.itemId,(int)d.statModifiers.get("MaxATK"),r.finalStats().weaponMaxAttack);
   int applied=hit(s);assertTrue(d.itemId,applied>0);
   audit.put(new JSONObject().put("id",d.itemId).put("name",d.name).put("stats",new JSONObject(d.statModifiers)).put("actualPhysicalDamage",applied).put("sourceNote",SourceEquipmentPerformance.note(d.itemId)));count++;
  }
  assertEquals(56,count);File dir=directory();java.nio.file.Files.write(new File(dir,"all56-weapon-runtime-audit.json").toPath(),audit.toString(2).getBytes("UTF-8"));
  r.unequip("무기");r.equip("IT_WARDROBE_MW039");int axe=hit(s);r.unequip("무기");r.equip("IT_WARDROBE_MW023");int gaia=hit(s);assertTrue(gaia>axe+100);
  assertEquals(150,r.finalStats().weaponMinAttack);assertEquals(210,r.finalStats().weaponMaxAttack);
 }
 @Test public void namedShieldsReduceRealEnemyDamageAndMagicDamageAndKeepSourcePenalties(){
  RuntimeState s=state();RpgProgressionState r=s.rpg();RuntimeCombatPortAdapter p=new RuntimeCombatPortAdapter(s,(a,b)->true,(a,b)->true);String enemy=s.monsters().get(0).id;
  r.unequip("방패");s.player().hp=100;p.applyDamage(enemy,"player","attack_proto_swing",40);int bare=100-s.player().hp;
  r.equip("IT_SHIELD_GOLD");s.player().hp=100;p.applyDamage(enemy,"player","attack_proto_swing",40);int shield=100-s.player().hp;assertTrue(shield<bare);
  FinalStats f=r.finalStats();assertEquals(20,f.magicDefense);assertEquals(80,CombatStatPipeline.resolveSkill(100,CombatStatPipeline.Channel.MAGIC,FinalStats.neutral(100,100),f).applied);
  r.unequip("방패");r.equip("IT_SHIELD_DEATHKNIGHT");assertEquals(Integer.valueOf(-500),r.itemDefinitions().get("IT_SHIELD_DEATHKNIGHT").statModifiers.get("HP"));assertEquals(Integer.valueOf(-500),r.itemDefinitions().get("IT_SHIELD_DEATHKNIGHT").statModifiers.get("MP"));assertEquals(30,r.finalStats().magicDefense);
  // An unverified wearable ID must not silently attach another shield's paper-doll.
  assertNull(r.itemDefinitions().get("IT_SHIELD_DEATHKNIGHT").appearanceId);
 }
 @Test public void allNamedShieldsAreOwnedSelectableAndHaveCorrectOriginalIcons()throws Exception{
  GameView view=new GameView(c);view.layout(0,0,960,540);RuntimeState s=ItemWindowReferenceTest.field(view,"state");SourceItemIconRegistry icons=new SourceItemIconRegistry(c);int count=0;
  for(RpgProgressionState.ItemDefinition d:s.rpg().itemDefinitions().values())if(d.itemId.startsWith("IT_SHIELD_")){
   assertTrue(d.itemId,s.rpg().inventory().containsKey(d.itemId));assertTrue(d.itemId,d.statModifiers.get("AC")<0);assertNotNull(d.itemId,icons.get(d));assertEquals("EXACT_SOURCE_KOREAN_LABEL",icons.receipt(d.itemId).getString("identityMatch"));count++;
  }
  assertEquals(14,count);
  for(String id:new String[]{"IT_WARDROBE_MW023","IT_WARDROBE_MW044","IT_WARDROBE_MW062","IT_SHIELD_LEATHER","IT_SHIELD_GOLD","IT_SHIELD_DEATHKNIGHT"}){
   ItemWindowReferenceTest.tap(view,608,28);ItemWindow w=ItemWindowReferenceTest.field(view,"itemWindow");List<RpgInventoryPresentation.ItemRow> rows=w.rows(s.rpg());int index=-1;for(int i=0;i<rows.size();i++)if(rows.get(i).itemId.equals(id))index=i;assertTrue(index>=0);
   while(w.page<index/50)ItemWindowReferenceTest.tap(view,894,480);while(w.page>index/50)ItemWindowReferenceTest.tap(view,372,480);
   RectF b=ItemWindow.cell(index%50);ItemWindowReferenceTest.tap(view,b.centerX(),b.centerY());
   Bitmap image=Bitmap.createBitmap(960,540,Bitmap.Config.ARGB_8888);view.draw(new Canvas(image));try(FileOutputStream out=new FileOutputStream(new File(directory(),id+"-stats.png"))){assertTrue(image.compress(Bitmap.CompressFormat.PNG,100,out));}image.recycle();
   ItemWindowReferenceTest.tap(view,916,247);ItemWindowReferenceTest.tap(view,916,66);
  }
 }
 @Test public void sourceRangesAndNewShieldOwnershipSurviveActualSaveRestore(){
  RuntimeState s=state();RpgProgressionState r=s.rpg();r.equip("IT_WARDROBE_MW023");r.equip("IT_SHIELD_PHOBOS");int damage=hit(s);int str=r.str();F5mSaveStore.saveRewardsActive(r);assertTrue(F5mSaveStore.writable());
  F5mSaveStore.install(c);RpgProgressionState copy=new RpgProgressionState();F5mSaveStore.restoreRewardsActive(copy);assertEquals(r.equipment(),copy.equipment());assertEquals(r.inventory(),copy.inventory());assertEquals(str,copy.str());assertEquals(150,copy.finalStats().weaponMinAttack);assertEquals(210,copy.finalStats().weaponMaxAttack);assertEquals(r.finalStats().ac,copy.finalStats().ac);assertEquals(r.finalStats().magicDefense,copy.finalStats().magicDefense);assertTrue(damage>100);
 }
 @Test public void adaptedRangesAndMissingLRemainExplicitInsteadOfClaimingOriginalFacts(){
  RpgProgressionState r=new RpgProgressionState();RpgProgressionState.ItemDefinition pana=r.itemDefinitions().get("IT_WARDROBE_MW062");assertEquals(Integer.valueOf(8),pana.statModifiers.get("MinATK"));assertEquals(RpgProgressionState.Evidence.ADAPTED,pana.evidence);assertTrue(ItemEffects.description(pana).toString().contains("원작 공격력 미확정"));assertFalse(pana.statModifiers.containsKey("LMaxATK"));
  assertFalse(r.itemDefinitions().get("IT_WARDROBE_MW023").statModifiers.containsKey("LMaxATK"));assertEquals(Integer.valueOf(14),r.itemDefinitions().get("IT_WARDROBE_MW018").statModifiers.get("LMinATK"));
 }
 File directory(){File dir=new File("build/reports/equipment-performance-v130");dir.mkdirs();return dir;}
}
