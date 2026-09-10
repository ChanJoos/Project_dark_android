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

/** Tile-locked isometric navigation for the playable village. */
public final class WorldMoveTargetController {
  public enum RequestKind { GROUND, NPC_APPROACH, MONSTER_APPROACH, DIRECT_STEP }
  public enum Status { IDLE, MOVING, REACHED, BLOCKED, CANCELLED }
  public enum CancelReason { NONE, REPLACED, DIRECT_INPUT, ACTION, EXPLICIT }
  public enum Direction {
    NW(-32f,-16f), NE(32f,-16f), SW(-32f,16f), SE(32f,16f);
    public final float dx,dy;
    Direction(float dx,float dy){this.dx=dx;this.dy=dy;}
    public static Direction between(float ax,float ay,float bx,float by){
      float dx=bx-ax,dy=by-ay;
      for(Direction d:values())if(close(dx,d.dx)&&close(dy,d.dy))return d;
      return null;
    }
  }
  public static final class TileCenter {
    public final float x,y;
    public TileCenter(float x,float y){this.x=x;this.y=y;}
  }
  public interface NavigationWorld {
    List<TileCenter> navigationTiles();
    boolean canPlayerOccupy(float worldX,float worldY);
  }
  public interface Walker {
    float worldX();
    float worldY();
    boolean moveToAdjacentTile(float destinationX,float destinationY,Direction direction);
  }
  public static final class Snapshot {
    public final long requestId,replacedRequestId;
    public final RequestKind kind;
    public final String targetEntityId;
    public final float targetX,targetY,tolerance;
    public final Status status;
    public final CancelReason cancelReason;
    public final int remainingWaypoints;
    public final Direction lastStepDirection;
    private Snapshot(long r,long rr,RequestKind k,String e,float x,float y,float t,Status s,CancelReason c,int w,Direction d){
      requestId=r;replacedRequestId=rr;kind=k;targetEntityId=e;targetX=x;targetY=y;tolerance=t;
      status=s;cancelReason=c;remainingWaypoints=w;lastStepDirection=d;
    }
  }
  private static final class Node {
    final TileCenter tile; final float g,f; final Node parent;
    Node(TileCenter tile,float g,float f,Node parent){this.tile=tile;this.g=g;this.f=f;this.parent=parent;}
  }

  public static final float TILE_STEP_SECONDS=.14f;
  public static final float DEFAULT_GROUND_TOLERANCE=0f;
  public static final float DEFAULT_NPC_APPROACH_TOLERANCE=56f;
  public static final float DEFAULT_MONSTER_APPROACH_TOLERANCE=72f;

  private final NavigationWorld world;
  private final Walker walker;
  private final List<TileCenter> tiles;
  private final Map<String,TileCenter> byCenter;
  private final float minTileX,maxTileX,minTileY,maxTileY;
  private long sequence,replacedRequestId;
  private RequestKind kind;
  private String targetEntityId;
  private float targetX,targetY,tolerance,stepClock;
  private Status status=Status.IDLE;
  private CancelReason cancelReason=CancelReason.NONE;
  private List<TileCenter> path=Collections.emptyList();
  private int waypointIndex;
  private Direction lastStepDirection;

  public WorldMoveTargetController(NavigationWorld world,Walker walker){
    if(world==null||walker==null)throw new IllegalArgumentException("world and walker are required");
    this.world=world;this.walker=walker;
    List<TileCenter> supplied=world.navigationTiles();
    if(supplied==null||supplied.isEmpty())throw new IllegalArgumentException("authored navigation tiles are required");
    tiles=Collections.unmodifiableList(new ArrayList<>(supplied));
    byCenter=new HashMap<>();
    float minX=Float.MAX_VALUE,maxX=-Float.MAX_VALUE,minY=Float.MAX_VALUE,maxY=-Float.MAX_VALUE;
    for(TileCenter tile:tiles){byCenter.put(key(tile.x,tile.y),tile);minX=Math.min(minX,tile.x);maxX=Math.max(maxX,tile.x);minY=Math.min(minY,tile.y);maxY=Math.max(maxY,tile.y);}
    minTileX=minX;maxTileX=maxX;minTileY=minY;maxTileY=maxY;
  }

