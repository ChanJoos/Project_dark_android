package com.projectdark.mobile;
/** Track the existing AI target while idle; damage classification never turns a defender. */
final class MonsterCombatFacingV260 {
 static void track(RuntimeState s,RuntimeState.Monster m){
  if(!s.player().alive||!m.alive||m.isMoving||m.visualFacing.attackLocked()||s.skillEffects().disabled(m.id)||s.skillEffects().hidden())return;
  if(!CanonicalMeleeTileContract.reachable(m.x,m.y,s.player().x,s.player().y))return;
  boolean taunt=s.skillEffects().has(m.id,"TAUNT");
  if(s.skillEffects().has(m.id,"AGGRO_RESET")&&!taunt)return;
  float dx=s.player().x-m.x,dy=s.player().y-m.y;
  if(!m.provoked&&!taunt&&dx*dx+dy*dy>=180f*180f)return;
  m.visualFacing.setLocomotion(CanonicalActorFacing.quantize(dx,dy,m.visualFacing.locomotion()));
 }
 private MonsterCombatFacingV260(){}
}
