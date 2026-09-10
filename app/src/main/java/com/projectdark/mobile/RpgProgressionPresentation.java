package com.projectdark.mobile;

/**
 * RPG-owned progression projection for HUD bars and progression gates.
 * Level curve values come from master/data/Level_EXP_Curve.csv and retain Evidence.B.
 */
public final class RpgProgressionPresentation {
  public enum JobSelectionGateStatus {
    LEVEL_NOT_MET,
    SKILL_REQUIREMENT_PENDING,
    READY,
    NOT_COMMONER
  }

  public static final class LevelProgress {
    public final Integer level;
    public final Long cumulativeExp;
    public final Long levelStartExp;
    public final Long nextLevelCumulativeExp;
    public final Long expIntoLevel;
    public final Long expRequiredThisLevel;
    public final Long expToNextLevel;
    public final float ratio;
    public final boolean levelCap;
    public final String segment;
    public final String primaryHunting;
    public final RpgProgressionState.Evidence evidence;

    LevelProgress(Integer level,Long cumulativeExp,Long levelStartExp,Long nextLevelCumulativeExp,
        Long expIntoLevel,Long expRequiredThisLevel,Long expToNextLevel,float ratio,boolean levelCap,
        String segment,String primaryHunting,RpgProgressionState.Evidence evidence){
      this.level=level;this.cumulativeExp=cumulativeExp;this.levelStartExp=levelStartExp;
      this.nextLevelCumulativeExp=nextLevelCumulativeExp;this.expIntoLevel=expIntoLevel;
      this.expRequiredThisLevel=expRequiredThisLevel;this.expToNextLevel=expToNextLevel;this.ratio=ratio;
      this.levelCap=levelCap;this.segment=segment;this.primaryHunting=primaryHunting;this.evidence=evidence;
    }
  }

  public static final class JobSelectionGate {
    public final JobSelectionGateStatus status;
    public final boolean levelRequirementMet;
    public final boolean skillRequirementResolved;
    public final boolean ready;
    public final String message;
    public final RpgProgressionState.Evidence evidence;

    JobSelectionGate(JobSelectionGateStatus status,boolean levelRequirementMet,boolean skillRequirementResolved,
        boolean ready,String message,RpgProgressionState.Evidence evidence){
      this.status=status;this.levelRequirementMet=levelRequirementMet;this.skillRequirementResolved=skillRequirementResolved;
      this.ready=ready;this.message=message;this.evidence=evidence;
    }
  }

  public LevelProgress levelProgress(RpgProgressionState rpg){
    if(rpg==null)throw new IllegalArgumentException("rpg");
    Integer level=rpg.normalLevel();Long exp=rpg.normalExp();
    if(level==null||exp==null)return new LevelProgress(level,exp,null,null,null,null,null,0f,false,null,null,RpgProgressionState.Evidence.PENDING);
    if(level>=LevelExpCurveCatalog.MAX_PROJECTED_LEVEL){
      return new LevelProgress(level,exp,LevelExpCurveCatalog.LEVEL_99_CUMULATIVE_EXP,null,0L,null,null,1f,true,"S5","뤼케시온해안 후반/99 준비",RpgProgressionState.Evidence.B);
    }
    LevelExpCurveCatalog.Entry entry=LevelExpCurveCatalog.entryForCurrentLevel(level);
    if(entry==null)return new LevelProgress(level,exp,null,null,null,null,null,0f,false,null,null,RpgProgressionState.Evidence.PENDING);
    long start=LevelExpCurveCatalog.cumulativeRequiredForLevel(level);
    long into=Math.max(0L,exp-start);
    long required=entry.requiredExp;
    long remain=Math.max(0L,entry.cumulativeExp-exp);
    float ratio=required<=0L?0f:Math.max(0f,Math.min(1f,(float)into/(float)required));
    return new LevelProgress(level,exp,start,entry.cumulativeExp,into,required,remain,ratio,false,
        entry.segment,entry.primaryHunting,entry.evidence);
  }

  /**
   * G01/P00 require early job-selection conditions and Lv10 + basic-skill acquisition.
   * The canonical data does not yet identify the exact required learned action IDs, so Lv10 alone cannot open the gate.
   */
  public JobSelectionGate basicJobSelectionGate(RpgProgressionState rpg){
    if(rpg==null)throw new IllegalArgumentException("rpg");
    if(!"COMMONER".equals(rpg.currentJobCode())){
      return new JobSelectionGate(JobSelectionGateStatus.NOT_COMMONER,true,false,false,"이미 기본직업 경로에 진입했습니다.",RpgProgressionState.Evidence.O);
    }
    Integer level=rpg.normalLevel();
    if(level==null||level<10){
      return new JobSelectionGate(JobSelectionGateStatus.LEVEL_NOT_MET,false,false,false,"직업 선택 준비: Lv10 필요",RpgProgressionState.Evidence.B);
    }
    return new JobSelectionGate(JobSelectionGateStatus.SKILL_REQUIREMENT_PENDING,true,false,false,
        "Lv10 달성 · 기본기 습득 조건 데이터 확인 필요",RpgProgressionState.Evidence.PENDING);
  }
}
