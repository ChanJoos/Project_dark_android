package com.projectdark.mobile.world;

import java.util.HashSet;
import java.util.Set;

/** Deterministic Android-free audit for the adapted village traversal semantics. */
public final class WorldSpatialLayoutAudit {
  private WorldSpatialLayoutAudit(){}

  public static boolean verify(){
    WorldSpatialLayout layout=AdaptedMillesVillageLayout.create();
    if(layout.areas().size()<6||layout.anchors().size()<8)return false;

    Set<String> ids=new HashSet<>();
    for(WorldSpatialLayout.Area a:layout.areas())if(!ids.add(a.id))return false;
    ids.clear();
    for(WorldSpatialLayout.Anchor a:layout.anchors())if(!ids.add(a.id))return false;

    WorldSpatialLayout.Anchor spawn=layout.anchor("spawn");
    WorldSpatialLayout.Anchor north=layout.anchor("north_cross");
    WorldSpatialLayout.Anchor plaza=layout.anchor("plaza_center");
    WorldSpatialLayout.Anchor west=layout.anchor("west_lane");
    WorldSpatialLayout.Anchor east=layout.anchor("east_lane");
    WorldSpatialLayout.Anchor gate=layout.anchor("south_gate_approach");
    WorldSpatialLayout.Anchor portal=layout.anchor("south_portal");
    if(spawn==null||north==null||plaza==null||west==null||east==null||gate==null||portal==null)return false;

    // Spawn/plaza/gate must all live on named navigable spatial areas.
    if(layout.areaAt(spawn.x,spawn.y)==null||layout.areaAt(plaza.x,plaza.y)==null||layout.areaAt(gate.x,gate.y)==null)return false;
    WorldSpatialLayout.Area portalArea=layout.areaAt(portal.x,portal.y);
    if(portalArea==null||portalArea.kind!=WorldSpatialLayout.AreaKind.GATE)return false;

    // The authored route should require a meaningful amount of walking instead of a tiny test room.
    float route=dist(spawn,north)+dist(north,plaza)+dist(plaza,west)+dist(west,east)+dist(east,gate)+dist(gate,portal);
    if(route<900f)return false;

    // Both lower lanes must be materially separated to provide route choice around structures.
    if(dist(west,east)<300f)return false;
    return true;
  }

  public static void main(String[] args){
    if(!verify())throw new AssertionError("World spatial layout audit failed");
    System.out.println("WorldSpatialLayoutAudit PASS");
  }

  private static float dist(WorldSpatialLayout.Anchor a,WorldSpatialLayout.Anchor b){
    float dx=a.x-b.x,dy=a.y-b.y;
    return(float)Math.sqrt(dx*dx+dy*dy);
  }
}
