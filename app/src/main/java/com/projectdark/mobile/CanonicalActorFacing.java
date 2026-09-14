package com.projectdark.mobile;

import com.projectdark.mobile.world.WorldMoveTargetController;

/**
 * The single presentation-facing domain for every runtime actor.
 * Continuous movement remains legal; only its visual projection is quantized.
 * Attack facing is resolved in the same 64x32 isometric tile-center basis used by World.
 */
public final class CanonicalActorFacing {
  private static final float ZERO_EPSILON=.0001f;
  private static final float TILE_HALF_WIDTH=Math.abs(WorldMoveTargetController.Direction.NE.dx);
  private static final float TILE_HALF_HEIGHT=Math.abs(WorldMoveTargetController.Direction.NE.dy);

  private CharacterRenderer.Direction locomotionFacing;
  private CharacterRenderer.Direction attackFacing;

  public CanonicalActorFacing(CharacterRenderer.Direction initial){
    locomotionFacing=initial==null?CharacterRenderer.Direction.SE:initial;
  }

  /**
   * Project a world-space target delta onto the two reciprocal isometric tile axes.
   * This deliberately does not use screen quadrants/free-angle/8-way indices.
   * Ties deterministically prefer the NE<->SW axis, so reversing a non-zero delta
   * always produces the exact reciprocal facing.
   */
  public static CharacterRenderer.Direction quantize(float dx,float dy,CharacterRenderer.Direction fallback){
    if(Math.abs(dx)<=ZERO_EPSILON&&Math.abs(dy)<=ZERO_EPSILON)
      return fallback==null?CharacterRenderer.Direction.SE:fallback;
    float neAxis=dx/TILE_HALF_WIDTH-dy/TILE_HALF_HEIGHT;
    float seAxis=dx/TILE_HALF_WIDTH+dy/TILE_HALF_HEIGHT;
    if(Math.abs(neAxis)>=Math.abs(seAxis))return neAxis>=0f?CharacterRenderer.Direction.NE:CharacterRenderer.Direction.SW;
    return seAxis>=0f?CharacterRenderer.Direction.SE:CharacterRenderer.Direction.NW;
  }

  public static CharacterRenderer.Direction reciprocal(CharacterRenderer.Direction facing){
    if(facing==null)return null;
    switch(facing){
      case NW:return CharacterRenderer.Direction.SE;
      case NE:return CharacterRenderer.Direction.SW;
      case SW:return CharacterRenderer.Direction.NE;
      case SE:default:return CharacterRenderer.Direction.NW;
    }
  }

  public void updateLocomotion(float dx,float dy){locomotionFacing=quantize(dx,dy,locomotionFacing);}
  public void setLocomotion(CharacterRenderer.Direction facing){if(facing!=null)locomotionFacing=facing;}
  public CharacterRenderer.Direction locomotion(){return locomotionFacing;}
  public void beginAttack(){attackFacing=locomotionFacing;}
  public void beginAttack(float targetDx,float targetDy){attackFacing=quantize(targetDx,targetDy,locomotionFacing);}
  public void endAttack(){attackFacing=null;}
  public boolean attackLocked(){return attackFacing!=null;}
  public CharacterRenderer.Direction attack(){return attackFacing;}
  public CharacterRenderer.Direction presentation(){return attackFacing==null?locomotionFacing:attackFacing;}
}
