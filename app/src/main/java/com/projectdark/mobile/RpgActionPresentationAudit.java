package com.projectdark.mobile;

import java.util.List;

/** Regression audit for canonical action projection and quick-slot eligibility. */
public final class RpgActionPresentationAudit {
  private RpgActionPresentationAudit(){}

  public static boolean verify(){
    RpgProgressionState rpg=new RpgProgressionState();
    List<RpgActionMetadataCatalog.ActionMetadata> warrior=RpgActionMetadataCatalog.forJob(rpg,"WARRIOR");
    if(warrior.size()!=15)return false;

    RpgActionMetadataCatalog.ActionMetadata shortBlade=find(warrior,"SK_전사_001");
    RpgActionMetadataCatalog.ActionMetadata doubleAttack=find(warrior,"SK_전사_002");
    RpgActionMetadataCatalog.ActionMetadata crusher=find(warrior,"SK_전사_015");
    if(shortBlade==null||doubleAttack==null||crusher==null)return false;
    if(shortBlade.learned()||shortBlade.visibilityState!=RpgActionMetadataCatalog.VisibilityState.OTHER_JOB)return false;
    if(shortBlade.resourceCost!=null||shortBlade.cooldown!=null)return false;
    if(!"기본 MP 소모 없음".equals(shortBlade.resourceLabel))return false;
    if(!doubleAttack.passive||doubleAttack.quickSlotEligible)return false;
    if(crusher.circle==null||crusher.circle!=5)return false;

    // Locked/unlearned canonical actions must never appear as active quick-slot candidates.
    for(RpgActionMetadataCatalog.ActionMetadata action:RpgActionMetadataCatalog.quickSlotCandidates(rpg)){
      if(!action.runtimeBound&& !action.learned())return false;
    }

    if(!rpg.setLearnedAction("SK_전사_001",true))return false;
    boolean learnedQuickSlot=false;
    for(RpgActionMetadataCatalog.ActionMetadata action:RpgActionMetadataCatalog.quickSlotCandidates(rpg)){
      if("SK_전사_001".equals(action.actionId)){learnedQuickSlot=true;if(!action.learned())return false;}
    }
    if(!learnedQuickSlot)return false;

    // Unknown action IDs remain rejected and cannot pollute persistent learned state.
    if(rpg.setLearnedAction("SK_UNKNOWN_999",true))return false;
    return !rpg.learnedActionIds().contains("SK_UNKNOWN_999");
  }

  private static RpgActionMetadataCatalog.ActionMetadata find(List<RpgActionMetadataCatalog.ActionMetadata> list,String id){
    for(RpgActionMetadataCatalog.ActionMetadata action:list)if(id.equals(action.actionId))return action;
    return null;
  }
}
