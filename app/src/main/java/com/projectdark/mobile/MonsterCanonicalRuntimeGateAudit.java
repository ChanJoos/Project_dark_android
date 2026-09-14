package com.projectdark.mobile;

import com.projectdark.mobile.world.IsometricTileMovementAudit;
import com.projectdark.mobile.world.MillesProductionCollisionAudit;

/** Composite World release gate for canonical monster locomotion + attack-facing stability. */
public final class MonsterCanonicalRuntimeGateAudit {
  private static final float EPS=.0015f;
  private MonsterCanonicalRuntimeGateAudit(){}

  public static boolean passes(){
    if(!MonsterDiagonalLocomotionAudit.passes())return false;
    if(!MonsterCanonicalTrajectoryAudit.passes())return false;
    if(!CanonicalActorFacingAudit.passes())return false;
    if(!IsometricTileMovementAudit.verify())return false;
    if(!MillesProductionCollisionAudit.verify())return false;

    // Critical regression: a target exactly north must not produce per-frame NE/NW alternation.
    MonsterCanonicalSegmentLock lock=new MonsterCanonicalSegmentLock();
    float mx=0f,my=0f,targetX=0f,targetY=-200f,frameDistance=.5f;
    CharacterRenderer.Direction first=null;
    float travelled=0f;
    while(travelled+EPS<MonsterCanonicalSegmentLock.SEGMENT_DISTANCE){
      float dx=targetX-mx,dy=targetY-my;
      MonsterDiagonalLocomotion.Step intent=lock.intent(dx,dy,frameDistance,CharacterRenderer.Direction.SE);
      if(intent==null||!MonsterDiagonalLocomotion.isCanonical(intent.dx,intent.dy))return false;
      if(first==null)first=intent.facing;
      if(intent.facing!=first)return false;
      float applied=(float)Math.sqrt(intent.dx*intent.dx+intent.dy*intent.dy);
      mx+=intent.dx;my+=intent.dy;travelled+=applied;
      lock.onApplied(applied,intent.facing);
    }
    if(first!=CharacterRenderer.Direction.NE)return false;
    if(Math.abs(mx)>MonsterCanonicalSegmentLock.SEGMENT_X+EPS)return false;

    // A legal collision detour becomes the new lock; no fifth/cardinal direction is emitted.
    lock.reset();
    MonsterDiagonalLocomotion.Step northIntent=lock.intent(0f,-100f,1f,CharacterRenderer.Direction.SE);
    if(northIntent==null||northIntent.facing!=CharacterRenderer.Direction.NE)return false;
    lock.onApplied(1f,CharacterRenderer.Direction.NW);
    MonsterDiagonalLocomotion.Step afterDetour=lock.intent(100f,-100f,1f,CharacterRenderer.Direction.SE);
    if(afterDetour==null||afterDetour.facing!=CharacterRenderer.Direction.NW||
        !MonsterDiagonalLocomotion.isCanonical(afterDetour.dx,afterDetour.dy))return false;

    // Attack facing snapshots canonical target delta and remains locked despite locomotion change.
    CanonicalActorFacing facing=new CanonicalActorFacing(CharacterRenderer.Direction.SE);
    CharacterRenderer.Direction expected=CanonicalActorFacing.quantize(-4f,-2f,CharacterRenderer.Direction.SE);
    facing.beginAttack(-4f,-2f);
    if(!facing.attackLocked()||facing.attack()!=expected||facing.presentation()!=expected)return false;
    facing.setLocomotion(CharacterRenderer.Direction.SE);
    if(facing.presentation()!=expected)return false;
    facing.endAttack();
    if(facing.attackLocked())return false;

    return true;
  }

  public static void main(String[] args){
    if(!passes())throw new AssertionError("MonsterCanonicalRuntimeGateAudit failed");
    System.out.println("MonsterCanonicalRuntimeGateAudit PASS: long-run trajectory, four-diagonal motion, collision, facing, attack lock, player/world regressions");
  }
}
