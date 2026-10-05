package com.projectdark.mobile;

import static org.junit.Assert.*;
import org.junit.Test;

public final class PostF5mProgressionAudit {
  @Test public void freshRuntimeStartsAtZeroExpWithoutInventedRatio(){
    RpgProgressionState rpg=new RpgProgressionState();
    assertEquals(Long.valueOf(0L),rpg.normalExp());
    assertEquals(Integer.valueOf(1),rpg.normalLevel());
    assertEquals("EXP · 0 / 10000",PostF5mHudPresentation.expLabel(rpg));
    assertEquals("Master now supplies the Lv1 threshold",0f,PostF5mHudPresentation.expRatio(rpg),0f);
  }

  @Test public void existingInventoryAndEquipmentContractSurvivesProgressionChange(){
    RpgProgressionState rpg=new RpgProgressionState();
    assertEquals(Integer.valueOf(1),rpg.inventory().get(RpgProgressionState.STARTER_SHIRT_ITEM_ID));
    assertEquals(Integer.valueOf(1),rpg.inventory().get(RpgProgressionState.PLAYTEST_WEAPON_ITEM_ID));
    assertEquals("starter weapon is already worn; the production action toggles it off",RpgProgressionState.EquipResult.UNEQUIPPED,rpg.equip(RpgProgressionState.PLAYTEST_WEAPON_ITEM_ID));
    assertFalse(rpg.equipment().containsKey(RpgProgressionState.WEAPON_SLOT));
    assertEquals(Integer.valueOf(1),rpg.inventory().get(RpgProgressionState.PLAYTEST_WEAPON_ITEM_ID));
    assertEquals(RpgProgressionState.EquipResult.EQUIPPED,rpg.equip(RpgProgressionState.PLAYTEST_WEAPON_ITEM_ID));
    assertEquals(RpgProgressionState.PLAYTEST_WEAPON_ITEM_ID,rpg.equipment().get(RpgProgressionState.WEAPON_SLOT));
  }
}