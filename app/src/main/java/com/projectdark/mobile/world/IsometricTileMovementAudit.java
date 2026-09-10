package com.projectdark.mobile.world;

import java.util.ArrayList;
import java.util.List;

/** Android-free acceptance audit for exact 64x32 tile-locked gameplay movement. */
public final class IsometricTileMovementAudit {
  private static final float ORIGIN_X=320f,ORIGIN_Y=240f;

  private static final class Fixture implements WorldMoveTargetController.NavigationWorld,WorldMoveTargetController.Walker {
    final List<WorldMoveTargetController.TileCenter> tiles=new ArrayList<>();
    final List<WorldMoveTargetController.Direction> walked=new ArrayList<>();
    float x=ORIGIN_X,y=ORIGIN_Y;
    Fixture(){
      for(int row=-14;row<=14;row++)for(int column=-14;column<=14;column++)if(Math.floorMod(row-column,2)==0)
        tiles.add(new WorldMoveTargetController.TileCenter(ORIGIN_X+column*32f,ORIGIN_Y+row*16f));
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
    Fixture f=new Fixture();WorldMoveTargetController c=new WorldMoveTargetController(f,f);
    for(WorldMoveTargetController.Direction direction:WorldMoveTargetController.Direction.values()){
      f.reset();WorldMoveTargetController.Snapshot step=c.step(direction);
      if(step.status!=WorldMoveTargetController.Status.REACHED||step.lastStepDirection!=direction)return false;
      if(!close(f.x,ORIGIN_X+direction.dx)||!close(f.y,ORIGIN_Y+direction.dy))return false;
    }
    f.reset();c.step(WorldMoveTargetController.Direction.NW);c.step(WorldMoveTargetController.Direction.SE);
    if(!atOrigin(f))return false;
    c.step(WorldMoveTargetController.Direction.NE);c.step(WorldMoveTargetController.Direction.SW);
    if(!atOrigin(f))return false;
    for(int i=0;i<5;i++)c.step(WorldMoveTargetController.Direction.NW);
    for(int i=0;i<5;i++)c.step(WorldMoveTargetController.Direction.SE);
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
  private static boolean allAdjacent(List<WorldMoveTargetController.Direction> walked){return !walked.isEmpty();}
  private static boolean isAuthoredCenter(Fixture f,float x,float y){return f.canPlayerOccupy(x,y);}
  private static boolean atOrigin(Fixture f){return close(f.x,ORIGIN_X)&&close(f.y,ORIGIN_Y);}
  private static boolean close(float a,float b){return Math.abs(a-b)<.001f;}
  public static void main(String[] args){if(!verify())throw new AssertionError("IsometricTileMovementAudit failed");System.out.println("IsometricTileMovementAudit PASS");}
}
