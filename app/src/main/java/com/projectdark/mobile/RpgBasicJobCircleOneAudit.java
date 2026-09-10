package com.projectdark.mobile;

import java.util.List;

/** Regression boundary for five-basic-job circle-1 metadata and post-selection visibility. */
public final class RpgBasicJobCircleOneAudit {
  private RpgBasicJobCircleOneAudit(){}

  public static boolean verify(){
    RpgProgressionState rpg=new RpgProgressionState();
    if(!counts(rpg,"WARRIOR",2))return false;
    if(!counts(rpg,"ROGUE",2))return false;
    if(!counts(rpg,"MAGE",1))return false;
    if(!counts(rpg,"CLERIC",3))return false;
    if(!counts(rpg,"MARTIAL_ARTIST",1))return false;

    RpgActionMetadataCatalog.ActionMetadata cleric=RpgActionMetadataCatalog.byId(rpg).get("SK_성직자_001");
    if(cleric==null||!"O/V".equals(cleric.sourceEvidence)||cleric.resourceCost!=null||cleric.cooldown!=null)return false;
    RpgActionMetadataCatalog.ActionMetadata martial=RpgActionMetadataCatalog.byId(rpg).get("SK_무도가_001");
    if(martial==null||!"HP/MP/조건부 - 개별 확인".equals(martial.resourceLabel))return false;

    RpgProgressionState.ExpApplyOutcome exp=rpg.applyNormalExp(300000,RpgProgressionState.Evidence.B);
    if(exp.status!=RpgProgressionState.ExpApplyStatus.APPLIED||rpg.normalLevel()!=10)return false;
    BasicJobSelectionService.SelectionOutcome selected=BasicJobSelectionService.select(
        rpg,"ROGUE",BasicJobSelectionService.BasicSkillProof.SATISFIED);
    if(selected.status!=BasicJobSelectionService.SelectionStatus.SELECTED)return false;

    List<RpgActionMetadataCatalog.ActionMetadata> rogueBook=RpgActionMetadataCatalog.forJobAndCircle(rpg,"ROGUE",1);
    for(RpgActionMetadataCatalog.ActionMetadata action:rogueBook){
      if(action.visibilityState!=RpgActionMetadataCatalog.VisibilityState.CURRENT_JOB_LOCKED||action.learned())return false;
    }
    if(RpgActionMetadataCatalog.quickSlotCandidates(rpg).size()!=3)return false; // prototype bindings only

    if(!rpg.setLearnedAction("SK_도적_001",true))return false;
    RpgActionMetadataCatalog.ActionMetadata learned=RpgActionMetadataCatalog.byId(rpg).get("SK_도적_001");
    if(learned==null||!learned.learned()||learned.visibilityState!=RpgActionMetadataCatalog.VisibilityState.LEARNED)return false;
    if(RpgActionMetadataCatalog.quickSlotCandidates(rpg).size()!=4)return false;
    return true;
  }

  private static boolean counts(RpgProgressionState rpg,String job,int expected){
    return RpgActionMetadataCatalog.forJobAndCircle(rpg,job,1).size()==expected;
  }
}
