package com.projectdark.mobile.world;

/**
 * Android-free acceptance gate for the current expanded village.
 * Covers 14 visible, occupiable, intentionally off-grid taps across three camera regions.
 */
public final class WorldArbitraryTapAcceptanceAudit {
  private static final float MIN_X=64f,MAX_X=2304f,MIN_Y=48f,MAX_Y=1600f,RADIUS=9f;
  private static final float[][] BLOCKERS={
      {150,105,390,300},{540,90,800,280},{1040,110,1320,315},{120,430,350,650},
      {1210,410,1480,660},{330,760,570,980},{1000,770,1260,995},{700,470,765,535},
      {850,625,920,690},{80,820,250,1050},{1390,790,1580,1040},{1660,420,1900,580},
      {1960,420,2200,600},{2140,700,2260,950},{1700,1000,1940,1200},
      {2020,1120,2220,1330},{300,1100,560,1300},{1040,1120,1300,1320},
      {520,1460,700,1580},{880,1460,1060,1580}
  };
  private static final float[][] FIXED_ENTITIES={{665,615,21},{1120,365,21},{790,1450,21},{285,705,21},{1900,820,21},{1315,715,23}};
  private static final float[][] TARGETS={
      {783.5f,590.5f},{812.25f,350.75f},{475.5f,380.25f},{405.25f,704.5f},
      {1095.75f,720.25f},{1510.5f,740.75f},{1735.25f,760.5f},{2040.75f,970.25f},
      {2070.5f,1365.75f},{930.25f,1040.5f},{792.75f,1190.25f},{650.5f,1360.75f},
      {790.25f,1515.5f},{790.75f,1570.25f}
  };

  private static final class SimWorld implements WorldMoveTargetController.NavigationWorld {
    public float minX(){return MIN_X;}public float maxX(){return MAX_X;}public float minY(){return MIN_Y;}public float maxY(){return MAX_Y;}
    public boolean canPlayerOccupy(float x,float y){
      if(x-RADIUS<MIN_X||x+RADIUS>MAX_X||y-RADIUS<MIN_Y||y+RADIUS>MAX_Y)return false;
      for(float[] b:BLOCKERS)if(x+RADIUS>b[0]&&x-RADIUS<b[2]&&y+RADIUS>b[1]&&y-RADIUS<b[3])return false;
      for(float[] e:FIXED_ENTITIES){float dx=x-e[0],dy=y-e[1];if(dx*dx+dy*dy<e[2]*e[2])return false;}
      return true;
    }
  }
  private static final class Walker implements WorldMoveTargetController.Walker {
    final SimWorld world;float x=620f,y=560f;Walker(SimWorld world){this.world=world;}
    public float worldX(){return x;}public float worldY(){return y;}
    public boolean tryWalkStep(float dx,float dy){float nx=x+dx,ny=y+dy;if(!world.canPlayerOccupy(nx,ny))return false;x=nx;y=ny;return true;}
  }
  private static final class DynamicWalker implements WorldMoveTargetController.Walker {
    final SimWorld world;final boolean persistent;float x=620f,y=560f;int attempts;
    DynamicWalker(SimWorld world,boolean persistent){this.world=world;this.persistent=persistent;}
    public float worldX(){return x;}public float worldY(){return y;}
    public boolean tryWalkStep(float dx,float dy){
      attempts++;
      if(persistent||attempts==2||attempts==20||attempts==40)return false;
      float nx=x+dx,ny=y+dy;if(!world.canPlayerOccupy(nx,ny))return false;x=nx;y=ny;return true;
    }
  }

  public static boolean verify(){
    SimWorld world=new SimWorld();
    for(int i=0;i<TARGETS.length;i++){
      float[] target=TARGETS[i];if(!world.canPlayerOccupy(target[0],target[1]))return false;
      Walker walker=new Walker(world);
      WorldCameraTransform camera=new WorldCameraTransform(MIN_X,MAX_X,MIN_Y,MAX_Y,640f,360f);
      camera.snapTo(i<5?620f:i<9?1840f:790f,i<5?560f:i<9?820f:1360f);
      WorldCameraTransform.Point screen=camera.worldToScreen(target[0],target[1]);
      WorldCameraTransform.Point roundTrip=camera.screenToWorld(screen.x,screen.y);
      if(distance(roundTrip.x,roundTrip.y,target[0],target[1])>.01f)return false;
      WorldMoveTargetController movement=new WorldMoveTargetController(world,walker);
      WorldMoveTargetController.Snapshot request=movement.requestGroundMove(roundTrip.x,roundTrip.y);
      if(request.status==WorldMoveTargetController.Status.BLOCKED)return false;
      int guard=0;while(movement.snapshot().status==WorldMoveTargetController.Status.MOVING&&guard++<10000)movement.tick(1f/30f);
      if(movement.snapshot().status!=WorldMoveTargetController.Status.REACHED)return false;
      if(distance(walker.x,walker.y,target[0],target[1])>movement.snapshot().tolerance+.5f)return false;
    }
    Walker walker=new Walker(world);WorldMoveTargetController movement=new WorldMoveTargetController(world,walker);
    if(movement.requestGroundMove(MIN_X+1f,MIN_Y+1f).status!=WorldMoveTargetController.Status.BLOCKED)return false;
    WorldMoveTargetController.Snapshot first=movement.requestGroundMove(TARGETS[0][0],TARGETS[0][1]);
    WorldMoveTargetController.Snapshot replacement=movement.requestGroundMove(TARGETS[5][0],TARGETS[5][1]);
    if(first.status!=WorldMoveTargetController.Status.MOVING||replacement.replacedRequestId!=first.requestId)return false;
    if(movement.cancelForDirectInput().cancelReason!=WorldMoveTargetController.CancelReason.DIRECT_INPUT)return false;

    // Non-consecutive temporary blockers must not consume one lifetime-wide replan budget.
    DynamicWalker transientWalker=new DynamicWalker(world,false);
    WorldMoveTargetController transientMove=new WorldMoveTargetController(world,transientWalker);
    transientMove.requestGroundMove(792.75f,1190.25f);
    int guard=0;while(transientMove.snapshot().status==WorldMoveTargetController.Status.MOVING&&guard++<10000)transientMove.tick(1f/30f);
    if(transientMove.snapshot().status!=WorldMoveTargetController.Status.REACHED)return false;

    // A genuinely persistent blocker still terminates deterministically instead of walking through it.
    DynamicWalker persistentWalker=new DynamicWalker(world,true);
    WorldMoveTargetController persistentMove=new WorldMoveTargetController(world,persistentWalker);
    persistentMove.requestGroundMove(TARGETS[0][0],TARGETS[0][1]);
    guard=0;while(persistentMove.snapshot().status==WorldMoveTargetController.Status.MOVING&&guard++<20)persistentMove.tick(1f/30f);
    return persistentMove.snapshot().status==WorldMoveTargetController.Status.BLOCKED;
  }

  private static float distance(float ax,float ay,float bx,float by){float dx=ax-bx,dy=ay-by;return(float)Math.sqrt(dx*dx+dy*dy);}
  public static void main(String[] args){if(!verify())throw new AssertionError("WorldArbitraryTapAcceptanceAudit failed");System.out.println("WorldArbitraryTapAcceptanceAudit PASS: "+TARGETS.length+" targets");}
}
