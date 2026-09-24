package com.projectdark.mobile.world;

/** Deterministic acceptance audit for Milles production-object collision gate B. */
public final class MillesProductionCollisionAudit {
  private static final float PLAYER_RADIUS=9f;

  private MillesProductionCollisionAudit(){}

  public static boolean verify(){
    boolean building=false,church=false;int fenceCount=0;
    for(MillesProductionCollision.Footprint f:MillesProductionCollision.blockers()){
      if(!MillesProductionCollision.blocked(f.centerX(),f.centerY(),PLAYER_RADIUS))return false;
      if(f.kind==MillesProductionCollision.Kind.BUILDING)building=true;
      if(f.kind==MillesProductionCollision.Kind.CHURCH)church=true;
      if(f.kind==MillesProductionCollision.Kind.FENCE)fenceCount++;
      // Scenery blockers require matching visible map assets.
      if(f.kind!=MillesProductionCollision.Kind.BUILDING&&f.kind!=MillesProductionCollision.Kind.CHURCH&&f.kind!=MillesProductionCollision.Kind.FENCE)return false;
    }
    if(!(building&&church&&fenceCount==11))return false;

    for(MillesProductionCollision.Approach approach:MillesProductionCollision.entrances()){
      if(!isExactAuthoredCenter(approach.x,approach.y))return false;
      if(MillesProductionCollision.blocked(approach.x,approach.y,PLAYER_RADIUS))return false;
    }
    return true;
  }

  private static boolean hasFreeAdjacentCell(AdaptedMillesIsometricTileLayer.Tile tile){
    float hw=AdaptedMillesIsometricTileLayer.HALF_WIDTH;
    float hh=AdaptedMillesIsometricTileLayer.HALF_HEIGHT;
    return freeAuthored(tile.centerX-hw,tile.centerY-hh)||freeAuthored(tile.centerX+hw,tile.centerY-hh)||
           freeAuthored(tile.centerX-hw,tile.centerY+hh)||freeAuthored(tile.centerX+hw,tile.centerY+hh);
  }

  private static boolean freeAuthored(float x,float y){
    return isExactAuthoredCenter(x,y)&&!MillesProductionCollision.blocked(x,y,PLAYER_RADIUS);
  }

  private static boolean isExactAuthoredCenter(float x,float y){
    for(AdaptedMillesIsometricTileLayer.Tile t:AdaptedMillesIsometricTileLayer.tiles())
      if(close(t.centerX,x)&&close(t.centerY,y))return true;
    return false;
  }

  private static AdaptedMillesIsometricTileLayer.Tile nearestTile(float x,float y){
    AdaptedMillesIsometricTileLayer.Tile best=null;float bestD=Float.MAX_VALUE;
    for(AdaptedMillesIsometricTileLayer.Tile t:AdaptedMillesIsometricTileLayer.tiles()){
      float dx=t.centerX-x,dy=t.centerY-y,d=dx*dx+dy*dy;
      if(d<bestD){bestD=d;best=t;}
    }
    return best;
  }

  private static boolean close(float a,float b){return Math.abs(a-b)<.001f;}

  public static void main(String[] args){
    if(!verify())throw new AssertionError("MillesProductionCollisionAudit failed");
  }
}
