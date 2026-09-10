package com.projectdark.mobile;

/**
 * RPG-owned action acquisition boundary.
 *
 * Skill_Requirements.csv still leaves most acquisition requirements unresolved, so this service
 * never invents level/stat/item/gold gates. A caller must provide SATISFIED proof only after the
 * owning quest/NPC/progression flow has resolved those requirements.
 */
public final class RpgActionLearningService {
  public enum RequirementProof { PENDING, SATISFIED }
  public enum LearnStatus {
    LEARNED,
    ALREADY_LEARNED,
    UNKNOWN_ACTION,
    RUNTIME_PROTOTYPE,
    WRONG_JOB,
    REQUIREMENT_PENDING,
    MUTATION_FAILED
  }

  public static final class LearnOutcome {
    public final LearnStatus status;
    public final String actionId;
    public final String actionName;
    public final String actionJobCode;
    public final String playerJobCode;
    public final boolean mutated;
    public final String message;
    public final RpgProgressionState.Evidence evidence;
    public final String sourceEvidence;

    LearnOutcome(LearnStatus status,String actionId,String actionName,String actionJobCode,String playerJobCode,
        boolean mutated,String message,RpgProgressionState.Evidence evidence,String sourceEvidence){
      this.status=status;this.actionId=actionId;this.actionName=actionName;this.actionJobCode=actionJobCode;
      this.playerJobCode=playerJobCode;this.mutated=mutated;this.message=message;this.evidence=evidence;
      this.sourceEvidence=sourceEvidence;
    }
  }

  private RpgActionLearningService(){}

  public static LearnOutcome learn(RpgProgressionState rpg,String actionId,RequirementProof proof){
    if(rpg==null)throw new IllegalArgumentException("rpg");
    RpgActionMetadataCatalog.ActionMetadata action=RpgActionMetadataCatalog.byId(rpg).get(actionId);
    if(action==null)return outcome(LearnStatus.UNKNOWN_ACTION,actionId,null,rpg,false,"알 수 없는 기술/마법입니다.");
    if(action.runtimeBound)return outcome(LearnStatus.RUNTIME_PROTOTYPE,actionId,action,rpg,false,"런타임 프로토타입 액션은 습득 대상이 아닙니다.");
    if(action.learned())return outcome(LearnStatus.ALREADY_LEARNED,actionId,action,rpg,false,"이미 습득한 기술/마법입니다.");
    if(action.jobCode==null||!action.jobCode.equals(rpg.currentJobCode())){
      return outcome(LearnStatus.WRONG_JOB,actionId,action,rpg,false,"현재 직업에서 습득할 수 없는 기술/마법입니다.");
    }
    if(proof!=RequirementProof.SATISFIED){
      return outcome(LearnStatus.REQUIREMENT_PENDING,actionId,action,rpg,false,"습득 조건 확인이 필요합니다.");
    }
    boolean changed=rpg.setLearnedAction(actionId,true);
    return changed
        ?outcome(LearnStatus.LEARNED,actionId,action,rpg,true,action.name+" 습득 완료")
        :outcome(LearnStatus.MUTATION_FAILED,actionId,action,rpg,false,"기술/마법 습득 상태 변경에 실패했습니다.");
  }

  private static LearnOutcome outcome(LearnStatus status,String actionId,RpgActionMetadataCatalog.ActionMetadata action,
      RpgProgressionState rpg,boolean mutated,String message){
    return new LearnOutcome(status,actionId,action==null?null:action.name,action==null?null:action.jobCode,
        rpg.currentJobCode(),mutated,message,action==null?RpgProgressionState.Evidence.PENDING:action.evidence,
        action==null?null:action.sourceEvidence);
  }
}