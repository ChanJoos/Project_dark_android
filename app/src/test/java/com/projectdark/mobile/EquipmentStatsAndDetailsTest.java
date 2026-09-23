package com.projectdark.mobile;
import static org.junit.Assert.*;
import org.junit.Test;
public final class EquipmentStatsAndDetailsTest {
 @Test public void starterEquipmentProjectsIntoFinalStats(){
   RpgProgressionState r=new RpgProgressionState();FinalStats f=r.finalStats();
   assertEquals(3,f.dam);assertEquals(1,f.hit);assertEquals(-4,f.ac);assertEquals(4,f.dex);
   assertEquals(20,f.prototypePhysicalAttack());
 }
 @Test public void unequipImmediatelyChangesFinalStats(){
   RpgProgressionState r=new RpgProgressionState();int atk=r.finalStats().prototypePhysicalAttack();
   assertEquals(RpgProgressionState.EquipResult.UNEQUIPPED,r.equip(RpgProgressionState.PLAYTEST_WEAPON_ITEM_ID));
   assertEquals(atk-3,r.finalStats().prototypePhysicalAttack());assertEquals(0,r.finalStats().dam);assertEquals(0,r.finalStats().hit);
   assertEquals(RpgProgressionState.EquipResult.UNEQUIPPED,r.equip(RpgProgressionState.STARTER_SHIELD_ITEM_ID));
   assertEquals(-2,r.finalStats().ac);
 }
 @Test public void sourceMasterGapRemainsExplicitlyAdapted(){
   RpgProgressionState r=new RpgProgressionState();
   assertEquals(RpgProgressionState.Evidence.ADAPTED,r.itemDefinitions().get(RpgProgressionState.PLAYTEST_WEAPON_ITEM_ID).evidence);
   assertEquals(RpgProgressionState.Evidence.ADAPTED,r.itemDefinitions().get("IT_SHOES").evidence);
 }
}