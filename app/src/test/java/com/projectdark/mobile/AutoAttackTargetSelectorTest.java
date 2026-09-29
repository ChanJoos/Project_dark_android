package com.projectdark.mobile;

import org.junit.Test;
import java.util.Arrays;
import static org.junit.Assert.*;

public final class AutoAttackTargetSelectorTest {
  @Test public void prefersShortestReachableApproachPathThenUsesDistanceAsTieBreak(){
    RuntimeState.Monster geometricallyNear=new RuntimeState.Monster("near","Near",12f,0f,10,"B");
    RuntimeState.Monster routeNear=new RuntimeState.Monster("route","Route",80f,0f,10,"B");
    RuntimeState.Monster blocked=new RuntimeState.Monster("blocked","Blocked",4f,0f,10,"B");
    AutoAttackTargetSelector.ApproachCost costs=m->{
      if(m==geometricallyNear)return 5;
      if(m==routeNear)return 2;
      return -1;
    };
    assertSame("a farther target with a shorter legal path wins",
        routeNear,AutoAttackTargetSelector.select(Arrays.asList(geometricallyNear,routeNear),0f,0f,100f,costs));
    assertSame("unreachable geometric neighbors are ignored",
        geometricallyNear,AutoAttackTargetSelector.select(Arrays.asList(blocked,geometricallyNear),0f,0f,100f,costs));
    assertNull("targets outside the acquisition radius are ignored",
        AutoAttackTargetSelector.select(Arrays.asList(routeNear),0f,0f,40f,costs));
  }

  @Test public void usesReservedMonsterEndpointWhenChoosingMovingTarget(){
    RuntimeState.Monster monster=new RuntimeState.Monster("moving","Moving",5f,5f,10,"B");
    monster.isMoving=true;monster.moveTargetX=30f;monster.moveTargetY=15f;
    assertSame(monster,AutoAttackTargetSelector.select(Arrays.asList(monster),0f,0f,40f,m->2));
    assertNull("a moving target's stale interpolated position does not win selection",
        AutoAttackTargetSelector.select(Arrays.asList(monster),0f,0f,10f,m->2));
  }
}
