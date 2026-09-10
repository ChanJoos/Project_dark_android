package com.projectdark.mobile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** RPG-owned stable metadata projection for HUD/inventory consumers. No action execution lives here. */
public final class RpgActionMetadataCatalog {
  public enum LearnedState { LEARNED, UNLEARNED }
  public enum VisibilityState { RUNTIME_PROTOTYPE, LEARNED, CURRENT_JOB_LOCKED, OTHER_JOB }

  /**
   * Stable presentation DTO. Nullable cost/cooldown deliberately preserve unresolved Master values;
   * consumers must render them as pending/unknown rather than inventing zero.
   */
  public static final class ActionMetadata {
    public final String actionId,name,iconKey,jobCode,actionClass,resourceLabel,targetLabel,rangeLabel,effectSummary;
    public final String sourceEvidence;
    public final Integer circle;
    public final Integer resourceCost;
    public final Float cooldown;
    public final boolean runtimeBound,quickSlotEligible,passive;
    public final LearnedState state;
    public final VisibilityState visibilityState;
    public final RpgProgressionState.Evidence evidence;

    ActionMetadata(String actionId,String name,String iconKey,String jobCode,Integer circle,String actionClass,
        String resourceLabel,Integer resourceCost,Float cooldown,String targetLabel,String rangeLabel,String effectSummary,
        boolean runtimeBound,boolean quickSlotEligible,boolean passive,LearnedState state,VisibilityState visibilityState,
        RpgProgressionState.Evidence evidence,String sourceEvidence){
      this.actionId=actionId;this.name=name;this.iconKey=iconKey;this.jobCode=jobCode;this.circle=circle;
      this.actionClass=actionClass;this.resourceLabel=resourceLabel;this.resourceCost=resourceCost;this.cooldown=cooldown;
      this.targetLabel=targetLabel;this.rangeLabel=rangeLabel;this.effectSummary=effectSummary;
      this.runtimeBound=runtimeBound;this.quickSlotEligible=quickSlotEligible;this.passive=passive;
      this.state=state;this.visibilityState=visibilityState;this.evidence=evidence;this.sourceEvidence=sourceEvidence;
    }
    public boolean learned(){return state==LearnedState.LEARNED;}
    public boolean resourceCostResolved(){return resourceCost!=null;}
    public boolean cooldownResolved(){return cooldown!=null;}
  }

  private static final class Definition {
    final String id,name,iconKey,jobCode,actionClass,resourceLabel,targetLabel,rangeLabel,effectSummary,sourceEvidence;
    final Integer circle,cost;
    final Float cooldown;
    final boolean runtimeBound,quickSlotEligible,passive;
    final RpgProgressionState.Evidence evidence;
    Definition(String id,String name,String iconKey,String jobCode,Integer circle,String actionClass,String resourceLabel,
        Integer cost,Float cooldown,String targetLabel,String rangeLabel,String effectSummary,boolean runtimeBound,
        boolean quickSlotEligible,boolean passive,RpgProgressionState.Evidence evidence){
      this(id,name,iconKey,jobCode,circle,actionClass,resourceLabel,cost,cooldown,targetLabel,rangeLabel,effectSummary,
          runtimeBound,quickSlotEligible,passive,evidence,evidence==null?null:evidence.name());
    }
    Definition(String id,String name,String iconKey,String jobCode,Integer circle,String actionClass,String resourceLabel,
        Integer cost,Float cooldown,String targetLabel,String rangeLabel,String effectSummary,boolean runtimeBound,
        boolean quickSlotEligible,boolean passive,RpgProgressionState.Evidence evidence,String sourceEvidence){
      this.id=id;this.name=name;this.iconKey=iconKey;this.jobCode=jobCode;this.circle=circle;this.actionClass=actionClass;
      this.resourceLabel=resourceLabel;this.cost=cost;this.cooldown=cooldown;this.targetLabel=targetLabel;
      this.rangeLabel=rangeLabel;this.effectSummary=effectSummary;this.runtimeBound=runtimeBound;
      this.quickSlotEligible=quickSlotEligible;this.passive=passive;this.evidence=evidence;this.sourceEvidence=sourceEvidence;
    }
  }

  private static final Map<String,Definition> DEFINITIONS=buildDefinitions();
  private RpgActionMetadataCatalog(){}

