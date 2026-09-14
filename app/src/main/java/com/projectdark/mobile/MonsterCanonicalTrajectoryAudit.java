package com.projectdark.mobile;

/**
 * Long-run executable evidence for the device-reported composite-cardinal monster trajectory bug.
 * This audit deliberately exercises 4096+ ticks rather than only validating individual vectors.
 */
public final class MonsterCanonicalTrajectoryAudit {
  private static final int LONG_TICKS=4096;
  private static final float FRAME_DISTANCE=.5f;
  private static final float EPS=.002f;
  // Mirrors the current [B] monster attack-begin/arrival threshold without depending on controller wiring.
  private static final float SETTLE_DISTANCE=42f;

  public static final class Metrics {
    public final String name;
    public int ticks,moves,directionChanges,blocked;
    public float maxLateralError,maxStepMagnitude;
    public float finalX,finalY;
    Metrics(String name){this.name=name;}
    public String summary(){
      return name+" ticks="+ticks+" moves="+moves+" dirChanges="+directionChanges+
          " blocked="+blocked+" maxLateral="+maxLateralError+" maxStep="+maxStepMagnitude+
          " final=("+finalX+","+finalY+")";
    }
  }

  private MonsterCanonicalTrajectoryAudit(){}

  public static boolean passes(){
    if(Math.abs(MonsterCanonicalSegmentLock.SEGMENT_X-8f)>EPS||
        Math.abs(MonsterCanonicalSegmentLock.SEGMENT_Y-4f)>EPS)return false;

    Metrics north=runFixed("north",0f,-100000f,LONG_TICKS,false);
    Metrics south=runFixed("south",0f,100000f,LONG_TICKS,false);
    Metrics nearNorth=runFixed("near-axis-north",1f,-100000f,LONG_TICKS,false);
    Metrics nearSouth=runFixed("near-axis-south",-1f,100000f,LONG_TICKS,false);
    Metrics moving=runMovingTarget();
    Metrics collision=runCollisionDetour();
    Metrics arrival=runArrivalSettle();

    float lateralLimit=MonsterCanonicalSegmentLock.SEGMENT_X+1.1f;
    if(north.maxLateralError>lateralLimit||south.maxLateralError>lateralLimit)return false;
    if(nearNorth.maxLateralError>lateralLimit+1f||nearSouth.maxLateralError>lateralLimit+1f)return false;
    if(north.moves!=LONG_TICKS||south.moves!=LONG_TICKS)return false;
    if(north.maxStepMagnitude>FRAME_DISTANCE+EPS||south.maxStepMagnitude>FRAME_DISTANCE+EPS)return false;
    if(moving.moves<3000||moving.maxStepMagnitude>FRAME_DISTANCE+EPS)return false;
    if(collision.blocked<=0||collision.moves<=0||collision.maxStepMagnitude>FRAME_DISTANCE+EPS)return false;
    if(arrival.moves!=0||Math.abs(arrival.finalX)>EPS||Math.abs(arrival.finalY)>EPS)return false;
    return true;
  }

  private static Metrics runFixed(String name,float targetX,float targetY,int ticks,boolean collision){
    MonsterCanonicalSegmentLock lock=new MonsterCanonicalSegmentLock();
    Metrics m=new Metrics(name);
    float x=0f,y=0f;
    CharacterRenderer.Direction previous=null;
    for(int i=0;i<ticks;i++){
      float dx=targetX-x,dy=targetY-y;
      float distance=hypot(dx,dy);
      if(distance<=SETTLE_DISTANCE){lock.reset();m.ticks++;continue;}
      boolean wasActive=lock.active();
      CharacterRenderer.Direction lockedBefore=lock.facing();
      MonsterDiagonalLocomotion.Step intent=lock.intent(dx,dy,FRAME_DISTANCE,
          previous==null?CharacterRenderer.Direction.SE:previous);
      if(intent==null||!MonsterDiagonalLocomotion.isCanonical(intent.dx,intent.dy))return fail(name);
      if(wasActive&&intent.facing!=lockedBefore)return fail(name);
      float stepDistance=hypot(intent.dx,intent.dy);
      MonsterDiagonalLocomotion.Step applied=MonsterDiagonalLocomotion.select(
          x,y,intent.dx,intent.dy,stepDistance,intent.facing,1,
          (nx,ny)->!collision||!(nx>3f&&nx<6f&&ny<0f));
      if(applied==null){lock.reset();m.blocked++;m.ticks++;continue;}
      if(!MonsterDiagonalLocomotion.isCanonical(applied.dx,applied.dy))return fail(name);
      if(applied.facing!=CanonicalActorFacing.quantize(applied.dx,applied.dy,null))return fail(name);
      if(previous!=null&&previous!=applied.facing)m.directionChanges++;
      previous=applied.facing;
      x+=applied.dx;y+=applied.dy;
      lock.onApplied(hypot(applied.dx,applied.dy),applied.facing);
      m.moves++;m.ticks++;
      m.maxStepMagnitude=Math.max(m.maxStepMagnitude,hypot(applied.dx,applied.dy));
      m.maxLateralError=Math.max(m.maxLateralError,Math.abs(x-targetX));
    }
    m.finalX=x;m.finalY=y;return m;
  }

