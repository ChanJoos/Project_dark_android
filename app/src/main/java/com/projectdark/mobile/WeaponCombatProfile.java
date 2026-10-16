package com.projectdark.mobile;
import java.util.*;
/** Reviewed source-name classification; exact animation cadence remains ADAPTED. */
public final class WeaponCombatProfile {
 public enum Grip { UNARMED, ONE_HAND, TWO_HAND }
 private static final Set<String> THRUST=new HashSet<>(Arrays.asList("mw002","mw024","mw008"));
 private static final Set<String> TWO=new HashSet<>(Arrays.asList("mw016","mw018","mw019","mw021","mw022","mw023","mw170","mw203","mw_chungryong"));
 public static Grip grip(RpgProgressionState.ItemDefinition d){return d==null?Grip.UNARMED:TWO.contains(d.appearanceId)?Grip.TWO_HAND:Grip.ONE_HAND;}
 public static AnimationAction basicAction(RpgProgressionState.ItemDefinition d){
  if(d==null)return AnimationAction.PUNCH;
  return THRUST.contains(d.appearanceId)?AnimationAction.THRUST:d.basicAttackAction==null?AnimationAction.PUNCH:d.basicAttackAction;
 }
 public static String motion(RpgProgressionState r,String requested){
  if(!"SWING".equals(requested)&&!"EQUIPPED_BASIC".equals(requested))return requested;
  RpgProgressionState.ItemDefinition d=r.equippedDefinition(RpgProgressionState.WEAPON_SLOT);
  if(d==null)return "PUNCH";
  if(grip(d)==Grip.TWO_HAND)return "TWO_HAND";
  return basicAction(d)==AnimationAction.THRUST?"THRUST":"SWING";
 }
 public static String label(RpgProgressionState.ItemDefinition d){return grip(d)==Grip.TWO_HAND?"양손무기":"한손무기";}
 private WeaponCombatProfile(){}
}


