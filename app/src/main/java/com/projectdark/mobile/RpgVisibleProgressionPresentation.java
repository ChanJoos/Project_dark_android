package com.projectdark.mobile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/** Single RPG-owned read-only snapshot for HUD/overlay rendering. */
public final class RpgVisibleProgressionPresentation {
  public enum RewardLineKind { EXP, ITEM_GRANTED, INVENTORY_FULL, INVALID_ITEM, INVALID_QUANTITY, UNRESOLVED, INFO }

  public static final class PlayerSummary {
    public final String jobCode;
    public final Integer level;
    public final Long exp;
    public final Long gold;
    public final RpgProgressionState.ProgressionNode progressionNode;
    PlayerSummary(RpgProgressionState rpg){
      this.jobCode=rpg.currentJobCode();this.level=rpg.normalLevel();this.exp=rpg.normalExp();this.gold=rpg.gold();
      this.progressionNode=rpg.progressionNode();
    }
  }

  /** Stable reward-feed row. UI may style by kind without parsing Korean display text. */
  public static final class RewardLine {
    public final RewardLineKind kind;
    public final String text;
    public final String itemId;
    public final Integer quantity;
    public final RpgProgressionState.RewardGrantStatus grantStatus;
    public final RpgProgressionState.Evidence evidence;
    RewardLine(RewardLineKind kind,String text,String itemId,Integer quantity,
        RpgProgressionState.RewardGrantStatus grantStatus,RpgProgressionState.Evidence evidence){
      this.kind=kind;this.text=text;this.itemId=itemId;this.quantity=quantity;this.grantStatus=grantStatus;this.evidence=evidence;
    }
  }

  public static final class Snapshot {
    public final PlayerSummary player;
    public final List<RpgInventoryPresentation.ItemRow> inventory;
    public final List<RpgActionMetadataCatalog.ActionMetadata> actions;
    public final List<RpgActionMetadataCatalog.ActionMetadata> quickSlotCandidates;
    public final List<RewardLine> latestRewardLines;
    Snapshot(PlayerSummary player,List<RpgInventoryPresentation.ItemRow> inventory,
        List<RpgActionMetadataCatalog.ActionMetadata> actions,List<RpgActionMetadataCatalog.ActionMetadata> quickSlotCandidates,
        List<RewardLine> latestRewardLines){
      this.player=player;this.inventory=inventory;this.actions=actions;this.quickSlotCandidates=quickSlotCandidates;
      this.latestRewardLines=latestRewardLines;
    }
  }

  private final RpgInventoryPresentation inventoryPresentation=new RpgInventoryPresentation();

  public Snapshot snapshot(RpgProgressionState rpg){
    if(rpg==null)throw new IllegalArgumentException("rpg");
    return new Snapshot(new PlayerSummary(rpg),inventoryPresentation.inventoryRows(rpg),
        RpgActionMetadataCatalog.visibleFor(rpg),RpgActionMetadataCatalog.quickSlotCandidates(rpg),latestRewardLines(rpg));
  }

  /** Read-only skill-book preview; does not learn skills or change the player's job. */
  public List<RpgActionMetadataCatalog.ActionMetadata> skillBook(RpgProgressionState rpg,String jobCode){
    if(rpg==null)throw new IllegalArgumentException("rpg");
    return RpgActionMetadataCatalog.forJob(rpg,jobCode);
  }

