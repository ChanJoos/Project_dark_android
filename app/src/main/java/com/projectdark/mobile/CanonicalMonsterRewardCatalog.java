package com.projectdark.mobile;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Read-only RPG projection of reward facts that are positively supported by current Master CSVs.
 *
 * Sources:
 * - master/data/Monster_Master.csv
 * - master/data/Item_Master.csv
 *
 * Important: a listed major-drop relationship is NOT a deterministic drop rule. Probability and
 * quantity stay null until authoritative values exist, so this catalog cannot fabricate world drops.
 */
public final class CanonicalMonsterRewardCatalog {
  public static final class DropHint {
    public final String itemId;
    public final Float probability;
    public final Integer quantity;
    public final RpgProgressionState.Evidence evidence;

    DropHint(String itemId,RpgProgressionState.Evidence evidence){
      this.itemId=itemId;
      this.probability=null;
      this.quantity=null;
      this.evidence=evidence;
    }

    public boolean emissionResolved(){return probability!=null&&quantity!=null;}
  }

  public static final class RewardEntry {
    public final String monsterId;
    public final Integer exp;
    public final RpgProgressionState.Evidence expEvidence;
    public final List<DropHint> dropHints;

    RewardEntry(String monsterId,Integer exp,RpgProgressionState.Evidence expEvidence,List<DropHint> dropHints){
      this.monsterId=monsterId;
      this.exp=exp;
      this.expEvidence=expEvidence;
      this.dropHints=Collections.unmodifiableList(dropHints);
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

    // Monster_Master: POTE_SPIRIT / 포테의정령 / EXP 308950 / Evidence V.
    // Major drop '세줄금반지' maps to Item_Master IT_RING_THREELINEGOLD.
    entries.put("POTE_SPIRIT",new RewardEntry(
        "POTE_SPIRIT",308950,RpgProgressionState.Evidence.V,
        Arrays.asList(new DropHint("IT_RING_THREELINEGOLD",RpgProgressionState.Evidence.V))));

    // Monster_Master: ABEL_BERSERKER / 해안의광폭자 / EXP 606252 / Evidence V.
    // Major drop '실버아쿠아링' maps to Item_Master IT_RING_SILVERAQUA.
    entries.put("ABEL_BERSERKER",new RewardEntry(
        "ABEL_BERSERKER",606252,RpgProgressionState.Evidence.V,
        Arrays.asList(new DropHint("IT_RING_SILVERAQUA",RpgProgressionState.Evidence.V))));

    // Monster_Master: LYK_TYRANT / 해안의폭군 / EXP 780575 / Evidence V.
    // No positively mapped item drop is asserted here.
    entries.put("LYK_TYRANT",new RewardEntry(
        "LYK_TYRANT",780575,RpgProgressionState.Evidence.V,
        Collections.<DropHint>emptyList()));

    byMonsterId=Collections.unmodifiableMap(entries);
  }

  public RewardEntry find(String monsterId){return byMonsterId.get(monsterId);}
  public Map<String,RewardEntry> entries(){return byMonsterId;}

  /**
   * Audit invariant: no catalog entry may silently become a deterministic drop while Master rates
   * and quantities are unresolved.
   */
  public boolean hasNoInventedDropEmission(){
    for(RewardEntry entry:byMonsterId.values())if(entry.hasEmittableDrop())return false;
    return true;
  }
}
