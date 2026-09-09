package com.projectdark.mobile;

/**
 * Runtime combat intent/cooldown controller.
 *
 * Evidence policy:
 * - [B] Current approach ranges, cooldown values and prototype action requirements come from
 *   AttackDef/SkillDef reconstruction fixtures.
 * - [ADAPTED] Auto-approach is a mobile usability behavior, not an original movement claim.
 * - This controller contains no XP, loot or unverified original server rules.
 */
public final class CombatController {
  public enum Intent { NONE, ATTACK, CAST, SKILL, KICK }

  private Intent intent=Intent.NONE;
  private RuntimeState.Monster target;
  private RuntimeState.Monster approachTarget;
  private int attackMode=0;
  private float attackCooldown=0f,castCooldown=0f,skillCooldown=0f,kickCooldown=0f;

  public void tick(float dt){
    attackCooldown=Math.max(0f,attackCooldown-dt);
    castCooldown=Math.max(0f,castCooldown-dt);
    skillCooldown=Math.max(0f,skillCooldown-dt);
    kickCooldown=Math.max(0f,kickCooldown-dt);
    if(target!=null&&!target.alive){target=null;cancelApproach();}
    if(approachTarget!=null&&!approachTarget.alive)cancelApproach();
  }

  public RuntimeState.Monster target(){return target;}
  public void selectTarget(RuntimeState.Monster monster){target=monster;cancelApproach();}
  public void clearTarget(){target=null;cancelApproach();}

  public int attackMode(){return attackMode;}
  public AttackDef attackDef(){return AttackDef.at(attackMode);}
  public void cycleAttackMode(){attackMode=(attackMode+1)%AttackDef.PROTOTYPES.length;}

  public float attackCooldown(){return attackCooldown;}
  public float castCooldown(){return castCooldown;}
  public float skillCooldown(){return skillCooldown;}
  public float kickCooldown(){return kickCooldown;}

  public boolean attackReady(){return attackCooldown<=0f;}
  public boolean castReady(){return castCooldown<=0f;}
  public boolean skillReady(){return skillCooldown<=0f;}
  public boolean kickReady(){return kickCooldown<=0f;}

  public void commitAttack(){attackCooldown=attackDef().cooldown;}
  public void commitCast(){castCooldown=SkillDef.CAST_PROTO.cooldown;}
  public void commitSkill(){skillCooldown=SkillDef.SKILL_PROTO.cooldown;}
  public void commitKick(){kickCooldown=SkillDef.KICK_PROTO.cooldown;}

  public Intent intent(){return intent;}
  public RuntimeState.Monster approachTarget(){return approachTarget;}
  public boolean approaching(){return intent!=Intent.NONE&&approachTarget!=null;}

  /** [ADAPTED] Stores a deferred action until the player reaches the prototype action range. */
  public boolean beginApproach(Intent next){
    if(next==null||next==Intent.NONE||target==null||!target.alive)return false;
    intent=next;approachTarget=target;return true;
  }

  public void cancelApproach(){intent=Intent.NONE;approachTarget=null;}

  public Intent consumeReadyIntent(){
    Intent ready=intent;intent=Intent.NONE;approachTarget=null;return ready;
  }

  public float intentRange(){
    switch(intent){
      case ATTACK:return attackDef().range;
      case CAST:return SkillDef.CAST_PROTO.range;
      case SKILL:return SkillDef.SKILL_PROTO.range;
      case KICK:return SkillDef.KICK_PROTO.range;
      default:return 0f;
    }
  }

  public boolean inRange(RuntimeState state,float range){
    return state!=null&&target!=null&&target.alive&&state.distanceTo(target)<=range;
  }

  public boolean hasUsableTarget(){return target!=null&&target.alive;}
}
