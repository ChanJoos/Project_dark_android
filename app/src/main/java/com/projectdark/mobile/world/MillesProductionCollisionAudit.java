package com.projectdark.mobile.world;

/** Deterministic acceptance audit for Milles production-object collision gate B. */
public final class MillesProductionCollisionAudit {
  private static final float PLAYER_RADIUS=9f;

  private MillesProductionCollisionAudit(){}

  public static boolean verify(){
    boolean building=false,church=false,tree=false,well=false,stall=false,fence=false,lake=false;

    // Every declared ground footprint must reject player occupancy at its physical center.
    for(MillesProductionCollision.Footprint f:MillesProductionCollision.blockers()){
      if(!MillesProductionCollision.blocked(f.centerX(),f.centerY(),PLAYER_RADIUS))return false;
      switch(f.kind){
        case BUILDING:building=true;break;
        case CHURCH:church=true;break;
        case TREE:tree=true;break;
        case WELL:well=true;break;
        case STALL:stall=true;break;
        case FENCE:fence=true;break;
        case LAKE:lake=true;break;
      }
      // Small world objects must still leave at least one immediately adjacent logical cell free.
      if(f.kind==MillesProductionCollision.Kind.TREE||f.kind==MillesProductionCollision.Kind.WELL||
         f.kind==MillesProductionCollision.Kind.STALL||f.kind==MillesProductionCollision.Kind.FENCE){
        AdaptedMillesIsometricTileLayer.Tile nearest=nearestTile(f.centerX(),f.centerY());
        if(nearest==null||!hasFreeAdjacentCell(nearest))return false;
      }
    }
    if(!(building&&church&&tree&&well&&stall&&fence&&lake))return false;

    // Building/church approach points are authored cell centers and remain occupiable.
    for(MillesProductionCollision.Approach a:MillesProductionCollision.entrances()){
      if(!isExactAuthoredCenter(a.x,a.y))return false;
      if(MillesProductionCollision.blocked(a.x,a.y,PLAYER_RADIUS))return false;
    }

    // Shoreline contract: an authored water cell is blocked while adjacent authored land remains free.
    if(!isExactAuthoredCenter(928f,768f)||!MillesProductionCollision.lakeWater(928f,768f)||
       !MillesProductionCollision.blocked(928f,768f,PLAYER_RADIUS))return false;
    if(!isExactAuthoredCenter(928f,704f)||MillesProductionCollision.lakeWater(928f,704f)||
       MillesProductionCollision.blocked(928f,704f,PLAYER_RADIUS))return false;

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
