package com.projectdark.mobile;
/** World-facing F5M quest presentation/turn-in facade. */
public final class F5mQuestUiFlow{
 private F5mQuestUiFlow(){}
 /** The left objective surface remains after completion so the player is not dropped into a dead end. */
 public static boolean showQuickQuest(F5mAdaptedPrologueQuest q){
  if(q==null)return false;
  return F5mCompletionPresentation.showQuickQuest(q.state())||F5mCompletionPresentation.showNextPurpose(q.state());
 }
 public static boolean showingNextPurpose(F5mAdaptedPrologueQuest q){return q!=null&&F5mCompletionPresentation.showNextPurpose(q.state());}
 public static String objectiveTitle(F5mAdaptedPrologueQuest q){
  return showingNextPurpose(q)?F5mCompletionPresentation.nextPurposeTitle(q.state()):"Quick Quest";
 }
 public static String objectiveBody(F5mAdaptedPrologueQuest q){
  return showingNextPurpose(q)?F5mCompletionPresentation.nextPurposeBody(q.state()):"";
 }
 public static boolean canConfirmTurnIn(F5mAdaptedPrologueQuest q){return q!=null&&q.state()==F5mAdaptedPrologueQuest.State.RETURN_READY;}
 public static F5mTurnInCoordinator.Result confirmTurnIn(F5mAdaptedPrologueQuest q,RpgProgressionState r){return F5mTurnInCoordinator.complete(q,r);}
 public static String completionMessage(F5mTurnInCoordinator.Result result){switch(result){case COMPLETED:return "퀘스트 완료 · EXP +7,500 · Gold +100 · 훈련 증표 [B] x1";case ALREADY_COMPLETED:return "첫 훈련 완료 · 보상 수령 완료";case REWARD_FAILED:return "보상 지급 실패 · 다시 시도하세요";case SAVE_FAILED:return "보상 저장 실패 · 다시 시도하세요";default:return "아직 완료할 수 없습니다";}}
}