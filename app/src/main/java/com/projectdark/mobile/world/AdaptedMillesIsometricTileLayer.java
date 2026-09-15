package com.projectdark.mobile.world;

import com.projectdark.mobile.WorldDef;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

/** Canonical 64x32 Milles ground grid shared by rendering and navigation. */
public final class AdaptedMillesIsometricTileLayer {
  public static final float TILE_WIDTH=64f;
  public static final float TILE_HEIGHT=32f;
  public static final float HALF_WIDTH=TILE_WIDTH*.5f;
  public static final float HALF_HEIGHT=TILE_HEIGHT*.5f;
  public static final float ROW_STEP=HALF_HEIGHT;
  public static final int EDGE_NW=1,EDGE_NE=2,EDGE_SE=4,EDGE_SW=8;

  // Central plaza anchor snapped to an actual authored/navigation tile center.
  public static final float PLAZA_CENTER_X=768f;
  public static final float PLAZA_CENTER_Y=592f;

  public enum TileKind { GROUND, ROAD, PLAZA, GATE }

  public static final class Tile {
    public final int row,column;
    public final float centerX,centerY;
    public final float leftX,topY,rightX,bottomY;
    public final TileKind kind;
    public final int variant;
    public final int transitionMask;
    public final String evidence,status,assetRef;

    Tile(int row,int column,float centerX,float centerY,TileKind kind,int variant,int transitionMask){
      this.row=row;this.column=column;this.centerX=centerX;this.centerY=centerY;
      leftX=centerX-HALF_WIDTH;topY=centerY-HALF_HEIGHT;rightX=centerX+HALF_WIDTH;bottomY=centerY+HALF_HEIGHT;
      this.kind=kind;this.variant=variant;this.transitionMask=transitionMask;
      evidence="ADAPTED/B";status="PROTOTYPE_REPLACE_WITH_VERIFIED_MILLES";
      assetRef=assetRefFor(kind,variant);
    }
    public boolean contains(float worldX,float worldY){
      return Math.abs(worldX-centerX)/HALF_WIDTH + Math.abs(worldY-centerY)/HALF_HEIGHT <= 1f;
    }
    public long depthKey(){return ((long)row<<32)|(column&0xffffffffL);}
  }

  private static final List<Tile> TILES=build();
  private AdaptedMillesIsometricTileLayer(){}
  public static List<Tile> tiles(){return TILES;}

  public static Tile tileAt(float worldX,float worldY){
    Tile best=null;
    for(Tile tile:TILES)if(tile.contains(worldX,worldY)&&(best==null||tile.depthKey()>best.depthKey()))best=tile;
    return best;
  }

  private static List<Tile> build(){
    List<Tile> raw=new ArrayList<>();
    int row=0;
    for(float y=WorldDef.MIN_Y+HALF_HEIGHT;y<=WorldDef.MAX_Y-HALF_HEIGHT+.01f;y+=ROW_STEP,row++){
      float offset=(row&1)==0?0f:HALF_WIDTH;
      int column=0;
      for(float x=WorldDef.MIN_X+HALF_WIDTH+offset;x<=WorldDef.MAX_X-HALF_WIDTH+.01f;x+=TILE_WIDTH,column++){
        TileKind kind=classify(x,y);
        int variant=Math.floorMod(row*31+column*17+kind.ordinal()*7,4);
        raw.add(new Tile(row,column,x,y,kind,variant,0));
      }
    }
    Map<String,Tile> byCenter=new HashMap<>();
    for(Tile tile:raw)byCenter.put(key(tile.centerX,tile.centerY),tile);
    List<Tile> result=new ArrayList<>(raw.size());
    for(Tile tile:raw){
      int mask=0;
      if(differs(tile,byCenter.get(key(tile.centerX-HALF_WIDTH,tile.centerY-HALF_HEIGHT))))mask|=EDGE_NW;
      if(differs(tile,byCenter.get(key(tile.centerX+HALF_WIDTH,tile.centerY-HALF_HEIGHT))))mask|=EDGE_NE;
      if(differs(tile,byCenter.get(key(tile.centerX+HALF_WIDTH,tile.centerY+HALF_HEIGHT))))mask|=EDGE_SE;
      if(differs(tile,byCenter.get(key(tile.centerX-HALF_WIDTH,tile.centerY+HALF_HEIGHT))))mask|=EDGE_SW;
      result.add(new Tile(tile.row,tile.column,tile.centerX,tile.centerY,tile.kind,tile.variant,mask));
    }
    return Collections.unmodifiableList(result);
  }

  private static boolean differs(Tile tile,Tile neighbor){return neighbor==null||neighbor.kind!=tile.kind;}
  private static String key(float x,float y){return Math.round(x*10f)+":"+Math.round(y*10f);}

  /**
   * Simple Milles readability pass requested for device validation:
   * - grass everywhere by default;
   * - a compact central plaza;
   * - four long diagonal road arms, TWO canonical tile lanes wide.
   *
   * u/v are integer isometric grid coordinates around the plaza. u-axis and v-axis are exactly the
   * same NW/NE/SW/SE directions used by movement, so visible roads cannot drift off navigation.
   */
  private static TileKind classify(float x,float y){
    float dx=x-PLAZA_CENTER_X,dy=y-PLAZA_CENTER_Y;
    float u=dx/64f-dy/32f;
    float v=dx/64f+dy/32f;
    int iu=Math.round(u),iv=Math.round(v);
    if(Math.abs(u-iu)<.01f&&Math.abs(v-iv)<.01f){
      // 3x3 plaza around the center so the landmark has breathing room.
      if(Math.abs(iu)<=1&&Math.abs(iv)<=1)return TileKind.PLAZA;
      // Two lanes only: axis lane 0 plus its neighboring lane +1. Both axes extend to map bounds.
      if(iv==0||iv==1||iu==0||iu==1)return TileKind.ROAD;
    }
    return TileKind.GROUND;
  }

  private static String assetRefFor(TileKind kind,int variant){
    return "PENDING_CROP/milles/tiles/"+kind.name().toLowerCase()+"_v"+variant;
  }
}
