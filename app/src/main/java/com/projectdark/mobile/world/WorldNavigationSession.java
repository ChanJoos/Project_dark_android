package com.projectdark.mobile.world;

/**
 * [ADAPTED] Integration boundary for world navigation.
 *
 * One tick always advances logical WALK movement first, follows the resulting world position with
 * the camera second, and observes a portal last. A runtime-accepted portal request cancels the
 * active move target with a dedicated reason. This class owns no touch hit-testing or rendering.
 */
public final class WorldNavigationSession {
  public static final class Snapshot {
    public final float playerWorldX,playerWorldY,playerScreenX,playerScreenY,cameraX,cameraY;
    public final WorldMoveTargetController.Snapshot moveTarget;
    public final WorldPortalTransitionController.Snapshot portal;
    private Snapshot(float wx,float wy,WorldCameraTransform.Point screen,WorldCameraTransform camera,
        WorldMoveTargetController.Snapshot move,WorldPortalTransitionController.Snapshot portal){
      playerWorldX=wx;playerWorldY=wy;playerScreenX=screen.x;playerScreenY=screen.y;
      cameraX=camera.cameraX();cameraY=camera.cameraY();moveTarget=move;this.portal=portal;
    }
  }

  private final WorldMoveTargetController.Walker walker;
  private final WorldMoveTargetController moveTarget;
  private final WorldCameraTransform camera;
  private final WorldPortalTransitionController portals;

  public WorldNavigationSession(WorldMoveTargetController.NavigationWorld world,
      WorldMoveTargetController.Walker walker,WorldCameraTransform camera,
      WorldPortalTransitionController.TransitionSink transitionSink){
    if(world==null||walker==null||camera==null||transitionSink==null)
      throw new IllegalArgumentException("world, walker, camera and transitionSink are required");
    this.walker=walker;this.camera=camera;
    moveTarget=new WorldMoveTargetController(world,walker);
    portals=new WorldPortalTransitionController(transitionSink,moveTarget::cancelForPortalTransition);
    camera.snapTo(walker.worldX(),walker.worldY());
  }

  public WorldMoveTargetController.Snapshot requestGroundMove(float worldX,float worldY){return moveTarget.requestGroundMove(worldX,worldY);}
  public WorldMoveTargetController.Snapshot requestNpcApproach(String npcId,float worldX,float worldY,float tolerance){return moveTarget.requestNpcApproach(npcId,worldX,worldY,tolerance);}
  public WorldMoveTargetController.Snapshot cancelForDirectInput(){return moveTarget.cancelForDirectInput();}
  public WorldMoveTargetController.Snapshot cancelForAction(){return moveTarget.cancelForAction();}
  public WorldMoveTargetController.Snapshot cancelMoveTarget(){return moveTarget.cancel();}

  /** Nullable observedPortal means no portal is in activation range this tick. */
  public Snapshot tick(float deltaSeconds,WorldPortalTransitionController.Portal observedPortal){
    moveTarget.tick(deltaSeconds);
    camera.follow(walker.worldX(),walker.worldY());
    WorldPortalTransitionController.Snapshot portal=observedPortal==null
        ?portals.observeOutside():portals.observe(observedPortal,walker.worldX(),walker.worldY());
    return capture(moveTarget.snapshot(),portal);
  }

  public WorldPortalTransitionController.Snapshot completeTransition(long requestId,boolean success,String detail){
    return portals.complete(requestId,success,detail);
  }
  public void snapCameraToPlayer(){camera.snapTo(walker.worldX(),walker.worldY());}
  public WorldCameraTransform.Point screenToWorld(float screenX,float screenY){return camera.screenToWorld(screenX,screenY);}
  public WorldCameraTransform.Point worldToScreen(float worldX,float worldY){return camera.worldToScreen(worldX,worldY);}
  public Snapshot snapshot(){return capture(moveTarget.snapshot(),portals.snapshot());}

  private Snapshot capture(WorldMoveTargetController.Snapshot move,WorldPortalTransitionController.Snapshot portal){
    float x=walker.worldX(),y=walker.worldY();
    return new Snapshot(x,y,camera.worldToScreen(x,y),camera,move,portal);
  }
}
