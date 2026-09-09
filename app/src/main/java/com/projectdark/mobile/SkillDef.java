package com.projectdark.mobile;

/**
 * Prototype skill/action definition used by the mobile runtime.
 *
 * Evidence policy:
 * - [B] Current costs, cooldowns, ranges and damage values are prototype reconstruction values.
 * - [ADAPTED] EffectType describes mobile/runtime presentation rather than an authenticated original effect asset.
 * - No values in this file are claimed to be original server data until replaced by verified [O]/[V] evidence.
 */
public final class SkillDef {
  public enum Evidence { O, V, B, ADAPTED }
  public enum EffectType { CAST_RING, MELEE_ARC, PROJECTILE, KICK_ARC }

  public final String id;
  public final String label;
  public final int mpCost;
  public final float cooldown;
  public final float range;
  public final int damage;
  public final EffectType effectType;
  public final Evidence evidence;

  public SkillDef(String id, String label, int mpCost, float cooldown, float range,
                  int damage, EffectType effectType, Evidence evidence) {
    this.id = id;
    this.label = label;
    this.mpCost = mpCost;
    this.cooldown = cooldown;
    this.range = range;
    this.damage = damage;
    this.effectType = effectType;
    this.evidence = evidence;
  }

  public static final SkillDef CAST_PROTO = new SkillDef(
      "cast_proto", "MAGIC", 8, 1.2f, 150f, 12, EffectType.CAST_RING, Evidence.B);
  public static final SkillDef SKILL_PROTO = new SkillDef(
      "skill_proto", "SK", 5, 2.0f, 90f, 16, EffectType.MELEE_ARC, Evidence.B);
  public static final SkillDef KICK_PROTO = new SkillDef(
      "kick_proto", "K", 0, 1.0f, 82f, 14, EffectType.KICK_ARC, Evidence.B);

  private SkillDef() { throw new AssertionError("No instances"); }
}
