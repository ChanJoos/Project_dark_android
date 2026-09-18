package com.projectdark.mobile;

/** Single projection for the currently visible/interactive quick quest. */
public final class ActiveQuestTracker {
  public enum Quest { PROLOGUE,GROWTH_2,NONE }
  private ActiveQuestTracker(){}
  public static Quest current(F5mAdaptedPrologueQuest q1,GrowthQuest2 q2){
    if(q1!=null&&q1.state()!=F5mAdaptedPrologueQuest.State.COMPLETED)return Quest.PROLOGUE;
    if(q2!=null&&q2.state()!=GrowthQuest2.State.COMPLETED&&q2.state()!=GrowthQuest2.State.LOCKED)return Quest.GROWTH_2;
    return Quest.NONE;
  }
  public static boolean visible(F5mAdaptedPrologueQuest q1,GrowthQuest2 q2){return current(q1,q2)!=Quest.NONE;}
}
