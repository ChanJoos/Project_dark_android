package com.projectdark.mobile;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Regression audit for the evidence-safe Pote map -> monster identity projection. */
public final class PoteSpawnManifestAudit {
  private PoteSpawnManifestAudit(){}

  public static boolean verify(){
    PoteMonsterRoster roster=new PoteMonsterRoster();
    PoteSpawnManifest manifest=new PoteSpawnManifest();

    if(manifest.relations().size()!=2)return false;
    if(!verifyMap(manifest,roster,"MAP_POTE_01",new String[]{"POTE_PURPLE","POTE_SILVER"}))return false;
    if(!verifyMap(manifest,roster,"MAP_POTE_02",new String[]{"POTE_WOLFRIDER","POTE_ANTGIANT"}))return false;

    // Ambiguous "강력몹" must not be promoted into any concrete strong-variant ID.
    for(PoteSpawnManifest.Relation r:manifest.forMap("MAP_POTE_02")){
      if(r.monsterId.startsWith("POTE_STRONG_"))return false;
    }
    // POTE_SPIRIT is a clear/condition reference in Map_Instance_Master, not part of these exact spawn-rule matches.
    for(java.util.List<PoteSpawnManifest.Relation> list:manifest.relations().values()){
      for(PoteSpawnManifest.Relation r:list)if("POTE_SPIRIT".equals(r.monsterId))return false;
    }
    return true;
  }

  private static boolean verifyMap(PoteSpawnManifest manifest,PoteMonsterRoster roster,String mapId,String[] expected){
    List<PoteSpawnManifest.Relation> relations=manifest.forMap(mapId);
    if(relations.size()!=expected.length)return false;
    Set<String> ids=new HashSet<>();
    for(PoteSpawnManifest.Relation r:relations){
      if(!mapId.equals(r.mapId))return false;
      if(!"V+B".equals(r.evidence))return false;
      if(r.sourceRuleText==null||r.sourceRuleText.isEmpty())return false;
      PoteMonsterRoster.Entry e=roster.find(r.monsterId);
      if(e==null||!e.hasStableIdentity()||!"포테의숲".equals(e.region))return false;
      if(!ids.add(r.monsterId))return false;
    }
    for(String id:expected)if(!ids.contains(id))return false;
    return true;
  }
}
