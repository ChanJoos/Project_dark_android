package com.projectdark.mobile;

/**
 * Evidence guard for the current prototype world monster layer.
 * Prevents unresolved/canonical monsters from being silently inserted into the Milles prototype fixture.
 */
public final class MonsterSpawnAdmissionAudit {
  private MonsterSpawnAdmissionAudit(){}

  public static boolean verify(WorldDef world){
    if(world==null)return false;
    if(!PoteMonsterRosterAudit.verify())return false;
    if(!PoteSpawnManifestAudit.verify())return false;
    MonsterDefinitionRegistry definitions=new MonsterDefinitionRegistry();
    for(WorldDef.MonsterSpawn spawn:world.monsterSpawns()){
      MonsterDefinition def=definitions.resolve(spawn.id);
      if(def.status==MonsterDefinition.Status.UNRESOLVED)return false;

      // The current world is explicitly a reconstruction prototype, not a canonical region spawn map.
      // Until a Master-backed map->monster spawn relation exists, only explicit prototype fixtures are admitted.
      if(WorldDef.ID.equals("milles_runtime_proto")&&def.status!=MonsterDefinition.Status.PROTOTYPE_PENDING)return false;

      // Prototype fixture identity must remain internally consistent and must not acquire canonical reward facts.
      if(def.status==MonsterDefinition.Status.PROTOTYPE_PENDING){
        if(def.hasCanonicalReward())return false;
        if(def.evidence!=MonsterDefinition.Evidence.B)return false;
      }
    }
    return true;
  }
}
