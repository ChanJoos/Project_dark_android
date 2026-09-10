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

    if(rpg.autoLootResolvedItem("UNKNOWN_ITEM",1)!=RpgProgressionState.AutoLootResult.INVALID_ITEM)return false;
    if(rpg.autoLootResolvedItem("IT_GLOVE_LEATHER",0)!=RpgProgressionState.AutoLootResult.INVALID_QUANTITY)return false;

    if(controller.selectInventoryItem(rpg,"UNKNOWN_ITEM"))return false;
    return controller.selectedInventoryItemId()==null;
  }
}
