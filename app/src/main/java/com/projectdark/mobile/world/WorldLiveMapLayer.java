package com.projectdark.mobile.world;

import android.graphics.Canvas;
import com.projectdark.mobile.RuntimeState;

/** Canonical TILE/OBJECT live-world surface with tile-locked movement. */
public final class WorldLiveMapLayer {
  private final WorldRuntimeAdapter world; private final AdaptedMillesMapRenderer renderer;
  public WorldLiveMapLayer(RuntimeState runtime,float viewportWidth,float viewportHeight){world=new WorldRuntimeAdapter(runtime,viewportWidth,viewportHeight);renderer=new AdaptedMillesMapRenderer();}
  public WorldRuntimeAdapter world(){return world;} public WorldMapProjection map(){return world.map();}
  public void draw(Canvas canvas){renderer.draw(canvas,world);}
  public WorldRuntimeAdapter.FrameSnapshot tick(float deltaSeconds){return world.tickNavigation(deltaSeconds);}
  public IsometricTileMovementController.Snapshot requestGroundTap(float screenX,float screenY){return world.requestGroundScreenTap(screenX,screenY);}
  public IsometricTileMovementController.Snapshot step(IsometricTileMovementController.Direction direction){return world.step(direction);}
  public IsometricTileMovementController.Snapshot cancelForDirectInput(){return world.cancelForDirectInput();}
  public IsometricTileMovementController.Snapshot cancelForAction(){return world.cancelForAction();}
  public IsometricTileMovementController.Snapshot cancel(){return world.cancel();}
  public IsometricTileMovementController.Direction lastStepDirection(){return world.lastStepDirection();}
  public WorldCameraTransform.Point worldToScreen(float x,float y){return world.worldToScreen(x,y);} public WorldCameraTransform.Point screenToWorld(float x,float y){return world.screenToWorld(x,y);}
}
