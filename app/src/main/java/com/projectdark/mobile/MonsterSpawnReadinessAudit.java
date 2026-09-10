package com.projectdark.mobile;

/** Regression guard for canonical identity-only vs playable monster spawns. */
public final class MonsterSpawnReadinessAudit {
  private MonsterSpawnReadinessAudit(){}

  public static boolean verify(){
    PoteSpawnManifest manifest=new PoteSpawnManifest();
    PoteMonsterRoster roster=new PoteMonsterRoster();

    int relations=0;
    for(java.util.List<PoteSpawnManifest.Relation> list:manifest.relations().values()){
      for(PoteSpawnManifest.Relation relation:list){
        relations++;
        MonsterSpawnReadiness readiness=MonsterSpawnReadiness.evaluate(relation,roster);
        if(readiness.state!=MonsterSpawnReadiness.State.IDENTITY_ONLY)return false;
        if(readiness.mapId==null||readiness.monsterId==null)return false;
      }
    }

    if(relations!=4)return false;

    // No current Pote relation may be promoted to a playable runtime spawn until placement/count and
    // combat admission inputs are evidenced. In particular, unresolved common-monster HP cannot be fabricated.
    for(PoteSpawnManifest.Relation r:manifest.forMap("MAP_POTE_01")){
      PoteMonsterRoster.Entry e=roster.find(r.monsterId);
      if(e==null||e.hp!=null||e.exp!=null)return false;
    }
    for(PoteSpawnManifest.Relation r:manifest.forMap("MAP_POTE_02")){
      PoteMonsterRoster.Entry e=roster.find(r.monsterId);
      if(e==null||e.hp!=null||e.exp!=null)return false;
    }
    return true;
  }
}
