package com.projectdark.mobile.world;

import android.graphics.RectF;
import com.projectdark.mobile.CharacterRenderer;
import com.projectdark.mobile.RuntimeState;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Thin World-owned integration surface for the mobile runtime.
 * GameView/UX should call this adapter instead of duplicating camera/path/collision wiring.
 */
public final class WorldRuntimeAdapter implements WorldMoveTargetController.NavigationWorld, WorldMoveTargetController.Walker {
  public static final class FrameSnapshot {
    public final float playerWorldX,playerWorldY,playerScreenX,playerScreenY,cameraX,cameraY,presentationWalkClock;
    public final WorldMoveTargetController.Snapshot movement;
    public final WorldMapProjection.Portal overlappingPortal;
    FrameSnapshot(float wx,float wy,float sx,float sy,float cx,float cy,float walkClock,WorldMoveTargetController.Snapshot movement,WorldMapProjection.Portal portal){
      playerWorldX=wx;playerWorldY=wy;playerScreenX=sx;playerScreenY=sy;cameraX=cx;cameraY=cy;
      presentationWalkClock=walkClock;this.movement=movement;overlappingPortal=portal;
    }
  }

  private final RuntimeState runtime;
  private final WorldMapProjection map;
  private WorldCameraTransform camera;
  private final WorldMoveTargetController movement;
  private final WorldStepInterpolator presentation;
  private final List<WorldMoveTargetController.TileCenter> navigationTiles;
  private final float sceneMinX,sceneMaxX,sceneMinY,sceneMaxY;
  private final List<RectF> sceneObstacles;
  private final boolean millesActors;
  private float presentationWalkClock;

  public WorldRuntimeAdapter(RuntimeState runtime,float viewportWidth,float viewportHeight){
    if(runtime==null)throw new IllegalArgumentException("runtime required");
    if(runtime.bootMode()!=RuntimeState.BootMode.MILLES)throw new IllegalArgumentException("Milles WorldRuntimeAdapter requires MILLES boot mode");
    this.runtime=runtime;
    map=WorldMapProjection.from(runtime.world());
    List<WorldMoveTargetController.TileCenter> centers=new ArrayList<>();
    for(AdaptedMillesIsometricTileLayer.Tile tile:map.tiles())centers.add(new WorldMoveTargetController.TileCenter(tile.centerX,tile.centerY));
    navigationTiles=Collections.unmodifiableList(centers);
    sceneMinX=map.bounds().minX;sceneMaxX=map.bounds().maxX;sceneMinY=map.bounds().minY;sceneMaxY=map.bounds().maxY;
    sceneObstacles=runtime.obstacles();millesActors=true;
    snapPlayerToNearestTraversableTile();
    camera=map.newCamera(viewportWidth,viewportHeight);
    camera.snapTo(runtime.player().x,runtime.player().y);
    movement=new WorldMoveTargetController(this,this);
    presentation=new WorldStepInterpolator(WorldMoveTargetController.TILE_STEP_SECONDS,runtime.player().x,runtime.player().y);
    CharacterRenderer.setPresentationWalkClock(0f);
  }


  /** Same canonical world movement/camera stack for non-Milles maps such as interiors. */
  public WorldRuntimeAdapter(RuntimeState runtime,float viewportWidth,float viewportHeight,
      float minX,float maxX,float minY,float maxY,List<WorldMoveTargetController.TileCenter> tiles,List<RectF> obstacles){
    this(runtime,viewportWidth,viewportHeight,minX,maxX,minY,maxY,tiles,obstacles,false);
  }

  public WorldRuntimeAdapter(RuntimeState runtime,float viewportWidth,float viewportHeight,
      float minX,float maxX,float minY,float maxY,List<WorldMoveTargetController.TileCenter> tiles,List<RectF> obstacles,boolean actorsBlockMovement){
    if(runtime==null||tiles==null||tiles.isEmpty())throw new IllegalArgumentException("runtime and authored tiles required");
    this.runtime=runtime;this.map=null;
    this.navigationTiles=Collections.unmodifiableList(new ArrayList<>(tiles));
    sceneMinX=minX;sceneMaxX=maxX;sceneMinY=minY;sceneMaxY=maxY;
    sceneObstacles=obstacles==null?Collections.emptyList():Collections.unmodifiableList(new ArrayList<>(obstacles));millesActors=actorsBlockMovement;
    snapPlayerToNearestTraversableTile();
    camera=new WorldCameraTransform(minX,maxX,minY,maxY,viewportWidth,viewportHeight);
    camera.snapTo(runtime.player().x,runtime.player().y);
    movement=new WorldMoveTargetController(this,this);
    presentation=new WorldStepInterpolator(WorldMoveTargetController.TILE_STEP_SECONDS,runtime.player().x,runtime.player().y);
    CharacterRenderer.setPresentationWalkClock(0f);
  }