  public List<RewardLine> latestRewardLines(RpgProgressionState rpg){
    RpgInventoryPresentation.RewardNotice notice=inventoryPresentation.latestRewardNotice(rpg);
    if(notice==null)return Collections.emptyList();
    List<RewardLine> lines=new ArrayList<>();
    if(notice.status==RpgProgressionState.RewardStatus.PENDING_NO_CANONICAL_MONSTER_REWARD){
      lines.add(new RewardLine(RewardLineKind.UNRESOLVED,"보상 데이터 확인 필요: "+notice.monsterId,null,null,
          RpgProgressionState.RewardGrantStatus.UNRESOLVED_REWARD,RpgProgressionState.Evidence.PENDING));
      return Collections.unmodifiableList(lines);
    }
    if(notice.exp!=null)lines.add(new RewardLine(RewardLineKind.EXP,"EXP +"+notice.exp,null,null,null,RpgProgressionState.Evidence.V));

    // Prefer explicit grant outcomes. This retains failures that autoLootedItems alone cannot represent.
    for(RpgProgressionState.RewardGrantOutcome outcome:notice.grantOutcomes)lines.add(grantOutcomeLine(rpg,outcome));

    // Compatibility for older resolved records that only expose the successful aggregate map.
    if(notice.grantOutcomes.isEmpty()){
      for(Map.Entry<String,Integer> grant:notice.autoLootedItems.entrySet()){
        RpgProgressionState.ItemDefinition def=rpg.itemDefinitions().get(grant.getKey());
        String name=def==null?grant.getKey():def.name;
        lines.add(new RewardLine(RewardLineKind.ITEM_GRANTED,name+" x"+grant.getValue()+" 자동 획득",grant.getKey(),grant.getValue(),
            RpgProgressionState.RewardGrantStatus.GRANTED,def==null?RpgProgressionState.Evidence.U:def.evidence));
      }
    }

    if(lines.isEmpty())lines.add(new RewardLine(RewardLineKind.INFO,"지급 가능한 확정 보상 없음",null,null,null,RpgProgressionState.Evidence.PENDING));
    return Collections.unmodifiableList(lines);
  }

  public RewardLine grantOutcomeLine(RpgProgressionState rpg,RpgProgressionState.RewardGrantOutcome outcome){
    if(outcome==null)return new RewardLine(RewardLineKind.INFO,"보상 처리 결과 없음",null,null,null,RpgProgressionState.Evidence.PENDING);
    RpgProgressionState.ItemDefinition def=rpg==null?null:rpg.itemDefinitions().get(outcome.itemId);
    String name=def==null?(outcome.itemId==null?"알 수 없는 아이템":outcome.itemId):def.name;
    switch(outcome.status){
      case GRANTED:
        return new RewardLine(RewardLineKind.ITEM_GRANTED,name+" x"+outcome.requestedQuantity+" 자동 획득",outcome.itemId,outcome.requestedQuantity,outcome.status,outcome.evidence);
      case INVENTORY_FULL:
        return new RewardLine(RewardLineKind.INVENTORY_FULL,name+" 획득 실패: 인벤토리 한도",outcome.itemId,outcome.requestedQuantity,outcome.status,outcome.evidence);
      case INVALID_ITEM:
        return new RewardLine(RewardLineKind.INVALID_ITEM,"획득 실패: 알 수 없는 아이템 "+name,outcome.itemId,outcome.requestedQuantity,outcome.status,outcome.evidence);
      case INVALID_QUANTITY:
        return new RewardLine(RewardLineKind.INVALID_QUANTITY,name+" 획득 실패: 잘못된 수량",outcome.itemId,outcome.requestedQuantity,outcome.status,outcome.evidence);
      case UNRESOLVED_REWARD:
      default:
        return new RewardLine(RewardLineKind.UNRESOLVED,name+" 보상 수량/확률 확인 필요",outcome.itemId,outcome.requestedQuantity,outcome.status,outcome.evidence);
    }
  }

  public static String learnedLabel(RpgActionMetadataCatalog.ActionMetadata action){
    if(action==null)return "";
    switch(action.visibilityState){
      case LEARNED:return "습득";
      case CURRENT_JOB_LOCKED:return "미습득";
      case OTHER_JOB:return "타직업";
      case RUNTIME_PROTOTYPE:
      default:return "PROTOTYPE";
    }
  }

  public static String costLabel(RpgActionMetadataCatalog.ActionMetadata action){
    if(action==null)return "";
    if(action.resourceCostResolved())return action.resourceLabel+" "+action.resourceCost;
    return action.resourceLabel==null?"소모 PENDING":action.resourceLabel;
  }

  public static String cooldownLabel(RpgActionMetadataCatalog.ActionMetadata action){
    return action!=null&&action.cooldownResolved()?String.valueOf(action.cooldown):"PENDING";
  }

  /** UI helper that never converts unresolved numeric values into zero. */
  public static String numericOrPending(Number value){return value==null?"PENDING":String.valueOf(value);}
}
