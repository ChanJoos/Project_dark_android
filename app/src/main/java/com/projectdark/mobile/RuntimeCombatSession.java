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

  public CombatActionOrchestrator.Submission submitPlayer(String targetId,String actionId){CombatActionOrchestrator.Submission result=actions.submitManual(PLAYER_ID,targetId,actionId);if(result.accepted())port.lockDirection(PLAYER_ID,targetId,actionId);return result;}
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

  public void setSkillTestMode(boolean enabled){port.setSkillTestMode(enabled);}
  public void setSkillProficiency(java.util.function.ToIntFunction<String> p){port.setSkillProficiency(p);}
  public void setSkillMovement(SkillAbilityExecutor.Movement m){port.setSkillMovement(m);}
  public String takeSkillNotice(){return port.takeSkillNotice();}
  public boolean useUtility(String id){SkillAbilityCatalog.Ability a=SkillAbilityCatalog.get(id);if(a==null||!a.supported()||!port.learned(PLAYER_ID,id)||!port.cooldownReady(PLAYER_ID,id)||port.actionReady(PLAYER_ID,PLAYER_ID,id,true)!=null)return false;port.prepareAction(PLAYER_ID,id);port.consumeResource(PLAYER_ID,a.mpCost);port.commitCooldown(PLAYER_ID,id,a.cooldown);port.applyDamage(PLAYER_ID,PLAYER_ID,id,0);port.finishAction(PLAYER_ID,id);return true;}
  public float cooldownRemaining(String actorId,String actionId){return port.cooldownRemaining(actorId,actionId);}
  public void setSkillVisibility(java.util.function.Predicate<String> visible){port.setVisible(visible);}
  public void setBasicHits(java.util.function.IntSupplier hits){port.setBasicHits(hits);}
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
    SkillActionContract.Rule rule=SkillActionContract.get(definition.actionId);
    if(rule!=null&&PLAYER_ID.equals(actorId)){
      RuntimeState.Monster target=findMonster(targetId);
      float tx=PLAYER_ID.equals(targetId)?state.player().x:target==null?Float.NaN:target.x;
      float ty=PLAYER_ID.equals(targetId)?state.player().y:target==null?Float.NaN:target.y;
      return SkillActionContract.canStart(rule,state.player().x,state.player().y,tx,ty,PLAYER_ID.equals(targetId)||port.visible(targetId))?null:CombatResolver.RejectReason.RANGE;
    }
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
    for(SkillDef d:SkillRuntimeCatalog.definitions()){
      if("SK_공통_001".equals(d.id))continue;
      boolean magic=d.actionClass==SkillDef.ActionClass.MAGIC,kick=d.effectType==SkillDef.EffectType.KICK_ARC;
      out.add(new CombatResolver.Definition(d.id,magic?CombatResolver.ActionKind.MAGIC:kick?CombatResolver.ActionKind.KICK:CombatResolver.ActionKind.SKILL,
          magic?CombatResolver.ActionState.MAGIC:kick?CombatResolver.ActionState.KICK:CombatResolver.ActionState.SKILL,
          magic?CombatResolver.EffectType.MAGIC_HIT:kick?CombatResolver.EffectType.KICK_HIT:CombatResolver.EffectType.SKILL_HIT,
          true,d.mpCost,d.cooldown,SkillActionContract.get(d.id)==null?d.range:Float.MAX_VALUE,SkillActionContract.get(d.id)==null?(magic?.24f:.14f):SkillActionContract.get(d.id).contact,d.damage));
    }
    for(SkillActionContract.Rule r:SkillActionContract.all())if(r.presentationAllowed()&&SkillRuntimeCatalog.get(r.id)==null){
      boolean magic="마법".equals(r.kind);
      out.add(new CombatResolver.Definition(r.id,magic?CombatResolver.ActionKind.MAGIC:CombatResolver.ActionKind.SKILL,magic?CombatResolver.ActionState.MAGIC:CombatResolver.ActionState.SKILL,magic?CombatResolver.EffectType.MAGIC_HIT:CombatResolver.EffectType.SKILL_HIT,true,0,1f,Float.MAX_VALUE,r.contact,0));
    }
    out.add(new CombatResolver.Definition(MONSTER_BASIC_ACTION_ID,CombatResolver.ActionKind.ATTACK,CombatResolver.ActionState.ATTACK,
        CombatResolver.EffectType.PHYSICAL_HIT,false,0,MonsterAIController.ATTACK_COOLDOWN_B,48f,.24f,MonsterAIController.ATTACK_DAMAGE_B));
    Map<String,CombatResolver.Definition> unique=new LinkedHashMap<>();
    for(CombatResolver.Definition definition:out)if(unique.put(definition.actionId,definition)!=null)throw new IllegalStateException("duplicate combat action id");
    return Collections.unmodifiableList(out);
  }
}
