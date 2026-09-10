package com.projectdark.mobile;

import java.util.List;

/** Deterministic audit for full [B] curve projection, reward application, cap and restart restore. */
public final class RpgExpProgressionAudit {
  private RpgExpProgressionAudit(){}
  public static boolean verify(){
    if(!LevelExpCurveCatalog.audit()||LevelExpCurveCatalog.entries().size()!=98)return false;
    if(LevelExpCurveCatalog.cumulativeRequiredForLevel(2)!=10000L)return false;
    if(LevelExpCurveCatalog.cumulativeRequiredForLevel(10)!=300000L)return false;
    if(LevelExpCurveCatalog.cumulativeRequiredForLevel(99)!=150000000L)return false;

    RpgProgressionState direct=new RpgProgressionState();
    RpgProgressionState.ExpApplyOutcome first=direct.applyNormalExp(308950,RpgProgressionState.Evidence.V);
    if(first.status!=RpgProgressionState.ExpApplyStatus.APPLIED||first.beforeLevel!=1||first.afterLevel!=10||
        first.levelsGained!=9||direct.normalExp()!=308950L||direct.expToNextLevel()!=69050L||
        first.curveEvidence!=RpgProgressionState.Evidence.B)return false;
    RpgProgressionState.ExpApplyOutcome next=direct.applyNormalExp(69050,RpgProgressionState.Evidence.V);
    if(next.afterLevel!=11||direct.expToNextLevel()!=84200L)return false;
    long unchanged=direct.normalExp();
    if(direct.applyNormalExp(0,RpgProgressionState.Evidence.V).status!=RpgProgressionState.ExpApplyStatus.INVALID_AMOUNT||
        direct.normalExp()!=unchanged)return false;

    RpgProgressionState capped=new RpgProgressionState();
    RpgProgressionState.ExpApplyOutcome cap=capped.applyNormalExp(150000000,RpgProgressionState.Evidence.B);
    if(cap.status!=RpgProgressionState.ExpApplyStatus.APPLIED||cap.afterLevel!=99||cap.levelsGained!=98||
        capped.normalExp()!=150000000L||capped.expToNextLevel()!=null)return false;
    if(capped.applyNormalExp(1,RpgProgressionState.Evidence.V).status!=RpgProgressionState.ExpApplyStatus.LEVEL_CAP||
        capped.normalExp()!=150000000L)return false;

    RpgProgressionState restored=new RpgProgressionState();
    if(restored.restoreSnapshot(direct.saveSnapshot())!=RpgProgressionState.RestoreResult.RESTORED||
        !restored.normalLevel().equals(direct.normalLevel())||!restored.normalExp().equals(direct.normalExp()))return false;

    RpgProgressionState rewardState=new RpgProgressionState();
    CombatLedger ledger=new CombatLedger();
    ledger.add(CombatLedger.Type.MONSTER_DEFEATED,"player","POTE_SPIRIT",0);
    List<CombatLedger.Event> events=ledger.snapshot();
    rewardState.consumeCombat(events,null);
    if(rewardState.normalLevel()!=10||rewardState.normalExp()!=308950L||rewardState.rewardHistory().size()!=1)return false;
    rewardState.consumeCombat(events,null);
    return rewardState.normalExp()==308950L&&rewardState.rewardHistory().size()==1;
  }
  public static void main(String[] args){
    if(!verify())throw new AssertionError("RpgExpProgressionAudit failed");
    System.out.println("RpgExpProgressionAudit PASS");
  }
}
