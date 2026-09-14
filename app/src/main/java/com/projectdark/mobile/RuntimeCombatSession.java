package com.projectdark.mobile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Game-owned façade for one RuntimeState combat session. */
public final class RuntimeCombatSession {
  public static final String PLAYER_ID="player";
  public static final String MONSTER_BASIC_ACTION_ID="monster_basic_attack_b";

  public static final class FrameResult {
    public final List<CombatResolver.Event> events;public final List<CombatResolver.ActionSnapshot> actions;
    FrameResult(List<CombatResolver.Event> e,List<CombatResolver.ActionSnapshot> a){events=e;actions=a;}
  }
  private final RuntimeState state;private final RuntimeCombatPortAdapter port;private final CombatResolver resolver;private final CombatActionOrchestrator actions;private final EquipmentActionResolver equipmentActions=new EquipmentActionResolver();private long lastLedgerSequence;
  public RuntimeCombatSession(RuntimeState state,RuntimeCombatPortAdapter.LineOfSightPort los,RuntimeCombatPortAdapter.LearnedActionPort learned,RuntimeCombatPortAdapter.ControlPort control){if(state==null)throw new IllegalArgumentException("runtime state");this.state=state;port=new RuntimeCombatPortAdapter(state,los,learned,control);resolver=new CombatResolver(port);actions=new CombatActionOrchestrator(resolver,definitions());}
  public static RuntimeCombatPortAdapter.LearnedActionPort startingCommonerLearnedActions(){return (actorId,actionId)->false;}
  public CombatActionOrchestrator.Submission submitPlayer(String targetId,String actionId){return actions.submitManual(PLAYER_ID,targetId,actionId);}
  public CombatActionOrchestrator.Submission submitPlayerAttack(String targetId,int attackMode){return submitPlayer(targetId,playerAttackActionId(attackMode));}
  public PlayerActionSubmission submitPlayerBasicAttack(String targetId){EquipmentActionResolver.Result p=equipmentActions.resolveBasicAttack(state.rpg());return new PlayerActionSubmission(p,submitPlayer(targetId,playerAttackActionId(p.animationAction)));}
  public static final class PlayerActionSubmission {public final String weaponAppearanceId;public final AnimationAction animationAction;public final String appearanceEvidence,actionEvidence;public final CombatActionOrchestrator.Submission combat;PlayerActionSubmission(EquipmentActionResolver.Result p,CombatActionOrchestrator.Submission c){weaponAppearanceId=p.weaponAppearanceId;animationAction=p.animationAction;appearanceEvidence=p.appearanceEvidence;actionEvidence=p.actionEvidence;combat=c;}}
  public MonsterAutoCombatBridge monsterAutoBridge(){return new MonsterAutoCombatBridge(actions,MONSTER_BASIC_ACTION_ID);}
  public FrameResult tick(float deltaSeconds){float dt=Math.max(0f,deltaSeconds);port.tick(dt);actions.tick(dt);synchronizeRespawns();return new FrameResult(actions.drainEvents(),actions.actionSnapshots());}
  public float cooldownRemaining(String actorId,String actionId){return port.cooldownRemaining(actorId,actionId);}public Map<String,CombatResolver.Definition> actionDefinitions(){return actions.definitions();}public boolean playerActionActive(){return resolver.actionActive(PLAYER_ID);}
  public static String playerAttackActionId(int attackMode){AttackDef d=AttackDef.at(attackMode);return "attack_proto_"+Math.floorMod(attackMode,AttackDef.PROTOTYPES.length)+'_'+d.kind.name().toLowerCase();}
  public static String playerAttackActionId(AnimationAction action){AttackDef.Kind kind;switch(action){case THRUST:kind=AttackDef.Kind.THRUST;break;case THROW:kind=AttackDef.Kind.THROW;break;case SWING:kind=AttackDef.Kind.SWING;break;case PUNCH:kind=AttackDef.Kind.PUNCH;break;default:throw new IllegalArgumentException("not a basic attack semantic: "+action);}for(int i=0;i<AttackDef.PROTOTYPES.length;i++)if(AttackDef.at(i).kind==kind)return playerAttackActionId(i);throw new IllegalStateException("missing attack definition: "+kind);}
  private void synchronizeRespawns(){for(CombatLedger.Event event:state.ledger().snapshot()){if(event.sequence<=lastLedgerSequence)continue;lastLedgerSequence=event.sequence;if(event.type==CombatLedger.Type.MONSTER_RESPAWNED)resolver.onTargetRespawned(event.targetId);}}
  private static List<CombatResolver.Definition> definitions(){
    List<CombatResolver.Definition> out=new ArrayList<>();for(int i=0;i<AttackDef.PROTOTYPES.length;i++)out.add(CombatResolver.attackPrototype(AttackDef.at(i),i));out.add(CombatResolver.magicPrototype());out.add(CombatResolver.skillPrototype());out.add(CombatResolver.kickPrototype());
    // Resolver still carries a scalar compatibility range, but MonsterAIController now fail-closes
    // on CanonicalMeleeTileContract before either legacy or shared-resolver submission.
    out.add(new CombatResolver.Definition(MONSTER_BASIC_ACTION_ID,CombatResolver.ActionKind.ATTACK,CombatResolver.ActionState.ATTACK,CombatResolver.EffectType.PHYSICAL_HIT,false,0,MonsterAIController.ATTACK_COOLDOWN_B,CanonicalMeleeTileContract.REACH_DISTANCE,.24f,MonsterAIController.ATTACK_DAMAGE_B));
    Map<String,CombatResolver.Definition> unique=new LinkedHashMap<>();for(CombatResolver.Definition d:out)if(unique.put(d.actionId,d)!=null)throw new IllegalStateException("duplicate combat action id");return Collections.unmodifiableList(out);
  }
}
