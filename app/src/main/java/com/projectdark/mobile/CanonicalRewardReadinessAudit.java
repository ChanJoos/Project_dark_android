package com.projectdark.mobile;

/**
 * Cross-checks canonical monster/map readiness against the RPG reward catalog.
 * This audit deliberately proves both what is usable and what must remain blocked.
 */
public final class CanonicalRewardReadinessAudit {
  private CanonicalRewardReadinessAudit(){}

  public static boolean verify(){
    PoteSpawnManifest spawns=new PoteSpawnManifest();
    PoteMonsterRoster roster=new PoteMonsterRoster();
    CanonicalMonsterRewardCatalog rewards=new CanonicalMonsterRewardCatalog();

    CanonicalMonsterRewardCatalog.RewardEntry spirit=rewards.find("POTE_SPIRIT");
    if(spirit==null||!spirit.hasResolvedExp()||spirit.exp!=308950)return false;
    if(spirit.hasEmittableDrop())return false;

    // Current Pote map manifest contains identity-only membership for MAP_POTE_01/02.
    // None may be promoted to PLAYABLE without authoritative placement/runtime admission.
    for(java.util.List<PoteSpawnManifest.Relation> relations:spawns.relations().values()){
      for(PoteSpawnManifest.Relation relation:relations){
        MonsterSpawnReadiness readiness=MonsterSpawnReadiness.evaluate(relation,roster);
        if(readiness.state!=MonsterSpawnReadiness.State.IDENTITY_ONLY)return false;
      }
    }

    // POTE_SPIRIT has verified combat stats/reward EXP but no authoritative map relation in the
    // current spawn manifest, so the target runtime must not fabricate a playable spawn or item loot.
    for(java.util.List<PoteSpawnManifest.Relation> relations:spawns.relations().values())
      for(PoteSpawnManifest.Relation relation:relations)if("POTE_SPIRIT".equals(relation.monsterId))return false;

    return rewards.hasNoInventedDropEmission();
  }
}
