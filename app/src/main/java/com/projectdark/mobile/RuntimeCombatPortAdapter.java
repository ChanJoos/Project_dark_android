package com.projectdark.mobile;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Combat-owned adapter exposing current RuntimeState facts/mutations to CombatResolver.
 *
 * RuntimeState remains the HP/MP/ledger authority. This adapter owns only per-action cooldown clocks
 * required by the resolver contract. LOS and learned-action truth are injected by their owning domains.
 */
public final class RuntimeCombatPortAdapter implements CombatResolver.Port {
  public interface LineOfSightPort {
    boolean hasLineOfSight(String actorId,String targetId);
  }

  public interface LearnedActionPort {
    boolean learned(String actorId,String actionId);
  }

  public interface ControlPort {
    boolean canAct(String actorId);
  }

  private final RuntimeState state;
  final SkillAbilityExecutor abilities;
  private final Map<String,SkillAbilityExecutor> companionAbilities=new HashMap<>();
  SkillAbilityExecutor executor(String actor){return "player".equals(actor)?abilities:companionAbilities.computeIfAbsent(actor,id->new SkillAbilityExecutor(state,id));}
  boolean friendly(String id){return state.party().friendly(id);}
  RuntimeState.Player body(String id){return state.party().body(state,id);}
  FinalStats friendlyStats(String id){return state.party().stats(state,id);}
  public void setSkillTestMode(boolean value){abilities.testMode=value;}
  public void setSkillProficiency(java.util.function.ToIntFunction<String> p){abilities.proficiency=p;}
  public void setSkillMovement(SkillAbilityExecutor.Movement m){abilities.movement=m;}
  private java.util.function.Supplier<CharacterRenderer.Direction> playerFacing;
  public void setPlayerFacing(java.util.function.Supplier<CharacterRenderer.Direction> facing){playerFacing=facing;}
  CharacterRenderer.Direction facing(){return playerFacing==null?CharacterRenderer.Direction.SE:playerFacing.get();}
  String frontTarget(String id){return MartialSkillPolicy.frontTarget(state,id,facing(),this::visible);}
  public String takeSkillNotice(){return abilities.takeNotice();}
  public CombatResolver.RejectReason actionReady(String actor,String target,String id,boolean begin){
    if(!"player".equals(actor)&&"player".equals(target)&&(state.skillEffects().hidden()||state.skillEffects().has(actor,"AGGRO_RESET")&&!state.skillEffects().has(actor,"TAUNT")))return CombatResolver.RejectReason.CONTROL;
    if("player".equals(actor)&&RogueSkillPolicy.sense(id)&&!RogueSkillPolicy.valid(state,target,facing(),this::visible))return CombatResolver.RejectReason.RANGE;
    if("player".equals(actor)&&RogueSkillPolicy.ray(id)&&!RogueSkillPolicy.validRay(state,id,target,facing(),this::visible))return CombatResolver.RejectReason.RANGE;
    if(begin&&"player".equals(actor)&&MagicTimingPolicy.managed(id)&&spellIntervalRemaining>0)return CombatResolver.RejectReason.ACTION_BUSY;
    if("player".equals(actor)&&isNpc(target)&&!MartialSkillPolicy.leap(id))return CombatResolver.RejectReason.TARGET_DEAD;
    if("player".equals(actor)&&MartialSkillPolicy.leap(id)&&playerFacing!=null&&!java.util.Objects.equals(target,frontTarget(id)))return CombatResolver.RejectReason.RANGE;
    CompanionParty.Member companionActor=state.party().find(actor);if(begin&&companionActor!=null&&MagicTimingPolicy.managed(id)&&companionActor.cooldown("party_spell_interval")>0)return CombatResolver.RejectReason.ACTION_BUSY;
    CombatResolver.RejectReason r=friendly(actor)?executor(actor).ready(actor,target,id,begin):null;if(r!=null)return r;
    SkillAbilityCatalog.Ability a=SkillAbilityCatalog.get(id);
    if(friendly(actor)&&!body(actor).alive)return CombatResolver.RejectReason.ACTOR_DEAD;
    if(state.party().find(actor)!=null&&!state.player().alive)return CombatResolver.RejectReason.CONTROL;
    if(state.party().find(actor)!=null&&id.startsWith("attack_proto_")&&state.party().holdAttack(actor,target))return CombatResolver.RejectReason.CONTROL;
    if(a!=null&&friendly(actor)){
      if(a.damage()&&friendly(target)&&!(actor.equals(target)&&SkillActionContract.get(id)!=null&&SkillActionContract.get(id).selfAnchored())||!a.damage()&&!friendly(target)&&a.heal())return CombatResolver.RejectReason.CONTROL;
      if(CompanionPartyRules.allySupport(id)&&!friendly(target))return CombatResolver.RejectReason.CONTROL;
      if(!a.damage()&&!a.heal()&&!CompanionPartyRules.allySupport(id)&&friendly(target)&&!actor.equals(target))return CombatResolver.RejectReason.CONTROL;
      if(state.party().find(actor)!=null&&a.damage()&&state.party().holdAttack(actor,target))return CombatResolver.RejectReason.CONTROL;
    }
    if(begin&&a!=null&&a.damage()&&recipients(actor,target,id).isEmpty())return CombatResolver.RejectReason.TARGET_DEAD;
    return null;
  }
  public CombatResolver.Definition resolveDefinition(String actor,CombatResolver.Definition d){
    AbelCombatProfile profile=AbelCombatProfile.forId(actor);if(profile!=null)return profile.resolve(d);
    if("player".equals(actor)&&d.actionId.startsWith("attack_proto_"))return new CombatResolver.Definition(d.actionId,d.kind,d.state,d.effectType,d.requiresLearned,d.resourceCost,BasicAttackTiming.cycle(state.rpg(),d.cooldown),d.range,BasicAttackTiming.contact(state.rpg(),d.hitTime),d.damage);
    if(state.party().find(actor)!=null){SkillAbilityCatalog.Ability a=SkillAbilityCatalog.get(d.actionId);return a==null?d:new CombatResolver.Definition(d.actionId,d.kind,d.state,d.effectType,d.requiresLearned,a.mpCost,a.cooldown,d.range,d.hitTime,d.damage);}
    if(!"player".equals(actor)||d.kind!=CombatResolver.ActionKind.MAGIC)return d;
    SkillAbilityCatalog.Ability a=SkillAbilityCatalog.get(d.actionId);
    int base=a==null?d.resourceCost:a.mpCost;
    float time=ItemEffects.castSeconds(state.rpg(),d.actionId,ItemEffects.baseCast(d.actionId,d.hitTime));
    return new CombatResolver.Definition(d.actionId,d.kind,d.state,d.effectType,d.requiresLearned,ItemEffects.mpCost(state.rpg(),d.actionId,base),d.cooldown,d.range,time,d.damage);
  }
  public void prepareAction(String actor,String id){if(friendly(actor))executor(actor).prepare(actor,id);}
  public void finishAction(String actor,String id){if(friendly(actor))executor(actor).finish(actor,id);directions.remove(actor+":"+id);state.party().finishReservation(actor,id);}
  private final LineOfSightPort lineOfSight;
  private final LearnedActionPort learnedActions;
  private final ControlPort control;
  private final Map<String,Float> cooldowns=new HashMap<>();
  private float spellIntervalRemaining;
  private final Map<String,float[]> directions=new HashMap<>();
  public void lockDirection(String actor,String target,String id){Position a=position(actor),t=position(target);if(MartialSkillPolicy.facingOnly(id)&&(playerFacing!=null||actor.equals(target)))directions.put(actor+":"+id,MartialSkillPolicy.step(facing()));else if(a!=null&&t!=null)directions.put(actor+":"+id,new float[]{t.x-a.x,t.y-a.y});}
  private java.util.function.Predicate<String> visible=id->true;
  private java.util.function.IntSupplier basicHits=()->1;
  public void setBasicHits(java.util.function.IntSupplier hits){basicHits=hits;}
  public int hitCount(String actor,String action){return "player".equals(actor)&&action.startsWith("attack_proto_")?Math.max(1,Math.min(3,basicHits.getAsInt())):1;}
  public int contactHitCount(String actor,String target,String action,int initial){return "player".equals(actor)&&MartialSourceV228.MUYEONG.equals(action)?Math.max(1,recipients(actor,target,action).size()):initial;}
  public float hitSpacing(String actor,String action){return "player".equals(actor)&&action.startsWith("attack_proto_")?0f:BasicAttackTiming.HIT_SPACING;}
  public void setVisible(java.util.function.Predicate<String> visible){this.visible=visible;}
  public boolean visible(String id){return isNpc(id)||visible.test(id);}
  public java.util.List<String> recipients(String actor,String target,String action){
    SkillActionContract.Rule r=SkillActionContract.get(action);
    if(r==null||!friendly(actor))return java.util.Collections.singletonList(target);
    Position a=position(actor),t=position(target);java.util.List<String> out=new java.util.ArrayList<>();
    if(a==null||t==null)return out;
    if(CompanionPartyRules.allySupport(action)&&r.pattern!=SkillActionContract.Pattern.GROUP)return friendly(target)&&distance(actor,target)<=192&&hasLineOfSight(actor,target)?java.util.Collections.singletonList(target):out;
    if(MartialSkillPolicy.leap(action))return targetAlive(target)?java.util.Collections.singletonList(target):out;
    if(RogueSkillPolicy.ray(action))return RogueSkillPolicy.validRay(state,action,target,facing(),this::visible)?java.util.Collections.singletonList(target):out;
    if(r.pattern==SkillActionContract.Pattern.SINGLE)return visible(target)&&SkillActionContract.canStart(r,a.x,a.y,t.x,t.y,true)?java.util.Collections.singletonList(target):out;
    if(r.pattern==SkillActionContract.Pattern.ALLY||r.pattern==SkillActionContract.Pattern.GROUP){
      if(r.pattern==SkillActionContract.Pattern.GROUP){out.add("player");CompanionParty.Member m=state.party().active();if(m!=null&&m.body.alive&&distance(actor,m.id)<=192&&hasLineOfSight(actor,m.id))out.add(m.id);}
      else if(friendly(target)&&distance(actor,target)<=192&&hasLineOfSight(actor,target))out.add(target);return out;
    }
    if(r.pattern==SkillActionContract.Pattern.SELF){out.add(actor);return out;}
    float tx=t.x,ty=t.y;float[] intent=directions.get(actor+":"+action);
    if(intent==null&&MartialSourceV228.line(action)&&(playerFacing!=null||actor.equals(target)))intent=MartialSkillPolicy.step(facing());
    if(intent!=null&&(r.pattern==SkillActionContract.Pattern.FRONT||r.pattern==SkillActionContract.Pattern.FRONT_BACK)){tx=a.x+intent[0];ty=a.y+intent[1];}
    for(RuntimeState.Monster m:state.monsters())if(m.alive&&visible(m.id)&&SkillActionContract.includes(r,a.x,a.y,tx,ty,m.x,m.y,true)&&hasActionLineOfSight(actor,m.id,action))out.add(m.id);
    return out;
  }

