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
  public enum RequestKind { GROUND, NPC_APPROACH, MONSTER_APPROACH, SKILL_APPROACH, DIRECT_STEP }
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
    default boolean canPlayerTraverse(float fromX,float fromY,float toX,float toY){
      float dx=toX-fromX,dy=toY-fromY;
      int samples=Math.max(2,(int)Math.ceil(Math.sqrt(dx*dx+dy*dy)/4f));
      for(int i=1;i<=samples;i++){
        float t=i/(float)samples;
        if(!canPlayerOccupy(fromX+dx*t,fromY+dy*t))return false;
      }
      return true;
    }
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

  public static final float TILE_STEP_SECONDS=.60f;
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

  /** One BFS to the nearest reachable skill-valid tile, rather than a pixel-radius melee proxy. */
  public List<TileCenter> skillApproachPath(java.util.function.Predicate<TileCenter> legal){
    TileCenter start=currentTile();if(start==null)return null;
    java.util.ArrayDeque<Node> queue=new java.util.ArrayDeque<>();Set<String> visited=new HashSet<>();
    queue.add(new Node(start,0,0,null));visited.add(key(start.x,start.y));
    while(!queue.isEmpty()){
      Node n=queue.remove();if(legal.test(n.tile))return reconstruct(n);
      for(Direction d:Direction.values()){
        TileCenter next=byCenter.get(key(n.tile.x+d.dx,n.tile.y+d.dy));
        if(next==null||visited.contains(key(next.x,next.y))||!world.canPlayerOccupy(next.x,next.y)||!edgeTraversable(n.tile,next,d))continue;
        visited.add(key(next.x,next.y));queue.add(new Node(next,n.g+1,0,n));
      }
    }return null;
  }
  public Snapshot requestSkillApproach(String id,List<TileCenter> planned){
    replacedRequestId=status==Status.MOVING?sequence:0;sequence++;kind=RequestKind.SKILL_APPROACH;targetEntityId=id;
    tolerance=0;cancelReason=CancelReason.NONE;lastStepDirection=null;stepClock=0;waypointIndex=0;
    if(planned==null)return blocked(walker.worldX(),walker.worldY());
    path=new ArrayList<>(planned);TileCenter goal=path.isEmpty()?currentTile():path.get(path.size()-1);
    targetX=goal.x;targetY=goal.y;status=path.isEmpty()?Status.REACHED:Status.MOVING;return snapshot();
  }

  /** One multi-goal BFS for the whole map. It avoids one separate A* search per monster. */
  public int nearestApproachTarget(List<TileCenter> targets){
    TileCenter start=currentTile();if(start==null)return -1;
    Map<String,Integer> goals=new HashMap<>();
    for(int i=0;i<targets.size();i++){
      TileCenter target=targets.get(i);
      for(Direction d:Direction.values()){
        TileCenter goal=byCenter.get(key(target.x+d.dx,target.y+d.dy));
        if(goal!=null&&world.canPlayerOccupy(goal.x,goal.y))goals.putIfAbsent(key(goal.x,goal.y),i);
      }
    }
    java.util.ArrayDeque<TileCenter> queue=new java.util.ArrayDeque<>();Set<String> seen=new HashSet<>();queue.add(start);seen.add(key(start.x,start.y));
    while(!queue.isEmpty()){
      TileCenter current=queue.remove();Integer index=goals.get(key(current.x,current.y));if(index!=null)return index;
      for(Direction d:Direction.values()){
        TileCenter next=byCenter.get(key(current.x+d.dx,current.y+d.dy));if(next==null)continue;String k=key(next.x,next.y);
        if(seen.contains(k)||!world.canPlayerOccupy(next.x,next.y)||!edgeTraversable(current,next,d))continue;
        seen.add(k);queue.add(next);
      }
    }
    return -1;
  }
  /** Shortest reachable legal melee approach measured in authored tile steps; -1 if unreachable. */
  public int monsterApproachPathSteps(float worldX,float worldY,float approachTolerance){
    TileCenter start=currentTile();
    if(start==null||!Float.isFinite(worldX)||!Float.isFinite(worldY))return -1;
    ApproachPlan plan=bestApproachPlan(start,worldX,worldY,approachTolerance,true);
    return plan==null?-1:plan.path.size();
  }
  private Snapshot beginEntity(RequestKind requestKind,String id,float x,float y,float approachTolerance){
    if(id==null||id.trim().isEmpty())throw new IllegalArgumentException("entity id is required");
    return begin(requestKind,id,x,y,Math.max(1f,approachTolerance));
  }
  private Snapshot begin(RequestKind requestKind,String entityId,float requestedX,float requestedY,float approachTolerance){
    replacedRequestId=status==Status.MOVING?sequence:0;
    sequence++;kind=requestKind;targetEntityId=entityId;tolerance=approachTolerance;cancelReason=CancelReason.NONE;lastStepDirection=null;stepClock=0f;
    if(requestKind==RequestKind.GROUND&&!insideAuthoredPlane(requestedX,requestedY))return blocked(requestedX,requestedY);
    TileCenter start=currentTile();
    TileCenter goal;
    List<TileCenter> plannedPath;
    if(requestKind==RequestKind.GROUND){
      goal=nearestTraversable(requestedX,requestedY);
      plannedPath=start==null||goal==null?Collections.emptyList():findPath(start,goal);
    }else{
      ApproachPlan plan=start==null?null:bestApproachPlan(start,requestedX,requestedY,approachTolerance,requestKind==RequestKind.MONSTER_APPROACH&&approachTolerance<=48f);
      goal=plan==null?null:plan.goal;
      plannedPath=plan==null?Collections.emptyList():plan.path;
    }
    if(start==null||goal==null)return blocked(requestedX,requestedY);
    targetX=goal.x;targetY=goal.y;path=plannedPath;waypointIndex=0;
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
  private static final class ApproachPlan {
    final TileCenter goal; final List<TileCenter> path; final float entityDistance;
    ApproachPlan(TileCenter goal,List<TileCenter> path,float entityDistance){this.goal=goal;this.path=path;this.entityDistance=entityDistance;}
  }
  /**
   * Entity approach is a multi-goal shortest-path problem. Choosing the tile physically closest
   * to the entity first can force an unnecessary detour around collision. Evaluate every
   * interaction-valid tile and prefer the reachable one with the fewest authored tile steps.
   * Entity distance is only a tie-breaker, so interaction remains visually tight.
   */
  private ApproachPlan bestApproachPlan(TileCenter start,float x,float y,float approachTolerance,boolean meleeOnly){
    // Collect legal goals cheaply before consulting collision. One multi-goal A* replaces
    // a complete A* per candidate; live occupancy is scoped to this request, never cached
    // across actor movement or map changes.
    Map<String,TileCenter> goals=new HashMap<>();
    Map<String,Integer> goalOrder=new HashMap<>();
    float radiusSquared=approachTolerance*approachTolerance;
    for(int i=0;i<tiles.size();i++){
      TileCenter tile=tiles.get(i);
      if(distanceSquared(tile.x,tile.y,x,y)>radiusSquared
          ||(meleeOnly&&!com.projectdark.mobile.CanonicalMeleeTileContract.reachable(tile.x,tile.y,x,y))
          ||!world.canPlayerOccupy(tile.x,tile.y))continue;
      String k=key(tile.x,tile.y);goals.put(k,tile);goalOrder.put(k,i);
    }
    if(goals.isEmpty())return null;
    PriorityQueue<Node> open=new PriorityQueue<>(Comparator.comparingDouble(n->n.f));
    Map<String,Float> best=new HashMap<>();Set<String> closed=new HashSet<>();
    Map<String,Boolean> occupancy=new HashMap<>();
    open.add(new Node(start,0f,approachHeuristic(start,goals.values()),null));best.put(key(start.x,start.y),0f);
    Node chosen=null;float bestSteps=Float.MAX_VALUE,bestDistance=Float.MAX_VALUE;int bestOrder=Integer.MAX_VALUE;
    while(!open.isEmpty()){
      Node current=open.poll();if(current.f>bestSteps)break;
      String currentKey=key(current.tile.x,current.tile.y);if(!closed.add(currentKey))continue;
      if(goals.containsKey(currentKey)){
        float d=distanceSquared(current.tile.x,current.tile.y,x,y);int order=goalOrder.get(currentKey);
        if(current.g<bestSteps||(current.g==bestSteps&&(d<bestDistance||(d==bestDistance&&order<bestOrder)))){
          chosen=current;bestSteps=current.g;bestDistance=d;bestOrder=order;
        }
        continue;
      }
      if(current.g>=bestSteps)continue;
      for(Direction direction:Direction.values()){
        TileCenter next=byCenter.get(key(current.tile.x+direction.dx,current.tile.y+direction.dy));
        if(next==null)continue;String nextKey=key(next.x,next.y);
        if(closed.contains(nextKey))continue;
        float ng=current.g+1f;Float previous=best.get(nextKey);if(previous!=null&&previous<=ng)continue;
        Boolean occupiable=occupancy.get(nextKey);
        if(occupiable==null){occupiable=world.canPlayerOccupy(next.x,next.y);occupancy.put(nextKey,occupiable);}
        if(!occupiable||!edgeTraversable(current.tile,next,direction))continue;
        float estimate=ng+approachHeuristic(next,goals.values());if(estimate>bestSteps)continue;
        best.put(nextKey,ng);open.add(new Node(next,ng,estimate,current));
      }
    }
    return chosen==null?null:new ApproachPlan(chosen.tile,reconstruct(chosen),(float)Math.sqrt(bestDistance));
  }
  private static float approachHeuristic(TileCenter tile,java.util.Collection<TileCenter> goals){
    float best=Float.MAX_VALUE;
    for(TileCenter goal:goals){
      // Every legal diagonal step changes X by32 and Y by16. This exact unobstructed
      // distance is consistent, so closed nodes cannot hide a shorter goal route.
      float h=Math.max(Math.abs(tile.x-goal.x)/32f,Math.abs(tile.y-goal.y)/16f);
      best=Math.min(best,h);
    }
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
        if(next==null||!world.canPlayerOccupy(next.x,next.y)||!edgeTraversable(current.tile,next,direction))continue;
        String nextKey=key(next.x,next.y);if(closed.contains(nextKey))continue;
        float ng=current.g+1f;Float previous=best.get(nextKey);if(previous!=null&&previous<=ng)continue;
        best.put(nextKey,ng);open.add(new Node(next,ng,ng+heuristic(next,goal),current));
      }
    }
    return Collections.emptyList();
  }
  private boolean edgeTraversable(TileCenter from,TileCenter to,Direction direction){
    if(direction==null)return false;
    return world.canPlayerTraverse(from.x,from.y,to.x,to.y);
  }
  private static List<TileCenter> reconstruct(Node goal){
    List<TileCenter> reversed=new ArrayList<>();for(Node n=goal;n!=null;n=n.parent)reversed.add(n.tile);
    Collections.reverse(reversed);if(!reversed.isEmpty())reversed.remove(0);return reversed;
  }
  private static float heuristic(TileCenter a,TileCenter b){
    float dx=Math.abs(a.x-b.x),dy=Math.abs(a.y-b.y);
    // E/W can consume 64 world-X in one logical traversal; diagonals consume 32x16.
    float verticalSteps=dy/16f;
    float residualX=Math.max(0f,dx-verticalSteps*32f);
    return verticalSteps+(float)Math.ceil(residualX/64f);
  }
  private static boolean same(TileCenter a,TileCenter b){return close(a.x,b.x)&&close(a.y,b.y);}
  private static boolean close(float a,float b){return Math.abs(a-b)<.01f;}
  private static float distanceSquared(float ax,float ay,float bx,float by){float dx=ax-bx,dy=ay-by;return dx*dx+dy*dy;}
  private static float distance(float ax,float ay,float bx,float by){return(float)Math.sqrt(distanceSquared(ax,ay,bx,by));}
  private static String key(float x,float y){return Math.round(x*100f)+":"+Math.round(y*100f);}
}
