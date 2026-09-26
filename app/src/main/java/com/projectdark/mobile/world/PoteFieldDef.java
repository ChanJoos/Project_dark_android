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
    for(float y=96f;y<=864f;y+=32f){
      int row=(int)((y-96f)/32f);
      for(float x=96f;x<=1376f;x+=64f){
        float px=x+((row&1)==0?0f:32f);
        if(px<=1376f&&!blocked(px,y))out.add(new WorldMoveTargetController.TileCenter(px,y));
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
    out.add(new RectF(1260,190,1408,896));
    // dense internal groves: leave readable corridors between them
    out.add(new RectF(260,250,430,410));
    out.add(new RectF(480,520,650,700));
    out.add(new RectF(760,210,930,360));
    out.add(new RectF(900,610,1090,790));
    // stream body on east/centre; bridge gaps remain walkable
    out.add(new RectF(1040,190,1245,390));
    out.add(new RectF(990,430,1180,575));
    out.add(new RectF(1080,620,1260,815));
    return Collections.unmodifiableList(out);
  }

  private static void trunk(List<RectF> out,float x,float y){out.add(new RectF(x-24f,y-18f,x+24f,y+18f));}\n\n  public static boolean atExit(float x,float y){
    float dx=x-EXIT_X,dy=y-EXIT_Y;return dx*dx+dy*dy<=EXIT_RADIUS*EXIT_RADIUS;
  }
  private static boolean blocked(float x,float y){
    for(RectF r:obstacles())if(x>=r.left&&x<=r.right&&y>=r.top&&y<=r.bottom)return true;
    return false;
  }
}
