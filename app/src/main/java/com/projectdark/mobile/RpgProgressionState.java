package com.projectdark.mobile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
  public enum RewardSource { CANONICAL, ADAPTED_TEST, UNRESOLVED }
  public enum AutoLootResult { LOOTED, INVALID_ITEM, INVALID_QUANTITY, INVENTORY_FULL }
  public enum EquipResult { EQUIPPED, UNEQUIPPED, ITEM_NOT_OWNED, UNKNOWN_ITEM, NOT_EQUIPPABLE, REQUIREMENT_PENDING, REQUIREMENT_NOT_MET }
  public enum UseResult { USED, ITEM_NOT_OWNED, NOT_CONSUMABLE, NO_EFFECT }
  public enum RequirementResult { MET, PENDING, LEVEL_NOT_MET, JOB_NOT_MET, UNKNOWN_ITEM }

  public enum ProgressionNode {
    COMMONER,
    BASIC_JOB,
    LV99_MASTER,
    JOB_CHANGE,
    PURE_JOB,
    POST_CHOICE_LV99,
    FIRST_ADVANCEMENT
  }

  public static final String ARMOR_SLOT="갑옷";
  public static final String LOWER_GARMENT_SLOT="각반";
  public static final String WEAPON_SLOT="무기";

  public static final class ItemDefinition {
    public final String itemId,name,equipSlot,appearanceId;
    public final AnimationAction basicAttackAction;
    public final Integer requiredLevel;
    public final Set<String> allowedJobCodes;
    public final boolean jobRestrictionResolved;
    public final String attackElement,defenseElement;
    public final Map<String,Integer> statModifiers;
    public final Evidence evidence;

    public ItemDefinition(String itemId,String name,String equipSlot,Integer requiredLevel,Set<String> allowedJobCodes,
        boolean jobRestrictionResolved,String attackElement,String defenseElement,Map<String,Integer> statModifiers,Evidence evidence){
      this(itemId,name,equipSlot,null,null,requiredLevel,allowedJobCodes,jobRestrictionResolved,
          attackElement,defenseElement,statModifiers,evidence);
    }
    public ItemDefinition(String itemId,String name,String equipSlot,String appearanceId,Integer requiredLevel,Set<String> allowedJobCodes,
        boolean jobRestrictionResolved,String attackElement,String defenseElement,Map<String,Integer> statModifiers,Evidence evidence){
      this(itemId,name,equipSlot,appearanceId,null,requiredLevel,allowedJobCodes,jobRestrictionResolved,
          attackElement,defenseElement,statModifiers,evidence);
    }
    public ItemDefinition(String itemId,String name,String equipSlot,String appearanceId,AnimationAction basicAttackAction,
        Integer requiredLevel,Set<String> allowedJobCodes,boolean jobRestrictionResolved,String attackElement,
        String defenseElement,Map<String,Integer> statModifiers,Evidence evidence){
      this.itemId=itemId;this.name=name;this.equipSlot=equipSlot;this.requiredLevel=requiredLevel;
      this.appearanceId=appearanceId;this.basicAttackAction=basicAttackAction;
      this.allowedJobCodes=Collections.unmodifiableSet(new LinkedHashSet<>(allowedJobCodes));
      this.jobRestrictionResolved=jobRestrictionResolved;
      this.attackElement=attackElement;this.defenseElement=defenseElement;
      this.statModifiers=Collections.unmodifiableMap(new LinkedHashMap<>(statModifiers));this.evidence=evidence;
    }
    public boolean equippable(){return equipSlot!=null&&!equipSlot.isEmpty();}
    public boolean unrestrictedJob(){return jobRestrictionResolved&&allowedJobCodes.isEmpty();}
  }

  public static final class RewardResolution {
    public final long combatSequence;
    public final String monsterId;
    public final RewardStatus status;
    public final Integer exp;
    public final Map<String,Integer> autoLootedItems;
    public final Map<String,AutoLootResult> itemOutcomes;
    public final RewardSource source;
    public final String policyId,evidence;
    RewardResolution(long combatSequence,String monsterId,RewardStatus status,Integer exp,Map<String,Integer> autoLootedItems){
      this(combatSequence,monsterId,status,exp,autoLootedItems,
          Collections.<String,AutoLootResult>emptyMap(),
          status==RewardStatus.RESOLVED?RewardSource.CANONICAL:RewardSource.UNRESOLVED,null,null);
    }
    RewardResolution(long combatSequence,String monsterId,RewardStatus status,Integer exp,
        Map<String,Integer> autoLootedItems,Map<String,AutoLootResult> itemOutcomes,
        RewardSource source,String policyId,String evidence){
      this.combatSequence=combatSequence;this.monsterId=monsterId;this.status=status;this.exp=exp;
      this.autoLootedItems=Collections.unmodifiableMap(new LinkedHashMap<>(autoLootedItems));
      this.itemOutcomes=Collections.unmodifiableMap(new LinkedHashMap<>(itemOutcomes));
      this.source=source==null?RewardSource.UNRESOLVED:source;this.policyId=policyId;this.evidence=evidence;
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
  private final AdaptedPrototypeRewardCatalog prototypeRewards=new AdaptedPrototypeRewardCatalog();
  private long lastCombatSequence=0L;

  public static final String B_SMALL_POTION_ITEM_ID="IT_B_SMALL_POTION";
  public static final long B_SMALL_POTION_PRICE=20L;
  public static final String SHOP_MOKDO_ITEM_ID="IT_ADAPTED_PLAYTEST_MOKDO";
  public static final String SHOP_LEATHER_GLOVE_ITEM_ID="IT_GLOVE_LEATHER";
  public static final String SHOP_SHOES_ITEM_ID="IT_SHOES";
  public static final String STARTER_SHIRT_ITEM_ID="IT_APPEARANCE_PEASANT_SHIRT";
  public static final String STARTER_HAT_ITEM_ID="IT_ADAPTED_STARTER_HAT";
  public static final String STARTER_HAT_APPEARANCE_ID="mh108";
  public static final String STARTER_SHIELD_ITEM_ID="IT_ADAPTED_STARTER_SHIELD";
  public static final String STARTER_SHIELD_APPEARANCE_ID="ms001";
  public static final String STARTER_SHIRT_APPEARANCE_ID="mu0000001";
  public static final String VERIFIED_STARTER_ROBE_ITEM_ID="IT_APPEARANCE_LUERS_LEATHER_ROBE";
  public static final String VERIFIED_STARTER_ROBE_APPEARANCE_ID="mu0000058";
  public static final String PLAYTEST_WEAPON_ITEM_ID="IT_ADAPTED_PLAYTEST_MOKDO";
  public static final String B5_SMALL_HP_POTION_ITEM_ID=B_SMALL_POTION_ITEM_ID;
  public static final int B5_SMALL_HP_POTION_HEAL=35;
  public static final String PLAYTEST_WEAPON_APPEARANCE_ID="mw001";
  public static final String PLAYTEST_WEAPON_SOURCE_EVIDENCE="Asset_Master mw001 목도 SOURCE_NAMED; COMMONER equip and SWING are ADAPTED PLAYTEST FIXTURE";

  private ProgressionNode progressionNode=ProgressionNode.COMMONER;
  private String currentJobCode="COMMONER";
  private Integer normalLevel=1;
  // EXP starts at zero and level-up uses only master/data/Level_EXP_Curve.csv thresholds.
  private Long normalExp=0L;
  private Long gold=0L;

  public RpgProgressionState(){
    Map<String,Integer> noStats=Collections.<String,Integer>emptyMap();
    Set<String> anyJob=Collections.<String>emptySet();
    Set<String> physicalJobs=jobSet("WARRIOR","ROGUE","MARTIAL_ARTIST");
    Set<String> magicJobs=jobSet("MAGE","CLERIC");

    registerItem(new ItemDefinition("IT_GLOVE_LEATHER","가죽장갑","장갑",11,anyJob,true,null,null,noStats,Evidence.O));
    registerItem(new ItemDefinition("IT_LEGGING_LEATHER","가죽각반","각반",11,anyJob,true,null,null,noStats,Evidence.O));
    registerItem(new ItemDefinition("IT_SHOES","신발","신발","ml228",1,anyJob,true,null,null,noStats,Evidence.O));
    registerItem(new ItemDefinition(STARTER_HAT_ITEM_ID,"초보 햇 [ADAPTED]","모자",STARTER_HAT_APPEARANCE_ID,1,anyJob,true,null,null,noStats,Evidence.ADAPTED));
    registerItem(new ItemDefinition(STARTER_SHIELD_ITEM_ID,"초보 방패 [ADAPTED]","방패",STARTER_SHIELD_APPEARANCE_ID,1,anyJob,true,null,null,noStats,Evidence.ADAPTED));
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
    registerItem(new ItemDefinition(AdaptedPrototypeRewardCatalog.TRAINING_TOKEN_ITEM_ID,
        "훈련 증표 [B]",null,null,anyJob,true,null,null,noStats,Evidence.B));
    registerItem(new ItemDefinition(B_SMALL_POTION_ITEM_ID,"소형 회복물약 [B]",null,null,anyJob,true,null,null,noStats,Evidence.B));
    registerItem(new ItemDefinition(STARTER_SHIRT_ITEM_ID,"셔츠 [PENDING WEARABLE FRAMES]",ARMOR_SLOT,
        STARTER_SHIRT_APPEARANCE_ID,1,anyJob,true,null,null,noStats,Evidence.V));
    registerItem(new ItemDefinition(PLAYTEST_WEAPON_ITEM_ID,"목도 [ADAPTED PLAYTEST]",WEAPON_SLOT,
        PLAYTEST_WEAPON_APPEARANCE_ID,AnimationAction.SWING,1,anyJob,true,null,null,noStats,Evidence.ADAPTED));
    // Playable visual-slice fixture: source-named appearance, no invented stats or reward relation.
    inventory.put(STARTER_SHIRT_ITEM_ID,1);
    // Starter armor uses the verified classic paper-doll garment frames while retaining the canonical shirt item identity.
    equipmentBySlot.put(ARMOR_SLOT,STARTER_SHIRT_ITEM_ID);
    inventory.put(PLAYTEST_WEAPON_ITEM_ID,1);
    inventory.put("IT_SHOES",1);
    inventory.put(STARTER_HAT_ITEM_ID,1);
    inventory.put(STARTER_SHIELD_ITEM_ID,1);
    equipmentBySlot.put("신발","IT_SHOES");
    equipmentBySlot.put("모자",STARTER_HAT_ITEM_ID);
    equipmentBySlot.put("방패",STARTER_SHIELD_ITEM_ID);
  }

  private static Set<String> jobSet(String... jobs){return new LinkedHashSet<>(Arrays.asList(jobs));}
  private void registerItem(ItemDefinition def){items.put(def.itemId,def);}
  public Map<String,ItemDefinition> itemDefinitions(){return Collections.unmodifiableMap(items);}
  public Map<String,Integer> inventory(){return Collections.unmodifiableMap(inventory);}
  public Map<String,String> equipment(){return Collections.unmodifiableMap(equipmentBySlot);}
  public ItemDefinition equippedDefinition(String slot){
    String itemId=equipmentBySlot.get(slot);return itemId==null?null:items.get(itemId);
  }
  public List<RewardResolution> rewardHistory(){return Collections.unmodifiableList(rewardHistory);}
  public ProgressionNode progressionNode(){return progressionNode;}
  public String currentJobCode(){return currentJobCode;}
  public Integer normalLevel(){return normalLevel;}
  public Long normalExp(){return normalExp;}
  public Long gold(){return gold;}
  private int str=3,intel=3,wis=3,con=3,dex=3,statPoints=0;
  private int baseMaxHp=100,baseMaxMp=100;
  private final LevelGrowthPolicy growthPolicy=new LevelGrowthPolicy.BalancedV1();
  public int str(){return str;} public int intel(){return intel;} public int wis(){return wis;} public int con(){return con;} public int dex(){return dex;} public int statPoints(){return statPoints;}
  public int physicalAttack(){return finalStats().prototypePhysicalAttack();}
  public int baseMaxHp(){return baseMaxHp;} public int baseMaxMp(){return baseMaxMp;}
  public int maxHpGrowth(){return finalStats().maxHp;} public int maxMpGrowth(){return finalStats().maxMp;}
  public FinalStats finalStats(){return FinalStats.from(this);}
  public String attackElement(){for(String id:equipmentBySlot.values()){ItemDefinition d=items.get(id);if(d!=null&&d.attackElement!=null)return d.attackElement;}return "NONE";}
  public String defenseElement(){for(String id:equipmentBySlot.values()){ItemDefinition d=items.get(id);if(d!=null&&d.defenseElement!=null)return d.defenseElement;}return "NONE";}
  public void restoreBaseResources(int hp,int mp){baseMaxHp=Math.max(1,hp);baseMaxMp=Math.max(0,mp);}
  public boolean spendStat(String stat){if(statPoints<=0)return false;if("STR".equals(stat))str++;else if("INT".equals(stat))intel++;else if("WIS".equals(stat))wis++;else if("CON".equals(stat))con++;else if("DEX".equals(stat))dex++;else return false;statPoints--;return true;}
  public void restoreStats(int s,int i,int w,int c,int d,int points){str=Math.max(3,s);intel=Math.max(3,i);wis=Math.max(3,w);con=Math.max(3,c);dex=Math.max(3,d);statPoints=Math.max(0,points);}
  public void restoreGold(long value){gold=Math.max(0L,value);}
  public boolean buySmallPotion(){return buyItem(B_SMALL_POTION_ITEM_ID,B_SMALL_POTION_PRICE);}
  public boolean buyItem(String itemId,long price){
    if(price<0||gold<price||!items.containsKey(itemId))return false;
    AutoLootResult added=autoLootResolvedItem(itemId,1);
    if(added!=AutoLootResult.LOOTED)return false;
    gold-=price;return true;
  }
  public int grantAdaptedReward(long exp,long goldAmount){if(exp>0)normalExp+=exp;if(goldAmount>0)gold+=goldAmount;return normalizeCanonicalLevel();}

  /** Restores durable progression without reflection, then normalizes against canonical Level_EXP_Curve. */
  public void restoreProgression(int level,long exp){
    normalLevel=Math.max(1,Math.min(99,level));
    normalExp=Math.max(0L,exp);
    normalizeCanonicalLevel();
  }

  /** Applies only canonical level thresholds and carries overflow EXP to the next level. */
  public int normalizeCanonicalLevel(){
    int before=normalLevel==null?1:normalLevel;
    int level=before;long exp=normalExp==null?0L:normalExp;
    while(level<99){Long required=LevelExpCurve.requiredForNext(level);if(required==null||exp<required)break;exp-=required;level++;}
    normalLevel=level;normalExp=exp;int gained=level-before;if(gained>0){for(int lv=before+1;lv<=level;lv++){baseMaxHp+=growthPolicy.hpGain(lv,currentJobCode,con);baseMaxMp+=growthPolicy.mpGain(lv,currentJobCode,wis);}statPoints+=gained*2;}return gained;
  }

  /** Pure requirement projection for UI/audits; it does not mutate player or item state. */
  public RequirementResult evaluateRequirements(String itemId,String jobCode,Integer level){
    ItemDefinition def=items.get(itemId);
    if(def==null)return RequirementResult.UNKNOWN_ITEM;
    if(def.requiredLevel!=null&&level==null)return RequirementResult.PENDING;
    if(def.requiredLevel!=null&&level<def.requiredLevel)return RequirementResult.LEVEL_NOT_MET;
    if(!def.jobRestrictionResolved)return RequirementResult.PENDING;
    if(!def.allowedJobCodes.isEmpty()&&(jobCode==null||!def.allowedJobCodes.contains(jobCode)))return RequirementResult.JOB_NOT_MET;
    return RequirementResult.MET;
  }

  public RequirementResult currentRequirements(String itemId){return evaluateRequirements(itemId,currentJobCode,normalLevel);}

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
      AdaptedPrototypeRewardCatalog.Entry prototype=prototypeRewards.find(e.targetId);
      if(prototype!=null){
        Map<String,Integer> granted=new LinkedHashMap<>();
        Map<String,AutoLootResult> outcomes=new LinkedHashMap<>();
        AutoLootResult result=autoLootResolvedItem(prototype.itemId,prototype.quantity);
        outcomes.put(prototype.itemId,result);
        if(result==AutoLootResult.LOOTED)granted.put(prototype.itemId,prototype.quantity);
        grantAdaptedReward(AdaptedPrototypeRewardCatalog.TRAINING_MONSTER_EXP,AdaptedPrototypeRewardCatalog.TRAINING_MONSTER_GOLD);
        rewardHistory.add(new RewardResolution(e.sequence,e.targetId,RewardStatus.RESOLVED,AdaptedPrototypeRewardCatalog.TRAINING_MONSTER_EXP,granted,outcomes,
            RewardSource.ADAPTED_TEST,prototype.policyId,prototype.evidence));
        trimRewardHistory();
        return;
      }
      rewardHistory.add(new RewardResolution(e.sequence,e.targetId,RewardStatus.PENDING_NO_CANONICAL_MONSTER_REWARD,null,Collections.<String,Integer>emptyMap()));
      trimRewardHistory();
      return;
    }

    Map<String,Integer> looted=new LinkedHashMap<>();
    Map<String,AutoLootResult> outcomes=new LinkedHashMap<>();
    for(CanonicalMonsterRewardCatalog.DropHint hint:reward.dropHints){
      if(!hint.emissionResolved())continue;
      AutoLootResult result=autoLootResolvedItem(hint.itemId,hint.quantity);
      outcomes.put(hint.itemId,result);
      if(result==AutoLootResult.LOOTED)looted.put(hint.itemId,value(looted,hint.itemId)+hint.quantity);
    }
    if(reward.exp!=null)normalExp+=reward.exp.longValue();
    rewardHistory.add(new RewardResolution(e.sequence,e.targetId,RewardStatus.RESOLVED,reward.exp,looted,outcomes,
        RewardSource.CANONICAL,"CANONICAL_MONSTER_REWARD",reward.expEvidence==null?null:reward.expEvidence.name()));
    trimRewardHistory();
  }

  /**
   * Direct-inventory endpoint for a reward whose item identity and quantity were already resolved upstream.
   * It deliberately refuses unknown items or quantities instead of creating a ground fallback.
   */
  public boolean isConsumable(String itemId){return B5_SMALL_HP_POTION_ITEM_ID.equals(itemId);}
  public UseResult useConsumable(String itemId,RuntimeState runtime){
    Integer owned=inventory.get(itemId);if(owned==null||owned<=0)return UseResult.ITEM_NOT_OWNED;
    if(!isConsumable(itemId)||runtime==null)return UseResult.NOT_CONSUMABLE;
    if(runtime.player().hp>=runtime.player().maxHp)return UseResult.NO_EFFECT;
    runtime.player().hp=Math.min(runtime.player().maxHp,runtime.player().hp+B5_SMALL_HP_POTION_HEAL);
    if(owned==1)inventory.remove(itemId);else inventory.put(itemId,owned-1);
    return UseResult.USED;
  }
  public boolean grantB5SmallHpPotion(int quantity){return autoLootResolvedItem(B5_SMALL_HP_POTION_ITEM_ID,quantity)==AutoLootResult.LOOTED;}
  public AutoLootResult autoLootResolvedItem(String itemId,int quantity){
    if(quantity<=0)return AutoLootResult.INVALID_QUANTITY;
    if(!items.containsKey(itemId))return AutoLootResult.INVALID_ITEM;
    int current=inventory.containsKey(itemId)?inventory.get(itemId):0;
    if(current>INVENTORY_STACK_LIMIT-quantity)return AutoLootResult.INVENTORY_FULL;
    inventory.put(itemId,current+quantity);
    return AutoLootResult.LOOTED;
  }

  /** Replace saved ownership exactly, including an explicitly empty equipment set. */
  public boolean restoreOwnedItems(Map<String,Integer> owned,Map<String,String> equipped){
    for(Map.Entry<String,Integer> e:owned.entrySet())
      if(!items.containsKey(e.getKey())||e.getValue()==null||e.getValue()<=0||e.getValue()>INVENTORY_STACK_LIMIT)return false;
    for(Map.Entry<String,String> e:equipped.entrySet()){
      ItemDefinition d=items.get(e.getValue());
      if(d==null||!d.equippable()||!e.getKey().equals(d.equipSlot)||!owned.containsKey(d.itemId))return false;
    }
    inventory.clear();inventory.putAll(owned);
    equipmentBySlot.clear();equipmentBySlot.putAll(equipped);
    // One-time playtest migration: newly introduced starter visuals are granted/equipped only
    // when absent from the saved inventory. Once owned, later user equip/unequip choices persist.
    grantStarterIfMissing("IT_SHOES","신발");
    grantStarterIfMissing(STARTER_HAT_ITEM_ID,"모자");
    grantStarterIfMissing(STARTER_SHIELD_ITEM_ID,"방패");
    return true;
  }
  private void grantStarterIfMissing(String itemId,String slot){
    if(inventory.containsKey(itemId))return;
    inventory.put(itemId,1);
    equipmentBySlot.put(slot,itemId);
  }
  public long consumedCombatSequence(){return lastCombatSequence;}
  public void restoreCombatSequence(long sequence){lastCombatSequence=Math.max(0L,sequence);rewardHistory.clear();}

  public EquipResult equip(String itemId){
    Integer owned=inventory.get(itemId);
    if(owned==null||owned<=0)return EquipResult.ITEM_NOT_OWNED;
    ItemDefinition def=items.get(itemId);
    if(def==null)return EquipResult.UNKNOWN_ITEM;
    if(!def.equippable())return EquipResult.NOT_EQUIPPABLE;
    if(itemId.equals(equipmentBySlot.get(def.equipSlot))){
      equipmentBySlot.remove(def.equipSlot);
      return EquipResult.UNEQUIPPED;
    }
    RequirementResult requirements=currentRequirements(itemId);
    if(requirements==RequirementResult.PENDING)return EquipResult.REQUIREMENT_PENDING;
    if(requirements!=RequirementResult.MET)return EquipResult.REQUIREMENT_NOT_MET;
    // Equipment slots stay source-facing (갑옷/각반). Visual coverage is a separate paper-doll concern:
    // FULL_BODY occupies both UPPER and LOWER coverage; UPPER + LOWER may coexist.
    CharacterVisualBinding.GarmentCoverage incoming=
        CharacterVisualBinding.garmentCoverageForAppearance(def.appearanceId);
    if(incoming==CharacterVisualBinding.GarmentCoverage.FULL_BODY){
      removeEquippedCoverage(CharacterVisualBinding.GarmentCoverage.UPPER);
      removeEquippedCoverage(CharacterVisualBinding.GarmentCoverage.LOWER);
    }else if(incoming==CharacterVisualBinding.GarmentCoverage.UPPER
        ||incoming==CharacterVisualBinding.GarmentCoverage.LOWER){
      removeEquippedCoverage(CharacterVisualBinding.GarmentCoverage.FULL_BODY);
    }
    equipmentBySlot.put(def.equipSlot,itemId);
    return EquipResult.EQUIPPED;
  }

  private void removeEquippedCoverage(CharacterVisualBinding.GarmentCoverage coverage){
    if(coverage==null)return;
    List<String> slots=new ArrayList<>();
    for(Map.Entry<String,String> e:equipmentBySlot.entrySet()){
      ItemDefinition equipped=items.get(e.getValue());
      if(equipped==null)continue;
      if(CharacterVisualBinding.garmentCoverageForAppearance(equipped.appearanceId)==coverage)slots.add(e.getKey());
    }
    for(String slot:slots)equipmentBySlot.remove(slot);
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