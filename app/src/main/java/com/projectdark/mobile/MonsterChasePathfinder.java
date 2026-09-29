package com.projectdark.mobile;

import com.projectdark.mobile.world.WorldMoveTargetController;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Deterministic shortest legal tile route to any melee-adjacent tile around a moving player. */
public final class MonsterChasePathfinder {
  public interface EdgePolicy {
    boolean canTraverse(WorldMoveTargetController.TileCenter from,
        WorldMoveTargetController.TileCenter to);
  }

  private static final class Node {
    final WorldMoveTargetController.TileCenter tile;
    final WorldMoveTargetController.Direction firstStep;
    Node(WorldMoveTargetController.TileCenter tile,WorldMoveTargetController.Direction firstStep){
      this.tile=tile;this.firstStep=firstStep;
    }
  }

  private MonsterChasePathfinder(){}

  public static WorldMoveTargetController.Direction nextStep(
      List<WorldMoveTargetController.TileCenter> tiles,float startX,float startY,
      float targetX,float targetY,EdgePolicy policy){
    if(tiles==null||policy==null||!Float.isFinite(startX)||!Float.isFinite(startY)
        ||!Float.isFinite(targetX)||!Float.isFinite(targetY))return null;
    Map<String,WorldMoveTargetController.TileCenter> byCenter=new HashMap<>();
    for(WorldMoveTargetController.TileCenter tile:tiles)if(tile!=null)byCenter.put(key(tile.x,tile.y),tile);
    WorldMoveTargetController.TileCenter start=byCenter.get(key(startX,startY));
    if(start==null)return null;
    ArrayDeque<Node> open=new ArrayDeque<>();Set<String> visited=new HashSet<>();
    open.addLast(new Node(start,null));visited.add(key(start.x,start.y));
    while(!open.isEmpty()){
      Node current=open.removeFirst();
      if(!same(start,current.tile)
          &&CanonicalMeleeTileContract.reachable(current.tile.x,current.tile.y,targetX,targetY))
        return current.firstStep;
      for(WorldMoveTargetController.Direction direction:WorldMoveTargetController.Direction.values()){
        WorldMoveTargetController.TileCenter next=byCenter.get(key(
            current.tile.x+direction.dx,current.tile.y+direction.dy));
        if(next==null||!policy.canTraverse(current.tile,next))continue;
        String nextKey=key(next.x,next.y);if(!visited.add(nextKey))continue;
        open.addLast(new Node(next,current.firstStep==null?direction:current.firstStep));
      }
    }
    return null;
  }

  private static boolean same(WorldMoveTargetController.TileCenter a,WorldMoveTargetController.TileCenter b){
    return Math.abs(a.x-b.x)<.01f&&Math.abs(a.y-b.y)<.01f;
  }
  private static String key(float x,float y){return Math.round(x*100f)+":"+Math.round(y*100f);}
}
