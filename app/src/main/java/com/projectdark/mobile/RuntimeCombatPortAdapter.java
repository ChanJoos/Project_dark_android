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
  public void setSkillTestMode(boolean value){abilities.testMode=value;}
  public void setSkillProficiency(java.util.function.ToIntFunction<String> p){abilities.proficiency=p;}
  public void setSkillMovement(SkillAbilityExecutor.Movement m){abilities.movement=m;}
  public String takeSkillNotice(){return abilities.takeNotice();}
  public CombatResolver.RejectReason actionReady(String actor,String target,String id,boolean begin){
    CombatResolver.RejectReason r=abilities.ready(actor,target,id,begin);if(r!=null)return r;
    SkillAbilityCatalog.Ability a=SkillAbilityCatalog.get(id);
    if(begin&&a!=null&&a.damage()&&recipients(actor,target,id).isEmpty())return CombatResolver.RejectReason.TARGET_DEAD;
    return null;
  }
  public CombatResolver.Definition resolveDefinition(String actor,CombatResolver.Definition d){
    if(!"player".equals(actor)||d.kind!=CombatResolver.ActionKind.MAGIC)return d;
    SkillAbilityCatalog.Ability a=SkillAbilityCatalog.get(d.actionId);
    int base=a==null?d.resourceCost:a.mpCost;
    float time=ItemEffects.castSeconds(state.rpg(),d.actionId,ItemEffects.baseCast(d.actionId,d.hitTime));
    return new CombatResolver.Definition(d.actionId,d.kind,d.state,d.effectType,d.requiresLearned,ItemEffects.mpCost(state.rpg(),d.actionId,base),d.cooldown,d.range,time,d.damage);
  }
  public void prepareAction(String actor,String id){abilities.prepare(actor,id);}
  public void finishAction(String actor,String id){abilities.finish(actor,id);directions.remove(actor+":"+id);}
  private final LineOfSightPort lineOfSight;
  private final LearnedActionPort learnedActions;
  private final ControlPort control;
  private final Map<String,Float> cooldowns=new HashMap<>();
  private final Map<String,float[]> directions=new HashMap<>();
  public void lockDirection(String actor,String target,String id){Position a=position(actor),t=position(target);if(a!=null&&t!=null)directions.put(actor+":"+id,new float[]{t.x-a.x,t.y-a.y});}
  private java.util.function.Predicate<String> visible=id->true;
  private java.util.function.IntSupplier basicHits=()->1;
  public void setBasicHits(java.util.function.IntSupplier hits){basicHits=hits;}
  public void setVisible(java.util.function.Predicate<String> visible){this.visible=visible;}
  public boolean visible(String id){return visible.test(id);}
  public java.util.List<String> recipients(String actor,String target,String action){
    SkillActionContract.Rule r=SkillActionContract.get(action);
    if(r==null||!"player".equals(actor))return java.util.Collections.singletonList(target);
    Position a=position(actor),t=position(target);java.util.List<String> out=new java.util.ArrayList<>();
    if(a==null||t==null)return out;
    if(r.pattern==SkillActionContract.Pattern.SINGLE)return visible(target)&&SkillActionContract.canStart(r,a.x,a.y,t.x,t.y,true)?java.util.Collections.singletonList(target):out;
    if(r.pattern==SkillActionContract.Pattern.SELF||r.pattern==SkillActionContract.Pattern.ALLY||r.pattern==SkillActionContract.Pattern.GROUP){out.add(actor);return out;}
    float tx=t.x,ty=t.y;float[] intent=directions.get(actor+":"+action);
    if(intent!=null&&(r.pattern==SkillActionContract.Pattern.FRONT||r.pattern==SkillActionContract.Pattern.FRONT_BACK)){tx=a.x+intent[0];ty=a.y+intent[1];}
    for(RuntimeState.Monster m:state.monsters())if(m.alive&&visible(m.id)&&SkillActionContract.includes(r,a.x,a.y,tx,ty,m.x,m.y,true)&&hasLineOfSight(actor,m.id))out.add(m.id);
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
  public boolean targetAlive(String targetId){return entityAlive(targetId);}
  public boolean canAct(String actorId){return !state.skillEffects().disabled(actorId)&&control.canAct(actorId);}
  public boolean learned(String actorId,String actionId){return learnedActions.learned(actorId,actionId);}
  public boolean cooldownReady(String actorId,String actionId){return cooldownRemaining(actorId,actionId)<=0f;}

  public boolean hasResource(String actorId,int amount){
    if(abilities.testMode&&"player".equals(actorId)||amount<=0)return true;
    if("player".equals(actorId))return state.player().alive&&state.player().mp>=amount;
    // Current monster runtime has no canonical MP/resource pool. Resource-costing monster actions fail closed.
    return false;
  }

  public float distance(String actorId,String targetId){
    Position a=position(actorId),b=position(targetId);
    if(a==null||b==null)return Float.POSITIVE_INFINITY;
    float dx=a.x-b.x,dy=a.y-b.y;
    return (float)Math.sqrt(dx*dx+dy*dy);
  }

  public boolean hasLineOfSight(String actorId,String targetId){return actorId.equals(targetId)||lineOfSight.hasLineOfSight(actorId,targetId);}

  public void consumeResource(String actorId,int amount){
    if(abilities.testMode&&"player".equals(actorId)||amount<=0)return;
    if(!"player".equals(actorId))throw new IllegalStateException("monster resource pool unresolved");
    RuntimeState.Player player=state.player();
    if(!player.alive||player.mp<amount)throw new IllegalStateException("resource commit without validation");
    player.mp-=amount;
  }

  public void commitCooldown(String actorId,String actionId,float cooldown){
    if(actorId==null||actionId==null)throw new IllegalArgumentException("cooldown identity");
    if(cooldown<=0f){cooldowns.remove(key(actorId,actionId));return;}
    cooldowns.put(key(actorId,actionId),cooldown);
  }

  public float cooldownRemaining(String actorId,String actionId){
    Float value=cooldowns.get(key(actorId,actionId));
    return value==null?0f:Math.max(0f,value);
  }

  public CombatResolver.EffectResult applyDamage(String actorId,String targetId,String actionId,int amount){
    SkillAbilityCatalog.Ability ability=SkillAbilityCatalog.get(actionId);
    boolean playerActor="player".equals(actorId);
    if(playerActor&&ability!=null){
      CombatResolver.EffectResult effect=abilities.apply(targetId,actionId);if(effect!=null)return effect;
      amount=abilities.amount(actionId,amount);abilities.chargeRelease(ability);
    }
    if(SkillRuntimeCatalog.healing(actionId)){
      if(!playerActor||!actorId.equals(targetId)||!state.player().alive)throw new IllegalArgumentException("self heal target");
      int before=state.player().hp;state.player().hp=(int)Math.min(state.player().maxHp,(long)before+amount);
      return new CombatResolver.EffectResult(state.player().hp-before,false,CombatResolver.DefeatPublication.PORT_ALREADY_PUBLISHED,CombatResolver.DefeatedTargetKind.PLAYER,CombatResolver.HitSemantic.HEAL);
    }
    CombatStatPipeline.Channel channel=SkillRuntimeCatalog.magic(actionId)?CombatStatPipeline.Channel.MAGIC:CombatStatPipeline.Channel.PHYSICAL;
    FinalStats attacker=(playerActor?state.rpg().finalStats():monsterStats()).withEffects(state.skillEffects(),actorId);
    FinalStats defender=("player".equals(targetId)?state.rpg().finalStats():monsterStats()).withEffects(state.skillEffects(),targetId);
    if(ability!=null){String element=skillElement(ability);if(!element.isEmpty())attacker=attacker.withAttackElement(element);}
    boolean basic=actionId.startsWith("attack_proto_");
    if(playerActor&&ability!=null){
      if(state.skillEffects().has(actorId,"FOCUS")&&(ability.formula.equals("CRASH")||ability.formula.equals("SOUL")))amount=safeScale(amount,2);
      if(!ability.fixed()&&state.skillEffects().has(actorId,"ELEMENT_BOOST"))amount=safeScale(amount,1.2);
      if(!ability.fixed())amount=CombatStatPipeline.resolveSkill(amount,channel,attacker,defender).applied;
    }else amount=CombatStatPipeline.resolve(amount,channel,attacker,defender).applied;
    if(playerActor&&basic){amount=safeScale(amount,Math.max(1,Math.min(5,basicHits.getAsInt())));if(state.skillEffects().has(actorId,"BASIC_POWER")&&abilities.random.getAsDouble()<.3)amount=safeScale(amount,2);}
    SkillEffectState fx=state.skillEffects();
    if(ability==null||!ability.fixed()){
      if(fx.has(targetId,"INVINCIBLE")||channel==CombatStatPipeline.Channel.PHYSICAL&&fx.has(targetId,"PHYSICAL_GUARD")||!basic&&fx.has(targetId,"TECH_MAGIC_GUARD"))amount=0;
      else if(channel==CombatStatPipeline.Channel.MAGIC&&fx.has(targetId,"MAGIC_GUARD"))amount=safeScale(amount,.5);
      if(fx.has(targetId,"PROTECT"))amount=safeScale(amount,.7);
      if(fx.has(targetId,"SHIELD"))amount=Math.max(0,amount-state.rpg().finalStats().wis);
      double missChance=Math.max(0,(fx.power(targetId,"EVASION")+fx.power(actorId,"BLIND"))/100d-attacker.hit*.01);
      if(channel==CombatStatPipeline.Channel.PHYSICAL&&missChance>0&&abilities.random.getAsDouble()<Math.min(.9,missChance))amount=0;
      if(fx.has(actorId,"DISARM"))amount=safeScale(amount,.65);
      if(channel==CombatStatPipeline.Channel.MAGIC&&fx.has(targetId,"REFLECT_MAGIC")||!basic&&channel==CombatStatPipeline.Channel.PHYSICAL&&fx.has(targetId,"REFLECT_TECH")&&abilities.random.getAsDouble()<.7){if(playerActor)state.damagePlayer(amount);else{RuntimeState.Monster reflected=findMonster(actorId);state.damage(reflected,amount);}amount=0;}
    }
    if(playerActor&&amount>0){fx.remove(actorId,"STEALTH");if(!basic&&ability!=null)abilities.damageStatus(targetId,ability,amount);fx.remove(targetId,"SLEEP");}
    if(amount<=0||!entityAlive(actorId)||!entityAlive(targetId))
      return new CombatResolver.EffectResult(0,false,CombatResolver.DefeatPublication.PORT_ALREADY_PUBLISHED,
          targetKind(targetId),CombatResolver.HitSemantic.MISS);

    if("player".equals(targetId)){
      int before=state.player().hp;
      state.damagePlayer(amount);
      int applied=Math.max(0,before-state.player().hp);
      return new CombatResolver.EffectResult(applied,before>0&&!state.player().alive,
          CombatResolver.DefeatPublication.PORT_ALREADY_PUBLISHED,CombatResolver.DefeatedTargetKind.PLAYER,
          CombatResolver.HitSemantic.DAMAGE);
    }

    RuntimeState.Monster monster=findMonster(targetId);
    if(monster!=null){
      int before=monster.hp;
      state.damage(monster,amount);
      int applied=Math.max(0,before-monster.hp);
      return new CombatResolver.EffectResult(applied,before>0&&!monster.alive,
          CombatResolver.DefeatPublication.PORT_ALREADY_PUBLISHED,CombatResolver.DefeatedTargetKind.MONSTER,
          ability!=null&&(ability.formula.equals("ASSASSIN")||ability.formula.equals("ASSASSIN_PLUS"))&&abilities.snapCritical(actionId)?CombatResolver.HitSemantic.CRIT:CombatResolver.HitSemantic.DAMAGE);
    }

    return new CombatResolver.EffectResult(0,false,CombatResolver.DefeatPublication.PORT_ALREADY_PUBLISHED,
        CombatResolver.DefeatedTargetKind.OTHER,CombatResolver.HitSemantic.DAMAGE);
  }

  private FinalStats monsterStats(){return FinalStats.neutral(100,0);}
  private boolean entityAlive(String id){
    if("player".equals(id))return state.player().alive;
    RuntimeState.Monster monster=findMonster(id);
    return monster!=null&&monster.alive;
  }

  private Position position(String id){
    if("player".equals(id))return new Position(state.player().x,state.player().y);
    RuntimeState.Monster monster=findMonster(id);
    return monster==null?null:new Position(monster.x,monster.y);
  }

  private RuntimeState.Monster findMonster(String id){
    if(id==null)return null;
    for(RuntimeState.Monster monster:state.monsters())if(id.equals(monster.id))return monster;
    return null;
  }

  private CombatResolver.DefeatedTargetKind targetKind(String targetId){
    if("player".equals(targetId))return CombatResolver.DefeatedTargetKind.PLAYER;
    return findMonster(targetId)==null?CombatResolver.DefeatedTargetKind.OTHER:CombatResolver.DefeatedTargetKind.MONSTER;
  }

  private static String skillElement(SkillAbilityCatalog.Ability a){String n=a.name;if(n.contains("마레")||n.equals("마네나로")||n.equals("아이스블러스트"))return "WATER";if(n.contains("테라")||n.equals("퀘이크"))return "EARTH";if(n.contains("아듀")||n.equals("플레쉬스톰")||n.equals("무영신공"))return "WIND";if(n.contains("플라")&&!n.startsWith("프라")||n.equals("플레어"))return "FIRE";return "";}
  private static int safeScale(int n,double scale){return (int)Math.min(Integer.MAX_VALUE,Math.max(0,Math.round(n*scale)));}
  private static String key(String actorId,String actionId){return String.valueOf(actorId)+'\u0000'+String.valueOf(actionId);}
  private static final class Position { final float x,y; Position(float x,float y){this.x=x;this.y=y;} }
}
