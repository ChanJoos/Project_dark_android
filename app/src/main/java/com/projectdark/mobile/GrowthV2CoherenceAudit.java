package com.projectdark.mobile;

/** Regression contract for Growth v2 state ownership and derived growth. */
public final class GrowthV2CoherenceAudit {
  private GrowthV2CoherenceAudit(){}
  public static boolean verify(){
    RpgProgressionState r=new RpgProgressionState();
    int s=r.str(),i=r.intel(),w=r.wis(),c=r.con(),d=r.dex(),hp=r.maxHpGrowth(),mp=r.maxMpGrowth();
    int gained=r.grantAdaptedReward(1000000,0); if(gained<=0)return false;
    if(r.str()!=s||r.intel()!=i||r.wis()!=w||r.con()!=c||r.dex()!=d)return false;
    if(r.statPoints()!=gained*5||r.maxHpGrowth()<=hp||r.maxMpGrowth()<=mp)return false;
    int points=r.statPoints(),atk=r.physicalAttack();if(!r.spendStat("STR"))return false;
    if(r.str()!=s+1||r.intel()!=i||r.wis()!=w||r.con()!=c||r.dex()!=d||r.statPoints()!=points-1||r.physicalAttack()<=atk)return false;
    F5mAdaptedPrologueQuest q1=new F5mAdaptedPrologueQuest(F5mAdaptedPrologueQuest.OPENING_MONSTER_ID);
    GrowthQuest2 q2=new GrowthQuest2();if(ActiveQuestTracker.current(q1,q2)!=ActiveQuestTracker.Quest.PROLOGUE)return false;
    q1.restore(F5mAdaptedPrologueQuest.State.COMPLETED,1);q2.unlockIfPrologueCompleted(q1);
    if(ActiveQuestTracker.current(q1,q2)!=ActiveQuestTracker.Quest.GROWTH_2||!ActiveQuestTracker.visible(q1,q2))return false;
    if(!q2.accept()||ActiveQuestTracker.current(q1,q2)!=ActiveQuestTracker.Quest.GROWTH_2)return false;
    return true;
  }
  public static void main(String[] args){if(!verify())throw new AssertionError("GrowthV2CoherenceAudit failed");System.out.println("GROWTH_V2_COHERENCE=PASS");}
}
