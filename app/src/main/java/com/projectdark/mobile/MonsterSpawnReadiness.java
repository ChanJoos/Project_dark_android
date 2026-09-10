package com.projectdark.mobile;

/**
 * Separates evidence-backed map/monster identity membership from a fully playable runtime spawn.
 * Canonical identity alone must never imply fabricated coordinates, count, HP, or combat tuning.
 */
public final class MonsterSpawnReadiness {
  public enum State { IDENTITY_ONLY, PLAYABLE, UNRESOLVED }

  public final String mapId;
  public final String monsterId;
  public final State state;
  public final String reason;

  public MonsterSpawnReadiness(String mapId,String monsterId,State state,String reason){
    this.mapId=mapId;
    this.monsterId=monsterId;
    this.state=state;
    this.reason=reason;
  }

  public static MonsterSpawnReadiness evaluate(PoteSpawnManifest.Relation relation,PoteMonsterRoster roster){
    if(relation==null||roster==null)return new MonsterSpawnReadiness(null,null,State.UNRESOLVED,"missing relation/roster");
    PoteMonsterRoster.Entry entry=roster.find(relation.monsterId);
    if(entry==null||!entry.hasStableIdentity())return new MonsterSpawnReadiness(relation.mapId,relation.monsterId,State.UNRESOLVED,"missing stable identity");

    // Pote map relations currently prove membership only. Exact coordinates/count and most combat stats are unresolved.
    // Even POTE_SPIRIT's resolved stats would not make a spawn PLAYABLE without an authoritative map relation and placement.
    return new MonsterSpawnReadiness(relation.mapId,relation.monsterId,State.IDENTITY_ONLY,
        "canonical map/monster relation exists; coordinates/count/runtime combat admission remain pending");
  }
}
