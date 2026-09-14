package com.projectdark.mobile;

/**
 * World-owned persistence gate for monster locomotion intent.
 *
 * <p>The target vector may change every frame, but once a canonical NW/NE/SW/SE segment starts,
 * its facing is retained for a short canonical 8x4 sub-segment of the 64x32 logical geometry.
 * This is long enough to prevent frame-by-frame NE/NW (or SE/SW) alternation, but short enough
 * to avoid a visible 32px side-to-side sawtooth when chasing a target directly north/south.</p>
 */
public final class MonsterCanonicalSegmentLock {
  public static final float SEGMENT_X=MonsterDiagonalLocomotion.LOGICAL_STEP_X*.25f;
  public static final float SEGMENT_Y=MonsterDiagonalLocomotion.LOGICAL_STEP_Y*.25f;
  public static final float SEGMENT_DISTANCE=(float)Math.sqrt(SEGMENT_X*SEGMENT_X+SEGMENT_Y*SEGMENT_Y);

  private CharacterRenderer.Direction facing;
  private float remaining;

  public MonsterDiagonalLocomotion.Step intent(float desiredDx,float desiredDy,float frameDistance,
      CharacterRenderer.Direction fallback){
    if(!Float.isFinite(frameDistance)||frameDistance<=MonsterDiagonalLocomotion.ZERO_EPSILON)return null;
    if(Math.abs(desiredDx)<=MonsterDiagonalLocomotion.ZERO_EPSILON&&
        Math.abs(desiredDy)<=MonsterDiagonalLocomotion.ZERO_EPSILON)return null;
    if(facing==null||remaining<=MonsterDiagonalLocomotion.ZERO_EPSILON){
      facing=CanonicalActorFacing.quantize(desiredDx,desiredDy,fallback);
      remaining=SEGMENT_DISTANCE;
    }
    return MonsterDiagonalLocomotion.forFacing(facing,Math.min(frameDistance,remaining));
  }

  /**
   * Consume one applied runtime step. If collision selected a legal detour, that actual movement
   * facing becomes the new locked segment so presentation and position never disagree.
   */
  public void onApplied(float distance,CharacterRenderer.Direction actualFacing){
    if(!Float.isFinite(distance)||distance<=0f)return;
    if(actualFacing!=null&&actualFacing!=facing){
      facing=actualFacing;
      remaining=SEGMENT_DISTANCE;
    }
    remaining=Math.max(0f,remaining-distance);
    if(remaining<=MonsterDiagonalLocomotion.ZERO_EPSILON){facing=null;remaining=0f;}
  }

  public void reset(){facing=null;remaining=0f;}
  public boolean active(){return facing!=null&&remaining>MonsterDiagonalLocomotion.ZERO_EPSILON;}
  public CharacterRenderer.Direction facing(){return facing;}
  public float remaining(){return remaining;}
}
