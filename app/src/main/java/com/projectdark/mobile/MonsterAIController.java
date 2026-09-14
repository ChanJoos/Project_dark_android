package com.projectdark.mobile;

import com.projectdark.mobile.world.WorldMoveTargetController;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Combat·Monster-owned runtime orchestration for prototype monster combat.
 * World locomotion follows the same authored 64x32 tile-center contract as player navigation.
 */
public final class MonsterAIController {
  private static final float CHASE_RADIUS_B = 180f;
  static final float ATTACK_BEGIN_RANGE_B = 42f;
  private static final float ATTACK_CANCEL_RANGE_B = 48f;
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

  private static final class TilePursuitState {
    float stepClock;
    boolean centered;
    WorldMoveTargetController.Direction lastDirection=WorldMoveTargetController.Direction.SE;
    void resetClock(){stepClock=0f;}
  }

  private final MonsterDefinitionRegistry definitions=new MonsterDefinitionRegistry();
  private final AttackRouter attackRouter;
  private final Map<RuntimeState.Monster,TilePursuitState> tileStates=new IdentityHashMap<>();
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
      if(!m.alive){tileStates.remove(m);continue;}
      MonsterDefinition def=definitions.resolve(m.id);
      if(def.status!=MonsterDefinition.Status.PROTOTYPE_PENDING)continue;
      tickPrototypeMonster(state,m,dt);
    }
  }

  private void tickPrototypeMonster(RuntimeState state,RuntimeState.Monster m,float dt){
    TilePursuitState tile=tileStates.computeIfAbsent(m,key->new TilePursuitState());
    ensureCentered(m,tile);
    float dx=state.player().x-m.x;
    float dy=state.player().y-m.y;
    float d=(float)Math.sqrt(dx*dx+dy*dy);

    if(m.attackPrimed){
      tile.resetClock();
      if(d>ATTACK_CANCEL_RANGE_B){state.cancelMonsterAttack(m);return;}
      if(state.monsterAttackReady(m))lastSubmission=attackRouter.submit(state,m,++submissionSequence);
      return;
    }

    if(d<CHASE_RADIUS_B&&d>ATTACK_BEGIN_RANGE_B){
      tile.stepClock+=Math.max(0f,dt);
      if(tile.stepClock+.00001f<WorldMoveTargetController.TILE_STEP_SECONDS)return;
      tile.stepClock-=WorldMoveTargetController.TILE_STEP_SECONDS;

      WorldMoveTargetController.Direction direction=MonsterTileCenterLocomotion.toward(dx,dy,tile.lastDirection);
      float beforeX=m.x,beforeY=m.y;
      boolean moved=state.tryMoveMonster(m,direction.dx,direction.dy,MonsterTileCenterLocomotion.STEP_DISTANCE);
      if(moved){
        if(!MonsterTileCenterLocomotion.isAdjacentEndpoint(beforeX,beforeY,m.x,m.y)
            ||!MonsterTileCenterLocomotion.isAuthoredCenter(m.x,m.y)){
          m.x=beforeX;m.y=beforeY;tile.resetClock();return;
        }
        tile.lastDirection=directionFromFacing(m.visualFacing.locomotion(),direction);
        if(m.state!=RuntimeState.Monster.State.ATTACK)m.state=RuntimeState.Monster.State.CHASE;
      }else{
        // Atomic collision rejection leaves the monster at the same valid center.
        m.x=beforeX;m.y=beforeY;tile.resetClock();
      }
      return;
    }

    tile.resetClock();
    if(d<=ATTACK_BEGIN_RANGE_B&&m.attackCooldown<=0f){
      // Same centered spatial delta is snapshotted into the canonical attack-facing contract.
      state.beginMonsterAttack(m);
    }else if(m.state==RuntimeState.Monster.State.CHASE){
      m.state=RuntimeState.Monster.State.IDLE;
    }
  }

  private static void ensureCentered(RuntimeState.Monster m,TilePursuitState state){
    if(state.centered&&MonsterTileCenterLocomotion.isAuthoredCenter(m.x,m.y))return;
    WorldMoveTargetController.TileCenter center=MonsterTileCenterLocomotion.nearestAuthoredCenter(m.x,m.y);
    if(center!=null){m.x=center.x;m.y=center.y;}
    state.centered=true;state.resetClock();
  }

  private static WorldMoveTargetController.Direction directionFromFacing(CharacterRenderer.Direction facing,
      WorldMoveTargetController.Direction fallback){
    if(facing==null)return fallback;
    switch(facing){
      case NW:return WorldMoveTargetController.Direction.NW;
      case NE:return WorldMoveTargetController.Direction.NE;
      case SW:return WorldMoveTargetController.Direction.SW;
      default:return WorldMoveTargetController.Direction.SE;
    }
  }
}
