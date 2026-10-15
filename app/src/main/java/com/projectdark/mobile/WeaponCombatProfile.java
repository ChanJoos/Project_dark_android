package com.projectdark.mobile;
import java.util.*;
/** Reviewed source-name classification; exact animation cadence remains ADAPTED. */
public final class WeaponCombatProfile {
 public enum Grip { UNARMED, ONE_HAND, TWO_HAND }
 private static final Set<String> TWO=new HashSet<>(Arrays.asList("mw016","mw018","mw019","mw021","mw022","mw023","mw170","mw203"));
 public static Grip grip(RpgProgressionState.ItemDefinition d){return d==null?Grip.UNARMED:TWO.contains(d.appearanceId)?Grip.TWO_HAND:Grip.ONE_HAND;}
 public static String motion(RpgProgressionState r,String requested){
  if(!"SWING".equals(requested)&&!"EQUIPPED_BASIC".equals(requested))return requested;
  RpgProgressionState.ItemDefinition d=r.equippedDefinition(RpgProgressionState.WEAPON_SLOT);
  if(grip(d)==Grip.TWO_HAND)return "TWO_HAND";
  return d!=null&&d.basicAttackAction==AnimationAction.THRUST?"THRUST":requested;
 }
 public static String label(RpgProgressionState.ItemDefinition d){return grip(d)==Grip.TWO_HAND?"양손무기":"한손무기";}
 private WeaponCombatProfile(){}
}
