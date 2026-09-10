package com.projectdark.mobile.world;

import com.projectdark.mobile.WorldDef;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Render-ready [ADAPTED]/[B] isometric ground composition for the current Milles prototype.
 *
 * This deliberately does not claim original Milles tile geometry. It turns the authored village
 * surfaces into a dense diamond-tile field so the runtime can render an actual connected map now,
 * while keeping every cell replaceable when calibrated source geometry becomes available.
 */
public final class AdaptedMillesIsometricTileLayer {
  public static final float TILE_WIDTH=64f;
  public static final float TILE_HEIGHT=32f;
  public static final float HALF_WIDTH=TILE_WIDTH*.5f;
  public static final float HALF_HEIGHT=TILE_HEIGHT*.5f;
  public static final float ROW_STEP=HALF_HEIGHT;

  public enum TileKind { GROUND, ROAD, PLAZA, GATE }

  public static final class Tile {
    public final int row,column;
    public final float centerX,centerY;
    public final float leftX,topY,rightX,bottomY;
    public final TileKind kind;
    public final int variant;
    public final String evidence,status,assetRef;

    Tile(int row,int column,float centerX,float centerY,TileKind kind,int variant){
      this.row=row;this.column=column;this.centerX=centerX;this.centerY=centerY;
      leftX=centerX-HALF_WIDTH;topY=centerY-HALF_HEIGHT;rightX=centerX+HALF_WIDTH;bottomY=centerY+HALF_HEIGHT;
      this.kind=kind;this.variant=variant;
      evidence="ADAPTED/B";
      status="PROTOTYPE_REPLACE_WITH_VERIFIED_MILLES";
      assetRef=assetRefFor(kind,variant);
    }

    /** Diamond hit-test in the same projected world plane consumed by the current camera. */
    public boolean contains(float worldX,float worldY){
      return Math.abs(worldX-centerX)/HALF_WIDTH + Math.abs(worldY-centerY)/HALF_HEIGHT <= 1f;
    }

    /** Painter order for ground diamonds; lower rows are drawn later. */
    public long depthKey(){return ((long)row<<32)|(column&0xffffffffL);}
  }

  private static final List<Tile> TILES=build();

  private AdaptedMillesIsometricTileLayer(){}

  public static List<Tile> tiles(){return TILES;}

  /** Finds the top-most authored tile under a projected world point. */
  public static Tile tileAt(float worldX,float worldY){
    Tile best=null;
    for(Tile tile:TILES){
      if(tile.contains(worldX,worldY)&&(best==null||tile.depthKey()>best.depthKey()))best=tile;
    }
    return best;
  }

  private static List<Tile> build(){
    List<Tile> result=new ArrayList<>();
    int row=0;
    for(float y=WorldDef.MIN_Y+HALF_HEIGHT;y<=WorldDef.MAX_Y-HALF_HEIGHT+.01f;y+=ROW_STEP,row++){
      float offset=(row&1)==0?0f:HALF_WIDTH;
      int column=0;
      for(float x=WorldDef.MIN_X+HALF_WIDTH+offset;x<=WorldDef.MAX_X-HALF_WIDTH+.01f;x+=TILE_WIDTH,column++){
        TileKind kind=classify(x,y);
        int variant=Math.floorMod(row*31+column*17+kind.ordinal()*7,4);
        result.add(new Tile(row,column,x,y,kind,variant));
      }
    }
    return Collections.unmodifiableList(result);
  }

  private static TileKind classify(float x,float y){
    AdaptedMillesMapLayer.SurfaceKind best=AdaptedMillesMapLayer.SurfaceKind.GROUND;
    int bestPriority=0;
    for(AdaptedMillesMapLayer.Surface s:AdaptedMillesMapLayer.surfaces()){
      if(x<s.left||x>s.right||y<s.top||y>s.bottom)continue;
      int priority=priority(s.kind);
      if(priority>=bestPriority){bestPriority=priority;best=s.kind;}
    }
    switch(best){
      case GATE:return TileKind.GATE;
      case PLAZA:return TileKind.PLAZA;
      case ROAD:return TileKind.ROAD;
      default:return TileKind.GROUND;
    }
  }

  private static int priority(AdaptedMillesMapLayer.SurfaceKind kind){
    switch(kind){case GATE:return 4;case PLAZA:return 3;case ROAD:return 2;default:return 1;}
  }

  private static String assetRefFor(TileKind kind,int variant){
    // Asset identities remain evidence-gated. These are renderer slots, not claims about original art.
    return "PENDING_CROP/milles/tiles/"+kind.name().toLowerCase()+"_v"+variant;
  }
}
