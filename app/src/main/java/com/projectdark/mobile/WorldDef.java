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

  // B4 device correction: keep meaningful traversable space west of the reagent shop so the shop is not pinned to the map edge.
  public static final float MIN_X=-512f,MAX_X=2304f,MIN_Y=48f,MAX_Y=1600f;
  public static final float PLAYER_SPAWN_X=620f,PLAYER_SPAWN_Y=560f;

  public enum LayerKind { TILE, OBJECT, COLLISION, NPC, MONSTER_SPAWN, PORTAL }
  public static final class LayerStatus { public final LayerKind kind; public final String evidence,status; LayerStatus(LayerKind k,String e,String s){kind=k;evidence=e;status=s;} }
  public static final class NpcSpawn { public final String id,name,dialogue,assetStatus; public final float x,y; NpcSpawn(String i,String n,float px,float py,String d,String a){id=i;name=NpcIdentity.forId(i).label();x=px;y=py;dialogue=NpcIdentity.text(d);assetStatus=a;} }
  public static final class MonsterSpawn { public final String id,name,assetStatus; public final float x,y; public final int hp; MonsterSpawn(String i,String n,float px,float py,int h,String a){id=i;name=n;x=px;y=py;hp=h;assetStatus=a;} }
  public static final class PortalSpawn { public final String portalId,targetMapId,evidence,status; public final float x,y,radius; PortalSpawn(String i,String t,float px,float py,float r,String e,String s){portalId=i;targetMapId=t;x=px;y=py;radius=r;evidence=e;status=s;} }
  public static final class WorldObject { public final String objectId,visualAssetRef,evidence,status; public final float x,y; WorldObject(String i,String v,float px,float py,String e,String s){objectId=i;visualAssetRef=v;x=px;y=py;evidence=e;status=s;} }

  private final List<RectF> blockers; private final List<NpcSpawn> npcSpawns; private final List<MonsterSpawn> monsterSpawns; private final List<PortalSpawn> portalSpawns; private final List<WorldObject> objects; private final Map<LayerKind,LayerStatus> layerStatuses;
  private final MillesMasterManifest masterManifest=new MillesMasterManifest(); private final MillesTraceContract traceContract=new MillesTraceContract(masterManifest);

  public WorldDef(){
    if(!MillesProductionCollisionAudit.verify())throw new IllegalStateException("Milles production collision audit failed");
    List<RectF> b=new ArrayList<>();
    for(MillesProductionCollision.Footprint f:MillesProductionCollision.blockers()){
      com.projectdark.mobile.world.MillesDoorAnchors.Door door=null;for(com.projectdark.mobile.world.MillesDoorAnchors.Door d:com.projectdark.mobile.world.MillesDoorAnchors.ALL)if(d.building.equals(f.id))door=d;
      if(door==null)b.add(new RectF(f.left,f.top,f.right,f.bottom));
      else{float half=40,top=door.y-28;b.add(new RectF(f.left,f.top,f.right,top));b.add(new RectF(f.left,top,door.x-half,f.bottom));b.add(new RectF(door.x+half+32,top,f.right,f.bottom));}
    }
    blockers=Collections.unmodifiableList(b);

    List<NpcSpawn> n=new ArrayList<>();
    n.add(new NpcSpawn("milles_guide_proto","제임스 · 마을 경비 [ADAPTED]",633.0f,599.0f,"여관 식료품 창고에 생쥐가 나타났어. 메리에게 사정을 듣고 와 주겠나?",ASSET_STATUS));
    // Field travel belongs at the player's first readable decision point, not at a remote map edge.
    // One canonical diagonal step from the initial spawn keeps the NPC visible without blocking spawn occupancy.
    // Keep outdoor NPCs one isometric step northwest of their prior anchors.
    n.add(new NpcSpawn("pote_travel_guide","포테의 숲 이동 안내인",620.0f,560.0f,"포테의 숲으로 이동하시겠습니까?",ASSET_STATUS));
    n.add(new NpcSpawn("milles_job_counselor","마이클 · 직업 상담관",930.0f,630.0f,"기본 수련을 마쳤다면 다섯 직업의 전투 방식과 입문 장비를 안내해 주겠네.",ASSET_STATUS));
    String[] mentors={"mentor_warrior","mentor_rogue","mentor_mage","mentor_cleric","mentor_monk"};
    for(int i=0;i<mentors.length;i++)n.add(new NpcSpawn(mentors[i],"직업 지도자",1056f+i*64f,592f,"직업 기술 실습과 장비 점검을 도와줍니다.",ASSET_STATUS));
    n.add(new NpcSpawn("milles_gate_proto","한스 · 숲길 경비 [ADAPTED]",758.0f,1434.0f,"남쪽 숲길에 팜팻 흔적이 보여. 준비가 되면 내게 말해.",ASSET_STATUS));
    n.add(new NpcSpawn("milles_west_proto","서부 지점 [B]",253.0f,689.0f,"서쪽 길을 따라가면 장비상점과 주민들의 집이 나옵니다.",ASSET_STATUS));
    n.add(new NpcSpawn("milles_market_proto","동부 시장 안내 지점 [B]",1868.0f,804.0f,"시장에서는 필요한 물건을 미리 챙기세요. 강을 건널 때는 다리를 이용하시고요.",ASSET_STATUS));
    npcSpawns=Collections.unmodifiableList(n);

    List<MonsterSpawn> m=new ArrayList<>();
    m.add(new MonsterSpawn("combat_dummy_01","훈련 몬스터 A [B]",1315f,715f,60,ASSET_STATUS));
    m.add(new MonsterSpawn("combat_dummy_02","훈련 몬스터 B [B]",1450f,790f,60,ASSET_STATUS));
    m.add(new MonsterSpawn("combat_dummy_03","훈련 몬스터 C [B]",1575f,720f,60,ASSET_STATUS));
    monsterSpawns=Collections.unmodifiableList(m);

    List<PortalSpawn> p=new ArrayList<>();
    // Building exteriors stay fully solid. Entry is exclusively through front-door portal triggers.
    // Door threshold itself is the portal. Keep it wider than one player radius so diagonal tile stepping cannot skip the trigger.
    p.add(new PortalSpawn("potion_shop_door","milles_interior_potion_shop",com.projectdark.mobile.world.MillesDoorAnchors.forId("potion_shop_door").x,com.projectdark.mobile.world.MillesDoorAnchors.forId("potion_shop_door").y,18f,EVIDENCE_GEOMETRY,"B_BUILDING_ENTRY_PORTAL_ACTIVE"));
    p.add(new PortalSpawn("weapon_shop_door","milles_interior_weapon_shop",com.projectdark.mobile.world.MillesDoorAnchors.forId("weapon_shop_door").x,com.projectdark.mobile.world.MillesDoorAnchors.forId("weapon_shop_door").y,18f,EVIDENCE_GEOMETRY,"B_BUILDING_ENTRY_PORTAL"));
    p.add(new PortalSpawn("general_shop_door","milles_interior_bank",com.projectdark.mobile.world.MillesDoorAnchors.forId("general_shop_door").x,com.projectdark.mobile.world.MillesDoorAnchors.forId("general_shop_door").y,18f,EVIDENCE_GEOMETRY,"B_BUILDING_ENTRY_PORTAL"));
    p.add(new PortalSpawn("church_door","milles_interior_church",com.projectdark.mobile.world.MillesDoorAnchors.forId("church_door").x,com.projectdark.mobile.world.MillesDoorAnchors.forId("church_door").y,18f,EVIDENCE_GEOMETRY,"B_BUILDING_ENTRY_PORTAL"));
    for(com.projectdark.mobile.world.MillesDoorAnchors.Door door:com.projectdark.mobile.world.MillesDoorAnchors.ALL){
      p.add(new PortalSpawn(door.id+"_2",com.projectdark.mobile.world.TownInteriorDef.forDoor(door.id).mapId,door.x+32,door.y+16,18f,EVIDENCE_GEOMETRY,"ADAPTED_TWO_TILE_DOOR"));
    }
    p.add(new PortalSpawn("inn_door","milles_interior_inn",2016f,736f,18f,EVIDENCE_GEOMETRY,"B_BUILDING_ENTRY_PORTAL"));
    p.add(new PortalSpawn("milles_south_exit_proto","MAP_POTE_01",790f,1565f,40f,EVIDENCE_GEOMETRY,"ADAPTED_FIELD_ENTRY_ACTIVE"));
    portalSpawns=Collections.unmodifiableList(p);

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
