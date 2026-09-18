package com.projectdark.mobile;

import static org.junit.Assert.*;
import org.junit.Test;

public final class F5mNextPurposeAudit {
  @Test public void completedQuestExposesGrowthLoopPurpose(){
    assertTrue(F5mCompletionPresentation.showNextPurpose(F5mAdaptedPrologueQuest.State.COMPLETED));
    assertEquals("다음 목표", F5mCompletionPresentation.nextPurposeTitle(F5mAdaptedPrologueQuest.State.COMPLETED));
    String body=F5mCompletionPresentation.nextPurposeBody(F5mAdaptedPrologueQuest.State.COMPLETED);
    assertTrue(body.contains("EXP"));
    assertTrue(body.contains("BAG"));
  }

  @Test public void purposeDoesNotLeakBeforeCompletion(){
    assertFalse(F5mCompletionPresentation.showNextPurpose(F5mAdaptedPrologueQuest.State.AVAILABLE));
    assertFalse(F5mCompletionPresentation.showNextPurpose(F5mAdaptedPrologueQuest.State.ACTIVE));
    assertFalse(F5mCompletionPresentation.showNextPurpose(F5mAdaptedPrologueQuest.State.RETURN_READY));
  }

  @Test public void completedNpcDialoguePointsPlayerForward(){
    String dialogue=F5mCompletionPresentation.dialogue(F5mAdaptedPrologueQuest.State.COMPLETED);
    assertNotNull(dialogue);
    assertTrue(dialogue.contains("경험"));
    assertTrue(dialogue.contains("장비"));
  }
}