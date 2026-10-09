package com.projectdark.mobile;
import com.projectdark.mobile.world.WorldMoveTargetController;
import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class MonsterChasePerformanceTest {
 static long key(float x,float y){return ((long)Math.round(x)<<32)^(Math.round(y)&0xffffffffL);}
 static List<WorldMoveTargetController.TileCenter> grid(int width,int height){List<WorldMoveTargetController.TileCenter> a=new ArrayList<>();for(int y=0;y<height;y++)for(int x=0;x<width;x++)if((x+y)%2==0)a.add(new WorldMoveTargetController.TileCenter(x*32,y*16));return a;}
 static int bfs(List<WorldMoveTargetController.TileCenter> cells,float sx,float sy,float tx,float ty,Set<Long> blocked){
  Map<Long,WorldMoveTargetController.TileCenter> by=new HashMap<>();for(WorldMoveTargetController.TileCenter t:cells)by.put(key(t.x,t.y),t);
  ArrayDeque<float[]> q=new ArrayDeque<>();Set<Long> seen=new HashSet<>();q.add(new float[]{sx,sy,0});seen.add(key(sx,sy));
  while(!q.isEmpty()){float[] n=q.remove();if(n[2]>0&&CanonicalMeleeTileContract.reachable(n[0],n[1],tx,ty))return (int)n[2];
   for(WorldMoveTargetController.Direction d:WorldMoveTargetController.Direction.values()){float x=n[0]+d.dx,y=n[1]+d.dy;long k=key(x,y);if(by.containsKey(k)&&!blocked.contains(k)&&seen.add(k))q.add(new float[]{x,y,n[2]+1});}}
  return -1;
 }
 @Test public void reusedIndexKeepsShortestLiveRoutesAgainstIndependentBfs(){
  List<WorldMoveTargetController.TileCenter> cells=grid(18,18);MonsterChasePathfinder.IndexedMap map=new MonsterChasePathfinder.IndexedMap(cells);Random random=new Random(119);
  for(int trial=0;trial<200;trial++){
   WorldMoveTargetController.TileCenter start=cells.get(random.nextInt(cells.size())),target=cells.get(random.nextInt(cells.size()));
   if(start==target||CanonicalMeleeTileContract.reachable(start.x,start.y,target.x,target.y)){trial--;continue;}
   Set<Long> blocked=new HashSet<>();for(WorldMoveTargetController.TileCenter t:cells)if(random.nextFloat()<.18f)blocked.add(key(t.x,t.y));blocked.remove(key(start.x,start.y));blocked.add(key(target.x,target.y));
   int expected=bfs(cells,start.x,start.y,target.x,target.y,blocked);
   WorldMoveTargetController.Direction step=map.nextStep(start.x,start.y,target.x,target.y,(a,b)->!blocked.contains(key(b.x,b.y)));
   if(expected<0)assertNull(step);else{assertNotNull(step);assertFalse(blocked.contains(key(start.x+step.dx,start.y+step.dy)));int remaining=CanonicalMeleeTileContract.reachable(start.x+step.dx,start.y+step.dy,target.x,target.y)?0:bfs(cells,start.x+step.dx,start.y+step.dy,target.x,target.y,blocked);assertEquals(expected-1,remaining);}
  }
 }
 @Test public void nearbyChaseDoesNotRebuildOrSearchTheWholeLargeForest(){
  List<WorldMoveTargetController.TileCenter> cells=grid(152,152);MonsterChasePathfinder.IndexedMap map=new MonsterChasePathfinder.IndexedMap(cells);int[] calls={0};
  for(int i=0;i<100;i++){assertNotNull(map.nextStep(64,64,256,128,(a,b)->{calls[0]++;return true;}));assertTrue(map.expandedNodes<20);}
  assertTrue(calls[0]<10000);
  calls[0]=0;assertNull(map.nextStep(64,64,256,128,(a,b)->{calls[0]++;return false;}));assertEquals(0,map.expandedNodes);assertTrue(calls[0]<=16);
  System.out.println("V119_LARGE_FOREST_TILES="+cells.size()+";BLOCKED_RING_EDGE_CHECKS="+calls[0]);
 }
}
