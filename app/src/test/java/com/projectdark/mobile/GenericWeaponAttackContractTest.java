package com.projectdark.mobile;
import static org.junit.Assert.*;import org.junit.Test;
public final class GenericWeaponAttackContractTest {
 @Test public void alternateWeaponsRemainSwingAndVisualWeaponSlot(){
  RpgProgressionState r=new RpgProgressionState();
  for(String id:new String[]{"IT_TEST_WEAPON_MW002","IT_TEST_WEAPON_MW003","IT_TEST_WEAPON_MW004","IT_TEST_WEAPON_MW005","IT_TEST_WEAPON_MW006","IT_TEST_WEAPON_MW007","IT_TEST_WEAPON_MW008","IT_TEST_WEAPON_MW009","IT_TEST_WEAPON_MW010"}){
   RpgProgressionState.ItemDefinition d=r.itemDefinitions().get(id);assertNotNull(d);assertEquals(CharacterVisualBinding.VisualSlot.WEAPON,CharacterVisualBinding.visualSlotForAppearance(d.appearanceId));
   r.equip(id);CharacterVisualBinding b=CharacterVisualBinding.from(r);assertEquals(d.appearanceId,b.weaponVisualRef());assertTrue(CharacterRenderer.usesSourceActionPose(CharacterRenderer.State.ATTACK,AnimationAction.SWING));
  }
 }
}