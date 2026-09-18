package com.projectdark.mobile;

import static org.junit.Assert.*;
import org.junit.Test;

public final class PostF5mProgressionAudit {
  @Test public void freshRuntimeStartsAtZeroExpWithoutInventedRatio(){
    RpgProgressionState rpg=new RpgProgressionState();
    assertEquals(Long.valueOf(0L),rpg.normalExp());
    assertEquals(Integer.valueOf(1),rpg.normalLevel());
    assertEquals("EXP · 0",PostF5mHudPresentation.expLabel(rpg));
    assertNull("No canonical next-level threshold: HUD must not fabricate a percentage",PostF5mHudPresentation.expRatio(rpg));
  }

  @Test public void existingInventoryAndEquipmentContractSurvivesProgressionChange(){
    RpgProgressionState rpg=new RpgProgressionState();
    assertEquals(Integer.valueOf(1),rpg.inventory().get(RpgProgressionState.STARTER_SHIRT_ITEM_ID));
    assertEquals(Integer.valueOf(1),rpg.inventory().get(RpgProgressionState.PLAYTEST_WEAPON_ITEM_ID));
    assertEquals(RpgProgressionState.EquipResult.EQUIPPED,rpg.equip(RpgProgressionState.PLAYTEST_WEAPON_ITEM_ID));
    assertEquals(RpgProgressionState.PLAYTEST_WEAPON_ITEM_ID,rpg.equipment().get(RpgProgressionState.WEAPON_SLOT));
  }
}