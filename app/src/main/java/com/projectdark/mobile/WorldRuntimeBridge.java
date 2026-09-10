package com.projectdark.mobile;

import com.projectdark.mobile.world.WorldCameraTransform;
import com.projectdark.mobile.world.WorldMoveTargetController;

/**
 * UX-owned thin adapter over World domain camera/move-target contracts.
 *
 * <p>This class deliberately owns no pathfinding, collision, portal, or movement semantics. It
 * only adapts the existing RuntimeState public surface to World-owned contracts so GameView can
 * remain a presentation/input coordinator.</p>
 */
public final class WorldRuntimeBridge {
  private final RuntimeState state;
  private final WorldCameraTransform camera;
  private final WorldMoveTargetController moveTarget;

  public WorldRuntimeBridge(RuntimeState state,float viewportWidth,float viewportHeight){
    if(state==null)throw new IllegalArgumentException("state is required");
    this.state=state;
    camera=new WorldCameraTransform(
        RuntimeState.WORLD_MIN_X,RuntimeState.WORLD_MAX_X,
        RuntimeState.WORLD_MIN_Y,RuntimeState.WORLD_MAX_Y,
        viewportWidth,viewportHeight);
    moveTarget=new WorldMoveTargetController(
        new WorldMoveTargetController.NavigationWorld(){
          @Override public float minX(){return RuntimeState.WORLD_MIN_X;}
          @Override public float maxX(){return RuntimeState.WORLD_MAX_X;}
          @Override public float minY(){return RuntimeState.WORLD_MIN_Y;}
          @Override public float maxY(){return RuntimeState.WORLD_MAX_Y;}
          @Override public boolean canPlayerOccupy(float x,float y){
            // Static world blockers are prefiltered here; RuntimeState.tryMove remains the final
            // authority for NPC/monster occupancy and any other runtime collision rejection.
            return !WorldRuntimeBridge.this.state.blocked(x,y);
          }
        },
        new WorldMoveTargetController.Walker(){
          @Override public float worldX(){return WorldRuntimeBridge.this.state.player().x;}
          @Override public float worldY(){return WorldRuntimeBridge.this.state.player().y;}
          @Override public boolean tryWalkStep(float dx,float dy){
            return WorldRuntimeBridge.this.state.tryMove(dx,dy);
          }
        });
    camera.snapTo(state.player().x,state.player().y);
  }

  public WorldCameraTransform.Point screenToWorld(float screenX,float screenY){
    return camera.screenToWorld(screenX,screenY);
  }

  public WorldCameraTransform.Point worldToScreen(float worldX,float worldY){
    return camera.worldToScreen(worldX,worldY);
  }

  public WorldMoveTargetController.Snapshot requestGroundMove(float screenX,float screenY){
    WorldCameraTransform.Point worldPoint=camera.screenToWorld(screenX,screenY);
    return moveTarget.requestGroundMove(worldPoint.x,worldPoint.y);
  }

  public WorldMoveTargetController.Snapshot tick(float dt){
    WorldMoveTargetController.Snapshot snapshot=moveTarget.tick(dt);
    camera.follow(state.player().x,state.player().y);
    return snapshot;
  }

  public WorldMoveTargetController.Snapshot cancelForDirectInput(){
    return moveTarget.cancelForDirectInput();
  }

  public WorldMoveTargetController.Snapshot cancelForAction(){
    return moveTarget.cancelForAction();
  }

  public WorldMoveTargetController.Snapshot cancel(){return moveTarget.cancel();}
  public WorldMoveTargetController.Snapshot snapshot(){return moveTarget.snapshot();}

  public void snapCameraToPlayer(){camera.snapTo(state.player().x,state.player().y);}
}
