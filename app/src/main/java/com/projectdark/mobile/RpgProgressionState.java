package com.projectdark.mobile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * RPG/Progression-owned runtime state for reward -> auto-loot -> inventory -> equipment -> stat recomputation.
 *
 * Design constraints:
 * - Content IDs/values may only enter through canonical definitions.
 * - Unknown EXP/drop/stat data stays PENDING; this class never fabricates rewards.
 * - Monster item rewards never create ground entities; resolved rewards mutate inventory exactly once.
 * - Equipment modifiers are additive projections over immutable base stats. They never mutate base stats.
 */
public final class RpgProgressionState {
  public enum Evidence { O,V,U,B,ADAPTED,PENDING,FAN }
  public enum RewardStatus { RESOLVED, PENDING_NO_CANONICAL_MONSTER_REWARD }
  public enum AutoLootResult { LOOTED, INVALID_ITEM, INVALID_QUANTITY, INVENTORY_FULL }
  public enum EquipResult { EQUIPPED, ITEM_NOT_OWNED, UNKNOWN_ITEM, NOT_EQUIPPABLE, REQUIREMENT_PENDING, REQUIREMENT_NOT_MET }

  public enum ProgressionNode {
    COMMONER,
    BASIC_JOB,
    LV99_MASTER,
    JOB_CHANGE,
    PURE_JOB,
    POST_CHOICE_LV99,
    FIRST_ADVANCEMENT
  }

  public static final class ItemDefinition {
    public final String itemId,name,equipSlot;
    public final Integer requiredLevel;
    public final Map<String,Integer> statModifiers;
    public final Evidence evidence;

    public ItemDefinition(String itemId,String name,String equipSlot,Integer requiredLevel,Map<String,Integer> statModifiers,Evidence evidence){
      this.itemId=itemId;this.name=name;this.equipSlot=equipSlot;this.requiredLevel=requiredLevel;
      this.statModifiers=Collections.unmodifiableMap(new LinkedHashMap<>(statModifiers));this.evidence=evidence;
    }
    public boolean equippable(){return equipSlot!=null&&!equipSlot.isEmpty();}
  }

  public static final class RewardResolution {
    public final long combatSequence;
    public final String monsterId;
    public final RewardStatus status;
    public final Integer exp;
    public final Map<String,Integer> autoLootedItems;
    RewardResolution(long combatSequence,String monsterId,RewardStatus status,Integer exp,Map<String,Integer> autoLootedItems){
      this.combatSequence=combatSequence;this.monsterId=monsterId;this.status=status;this.exp=exp;
      this.autoLootedItems=Collections.unmodifiableMap(new LinkedHashMap<>(autoLootedItems));
    }
  }

  public static final class StatSnapshot {
    public final Map<String,Integer> base,equipment,total;
    StatSnapshot(Map<String,Integer> base,Map<String,Integer> equipment,Map<String,Integer> total){
      this.base=Collections.unmodifiableMap(new LinkedHashMap<>(base));
      this.equipment=Collections.unmodifiableMap(new LinkedHashMap<>(equipment));
      this.total=Collections.unmodifiableMap(new LinkedHashMap<>(total));
    }
  }

  private static final int INVENTORY_STACK_LIMIT=999999; // safety ceiling only; per-item canonical stack limits remain PENDING.
  private final Map<String,ItemDefinition> items=new LinkedHashMap<>();
  private final Map<String,Integer> inventory=new LinkedHashMap<>();
  private final Map<String,String> equipmentBySlot=new LinkedHashMap<>();
  private final Map<String,Integer> baseStats=new LinkedHashMap<>();
  private final List<RewardResolution> rewardHistory=new ArrayList<>();
  private final CanonicalMonsterRewardCatalog monsterRewards=new CanonicalMonsterRewardCatalog();
  private long lastCombatSequence=0L;

  private ProgressionNode progressionNode=ProgressionNode.COMMONER;
  private String currentJobCode="COMMONER";
  private Integer normalLevel=1;
  private Long normalExp=null;

  public RpgProgressionState(){
    registerItem(new ItemDefinition("IT_GLOVE_LEATHER","가죽장갑","장갑",11,Collections.<String,Integer>emptyMap(),Evidence.O));
    registerItem(new ItemDefinition("IT_RING_THREELINEGOLD","세줄금반지","반지",11,Collections.<String,Integer>emptyMap(),Evidence.O));
    registerItem(new ItemDefinition("IT_RING_SILVERAQUA","실버아쿠아링","반지",51,Collections.<String,Integer>emptyMap(),Evidence.O));
  }

