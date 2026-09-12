package com.projectdark.mobile.world;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Renderable [ADAPTED]/[B] Milles prototype map composition.
 * This is not claimed as verified original geometry; replace zone-by-zone as source evidence is calibrated.
 *
 * Gate C composition rule: broad grass is the default. Bright stone is reserved for a small readable
 * village spine, shop/church frontage, plaza, market approach, lake approach, and south exit. This
 * deliberately avoids the former overlapping road/plaza checker that made the production slice noisy.
 */
public final class AdaptedMillesMapLayer {
  public enum SurfaceKind { GROUND, ROAD, PLAZA, GATE }
  public enum StructureKind { HOUSE, HALL, SHOP, WALL, LANDMARK }

  public static final class Surface {
    public final String id; public final SurfaceKind kind; public final float left,top,right,bottom; public final String evidence,status;
    Surface(String id,SurfaceKind kind,float l,float t,float r,float b,String evidence,String status){this.id=id;this.kind=kind;left=l;top=t;right=r;bottom=b;this.evidence=evidence;this.status=status;}
  }
  public static final class Structure {
    public final String id; public final StructureKind kind; public final float left,top,right,bottom; public final String evidence,status,assetRef;
    Structure(String id,StructureKind kind,float l,float t,float r,float b,String evidence,String status,String assetRef){this.id=id;this.kind=kind;left=l;top=t;right=r;bottom=b;this.evidence=evidence;this.status=status;this.assetRef=assetRef;}
  }

  private static final String E="ADAPTED/B", S="PROTOTYPE_REPLACE_WITH_VERIFIED_MILLES", A="PENDING_CROP";

  /**
   * World-owned authored surface composition for the first playable Milles slice.
   *
   * Landmarks in the current production placement cluster around spawn (620,560): potion shop west,
   * weapon/general shops north, church north-east, inn east, well/plaza center, market south-west,
   * lake south-east. Keep those destinations connected without paving the entire village.
   */
  private static final List<Surface> SURFACES=Collections.unmodifiableList(Arrays.asList(
      // Broad grass field: visual breathing room is the default everywhere.
      new Surface("milles_ground",SurfaceKind.GROUND,64f,48f,2304f,1600f,E,S),

      // Single bright northern frontage connecting potion -> weapon -> general -> church -> inn.
      new Surface("north_landmark_frontage",SurfaceKind.ROAD,300f,365f,1075f,485f,E,S),

      // Compact center around spawn/well. Intentionally much smaller than the former 540x355 plaza.
      new Surface("central_plaza",SurfaceKind.PLAZA,500f,500f,790f,690f,E,S),

      // Main south spine from central plaza toward market/exit. One readable route, not parallel lanes.
      new Surface("south_spine",SurfaceKind.ROAD,585f,650f,715f,1510f,E,S),

      // Short west branch to the stall/cart district while preserving grass around props.
      new Surface("west_market_branch",SurfaceKind.ROAD,355f,700f,585f,805f,E,S),

      // Short east branch approaches the lake shoreline but does not pave beneath the lake landmark.
      new Surface("lake_approach",SurfaceKind.ROAD,700f,675f,900f,755f,E,S),

      // Small south commons landing before the exit rather than a second giant plaza.
      new Surface("south_commons",SurfaceKind.PLAZA,525f,1260f,825f,1450f,E,S),
      new Surface("south_gate",SurfaceKind.GATE,585f,1450f,715f,1600f,E,"PROTOTYPE_TARGET_PENDING")));

  private static final List<Structure> STRUCTURES=Collections.unmodifiableList(Arrays.asList(
      new Structure("northwest_house",StructureKind.HOUSE,150f,105f,390f,300f,E,S,A),
      new Structure("north_hall",StructureKind.HALL,540f,90f,800f,280f,E,S,A),
      new Structure("northeast_house",StructureKind.HOUSE,1040f,110f,1320f,315f,E,S,A),
      new Structure("west_house",StructureKind.HOUSE,256f,528f,384f,592f,E,S,A),
      new Structure("east_shop",StructureKind.SHOP,1210f,410f,1480f,660f,E,S,A),
      new Structure("southwest_house",StructureKind.HOUSE,330f,760f,570f,980f,E,S,A),
      new Structure("southeast_house",StructureKind.HOUSE,1000f,770f,1260f,995f,E,S,A),
      new Structure("plaza_landmark_a",StructureKind.SHOP,672f,480f,800f,544f,E,S,A),
      new Structure("plaza_landmark_b",StructureKind.HOUSE,800f,608f,928f,672f,E,S,A),
      new Structure("west_perimeter",StructureKind.WALL,80f,820f,250f,1050f,E,S,A),
      new Structure("east_perimeter",StructureKind.WALL,1390f,790f,1580f,1040f,E,S,A),
      new Structure("market_north_shop",StructureKind.SHOP,1660f,420f,1900f,580f,E,S,A),
      new Structure("market_northeast_house",StructureKind.HOUSE,1960f,420f,2200f,600f,E,S,A),
      new Structure("east_warehouse",StructureKind.HALL,2140f,700f,2260f,950f,E,S,A),
      new Structure("market_south_house",StructureKind.HOUSE,1700f,1000f,1940f,1200f,E,S,A),
      new Structure("southeast_outer_house",StructureKind.HOUSE,2020f,1120f,2220f,1330f,E,S,A),
      new Structure("southwest_outer_house",StructureKind.HOUSE,300f,1100f,560f,1300f,E,S,A),
      new Structure("south_commons_house",StructureKind.HOUSE,1040f,1120f,1300f,1320f,E,S,A),
      new Structure("south_gate_west_wall",StructureKind.WALL,520f,1460f,700f,1580f,E,S,A),
      new Structure("south_gate_east_wall",StructureKind.WALL,880f,1460f,1060f,1580f,E,S,A)));

  private AdaptedMillesMapLayer(){}
  public static List<Surface> surfaces(){return SURFACES;}
  public static List<Structure> structures(){return STRUCTURES;}
}
