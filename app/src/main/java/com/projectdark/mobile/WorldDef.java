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
 * [O] visualSourceUrl points to an official-hosted Nexon screenshot used only as the current visual reference/background.
 * [B] bounds, collision rectangles and entity fixture coordinates are reconstruction geometry for runtime validation.
 * PENDING_CROP means no authenticated redistributable tile/object/sprite mapping has been attached yet.
 *
 * The screenshot is reference/prototype presentation only. Runtime world structure is exposed as explicit
 * TILE / OBJECT / COLLISION / NPC / MONSTER_SPAWN / PORTAL layers so later Master-backed data can replace
 * fixtures without turning the screenshot into a monolithic final map texture.
 */
public final class WorldDef {
  public static final String ID="milles_runtime_proto";
  public static final String EVIDENCE_VISUAL="O";
  public static final String EVIDENCE_GEOMETRY="B";
  public static final String ASSET_STATUS="PENDING_CROP";
  public static final String VISUAL_SOURCE_URL="https://storage.nexon.com/dsk03/13/NX_FILE/Board/196608/05/2/000/00/69/5557538701493472772.png";

  public static final float MIN_X=180f,MAX_X=760f,MIN_Y=120f,MAX_Y=455f;
  public static final float PLAYER_SPAWN_X=480f,PLAYER_SPAWN_Y=300f;

  /** Stable world-layer vocabulary required by DATA_CONTRACT. */
  public enum LayerKind { TILE, OBJECT, COLLISION, NPC, MONSTER_SPAWN, PORTAL }

  /**
   * Describes provenance/readiness for a world layer without fabricating missing canonical content.
   * evidence uses the project evidence vocabulary; status remains PENDING/PENDING_CROP where source data is absent.
   */
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

  /** Portal contract only; no canonical Milles portal is fabricated until Master-backed placement is wired. */
  public static final class PortalSpawn {
    public final String portalId,targetMapId,evidence;
    public final float x,y;
    PortalSpawn(String portalId,String targetMapId,float x,float y,String evidence){this.portalId=portalId;this.targetMapId=targetMapId;this.x=x;this.y=y;this.evidence=evidence;}
  }

  /** Object contract only; authenticated object decomposition remains PENDING_CROP. */
  public static final class WorldObject {
    public final String objectId,visualAssetRef,evidence;
    public final float x,y;
    WorldObject(String objectId,String visualAssetRef,float x,float y,String evidence){this.objectId=objectId;this.visualAssetRef=visualAssetRef;this.x=x;this.y=y;this.evidence=evidence;}
  }

  private final List<RectF> blockers;
  private final List<NpcSpawn> npcSpawns;
  private final List<MonsterSpawn> monsterSpawns;
  private final List<PortalSpawn> portalSpawns;
  private final List<WorldObject> objects;
  private final Map<LayerKind,LayerStatus> layerStatuses;

  public WorldDef(){
    List<RectF> b=new ArrayList<>();
    b.add(new RectF(272f,170f,342f,238f));
    b.add(new RectF(600f,150f,684f,218f));
    b.add(new RectF(312f,350f,374f,430f));
    b.add(new RectF(620f,332f,700f,420f));
    blockers=Collections.unmodifiableList(b);

    List<NpcSpawn> n=new ArrayList<>();
    n.add(new NpcSpawn("milles_guide_proto","밀레스 안내인 [B]",555f,248f,
        "밀레스에 온 것을 환영합니다. 이 대화는 모바일 접근/대화 루프 검증용 프로토타입입니다.",ASSET_STATUS));
    npcSpawns=Collections.unmodifiableList(n);

    List<MonsterSpawn> m=new ArrayList<>();
    m.add(new MonsterSpawn("combat_dummy_01","훈련용 몬스터 [B]",430f,205f,60,ASSET_STATUS));
    monsterSpawns=Collections.unmodifiableList(m);

    // Intentionally empty: no canonical portal/object placement is invented from the screenshot.
    portalSpawns=Collections.emptyList();
    objects=Collections.emptyList();

    EnumMap<LayerKind,LayerStatus> ls=new EnumMap<>(LayerKind.class);
    ls.put(LayerKind.TILE,new LayerStatus(LayerKind.TILE,"PENDING","PENDING_CROP"));
    ls.put(LayerKind.OBJECT,new LayerStatus(LayerKind.OBJECT,"PENDING","PENDING_CROP"));
    ls.put(LayerKind.COLLISION,new LayerStatus(LayerKind.COLLISION,EVIDENCE_GEOMETRY,"PROTOTYPE"));
    ls.put(LayerKind.NPC,new LayerStatus(LayerKind.NPC,EVIDENCE_GEOMETRY,"PROTOTYPE/PENDING_CROP"));
    ls.put(LayerKind.MONSTER_SPAWN,new LayerStatus(LayerKind.MONSTER_SPAWN,EVIDENCE_GEOMETRY,"PROTOTYPE/PENDING_CROP"));
    ls.put(LayerKind.PORTAL,new LayerStatus(LayerKind.PORTAL,"PENDING","PENDING"));
    layerStatuses=Collections.unmodifiableMap(ls);
  }

  public List<RectF> blockers(){return blockers;}
  public List<NpcSpawn> npcSpawns(){return npcSpawns;}
  public List<MonsterSpawn> monsterSpawns(){return monsterSpawns;}
  public List<PortalSpawn> portalSpawns(){return portalSpawns;}
  public List<WorldObject> objects(){return objects;}
  public Map<LayerKind,LayerStatus> layerStatuses(){return layerStatuses;}

  /** Static/runtime audit hook: every required layer kind must remain explicitly represented. */
  public boolean hasCompleteLayerContract(){
    for(LayerKind kind:LayerKind.values())if(!layerStatuses.containsKey(kind))return false;
    return true;
  }
}