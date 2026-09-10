package com.projectdark.mobile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Read-only presentation projection for inventory/equipment/auto-loot UI. */
public final class RpgInventoryPresentation {
  public static final class ItemRow {
    public final String itemId,name,equipSlot;
    public final int quantity;
    public final Integer requiredLevel;
    public final boolean equipped;
    public final Set<String> allowedJobCodes;
    public final boolean jobRestrictionResolved;
    public final String attackElement,defenseElement;
    public final RpgProgressionState.RequirementResult requirementResult;
    public final RpgProgressionState.Evidence evidence;

    ItemRow(RpgProgressionState.ItemDefinition def,int quantity,boolean equipped,RpgProgressionState.RequirementResult requirementResult){
      this.itemId=def.itemId;this.name=def.name;this.equipSlot=def.equipSlot;this.quantity=quantity;
      this.requiredLevel=def.requiredLevel;this.equipped=equipped;
      this.allowedJobCodes=Collections.unmodifiableSet(new LinkedHashSet<>(def.allowedJobCodes));
      this.jobRestrictionResolved=def.jobRestrictionResolved;
      this.attackElement=def.attackElement;this.defenseElement=def.defenseElement;
      this.requirementResult=requirementResult;this.evidence=def.evidence;
    }
  }

  public static final class RewardNotice {
    public final long combatSequence;
    public final String monsterId;
    public final RpgProgressionState.RewardStatus status;
    public final Integer exp;
    public final Map<String,Integer> autoLootedItems;
    public final List<RpgProgressionState.RewardGrantOutcome> grantOutcomes;
    RewardNotice(RpgProgressionState.RewardResolution reward){
      this.combatSequence=reward.combatSequence;this.monsterId=reward.monsterId;this.status=reward.status;
      this.exp=reward.exp;this.autoLootedItems=reward.autoLootedItems;this.grantOutcomes=reward.grantOutcomes;
    }
  }

  public List<ItemRow> inventoryRows(RpgProgressionState rpg){
    List<ItemRow> rows=new ArrayList<>();
    for(Map.Entry<String,Integer> entry:rpg.inventory().entrySet()){
      RpgProgressionState.ItemDefinition def=rpg.itemDefinitions().get(entry.getKey());
      if(def==null)continue;
      boolean equipped=false;
      for(String equippedItemId:rpg.equipment().values())if(def.itemId.equals(equippedItemId)){equipped=true;break;}
      rows.add(new ItemRow(def,entry.getValue(),equipped,rpg.currentRequirements(def.itemId)));
    }
    return Collections.unmodifiableList(rows);
  }

  public RewardNotice latestRewardNotice(RpgProgressionState rpg){List<RpgProgressionState.RewardResolution> history=rpg.rewardHistory();return history.isEmpty()?null:new RewardNotice(history.get(history.size()-1));}

  public String requirementLabel(ItemRow row){
    if(row==null)return "조건 확인 불가";
    switch(row.requirementResult){
      case MET:return "장착 가능";
      case LEVEL_NOT_MET:return "레벨 조건 미충족";
      case JOB_NOT_MET:return "직업 조건 미충족";
      case PENDING:return "장착 조건 확인 필요";
      case UNKNOWN_ITEM:
      default:return "아이템 정의 확인 필요";
    }
  }

  public String elementLabel(ItemRow row){
    if(row==null)return "";
    if(row.attackElement!=null)return "공격속성 "+row.attackElement;
    if(row.defenseElement!=null)return "방어속성 "+row.defenseElement;
    return "";
  }

  public boolean rowsReferenceDefinitions(RpgProgressionState rpg){for(ItemRow row:inventoryRows(rpg))if(!rpg.itemDefinitions().containsKey(row.itemId))return false;return true;}
}