  public RuntimeCombatPortAdapter(RuntimeState state,LineOfSightPort lineOfSight,LearnedActionPort learnedActions){
    this(state,lineOfSight,learnedActions,actorId->true);
  }

  public RuntimeCombatPortAdapter(RuntimeState state,LineOfSightPort lineOfSight,
      LearnedActionPort learnedActions,ControlPort control){
    if(state==null||lineOfSight==null||learnedActions==null||control==null)throw new IllegalArgumentException("runtime combat dependencies");
    this.state=state;
    abilities=new SkillAbilityExecutor(state);
    this.lineOfSight=lineOfSight;
    this.learnedActions=learnedActions;
    this.control=control;
  }

  public void tick(float dt){
    spellIntervalRemaining=Math.max(0,spellIntervalRemaining-Math.max(0,dt));
    abilities.tick(Math.max(0,dt));
    if(dt<=0f||cooldowns.isEmpty())return;
    Iterator<Map.Entry<String,Float>> it=cooldowns.entrySet().iterator();
    while(it.hasNext()){
      Map.Entry<String,Float> entry=it.next();
      float next=Math.max(0f,entry.getValue()-dt);
      if(next<=0f)it.remove(); else entry.setValue(next);
    }
  }

  public boolean actorAlive(String actorId){return entityAlive(actorId);}
  public boolean targetAlive(String targetId){return entityAlive(targetId)||isNpc(targetId);}
  public boolean canAct(String actorId){return !state.skillEffects().disabled(actorId)&&control.canAct(actorId);}
  public boolean learned(String actorId,String actionId){CompanionParty.Member m=state.party().find(actorId);return m!=null?m.learned(actionId):learnedActions.learned(actorId,actionId);}
  public boolean cooldownReady(String actorId,String actionId){return cooldownRemaining(actorId,actionId)<=0f;}

