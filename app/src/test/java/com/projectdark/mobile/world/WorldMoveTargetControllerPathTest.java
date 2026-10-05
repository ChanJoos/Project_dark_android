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

  @Test public void unreachableNpcCandidatesShareOneSearchInsteadOfRepeatingFullMapWork(){
    java.util.ArrayList<WorldMoveTargetController.TileCenter> cells=new java.util.ArrayList<>();
    for(int x=0;x<80;x++)for(int y=0;y<80;y++)if((x+y)%2==0)cells.add(tile(x,y));
    final int[] occupancyCalls={0};
    WorldMoveTargetController.NavigationWorld world=new WorldMoveTargetController.NavigationWorld(){
      public List<WorldMoveTargetController.TileCenter> navigationTiles(){return cells;}
      public boolean canPlayerOccupy(float x,float y){occupancyCalls[0]++;return x!=40*32f;}
      public boolean canPlayerTraverse(float ax,float ay,float bx,float by){return bx!=40*32f;}
    };
    WorldMoveTargetController.Walker walker=new WorldMoveTargetController.Walker(){
      public float worldX(){return 0;}public float worldY(){return 0;}
      public boolean moveToAdjacentTile(float x,float y,WorldMoveTargetController.Direction d){return true;}
    };
    WorldMoveTargetController movement=new WorldMoveTargetController(world,walker);
    assertEquals(WorldMoveTargetController.Status.BLOCKED,movement.requestNpcApproach("npc",70*32f,40*16f,80f).status);
    assertTrue("bounded collision work: "+occupancyCalls[0],occupancyCalls[0]<cells.size()*2);
  }

  @Test public void equalLengthNpcGoalsPreferCloserEntityAndCanAlreadyBeReached(){
    List<WorldMoveTargetController.TileCenter> cells=Arrays.asList(tile(0,0),tile(1,-1),tile(1,1),tile(2,0));
    World world=new World(cells,tile(-9,-9),tile(-9,-9));Walker walker=new Walker(world,tile(0,0));
    WorldMoveTargetController movement=new WorldMoveTargetController(world,walker);
    WorldMoveTargetController.Snapshot result=movement.requestNpcApproach("npc",64,8,40);
    assertEquals(32,result.targetX,0);assertEquals(16,result.targetY,0);assertEquals(1,result.remainingWaypoints);
    movement.tick(.6f);
    assertEquals(WorldMoveTargetController.Status.REACHED,movement.requestNpcApproach("npc",64,8,40).status);
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
