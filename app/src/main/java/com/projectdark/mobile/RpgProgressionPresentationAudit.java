package com.projectdark.mobile;

/** Lightweight regression audit for progression presentation contracts. */
public final class RpgProgressionPresentationAudit {
  private RpgProgressionPresentationAudit(){}

  public static boolean run(){
    RpgProgressionPresentation p=new RpgProgressionPresentation();

    RpgProgressionState fresh=new RpgProgressionState();
    RpgProgressionPresentation.LevelProgress start=p.levelProgress(fresh);
    if(start.level==null||start.level!=1||start.cumulativeExp==null||start.cumulativeExp!=0L)return false;
    if(start.expRequiredThisLevel==null||start.expRequiredThisLevel!=10000L||start.expToNextLevel!=10000L)return false;
    if(start.ratio!=0f||start.evidence!=RpgProgressionState.Evidence.B)return false;
    if(p.basicJobSelectionGate(fresh).status!=RpgProgressionPresentation.JobSelectionGateStatus.LEVEL_NOT_MET)return false;

    RpgProgressionState.ExpApplyOutcome half=fresh.applyNormalExp(5000,RpgProgressionState.Evidence.B);
    if(half.status!=RpgProgressionState.ExpApplyStatus.APPLIED)return false;
    RpgProgressionPresentation.LevelProgress mid=p.levelProgress(fresh);
    if(mid.level!=1||mid.expIntoLevel!=5000L||mid.expToNextLevel!=5000L)return false;
    if(Math.abs(mid.ratio-0.5f)>0.0001f)return false;

    RpgProgressionState.ExpApplyOutcome toTen=fresh.applyNormalExp(295000,RpgProgressionState.Evidence.B);
    if(toTen.status!=RpgProgressionState.ExpApplyStatus.APPLIED||fresh.normalLevel()!=10||fresh.normalExp()!=300000L)return false;
    RpgProgressionPresentation.LevelProgress ten=p.levelProgress(fresh);
    if(ten.expRequiredThisLevel==null||ten.expRequiredThisLevel!=78000L||ten.expToNextLevel!=78000L)return false;
    if(p.basicJobSelectionGate(fresh).status!=RpgProgressionPresentation.JobSelectionGateStatus.SKILL_REQUIREMENT_PENDING)return false;

    RpgProgressionState high=new RpgProgressionState();
    RpgProgressionState.ExpApplyOutcome toNinetyNine=high.applyNormalExp(150000000,RpgProgressionState.Evidence.B);
    if(toNinetyNine.status!=RpgProgressionState.ExpApplyStatus.APPLIED||high.normalLevel()!=99||high.normalExp()!=150000000L)return false;
    RpgProgressionPresentation.LevelProgress capped=p.levelProgress(high);
    if(!capped.levelCap||capped.ratio!=1f||capped.expToNextLevel!=null)return false;
    if(high.applyNormalExp(1,RpgProgressionState.Evidence.B).status!=RpgProgressionState.ExpApplyStatus.LEVEL_CAP)return false;

    RpgVisibleProgressionPresentation.Snapshot visible=new RpgVisibleProgressionPresentation().snapshot(fresh);
    return visible.levelProgress!=null&&visible.jobSelectionGate!=null&&visible.levelProgress.level==10;
  }
}
