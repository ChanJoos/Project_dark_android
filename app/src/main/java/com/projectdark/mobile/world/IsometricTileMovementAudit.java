package com.projectdark.mobile.world;

import java.util.ArrayList;
import java.util.List;

/** Android-free acceptance audit for exact 64x32 tile-locked gameplay movement. */
public final class IsometricTileMovementAudit {
  private static final float ORIGIN_X=320f,ORIGIN_Y=240f;
  private static final int DRIFT_STEPS=64;

  private static final class Fixture implements WorldMoveTargetController.NavigationWorld,WorldMoveTargetController.Walker {
    final List<WorldMoveTargetController.TileCenter> tiles=new ArrayList<>();
    final List<WorldMoveTargetController.Direction> walked=new ArrayList<>();
    float x=ORIGIN_X,y=ORIGIN_Y;
    Fixture(){
      for(int row=-80;row<=80;row++)for(int column=-80;column<=80;column++)if(Math.floorMod(row-column,2)==0)
        tiles.add(new WorldMoveTargetController.TileCenter(
          ORIGIN_X+column*AdaptedMillesIsometricTileLayer.HALF_WIDTH,
          ORIGIN_Y+row*AdaptedMillesIsometricTileLayer.HALF_HEIGHT));
    }
    public List<WorldMoveTargetController.TileCenter> navigationTiles(){return tiles;}
    public boolean canPlayerOccupy(float wx,float wy){for(WorldMoveTargetController.TileCenter t:tiles)if(close(t.x,wx)&&close(t.y,wy))return true;return false;}
    public float worldX(){return x;}public float worldY(){return y;}
    public boolean moveToAdjacentTile(float destinationX,float destinationY,WorldMoveTargetController.Direction direction){
      if(WorldMoveTargetController.Direction.between(x,y,destinationX,destinationY)!=direction)return false;
      x=destinationX;y=destinationY;walked.add(direction);return true;
    }
    void reset(){x=ORIGIN_X;y=ORIGIN_Y;walked.clear();}
  }

  private IsometricTileMovementAudit(){}
  public static boolean verify(){
    if(!close(AdaptedMillesIsometricTileLayer.TILE_WIDTH,64f)||!close(AdaptedMillesIsometricTileLayer.TILE_HEIGHT,32f))return false;
    if(!close(AdaptedMillesIsometricTileLayer.HALF_WIDTH,32f)||!close(AdaptedMillesIsometricTileLayer.HALF_HEIGHT,16f))return false;

    Fixture f=new Fixture();WorldMoveTargetController c=new WorldMoveTargetController(f,f);
    for(WorldMoveTargetController.Direction direction:WorldMoveTargetController.Direction.values()){
      f.reset();WorldMoveTargetController.Snapshot step=c.step(direction);
      if(step.status!=WorldMoveTargetController.Status.REACHED||step.lastStepDirection!=direction)return false;
      if(!close(f.x,ORIGIN_X+direction.dx)||!close(f.y,ORIGIN_Y+direction.dy))return false;
      if(!close(Math.abs(direction.dx),AdaptedMillesIsometricTileLayer.HALF_WIDTH))return false;
      if(!close(Math.abs(direction.dy),AdaptedMillesIsometricTileLayer.HALF_HEIGHT))return false;
    }

    // Long deterministic runs must land on the exact authored cell center with no accumulated drift.
    f.reset();
    for(int i=0;i<DRIFT_STEPS;i++){
      WorldMoveTargetController.Snapshot step=c.step(WorldMoveTargetController.Direction.SE);
      if(step.status!=WorldMoveTargetController.Status.REACHED)return false;
      float expectedX=ORIGIN_X+(i+1)*AdaptedMillesIsometricTileLayer.HALF_WIDTH;
      float expectedY=ORIGIN_Y+(i+1)*AdaptedMillesIsometricTileLayer.HALF_HEIGHT;
      if(!close(f.x,expectedX)||!close(f.y,expectedY))return false;
    }
    for(int i=0;i<DRIFT_STEPS;i++){
      WorldMoveTargetController.Snapshot step=c.step(WorldMoveTargetController.Direction.NW);
      if(step.status!=WorldMoveTargetController.Status.REACHED)return false;
    }
    if(!atOrigin(f))return false;

    f.reset();c.step(WorldMoveTargetController.Direction.NW);c.step(WorldMoveTargetController.Direction.SE);
    if(!atOrigin(f))return false;
    c.step(WorldMoveTargetController.Direction.NE);c.step(WorldMoveTargetController.Direction.SW);
    if(!atOrigin(f))return false;

    // Off-grid taps snap to authored centers. Camera placement must not change the world goal.
    f.reset();WorldCameraTransform camera=new WorldCameraTransform(0,900,0,700,640,360);camera.snapTo(ORIGIN_X,ORIGIN_Y);
    float requestedX=ORIGIN_X+319.2f,requestedY=ORIGIN_Y+3.7f;
    WorldCameraTransform.Point screen=camera.worldToScreen(requestedX,requestedY);
    WorldCameraTransform.Point roundTrip=camera.screenToWorld(screen.x,screen.y);
    WorldMoveTargetController.Snapshot request=c.requestGroundMove(roundTrip.x,roundTrip.y);
    if(request.status!=WorldMoveTargetController.Status.MOVING)return false;
    if(!isAuthoredCenter(f,request.targetX,request.targetY))return false;
    int guard=0;while(c.snapshot().status==WorldMoveTargetController.Status.MOVING&&guard++<200)c.tick(WorldMoveTargetController.TILE_STEP_SECONDS);
    if(c.snapshot().status!=WorldMoveTargetController.Status.REACHED||!close(f.x,request.targetX)||!close(f.y,request.targetY))return false;
    if(!allAdjacent(f.walked))return false;

    // Both left and right routes must be made only from the same four exact deltas.
    f.reset();if(!walkTo(c,ORIGIN_X-320f,ORIGIN_Y)||!allAdjacent(f.walked))return false;
    f.reset();if(!walkTo(c,ORIGIN_X+320f,ORIGIN_Y)||!allAdjacent(f.walked))return false;
    return true;
  }
  private static boolean walkTo(WorldMoveTargetController c,float x,float y){
    if(c.requestGroundMove(x,y).status!=WorldMoveTargetController.Status.MOVING)return false;
    int guard=0;while(c.snapshot().status==WorldMoveTargetController.Status.MOVING&&guard++<200)c.tick(WorldMoveTargetController.TILE_STEP_SECONDS);
    return c.snapshot().status==WorldMoveTargetController.Status.REACHED;
  }
  private static boolean allAdjacent(List<WorldMoveTargetController.Direction> walked){
    if(walked.isEmpty())return false;
    for(WorldMoveTargetController.Direction d:walked){
      if(!close(Math.abs(d.dx),AdaptedMillesIsometricTileLayer.HALF_WIDTH)||!close(Math.abs(d.dy),AdaptedMillesIsometricTileLayer.HALF_HEIGHT))return false;
    }
    return true;
  }
  private static boolean isAuthoredCenter(Fixture f,float x,float y){return f.canPlayerOccupy(x,y);}
  private static boolean atOrigin(Fixture f){return close(f.x,ORIGIN_X)&&close(f.y,ORIGIN_Y);}
  private static boolean close(float a,float b){return Math.abs(a-b)<.001f;}
  public static void main(String[] args){if(!verify())throw new AssertionError("IsometricTileMovementAudit failed");System.out.println("IsometricTileMovementAudit PASS");}
}
