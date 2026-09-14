package com.projectdark.mobile.world;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * World-space [ADAPTED]/[B] Milles composition.
 *
 * Verified full original geometry is unavailable, so unknown space is deliberately designed as a
 * coherent quiet medieval village rather than left empty or filled with spawn-relative clutter.
 * Logical surface rectangles remain available to navigation/classification code, while visible roads,
 * water and crossings are authored as stable world-space geometry.
 */
public final class AdaptedMillesMapLayer {
  public enum SurfaceKind { GROUND, ROAD, PLAZA, GATE }
  public enum StructureKind { HOUSE, HALL, SHOP, WALL, LANDMARK }
  public enum RouteKind { PRIMARY, SECONDARY, QUIET }

  public static final class Surface {
    public final String id; public final SurfaceKind kind; public final float left,top,right,bottom; public final String evidence,status;
    Surface(String id,SurfaceKind kind,float l,float t,float r,float b,String evidence,String status){this.id=id;this.kind=kind;left=l;top=t;right=r;bottom=b;this.evidence=evidence;this.status=status;}
  }
  public static final class Structure {
    public final String id; public final StructureKind kind; public final float left,top,right,bottom; public final String evidence,status,assetRef;
    Structure(String id,StructureKind kind,float l,float t,float r,float b,String evidence,String status,String assetRef){this.id=id;this.kind=kind;left=l;top=t;right=r;bottom=b;this.evidence=evidence;this.status=status;this.assetRef=assetRef;}
  }
  public static final class Route {
    public final String id; public final RouteKind kind; public final float width; public final float[] points; public final String evidence,status;
    Route(String id,RouteKind kind,float width,String evidence,String status,float... points){
      if(points==null||points.length<4||(points.length&1)!=0)throw new IllegalArgumentException("route needs x/y pairs");
      this.id=id;this.kind=kind;this.width=width;this.evidence=evidence;this.status=status;this.points=points.clone();
    }
  }
  public static final class WaterBody {
    public final String id; public final float[] bankPoints,waterPoints; public final String evidence,status;
    WaterBody(String id,String evidence,String status,float[] bankPoints,float[] waterPoints){
      requirePolygon(bankPoints,"bank"); requirePolygon(waterPoints,"water");
      this.id=id;this.evidence=evidence;this.status=status;
      this.bankPoints=bankPoints.clone();this.waterPoints=waterPoints.clone();
    }
  }
  public static final class Crossing {
    public final String id; public final float width; public final float[] points; public final String evidence,status;
    Crossing(String id,float width,String evidence,String status,float... points){
      if(points==null||points.length<4||(points.length&1)!=0)throw new IllegalArgumentException("crossing needs x/y pairs");
      this.id=id;this.width=width;this.evidence=evidence;this.status=status;this.points=points.clone();
    }
  }

  private static final String E="ADAPTED/B";
  private static final String S="ADAPTED_COHERENT_MEDIEVAL_VILLAGE_PENDING_SOURCE_CALIBRATION";
  private static final String A="PENDING_ASSET_CLASSIFICATION";

  /**
   * Logical terrain coverage. These rectangles are intentionally broad semantic regions for existing
   * navigation/tile classification, not the visible road geometry.
   */
  private static final List<Surface> SURFACES=Collections.unmodifiableList(Arrays.asList(
      new Surface("milles_ground",SurfaceKind.GROUND,64f,48f,2304f,1600f,E,S),
      new Surface("north_service_corridor",SurfaceKind.ROAD,550f,120f,760f,520f,E,S),
      new Surface("west_craft_corridor",SurfaceKind.ROAD,150f,520f,540f,720f,E,S),
      new Surface("east_church_corridor",SurfaceKind.ROAD,780f,430f,1380f,660f,E,S),
      new Surface("central_civic_square",SurfaceKind.PLAZA,500f,480f,830f,730f,E,S),
      new Surface("south_market_corridor",SurfaceKind.ROAD,540f,690f,760f,1510f,E,S),
      new Surface("waterside_corridor",SurfaceKind.ROAD,760f,690f,1390f,1160f,E,S),
      new Surface("south_gate",SurfaceKind.GATE,570f,1450f,730f,1600f,E,"ADAPTED_EXIT_AXIS_TARGET_PENDING")));

  /** Visible road hierarchy; all routes use stable absolute world coordinates. */
  private static final List<Route> ROUTES=Collections.unmodifiableList(Arrays.asList(
      new Route("north_service_route",RouteKind.PRIMARY,112f,E,S,
          665f,535f, 650f,405f, 675f,285f, 735f,150f),
      new Route("west_craft_route",RouteKind.SECONDARY,92f,E,S,
          530f,605f, 420f,620f, 315f,665f, 185f,725f),
      new Route("east_church_route",RouteKind.PRIMARY,104f,E,S,
          800f,585f, 955f,570f, 1120f,525f, 1325f,455f),
      new Route("south_market_gate_route",RouteKind.PRIMARY,126f,E,S,
          665f,700f, 680f,860f, 645f,1040f, 625f,1240f, 650f,1515f),
      new Route("waterside_route",RouteKind.QUIET,76f,E,S,
          790f,680f, 930f,735f, 1055f,820f, 1165f,940f, 1305f,1085f)));

  /**
   * South-east water is deliberately [ADAPTED]. The source index confirms that an old Milles image
   * exists but does not identify recoverable water geometry, so this stable world-space pond/stream
   * edge supplies the required quiet waterside contrast without claiming original coordinates.
   * Bank and water are separate polygons so the renderer can show a continuous wet-ground transition.
   */
  private static final List<WaterBody> WATER_BODIES=Collections.unmodifiableList(Arrays.asList(
      new WaterBody("southeast_village_water",E,"ADAPTED_WATERSIDE_PENDING_SOURCE_IDENTIFICATION",
          new float[]{
              1320f,965f, 1450f,885f, 1620f,850f, 1810f,875f, 1990f,960f,
              2070f,1080f, 2025f,1215f, 1890f,1320f, 1690f,1360f, 1500f,1315f,
              1370f,1215f, 1290f,1090f},
          new float[]{
              1365f,985f, 1475f,925f, 1625f,895f, 1785f,915f, 1940f,985f,
              2015f,1085f, 1970f,1185f, 1860f,1270f, 1690f,1310f, 1530f,1270f,
              1415f,1185f, 1345f,1090f})));

  /**
   * A single narrow timber/stone crossing continues the waterside route across the western bank.
   * Geometry is [ADAPTED] and remains purely visual until collision/interaction phase 9.
   */
  private static final List<Crossing> CROSSINGS=Collections.unmodifiableList(Arrays.asList(
      new Crossing("southeast_bank_crossing",58f,E,"ADAPTED_CROSSING_PENDING_ASSET_REWORK",
          1275f,1070f, 1345f,1100f, 1420f,1135f)));

  /*
   * Structure footprints are retained as planning/collision candidates only. They are NOT rendered by
   * the floor pass and must be reclassified REUSE/REWORK/REMAKE/NEW before vertical assets return.
   */
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

  private static void requirePolygon(float[] points,String name){
    if(points==null||points.length<6||(points.length&1)!=0)throw new IllegalArgumentException(name+" needs 3+ x/y pairs");
  }

  private AdaptedMillesMapLayer(){}
  public static List<Surface> surfaces(){return SURFACES;}
  public static List<Route> routes(){return ROUTES;}
  public static List<WaterBody> waterBodies(){return WATER_BODIES;}
  public static List<Crossing> crossings(){return CROSSINGS;}
  public static List<Structure> structures(){return STRUCTURES;}
}
