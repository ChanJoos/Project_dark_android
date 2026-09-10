package com.projectdark.mobile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * RPG-owned Commoner -> basic-job transition contract.
 *
 * Canonical boundaries:
 * - P00 exit requires Lv10 + basic-skill acquisition.
 * - G01 says the Commoner -> basic-job gate is enforced.
 * - Skill_Milestones identifies representative 1-circle skills, but Skill_Requirements does not yet
 *   resolve the exact acquisition/gate semantics for most starter skills.
 *
 * Therefore this service never infers that Lv10 alone is sufficient and never auto-learns a skill.
 * A caller must supply a SATISFIED proof from the owning quest/NPC/progression gate orchestration.
 */
public final class BasicJobSelectionService {
  public static final String SOURCE_PROGRESS="master/data/Progression_Master.csv";
  public static final String SOURCE_GATES="master/data/Content_Gates.csv";
  public static final String SOURCE_MILESTONES="master/data/Skill_Milestones.csv";
  public static final String SOURCE_REQUIREMENTS="master/data/Skill_Requirements.csv";

  public enum BasicSkillProof { PENDING, SATISFIED }
  public enum SelectionStatus {
    SELECTED,
    INVALID_JOB,
    NOT_COMMONER,
    LEVEL_NOT_MET,
    BASIC_SKILL_PROOF_REQUIRED,
    STATE_RESTORE_FAILED
  }

  public static final class JobOption {
    public final String jobCode;
    public final String displayName;
    public final List<String> starterMilestoneActionIds;
    public final RpgProgressionState.Evidence jobEvidence;
    public final RpgProgressionState.Evidence starterMilestoneEvidence;

    JobOption(String jobCode,String displayName,List<String> starterMilestoneActionIds,
        RpgProgressionState.Evidence jobEvidence,RpgProgressionState.Evidence starterMilestoneEvidence){
      this.jobCode=jobCode;this.displayName=displayName;
      this.starterMilestoneActionIds=Collections.unmodifiableList(new ArrayList<>(starterMilestoneActionIds));
      this.jobEvidence=jobEvidence;this.starterMilestoneEvidence=starterMilestoneEvidence;
    }
  }

  public static final class SelectionOutcome {
    public final SelectionStatus status;
    public final String requestedJobCode;
    public final String beforeJobCode;
    public final String afterJobCode;
    public final RpgProgressionState.ProgressionNode beforeNode;
    public final RpgProgressionState.ProgressionNode afterNode;
    public final boolean mutated;
    public final String message;

    SelectionOutcome(SelectionStatus status,String requestedJobCode,String beforeJobCode,String afterJobCode,
        RpgProgressionState.ProgressionNode beforeNode,RpgProgressionState.ProgressionNode afterNode,
        boolean mutated,String message){
      this.status=status;this.requestedJobCode=requestedJobCode;this.beforeJobCode=beforeJobCode;this.afterJobCode=afterJobCode;
      this.beforeNode=beforeNode;this.afterNode=afterNode;this.mutated=mutated;this.message=message;
    }
  }

  private static final Map<String,JobOption> OPTIONS=new LinkedHashMap<>();
  static {
    put(new JobOption("WARRIOR","전사",Arrays.asList("SK_전사_001"),RpgProgressionState.Evidence.O,RpgProgressionState.Evidence.B));
    put(new JobOption("ROGUE","도적",Arrays.asList("SK_도적_001","SK_도적_002"),RpgProgressionState.Evidence.O,RpgProgressionState.Evidence.B));
    put(new JobOption("MAGE","마법사",Arrays.asList("SK_마법사_001"),RpgProgressionState.Evidence.O,RpgProgressionState.Evidence.B));
    put(new JobOption("CLERIC","성직자",Arrays.asList("SK_성직자_001"),RpgProgressionState.Evidence.O,RpgProgressionState.Evidence.B));
    put(new JobOption("MARTIAL_ARTIST","무도가",Arrays.asList("SK_무도가_001"),RpgProgressionState.Evidence.O,RpgProgressionState.Evidence.B));
  }

  private BasicJobSelectionService(){}
  private static void put(JobOption option){OPTIONS.put(option.jobCode,option);}

  public static List<JobOption> options(){return Collections.unmodifiableList(new ArrayList<>(OPTIONS.values()));}
  public static JobOption option(String jobCode){return OPTIONS.get(jobCode);}
  public static boolean isBasicJob(String jobCode){return OPTIONS.containsKey(jobCode);}

  public static SelectionOutcome select(RpgProgressionState rpg,String requestedJobCode,BasicSkillProof proof){
    if(rpg==null)throw new IllegalArgumentException("rpg");
    String beforeJob=rpg.currentJobCode();RpgProgressionState.ProgressionNode beforeNode=rpg.progressionNode();
    if(!isBasicJob(requestedJobCode))return outcome(SelectionStatus.INVALID_JOB,requestedJobCode,rpg,beforeJob,beforeNode,false,"선택할 수 없는 기본직업입니다.");
    if(!"COMMONER".equals(beforeJob)||beforeNode!=RpgProgressionState.ProgressionNode.COMMONER){
      return outcome(SelectionStatus.NOT_COMMONER,requestedJobCode,rpg,beforeJob,beforeNode,false,"이미 평민 직업 선택 단계를 벗어났습니다.");
    }
    Integer level=rpg.normalLevel();
    if(level==null||level<10)return outcome(SelectionStatus.LEVEL_NOT_MET,requestedJobCode,rpg,beforeJob,beforeNode,false,"직업 선택에는 Lv10이 필요합니다.");
    if(proof!=BasicSkillProof.SATISFIED){
      return outcome(SelectionStatus.BASIC_SKILL_PROOF_REQUIRED,requestedJobCode,rpg,beforeJob,beforeNode,false,
          "기본기 습득 게이트 확인이 필요합니다.");
    }

    RpgSaveSnapshot current=rpg.saveSnapshot();
    RpgSaveSnapshot transitioned=new RpgSaveSnapshot(
        current.schemaVersion,RpgProgressionState.ProgressionNode.BASIC_JOB,requestedJobCode,
        current.normalLevel,current.normalExp,current.gold,current.lastCombatSequence,
        current.inventory,current.equipmentBySlot,current.learnedActionIds);
    RpgProgressionState.RestoreResult restored=rpg.restoreSnapshot(transitioned);
    if(restored!=RpgProgressionState.RestoreResult.RESTORED){
      return outcome(SelectionStatus.STATE_RESTORE_FAILED,requestedJobCode,rpg,beforeJob,beforeNode,false,"직업 상태 변경에 실패했습니다.");
    }
    return outcome(SelectionStatus.SELECTED,requestedJobCode,rpg,beforeJob,beforeNode,true,
        option(requestedJobCode).displayName+" 직업을 선택했습니다.");
  }

  private static SelectionOutcome outcome(SelectionStatus status,String requestedJobCode,RpgProgressionState rpg,
      String beforeJob,RpgProgressionState.ProgressionNode beforeNode,boolean mutated,String message){
    return new SelectionOutcome(status,requestedJobCode,beforeJob,rpg.currentJobCode(),beforeNode,rpg.progressionNode(),mutated,message);
  }

  public static boolean audit(){
    if(OPTIONS.size()!=5)return false;
    Set<String> ids=new LinkedHashSet<>();
    for(JobOption option:OPTIONS.values()){
      if(option.jobCode==null||option.displayName==null||option.starterMilestoneActionIds.isEmpty()||!ids.add(option.jobCode))return false;
    }
    return true;
  }
}
