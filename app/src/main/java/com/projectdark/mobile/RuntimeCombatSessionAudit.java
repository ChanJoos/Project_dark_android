package com.projectdark.mobile;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Android-free GAME-01 audit over the actual RuntimeState mutation and reward path. */
public final class RuntimeCombatSessionAudit {
  private RuntimeCombatSessionAudit(){}

  public static boolean verify(){
    if(!verifyEquipmentSemanticSubmission())return false;
    RuntimeState state=new RuntimeState(RuntimeState.BootMode.MILLES,true);
    if(state.monsters().isEmpty())return false;
    RuntimeState.Monster monster=state.monsters().get(0);
    boolean[] los={true},control={true},learned={false};
    RuntimeCombatSession session=new RuntimeCombatSession(
        state,(actor,target)->los[0],(actor,action)->learned[0],actor->control[0]);

    Set<String> ids=new HashSet<>(session.actionDefinitions().keySet());
    if(ids.size()!=AttackDef.PROTOTYPES.length+4)return false;
    for(int i=0;i<AttackDef.PROTOTYPES.length;i++)if(!ids.contains(RuntimeCombatSession.playerAttackActionId(i)))return false;
    if(!ids.contains(SkillDef.CAST_PROTO.id)||!ids.contains(SkillDef.SKILL_PROTO.id)||!ids.contains(SkillDef.KICK_PROTO.id))return false;

    state.player().x=monster.x;state.player().y=monster.y;
    control[0]=false;
    if(reason(session.submitPlayerAttack(monster.id,0))!=CombatResolver.RejectReason.CONTROL)return false;
    control[0]=true;
    int initialMp=state.player().mp,initialHp=monster.hp;
    if(reason(session.submitPlayer(monster.id,SkillDef.CAST_PROTO.id))!=CombatResolver.RejectReason.NOT_LEARNED)return false;
    if(state.player().mp!=initialMp||monster.hp!=initialHp)return false;

    learned[0]=true;
    los[0]=false;
    if(reason(session.submitPlayer(monster.id,SkillDef.CAST_PROTO.id))!=CombatResolver.RejectReason.LOS)return false;
    los[0]=true;
    state.player().mp=SkillDef.CAST_PROTO.mpCost-1;
    if(reason(session.submitPlayer(monster.id,SkillDef.CAST_PROTO.id))!=CombatResolver.RejectReason.RESOURCE)return false;
    state.player().mp=initialMp;
    state.player().x=monster.x+SkillDef.CAST_PROTO.range+1f;
    if(reason(session.submitPlayer(monster.id,SkillDef.CAST_PROTO.id))!=CombatResolver.RejectReason.RANGE)return false;
    state.player().x=monster.x;

    CombatActionOrchestrator.Submission cast=session.submitPlayer(monster.id,SkillDef.CAST_PROTO.id);
    if(!cast.accepted()||state.player().mp!=initialMp-SkillDef.CAST_PROTO.mpCost)return false;
    session.tick(.23f);
    if(monster.hp!=initialHp)return false; // animation/start never mutates HP
    RuntimeCombatSession.FrameResult castHit=session.tick(.01f);
    if(monster.hp!=initialHp-SkillDef.CAST_PROTO.damage||countEffects(castHit.events,cast.actionSequence)!=1)return false;
    if(countEffects(session.tick(.50f).events,cast.actionSequence)!=0)return false;
    if(reason(session.submitPlayer(monster.id,SkillDef.CAST_PROTO.id))!=CombatResolver.RejectReason.COOLDOWN)return false;

    session.tick(SkillDef.CAST_PROTO.cooldown);
    monster.hp=1;monster.alive=true;state.ledger().clear();
    int tokenBefore=quantity(state,AdaptedPrototypeRewardCatalog.TRAINING_TOKEN_ITEM_ID);
    CombatActionOrchestrator.Submission attack=session.submitPlayerAttack(monster.id,1);
    if(!attack.accepted())return false;
    session.tick(.18f);
    if(monster.alive)return false;
    if(countLedger(state.ledger().snapshot(),CombatLedger.Type.MONSTER_DEFEATED)!=1)return false;
    state.tick(.01f);
    if(quantity(state,AdaptedPrototypeRewardCatalog.TRAINING_TOKEN_ITEM_ID)!=tokenBefore+1)return false;
    if(reason(session.submitPlayerAttack(monster.id,1))!=CombatResolver.RejectReason.TARGET_DEAD)return false;
    state.tick(.01f);
    return quantity(state,AdaptedPrototypeRewardCatalog.TRAINING_TOKEN_ITEM_ID)==tokenBefore+1;
  }

