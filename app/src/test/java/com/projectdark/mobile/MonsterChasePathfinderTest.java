package com.projectdark.mobile;

import com.projectdark.mobile.world.WorldMoveTargetController;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.junit.Test;
import static org.junit.Assert.*;

public final class MonsterChasePathfinderTest {
  @Test public void blockedDirectApproachTakesStableShortestDetourInsteadOfAlternatingSides(){
    WorldMoveTargetController.TileCenter start=tile(0,0);
    WorldMoveTargetController.TileCenter blocked=tile(1,-1);
    WorldMoveTargetController.TileCenter via=tile(1,1);
    WorldMoveTargetController.TileCenter middle=tile(2,0);
    WorldMoveTargetController.TileCenter goal=tile(3,-1);
    WorldMoveTargetController.TileCenter player=tile(4,0);
    Set<String> allowed=new HashSet<>(Arrays.asList(key(start),key(via),key(middle),key(goal)));
    WorldMoveTargetController.Direction[] expected={
        WorldMoveTargetController.Direction.SE,
        WorldMoveTargetController.Direction.NE,
        WorldMoveTargetController.Direction.NE};
    WorldMoveTargetController.TileCenter current=start;
    for(WorldMoveTargetController.Direction direction:expected){
      WorldMoveTargetController.Direction next=MonsterChasePathfinder.nextStep(
          Arrays.asList(start,blocked,via,middle,goal),current.x,current.y,player.x,player.y,
          (from,to)->allowed.contains(key(to)));
      assertEquals("replanning at every tile must keep following the same shortest route",direction,next);
      current=allowedTile(current,direction,Arrays.asList(start,blocked,via,middle,goal));
    }
    assertEquals("the route ends at the melee-adjacent goal",key(goal),key(current));
  }

  @Test public void noPathReturnsNoStepRatherThanInventingAClockwiseOrCounterclockwiseDetour(){
    WorldMoveTargetController.TileCenter start=tile(0,0),target=tile(4,0);
    assertNull(MonsterChasePathfinder.nextStep(Arrays.asList(start,tile(1,-1),target),
        start.x,start.y,target.x,target.y,(from,to)->false));
  }

  private static WorldMoveTargetController.TileCenter allowedTile(
      WorldMoveTargetController.TileCenter from,WorldMoveTargetController.Direction direction,
      java.util.List<WorldMoveTargetController.TileCenter> tiles){
    float x=from.x+direction.dx,y=from.y+direction.dy;
    WorldMoveTargetController.TileCenter result=tiles.stream()
        .filter(t->Math.abs(t.x-x)<.01f&&Math.abs(t.y-y)<.01f).findFirst().orElse(null);
    assertNotNull(result);return result;
  }
  private static WorldMoveTargetController.TileCenter tile(int u,int v){return new WorldMoveTargetController.TileCenter(u*32f,v*16f);}
  private static String key(WorldMoveTargetController.TileCenter tile){return Math.round(tile.x*100f)+":"+Math.round(tile.y*100f);}
}
