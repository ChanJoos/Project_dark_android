package com.projectdark.mobile.world;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * [ADAPTED]/[B] renderer-facing structure profiles for the Milles prototype.
 * Geometry is derived from the collision-aligned structure layer; visual heights are prototype-only.
 */
public final class AdaptedMillesStructureVisualLayer {
  public static final class Visual {
    public final String structureId;
    public final AdaptedMillesMapLayer.StructureKind kind;
    public final float wallHeight,roofRise,roofOverhang,doorWidth,doorHeight;
    public final boolean hasDoor;
    public final String evidence,status,assetRef;

    Visual(AdaptedMillesMapLayer.Structure s,float wallHeight,float roofRise,float roofOverhang,
        float doorWidth,float doorHeight,boolean hasDoor){
      structureId=s.id;kind=s.kind;
      this.wallHeight=wallHeight;this.roofRise=roofRise;this.roofOverhang=roofOverhang;
      this.doorWidth=doorWidth;this.doorHeight=doorHeight;this.hasDoor=hasDoor;
      evidence="ADAPTED/B";
      status="PROTOTYPE_SILHOUETTE_REPLACE_WITH_VERIFIED_MILLES";
      assetRef="PENDING_CROP/milles/structure/"+s.id;
    }
  }

  private static final List<Visual> VISUALS=build();
  private AdaptedMillesStructureVisualLayer(){}
  public static List<Visual> visuals(){return VISUALS;}
  public static Visual byStructureId(String id){for(Visual v:VISUALS)if(v.structureId.equals(id))return v;return null;}

  private static List<Visual> build(){
    List<Visual> result=new ArrayList<>();
    for(AdaptedMillesMapLayer.Structure s:AdaptedMillesMapLayer.structures()){
      switch(s.kind){
        case HALL: result.add(new Visual(s,82f,44f,18f,42f,48f,true));break;
        case SHOP: result.add(new Visual(s,68f,34f,16f,48f,42f,true));break;
        case HOUSE: result.add(new Visual(s,62f,38f,14f,36f,40f,true));break;
        case WALL: result.add(new Visual(s,42f,0f,4f,0f,0f,false));break;
        case LANDMARK: result.add(new Visual(s,52f,22f,8f,0f,0f,false));break;
      }
    }
    return Collections.unmodifiableList(result);
  }
}
