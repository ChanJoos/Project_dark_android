package com.projectdark.mobile;
import static org.junit.Assert.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.Config;
import android.content.Context;
import org.json.*;

@RunWith(RobolectricTestRunner.class) @Config(sdk=34,manifest=Config.NONE)
@org.robolectric.annotation.GraphicsMode(org.robolectric.annotation.GraphicsMode.Mode.NATIVE)
public class EquipmentFoundationV124Test {
 Context c;
 @Before public void setup(){c=RuntimeEnvironment.getApplication();c.getSharedPreferences("project_dark_f5m_v1",0).edit().clear().commit();F5mSaveStore.install(c);}
 static RuntimeCombatPortAdapter port(RuntimeState s){return new RuntimeCombatPortAdapter(s,(a,b)->true,(a,b)->true);}
 static int hit(RuntimeState s){RuntimeState.Monster m=s.monsters().get(0);int before=m.hp;port(s).applyDamage("player",m.id,"attack_proto_swing",8);return before-m.hp;}
 @Test public void actualWeaponHitAndHitsToKillChange(){
  RuntimeState s=new RuntimeState();RpgProgressionState r=s.rpg();RuntimeState.Monster m=s.monsters().get(0);
  int hp=m.maxHp;int mokdo=hit(s);m.hp=hp;m.alive=true;
  assertEquals(RpgProgressionState.EquipResult.EQUIPPED,r.equip("IT_TEST_WEAPON_MW002"));
  int epe=hit(s);assertEquals(Math.min(hp,37),epe);assertTrue(epe>mokdo);
  assertTrue((hp+epe-1)/epe < (hp+mokdo-1)/mokdo);
  m.hp=hp;m.alive=true;assertEquals(RpgProgressionState.EquipResult.EQUIPPED,r.equip(RpgProgressionState.CHUNGRYONG_ITEM_ID));
  assertEquals(Math.min(hp,11),hit(s)); // source S1~2 must not silently inherit stronger weapon/base floor
 }
 @Test public void weaponRangeSurvivesEffectsAndChangesRealPhysicalSkills(){
  RuntimeState s=new RuntimeState();RpgProgressionState r=s.rpg();RuntimeCombatPortAdapter p=port(s);
  p.prepareAction("player","SK_전사_001");int old=p.abilities.amount("SK_전사_001",8);p.finishAction("player","SK_전사_001");
  r.equip("IT_TEST_WEAPON_MW002");p.prepareAction("player","SK_전사_001");int upgraded=p.abilities.amount("SK_전사_001",8);
  assertTrue(upgraded>old);FinalStats f=r.finalStats().withEffects(s.skillEffects(),"player").withAttackElement("FIRE");assertEquals(20,f.weaponMinAttack);assertEquals(30,f.weaponMaxAttack);
 }
 @Test public void originalArmorDefenseAndResourcePenaltiesReachRuntime(){
  RuntimeState s=new RuntimeState();RpgProgressionState r=s.rpg();RuntimeCombatPortAdapter p=port(s);String enemy=s.monsters().get(0).id;
  s.player().hp=100;p.applyDamage(enemy,"player","attack_proto_swing",40);int before=100-s.player().hp;
  assertEquals(RpgProgressionState.EquipResult.EQUIPPED,r.equip(RpgProgressionState.REFERENCE_LEOPARD_ITEM_ID));s.applyDerivedGrowth();
  assertEquals(1600,s.player().maxHp);assertEquals(0,s.player().maxMp);assertTrue(s.player().mp>=0);assertEquals(0,r.finalStats().con);
  s.player().hp=100;p.applyDamage(enemy,"player","attack_proto_swing",40);assertTrue(100-s.player().hp<before);
  assertEquals(RpgProgressionState.EquipResult.EQUIPPED,r.equip(RpgProgressionState.REFERENCE_HELM_ITEM_ID));s.applyDerivedGrowth();assertEquals(2000,s.player().maxHp);assertEquals(100,s.player().maxMp);
 }
 @Test public void equippedSourceStatsRestoreWithoutMutatingBase(){
  RpgProgressionState r=new RpgProgressionState();int str=r.str();r.equip("IT_TEST_WEAPON_MW002");r.equip(RpgProgressionState.REFERENCE_LEOPARD_ITEM_ID);
  F5mSaveStore.saveRewardsActive(r);assertTrue(F5mSaveStore.writable());F5mSaveStore.install(c);RpgProgressionState copy=new RpgProgressionState();F5mSaveStore.restoreRewardsActive(copy);
  assertEquals(r.equipment(),copy.equipment());assertEquals(20,copy.finalStats().weaponMinAttack);assertEquals(1600,copy.finalStats().maxHp);assertEquals(str,copy.str());
 }
 @Test public void equipmentCannotHealByRepeatedCapacityToggle(){
  RuntimeState s=new RuntimeState();s.player().hp=50;s.player().mp=20;
  for(int i=0;i<3;i++){s.rpg().equip(RpgProgressionState.REFERENCE_HELM_ITEM_ID);s.applyDerivedGrowth();assertEquals(50,s.player().hp);assertEquals(20,s.player().mp);s.rpg().equip(RpgProgressionState.REFERENCE_HELM_ITEM_ID);s.applyDerivedGrowth();assertEquals(50,s.player().hp);assertEquals(20,s.player().mp);}
 }
 @Test public void actualInventoryUsesOriginalMasterIconsAndShowsRangeComparison()throws Exception{
  GameView v=new GameView(c);v.layout(0,0,960,540);RuntimeState s=ItemWindowReferenceTest.field(v,"state");
  SourceItemIconRegistry registry=new SourceItemIconRegistry(c);int count=0;
  for(RpgProgressionState.ItemDefinition d:s.rpg().itemDefinitions().values())if(d.appearanceId!=null&&!d.appearanceId.isEmpty()){assertNotNull(d.itemId,registry.get(d));count++;}
  for(String id:new String[]{"IT_RING_THREELINEGOLD"}){
   assertFalse(registry.pending(id));assertNotNull(registry.get(s.rpg().itemDefinitions().get(id)));
   assertEquals(Integer.valueOf(300),s.rpg().itemDefinitions().get(id).statModifiers.get("HP"));
  }
  assertTrue(count>=30);ItemWindowReferenceTest.tap(v,608,28);
  ItemWindow w=ItemWindowReferenceTest.field(v,"itemWindow");int index=-1;java.util.List<RpgInventoryPresentation.ItemRow> rows=w.rows(s.rpg());for(int i=0;i<rows.size();i++)if(rows.get(i).itemId.equals("IT_TEST_WEAPON_MW002"))index=i;
  assertTrue(index>=0);android.graphics.RectF b=ItemWindow.cell(index%50);ItemWindowReferenceTest.tap(v,b.centerX(),b.centerY());
  ItemWindowReferenceTest.render(v,"equipment-v124-source-epe-comparison.png");
  assertEquals(20,ItemWindow.mod(s.rpg().itemDefinitions().get("IT_TEST_WEAPON_MW002"),"MinATK"));
 }

}
