package com.projectdark.mobile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Evidence-safe trace/transform contract for MAP_MILLES. */
public final class MillesTraceContract {
  public enum CoordinateSpace { MASTER_TILE, PROTOTYPE_SCREEN }
  public enum TransformStatus { PENDING_ANCHORS, CALIBRATED }
  public enum TraceLayer { TILE, OBJECT, COLLISION, NPC, MONSTER_SPAWN, PORTAL }
  public enum SourceEra { LEGACY_OLD_MAP, MODERN_MAP, EVENT_MAP, UNKNOWN }
  public enum AnchorStatus { PENDING_PIXEL_IDENTIFICATION, VERIFIED }

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

  public static final class CalibrationAnchor {
    public final String sourceId,locationId,evidence;
    public final int tileX,tileY;
    public final float visualX,visualY;
    public final SourceEra era;
    public final AnchorStatus status;
    public CalibrationAnchor(String sourceId,String locationId,int tileX,int tileY,float visualX,float visualY,
        String evidence,SourceEra era,AnchorStatus status){
      this.sourceId=sourceId;this.locationId=locationId;this.tileX=tileX;this.tileY=tileY;
      this.visualX=visualX;this.visualY=visualY;this.evidence=evidence;this.era=era;this.status=status;
    }
    public boolean isVerifiedLegacyAnchor(){
      return sourceId!=null&&!sourceId.isEmpty()&&locationId!=null&&!locationId.isEmpty()
          &&evidence!=null&&!evidence.isEmpty()&&era==SourceEra.LEGACY_OLD_MAP&&status==AnchorStatus.VERIFIED;
    }
  }

  public static final class ScreenPoint {
    public final float x,y;
    ScreenPoint(float x,float y){this.x=x;this.y=y;}
  }

  private final List<MasterAnchor> masterAnchors;
  private final List<CalibrationAnchor> calibrationAnchors;
  private final MillesVisualEvidenceRegistry visualEvidence=new MillesVisualEvidenceRegistry();

  public MillesTraceContract(MillesMasterManifest manifest){
    if(manifest==null||!manifest.hasCanonicalIdentity())throw new IllegalArgumentException("Milles Master manifest required");
    List<MasterAnchor> anchors=new ArrayList<>();
    for(MillesMasterManifest.LocationRecord location:manifest.locations())
      anchors.add(new MasterAnchor(location.locationId,location.x,location.y));
    masterAnchors=Collections.unmodifiableList(anchors);
    calibrationAnchors=Collections.emptyList();
  }

  public CoordinateSpace canonicalCoordinateSpace(){return CoordinateSpace.MASTER_TILE;}
  public CoordinateSpace prototypeCoordinateSpace(){return CoordinateSpace.PROTOTYPE_SCREEN;}
  public List<MasterAnchor> masterAnchors(){return masterAnchors;}
  public List<CalibrationAnchor> calibrationAnchors(){return calibrationAnchors;}
  public MillesVisualEvidenceRegistry visualEvidence(){return visualEvidence;}

  public TransformStatus transformStatus(){return hasCalibrationEvidence()?TransformStatus.CALIBRATED:TransformStatus.PENDING_ANCHORS;}

  public boolean hasCalibrationEvidence(){
    if(visualEvidence.verifiedCalibrationSourceCount()==0)return false;
    CalibrationAnchor first=null;
    for(CalibrationAnchor anchor:calibrationAnchors){
      if(!anchor.isVerifiedLegacyAnchor())continue;
      if(first==null){first=anchor;continue;}
      boolean distinctTile=first.tileX!=anchor.tileX||first.tileY!=anchor.tileY;
      boolean distinctVisual=first.visualX!=anchor.visualX||first.visualY!=anchor.visualY;
      if(distinctTile&&distinctVisual)return true;
    }
    return false;
  }

  public ScreenPoint projectMasterToScreen(int tileX,int tileY){
    if(!canProject())throw new IllegalStateException("MAP_MILLES tile->screen transform is PENDING_ANCHORS");
    throw new IllegalStateException("MAP_MILLES calibrated projection implementation is not yet installed");
  }

  public boolean canProject(){return transformStatus()==TransformStatus.CALIBRATED;}
  public boolean hasRequiredTraceLayers(){return TraceLayer.values().length==6;}
  public boolean preservesCoordinateSeparation(){return canonicalCoordinateSpace()!=prototypeCoordinateSpace()&&!canProject();}

  public boolean passesAudit(){
    return MAP_ID.equals("MAP_MILLES")&&SOURCE_STATUS.equals("SOURCE_FOUND")&&TRACE_STATUS.equals("READY_FOR_TRACE")
        &&masterAnchors.size()==6&&hasRequiredTraceLayers()&&calibrationAnchors.isEmpty()
        &&visualEvidence.preservesEvidenceGate()&&preservesCoordinateSeparation();
  }
}
