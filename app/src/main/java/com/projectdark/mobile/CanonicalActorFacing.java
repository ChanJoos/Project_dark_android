package com.projectdark.mobile;

/**
 * The single presentation-facing domain for every runtime actor.
 * Continuous movement remains legal; only its visual projection is quantized.
 */
public final class CanonicalActorFacing {
  private static final float ZERO_EPSILON=.0001f;

  private CharacterRenderer.Direction locomotionFacing;
  private CharacterRenderer.Direction attackFacing;

  public CanonicalActorFacing(CharacterRenderer.Direction initial){
    locomotionFacing=initial==null?CharacterRenderer.Direction.SE:initial;
  }

  /** Half-open quadrant policy: north owns y<0, west owns x<0; axes fall east/south. */
  public static CharacterRenderer.Direction quantize(float dx,float dy,CharacterRenderer.Direction fallback){
    if(Math.abs(dx)<=ZERO_EPSILON&&Math.abs(dy)<=ZERO_EPSILON)
      return fallback==null?CharacterRenderer.Direction.SE:fallback;
    if(dy<0f)return dx<0f?CharacterRenderer.Direction.NW:CharacterRenderer.Direction.NE;
    return dx<0f?CharacterRenderer.Direction.SW:CharacterRenderer.Direction.SE;
  }

  public void updateLocomotion(float dx,float dy){locomotionFacing=quantize(dx,dy,locomotionFacing);}
  public void setLocomotion(CharacterRenderer.Direction facing){if(facing!=null)locomotionFacing=facing;}
  public CharacterRenderer.Direction locomotion(){return locomotionFacing;}
  public void beginAttack(){attackFacing=locomotionFacing;}
  public void beginAttack(float targetDx,float targetDy){
    CharacterRenderer.Direction canonical=CanonicalMeleeTileContract.facing(0f,0f,targetDx,targetDy);
    attackFacing=canonical==null?quantize(targetDx,targetDy,locomotionFacing):canonical;
  }
  public void endAttack(){attackFacing=null;}
  public boolean attackLocked(){return attackFacing!=null;}
  public CharacterRenderer.Direction attack(){return attackFacing;}
  public CharacterRenderer.Direction presentation(){return attackFacing==null?locomotionFacing:attackFacing;}
}
