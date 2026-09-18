package com.projectdark.mobile.world;

import static org.junit.Assert.assertTrue;
import org.junit.Test;

/** Robolectric/JVM wrapper for the exact production-coordinate Quick Quest route regression. */
public final class F5mProductionQuickQuestRouteTest {
  @Test public void productionNpcMonsterNpcRouteAvoidsSawTooth(){assertTrue(F5mProductionQuickQuestRouteAudit.verify());}
}
