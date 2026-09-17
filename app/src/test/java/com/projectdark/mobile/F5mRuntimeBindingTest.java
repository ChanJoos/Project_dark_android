package com.projectdark.mobile;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/** Android-capable regression for the production Milles -> defeat ledger -> quest binding. */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34, manifest = Config.NONE)
public final class F5mRuntimeBindingTest {
  @Test
  public void productionMillesDefeatAdvancesAdaptedQuestExactlyOnce() {
    F5mRuntimeBindingAudit.Result result = F5mRuntimeBindingAudit.run();
    assertTrue(result.detail, result.passed);
    assertTrue("runtime target identity diverged", F5mAdaptedPrologueQuest.OPENING_MONSTER_ID.equals(result.monsterId));
    assertTrue("authoritative defeat sequence missing", result.defeatSequence > 0L);
  }
}
