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

  /** Narrow, single-cell path spines. Each branch is authored once; joins occur only at the square. */
  private static final float PATH_HALF_WIDTH=20f;
  private static final float[][][] PATHS={
      // Four distinct village arms. The plaza is the only shared junction.
      {{768,592},{704,576},{640,544},{576,512},{512,480},{448,464},{384,448},{320,448}},
      {{768,592},{768,544},{752,496},{752,448},{736,400},{736,368}},
      {{768,592},{832,560},{896,528},{960,480},{1024,432},{1088,400},{1152,400},{1216,416},{1280,432},{1344,448},{1408,448},{1472,448},{1536,448}},
      {{768,592},{832,640},{896,672},{960,704},{1056,720},{1184,736},{1312,752},{1440,768},{1568,784},{1696,784},{1824,752},{1920,704},{1984,656}},
      {{768,592},{736,656},{720,720},{704,784},{704,848},{720,912},{736,976},{752,1040},{768,1104},{784,1168},{800,1232},{800,1328},{800,1456},{800,1568}}
  };

  private static final List<Tile> TILES=build();

  /** World-coordinate road centerlines for the renderer's terrain overlay. */
  public static float[][][] roadPaths(){
    float[][][] copy=new float[PATHS.length][][];
    for(int i=0;i<PATHS.length;i++){copy[i]=new float[PATHS[i].length][];for(int j=0;j<PATHS[i].length;j++)copy[i][j]=PATHS[i][j].clone();}
    return copy;
  }

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
