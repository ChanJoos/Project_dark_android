package com.projectdark.mobile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Game-owned façade for one RuntimeState combat session.
 *
 * Director owns input wiring. It submits one stable action id here, calls tick once per frame, and
 * consumes the returned events once. HP/MP/cooldown and defeat publication must not be duplicated
 * in GameView. RuntimeState's CombatLedger remains the defeat/reward authority.
 */
public final class RuntimeCombatSession {
  public static final String PLAYER_ID="player";
  public static final String MONSTER_BASIC_ACTION_ID="monster_basic_attack_b";

  public static final class FrameResult {
    public final List<CombatResolver.Event> events;
    public final List<CombatResolver.ActionSnapshot> actions;
    FrameResult(List<CombatResolver.Event> events,List<CombatResolver.ActionSnapshot> actions){this.events=events;this.actions=actions;}
  }

  private final RuntimeState state;
  private final RuntimeCombatPortAdapter port;
  private final CombatResolver resolver;
  private final CombatActionOrchestrator actions;
  private final EquipmentActionResolver equipmentActions=new EquipmentActionResolver();
  private long lastLedgerSequence;

  public RuntimeCombatSession(RuntimeState state,
      RuntimeCombatPortAdapter.LineOfSightPort lineOfSight,
      RuntimeCombatPortAdapter.LearnedActionPort learnedActions,
      RuntimeCombatPortAdapter.ControlPort control){
    if(state==null)throw new IllegalArgumentException("runtime state");
    this.state=state;
    port=new RuntimeCombatPortAdapter(state,lineOfSight,learnedActions,control);
    resolver=new CombatResolver(port,(a,t,d)->canonicalMeleeRejectReason(a,t,d,CombatResolver.InputMode.MANUAL));
    actions=new CombatActionOrchestrator(resolver,definitions(),this::canonicalMeleeRejectReason);
  }

  /** Fail closed for the starting commoner: no skill or magic fixture is learned implicitly. */
  public static RuntimeCombatPortAdapter.LearnedActionPort startingCommonerLearnedActions(){return (actorId,actionId)->false;}

  public CombatActionOrchestrator.Submission submitPlayer(String targetId,String actionId){return actions.submitManual(PLAYER_ID,targetId,actionId);}
  public CombatActionOrchestrator.Submission submitPlayerAttack(String targetId,int attackMode){return submitPlayer(targetId,playerAttackActionId(attackMode));}

  /** Resolves the equipped weapon semantic before submitting one shared Resolver action. */
  public PlayerActionSubmission submitPlayerBasicAttack(String targetId){
    EquipmentActionResolver.Result presentation=equipmentActions.resolveBasicAttack(state.rpg());
    return new PlayerActionSubmission(presentation,submitPlayer(targetId,playerAttackActionId(presentation.animationAction)));
  }

  /** Stable Game -> Director/Visual DTO: source appearance identity plus semantic action, never frame numbers. */
  public static final class PlayerActionSubmission {
    public final String weaponAppearanceId; public final AnimationAction animationAction;
    public final String appearanceEvidence,actionEvidence; public final CombatActionOrchestrator.Submission combat;
    PlayerActionSubmission(EquipmentActionResolver.Result presentation,CombatActionOrchestrator.Submission combat){
      weaponAppearanceId=presentation.weaponAppearanceId;animationAction=presentation.animationAction;
      appearanceEvidence=presentation.appearanceEvidence;actionEvidence=presentation.actionEvidence;this.combat=combat;
    }
  }

  public MonsterAutoCombatBridge monsterAutoBridge(){return new MonsterAutoCombatBridge(actions,MONSTER_BASIC_ACTION_ID);}

  /** The only per-frame combat tick: cooldowns, pending hit frames, respawn sync, then one drain. */
  public FrameResult tick(float deltaSeconds){
    float dt=Math.max(0f,deltaSeconds); port.tick(dt); actions.tick(dt); synchronizeRespawns();
    List<CombatResolver.Event> events=actions.drainEvents(); return new FrameResult(events,actions.actionSnapshots());
  }

