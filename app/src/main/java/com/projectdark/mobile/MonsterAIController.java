package com.projectdark.mobile;

import com.projectdark.mobile.world.WorldMoveTargetController;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Combat·Monster-owned runtime orchestration for prototype monster combat.
 * World locomotion and melee legality share the authored 64x32 tile-center contract.
 */
public final class MonsterAIController {
  private static final float CHASE_RADIUS_B = 180f;
  static final int ATTACK_DAMAGE_B = 4;
  static final float ATTACK_COOLDOWN_B = 1.8f;
  static final float MONSTER_STEP_SECONDS_B = WorldMoveTargetController.TILE_STEP_SECONDS;

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
    default AttackSubmissionSnapshot submitAction(RuntimeState state,RuntimeState.Monster m,long sequence,String action){return submit(state,m,sequence);}
  }

  private static final class LegacyAttackRouter implements AttackRouter {
    public AttackRoute route(){return AttackRoute.LEGACY_RUNTIME;}
    public AttackSubmissionSnapshot submit(RuntimeState state,RuntimeState.Monster monster,long sequence){
      if(state==null||monster==null||!state.monsterAttackReady(monster)
          ||!CanonicalMeleeTileContract.reachable(monster.x,monster.y,state.player().x,state.player().y))
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
    public AttackSubmissionSnapshot submitAction(RuntimeState state,RuntimeState.Monster m,long sequence,String action){
      state.cancelMonsterAttack(m);String target=action.equals("MONSTER_RECOVER")?m.id:"player";MonsterAutoCombatBridge.Result result=bridge.submit(m.id,target,action);
      if(result.outcome==MonsterAutoCombatBridge.Outcome.ACCEPTED){m.visualFacing.beginAttack(state.player().x-m.x,state.player().y-m.y);m.attackVisualRemaining=action.equals("MONSTER_RECOVER")?1.46f:AbelCombatProfile.spell(action)?1.01f:.60f;m.state=RuntimeState.Monster.State.ATTACK;}
      return new AttackSubmissionSnapshot(sequence,m.id,target,action,route(),result.outcome==MonsterAutoCombatBridge.Outcome.ACCEPTED?SubmissionOutcome.ACCEPTED:result.outcome==MonsterAutoCombatBridge.Outcome.ACTION_UNRESOLVED?SubmissionOutcome.ACTION_UNRESOLVED:SubmissionOutcome.REJECTED,result.rejectReason);
    }
    public AttackSubmissionSnapshot submit(RuntimeState state,RuntimeState.Monster monster,long sequence){
      if(state==null||monster==null||!state.monsterAttackReady(monster)
          ||!CanonicalMeleeTileContract.reachable(monster.x,monster.y,state.player().x,state.player().y))
        return new AttackSubmissionSnapshot(sequence,monster==null?null:monster.id,"player",bridge.actionId(),route(),SubmissionOutcome.REJECTED,null);
      CharacterRenderer.Direction lockedFacing=CanonicalMeleeTileContract.facing(monster.x,monster.y,state.player().x,state.player().y);
      state.cancelMonsterAttack(monster);
      MonsterAutoCombatBridge.Result result=bridge.submit(monster.id,"player");
      if(monster.alive&&result.outcome==MonsterAutoCombatBridge.Outcome.ACCEPTED){
        monster.visualFacing.setLocomotion(lockedFacing);monster.visualFacing.beginAttack();
        monster.attackVisualRemaining=.36f;
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
    float stepClock,spellClock=7;int retreatSteps;
    boolean centered;
    WorldMoveTargetController.Direction lastDirection=WorldMoveTargetController.Direction.SE;
    void resetClock(){stepClock=0f;}
  }

  private final MonsterDefinitionRegistry definitions=new MonsterDefinitionRegistry();
  private final AttackRouter attackRouter;
  private final Map<RuntimeState.Monster,TilePursuitState> tileStates=new IdentityHashMap<>();
  private long submissionSequence;private String mapId="";private RuntimeState priorState;
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
    if(priorState!=state||!mapId.equals(state.currentMapId())){tileStates.clear();priorState=state;mapId=state.currentMapId();}
    for(RuntimeState.Monster m:state.monsters()){
      if(!m.alive){tileStates.remove(m);continue;}
      MonsterDefinition def=definitions.resolve(m.campaignRewardProfileId==null?m.id:m.campaignRewardProfileId);
      boolean poteShowcase=com.projectdark.mobile.world.CampaignWorld.contains(state.currentMapId())
          &&PoteForestMonsterShowcase.containsMonster(m.id);
      if(def.status!=MonsterDefinition.Status.PROTOTYPE_PENDING&&!poteShowcase)continue;
      AbelCombatProfile profile=AbelCombatProfile.forId(m.id);if(profile!=null)tickCoastMonster(state,m,dt,profile);else tickPrototypeMonster(state,m,dt);
    }
  }


  private void tickCoastMonster(RuntimeState state,RuntimeState.Monster m,float dt,AbelCombatProfile p){
    TilePursuitState tile=tileStates.computeIfAbsent(m,key->new TilePursuitState());tile.spellClock+=Math.max(0,dt);
    if(state.skillEffects().disabled(m.id)||state.skillEffects().hidden()||state.skillEffects().has(m.id,"AGGRO_RESET")&&!state.skillEffects().has(m.id,"TAUNT")){state.cancelMonsterAttack(m);tile.resetClock();return;}
    if(m.isMoving||m.attackVisualRemaining>0)return;if(!state.skillEffects().rooted(m.id))ensureCentered(state,m,tile);
    float distance=(float)Math.hypot(state.player().x-m.x,state.player().y-m.y);boolean near=CanonicalMeleeTileContract.reachable(m.x,m.y,state.player().x,state.player().y);
    if(m.id.startsWith("ABEL_BERSERKER")&&m.hp<m.maxHp/2&&!state.skillEffects().has(m.id,"DAM"))state.skillEffects().put(m.id,"MONSTER_FIRE","DAM",8,120);
    if(p.id.equals("ABEL_KINGARK")&&!m.bossRecovered&&m.hp<m.maxHp*.35f&&m.mp>=16){
      if(tile.retreatSteps<2&&!state.skillEffects().rooted(m.id)){tile.stepClock+=dt;if(tile.stepClock<MONSTER_STEP_SECONDS_B)return;tile.resetClock();WorldMoveTargetController.Direction best=null;double away=distance;for(WorldMoveTargetController.Direction d:WorldMoveTargetController.Direction.values()){double n=Math.hypot(m.x+d.dx-state.player().x,m.y+d.dy-state.player().y);if(n>away&&state.tryMoveMonster(m,d.dx,d.dy,MonsterTileCenterLocomotion.STEP_DISTANCE)){best=d;break;}}tile.retreatSteps++;if(best!=null)return;}
      if(m.attackCooldown<=0)lastSubmission=attackRouter.submitAction(state,m,++submissionSequence,"MONSTER_RECOVER");return;
    }
    if(distance>CHASE_RADIUS_B&&!m.provoked&&!state.skillEffects().has(m.id,"TAUNT")){tile.resetClock();return;}
    if(m.attackCooldown<=0&&tile.spellClock>=7&&!p.spell.isEmpty()&&distance<=p.range&&m.mp>=8){lastSubmission=attackRouter.submitAction(state,m,++submissionSequence,p.spell);if(lastSubmission.outcome==SubmissionOutcome.ACCEPTED){tile.spellClock=0;return;}}
    if(near){tile.resetClock();if(m.attackCooldown<=0)lastSubmission=attackRouter.submitAction(state,m,++submissionSequence,RuntimeCombatSession.MONSTER_BASIC_ACTION_ID);return;}
    if(state.skillEffects().rooted(m.id))return;tile.stepClock+=Math.max(0,dt);if(tile.stepClock<MONSTER_STEP_SECONDS_B)return;tile.stepClock-=MONSTER_STEP_SECONDS_B;WorldMoveTargetController.Direction d=state.nextMonsterChaseStep(m,state.player().x,state.player().y);if(d!=null&&state.tryMoveMonster(m,d.dx,d.dy,MonsterTileCenterLocomotion.STEP_DISTANCE))m.state=RuntimeState.Monster.State.CHASE;
  }

  private void tickPrototypeMonster(RuntimeState state,RuntimeState.Monster m,float dt){
    TilePursuitState tile=tileStates.computeIfAbsent(m,key->new TilePursuitState());
    if(state.skillEffects().disabled(m.id)){state.cancelMonsterAttack(m);tile.resetClock();return;}
    boolean taunted=state.skillEffects().has(m.id,"TAUNT");
    if(state.skillEffects().hidden()||!taunted&&state.skillEffects().has(m.id,"AGGRO_RESET")){state.cancelMonsterAttack(m);tile.resetClock();return;}
    if(m.isMoving){tile.resetClock();return;}
    if(!state.skillEffects().rooted(m.id))ensureCentered(state,m,tile);
    float dx=state.player().x-m.x;
    float dy=state.player().y-m.y;
    float d=(float)Math.sqrt(dx*dx+dy*dy);
    boolean meleeAdjacent=CanonicalMeleeTileContract.reachable(m.x,m.y,state.player().x,state.player().y);

    if(m.attackPrimed){
      tile.resetClock();
      if(!meleeAdjacent){state.cancelMonsterAttack(m);return;}
      if(state.monsterAttackReady(m))lastSubmission=attackRouter.submit(state,m,++submissionSequence);
      return;
    }

    // Let the hit frame submit first; only pause pursuit while the attack recovery is still visible.
    if(m.attackVisualRemaining>0f){tile.resetClock();return;}

    if((taunted||m.provoked||d<CHASE_RADIUS_B)&&!meleeAdjacent&&!state.skillEffects().rooted(m.id)){
      tile.stepClock+=Math.max(0f,dt);
      if(tile.stepClock+.00001f<MONSTER_STEP_SECONDS_B)return;
      tile.stepClock-=MONSTER_STEP_SECONDS_B;

      WorldMoveTargetController.Direction direction=state.nextMonsterChaseStep(m,state.player().x,state.player().y);
      if(direction==null){tile.resetClock();return;}
      float beforeX=m.x,beforeY=m.y;
      boolean moved=state.tryMoveMonster(m,direction.dx,direction.dy,MonsterTileCenterLocomotion.STEP_DISTANCE);
      if(moved){
        if(!MonsterTileCenterLocomotion.isAdjacentEndpoint(beforeX,beforeY,m.moveTargetX,m.moveTargetY)
            ||!state.isMonsterTileCenter(m.moveTargetX,m.moveTargetY)){
          m.x=beforeX;m.y=beforeY;tile.resetClock();return;
        }
        tile.lastDirection=directionFromFacing(m.visualFacing.locomotion(),direction);
        if(m.state!=RuntimeState.Monster.State.ATTACK)m.state=RuntimeState.Monster.State.CHASE;
      }else{
        m.x=beforeX;m.y=beforeY;tile.resetClock();
      }
      return;
    }

    tile.resetClock();
    if(meleeAdjacent&&m.attackCooldown<=0f){
      CharacterRenderer.Direction attackFacing=CanonicalMeleeTileContract.facing(m.x,m.y,state.player().x,state.player().y);
      if(attackFacing!=null)m.visualFacing.setLocomotion(attackFacing);
      state.beginMonsterAttack(m);
    }else if(m.state==RuntimeState.Monster.State.CHASE){
      m.state=RuntimeState.Monster.State.IDLE;
    }
  }

  private static void ensureCentered(RuntimeState runtime,RuntimeState.Monster m,TilePursuitState pursuit){
    if(pursuit.centered&&runtime.isMonsterTileCenter(m.x,m.y))return;
    WorldMoveTargetController.TileCenter center=runtime.nearestMonsterTileCenter(m.x,m.y);
    if(center!=null){m.x=center.x;m.y=center.y;}
    pursuit.centered=true;pursuit.resetClock();
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
