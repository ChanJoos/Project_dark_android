package com.projectdark.mobile;

/**
 * Isolated regression checks for inventory selection and equipment requirement behavior.
 * The audit creates its own RPG state and never mutates the live player state.
 */
public final class RpgEquipmentInteractionAudit {
  private RpgEquipmentInteractionAudit(){}

  public static boolean verify(){
    RpgProgressionState rpg=new RpgProgressionState();
    RpgInteractionController controller=new RpgInteractionController();

    if(!Integer.valueOf(1).equals(rpg.inventory().get(RpgProgressionState.PLAYTEST_ROBE_ITEM_ID)))return false;
    RpgProgressionState.ItemDefinition robe=rpg.itemDefinitions().get(RpgProgressionState.PLAYTEST_ROBE_ITEM_ID);
    if(robe==null||!RpgProgressionState.PLAYTEST_ROBE_APPEARANCE_ID.equals(robe.appearanceId))return false;
    if(!controller.selectInventoryItem(rpg,RpgProgressionState.PLAYTEST_ROBE_ITEM_ID))return false;
    if(controller.equipSelectedDetailed(rpg)!=RpgProgressionState.EquipResult.EQUIPPED)return false;
    CharacterVisualBinding equippedVisual=CharacterVisualBinding.from(rpg);
    if(!equippedVisual.hasResolvedSpriteAssets())return false;
    if(!CharacterVisualBinding.RESOLVED_ROBE_APPEARANCE_ID.equals(equippedVisual.equipmentVisualRef()))return false;
    if(controller.equipSelectedDetailed(rpg)!=RpgProgressionState.EquipResult.UNEQUIPPED)return false;
    if(!rpg.equipment().isEmpty()||CharacterVisualBinding.from(rpg).hasResolvedSpriteAssets())return false;

    if(rpg.normalLevel()==null||rpg.normalLevel()!=1)return false;
    if(controller.selectInventoryItem(rpg,"IT_GLOVE_LEATHER"))return false;
    if(controller.selectedInventoryItemId()!=null)return false;

    if(rpg.autoLootResolvedItem("IT_GLOVE_LEATHER",1)!=RpgProgressionState.AutoLootResult.LOOTED)return false;
    if(!controller.selectInventoryItem(rpg,"IT_GLOVE_LEATHER"))return false;
    if(!"IT_GLOVE_LEATHER".equals(controller.selectedInventoryItemId()))return false;

    RpgProgressionState.EquipResult blocked=controller.equipSelectedDetailed(rpg);
    if(blocked!=RpgProgressionState.EquipResult.REQUIREMENT_NOT_MET)return false;
    if(!rpg.equipment().isEmpty())return false;
    if(!controller.equipmentReferencesOwnedItems(rpg))return false;

    RpgProgressionState.StatSnapshot stats=controller.statSnapshot(rpg);
    if(stats==null||!stats.equipment.isEmpty())return false;

    RpgProgressionState.ItemDefinition physical=rpg.itemDefinitions().get("IT_EARRING_DOUBLE_SILVER");
    if(physical==null||!physical.jobRestrictionResolved)return false;
    if(!physical.allowedJobCodes.contains("WARRIOR")||!physical.allowedJobCodes.contains("ROGUE")||!physical.allowedJobCodes.contains("MARTIAL_ARTIST"))return false;
    if(physical.allowedJobCodes.size()!=3)return false;
    if(rpg.evaluateRequirements("IT_EARRING_DOUBLE_SILVER","WARRIOR",11)!=RpgProgressionState.RequirementResult.MET)return false;
    if(rpg.evaluateRequirements("IT_EARRING_DOUBLE_SILVER","MAGE",11)!=RpgProgressionState.RequirementResult.JOB_NOT_MET)return false;

    RpgProgressionState.ItemDefinition magic=rpg.itemDefinitions().get("IT_RING_GORU");
    if(magic==null||!magic.jobRestrictionResolved)return false;
    if(!magic.allowedJobCodes.contains("MAGE")||!magic.allowedJobCodes.contains("CLERIC")||magic.allowedJobCodes.size()!=2)return false;
    if(rpg.evaluateRequirements("IT_RING_GORU","CLERIC",11)!=RpgProgressionState.RequirementResult.MET)return false;
    if(rpg.evaluateRequirements("IT_RING_GORU","ROGUE",11)!=RpgProgressionState.RequirementResult.JOB_NOT_MET)return false;
    if(rpg.evaluateRequirements("IT_RING_GORU","MAGE",10)!=RpgProgressionState.RequirementResult.LEVEL_NOT_MET)return false;

    RpgProgressionState.ItemDefinition waterNeck=rpg.itemDefinitions().get("IT_NECK_WATER_PEARL");
    RpgProgressionState.ItemDefinition waterBelt=rpg.itemDefinitions().get("IT_BELT_WATER_LEATHER");
    if(waterNeck==null||!"바다".equals(waterNeck.attackElement)||waterNeck.defenseElement!=null)return false;
    if(waterBelt==null||waterBelt.attackElement!=null||!"바다".equals(waterBelt.defenseElement))return false;

    if(rpg.currentRequirements("IT_EARRING_DOUBLE_SILVER")!=RpgProgressionState.RequirementResult.LEVEL_NOT_MET)return false;
    if(rpg.evaluateRequirements("UNKNOWN_ITEM","WARRIOR",11)!=RpgProgressionState.RequirementResult.UNKNOWN_ITEM)return false;

    if(rpg.autoLootResolvedItem("UNKNOWN_ITEM",1)!=RpgProgressionState.AutoLootResult.INVALID_ITEM)return false;
    if(rpg.autoLootResolvedItem("IT_GLOVE_LEATHER",0)!=RpgProgressionState.AutoLootResult.INVALID_QUANTITY)return false;

    if(controller.selectInventoryItem(rpg,"UNKNOWN_ITEM"))return false;
    return controller.selectedInventoryItemId()==null;
  }
}
