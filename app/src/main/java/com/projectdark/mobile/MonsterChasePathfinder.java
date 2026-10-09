package com.projectdark.mobile;

import com.projectdark.mobile.world.WorldMoveTargetController;
import java.util.*;

/** Shortest live-legal chase route; immutable map topology is indexed once per map. */
public final class MonsterChasePathfinder {
  public interface EdgePolicy {
    boolean canTraverse(WorldMoveTargetController.TileCenter from,WorldMoveTargetController.TileCenter to);
  }
  private static final WorldMoveTargetController.Direction[] DIRECTIONS=WorldMoveTargetController.Direction.values();
  private static final class Node {
    final int tile,g;final float f;final WorldMoveTargetController.Direction first;
    Node(int tile,int g,float f,WorldMoveTargetController.Direction first){this.tile=tile;this.g=g;this.f=f;this.first=first;}
  }
  public static final class IndexedMap {
    private final List<WorldMoveTargetController.TileCenter> tiles;
    private final Map<Long,Integer> centers=new HashMap<>();
    private final int[][] neighbors;
    private final int[] seen,best,closed;
    private int generation;
    int expandedNodes;
    public IndexedMap(List<WorldMoveTargetController.TileCenter> source){
      tiles=new ArrayList<>();if(source!=null)for(WorldMoveTargetController.TileCenter t:source)if(t!=null)tiles.add(t);
      for(int i=0;i<tiles.size();i++){WorldMoveTargetController.TileCenter t=tiles.get(i);centers.put(key(t.x,t.y),i);}
      neighbors=new int[tiles.size()][4];seen=new int[tiles.size()];best=new int[tiles.size()];closed=new int[tiles.size()];
      for(int i=0;i<tiles.size();i++){WorldMoveTargetController.TileCenter t=tiles.get(i);for(int j=0;j<4;j++){WorldMoveTargetController.Direction d=DIRECTIONS[j];Integer n=centers.get(key(t.x+d.dx,t.y+d.dy));neighbors[i][j]=n==null?-1:n;}}
    }
    public WorldMoveTargetController.Direction nextStep(float startX,float startY,float targetX,float targetY,EdgePolicy policy){
      expandedNodes=0;
      if(policy==null||!Float.isFinite(startX)||!Float.isFinite(startY)||!Float.isFinite(targetX)||!Float.isFinite(targetY))return null;
      Integer start=centers.get(key(startX,startY));if(start==null)return null;
      // Only the four true melee-adjacent cells are goals; never occupy the player cell.
      int[] goals=new int[4];int goalCount=0;
      for(WorldMoveTargetController.Direction d:DIRECTIONS){Integer goal=centers.get(key(targetX-d.dx,targetY-d.dy));if(goal!=null&&goal!=start&&CanonicalMeleeTileContract.reachable(tiles.get(goal).x,tiles.get(goal).y,targetX,targetY))goals[goalCount++]=goal;}
      if(goalCount==0)return null;
      // A fully occupied melee ring cannot be reached. Check its incoming live edges
      // before an otherwise exhaustive search of thousands of distant forest cells.
      boolean goalOpen=false;
      for(int i=0;i<goalCount&&!goalOpen;i++)for(int from:neighbors[goals[i]])if(from>=0&&policy.canTraverse(tiles.get(from),tiles.get(goals[i]))){goalOpen=true;break;}
      if(!goalOpen)return null;
      if(++generation==0){Arrays.fill(seen,0);Arrays.fill(closed,0);generation=1;}
      final int epoch=generation;
      PriorityQueue<Node> open=new PriorityQueue<>(Comparator.<Node>comparingDouble(n->n.f).thenComparingInt(n->-n.g).thenComparingInt(n->n.first==null?-1:n.first.ordinal()).thenComparingInt(n->n.tile));
      seen[start]=epoch;best[start]=0;open.add(new Node(start,0,heuristic(start,goals,goalCount),null));
      while(!open.isEmpty()){
        Node current=open.poll();if(closed[current.tile]==epoch||best[current.tile]!=current.g)continue;
        closed[current.tile]=epoch;expandedNodes++;
        for(int i=0;i<goalCount;i++)if(current.tile==goals[i])return current.first;
        for(int j=0;j<4;j++){
          int next=neighbors[current.tile][j],cost=current.g+1;
          if(next<0||closed[next]==epoch||(seen[next]==epoch&&best[next]<=cost))continue;
          if(!policy.canTraverse(tiles.get(current.tile),tiles.get(next)))continue;
          seen[next]=epoch;best[next]=cost;
          open.add(new Node(next,cost,cost+heuristic(next,goals,goalCount),current.first==null?DIRECTIONS[j]:current.first));
        }
      }
      return null;
    }
    private float heuristic(int index,int[] goals,int count){
      WorldMoveTargetController.TileCenter a=tiles.get(index);float h=Float.MAX_VALUE;
      for(int i=0;i<count;i++){WorldMoveTargetController.TileCenter b=tiles.get(goals[i]);h=Math.min(h,Math.max(Math.abs(a.x-b.x)/32f,Math.abs(a.y-b.y)/16f));}
      return h;
    }
  }
  private MonsterChasePathfinder(){}
  public static WorldMoveTargetController.Direction nextStep(List<WorldMoveTargetController.TileCenter> tiles,float startX,float startY,float targetX,float targetY,EdgePolicy policy){
    if(tiles==null)return null;return new IndexedMap(tiles).nextStep(startX,startY,targetX,targetY,policy);
  }
  private static long key(float x,float y){return ((long)Math.round(x*100f)<<32)^(Math.round(y*100f)&0xffffffffL);}
}
