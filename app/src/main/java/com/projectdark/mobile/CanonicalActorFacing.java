package com.projectdark.mobile;

import com.projectdark.mobile.world.WorldMoveTargetController;

/** Single facing state for locomotion, combat lock and presentation. */
public final class CanonicalActorFacing {
  private static final float ZERO_EPSILON=.0001f;
  private CharacterRenderer.Direction locomotionFacing;
  private CharacterRenderer.Direction attackFacing;
  public CanonicalActorFacing(CharacterRenderer.Direction initial){locomotionFacing=initial==null?CharacterRenderer.Direction.SE:initial;}
  public static CharacterRenderer.Direction fromWorldDirection(WorldMoveTargetController.Direction d){return CanonicalMeleeTileContract.facing(d);}
  /** Compatibility projection for non-tile inputs only. Authored movement/combat must pass a World Direction. */
  public static CharacterRenderer.Direction quantize(float dx,float dy,CharacterRenderer.Direction fallback){if(Math.abs(dx)<=ZERO_EPSILON&&Math.abs(dy)<=ZERO_EPSILON)return fallback==null?CharacterRenderer.Direction.SE:fallback;if(dy<0f)return dx<0f?CharacterRenderer.Direction.NW:CharacterRenderer.Direction.NE;return dx<0f?CharacterRenderer.Direction.SW:CharacterRenderer.Direction.SE;}
  public void updateLocomotion(float dx,float dy){locomotionFacing=quantize(dx,dy,locomotionFacing);}
  public void updateLocomotion(WorldMoveTargetController.Direction d){CharacterRenderer.Direction f=fromWorldDirection(d);if(f!=null)locomotionFacing=f;}
  public void setLocomotion(CharacterRenderer.Direction facing){if(facing!=null)locomotionFacing=facing;}
  public CharacterRenderer.Direction locomotion(){return locomotionFacing;}
  public void beginAttack(){attackFacing=locomotionFacing;}
  public void beginAttack(WorldMoveTargetController.Direction d){CharacterRenderer.Direction f=fromWorldDirection(d);attackFacing=f==null?locomotionFacing:f;}
  public void beginAttack(float targetDx,float targetDy){WorldMoveTargetController.Direction d=WorldMoveTargetController.Direction.between(0f,0f,targetDx,targetDy);if(d!=null){beginAttack(d);return;}attackFacing=quantize(targetDx,targetDy,locomotionFacing);}
  public void endAttack(){attackFacing=null;} public boolean attackLocked(){return attackFacing!=null;} public CharacterRenderer.Direction attack(){return attackFacing;} public CharacterRenderer.Direction presentation(){return attackFacing==null?locomotionFacing:attackFacing;}
}
