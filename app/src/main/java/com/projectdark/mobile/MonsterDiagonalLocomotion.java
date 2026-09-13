package com.projectdark.mobile;

/**
 * World-owned monster locomotion projection for the 64x32 isometric grid.
 *
 * <p>AI targets may be arbitrary points, but every applied movement segment is one of the four
 * legal screen diagonals.  The 2:1 component ratio is the normalized form of a 32x16 half-tile
 * step.  Distances remain [B] runtime values; the direction geometry is the accepted world
 * contract.</p>
 */
public final class MonsterDiagonalLocomotion {
  public static final float LOGICAL_STEP_X=32f;
  public static final float LOGICAL_STEP_Y=16f;
  private static final float INV_SQRT_5=(float)(1d/Math.sqrt(5d));
  public static final float UNIT_X=2f*INV_SQRT_5;
  public static final float UNIT_Y=INV_SQRT_5;
  public static final float ZERO_EPSILON=.0001f;

  public static final class Step {
    public final CharacterRenderer.Direction facing;
    public final float dx,dy;
    Step(CharacterRenderer.Direction facing,float dx,float dy){
      this.facing=facing;this.dx=dx;this.dy=dy;
    }
  }

  public interface Occupancy {
    boolean canOccupy(float x,float y);
  }

  private MonsterDiagonalLocomotion(){}

  public static Step toward(float desiredDx,float desiredDy,float distance,
      CharacterRenderer.Direction fallback){
    if(!Float.isFinite(desiredDx)||!Float.isFinite(desiredDy)||!Float.isFinite(distance)
        ||distance<=ZERO_EPSILON)return null;
    if(Math.abs(desiredDx)<=ZERO_EPSILON&&Math.abs(desiredDy)<=ZERO_EPSILON)return null;
    return forFacing(CanonicalActorFacing.quantize(desiredDx,desiredDy,fallback),distance);
  }

  public static Step forFacing(CharacterRenderer.Direction facing,float distance){
    if(facing==null||!Float.isFinite(distance)||distance<=ZERO_EPSILON)return null;
    float sx=(facing==CharacterRenderer.Direction.NW||facing==CharacterRenderer.Direction.SW)?-1f:1f;
    float sy=(facing==CharacterRenderer.Direction.NW||facing==CharacterRenderer.Direction.NE)?-1f:1f;
    return new Step(facing,sx*UNIT_X*distance,sy*UNIT_Y*distance);
  }

  /**
   * Chooses the exact legal segment RuntimeState may apply. Collision is evaluated atomically at
   * the segment endpoint, so a rejected diagonal can never degrade into a one-axis move.
   */
  public static Step select(float originX,float originY,float desiredDx,float desiredDy,float distance,
      CharacterRenderer.Direction fallback,int detourSign,Occupancy occupancy){
    if(occupancy==null)return null;
    Step primary=toward(desiredDx,desiredDy,distance,fallback);
    if(canApply(originX,originY,primary,occupancy))return primary;
    if(primary==null)return null;
    Step first=forFacing(turn(primary.facing,detourSign),distance);
    if(canApply(originX,originY,first,occupancy))return first;
    Step second=forFacing(turn(primary.facing,-detourSign),distance);
    return canApply(originX,originY,second,occupancy)?second:null;
  }

  private static boolean canApply(float x,float y,Step step,Occupancy occupancy){
    return step!=null&&isCanonical(step.dx,step.dy)&&occupancy.canOccupy(x+step.dx,y+step.dy);
  }

  /** Deterministic adjacent detour; sign chooses which legal diagonal is attempted first. */
  public static CharacterRenderer.Direction turn(CharacterRenderer.Direction facing,int sign){
    if(sign>=0){
      switch(facing){
        case NW:return CharacterRenderer.Direction.NE;
        case NE:return CharacterRenderer.Direction.SE;
        case SE:return CharacterRenderer.Direction.SW;
        default:return CharacterRenderer.Direction.NW;
      }
    }
    switch(facing){
      case NW:return CharacterRenderer.Direction.SW;
      case SW:return CharacterRenderer.Direction.SE;
      case SE:return CharacterRenderer.Direction.NE;
      default:return CharacterRenderer.Direction.NW;
    }
  }

  public static boolean isCanonical(float dx,float dy){
    if(!Float.isFinite(dx)||!Float.isFinite(dy)||Math.abs(dx)<=ZERO_EPSILON
        ||Math.abs(dy)<=ZERO_EPSILON)return false;
    return Math.abs(Math.abs(dx)-2f*Math.abs(dy))<=.0005f;
  }
}