  public boolean hasResource(String actorId,int amount){
    if(abilities.testMode&&"player".equals(actorId)||amount<=0)return true;
    if("player".equals(actorId))return state.player().alive&&state.player().mp>=amount;
    if(state.party().find(actorId)!=null)return body(actorId).alive&&body(actorId).mp>=amount;
    // Current monster runtime has no canonical MP/resource pool. Resource-costing monster actions fail closed.
    RuntimeState.Monster m=findMonster(actorId);return AbelCombatProfile.forId(actorId)!=null&&m!=null&&m.alive&&m.mp>=amount;
  }

  public float distance(String actorId,String targetId){
    Position a=position(actorId),b=position(targetId);
    if(a==null||b==null)return Float.POSITIVE_INFINITY;
    float dx=a.x-b.x,dy=a.y-b.y;
    return (float)Math.sqrt(dx*dx+dy*dy);
  }

  public boolean hasLineOfSight(String actorId,String targetId){return actorId.equals(targetId)||lineOfSight.hasLineOfSight(actorId,targetId);}
  /** Screen spells are viewport effects, not rays through terrain; physical and targeted actions retain LOS. */
  public boolean hasActionLineOfSight(String actor,String target,String action){
    SkillActionContract.Rule r=SkillActionContract.get(action);
    return "player".equals(actor)&&r!=null&&r.pattern==SkillActionContract.Pattern.SCREEN&&"마법".equals(r.kind)||hasLineOfSight(actor,target);
  }