  public Snapshot requestGroundMove(float x,float y){return begin(RequestKind.GROUND,null,x,y,DEFAULT_GROUND_TOLERANCE);}
  public Snapshot requestNpcApproach(String id,float x,float y,float approachTolerance){return beginEntity(RequestKind.NPC_APPROACH,id,x,y,approachTolerance);}
  public Snapshot requestMonsterApproach(String id,float x,float y,float approachTolerance){return beginEntity(RequestKind.MONSTER_APPROACH,id,x,y,approachTolerance);}
  private Snapshot beginEntity(RequestKind requestKind,String id,float x,float y,float approachTolerance){
    if(id==null||id.trim().isEmpty())throw new IllegalArgumentException("entity id is required");
    return begin(requestKind,id,x,y,Math.max(1f,approachTolerance));
  }
  private Snapshot begin(RequestKind requestKind,String entityId,float requestedX,float requestedY,float approachTolerance){
    replacedRequestId=status==Status.MOVING?sequence:0;
    sequence++;kind=requestKind;targetEntityId=entityId;tolerance=approachTolerance;cancelReason=CancelReason.NONE;lastStepDirection=null;stepClock=0f;
    if(requestKind==RequestKind.GROUND&&!insideAuthoredPlane(requestedX,requestedY))return blocked(requestedX,requestedY);
    TileCenter start=currentTile();
    TileCenter goal=requestKind==RequestKind.GROUND?nearestTraversable(requestedX,requestedY):nearestApproachTile(requestedX,requestedY,approachTolerance);
    if(start==null||goal==null)return blocked(requestedX,requestedY);
    targetX=goal.x;targetY=goal.y;path=findPath(start,goal);waypointIndex=0;
    if(same(start,goal)){status=Status.REACHED;path=Collections.emptyList();}
    else if(path.isEmpty())status=Status.BLOCKED;
    else status=Status.MOVING;
    return snapshot();
  }

  /** One direct input pulse always means exactly one adjacent authored tile. */
  public Snapshot step(Direction direction){
    if(direction==null)throw new IllegalArgumentException("direction required");
    long replaced=status==Status.MOVING?sequence:0;cancel(CancelReason.DIRECT_INPUT);
    sequence++;replacedRequestId=replaced;kind=RequestKind.DIRECT_STEP;targetEntityId=null;tolerance=0f;cancelReason=CancelReason.NONE;lastStepDirection=null;
    TileCenter start=currentTile();
    TileCenter goal=start==null?null:byCenter.get(key(start.x+direction.dx,start.y+direction.dy));
    float requestedX=start==null?walker.worldX()+direction.dx:start.x+direction.dx;
    float requestedY=start==null?walker.worldY()+direction.dy:start.y+direction.dy;
    if(goal==null||!world.canPlayerOccupy(goal.x,goal.y)||!walker.moveToAdjacentTile(goal.x,goal.y,direction))return blocked(requestedX,requestedY);
    targetX=goal.x;targetY=goal.y;status=Status.REACHED;lastStepDirection=direction;path=Collections.emptyList();waypointIndex=0;
    return snapshot();
  }

  public Snapshot tick(float dt){
    if(status!=Status.MOVING||dt<=0f)return snapshot();
    stepClock+=dt;
    while(status==Status.MOVING&&stepClock+.00001f>=TILE_STEP_SECONDS){
      stepClock-=TILE_STEP_SECONDS;
      if(waypointIndex>=path.size()){status=Status.REACHED;break;}
      TileCenter from=currentTile(),to=path.get(waypointIndex);
      Direction direction=from==null?null:Direction.between(from.x,from.y,to.x,to.y);
      if(direction==null||!world.canPlayerOccupy(to.x,to.y)||!walker.moveToAdjacentTile(to.x,to.y,direction)){status=Status.BLOCKED;break;}
      lastStepDirection=direction;waypointIndex++;
      if(waypointIndex>=path.size())status=Status.REACHED;
    }
    return snapshot();
  }

