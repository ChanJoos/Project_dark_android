package com.projectdark.mobile.world;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Stable [ADAPTED]/[B] exploration anchors for the expanded Milles prototype.
 * Coordinates are world-space navigation targets, not verified original Milles geometry.
 */
public final class WorldExplorationContract {
  public static final class Anchor {
    public final String id;
    public final float x,y;
    public final String role,evidence,status;
    public Anchor(String id,float x,float y,String role,String evidence,String status){this.id=id;this.x=x;this.y=y;this.role=role;this.evidence=evidence;this.status=status;}
  }

  private static final List<Anchor> MILLES=Collections.unmodifiableList(Arrays.asList(
      new Anchor("spawn",620f,560f,"SPAWN","ADAPTED/B","PROTOTYPE"),
      new Anchor("north_cross",790f,350f,"ROAD_JUNCTION","ADAPTED/B","PROTOTYPE"),
      new Anchor("central_plaza",790f,590f,"PLAZA","ADAPTED/B","PROTOTYPE"),
      new Anchor("west_district",320f,705f,"DISTRICT_APPROACH","ADAPTED/B","PROTOTYPE"),
      new Anchor("east_district",1350f,715f,"DISTRICT_APPROACH","ADAPTED/B","PROTOTYPE"),
      new Anchor("east_market",1900f,760f,"MARKET_PLAZA","ADAPTED/B","PROTOTYPE"),
      new Anchor("east_outer_lane",2070f,1360f,"OUTER_ROAD","ADAPTED/B","PROTOTYPE"),
      new Anchor("south_west_lane",650f,900f,"ROAD","ADAPTED/B","PROTOTYPE"),
      new Anchor("south_east_lane",930f,900f,"ROAD","ADAPTED/B","PROTOTYPE"),
      new Anchor("south_commons",790f,1360f,"PLAZA","ADAPTED/B","PROTOTYPE"),
      new Anchor("south_gate",790f,1510f,"GATE_APPROACH","ADAPTED/B","PROTOTYPE_TARGET_PENDING"),
      new Anchor("south_portal",790f,1565f,"PORTAL","ADAPTED/B","PROTOTYPE_TARGET_PENDING")));

  private WorldExplorationContract(){}
  public static List<Anchor> millesPrototype(){return MILLES;}
  public static Anchor byId(String id){for(Anchor a:MILLES)if(a.id.equals(id))return a;return null;}

  public static float representativeRouteLength(){
    String[] route={"spawn","north_cross","central_plaza","west_district","central_plaza","east_district","east_market","east_outer_lane","south_commons","south_gate","south_portal"};
    float total=0f;
    for(int i=1;i<route.length;i++){Anchor a=byId(route[i-1]),b=byId(route[i]);float dx=b.x-a.x,dy=b.y-a.y;total+=(float)Math.sqrt(dx*dx+dy*dy);}
    return total;
  }
}