  public void consumeResource(String actorId,int amount){
    if(abilities.testMode&&"player".equals(actorId)||amount<=0)return;
    if(state.party().find(actorId)!=null){if(body(actorId).mp<amount)throw new IllegalStateException("companion MP gate");body(actorId).mp-=amount;return;}
    if(!"player".equals(actorId)){RuntimeState.Monster m=findMonster(actorId);if(AbelCombatProfile.forId(actorId)==null||m==null||m.mp<amount)throw new IllegalStateException("monster MP gate");m.mp-=amount;return;}
    RuntimeState.Player player=state.player();
    if(!player.alive||player.mp<amount)throw new IllegalStateException("resource commit without validation");
    player.mp-=amount;
  }

  public void commitCooldown(String actorId,String actionId,float cooldown){
    if("player".equals(actorId)&&MagicTimingPolicy.managed(actionId))spellIntervalRemaining=MagicTimingPolicy.SPELL_INTERVAL;
    if(actorId==null||actionId==null)throw new IllegalArgumentException("cooldown identity");
    CompanionParty.Member companion=state.party().find(actorId);if(companion!=null){companion.cooldown(actionId,cooldown);if(MagicTimingPolicy.managed(actionId))companion.cooldown("party_spell_interval",MagicTimingPolicy.SPELL_INTERVAL);}
    if(cooldown<=0f){cooldowns.remove(cooldownKey(actorId,actionId));return;}
    cooldowns.put(cooldownKey(actorId,actionId),cooldown);RuntimeState.Monster m=findMonster(actorId);if(m!=null&&AbelCombatProfile.forId(actorId)!=null)m.attackCooldown=cooldown;
  }

  private static String cooldownKey(String actor,String action){return key(actor,"player".equals(actor)&&action!=null&&action.startsWith("attack_proto_")?"player_basic_shared":action);}
  public float cooldownRemaining(String actorId,String actionId){
    CompanionParty.Member companion=state.party().find(actorId);if(companion!=null)return companion.cooldown(actionId);
    Float value=cooldowns.get(cooldownKey(actorId,actionId));
    return value==null?0f:Math.max(0f,value);
  }

