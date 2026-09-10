package com.projectdark.mobile;

import android.graphics.RectF;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Evidence-aware world manifest for the current PROJECT DARK runtime.
 *
 * [O] visualSourceUrl points to an official-hosted Nexon screenshot used only as visual evidence.
 * [ADAPTED] / [B] village bounds, collision geometry and fixture coordinates are prototype spatial
 * structure so the starting slice can be explored for tens of seconds while original Milles geometry
 * remains unverified.  They must stay data-driven and replaceable by traced source-backed layers.
 * PENDING_CROP means no authenticated redistributable tile/object/sprite mapping has been attached yet.
 */
public final class WorldDef {
  public static final String ID="milles_runtime_proto";
  public static final String EVIDENCE_VISUAL="O";
  public static final String EVIDENCE_GEOMETRY="ADAPTED/B";
  public static final String GEOMETRY_STATUS="ADAPTED_PROTOTYPE_VILLAGE";
  public static final String ASSET_STATUS="PENDING_CROP";
  public static final String VISUAL_SOURCE_URL="https://storage.nexon.com/dsk03/13/NX_FILE/Board/196608/05/2/000/00/69/5557538701493472772.png";

  // [ADAPTED/B] Expanded prototype footprint. Logical coordinates are intentionally renderer-independent.
  public static final float MIN_X=96f,MAX_X=1184f,MIN_Y=64f,MAX_Y=864f;
  public static final float PLAYER_SPAWN_X=500f,PLAYER_SPAWN_Y=470f;

  /** Stable world-layer vocabulary required by DATA_CONTRACT. */
  public enum LayerKind { TILE, OBJECT, COLLISION, NPC, MONSTER_SPAWN, PORTAL }

  public static final class LayerStatus {
    public final LayerKind kind;
    public final String evidence,status;
    LayerStatus(LayerKind kind,String evidence,String status){this.kind=kind;this.evidence=evidence;this.status=status;}
  }

  public static final class NpcSpawn {
    public final String id,name,dialogue,assetStatus;
    public final float x,y;
    NpcSpawn(String id,String name,float x,float y,String dialogue,String assetStatus){this.id=id;this.name=name;this.x=x;this.y=y;this.dialogue=dialogue;this.assetStatus=assetStatus;}
  }

  public static final class MonsterSpawn {
    public final String id,name,assetStatus;
    public final float x,y;
    public final int hp;
    MonsterSpawn(String id,String name,float x,float y,int hp,String assetStatus){this.id=id;this.name=name;this.x=x;this.y=y;this.hp=hp;this.assetStatus=assetStatus;}
  }

  /** Portal geometry may exist as an adapted prototype without asserting an original destination. */
  public static final class PortalSpawn {
    public final String portalId,targetMapId,evidence,status;
    public final float x,y,radius;
    PortalSpawn(String portalId,String targetMapId,float x,float y,float radius,String evidence,String status){
      this.portalId=portalId;this.targetMapId=targetMapId;this.x=x;this.y=y;this.radius=radius;this.evidence=evidence;this.status=status;
    }
  }

  /** Spatial landmark/object anchor. Visual decomposition remains PENDING_CROP. */
  public static final class WorldObject {
    public final String objectId,visualAssetRef,evidence,status;
    public final float x,y;
    WorldObject(String objectId,String visualAssetRef,float x,float y,String evidence,String status){
      this.objectId=objectId;this.visualAssetRef=visualAssetRef;this.x=x;this.y=y;this.evidence=evidence;this.status=status;
    }
  }

  private final List<RectF> blockers;
  private final List<NpcSpawn> npcSpawns;
  private final List<MonsterSpawn> monsterSpawns;
  private final List<PortalSpawn> portalSpawns;
  private final List<WorldObject> objects;
  private final Map<LayerKind,LayerStatus> layerStatuses;
  private final MillesMasterManifest masterManifest=new MillesMasterManifest();
  private final MillesTraceContract traceContract=new MillesTraceContract(masterManifest);

