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
 * [ADAPTED] World-owned tap movement contract.
 *
 * UX converts screen taps to world coordinates and then calls either requestGroundMove or
 * requestNpcApproach.  This controller never teleports: tick() advances through four-neighbour
 * waypoints via Walker.tryWalkStep, so the runtime remains the authority for collision/portals.
 */
public final class WorldMoveTargetController {
  public enum RequestKind { GROUND, NPC_APPROACH }
  public enum Status { IDLE, MOVING, REACHED, BLOCKED, CANCELLED }
  public enum CancelReason { NONE, REPLACED, DIRECT_INPUT, ACTION, EXPLICIT }

  public interface NavigationWorld {
    float minX();
    float maxX();
    float minY();
    float maxY();
    boolean canPlayerOccupy(float worldX,float worldY);
  }

  public interface Walker {
    float worldX();
    float worldY();
    /** Applies an incremental WALK command. False means runtime collision rejected the step. */
    boolean tryWalkStep(float dx,float dy);
  }

  public static final class Snapshot {
    public final long requestId,replacedRequestId;
    public final RequestKind kind;
    public final String targetEntityId;
    public final float targetX,targetY,tolerance;
    public final Status status;
    public final CancelReason cancelReason;
    public final int remainingWaypoints;

    private Snapshot(long requestId,long replacedRequestId,RequestKind kind,String targetEntityId,float targetX,float targetY,
        float tolerance,Status status,CancelReason cancelReason,int remainingWaypoints){
      this.requestId=requestId;
      this.replacedRequestId=replacedRequestId;
      this.kind=kind;
      this.targetEntityId=targetEntityId;
      this.targetX=targetX;
      this.targetY=targetY;
      this.tolerance=tolerance;
      this.status=status;
      this.cancelReason=cancelReason;
      this.remainingWaypoints=remainingWaypoints;
    }
  }

  private static final class Node {
    final int x,y;
    final float g,f;
    final Node parent;
    Node(int x,int y,float g,float f,Node parent){this.x=x;this.y=y;this.g=g;this.f=f;this.parent=parent;}
  }

  public static final float DEFAULT_CELL_SIZE=16f;
  public static final float DEFAULT_WALK_SPEED=96f;
  public static final float DEFAULT_GROUND_TOLERANCE=6f;
  public static final float DEFAULT_NPC_APPROACH_TOLERANCE=28f;
  private static final int MAX_EXPANSIONS=4096;
  private static final int MAX_REPLANS=2;

  private final NavigationWorld world;
  private final Walker walker;
  private final float cellSize,walkSpeed;
  private long sequence=0;
  private long replacedRequestId=0;
  private RequestKind kind;
  private String targetEntityId;
  private float targetX,targetY,tolerance;
  private Status status=Status.IDLE;
  private CancelReason cancelReason=CancelReason.NONE;
  private List<float[]> path=Collections.emptyList();
  private int waypointIndex=0,replans=0;

  public WorldMoveTargetController(NavigationWorld world,Walker walker){
    this(world,walker,DEFAULT_CELL_SIZE,DEFAULT_WALK_SPEED);
  }

  public WorldMoveTargetController(NavigationWorld world,Walker walker,float cellSize,float walkSpeed){
    if(world==null||walker==null)throw new IllegalArgumentException("world and walker are required");
    if(cellSize<=0f||walkSpeed<=0f)throw new IllegalArgumentException("cellSize and walkSpeed must be positive");
    this.world=world;
    this.walker=walker;
    this.cellSize=cellSize;
    this.walkSpeed=walkSpeed;
  }

  /** Generic empty-ground command. NPC hit-testing must not call this overload. */
  public Snapshot requestGroundMove(float worldX,float worldY){
    return begin(RequestKind.GROUND,null,worldX,worldY,DEFAULT_GROUND_TOLERANCE);
  }

  /** Entity-specific approach command, deliberately separate from generic map movement. */
  public Snapshot requestNpcApproach(String npcId,float worldX,float worldY,float approachTolerance){
    if(npcId==null||npcId.trim().isEmpty())throw new IllegalArgumentException("npcId is required");
    return begin(RequestKind.NPC_APPROACH,npcId,worldX,worldY,approachTolerance);
  }

  private Snapshot begin(RequestKind nextKind,String entityId,float x,float y,float requestedTolerance){
    replacedRequestId=status==Status.MOVING?sequence:0;
    sequence++;
    kind=nextKind;
    targetEntityId=entityId;
    targetX=x;
    targetY=y;
    tolerance=Math.max(1f,requestedTolerance);
    replans=0;
    cancelReason=CancelReason.NONE;
    if(!inside(x,y)||!rebuildPath()){
      status=Status.BLOCKED;
      path=Collections.emptyList();
      waypointIndex=0;
    }else{
      status=distance(walker.worldX(),walker.worldY(),targetX,targetY)<=tolerance?Status.REACHED:Status.MOVING;
    }
    return snapshot();
  }