  public RuntimeState runtime(){return runtime;}
  public WorldMapProjection map(){return map;}
  public WorldCameraTransform camera(){return camera;}
  /** Resize only the presentation viewport; world coordinates and navigation remain unchanged. */
  public void resizeViewport(float width,float height){
    if(width<=0f||height<=0f)throw new IllegalArgumentException("viewport must be positive");
    float x=presentation==null?runtime.player().x:presentation.x();
    float y=presentation==null?runtime.player().y:presentation.y();
    camera=map!=null?map.newCamera(width,height):new WorldCameraTransform(sceneMinX,sceneMaxX,sceneMinY,sceneMaxY,width,height);
    camera.snapTo(x,y);
  }
  public WorldMoveTargetController movement(){return movement;}

  /** Empty-world tap path. UX must filter HUD/dialogue touches before calling this. */
  public WorldMoveTargetController.Snapshot requestGroundScreenTap(float screenX,float screenY){
    WorldCameraTransform.Point world=camera.screenToWorld(screenX,screenY);
    return movement.requestGroundMove(world.x,world.y);
  }

  public WorldMoveTargetController.Snapshot requestGroundWorld(float worldX,float worldY){return movement.requestGroundMove(worldX,worldY);}

  /** Joystick/gamepad input: exactly one NW/NE/SW/SE logical tile. */
  public WorldMoveTargetController.Snapshot step(WorldMoveTargetController.Direction direction){
    if(presentation.active())return movement.snapshot();
    WorldMoveTargetController.Snapshot result=movement.step(direction);beginPresentationIfMoved();return result;
  }

  /** NPC tap path remains deliberately distinct from generic ground movement. */
  public WorldMoveTargetController.Snapshot requestNpcApproach(String npcId){
    RuntimeState.Npc npc=findNpc(npcId);
    if(npc==null)throw new IllegalArgumentException("unknown npcId: "+npcId);
    return movement.requestNpcApproach(npc.id,npc.x,npc.y,WorldMoveTargetController.DEFAULT_NPC_APPROACH_TOLERANCE);
  }

  /** Combat approach is separate from both empty-ground taps and NPC interaction. */
  public WorldMoveTargetController.Snapshot requestMonsterApproach(String monsterId,float approachTolerance){
    RuntimeState.Monster monster=findMonster(monsterId);
    if(monster==null||!monster.alive)throw new IllegalArgumentException("unknown or defeated monsterId: "+monsterId);
    return movement.requestMonsterApproach(monster.id,monster.x,monster.y,Math.max(1f,approachTolerance));
  }

  public WorldMoveTargetController.Snapshot cancelForDirectInput(){return movement.cancelForDirectInput();}
  public WorldMoveTargetController.Snapshot cancelForAction(){WorldMoveTargetController.Snapshot result=movement.cancelForAction();presentation.snap(runtime.player().x,runtime.player().y);return result;}
  public WorldMoveTargetController.Snapshot cancel(){WorldMoveTargetController.Snapshot result=movement.cancel();presentation.snap(runtime.player().x,runtime.player().y);return result;}

  /** WALK first, then camera follow. Runtime collision remains authoritative for every step. */
  public FrameSnapshot tickNavigation(float deltaSeconds){
    float remaining=Math.max(0f,deltaSeconds);
    WorldMoveTargetController.Snapshot move=movement.snapshot();
    while(remaining>0f){
      if(presentation.active()){
        float before=remaining;
        remaining=presentation.advance(remaining);
        float consumed=Math.max(0f,before-remaining);
        presentationWalkClock+=consumed;
        CharacterRenderer.setPresentationWalkClock(presentationWalkClock);
        if(remaining>=before-.000001f)break;
        continue;
      }
      if(move.status!=WorldMoveTargetController.Status.MOVING)break;
      move=movement.tick(WorldMoveTargetController.TILE_STEP_SECONDS);beginPresentationIfMoved();
      if(!presentation.active())break;
    }
    camera.follow(presentation.x(),presentation.y());
    return frame(move);
  }

  /** Re-establishes the tile-center invariant after spawn/revive before snapping the camera. */
  public void snapCameraToPlayer(){snapPlayerToNearestTraversableTile();presentation.snap(runtime.player().x,runtime.player().y);presentationWalkClock=0f;CharacterRenderer.setPresentationWalkClock(0f);camera.snapTo(runtime.player().x,runtime.player().y);}
  public float presentationPlayerX(){return presentation.x();}
  public float presentationPlayerY(){return presentation.y();}
  public float presentationWalkClock(){return presentationWalkClock;}
  public boolean presentationMoving(){return presentation.active();}
  public WorldCameraTransform.Point worldToScreen(float worldX,float worldY){return camera.worldToScreen(worldX,worldY);}
  public WorldCameraTransform.Point screenToWorld(float screenX,float screenY){return camera.screenToWorld(screenX,screenY);}
  public FrameSnapshot snapshot(){return frame(movement.snapshot());}

  private FrameSnapshot frame(WorldMoveTargetController.Snapshot move){
    WorldCameraTransform.Point p=camera.worldToScreen(presentation.x(),presentation.y());
    return new FrameSnapshot(presentation.x(),presentation.y(),p.x,p.y,camera.cameraX(),camera.cameraY(),presentationWalkClock,move,portalAt(runtime.player().x,runtime.player().y));
  }

