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
  public enum RewardSource { CANONICAL, ADAPTED_TEST, ADAPTED_CAMPAIGN, UNRESOLVED }
  public enum AutoLootResult { LOOTED, INVALID_ITEM, INVALID_QUANTITY, INVENTORY_FULL }
  public enum EquipResult { EQUIPPED, UNEQUIPPED, ITEM_NOT_OWNED, UNKNOWN_ITEM, NOT_EQUIPPABLE, INVALID_SLOT, REQUIREMENT_PENDING, REQUIREMENT_NOT_MET }
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

  public static final String RIGHT_GLOVE_SLOT="장갑(오른손)";
  public static final String RIGHT_RING_SLOT="반지(오른손)";
  public static boolean pairedSlot(String slot){return "장갑".equals(slot)||"반지".equals(slot);}
  public static String secondSlot(String slot){return "장갑".equals(slot)?RIGHT_GLOVE_SLOT:"반지".equals(slot)?RIGHT_RING_SLOT:null;}
  public static String baseSlot(String slot){return RIGHT_GLOVE_SLOT.equals(slot)?"장갑":RIGHT_RING_SLOT.equals(slot)?"반지":slot;}
  public static boolean acceptsSlot(String slot,ItemDefinition def){return def!=null&&def.equippable()&&def.equipSlot.equals(baseSlot(slot));}
  public static final String ARMOR_SLOT="갑옷";
  public static final String LOWER_GARMENT_SLOT="각반";
  public static final String WEAPON_SLOT="무기";
  public static final String HEAD_SLOT="모자";
  public static final String SHIELD_SLOT="방패";
  public static final String SHOES_SLOT="신발";

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
    public final long gold;
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
      this(combatSequence,monsterId,status,exp,autoLootedItems,itemOutcomes,source,policyId,evidence,0);
    }
    RewardResolution(long combatSequence,String monsterId,RewardStatus status,Integer exp,Map<String,Integer> autoLootedItems,Map<String,AutoLootResult> itemOutcomes,RewardSource source,String policyId,String evidence,long gold){
      this.gold=gold;
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
  private boolean equipmentSandbox;
  private final Map<String,Integer> inventory=new LinkedHashMap<>();
  private final Map<String,String> equipmentBySlot=new LinkedHashMap<>();
  private final Map<String,Integer> baseStats=new LinkedHashMap<>();
  private final List<RewardResolution> rewardHistory=new ArrayList<>();
  private final CanonicalMonsterRewardCatalog monsterRewards=new CanonicalMonsterRewardCatalog();
  private final AdaptedPrototypeRewardCatalog prototypeRewards=new AdaptedPrototypeRewardCatalog();
  private final AdaptedCampaignRewardCatalog campaignRewards=new AdaptedCampaignRewardCatalog();
  private long lastCombatSequence=0L;

  public static final String B_SMALL_POTION_ITEM_ID="IT_B_SMALL_POTION";
  public static final String REAGENT_KOMADIUM_ITEM_ID="IT_REAGENT_KOMADIUM";
  public static final String REAGENT_DIBENOMUM_ITEM_ID="IT_REAGENT_DIBENOMUM";
  public static final String REAGENT_CURANUM_ITEM_ID="IT_REAGENT_CURANUM";
  public static final String RECALL_MILLES_ITEM_ID="IT_RECALL_MILLES";
  public static final long B_SMALL_POTION_PRICE=20L;
  public static final String SHOP_MOKDO_ITEM_ID="IT_ADAPTED_PLAYTEST_MOKDO";
  public static final String REFERENCE_LEOPARD_ITEM_ID="IT_REFERENCE_LEOPARD";
  public static final String REFERENCE_HELM_ITEM_ID="IT_REFERENCE_HELM";
  public static final String CHUNGRYONG_ITEM_ID="IT_WEAPON_CHUNGRYONG";
  public static final String SHOP_LEATHER_GLOVE_ITEM_ID="IT_GLOVE_LEATHER";
  public static final String SHOP_SHOES_ITEM_ID="IT_SHOES";
  public static final String STARTER_SHIRT_ITEM_ID="IT_APPEARANCE_PEASANT_SHIRT";
  public static final String STARTER_HAT_ITEM_ID="IT_ADAPTED_STARTER_HAT";
  public static final String STARTER_HAT_APPEARANCE_ID="mh172";
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

  private final CampaignProgress campaign=new CampaignProgress();
  public CampaignProgress campaign(){return campaign;}
  private ProgressionNode progressionNode=ProgressionNode.COMMONER;
  private String currentJobCode="COMMONER";
  private Integer normalLevel=1;
  // EXP starts at zero and level-up uses only master/data/Level_EXP_Curve.csv thresholds.
  private Long normalExp=0L;
  private Long gold=0L;

  public RpgProgressionState(){
    Map<String,Integer> noStats=Collections.<String,Integer>emptyMap();
    Map<String,Integer> gloveStats=stats("AC",-1),leggingStats=stats("AC",-2),shoeStats=stats("DEX",1),shirtStats=stats("AC",-1),hatStats=stats("AC",-1),shieldStats=stats("AC",-2),mokdoStats=stats("DAM",3,"HIT",1);
    Set<String> anyJob=Collections.<String>emptySet();
    Set<String> physicalJobs=jobSet("WARRIOR","ROGUE","MARTIAL_ARTIST");
    Set<String> magicJobs=jobSet("MAGE","CLERIC");

    registerItem(new ItemDefinition("IT_GLOVE_LEATHER","가죽장갑","장갑",11,anyJob,true,null,null,gloveStats,Evidence.ADAPTED));
    registerItem(new ItemDefinition("IT_LEGGING_LEATHER","가죽각반","각반",11,anyJob,true,null,null,leggingStats,Evidence.O));
    registerItem(new ItemDefinition("IT_SHOES","신발",SHOES_SLOT,"ml228",1,anyJob,true,null,null,shoeStats,Evidence.ADAPTED));
    registerItem(new ItemDefinition(STARTER_HAT_ITEM_ID,"밀레스털모자",HEAD_SLOT,STARTER_HAT_APPEARANCE_ID,1,anyJob,true,null,null,hatStats,Evidence.ADAPTED));
    registerItem(new ItemDefinition(STARTER_SHIELD_ITEM_ID,"기본 방패",SHIELD_SLOT,STARTER_SHIELD_APPEARANCE_ID,1,anyJob,true,null,null,shieldStats,Evidence.ADAPTED));
    registerItem(new ItemDefinition("IT_EARRING_DOUBLE_SILVER","쌍은귀걸이","귀걸이",11,physicalJobs,true,null,null,stats("STR",1),Evidence.O));
    registerItem(new ItemDefinition("IT_RING_REDJADE","홍옥반지","반지",11,anyJob,true,null,null,stats("HP",100,"MP",50),Evidence.O));
    registerItem(new ItemDefinition("IT_RING_THREELINEGOLD","세줄금반지","반지",11,anyJob,true,null,null,stats("AC",-1,"HP",300,"MP",150,"MAGIC_DEFENSE",1),Evidence.O));
    registerItem(new ItemDefinition("IT_RING_GORU","고루반지","반지",11,magicJobs,true,null,null,stats("MP",150),Evidence.O));
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
    registerItem(new ItemDefinition(REAGENT_KOMADIUM_ITEM_ID,"코마디움",null,null,anyJob,true,null,null,noStats,Evidence.O));
    registerItem(new ItemDefinition(REAGENT_DIBENOMUM_ITEM_ID,"디베노뭄",null,null,anyJob,true,null,null,noStats,Evidence.O));
    registerItem(new ItemDefinition(REAGENT_CURANUM_ITEM_ID,"쿠라눔",null,null,anyJob,true,null,null,noStats,Evidence.O));
    registerItem(new ItemDefinition("IT_SHOP_ARMOR_JIPON","지폰",ARMOR_SLOT,"mu0000007",11,jobSet("WARRIOR"),true,null,null,stats("AC",-3),Evidence.ADAPTED));
    registerItem(new ItemDefinition("IT_B_MP_POTION","마나 회복약",null,null,anyJob,true,null,null,noStats,Evidence.ADAPTED));
    registerItem(new ItemDefinition("IT_REAGENT_CURUM","쿠룸",null,null,anyJob,true,null,null,noStats,Evidence.ADAPTED));
    registerItem(new ItemDefinition("IT_REAGENT_EXCURANUM","엑스쿠라눔",null,null,anyJob,true,null,null,noStats,Evidence.O));
    registerItem(new ItemDefinition("IT_REAGENT_HOLYWATER","성수",null,null,anyJob,true,null,null,noStats,Evidence.ADAPTED));
    registerItem(new ItemDefinition("IT_SHOP_WEAPON_MW004","세이버",WEAPON_SLOT,"mw004",AnimationAction.SWING,11,anyJob,true,null,null,stats("DAM",7,"HIT",2),Evidence.ADAPTED));
    registerItem(new ItemDefinition("IT_SHOP_WEAPON_MW005","그라디우스",WEAPON_SLOT,"mw005",AnimationAction.SWING,11,anyJob,true,null,null,stats("DAM",9,"HIT",1),Evidence.ADAPTED));
    registerItem(new ItemDefinition(RECALL_MILLES_ITEM_ID,"밀레스리콜",null,null,anyJob,true,null,null,noStats,Evidence.O));
    registerItem(new ItemDefinition(STARTER_SHIRT_ITEM_ID,"셔츠 [PENDING WEARABLE FRAMES]",ARMOR_SLOT,
        STARTER_SHIRT_APPEARANCE_ID,1,anyJob,true,null,null,shirtStats,Evidence.ADAPTED));
    registerItem(new ItemDefinition(PLAYTEST_WEAPON_ITEM_ID,"목도 [ADAPTED PLAYTEST]",WEAPON_SLOT,
        PLAYTEST_WEAPON_APPEARANCE_ID,AnimationAction.SWING,1,anyJob,true,null,null,mokdoStats,Evidence.ADAPTED));
    registerItem(new ItemDefinition("IT_TEST_WEAPON_MW002","에페",WEAPON_SLOT,"mw002",AnimationAction.SWING,1,anyJob,true,null,null,stats("DAM",4,"HIT",1),Evidence.ADAPTED));
    registerItem(new ItemDefinition("IT_TEST_WEAPON_MW003","커틀라스",WEAPON_SLOT,"mw003",AnimationAction.SWING,1,anyJob,true,null,null,stats("DAM",5),Evidence.ADAPTED));
    registerItem(new ItemDefinition(REFERENCE_LEOPARD_ITEM_ID,"레오파드",ARMOR_SLOT,"mu0000180",1,anyJob,true,null,null,noStats,Evidence.ADAPTED));
    registerItem(new ItemDefinition(REFERENCE_HELM_ITEM_ID,"헬름",HEAD_SLOT,"mh168",1,anyJob,true,null,null,noStats,Evidence.ADAPTED));
    registerItem(new ItemDefinition(CHUNGRYONG_ITEM_ID,"청룡의숨결",WEAPON_SLOT,"mw_chungryong",AnimationAction.SWING,1,anyJob,true,null,null,noStats,Evidence.ADAPTED));
    registerItem(new ItemDefinition("IT_TEST_SHOES_ML229","가죽 신발",SHOES_SLOT,"ml229",1,anyJob,true,null,null,stats("DEX",2),Evidence.ADAPTED));
    registerItem(new ItemDefinition("IT_TEST_SHOES_ML230","신발 ml230",SHOES_SLOT,"ml230",1,anyJob,true,null,null,stats("DEX",3),Evidence.ADAPTED));
    registerItem(new ItemDefinition("IT_TEST_SHIELD_MS002","방패 ms002",SHIELD_SLOT,"ms002",1,anyJob,true,null,null,stats("AC",-3),Evidence.ADAPTED));
    registerItem(new ItemDefinition("IT_TEST_SHIELD_MS003","방패 ms003",SHIELD_SLOT,"ms003",1,anyJob,true,null,null,stats("AC",-4),Evidence.ADAPTED));
    registerItem(new ItemDefinition("IT_TEST_HAT_MH173","아벨털모자",HEAD_SLOT,"mh173",1,anyJob,true,null,null,stats("AC",-2),Evidence.ADAPTED));
    registerItem(new ItemDefinition("IT_TEST_HAT_MH174","루어스털모자",HEAD_SLOT,"mh174",1,anyJob,true,null,null,stats("AC",-2,"DEX",1),Evidence.ADAPTED));
    registerItem(new ItemDefinition("IT_TEST_ARMOR_MU0000002","레더튜닉",ARMOR_SLOT,"mu0000002",1,anyJob,true,null,null,stats("AC",-2),Evidence.ADAPTED));
    registerItem(new ItemDefinition("IT_TEST_ARMOR_MU0000003","도복",ARMOR_SLOT,"mu0000003",1,anyJob,true,null,null,stats("AC",-2,"DEX",1),Evidence.ADAPTED));
    registerItem(new ItemDefinition("IT_B_JOB_WARRIOR_TUNIC","전사 수련 튜닉 [ADAPTED]",ARMOR_SLOT,"mu0000007",1,jobSet("WARRIOR"),true,null,null,stats("AC",-2),Evidence.ADAPTED));
    registerItem(new ItemDefinition("IT_B_JOB_ROGUE_GARMENT","도적 수련복 [ADAPTED]",ARMOR_SLOT,"mu0000055",1,jobSet("ROGUE"),true,null,null,stats("AC",-1,"DEX",1),Evidence.ADAPTED));
    registerItem(new ItemDefinition("IT_B_JOB_MAGE_ROBE","마법사 수련 로브 [ADAPTED]",ARMOR_SLOT,"mu0000058",1,jobSet("MAGE"),true,null,null,stats("AC",-1,"MP",20),Evidence.ADAPTED));
    registerItem(new ItemDefinition("IT_B_JOB_CLERIC_ROBE","성직자 수련 로브 [ADAPTED]",ARMOR_SLOT,"mu0000015",1,jobSet("CLERIC"),true,null,null,stats("AC",-1,"MP",20),Evidence.ADAPTED));
    registerItem(new ItemDefinition("IT_B_JOB_MONK_GI","무도가 수련 도복 [ADAPTED]",ARMOR_SLOT,"mu0000057",1,jobSet("MARTIAL_ARTIST"),true,null,null,stats("AC",-2,"DEX",1),Evidence.ADAPTED));
    registerItem(new ItemDefinition("IT_B_JOB_WARRIOR_WEAPON","경비대 수련검 [ADAPTED]",WEAPON_SLOT,"mw015",AnimationAction.SWING,1,jobSet("WARRIOR"),true,null,null,stats("DAM",4),Evidence.ADAPTED));
    registerItem(new ItemDefinition("IT_B_JOB_ROGUE_WEAPON","정찰자 단검 [ADAPTED]",WEAPON_SLOT,"mw024",AnimationAction.THRUST,1,jobSet("ROGUE"),true,null,null,stats("DAM",4,"HIT",1),Evidence.ADAPTED));
    registerItem(new ItemDefinition("IT_B_JOB_MAGE_WAND","수련 지팡이 [ADAPTED]",WEAPON_SLOT,"mw024",AnimationAction.THRUST,1,jobSet("MAGE"),true,null,null,stats("DAM",3),Evidence.ADAPTED));
    registerItem(new ItemDefinition("IT_B_JOB_CLERIC_WAND","성직 완드 [ADAPTED]",WEAPON_SLOT,"mw026",AnimationAction.THRUST,1,jobSet("CLERIC"),true,null,null,stats("DAM",2),Evidence.ADAPTED));
    // Playable visual-slice fixture: source-named appearance, no invented stats or reward relation.
    inventory.put(STARTER_SHIRT_ITEM_ID,1);
    // Starter armor uses the verified classic paper-doll garment frames while retaining the canonical shirt item identity.
    equipmentBySlot.put(ARMOR_SLOT,STARTER_SHIRT_ITEM_ID);
    inventory.put(PLAYTEST_WEAPON_ITEM_ID,1);
    equipmentBySlot.put(WEAPON_SLOT,PLAYTEST_WEAPON_ITEM_ID);
    inventory.put("IT_SHOES",1);inventory.put(STARTER_HAT_ITEM_ID,1);inventory.put(STARTER_SHIELD_ITEM_ID,1);
    inventory.put("IT_TEST_WEAPON_MW002",1);inventory.put("IT_TEST_WEAPON_MW003",1);
    registerCampaignEquipment();
    SourceAccessoryCatalog.register(this);
    SourceWardrobeCatalog.install(this);
    SourceItemFunctions.install(this);
    SourceEquipmentPerformance.install(this);
    inventory.put(CHUNGRYONG_ITEM_ID,1);
    inventory.put(REFERENCE_LEOPARD_ITEM_ID,1);inventory.put(REFERENCE_HELM_ITEM_ID,1);
    inventory.put("IT_TEST_SHOES_ML229",1);inventory.put("IT_TEST_SHOES_ML230",1);
    inventory.put("IT_TEST_SHIELD_MS002",1);inventory.put("IT_TEST_SHIELD_MS003",1);
    inventory.put("IT_TEST_HAT_MH173",1);inventory.put("IT_TEST_HAT_MH174",1);
    inventory.put("IT_TEST_ARMOR_MU0000002",1);inventory.put("IT_TEST_ARMOR_MU0000003",1);
    equipmentBySlot.put(SHOES_SLOT,"IT_SHOES");equipmentBySlot.put(HEAD_SLOT,STARTER_HAT_ITEM_ID);equipmentBySlot.put(SHIELD_SLOT,STARTER_SHIELD_ITEM_ID);
  }

  private void registerCampaignEquipment(){
    registerItem(new ItemDefinition("IT_B_POTE_SHOES","숲길 신발",SHOES_SLOT,"ml230",20,jobSet(),true,null,null,stats("AC",-1,"DEX",3),Evidence.ADAPTED));
    registerItem(new ItemDefinition("IT_B_PURIFIED_ESSENCE","정화된 숲의 정수",null,null,0,jobSet(),true,null,null,stats(),Evidence.ADAPTED));
    String[] looks={"mu0000059","mu0000055","mu0000117","mu0000210","mu0000057"};
    for(int i=0;i<CampaignProgress.JOBS.length;i++)for(int level:new int[]{11,26}){
      String job=CampaignProgress.JOBS[i],id="IT_B_CAMPAIGN_"+job+"_"+level;
      registerItem(new ItemDefinition(id,CampaignProgress.jobName(job)+(level==11?" 견습 장비":" 숙련 장비"),ARMOR_SLOT,looks[i],level,jobSet(job),true,null,null,stats("AC",level==11?-5:-10,"DAM",level==11?3:7,"HP",level==11?80:180,"MP",level==11?80:180),Evidence.ADAPTED));
      String starter="WARRIOR".equals(job)?"IT_B_JOB_WARRIOR_WEAPON":"ROGUE".equals(job)?"IT_B_JOB_ROGUE_WEAPON":"MAGE".equals(job)?"IT_B_JOB_MAGE_WAND":"IT_B_JOB_CLERIC_WAND";
      ItemDefinition base=items.get(starter);boolean monk="MARTIAL_ARTIST".equals(job),magic="MAGE".equals(job)||"CLERIC".equals(job);
      registerItem(new ItemDefinition("IT_B_CAMPAIGN_TOOL_"+job+"_"+level,CampaignProgress.jobName(job)+(level==11?" 수련 도구":" 숙련 도구"),monk?"장갑":WEAPON_SLOT,monk?null:base.appearanceId,monk?null:base.basicAttackAction,level,jobSet(job),true,null,null,stats("DAM",level==11?4:9,magic?"INT":"STR",level==11?3:6,magic?"WIS":"CON",level==11?2:4),Evidence.ADAPTED));
    }
  }
  public boolean grantCampaignGear(int level){String id="IT_B_CAMPAIGN_"+currentJobCode+"_"+level;if(!items.containsKey(id)||autoLootResolvedItem(id,1)!=AutoLootResult.LOOTED)return false;equipmentBySlot.put(ARMOR_SLOT,id);return true;}
  private static Set<String> jobSet(String... jobs){return new LinkedHashSet<>(Arrays.asList(jobs));}
  private static Map<String,Integer> stats(Object... kv){Map<String,Integer> out=new LinkedHashMap<>();for(int i=0;i+1<kv.length;i+=2)out.put((String)kv[i],(Integer)kv[i+1]);return out;}
  private void registerItem(ItemDefinition def){
    Map<String,Integer> source=SourceEquipmentStats.forItem(def.itemId);
    if(source!=null)def=new ItemDefinition(def.itemId,def.name,def.equipSlot,def.appearanceId,def.basicAttackAction,def.requiredLevel,def.allowedJobCodes,def.jobRestrictionResolved,def.attackElement,def.defenseElement,source,Evidence.ADAPTED);
    items.put(def.itemId,def);
  }
  // Historical fields fill gaps; accepted canonical fields and original requirements win collisions.
  void registerSourceAccessory(ItemDefinition def){
    ItemDefinition old=items.get(def.itemId);
    if(old==null){items.put(def.itemId,def);return;}
    Map<String,Integer> merged=new LinkedHashMap<>(def.statModifiers);merged.putAll(old.statModifiers);
    Map<String,Integer> canonical=SourceEquipmentStats.forItem(def.itemId);if(canonical!=null)merged.putAll(canonical);
    items.put(def.itemId,new ItemDefinition(old.itemId,old.name,old.equipSlot,old.appearanceId,old.basicAttackAction,old.requiredLevel,old.allowedJobCodes,old.jobRestrictionResolved,old.attackElement,old.defenseElement,merged,Evidence.ADAPTED));
  }
  void fillSourceOptions(String id,Map<String,Integer> values,Integer level){
    ItemDefinition d=items.get(id);if(d==null||d.evidence!=Evidence.PENDING)return;
    items.put(id,new ItemDefinition(id,d.name,d.equipSlot,d.appearanceId,d.basicAttackAction,level,d.allowedJobCodes,d.jobRestrictionResolved,d.attackElement,d.defenseElement,values,Evidence.V));
  }
  /** Reviewed ranges merge without erasing existing DAM, effects or stable save identities. */
  void installEquipmentPerformance(String id,Map<String,Integer> values,Integer sourceLevel,Evidence evidence){
    ItemDefinition d=items.get(id);if(d==null)throw new IllegalArgumentException("Unknown performance item "+id);
    Map<String,Integer> m=new LinkedHashMap<>(d.statModifiers);m.putAll(values);
    items.put(id,new ItemDefinition(id,d.name,d.equipSlot,d.appearanceId,d.basicAttackAction,d.requiredLevel!=null?d.requiredLevel:sourceLevel,d.allowedJobCodes,d.jobRestrictionResolved,d.attackElement,d.defenseElement,m,evidence));
  }
  void fillSourceLevel(String id,int level){
    ItemDefinition d=items.get(id);if(d==null||d.requiredLevel!=null)return;
    items.put(id,new ItemDefinition(id,d.name,d.equipSlot,d.appearanceId,d.basicAttackAction,level,d.allowedJobCodes,d.jobRestrictionResolved,d.attackElement,d.defenseElement,d.statModifiers,d.evidence));
  }
  void addSourceOptions(String id,Map<String,Integer> values){
    ItemDefinition d=items.get(id);if(d==null)return;Map<String,Integer> m=new LinkedHashMap<>(d.statModifiers);m.putAll(values);
    items.put(id,new ItemDefinition(id,d.name,d.equipSlot,d.appearanceId,d.basicAttackAction,d.requiredLevel,d.allowedJobCodes,d.jobRestrictionResolved,d.attackElement,d.defenseElement,m,d.evidence));
  }
  /** Identity correction keeps stable save IDs and established numeric modifiers. */
  void rebindSourceIdentity(String id,String name,String appearance){
    ItemDefinition d=items.get(id);if(d==null)return;
    items.put(id,new ItemDefinition(id,name,d.equipSlot,appearance,d.basicAttackAction,d.requiredLevel,d.allowedJobCodes,d.jobRestrictionResolved,d.attackElement,d.defenseElement,d.statModifiers,d.evidence));
  }
  public boolean equipmentSandbox(){return equipmentSandbox;}
  public void enableEquipmentSandbox(boolean grant){
    equipmentSandbox=true;
    if(grant)for(ItemDefinition d:items.values())inventory.put(d.itemId,Math.max(pairedSlot(d.equipSlot)?2:1,inventory.getOrDefault(d.itemId,0)));
  }
  public Map<String,ItemDefinition> itemDefinitions(){return Collections.unmodifiableMap(items);}
  public Map<String,Integer> inventory(){return Collections.unmodifiableMap(inventory);}
  public Map<String,String> equipment(){return Collections.unmodifiableMap(equipmentBySlot);}
  public ItemDefinition equippedDefinition(String slot){
    String itemId=equipmentBySlot.get(slot);return itemId==null?null:items.get(itemId);
  }
  public List<RewardResolution> rewardHistory(){return Collections.unmodifiableList(rewardHistory);}
  public ProgressionNode progressionNode(){return progressionNode;}
  public String currentJobCode(){return currentJobCode;}
  public boolean restoreJobCode(String code){
    if(code==null||!("COMMONER".equals(code)||"WARRIOR".equals(code)||"ROGUE".equals(code)||"MAGE".equals(code)||"CLERIC".equals(code)||"MARTIAL_ARTIST".equals(code)))return false;
    if("COMMONER".equals(code)){currentJobCode=code;progressionNode=ProgressionNode.COMMONER;return true;}
    if(normalLevel==null||normalLevel<3)return false;
    currentJobCode=code;progressionNode=ProgressionNode.BASIC_JOB;return true;
  }
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
  public boolean canResetIntroAllocation(SkillBook book){String id=campaign.activeId();return !campaign.legacy()&&("T01".equals(id)||"T02".equals(id))&&book!=null&&!book.owned(CampaignProgress.beginnerSkill(currentJobCode))&&str+intel+wis+con+dex>15;}
  /** Explicit beginner correction only: refund allocated points, retain levels, rewards and HP/MP growth. */
  public boolean resetIntroAllocation(SkillBook book){if(!canResetIntroAllocation(book))return false;int spent=str+intel+wis+con+dex-15;if(spent<=0||statPoints>Integer.MAX_VALUE-spent)return false;str=intel=wis=con=dex=3;statPoints+=spent;return true;}
  public boolean spendStat(String stat){if(statPoints<=0)return false;if("STR".equals(stat))str++;else if("INT".equals(stat))intel++;else if("WIS".equals(stat))wis++;else if("CON".equals(stat))con++;else if("DEX".equals(stat))dex++;else return false;statPoints--;campaign.record("STAT",stat);return true;}
  public void restoreStats(int s,int i,int w,int c,int d,int points){str=Math.max(3,s);intel=Math.max(3,i);wis=Math.max(3,w);con=Math.max(3,c);dex=Math.max(3,d);statPoints=Math.max(0,points);}
  private long bankGold;
  private final Map<String,Integer> bankInventory=new LinkedHashMap<>();
  public long bankGold(){return bankGold;}
  public Map<String,Integer> bankInventory(){return Collections.unmodifiableMap(bankInventory);}
  public boolean restoreBank(long balance,Map<String,Integer> deposited){
    if(balance<0||deposited==null)return false;
    for(Map.Entry<String,Integer> e:deposited.entrySet())if(!items.containsKey(e.getKey())||e.getValue()==null||e.getValue()<=0||e.getValue()>INVENTORY_STACK_LIMIT)return false;
    bankGold=balance;bankInventory.clear();bankInventory.putAll(deposited);return true;
  }
  public void restoreGold(long value){gold=Math.max(0L,value);}
  /** First-job choice is a one-time, cost-free mobile campaign transaction. */
  public boolean chooseInitialJob(String jobCode){
    if(normalLevel==null||normalLevel<3||!"COMMONER".equals(currentJobCode))return false;
    if(!("WARRIOR".equals(jobCode)||"ROGUE".equals(jobCode)||"MAGE".equals(jobCode)||"CLERIC".equals(jobCode)||"MARTIAL_ARTIST".equals(jobCode)))return false;
    currentJobCode=jobCode;
    progressionNode=ProgressionNode.BASIC_JOB;
    String weapon="WARRIOR".equals(jobCode)?"IT_B_JOB_WARRIOR_WEAPON":"ROGUE".equals(jobCode)?"IT_B_JOB_ROGUE_WEAPON":"MAGE".equals(jobCode)?"IT_B_JOB_MAGE_WAND":"CLERIC".equals(jobCode)?"IT_B_JOB_CLERIC_WAND":null;
    String armor="WARRIOR".equals(jobCode)?"IT_B_JOB_WARRIOR_TUNIC":"ROGUE".equals(jobCode)?"IT_B_JOB_ROGUE_GARMENT":"MAGE".equals(jobCode)?"IT_B_JOB_MAGE_ROBE":"CLERIC".equals(jobCode)?"IT_B_JOB_CLERIC_ROBE":"IT_B_JOB_MONK_GI";
    if(weapon==null)equipmentBySlot.remove(WEAPON_SLOT);else{inventory.put(weapon,Math.max(1,value(inventory,weapon)));equipmentBySlot.put(WEAPON_SLOT,weapon);}
    inventory.put(armor,Math.max(1,value(inventory,armor)));equipmentBySlot.put(ARMOR_SLOT,armor);
    return true;
  }
  public boolean buySmallPotion(){return buyItem(B_SMALL_POTION_ITEM_ID,B_SMALL_POTION_PRICE);}
  public boolean buyItem(String itemId,long price){
    if(price<0||gold<price||!items.containsKey(itemId))return false;
    AutoLootResult added=autoLootResolvedItem(itemId,1);
    if(added!=AutoLootResult.LOOTED)return false;
    gold-=price;campaign.bought(itemId);return true;
  }
  /** Atomic project skill-acquisition payment: validate every cost before mutating any balance. */
  public boolean paySkillLearningCost(long price,Map<String,Integer> materials){
    if(price<0||gold<price||materials==null)return false;
    for(Map.Entry<String,Integer> e:materials.entrySet()){
      ItemDefinition d=items.get(e.getKey());Integer n=e.getValue();
      if(d==null||d.equippable()||n==null||n<=0||value(inventory,e.getKey())<n)return false;
    }
    gold-=price;
    for(Map.Entry<String,Integer> e:materials.entrySet()){int left=value(inventory,e.getKey())-e.getValue();if(left==0)inventory.remove(e.getKey());else inventory.put(e.getKey(),left);}
    return true;
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

  public RequirementResult currentRequirements(String itemId){if(equipmentSandbox&&items.containsKey(itemId)&&items.get(itemId).equippable())return RequirementResult.MET;return evaluateRequirements(itemId,currentJobCode,normalLevel);}

  /**
   * Consumes combat events exactly once. Unknown monster reward mapping produces an explicit PENDING result.
   * Resolved item rewards are inserted directly into inventory; no world-drop entity or pickup phase exists.
   */
  public void consumeCombat(List<CombatLedger.Event> events,RuntimeState runtime){
    for(CombatLedger.Event e:events){
      if(e.sequence<=lastCombatSequence)continue;
      lastCombatSequence=e.sequence;
      campaign.consume(e,runtime);
      if(e.type!=CombatLedger.Type.MONSTER_DEFEATED)continue;
      String campaignId=runtime==null?null:runtime.campaignRewardProfileFor(e.targetId);
      resolveCampaignDefeat(e,campaignId);
    }
  }

  private void resolveMonsterDefeat(CombatLedger.Event e){
    resolveMonsterDefeat(e,monsterRewards.find(e.targetId));
  }

  private void resolveMonsterDefeat(CombatLedger.Event e,CanonicalMonsterRewardCatalog.RewardEntry reward){
    if(reward==null){
      // An explicit actor-level adapter is required. The canonical POTE_PURPLE fixture and
      // every unprofiled defeat remain unresolved, preserving the fail-closed boundary audit.
      // (The runtime supplies its profile through the overload below.)
      AdaptedPrototypeRewardCatalog.Entry prototype=prototypeRewards.find(e.targetId);
      if(prototype!=null){
        Map<String,Integer> granted=new LinkedHashMap<>();
        Map<String,AutoLootResult> outcomes=new LinkedHashMap<>();
        AutoLootResult result=autoLootResolvedItem(prototype.itemId,prototype.quantity);
        outcomes.put(prototype.itemId,result);
        if(result==AutoLootResult.LOOTED)granted.put(prototype.itemId,prototype.quantity);
        grantAdaptedReward(AdaptedPrototypeRewardCatalog.TRAINING_MONSTER_EXP,AdaptedPrototypeRewardCatalog.TRAINING_MONSTER_GOLD);
        rewardHistory.add(new RewardResolution(e.sequence,e.targetId,RewardStatus.RESOLVED,AdaptedPrototypeRewardCatalog.TRAINING_MONSTER_EXP,granted,outcomes,
            RewardSource.ADAPTED_TEST,prototype.policyId,prototype.evidence,AdaptedPrototypeRewardCatalog.TRAINING_MONSTER_GOLD));
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

  /** Campaign-aware defeat endpoint; only map actors admitted by the mobile field adapter can resolve. */
  private void resolveCampaignDefeat(CombatLedger.Event e,String profileId){
    // Same-HP campaign spirits use the mobile economy; preserve the V source record separately.
    if("POTE_SPIRIT".equals(profileId)&&profileId.equals(PoteForestMonsterShowcase.species(e.targetId))){
      applyCampaignReward(e,campaignRewards.find(profileId));return;
    }
    CanonicalMonsterRewardCatalog.RewardEntry canonical=monsterRewards.find(e.targetId);
    // Admit instance rewards only through a matching live actor species profile.
    if(canonical==null&&profileId!=null&&profileId.equals(PoteForestMonsterShowcase.species(e.targetId)))canonical=monsterRewards.find(profileId);
    if(canonical!=null){resolveMonsterDefeat(e,canonical);return;}
    AdaptedCampaignRewardCatalog.Reward reward=campaignRewards.find(profileId);
    if(reward==null){resolveMonsterDefeat(e);return;}
    applyCampaignReward(e,reward);
  }
  private void applyCampaignReward(CombatLedger.Event e,AdaptedCampaignRewardCatalog.Reward reward){
    grantAdaptedReward(reward.exp,reward.gold);
    rewardHistory.add(new RewardResolution(e.sequence,e.targetId,RewardStatus.RESOLVED,reward.exp,
        Collections.<String,Integer>emptyMap(),Collections.<String,AutoLootResult>emptyMap(),
        RewardSource.ADAPTED_CAMPAIGN,AdaptedCampaignRewardCatalog.POLICY_ID,"B+ADAPTED; zone="+reward.zone,reward.gold));
    trimRewardHistory();
  }

  /**
   * Direct-inventory endpoint for a reward whose item identity and quantity were already resolved upstream.
   * It deliberately refuses unknown items or quantities instead of creating a ground fallback.
   */
  public boolean isConsumable(String itemId){return ItemEffects.usable(itemId);}
  public UseResult useQuickConsumable(String id,RuntimeState runtime){UseResult result=useConsumable(id,runtime);if(result==UseResult.USED)campaign.quickUse(id,this);return result;}
  public UseResult useConsumable(String itemId,RuntimeState runtime){
    Integer owned=inventory.get(itemId);if(owned==null||owned<=0)return UseResult.ITEM_NOT_OWNED;
    if(!isConsumable(itemId)||runtime==null)return UseResult.NOT_CONSUMABLE;
    RuntimeState.Player p=runtime.player();if(!p.alive||p.hp<=0)return UseResult.NO_EFFECT;
    if(REAGENT_DIBENOMUM_ITEM_ID.equals(itemId)){
      if(!runtime.skillEffects().has("player","POISON"))return UseResult.NO_EFFECT;
      runtime.skillEffects().remove("player","POISON");
    }else{
      int hp=ItemEffects.healHp(itemId),mp=ItemEffects.healMp(itemId);
      if(hp>0&&p.hp>=p.maxHp||mp>0&&p.mp>=p.maxMp)return UseResult.NO_EFFECT;
      if(hp>0)p.hp=(int)Math.min(p.maxHp,(long)p.hp+hp);
      if(mp>0)p.mp=(int)Math.min(p.maxMp,(long)p.mp+mp);
    }
    if(owned==1)inventory.remove(itemId);else inventory.put(itemId,owned-1);
    campaign.healed(itemId);return UseResult.USED;
  }
  public boolean isRecall(String id){return RECALL_MILLES_ITEM_ID.equals(id);}
  public UseResult useMillesRecall(RuntimeState runtime){int owned=inventory.getOrDefault(RECALL_MILLES_ITEM_ID,0);if(owned<=0)return UseResult.ITEM_NOT_OWNED;if(runtime==null||!runtime.player().alive)return UseResult.NO_EFFECT;if(owned==1)inventory.remove(RECALL_MILLES_ITEM_ID);else inventory.put(RECALL_MILLES_ITEM_ID,owned-1);return UseResult.USED;}
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
    Map<String,Integer> counts=new LinkedHashMap<>();
    for(Map.Entry<String,String> e:equipped.entrySet()){
      ItemDefinition d=items.get(e.getValue());
      if(!acceptsSlot(e.getKey(),d)||!owned.containsKey(d.itemId))return false;
      int count=counts.getOrDefault(d.itemId,0)+1;counts.put(d.itemId,count);
      if(count>owned.get(d.itemId))return false;
    }
    Map<String,Integer> ownCopy=new LinkedHashMap<>(owned);Map<String,String> equipCopy=new LinkedHashMap<>(equipped);
    inventory.clear();inventory.putAll(ownCopy);
    equipmentBySlot.clear();equipmentBySlot.putAll(equipCopy);
    return true;
  }
  public long consumedCombatSequence(){return lastCombatSequence;}
  public void restoreCombatSequence(long sequence){lastCombatSequence=Math.max(0L,sequence);rewardHistory.clear();}

  /** Inventory chooses an available hand; explicit slot actions can replace/remove either hand. */
  public EquipResult equip(String itemId){
    ItemDefinition def=items.get(itemId);if(def==null)return inventory.containsKey(itemId)?EquipResult.UNKNOWN_ITEM:EquipResult.ITEM_NOT_OWNED;
    String slot=def.equipSlot;
    if(pairedSlot(slot)){
      String second=secondSlot(slot);int count=equippedCount(itemId);
      if(count>0&&inventory.getOrDefault(itemId,0)<=count){
        return unequip(itemId.equals(equipmentBySlot.get(second))?second:slot);
      }
      if(equipmentBySlot.containsKey(slot)&&!equipmentBySlot.containsKey(second))slot=second;
      else if(equipmentBySlot.containsKey(slot)&&equipmentBySlot.containsKey(second)&&count>0)return unequip(itemId.equals(equipmentBySlot.get(second))?second:slot);
    }
    return equipToSlot(itemId,slot);
  }
  public int equippedCount(String id){int n=0;for(String value:equipmentBySlot.values())if(value.equals(id))n++;return n;}
  public EquipResult unequip(String slot){return equipmentBySlot.remove(slot)==null?EquipResult.UNKNOWN_ITEM:EquipResult.UNEQUIPPED;}
  public EquipResult equipToSlot(String itemId,String slot){
    Integer owned=inventory.get(itemId);
    if(owned==null||owned<=0)return EquipResult.ITEM_NOT_OWNED;
    ItemDefinition def=items.get(itemId);
    if(def==null)return EquipResult.UNKNOWN_ITEM;
    if(!def.equippable())return EquipResult.NOT_EQUIPPABLE;
    if(!acceptsSlot(slot,def))return EquipResult.INVALID_SLOT;
    if(itemId.equals(equipmentBySlot.get(slot))){
      equipmentBySlot.remove(slot);
      return EquipResult.UNEQUIPPED;
    }
    if(equippedCount(itemId)>=owned)return EquipResult.ITEM_NOT_OWNED;
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
    equipmentBySlot.put(slot,itemId);
    campaign.record("EQUIP",itemId);return EquipResult.EQUIPPED;
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
