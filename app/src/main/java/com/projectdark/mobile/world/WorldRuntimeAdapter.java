package com.projectdark.mobile.world;

import android.graphics.RectF;
import com.projectdark.mobile.RuntimeState;

/**
 * Thin World-owned integration surface for the mobile runtime.
 * GameView/UX should call this adapter instead of duplicating camera/path/collision wiring.
 */
public final class WorldRuntimeAdapter implements WorldMoveTargetController.NavigationWorld, WorldMoveTargetController.Walker {
  public static final class FrameSnapshot {
    public final float playerWorldX,playerWorldY,playerScreenX,playerScreenY,cameraX,cameraY;
    public final WorldMoveTargetController.Snapshot movement;
    public final WorldMapProjection.Portal overlappingPortal;
    FrameSnapshot(float wx,float wy,float sx,float sy,float cx,float cy,WorldMoveTargetController.Snapshot movement,WorldMapProjection.Portal portal){
      playerWorldX=wx;playerWorldY=wy;playerScreenX=sx;playerScreenY=sy;cameraX=cx;cameraY=cy;this.movement=movement;overlappingPortal=portal;
    }
  }

  private final RuntimeState runtime;
  private final WorldMapProjection map;
  private final WorldCameraTransform camera;
  private final WorldMoveTargetController movement;

  public WorldRuntimeAdapter(RuntimeState runtime,float viewportWidth,float viewportHeight){
    if(runtime==null)throw new IllegalArgumentException("runtime required");
    if(runtime.bootMode()!=RuntimeState.BootMode.MILLES)throw new IllegalArgumentException("Milles WorldRuntimeAdapter requires MILLES boot mode");
    this.runtime=runtime;
    map=WorldMapProjection.from(runtime.world());
    camera=map.newCamera(viewportWidth,viewportHeight);
    camera.snapTo(runtime.player().x,runtime.player().y);
    movement=new WorldMoveTargetController(this,this);
  }

  public RuntimeState runtime(){return runtime;}
  public WorldMapProjection map(){return map;}
  public WorldCameraTransform camera(){return camera;}
  public WorldMoveTargetController movement(){return movement;}

  /** Empty-world tap path. UX must filter HUD/dialogue touches before calling this. */
  public WorldMoveTargetController.Snapshot requestGroundScreenTap(float screenX,float screenY){
    WorldCameraTransform.Point world=camera.screenToWorld(screenX,screenY);
    return movement.requestGroundMove(world.x,world.y);
  }

  public WorldMoveTargetController.Snapshot requestGroundWorld(float worldX,float worldY){return movement.requestGroundMove(worldX,worldY);}

  /** NPC tap path remains deliberately distinct from generic ground movement. */
  public WorldMoveTargetController.Snapshot requestNpcApproach(String npcId){
    RuntimeState.Npc npc=findNpc(npcId);
    if(npc==null)throw new IllegalArgumentException("unknown npcId: "+npcId);
    return movement.requestNpcApproach(npc.id,npc.x,npc.y,WorldMoveTargetController.DEFAULT_NPC_APPROACH_TOLERANCE);
  }

  public WorldMoveTargetController.Snapshot cancelForDirectInput(){return movement.cancelForDirectInput();}
  public WorldMoveTargetController.Snapshot cancelForAction(){return movement.cancelForAction();}
  public WorldMoveTargetController.Snapshot cancel(){return movement.cancel();}

  /** WALK first, then camera follow. Runtime collision remains authoritative for every step. */
  public FrameSnapshot tickNavigation(float deltaSeconds){
    WorldMoveTargetController.Snapshot move=movement.tick(deltaSeconds);
    camera.follow(runtime.player().x,runtime.player().y);
    return frame(move);
  }

  public void snapCameraToPlayer(){camera.snapTo(runtime.player().x,runtime.player().y);}
  public WorldCameraTransform.Point worldToScreen(float worldX,float worldY){return camera.worldToScreen(worldX,worldY);}
  public WorldCameraTransform.Point screenToWorld(float screenX,float screenY){return camera.screenToWorld(screenX,screenY);}
  public FrameSnapshot snapshot(){return frame(movement.snapshot());}

  private FrameSnapshot frame(WorldMoveTargetController.Snapshot move){
    WorldCameraTransform.Point p=camera.worldToScreen(runtime.player().x,runtime.player().y);
    return new FrameSnapshot(runtime.player().x,runtime.player().y,p.x,p.y,camera.cameraX(),camera.cameraY(),move,portalAt(runtime.player().x,runtime.player().y));
  }

  public WorldMapProjection.Portal portalAt(float worldX,float worldY){for(WorldMapProjection.Portal p:map.portals())if(p.contains(worldX,worldY))return p;return null;}

  private RuntimeState.Npc findNpc(String id){if(id==null)return null;for(RuntimeState.Npc n:runtime.npcs())if(id.equals(n.id))return n;return null;}

  @Override public float minX(){return map.bounds().minX;}
  @Override public float maxX(){return map.bounds().maxX;}
  @Override public float minY(){return map.bounds().minY;}
  @Override public float maxY(){return map.bounds().maxY;}

  /** Mirrors current RuntimeState player occupancy without making GameView know collision details. */
  @Override public boolean canPlayerOccupy(float x,float y){
    float r=RuntimeState.PLAYER_RADIUS;
    if(x<minX()||x>maxX()||y<minY()||y>maxY())return false;
    for(RectF obstacle:runtime.obstacles())if(x+r>obstacle.left&&x-r<obstacle.right&&y+r>obstacle.top&&y-r<obstacle.bottom)return false;
    for(RuntimeState.Npc n:runtime.npcs())if(distance(x,y,n.x,n.y)<r+RuntimeState.NPC_RADIUS+2f)return false;
    for(RuntimeState.Monster m:runtime.monsters())if(m.alive&&distance(x,y,m.x,m.y)<r+RuntimeState.MONSTER_RADIUS+3f)return false;
    return true;
  }

  @Override public float worldX(){return runtime.player().x;}
  @Override public float worldY(){return runtime.player().y;}
  @Override public boolean tryWalkStep(float dx,float dy){return runtime.tryMove(dx,dy);}

  private static float distance(float ax,float ay,float bx,float by){float dx=ax-bx,dy=ay-by;return(float)Math.sqrt(dx*dx+dy*dy);}
}
