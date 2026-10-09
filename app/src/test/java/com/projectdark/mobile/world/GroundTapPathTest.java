package com.projectdark.mobile.world;
import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;
/** Independent exhaustive destination and BFS route oracles, including live blockers. */
public class GroundTapPathTest {
 static class Grid implements WorldMoveTargetController.NavigationWorld,WorldMoveTargetController.Walker {
  final List<WorldMoveTargetController.TileCenter> tiles=new ArrayList<>();final Set<String> blocked=new HashSet<>();
  float x,y;int occupancy,edges;
  Grid(int width,int height){for(int row=0;row<height;row++)for(int col=0;col<width;col++)if((col+row)%2==0)tiles.add(new WorldMoveTargetController.TileCenter(col*32,row*16));}
  static String key(float x,float y){return x+":"+y;}
  public List<WorldMoveTargetController.TileCenter> navigationTiles(){return tiles;}
  public boolean canPlayerOccupy(float x,float y){occupancy++;return !blocked.contains(key(x,y));}
  public boolean canPlayerTraverse(float ax,float ay,float bx,float by){edges++;return !blocked.contains(key(bx,by));}
  public float worldX(){return x;}public float worldY(){return y;}
  public boolean moveToAdjacentTile(float nx,float ny,WorldMoveTargetController.Direction d){if(!canPlayerTraverse(x,y,nx,ny))return false;x=nx;y=ny;return true;}
  WorldMoveTargetController.TileCenter oracleGoal(float tx,float ty){WorldMoveTargetController.TileCenter best=null;float bd=Float.MAX_VALUE;for(WorldMoveTargetController.TileCenter t:tiles){float dx=t.x-tx,dy=t.y-ty,d=dx*dx+dy*dy;if(!blocked.contains(key(t.x,t.y))&&d<bd){bd=d;best=t;}}return best;}
  int bfs(WorldMoveTargetController.TileCenter goal){Map<String,WorldMoveTargetController.TileCenter> map=new HashMap<>();for(WorldMoveTargetController.TileCenter t:tiles)map.put(key(t.x,t.y),t);ArrayDeque<WorldMoveTargetController.TileCenter> q=new ArrayDeque<>();Map<String,Integer> dist=new HashMap<>();q.add(map.get(key(x,y)));dist.put(key(x,y),0);while(!q.isEmpty()){WorldMoveTargetController.TileCenter t=q.remove();int d=dist.get(key(t.x,t.y));if(t==goal)return d;for(WorldMoveTargetController.Direction dir:WorldMoveTargetController.Direction.values()){String k=key(t.x+dir.dx,t.y+dir.dy);if(map.containsKey(k)&&!dist.containsKey(k)&&!blocked.contains(k)){dist.put(k,d+1);q.add(map.get(k));}}}return -1;}
 }
 @Test public void largeMapGroundTapDoesNotScanEveryActorForEveryTile(){Grid g=new Grid(150,150);WorldMoveTargetController c=new WorldMoveTargetController(g,g);WorldMoveTargetController.Snapshot s=c.requestGroundMove(3200,0);assertEquals(100,s.remainingWaypoints);assertTrue("occupancy calls="+g.occupancy,g.occupancy<500);assertTrue("edge calls="+g.edges,g.edges<500);}
 @Test public void destinationsAndShortestRoutesMatchIndependentOracles(){Random random=new Random(117);for(int scene=0;scene<20;scene++){Grid g=new Grid(22,22);for(WorldMoveTargetController.TileCenter t:g.tiles)if((t.x!=0||t.y!=0)&&random.nextFloat()<.2)g.blocked.add(Grid.key(t.x,t.y));WorldMoveTargetController c=new WorldMoveTargetController(g,g);for(int tap=0;tap<15;tap++){float x=random.nextInt(21*32),y=random.nextInt(21*16);WorldMoveTargetController.TileCenter goal=g.oracleGoal(x,y);int distance=g.bfs(goal);WorldMoveTargetController.Snapshot s=c.requestGroundMove(x,y);assertEquals(goal.x,s.targetX,0);assertEquals(goal.y,s.targetY,0);assertEquals(distance<0?0:distance,s.remainingWaypoints);assertEquals(distance<0?WorldMoveTargetController.Status.BLOCKED:distance==0?WorldMoveTargetController.Status.REACHED:WorldMoveTargetController.Status.MOVING,s.status);}}}
 @Test public void requestCachesDoNotSurviveActorMovementAndLiveStepsRevalidate(){Grid g=new Grid(12,12);WorldMoveTargetController c=new WorldMoveTargetController(g,g);assertEquals(WorldMoveTargetController.Status.MOVING,c.requestGroundMove(32,16).status);g.blocked.add(Grid.key(32,16));assertEquals(WorldMoveTargetController.Status.BLOCKED,c.tick(.6f).status);WorldMoveTargetController.Snapshot second=c.requestGroundMove(32,16);assertFalse(second.targetX==32&&second.targetY==16);g.blocked.clear();assertEquals(32,c.requestGroundMove(32,16).targetX,0);}
}
