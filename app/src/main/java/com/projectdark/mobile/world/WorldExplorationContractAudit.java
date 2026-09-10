package com.projectdark.mobile.world;

import java.util.HashSet;
import java.util.Set;

/** Android-free regression gate for the expanded [ADAPTED] village exploration contract. */
public final class WorldExplorationContractAudit {
  private WorldExplorationContractAudit(){}
  public static boolean verify(){
    if(WorldExplorationContract.millesPrototype().size()<9)return false;
    Set<String> ids=new HashSet<>();
    for(WorldExplorationContract.Anchor a:WorldExplorationContract.millesPrototype()){
      if(a.id==null||!ids.add(a.id))return false;
      if(!"ADAPTED/B".equals(a.evidence))return false;
    }
    WorldExplorationContract.Anchor west=WorldExplorationContract.byId("west_district");
    WorldExplorationContract.Anchor east=WorldExplorationContract.byId("east_district");
    WorldExplorationContract.Anchor north=WorldExplorationContract.byId("north_cross");
    WorldExplorationContract.Anchor portal=WorldExplorationContract.byId("south_portal");
    if(west==null||east==null||north==null||portal==null)return false;
    if(east.x-west.x<900f)return false;
    if(portal.y-north.y<700f)return false;
    if(WorldExplorationContract.representativeRouteLength()<2400f)return false;
    return true;
  }
  public static void main(String[] args){if(!verify())throw new AssertionError("World exploration contract audit failed");System.out.println("WorldExplorationContractAudit PASS route="+WorldExplorationContract.representativeRouteLength());}
}
