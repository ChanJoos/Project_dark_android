package com.projectdark.mobile.world;

import android.graphics.Canvas;
import com.projectdark.mobile.RuntimeState;

/**
 * Canonical live-world integration surface for the Milles vertical slice.
 *
 * This class exists to make the approved runtime path explicit and difficult to bypass:
 * live world presentation is rendered from TILE/OBJECT data through AdaptedMillesMapRenderer.
 * Whole gameplay screenshots are reference-only and must not be stretched as a live map texture.
 */
public final class WorldLiveMapLayer {
  private final WorldRuntimeAdapter world;
  private final AdaptedMillesMapRenderer renderer;

  public WorldLiveMapLayer(RuntimeState runtime,float viewportWidth,float viewportHeight){
    world=new WorldRuntimeAdapter(runtime,viewportWidth,viewportHeight);
    renderer=new AdaptedMillesMapRenderer();
  }

  public WorldRuntimeAdapter world(){return world;}
  public WorldMapProjection map(){return world.map();}

  /** Draw the canonical live TILE+OBJECT world before dynamic entities. */
  public void draw(Canvas canvas){renderer.draw(canvas,world);}

  /** Advance tap navigation, then camera follow, using the same transform used for drawing. */
  public WorldRuntimeAdapter.FrameSnapshot tick(float deltaSeconds){return world.tickNavigation(deltaSeconds);}

  /** Empty visible world tap. UI/modal hit rejection remains UX-owned. */
  public WorldMoveTargetController.Snapshot requestGroundTap(float screenX,float screenY){
    return world.requestGroundScreenTap(screenX,screenY);
  }

  /** NPC approach remains distinct from generic ground tap movement. */
  public WorldMoveTargetController.Snapshot requestNpcApproach(String npcId){return world.requestNpcApproach(npcId);}

  public WorldMoveTargetController.Snapshot cancelForDirectInput(){return world.cancelForDirectInput();}
  public WorldMoveTargetController.Snapshot cancelForAction(){return world.cancelForAction();}
  public WorldCameraTransform.Point worldToScreen(float worldX,float worldY){return world.worldToScreen(worldX,worldY);}
  public WorldCameraTransform.Point screenToWorld(float screenX,float screenY){return world.screenToWorld(screenX,screenY);}
}