  public float cooldownRemaining(String actorId,String actionId){return port.cooldownRemaining(actorId,actionId);}
  public Map<String,CombatResolver.Definition> actionDefinitions(){return actions.definitions();}
  public boolean playerActionActive(){return resolver.actionActive(PLAYER_ID);}

  public static String playerAttackActionId(int attackMode){
    AttackDef def=AttackDef.at(attackMode);
    return "attack_proto_"+Math.floorMod(attackMode,AttackDef.PROTOTYPES.length)+'_'+def.kind.name().toLowerCase();
  }

  public static String playerAttackActionId(AnimationAction action){
    AttackDef.Kind kind;
    switch(action){case THRUST:kind=AttackDef.Kind.THRUST;break;case THROW:kind=AttackDef.Kind.THROW;break;case SWING:kind=AttackDef.Kind.SWING;break;case PUNCH:kind=AttackDef.Kind.PUNCH;break;default:throw new IllegalArgumentException("not a basic attack semantic: "+action);}
    for(int i=0;i<AttackDef.PROTOTYPES.length;i++)if(AttackDef.at(i).kind==kind)return playerAttackActionId(i);
    throw new IllegalStateException("missing attack definition: "+kind);
  }

  /** One shared legality gate prevents Resolver broad-phase distance from becoming a second melee contract. */
  private CombatResolver.RejectReason canonicalMeleeRejectReason(String actorId,String targetId,CombatResolver.Definition definition,CombatResolver.InputMode mode){
    if(!isCanonicalMeleeAction(definition.actionId))return null;
    float ax,ay,tx,ty;
    if(PLAYER_ID.equals(actorId)){
      RuntimeState.Monster target=findMonster(targetId); if(target==null)return null;
      ax=state.player().x;ay=state.player().y;tx=target.x;ty=target.y;
    }else if(PLAYER_ID.equals(targetId)){
      RuntimeState.Monster actor=findMonster(actorId); if(actor==null)return null;
      ax=actor.x;ay=actor.y;tx=state.player().x;ty=state.player().y;
    }else return null;
    return CanonicalMeleeTileContract.reachable(ax,ay,tx,ty)?null:CombatResolver.RejectReason.RANGE;
  }

  private RuntimeState.Monster findMonster(String id){
    if(id==null)return null;
    for(RuntimeState.Monster monster:state.monsters())if(id.equals(monster.id))return monster;
    return null;
  }

  private static boolean isCanonicalMeleeAction(String actionId){
    if(MONSTER_BASIC_ACTION_ID.equals(actionId))return true;
    for(int i=0;i<AttackDef.PROTOTYPES.length;i++){
      AttackDef def=AttackDef.at(i);
      if(CanonicalMeleeTileContract.isMelee(def.kind)&&playerAttackActionId(i).equals(actionId))return true;
    }
    return false;
  }

  private void synchronizeRespawns(){
    for(CombatLedger.Event event:state.ledger().snapshot()){
      if(event.sequence<=lastLedgerSequence)continue; lastLedgerSequence=event.sequence;
      if(event.type==CombatLedger.Type.MONSTER_RESPAWNED)resolver.onTargetRespawned(event.targetId);
    }
  }

  private static List<CombatResolver.Definition> definitions(){
    List<CombatResolver.Definition> out=new ArrayList<>();
    for(int i=0;i<AttackDef.PROTOTYPES.length;i++)out.add(CombatResolver.attackPrototype(AttackDef.at(i),i));
    out.add(CombatResolver.magicPrototype()); out.add(CombatResolver.skillPrototype()); out.add(CombatResolver.kickPrototype());
    out.add(new CombatResolver.Definition(MONSTER_BASIC_ACTION_ID,CombatResolver.ActionKind.ATTACK,CombatResolver.ActionState.ATTACK,
        CombatResolver.EffectType.PHYSICAL_HIT,false,0,MonsterAIController.ATTACK_COOLDOWN_B,48f,.24f,MonsterAIController.ATTACK_DAMAGE_B));
    Map<String,CombatResolver.Definition> unique=new LinkedHashMap<>();
    for(CombatResolver.Definition definition:out)if(unique.put(definition.actionId,definition)!=null)throw new IllegalStateException("duplicate combat action id");
    return Collections.unmodifiableList(out);
  }
}