  public Snapshot cancelForDirectInput(){return cancel(CancelReason.DIRECT_INPUT);}
  public Snapshot cancelForAction(){return cancel(CancelReason.ACTION);}
  public Snapshot cancel(){return cancel(CancelReason.EXPLICIT);}
  private Snapshot cancel(CancelReason reason){
    if(status==Status.MOVING){status=Status.CANCELLED;cancelReason=reason;path=Collections.emptyList();waypointIndex=0;stepClock=0f;}
    return snapshot();
  }
  private Snapshot blocked(float x,float y){targetX=x;targetY=y;status=Status.BLOCKED;path=Collections.emptyList();waypointIndex=0;stepClock=0f;return snapshot();}
  public Snapshot snapshot(){return new Snapshot(sequence,replacedRequestId,kind,targetEntityId,targetX,targetY,tolerance,status,cancelReason,Math.max(0,path.size()-waypointIndex),lastStepDirection);}

  private TileCenter currentTile(){
    TileCenter exact=byCenter.get(key(walker.worldX(),walker.worldY()));
    return exact;
  }
  private TileCenter nearestTraversable(float x,float y){
    TileCenter best=null;float bestDistance=Float.MAX_VALUE;
    for(TileCenter tile:tiles){if(!world.canPlayerOccupy(tile.x,tile.y))continue;float d=distanceSquared(tile.x,tile.y,x,y);if(d<bestDistance){bestDistance=d;best=tile;}}
    return best;
  }
  private boolean insideAuthoredPlane(float x,float y){return x>=minTileX-32f&&x<=maxTileX+32f&&y>=minTileY-16f&&y<=maxTileY+16f;}
  private TileCenter nearestApproachTile(float x,float y,float approachTolerance){
    TileCenter best=null;float bestToEntity=Float.MAX_VALUE;
    for(TileCenter tile:tiles){if(!world.canPlayerOccupy(tile.x,tile.y))continue;float d=distance(tile.x,tile.y,x,y);if(d<=approachTolerance&&d<bestToEntity){bestToEntity=d;best=tile;}}
    return best;
  }
  private List<TileCenter> findPath(TileCenter start,TileCenter goal){
    PriorityQueue<Node> open=new PriorityQueue<>(Comparator.comparingDouble(n->n.f));
    Map<String,Float> best=new HashMap<>();Set<String> closed=new HashSet<>();
    open.add(new Node(start,0f,heuristic(start,goal),null));best.put(key(start.x,start.y),0f);
    while(!open.isEmpty()){
      Node current=open.poll();String currentKey=key(current.tile.x,current.tile.y);if(!closed.add(currentKey))continue;
      if(same(current.tile,goal))return reconstruct(current);
      for(Direction direction:Direction.values()){
        TileCenter next=byCenter.get(key(current.tile.x+direction.dx,current.tile.y+direction.dy));
        if(next==null||!world.canPlayerOccupy(next.x,next.y))continue;
        String nextKey=key(next.x,next.y);if(closed.contains(nextKey))continue;
        float ng=current.g+1f;Float previous=best.get(nextKey);if(previous!=null&&previous<=ng)continue;
        best.put(nextKey,ng);open.add(new Node(next,ng,ng+heuristic(next,goal),current));
      }
    }
    return Collections.emptyList();
  }
  private static List<TileCenter> reconstruct(Node goal){
    List<TileCenter> reversed=new ArrayList<>();for(Node n=goal;n!=null;n=n.parent)reversed.add(n.tile);
    Collections.reverse(reversed);if(!reversed.isEmpty())reversed.remove(0);return reversed;
  }
  private static float heuristic(TileCenter a,TileCenter b){
    float row=Math.abs(a.y-b.y)/16f,column=Math.abs(a.x-b.x)/32f;return Math.max(row,column);
  }
  private static boolean same(TileCenter a,TileCenter b){return close(a.x,b.x)&&close(a.y,b.y);}
  private static boolean close(float a,float b){return Math.abs(a-b)<.01f;}
  private static float distanceSquared(float ax,float ay,float bx,float by){float dx=ax-bx,dy=ay-by;return dx*dx+dy*dy;}
  private static float distance(float ax,float ay,float bx,float by){return(float)Math.sqrt(distanceSquared(ax,ay,bx,by));}
  private static String key(float x,float y){return Math.round(x*100f)+":"+Math.round(y*100f);}
}
