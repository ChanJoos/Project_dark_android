package com.projectdark.mobile;

import java.util.List;

/** Regression boundary for typed action learning. */
public final class RpgActionLearningAudit {
  private RpgActionLearningAudit(){}

  public static boolean verify(){
    RpgProgressionState rpg=new RpgProgressionState();
    if(rpg.applyNormalExp(300000,RpgProgressionState.Evidence.B).afterLevel!=10)return false;
    BasicJobSelectionService.SelectionOutcome selected=BasicJobSelectionService.select(rpg,"ROGUE");
    if(selected.status!=BasicJobSelectionService.SelectionStatus.SELECTED)return false;

    RpgActionLearningService.LearnOutcome wrongJob=RpgActionLearningService.learn(
        rpg,"SK_전사_001",RpgActionLearningService.RequirementProof.PENDING);
    if(wrongJob.status!=RpgActionLearningService.LearnStatus.WRONG_JOB||wrongJob.mutated)return false;

    // 2026 official update removed 찌르기 acquisition requirements: PENDING proof must not block it.
    RpgActionLearningService.LearnOutcome learned=RpgActionLearningService.learn(
        rpg,"SK_도적_001",RpgActionLearningService.RequirementProof.PENDING);
    if(learned.status!=RpgActionLearningService.LearnStatus.LEARNED||!learned.mutated||!learned.requirementRemovedOfficially)return false;
    if(!rpg.learnedActionIds().contains("SK_도적_001"))return false;

    // 센스몬스터 is not in the official removed-requirement list and therefore remains fail-closed.
    RpgActionLearningService.LearnOutcome pending=RpgActionLearningService.learn(
        rpg,"SK_도적_002",RpgActionLearningService.RequirementProof.PENDING);
    if(pending.status!=RpgActionLearningService.LearnStatus.REQUIREMENT_PENDING||pending.mutated)return false;

    boolean inQuickSlot=false;
    List<RpgActionMetadataCatalog.ActionMetadata> quick=RpgActionMetadataCatalog.quickSlotCandidates(rpg);
    for(RpgActionMetadataCatalog.ActionMetadata action:quick)if("SK_도적_001".equals(action.actionId)){inQuickSlot=true;break;}
    if(!inQuickSlot)return false;

    RpgActionLearningService.LearnOutcome duplicate=RpgActionLearningService.learn(
        rpg,"SK_도적_001",RpgActionLearningService.RequirementProof.PENDING);
    if(duplicate.status!=RpgActionLearningService.LearnStatus.ALREADY_LEARNED||duplicate.mutated)return false;

    RpgSaveSnapshot save=rpg.saveSnapshot();RpgProgressionState restored=new RpgProgressionState();
    if(restored.restoreSnapshot(save)!=RpgProgressionState.RestoreResult.RESTORED)return false;
    if(!restored.learnedActionIds().contains("SK_도적_001"))return false;

    RpgActionLearningService.LearnOutcome unknown=RpgActionLearningService.learn(
        restored,"SK_UNKNOWN_999",RpgActionLearningService.RequirementProof.SATISFIED);
    return unknown.status==RpgActionLearningService.LearnStatus.UNKNOWN_ACTION&&!unknown.mutated;
  }
}
