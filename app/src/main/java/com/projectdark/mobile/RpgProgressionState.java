package com.projectdark.mobile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** RPG-owned persistent progression, reward, inventory, equipment and learned-action state. */
public final class RpgProgressionState {
  public enum Evidence { O,V,U,B,ADAPTED,PENDING,FAN }
  public enum RewardStatus { RESOLVED, PENDING_NO_CANONICAL_MONSTER_REWARD }
  public enum AutoLootResult { LOOTED, INVALID_ITEM, INVALID_QUANTITY, INVENTORY_FULL }
  public enum RewardGrantStatus { GRANTED, INVENTORY_FULL, INVALID_ITEM, INVALID_QUANTITY, UNRESOLVED_REWARD }
  public enum CombatConsumeStatus { PROCESSED_DEFEAT, DUPLICATE_OR_STALE, IGNORED_NON_DEFEAT }
  public enum EquipResult { EQUIPPED, ITEM_NOT_OWNED, UNKNOWN_ITEM, NOT_EQUIPPABLE, REQUIREMENT_PENDING, REQUIREMENT_NOT_MET }
  public enum RequirementResult { MET, PENDING, LEVEL_NOT_MET, JOB_NOT_MET, UNKNOWN_ITEM }
  public enum RestoreResult { RESTORED, UNSUPPORTED_SCHEMA, INVALID_STATE }

  public enum ProgressionNode { COMMONER,BASIC_JOB,LV99_MASTER,JOB_CHANGE,PURE_JOB,POST_CHOICE_LV99,FIRST_ADVANCEMENT }

  public static final class ItemDefinition {
    public final String itemId,name,equipSlot;
    public final Integer requiredLevel;
    public final Set<String> allowedJobCodes;
    public final boolean jobRestrictionResolved;
    public final String attackElement,defenseElement;
    public final Map<String,Integer> statModifiers;
    public final Evidence evidence;
    public ItemDefinition(String itemId,String name,String equipSlot,Integer requiredLevel,Set<String> allowedJobCodes,
        boolean jobRestrictionResolved,String attackElement,String defenseElement,Map<String,Integer> statModifiers,Evidence evidence){
      this.itemId=itemId;this.name=name;this.equipSlot=equipSlot;this.requiredLevel=requiredLevel;
      this.allowedJobCodes=Collections.unmodifiableSet(new LinkedHashSet<>(allowedJobCodes));
      this.jobRestrictionResolved=jobRestrictionResolved;this.attackElement=attackElement;this.defenseElement=defenseElement;
      this.statModifiers=Collections.unmodifiableMap(new LinkedHashMap<>(statModifiers));this.evidence=evidence;
    }
    public boolean equippable(){return equipSlot!=null&&!equipSlot.isEmpty();}
    public boolean unrestrictedJob(){return jobRestrictionResolved&&allowedJobCodes.isEmpty();}
  }

  public static final class RewardGrantOutcome {
    public final String itemId;
    public final Integer requestedQuantity;
    public final RewardGrantStatus status;
    public final Evidence evidence;
    RewardGrantOutcome(String itemId,Integer requestedQuantity,RewardGrantStatus status,Evidence evidence){
      this.itemId=itemId;this.requestedQuantity=requestedQuantity;this.status=status;this.evidence=evidence;
    }
  }

  public static final class RewardResolution {
    public final long combatSequence;public final String monsterId;public final RewardStatus status;public final Integer exp;
    public final Map<String,Integer> autoLootedItems;
    public final List<RewardGrantOutcome> grantOutcomes;
    RewardResolution(long combatSequence,String monsterId,RewardStatus status,Integer exp,Map<String,Integer> autoLootedItems,
        List<RewardGrantOutcome> grantOutcomes){
      this.combatSequence=combatSequence;this.monsterId=monsterId;this.status=status;this.exp=exp;
      this.autoLootedItems=Collections.unmodifiableMap(new LinkedHashMap<>(autoLootedItems));
      this.grantOutcomes=Collections.unmodifiableList(new ArrayList<>(grantOutcomes));
    }
  }

  public static final class CombatConsumeOutcome {
    public final long combatSequence;
    public final CombatConsumeStatus status;
    CombatConsumeOutcome(long combatSequence,CombatConsumeStatus status){this.combatSequence=combatSequence;this.status=status;}
  }

  public static final class StatSnapshot {
    public final Map<String,Integer> base,equipment,total;
    StatSnapshot(Map<String,Integer> base,Map<String,Integer> equipment,Map<String,Integer> total){
      this.base=Collections.unmodifiableMap(new LinkedHashMap<>(base));this.equipment=Collections.unmodifiableMap(new LinkedHashMap<>(equipment));
      this.total=Collections.unmodifiableMap(new LinkedHashMap<>(total));
    }
  }

