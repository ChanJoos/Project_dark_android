package com.projectdark.mobile;

/** Regression boundary for PASS 29 early EXP/level progression. */
public final class RpgEarlyExpProgressionAudit {
  private RpgEarlyExpProgressionAudit(){}

  public static boolean verify(){
    if(!LevelExpCurveCatalog.audit())return false;

    RpgProgressionState rpg=new RpgProgressionState();
    if(rpg.normalLevel()!=1||rpg.normalExp()==null||rpg.normalExp()!=0L)return false;
    if(rpg.expToNextLevel()==null||rpg.expToNextLevel()!=10000L)return false;

    RpgProgressionState.ExpApplyOutcome first=rpg.applyNormalExp(10000,RpgProgressionState.Evidence.B);
    if(first.status!=RpgProgressionState.ExpApplyStatus.APPLIED)return false;
    if(first.beforeLevel!=1||first.afterLevel!=2||first.levelsGained!=1)return false;
    if(rpg.normalLevel()!=2||rpg.normalExp()!=10000L)return false;
    if(rpg.expToNextLevel()==null||rpg.expToNextLevel()!=12800L)return false;

    RpgProgressionState.ExpApplyOutcome toTen=rpg.applyNormalExp(290000,RpgProgressionState.Evidence.B);
    if(toTen.status!=RpgProgressionState.ExpApplyStatus.APPLIED)return false;
    if(rpg.normalLevel()!=10||rpg.normalExp()!=300000L||rpg.expToNextLevel()!=null)return false;

    RpgProgressionState.ExpApplyOutcome beyond=rpg.applyNormalExp(1,RpgProgressionState.Evidence.B);
    if(beyond.status!=RpgProgressionState.ExpApplyStatus.PROJECTED_LEVEL_LIMIT)return false;
    if(rpg.normalExp()!=300000L)return false;

    // Existing nullable legacy save semantics remain fail-safe: null EXP does not become zero on restore.
    RpgSaveSnapshot legacyLike=new RpgSaveSnapshot(RpgSaveSnapshot.CURRENT_SCHEMA_VERSION,
        RpgProgressionState.ProgressionNode.COMMONER,"COMMONER",1,null,null,0L,
        java.util.Collections.emptyMap(),java.util.Collections.emptyMap(),java.util.Collections.emptySet());
    RpgProgressionState restored=new RpgProgressionState();
    if(restored.restoreSnapshot(legacyLike)!=RpgProgressionState.RestoreResult.RESTORED)return false;
    if(restored.normalExp()!=null)return false;
    if(restored.applyNormalExp(10000,RpgProgressionState.Evidence.B).status!=RpgProgressionState.ExpApplyStatus.UNINITIALIZED)return false;

    return true;
  }
}
