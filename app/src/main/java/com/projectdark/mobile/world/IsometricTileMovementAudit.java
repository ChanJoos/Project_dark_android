package com.projectdark.mobile.world;

/** Deterministic audit for the 2026-09-10 21:49 tile-locked movement canon. */
public final class IsometricTileMovementAudit {
  private IsometricTileMovementAudit(){}
  private static final class Walker implements IsometricTileMovementController.Walker {
    float x,y; Walker(float x,float y){this.x=x;this.y=y;}
    @Override public float worldX(){return x;} @Override public float worldY(){return y;}
    @Override public boolean tryAdjacentTileStep(float dx,float dy){x+=dx;y+=dy;return true;}
  }
  public static boolean verify(){
    AdaptedMillesIsometricTileLayer.Tile start=findOpenCenter();if(start==null)return false;
    Walker walker=new Walker(start.centerX,start.centerY);
    IsometricTileMovementController.NavigationWorld world=(x,y)->AdaptedMillesIsometricTileLayer.tileAt(x,y)!=null;
    IsometricTileMovementController movement=new IsometricTileMovementController(world,walker);
    float ox=walker.x,oy=walker.y;
    if(!step(movement,IsometricTileMovementController.Direction.NW)||!step(movement,IsometricTileMovementController.Direction.SE))return false;
    if(!close(walker.x,ox)||!close(walker.y,oy))return false;
    if(!step(movement,IsometricTileMovementController.Direction.NE)||!step(movement,IsometricTileMovementController.Direction.SW))return false;
    if(!close(walker.x,ox)||!close(walker.y,oy))return false;
    for(int i=0;i<5;i++){if(!step(movement,IsometricTileMovementController.Direction.SE)||!step(movement,IsometricTileMovementController.Direction.NW))return false;}
    if(!close(walker.x,ox)||!close(walker.y,oy))return false;
    IsometricTileMovementController.Snapshot tap=movement.requestGroundMove(ox+127f,oy+79f);
    if(tap.status==IsometricTileMovementController.Status.BLOCKED)return false;
    float px=walker.x,py=walker.y;int guard=256;
    while(movement.snapshot().status==IsometricTileMovementController.Status.MOVING&&guard-->0){
      IsometricTileMovementController.Snapshot before=movement.snapshot();movement.step();float dx=walker.x-px,dy=walker.y-py;
      if(!canonical(dx,dy))return false;px=walker.x;py=walker.y;
      if(before.remainingSteps<=0)return false;
    }
    IsometricTileMovementController.Snapshot done=movement.snapshot();
    return guard>0&&done.status==IsometricTileMovementController.Status.REACHED&&close(walker.x,done.targetX)&&close(walker.y,done.targetY);
  }
  private static boolean step(IsometricTileMovementController movement,IsometricTileMovementController.Direction d){return movement.step(d).status==IsometricTileMovementController.Status.REACHED;}
  private static AdaptedMillesIsometricTileLayer.Tile findOpenCenter(){
    for(AdaptedMillesIsometricTileLayer.Tile t:AdaptedMillesIsometricTileLayer.tiles()){
      boolean all=true;for(IsometricTileMovementController.Direction d:IsometricTileMovementController.Direction.values())if(AdaptedMillesIsometricTileLayer.tileAt(t.centerX+d.dx,t.centerY+d.dy)==null){all=false;break;}
      if(all)return t;
    }return null;
  }
  private static boolean canonical(float dx,float dy){return close(Math.abs(dx),32f)&&close(Math.abs(dy),16f);}
  private static boolean close(float a,float b){return Math.abs(a-b)<0.01f;}
}