  private static final int INVENTORY_STACK_LIMIT=999999;
  private final Map<String,ItemDefinition> items=new LinkedHashMap<>();
  private final Map<String,Integer> inventory=new LinkedHashMap<>();
  private final Map<String,String> equipmentBySlot=new LinkedHashMap<>();
  private final Map<String,Integer> baseStats=new LinkedHashMap<>();
  private final List<RewardResolution> rewardHistory=new ArrayList<>();
  private final Set<String> learnedActionIds=new LinkedHashSet<>();
  private final CanonicalMonsterRewardCatalog monsterRewards=new CanonicalMonsterRewardCatalog();
  private long lastCombatSequence=0L;
  private ProgressionNode progressionNode=ProgressionNode.COMMONER;
  private String currentJobCode="COMMONER";
  private Integer normalLevel=1;
  private Long normalExp=null;
  private Long gold=null;

  public RpgProgressionState(){
    Map<String,Integer> noStats=Collections.emptyMap();Set<String> anyJob=Collections.emptySet();
    Set<String> physicalJobs=jobSet("WARRIOR","ROGUE","MARTIAL_ARTIST"),magicJobs=jobSet("MAGE","CLERIC");
    registerItem(new ItemDefinition("IT_GLOVE_LEATHER","가죽장갑","장갑",11,anyJob,true,null,null,noStats,Evidence.O));
    registerItem(new ItemDefinition("IT_LEGGING_LEATHER","가죽각반","각반",11,anyJob,true,null,null,noStats,Evidence.O));
    registerItem(new ItemDefinition("IT_SHOES","신발","신발",11,anyJob,true,null,null,noStats,Evidence.O));
    registerItem(new ItemDefinition("IT_EARRING_DOUBLE_SILVER","쌍은귀걸이","귀걸이",11,physicalJobs,true,null,null,noStats,Evidence.O));
    registerItem(new ItemDefinition("IT_RING_REDJADE","홍옥반지","반지",11,anyJob,true,null,null,noStats,Evidence.O));
    registerItem(new ItemDefinition("IT_RING_THREELINEGOLD","세줄금반지","반지",11,anyJob,true,null,null,noStats,Evidence.O));
    registerItem(new ItemDefinition("IT_RING_GORU","고루반지","반지",11,magicJobs,true,null,null,noStats,Evidence.O));
    registerItem(new ItemDefinition("IT_NECK_WATER_PEARL","바다의진주목걸이","목걸이",11,anyJob,true,"바다",null,noStats,Evidence.O));
    registerItem(new ItemDefinition("IT_BELT_WATER_LEATHER","바다의가죽벨트","벨트",11,anyJob,true,null,"바다",noStats,Evidence.O));
    registerItem(new ItemDefinition("IT_NECK_EARTH_PEARL","대지의진주목걸이","목걸이",11,anyJob,true,"대지",null,noStats,Evidence.O));
    registerItem(new ItemDefinition("IT_BELT_EARTH_LEATHER","대지의가죽벨트","벨트",11,anyJob,true,null,"대지",noStats,Evidence.O));
    registerItem(new ItemDefinition("IT_NECK_WIND_PEARL","바람의진주목걸이","목걸이",11,anyJob,true,"바람",null,noStats,Evidence.O));
    registerItem(new ItemDefinition("IT_BELT_WIND_LEATHER","바람의가죽벨트","벨트",11,anyJob,true,null,"바람",noStats,Evidence.O));
    registerItem(new ItemDefinition("IT_NECK_FIRE_PEARL","화염의진주목걸이","목걸이",11,anyJob,true,"화염",null,noStats,Evidence.O));
    registerItem(new ItemDefinition("IT_BELT_FIRE_LEATHER","화염의가죽벨트","벨트",11,anyJob,true,null,"화염",noStats,Evidence.O));
    registerItem(new ItemDefinition("IT_RING_SILVERAQUA","실버아쿠아링","반지",51,anyJob,true,null,null,noStats,Evidence.O));
  }

  private static Set<String> jobSet(String... jobs){return new LinkedHashSet<>(Arrays.asList(jobs));}
  private void registerItem(ItemDefinition def){items.put(def.itemId,def);}
  public Map<String,ItemDefinition> itemDefinitions(){return Collections.unmodifiableMap(items);}
  public Map<String,Integer> inventory(){return Collections.unmodifiableMap(inventory);}
  public Map<String,String> equipment(){return Collections.unmodifiableMap(equipmentBySlot);}
  public List<RewardResolution> rewardHistory(){return Collections.unmodifiableList(rewardHistory);}
  public Set<String> learnedActionIds(){return Collections.unmodifiableSet(learnedActionIds);}
  public ProgressionNode progressionNode(){return progressionNode;}
  public String currentJobCode(){return currentJobCode;}
  public Integer normalLevel(){return normalLevel;}
  public Long normalExp(){return normalExp;}
  public Long gold(){return gold;}
  public long lastCombatSequence(){return lastCombatSequence;}

