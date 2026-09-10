package com.projectdark.mobile.world;

/**
 * World-owned portal activation and transition lifecycle.
 *
 * The controller does not mutate maps or player coordinates. It validates entry range, emits one
 * transition request, latches the occupied portal against duplicate requests, and lets the runtime
 * complete or reject the request explicitly.
 */
public final class WorldPortalTransitionController {
  public enum PortalReadiness { READY, TARGET_PENDING, DISABLED }
  public enum Status {
    IDLE, OUTSIDE, BLOCKED_TARGET_PENDING, BLOCKED_DISABLED,
    REQUESTED, PENDING, COMPLETED, REJECTED, LATCHED, STALE_COMPLETION
  }

  public static final class Portal {
    public final String portalId,sourceMapId,targetMapId,targetSpawnId;
    public final float worldX,worldY,activationRadius;
    public final PortalReadiness readiness;

    public Portal(String portalId,String sourceMapId,String targetMapId,String targetSpawnId,
        float worldX,float worldY,float activationRadius,PortalReadiness readiness){
      this.portalId=require(portalId,"portalId");
      this.sourceMapId=require(sourceMapId,"sourceMapId");
      this.targetMapId=targetMapId;
      this.targetSpawnId=targetSpawnId;
      if(activationRadius<=0f)throw new IllegalArgumentException("activationRadius must be positive");
      if(readiness==null)throw new IllegalArgumentException("readiness is required");
      this.worldX=worldX;this.worldY=worldY;this.activationRadius=activationRadius;this.readiness=readiness;
    }
  }

  public static final class Request {
    public final long requestId;
    public final String portalId,sourceMapId,targetMapId,targetSpawnId;
    private Request(long requestId,Portal portal){
      this.requestId=requestId;this.portalId=portal.portalId;this.sourceMapId=portal.sourceMapId;
      this.targetMapId=portal.targetMapId;this.targetSpawnId=portal.targetSpawnId;
    }
  }

  public static final class Snapshot {
    public final long requestId;
    public final String portalId,targetMapId,targetSpawnId,detail;
    public final Status status;
    private Snapshot(long requestId,String portalId,String targetMapId,String targetSpawnId,
        Status status,String detail){
      this.requestId=requestId;this.portalId=portalId;this.targetMapId=targetMapId;
      this.targetSpawnId=targetSpawnId;this.status=status;this.detail=detail;
    }
  }

  public interface TransitionSink {
    /** True means the runtime accepted ownership of this request. */
    boolean requestTransition(Request request);
  }

  public interface MoveTargetCanceller {
    /** Called exactly once after a transition request is accepted, never for a blocked portal. */
    void cancelMoveTargetForPortal();
  }

  private final TransitionSink sink;
  private final MoveTargetCanceller moveTargetCanceller;
  private long sequence;
  private String occupiedPortalId;
  private Request pending;
  private Snapshot snapshot=new Snapshot(0,null,null,null,Status.IDLE,null);

  public WorldPortalTransitionController(TransitionSink sink,MoveTargetCanceller moveTargetCanceller){
    if(sink==null||moveTargetCanceller==null)throw new IllegalArgumentException("sinks are required");
    this.sink=sink;this.moveTargetCanceller=moveTargetCanceller;
  }

  /**
   * Observe the closest eligible portal once per world tick. Call observeOutside() when none is in
   * range. Remaining inside a portal never emits a second transition request.
   */
  public Snapshot observe(Portal portal,float playerWorldX,float playerWorldY){
    if(portal==null)throw new IllegalArgumentException("portal is required");
    if(distance(playerWorldX,playerWorldY,portal.worldX,portal.worldY)>portal.activationRadius){
      if(portal.portalId.equals(occupiedPortalId))observeOutside();
      return set(0,portal,Status.OUTSIDE,"PLAYER_OUTSIDE_ACTIVATION_RADIUS");
    }
    if(portal.portalId.equals(occupiedPortalId)){
      if(pending!=null&&pending.portalId.equals(portal.portalId))
        return set(pending.requestId,portal,Status.PENDING,"TRANSITION_ALREADY_PENDING");
      return set(snapshot.requestId,portal,Status.LATCHED,"LEAVE_PORTAL_BEFORE_RETRY");
    }
    occupiedPortalId=portal.portalId;
    if(portal.readiness==PortalReadiness.TARGET_PENDING)
      return set(0,portal,Status.BLOCKED_TARGET_PENDING,"TARGET_MAP_OR_SPAWN_PENDING");
    if(portal.readiness==PortalReadiness.DISABLED)
      return set(0,portal,Status.BLOCKED_DISABLED,"PORTAL_DISABLED");
    if(blank(portal.targetMapId)||blank(portal.targetSpawnId))
      return set(0,portal,Status.BLOCKED_TARGET_PENDING,"TARGET_MAP_OR_SPAWN_REQUIRED");

    Request request=new Request(++sequence,portal);
    if(!sink.requestTransition(request))
      return set(request.requestId,portal,Status.REJECTED,"RUNTIME_REJECTED_REQUEST");
    pending=request;
    moveTargetCanceller.cancelMoveTargetForPortal();
    return set(request.requestId,portal,Status.REQUESTED,"RUNTIME_ACCEPTED_REQUEST");
  }

  public Snapshot observeOutside(){
    occupiedPortalId=null;
    if(pending==null)snapshot=new Snapshot(0,null,null,null,Status.OUTSIDE,"NO_PORTAL_IN_RANGE");
    return snapshot;
  }

  public Snapshot complete(long requestId,boolean success,String detail){
    if(pending==null||pending.requestId!=requestId)
      return new Snapshot(requestId,null,null,null,Status.STALE_COMPLETION,"NO_MATCHING_PENDING_REQUEST");
    Request request=pending;
    pending=null;
    snapshot=new Snapshot(request.requestId,request.portalId,request.targetMapId,request.targetSpawnId,
        success?Status.COMPLETED:Status.REJECTED,blank(detail)?(success?"TRANSITION_COMPLETED":"TRANSITION_REJECTED"):detail);
    return snapshot;
  }

  public Snapshot snapshot(){return snapshot;}
  public boolean hasPendingRequest(){return pending!=null;}

  private Snapshot set(long requestId,Portal portal,Status status,String detail){
    snapshot=new Snapshot(requestId,portal.portalId,portal.targetMapId,portal.targetSpawnId,status,detail);
    return snapshot;
  }
  private static String require(String value,String name){
    if(blank(value))throw new IllegalArgumentException(name+" is required");
    return value;
  }
  private static boolean blank(String value){return value==null||value.trim().isEmpty();}
  private static float distance(float ax,float ay,float bx,float by){
    float dx=ax-bx,dy=ay-by;return(float)Math.sqrt(dx*dx+dy*dy);
  }
}
