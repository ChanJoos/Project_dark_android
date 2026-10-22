package com.projectdark.mobile;
/** Per-ID behavior from the seven user-labelled source demonstrations. */
final class MartialSourceV228 {
 static final String ADVANCE="SK_무도가_035",TRIPLE="SK_무도가_027",MUYEONG="SK_무도가_028",PACHEON="SK_무도가_026";
 static boolean line(String id){return MartialSkillPolicy.LINE.equals(id)||MUYEONG.equals(id);}
 static float[] advanceLanding(RuntimeState s,String target,SkillAbilityExecutor.Movement movement){
  float[] t=MartialSkillPolicy.position(s,target);if(t==null||movement==null||s.skillEffects().rooted("player"))return null;
  float dx=t[0]-s.player().x,dy=t[1]-s.player().y;int steps=SkillActionContract.distance(s.player().x,s.player().y,t[0],t[1]);
  if(steps<1||steps>3||!SkillActionContract.integer(SkillActionContract.u(dx,dy))||!SkillActionContract.integer(SkillActionContract.v(dx,dy)))return null;
  float u=SkillActionContract.u(dx,dy),v=SkillActionContract.v(dx,dy);if(Math.abs(u)>.01f&&Math.abs(v)>.01f)return null;
  float x=s.player().x+dx/steps*3,y=s.player().y+dy/steps*3;
  return movement.canLeapPlayer(x,y)?new float[]{x,y}:null;
 }
 private MartialSourceV228(){}
}
