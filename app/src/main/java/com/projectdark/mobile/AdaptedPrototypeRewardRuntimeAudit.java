package com.projectdark.mobile;

import java.util.List;

/** End-to-end audit of the live Milles training-monster defeat -> direct inventory path. */
public final class AdaptedPrototypeRewardRuntimeAudit {
  private AdaptedPrototypeRewardRuntimeAudit(){}

  public static boolean verify(){
    RuntimeState runtime=new RuntimeState(RuntimeState.BootMode.MILLES,true);
    RuntimeState.Monster training=find(runtime,"combat_dummy_01");
    if(training==null)return false;

    runtime.damage(training,training.maxHp);
    runtime.tick(0f);
    if(quantity(runtime)!=1||runtime.rpg().rewardHistory().size()!=1)return false;
    RpgProgressionState.RewardResolution first=latest(runtime);
    if(first.status!=RpgProgressionState.RewardStatus.RESOLVED)return false;
    if(first.source!=RpgProgressionState.RewardSource.ADAPTED_TEST)return false;
    if(!AdaptedPrototypeRewardCatalog.TRAINING_POLICY_ID.equals(first.policyId))return false;
    if(!"B+ADAPTED".equals(first.evidence))return false;
    if(value(first.autoLootedItems,AdaptedPrototypeRewardCatalog.TRAINING_TOKEN_ITEM_ID)!=1)return false;
    if(first.itemOutcomes.get(AdaptedPrototypeRewardCatalog.TRAINING_TOKEN_ITEM_ID)!=RpgProgressionState.AutoLootResult.LOOTED)return false;

    // Runtime feeds the retained ledger snapshot every tick; the same defeat must never grant twice.
    runtime.tick(0f);
    if(quantity(runtime)!=1||runtime.rpg().rewardHistory().size()!=1)return false;

    // A real later life is a new defeat and grants exactly one additional prototype token.
    runtime.tick(4f);
    if(!training.alive)return false;
    runtime.damage(training,training.maxHp);
    runtime.tick(0f);
    if(quantity(runtime)!=2||runtime.rpg().rewardHistory().size()!=2)return false;
    RpgProgressionState.RewardResolution second=latest(runtime);
    return second.combatSequence>first.combatSequence&&second.source==RpgProgressionState.RewardSource.ADAPTED_TEST;
  }

  private static RuntimeState.Monster find(RuntimeState runtime,String id){
    for(RuntimeState.Monster monster:runtime.monsters())if(id.equals(monster.id))return monster;
    return null;
  }

  private static RpgProgressionState.RewardResolution latest(RuntimeState runtime){
    List<RpgProgressionState.RewardResolution> history=runtime.rpg().rewardHistory();
    return history.get(history.size()-1);
  }

  private static int quantity(RuntimeState runtime){
    Integer value=runtime.rpg().inventory().get(AdaptedPrototypeRewardCatalog.TRAINING_TOKEN_ITEM_ID);
    return value==null?0:value;
  }

  private static int value(java.util.Map<String,Integer> map,String key){Integer value=map.get(key);return value==null?0:value;}

  public static void main(String[] args){
    if(!verify())throw new AssertionError("AdaptedPrototypeRewardRuntimeAudit failed");
    System.out.println("AdaptedPrototypeRewardRuntimeAudit PASS");
  }
}
