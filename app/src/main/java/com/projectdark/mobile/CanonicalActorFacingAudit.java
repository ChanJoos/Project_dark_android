package com.projectdark.mobile;

import java.util.EnumSet;

/** Focused deterministic audit; deliberately not executed as an app-startup gate. */
public final class CanonicalActorFacingAudit {
  private CanonicalActorFacingAudit(){}

  public static boolean passes(){
    EnumSet<CharacterRenderer.Direction> seen=EnumSet.noneOf(CharacterRenderer.Direction.class);
    float[] values={-100f,-1f,-.0002f,0f,.0002f,1f,100f};
    for(float dx:values)for(float dy:values){
      CharacterRenderer.Direction player=CanonicalActorFacing.quantize(dx,dy,CharacterRenderer.Direction.NW);
      CharacterRenderer.Direction monster=CanonicalActorFacing.quantize(dx,dy,CharacterRenderer.Direction.NW);
      if(player==null||monster==null||player!=monster)return false;
      seen.add(player);
    }
    if(seen.size()!=4)return false;
    if(CanonicalActorFacing.quantize(-1f,-1f,null)!=CharacterRenderer.Direction.NW)return false;
    if(CanonicalActorFacing.quantize(1f,-1f,null)!=CharacterRenderer.Direction.NE)return false;
    if(CanonicalActorFacing.quantize(-1f,1f,null)!=CharacterRenderer.Direction.SW)return false;
    if(CanonicalActorFacing.quantize(1f,1f,null)!=CharacterRenderer.Direction.SE)return false;
    if(CanonicalActorFacing.quantize(1f,0f,null)!=CharacterRenderer.Direction.SE)return false;
    if(CanonicalActorFacing.quantize(0f,-1f,null)!=CharacterRenderer.Direction.NE)return false;

    CanonicalActorFacing actor=new CanonicalActorFacing(CharacterRenderer.Direction.SW);
    actor.beginAttack(4f,-2f);
    CharacterRenderer.Direction snapshot=actor.presentation();
    actor.updateLocomotion(-5f,3f);
    if(!actor.attackLocked()||actor.presentation()!=snapshot)return false;
    actor.endAttack();
    if(actor.presentation()!=CharacterRenderer.Direction.SW)return false;
    CharacterRenderer.Direction[] dirs={CharacterRenderer.Direction.NW,CharacterRenderer.Direction.NE,CharacterRenderer.Direction.SW,CharacterRenderer.Direction.SE};
    for(CharacterRenderer.Direction previous:dirs)for(CharacterRenderer.Direction target:dirs){CanonicalActorFacing locked=new CanonicalActorFacing(previous);locked.beginAttack(target);if(locked.presentation()!=target||!locked.attackLocked())return false;locked.setLocomotion(previous);if(locked.presentation()!=target)return false;locked.endAttack();if(locked.presentation()!=previous)return false;}
    for(CharacterRenderer.Direction locomotion:CharacterRenderer.Direction.values())for(CharacterRenderer.Direction attack:CharacterRenderer.Direction.values()){CanonicalActorFacing isolated=new CanonicalActorFacing(locomotion);isolated.beginAttack(attack);if(isolated.presentation()!=attack||isolated.locomotion()!=locomotion)return false;isolated.endAttack();if(isolated.presentation()!=locomotion)return false;} return true;
  }

  public static void main(String[] args){
    if(!passes())throw new AssertionError("CanonicalActorFacingAudit failed");
    System.out.println("CanonicalActorFacingAudit PASS");
  }
}
