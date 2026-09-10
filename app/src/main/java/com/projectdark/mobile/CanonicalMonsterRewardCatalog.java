package com.projectdark.mobile;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Read-only RPG projection of reward facts plus explicitly isolated QA fixtures.
 *
 * Canonical sources:
 * - master/data/Monster_Master.csv
 * - master/data/Item_Master.csv
 * - design/PLAYTEST_CANON_20260910_1938.md
 *
 * Canonical monster drop hints stay unresolved unless probability/quantity are evidenced.
 * The combat_dummy_01 entry is an explicitly labeled [B]/[ADAPTED] device-test fixture only.
 */
public final class CanonicalMonsterRewardCatalog {
  public static final class DropHint {
    public final String itemId;
    public final Float probability;
    public final Integer quantity;
    public final RpgProgressionState.Evidence evidence;
    public final boolean qaFixture;
    public final String sourceLabel;

    DropHint(String itemId,RpgProgressionState.Evidence evidence){
      this(itemId,null,null,evidence,false,"CANONICAL_UNRESOLVED");
    }

    DropHint(String itemId,Float probability,Integer quantity,RpgProgressionState.Evidence evidence,
        boolean qaFixture,String sourceLabel){
      this.itemId=itemId;this.probability=probability;this.quantity=quantity;this.evidence=evidence;
      this.qaFixture=qaFixture;this.sourceLabel=sourceLabel;
    }

    public boolean emissionResolved(){return probability!=null&&quantity!=null;}
  }

  public static final class RewardEntry {
    public final String monsterId;
    public final Integer exp;
    public final RpgProgressionState.Evidence expEvidence;
    public final List<DropHint> dropHints;
    public final boolean qaFixture;
    public final String sourceLabel;

    RewardEntry(String monsterId,Integer exp,RpgProgressionState.Evidence expEvidence,List<DropHint> dropHints){
      this(monsterId,exp,expEvidence,dropHints,false,"CANONICAL");
    }

    RewardEntry(String monsterId,Integer exp,RpgProgressionState.Evidence expEvidence,List<DropHint> dropHints,
        boolean qaFixture,String sourceLabel){
      this.monsterId=monsterId;this.exp=exp;this.expEvidence=expEvidence;
      this.dropHints=Collections.unmodifiableList(dropHints);this.qaFixture=qaFixture;this.sourceLabel=sourceLabel;
    }

    public boolean hasResolvedExp(){return exp!=null;}
    public boolean hasEmittableDrop(){
      for(DropHint hint:dropHints)if(hint.emissionResolved())return true;
      return false;
    }
  }

  private final Map<String,RewardEntry> byMonsterId;

  public CanonicalMonsterRewardCatalog(){
    Map<String,RewardEntry> entries=new LinkedHashMap<>();

    entries.put("POTE_SPIRIT",new RewardEntry(
        "POTE_SPIRIT",308950,RpgProgressionState.Evidence.V,
        Arrays.asList(new DropHint("IT_RING_THREELINEGOLD",RpgProgressionState.Evidence.V))));

    entries.put("ABEL_BERSERKER",new RewardEntry(
        "ABEL_BERSERKER",606252,RpgProgressionState.Evidence.V,
        Arrays.asList(new DropHint("IT_RING_SILVERAQUA",RpgProgressionState.Evidence.V))));

    entries.put("LYK_TYRANT",new RewardEntry(
        "LYK_TYRANT",780575,RpgProgressionState.Evidence.V,
        Collections.<DropHint>emptyList()));

    // USER-CONFIRMED device-playtest canon 2026-09-10 19:38 KST.
    // This is NOT original LOD drop data. It exists only so the live vertical-slice dummy can verify
    // the direct-inventory pipeline end-to-end on device. Quantity 1 is deterministic by test design.
    entries.put("combat_dummy_01",new RewardEntry(
        "combat_dummy_01",null,RpgProgressionState.Evidence.ADAPTED,
        Arrays.asList(new DropHint("IT_GLOVE_LEATHER",1.0f,1,RpgProgressionState.Evidence.ADAPTED,
            true,"[B]/[ADAPTED] TEST REWARD — PLAYTEST_CANON_20260910_1938")),
        true,"[B]/[ADAPTED] TEST REWARD — PLAYTEST_CANON_20260910_1938"));

    byMonsterId=Collections.unmodifiableMap(entries);
  }

  public RewardEntry find(String monsterId){return byMonsterId.get(monsterId);}
  public Map<String,RewardEntry> entries(){return byMonsterId;}

  /**
   * Invariant: no canonical/original reward hint may silently become deterministic while its rate or
   * quantity is unresolved. Explicit QA fixtures are excluded from this canon audit by construction.
   */
  public boolean hasNoInventedDropEmission(){
    for(RewardEntry entry:byMonsterId.values()){
      if(entry.qaFixture)continue;
      for(DropHint hint:entry.dropHints)if(!hint.qaFixture&&hint.emissionResolved())return false;
    }
    return true;
  }

  public boolean hasIsolatedDummyQaFixture(){
    RewardEntry entry=byMonsterId.get("combat_dummy_01");
    if(entry==null||!entry.qaFixture||entry.exp!=null||entry.dropHints.size()!=1)return false;
    DropHint hint=entry.dropHints.get(0);
    return hint.qaFixture&&"IT_GLOVE_LEATHER".equals(hint.itemId)&&hint.quantity!=null&&hint.quantity==1
        &&hint.probability!=null&&hint.probability==1.0f&&hint.evidence==RpgProgressionState.Evidence.ADAPTED;
  }
}
