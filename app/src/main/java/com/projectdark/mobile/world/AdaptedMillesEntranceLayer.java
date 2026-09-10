package com.projectdark.mobile.world;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * [ADAPTED]/[B] visible entrance markers tied to structure IDs.
 * Entrances expose stable approach points but do not imply an interior map or active transition.
 */
public final class AdaptedMillesEntranceLayer {
  public static final class Entrance {
    public final String id,structureId,evidence,status;
    public final float footX,footY,width,approachX,approachY;
    Entrance(String id,String structureId,float footX,float footY,float width,float approachX,float approachY){
      this.id=id;this.structureId=structureId;this.footX=footX;this.footY=footY;this.width=width;
      this.approachX=approachX;this.approachY=approachY;
      evidence="ADAPTED/B";status="VISUAL_ENTRANCE_ONLY_INTERIOR_PENDING";
    }
    public long depthKey(){return (Math.round(footY*100f)<<20)|(Math.round(footX*10f)&0xfffffL);}
  }

  private static final List<Entrance> ENTRANCES=build();
  private AdaptedMillesEntranceLayer(){}
  public static List<Entrance> entrances(){return ENTRANCES;}
  public static Entrance byStructureId(String structureId){for(Entrance e:ENTRANCES)if(e.structureId.equals(structureId))return e;return null;}

  private static List<Entrance> build(){
    List<Entrance> r=new ArrayList<>();
    for(AdaptedMillesMapLayer.Structure s:AdaptedMillesMapLayer.structures()){
      if(s.kind==AdaptedMillesMapLayer.StructureKind.WALL||s.kind==AdaptedMillesMapLayer.StructureKind.LANDMARK)continue;
      AdaptedMillesIsoBuildingLayer.Building iso=AdaptedMillesIsoBuildingLayer.byStructureId(s.id);
      if(iso!=null){r.add(new Entrance("entrance_"+s.id,s.id,iso.doorFootX,iso.doorFootY,28f,iso.approachX,iso.approachY));continue;}
      float cx=(s.left+s.right)*.5f;
      float doorWidth=Math.min(54f,Math.max(34f,(s.right-s.left)*.18f));
      r.add(new Entrance("entrance_"+s.id,s.id,cx,s.bottom,doorWidth,cx,s.bottom+34f));
    }
    return Collections.unmodifiableList(r);
  }
}
