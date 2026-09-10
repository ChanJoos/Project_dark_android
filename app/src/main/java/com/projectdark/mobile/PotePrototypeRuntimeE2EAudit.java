package com.projectdark.mobile;

import java.util.List;

/**
 * Runtime E2E regression for the first selectable Pote prototype combat slice.
 *
 * Canonical identity/map membership is preserved while placement and HP remain explicit [B]/[ADAPTED].
 * POTE_PURPLE currently has no canonical reward entry, so defeat must terminate in an explicit pending
 * reward result with no fabricated EXP or inventory mutation.
 */
public final class PotePrototypeRuntimeE2EAudit {
  private PotePrototypeRuntimeE2EAudit(){}

  public static boolean verify(){
    RuntimeState runtime=new RuntimeState(RuntimeState.BootMode.POTE_01_PROTOTYPE,true);
    if(runtime.bootMode()!=RuntimeState.BootMode.POTE_01_PROTOTYPE)return false;
    if(!PotePrototypeWorldDef.MAP_ID.equals(runtime.currentMapId()))return false;
    if(runtime.player().spawnX!=PotePrototypeWorldDef.PLAYER_X_B||runtime.player().spawnY!=PotePrototypeWorldDef.PLAYER_Y_B)return false;
    if(runtime.monsters().size()!=1)return false;

    RuntimeState.Monster monster=runtime.monsters().get(0);
    if(!PotePrototypeCombatProfile.MONSTER_ID.equals(monster.id))return false;
    if(monster.maxHp!=PotePrototypeCombatProfile.RUNTIME_HP_B||!monster.alive)return false;

    runtime.damage(monster,monster.maxHp);
    if(monster.alive||monster.hp!=0||monster.state!=RuntimeState.Monster.State.DEAD)return false;

    boolean defeatFound=false;
    for(CombatLedger.Event event:runtime.ledger().snapshot()){
      if(event.type==CombatLedger.Type.MONSTER_DEFEATED&&monster.id.equals(event.targetId)){defeatFound=true;break;}
    }
    if(!defeatFound)return false;

    runtime.tick(0f);
    List<RpgProgressionState.RewardResolution> history=runtime.rpg().rewardHistory();
    if(history.size()!=1)return false;
    RpgProgressionState.RewardResolution reward=history.get(0);
    if(!monster.id.equals(reward.monsterId))return false;
    if(reward.status!=RpgProgressionState.RewardStatus.PENDING_NO_CANONICAL_MONSTER_REWARD)return false;
    if(reward.exp!=null||!reward.autoLootedItems.isEmpty())return false;
    if(runtime.rpg().normalExp()!=null||!runtime.rpg().inventory().isEmpty())return false;

    // Same ledger snapshot must not resolve the same defeat twice.
    runtime.tick(0f);
    return runtime.rpg().rewardHistory().size()==1;
  }
}
