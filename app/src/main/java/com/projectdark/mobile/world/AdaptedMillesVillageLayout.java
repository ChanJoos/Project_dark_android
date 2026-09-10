package com.projectdark.mobile.world;

import java.util.ArrayList;
import java.util.List;

/**
 * [ADAPTED]/[B] prototype spatial semantics for the current expanded Milles fixture.
 * This is NOT verified original Milles geometry. It exists so renderer/input/navigation can consume
 * named roads, plaza and gates from data instead of rediscovering them from hard-coded view logic.
 */
public final class AdaptedMillesVillageLayout {
  public static final String EVIDENCE="ADAPTED/B";
  public static final String STATUS="PROTOTYPE_REPLACE_WITH_VERIFIED_MILLES";

  private AdaptedMillesVillageLayout(){}

  public static WorldSpatialLayout create(){
    List<WorldSpatialLayout.Area> areas=new ArrayList<>();
    // Main north-south spine: spawn/plaza -> south gate.
    areas.add(new WorldSpatialLayout.Area("road_spine",WorldSpatialLayout.AreaKind.ROAD,
        470f,260f,720f,850f,EVIDENCE,STATUS));
    // East-west connector below the north building row.
    areas.add(new WorldSpatialLayout.Area("road_north_cross",WorldSpatialLayout.AreaKind.ROAD,
        110f,275f,1135f,350f,EVIDENCE,STATUS));
    // Central readable hub kept broad enough for camera-follow and NPC approach testing.
    areas.add(new WorldSpatialLayout.Area("central_plaza",WorldSpatialLayout.AreaKind.PLAZA,
        350f,350f,875f,625f,EVIDENCE,STATUS));
    // Lower lanes preserve alternate circulation around south structures.
    areas.add(new WorldSpatialLayout.Area("road_south_west",WorldSpatialLayout.AreaKind.ROAD,
        120f,570f,520f,655f,EVIDENCE,STATUS));
    areas.add(new WorldSpatialLayout.Area("road_south_east",WorldSpatialLayout.AreaKind.ROAD,
        680f,585f,1140f,660f,EVIDENCE,STATUS));
    areas.add(new WorldSpatialLayout.Area("south_gate",WorldSpatialLayout.AreaKind.GATE,
        530f,735f,655f,864f,EVIDENCE,"PROTOTYPE_TARGET_PENDING"));

    List<WorldSpatialLayout.Anchor> anchors=new ArrayList<>();
    anchors.add(new WorldSpatialLayout.Anchor("spawn",500f,470f,"player spawn",EVIDENCE,STATUS));
    anchors.add(new WorldSpatialLayout.Anchor("plaza_center",590f,485f,"central navigation hub",EVIDENCE,STATUS));
    anchors.add(new WorldSpatialLayout.Anchor("north_cross",590f,315f,"north road junction",EVIDENCE,STATUS));
    anchors.add(new WorldSpatialLayout.Anchor("service_npc_approach",800f,315f,"east service approach",EVIDENCE,STATUS));
    anchors.add(new WorldSpatialLayout.Anchor("west_lane",390f,610f,"west lower-lane waypoint",EVIDENCE,STATUS));
    anchors.add(new WorldSpatialLayout.Anchor("east_lane",780f,625f,"east lower-lane waypoint",EVIDENCE,STATUS));
    anchors.add(new WorldSpatialLayout.Anchor("south_gate_approach",590f,760f,"south gate approach",EVIDENCE,"PROTOTYPE_TARGET_PENDING"));
    anchors.add(new WorldSpatialLayout.Anchor("south_portal",590f,842f,"portal activation anchor",EVIDENCE,"PROTOTYPE_TARGET_PENDING"));
    return new WorldSpatialLayout(areas,anchors);
  }
}
