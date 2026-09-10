package com.projectdark.mobile;

import java.util.Arrays;
import java.util.Collections;

/** Isolated process-restart style audit for RPG-owned persistent state. */
public final class RpgPersistenceAudit {
  private RpgPersistenceAudit(){}

  public static boolean verify(){
    RpgProgressionState before=new RpgProgressionState();
    if(before.gold()!=null)return false;
    if(before.autoLootResolvedItem("IT_GLOVE_LEATHER",2)!=RpgProgressionState.AutoLootResult.LOOTED)return false;
    if(!before.setLearnedAction("skill_proto",true))return false;

    CombatLedger.Event defeat7=new CombatLedger.Event(CombatLedger.Type.MONSTER_DEFEATED,"player","combat_dummy_01",0,7L);
    before.consumeCombat(Arrays.asList(defeat7),null);
    if(before.lastCombatSequence()!=7L||before.rewardHistory().size()!=1)return false;
    RpgSaveSnapshot snapshot=before.saveSnapshot();

    RpgProgressionState after=new RpgProgressionState();
    if(after.restoreSnapshot(snapshot)!=RpgProgressionState.RestoreResult.RESTORED)return false;
    if(after.inventory().get("IT_GLOVE_LEATHER")==null||after.inventory().get("IT_GLOVE_LEATHER")!=2)return false;
    if(after.gold()!=null)return false;
    if(!after.learnedActionIds().contains("skill_proto"))return false;
    if(after.lastCombatSequence()!=7L||!after.rewardHistory().isEmpty())return false;

    // Replay of the persisted defeat sequence must be ignored after process restart.
    after.consumeCombat(Arrays.asList(defeat7),null);
    if(!after.rewardHistory().isEmpty()||after.lastCombatSequence()!=7L)return false;
    CombatLedger.Event defeat8=new CombatLedger.Event(CombatLedger.Type.MONSTER_DEFEATED,"player","combat_dummy_01",0,8L);
    after.consumeCombat(Arrays.asList(defeat8),null);
    if(after.rewardHistory().size()!=1||after.lastCombatSequence()!=8L)return false;

    RpgSaveSnapshot invalid=new RpgSaveSnapshot(RpgSaveSnapshot.CURRENT_SCHEMA_VERSION,
        RpgProgressionState.ProgressionNode.COMMONER,"COMMONER",1,null,null,8L,
        Collections.singletonMap("UNKNOWN_ITEM",1),Collections.emptyMap(),Collections.emptySet());
    if(after.restoreSnapshot(invalid)!=RpgProgressionState.RestoreResult.INVALID_STATE)return false;
    return after.inventory().get("IT_GLOVE_LEATHER")==2&&after.lastCombatSequence()==8L;
  }
}
