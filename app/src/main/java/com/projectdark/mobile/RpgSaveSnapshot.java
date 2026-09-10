package com.projectdark.mobile;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Versioned RPG-owned persistence DTO. Stores mutable IDs/state only; canonical definitions stay in data catalogs.
 */
public final class RpgSaveSnapshot {
  public static final int CURRENT_SCHEMA_VERSION=1;

  public final int schemaVersion;
  public final RpgProgressionState.ProgressionNode progressionNode;
  public final String currentJobCode;
  public final Integer normalLevel;
  public final Long normalExp;
  public final long gold;
  public final long lastCombatSequence;
  public final Map<String,Integer> inventory;
  public final Map<String,String> equipmentBySlot;
  public final Set<String> learnedActionIds;

  public RpgSaveSnapshot(int schemaVersion,
      RpgProgressionState.ProgressionNode progressionNode,
      String currentJobCode,Integer normalLevel,Long normalExp,long gold,long lastCombatSequence,
      Map<String,Integer> inventory,Map<String,String> equipmentBySlot,Set<String> learnedActionIds){
    this.schemaVersion=schemaVersion;
    this.progressionNode=progressionNode;
    this.currentJobCode=currentJobCode;
    this.normalLevel=normalLevel;
    this.normalExp=normalExp;
    this.gold=gold;
    this.lastCombatSequence=lastCombatSequence;
    this.inventory=Collections.unmodifiableMap(new LinkedHashMap<>(inventory));
    this.equipmentBySlot=Collections.unmodifiableMap(new LinkedHashMap<>(equipmentBySlot));
    this.learnedActionIds=Collections.unmodifiableSet(new LinkedHashSet<>(learnedActionIds));
  }
}
