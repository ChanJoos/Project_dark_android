package com.projectdark.mobile;

import static org.junit.Assert.*;

import android.content.Context;
import org.robolectric.RuntimeEnvironment;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/** F5M-028 process-boundary persistence/idempotency acceptance matrix. */
@RunWith(RobolectricTestRunner.class)
@Config(sdk=34,manifest=Config.NONE)
public final class F5mRestartPersistenceMatrixTest {
  private Context context;

  @Before public void resetStore(){
    context=RuntimeEnvironment.getApplication();
    context.getSharedPreferences("project_dark_f5m_v1",Context.MODE_PRIVATE).edit().clear().commit();
    F5mSaveStore.install(context);
  }

  @Test public void activeZeroOfOneRestores(){
    F5mAdaptedPrologueQuest q=F5mAdaptedPrologueQuest.openingFixture();
    assertEquals(F5mAdaptedPrologueQuest.AcceptResult.ACTIVATED,q.accept());
    F5mAdaptedPrologueQuest restored=F5mAdaptedPrologueQuest.openingFixture();
    assertEquals(F5mAdaptedPrologueQuest.State.ACTIVE,restored.state());
    assertEquals(0,restored.currentCount());
  }

  @Test public void returnReadyOneOfOneRestores(){
    F5mAdaptedPrologueQuest q=F5mAdaptedPrologueQuest.openingFixture();
    q.accept();
    CombatLedger ledger=new CombatLedger();
    ledger.add(CombatLedger.Type.MONSTER_DEFEATED,"player",F5mAdaptedPrologueQuest.OPENING_MONSTER_ID,1);
    assertEquals(F5mAdaptedPrologueQuest.DefeatResult.RETURN_READY,q.consume(ledger.snapshot().get(0)));
    F5mAdaptedPrologueQuest restored=F5mAdaptedPrologueQuest.openingFixture();
    assertEquals(F5mAdaptedPrologueQuest.State.RETURN_READY,restored.state());
    assertEquals(1,restored.currentCount());
  }

  @Test public void rpgRewardEquipmentStatsAndProgressionRestore(){
    RpgProgressionState r=new RpgProgressionState();
    r.grantAdaptedReward(2500,25);
    assertEquals(RpgProgressionState.AutoLootResult.LOOTED,r.autoLootResolvedItem(AdaptedPrototypeRewardCatalog.TRAINING_TOKEN_ITEM_ID,1));
    assertEquals(RpgProgressionState.EquipResult.EQUIPPED,r.equip(RpgProgressionState.PLAYTEST_WEAPON_ITEM_ID));
    r.restoreStats(7,6,5,8,9,4);
    F5mSaveStore.saveRewardsActive(r);

    RpgProgressionState restored=new RpgProgressionState();
    F5mSaveStore.restoreRewardsActive(restored);
    assertEquals(r.normalLevel(),restored.normalLevel());
    assertEquals(r.normalExp(),restored.normalExp());
    assertEquals(r.gold(),restored.gold());
    assertEquals(Integer.valueOf(1),restored.inventory().get(AdaptedPrototypeRewardCatalog.TRAINING_TOKEN_ITEM_ID));
    assertEquals(RpgProgressionState.PLAYTEST_WEAPON_ITEM_ID,restored.equipment().get(RpgProgressionState.WEAPON_SLOT));
    assertEquals(7,restored.str()); assertEquals(6,restored.intel()); assertEquals(5,restored.wis());
    assertEquals(8,restored.con()); assertEquals(9,restored.dex()); assertEquals(4,restored.statPoints());
  }

  @Test public void completedTurnInRestoresAndCannotDuplicateReward(){
    F5mAdaptedPrologueQuest q=F5mAdaptedPrologueQuest.openingFixture();
    q.accept();
    CombatLedger ledger=new CombatLedger();
    ledger.add(CombatLedger.Type.MONSTER_DEFEATED,"player",F5mAdaptedPrologueQuest.OPENING_MONSTER_ID,1);
    q.consume(ledger.snapshot().get(0));
    RpgProgressionState r=new RpgProgressionState();
    assertEquals(F5mTurnInCoordinator.Result.COMPLETED,F5mTurnInCoordinator.complete(q,r));
    int tokenQty=r.inventory().get(AdaptedPrototypeRewardCatalog.TRAINING_TOKEN_ITEM_ID);
    long exp=r.normalExp(),gold=r.gold();

    F5mSaveStore.install(context);
    F5mAdaptedPrologueQuest restoredQ=F5mAdaptedPrologueQuest.openingFixture();
    RpgProgressionState restoredR=new RpgProgressionState();
    F5mSaveStore.restoreRewardsActive(restoredR);
    assertEquals(F5mAdaptedPrologueQuest.State.COMPLETED,restoredQ.state());
    assertEquals(tokenQty,(int)restoredR.inventory().get(AdaptedPrototypeRewardCatalog.TRAINING_TOKEN_ITEM_ID));
    assertEquals(exp,(long)restoredR.normalExp());
    assertEquals(gold,(long)restoredR.gold());
    assertEquals(F5mTurnInCoordinator.Result.ALREADY_COMPLETED,F5mTurnInCoordinator.complete(restoredQ,restoredR));
    assertEquals(tokenQty,(int)restoredR.inventory().get(AdaptedPrototypeRewardCatalog.TRAINING_TOKEN_ITEM_ID));
    assertEquals(exp,(long)restoredR.normalExp());
    assertEquals(gold,(long)restoredR.gold());
    assertFalse(F5mCompletionPresentation.showQuickQuest(restoredQ.state()));
  }
}
