package com.projectdark.mobile;

import com.projectdark.mobile.world.PoteFieldDef;
import com.projectdark.mobile.world.PoteFieldRenderer;
import org.junit.Test;
import static org.junit.Assert.*;

public final class PoteForestSpatialGrammarTest {
  @Test public void forestUsesDenseAuthoredAssetPlacements(){
    PoteFieldRenderer renderer=new PoteFieldRenderer();
    assertEquals("POTE_FOREST_MASS_V3",PoteFieldRenderer.STATUS);
    assertTrue("forest should be densely authored, not a sparse shell",renderer.placementCount()>=120);
  }

  @Test public void navigationRetainsCorridorsAroundDenseGroves(){
    assertTrue(PoteFieldDef.navigationTiles().size()>250);
    // Entry and exit must remain represented by nearby walkable navigation tiles after visual/collision redesign.
    assertTrue(PoteFieldDef.navigationTiles().stream().anyMatch(t -> Math.abs(t.x-PoteFieldDef.ENTRY_X)<=64f && Math.abs(t.y-PoteFieldDef.ENTRY_Y)<=32f));
    assertTrue(PoteFieldDef.navigationTiles().stream().anyMatch(t -> Math.abs(t.x-PoteFieldDef.EXIT_X)<=64f && Math.abs(t.y-PoteFieldDef.EXIT_Y)<=32f));
    assertFalse(PoteFieldDef.atExit(PoteFieldDef.ENTRY_X,PoteFieldDef.ENTRY_Y));
    assertTrue(PoteFieldDef.atExit(PoteFieldDef.EXIT_X,PoteFieldDef.EXIT_Y));
  }

  @Test public void fieldIsMateriallyLargerThanOldPrototypeShell(){
    assertTrue(PoteFieldDef.MAX_X-PoteFieldDef.MIN_X>=1200f);
    assertTrue(PoteFieldDef.MAX_Y-PoteFieldDef.MIN_Y>=760f);
  }
}
