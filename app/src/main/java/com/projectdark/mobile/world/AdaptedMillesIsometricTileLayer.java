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

  private static final List<Tile> TILES=build();
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

  /**
   * Milles story layout. Grass is dominant. Dirt routes are deliberately finite and asymmetric:
   * central village square -> east church, west craft/equipment quarter, north homes/service,
   * south market/gate, plus a quieter south-east waterside spur. Unknown geometry is [ADAPTED].
   * Width is two canonical tiles, but unlike the previous infinite axis test the routes stop at
   * district destinations and leave large readable grass areas between them.
   */
  private static TileKind classify(float x,float y){
    float dx=x-PLAZA_CENTER_X,dy=y-PLAZA_CENTER_Y;
    float u=dx/64f-dy/32f, v=dx/64f+dy/32f;
    int iu=Math.round(u),iv=Math.round(v);
    if(Math.abs(u-iu)>.01f||Math.abs(v-iv)>.01f)return TileKind.GROUND;
    if(Math.abs(iu)<=1&&Math.abs(iv)<=1)return TileKind.PLAZA;

    // NW: north residential/service district. Two-tile lane, finite.
    if((iv==0||iv==1)&&iu>=-13&&iu<=-2)return TileKind.ROAD;
    // SE: south market and village gate; longest civic route.
    if((iv==0||iv==1)&&iu>=2&&iu<=26)return iu>=23?TileKind.GATE:TileKind.ROAD;
    // NE: church/quiet district.
    if((iu==0||iu==1)&&iv>=-17&&iv<=-2)return TileKind.ROAD;
    // SW: craft/equipment district.
    if((iu==0||iu==1)&&iv>=2&&iv<=16)return TileKind.ROAD;
    // Quiet waterside spur branching from the southern route toward the east bank.
    if(iu>=8&&iu<=18&&(iv==-5||iv==-4))return TileKind.ROAD;
    // Small bend/connector into the spur so it does not float as an isolated stripe.
    if(iu>=7&&iu<=10&&(iv==-3||iv==-2||iv==-1))return TileKind.ROAD;
    return TileKind.GROUND;
  }

  private static String assetRefFor(TileKind kind,int variant){return "PENDING_CROP/milles/tiles/"+kind.name().toLowerCase()+"_v"+variant;}
}
