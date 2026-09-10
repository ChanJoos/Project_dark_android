package com.projectdark.mobile;

/** Regression guard for the first isolated Pote prototype placement/runtime spawn. */
public final class PotePrototypeWorldAudit {
  private PotePrototypeWorldAudit(){}

  public static boolean verify(){
    PotePrototypeWorldDef.Spawn spawn=PotePrototypeWorldDef.primarySpawn();
    if(spawn==null)return false;
    if(!"MAP_POTE_01".equals(spawn.mapId))return false;
    if(!"POTE_PURPLE".equals(spawn.monsterId))return false;
    if(!"B/ADAPTED".equals(spawn.evidence))return false;
    if(spawn.hp!=PotePrototypeCombatProfile.RUNTIME_HP_B)return false;

    // Canonical relation must exist independently of prototype placement.
    boolean relationFound=false;
    PoteSpawnManifest manifest=new PoteSpawnManifest();
    for(PoteSpawnManifest.Relation r:manifest.forMap(spawn.mapId)){
      if(spawn.monsterId.equals(r.monsterId)){relationFound=true;break;}
    }
    if(!relationFound)return false;

    // Canonical roster values remain unresolved; prototype HP must not leak into Master projection.
    PoteMonsterRoster.Entry canonical=new PoteMonsterRoster().find(spawn.monsterId);
    if(canonical==null||canonical.hp!=null||canonical.exp!=null)return false;

    RuntimeState.Monster runtime=spawn.instantiateRuntimeMonster();
    if(runtime==null)return false;
    if(!spawn.monsterId.equals(runtime.id))return false;
    if(runtime.maxHp!=PotePrototypeCombatProfile.RUNTIME_HP_B||runtime.hp!=runtime.maxHp)return false;
    if(runtime.spawnX!=PotePrototypeWorldDef.MONSTER_X_B||runtime.spawnY!=PotePrototypeWorldDef.MONSTER_Y_B)return false;
    if(runtime.state!=RuntimeState.Monster.State.IDLE||!runtime.alive)return false;

    return PotePrototypeWorldDef.MONSTER_COUNT_B==1;
  }
}
