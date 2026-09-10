package com.projectdark.mobile;

/**
 * Combat·Monster-owned runtime orchestration for prototype monster combat.
 *
 * Canon/evidence guardrails:
 * - Existing monster IDs/stats/spawn relationships remain owned by canonical/runtime data.
 * - The thresholds, movement speed, damage and cooldown below preserve the pre-existing [B]
 *   prototype behavior exactly; they are not promoted to original-game facts.
 * - WANDER and DETECT remain contract states only. No behavior is invented for them here.
 */
public final class MonsterAIController {
  private static final float CHASE_RADIUS_B = 180f;
  private static final float ATTACK_BEGIN_RANGE_B = 42f;
  private static final float ATTACK_CANCEL_RANGE_B = 48f;
  private static final float CHASE_SPEED_B = 28f;
  private static final int ATTACK_DAMAGE_B = 4;
  private static final float ATTACK_COOLDOWN_B = 1.2f;

  public void tick(RuntimeState state,float dt){
    if(state==null||!state.player().alive)return;
    for(RuntimeState.Monster m:state.monsters()){
      if(!m.alive)continue;
      tickMonster(state,m,dt);
    }
  }

  private void tickMonster(RuntimeState state,RuntimeState.Monster m,float dt){
    float dx=state.player().x-m.x;
    float dy=state.player().y-m.y;
    float d=(float)Math.sqrt(dx*dx+dy*dy);

    if(m.attackPrimed){
      if(d>ATTACK_CANCEL_RANGE_B){
        state.cancelMonsterAttack(m);
        return;
      }
      if(state.monsterAttackReady(m)){
        state.resolveMonsterAttack(m,ATTACK_DAMAGE_B,ATTACK_COOLDOWN_B);
      }
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
