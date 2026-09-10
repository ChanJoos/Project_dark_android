package com.projectdark.mobile;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * RPG-owned action acquisition boundary.
 *
 * Canonical acquisition policy:
 * - The 2026 official update removed acquisition requirements for the starter actions listed in
 *   OFFICIAL_NO_REQUIREMENT_ACTIONS. Those actions may be learned without an external proof once
 *   the player is in the matching job.
 * - Other actions remain fail-closed while exact level/stat/item/gold rules are unresolved in
 *   Skill_Requirements.csv; callers must provide SATISFIED proof only after owning orchestration
 *   has resolved those requirements.
 */
public final class RpgActionLearningService {
  public static final String OFFICIAL_STARTER_RULE_SOURCE="https://lod.nexon.com/news/update/147557";
  private static final Set<String> OFFICIAL_NO_REQUIREMENT_ACTIONS=Collections.unmodifiableSet(new LinkedHashSet<>(Arrays.asList(
      "SK_전사_001","SK_전사_012","SK_도적_001","SK_마법사_001","SK_성직자_001","SK_성직자_003","SK_무도가_001"
  )));

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
    public final boolean requirementRemovedOfficially;
    public final String message;
    public final RpgProgressionState.Evidence evidence;
    public final String sourceEvidence;

    LearnOutcome(LearnStatus status,String actionId,String actionName,String actionJobCode,String playerJobCode,
        boolean mutated,boolean requirementRemovedOfficially,String message,RpgProgressionState.Evidence evidence,String sourceEvidence){
      this.status=status;this.actionId=actionId;this.actionName=actionName;this.actionJobCode=actionJobCode;
      this.playerJobCode=playerJobCode;this.mutated=mutated;this.requirementRemovedOfficially=requirementRemovedOfficially;
      this.message=message;this.evidence=evidence;this.sourceEvidence=sourceEvidence;
    }
  }

  private RpgActionLearningService(){}

  public static boolean requirementRemovedOfficially(String actionId){return OFFICIAL_NO_REQUIREMENT_ACTIONS.contains(actionId);}
  public static Set<String> officialNoRequirementActions(){return OFFICIAL_NO_REQUIREMENT_ACTIONS;}

  public static LearnOutcome learn(RpgProgressionState rpg,String actionId,RequirementProof proof){
    if(rpg==null)throw new IllegalArgumentException("rpg");
    RpgActionMetadataCatalog.ActionMetadata action=RpgActionMetadataCatalog.byId(rpg).get(actionId);
    if(action==null)return outcome(LearnStatus.UNKNOWN_ACTION,actionId,null,rpg,false,false,"알 수 없는 기술/마법입니다.");
    if(action.runtimeBound)return outcome(LearnStatus.RUNTIME_PROTOTYPE,actionId,action,rpg,false,false,"런타임 프로토타입 액션은 습득 대상이 아닙니다.");
    if(action.learned())return outcome(LearnStatus.ALREADY_LEARNED,actionId,action,rpg,false,requirementRemovedOfficially(actionId),"이미 습득한 기술/마법입니다.");
    if(action.jobCode==null||!action.jobCode.equals(rpg.currentJobCode())){
      return outcome(LearnStatus.WRONG_JOB,actionId,action,rpg,false,requirementRemovedOfficially(actionId),"현재 직업에서 습득할 수 없는 기술/마법입니다.");
    }
    boolean removed=requirementRemovedOfficially(actionId);
    if(!removed&&proof!=RequirementProof.SATISFIED){
      return outcome(LearnStatus.REQUIREMENT_PENDING,actionId,action,rpg,false,false,"습득 조건 확인이 필요합니다.");
    }
    boolean changed=rpg.setLearnedAction(actionId,true);
    return changed
        ?outcome(LearnStatus.LEARNED,actionId,action,rpg,true,removed,action.name+" 습득 완료")
        :outcome(LearnStatus.MUTATION_FAILED,actionId,action,rpg,false,removed,"기술/마법 습득 상태 변경에 실패했습니다.");
  }

  private static LearnOutcome outcome(LearnStatus status,String actionId,RpgActionMetadataCatalog.ActionMetadata action,
      RpgProgressionState rpg,boolean mutated,boolean removed,String message){
    return new LearnOutcome(status,actionId,action==null?null:action.name,action==null?null:action.jobCode,
        rpg.currentJobCode(),mutated,removed,message,action==null?RpgProgressionState.Evidence.PENDING:action.evidence,
        action==null?null:action.sourceEvidence);
  }
}
