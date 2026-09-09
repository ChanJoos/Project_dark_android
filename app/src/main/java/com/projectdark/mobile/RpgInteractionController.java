package com.projectdark.mobile;

import java.util.Map;

/**
 * Integrator-owned orchestration for world-drop pickup and equipment interaction.
 *
 * This controller intentionally contains no item/drop/EXP/stat values. Canonical content remains
 * owned by RpgProgressionState / Master data. It only translates player interaction into stable
 * RPG contract calls so GameView does not need to duplicate inventory/equipment rules.
 */
public final class RpgInteractionController {
  public enum InteractionResult {
    NO_DROP,
    DROP_TOO_FAR,
    PICKED_UP,
    PICKUP_INVALID,
    INVENTORY_FULL,
    EQUIPPED,
    EQUIP_BLOCKED,
    NO_ITEM
  }

  private long selectedDropId = -1L;
  private String selectedInventoryItemId = null;

  public long selectedDropId(){ return selectedDropId; }
  public String selectedInventoryItemId(){ return selectedInventoryItemId; }

  /** Select the nearest visible world drop inside maxSelectionDistance. No pickup mutation occurs. */
  public boolean selectNearestDrop(RpgProgressionState rpg,float playerX,float playerY,float maxSelectionDistance){
    RpgProgressionState.WorldDrop best=null;
    float bestSq=maxSelectionDistance*maxSelectionDistance;
    for(RpgProgressionState.WorldDrop d:rpg.worldDrops()){
      float dx=playerX-d.x,dy=playerY-d.y;
      float sq=dx*dx+dy*dy;
      if(sq<=bestSq){best=d;bestSq=sq;}
    }
    selectedDropId=best==null?-1L:best.dropId;
    return best!=null;
  }

  /**
   * Pick up the selected ground entity through the RPG-owned validation path.
   * AUTO/manual callers must use the same function; there is no inventory teleport shortcut.
   */
  public InteractionResult pickupSelected(RpgProgressionState rpg,float playerX,float playerY){
    if(selectedDropId<0)return InteractionResult.NO_DROP;
    RpgProgressionState.PickupResult result=rpg.pickup(selectedDropId,playerX,playerY);
    switch(result){
      case PICKED_UP:
        selectedDropId=-1L;
        return InteractionResult.PICKED_UP;
      case TOO_FAR:return InteractionResult.DROP_TOO_FAR;
      case INVENTORY_FULL:return InteractionResult.INVENTORY_FULL;
      case INVALID_ITEM:return InteractionResult.PICKUP_INVALID;
      case NOT_FOUND:
      default:
        selectedDropId=-1L;
        return InteractionResult.NO_DROP;
    }
  }

  /** Select an owned item by canonical ID. Display names are deliberately not used as identity. */
  public boolean selectInventoryItem(RpgProgressionState rpg,String itemId){
    Integer quantity=rpg.inventory().get(itemId);
    if(quantity==null||quantity<=0){selectedInventoryItemId=null;return false;}
    selectedInventoryItemId=itemId;
    return true;
  }

  /** Equip through RpgProgressionState so level/slot/evidence requirements stay centralized. */
  public InteractionResult equipSelected(RpgProgressionState rpg){
    if(selectedInventoryItemId==null)return InteractionResult.NO_ITEM;
    RpgProgressionState.EquipResult result=rpg.equip(selectedInventoryItemId);
    switch(result){
      case EQUIPPED:return InteractionResult.EQUIPPED;
      case ITEM_NOT_OWNED:
        selectedInventoryItemId=null;
        return InteractionResult.NO_ITEM;
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
