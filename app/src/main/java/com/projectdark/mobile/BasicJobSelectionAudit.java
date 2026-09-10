package com.projectdark.mobile;

import java.util.Collections;

/** Regression audit for the evidence-safe Commoner -> basic-job transition. */
public final class BasicJobSelectionAudit {
  private BasicJobSelectionAudit(){}

  public static boolean run(){
    if(!BasicJobSelectionService.audit())return false;

    RpgProgressionState lv9=new RpgProgressionState();
    RpgSaveSnapshot lv9Snapshot=new RpgSaveSnapshot(
        RpgSaveSnapshot.CURRENT_SCHEMA_VERSION,RpgProgressionState.ProgressionNode.COMMONER,"COMMONER",9,225600L,null,0L,
        Collections.emptyMap(),Collections.emptyMap(),Collections.emptySet());
    if(lv9.restoreSnapshot(lv9Snapshot)!=RpgProgressionState.RestoreResult.RESTORED)return false;
    BasicJobSelectionService.SelectionOutcome blockedByLevel=BasicJobSelectionService.select(
        lv9,"WARRIOR",BasicJobSelectionService.BasicSkillProof.SATISFIED);
    if(blockedByLevel.status!=BasicJobSelectionService.SelectionStatus.LEVEL_NOT_MET||blockedByLevel.mutated)return false;

    RpgProgressionState lv10=new RpgProgressionState();
    RpgSaveSnapshot lv10Snapshot=new RpgSaveSnapshot(
        RpgSaveSnapshot.CURRENT_SCHEMA_VERSION,RpgProgressionState.ProgressionNode.COMMONER,"COMMONER",10,300000L,null,0L,
        Collections.emptyMap(),Collections.emptyMap(),Collections.emptySet());
    if(lv10.restoreSnapshot(lv10Snapshot)!=RpgProgressionState.RestoreResult.RESTORED)return false;
    BasicJobSelectionService.SelectionOutcome pending=BasicJobSelectionService.select(
        lv10,"WARRIOR",BasicJobSelectionService.BasicSkillProof.PENDING);
    if(pending.status!=BasicJobSelectionService.SelectionStatus.BASIC_SKILL_PROOF_REQUIRED||pending.mutated)return false;
    if(!"COMMONER".equals(lv10.currentJobCode())||lv10.progressionNode()!=RpgProgressionState.ProgressionNode.COMMONER)return false;

    BasicJobSelectionService.SelectionOutcome selected=BasicJobSelectionService.select(
        lv10,"WARRIOR",BasicJobSelectionService.BasicSkillProof.SATISFIED);
    if(selected.status!=BasicJobSelectionService.SelectionStatus.SELECTED||!selected.mutated)return false;
    if(!"WARRIOR".equals(lv10.currentJobCode())||lv10.progressionNode()!=RpgProgressionState.ProgressionNode.BASIC_JOB)return false;
    if(!lv10.learnedActionIds().isEmpty())return false; // no fabricated auto-learning

    RpgSaveSnapshot saved=lv10.saveSnapshot();
    RpgProgressionState restored=new RpgProgressionState();
    if(restored.restoreSnapshot(saved)!=RpgProgressionState.RestoreResult.RESTORED)return false;
    if(!"WARRIOR".equals(restored.currentJobCode())||restored.progressionNode()!=RpgProgressionState.ProgressionNode.BASIC_JOB)return false;
    if(restored.normalLevel()!=10||restored.normalExp()==null||restored.normalExp()!=300000L)return false;

    BasicJobSelectionService.SelectionOutcome secondSelection=BasicJobSelectionService.select(
        restored,"MAGE",BasicJobSelectionService.BasicSkillProof.SATISFIED);
    if(secondSelection.status!=BasicJobSelectionService.SelectionStatus.NOT_COMMONER||secondSelection.mutated)return false;

    return true;
  }
}
