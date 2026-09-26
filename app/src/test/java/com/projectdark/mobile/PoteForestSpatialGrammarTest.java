package com.projectdark.mobile;

import com.projectdark.mobile.world.PoteFieldDef;
import com.projectdark.mobile.world.PoteFieldRenderer;
import org.junit.Test;
import static org.junit.Assert.*;

public final class PoteForestSpatialGrammarTest {
  @Test public void forestUsesDenseAuthoredAssetPlacements(){
    PoteFieldRenderer renderer=new PoteFieldRenderer();
    assertEquals("POTE_ASSET_DRIVEN_FOREST_V1",PoteFieldRenderer.STATUS);
    assertTrue("forest should be densely authored, not a sparse shell",renderer.placementCount()>=90);
  }

  @Test public void navigationRetainsCorridorsAroundDenseGroves(){
    assertTrue(PoteFieldDef.navigationTiles().size()>250);
    assertFalse(PoteFieldDef.atExit(PoteFieldDef.ENTRY_X,PoteFieldDef.ENTRY_Y));
    assertTrue(PoteFieldDef.atExit(PoteFieldDef.EXIT_X,PoteFieldDef.EXIT_Y));
  }

  @Test public void fieldIsMateriallyLargerThanOldPrototypeShell(){
    assertTrue(PoteFieldDef.MAX_X-PoteFieldDef.MIN_X>=1200f);
    assertTrue(PoteFieldDef.MAX_Y-PoteFieldDef.MIN_Y>=760f);
  }
}
