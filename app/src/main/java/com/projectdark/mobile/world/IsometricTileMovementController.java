package com.projectdark.mobile.world;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

/**
 * [ADAPTED] Tile-locked movement for the current 64x32 Milles projection.
 * Logical positions advance only from one traversable tile center to an adjacent tile center.
 */
public final class IsometricTileMovementController {
  public enum Direction {
    NW(-32f,-16f), NE(32f,-16f), SW(-32f,16f), SE(32f,16f);
    public final float dx,dy;
    Direction(float dx,float dy){this.dx=dx;this.dy=dy;}
  }
  public enum Status { IDLE,MOVING,REACHED,BLOCKED,CANCELLED }
  public enum CancelReason { NONE,REPLACED,DIRECT_INPUT,ACTION,EXPLICIT }
  public interface NavigationWorld { boolean canPlayerOccupy(float x,float y); }
  public interface Walker {
    float worldX(); float worldY();
    /** Must be atomic: success means the entire adjacent-tile delta was applied. */
    boolean tryAdjacentTileStep(float dx,float dy);
  }
  public interface StepListener { void onStep(Direction direction,float fromX,float fromY,float toX,float toY); }
  public static final class Snapshot {
    public final long requestId,replacedRequestId;
    public final float requestedX,requestedY,targetX,targetY;
    public final Status status; public final CancelReason cancelReason;
    public final int remainingSteps; public final Direction activeDirection;
    Snapshot(long id,long replaced,float rx,float ry,float tx,float ty,Status status,CancelReason reason,int remaining,Direction direction){
      requestId=id;replacedRequestId=replaced;requestedX=rx;requestedY=ry;targetX=tx;targetY=ty;this.status=status;cancelReason=reason;remainingSteps=remaining;activeDirection=direction;
    }
  }
  private static final class SearchNode {
    final AdaptedMillesIsometricTileLayer.Tile tile; final int g,f; final SearchNode parent;
    SearchNode(AdaptedMillesIsometricTileLayer.Tile tile,int g,int f,SearchNode parent){this.tile=tile;this.g=g;this.f=f;this.parent=parent;}
  }
  private final NavigationWorld world; private final Walker walker; private final StepListener listener;
  private final Map<String,AdaptedMillesIsometricTileLayer.Tile> byCenter=new HashMap<>();
  private long sequence=0,replacedRequestId=0; private float requestedX,requestedY,targetX,targetY;
  private Status status=Status.IDLE; private CancelReason cancelReason=CancelReason.NONE;
  private List<AdaptedMillesIsometricTileLayer.Tile> path=Collections.emptyList(); private int pathIndex=0;
  private Direction activeDirection;

  public IsometricTileMovementController(NavigationWorld world,Walker walker){this(world,walker,null);}
  public IsometricTileMovementController(NavigationWorld world,Walker walker,StepListener listener){
    if(world==null||walker==null)throw new IllegalArgumentException("world and walker required");
    this.world=world;this.walker=walker;this.listener=listener;
    for(AdaptedMillesIsometricTileLayer.Tile tile:AdaptedMillesIsometricTileLayer.tiles())byCenter.put(key(tile.centerX,tile.centerY),tile);
  }

  /** Resolve arbitrary tap to nearest traversable authored tile and path only through adjacent diamonds. */
  public Snapshot requestGroundMove(float x,float y){
    replacedRequestId=status==Status.MOVING?sequence:0;sequence++;requestedX=x;requestedY=y;cancelReason=CancelReason.NONE;activeDirection=null;
    AdaptedMillesIsometricTileLayer.Tile start=nearestTraversable(walker.worldX(),walker.worldY());
    AdaptedMillesIsometricTileLayer.Tile goal=nearestTraversable(x,y);
    if(start==null||goal==null||!sameCenter(start,walker.worldX(),walker.worldY())){status=Status.BLOCKED;path=Collections.emptyList();pathIndex=0;return snapshot();}
    targetX=goal.centerX;targetY=goal.centerY;
    path=findPath(start,goal);pathIndex=0;
    if(start==goal){status=Status.REACHED;return snapshot();}
    status=path.isEmpty()?Status.BLOCKED:Status.MOVING;return snapshot();
  }

  /** Consume exactly one adjacent tile step. No free-pixel interpolation mutates logical position here. */
  public Snapshot step(){
    if(status!=Status.MOVING)return snapshot();
    if(pathIndex>=path.size()){status=Status.REACHED;activeDirection=null;return snapshot();}
    AdaptedMillesIsometricTileLayer.Tile next=path.get(pathIndex);
    float fromX=walker.worldX(),fromY=walker.worldY();
    Direction direction=directionFor(next.centerX-fromX,next.centerY-fromY);
    if(direction==null||!world.canPlayerOccupy(next.centerX,next.centerY)){status=Status.BLOCKED;activeDirection=null;return snapshot();}
    if(!walker.tryAdjacentTileStep(direction.dx,direction.dy)){status=Status.BLOCKED;activeDirection=null;return snapshot();}
    if(!sameCenter(next,walker.worldX(),walker.worldY())){status=Status.BLOCKED;activeDirection=null;return snapshot();}
    activeDirection=direction;pathIndex++;
    if(listener!=null)listener.onStep(direction,fromX,fromY,next.centerX,next.centerY);
    if(pathIndex>=path.size())status=Status.REACHED;
    return snapshot();
  }

