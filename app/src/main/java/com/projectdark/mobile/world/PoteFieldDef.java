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
  public static final float MIN_X=64f,MAX_X=1408f,MIN_Y=64f,MAX_Y=896f;
  public static final float ENTRY_X=192f,ENTRY_Y=800f;
  public static final float EXIT_X=128f,EXIT_Y=832f;
  public static final float EXIT_RADIUS=34f;
  private static final List<RectF> OBSTACLES=buildObstacles();
  private PoteFieldDef(){}

  public static List<WorldMoveTargetController.TileCenter> navigationTiles(){
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
    // The stream is an impassable winding barrier at the eastern/southern edge. Its visual ribbon
    // and collision line share the same source points; the main trail stays to the west.
    float[][] water={{820,700},{862,674},{904,648},{946,622},{988,596},{1030,570},{1072,544},{1114,518},{1156,492},{1198,466},{1240,440},{1282,414},{1324,388},{1366,362}};
    for(int i=1;i<water.length;i++)out.add(new RectF(
        Math.min(water[i-1][0],water[i][0])-27f,Math.min(water[i-1][1],water[i][1])-22f,
        Math.max(water[i-1][0],water[i][0])+27f,Math.max(water[i-1][1],water[i][1])+22f));
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
