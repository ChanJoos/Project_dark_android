package com.projectdark.mobile.world;

/**
 * World-owned projection policy for monster locomotion on the 64x32 isometric plane.
 *
 * The runtime may interpolate continuously, but every non-zero movement delta must lie on
 * one of the four legal NW/NE/SW/SE axes. For the 64x32 projection that means |dx|:|dy| = 2:1.
 */
public final class MonsterIsometricMovementPolicy {
  private static final float INV_SQRT_5 = (float)(1.0 / Math.sqrt(5.0));

  public static final class Step {
    public final float dx,dy;
    Step(float dx,float dy){this.dx=dx;this.dy=dy;}
    public boolean isZero(){return dx==0f&&dy==0f;}
  }

  private MonsterIsometricMovementPolicy(){}

  /**
   * Quantize an arbitrary pursuit request onto a legal isometric diagonal while preserving
   * the requested Euclidean step magnitude. Exact axis ties use deterministic positive fallback
   * for the missing sign so no cardinal movement is ever emitted.
   */
  public static Step project(float requestedDx,float requestedDy){
    float magnitude=(float)Math.sqrt(requestedDx*requestedDx+requestedDy*requestedDy);
    if(magnitude<=0f)return new Step(0f,0f);
    float sx=requestedDx<0f?-1f:1f;
    float sy=requestedDy<0f?-1f:1f;
    return new Step(sx*magnitude*2f*INV_SQRT_5,sy*magnitude*INV_SQRT_5);
  }

  /** Alternate legal diagonal by flipping the horizontal sign only. */
  public static Step detourX(Step primary){
    return primary==null?new Step(0f,0f):new Step(-primary.dx,primary.dy);
  }

  /** Alternate legal diagonal by flipping the vertical sign only. */
  public static Step detourY(Step primary){
    return primary==null?new Step(0f,0f):new Step(primary.dx,-primary.dy);
  }

  public static boolean isLegalDiagonal(float dx,float dy){
    if(dx==0f&&dy==0f)return true;
    if(dx==0f||dy==0f)return false;
    return Math.abs(Math.abs(dx)-2f*Math.abs(dy))<=0.0005f;
  }
}