  public CombatResolver.EffectResult applyDamage(String actorId,String targetId,String actionId,int amount){
    if(!"player".equals(actorId)&&"player".equals(targetId)&&state.skillEffects().hidden())return new CombatResolver.EffectResult(0,false,CombatResolver.DefeatPublication.PORT_ALREADY_PUBLISHED,CombatResolver.DefeatedTargetKind.PLAYER,CombatResolver.HitSemantic.STATUS);
    AbelCombatProfile profile=AbelCombatProfile.forId(actorId);
    if(profile!=null&&actionId.equals("MONSTER_RECOVER")){RuntimeState.Monster m=findMonster(actorId);if(m==null||!m.alive||m.bossRecovered)return new CombatResolver.EffectResult(0,false,CombatResolver.DefeatPublication.PORT_ALREADY_PUBLISHED,CombatResolver.DefeatedTargetKind.MONSTER,CombatResolver.HitSemantic.MISS);int before=m.hp;m.hp=Math.min(m.maxHp,m.hp+m.maxHp/8);m.bossRecovered=true;return new CombatResolver.EffectResult(m.hp-before,false,CombatResolver.DefeatPublication.PORT_ALREADY_PUBLISHED,CombatResolver.DefeatedTargetKind.MONSTER,CombatResolver.HitSemantic.HEAL);}
    SkillAbilityCatalog.Ability ability=SkillAbilityCatalog.get(actionId);
    boolean playerActor=friendly(actorId);
    SkillAbilityExecutor activeAbilities=playerActor?executor(actorId):abilities;
    if(playerActor&&ability!=null){
      CombatResolver.EffectResult effect=activeAbilities.apply(targetId,actionId);if(effect!=null)return effect;
      amount=activeAbilities.amount(actionId,amount);activeAbilities.chargeRelease(ability);
      if(MartialSourceV228.ADVANCE.equals(actionId)&&isNpc(targetId))return new CombatResolver.EffectResult(0,false,CombatResolver.DefeatPublication.PORT_ALREADY_PUBLISHED,CombatResolver.DefeatedTargetKind.OTHER,CombatResolver.HitSemantic.STATUS);
    }
    if(SkillRuntimeCatalog.healing(actionId)){
      if(!playerActor||!friendly(targetId)||!body(targetId).alive)throw new IllegalArgumentException("party heal target");
      RuntimeState.Player recipient=body(targetId);int before=recipient.hp;recipient.hp=(int)Math.min(recipient.maxHp,(long)before+amount);
      return new CombatResolver.EffectResult(recipient.hp-before,false,CombatResolver.DefeatPublication.PORT_ALREADY_PUBLISHED,CombatResolver.DefeatedTargetKind.PLAYER,CombatResolver.HitSemantic.HEAL);
    }
    CombatStatPipeline.Channel channel=(SkillRuntimeCatalog.magic(actionId)||AbelCombatProfile.spell(actionId))?CombatStatPipeline.Channel.MAGIC:CombatStatPipeline.Channel.PHYSICAL;
    FinalStats attacker=(playerActor?friendlyStats(actorId):monsterStats(actorId)).withEffects(state.skillEffects(),actorId);
    FinalStats defender=(friendly(targetId)?friendlyStats(targetId):monsterStats(targetId)).withEffects(state.skillEffects(),targetId);
    if(profile!=null&&AbelCombatProfile.spell(actionId)){String element=actionId.equals("MONSTER_FIRE")?"FIRE":actionId.equals("MONSTER_WATER")?"WATER":actionId.equals("MONSTER_ROOT")?"EARTH":attacker.attackElement;attacker=attacker.withAttackElement(element);}
    if(ability!=null){String element=skillElement(ability);if(!element.isEmpty())attacker=attacker.withAttackElement(element);}
    boolean basic=actionId.startsWith("attack_proto_");
    if(playerActor&&ability!=null){
      if(state.skillEffects().has(actorId,"FOCUS")&&(ability.formula.equals("CRASH")||ability.formula.equals("SOUL")))amount=safeScale(amount,2);
      if(!ability.fixed()&&state.skillEffects().has(actorId,"ELEMENT_BOOST"))amount=safeScale(amount,1.2);
      if(!ability.fixed())amount=CombatStatPipeline.resolveSkill(amount,channel,attacker,defender).applied;
    }else amount=(profile!=null&&AbelCombatProfile.spell(actionId)?CombatStatPipeline.resolveSkill(amount,channel,attacker,defender):CombatStatPipeline.resolve(amount,channel,attacker,defender)).applied;
    if(playerActor&&basic){if(state.skillEffects().has(actorId,"BASIC_POWER")&&abilities.random.getAsDouble()<.3)amount=safeScale(amount,2);}
    SkillEffectState fx=state.skillEffects();
    // Reflection redirects the computed incoming hit once, before other immunity; never recurses.
    if(!playerActor&&"player".equals(targetId)&&fx.has(targetId,"REFLECT_ALL")){
      RuntimeState.Monster reflected=findMonster(actorId);if(reflected!=null&&amount>0)state.damage(reflected,amount);
      return new CombatResolver.EffectResult(0,false,CombatResolver.DefeatPublication.PORT_ALREADY_PUBLISHED,CombatResolver.DefeatedTargetKind.PLAYER,CombatResolver.HitSemantic.STATUS);
    }
    if(ability==null||!ability.fixed()){
      if(fx.has(targetId,"INVINCIBLE")||channel==CombatStatPipeline.Channel.PHYSICAL&&fx.has(targetId,"PHYSICAL_GUARD")||!basic&&fx.has(targetId,"TECH_MAGIC_GUARD"))amount=0;
      else if(channel==CombatStatPipeline.Channel.MAGIC&&fx.has(targetId,"MAGIC_GUARD"))amount=safeScale(amount,.5);
      if(fx.has(targetId,"PROTECT"))amount=safeScale(amount,.7);
      if(fx.has(targetId,"SHIELD"))amount=Math.max(0,amount-defender.wis);
      double missChance=Math.max(0,(fx.power(targetId,"EVASION")+fx.power(actorId,"BLIND"))/100d-attacker.hit*.01);
      if(channel==CombatStatPipeline.Channel.PHYSICAL&&missChance>0&&abilities.random.getAsDouble()<Math.min(.9,missChance))amount=0;
      if(fx.has(actorId,"DISARM"))amount=safeScale(amount,.65);
      if(channel==CombatStatPipeline.Channel.MAGIC&&fx.has(targetId,"REFLECT_MAGIC")||!basic&&channel==CombatStatPipeline.Channel.PHYSICAL&&fx.has(targetId,"REFLECT_TECH")&&abilities.random.getAsDouble()<.7){if(playerActor){if("player".equals(actorId))state.damagePlayer(amount);else damageCompanion(actorId,amount);}else{RuntimeState.Monster reflected=findMonster(actorId);state.damage(reflected,amount);}amount=0;}
    }
    if(playerActor&&ability!=null&&ability.status.equals("FREEZE_HIT")&&entityAlive(targetId))activeAbilities.damageStatus(targetId,ability,amount);
    if(playerActor&&amount>0){fx.remove(actorId,"STEALTH");if(!basic&&ability!=null&&!ability.status.equals("FREEZE_HIT"))activeAbilities.damageStatus(targetId,ability,amount);fx.remove(targetId,"SLEEP");}
    if(amount<=0||!entityAlive(actorId)||!entityAlive(targetId))
      return new CombatResolver.EffectResult(0,false,CombatResolver.DefeatPublication.PORT_ALREADY_PUBLISHED,
          targetKind(targetId),ability!=null&&ability.status.equals("FREEZE_HIT")?CombatResolver.HitSemantic.STATUS:CombatResolver.HitSemantic.MISS);

    if(state.party().find(targetId)!=null){int before=body(targetId).hp;damageCompanion(targetId,amount);return new CombatResolver.EffectResult(before-body(targetId).hp,false,CombatResolver.DefeatPublication.PORT_ALREADY_PUBLISHED,CombatResolver.DefeatedTargetKind.OTHER,CombatResolver.HitSemantic.DAMAGE);}
    if("player".equals(targetId)){
      int before=state.player().hp;
      state.damagePlayer(amount);
      if(profile!=null&&state.player().alive&&AbelCombatProfile.spell(actionId)){String kind=actionId.equals("MONSTER_BLIND")?"BLIND":actionId.equals("MONSTER_ROOT")?"ROOT":actionId.equals("MONSTER_CURSE")?"CURSE":actionId.startsWith("MONSTER_VENOM")?"POISON":actionId.equals("MONSTER_FREEZE")?"FREEZE":null;if(kind!=null)state.skillEffects().put("player",actionId,kind,kind.equals("POISON")?2:kind.equals("CURSE")?10:kind.equals("BLIND")?20:0,kind.equals("FREEZE")?1.1f:kind.equals("ROOT")?1.5f:5f);}
      int applied=Math.max(0,before-state.player().hp);
      return new CombatResolver.EffectResult(applied,before>0&&!state.player().alive,
          CombatResolver.DefeatPublication.PORT_ALREADY_PUBLISHED,CombatResolver.DefeatedTargetKind.PLAYER,
          CombatResolver.HitSemantic.DAMAGE);
    }

    RuntimeState.Monster monster=findMonster(targetId);
    if(monster!=null){
      int before=monster.hp;
      if(state.party().find(actorId)!=null&&!monster.provoked)monster.attackTarget=actorId;
      state.damage(monster,amount);
      int applied=Math.max(0,before-monster.hp);
      if(playerActor&&ability!=null&&ability.status.equals("DRAIN"))body(actorId).hp=Math.min(body(actorId).maxHp,body(actorId).hp+applied);
      return new CombatResolver.EffectResult(applied,before>0&&!monster.alive,
          CombatResolver.DefeatPublication.PORT_ALREADY_PUBLISHED,CombatResolver.DefeatedTargetKind.MONSTER,
          ability!=null&&(ability.formula.equals("ASSASSIN")||ability.formula.equals("ASSASSIN_PLUS"))&&activeAbilities.snapCritical(actionId)?CombatResolver.HitSemantic.CRIT:CombatResolver.HitSemantic.DAMAGE);
    }

    return new CombatResolver.EffectResult(0,false,CombatResolver.DefeatPublication.PORT_ALREADY_PUBLISHED,
        CombatResolver.DefeatedTargetKind.OTHER,CombatResolver.HitSemantic.DAMAGE);
  }

