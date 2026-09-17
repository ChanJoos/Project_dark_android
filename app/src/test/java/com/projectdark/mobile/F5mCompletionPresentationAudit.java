package com.projectdark.mobile;
/** Focused JVM audit for F5M completion presentation policy. */
public final class F5mCompletionPresentationAudit{
 public static void main(String[] args){
  require(F5mCompletionPresentation.showQuickQuest(F5mAdaptedPrologueQuest.State.AVAILABLE),"available tracker");
  require(F5mCompletionPresentation.showQuickQuest(F5mAdaptedPrologueQuest.State.ACTIVE),"active tracker");
  require(F5mCompletionPresentation.showQuickQuest(F5mAdaptedPrologueQuest.State.RETURN_READY),"return tracker");
  require(!F5mCompletionPresentation.showQuickQuest(F5mAdaptedPrologueQuest.State.COMPLETED),"completed tracker must disappear");
  require(F5mCompletionPresentation.dialogue(F5mAdaptedPrologueQuest.State.RETURN_READY).contains("보상"),"turn-in copy");
  require(F5mCompletionPresentation.dialogue(F5mAdaptedPrologueQuest.State.COMPLETED).contains("훈련 증표"),"completion reward copy");
  System.out.println("F5M completion presentation audit PASS");
 }
 private static void require(boolean ok,String label){if(!ok)throw new AssertionError(label);}
}
