package com.projectdark.mobile;

import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Combat·Monster-owned runtime orchestration for prototype monster combat.
 * Basic melee legality is shared with player combat through MeleeTileContract.
 */
public final class MonsterAIController {
  private static final float CHASE_RADIUS_B = 180f;
  private static final float CHASE_SPEED_B = 28f;
  static final int ATTACK_DAMAGE_B = 4;
  static final float ATTACK_COOLDOWN_B = 1.2f;

  public enum AttackRoute { LEGACY_RUNTIME, SHARED_RESOLVER }
  public enum SubmissionOutcome { NONE, ACCEPTED, REJECTED, ACTION_UNRESOLVED }

  public static final class AttackSubmissionSnapshot {
    public final long sequence;
    public final String monsterId,targetId,actionId;
    public final AttackRoute route;
    public final SubmissionOutcome outcome;
    public final CombatResolver.RejectReason rejectReason;
    AttackSubmissionSnapshot(long sequence,String monsterId,String targetId,String actionId,AttackRoute route,
        SubmissionOutcome outcome,CombatResolver.RejectReason rejectReason){
      this.sequence=sequence;this.monsterId=monsterId;this.targetId=targetId;this.actionId=actionId;
      this.route=route;this.outcome=outcome;this.rejectReason=rejectReason;
    }
  }

  public interface AttackRouter {
    AttackRoute route();
    AttackSubmissionSnapshot submit(RuntimeState state,RuntimeState.Monster monster,long sequence);
  }

  private static final class LegacyAttackRouter implements AttackRouter {
    public AttackRoute route(){return AttackRoute.LEGACY_RUNTIME;}
    public AttackSubmissionSnapshot submit(RuntimeState state,RuntimeState.Monster monster,long sequence){
      if(state==null||monster==null||!state.monsterAttackReady(monster))
        return new AttackSubmissionSnapshot(sequence,monster==null?null:monster.id,"player",null,route(),SubmissionOutcome.REJECTED,null);
      state.resolveMonsterAttack(monster,ATTACK_DAMAGE_B,ATTACK_COOLDOWN_B);
      return new AttackSubmissionSnapshot(sequence,monster.id,"player",null,route(),SubmissionOutcome.ACCEPTED,null);
    }
  }

  public static final class SharedResolverAttackRouter implements AttackRouter {
    private final MonsterAutoCombatBridge bridge;
    public SharedResolverAttackRouter(MonsterAutoCombatBridge bridge){
      if(bridge==null)throw new IllegalArgumentException("bridge");this.bridge=bridge;
    }
    public AttackRoute route(){return AttackRoute.SHARED_RESOLVER;}
    public AttackSubmissionSnapshot submit(RuntimeState state,RuntimeState.Monster monster,long sequence){
      if(state==null||monster==null||!state.monsterAttackReady(monster))
        return new AttackSubmissionSnapshot(sequence,monster==null?null:monster.id,"player",bridge.actionId(),route(),SubmissionOutcome.REJECTED,null);
      CharacterRenderer.Direction lockedFacing=monster.visualFacing.presentation();
      state.cancelMonsterAttack(monster);
      MonsterAutoCombatBridge.Result result=bridge.submit(monster.id,"player");
      if(monster.alive&&result.outcome==MonsterAutoCombatBridge.Outcome.ACCEPTED){
        monster.visualFacing.setLocomotion(lockedFacing);monster.visualFacing.beginAttack();
        monster.state=RuntimeState.Monster.State.ATTACK;
      }
      SubmissionOutcome outcome;
      switch(result.outcome){
        case ACCEPTED: outcome=SubmissionOutcome.ACCEPTED; break;
        case ACTION_UNRESOLVED: outcome=SubmissionOutcome.ACTION_UNRESOLVED; break;
        default: outcome=SubmissionOutcome.REJECTED; break;
      }
      return new AttackSubmissionSnapshot(sequence,monster.id,"player",result.actionId,route(),outcome,result.rejectReason);
    }
  }

  private final MonsterDefinitionRegistry definitions=new MonsterDefinitionRegistry();
  private final AttackRouter attackRouter;
  private final Map<RuntimeState.Monster,MonsterCanonicalSegmentLock> locomotionLocks=new IdentityHashMap<>();
  private long submissionSequence;
  private AttackSubmissionSnapshot lastSubmission=new AttackSubmissionSnapshot(0,null,null,null,AttackRoute.LEGACY_RUNTIME,SubmissionOutcome.NONE,null);

  public MonsterAIController(){this(new LegacyAttackRouter());}
  public MonsterAIController(AttackRouter attackRouter){
    if(attackRouter==null)throw new IllegalArgumentException("attackRouter");this.attackRouter=attackRouter;
    lastSubmission=new AttackSubmissionSnapshot(0,null,null,null,attackRouter.route(),SubmissionOutcome.NONE,null);
  }

  public AttackRoute attackRoute(){return attackRouter.route();}
  public AttackSubmissionSnapshot lastAttackSubmission(){return lastSubmission;}

  public void tick(RuntimeState state,float dt){
    if(state==null||!state.player().alive)return;
    for(RuntimeState.Monster m:state.monsters()){
      if(!m.alive){locomotionLocks.remove(m);continue;}
      MonsterDefinition def=definitions.resolve(m.id);
      if(def.status!=MonsterDefinition.Status.PROTOTYPE_PENDING)continue;
      tickPrototypeMonster(state,m,dt);
    }
  }

  private void tickPrototypeMonster(RuntimeState state,RuntimeState.Monster m,float dt){
    float dx=state.player().x-m.x;
    float dy=state.player().y-m.y;
    float d=(float)Math.sqrt(dx*dx+dy*dy);
    boolean adjacent=MeleeTileContract.canAttack(m.x,m.y,state.player().x,state.player().y);
    MonsterCanonicalSegmentLock lock=locomotionLocks.computeIfAbsent(m,key->new MonsterCanonicalSegmentLock());

    if(m.attackPrimed){
      // Attack-start facing is a stable snapshot for the full action; target movement never re-aims it.
      lock.reset();
      if(state.monsterAttackReady(m))lastSubmission=attackRouter.submit(state,m,++submissionSequence);
      return;
    }

    if(d<CHASE_RADIUS_B&&!adjacent){
      float frameDistance=CHASE_SPEED_B*dt;
      MonsterDiagonalLocomotion.Step intent=lock.intent(dx,dy,frameDistance,m.visualFacing.locomotion());
      if(intent==null)return;
      float lockedDistance=(float)Math.sqrt(intent.dx*intent.dx+intent.dy*intent.dy);
      boolean moved=state.tryMoveMonster(m,intent.dx,intent.dy,lockedDistance);
      if(moved)lock.onApplied(lockedDistance,m.visualFacing.locomotion());
      else lock.reset();
      return;
    }

    lock.reset();
    if(adjacent&&m.attackCooldown<=0f){
      WorldAttackFacing.startMonsterAttack(state,m);
    }else if(m.state==RuntimeState.Monster.State.CHASE){
      m.state=RuntimeState.Monster.State.IDLE;
    }
  }
}
