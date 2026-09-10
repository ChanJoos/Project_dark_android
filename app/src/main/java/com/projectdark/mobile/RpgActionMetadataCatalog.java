package com.projectdark.mobile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** RPG-owned stable metadata projection for HUD/inventory consumers. No action execution lives here. */
public final class RpgActionMetadataCatalog {
  public enum LearnedState { LEARNED, UNLEARNED }
  public static final class ActionMetadata {
    public final String actionId,name,iconKey;
    public final int resourceCost;
    public final float cooldown;
    public final LearnedState state;
    public final RpgProgressionState.Evidence evidence;
    ActionMetadata(String actionId,String name,String iconKey,int resourceCost,float cooldown,LearnedState state,RpgProgressionState.Evidence evidence){
      this.actionId=actionId;this.name=name;this.iconKey=iconKey;this.resourceCost=resourceCost;this.cooldown=cooldown;this.state=state;this.evidence=evidence;
    }
    public boolean learned(){return state==LearnedState.LEARNED;}
  }

  private static final List<SkillDef> KNOWN=Collections.unmodifiableList(Arrays.asList(SkillDef.CAST_PROTO,SkillDef.SKILL_PROTO,SkillDef.KICK_PROTO));
  private RpgActionMetadataCatalog(){}

  public static boolean isKnownAction(String actionId){
    if(actionId==null)return false;for(SkillDef def:KNOWN)if(def.id.equals(actionId))return true;return false;
  }

  public static List<ActionMetadata> snapshot(RpgProgressionState rpg){
    List<ActionMetadata> out=new ArrayList<>();
    for(SkillDef def:KNOWN){
      boolean learned=rpg!=null&&rpg.learnedActionIds().contains(def.id);
      out.add(new ActionMetadata(def.id,def.label,"PENDING_CROP:"+def.id,def.mpCost,def.cooldown,
          learned?LearnedState.LEARNED:LearnedState.UNLEARNED,RpgProgressionState.Evidence.B));
    }
    return Collections.unmodifiableList(out);
  }

  public static Map<String,ActionMetadata> byId(RpgProgressionState rpg){
    Map<String,ActionMetadata> out=new LinkedHashMap<>();for(ActionMetadata m:snapshot(rpg))out.put(m.actionId,m);return Collections.unmodifiableMap(out);
  }
}
