package com.projectdark.mobile;
import java.util.*;
/** One effect/resource implementation shared by manual, quickslot and AUTO Resolver routes. */
final class SkillAbilityExecutor {
 interface Movement {boolean movePlayer(float x,float y);boolean moveMonster(RuntimeState.Monster m,float x,float y);}
 final RuntimeState state;final SkillEffectState fx;
 private final Map<String,SkillAbilityCatalog.Snapshot> snapshots=new HashMap<>();private final Set<String> charged=new HashSet<>();
 java.util.function.ToIntFunction<String> proficiency=id->100;java.util.function.DoubleSupplier random=Math::random;boolean testMode;
 Movement movement;String notice="";
 private static final class Trap {float x,y,life=30;String id;int damage;Trap(float x,float y,String id,int d){this.x=x;this.y=y;this.id=id;damage=d;}}
 private final List<Trap> traps=new ArrayList<>();
 SkillAbilityExecutor(RuntimeState s){state=s;fx=s.skillEffects();}
 CombatResolver.RejectReason ready(String actor,String target,String id,boolean begin){
  if(fx.disabled(actor))return CombatResolver.RejectReason.CONTROL;
  SkillAbilityCatalog.Ability a=SkillAbilityCatalog.get(id);if(a==null||!actor.equals("player"))return null;
  if(!a.supported())return CombatResolver.RejectReason.CONTROL;
  RuntimeState.Player p=state.player();
  if(a.status.equals("TRANSFER"))return CombatResolver.RejectReason.TARGET_DEAD;
  if(testMode)return null;
  if(!begin&&!a.resourceBurst())return null;
  if(a.formula.equals("CRASH")&&p.hp>Math.max(1,state.rpg().finalStats().maxHp*.02))return CombatResolver.RejectReason.RESOURCE;
  if(a.formula.equals("DARA")&&(p.mp<1440||p.hp<=1))return CombatResolver.RejectReason.RESOURCE;
  if(a.formula.equals("SEMELIA")&&p.mp<=3240||a.formula.equals("METEOR")&&p.mp<=12960)return CombatResolver.RejectReason.RESOURCE;
  if((a.allMp&&p.mp<=0)||begin&&p.mp<a.mpCost)return CombatResolver.RejectReason.RESOURCE;
  if(Arrays.asList("SOUL","ASSASSIN","ASSASSIN_PLUS","HP_BURST","DALMA").contains(a.formula)&&p.hp<=1)return CombatResolver.RejectReason.RESOURCE;
  RuntimeState.Monster m=SkillEffectState.monster(state,target);
  if(a.status.equals("DRAIN")&&(m==null||m.hp>=m.maxHp*.05))return CombatResolver.RejectReason.CONTROL;
  if(Arrays.asList("습격","기습","습격진","백스탭").contains(a.name)&&m!=null&&!fx.hidden()&&!fx.has(m.id,"AGGRO_RESET"))return CombatResolver.RejectReason.CONTROL;
  return null;
 }
 void prepare(String actor,String id){if(!actor.equals("player"))return;SkillAbilityCatalog.Ability a=SkillAbilityCatalog.get(id);if(a==null)return;FinalStats stats=state.rpg().finalStats().withEffects(fx,"player");snapshots.put(id,new SkillAbilityCatalog.Snapshot(stats,state.player().hp,state.player().mp,state.rpg().normalLevel()==null?1:state.rpg().normalLevel(),proficiency.applyAsInt(id),random.getAsDouble()<.25));charged.remove(id);}
 void finish(String actor,String id){if(actor.equals("player")){snapshots.remove(id);charged.remove(id);}}
 boolean snapCritical(String id){SkillAbilityCatalog.Snapshot x=snapshots.get(id);return x!=null&&x.critical;}
 int amount(String id,int fallback){refreshBurst(id);SkillAbilityCatalog.Ability a=SkillAbilityCatalog.get(id);SkillAbilityCatalog.Snapshot x=snapshots.get(id);return a==null||x==null?fallback:SkillAbilityCatalog.amount(a,x);}
 void refreshBurst(String id){SkillAbilityCatalog.Ability a=SkillAbilityCatalog.get(id);SkillAbilityCatalog.Snapshot x=snapshots.get(id);if(a!=null&&a.resourceBurst()&&x!=null&&!charged.contains(id))snapshots.put(id,new SkillAbilityCatalog.Snapshot(x.stats,state.player().hp,state.player().mp,x.level,x.proficiency,x.critical));}
 void chargeRelease(SkillAbilityCatalog.Ability a){
  if(!charged.add(a.id)||testMode)return;RuntimeState.Player p=state.player();
  if(a.allMp)p.mp=0;
  switch(a.formula){case "DARA":p.hp=1;p.mp=1;break;case "SOUL":case "ASSASSIN":case "ASSASSIN_PLUS":case "HP_BURST":p.hp=Math.max(1,p.hp-(int)Math.floor(p.hp*.9));if(a.name.equals("매드소울진"))p.hp=1;break;case "DALMA":p.hp=Math.max(1,p.hp-(int)Math.floor(p.hp*.6));break;default:break;}
 }
 /** Status effects return STATUS feedback, preventing invented damage popups. */
 CombatResolver.EffectResult apply(String target,String id){
  SkillAbilityCatalog.Ability a=SkillAbilityCatalog.get(id);if(a==null)return null;
  RuntimeState.Monster m=SkillEffectState.monster(state,target);boolean self=target.equals("player");
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
   case "DRAGON":case "PHOENIX":
    boolean active=fx.has(target,kind);fx.remove(target,"DRAGON");fx.remove(target,"PHOENIX");if(!active)fx.put(target,id,kind,kind.equals("DRAGON")?4:8,duration);break;
   case "POISON":fx.put(target,id,kind,Math.max(1,(state.rpg().finalStats().intel+a.circle*2)/3),duration);break;
   case "DEATH_POISON":fx.put(target,id,kind,5,10);break;
   case "REGEN":fx.put(target,id,kind,Math.max(1,state.rpg().finalStats().wis/2+a.circle*2),duration);break;
   case "REST":fx.put(target,id,"REGEN",200,60);notice="휴식 · 이동하거나 피해를 받으면 중단";break;
   case "TRANSFER":
    // No group member is present in the single-player runtime. Never heal self using donated self HP.
    notice="수혈할 그룹원이 없습니다";return status(0);
   case "BLESS_ALL":putStrong(target,id,"ARMOR",10,duration);putStrong(target,id,"HIT",30,duration);putStrong(target,id,"DAM",8,duration);fx.put(target,id,"PROTECT",30,duration);break;
   case "RESCUE":if(self){int before=state.player().hp;state.player().hp=Math.min(state.player().maxHp,Math.max(before,state.player().maxHp/10));power=state.player().hp-before;}break;
   case "RESET_AGGRO":fx.put(target,id,"AGGRO_RESET",1,8);if(m!=null){state.cancelMonsterAttack(m);m.state=RuntimeState.Monster.State.IDLE;}break;
   case "PUSH":case "LEAP":
    if(m!=null&&movement!=null){float dx=m.x-state.player().x,dy=m.y-state.player().y;
     boolean ok=kind.equals("PUSH")?movement.moveMonster(m,m.x+dx,m.y+dy):movement.movePlayer(m.x+dx,m.y+dy);
     notice=ok?a.name+" 성공":"이동할 타일이 막혀 있습니다";}break;
   case "DOOR":notice="DOOR";break;
   case "BASIC":notice="기본공격";break;
   case "INSPECT":notice=m==null?"HP "+state.player().hp+" / MP "+state.player().mp:m.name+" · HP "+m.hp+" / "+m.maxHp;break;
   case "TRAP":traps.removeIf(t->t.x==state.player().x&&t.y==state.player().y);traps.add(new Trap(state.player().x,state.player().y,id,Math.max(1,state.rpg().finalStats().prototypePhysicalAttack()*2)));notice="함정 설치 · 30초 유지";break;
   case "FIND_TRAP":notice="주변 함정 "+traps.size()+"개";break;
   case "CLEAR_TRAP":traps.removeIf(t->SkillActionContract.distance(state.player().x,state.player().y,t.x,t.y)<=2);notice="주변 함정을 해체했습니다";break;
   case "CHANGE_ELEMENT":fx.put(target,id,"CHANGE_ELEMENT",1,duration);notice="대상 방어 속성: 화";break;
   case "TRAVEL":notice="TRAVEL";break;
   case "STAFF":notice="스태프 사용 허용";break;
   case "TECH_MAGIC_GUARD":case "PHYSICAL_GUARD":
    if(fx.has(target,kind))fx.remove(target,kind);else fx.put(target,id,kind,1,duration);break;
   default:if(!kind.isEmpty())fx.put(target,id,kind,kind.equals("PROTECT")?30:kind.equals("EVASION")?25:kind.equals("BLIND")?50:1,duration);
  }
  if(m!=null&&fx.disabled(target)){state.cancelMonsterAttack(m);m.isMoving=false;}
  return status(power);
 }
 void damageStatus(String target,SkillAbilityCatalog.Ability a,int applied){
  if(applied<=0)return;RuntimeState.Monster m=SkillEffectState.monster(state,target);
  if(a.status.equals("POISON_HIT"))fx.put(target,a.id,"POISON",Math.max(1,applied/8),8);
  if(a.status.equals("FREEZE_HIT")){fx.put(target,a.id,"FREEZE",1,10);if(m!=null){state.cancelMonsterAttack(m);m.isMoving=false;}}
  if(a.status.equals("CHARGE")&&m!=null&&movement!=null){float dx=m.x-state.player().x,dy=m.y-state.player().y;int d=SkillActionContract.distance(state.player().x,state.player().y,m.x,m.y);if(d>1&&d<6)movement.movePlayer(m.x-dx/d,m.y-dy/d);}
  if(a.status.equals("DRAIN"))state.player().hp=Math.min(state.player().maxHp,state.player().hp+applied);
  if(a.name.equals("기습")||a.name.equals("습격진"))fx.put(target,a.id,"CURSE",20,10);
 }
 private void putStrong(String target,String id,String kind,int n,float duration){if(n>=fx.power(target,kind))fx.put(target,id,kind,n,duration);}
 void tick(float dt){for(Trap t:new ArrayList<>(traps)){t.life-=dt;for(RuntimeState.Monster m:state.monsters())if(m.alive&&SkillActionContract.distance(t.x,t.y,m.x,m.y)<=1){state.damage(m,t.damage);t.life=0;break;}if(t.life<=0)traps.remove(t);}}
 private CombatResolver.EffectResult status(int n){return new CombatResolver.EffectResult(n,false,CombatResolver.DefeatPublication.PORT_ALREADY_PUBLISHED,CombatResolver.DefeatedTargetKind.OTHER,CombatResolver.HitSemantic.STATUS);}
 String takeNotice(){String n=notice;notice="";return n;}
}
