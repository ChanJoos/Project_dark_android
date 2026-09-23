package com.projectdark.mobile.world;

import com.projectdark.mobile.RuntimeState;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Interior navigation adapter. It intentionally reuses WorldMoveTargetController and
 * WorldStepInterpolator instead of introducing an interior-specific movement model.
 */
public final class ReagentShopWorldRuntime implements WorldMoveTargetController.NavigationWorld,WorldMoveTargetController.Walker {
  private final RuntimeState runtime;
  private final WorldMoveTargetController movement;
  private final WorldStepInterpolator presentation;
  private final WorldCameraTransform camera;
  private final List<WorldMoveTargetController.TileCenter> navigationTiles;

  public ReagentShopWorldRuntime(RuntimeState runtime){
    if(runtime==null)throw new IllegalArgumentException("runtime required");
    this.runtime=runtime;
    ArrayList<WorldMoveTargetController.TileCenter> c=new ArrayList<>();
    for(ReagentShopMapDef.Tile t:ReagentShopMapDef.tiles())c.add(new WorldMoveTargetController.TileCenter(t.centerX,t.centerY));
    navigationTiles=Collections.unmodifiableList(c);
    runtime.player().x=ReagentShopMapDef.ENTRY_X;
    runtime.player().y=ReagentShopMapDef.ENTRY_Y;
    movement=new WorldMoveTargetController(this,this);
    presentation=new WorldStepInterpolator(WorldMoveTargetController.TILE_STEP_SECONDS,runtime.player().x,runtime.player().y);
    camera=new WorldCameraTransform(ReagentShopMapDef.MIN_X,ReagentShopMapDef.MAX_X,ReagentShopMapDef.MIN_Y,ReagentShopMapDef.MAX_Y,960f,540f);
    camera.snapTo(runtime.player().x,runtime.player().y);
  }

  public WorldMoveTargetController movement(){return movement;}
  public float presentationX(){return presentation.x();}
  public float presentationY(){return presentation.y();}
  public boolean presentationMoving(){return presentation.active();}
  public WorldCameraTransform camera(){return camera;}
  public WorldCameraTransform.Point worldToScreen(float x,float y){return camera.worldToScreen(x,y);}
  public WorldCameraTransform.Point screenToWorld(float x,float y){return camera.screenToWorld(x,y);}
  public WorldMoveTargetController.Snapshot requestGroundScreenTap(float x,float y){WorldCameraTransform.Point w=camera.screenToWorld(x,y);return movement.requestGroundMove(w.x,w.y);}

  public WorldMoveTargetController.Snapshot step(WorldMoveTargetController.Direction d){
    if(presentation.active())return movement.snapshot();
    WorldMoveTargetController.Snapshot s=movement.step(d);beginPresentationIfMoved();return s;
  }
  public WorldMoveTargetController.Snapshot requestGround(float x,float y){return movement.requestGroundMove(x,y);}
  public WorldMoveTargetController.Snapshot cancel(){WorldMoveTargetController.Snapshot s=movement.cancel();presentation.snap(runtime.player().x,runtime.player().y);return s;}

  public WorldMoveTargetController.Snapshot tick(float dt){
    float remaining=Math.max(0f,dt);WorldMoveTargetController.Snapshot s=movement.snapshot();
    while(remaining>0f){
      if(presentation.active()){float before=remaining;remaining=presentation.advance(remaining);if(remaining>=before-.000001f)break;continue;}
      if(s.status!=WorldMoveTargetController.Status.MOVING)break;
      s=movement.tick(WorldMoveTargetController.TILE_STEP_SECONDS);beginPresentationIfMoved();if(!presentation.active())break;
    }
    camera.follow(presentation.x(),presentation.y());
    return s;
  }

  public boolean atExitPortal(){
    float dx=runtime.player().x-ReagentShopMapDef.ENTRY_X,dy=runtime.player().y-(ReagentShopMapDef.ENTRY_Y+16f);
    float r=ReagentShopMapDef.exitPortal().activationRadius;return dx*dx+dy*dy<=r*r;
  }

  @Override public List<WorldMoveTargetController.TileCenter> navigationTiles(){return navigationTiles;}
  @Override public float worldX(){return runtime.player().x;}
  @Override public float worldY(){return runtime.player().y;}
  @Override public boolean canPlayerOccupy(float x,float y){return ReagentShopMapDef.canOccupy(x,y,RuntimeState.PLAYER_RADIUS);}
  @Override public boolean moveToAdjacentTile(float x,float y,WorldMoveTargetController.Direction d){
    if(WorldMoveTargetController.Direction.between(runtime.player().x,runtime.player().y,x,y)!=d)return false;
    if(!canPlayerOccupy(x,y))return false;
    runtime.player().x=x;runtime.player().y=y;return true;
  }
  private void beginPresentationIfMoved(){if(Math.abs(presentation.x()-runtime.player().x)>.0001f||Math.abs(presentation.y()-runtime.player().y)>.0001f)presentation.begin(runtime.player().x,runtime.player().y);}
}
