package com.projectdark.mobile;

/** Growth v2 contract: Quest2 -> EXP -> level -> stat points -> derived character growth. */
public final class GrowthV2LoopAudit {
  private GrowthV2LoopAudit(){}
  public static boolean verify(){
    RpgProgressionState r=new RpgProgressionState();
    F5mAdaptedPrologueQuest p=new F5mAdaptedPrologueQuest(F5mAdaptedPrologueQuest.OPENING_MONSTER_ID);
    p.accept();p.restore(F5mAdaptedPrologueQuest.State.COMPLETED,1);
    GrowthQuest2 q=new GrowthQuest2();q.unlockIfPrologueCompleted(p);if(q.state()!=GrowthQuest2.State.AVAILABLE||!q.accept())return false;
    CombatLedger ledger=new CombatLedger();
    for(int n=1;n<=3;n++){ledger.add(CombatLedger.Type.MONSTER_DEFEATED,"player","combat_dummy_01",0);if(!q.consume(ledger.latest()))return false;}
    if(q.state()!=GrowthQuest2.State.RETURN_READY||q.currentCount()!=3)return false;
    int atk=r.physicalAttack(),hp=r.maxHpGrowth(),mp=r.maxMpGrowth();long gold=r.gold();if(!q.turnIn(r))return false;
    if(r.gold()!=gold+250||r.normalExp()<0)return false;
    r.restoreStats(r.str(),r.intel(),r.wis(),r.con(),r.dex(),5);
    if(!r.spendStat("STR")||r.physicalAttack()<=atk)return false;
    r.restoreStats(r.str(),r.intel(),r.wis(),r.con()+1,r.dex(),4);if(r.maxHpGrowth()!=r.baseMaxHp())return false;
    r.restoreStats(r.str(),r.intel()+1,r.wis()+1,r.con(),r.dex(),2);if(r.maxMpGrowth()!=r.baseMaxMp())return false;
    return true;
  }
  public static void main(String[] args){if(!verify())throw new AssertionError("GrowthV2LoopAudit failed");System.out.println("GROWTH_V2_LOOP=PASS");}
}