  private static Metrics runMovingTarget(){
    MonsterCanonicalSegmentLock lock=new MonsterCanonicalSegmentLock();
    Metrics m=new Metrics("moving-target");
    float x=0f,y=0f;
    CharacterRenderer.Direction previous=null;
    for(int i=0;i<LONG_TICKS;i++){
      float targetX=((i/256)&1)==0?24f:-24f;
      float targetY=-100000f+i*.02f;
      float dx=targetX-x,dy=targetY-y;
      boolean wasActive=lock.active();CharacterRenderer.Direction before=lock.facing();
      MonsterDiagonalLocomotion.Step intent=lock.intent(dx,dy,FRAME_DISTANCE,
          previous==null?CharacterRenderer.Direction.SE:previous);
      if(intent==null||!MonsterDiagonalLocomotion.isCanonical(intent.dx,intent.dy))return fail("moving-target");
      if(wasActive&&intent.facing!=before)return fail("moving-target");
      float distance=hypot(intent.dx,intent.dy);
      MonsterDiagonalLocomotion.Step applied=MonsterDiagonalLocomotion.select(
          x,y,intent.dx,intent.dy,distance,intent.facing,1,(nx,ny)->true);
      if(applied==null||!MonsterDiagonalLocomotion.isCanonical(applied.dx,applied.dy))return fail("moving-target");
      if(previous!=null&&previous!=applied.facing)m.directionChanges++;
      previous=applied.facing;x+=applied.dx;y+=applied.dy;
      lock.onApplied(hypot(applied.dx,applied.dy),applied.facing);
      m.moves++;m.ticks++;m.maxStepMagnitude=Math.max(m.maxStepMagnitude,hypot(applied.dx,applied.dy));
      m.maxLateralError=Math.max(m.maxLateralError,Math.abs(x-targetX));
    }
    m.finalX=x;m.finalY=y;return m;
  }

  private static Metrics runCollisionDetour(){
    return runFixed("collision-detour",0f,-100000f,LONG_TICKS,true);
  }

  private static Metrics runArrivalSettle(){
    MonsterCanonicalSegmentLock lock=new MonsterCanonicalSegmentLock();
    Metrics m=new Metrics("arrival-settle");
    float x=0f,y=0f,targetX=5f,targetY=-5f;
    for(int i=0;i<512;i++){
      float dx=targetX-x,dy=targetY-y;
      if(hypot(dx,dy)<=SETTLE_DISTANCE){lock.reset();m.ticks++;continue;}
      MonsterDiagonalLocomotion.Step intent=lock.intent(dx,dy,FRAME_DISTANCE,CharacterRenderer.Direction.SE);
      if(intent==null)return fail("arrival-settle");
      x+=intent.dx;y+=intent.dy;lock.onApplied(hypot(intent.dx,intent.dy),intent.facing);m.moves++;m.ticks++;
    }
    m.finalX=x;m.finalY=y;return m;
  }

  private static Metrics fail(String name){Metrics m=new Metrics(name);m.maxStepMagnitude=Float.POSITIVE_INFINITY;return m;}
  private static float hypot(float x,float y){return(float)Math.sqrt(x*x+y*y);}

  public static void main(String[] args){
    Metrics north=runFixed("north",0f,-100000f,LONG_TICKS,false);
    Metrics south=runFixed("south",0f,100000f,LONG_TICKS,false);
    Metrics nearNorth=runFixed("near-axis-north",1f,-100000f,LONG_TICKS,false);
    Metrics nearSouth=runFixed("near-axis-south",-1f,100000f,LONG_TICKS,false);
    Metrics moving=runMovingTarget();
    Metrics collision=runCollisionDetour();
    Metrics arrival=runArrivalSettle();
    System.out.println(north.summary());
    System.out.println(south.summary());
    System.out.println(nearNorth.summary());
    System.out.println(nearSouth.summary());
    System.out.println(moving.summary());
    System.out.println(collision.summary());
    System.out.println(arrival.summary());
    if(!passes())throw new AssertionError("MonsterCanonicalTrajectoryAudit failed");
    System.out.println("MonsterCanonicalTrajectoryAudit PASS");
  }
}
