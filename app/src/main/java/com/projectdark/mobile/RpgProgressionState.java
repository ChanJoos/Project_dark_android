package com.projectdark.mobile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * RPG/Progression-owned runtime state for the reward -> world drop -> pickup -> inventory
 * -> equipment -> stat recomputation chain.
 *
 * Design constraints:
 * - Content IDs/values may only enter through canonical definitions.
 * - Unknown EXP/drop/stat data stays PENDING; this class never fabricates rewards.
 * - Equipment modifiers are additive projections over immutable base stats. They never mutate base stats.
 */
public final class RpgProgressionState {
  public enum Evidence { O,V,U,B,ADAPTED,PENDING,FAN }
  public enum RewardStatus { RESOLVED, PENDING_NO_CANONICAL_MONSTER_REWARD }
  public enum PickupResult { PICKED_UP, NOT_FOUND, TOO_FAR, INVALID_ITEM, INVENTORY_FULL }
  public enum EquipResult { EQUIPPED, ITEM_NOT_OWNED, UNKNOWN_ITEM, NOT_EQUIPPABLE, REQUIREMENT_PENDING, REQUIREMENT_NOT_MET }

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

  public static final class WorldDrop {
    public final long dropId;
    public final String itemId,sourceMonsterId;
    public final int quantity;
    public final float x,y;
    public final Evidence evidence;
    WorldDrop(long dropId,String itemId,String sourceMonsterId,int quantity,float x,float y,Evidence evidence){
      this.dropId=dropId;this.itemId=itemId;this.sourceMonsterId=sourceMonsterId;this.quantity=quantity;this.x=x;this.y=y;this.evidence=evidence;
    }
  }

  public static final class RewardResolution {
    public final long combatSequence;
    public final String monsterId;
    public final RewardStatus status;
    public final Integer exp;
    public final List<Long> createdDropIds;
    RewardResolution(long combatSequence,String monsterId,RewardStatus status,Integer exp,List<Long> createdDropIds){
      this.combatSequence=combatSequence;this.monsterId=monsterId;this.status=status;this.exp=exp;
      this.createdDropIds=Collections.unmodifiableList(new ArrayList<>(createdDropIds));
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

  private static final float PICKUP_RADIUS=34f; // [B] interaction fixture, not original pickup distance.
  private static final int INVENTORY_STACK_LIMIT=999999; // safety ceiling only; per-item canonical stack limits remain PENDING.
  private final Map<String,ItemDefinition> items=new LinkedHashMap<>();
  private final Map<String,Integer> inventory=new LinkedHashMap<>();
  private final Map<String,String> equipmentBySlot=new LinkedHashMap<>();
  private final Map<String,Integer> baseStats=new LinkedHashMap<>();
  private final List<WorldDrop> worldDrops=new ArrayList<>();
  private final List<RewardResolution> rewardHistory=new ArrayList<>();
  private long nextDropId=1L,lastCombatSequence=0L;
  private Integer normalLevel=null;
  private Long normalExp=null;

  public RpgProgressionState(){
    // Canonical identity/requirements copied from Master Item_Master; no unverified stat modifiers are invented.
    registerItem(new ItemDefinition("IT_GLOVE_LEATHER","가죽장갑","장갑",11,Collections.<String,Integer>emptyMap(),Evidence.O));
  }

  private void registerItem(ItemDefinition def){items.put(def.itemId,def);}
  public Map<String,ItemDefinition> itemDefinitions(){return Collections.unmodifiableMap(items);}
  public Map<String,Integer> inventory(){return Collections.unmodifiableMap(inventory);}
  public Map<String,String> equipment(){return Collections.unmodifiableMap(equipmentBySlot);}
  public List<WorldDrop> worldDrops(){return Collections.unmodifiableList(worldDrops);}
  public List<RewardResolution> rewardHistory(){return Collections.unmodifiableList(rewardHistory);}
  public Integer normalLevel(){return normalLevel;}
  public Long normalExp(){return normalExp;}

  /**
   * Consumes combat events exactly once. Unknown monster reward mapping produces an explicit PENDING result
   * and no EXP/Gold/drop mutation.
   */
  public void consumeCombat(List<CombatLedger.Event> events,RuntimeState runtime){
    for(CombatLedger.Event e:events){
      if(e.sequence<=lastCombatSequence)continue;
      lastCombatSequence=e.sequence;
      if(e.type!=CombatLedger.Type.MONSTER_DEFEATED)continue;
      resolveMonsterDefeat(e,runtime);
    }
  }

  private void resolveMonsterDefeat(CombatLedger.Event e,RuntimeState runtime){
    // combat_dummy_01 is explicitly a [B] fixture and has no canonical Master reward row.
    // Any future canonical monster must be mapped from Master data before rewards are emitted.
    rewardHistory.add(new RewardResolution(e.sequence,e.targetId,RewardStatus.PENDING_NO_CANONICAL_MONSTER_REWARD,null,Collections.<Long>emptyList()));
    trimRewardHistory();
  }

  /** Canonical reward systems may call this only after item/drop evidence is resolved upstream. */
  public long createWorldDrop(String itemId,String sourceMonsterId,int quantity,float x,float y,Evidence evidence){
    if(quantity<=0||!items.containsKey(itemId))return -1L;
    WorldDrop d=new WorldDrop(nextDropId++,itemId,sourceMonsterId,quantity,x,y,evidence);
    worldDrops.add(d);return d.dropId;
  }

  public PickupResult pickup(long dropId,float playerX,float playerY){
    for(int i=0;i<worldDrops.size();i++){
      WorldDrop d=worldDrops.get(i);
      if(d.dropId!=dropId)continue;
      if(!items.containsKey(d.itemId))return PickupResult.INVALID_ITEM;
      float dx=playerX-d.x,dy=playerY-d.y;
      if(dx*dx+dy*dy>PICKUP_RADIUS*PICKUP_RADIUS)return PickupResult.TOO_FAR;
      int current=inventory.containsKey(d.itemId)?inventory.get(d.itemId):0;
      if(current>INVENTORY_STACK_LIMIT-d.quantity)return PickupResult.INVENTORY_FULL;
      inventory.put(d.itemId,current+d.quantity);
      worldDrops.remove(i);
      return PickupResult.PICKED_UP;
    }
    return PickupResult.NOT_FOUND;
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
