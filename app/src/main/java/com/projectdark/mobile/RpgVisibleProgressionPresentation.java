package com.projectdark.mobile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Single RPG-owned read-only snapshot for HUD/overlay rendering.
 * Integrator/GameView may consume this DTO without learning RPG mutation rules.
 */
public final class RpgVisibleProgressionPresentation {
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

  public static final class RewardLine {
    public final String text;
    public final RpgProgressionState.Evidence evidence;
    RewardLine(String text,RpgProgressionState.Evidence evidence){this.text=text;this.evidence=evidence;}
  }

  public static final class Snapshot {
    public final PlayerSummary player;
    public final List<RpgInventoryPresentation.ItemRow> inventory;
    public final List<RpgActionMetadataCatalog.ActionMetadata> actions;
    public final List<RewardLine> latestRewardLines;
    Snapshot(PlayerSummary player,List<RpgInventoryPresentation.ItemRow> inventory,
        List<RpgActionMetadataCatalog.ActionMetadata> actions,List<RewardLine> latestRewardLines){
      this.player=player;this.inventory=inventory;this.actions=actions;this.latestRewardLines=latestRewardLines;
    }
  }

  private final RpgInventoryPresentation inventoryPresentation=new RpgInventoryPresentation();

  public Snapshot snapshot(RpgProgressionState rpg){
    if(rpg==null)throw new IllegalArgumentException("rpg");
    return new Snapshot(new PlayerSummary(rpg),inventoryPresentation.inventoryRows(rpg),
        RpgActionMetadataCatalog.visibleFor(rpg),latestRewardLines(rpg));
  }

  public List<RewardLine> latestRewardLines(RpgProgressionState rpg){
    RpgInventoryPresentation.RewardNotice notice=inventoryPresentation.latestRewardNotice(rpg);
    if(notice==null)return Collections.emptyList();
    List<RewardLine> lines=new ArrayList<>();
    if(notice.status==RpgProgressionState.RewardStatus.PENDING_NO_CANONICAL_MONSTER_REWARD){
      lines.add(new RewardLine("보상 데이터 확인 필요: "+notice.monsterId,RpgProgressionState.Evidence.PENDING));
      return Collections.unmodifiableList(lines);
    }
    if(notice.exp!=null)lines.add(new RewardLine("EXP +"+notice.exp,RpgProgressionState.Evidence.V));
    for(Map.Entry<String,Integer> grant:notice.autoLootedItems.entrySet()){
      RpgProgressionState.ItemDefinition def=rpg.itemDefinitions().get(grant.getKey());
      String name=def==null?grant.getKey():def.name;
      lines.add(new RewardLine(name+" x"+grant.getValue()+" 자동 획득",def==null?RpgProgressionState.Evidence.U:def.evidence));
    }
    if(notice.autoLootedItems.isEmpty()&&notice.exp==null){
      lines.add(new RewardLine("지급 가능한 확정 보상 없음",RpgProgressionState.Evidence.PENDING));
    }
    return Collections.unmodifiableList(lines);
  }

  /** UI helper that never converts unresolved numeric values into zero. */
  public static String numericOrPending(Number value){return value==null?"PENDING":String.valueOf(value);}
}
