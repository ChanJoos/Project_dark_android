package com.projectdark.mobile;

import java.util.Map;

/**
 * Integrator-owned orchestration for inventory/equipment interaction.
 *
 * Monster item rewards are auto-looted directly by the RPG reward path. This controller intentionally
 * owns no ground-drop selection/pickup behavior and contains no item/drop/EXP/stat values.
 */
public final class RpgInteractionController {
  public enum InteractionResult {
    EQUIPPED,
    EQUIP_BLOCKED,
    NO_ITEM
  }

  private String selectedInventoryItemId = null;
  private RpgProgressionState.EquipResult lastEquipResult = null;

  public String selectedInventoryItemId(){ return selectedInventoryItemId; }
  public RpgProgressionState.EquipResult lastEquipResult(){ return lastEquipResult; }

  /** Select an owned item by canonical ID. Display names are deliberately not used as identity. */
  public boolean selectInventoryItem(RpgProgressionState rpg,String itemId){
    Integer quantity=rpg.inventory().get(itemId);
    if(quantity==null||quantity<=0){selectedInventoryItemId=null;lastEquipResult=null;return false;}
    selectedInventoryItemId=itemId;
    lastEquipResult=null;
    return true;
  }

  /**
   * Detailed equipment request. All ownership/definition/slot/requirement rules remain in
   * RpgProgressionState; this controller only preserves selection and reports the canonical result.
   */
  public RpgProgressionState.EquipResult equipSelectedDetailed(RpgProgressionState rpg){
    if(selectedInventoryItemId==null){lastEquipResult=RpgProgressionState.EquipResult.ITEM_NOT_OWNED;return lastEquipResult;}
    lastEquipResult=rpg.equip(selectedInventoryItemId);
    if(lastEquipResult==RpgProgressionState.EquipResult.ITEM_NOT_OWNED)selectedInventoryItemId=null;
    return lastEquipResult;
  }

  /** Legacy coarse result retained for existing callers; delegates to the detailed result. */
  public InteractionResult equipSelected(RpgProgressionState rpg){
    if(selectedInventoryItemId==null)return InteractionResult.NO_ITEM;
    RpgProgressionState.EquipResult result=equipSelectedDetailed(rpg);
    switch(result){
      case EQUIPPED:return InteractionResult.EQUIPPED;
      case ITEM_NOT_OWNED:return InteractionResult.NO_ITEM;
      case UNKNOWN_ITEM:
      case NOT_EQUIPPABLE:
      case REQUIREMENT_PENDING:
      case REQUIREMENT_NOT_MET:
      default:return InteractionResult.EQUIP_BLOCKED;
    }
  }

  /** Read-only snapshot helper for HUD/QA; it does not mutate canonical/base stats. */
  public RpgProgressionState.StatSnapshot statSnapshot(RpgProgressionState rpg){
    return rpg.recomputeStats();
  }

  /** QA invariant: every equipped canonical ID must still exist in inventory with positive quantity. */
  public boolean equipmentReferencesOwnedItems(RpgProgressionState rpg){
    Map<String,Integer> inventory=rpg.inventory();
    for(String itemId:rpg.equipment().values()){
      Integer quantity=inventory.get(itemId);
      if(quantity==null||quantity<=0)return false;
    }
    return true;
  }
}
