package com.projectdark.mobile;

/** USER video calibrated adaptation, not an original server timing claim. */
public final class BasicAttackTiming {
  public static final float TWO_HAND_CONTACT=.30f,BASIC_CYCLE=1f,TWO_HAND_CYCLE=.80f,HIT_SPACING=.065f;
  public static boolean twoHand(RpgProgressionState r){return WeaponCombatProfile.grip(r.equippedDefinition(RpgProgressionState.WEAPON_SLOT))==WeaponCombatProfile.Grip.TWO_HAND;}
  public static float contact(RpgProgressionState r,float fallback){return twoHand(r)?TWO_HAND_CONTACT:fallback;}
  public static float cycle(RpgProgressionState r,float fallback){return BASIC_CYCLE;}
  private BasicAttackTiming(){}
}
