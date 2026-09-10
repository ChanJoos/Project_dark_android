package com.projectdark.mobile;

/**
 * Explicitly non-canonical combat tuning for a future playable Pote vertical slice.
 *
 * The monster identity/map relation is canonical; these combat numbers are NOT. They intentionally reuse the
 * existing combat_dummy_01 [B] baseline so no new balance numbers are invented in this pass. Placement remains
 * separate and unresolved.
 */
public final class PotePrototypeCombatProfile {
  public static final String MAP_ID="MAP_POTE_01";
  public static final String MONSTER_ID="POTE_PURPLE";
  public static final String EVIDENCE="B";
  public static final String AI_PROFILE_ID="combat_dummy_baseline_v1";

  // Reuse of the existing Milles combat dummy prototype baseline only; not an original POTE_PURPLE stat claim.
  public static final int RUNTIME_HP_B=60;
  public static final float CHASE_RADIUS_B=180f;
  public static final float ATTACK_BEGIN_RANGE_B=42f;
  public static final float ATTACK_CANCEL_RANGE_B=48f;
  public static final float CHASE_SPEED_B=28f;
  public static final int ATTACK_DAMAGE_B=4;
  public static final float ATTACK_COOLDOWN_B=1.2f;

  private PotePrototypeCombatProfile(){}

  public static boolean isPrototypeOnly(){return "B".equals(EVIDENCE);}
  public static boolean hasPlacement(){return false;}
}
