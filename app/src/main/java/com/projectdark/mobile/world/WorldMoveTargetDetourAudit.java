package com.projectdark.mobile.world;

import java.util.HashSet;
import java.util.Set;

/** Android-free regression for proactive detour and fail-closed topology changes. */
public final class WorldMoveTargetDetourAudit {
  private WorldMoveTargetDetourAudit(){}

  private static final class GridWorld implements WorldMoveTargetController.NavigationWorld,WorldMoveTargetController.Walker {
    float x=16f,y=48f; boolean gateClosed=false; boolean hardWall=false; int walked=0;
    public float minX(){return 0f;} public float maxX(){return 160f;} public float minY(){return 0f;} public float maxY(){return 96f;}
    public float worldX(){return x;} public float worldY(){return y;}
    public boolean canPlayerOccupy(float wx,float wy){
      if(wx<0f||wx>160f||wy<0f||wy>96f)return false;
      int cx=Math.round(wx/16f),cy=Math.round(wy/16f);
      if(hardWall&&cx==5)return false;
      if(gateClosed&&cx==5&&cy==3)return false;
      return true;
    }
    public boolean tryWalkStep(float dx,float dy){float nx=x+dx,ny=y+dy;if(!canPlayerOccupy(nx,ny))return false;x=nx;y=ny;walked++;return true;}
  }

  private static WorldMoveTargetController.Status run(WorldMoveTargetController c,int maxTicks){
    WorldMoveTargetController.Snapshot s=c.snapshot();
    for(int i=0;i<maxTicks&&s.status==WorldMoveTargetController.Status.MOVING;i++)s=c.tick(.08f);
    return s.status;
  }

  public static boolean verify(){
    // Route initially uses the center lane. Closing one center cell must trigger a proactive detour.
    GridWorld detourWorld=new GridWorld();
    WorldMoveTargetController detour=new WorldMoveTargetController(detourWorld,detourWorld);
    if(detour.requestGroundMove(144f,48f).status!=WorldMoveTargetController.Status.MOVING)return false;
    for(int i=0;i<5;i++)detour.tick(.08f);
    float beforeX=detourWorld.x;
    detourWorld.gateClosed=true;
    detour.notifyNavigationChanged();
    if(run(detour,300)!=WorldMoveTargetController.Status.REACHED)return false;
    if(detourWorld.walked<=0||detourWorld.x<=beforeX)return false;
    if(Math.abs(detourWorld.y-48f)>6.1f)return false;

    // A topology update that removes every passage must fail closed before stale-path walking continues.
    GridWorld sealedWorld=new GridWorld();
    WorldMoveTargetController sealed=new WorldMoveTargetController(sealedWorld,sealedWorld);
    if(sealed.requestGroundMove(144f,48f).status!=WorldMoveTargetController.Status.MOVING)return false;
    for(int i=0;i<4;i++)sealed.tick(.08f);
    sealedWorld.hardWall=true;
    float sealedX=sealedWorld.x,sealedY=sealedWorld.y;
    sealed.notifyNavigationChanged();
    WorldMoveTargetController.Snapshot blocked=sealed.tick(.08f);
    if(blocked.status!=WorldMoveTargetController.Status.BLOCKED)return false;
    if(Math.abs(sealedWorld.x-sealedX)>.001f||Math.abs(sealedWorld.y-sealedY)>.001f)return false;

    // Notification while idle/reached must be harmless and must not fabricate a request.
    GridWorld idleWorld=new GridWorld();
    WorldMoveTargetController idle=new WorldMoveTargetController(idleWorld,idleWorld);
    if(idle.notifyNavigationChanged().status!=WorldMoveTargetController.Status.IDLE)return false;
    return true;
  }

  public static void main(String[] args){if(!verify())throw new AssertionError("World detour audit failed");System.out.println("WorldMoveTargetDetourAudit PASS");}
}
