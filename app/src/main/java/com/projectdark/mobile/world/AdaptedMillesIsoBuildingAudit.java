package com.projectdark.mobile.world;

/** Geometry audit for the opening-screen coherent isometric village buildings and entrances. */
public final class AdaptedMillesIsoBuildingAudit {
  private AdaptedMillesIsoBuildingAudit(){}
  public static boolean verify(){
    if(AdaptedMillesIsoBuildingLayer.buildings().size()!=3)return false;
    for(AdaptedMillesIsoBuildingLayer.Building building:AdaptedMillesIsoBuildingLayer.buildings()){
      if(!close(building.halfWidth,building.halfDepth*2f))return false;
      AdaptedMillesMapLayer.Structure structure=null;
      for(AdaptedMillesMapLayer.Structure candidate:AdaptedMillesMapLayer.structures())if(building.structureId.equals(candidate.id)){structure=candidate;break;}
      if(structure==null||structure.kind==AdaptedMillesMapLayer.StructureKind.WALL||structure.kind==AdaptedMillesMapLayer.StructureKind.LANDMARK)return false;
      if(!close(structure.left,building.collisionLeft)||!close(structure.top,building.collisionTop)
          ||!close(structure.right,building.collisionRight)||!close(structure.bottom,building.collisionBottom))return false;
      AdaptedMillesEntranceLayer.Entrance entrance=AdaptedMillesEntranceLayer.byStructureId(building.structureId);
      if(entrance==null||!close(entrance.footX,building.doorFootX)||!close(entrance.footY,building.doorFootY)
          ||!close(entrance.approachX,building.approachX)||!close(entrance.approachY,building.approachY))return false;
      AdaptedMillesIsometricTileLayer.Tile approach=null;
      for(AdaptedMillesIsometricTileLayer.Tile tile:AdaptedMillesIsometricTileLayer.tiles())
        if(close(tile.centerX,entrance.approachX)&&close(tile.centerY,entrance.approachY)){approach=tile;break;}
      if(approach==null||building.footprintContains(approach.centerX,approach.centerY))return false;
      if(!building.footprintContains(building.centerX,building.centerY)
          ||building.footprintContains(building.collisionRight,building.collisionBottom))return false;
    }
    return true;
  }
  private static boolean close(float a,float b){return Math.abs(a-b)<.01f;}
  public static void main(String[] args){if(!verify())throw new AssertionError("AdaptedMillesIsoBuildingAudit failed");System.out.println("AdaptedMillesIsoBuildingAudit PASS");}
}
