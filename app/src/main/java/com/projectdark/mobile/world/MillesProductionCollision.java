package com.projectdark.mobile.world;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * World-owned ground-contact collision model for the current Milles production vertical slice.
 *
 * These are deliberately small ground footprints, not sprite bounding boxes. Visual sprites are
 * bottom-center anchored; collision only covers the area where an object meets the walk plane.
 */
public final class MillesProductionCollision {
  public enum Kind { BUILDING, CHURCH, TREE, WELL, STALL, FENCE, LAKE }

  public static final class Footprint {
    public final String id;
    public final Kind kind;
    public final float left,top,right,bottom;
    Footprint(String id,Kind kind,float left,float top,float right,float bottom){
      this.id=id;this.kind=kind;this.left=left;this.top=top;this.right=right;this.bottom=bottom;
    }
    public float centerX(){return (left+right)*.5f;}
    public float centerY(){return (top+bottom)*.5f;}
    public boolean contains(float x,float y){return x>=left&&x<=right&&y>=top&&y<=bottom;}
    public boolean blocksCircle(float x,float y,float radius){
      return x+radius>left&&x-radius<right&&y+radius>top&&y-radius<bottom;
    }
  }

  public static final class Approach {
    public final String id;
    public final float x,y;
    Approach(String id,float x,float y){this.id=id;this.x=x;this.y=y;}
  }

  private static final float SX=620f,SY=560f;
  private static final List<Footprint> BLOCKERS;
  private static final List<Approach> ENTRANCES;

  static {
    List<Footprint> b=new ArrayList<>();

    // B1/B5 visible-building ground contact. These coordinates match the actual renderer anchors.
    // Footprints are deliberately much shallower than sprite bounds: only the ground mass blocks.
    add(b,"potion_shop",Kind.BUILDING,320f,420f,210f,78f);
    add(b,"weapon_shop",Kind.BUILDING,760f,300f,205f,78f);
    add(b,"general_shop",Kind.BUILDING,1120f,360f,205f,78f);
    add(b,"church",Kind.CHURCH,1540f,455f,270f,105f);
    add(b,"inn",Kind.BUILDING,1980f,650f,205f,80f);

    // Nature/street objects: only the trunk/base/ring/stall base blocks the walk plane.
    add(b,"tree_01",Kind.TREE,SX-330f,SY+45f,22f,18f);
    add(b,"tree_02",Kind.TREE,SX-270f,SY+125f,22f,18f);
    add(b,"tree_03",Kind.TREE,SX+455f,SY+110f,22f,18f);
    add(b,"well",Kind.WELL,SX+35f,SY+70f,34f,20f);
    add(b,"stall_01",Kind.STALL,SX-225f,SY+205f,58f,22f);

    // Fences are narrow ground strips; surrounding logical cells remain navigable.
    for(int i=0;i<4;i++){
      add(b,"fence_w_"+i,Kind.FENCE,SX-390f+i*72f,SY+245f,50f,10f);
      add(b,"fence_e_"+i,Kind.FENCE,SX+245f+i*72f,SY+300f,50f,10f);
    }

    // The lake uses shoreline-aware strips instead of one sprite-sized box. This keeps the water
    // interior blocked without swallowing the surrounding land path.
    float lx=SX+325f,ly=SY+210f;
    rect(b,"lake_core",Kind.LAKE,lx-92f,ly-50f,lx+92f,ly+42f);
    rect(b,"lake_west_lobe",Kind.LAKE,lx-118f,ly-30f,lx-86f,ly+24f);
    rect(b,"lake_east_lobe",Kind.LAKE,lx+86f,ly-26f,lx+116f,ly+20f);
    BLOCKERS=Collections.unmodifiableList(b);

    // Reachable south-side interaction anchors for the B1/B5 building placement.
    List<Approach> a=new ArrayList<>();
    a.add(new Approach("potion_shop",320f,496f));
    a.add(new Approach("weapon_shop",768f,384f));
    a.add(new Approach("general_shop",1120f,448f));
    a.add(new Approach("church",1536f,560f));
    a.add(new Approach("inn",1984f,736f));
    ENTRANCES=Collections.unmodifiableList(a);
  }

  private MillesProductionCollision(){}
  public static List<Footprint> blockers(){return BLOCKERS;}
  public static List<Approach> entrances(){return ENTRANCES;}

  public static boolean blocked(float x,float y,float radius){
    for(Footprint f:BLOCKERS)if(f.blocksCircle(x,y,radius))return true;
    return false;
  }

  public static boolean lakeWater(float x,float y){
    for(Footprint f:BLOCKERS)if(f.kind==Kind.LAKE&&f.contains(x,y))return true;
    return false;
  }

  private static void add(List<Footprint> out,String id,Kind kind,float cx,float cy,float width,float depth){
    rect(out,id,kind,cx-width*.5f,cy-depth*.5f,cx+width*.5f,cy+depth*.5f);
  }
  private static void rect(List<Footprint> out,String id,Kind kind,float left,float top,float right,float bottom){
    out.add(new Footprint(id,kind,left,top,right,bottom));
  }
}