  public boolean setLearnedAction(String actionId,boolean learned){
    if(!RpgActionMetadataCatalog.isKnownAction(actionId))return false;
    if(learned)learnedActionIds.add(actionId);else learnedActionIds.remove(actionId);return true;
  }

  public RequirementResult evaluateRequirements(String itemId,String jobCode,Integer level){
    ItemDefinition def=items.get(itemId);if(def==null)return RequirementResult.UNKNOWN_ITEM;
    if(def.requiredLevel!=null&&level==null)return RequirementResult.PENDING;
    if(def.requiredLevel!=null&&level<def.requiredLevel)return RequirementResult.LEVEL_NOT_MET;
    if(!def.jobRestrictionResolved)return RequirementResult.PENDING;
    if(!def.allowedJobCodes.isEmpty()&&(jobCode==null||!def.allowedJobCodes.contains(jobCode)))return RequirementResult.JOB_NOT_MET;
    return RequirementResult.MET;
  }
  public RequirementResult currentRequirements(String itemId){return evaluateRequirements(itemId,currentJobCode,normalLevel);}

  public void consumeCombat(List<CombatLedger.Event> events,RuntimeState runtime){consumeCombatWithOutcomes(events,runtime);}

  public List<CombatConsumeOutcome> consumeCombatWithOutcomes(List<CombatLedger.Event> events,RuntimeState runtime){
    List<CombatConsumeOutcome> outcomes=new ArrayList<>();
    for(CombatLedger.Event e:events){
      if(e.sequence<=lastCombatSequence){outcomes.add(new CombatConsumeOutcome(e.sequence,CombatConsumeStatus.DUPLICATE_OR_STALE));continue;}
      lastCombatSequence=e.sequence;
      if(e.type==CombatLedger.Type.MONSTER_DEFEATED){
        resolveMonsterDefeat(e);outcomes.add(new CombatConsumeOutcome(e.sequence,CombatConsumeStatus.PROCESSED_DEFEAT));
      }else outcomes.add(new CombatConsumeOutcome(e.sequence,CombatConsumeStatus.IGNORED_NON_DEFEAT));
    }
    return Collections.unmodifiableList(outcomes);
  }

  private void resolveMonsterDefeat(CombatLedger.Event e){
    CanonicalMonsterRewardCatalog.RewardEntry reward=monsterRewards.find(e.targetId);
    if(reward==null){rewardHistory.add(new RewardResolution(e.sequence,e.targetId,RewardStatus.PENDING_NO_CANONICAL_MONSTER_REWARD,
        null,Collections.emptyMap(),Collections.emptyList()));trimRewardHistory();return;}
    Map<String,Integer> looted=new LinkedHashMap<>();List<RewardGrantOutcome> outcomes=new ArrayList<>();
    for(CanonicalMonsterRewardCatalog.DropHint hint:reward.dropHints){
      RewardGrantOutcome outcome=hint.emissionResolved()
          ?grantResolvedRewardItem(hint.itemId,hint.quantity,hint.evidence)
          :new RewardGrantOutcome(hint.itemId,hint.quantity,RewardGrantStatus.UNRESOLVED_REWARD,hint.evidence);
      outcomes.add(outcome);
      if(outcome.status==RewardGrantStatus.GRANTED)looted.put(hint.itemId,value(looted,hint.itemId)+outcome.requestedQuantity);
    }
    rewardHistory.add(new RewardResolution(e.sequence,e.targetId,RewardStatus.RESOLVED,reward.exp,looted,outcomes));trimRewardHistory();
  }

  public RewardGrantOutcome grantResolvedRewardItem(String itemId,Integer quantity,Evidence evidence){
    if(quantity==null)return new RewardGrantOutcome(itemId,null,RewardGrantStatus.UNRESOLVED_REWARD,evidence);
    if(quantity<=0)return new RewardGrantOutcome(itemId,quantity,RewardGrantStatus.INVALID_QUANTITY,evidence);
    if(itemId==null||!items.containsKey(itemId))return new RewardGrantOutcome(itemId,quantity,RewardGrantStatus.INVALID_ITEM,evidence);
    int current=inventory.containsKey(itemId)?inventory.get(itemId):0;
    if(current>INVENTORY_STACK_LIMIT-quantity)return new RewardGrantOutcome(itemId,quantity,RewardGrantStatus.INVENTORY_FULL,evidence);
    inventory.put(itemId,current+quantity);return new RewardGrantOutcome(itemId,quantity,RewardGrantStatus.GRANTED,evidence);
  }

