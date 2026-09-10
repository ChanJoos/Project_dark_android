package com.projectdark.mobile;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Explicitly non-canonical deterministic rewards used only by [B]/[ADAPTED] runtime fixtures.
 * Entries here must never be promoted into CanonicalMonsterRewardCatalog.
 */
public final class AdaptedPrototypeRewardCatalog {
  public static final String TRAINING_TOKEN_ITEM_ID="IT_B_TRAINING_TOKEN";
  public static final String TRAINING_POLICY_ID="ADAPTED_TRAINING_REWARD_V1";

  public static final class Entry {
    public final String monsterId,itemId,policyId,evidence;
    public final int quantity;

    Entry(String monsterId,String itemId,int quantity,String policyId,String evidence){
      this.monsterId=monsterId;this.itemId=itemId;this.quantity=quantity;
      this.policyId=policyId;this.evidence=evidence;
    }
  }

  private final Map<String,Entry> byMonsterId;

  public AdaptedPrototypeRewardCatalog(){
    Map<String,Entry> entries=new LinkedHashMap<>();
    entries.put("combat_dummy_01",new Entry("combat_dummy_01",TRAINING_TOKEN_ITEM_ID,1,
        TRAINING_POLICY_ID,"B+ADAPTED"));
    byMonsterId=Collections.unmodifiableMap(entries);
  }

  public Entry find(String monsterId){return byMonsterId.get(monsterId);}
  public Map<String,Entry> entries(){return byMonsterId;}
}
