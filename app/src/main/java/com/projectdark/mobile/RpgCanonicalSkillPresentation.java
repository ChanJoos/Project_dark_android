package com.projectdark.mobile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Read-only presentation adapter over the complete CSV-backed Skill_Master repository.
 * Unlike RpgActionMetadataCatalog, this is not a partial executable-action registry: it exposes every
 * canonical runtime-included skill/magic row for skill-book/detail UI while preserving unresolved data.
 */
public final class RpgCanonicalSkillPresentation {
  public enum IconStatus { OFFICIAL_SOURCE_SHEET_FOUND, INDIVIDUAL_MAPPING_PENDING }

  public static final class SkillRow {
    public final String actionId,name,job,stage,type,circle,resource,resourceAmount,requiredLevel,requiredStats,requirementText;
    public final String actionClass,target,range,element,effect,activationLimit,mastery,prerequisite;
    public final String evidence,effectEvidence,sourceUrl,detailSourceUrl,runtimeDataStatus,runtimeInclusion;
    public final String acquisitionOverride,acquisitionOverrideSource,acquisitionOverrideEvidence;
    public final boolean learned,currentJob,acquisitionRequirementRemoved;
    public final IconStatus iconStatus;
    public final String iconKey;

    SkillRow(RpgCanonicalSkillRepository.SkillRecord r,RpgProgressionState state,boolean officialVisualSource){
      actionId=r.skillId;name=r.name;job=r.job;stage=r.stage;type=r.type;circle=r.circle;resource=r.resource;
      resourceAmount=r.resourceAmount;requiredLevel=r.requiredLevel;requiredStats=r.requiredStats;requirementText=r.requirementText;
      actionClass=r.actionClass;target=r.target;range=r.range;element=r.element;effect=r.effect;activationLimit=r.activationLimit;
      mastery=r.mastery;prerequisite=r.prerequisite;evidence=r.evidence;effectEvidence=r.effectEvidence;sourceUrl=r.sourceUrl;
      detailSourceUrl=r.detailSourceUrl;runtimeDataStatus=r.runtimeDataStatus;runtimeInclusion=r.runtimeInclusion;
      acquisitionOverride=r.acquisitionOverride;acquisitionOverrideSource=r.acquisitionOverrideSource;
      acquisitionOverrideEvidence=r.acquisitionOverrideEvidence;acquisitionRequirementRemoved=r.acquisitionRequirementRemoved();
      learned=state!=null&&state.learnedActionIds().contains(r.skillId);
      currentJob=state!=null&&runtimeJobCode(r.job).equals(state.currentJobCode());
      iconStatus=officialVisualSource?IconStatus.OFFICIAL_SOURCE_SHEET_FOUND:IconStatus.INDIVIDUAL_MAPPING_PENDING;
      // No false per-skill crop mapping: preserve a stable unresolved key until a source-backed crop manifest exists.
      iconKey="OFFICIAL_SHEET_PENDING_CROP:"+r.skillId;
    }
  }

  public static final class Snapshot {
    public final List<SkillRow> allRuntimeSkills,currentJobSkills,currentJobCircleOne;
    public final List<RpgCanonicalSkillRepository.VisualSource> officialIconSources;
    public final int totalCanonicalRuntimeSkills;
    public final boolean officialIconSourceAvailable;
    public final boolean individualIconMappingComplete;
    Snapshot(List<SkillRow> all,List<SkillRow> current,List<SkillRow> circleOne,
        List<RpgCanonicalSkillRepository.VisualSource> sources,boolean sourceAvailable){
      allRuntimeSkills=Collections.unmodifiableList(all);currentJobSkills=Collections.unmodifiableList(current);
      currentJobCircleOne=Collections.unmodifiableList(circleOne);officialIconSources=sources;
      totalCanonicalRuntimeSkills=all.size();officialIconSourceAvailable=sourceAvailable;
      individualIconMappingComplete=false;
    }
  }

  private RpgCanonicalSkillPresentation(){}

  public static Snapshot snapshot(RpgProgressionState state,RpgCanonicalSkillRepository repo){
    if(state==null||repo==null)throw new IllegalArgumentException("state/repo");
    boolean visual=repo.hasOfficialVisualSource();List<SkillRow> all=new ArrayList<>(),current=new ArrayList<>(),circleOne=new ArrayList<>();
    for(RpgCanonicalSkillRepository.SkillRecord r:repo.byId().values()){
      if(!r.runtimeIncluded())continue;SkillRow row=new SkillRow(r,state,visual);all.add(row);
      if(row.currentJob){current.add(row);if("1".equals(row.circle))circleOne.add(row);}
    }
    return new Snapshot(all,current,circleOne,repo.visualSources(),visual);
  }

  /** Canonical Korean job label -> existing runtime job code. Unknown labels remain explicit and non-matching. */
  static String runtimeJobCode(String koreanJob){
    if("전사".equals(koreanJob))return "WARRIOR";
    if("도적".equals(koreanJob))return "ROGUE";
    if("마법사".equals(koreanJob))return "MAGE";
    if("성직자".equals(koreanJob))return "CLERIC";
    if("무도가".equals(koreanJob))return "MARTIAL_ARTIST";
    if("평민".equals(koreanJob))return "COMMONER";
    return "UNMAPPED:"+(koreanJob==null?"":koreanJob);
  }
}
