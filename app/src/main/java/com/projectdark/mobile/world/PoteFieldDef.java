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

  /** Collision is intentionally the trunk/rock/stream footprint, never the canopy bitmap bounds. */
  public static List<RectF> obstacles(){
    List<RectF> out=new ArrayList<>();
    // west/north forest wall
    out.add(new RectF(64,64,210,690)); out.add(new RectF(210,64,510,205));
    out.add(new RectF(510,64,850,150)); out.add(new RectF(850,64,1408,190));
    // east forest wall
    out.add(new RectF(1280,190,1408,896));
    // V3 visible forest masses / peninsulas. Collision follows trunks, not canopy silhouettes.
    out.add(new RectF(260,250,430,410));
    out.add(new RectF(470,360,625,485));
    out.add(new RectF(690,525,825,635));
    out.add(new RectF(485,730,610,820));
    out.add(new RectF(760,210,930,360));
    out.add(new RectF(900,610,1015,790));
    // Continuous meandering stream follows the eastern edge; the entry-to-clearing route stays west of it.
    out.add(new RectF(1190,130,1260,350));
    out.add(new RectF(1208,350,1280,590));
    out.add(new RectF(1198,590,1270,865));
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
