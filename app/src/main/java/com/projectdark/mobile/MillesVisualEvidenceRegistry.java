package com.projectdark.mobile;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Evidence registry for Milles visual-source candidates gathered from Nexon-hosted pages.
 *
 * The registry deliberately separates SOURCE_LOCATED from PIXEL_VERIFIED. A source may be useful
 * evidence without being eligible for map calibration until the actual pixels/landmark identity are
 * positively inspected.
 */
public final class MillesVisualEvidenceRegistry {
  public enum SourceStatus { SOURCE_LOCATED, PIXEL_FETCH_PENDING, PIXEL_VERIFIED }
  public enum GeometryUse { LEGACY_CALIBRATION_CANDIDATE, SPATIAL_RELATION_REFERENCE, REFERENCE_ONLY }

  public static final class SourceRecord {
    public final String sourceId,pageUrl,title,published,evidence;
    public final SourceStatus status;
    public final GeometryUse geometryUse;
    public final String[] relatedLocationIds;

    SourceRecord(String sourceId,String pageUrl,String title,String published,String evidence,
        SourceStatus status,GeometryUse geometryUse,String... relatedLocationIds){
      this.sourceId=sourceId;
      this.pageUrl=pageUrl;
      this.title=title;
      this.published=published;
      this.evidence=evidence;
      this.status=status;
      this.geometryUse=geometryUse;
      this.relatedLocationIds=relatedLocationIds;
    }

    public boolean canCalibrate(){
      return status==SourceStatus.PIXEL_VERIFIED
          &&geometryUse==GeometryUse.LEGACY_CALIBRATION_CANDIDATE;
    }
  }

  private static final List<SourceRecord> SOURCES=Collections.unmodifiableList(Arrays.asList(
      new SourceRecord(
          "NX_MILLES_OLD_TOWN_MAP_SET_20210601",
          "https://lod.nexon.com/Community/screenshot/136184?Category2=2",
          "구 마을맵 지도 동서북 밀레스 구아벨 구뤼케시온 구루어스 구마인",
          "2021-06-01 13:27","V",SourceStatus.PIXEL_FETCH_PENDING,
          GeometryUse.LEGACY_CALIBRATION_CANDIDATE,
          "LOC_MILLES_CASTLE","LOC_MILLES_SEA","LOC_MILLES_ALTAR","LOC_MILLES_ROSEMARY","LOC_MILLES_MODIA","LOC_MILLES_TELLIDOAH"),
      new SourceRecord(
          "NX_MILLES_GILLENOA_ROSEMARY_20040527",
          "https://lod.nexon.com/Community/screenshot/121256?Category2=2",
          "헉...길레노아 실종사건",
          "2004-05-27 03:10","V",SourceStatus.PIXEL_FETCH_PENDING,
          GeometryUse.SPATIAL_RELATION_REFERENCE,
          "LOC_MILLES_ROSEMARY"),
      new SourceRecord(
          "NX_MILLES_HOMELESS_20051126",
          "https://lod.nexon.com/Community/screenshot/125266?Category2=2",
          "밀레스의 노숙자들",
          "2005-11-26 21:49","V",SourceStatus.PIXEL_FETCH_PENDING,
          GeometryUse.REFERENCE_ONLY),
      new SourceRecord(
          "NX_MILLES_EVENT_MAP_20240411",
          "https://lod.nexon.com/Community/screenshot/137421?Category2=2",
          "밀레스 이벤트 맵",
          "2024-04-11 12:23","V",SourceStatus.PIXEL_FETCH_PENDING,
          GeometryUse.REFERENCE_ONLY),
      new SourceRecord(
          "NX_MILLES_MODERN_SCENE_20240418_137555",
          "https://lod.nexon.com/community/screenshot/137555",
          "[넥슨30주년] 밀레스마을, 감탄을 하게되네요..",
          "2024-04-18 10:15","V",SourceStatus.PIXEL_FETCH_PENDING,
          GeometryUse.REFERENCE_ONLY)
  ));

  public List<SourceRecord> sources(){return SOURCES;}

  public int calibrationCandidateCount(){
    int count=0;
    for(SourceRecord source:SOURCES)
      if(source.geometryUse==GeometryUse.LEGACY_CALIBRATION_CANDIDATE)count++;
    return count;
  }

  public int verifiedCalibrationSourceCount(){
    int count=0;
    for(SourceRecord source:SOURCES)if(source.canCalibrate())count++;
    return count;
  }

  public boolean preservesEvidenceGate(){
    return calibrationCandidateCount()>=1&&verifiedCalibrationSourceCount()==0;
  }
}