  public Snapshot tick(float deltaSeconds){
    if(status!=Status.MOVING||deltaSeconds<=0f)return snapshot();
    if(distance(walker.worldX(),walker.worldY(),targetX,targetY)<=tolerance){status=Status.REACHED;return snapshot();}
    if(waypointIndex>=path.size()){
      if(!rebuildPath()){status=Status.BLOCKED;return snapshot();}
    }
    float[] waypoint=path.get(waypointIndex);
    float dx=waypoint[0]-walker.worldX(),dy=waypoint[1]-walker.worldY();
    float distance=(float)Math.sqrt(dx*dx+dy*dy);
    if(distance<=Math.max(1f,cellSize*.2f)){waypointIndex++;return tick(deltaSeconds);}
    float step=Math.min(walkSpeed*deltaSeconds,distance);
    boolean moved=walker.tryWalkStep(dx/distance*step,dy/distance*step);
    if(!moved){
      replans++;
      if(replans>MAX_REPLANS||!rebuildPath())status=Status.BLOCKED;
    }
    if(distance(walker.worldX(),walker.worldY(),targetX,targetY)<=tolerance)status=Status.REACHED;
    return snapshot();
  }

  public Snapshot cancelForDirectInput(){return cancel(CancelReason.DIRECT_INPUT);}
  public Snapshot cancelForAction(){return cancel(CancelReason.ACTION);}
  public Snapshot cancel(){return cancel(CancelReason.EXPLICIT);}

  private Snapshot cancel(CancelReason reason){
    if(status==Status.MOVING){status=Status.CANCELLED;cancelReason=reason;path=Collections.emptyList();waypointIndex=0;}
    return snapshot();
  }

  public Snapshot snapshot(){
    return new Snapshot(sequence,replacedRequestId,kind,targetEntityId,targetX,targetY,tolerance,status,cancelReason,
        Math.max(0,path.size()-waypointIndex));
  }

  private boolean rebuildPath(){
    path=findPath(walker.worldX(),walker.worldY(),targetX,targetY,tolerance);
    waypointIndex=0;
    return !path.isEmpty()||distance(walker.worldX(),walker.worldY(),targetX,targetY)<=tolerance;
  }

  private List<float[]> findPath(float startX,float startY,float goalX,float goalY,float goalTolerance){
    int sx=toCellX(startX),sy=toCellY(startY);
    PriorityQueue<Node> open=new PriorityQueue<>(Comparator.comparingDouble(n->n.f));
    Map<Long,Float> best=new HashMap<>();
    Set<Long> closed=new HashSet<>();
    open.add(new Node(sx,sy,0f,heuristic(cellX(sx),cellY(sy),goalX,goalY),null));
    best.put(key(sx,sy),0f);
    int expanded=0;
    final int[] ox={1,-1,0,0},oy={0,0,1,-1};
    while(!open.isEmpty()&&expanded++<MAX_EXPANSIONS){
      Node current=open.poll();
      long currentKey=key(current.x,current.y);
      if(!closed.add(currentKey))continue;
      float wx=cellX(current.x),wy=cellY(current.y);
      if(distance(wx,wy,goalX,goalY)<=goalTolerance)return reconstruct(current,goalX,goalY);
      for(int i=0;i<4;i++){
        int nx=current.x+ox[i],ny=current.y+oy[i];
        float nwx=cellX(nx),nwy=cellY(ny);
        long nk=key(nx,ny);
        if(closed.contains(nk)||!inside(nwx,nwy)||!world.canPlayerOccupy(nwx,nwy))continue;
        float ng=current.g+1f;
        Float old=best.get(nk);
        if(old!=null&&old<=ng)continue;
        best.put(nk,ng);
        open.add(new Node(nx,ny,ng,ng+heuristic(nwx,nwy,goalX,goalY),current));
      }
    }
    return Collections.emptyList();
  }

  private List<float[]> reconstruct(Node goal,float exactGoalX,float exactGoalY){
    List<float[]> reversed=new ArrayList<>();
    for(Node n=goal;n!=null;n=n.parent)reversed.add(new float[]{cellX(n.x),cellY(n.y)});
    Collections.reverse(reversed);
    if(!reversed.isEmpty())reversed.remove(0);
    if(kind==RequestKind.GROUND&&world.canPlayerOccupy(exactGoalX,exactGoalY))reversed.add(new float[]{exactGoalX,exactGoalY});
    return reversed;
  }

  private int toCellX(float x){return Math.round((x-world.minX())/cellSize);}
  private int toCellY(float y){return Math.round((y-world.minY())/cellSize);}
  private float cellX(int x){return world.minX()+x*cellSize;}
  private float cellY(int y){return world.minY()+y*cellSize;}
  private boolean inside(float x,float y){return x>=world.minX()&&x<=world.maxX()&&y>=world.minY()&&y<=world.maxY();}
  private static float heuristic(float x,float y,float gx,float gy){return Math.abs(x-gx)+Math.abs(y-gy);}
  private static float distance(float ax,float ay,float bx,float by){float dx=ax-bx,dy=ay-by;return(float)Math.sqrt(dx*dx+dy*dy);}
  private static long key(int x,int y){return((long)x<<32)^(y&0xffffffffL);}
}
