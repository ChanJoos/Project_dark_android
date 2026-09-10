package com.projectdark.mobile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** RPG-owned stable metadata projection for HUD/inventory consumers. No action execution lives here. */
public final class RpgActionMetadataCatalog {
  public enum LearnedState { LEARNED, UNLEARNED }

  /**
   * Stable presentation DTO. Nullable cost/cooldown deliberately preserve unresolved Master values;
   * consumers must render them as pending/unknown rather than inventing zero.
   */
  public static final class ActionMetadata {
    public final String actionId,name,iconKey,jobCode,actionClass;
    public final Integer circle;
    public final Integer resourceCost;
    public final Float cooldown;
    public final boolean runtimeBound;
    public final LearnedState state;
    public final RpgProgressionState.Evidence evidence;

    ActionMetadata(String actionId,String name,String iconKey,String jobCode,Integer circle,String actionClass,
        Integer resourceCost,Float cooldown,boolean runtimeBound,LearnedState state,RpgProgressionState.Evidence evidence){
      this.actionId=actionId;this.name=name;this.iconKey=iconKey;this.jobCode=jobCode;this.circle=circle;
      this.actionClass=actionClass;this.resourceCost=resourceCost;this.cooldown=cooldown;this.runtimeBound=runtimeBound;
      this.state=state;this.evidence=evidence;
    }
    public boolean learned(){return state==LearnedState.LEARNED;}
    public boolean resourceCostResolved(){return resourceCost!=null;}
    public boolean cooldownResolved(){return cooldown!=null;}
  }

  private static final class Definition {
    final String id,name,iconKey,jobCode,actionClass;
    final Integer circle,cost;
    final Float cooldown;
    final boolean runtimeBound;
    final RpgProgressionState.Evidence evidence;
    Definition(String id,String name,String iconKey,String jobCode,Integer circle,String actionClass,Integer cost,Float cooldown,
        boolean runtimeBound,RpgProgressionState.Evidence evidence){
      this.id=id;this.name=name;this.iconKey=iconKey;this.jobCode=jobCode;this.circle=circle;this.actionClass=actionClass;
      this.cost=cost;this.cooldown=cooldown;this.runtimeBound=runtimeBound;this.evidence=evidence;
    }
  }

  private static final Map<String,Definition> DEFINITIONS=buildDefinitions();
  private RpgActionMetadataCatalog(){}

  private static Map<String,Definition> buildDefinitions(){
    Map<String,Definition> out=new LinkedHashMap<>();

    // Executing prototype actions remain explicit [B] bindings until Combat-owned wiring migrates.
    add(out,new Definition(SkillDef.CAST_PROTO.id,SkillDef.CAST_PROTO.label,"PENDING_CROP:"+SkillDef.CAST_PROTO.id,
        "PROTOTYPE",null,"Cast",SkillDef.CAST_PROTO.mpCost,SkillDef.CAST_PROTO.cooldown,true,RpgProgressionState.Evidence.B));
    add(out,new Definition(SkillDef.SKILL_PROTO.id,SkillDef.SKILL_PROTO.label,"PENDING_CROP:"+SkillDef.SKILL_PROTO.id,
        "PROTOTYPE",null,"Skill",SkillDef.SKILL_PROTO.mpCost,SkillDef.SKILL_PROTO.cooldown,true,RpgProgressionState.Evidence.B));
    add(out,new Definition(SkillDef.KICK_PROTO.id,SkillDef.KICK_PROTO.label,"PENDING_CROP:"+SkillDef.KICK_PROTO.id,
        "PROTOTYPE",null,"Skill",SkillDef.KICK_PROTO.mpCost,SkillDef.KICK_PROTO.cooldown,true,RpgProgressionState.Evidence.B));

    // Skill_Master.csv canonical first-circle/early Warrior projection. Cost/cooldown are unresolved in Master.
    add(out,new Definition("SK_전사_001","숏블레이드","PENDING_CROP:SK_전사_001","WARRIOR",1,"Attack",null,null,false,RpgProgressionState.Evidence.V));
    add(out,new Definition("SK_전사_002","더블어택","PENDING_CROP:SK_전사_002","WARRIOR",2,"Passive/Attack",null,null,false,RpgProgressionState.Evidence.V));
    add(out,new Definition("SK_전사_003","윈드블레이드","PENDING_CROP:SK_전사_003","WARRIOR",2,"Attack",null,null,false,RpgProgressionState.Evidence.V));
    add(out,new Definition("SK_전사_004","디바투","PENDING_CROP:SK_전사_004","WARRIOR",2,"Dispel",null,null,false,RpgProgressionState.Evidence.V));
    add(out,new Definition("SK_전사_005","트리플어택","PENDING_CROP:SK_전사_005","WARRIOR",3,"Passive/Attack",null,null,false,RpgProgressionState.Evidence.V));
    return Collections.unmodifiableMap(out);
  }

  private static void add(Map<String,Definition> out,Definition def){out.put(def.id,def);}

  public static boolean isKnownAction(String actionId){return actionId!=null&&DEFINITIONS.containsKey(actionId);}

  public static List<ActionMetadata> snapshot(RpgProgressionState rpg){
    List<ActionMetadata> out=new ArrayList<>();
    for(Definition def:DEFINITIONS.values()){
      boolean learned=rpg!=null&&rpg.learnedActionIds().contains(def.id);
      out.add(new ActionMetadata(def.id,def.name,def.iconKey,def.jobCode,def.circle,def.actionClass,def.cost,def.cooldown,
          def.runtimeBound,learned?LearnedState.LEARNED:LearnedState.UNLEARNED,def.evidence));
    }
    return Collections.unmodifiableList(out);
  }

  /** HUD-facing filter: canonical actions for the player's job plus currently executing prototype bindings. */
  public static List<ActionMetadata> visibleFor(RpgProgressionState rpg){
    List<ActionMetadata> out=new ArrayList<>();
    String job=rpg==null?null:rpg.currentJobCode();
    for(ActionMetadata m:snapshot(rpg)){
      if(m.runtimeBound||m.jobCode.equals(job)||m.learned())out.add(m);
    }
    return Collections.unmodifiableList(out);
  }

  public static Map<String,ActionMetadata> byId(RpgProgressionState rpg){
    Map<String,ActionMetadata> out=new LinkedHashMap<>();for(ActionMetadata m:snapshot(rpg))out.put(m.actionId,m);return Collections.unmodifiableMap(out);
  }
}
