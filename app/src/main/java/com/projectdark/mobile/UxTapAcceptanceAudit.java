package com.projectdark.mobile;

import com.projectdark.mobile.world.WorldCameraTransform;
import com.projectdark.mobile.world.WorldMoveTargetController;
import com.projectdark.mobile.world.WorldRuntimeAdapter;

/**
 * [ADAPTED] UX regression audit for the 2026-09-10 device-playtest tap-routing failure.
 * Verifies ten representative empty-world tap regions at the spawn camera and again after a camera move.
 */
public final class UxTapAcceptanceAudit {
  public static final int REQUIRED_POINTS=10;

  public static final class Result {
    public final int initialCameraAccepted,movedCameraAccepted;
    public final boolean passed;
    Result(int initial,int moved){initialCameraAccepted=initial;movedCameraAccepted=moved;passed=initial==REQUIRED_POINTS&&moved==REQUIRED_POINTS;}
  }

  private static final float[][] SCREEN_ANCHORS={
      {480f,270f}, // center
      {260f,250f}, // left
      {680f,250f}, // right
      {470f,92f},  // top
      {470f,410f}, // bottom safe world
      {190f,185f}, // upper-left
      {680f,155f}, // upper-right
      {285f,410f}, // lower-left outside joystick/chat
      {650f,410f}, // lower-right outside combat cluster
      {530f,340f}  // center-bottom
  };

  private UxTapAcceptanceAudit(){}

  public static boolean verify(){return run().passed;}

  public static Result run(){
    RuntimeState runtime=new RuntimeState();
    WorldRuntimeAdapter world=new WorldRuntimeAdapter(runtime,960f,540f);
    int initial=verifyCamera(world,runtime);
    float[] far=findSafeWorld(world,WorldDef.MIN_X+(WorldDef.MAX_X-WorldDef.MIN_X)*.72f,WorldDef.MIN_Y+(WorldDef.MAX_Y-WorldDef.MIN_Y)*.68f);
    if(far==null)return new Result(initial,0);
    runtime.player().x=far[0];runtime.player().y=far[1];world.snapCameraToPlayer();
    int moved=verifyCamera(world,runtime);
    return new Result(initial,moved);
  }

  private static int verifyCamera(WorldRuntimeAdapter world,RuntimeState runtime){
    int accepted=0;
    for(float[] anchor:SCREEN_ANCHORS)if(findAcceptedTap(world,runtime,anchor[0],anchor[1]))accepted++;
    return accepted;
  }

  private static boolean findAcceptedTap(WorldRuntimeAdapter world,RuntimeState runtime,float anchorX,float anchorY){
    final int[] offsets={0,16,-16,32,-32,48,-48,64,-64,80,-80,96,-96};
    for(int oy:offsets)for(int ox:offsets){
      float sx=anchorX+ox,sy=anchorY+oy;
      if(sx<8f||sx>952f||sy<8f||sy>532f||GameView.blocksWorldTapForHud(sx,sy,true))continue;
      WorldCameraTransform.Point wp=world.screenToWorld(sx,sy);
      if(!world.canPlayerOccupy(wp.x,wp.y))continue;
      if(runtime.hitNpc(wp.x,wp.y,34f)!=null||runtime.hitMonster(wp.x,wp.y,34f)!=null)continue;
      WorldMoveTargetController.Snapshot move=world.requestGroundScreenTap(sx,sy);
      boolean ok=move.status==WorldMoveTargetController.Status.MOVING||move.status==WorldMoveTargetController.Status.REACHED;
      world.cancel();
      if(ok)return true;
    }
    return false;
  }

  private static float[] findSafeWorld(WorldRuntimeAdapter world,float targetX,float targetY){
    final int[] offsets={0,32,-32,64,-64,96,-96,128,-128,160,-160,192,-192};
    for(int oy:offsets)for(int ox:offsets){float x=targetX+ox,y=targetY+oy;if(world.canPlayerOccupy(x,y))return new float[]{x,y};}
    return null;
  }
}