  private void beginPresentationIfMoved(){
    if(Math.abs(presentation.x()-runtime.player().x)>.0001f||Math.abs(presentation.y()-runtime.player().y)>.0001f)
      presentation.begin(runtime.player().x,runtime.player().y);
  }

  public WorldMapProjection.Portal portalAt(float worldX,float worldY){if(map==null)return null;for(WorldMapProjection.Portal p:map.portals())if(p.contains(worldX,worldY))return p;return null;}

  private RuntimeState.Npc findNpc(String id){if(id==null)return null;for(RuntimeState.Npc n:runtime.npcs())if(id.equals(n.id))return n;return null;}

  @Override public List<WorldMoveTargetController.TileCenter> navigationTiles(){return navigationTiles;}

  /** Mirrors current RuntimeState player occupancy without making GameView know collision details. */
  @Override public boolean canPlayerOccupy(float x,float y){
    float r=RuntimeState.PLAYER_RADIUS;
    if(x-r<sceneMinX||x+r>sceneMaxX||y-r<sceneMinY||y+r>sceneMaxY)return false;
    for(RectF obstacle:sceneObstacles)if(x+r>obstacle.left&&x-r<obstacle.right&&y+r>obstacle.top&&y-r<obstacle.bottom)return false;
    if(millesActors){for(RuntimeState.Npc n:runtime.npcs())if(distance(x,y,n.x,n.y)<r+RuntimeState.NPC_RADIUS+2f)return false;
    for(RuntimeState.Monster m:runtime.monsters())if(m.alive&&distance(x,y,m.x,m.y)<r+RuntimeState.MONSTER_RADIUS+3f)return false;}
    return true;
  }

  /** Obstacle-only sight test; actors do not occlude their own attack endpoints. */
  public boolean hasCombatLineOfSight(String actorId,String targetId){
    float[] a=combatPosition(actorId),b=combatPosition(targetId);if(a==null||b==null)return false;
    int steps=Math.max(1,(int)Math.ceil(distance(a[0],a[1],b[0],b[1])/4f));
    for(int i=0;i<=steps;i++){
      float t=i/(float)steps,x=a[0]+(b[0]-a[0])*t,y=a[1]+(b[1]-a[1])*t;
      for(RectF obstacle:sceneObstacles)if(obstacle.contains(x,y))return false;
    }
    return true;
  }
  private float[] combatPosition(String id){
    if("player".equals(id))return new float[]{runtime.player().x,runtime.player().y};
    for(RuntimeState.Monster m:runtime.monsters())if(m.id.equals(id))return new float[]{m.x,m.y};
    return null;
  }

  @Override public float worldX(){return runtime.player().x;}
  @Override public float worldY(){return runtime.player().y;}
  @Override public boolean moveToAdjacentTile(float destinationX,float destinationY,WorldMoveTargetController.Direction direction){
    float startX=runtime.player().x,startY=runtime.player().y;
    if(WorldMoveTargetController.Direction.between(startX,startY,destinationX,destinationY)!=direction)return false;
    if(!canPlayerOccupy(destinationX,destinationY)||!segmentTraversable(startX,startY,destinationX,destinationY))return false;
    runtime.player().x=destinationX;
    runtime.player().y=destinationY;
    return true;
  }

  private boolean segmentTraversable(float ax,float ay,float bx,float by){
    float r=RuntimeState.PLAYER_RADIUS;
    int steps=Math.max(2,(int)Math.ceil(distance(ax,ay,bx,by)/4f));
    for(int i=1;i<=steps;i++){
      float t=i/(float)steps,x=ax+(bx-ax)*t,y=ay+(by-ay)*t;
      if(x-r<sceneMinX||x+r>sceneMaxX||y-r<sceneMinY||y+r>sceneMaxY)return false;
      for(RectF obstacle:sceneObstacles)if(x+r>obstacle.left&&x-r<obstacle.right&&y+r>obstacle.top&&y-r<obstacle.bottom)return false;
    }
    return true;
  }

  private void snapPlayerToNearestTraversableTile(){
    WorldMoveTargetController.TileCenter best=null;float bestDistance=Float.MAX_VALUE;
    for(WorldMoveTargetController.TileCenter tile:navigationTiles){
      if(!canPlayerOccupy(tile.x,tile.y))continue;
      float dx=tile.x-runtime.player().x,dy=tile.y-runtime.player().y,d=dx*dx+dy*dy;
      if(d<bestDistance){bestDistance=d;best=tile;}
    }
    if(best==null)throw new IllegalStateException("no traversable spawn tile");
    runtime.player().x=best.x;runtime.player().y=best.y;
  }

  private RuntimeState.Monster findMonster(String id){if(id==null)return null;for(RuntimeState.Monster m:runtime.monsters())if(id.equals(m.id))return m;return null;}

  private static float distance(float ax,float ay,float bx,float by){float dx=ax-bx,dy=ay-by;return(float)Math.sqrt(dx*dx+dy*dy);}
}
