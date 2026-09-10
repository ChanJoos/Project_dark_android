package com.projectdark.mobile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Evidence-safe trace/transform contract for MAP_MILLES.
 *
 * Master tile coordinates and current prototype screen coordinates are intentionally distinct spaces.
 * No tile->screen transform is exposed until authenticated trace anchors are available.
 */
public final class MillesTraceContract {
  public enum CoordinateSpace { MASTER_TILE, PROTOTYPE_SCREEN }
  public enum TransformStatus { PENDING_ANCHORS, CALIBRATED }
  public enum TraceLayer { TILE, OBJECT, COLLISION, NPC, MONSTER_SPAWN, PORTAL }

  public static final String MAP_ID=MillesMasterManifest.MAP_ID;
  public static final String SOURCE_STATUS="SOURCE_FOUND";
  public static final String TRACE_STATUS="READY_FOR_TRACE";
  public static final String EVIDENCE="V";
  public static final String IDENTIFICATION_STATUS="PENDING";

  public static final class MasterAnchor {
    public final String locationId;
    public final int tileX,tileY;
    MasterAnchor(String locationId,int tileX,int tileY){this.locationId=locationId;this.tileX=tileX;this.tileY=tileY;}
  }

  public static final class ScreenPoint {
    public final float x,y;
    ScreenPoint(float x,float y){this.x=x;this.y=y;}
  }

  private final List<MasterAnchor> masterAnchors;

  public MillesTraceContract(MillesMasterManifest manifest){
    if(manifest==null||!manifest.hasCanonicalIdentity())throw new IllegalArgumentException("Milles Master manifest required");
    List<MasterAnchor> anchors=new ArrayList<>();
    for(MillesMasterManifest.LocationRecord location:manifest.locations())
      anchors.add(new MasterAnchor(location.locationId,location.x,location.y));
    masterAnchors=Collections.unmodifiableList(anchors);
  }

  public CoordinateSpace canonicalCoordinateSpace(){return CoordinateSpace.MASTER_TILE;}
  public CoordinateSpace prototypeCoordinateSpace(){return CoordinateSpace.PROTOTYPE_SCREEN;}
  public TransformStatus transformStatus(){return TransformStatus.PENDING_ANCHORS;}
  public List<MasterAnchor> masterAnchors(){return masterAnchors;}

  /**
   * Deliberately refuses projection while calibration is unresolved. Callers must not infer a transform
   * from the screenshot, prototype blockers, player spawn, or arbitrary fitting.
   */
  public ScreenPoint projectMasterToScreen(int tileX,int tileY){
    throw new IllegalStateException("MAP_MILLES tile->screen transform is PENDING_ANCHORS");
  }

  public boolean canProject(){return transformStatus()==TransformStatus.CALIBRATED;}

  public boolean hasRequiredTraceLayers(){
    return TraceLayer.values().length==6;
  }

  public boolean preservesCoordinateSeparation(){
    return canonicalCoordinateSpace()!=prototypeCoordinateSpace()&&!canProject();
  }

  public boolean passesAudit(){
    return MAP_ID.equals("MAP_MILLES")
        &&SOURCE_STATUS.equals("SOURCE_FOUND")
        &&TRACE_STATUS.equals("READY_FOR_TRACE")
        &&masterAnchors.size()==6
        &&hasRequiredTraceLayers()
        &&preservesCoordinateSeparation();
  }
}