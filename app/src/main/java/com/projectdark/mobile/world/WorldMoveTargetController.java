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

/** [ADAPTED] World-owned tap movement contract. */
public final class WorldMoveTargetController {
  public enum RequestKind { GROUND, NPC_APPROACH }
  public enum Status { IDLE, MOVING, REACHED, BLOCKED, CANCELLED }
  public enum CancelReason { NONE, REPLACED, DIRECT_INPUT, ACTION, EXPLICIT }
  public interface NavigationWorld { float minX(); float maxX(); float minY(); float maxY(); boolean canPlayerOccupy(float worldX,float worldY); }
  public interface Walker { float worldX(); float worldY(); boolean tryWalkStep(float dx,float dy); }
  public static final class Snapshot {
    public final long requestId,replacedRequestId; public final RequestKind kind; public final String targetEntityId; public final float targetX,targetY,tolerance; public final Status status; public final CancelReason cancelReason; public final int remainingWaypoints;
    private Snapshot(long r,long rr,RequestKind k,String e,float x,float y,float t,Status s,CancelReason c,int w){requestId=r;replacedRequestId=rr;kind=k;targetEntityId=e;targetX=x;targetY=y;tolerance=t;status=s;cancelReason=c;remainingWaypoints=w;}
  }
  private static final class Node { final int x,y; final float g,f; final Node parent; Node(int x,int y,float g,float f,Node p){this.x=x;this.y=y;this.g=g;this.f=f;parent=p;} }
  public static final float DEFAULT_CELL_SIZE=16f,DEFAULT_WALK_SPEED=96f,DEFAULT_GROUND_TOLERANCE=6f,DEFAULT_NPC_APPROACH_TOLERANCE=28f;
  private static final int MAX_EXPANSIONS=4096,MAX_CONSECUTIVE_REPLANS=2;
  private final NavigationWorld world; private final Walker walker; private final float cellSize,walkSpeed;
  private long sequence=0,replacedRequestId=0; private RequestKind kind; private String targetEntityId; private float targetX,targetY,tolerance; private Status status=Status.IDLE; private CancelReason cancelReason=CancelReason.NONE; private List<float[]> path=Collections.emptyList(); private int waypointIndex=0,consecutiveReplans=0; private boolean navigationInvalidated=false;

  public WorldMoveTargetController(NavigationWorld world,Walker walker){this(world,walker,DEFAULT_CELL_SIZE,DEFAULT_WALK_SPEED);}
  public WorldMoveTargetController(NavigationWorld world,Walker walker,float cellSize,float walkSpeed){if(world==null||walker==null)throw new IllegalArgumentException("world and walker are required");if(cellSize<=0f||walkSpeed<=0f)throw new IllegalArgumentException("cellSize and walkSpeed must be positive");this.world=world;this.walker=walker;this.cellSize=cellSize;this.walkSpeed=walkSpeed;}
  public Snapshot requestGroundMove(float x,float y){return begin(RequestKind.GROUND,null,x,y,DEFAULT_GROUND_TOLERANCE);}
  public Snapshot requestNpcApproach(String id,float x,float y,float approachTolerance){if(id==null||id.trim().isEmpty())throw new IllegalArgumentException("npcId is required");return begin(RequestKind.NPC_APPROACH,id,x,y,approachTolerance);}
  private Snapshot begin(RequestKind k,String e,float x,float y,float tol){replacedRequestId=status==Status.MOVING?sequence:0;sequence++;kind=k;targetEntityId=e;targetX=x;targetY=y;tolerance=Math.max(1f,tol);consecutiveReplans=0;navigationInvalidated=false;cancelReason=CancelReason.NONE;if(!inside(x,y)||!rebuildPath()){status=Status.BLOCKED;path=Collections.emptyList();waypointIndex=0;}else status=distance(walker.worldX(),walker.worldY(),targetX,targetY)<=tolerance?Status.REACHED:Status.MOVING;return snapshot();}

  /**
   * Runtime calls this after occupancy topology changes (NPC/monster movement, gate state, collision layer swap).
   * The next tick replans before walking into a stale waypoint; no teleport or direct position mutation occurs.
   */
  public Snapshot notifyNavigationChanged(){if(status==Status.MOVING)navigationInvalidated=true;return snapshot();}