  private static Map<String,Definition> buildDefinitions(){
    Map<String,Definition> out=new LinkedHashMap<>();

    // Executing prototype actions remain explicit [B] bindings until Combat-owned wiring migrates.
    add(out,new Definition(SkillDef.CAST_PROTO.id,SkillDef.CAST_PROTO.label,"PENDING_CROP:"+SkillDef.CAST_PROTO.id,
        "PROTOTYPE",null,"Cast","MP",SkillDef.CAST_PROTO.mpCost,SkillDef.CAST_PROTO.cooldown,"적",null,null,
        true,true,false,RpgProgressionState.Evidence.B));
    add(out,new Definition(SkillDef.SKILL_PROTO.id,SkillDef.SKILL_PROTO.label,"PENDING_CROP:"+SkillDef.SKILL_PROTO.id,
        "PROTOTYPE",null,"Skill","MP",SkillDef.SKILL_PROTO.mpCost,SkillDef.SKILL_PROTO.cooldown,"적",null,null,
        true,true,false,RpgProgressionState.Evidence.B));
    add(out,new Definition(SkillDef.KICK_PROTO.id,SkillDef.KICK_PROTO.label,"PENDING_CROP:"+SkillDef.KICK_PROTO.id,
        "PROTOTYPE",null,"Skill","MP",SkillDef.KICK_PROTO.mpCost,SkillDef.KICK_PROTO.cooldown,"적",null,null,
        true,true,false,RpgProgressionState.Evidence.B));

    // Skill_Master.csv Warrior canonical projection. Numeric MP/cooldown fields are unresolved in Master.
    warrior(out,"SK_전사_001","숏블레이드",1,"Attack","적","전방 1칸","초기 전사 공격기",true,false,RpgProgressionState.Evidence.V);
    warrior(out,"SK_전사_002","더블어택",2,"Passive/Attack","적","기본공격 연동","기본공격과 동시 발동하여 2회 타격 구조",false,true,RpgProgressionState.Evidence.V);
    warrior(out,"SK_전사_003","윈드블레이드",2,"Attack","적","전방 3칸","전방 직선 공격",true,false,RpgProgressionState.Evidence.V);
    warrior(out,"SK_전사_004","디바투",2,"Dispel","자신","자신","이동불가(바투) 해제",true,false,RpgProgressionState.Evidence.V);
    warrior(out,"SK_전사_005","트리플어택",3,"Passive/Attack","적","기본공격 연동","기본공격 3타 구조",false,true,RpgProgressionState.Evidence.V);
    warrior(out,"SK_전사_006","메가블레이드",3,"Attack","적","사방 1칸","전후좌우 1칸 공격",true,false,RpgProgressionState.Evidence.V);
    warrior(out,"SK_전사_007","바투",3,"CrowdControl","적","단일","이동 불가; 기술/마법/아이템 사용 가능",true,false,RpgProgressionState.Evidence.V);
    warrior(out,"SK_전사_008","투핸드어택",4,"Passive/Equipment","자신","자신","양손무기 장착 허용",false,true,RpgProgressionState.Evidence.V);
    warrior(out,"SK_전사_009","드래곤모드",4,"Buff/Drain","자신","자신","DAM+4, HIT+10; 지속 중 체력 감소",true,false,RpgProgressionState.Evidence.V);
    warrior(out,"SK_전사_010","피닉스모드",4,"Buff/Drain","자신","자신","드래곤모드 상위 모드; 습득 시 대체",true,false,RpgProgressionState.Evidence.V);
    warrior(out,"SK_전사_011","적무기쳐내기",4,"Disarm/PvP","적 플레이어","단일","상대 무기 장착 해제",true,false,RpgProgressionState.Evidence.V);
    warrior(out,"SK_전사_012","레스큐",1,"Utility/Aggro","적","단일","몬스터 공격 대상을 자신으로 유도",true,false,RpgProgressionState.Evidence.V);
    warrior(out,"SK_전사_013","매드소울",5,"HP-Cost Burst","적","단일","현재 체력 희생량에 비례하는 강공격",true,false,RpgProgressionState.Evidence.V);
    warrior(out,"SK_전사_014","완전방어",5,"Defense","자신","자신","물리/기술 공격 방어; 마법 공격 제외",true,false,RpgProgressionState.Evidence.V);
    warrior(out,"SK_전사_015","크래셔",5,"HP-Threshold Burst","적","단일","HP 2% 이하 발동 초필살기",true,false,RpgProgressionState.Evidence.V);

    // Circle-1 projections for all five basic jobs. Acquisition requirements remain unresolved, so none auto-learn.
    canonical(out,"SK_도적_001","찌르기","ROGUE",1,"Attack","기본 MP 소모 없음","적","전방 1칸",
        "도적 기본 근접 공격 기술",true,false,RpgProgressionState.Evidence.V,"V");
    canonical(out,"SK_도적_002","센스몬스터","ROGUE",1,"Identify","기본 MP 소모 없음","몬스터","단일",
        "숙련도에 따라 몬스터 이름/EXP/HP/공격속성/방어속성 정보 확인",true,false,RpgProgressionState.Evidence.V,"V");
    canonical(out,"SK_마법사_001","마레노","MAGE",1,"Magic Attack","MP","적","단일",
        "1단계 수속성 공격마법",true,false,RpgProgressionState.Evidence.V,"V");
    canonical(out,"SK_성직자_001","쿠로","CLERIC",1,"Heal","MP","아군/자신","단일",
        "초급 단일 회복",true,false,RpgProgressionState.Evidence.V,"O/V");
    canonical(out,"SK_성직자_002","수혈","CLERIC",1,"HP Transfer / AoE Heal","MP","주변 아군","자신 주변",
        "자신의 HP를 소모해 주변 인원의 HP를 회복",true,false,RpgProgressionState.Evidence.V,"O/V");
    canonical(out,"SK_성직자_003","디렌토","CLERIC",1,"Dispel","MP","아군/자신","단일",
        "렌토 해제",true,false,RpgProgressionState.Evidence.V,"O/V");
    canonical(out,"SK_무도가_001","정권","MARTIAL_ARTIST",1,"Attack","HP/MP/조건부 - 개별 확인","적","전방 1칸",
        "무도가 초기 공격기",true,false,RpgProgressionState.Evidence.V,"O/V");

    return Collections.unmodifiableMap(out);
  }

