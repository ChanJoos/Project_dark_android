package com.projectdark.mobile;

/** Focused deterministic gate for canonical four-diagonal monster world movement. */
public final class MonsterDiagonalLocomotionAudit {
  private static final float EPS=.0015f;
  private MonsterDiagonalLocomotionAudit(){}

  public static boolean passes(){
    if(MonsterDiagonalLocomotion.LOGICAL_STEP_X!=32f
        ||MonsterDiagonalLocomotion.LOGICAL_STEP_Y!=16f)return false;
    CharacterRenderer.Direction[] directions=CharacterRenderer.Direction.values();
    if(directions.length!=4)return false;

    float expectedMagnitude=-1f;
    for(CharacterRenderer.Direction direction:directions){
      MonsterDiagonalLocomotion.Step step=MonsterDiagonalLocomotion.forFacing(direction,7f);
      if(step==null||step.facing!=direction||!MonsterDiagonalLocomotion.isCanonical(step.dx,step.dy))return false;
      float magnitude=hypot(step.dx,step.dy);
      if(expectedMagnitude<0f)expectedMagnitude=magnitude;
      else if(Math.abs(magnitude-expectedMagnitude)>EPS)return false;
      if(Math.abs(magnitude-7f)>EPS)return false;
      if(CanonicalActorFacing.quantize(step.dx,step.dy,null)!=direction)return false;
    }

    float[] samples={-100f,-1f,-.0002f,0f,.0002f,1f,100f};
    for(float dx:samples)for(float dy:samples){
      MonsterDiagonalLocomotion.Step step=MonsterDiagonalLocomotion.toward(
          dx,dy,3f,CharacterRenderer.Direction.SE);
      if(Math.abs(dx)<=MonsterDiagonalLocomotion.ZERO_EPSILON
          &&Math.abs(dy)<=MonsterDiagonalLocomotion.ZERO_EPSILON){if(step!=null)return false;continue;}
      if(step==null||!MonsterDiagonalLocomotion.isCanonical(step.dx,step.dy))return false;
      if(step.facing!=CanonicalActorFacing.quantize(dx,dy,CharacterRenderer.Direction.SE))return false;
    }

    // Long opposing runs must return without accumulating a free-angle component.
    float x=0f,y=0f;
    MonsterDiagonalLocomotion.Step ne=MonsterDiagonalLocomotion.forFacing(CharacterRenderer.Direction.NE,1f);
    MonsterDiagonalLocomotion.Step sw=MonsterDiagonalLocomotion.forFacing(CharacterRenderer.Direction.SW,1f);
    for(int i=0;i<4096;i++){x+=ne.dx;y+=ne.dy;}
    for(int i=0;i<4096;i++){x+=sw.dx;y+=sw.dy;}
    if(Math.abs(x)>EPS||Math.abs(y)>EPS)return false;

    float actorX=600f,actorY=400f;
    for(CharacterRenderer.Direction direction:directions){
      MonsterDiagonalLocomotion.Step request=MonsterDiagonalLocomotion.forFacing(direction,5f);
      MonsterDiagonalLocomotion.Step applied=MonsterDiagonalLocomotion.select(
          actorX,actorY,request.dx,request.dy,5f,direction,1,(x1,y1)->true);
      if(applied==null||!MonsterDiagonalLocomotion.isCanonical(applied.dx,applied.dy))return false;
      if(Math.abs(hypot(applied.dx,applied.dy)-5f)>EPS)return false;
      if(applied.facing!=CanonicalActorFacing.quantize(applied.dx,applied.dy,null))return false;
      actorX+=applied.dx;actorY+=applied.dy;
    }
    if(MonsterDiagonalLocomotion.select(actorX,actorY,0f,0f,5f,
        CharacterRenderer.Direction.SE,1,(x1,y1)->true)!=null)return false;
    if(MonsterDiagonalLocomotion.select(actorX,actorY,1f,1f,
        MonsterDiagonalLocomotion.ZERO_EPSILON*.5f,
        CharacterRenderer.Direction.SE,1,(x1,y1)->true)!=null)return false;

    // Runtime's shared selector must reject a blocked segment atomically, never return one axis.
    MonsterDiagonalLocomotion.Step blocked=MonsterDiagonalLocomotion.select(
        10f,10f,1f,1f,2f,CharacterRenderer.Direction.SE,1,(x1,y1)->false);
    if(blocked!=null)return false;

    // If the direct route is blocked, the selected detour remains one of the four legal diagonals.
    MonsterDiagonalLocomotion.Step detour=MonsterDiagonalLocomotion.select(
        10f,10f,1f,1f,2f,CharacterRenderer.Direction.SE,1,
        (x1,y1)->y1<10f);
    if(detour==null||detour.facing!=CharacterRenderer.Direction.NE
        ||!MonsterDiagonalLocomotion.isCanonical(detour.dx,detour.dy))return false;

    return true;
  }

  private static float hypot(float x,float y){return(float)Math.sqrt(x*x+y*y);}

  public static void main(String[] args){
    if(!passes())throw new AssertionError("MonsterDiagonalLocomotionAudit failed");
    System.out.println("MonsterDiagonalLocomotionAudit PASS: four 2:1 diagonals, equal speed, atomic collision, zero drift");
  }
}
