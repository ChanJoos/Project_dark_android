package com.projectdark.mobile.world;

/**
 * Deterministic classic-style Milles reagent-shop interior.
 * World owns room geometry, collision, portal and NPC hit geometry.
 */
public final class MillesReagentShopInterior {
  public static final String MAP_ID="milles_interior_reagent_shop";
  public static final String NPC_ID="milles_merlin";
  public static final String NPC_NAME="멀린";
  public static final float MIN_X=248f,MAX_X=712f,MIN_Y=145f,MAX_Y=438f;
  public static final float SPAWN_X=480f,SPAWN_Y=410f;
  public static final float EXIT_L=430f,EXIT_R=530f,EXIT_Y=430f;
  public static final float NPC_X=535f,NPC_Y=235f;

  public static final class Snapshot {
    public final boolean active; public final float x,y;
    Snapshot(boolean active,float x,float y){this.active=active;this.x=x;this.y=y;}
  }

  private boolean active; private float x,y,returnX,returnY;

  public boolean enter(float exteriorX,float exteriorY){
    if(active)return false;active=true;returnX=exteriorX;returnY=exteriorY;x=SPAWN_X;y=SPAWN_Y;return true;
  }
  public boolean active(){return active;} public float x(){return x;} public float y(){return y;}
  public float returnX(){return returnX;} public float returnY(){return returnY;}
  public boolean move(float dx,float dy,float distance){
    if(!active||distance<=0)return false;float len=(float)Math.sqrt(dx*dx+dy*dy);if(len<.001f)return false;
    float nx=clamp(x+dx/len*distance,MIN_X,MAX_X),ny=clamp(y+dy/len*distance,MIN_Y,MAX_Y);
    // Counter occupies the north-center band; player can walk around its ends but not through it.
    if(ny<330f&&ny>250f&&nx>300f&&nx<710f)ny=y;
    boolean changed=Math.abs(nx-x)>.001f||Math.abs(ny-y)>.001f;x=nx;y=ny;return changed;
  }
  public boolean atExit(){return active&&y>=EXIT_Y&&x>=EXIT_L&&x<=EXIT_R;}
  public boolean hitMerlin(float sx,float sy){float dx=sx-NPC_X,dy=sy-NPC_Y;return active&&dx*dx+dy*dy<=34f*34f;}
  public boolean exit(){if(!active)return false;active=false;return true;}
  public Snapshot snapshot(){return new Snapshot(active,x,y);}
  private static float clamp(float v,float a,float b){return Math.max(a,Math.min(b,v));}
}