  public WorldDef(){
    List<RectF> b=new ArrayList<>();
    // [ADAPTED/B] North building row leaves a broad east-west road below it.
    b.add(new RectF(180f,120f,360f,255f));
    b.add(new RectF(455f,105f,640f,235f));
    b.add(new RectF(780f,125f,1000f,270f));
    // [ADAPTED/B] West/east structures frame the central plaza without sealing it.
    b.add(new RectF(145f,365f,315f,545f));
    b.add(new RectF(900f,350f,1080f,540f));
    // [ADAPTED/B] South structures create two lower lanes and a central exit route.
    b.add(new RectF(255f,665f,450f,815f));
    b.add(new RectF(735f,670f,955f,820f));
    // Small landmark blockers make the plaza navigational rather than an empty rectangle.
    b.add(new RectF(565f,390f,625f,450f));
    b.add(new RectF(650f,520f,710f,575f));
    blockers=Collections.unmodifiableList(b);

    List<NpcSpawn> n=new ArrayList<>();
    n.add(new NpcSpawn("milles_guide_proto","밀레스 안내인 [B]",525f,515f,
        "밀레스 탐색/대화 루프 검증용 프로토타입 NPC입니다.",ASSET_STATUS));
    n.add(new NpcSpawn("milles_service_proto","마을 서비스 지점 [B]",830f,315f,
        "서비스 동선 검증용 프로토타입 NPC입니다.",ASSET_STATUS));
    n.add(new NpcSpawn("milles_gate_proto","남문 안내 지점 [B]",590f,760f,
        "출입구 접근 동선 검증용 프로토타입 NPC입니다.",ASSET_STATUS));
    npcSpawns=Collections.unmodifiableList(n);

    List<MonsterSpawn> m=new ArrayList<>();
    // Kept away from the central plaza so traversal can be judged independently from combat pressure.
    m.add(new MonsterSpawn("combat_dummy_01","훈련용 몬스터 [B]",1040f,650f,60,ASSET_STATUS));
    monsterSpawns=Collections.unmodifiableList(m);

    List<PortalSpawn> p=new ArrayList<>();
    p.add(new PortalSpawn("milles_south_exit_proto","PENDING_TARGET_MAP",590f,842f,22f,EVIDENCE_GEOMETRY,"PROTOTYPE_DISABLED_TARGET_PENDING"));
    portalSpawns=Collections.unmodifiableList(p);

    List<WorldObject> o=new ArrayList<>();
    o.add(new WorldObject("central_plaza_anchor",ASSET_STATUS,590f,485f,EVIDENCE_GEOMETRY,GEOMETRY_STATUS));
    o.add(new WorldObject("north_road_anchor",ASSET_STATUS,590f,300f,EVIDENCE_GEOMETRY,GEOMETRY_STATUS));
    o.add(new WorldObject("south_gate_anchor",ASSET_STATUS,590f,760f,EVIDENCE_GEOMETRY,GEOMETRY_STATUS));
    objects=Collections.unmodifiableList(o);

    EnumMap<LayerKind,LayerStatus> ls=new EnumMap<>(LayerKind.class);
    ls.put(LayerKind.TILE,new LayerStatus(LayerKind.TILE,"PENDING","PENDING_CROP/ADAPTED_GEOMETRY_ONLY"));
    ls.put(LayerKind.OBJECT,new LayerStatus(LayerKind.OBJECT,EVIDENCE_GEOMETRY,"PROTOTYPE_ANCHORS/PENDING_CROP"));
    ls.put(LayerKind.COLLISION,new LayerStatus(LayerKind.COLLISION,EVIDENCE_GEOMETRY,GEOMETRY_STATUS));
    ls.put(LayerKind.NPC,new LayerStatus(LayerKind.NPC,EVIDENCE_GEOMETRY,"PROTOTYPE/PENDING_CROP"));
    ls.put(LayerKind.MONSTER_SPAWN,new LayerStatus(LayerKind.MONSTER_SPAWN,EVIDENCE_GEOMETRY,"PROTOTYPE/PENDING_CROP"));
    ls.put(LayerKind.PORTAL,new LayerStatus(LayerKind.PORTAL,EVIDENCE_GEOMETRY,"PROTOTYPE_TARGET_PENDING"));
    layerStatuses=Collections.unmodifiableMap(ls);
  }

  public List<RectF> blockers(){return blockers;}
  public List<NpcSpawn> npcSpawns(){return npcSpawns;}
  public List<MonsterSpawn> monsterSpawns(){return monsterSpawns;}
  public List<PortalSpawn> portalSpawns(){return portalSpawns;}
  public List<WorldObject> objects(){return objects;}
  public Map<LayerKind,LayerStatus> layerStatuses(){return layerStatuses;}
  public MillesMasterManifest masterManifest(){return masterManifest;}
  public MillesTraceContract traceContract(){return traceContract;}

  public boolean hasCompleteLayerContract(){
    for(LayerKind kind:LayerKind.values())if(!layerStatuses.containsKey(kind))return false;
    return true;
  }

  public boolean hasMasterBackedMillesIdentity(){return masterManifest.hasCanonicalIdentity();}
  public boolean hasSafeMillesTraceContract(){return traceContract.passesAudit();}
}
