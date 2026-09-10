package com.projectdark.mobile.world;

/** Geometry audit for the first integrated M2 building and its road-facing entrance. */
public final class AdaptedMillesIsoBuildingAudit {
  private AdaptedMillesIsoBuildingAudit(){}
  public static boolean verify(){
    AdaptedMillesIsoBuildingLayer.Building building=AdaptedMillesIsoBuildingLayer.byStructureId("plaza_landmark_a");
    if(building==null||building.halfWidth!=building.halfDepth*2f)return false;
    AdaptedMillesMapLayer.Structure structure=null;
    for(AdaptedMillesMapLayer.Structure candidate:AdaptedMillesMapLayer.structures())if(building.structureId.equals(candidate.id)){structure=candidate;break;}
    if(structure==null||structure.kind!=AdaptedMillesMapLayer.StructureKind.SHOP)return false;
    if(!close(structure.left,building.collisionLeft)||!close(structure.top,building.collisionTop)
        ||!close(structure.right,building.collisionRight)||!close(structure.bottom,building.collisionBottom))return false;
    AdaptedMillesEntranceLayer.Entrance entrance=AdaptedMillesEntranceLayer.byStructureId(building.structureId);
    if(entrance==null||!close(entrance.footX,building.doorFootX)||!close(entrance.footY,building.doorFootY)
        ||!close(entrance.approachX,building.approachX)||!close(entrance.approachY,building.approachY))return false;
    AdaptedMillesIsometricTileLayer.Tile approach=null;
    for(AdaptedMillesIsometricTileLayer.Tile tile:AdaptedMillesIsometricTileLayer.tiles())
      if(close(tile.centerX,entrance.approachX)&&close(tile.centerY,entrance.approachY)){approach=tile;break;}
    if(approach==null||approach.kind!=AdaptedMillesIsometricTileLayer.TileKind.PLAZA)return false;
    return building.footprintContains(building.centerX,building.centerY)
        &&!building.footprintContains(building.collisionRight,building.collisionBottom);
  }
  private static boolean close(float a,float b){return Math.abs(a-b)<.01f;}
  public static void main(String[] args){if(!verify())throw new AssertionError("AdaptedMillesIsoBuildingAudit failed");System.out.println("AdaptedMillesIsoBuildingAudit PASS");}
}
