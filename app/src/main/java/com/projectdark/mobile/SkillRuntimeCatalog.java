package com.projectdark.mobile;

import java.util.*;

/** Explicit mobile combat adaptations, independent from read-only Master descriptions.
 * Numeric values are B/ADAPTED balance fixtures, not original server values.
 * Only single-target damage and self healing are implemented here. Never alias utility/AOE skills.
 */
public final class SkillRuntimeCatalog {
  private static final Map<String,SkillDef> DEFINITIONS=new LinkedHashMap<>();
  static {
    physical("SK_전사_001","숏블레이드",16,2f,48f,false);
    physical("SK_도적_001","찌르기",14,1.5f,48f,false);
    physical("SK_도적_007","찔러휘비기",20,3f,48f,false);
    physical("SK_무도가_001","정권",12,1f,48f,false);
    physical("SK_무도가_002","단각",14,2f,48f,true);
    physical("SK_무도가_007","붕각",22,4f,48f,true);
    for(String id:new String[]{"001","003","004","005"})magic("SK_마법사_"+id,8,1.2f,12);
    for(String id:new String[]{"013","014","015","016"})magic("SK_마법사_"+id,12,1.6f,20);
    for(String id:new String[]{"026","027","028","029"})magic("SK_마법사_"+id,18,2f,28);
    magic("SK_성직자_013",10,1.5f,18);
    magic("SK_성직자_020",16,2f,26);
    magic("SK_성직자_052",6,1.2f,10);
    for(String id:new String[]{"SK_성직자_058","SK_마법사_056","SK_무도가_032","SK_공통_010"})heal(id,6,2f,15);
    heal("SK_성직자_001",8,2f,22);heal("SK_성직자_005",12,3f,35);
    heal("SK_성직자_011",18,4f,50);heal("SK_무도가_017",14,4f,40);
  }
  private static void physical(String id,String name,int damage,float cd,float range,boolean kick){DEFINITIONS.put(id,new SkillDef(id,name,SkillDef.ActionClass.TECHNIQUE,SkillDef.TargetPolicy.ENEMY,0,cd,range,damage,kick?SkillDef.EffectType.KICK_ARC:SkillDef.EffectType.MELEE_ARC,SkillDef.Evidence.ADAPTED));}
  private static void magic(String id,int cost,float cd,int damage){DEFINITIONS.put(id,new SkillDef(id,id,SkillDef.ActionClass.MAGIC,SkillDef.TargetPolicy.ENEMY,cost,cd,150f,damage,SkillDef.EffectType.CAST_RING,SkillDef.Evidence.ADAPTED));}
  private static void heal(String id,int cost,float cd,int amount){DEFINITIONS.put(id,new SkillDef(id,id,SkillDef.ActionClass.MAGIC,SkillDef.TargetPolicy.SELF,cost,cd,0f,amount,SkillDef.EffectType.CAST_RING,SkillDef.Evidence.ADAPTED));}
  public static SkillDef get(String id){return DEFINITIONS.get(id);}
  public static Collection<SkillDef> definitions(){return Collections.unmodifiableCollection(DEFINITIONS.values());}
  public static boolean healing(String id){SkillDef d=get(id);return d!=null&&d.targetPolicy==SkillDef.TargetPolicy.SELF;}
  public static boolean magic(String id){SkillDef d=get(id);return "cast_proto".equals(id)||d!=null&&d.actionClass==SkillDef.ActionClass.MAGIC;}
}
