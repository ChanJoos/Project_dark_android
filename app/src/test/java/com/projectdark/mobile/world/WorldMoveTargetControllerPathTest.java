package com.projectdark.mobile.world;

import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

public final class WorldMoveTargetControllerPathTest {
  @Test public void pathPlannerAvoidsActorBlockedEdgeAndLiveStepsFollowSameDetour(){
    WorldMoveTargetController.TileCenter start=tile(0,0);
    WorldMoveTargetController.TileCenter blockedShortcut=tile(1,-1);
    WorldMoveTargetController.TileCenter detour=tile(1,1);
    WorldMoveTargetController.TileCenter middle=tile(2,0);
    WorldMoveTargetController.TileCenter goal=tile(3,-1);
    List<WorldMoveTargetController.TileCenter> tiles=
        Arrays.asList(start,blockedShortcut,detour,middle,goal);
    World blockedWorld=new World(tiles,start,blockedShortcut);
    Walker walker=new Walker(blockedWorld,start);
    WorldMoveTargetController movement=new WorldMoveTargetController(blockedWorld,walker);

    WorldMoveTargetController.Snapshot request=movement.requestMonsterApproach("target",128f,0f,40f);
    assertEquals(WorldMoveTargetController.Status.MOVING,request.status);
    assertEquals("the planner picks the only stable detour to a legal melee tile",goal.x,request.targetX,.01f);
    assertEquals(goal.y,request.targetY,.01f);

    movement.tick(WorldMoveTargetController.TILE_STEP_SECONDS);
    assertEquals("live movement follows the same edge policy as planning",detour.x,walker.x,.01f);
    assertEquals(detour.y,walker.y,.01f);
  }

  private static WorldMoveTargetController.TileCenter tile(int u,int v){
    return new WorldMoveTargetController.TileCenter(u*32f,v*16f);
  }

  private static final class World implements WorldMoveTargetController.NavigationWorld {
    final List<WorldMoveTargetController.TileCenter> tiles;
    final WorldMoveTargetController.TileCenter start,blocked;
    World(List<WorldMoveTargetController.TileCenter> tiles,
        WorldMoveTargetController.TileCenter start,WorldMoveTargetController.TileCenter blocked){
      this.tiles=tiles;this.start=start;this.blocked=blocked;
    }
    public List<WorldMoveTargetController.TileCenter> navigationTiles(){return tiles;}
    public boolean canPlayerOccupy(float x,float y){return true;}
    @Override public boolean canPlayerTraverse(float fromX,float fromY,float toX,float toY){
      return !(same(fromX,fromY,start)&&same(toX,toY,blocked));
    }
  }

  private static final class Walker implements WorldMoveTargetController.Walker {
    final World world;float x,y;
    Walker(World world,WorldMoveTargetController.TileCenter start){this.world=world;x=start.x;y=start.y;}
    public float worldX(){return x;}
    public float worldY(){return y;}
    public boolean moveToAdjacentTile(float destinationX,float destinationY,
        WorldMoveTargetController.Direction direction){
      if(WorldMoveTargetController.Direction.between(x,y,destinationX,destinationY)!=direction
          ||!world.canPlayerTraverse(x,y,destinationX,destinationY))return false;
      x=destinationX;y=destinationY;return true;
    }
  }

  private static boolean same(float x,float y,WorldMoveTargetController.TileCenter tile){
    return Math.abs(x-tile.x)<.01f&&Math.abs(y-tile.y)<.01f;
  }
}