  private void damageCompanion(String id,int amount){RuntimeState.Player b=body(id);if(b==null||!b.alive||amount<=0)return;b.hp=Math.max(0,b.hp-amount);b.alive=b.hp>0;b.hitFlash=.18f;}
  private FinalStats monsterStats(String actor){AbelCombatProfile p=AbelCombatProfile.forId(actor);RuntimeState.Monster m=findMonster(actor);FinalStats stats=p==null?FinalStats.neutral(m==null?100:m.maxHp,0):p.stats();return m==null?stats:stats.withElements(m.attackElement,m.defenseElement);}
  private boolean entityAlive(String id){
    if("player".equals(id))return state.player().alive;
    CompanionParty.Member companion=state.party().find(id);if(companion!=null)return companion.body.alive;
    RuntimeState.Monster monster=findMonster(id);
    return monster!=null&&monster.alive;
  }

  private Position position(String id){
    if("player".equals(id))return new Position(state.player().x,state.player().y);
    CompanionParty.Member companion=state.party().find(id);if(companion!=null)return new Position(companion.body.x,companion.body.y);
    RuntimeState.Monster monster=findMonster(id);
    if(monster!=null)return new Position(monster.x,monster.y);
    for(RuntimeState.Npc n:state.npcs())if(n.id.equals(id))return new Position(n.x,n.y);return null;
  }

