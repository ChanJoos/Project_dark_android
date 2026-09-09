package com.projectdark.mobile;

/**
 * Skill/action definition used by the mobile runtime.
 *
 * Evidence policy:
 * - [O] Official guide distinguishes MP-free techniques (기술) from MP-consuming magic (마법).
 *   Source: https://lod.nexon.com/info/guide/82293
 * - [B] Current costs, cooldowns, ranges and damage values are prototype reconstruction values.
 * - [ADAPTED] EffectType/TargetPolicy describe mobile runtime presentation/input behavior rather
 *   than authenticated original effect assets or targeting internals.
 * - No numeric values in this file are claimed to be original server data until replaced by
 *   verified [O]/[V] evidence.
 */
public final class SkillDef {
  public enum Evidence { O, V, B, ADAPTED }
  /** [O] High-level distinction only; individual prototype membership remains provisional. */
  public enum ActionClass { TECHNIQUE, MAGIC }
  public enum TargetPolicy { ENEMY, SELF, ALLY, GROUND }
  public enum EffectType { CAST_RING, MELEE_ARC, PROJECTILE, KICK_ARC }

  public final String id;
  public final String label;
  public final ActionClass actionClass;
  public final TargetPolicy targetPolicy;
  public final int mpCost;
  public final float cooldown;
  public final float range;
  public final int damage;
  public final EffectType effectType;
  public final Evidence evidence;

  public SkillDef(String id,String label,ActionClass actionClass,TargetPolicy targetPolicy,
                  int mpCost,float cooldown,float range,int damage,
                  EffectType effectType,Evidence evidence){
    this.id=id;
    this.label=label;
    this.actionClass=actionClass;
    this.targetPolicy=targetPolicy;
    this.mpCost=mpCost;
    this.cooldown=cooldown;
    this.range=range;
    this.damage=damage;
    this.effectType=effectType;
    this.evidence=evidence;
  }

  /** [B] Offensive magic fixture used to validate CAST + target/range/resource behavior. */
  public static final SkillDef CAST_PROTO=new SkillDef(
      "cast_proto","MAGIC",ActionClass.MAGIC,TargetPolicy.ENEMY,
      8,1.2f,150f,12,EffectType.CAST_RING,Evidence.B);

  /** [B] Generic combat skill fixture. mpCost is provisional and intentionally not an [O] claim. */
  public static final SkillDef SKILL_PROTO=new SkillDef(
      "skill_proto","SK",ActionClass.TECHNIQUE,TargetPolicy.ENEMY,
      5,2.0f,90f,16,EffectType.MELEE_ARC,Evidence.B);

  /**
   * [B] Martial-artist kick fixture. The dedicated kick motion is supported conceptually by the
   * official class introduction, while timing/range/damage remain reconstructed prototype data.
   * Source: https://lod.nexon.com/info/intro
   */
  public static final SkillDef KICK_PROTO=new SkillDef(
      "kick_proto","K",ActionClass.TECHNIQUE,TargetPolicy.ENEMY,
      0,1.0f,82f,14,EffectType.KICK_ARC,Evidence.B);

  private SkillDef(){throw new AssertionError("No instances");}
}
