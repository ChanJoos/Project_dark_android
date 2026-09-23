package com.projectdark.mobile.world;

import android.graphics.RectF;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Reagent shop interior as a normal World map definition.
 * Coordinates use the same authored 64x32 isometric tile-center contract as Milles.
 */
public final class ReagentShopMapDef {
  public static final String MAP_ID="MAP_MILLES_REAGENT_SHOP";
  public static final String MILLES_MAP_ID="milles_runtime_proto";
  public static final String ENTRY_SPAWN_ID="entry_south";
  public static final String EXIT_PORTAL_ID="reagent_shop_exit";
  public static final float TILE_W=64f,TILE_H=32f,HALF_W=32f,HALF_H=16f;
  public static final float MIN_X=128f,MAX_X=832f,MIN_Y=96f,MAX_Y=448f;
  public static final float ENTRY_X=480f,ENTRY_Y=400f;
  public static final float MERLIN_X=480f,MERLIN_Y=176f;

  public static final class Tile {
    public final float centerX,centerY;
    Tile(float x,float y){centerX=x;centerY=y;}
  }
  public static final class Fixture {
    public final String id; public final RectF bounds;
    Fixture(String id,float l,float t,float r,float b){this.id=id;bounds=new RectF(l,t,r,b);}
  }

  private static final List<Tile> TILES=buildTiles();
  private static final List<Fixture> FIXTURES=buildFixtures();
  private ReagentShopMapDef(){}

  public static List<Tile> tiles(){return TILES;}
  public static List<Fixture> fixtures(){return FIXTURES;}

  public static WorldPortalTransitionController.Portal exitPortal(){
    return new WorldPortalTransitionController.Portal(
        EXIT_PORTAL_ID,MAP_ID,MILLES_MAP_ID,"potion_shop_exterior",
        ENTRY_X,ENTRY_Y+16f,28f,WorldPortalTransitionController.PortalReadiness.READY);
  }

  public static boolean canOccupy(float x,float y,float radius){
    if(x-radius<MIN_X||x+radius>MAX_X||y-radius<MIN_Y||y+radius>MAX_Y)return false;
    for(Fixture f:FIXTURES){
      RectF r=f.bounds;
      if(x+radius>r.left&&x-radius<r.right&&y+radius>r.top&&y-radius<r.bottom)return false;
    }
    return true;
  }

  private static List<Tile> buildTiles(){
    ArrayList<Tile> out=new ArrayList<>();
    int row=0;
    for(float y=MIN_Y+HALF_H;y<=MAX_Y-HALF_H+.01f;y+=HALF_H,row++){
      float offset=(row&1)==0?0f:HALF_W;
      for(float x=MIN_X+HALF_W+offset;x<=MAX_X-HALF_W+.01f;x+=TILE_W)
        if(canOccupy(x,y,9f))out.add(new Tile(x,y));
    }
    return Collections.unmodifiableList(out);
  }

  private static List<Fixture> buildFixtures(){
    ArrayList<Fixture> out=new ArrayList<>();
    // Counter and shelves are collision geometry; black void is represented by map bounds.
    out.add(new Fixture("counter",304f,224f,656f,264f));
    out.add(new Fixture("shelf_west",160f,128f,224f,288f));
    out.add(new Fixture("shelf_east",736f,128f,800f,288f));
    out.add(new Fixture("back_wall",160f,96f,800f,128f));
    return Collections.unmodifiableList(out);
  }
}
