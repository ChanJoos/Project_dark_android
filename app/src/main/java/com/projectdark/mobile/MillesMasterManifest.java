package com.projectdark.mobile;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Read-only runtime projection of positively supported Milles identifiers from the v4.4 Master CSVs.
 *
 * This class deliberately does NOT translate canonical tile coordinates into the current screenshot-space
 * prototype geometry. Tile/object/collision tracing remains PENDING and must be supplied by a later
 * evidence-backed map decomposition pass.
 *
 * Sources:
 * - master/data/World_Master.csv
 * - master/data/Map_Instance_Master.csv
 * - master/data/Location_Points.csv
 * - master/data/NPC_Master.csv
 * - master/data/Asset_Map_Mapping.csv
 * - master/data/Scene_Object_Mapping.csv
 */
public final class MillesMasterManifest {
  public static final String WORLD_ID="TOWN_MILLES";
  public static final String MAP_ID="MAP_MILLES";
  public static final String CANONICAL_NAME="밀레스";
  public static final String WORLD_STATUS="ORIGINAL";
  public static final String MAP_EVIDENCE="O+B";
  public static final String MAP_ASSET_STATUS="SOURCE_FOUND";
  public static final String TERRAIN_STATUS="READY_FOR_TRACE";
  public static final String GEOMETRY_STATUS="PENDING";
  public static final String VISUAL_STATUS="PENDING_CROP";

  public static final class LocationRecord {
    public final String locationId,type,name,function,status,note;
    public final int x,y;
    LocationRecord(String locationId,String type,String name,int x,int y,String function,String status,String note){
      this.locationId=locationId;this.type=type;this.name=name;this.x=x;this.y=y;
      this.function=function;this.status=status;this.note=note;
    }
  }

  public static final class NpcRecord {
    public final String npcId,name,originalLocation,function,status,note;
    NpcRecord(String npcId,String name,String originalLocation,String function,String status,String note){
      this.npcId=npcId;this.name=name;this.originalLocation=originalLocation;
      this.function=function;this.status=status;this.note=note;
    }
  }

  private static final List<LocationRecord> LOCATIONS=Collections.unmodifiableList(Arrays.asList(
      new LocationRecord("LOC_MILLES_CASTLE","시설","밀레스성",118,50,"밀레스성 입구","ORIGINAL","쉐폰/유론 관련"),
      new LocationRecord("LOC_MILLES_SEA","사냥터","밀레스 해저던전",118,82,"해저던전 입구","ORIGINAL","산호각반/실버아쿠아링/산호석귀걸이/아쿠아글러브 등"),
      new LocationRecord("LOC_MILLES_ALTAR","퀘스트","밀레스 제단",103,11,"킹아크퍼스 후속 절차","ORIGINAL","103,11 또는 104,11"),
      new LocationRecord("LOC_MILLES_ROSEMARY","NPC","로즈마리",81,43,"퀘스트 NPC","ORIGINAL","모디아/아이리스 관련"),
      new LocationRecord("LOC_MILLES_MODIA","NPC","모디아",78,28,"퀘스트 NPC","ORIGINAL","모디아의 사랑"),
      new LocationRecord("LOC_MILLES_TELLIDOAH","NPC","텔리도아",58,105,"독거미 퀘스트","ORIGINAL","")
  ));

  private static final List<NpcRecord> NPCS=Collections.unmodifiableList(Arrays.asList(
      new NpcRecord("NPC_LANSEL","란셀주교","교회","아이리스 이야기/성직자 세계관","ORIGINAL","밀레스 관련 퀘스트 자료"),
      new NpcRecord("NPC_ROSEMARY","로즈마리","81,43","모디아/아이리스 관련","ORIGINAL",""),
      new NpcRecord("NPC_MODIA","모디아","78,28","모디아의 사랑","ORIGINAL",""),
      new NpcRecord("NPC_TELLIDOAH","텔리도아","58,105","독거미 퀘스트","ORIGINAL",""),
      new NpcRecord("NPC_MERLIN_MILLES","멀린","시약점","시약/성수 판매","ORIGINAL","구 Q&A 자료"),
      new NpcRecord("NPC_GILLENOA_MILLES","길레노아","보관소/은행","은행/아이템 보관","ORIGINAL","구 Q&A 자료")
  ));

  public List<LocationRecord> locations(){return LOCATIONS;}
  public List<NpcRecord> npcs(){return NPCS;}

  /**
   * Static contract audit only. It intentionally checks identity/readiness, not unverified geometry.
   */
  public boolean hasCanonicalIdentity(){
    return !WORLD_ID.isEmpty()&&!MAP_ID.isEmpty()&&!CANONICAL_NAME.isEmpty()&&!LOCATIONS.isEmpty()&&!NPCS.isEmpty();
  }
}
