package com.projectdark.mobile;
/** Focused transaction coordinator for the adapted F5M quest turn-in. */
public final class F5mTurnInCoordinator{
 public enum Result{COMPLETED,NOT_READY,ALREADY_COMPLETED,REWARD_FAILED,SAVE_FAILED}
 private F5mTurnInCoordinator(){}
 public static Result complete(F5mAdaptedPrologueQuest q,RpgProgressionState r){
  if(q==null||r==null)return Result.NOT_READY;
  if(q.state()==F5mAdaptedPrologueQuest.State.COMPLETED)return F5mSaveStore.rewardClaimedActive()?Result.ALREADY_COMPLETED:Result.SAVE_FAILED;
  if(q.state()!=F5mAdaptedPrologueQuest.State.RETURN_READY)return Result.NOT_READY;
  if(!F5mSaveStore.commitTurnInActive(q,r))return Result.SAVE_FAILED;
  return Result.COMPLETED;
 }
}
