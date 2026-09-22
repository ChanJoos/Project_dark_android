package com.projectdark.mobile;

/** [ADAPTED] Second playable growth quest unlocked after the F5M prologue. */
public final class GrowthQuest2 {
  public static final String QUEST_ID="Q_ADAPTED_GROWTH_02";
  public static final int REQUIRED=3;
  public enum State{LOCKED,AVAILABLE,ACTIVE,RETURN_READY,COMPLETED}
  private State state=State.LOCKED; private int defeated=0; private long lastSequence=0L;
  public State state(){return state;} public int currentCount(){return defeated;} public int requiredCount(){return REQUIRED;}
  public void unlockIfPrologueCompleted(F5mAdaptedPrologueQuest q){if(state==State.LOCKED&&q!=null&&q.state()==F5mAdaptedPrologueQuest.State.COMPLETED)state=State.AVAILABLE;}
  public boolean accept(){if(state!=State.AVAILABLE)return false;state=State.ACTIVE;persist();return true;}
  public boolean consume(CombatLedger.Event e){if(e==null||e.sequence<=lastSequence)return false;lastSequence=e.sequence;if(state!=State.ACTIVE||e.type!=CombatLedger.Type.MONSTER_DEFEATED||!isTrainingMonster(e.targetId))return false;defeated=Math.min(REQUIRED,defeated+1);if(defeated>=REQUIRED)state=State.RETURN_READY;persist();return true;}
  private static boolean isTrainingMonster(String id){return "combat_dummy_01".equals(id)||"combat_dummy_02".equals(id)||"combat_dummy_03".equals(id);}
  public boolean turnIn(RpgProgressionState r){if(state!=State.RETURN_READY||r==null)return false;if(F5mSaveStore.installed())return F5mSaveStore.commitQuest2TurnInActive(this,r);r.grantAdaptedReward(15000,250);state=State.COMPLETED;return true;}
  public void restore(State s,int count){state=s==null?State.LOCKED:s;defeated=Math.max(0,Math.min(REQUIRED,count));}
  public long consumedSequence(){return lastSequence;}
  public void restoreSequence(long value){lastSequence=Math.max(0L,value);}
  private void persist(){F5mSaveStore.saveQuest2Active(this);}
}
