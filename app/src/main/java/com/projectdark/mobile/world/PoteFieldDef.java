package com.projectdark.mobile.world;

import android.graphics.RectF;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * [ADAPTED/B] Pote forest 1 authored spatial shell.
 * Geometry follows the production asset grammar: dense boundary -> understory -> groundcover,
 * stream -> bank -> rocks/moisture vegetation, and open encounter pockets connected by soil corridors.
 */
public final class PoteFieldDef {
  public static final String MAP_ID="MAP_POTE_01";
  public static final float MIN_X=64f,MAX_X=4096f,MIN_Y=64f,MAX_Y=2048f;
  /** Field arrivals open on the central trail so the player can see where the route goes. */
  public static final float ENTRY_X=800f,ENTRY_Y=528f;
  public static final float EXIT_X=128f,EXIT_Y=832f;
  public static final float EXIT_RADIUS=34f;
  public static final float BRIDGE_X=PoteForestGeometry.BRIDGE_X,BRIDGE_Y=PoteForestGeometry.BRIDGE_Y;
  private static final List<RectF> OBSTACLES=buildObstacles();
  private static final List<WorldMoveTargetController.TileCenter> NAVIGATION_TILES=buildNavigationTiles();
  private PoteFieldDef(){}

  public static List<WorldMoveTargetController.TileCenter> navigationTiles(){
    return NAVIGATION_TILES;
  }

  private static List<WorldMoveTargetController.TileCenter> buildNavigationTiles(){
    List<WorldMoveTargetController.TileCenter> out=new ArrayList<>();
    // Match WorldMoveTargetController's real isometric steps exactly: NW/NE/SW/SE =
    // (-32,-16)/(32,-16)/(-32,16)/(32,16). The former 64x32 lattice had no legal edges.
    for(float y=MIN_Y;y<=MAX_Y;y+=16f){
      for(float x=MIN_X;x<=MAX_X;x+=32f){
        int parity=(int)(x/32f+y/16f);
        if((parity&1)==0&&!blocked(x,y))out.add(new WorldMoveTargetController.TileCenter(x,y));
      }
    }
    return Collections.unmodifiableList(out);
  }

  /** Pote's tile lattice is map-local; it shares 32x16 moves with Milles, not its absolute origin. */
  public static boolean isNavigationCenter(float x,float y){
    int col=Math.round(x/32f),row=Math.round(y/16f);
    return Math.abs(x-col*32f)<.01f&&Math.abs(y-row*16f)<.01f
        &&((col+row)&1)==0&&!blocked(x,y);
  }

  public static WorldMoveTargetController.TileCenter nearestNavigationCenter(float x,float y){
    WorldMoveTargetController.TileCenter best=null;float bestDistance=Float.MAX_VALUE;
    for(WorldMoveTargetController.TileCenter tile:NAVIGATION_TILES){
      float dx=tile.x-x,dy=tile.y-y,d=dx*dx+dy*dy;
      if(d<bestDistance){bestDistance=d;best=tile;}
    }
    return best;
  }

  /** Full terrain coverage on the same staggered 64x32 diamond lattice as Milles. */
  public static List<WorldMoveTargetController.TileCenter> groundTiles(){
    List<WorldMoveTargetController.TileCenter> out=new ArrayList<>();
    int row=0;
    for(float y=MIN_Y;y<=MAX_Y;y+=16f,row++){
      float firstX=MIN_X+((row&1)==0?0f:32f);
      for(float x=firstX;x<=MAX_X;x+=64f)out.add(new WorldMoveTargetController.TileCenter(x,y));
    }
    return Collections.unmodifiableList(out);
  }

  /** Winding creek crosses the central trail at the single authored footbridge. */
  public static float[][] creekCenterline(){
    return PoteForestGeometry.creekCenterline();
  }

  /** Isometric cardinal neighbours must share exactly the same coordinates as movement. */
  public static boolean areAdjacentGroundTiles(float ax,float ay,float bx,float by){
    return Math.abs(Math.abs(ax-bx)-32f)<.01f&&Math.abs(Math.abs(ay-by)-16f)<.01f;
  }

  /** Collision is intentionally the trunk/rock/stream footprint, never the canopy bitmap bounds. */
  public static List<RectF> obstacles(){
    return OBSTACLES;
  }

  private static List<RectF> buildObstacles(){
    List<RectF> out=new ArrayList<>();
    out.addAll(PoteFieldRenderer.blockingFootprints());

    out.addAll(PoteForestGeometry.waterObstacles(PoteFieldDef.MAP_ID));
    return Collections.unmodifiableList(out);
  }

  private static void trunk(List<RectF> out,float x,float y){out.add(new RectF(x-24f,y-18f,x+24f,y+18f));}

  public static boolean atExit(float x,float y){
    float dx=x-EXIT_X,dy=y-EXIT_Y;return dx*dx+dy*dy<=EXIT_RADIUS*EXIT_RADIUS;
  }
  private static boolean blocked(float x,float y){
    float radius=14f;
    if(x-radius<MIN_X||x+radius>MAX_X||y-radius<MIN_Y||y+radius>MAX_Y)return true;
    for(RectF r:obstacles())if(x+radius>r.left&&x-radius<r.right&&y+radius>r.top&&y-radius<r.bottom)return true;
    return false;
  }
}