  private static boolean verifyEquipmentSemanticSubmission(){
    RuntimeState unarmedState=new RuntimeState(RuntimeState.BootMode.MILLES,true);
    RuntimeState.Monster unarmedTarget=unarmedState.monsters().get(0);
    unarmedState.player().x=unarmedTarget.x;unarmedState.player().y=unarmedTarget.y;
    RuntimeCombatSession unarmedSession=new RuntimeCombatSession(
        unarmedState,(actor,target)->true,(actor,action)->false,actor->true);
    RuntimeCombatSession.PlayerActionSubmission punch=unarmedSession.submitPlayerBasicAttack(unarmedTarget.id);
    if(!punch.combat.accepted()||punch.animationAction!=AnimationAction.PUNCH||punch.weaponAppearanceId!=null)return false;
    if(!RuntimeCombatSession.playerAttackActionId(AnimationAction.PUNCH).equals(punch.combat.actionId))return false;

    RuntimeState armedState=new RuntimeState(RuntimeState.BootMode.MILLES,true);
    if(armedState.rpg().equip(RpgProgressionState.PLAYTEST_ROBE_ITEM_ID)!=RpgProgressionState.EquipResult.EQUIPPED)return false;
    if(armedState.rpg().equip(RpgProgressionState.PLAYTEST_WEAPON_ITEM_ID)!=RpgProgressionState.EquipResult.EQUIPPED)return false;
    RuntimeState.Monster armedTarget=armedState.monsters().get(0);
    armedState.player().x=armedTarget.x;armedState.player().y=armedTarget.y;
    RuntimeCombatSession armedSession=new RuntimeCombatSession(
        armedState,(actor,target)->true,(actor,action)->false,actor->true);
    RuntimeCombatSession.PlayerActionSubmission swing=armedSession.submitPlayerBasicAttack(armedTarget.id);
    if(!swing.combat.accepted()||swing.animationAction!=AnimationAction.SWING)return false;
    if(!RpgProgressionState.PLAYTEST_WEAPON_APPEARANCE_ID.equals(swing.weaponAppearanceId))return false;
    if(!RuntimeCombatSession.playerAttackActionId(AnimationAction.SWING).equals(swing.combat.actionId))return false;
    return RpgProgressionState.PLAYTEST_ROBE_ITEM_ID.equals(armedState.rpg().equipment().get(RpgProgressionState.ARMOR_SLOT));
  }

  private static CombatResolver.RejectReason reason(CombatActionOrchestrator.Submission submission){return submission.rejectReason;}
  private static int countEffects(List<CombatResolver.Event> events,long actionSequence){int n=0;for(CombatResolver.Event event:events)if(event.actionSequence==actionSequence&&event.type==CombatResolver.EventType.EFFECT_APPLIED)n++;return n;}
  private static int countLedger(List<CombatLedger.Event> events,CombatLedger.Type type){int n=0;for(CombatLedger.Event event:events)if(event.type==type)n++;return n;}
  private static int quantity(RuntimeState state,String itemId){Integer n=state.rpg().inventory().get(itemId);return n==null?0:n;}

  public static void main(String[] args){
    if(!verify())throw new AssertionError("RuntimeCombatSessionAudit failed");
    System.out.println("RuntimeCombatSessionAudit PASS");
  }
}
