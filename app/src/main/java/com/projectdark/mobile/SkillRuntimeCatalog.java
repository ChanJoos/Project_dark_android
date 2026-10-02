package com.projectdark.mobile;
import java.util.*;
/** Complete per-ID V83 policy. Definitions no longer contain a shared damage fallback. */
public final class SkillRuntimeCatalog {
 private static final Map<String,SkillDef> DEFINITIONS=new LinkedHashMap<>();
 static{for(SkillAbilityCatalog.Ability a:SkillAbilityCatalog.all()){
  if(!a.supported())continue;SkillActionContract.Rule r=SkillActionContract.get(a.id);boolean magic="마법".equals(r.kind);
  DEFINITIONS.put(a.id,new SkillDef(a.id,a.name,magic?SkillDef.ActionClass.MAGIC:SkillDef.ActionClass.TECHNIQUE,
   r.selfAnchored()?SkillDef.TargetPolicy.SELF:SkillDef.TargetPolicy.ENEMY,a.mpCost,a.cooldown,Float.MAX_VALUE,0,
   a.name.contains("각")?SkillDef.EffectType.KICK_ARC:magic?SkillDef.EffectType.CAST_RING:SkillDef.EffectType.MELEE_ARC,SkillDef.Evidence.ADAPTED));
 }}
 public static SkillDef get(String id){return DEFINITIONS.get(id);}
 public static Collection<SkillDef> definitions(){return Collections.unmodifiableCollection(DEFINITIONS.values());}
 public static boolean healing(String id){SkillAbilityCatalog.Ability a=SkillAbilityCatalog.get(id);return a!=null&&a.heal();}
 public static boolean magic(String id){SkillDef d=get(id);return "cast_proto".equals(id)||d!=null&&d.actionClass==SkillDef.ActionClass.MAGIC;}
}
