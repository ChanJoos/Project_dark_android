package com.projectdark.mobile;

import com.projectdark.mobile.world.WorldMoveTargetController;
import java.util.IdentityHashMap;
import java.util.Map;

/** Combat·Monster runtime orchestration using one tile-center spatial contract for movement and melee. */
public final class MonsterAIController {
  private static final float CHASE_RADIUS_B=180f;
  /** Monster pursuit is intentionally slower than player tile locomotion for readable disengage/reposition play. */
  static final float MONSTER_TILE_STEP_SECONDS=.82f;
  /** Derived compatibility range for shared resolver; attack eligibility itself is exact tile adjacency. */
  static final float ATTACK_BEGIN_RANGE_B=CanonicalMeleeTileContract.REACH_DISTANCE;
  static final int ATTACK_DAMAGE_B=4;
  static final float ATTACK_COOLDOWN_B=1.2f;

  public enum AttackRoute { LEGACY_RUNTIME, SHARED_RESOLVER }
  public enum SubmissionOutcome { NONE, ACCEPTED, REJECTED, ACTION_UNRESOLVED }
  public static final class AttackSubmissionSnapshot {
    public final long sequence;public final String monsterId,targetId,actionId;public final AttackRoute route;public final SubmissionOutcome outcome;public final CombatResolver.RejectReason rejectReason;
    AttackSubmissionSnapshot(long s,String m,String t,String a,AttackRoute r,SubmissionOutcome o,CombatResolver.RejectReason rr){sequence=s;monsterId=m;targetId=t;actionId=a;route=r;outcome=o;rejectReason=rr;}
  }
  public interface AttackRouter {AttackRoute route();AttackSubmissionSnapshot submit(RuntimeState state,RuntimeState.Monster monster,long sequence);}
  private static boolean adjacentToPlayer(RuntimeState state,RuntimeState.Monster monster){return state!=null&&monster!=null&&CanonicalMeleeTileContract.reachable(monster.x,monster.y,state.player().x,state.player().y);}
  private static final class LegacyAttackRouter implements AttackRouter {
    public AttackRoute route(){return AttackRoute.LEGACY_RUNTIME;}
    public AttackSubmissionSnapshot submit(RuntimeState state,RuntimeState.Monster monster,long sequence){
      if(state==null||monster==null||!state.monsterAttackReady(monster)||!adjacentToPlayer(state,monster))return new AttackSubmissionSnapshot(sequence,monster==null?null:monster.id,"player",null,route(),SubmissionOutcome.REJECTED,null);
      state.resolveMonsterAttack(monster,ATTACK_DAMAGE_B,ATTACK_COOLDOWN_B);return new AttackSubmissionSnapshot(sequence,monster.id,"player",null,route(),SubmissionOutcome.ACCEPTED,null);
    }
  }
  public static final class SharedResolverAttackRouter implements AttackRouter {
    private final MonsterAutoCombatBridge bridge;public SharedResolverAttackRouter(MonsterAutoCombatBridge b){if(b==null)throw new IllegalArgumentException("bridge");bridge=b;}
    public AttackRoute route(){return AttackRoute.SHARED_RESOLVER;}
    public AttackSubmissionSnapshot submit(RuntimeState state,RuntimeState.Monster monster,long sequence){
      if(state==null||monster==null||!state.monsterAttackReady(monster)||!adjacentToPlayer(state,monster))return new AttackSubmissionSnapshot(sequence,monster==null?null:monster.id,"player",bridge.actionId(),route(),SubmissionOutcome.REJECTED,null);
      CharacterRenderer.Direction locked=monster.visualFacing.presentation();state.cancelMonsterAttack(monster);MonsterAutoCombatBridge.Result result=bridge.submit(monster.id,"player");
      if(monster.alive&&result.outcome==MonsterAutoCombatBridge.Outcome.ACCEPTED){monster.visualFacing.setLocomotion(locked);monster.visualFacing.beginAttack();monster.state=RuntimeState.Monster.State.ATTACK;}
      SubmissionOutcome o=result.outcome==MonsterAutoCombatBridge.Outcome.ACCEPTED?SubmissionOutcome.ACCEPTED:result.outcome==MonsterAutoCombatBridge.Outcome.ACTION_UNRESOLVED?SubmissionOutcome.ACTION_UNRESOLVED:SubmissionOutcome.REJECTED;
      return new AttackSubmissionSnapshot(sequence,monster.id,"player",result.actionId,route(),o,result.rejectReason);
    }
  }
  private static final class TilePursuitState {float stepClock;boolean centered;WorldMoveTargetController.Direction lastDirection=WorldMoveTargetController.Direction.SE;void resetClock(){stepClock=0f;}}
  private final MonsterDefinitionRegistry definitions=new MonsterDefinitionRegistry();private final AttackRouter attackRouter;private final Map<RuntimeState.Monster,TilePursuitState> tileStates=new IdentityHashMap<>();private long submissionSequence;
  private AttackSubmissionSnapshot lastSubmission=new AttackSubmissionSnapshot(0,null,null,null,AttackRoute.LEGACY_RUNTIME,SubmissionOutcome.NONE,null);
  public MonsterAIController(){this(new LegacyAttackRouter());}public MonsterAIController(AttackRouter r){if(r==null)throw new IllegalArgumentException("attackRouter");attackRouter=r;lastSubmission=new AttackSubmissionSnapshot(0,null,null,null,r.route(),SubmissionOutcome.NONE,null);}
  public AttackRoute attackRoute(){return attackRouter.route();}public AttackSubmissionSnapshot lastAttackSubmission(){return lastSubmission;}
  public void tick(RuntimeState state,float dt){if(state==null||!state.player().alive)return;for(RuntimeState.Monster m:state.monsters()){if(!m.alive){tileStates.remove(m);continue;}MonsterDefinition def=definitions.resolve(m.id);if(def.status==MonsterDefinition.Status.PROTOTYPE_PENDING)tickPrototypeMonster(state,m,dt);}}
  private void tickPrototypeMonster(RuntimeState state,RuntimeState.Monster m,float dt){
    TilePursuitState tile=tileStates.computeIfAbsent(m,k->new TilePursuitState());ensureCentered(m,tile);float dx=state.player().x-m.x,dy=state.player().y-m.y,d=(float)Math.sqrt(dx*dx+dy*dy);
    WorldMoveTargetController.Direction melee=CanonicalMeleeTileContract.direction(m.x,m.y,state.player().x,state.player().y);boolean reachable=melee!=null;
    if(m.attackPrimed){tile.resetClock();if(!reachable){state.cancelMonsterAttack(m);return;}if(state.monsterAttackReady(m))lastSubmission=attackRouter.submit(state,m,++submissionSequence);return;}
    if(d<CHASE_RADIUS_B&&!reachable){tile.stepClock+=Math.max(0f,dt);if(tile.stepClock+.00001f<MONSTER_TILE_STEP_SECONDS)return;tile.stepClock-=MONSTER_TILE_STEP_SECONDS;
      WorldMoveTargetController.Direction dir=MonsterTileCenterLocomotion.toward(dx,dy,tile.lastDirection);float bx=m.x,by=m.y;boolean moved=state.tryMoveMonster(m,dir.dx,dir.dy,MonsterTileCenterLocomotion.STEP_DISTANCE);
      if(moved){if(!MonsterTileCenterLocomotion.isAdjacentEndpoint(bx,by,m.x,m.y)||!MonsterTileCenterLocomotion.isAuthoredCenter(m.x,m.y)){m.x=bx;m.y=by;tile.resetClock();return;}tile.lastDirection=directionFromFacing(m.visualFacing.locomotion(),dir);if(m.state!=RuntimeState.Monster.State.ATTACK)m.state=RuntimeState.Monster.State.CHASE;}else{m.x=bx;m.y=by;tile.resetClock();}return;}
    tile.resetClock();if(reachable&&m.attackCooldown<=0f){m.visualFacing.setLocomotion(CanonicalMeleeTileContract.facing(melee));state.beginMonsterAttack(m);}else if(m.state==RuntimeState.Monster.State.CHASE)m.state=RuntimeState.Monster.State.IDLE;
  }
  private static void ensureCentered(RuntimeState.Monster m,TilePursuitState s){if(s.centered&&MonsterTileCenterLocomotion.isAuthoredCenter(m.x,m.y))return;WorldMoveTargetController.TileCenter c=MonsterTileCenterLocomotion.nearestAuthoredCenter(m.x,m.y);if(c!=null){m.x=c.x;m.y=c.y;}s.centered=true;s.resetClock();}
  private static WorldMoveTargetController.Direction directionFromFacing(CharacterRenderer.Direction f,WorldMoveTargetController.Direction fallback){if(f==null)return fallback;switch(f){case NW:return WorldMoveTargetController.Direction.NW;case NE:return WorldMoveTargetController.Direction.NE;case SW:return WorldMoveTargetController.Direction.SW;default:return WorldMoveTargetController.Direction.SE;}}
}
