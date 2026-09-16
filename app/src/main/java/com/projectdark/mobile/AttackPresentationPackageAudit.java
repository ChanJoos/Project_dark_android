package com.projectdark.mobile;

import com.projectdark.mobile.world.WorldMoveTargetController;

/** Package-level contract audit for ATTACK_PRESENTATION_003_DEVICE_REPAIR. */
public final class AttackPresentationPackageAudit {
  public static final float MAX_FOOT_ERROR_PX=1f;
  public static final float MAX_HANDLE_ERROR_PX=1f;

  private AttackPresentationPackageAudit(){}

  public static boolean run(){
    return canonicalFacingFourWays()
        && coherentCompositionFourWays()
        && singlePosePolicyPreserved()
        && finalCompositeNormalizationContract()
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

  /** Proves the package transform removes action-silhouette size/foot drift in final screen space. */
  static boolean finalCompositeNormalizationContract(){
    float base=CharacterRenderer.SOURCE_PRESENTATION_SCALE;
    int idleTop=8,idleBottom=45;
    float idleCenter=18f;
    float expectedHeight=(idleBottom-idleTop+1)*base;
    float expectedFoot=240f-CharacterRenderer.SOURCE_FOOT_ANCHOR_Y*base+idleBottom*base;
    float expectedCenter=320f+(idleCenter-CharacterRenderer.SOURCE_FOOT_ANCHOR_X)*base;
    int[][] action={{0,50},{1,51},{0,50},{0,48}};
    float[] centers={8.5f,9f,9.5f,10.5f};
    for(int i=0;i<action.length;i++){
      AttackCompositeTransform.Result r=AttackCompositeTransform.normalize(
          320f,240f,base,CharacterRenderer.SOURCE_FOOT_ANCHOR_Y,CharacterRenderer.SOURCE_FOOT_ANCHOR_X,
          idleTop,idleBottom,idleCenter,action[i][0],action[i][1],centers[i]);
      if(!AttackCompositeTransform.finalGeometryWithinTolerance(r,action[i][0],action[i][1],centers[i],
          expectedHeight,expectedFoot,expectedCenter))return false;
    }
    return true;
  }

  static boolean singleMirrorWeaponContract(){return CharacterRenderer.weaponTransformUsesSingleMirror();}
}
