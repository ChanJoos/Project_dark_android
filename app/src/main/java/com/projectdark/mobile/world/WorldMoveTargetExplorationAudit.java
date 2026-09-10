package com.projectdark.mobile.world;

import java.util.Arrays;
import java.util.List;

/**
 * Android-free end-to-end regression for expanded Milles prototype tap movement.
 * It drives the real WorldMoveTargetController from spawn through stable exploration anchors.
 */
public final class WorldMoveTargetExplorationAudit {
  private static final float MIN_X=64f,MAX_X=1664f,MIN_Y=48f,MAX_Y=1168f;
  private static final float PLAYER_RADIUS=9f,NPC_RADIUS=10f,MONSTER_RADIUS=11f;
  private static final float[][] BLOCKERS={
      {150f,105f,390f,300f},{540f,90f,800f,280f},{1040f,110f,1320f,315f},
      {120f,430f,350f,650f},{1210f,410f,1480f,660f},{330f,760f,570f,980f},
      {1000f,770f,1260f,995f},{700f,470f,765f,535f},{850f,625f,920f,690f},
      {80f,820f,250f,1050f},{1390f,790f,1580f,1040f}
  };
  private static final float[][] NPCS={{665f,615f},{1120f,365f},{790f,1035f},{285f,705f}};
  private static final float[][] MONSTERS={{1315f,715f}};

  private static final class SimWorld implements WorldMoveTargetController.NavigationWorld {
    public float minX(){return MIN_X;} public float maxX(){return MAX_X;} public float minY(){return MIN_Y;} public float maxY(){return MAX_Y;}
    public boolean canPlayerOccupy(float x,float y){
      if(x-PLAYER_RADIUS<MIN_X||x+PLAYER_RADIUS>MAX_X||y-PLAYER_RADIUS<MIN_Y||y+PLAYER_RADIUS>MAX_Y)return false;
      for(float[] b:BLOCKERS)if(x+PLAYER_RADIUS>b[0]&&x-PLAYER_RADIUS<b[2]&&y+PLAYER_RADIUS>b[1]&&y-PLAYER_RADIUS<b[3])return false;
      for(float[] n:NPCS)if(distance(x,y,n[0],n[1])<PLAYER_RADIUS+NPC_RADIUS+2f)return false;
      for(float[] m:MONSTERS)if(distance(x,y,m[0],m[1])<PLAYER_RADIUS+MONSTER_RADIUS+3f)return false;
      return true;
    }
  }

  private static final class SimWalker implements WorldMoveTargetController.Walker {
    final SimWorld world; float x=620f,y=560f; int steps=0;
    SimWalker(SimWorld world){this.world=world;}
    public float worldX(){return x;} public float worldY(){return y;}
    public boolean tryWalkStep(float dx,float dy){float nx=x+dx,ny=y+dy;if(!world.canPlayerOccupy(nx,ny))return false;x=nx;y=ny;steps++;return true;}
  }

  public static boolean verify(){
    SimWorld world=new SimWorld(); SimWalker walker=new SimWalker(world);
    WorldMoveTargetController controller=new WorldMoveTargetController(world,walker);
    List<String> route=Arrays.asList("north_cross","central_plaza","west_district","central_plaza","east_district","south_east_lane","south_gate","south_portal");
    long lastRequest=0;
    for(String id:route){
      WorldExplorationContract.Anchor a=WorldExplorationContract.byId(id); if(a==null||!world.canPlayerOccupy(a.x,a.y))return false;
      WorldMoveTargetController.Snapshot requested=controller.requestGroundMove(a.x,a.y);
      if(requested.status==WorldMoveTargetController.Status.BLOCKED||requested.requestId<=lastRequest)return false;
      lastRequest=requested.requestId;
      int guard=0;
      while(controller.snapshot().status==WorldMoveTargetController.Status.MOVING&&guard++<4000)controller.tick(1f/30f);
      WorldMoveTargetController.Snapshot done=controller.snapshot();
      if(done.status!=WorldMoveTargetController.Status.REACHED)return false;
      if(distance(walker.x,walker.y,a.x,a.y)>done.tolerance+0.5f)return false;
    }
    // Blocked target inside a structure must terminate as BLOCKED without moving the walker.
    float bx=walker.x,by=walker.y;
    WorldMoveTargetController.Snapshot blocked=controller.requestGroundMove(200f,200f);
    if(blocked.status!=WorldMoveTargetController.Status.BLOCKED)return false;
    if(distance(bx,by,walker.x,walker.y)>0.01f)return false;
    return walker.steps>100;
  }

  private static float distance(float ax,float ay,float bx,float by){float dx=ax-bx,dy=ay-by;return(float)Math.sqrt(dx*dx+dy*dy);}
  public static void main(String[] args){if(!verify())throw new AssertionError("WorldMoveTargetExplorationAudit failed");System.out.println("WorldMoveTargetExplorationAudit PASS");}
}
