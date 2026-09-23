package com.projectdark.mobile;
import static org.junit.Assert.*;import org.junit.Test;
public final class GenericWeaponAttackContractTest {
 @Test public void alternateWeaponsRemainSwingAndVisualWeaponSlot(){
  RpgProgressionState r=new RpgProgressionState();
  for(String id:new String[]{"IT_TEST_WEAPON_MW002","IT_TEST_WEAPON_MW003"}){
   RpgProgressionState.ItemDefinition d=r.itemDefinitions().get(id);assertNotNull(d);assertEquals(AnimationAction.SWING,d.animationAction);assertEquals(CharacterVisualBinding.VisualSlot.WEAPON,CharacterVisualBinding.visualSlotForAppearance(d.appearanceId));
   r.equip(id);CharacterVisualBinding b=CharacterVisualBinding.from(r);assertEquals(d.appearanceId,b.weaponVisualRef());assertTrue(CharacterRenderer.usesSourceActionPose(CharacterRenderer.State.ATTACK,d.animationAction));
  }
 }
}