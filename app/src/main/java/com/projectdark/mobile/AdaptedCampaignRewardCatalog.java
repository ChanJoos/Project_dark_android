package com.projectdark.mobile;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** Explicit mobile-campaign rewards; never used as canonical Pote source facts. */
public final class AdaptedCampaignRewardCatalog {
  public static final String POLICY_ID="ADAPTED_LV1_40_CAMPAIGN_V1";
  public static final class Reward {
    public final String monsterId,zone;
    public final int exp;
    public final long gold;
    Reward(String id,String zone,int exp,long gold){this.monsterId=id;this.zone=zone;this.exp=exp;this.gold=gold;}
  }
  private final Map<String,Reward> rewards;
  public AdaptedCampaignRewardCatalog(){
    Map<String,Reward> m=new LinkedHashMap<>();
    add(m,"milles_mouse_proto","밀레스 여관",2500,10);
    add(m,"POTE_RED","포테 1구역",5000,12);add(m,"POTE_GREEN","포테 1구역",5500,13);
    add(m,"POTE_PURPLE","포테 2구역",9000,20);add(m,"POTE_SILVER","포테 2구역",11000,24);
    add(m,"POTE_TREANT","포테 2구역",16000,35);add(m,"POTE_ANTLION","포테 2구역",18000,38);
    add(m,"POTE_GNOLL","포테 3구역",20000,42);add(m,"POTE_LYCAN","포테 3구역",22000,46);
    add(m,"POTE_WOLFRIDER","포테 3구역",26000,54);add(m,"POTE_ANTGIANT","포테 3구역",28000,58);
    add(m,"POTE_SILVERWOLF","포테 4구역",36000,74);add(m,"POTE_STRONG_GNOLL","포테 4구역",48000,96);
    add(m,"POTE_STRONG_WOLFRIDER","포테 4구역",52000,104);add(m,"POTE_STRONG_TREANT","포테 4구역",56000,112);
    // POTE_SPIRIT is deliberately absent: its canonical V EXP remains the only rule for that actor.
    rewards=Collections.unmodifiableMap(m);
  }
  private static void add(Map<String,Reward> m,String id,String zone,int exp,long gold){m.put(id,new Reward(id,zone,exp,gold));}
  public Reward find(String id){return rewards.get(id);}
  public Map<String,Reward> entries(){return rewards;}
}
