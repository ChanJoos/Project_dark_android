package com.projectdark.mobile.world;

import android.graphics.RectF;
import com.projectdark.mobile.RuntimeState;

/** World-owned live adapter. PASS 40 movement is locked to adjacent 64x32 isometric tile centers. */
public final class WorldRuntimeAdapter implements IsometricTileMovementController.NavigationWorld, IsometricTileMovementController.Walker {
  public static final class FrameSnapshot {
    public final float playerWorldX,playerWorldY,playerScreenX,playerScreenY,cameraX,cameraY;
    public final IsometricTileMovementController.Snapshot movement;
    public final WorldMapProjection.Portal overlappingPortal;
    FrameSnapshot(float wx,float wy,float sx,float sy,float cx,float cy,IsometricTileMovementController.Snapshot movement,WorldMapProjection.Portal portal){
      playerWorldX=wx;playerWorldY=wy;playerScreenX=sx;playerScreenY=sy;cameraX=cx;cameraY=cy;this.movement=movement;overlappingPortal=portal;
    }
  }

  private final RuntimeState runtime;
  private final WorldMapProjection map;
  private final WorldCameraTransform camera;
  private final IsometricTileMovementController movement;
  private IsometricTileMovementController.Direction lastStepDirection;

  public WorldRuntimeAdapter(RuntimeState runtime,float viewportWidth,float viewportHeight){
    if(runtime==null)throw new IllegalArgumentException("runtime required");
    if(runtime.bootMode()!=RuntimeState.BootMode.MILLES)throw new IllegalArgumentException("Milles WorldRuntimeAdapter requires MILLES boot mode");
    this.runtime=runtime;map=WorldMapProjection.from(runtime.world());camera=map.newCamera(viewportWidth,viewportHeight);camera.snapTo(runtime.player().x,runtime.player().y);
    movement=new IsometricTileMovementController(this,this,(direction,fromX,fromY,toX,toY)->lastStepDirection=direction);
  }

  public RuntimeState runtime(){return runtime;} public WorldMapProjection map(){return map;} public WorldCameraTransform camera(){return camera;}
  public IsometricTileMovementController movement(){return movement;} public IsometricTileMovementController.Direction lastStepDirection(){return lastStepDirection;}

  /** Arbitrary screen tap resolves to nearest traversable authored tile. */
  public IsometricTileMovementController.Snapshot requestGroundScreenTap(float screenX,float screenY){WorldCameraTransform.Point p=camera.screenToWorld(screenX,screenY);return movement.requestGroundMove(p.x,p.y);}
  public IsometricTileMovementController.Snapshot requestGroundWorld(float worldX,float worldY){return movement.requestGroundMove(worldX,worldY);}
  /** Direct control is also one tile per command; Character can consume the same direction. */
  public IsometricTileMovementController.Snapshot step(IsometricTileMovementController.Direction direction){return movement.step(direction);}

  public IsometricTileMovementController.Snapshot cancelForDirectInput(){return movement.cancelForDirectInput();}
  public IsometricTileMovementController.Snapshot cancelForAction(){return movement.cancelForAction();}
  public IsometricTileMovementController.Snapshot cancel(){return movement.cancel();}

  /** One call consumes at most one logical tile step, then follows with camera. */
  public FrameSnapshot tickNavigation(float ignoredDeltaSeconds){IsometricTileMovementController.Snapshot move=movement.step();camera.follow(runtime.player().x,runtime.player().y);return frame(move);}
  public void snapCameraToPlayer(){camera.snapTo(runtime.player().x,runtime.player().y);}
  public WorldCameraTransform.Point worldToScreen(float x,float y){return camera.worldToScreen(x,y);} public WorldCameraTransform.Point screenToWorld(float x,float y){return camera.screenToWorld(x,y);}
  public FrameSnapshot snapshot(){return frame(movement.snapshot());}
  private FrameSnapshot frame(IsometricTileMovementController.Snapshot move){WorldCameraTransform.Point p=camera.worldToScreen(runtime.player().x,runtime.player().y);return new FrameSnapshot(runtime.player().x,runtime.player().y,p.x,p.y,camera.cameraX(),camera.cameraY(),move,portalAt(runtime.player().x,runtime.player().y));}
  public WorldMapProjection.Portal portalAt(float x,float y){for(WorldMapProjection.Portal p:map.portals())if(p.contains(x,y))return p;return null;}

  @Override public boolean canPlayerOccupy(float x,float y){
    float r=RuntimeState.PLAYER_RADIUS;WorldMapProjection.Bounds b=map.bounds();if(x-r<b.minX||x+r>b.maxX||y-r<b.minY||y+r>b.maxY)return false;
    for(RectF obstacle:runtime.obstacles())if(x+r>obstacle.left&&x-r<obstacle.right&&y+r>obstacle.top&&y-r<obstacle.bottom)return false;
    for(RuntimeState.Npc n:runtime.npcs())if(distance(x,y,n.x,n.y)<r+RuntimeState.NPC_RADIUS+2f)return false;
    for(RuntimeState.Monster m:runtime.monsters())if(m.alive&&distance(x,y,m.x,m.y)<r+RuntimeState.MONSTER_RADIUS+3f)return false;
    return true;
  }
  @Override public float worldX(){return runtime.player().x;} @Override public float worldY(){return runtime.player().y;}

  /**
   * RuntimeState.tryMove is axis-resolved. Preflight both axis intermediates plus the destination so
   * a rejected diagonal can never leave the player half a tile away and accumulate drift.
   */
  @Override public boolean tryAdjacentTileStep(float dx,float dy){
    if(!isCanonicalDelta(dx,dy))return false;float x=runtime.player().x,y=runtime.player().y;
    if(!canPlayerOccupy(x+dx,y)||!canPlayerOccupy(x,y+dy)||!canPlayerOccupy(x+dx,y+dy))return false;
    if(!runtime.tryMove(dx,dy))return false;
    return close(runtime.player().x,x+dx)&&close(runtime.player().y,y+dy);
  }

  private static boolean isCanonicalDelta(float dx,float dy){return close(Math.abs(dx),32f)&&close(Math.abs(dy),16f);}
  private static boolean close(float a,float b){return Math.abs(a-b)<0.01f;}
  private static float distance(float ax,float ay,float bx,float by){float dx=ax-bx,dy=ay-by;return(float)Math.sqrt(dx*dx+dy*dy);}
}
