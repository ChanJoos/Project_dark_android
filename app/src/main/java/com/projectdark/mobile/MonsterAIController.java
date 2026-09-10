package com.projectdark.mobile;

/**
 * Combat·Monster-owned runtime orchestration for prototype monster combat.
 *
 * Canon/evidence guardrails:
 * - Existing monster IDs/stats/spawn relationships remain owned by canonical/runtime data.
 * - Thresholds/movement values below preserve the pre-existing [B] prototype behavior.
 * - Shared-resolver routing owns action submission only; World/RuntimeState still owns movement facts.
 * - Default construction preserves the current legacy runtime until Director injects the shared route.
 */
public final class MonsterAIController {
  private static final float CHASE_RADIUS_B = 180f;
  private static final float ATTACK_BEGIN_RANGE_B = 42f;
  private static final float ATTACK_CANCEL_RANGE_B = 48f;
  private static final float CHASE_SPEED_B = 28f;
  private static final int ATTACK_DAMAGE_B = 4;
  private static final float ATTACK_COOLDOWN_B = 1.2f;

  public enum AttackRoute { LEGACY_RUNTIME, SHARED_RESOLVER }
  public enum SubmissionOutcome { NONE, ACCEPTED, REJECTED, ACTION_UNRESOLVED }

  /** Renderer/QA-safe record of the most recent monster attack handoff. */
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

  /** Combat submission boundary. Implementations must not own movement/pathfinding. */
  public interface AttackRouter {
    AttackRoute route();
    AttackSubmissionSnapshot submit(RuntimeState state,RuntimeState.Monster monster,long sequence);
  }

  /** Temporary compatibility route for the current GameView construction path. */
  private static final class LegacyAttackRouter implements AttackRouter {
    public AttackRoute route(){return AttackRoute.LEGACY_RUNTIME;}
    public AttackSubmissionSnapshot submit(RuntimeState state,RuntimeState.Monster monster,long sequence){
      if(state==null||monster==null||!state.monsterAttackReady(monster))
        return new AttackSubmissionSnapshot(sequence,monster==null?null:monster.id,"player",null,route(),SubmissionOutcome.REJECTED,null);
      state.resolveMonsterAttack(monster,ATTACK_DAMAGE_B,ATTACK_COOLDOWN_B);
      return new AttackSubmissionSnapshot(sequence,monster.id,"player",null,route(),SubmissionOutcome.ACCEPTED,null);
    }
  }

  /**
   * Shared resolver route. It consumes only the legacy windup marker and never calls
   * RuntimeState.resolveMonsterAttack()/damagePlayer(). Actual hit timing/effect is Resolver-owned.
   */
  public static final class SharedResolverAttackRouter implements AttackRouter {
    private final MonsterAutoCombatBridge bridge;
    public SharedResolverAttackRouter(MonsterAutoCombatBridge bridge){
      if(bridge==null)throw new IllegalArgumentException("bridge");this.bridge=bridge;
    }
    public AttackRoute route(){return AttackRoute.SHARED_RESOLVER;}
    public AttackSubmissionSnapshot submit(RuntimeState state,RuntimeState.Monster monster,long sequence){
      if(state==null||monster==null||!state.monsterAttackReady(monster))
        return new AttackSubmissionSnapshot(sequence,monster==null?null:monster.id,"player",bridge.actionId(),route(),SubmissionOutcome.REJECTED,null);
      // Clear the old windup authority before submission. This does not apply damage.
      state.cancelMonsterAttack(monster);
      MonsterAutoCombatBridge.Result result=bridge.submit(monster.id,"player");
      if(monster.alive&&result.outcome==MonsterAutoCombatBridge.Outcome.ACCEPTED)monster.state=RuntimeState.Monster.State.ATTACK;
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
  private long submissionSequence;
  private AttackSubmissionSnapshot lastSubmission=new AttackSubmissionSnapshot(0,null,null,null,AttackRoute.LEGACY_RUNTIME,SubmissionOutcome.NONE,null);

  /** Current GameView compatibility. Director should migrate to the injected constructor. */
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
      if(!m.alive)continue;
      MonsterDefinition def=definitions.resolve(m.id);
      if(def.status!=MonsterDefinition.Status.PROTOTYPE_PENDING)continue;
      tickPrototypeMonster(state,m,dt);
    }
  }

  private void tickPrototypeMonster(RuntimeState state,RuntimeState.Monster m,float dt){
    float dx=state.player().x-m.x;
    float dy=state.player().y-m.y;
    float d=(float)Math.sqrt(dx*dx+dy*dy);

    if(m.attackPrimed){
      if(d>ATTACK_CANCEL_RANGE_B){
        state.cancelMonsterAttack(m);
        return;
      }
      if(state.monsterAttackReady(m))lastSubmission=attackRouter.submit(state,m,++submissionSequence);
      return;
    }

    if(d<CHASE_RADIUS_B&&d>ATTACK_BEGIN_RANGE_B){
      float step=CHASE_SPEED_B*dt;
      state.tryMoveMonster(m,Math.signum(dx)*step,Math.signum(dy)*step);
      return;
    }

    if(d<=ATTACK_BEGIN_RANGE_B&&m.attackCooldown<=0f){
      state.beginMonsterAttack(m);
    }else if(m.state==RuntimeState.Monster.State.CHASE){
      m.state=RuntimeState.Monster.State.IDLE;
    }
  }
}
