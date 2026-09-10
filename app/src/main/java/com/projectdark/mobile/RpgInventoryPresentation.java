package com.projectdark.mobile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Read-only presentation projection for inventory/equipment/auto-loot UI.
 *
 * This class introduces no gameplay values. It only converts RPG-owned canonical state into
 * stable UI rows/notices so GameView or a later inventory screen does not duplicate RPG rules.
 */
public final class RpgInventoryPresentation {
  public static final class ItemRow {
    public final String itemId,name,equipSlot;
    public final int quantity;
    public final Integer requiredLevel;
    public final boolean equipped;
    public final RpgProgressionState.Evidence evidence;

    ItemRow(String itemId,String name,String equipSlot,int quantity,Integer requiredLevel,boolean equipped,RpgProgressionState.Evidence evidence){
      this.itemId=itemId;this.name=name;this.equipSlot=equipSlot;this.quantity=quantity;
      this.requiredLevel=requiredLevel;this.equipped=equipped;this.evidence=evidence;
    }
  }

  public static final class RewardNotice {
    public final long combatSequence;
    public final String monsterId;
    public final RpgProgressionState.RewardStatus status;
    public final Integer exp;
    public final Map<String,Integer> autoLootedItems;

    RewardNotice(RpgProgressionState.RewardResolution reward){
      this.combatSequence=reward.combatSequence;
      this.monsterId=reward.monsterId;
      this.status=reward.status;
      this.exp=reward.exp;
      this.autoLootedItems=reward.autoLootedItems;
    }
  }

  public List<ItemRow> inventoryRows(RpgProgressionState rpg){
    List<ItemRow> rows=new ArrayList<>();
    for(Map.Entry<String,Integer> entry:rpg.inventory().entrySet()){
      RpgProgressionState.ItemDefinition def=rpg.itemDefinitions().get(entry.getKey());
      if(def==null)continue;
      boolean equipped=false;
      for(String equippedItemId:rpg.equipment().values()){
        if(def.itemId.equals(equippedItemId)){equipped=true;break;}
      }
      rows.add(new ItemRow(def.itemId,def.name,def.equipSlot,entry.getValue(),def.requiredLevel,equipped,def.evidence));
    }
    return Collections.unmodifiableList(rows);
  }

  public RewardNotice latestRewardNotice(RpgProgressionState rpg){
    List<RpgProgressionState.RewardResolution> history=rpg.rewardHistory();
    if(history.isEmpty())return null;
    return new RewardNotice(history.get(history.size()-1));
  }

  /** QA invariant: every inventory row must resolve to an RPG-owned canonical item definition. */
  public boolean rowsReferenceDefinitions(RpgProgressionState rpg){
    for(ItemRow row:inventoryRows(rpg))if(!rpg.itemDefinitions().containsKey(row.itemId))return false;
    return true;
  }
}
