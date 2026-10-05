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
    // Buildings are solid exterior objects. Their complete authored exterior footprint is blocked; entry happens only through a front-door portal.
    rect(b,"potion_shop",Kind.BUILDING,200f,250f,440f,459f);
    rect(b,"weapon_shop",Kind.BUILDING,640f,125f,880f,339f);
    rect(b,"bank",Kind.BUILDING,1000f,165f,1240f,399f);
    rect(b,"church",Kind.CHURCH,1370f,150f,1710f,494f);
    rect(b,"inn",Kind.BUILDING,1860f,430f,2100f,689f);
    // Non-enterable residential houses use small ground-contact footprints.
    rect(b,"house_west",Kind.BUILDING,-60f,530f,180f,739f);
    rect(b,"house_southwest",Kind.BUILDING,280f,870f,520f,1079f);
    rect(b,"house_south",Kind.BUILDING,1000f,880f,1240f,1089f);
    rect(b,"house_east",Kind.BUILDING,1410f,1000f,1650f,1209f);
    rect(b,"house_northeast",Kind.BUILDING,1760f,150f,2000f,359f);

    // Ground contact of the authored palisade enclosure. Keep the southwest opening walkable.
    add(b,"garden_fence_nw_1",Kind.FENCE,996f,530f,38f,16f);
    add(b,"garden_fence_nw_2",Kind.FENCE,1030f,510f,38f,16f);
    add(b,"garden_fence_nw_3",Kind.FENCE,1064f,490f,38f,16f);
    add(b,"garden_fence_ne_1",Kind.FENCE,1096f,490f,38f,16f);
    add(b,"garden_fence_ne_2",Kind.FENCE,1130f,510f,38f,16f);
    add(b,"garden_fence_ne_3",Kind.FENCE,1164f,530f,38f,16f);
    add(b,"garden_fence_se_1",Kind.FENCE,1164f,550f,38f,16f);
    add(b,"garden_fence_se_2",Kind.FENCE,1130f,570f,38f,16f);
    add(b,"garden_fence_se_3",Kind.FENCE,1096f,590f,38f,16f);
    add(b,"garden_fence_sw_3",Kind.FENCE,1064f,590f,38f,16f);
    MillesDistrictFenceFootprints.addTo(b);

    // Only currently visible buildings and the garden palisade are collision-authoritative.
    // Invisible legacy scenery footprints are intentionally excluded from runtime blockers.
    BLOCKERS=Collections.unmodifiableList(b);

    // Reachable south-side interaction anchors for the B1/B5 building placement.
    List<Approach> a=new ArrayList<>();
    a.add(new Approach("potion_shop",320f,496f));
    a.add(new Approach("weapon_shop",736f,384f));
    a.add(new Approach("bank",1120f,448f));
    a.add(new Approach("church",1536f,560f));
    a.add(new Approach("inn",2016f,736f));
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
