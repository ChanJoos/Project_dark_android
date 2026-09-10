package com.projectdark.mobile;

import java.util.Arrays;
import java.util.List;

/** Regression audit for shared resolver mutations against the real RuntimeState surface. */
public final class RuntimeCombatPortAdapterAudit {
  private RuntimeCombatPortAdapterAudit(){}

  public static boolean verify(){
    RuntimeState state=new RuntimeState(RuntimeState.BootMode.MILLES,true);
    if(state.monsters().isEmpty())return false;
    RuntimeState.Monster monster=state.monsters().get(0);
    state.player().x=monster.x;
    state.player().y=monster.y;

    RuntimeCombatPortAdapter port=new RuntimeCombatPortAdapter(
        state,
        (actorId,targetId)->true,
        (actorId,actionId)->"player_magic".equals(actionId));
    CombatResolver resolver=new CombatResolver(port);
    CombatResolver.Definition magic=new CombatResolver.Definition(
        "player_magic",CombatResolver.ActionKind.MAGIC,CombatResolver.ActionState.MAGIC,
        CombatResolver.EffectType.MAGIC_HIT,true,8,1.0f,100f,.20f,3);
    CombatResolver.Definition monsterAttack=new CombatResolver.Definition(
        "monster_basic",CombatResolver.ActionKind.ATTACK,CombatResolver.ActionState.ATTACK,
        CombatResolver.EffectType.PHYSICAL_HIT,false,0,.50f,100f,.20f,5);
    CombatActionOrchestrator actions=new CombatActionOrchestrator(resolver,Arrays.asList(magic,monsterAttack));

    int initialMp=state.player().mp;
    int initialMonsterHp=monster.hp;
    CombatActionOrchestrator.Submission cast=actions.submitManual("player",monster.id,"player_magic");
    if(!cast.accepted()||state.player().mp!=initialMp-8)return false;
    if(port.cooldownRemaining("player","player_magic")<=0f)return false;
    actions.tick(.19f);
    if(monster.hp!=initialMonsterHp)return false;
    actions.tick(.01f);
    if(monster.hp!=Math.max(0,initialMonsterHp-3))return false;

    CombatActionOrchestrator.Submission cooled=actions.submitManual("player",monster.id,"player_magic");
    if(cooled.rejectReason!=CombatResolver.RejectReason.COOLDOWN)return false;
    port.tick(1.0f);
    if(port.cooldownRemaining("player","player_magic")!=0f)return false;

    // Monster AUTO damage reaches RuntimeState only at the shared resolver hit frame.
    state.player().hp=100;
    state.player().alive=true;
    int beforePlayerHp=state.player().hp;
    if(!actions.submitAuto(monster.id,"player","monster_basic").accepted())return false;
    if(state.player().hp!=beforePlayerHp)return false;
    actions.tick(.20f);
    if(state.player().hp!=beforePlayerHp-5)return false;
    List<CombatResolver.Event> autoEvents=actions.drainEvents();
    if(!hasEffect(autoEvents,monster.id,"monster_basic",CombatResolver.InputMode.AUTO))return false;

    // RuntimeState is currently the legacy defeat-ledger authority. Resolver must not duplicate it.
    monster.hp=1;
    monster.alive=true;
    state.ledger().clear();
    port.tick(1f);
    if(!actions.submitManual("player",monster.id,"player_magic").accepted())return false;
    actions.tick(.20f);
    if(monster.alive)return false;
    if(countLedger(state.ledger().snapshot(),CombatLedger.Type.MONSTER_DEFEATED)!=1)return false;
    if(countResolver(actions.drainEvents(),CombatResolver.EventType.MONSTER_DEFEATED)!=0)return false;

    // A monster killing the player must publish PLAYER_DEFEATED only, never MONSTER_DEFEATED.
    state.revivePlayer();
    state.player().hp=4;
    monster.alive=true;
    monster.hp=Math.max(1,monster.maxHp);
    state.ledger().clear();
    port.tick(1f);
    if(!actions.submitAuto(monster.id,"player","monster_basic").accepted())return false;
    actions.tick(.20f);
    if(state.player().alive)return false;
    if(countLedger(state.ledger().snapshot(),CombatLedger.Type.PLAYER_DEFEATED)!=1)return false;
    return countResolver(actions.drainEvents(),CombatResolver.EventType.MONSTER_DEFEATED)==0;
  }

  private static boolean hasEffect(List<CombatResolver.Event> events,String actor,String actionId,CombatResolver.InputMode mode){
    for(CombatResolver.Event event:events)
      if(event.type==CombatResolver.EventType.EFFECT_APPLIED&&actor.equals(event.actorId)&&actionId.equals(event.actionId)&&event.inputMode==mode)return true;
    return false;
  }

  private static int countLedger(List<CombatLedger.Event> events,CombatLedger.Type type){
    int count=0;for(CombatLedger.Event event:events)if(event.type==type)count++;return count;
  }

  private static int countResolver(List<CombatResolver.Event> events,CombatResolver.EventType type){
    int count=0;for(CombatResolver.Event event:events)if(event.type==type)count++;return count;
  }

  public static void main(String[] args){
    if(!verify())throw new AssertionError("RuntimeCombatPortAdapterAudit failed");
    System.out.println("RuntimeCombatPortAdapterAudit PASS");
  }
}