  /** One direct joystick/d-pad command = one adjacent isometric tile. */
  public Snapshot step(Direction direction){
    if(direction==null)return snapshot();
    cancel(CancelReason.DIRECT_INPUT);sequence++;replacedRequestId=0;cancelReason=CancelReason.NONE;
    requestedX=walker.worldX()+direction.dx;requestedY=walker.worldY()+direction.dy;targetX=requestedX;targetY=requestedY;
    AdaptedMillesIsometricTileLayer.Tile next=byCenter.get(key(targetX,targetY));
    if(next==null||!world.canPlayerOccupy(targetX,targetY)){status=Status.BLOCKED;activeDirection=null;return snapshot();}
    float fromX=walker.worldX(),fromY=walker.worldY();
    if(!walker.tryAdjacentTileStep(direction.dx,direction.dy)||!sameCenter(next,walker.worldX(),walker.worldY())){status=Status.BLOCKED;activeDirection=null;return snapshot();}
    activeDirection=direction;status=Status.REACHED;if(listener!=null)listener.onStep(direction,fromX,fromY,targetX,targetY);return snapshot();
  }

  public Snapshot cancelForDirectInput(){return cancel(CancelReason.DIRECT_INPUT);} public Snapshot cancelForAction(){return cancel(CancelReason.ACTION);} public Snapshot cancel(){return cancel(CancelReason.EXPLICIT);}
  private Snapshot cancel(CancelReason reason){if(status==Status.MOVING){status=Status.CANCELLED;cancelReason=reason;}path=Collections.emptyList();pathIndex=0;activeDirection=null;return snapshot();}
  public Snapshot snapshot(){return new Snapshot(sequence,replacedRequestId,requestedX,requestedY,targetX,targetY,status,cancelReason,Math.max(0,path.size()-pathIndex),activeDirection);}

  public AdaptedMillesIsometricTileLayer.Tile nearestTraversable(float x,float y){
    AdaptedMillesIsometricTileLayer.Tile best=null;float bestD=Float.MAX_VALUE;
    for(AdaptedMillesIsometricTileLayer.Tile tile:AdaptedMillesIsometricTileLayer.tiles()){
      if(!world.canPlayerOccupy(tile.centerX,tile.centerY))continue;float dx=x-tile.centerX,dy=y-tile.centerY,d=dx*dx+dy*dy;
      if(d<bestD){bestD=d;best=tile;}
    }return best;
  }

  private List<AdaptedMillesIsometricTileLayer.Tile> findPath(AdaptedMillesIsometricTileLayer.Tile start,AdaptedMillesIsometricTileLayer.Tile goal){
    PriorityQueue<SearchNode> open=new PriorityQueue<>(Comparator.comparingInt(n->n.f));Map<String,Integer> best=new HashMap<>();Set<String> closed=new HashSet<>();
    open.add(new SearchNode(start,0,heuristic(start,goal),null));best.put(key(start.centerX,start.centerY),0);
    while(!open.isEmpty()){
      SearchNode current=open.poll();String ck=key(current.tile.centerX,current.tile.centerY);if(!closed.add(ck))continue;
      if(current.tile==goal)return reconstruct(current);
      for(Direction direction:Direction.values()){
        AdaptedMillesIsometricTileLayer.Tile next=byCenter.get(key(current.tile.centerX+direction.dx,current.tile.centerY+direction.dy));
        if(next==null||!world.canPlayerOccupy(next.centerX,next.centerY))continue;String nk=key(next.centerX,next.centerY);if(closed.contains(nk))continue;
        int ng=current.g+1;Integer old=best.get(nk);if(old!=null&&old<=ng)continue;best.put(nk,ng);open.add(new SearchNode(next,ng,ng+heuristic(next,goal),current));
      }
    }return Collections.emptyList();
  }
  private List<AdaptedMillesIsometricTileLayer.Tile> reconstruct(SearchNode goal){List<AdaptedMillesIsometricTileLayer.Tile> rev=new ArrayList<>();for(SearchNode n=goal;n!=null;n=n.parent)rev.add(n.tile);Collections.reverse(rev);if(!rev.isEmpty())rev.remove(0);return rev;}
  private static int heuristic(AdaptedMillesIsometricTileLayer.Tile a,AdaptedMillesIsometricTileLayer.Tile b){return Math.max(Math.abs(a.row-b.row),Math.abs(a.column-b.column));}
  private static Direction directionFor(float dx,float dy){for(Direction d:Direction.values())if(close(dx,d.dx)&&close(dy,d.dy))return d;return null;}
  private static boolean sameCenter(AdaptedMillesIsometricTileLayer.Tile tile,float x,float y){return close(tile.centerX,x)&&close(tile.centerY,y);}
  private static boolean close(float a,float b){return Math.abs(a-b)<0.01f;}
  private static String key(float x,float y){return Math.round(x*10f)+":"+Math.round(y*10f);}
}
