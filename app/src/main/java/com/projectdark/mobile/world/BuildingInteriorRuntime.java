package com.projectdark.mobile.world;

/**
 * Small deterministic interior runtime for B4 building transitions.
 * Coordinates are logical 960x540 presentation coordinates and are deliberately independent from
 * the Milles exterior so entering a building never mutates exterior collision or quest state.
 */
public final class BuildingInteriorRuntime {
  public static final String POTION_SHOP_MAP_ID="milles_interior_potion_shop";
  public static final String POTION_SHOP_SPAWN_ID="entry_south";
  public static final String POTION_SHOP_EXIT_ID="exit_to_milles";

  public static final class Snapshot {
    public final boolean active;
    public final String mapId;
    public final float x,y;
    Snapshot(boolean active,String mapId,float x,float y){this.active=active;this.mapId=mapId;this.x=x;this.y=y;}
  }

  private boolean active;
  private String mapId;
  private float x,y;
  private float exteriorReturnX,exteriorReturnY;

  public boolean enterPotionShop(float returnX,float returnY){
    if(active)return false;
    active=true;mapId=POTION_SHOP_MAP_ID;
    exteriorReturnX=returnX;exteriorReturnY=returnY;
    x=480f;y=420f;
    return true;
  }

  public boolean active(){return active;}
  public String mapId(){return mapId;}
  public float x(){return x;} public float y(){return y;}
  public float exteriorReturnX(){return exteriorReturnX;} public float exteriorReturnY(){return exteriorReturnY;}

  public boolean move(float dx,float dy,float distance){
    if(!active||distance<=0f)return false;
    float len=(float)Math.sqrt(dx*dx+dy*dy);if(len<.001f)return false;
    float nx=clamp(x+dx/len*distance,270f,690f);
    float ny=clamp(y+dy/len*distance,155f,438f);
    boolean changed=Math.abs(nx-x)>.001f||Math.abs(ny-y)>.001f;x=nx;y=ny;return changed;
  }

  public boolean atExit(){return active&&y>=430f&&x>=430f&&x<=530f;}

  public boolean exit(){
    if(!active)return false;
    active=false;mapId=null;return true;
  }

  public Snapshot snapshot(){return new Snapshot(active,mapId,x,y);}
  private static float clamp(float v,float min,float max){return Math.max(min,Math.min(max,v));}
}
