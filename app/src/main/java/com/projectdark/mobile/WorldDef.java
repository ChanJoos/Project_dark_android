package com.projectdark.mobile;

import android.graphics.RectF;
import com.projectdark.mobile.world.MillesProductionCollision;
import com.projectdark.mobile.world.MillesProductionCollisionAudit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** Evidence-aware runtime map definition. Unverified geometry is explicitly [ADAPTED]/[B]. */
public final class WorldDef {
  public static final String ID="milles_runtime_proto";
  public static final String EVIDENCE_VISUAL="O";
  public static final String EVIDENCE_GEOMETRY="ADAPTED/B";
  public static final String GEOMETRY_STATUS="PRODUCTION_GROUND_FOOTPRINT_COLLISION_V1";
  public static final String ASSET_STATUS="PENDING_CROP";
  public static final String VISUAL_SOURCE_URL="https://storage.nexon.com/dsk03/13/NX_FILE/Board/196608/05/2/000/00/69/5557538701493472772.png";

  public static final float MIN_X=64f,MAX_X=2304f,MIN_Y=48f,MAX_Y=1600f;
  public static final float PLAYER_SPAWN_X=620f,PLAYER_SPAWN_Y=560f;

  public enum LayerKind { TILE, OBJECT, COLLISION, NPC, MONSTER_SPAWN, PORTAL }
  public static final class LayerStatus { public final LayerKind kind; public final String evidence,status; LayerStatus(LayerKind k,String e,String s){kind=k;evidence=e;status=s;} }
  public static final class NpcSpawn { public final String id,name,dialogue,assetStatus; public final float x,y; NpcSpawn(String i,String n,float px,float py,String d,String a){id=i;name=n;x=px;y=py;dialogue=d;assetStatus=a;} }
  public static final class MonsterSpawn { public final String id,name,assetStatus; public final float x,y; public final int hp; MonsterSpawn(String i,String n,float px,float py,int h,String a){id=i;name=n;x=px;y=py;hp=h;assetStatus=a;} }
  public static final class PortalSpawn { public final String portalId,targetMapId,evidence,status; public final float x,y,radius; PortalSpawn(String i,String t,float px,float py,float r,String e,String s){portalId=i;targetMapId=t;x=px;y=py;radius=r;evidence=e;status=s;} }
  public static final class WorldObject { public final String objectId,visualAssetRef,evidence,status; public final float x,y; WorldObject(String i,String v,float px,float py,String e,String s){objectId=i;visualAssetRef=v;x=px;y=py;evidence=e;status=s;} }

  private final List<RectF> blockers; private final List<NpcSpawn> npcSpawns; private final List<MonsterSpawn> monsterSpawns; private final List<PortalSpawn> portalSpawns; private final List<WorldObject> objects; private final Map<LayerKind,LayerStatus> layerStatuses;
  private final MillesMasterManifest masterManifest=new MillesMasterManifest(); private final MillesTraceContract traceContract=new MillesTraceContract(masterManifest);

