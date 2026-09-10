package com.projectdark.mobile;

import java.util.Collections;

/** Regression audit for Commoner -> basic-job transition before starter-skill acquisition. */
public final class BasicJobSelectionAudit {
  private BasicJobSelectionAudit(){}

  public static boolean run(){
    if(!BasicJobSelectionService.audit())return false;
    RpgProgressionState lv9=new RpgProgressionState();
    RpgSaveSnapshot lv9Snapshot=new RpgSaveSnapshot(RpgSaveSnapshot.CURRENT_SCHEMA_VERSION,
        RpgProgressionState.ProgressionNode.COMMONER,"COMMONER",9,225600L,null,0L,
        Collections.emptyMap(),Collections.emptyMap(),Collections.emptySet());
    if(lv9.restoreSnapshot(lv9Snapshot)!=RpgProgressionState.RestoreResult.RESTORED)return false;
    if(BasicJobSelectionService.select(lv9,"WARRIOR").status!=BasicJobSelectionService.SelectionStatus.LEVEL_NOT_MET)return false;

    RpgProgressionState lv10=new RpgProgressionState();
    RpgSaveSnapshot lv10Snapshot=new RpgSaveSnapshot(RpgSaveSnapshot.CURRENT_SCHEMA_VERSION,
        RpgProgressionState.ProgressionNode.COMMONER,"COMMONER",10,300000L,null,0L,
        Collections.emptyMap(),Collections.emptyMap(),Collections.emptySet());
    if(lv10.restoreSnapshot(lv10Snapshot)!=RpgProgressionState.RestoreResult.RESTORED)return false;
    BasicJobSelectionService.SelectionOutcome selected=BasicJobSelectionService.select(lv10,"WARRIOR");
    if(selected.status!=BasicJobSelectionService.SelectionStatus.SELECTED||!selected.mutated)return false;
    if(!"WARRIOR".equals(lv10.currentJobCode())||lv10.progressionNode()!=RpgProgressionState.ProgressionNode.BASIC_JOB)return false;
    if(!lv10.learnedActionIds().isEmpty())return false;

    // Official 2026 policy: starter requirement removed, so matching job starter can now learn without external proof.
    RpgActionLearningService.LearnOutcome starter=RpgActionLearningService.learn(
        lv10,"SK_전사_001",RpgActionLearningService.RequirementProof.PENDING);
    if(starter.status!=RpgActionLearningService.LearnStatus.LEARNED||!starter.requirementRemovedOfficially)return false;

    RpgSaveSnapshot saved=lv10.saveSnapshot();RpgProgressionState restored=new RpgProgressionState();
    if(restored.restoreSnapshot(saved)!=RpgProgressionState.RestoreResult.RESTORED)return false;
    if(!"WARRIOR".equals(restored.currentJobCode())||!restored.learnedActionIds().contains("SK_전사_001"))return false;
    if(BasicJobSelectionService.select(restored,"MAGE").status!=BasicJobSelectionService.SelectionStatus.NOT_COMMONER)return false;
    return true;
  }
}
