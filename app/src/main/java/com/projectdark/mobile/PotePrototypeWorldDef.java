package com.projectdark.mobile;

/**
 * Explicit non-canonical placement shell for the first playable Pote combat slice.
 *
 * Canonical facts retained:
 * - map identity: MAP_POTE_01
 * - monster identity/map membership: POTE_PURPLE belongs to MAP_POTE_01
 *
 * Prototype/adapted facts below are NOT original-game claims. Coordinates and count exist only so the
 * runtime can exercise a real canonical Monster_ID without contaminating Master data.
 */
public final class PotePrototypeWorldDef {
  public static final String MAP_ID="MAP_POTE_01";
  public static final String EVIDENCE_PLACEMENT="B/ADAPTED";
  public static final String STATUS="PROTOTYPE_PLACEMENT";

  // Prototype screen-space placement only; not original tile coordinates.
  public static final float PLAYER_X_B=480f;
  public static final float PLAYER_Y_B=320f;
  public static final float MONSTER_X_B=480f;
  public static final float MONSTER_Y_B=220f;
  public static final int MONSTER_COUNT_B=1;

  public static final class Spawn {
    public final String mapId,monsterId,name,evidence;
    public final float x,y;
    public final int hp;

    Spawn(String mapId,String monsterId,String name,float x,float y,int hp,String evidence){
      this.mapId=mapId;
      this.monsterId=monsterId;
      this.name=name;
      this.x=x;
      this.y=y;
      this.hp=hp;
      this.evidence=evidence;
    }

    /** Produces the same runtime monster type used by the current combat engine. */
    public RuntimeState.Monster instantiateRuntimeMonster(){
      return new RuntimeState.Monster(monsterId,name,x,y,hp,"PENDING_CROP");
    }
  }

  private PotePrototypeWorldDef(){}

  public static Spawn primarySpawn(){
    return new Spawn(
        MAP_ID,
        PotePrototypeCombatProfile.MONSTER_ID,
        "퍼플팜팻 [B placement]",
        MONSTER_X_B,
        MONSTER_Y_B,
        PotePrototypeCombatProfile.RUNTIME_HP_B,
        EVIDENCE_PLACEMENT);
  }
}