  public Snapshot tick(float dt){
    if(status!=Status.MOVING||dt<=0f)return snapshot();
    if(distance(walker.worldX(),walker.worldY(),targetX,targetY)<=tolerance){status=Status.REACHED;return snapshot();}
    if(navigationInvalidated){
      navigationInvalidated=false;
      if(!rebuildPath()){status=Status.BLOCKED;path=Collections.emptyList();waypointIndex=0;return snapshot();}
      consecutiveReplans=0;
    }
    if(waypointIndex>=path.size()&&!rebuildPath()){status=Status.BLOCKED;return snapshot();}
    float[] wp=path.get(waypointIndex);float dx=wp[0]-walker.worldX(),dy=wp[1]-walker.worldY();float d=(float)Math.sqrt(dx*dx+dy*dy);
    if(d<=Math.max(1f,cellSize*.2f)){waypointIndex++;return tick(dt);}
    float step=Math.min(walkSpeed*dt,d);
    boolean moved=walker.tryWalkStep(dx/d*step,dy/d*step);
    if(!moved){
      consecutiveReplans++;
      if(consecutiveReplans>MAX_CONSECUTIVE_REPLANS||!rebuildPath())status=Status.BLOCKED;
    }else consecutiveReplans=0;
    if(distance(walker.worldX(),walker.worldY(),targetX,targetY)<=tolerance)status=Status.REACHED;
    return snapshot();
  }
  public Snapshot cancelForDirectInput(){return cancel(CancelReason.DIRECT_INPUT);} public Snapshot cancelForAction(){return cancel(CancelReason.ACTION);} public Snapshot cancel(){return cancel(CancelReason.EXPLICIT);}
  private Snapshot cancel(CancelReason r){if(status==Status.MOVING){status=Status.CANCELLED;cancelReason=r;path=Collections.emptyList();waypointIndex=0;navigationInvalidated=false;}return snapshot();}
  public Snapshot snapshot(){return new Snapshot(sequence,replacedRequestId,kind,targetEntityId,targetX,targetY,tolerance,status,cancelReason,Math.max(0,path.size()-waypointIndex));}
  private boolean rebuildPath(){path=findPath(walker.worldX(),walker.worldY(),targetX,targetY,tolerance);waypointIndex=0;return !path.isEmpty()||distance(walker.worldX(),walker.worldY(),targetX,targetY)<=tolerance;}

  private List<float[]> findPath(float startX,float startY,float goalX,float goalY,float goalTolerance){
    int sx=toCellX(startX),sy=toCellY(startY); PriorityQueue<Node> open=new PriorityQueue<>(Comparator.comparingDouble(n->n.f)); Map<Long,Float> best=new HashMap<>(); Set<Long> closed=new HashSet<>();
    open.add(new Node(sx,sy,0f,heuristic(cellX(sx),cellY(sy),goalX,goalY),null));best.put(key(sx,sy),0f);int expanded=0;final int[] ox={1,-1,0,0},oy={0,0,1,-1};
    float searchTolerance=kind==RequestKind.GROUND&&world.canPlayerOccupy(goalX,goalY)?Math.max(goalTolerance,(float)(cellSize*Math.sqrt(2d)*0.5d)+0.01f):goalTolerance;
    while(!open.isEmpty()&&expanded++<MAX_EXPANSIONS){Node c=open.poll();long ck=key(c.x,c.y);if(!closed.add(ck))continue;float wx=cellX(c.x),wy=cellY(c.y);if(distance(wx,wy,goalX,goalY)<=searchTolerance)return reconstruct(c,goalX,goalY);for(int i=0;i<4;i++){int nx=c.x+ox[i],ny=c.y+oy[i];float nwx=cellX(nx),nwy=cellY(ny);long nk=key(nx,ny);if(closed.contains(nk)||!inside(nwx,nwy)||!world.canPlayerOccupy(nwx,nwy))continue;float ng=c.g+1f;Float old=best.get(nk);if(old!=null&&old<=ng)continue;best.put(nk,ng);open.add(new Node(nx,ny,ng,ng+heuristic(nwx,nwy,goalX,goalY),c));}}
    return Collections.emptyList();
  }
  private List<float[]> reconstruct(Node goal,float gx,float gy){List<float[]> rev=new ArrayList<>();for(Node n=goal;n!=null;n=n.parent)rev.add(new float[]{cellX(n.x),cellY(n.y)});Collections.reverse(rev);if(!rev.isEmpty())rev.remove(0);if(kind==RequestKind.GROUND&&world.canPlayerOccupy(gx,gy))rev.add(new float[]{gx,gy});return rev;}
  private int toCellX(float x){return Math.round((x-world.minX())/cellSize);} private int toCellY(float y){return Math.round((y-world.minY())/cellSize);} private float cellX(int x){return world.minX()+x*cellSize;} private float cellY(int y){return world.minY()+y*cellSize;} private boolean inside(float x,float y){return x>=world.minX()&&x<=world.maxX()&&y>=world.minY()&&y<=world.maxY();}
  private static float heuristic(float x,float y,float gx,float gy){return Math.abs(x-gx)+Math.abs(y-gy);} private static float distance(float ax,float ay,float bx,float by){float dx=ax-bx,dy=ay-by;return(float)Math.sqrt(dx*dx+dy*dy);} private static long key(int x,int y){return((long)x<<32)^(y&0xffffffffL);}
}
