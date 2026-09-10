package com.projectdark.mobile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Evidence-safe map -> monster identity relations projected from master/data/Map_Instance_Master.csv.
 *
 * This manifest intentionally stops at roster membership. It does not invent coordinates, counts,
 * respawn timing, HP, damage, or drop probability/quantity.
 */
public final class PoteSpawnManifest {
  public static final class Relation {
    public final String mapId;
    public final String monsterId;
    public final String sourceRuleText;
    public final String evidence;

    Relation(String mapId,String monsterId,String sourceRuleText,String evidence){
      this.mapId=mapId;
      this.monsterId=monsterId;
      this.sourceRuleText=sourceRuleText;
      this.evidence=evidence;
    }
  }

  private final Map<String,List<Relation>> byMap;

  public PoteSpawnManifest(){
    Map<String,List<Relation>> m=new LinkedHashMap<>();
    // Map_Instance_Master.csv: MAP_POTE_01 Spawn/Room Rule = "퍼플/실버팜팻 중심" (V+B).
    add(m,"MAP_POTE_01","POTE_PURPLE","퍼플/실버팜팻 중심","V+B");
    add(m,"MAP_POTE_01","POTE_SILVER","퍼플/실버팜팻 중심","V+B");

    // Map_Instance_Master.csv: MAP_POTE_02 Spawn/Room Rule = "울프라이더/앤트자이언트/강력몹" (V+B).
    // Only the two exact-name matches are projected. "강력몹" is intentionally left unresolved because
    // the row does not identify which strong-variant Monster_ID(s) are meant.
    add(m,"MAP_POTE_02","POTE_WOLFRIDER","울프라이더/앤트자이언트/강력몹","V+B");
    add(m,"MAP_POTE_02","POTE_ANTGIANT","울프라이더/앤트자이언트/강력몹","V+B");

    Map<String,List<Relation>> frozen=new LinkedHashMap<>();
    for(Map.Entry<String,List<Relation>> e:m.entrySet())frozen.put(e.getKey(),Collections.unmodifiableList(e.getValue()));
    byMap=Collections.unmodifiableMap(frozen);
  }

  private static void add(Map<String,List<Relation>> m,String mapId,String monsterId,String rule,String evidence){
    List<Relation> list=m.get(mapId);
    if(list==null){list=new ArrayList<>();m.put(mapId,list);}
    list.add(new Relation(mapId,monsterId,rule,evidence));
  }

  public List<Relation> forMap(String mapId){
    List<Relation> list=byMap.get(mapId);
    return list==null?Collections.<Relation>emptyList():list;
  }

  public Map<String,List<Relation>> relations(){return byMap;}
}
