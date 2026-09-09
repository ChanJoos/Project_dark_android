package com.projectdark.mobile;

import android.graphics.RectF;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Evidence-aware world manifest for the current PROJECT DARK runtime.
 *
 * [O] visualSourceUrl points to an official-hosted Nexon screenshot used only as the current visual reference/background.
 * [B] bounds, collision rectangles and entity fixture coordinates are reconstruction geometry for runtime validation.
 * PENDING_CROP means no authenticated redistributable tile/object/sprite mapping has been attached yet.
 */
public final class WorldDef {
  public static final String ID="milles_runtime_proto";
  public static final String EVIDENCE_VISUAL="O";
  public static final String EVIDENCE_GEOMETRY="B";
  public static final String ASSET_STATUS="PENDING_CROP";
  public static final String VISUAL_SOURCE_URL="https://storage.nexon.com/dsk03/13/NX_FILE/Board/196608/05/2/000/00/69/5557538701493472772.png";

  public static final float MIN_X=180f,MAX_X=760f,MIN_Y=120f,MAX_Y=455f;
  public static final float PLAYER_SPAWN_X=480f,PLAYER_SPAWN_Y=300f;

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

  private final List<RectF> blockers;
  private final List<NpcSpawn> npcSpawns;
  private final List<MonsterSpawn> monsterSpawns;

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
  }

  public List<RectF> blockers(){return blockers;}
  public List<NpcSpawn> npcSpawns(){return npcSpawns;}
  public List<MonsterSpawn> monsterSpawns(){return monsterSpawns;}
}