  public AutoLootResult autoLootResolvedItem(String itemId,int quantity){
    RewardGrantStatus status=grantResolvedRewardItem(itemId,quantity,Evidence.PENDING).status;
    switch(status){
      case GRANTED:return AutoLootResult.LOOTED;
      case INVALID_ITEM:return AutoLootResult.INVALID_ITEM;
      case INVALID_QUANTITY:return AutoLootResult.INVALID_QUANTITY;
      case INVENTORY_FULL:return AutoLootResult.INVENTORY_FULL;
      case UNRESOLVED_REWARD:
      default:return AutoLootResult.INVALID_QUANTITY;
    }
  }

  public EquipResult equip(String itemId){
    Integer owned=inventory.get(itemId);if(owned==null||owned<=0)return EquipResult.ITEM_NOT_OWNED;ItemDefinition def=items.get(itemId);
    if(def==null)return EquipResult.UNKNOWN_ITEM;if(!def.equippable())return EquipResult.NOT_EQUIPPABLE;
    RequirementResult requirements=currentRequirements(itemId);if(requirements==RequirementResult.PENDING)return EquipResult.REQUIREMENT_PENDING;
    if(requirements!=RequirementResult.MET)return EquipResult.REQUIREMENT_NOT_MET;equipmentBySlot.put(def.equipSlot,itemId);return EquipResult.EQUIPPED;
  }

  public RpgSaveSnapshot saveSnapshot(){
    return new RpgSaveSnapshot(RpgSaveSnapshot.CURRENT_SCHEMA_VERSION,progressionNode,currentJobCode,normalLevel,normalExp,gold,lastCombatSequence,inventory,equipmentBySlot,learnedActionIds);
  }

  /** Atomic fail-closed restore: validates all IDs/quantities before replacing mutable state. */
  public RestoreResult restoreSnapshot(RpgSaveSnapshot s){
    if(s==null||s.schemaVersion!=RpgSaveSnapshot.CURRENT_SCHEMA_VERSION)return s==null?RestoreResult.INVALID_STATE:RestoreResult.UNSUPPORTED_SCHEMA;
    if(s.progressionNode==null||s.currentJobCode==null||s.normalLevel==null||s.normalLevel<1||s.normalLevel>99||(s.gold!=null&&s.gold<0)||s.lastCombatSequence<0)return RestoreResult.INVALID_STATE;
    for(Map.Entry<String,Integer> e:s.inventory.entrySet())if(!items.containsKey(e.getKey())||e.getValue()==null||e.getValue()<=0||e.getValue()>INVENTORY_STACK_LIMIT)return RestoreResult.INVALID_STATE;
    for(Map.Entry<String,String> e:s.equipmentBySlot.entrySet()){
      ItemDefinition def=items.get(e.getValue());Integer owned=s.inventory.get(e.getValue());
      if(def==null||owned==null||owned<=0||!def.equippable()||!def.equipSlot.equals(e.getKey()))return RestoreResult.INVALID_STATE;
    }
    for(String actionId:s.learnedActionIds)if(!RpgActionMetadataCatalog.isKnownAction(actionId))return RestoreResult.INVALID_STATE;
    inventory.clear();inventory.putAll(s.inventory);equipmentBySlot.clear();equipmentBySlot.putAll(s.equipmentBySlot);
    learnedActionIds.clear();learnedActionIds.addAll(s.learnedActionIds);progressionNode=s.progressionNode;currentJobCode=s.currentJobCode;
    normalLevel=s.normalLevel;normalExp=s.normalExp;gold=s.gold;lastCombatSequence=s.lastCombatSequence;rewardHistory.clear();return RestoreResult.RESTORED;
  }

  public StatSnapshot recomputeStats(){
    Map<String,Integer> equip=new LinkedHashMap<>();for(String itemId:equipmentBySlot.values()){ItemDefinition def=items.get(itemId);if(def==null)continue;for(Map.Entry<String,Integer> m:def.statModifiers.entrySet())equip.put(m.getKey(),value(equip,m.getKey())+m.getValue());}
    Map<String,Integer> total=new LinkedHashMap<>(baseStats);for(Map.Entry<String,Integer> m:equip.entrySet())total.put(m.getKey(),value(total,m.getKey())+m.getValue());return new StatSnapshot(baseStats,equip,total);
  }
  private void trimRewardHistory(){while(rewardHistory.size()>48)rewardHistory.remove(0);}
  private static int value(Map<String,Integer> map,String key){Integer v=map.get(key);return v==null?0:v;}
}