  private void registerItem(ItemDefinition def){items.put(def.itemId,def);}
  public Map<String,ItemDefinition> itemDefinitions(){return Collections.unmodifiableMap(items);}
  public Map<String,Integer> inventory(){return Collections.unmodifiableMap(inventory);}
  public Map<String,String> equipment(){return Collections.unmodifiableMap(equipmentBySlot);}
  public List<RewardResolution> rewardHistory(){return Collections.unmodifiableList(rewardHistory);}
  public ProgressionNode progressionNode(){return progressionNode;}
  public String currentJobCode(){return currentJobCode;}
  public Integer normalLevel(){return normalLevel;}
  public Long normalExp(){return normalExp;}

  /**
   * Consumes combat events exactly once. Unknown monster reward mapping produces an explicit PENDING result.
   * Resolved item rewards are inserted directly into inventory; no world-drop entity or pickup phase exists.
   */
  public void consumeCombat(List<CombatLedger.Event> events,RuntimeState runtime){
    for(CombatLedger.Event e:events){
      if(e.sequence<=lastCombatSequence)continue;
      lastCombatSequence=e.sequence;
      if(e.type!=CombatLedger.Type.MONSTER_DEFEATED)continue;
      resolveMonsterDefeat(e);
    }
  }

  private void resolveMonsterDefeat(CombatLedger.Event e){
    CanonicalMonsterRewardCatalog.RewardEntry reward=monsterRewards.find(e.targetId);
    if(reward==null){
      rewardHistory.add(new RewardResolution(e.sequence,e.targetId,RewardStatus.PENDING_NO_CANONICAL_MONSTER_REWARD,null,Collections.<String,Integer>emptyMap()));
      trimRewardHistory();
      return;
    }

    Map<String,Integer> looted=new LinkedHashMap<>();
    for(CanonicalMonsterRewardCatalog.DropHint hint:reward.dropHints){
      if(!hint.emissionResolved())continue;
      AutoLootResult result=autoLootResolvedItem(hint.itemId,hint.quantity);
      if(result==AutoLootResult.LOOTED)looted.put(hint.itemId,value(looted,hint.itemId)+hint.quantity);
    }
    rewardHistory.add(new RewardResolution(e.sequence,e.targetId,RewardStatus.RESOLVED,reward.exp,looted));
    trimRewardHistory();
  }

  /**
   * Direct-inventory endpoint for a reward whose item identity and quantity were already resolved upstream.
   * It deliberately refuses unknown items or quantities instead of creating a ground fallback.
   */
  public AutoLootResult autoLootResolvedItem(String itemId,int quantity){
    if(quantity<=0)return AutoLootResult.INVALID_QUANTITY;
    if(!items.containsKey(itemId))return AutoLootResult.INVALID_ITEM;
    int current=inventory.containsKey(itemId)?inventory.get(itemId):0;
    if(current>INVENTORY_STACK_LIMIT-quantity)return AutoLootResult.INVENTORY_FULL;
    inventory.put(itemId,current+quantity);
    return AutoLootResult.LOOTED;
  }

  public EquipResult equip(String itemId){
    Integer owned=inventory.get(itemId);
    if(owned==null||owned<=0)return EquipResult.ITEM_NOT_OWNED;
    ItemDefinition def=items.get(itemId);
    if(def==null)return EquipResult.UNKNOWN_ITEM;
    if(!def.equippable())return EquipResult.NOT_EQUIPPABLE;
    if(def.requiredLevel!=null&&normalLevel==null)return EquipResult.REQUIREMENT_PENDING;
    if(def.requiredLevel!=null&&normalLevel<def.requiredLevel)return EquipResult.REQUIREMENT_NOT_MET;
    equipmentBySlot.put(def.equipSlot,itemId);
    return EquipResult.EQUIPPED;
  }

  public StatSnapshot recomputeStats(){
    Map<String,Integer> equip=new LinkedHashMap<>();
    for(String itemId:equipmentBySlot.values()){
      ItemDefinition def=items.get(itemId);if(def==null)continue;
      for(Map.Entry<String,Integer> m:def.statModifiers.entrySet())equip.put(m.getKey(),value(equip,m.getKey())+m.getValue());
    }
    Map<String,Integer> total=new LinkedHashMap<>(baseStats);
    for(Map.Entry<String,Integer> m:equip.entrySet())total.put(m.getKey(),value(total,m.getKey())+m.getValue());
    return new StatSnapshot(baseStats,equip,total);
  }

  private void trimRewardHistory(){while(rewardHistory.size()>48)rewardHistory.remove(0);}
  private static int value(Map<String,Integer> map,String key){Integer v=map.get(key);return v==null?0:v;}
}
