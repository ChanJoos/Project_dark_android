package com.projectdark.mobile.world;

import static org.junit.Assert.assertTrue;
import org.junit.Test;

public final class ReagentShopWorldAuditTest {
  @Test public void reagentShopUsesCanonicalWorldMovementContract(){assertTrue(ReagentShopWorldAudit.verify());}
}
