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
  public static final float PLAZA_CENTER_X=768f;
  public static final float PLAZA_CENTER_Y=592f;

  public enum TileKind { GROUND, ROAD, PLAZA, GATE }
  public static final class Tile {
    public final int row,column; public final float centerX,centerY,leftX,topY,rightX,bottomY;
    public final TileKind kind; public final int variant,transitionMask; public final String evidence,status,assetRef;
    Tile(int row,int column,float centerX,float centerY,TileKind kind,int variant,int transitionMask){
      this.row=row;this.column=column;this.centerX=centerX;this.centerY=centerY;
      leftX=centerX-HALF_WIDTH;topY=centerY-HALF_HEIGHT;rightX=centerX+HALF_WIDTH;bottomY=centerY+HALF_HEIGHT;
      this.kind=kind;this.variant=variant;this.transitionMask=transitionMask;
      evidence="ADAPTED/B";status="STORY_DRIVEN_MILLES_LAYOUT_PENDING_DEVICE";assetRef=assetRefFor(kind,variant);
    }
    public boolean contains(float x,float y){return Math.abs(x-centerX)/HALF_WIDTH+Math.abs(y-centerY)/HALF_HEIGHT<=1f;}
    public long depthKey(){return ((long)row<<32)|(column&0xffffffffL);}
  }

  private AdaptedMillesIsometricTileLayer(){}
  public static List<Tile> tiles(){return TILES;}
  public static Tile tileAt(float x,float y){Tile best=null;for(Tile t:TILES)if(t.contains(x,y)&&(best==null||t.depthKey()>best.depthKey()))best=t;return best;}

  private static List<Tile> build(){
    List<Tile> raw=new ArrayList<>(); int row=0;
    for(float y=WorldDef.MIN_Y+HALF_HEIGHT;y<=WorldDef.MAX_Y-HALF_HEIGHT+.01f;y+=ROW_STEP,row++){
      float offset=(row&1)==0?0f:HALF_WIDTH; int col=0;
      for(float x=WorldDef.MIN_X+HALF_WIDTH+offset;x<=WorldDef.MAX_X-HALF_WIDTH+.01f;x+=TILE_WIDTH,col++){
        TileKind kind=classify(x,y); int variant=Math.floorMod(row*31+col*17+kind.ordinal()*7,4);
        raw.add(new Tile(row,col,x,y,kind,variant,0));
      }
    }
    Map<String,Tile> by=new HashMap<>();for(Tile t:raw)by.put(key(t.centerX,t.centerY),t);
    List<Tile> out=new ArrayList<>(raw.size());
    for(Tile t:raw){int m=0;
      if(differs(t,by.get(key(t.centerX-HALF_WIDTH,t.centerY-HALF_HEIGHT))))m|=EDGE_NW;
      if(differs(t,by.get(key(t.centerX+HALF_WIDTH,t.centerY-HALF_HEIGHT))))m|=EDGE_NE;
      if(differs(t,by.get(key(t.centerX+HALF_WIDTH,t.centerY+HALF_HEIGHT))))m|=EDGE_SE;
      if(differs(t,by.get(key(t.centerX-HALF_WIDTH,t.centerY+HALF_HEIGHT))))m|=EDGE_SW;
      out.add(new Tile(t.row,t.column,t.centerX,t.centerY,t.kind,t.variant,m));}
    return Collections.unmodifiableList(out);
  }
  private static boolean differs(Tile a,Tile b){return b==null||b.kind!=a.kind;}
  private static String key(float x,float y){return Math.round(x*10f)+":"+Math.round(y*10f);}

  /** Authored, asymmetric Milles paths that connect the active building doors to the well square. */
  private static final float PATH_HALF_WIDTH=34f;
  private static final float[][][] PATHS={
      // West potion shop: a short bend around the lawn edge.
      {{768,592},{650,560},{520,515},{420,480},{320,459}},
      // North-west weapon shop: a gentle curve into the north lane.
      {{768,592},{770,530},{752,470},{736,384}},
      // East general shop and church share a market walk.
      {{768,592},{875,545},{990,505},{1120,448},{1240,505},{1385,545},{1536,560}},
      // Long east garden walk to the inn.
      {{768,592},{900,665},{1050,710},{1230,735},{1460,744},{1710,740},{2016,736}},
      // South gate path.
      {{768,592},{715,760},{710,940},{748,1150},{790,1370},{790,1565}},
      // A looping garden walk gives the central grove a second entrance.
      {{875,545},{930,650},{1030,760},{1175,815},{1320,780},{1390,675},{1385,545}}
  };

  private static final List<Tile> TILES=build();

  private static TileKind classify(float x,float y){
    float dx=x-PLAZA_CENTER_X,dy=y-PLAZA_CENTER_Y;
    float u=dx/64f-dy/32f, v=dx/64f+dy/32f;
    int iu=Math.round(u),iv=Math.round(v);
    if(Math.abs(u-iu)>.01f||Math.abs(v-iv)>.01f)return TileKind.GROUND;
    if(Math.abs(iu)<=1&&Math.abs(iv)<=1)return TileKind.PLAZA;

    if(distanceToPaths(x,y)<=PATH_HALF_WIDTH){
      if(y>=1420f&&distanceToPath(x,y,PATHS[4])<=PATH_HALF_WIDTH+24f)return TileKind.GATE;
      return TileKind.ROAD;
    }
    return TileKind.GROUND;
  }

  private static float distanceToPaths(float x,float y){float best=Float.MAX_VALUE;for(float[][] path:PATHS)best=Math.min(best,distanceToPath(x,y,path));return best;}
  private static float distanceToPath(float x,float y,float[][] path){float best=Float.MAX_VALUE;for(int i=1;i<path.length;i++)best=Math.min(best,distanceToSegment(x,y,path[i-1][0],path[i-1][1],path[i][0],path[i][1]));return best;}
  private static float distanceToSegment(float x,float y,float ax,float ay,float bx,float by){float dx=bx-ax,dy=by-ay,len=dx*dx+dy*dy;float t=len==0f?0f:((x-ax)*dx+(y-ay)*dy)/len;t=Math.max(0f,Math.min(1f,t));float ex=x-(ax+t*dx),ey=y-(ay+t*dy);return(float)Math.sqrt(ex*ex+ey*ey);}

  private static String assetRefFor(TileKind kind,int variant){
    if(kind==TileKind.GROUND)return "video_reference/terrain/grass_tile_0"+(Math.floorMod(variant,3)+1)+".png";
    if(kind==TileKind.ROAD||kind==TileKind.GATE)return "video_reference/terrain/dirt_path_tile_0"+(Math.floorMod(variant,2)+1)+".png";
    return "terrain/OBJ_stone_01.png";
  }
}
