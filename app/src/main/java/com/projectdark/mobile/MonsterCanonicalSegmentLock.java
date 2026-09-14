package com.projectdark.mobile;

/**
 * World-owned persistence gate for monster locomotion intent.
 *
 * <p>Device validation of the former 8x4 quarter-stride showed visible high-frequency zig-zag.
 * The lock therefore commits to a half-stride 16x8 visual run before direction may be re-quantized.
 * Every applied micro-step still remains an equal-speed canonical NW/NE/SW/SE diagonal; this class
 * changes only how long one visual facing is held.</p>
 */
public final class MonsterCanonicalSegmentLock {
  public static final float SEGMENT_X=MonsterDiagonalLocomotion.LOGICAL_STEP_X*.50f;
  public static final float SEGMENT_Y=MonsterDiagonalLocomotion.LOGICAL_STEP_Y*.50f;
  public static final float SEGMENT_DISTANCE=(float)Math.sqrt(SEGMENT_X*SEGMENT_X+SEGMENT_Y*SEGMENT_Y);

  private CharacterRenderer.Direction facing;
  private float remaining;
  private float committed;

  public MonsterDiagonalLocomotion.Step intent(float desiredDx,float desiredDy,float frameDistance,
      CharacterRenderer.Direction fallback){
    if(!Float.isFinite(frameDistance)||frameDistance<=MonsterDiagonalLocomotion.ZERO_EPSILON)return null;
    if(Math.abs(desiredDx)<=MonsterDiagonalLocomotion.ZERO_EPSILON&&
        Math.abs(desiredDy)<=MonsterDiagonalLocomotion.ZERO_EPSILON)return null;
    if(facing==null||remaining<=MonsterDiagonalLocomotion.ZERO_EPSILON){
      facing=CanonicalActorFacing.quantize(desiredDx,desiredDy,fallback);
      remaining=SEGMENT_DISTANCE;
      committed=0f;
    }
    return MonsterDiagonalLocomotion.forFacing(facing,Math.min(frameDistance,remaining));
  }

  /**
   * Consume one applied runtime step. Collision may select a legal detour, but a detour begins a
   * fresh full visual run instead of inheriting a nearly-expired segment. This prevents immediate
   * direction flapping after collision while keeping collision selection canonical and atomic.
   */
  public void onApplied(float distance,CharacterRenderer.Direction actualFacing){
    if(!Float.isFinite(distance)||distance<=0f)return;
    if(actualFacing!=null&&actualFacing!=facing){
      facing=actualFacing;
      remaining=SEGMENT_DISTANCE;
      committed=0f;
    }
    committed+=distance;
    remaining=Math.max(0f,remaining-distance);
    if(remaining<=MonsterDiagonalLocomotion.ZERO_EPSILON){facing=null;remaining=0f;}
  }

  public void reset(){facing=null;remaining=0f;committed=0f;}
  public boolean active(){return facing!=null&&remaining>MonsterDiagonalLocomotion.ZERO_EPSILON;}
  public CharacterRenderer.Direction facing(){return facing;}
  public float remaining(){return remaining;}
  public float committed(){return committed;}
}
