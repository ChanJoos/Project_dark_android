package com.projectdark.mobile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** RPG-owned transient reward feed. UI renders snapshots; this class does not draw or own layout. */
public final class RpgRewardFeedPresentation {
  public enum Semantic {
    ITEM_GRANTED, EXP_GAINED, INVENTORY_FULL, INVALID_ITEM, INVALID_QUANTITY,
    UNRESOLVED_REWARD, NO_CONFIRMED_REWARD
  }
  public static final float DEFAULT_DURATION_SECONDS=2.4f; // [ADAPTED/B] presentation lifetime.

  public static final class Snapshot {
    public final long id,combatSequence;
    public final Semantic semantic;
    public final String monsterId,itemId,text;
    public final Integer quantity;
    public final RpgProgressionState.Evidence evidence;
    public final float progress,alpha;
    Snapshot(long id,long combatSequence,Semantic semantic,String monsterId,String itemId,String text,
        Integer quantity,RpgProgressionState.Evidence evidence,float progress,float alpha){
      this.id=id;this.combatSequence=combatSequence;this.semantic=semantic;this.monsterId=monsterId;
      this.itemId=itemId;this.text=text;this.quantity=quantity;this.evidence=evidence;
      this.progress=progress;this.alpha=alpha;
    }
  }
  private static final class Entry {
    final long id,combatSequence;final Semantic semantic;final String monsterId,itemId,text;
    final Integer quantity;final RpgProgressionState.Evidence evidence;float age;
    Entry(long id,RpgProgressionState.RewardResolution reward,Semantic semantic,String itemId,String text,
        Integer quantity,RpgProgressionState.Evidence evidence){
      this.id=id;combatSequence=reward.combatSequence;monsterId=reward.monsterId;this.semantic=semantic;
      this.itemId=itemId;this.text=text;this.quantity=quantity;this.evidence=evidence;
    }
  }

  private final List<Entry> active=new ArrayList<>();
  private final Set<Long> seenCombatSequences=new HashSet<>();
  private long sequence;

  public void sync(RpgProgressionState rpg){
    if(rpg==null)throw new IllegalArgumentException("rpg");
    sync(rpg.rewardHistory(),rpg.itemDefinitions());
  }

  void sync(List<RpgProgressionState.RewardResolution> rewards,
      Map<String,RpgProgressionState.ItemDefinition> definitions){
    for(RpgProgressionState.RewardResolution reward:rewards){
      if(!seenCombatSequences.add(reward.combatSequence))continue;
      int before=active.size();
      if(reward.status==RpgProgressionState.RewardStatus.PENDING_NO_CANONICAL_MONSTER_REWARD){
        add(reward,Semantic.UNRESOLVED_REWARD,null,"보상 데이터 확인 필요: "+reward.monsterId,null,RpgProgressionState.Evidence.PENDING);
        continue;
      }
      if(reward.exp!=null)add(reward,Semantic.EXP_GAINED,null,"EXP +"+reward.exp,null,RpgProgressionState.Evidence.V);
      for(RpgProgressionState.RewardGrantOutcome outcome:reward.grantOutcomes){
        RpgProgressionState.ItemDefinition def=definitions.get(outcome.itemId);
        String name=def==null?(outcome.itemId==null?"알 수 없는 아이템":outcome.itemId):def.name;
        switch(outcome.status){
          case GRANTED:add(reward,Semantic.ITEM_GRANTED,outcome.itemId,name+" x"+outcome.requestedQuantity+" 자동 획득",
              outcome.requestedQuantity,outcome.evidence);break;
          case INVENTORY_FULL:add(reward,Semantic.INVENTORY_FULL,outcome.itemId,"인벤토리가 가득 차 지급 실패: "+name,
              outcome.requestedQuantity,outcome.evidence);break;
          case INVALID_ITEM:add(reward,Semantic.INVALID_ITEM,outcome.itemId,"잘못된 아이템 ID: "+name,
              outcome.requestedQuantity,outcome.evidence);break;
          case INVALID_QUANTITY:add(reward,Semantic.INVALID_QUANTITY,outcome.itemId,"잘못된 지급 수량: "+name,
              outcome.requestedQuantity,outcome.evidence);break;
          case UNRESOLVED_REWARD:
          default:add(reward,Semantic.UNRESOLVED_REWARD,outcome.itemId,"드롭 확률/수량 확인 필요: "+name,
              outcome.requestedQuantity,outcome.evidence);break;
        }
      }
      if(active.size()==before)add(reward,Semantic.NO_CONFIRMED_REWARD,null,"지급 가능한 확정 보상 없음",null,RpgProgressionState.Evidence.PENDING);
    }
  }

  public void tick(float deltaSeconds){
    if(deltaSeconds<=0f)return;
    for(Iterator<Entry> it=active.iterator();it.hasNext();){Entry e=it.next();e.age+=deltaSeconds;if(e.age>=DEFAULT_DURATION_SECONDS)it.remove();}
  }
  public List<Snapshot> snapshot(){
    List<Snapshot> out=new ArrayList<>();
    for(Entry e:active){
      float p=Math.max(0f,Math.min(1f,e.age/DEFAULT_DURATION_SECONDS));
      float alpha=p<.7f?1f:Math.max(0f,1f-(p-.7f)/.3f);
      out.add(new Snapshot(e.id,e.combatSequence,e.semantic,e.monsterId,e.itemId,e.text,e.quantity,e.evidence,p,alpha));
    }
    return Collections.unmodifiableList(out);
  }
  public int size(){return active.size();}
  public void clearVisible(){active.clear();}
  private void add(RpgProgressionState.RewardResolution reward,Semantic semantic,String itemId,String text,
      Integer quantity,RpgProgressionState.Evidence evidence){
    active.add(new Entry(++sequence,reward,semantic,itemId,text,quantity,evidence==null?RpgProgressionState.Evidence.PENDING:evidence));
  }
}
