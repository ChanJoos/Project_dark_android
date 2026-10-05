package com.projectdark.mobile;

import java.util.List;
import java.util.function.Predicate;

/** Stable auto-attack choice: compare legal approach path length before straight-line distance. */
public final class AutoAttackTargetSelector {
  public interface ApproachCost {
    int steps(RuntimeState.Monster monster);
  }

  private AutoAttackTargetSelector(){}

  public static RuntimeState.Monster select(List<RuntimeState.Monster> monsters,float fromX,float fromY,
      float radius,ApproachCost approachCost){
    return select(monsters,fromX,fromY,radius,approachCost,monster->true);
  }

  public static RuntimeState.Monster select(List<RuntimeState.Monster> monsters,float fromX,float fromY,
      float radius,ApproachCost approachCost,Predicate<RuntimeState.Monster> eligible){
    if(monsters==null||approachCost==null||eligible==null||!Float.isFinite(fromX)||!Float.isFinite(fromY)
        ||!Float.isFinite(radius)||radius<0f)return null;
    RuntimeState.Monster best=null;int bestSteps=Integer.MAX_VALUE;float bestDistance=Float.MAX_VALUE;
    float radiusSquared=radius*radius;
    for(RuntimeState.Monster monster:monsters){
      if(monster==null||!monster.alive||!eligible.test(monster))continue;
      float x=monster.isMoving?monster.moveTargetX:monster.x;
      float y=monster.isMoving?monster.moveTargetY:monster.y;
      float dx=x-fromX,dy=y-fromY,distance=dx*dx+dy*dy;
      if(distance>radiusSquared)continue;
      int steps=approachCost.steps(monster);
      if(steps<0)continue;
      if(steps<bestSteps||(steps==bestSteps&&distance<bestDistance)){
        best=monster;bestSteps=steps;bestDistance=distance;
      }
    }
    return best;
  }
}
