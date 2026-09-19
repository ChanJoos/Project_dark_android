package com.projectdark.mobile;

import java.util.EnumSet;

/** Focused stabilization audit. Not invoked from app startup. */
public final class RuntimeVisualStabilizationAudit {
  private RuntimeVisualStabilizationAudit(){}

  public static boolean passes(){
    if(GameView.inventoryOverlayCancelsMovement())return false;
    if(GameView.hitCharacterPoseEnabled())return false;
    if(CharacterRenderer.hitCharacterPoseEnabled())return false;
    if(CharacterRenderer.Direction.values().length!=4)return false;

    EnumSet<CharacterRenderer.Direction> facings=EnumSet.noneOf(CharacterRenderer.Direction.class);
    float[] v={-100f,-1f,-.0002f,0f,.0002f,1f,100f};
    for(float dx:v)for(float dy:v){
      CharacterRenderer.Direction player=GameView.canonicalAttackFacing(dx,dy,CharacterRenderer.Direction.SE);
      CharacterRenderer.Direction monster=CanonicalActorFacing.quantize(dx,dy,CharacterRenderer.Direction.SE);
      if(player==null||player!=monster)return false;
      facings.add(player);
    }
    if(facings.size()!=4)return false;

    CanonicalActorFacing player=new CanonicalActorFacing(CharacterRenderer.Direction.SW);
    player.beginAttack(8f,-3f);
    CharacterRenderer.Direction locked=player.presentation();
    player.updateLocomotion(-8f,4f);
    if(!player.attackLocked()||player.presentation()!=locked)return false;
    player.endAttack();

    if(CharacterVisualBinding.garmentCoverageForAppearance("mu0000058")!=CharacterVisualBinding.GarmentCoverage.FULL_BODY)return false;
    if(!CharacterRenderer.sourceActionLayersReadyContract("mu0000058","mw001",true,true,true))return false;
    if(CharacterRenderer.sourceActionLayersReadyContract("mu0000058","mw001",true,false,true))return false;
    if(CharacterRenderer.sourceActionLayersReadyContract("mu0000058","mw001",true,true,false))return false;
    if(!CharacterRenderer.weaponTransformUsesSingleMirror())return false;
    if(!CharacterRenderer.alphaGeometryWithinTolerance(40,41,1,2))return false;
    if(CharacterRenderer.alphaGeometryWithinTolerance(40,42,0,0))return false;
    return CharacterRendererAudit.passes();
  }

  public static void main(String[] args){
    if(!passes())throw new AssertionError("RuntimeVisualStabilizationAudit failed");
    System.out.println("RuntimeVisualStabilizationAudit PASS");
  }
}