  public WorldDef(){
    if(!MillesProductionCollisionAudit.verify())throw new IllegalStateException("Milles production collision audit failed");
    List<RectF> b=new ArrayList<>();
    for(MillesProductionCollision.Footprint f:MillesProductionCollision.blockers())b.add(new RectF(f.left,f.top,f.right,f.bottom));
    blockers=Collections.unmodifiableList(b);

    List<NpcSpawn> n=new ArrayList<>();
    n.add(new NpcSpawn("milles_guide_proto","밀레스 안내인 [B]",665f,615f,"밀레스 탐색/대화 루프 검증용 프로토타입 NPC입니다.",ASSET_STATUS));
    n.add(new NpcSpawn("milles_service_proto","마을 서비스 지점 [B]",1120f,365f,"서비스 동선 검증용 프로토타입 NPC입니다.",ASSET_STATUS));
    n.add(new NpcSpawn("milles_gate_proto","남문 안내 지점 [B]",790f,1450f,"확장 남문 접근 동선 검증용 프로토타입 NPC입니다.",ASSET_STATUS));
    n.add(new NpcSpawn("milles_west_proto","서부 지점 [B]",285f,705f,"확장 마을 서부 탐색 동선 검증용 지점입니다.",ASSET_STATUS));
    n.add(new NpcSpawn("milles_market_proto","동부 시장 안내 지점 [B]",1900f,820f,"확장 동부 시장 탐색 동선 검증용 지점입니다.",ASSET_STATUS));
    npcSpawns=Collections.unmodifiableList(n);

    List<MonsterSpawn> m=new ArrayList<>();
    m.add(new MonsterSpawn("combat_dummy_01","훈련용 몬스터 [B]",1315f,715f,60,ASSET_STATUS)); monsterSpawns=Collections.unmodifiableList(m);

    List<PortalSpawn> p=new ArrayList<>();
    p.add(new PortalSpawn("milles_south_exit_proto","PENDING_TARGET_MAP",790f,1565f,22f,EVIDENCE_GEOMETRY,"PROTOTYPE_DISABLED_TARGET_PENDING")); portalSpawns=Collections.unmodifiableList(p);

    List<WorldObject> o=new ArrayList<>();
    o.add(new WorldObject("central_plaza_anchor",ASSET_STATUS,790f,590f,EVIDENCE_GEOMETRY,GEOMETRY_STATUS));
    o.add(new WorldObject("north_road_anchor",ASSET_STATUS,790f,350f,EVIDENCE_GEOMETRY,GEOMETRY_STATUS));
    o.add(new WorldObject("west_district_anchor",ASSET_STATUS,285f,705f,EVIDENCE_GEOMETRY,GEOMETRY_STATUS));
    o.add(new WorldObject("east_district_anchor",ASSET_STATUS,1315f,715f,EVIDENCE_GEOMETRY,GEOMETRY_STATUS));
    o.add(new WorldObject("east_market_anchor",ASSET_STATUS,1900f,760f,EVIDENCE_GEOMETRY,GEOMETRY_STATUS));
    o.add(new WorldObject("south_commons_anchor",ASSET_STATUS,790f,1360f,EVIDENCE_GEOMETRY,GEOMETRY_STATUS));
    o.add(new WorldObject("south_gate_anchor",ASSET_STATUS,790f,1510f,EVIDENCE_GEOMETRY,GEOMETRY_STATUS)); objects=Collections.unmodifiableList(o);

    EnumMap<LayerKind,LayerStatus> ls=new EnumMap<>(LayerKind.class);
    ls.put(LayerKind.TILE,new LayerStatus(LayerKind.TILE,"PENDING","PENDING_CROP/ADAPTED_GEOMETRY_ONLY"));
    ls.put(LayerKind.OBJECT,new LayerStatus(LayerKind.OBJECT,EVIDENCE_GEOMETRY,"PROTOTYPE_ANCHORS/PENDING_CROP"));
    ls.put(LayerKind.COLLISION,new LayerStatus(LayerKind.COLLISION,EVIDENCE_GEOMETRY,GEOMETRY_STATUS));
    ls.put(LayerKind.NPC,new LayerStatus(LayerKind.NPC,EVIDENCE_GEOMETRY,"PROTOTYPE/PENDING_CROP"));
    ls.put(LayerKind.MONSTER_SPAWN,new LayerStatus(LayerKind.MONSTER_SPAWN,EVIDENCE_GEOMETRY,"PROTOTYPE/PENDING_CROP"));
    ls.put(LayerKind.PORTAL,new LayerStatus(LayerKind.PORTAL,EVIDENCE_GEOMETRY,"PROTOTYPE_TARGET_PENDING")); layerStatuses=Collections.unmodifiableMap(ls);
  }
  public List<RectF> blockers(){return blockers;} public List<NpcSpawn> npcSpawns(){return npcSpawns;} public List<MonsterSpawn> monsterSpawns(){return monsterSpawns;} public List<PortalSpawn> portalSpawns(){return portalSpawns;} public List<WorldObject> objects(){return objects;} public Map<LayerKind,LayerStatus> layerStatuses(){return layerStatuses;} public MillesMasterManifest masterManifest(){return masterManifest;} public MillesTraceContract traceContract(){return traceContract;}
  public boolean hasCompleteLayerContract(){for(LayerKind k:LayerKind.values())if(!layerStatuses.containsKey(k))return false;return true;} public boolean hasMasterBackedMillesIdentity(){return masterManifest.hasCanonicalIdentity();} public boolean hasSafeMillesTraceContract(){return traceContract.passesAudit();}
}
