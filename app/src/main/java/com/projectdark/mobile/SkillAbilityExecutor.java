package com.projectdark.mobile;
import java.util.*;
/** One effect/resource implementation shared by manual, quickslot and AUTO Resolver routes. */
final class SkillAbilityExecutor {
 interface Movement {boolean movePlayer(float x,float y);boolean moveMonster(RuntimeState.Monster m,float x,float y);default boolean canLeapPlayer(float x,float y){return false;}default boolean leapPlayer(float x,float y){return false;}}
 final RuntimeState state;final SkillEffectState fx;final String actorId;
 int actorLevel(){CompanionParty.Member m=state.party().find(actorId);return m==null?(state.rpg().normalLevel()==null?1:state.rpg().normalLevel()):m.level;}
 RuntimeState.Player actor(){return state.party().body(state,actorId);}
 FinalStats stats(){return state.party().stats(state,actorId);}
 int mpCost(String id,int base){return "player".equals(actorId)?ItemEffects.mpCost(state.rpg(),id,base):base;}
 RuntimeState.Player friendly(String target){return state.party().body(state,target);}
 private final Map<String,SkillAbilityCatalog.Snapshot> snapshots=new HashMap<>();private final Set<String> charged=new HashSet<>();
 java.util.function.ToIntFunction<String> proficiency=id->100;java.util.function.DoubleSupplier random=Math::random;boolean testMode;
 Movement movement;String notice="";private float[] advanceLanding,advanceTarget;float[] completedAdvanceFacing;
 private static final class Trap {float x,y,life=30;String id;int damage;Trap(float x,float y,String id,int d){this.x=x;this.y=y;this.id=id;damage=d;}}
 private final List<Trap> traps=new ArrayList<>();
 SkillAbilityExecutor(RuntimeState s){this(s,"player");}
 SkillAbilityExecutor(RuntimeState s,String id){state=s;fx=s.skillEffects();actorId=id;}
 CombatResolver.RejectReason ready(String actor,String target,String id,boolean begin){
  if(fx.disabled(actor))return CombatResolver.RejectReason.CONTROL;
  SkillAbilityCatalog.Ability a=SkillAbilityCatalog.get(id);if(a==null||!actor.equals(actorId))return null;
  if(!a.supported())return CombatResolver.RejectReason.CONTROL;
  RuntimeState.Player p=actor();
  // Player-only movement services have no companion navigation adapter; never move the player for them.
  if(!actorId.equals("player")&&(a.status.equals("LEAP")||MartialSourceV228.ADVANCE.equals(id)))return CombatResolver.RejectReason.CONTROL;
  if(a.status.equals("TRANSFER"))return CombatResolver.RejectReason.TARGET_DEAD;
  if(a.status.equals("LEAP")&&MartialSkillPolicy.landings(state,target,movement).isEmpty())return CombatResolver.RejectReason.RANGE;
  if(MartialSourceV228.ADVANCE.equals(id)){
   float[] current=MartialSkillPolicy.position(state,target);
   if(begin||current==null||advanceTarget==null||!MartialSkillPolicy.same(current[0],current[1],advanceTarget[0],advanceTarget[1])||advanceLanding==null||movement==null||!movement.canLeapPlayer(advanceLanding[0],advanceLanding[1])){
    advanceLanding=MartialSourceV228.advanceLanding(state,target,movement,random.getAsDouble());advanceTarget=current;if(begin)completedAdvanceFacing=null;
   }
   if(advanceLanding==null||movement==null||fx.rooted(actorId)||!movement.canLeapPlayer(advanceLanding[0],advanceLanding[1]))return CombatResolver.RejectReason.RANGE;
  }
  if(testMode&&!a.formula.equals("DARA"))return null;
  if(MartialSourceV228.MUYEONG.equals(id)&&p.mp<=0)return CombatResolver.RejectReason.RESOURCE;
  if(!begin&&!a.resourceBurst())return null;
  if(a.formula.equals("CRASH")&&p.hp>Math.max(1,stats().maxHp*.02))return CombatResolver.RejectReason.RESOURCE;
  if(a.formula.equals("DARA")&&(p.mp<1440||p.hp<=1))return CombatResolver.RejectReason.RESOURCE;
  if(a.formula.equals("SEMELIA")&&p.mp<=3240||a.formula.equals("METEOR")&&p.mp<=mpCost(id,a.mpCost))return CombatResolver.RejectReason.RESOURCE;
  if((a.allMp&&p.mp<=0)||begin&&p.mp<mpCost(id,a.mpCost))return CombatResolver.RejectReason.RESOURCE;
  if(Arrays.asList("SOUL","ASSASSIN","ASSASSIN_PLUS","HP_BURST","DALMA").contains(a.formula)&&p.hp<=1)return CombatResolver.RejectReason.RESOURCE;
  RuntimeState.Monster m=SkillEffectState.monster(state,target);
  if(a.status.equals("DRAIN")&&(m==null||m.hp>=m.maxHp*.05))return CombatResolver.RejectReason.CONTROL;
  if(Arrays.asList("습격","기습","습격진","백스탭").contains(a.name)&&m!=null&&!fx.has(actorId,"STEALTH")&&!fx.has(m.id,"AGGRO_RESET"))return CombatResolver.RejectReason.CONTROL;
  return null;
 }
 static int previewMp(SkillAbilityCatalog.Ability a,int current,boolean test){return test&&a!=null&&a.damage()&&!a.formula.equals("DARA")&&(a.resourceBurst()||a.formula.equals("METEOR")||a.formula.equals("HOLY_DRAGON"))?Math.max(50000,current):current;}
 void prepare(String actor,String id){if(!actor.equals(actorId))return;SkillAbilityCatalog.Ability a=SkillAbilityCatalog.get(id);if(a==null)return;FinalStats stats=stats().withEffects(fx,actorId);snapshots.put(id,new SkillAbilityCatalog.Snapshot(stats,actor().hp,previewMp(a,actor().mp,testMode),actorLevel(),proficiency.applyAsInt(id),random.getAsDouble()<.25,mpCost(id,a.mpCost)));charged.remove(id);}
 void finish(String actor,String id){if(actor.equals(actorId)){
  if(MartialSourceV228.ADVANCE.equals(id)){if(charged.contains(id)&&advanceLanding!=null&&movement!=null&&movement.leapPlayer(advanceLanding[0],advanceLanding[1])&&advanceTarget!=null)completedAdvanceFacing=new float[]{advanceTarget[0]-actor().x,advanceTarget[1]-actor().y};advanceLanding=null;advanceTarget=null;}
  snapshots.remove(id);charged.remove(id);}}
 boolean snapCritical(String id){SkillAbilityCatalog.Snapshot x=snapshots.get(id);return x!=null&&x.critical;}
 int amount(String id,int fallback){refreshBurst(id);SkillAbilityCatalog.Ability a=SkillAbilityCatalog.get(id);SkillAbilityCatalog.Snapshot x=snapshots.get(id);return a==null||x==null?fallback:SkillAbilityCatalog.amount(a,x);}
 void refreshBurst(String id){SkillAbilityCatalog.Ability a=SkillAbilityCatalog.get(id);SkillAbilityCatalog.Snapshot x=snapshots.get(id);if(a!=null&&a.resourceBurst()&&x!=null&&!charged.contains(id))snapshots.put(id,new SkillAbilityCatalog.Snapshot(x.stats,actor().hp,previewMp(a,actor().mp,testMode),x.level,x.proficiency,x.critical,x.mpCost));}
 void chargeRelease(SkillAbilityCatalog.Ability a){
  if(!charged.add(a.id)||testMode)return;RuntimeState.Player p=actor();
  if(a.allMp)p.mp=0;
  switch(a.formula){case "DARA":p.hp=1;p.mp=1;break;case "SOUL":case "ASSASSIN":case "ASSASSIN_PLUS":case "HP_BURST":p.hp=Math.max(1,p.hp-(int)Math.floor(p.hp*.9));if(a.name.equals("매드소울진"))p.hp=1;break;case "DALMA":p.hp=Math.max(1,p.hp-(int)Math.floor(p.hp*.6));break;default:break;}
 }
 /** Status effects return STATUS feedback, preventing invented damage popups. */
 CombatResolver.EffectResult apply(String target,String id){
  SkillAbilityCatalog.Ability a=SkillAbilityCatalog.get(id);if(a==null)return null;
  RuntimeState.Monster m=SkillEffectState.monster(state,target);boolean self=state.party().friendly(target);
  if(a.damage()||a.heal())return null;
  chargeRelease(a);int power=0;String kind=a.status;float duration=a.duration;
  if(kind.startsWith("CLEAR_")){
   String clear=kind.substring(6);
   if(clear.equals("ALL"))fx.clear(target);
   else if(clear.startsWith("CURSE")){int n=Integer.parseInt(clear.substring(5));if(fx.power(target,"CURSE")<=n)fx.remove(target,"CURSE");}
   else fx.remove(target,clear);
  }else if(kind.startsWith("CURSE")){power=Integer.parseInt(kind.substring(5));if(power>=fx.power(target,"CURSE"))fx.put(target,id,"CURSE",power,duration);
  }else if(kind.startsWith("ARMOR")&&!kind.equals("ARMOR_BREAK")){power=Integer.parseInt(kind.substring(5));putStrong(target,id,"ARMOR",power,duration);
  }else if(kind.startsWith("HIT")){putStrong(target,id,"HIT",Integer.parseInt(kind.substring(3)),duration);
  }else if(kind.startsWith("DAM")){putStrong(target,id,"DAM",Integer.parseInt(kind.substring(3)),duration);
  }else switch(kind){
   case "STEALTH":fx.put(target,id,kind,1,duration);if(self)for(RuntimeState.Monster monster:state.monsters())if(actorId.equals("player")||actorId.equals(monster.attackTarget)){state.cancelMonsterAttack(monster);if(actorId.equals("player"))monster.provoked=false;else monster.attackTarget="player";}break;
   case "DRAGON":case "PHOENIX":
    boolean active=fx.has(target,kind);fx.remove(target,"DRAGON");fx.remove(target,"PHOENIX");if(!active)fx.put(target,id,kind,kind.equals("DRAGON")?4:8,duration);break;
   case "POISON":fx.put(target,id,kind,Math.max(1,(stats().intel+a.circle*2)/3),duration);break;
   case "DEATH_POISON":fx.put(target,id,kind,5,10);break;
   case "REGEN":fx.put(target,id,kind,Math.max(1,stats().wis/2+a.circle*2),duration);break;
   case "REST":fx.put(target,id,"REGEN",200,60);notice="휴식 · 이동하거나 피해를 받으면 중단";break;
   case "TRANSFER":
    // No group member is present in the single-player runtime. Never heal self using donated self HP.
    notice="수혈할 그룹원이 없습니다";return status(0);
   case "BLESS_ALL":putStrong(target,id,"ARMOR",10,duration);putStrong(target,id,"HIT",30,duration);putStrong(target,id,"DAM",8,duration);fx.put(target,id,"PROTECT",30,duration);break;
   case "RESCUE":if(self){RuntimeState.Player recipient=friendly(target);int before=recipient.hp;recipient.hp=Math.min(recipient.maxHp,Math.max(before,recipient.maxHp/10));power=recipient.hp-before;}break;
   case "RESET_AGGRO":float resetSeconds=duration;fx.put(target,id,"AGGRO_RESET",1,resetSeconds);fx.put(target,id,"AMNESIA",2,resetSeconds);if(m!=null){state.cancelMonsterAttack(m);m.provoked=false;m.state=RuntimeState.Monster.State.IDLE;}break;
   case "TAUNT":
    if(m!=null){state.cancelMonsterAttack(m);m.attackTarget=actorId;fx.remove(target,"AGGRO_RESET");fx.put(target,id,"TAUNT",1,duration);m.state=RuntimeState.Monster.State.CHASE;notice=a.name+" · "+m.name+"이 당신을 추적합니다";}break;
   case "LEAP":
    java.util.List<float[]> landing=MartialSkillPolicy.landings(state,target,movement);
    if(!landing.isEmpty()){float[] at=landing.get(Math.min(landing.size()-1,(int)(random.getAsDouble()*landing.size())));movement.leapPlayer(at[0],at[1]);}break;
   case "REFLECT_ALL":if(fx.has(target,kind))fx.remove(target,kind);else fx.put(target,id,kind,1,duration);break;
   case "PUSH":
    if(m!=null&&movement!=null){float dx=m.x-actor().x,dy=m.y-actor().y;
     boolean ok=kind.equals("PUSH")?movement.moveMonster(m,m.x+dx,m.y+dy):movement.movePlayer(m.x+dx,m.y+dy);
     notice=ok?a.name+" 성공":"이동할 타일이 막혀 있습니다";}break;
   case "DOOR":notice="DOOR";break;
   case "BASIC":notice="기본공격";break;
   case "INSPECT":if(id.equals("SK_도적_002")&&m!=null){notice="SENSE_MONSTER:"+m.id;break;}notice=m==null?"HP "+actor().hp+" / MP "+actor().mp:m.name+" · HP "+m.hp+" / "+m.maxHp;break;
   case "TRAP":traps.removeIf(t->t.x==actor().x&&t.y==actor().y);traps.add(new Trap(actor().x,actor().y,id,Math.max(1,stats().prototypePhysicalAttack()*2)));notice="함정 설치 · 30초 유지";break;
   case "FIND_TRAP":notice="주변 함정 "+traps.size()+"개";break;
   case "CLEAR_TRAP":traps.removeIf(t->SkillActionContract.distance(actor().x,actor().y,t.x,t.y)<=2);notice="주변 함정을 해체했습니다";break;
   case "CHANGE_ELEMENT":fx.put(target,id,"CHANGE_ELEMENT",1,duration);notice="대상 방어 속성: 화";break;
   case "TRAVEL":notice=a.id.equals("SK_무도가_034")?"TRAVEL_MARTIAL":"TRAVEL";break;
   case "STAFF":notice="스태프 사용 허용";break;
   case "TECH_MAGIC_GUARD":case "PHYSICAL_GUARD":
    if(fx.has(target,kind))fx.remove(target,kind);else fx.put(target,id,kind,1,duration);break;
   default:if(!kind.isEmpty())fx.put(target,id,kind,kind.equals("PROTECT")?30:kind.equals("EVASION")?25:kind.equals("BLIND")?50:1,duration);
  }
  if(m!=null&&fx.rooted(target))m.isMoving=false;
  if(m!=null&&fx.disabled(target))state.cancelMonsterAttack(m);
  return status(power);
 }
 void damageStatus(String target,SkillAbilityCatalog.Ability a,int applied){
  if(applied<=0&&!a.status.equals("FREEZE_HIT"))return;RuntimeState.Monster m=SkillEffectState.monster(state,target);
  if(a.status.equals("POISON_HIT"))fx.put(target,a.id,"POISON",Math.max(1,applied/8),8);
  if(a.status.equals("FREEZE_HIT")){fx.put(target,a.id,"FREEZE",1,a.duration);if(m!=null){state.cancelMonsterAttack(m);m.isMoving=false;}}
  if(a.status.equals("CHARGE")&&m!=null&&movement!=null){float dx=m.x-actor().x,dy=m.y-actor().y;int d=SkillActionContract.distance(actor().x,actor().y,m.x,m.y);if(d>1&&d<6)movement.movePlayer(m.x-dx/d,m.y-dy/d);}

  if(a.name.equals("기습")||a.name.equals("습격진"))fx.put(target,a.id,"CURSE",20,10);
 }
 private void putStrong(String target,String id,String kind,int n,float duration){if(n>=fx.power(target,kind))fx.put(target,id,kind,n,duration);}
 void tick(float dt){for(Trap t:new ArrayList<>(traps)){t.life-=dt;for(RuntimeState.Monster m:state.monsters())if(m.alive&&SkillActionContract.distance(t.x,t.y,m.x,m.y)<=1){state.damage(m,t.damage);t.life=0;break;}if(t.life<=0)traps.remove(t);}}
 private CombatResolver.EffectResult status(int n){return new CombatResolver.EffectResult(n,false,CombatResolver.DefeatPublication.PORT_ALREADY_PUBLISHED,CombatResolver.DefeatedTargetKind.OTHER,CombatResolver.HitSemantic.STATUS);}
 String takeNotice(){String n=notice;notice="";return n;}
}
