package com.projectdark.mobile;

import com.projectdark.mobile.world.WorldMoveTargetController;

/**
 * Package-level static/runtime-contract audit for ATTACK_PRESENTATION_003_DEVICE_REPAIR.
 * Device/visual acceptance still requires a fresh exact-APK recording.
 */
public final class AttackPresentationPackageAudit {
  public static final float MAX_FOOT_ERROR_PX=1f;
  public static final float MAX_HANDLE_ERROR_PX=1f;

  private AttackPresentationPackageAudit(){}

  public static boolean run(){
    return canonicalFacingFourWays()
        && coherentCompositionFourWays()
        && singlePosePolicyPreserved()
        && commonPresentationScale()
        && singleMirrorWeaponContract();
  }

  static boolean canonicalFacingFourWays(){
    float ax=320f,ay=240f;
    for(WorldMoveTargetController.Direction d:WorldMoveTargetController.Direction.values()){
      float tx=ax+d.dx,ty=ay+d.dy;
      if(!CanonicalMeleeTileContract.reachable(ax,ay,tx,ty))return false;
      CharacterRenderer.Direction expected=CanonicalMeleeTileContract.facing(d);
      if(expected==null||CanonicalMeleeTileContract.facing(ax,ay,tx,ty)!=expected)return false;
      CanonicalActorFacing facing=new CanonicalActorFacing(CharacterRenderer.Direction.SW);
      facing.beginAttack(tx-ax,ty-ay);
      if(!facing.attackLocked()||facing.presentation()!=expected)return false;
      facing.updateLocomotion(-d.dx,-d.dy);
      if(facing.presentation()!=expected)return false;
      facing.endAttack();
    }
    return true;
  }

  static boolean coherentCompositionFourWays(){
    String robe=CharacterVisualBinding.RESOLVED_ROBE_APPEARANCE_ID;
    String weapon=CharacterVisualBinding.RESOLVED_WEAPON_APPEARANCE_ID;
    for(CharacterRenderer.Direction d:CharacterRenderer.Direction.values()){
      CharacterRenderer.AttackVisualComposition c=CharacterRenderer.attackVisualComposition(d,robe,weapon);
      if(c.direction!=d||c.sourceIndex!=CharacterRenderer.actionSourceIndex(d))return false;
      if(!c.robeVisible||!c.weaponVisible)return false;
      if(c.mirrorBody!=CharacterRenderer.bodyMirrorX(d))return false;
    }
    return true;
  }

  static boolean singlePosePolicyPreserved(){
    return CharacterRenderer.attackUsesSinglePosePlaceholder()
        && !CharacterRenderer.attackTemporalSourceResolved()
        && !CharacterRenderer.attackUsesDestructiveCrop()
        && CharacterRenderer.attackUsesActionPose(.50f)
        && !CharacterRenderer.attackUsesActionPose(.05f)
        && !CharacterRenderer.attackUsesActionPose(.95f);
  }

  static boolean commonPresentationScale(){
    for(CharacterRenderer.Direction d:CharacterRenderer.Direction.values()){
      int source=CharacterRenderer.actionSourceIndex(d);
      if(Math.abs(CharacterRenderer.normalizedActionScale(source)-CharacterRenderer.SOURCE_PRESENTATION_SCALE)>.0001f)return false;
      CharacterRenderer.AttackVisualComposition c=CharacterRenderer.attackVisualComposition(d,CharacterVisualBinding.RESOLVED_ROBE_APPEARANCE_ID,CharacterVisualBinding.RESOLVED_WEAPON_APPEARANCE_ID);
      if(Math.abs(c.presentationScale-CharacterRenderer.PLAYER_RENDER_SCALE)>.0001f)return false;
    }
    return true;
  }

  static boolean singleMirrorWeaponContract(){
    return CharacterRenderer.weaponTransformUsesSingleMirror();
  }
}
