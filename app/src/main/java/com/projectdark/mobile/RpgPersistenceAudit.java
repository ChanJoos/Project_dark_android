package com.projectdark.mobile;

import java.util.Collections;

/** Isolated process-restart style audit for RPG-owned persistent state. */
public final class RpgPersistenceAudit {
  private RpgPersistenceAudit(){}

  public static boolean verify(){
    RpgProgressionState before=new RpgProgressionState();
    if(before.autoLootResolvedItem("IT_GLOVE_LEATHER",2)!=RpgProgressionState.AutoLootResult.LOOTED)return false;
    if(!before.setLearnedAction("skill_proto",true))return false;
    RpgSaveSnapshot snapshot=before.saveSnapshot();

    RpgProgressionState after=new RpgProgressionState();
    if(after.restoreSnapshot(snapshot)!=RpgProgressionState.RestoreResult.RESTORED)return false;
    if(after.inventory().get("IT_GLOVE_LEATHER")==null||after.inventory().get("IT_GLOVE_LEATHER")!=2)return false;
    if(!after.learnedActionIds().contains("skill_proto"))return false;
    if(after.lastCombatSequence()!=before.lastCombatSequence())return false;

    RpgSaveSnapshot invalid=new RpgSaveSnapshot(RpgSaveSnapshot.CURRENT_SCHEMA_VERSION,
        RpgProgressionState.ProgressionNode.COMMONER,"COMMONER",1,null,0,0,
        Collections.singletonMap("UNKNOWN_ITEM",1),Collections.emptyMap(),Collections.emptySet());
    if(after.restoreSnapshot(invalid)!=RpgProgressionState.RestoreResult.INVALID_STATE)return false;
    return after.inventory().get("IT_GLOVE_LEATHER")==2;
  }
}