  private static void warrior(Map<String,Definition> out,String id,String name,int circle,String actionClass,String target,
      String range,String effect,boolean quickSlotEligible,boolean passive,RpgProgressionState.Evidence evidence){
    add(out,new Definition(id,name,"PENDING_CROP:"+id,"WARRIOR",circle,actionClass,"기본 MP 소모 없음",
        null,null,target,range,effect,false,quickSlotEligible,passive,evidence));
  }

  private static void canonical(Map<String,Definition> out,String id,String name,String jobCode,int circle,String actionClass,
      String resourceLabel,String target,String range,String effect,boolean quickSlotEligible,boolean passive,
      RpgProgressionState.Evidence evidence,String sourceEvidence){
    add(out,new Definition(id,name,"PENDING_CROP:"+id,jobCode,circle,actionClass,resourceLabel,null,null,target,range,effect,
        false,quickSlotEligible,passive,evidence,sourceEvidence));
  }

  private static void add(Map<String,Definition> out,Definition def){out.put(def.id,def);}
  public static boolean isKnownAction(String actionId){return actionId!=null&&DEFINITIONS.containsKey(actionId);}

  private static ActionMetadata project(Definition def,RpgProgressionState rpg){
    boolean learned=rpg!=null&&rpg.learnedActionIds().contains(def.id);
    String job=rpg==null?null:rpg.currentJobCode();
    VisibilityState visibility;
    if(def.runtimeBound)visibility=VisibilityState.RUNTIME_PROTOTYPE;
    else if(learned)visibility=VisibilityState.LEARNED;
    else if(def.jobCode.equals(job))visibility=VisibilityState.CURRENT_JOB_LOCKED;
    else visibility=VisibilityState.OTHER_JOB;
    return new ActionMetadata(def.id,def.name,def.iconKey,def.jobCode,def.circle,def.actionClass,def.resourceLabel,
        def.cost,def.cooldown,def.targetLabel,def.rangeLabel,def.effectSummary,def.runtimeBound,def.quickSlotEligible,
        def.passive,learned?LearnedState.LEARNED:LearnedState.UNLEARNED,visibility,def.evidence,def.sourceEvidence);
  }

  public static List<ActionMetadata> snapshot(RpgProgressionState rpg){
    List<ActionMetadata> out=new ArrayList<>();
    for(Definition def:DEFINITIONS.values())out.add(project(def,rpg));
    return Collections.unmodifiableList(out);
  }

  /** Skill-book projection for one canonical job; useful before/after job selection without mutating player state. */
  public static List<ActionMetadata> forJob(RpgProgressionState rpg,String jobCode){
    List<ActionMetadata> out=new ArrayList<>();
    if(jobCode==null)return Collections.unmodifiableList(out);
    for(Definition def:DEFINITIONS.values())if(jobCode.equals(def.jobCode))out.add(project(def,rpg));
    return Collections.unmodifiableList(out);
  }

  /** Circle-filtered skill-book projection; preserves locked/learned state without mutating acquisition. */
  public static List<ActionMetadata> forJobAndCircle(RpgProgressionState rpg,String jobCode,int circle){
    List<ActionMetadata> out=new ArrayList<>();
    for(ActionMetadata m:forJob(rpg,jobCode))if(m.circle!=null&&m.circle==circle)out.add(m);
    return Collections.unmodifiableList(out);
  }

  /** HUD-facing actions: current-job catalog plus learned actions and currently executing prototype bindings. */
  public static List<ActionMetadata> visibleFor(RpgProgressionState rpg){
    List<ActionMetadata> out=new ArrayList<>();
    String job=rpg==null?null:rpg.currentJobCode();
    for(ActionMetadata m:snapshot(rpg))if(m.runtimeBound||m.jobCode.equals(job)||m.learned())out.add(m);
    return Collections.unmodifiableList(out);
  }

  /** Quick-slot candidates never treat a merely catalogued/locked canonical action as learned. */
  public static List<ActionMetadata> quickSlotCandidates(RpgProgressionState rpg){
    List<ActionMetadata> out=new ArrayList<>();
    for(ActionMetadata m:snapshot(rpg)){
      if(!m.quickSlotEligible||m.passive)continue;
      if(m.runtimeBound||m.learned())out.add(m);
    }
    return Collections.unmodifiableList(out);
  }

  public static Map<String,ActionMetadata> byId(RpgProgressionState rpg){
    Map<String,ActionMetadata> out=new LinkedHashMap<>();for(ActionMetadata m:snapshot(rpg))out.put(m.actionId,m);return Collections.unmodifiableMap(out);
  }
}