  private boolean isNpc(String id){for(RuntimeState.Npc n:state.npcs())if(n.id.equals(id))return true;return false;}
  private RuntimeState.Monster findMonster(String id){
    if(id==null)return null;
    for(RuntimeState.Monster monster:state.monsters())if(id.equals(monster.id))return monster;
    return null;
  }

  private CombatResolver.DefeatedTargetKind targetKind(String targetId){
    if("player".equals(targetId))return CombatResolver.DefeatedTargetKind.PLAYER;
    return findMonster(targetId)==null?CombatResolver.DefeatedTargetKind.OTHER:CombatResolver.DefeatedTargetKind.MONSTER;
  }

  private static String skillElement(SkillAbilityCatalog.Ability a){String n=a.name;if(a.formula.equals("METEOR")||a.formula.equals("HOLY_DRAGON"))return "DARK";if(n.contains("마레")||n.equals("마네나로")||n.equals("아이스블러스트"))return "WATER";if(n.contains("테라")||n.equals("퀘이크"))return "EARTH";if(n.contains("아듀")||n.equals("플레쉬스톰")||n.equals("무영신공"))return "WIND";if(n.contains("플라")&&!n.startsWith("프라")||n.equals("플레어"))return "FIRE";return "";}
  private static int safeScale(int n,double scale){return (int)Math.min(Integer.MAX_VALUE,Math.max(0,Math.round(n*scale)));}
  private static String key(String actorId,String actionId){return String.valueOf(actorId)+'\u0000'+String.valueOf(actionId);}
  private static final class Position { final float x,y; Position(float x,float y){this.x=x;this.y=y;} }
}
