package com.projectdark.mobile;
import static org.junit.Assert.*;
import org.junit.Test;
public final class EquipmentStatsAndDetailsTest {
 @Test public void starterEquipmentProjectsIntoFinalStats(){
   RpgProgressionState r=new RpgProgressionState();FinalStats f=r.finalStats();
   assertEquals(0,f.dam);assertEquals(0,f.hit);assertEquals(-4,f.ac);assertEquals(3,f.dex);
   assertEquals(4,f.weaponMinAttack);assertEquals(14,f.weaponMaxAttack);
   assertEquals(18,f.prototypePhysicalAttack());
 }
 @Test public void unequipImmediatelyChangesFinalStats(){
   RpgProgressionState r=new RpgProgressionState();int atk=r.finalStats().prototypePhysicalAttack();
   assertEquals(RpgProgressionState.EquipResult.UNEQUIPPED,r.equip(RpgProgressionState.PLAYTEST_WEAPON_ITEM_ID));
   assertEquals(18,atk);assertEquals(17,r.finalStats().prototypePhysicalAttack());assertEquals(0,r.finalStats().dam);assertEquals(0,r.finalStats().hit);
   assertEquals(RpgProgressionState.EquipResult.UNEQUIPPED,r.equip(RpgProgressionState.STARTER_SHIELD_ITEM_ID));
   assertEquals(-4,r.finalStats().ac);assertTrue(r.itemDefinitions().get(RpgProgressionState.STARTER_SHIELD_ITEM_ID).statModifiers.isEmpty());
   assertEquals(RpgProgressionState.EquipResult.UNEQUIPPED,r.equip(RpgProgressionState.STARTER_SHIRT_ITEM_ID));assertEquals(0,r.finalStats().ac);
 }
 @Test public void sourceMokdoOptionsResolveButUnidentifiedShoesStayPending(){
   RpgProgressionState r=new RpgProgressionState();
   assertEquals(RpgProgressionState.Evidence.U,r.itemDefinitions().get(RpgProgressionState.PLAYTEST_WEAPON_ITEM_ID).evidence);
   assertEquals(RpgProgressionState.Evidence.PENDING,r.itemDefinitions().get("IT_SHOES").evidence);assertTrue(r.itemDefinitions().get("IT_SHOES").statModifiers.isEmpty());
 }
}