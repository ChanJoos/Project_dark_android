package com.projectdark.mobile;

import com.projectdark.mobile.world.AdaptedMillesIsometricTileLayer;
import com.projectdark.mobile.world.WorldMoveTargetController;

/**
 * World contract shared with player navigation: authoritative actor positions live on authored
 * 64x32 isometric tile centers and every logical step is exactly one adjacent (±32, ±16) move.
 * No fractional segment-length tuning is permitted here.
 */
public final class MonsterTileCenterLocomotion {
  public static final float STEP_X=AdaptedMillesIsometricTileLayer.HALF_WIDTH;
  public static final float STEP_Y=AdaptedMillesIsometricTileLayer.HALF_HEIGHT;
  public static final float STEP_DISTANCE=(float)Math.sqrt(STEP_X*STEP_X+STEP_Y*STEP_Y);

  private MonsterTileCenterLocomotion(){}

  public static WorldMoveTargetController.Direction toward(float dx,float dy,
      WorldMoveTargetController.Direction fallback){
    if(Math.abs(dx)<.0001f&&Math.abs(dy)<.0001f)return fallback==null?WorldMoveTargetController.Direction.SE:fallback;
    if(dy<0f)return dx<0f?WorldMoveTargetController.Direction.NW:WorldMoveTargetController.Direction.NE;
    return dx<0f?WorldMoveTargetController.Direction.SW:WorldMoveTargetController.Direction.SE;
  }

  public static CharacterRenderer.Direction facing(WorldMoveTargetController.Direction direction){
    if(direction==null)return CharacterRenderer.Direction.SE;
    switch(direction){
      case NW:return CharacterRenderer.Direction.NW;
      case NE:return CharacterRenderer.Direction.NE;
      case SW:return CharacterRenderer.Direction.SW;
      default:return CharacterRenderer.Direction.SE;
    }
  }

  public static WorldMoveTargetController.TileCenter nearestAuthoredCenter(float x,float y){
    AdaptedMillesIsometricTileLayer.Tile best=null;float bestDistance=Float.MAX_VALUE;
    for(AdaptedMillesIsometricTileLayer.Tile tile:AdaptedMillesIsometricTileLayer.tiles()){
      float dx=tile.centerX-x,dy=tile.centerY-y,d=dx*dx+dy*dy;
      if(d<bestDistance){bestDistance=d;best=tile;}
    }
    return best==null?null:new WorldMoveTargetController.TileCenter(best.centerX,best.centerY);
  }

  public static boolean isAuthoredCenter(float x,float y){
    for(AdaptedMillesIsometricTileLayer.Tile tile:AdaptedMillesIsometricTileLayer.tiles())
      if(close(tile.centerX,x)&&close(tile.centerY,y))return true;
    return false;
  }

  public static boolean isAdjacentEndpoint(float fromX,float fromY,float toX,float toY){
    return WorldMoveTargetController.Direction.between(fromX,fromY,toX,toY)!=null;
  }

  private static boolean close(float a,float b){return Math.abs(a-b)<.01f;}
}
