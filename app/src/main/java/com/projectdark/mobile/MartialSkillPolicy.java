package com.projectdark.mobile;

import java.util.*;
import com.projectdark.mobile.world.WorldMoveTargetController.Direction;

/** User-confirmed V173 facing/landing rules; no target search, approach or terrain bypass. */
final class MartialSkillPolicy {
 static final String LEAP="SK_무도가_003",LINE="SK_무도가_009";
 static boolean facingOnly(String id){return LEAP.equals(id)||LINE.equals(id)||MartialSourceV228.MUYEONG.equals(id);}
 static float[] step(CharacterRenderer.Direction d){Direction a=Direction.valueOf(d.name());return new float[]{a.dx,a.dy};}
 static float[] position(RuntimeState s,String id){if("player".equals(id))return new float[]{s.player().x,s.player().y};for(RuntimeState.Monster m:s.monsters())if(m.alive&&m.id.equals(id))return new float[]{m.x,m.y};for(RuntimeState.Npc n:s.npcs())if(n.id.equals(id))return new float[]{n.x,n.y};return null;}
 static String frontTarget(RuntimeState s,String id,CharacterRenderer.Direction facing,java.util.function.Predicate<String> visible){
  float[] d=step(facing);int reach=LEAP.equals(id)?1:3;
  for(int n=1;n<=reach;n++){float x=s.player().x+d[0]*n,y=s.player().y+d[1]*n;
   for(RuntimeState.Monster m:s.monsters())if(m.alive&&visible.test(m.id)&&same(x,y,m.x,m.y))return m.id;
   if(LEAP.equals(id))for(RuntimeState.Npc a:s.npcs())if(same(x,y,a.x,a.y))return a.id;
  }return null;
 }
 static boolean same(float x,float y,float a,float b){return Math.abs(x-a)<.1f&&Math.abs(y-b)<.1f;}
 static List<float[]> landings(RuntimeState s,String target,SkillAbilityExecutor.Movement movement){
  List<float[]> out=new ArrayList<>();float[] p=position(s,target);if(p==null||movement==null||s.skillEffects().rooted("player"))return out;
  Direction d=Direction.between(s.player().x,s.player().y,p[0],p[1]);if(d==null)return out;
  float x=p[0]+d.dx,y=p[1]+d.dy;
  if(movement.canLeapPlayer(x,y)){out.add(new float[]{x,y});return out;}
  float lx=d.dy*2,ly=-d.dx/2;
  for(int sign:new int[]{-1,1})if(movement.canLeapPlayer(p[0]+sign*lx,p[1]+sign*ly))out.add(new float[]{p[0]+sign*lx,p[1]+sign*ly});
  return out;
 }
 private MartialSkillPolicy(){}
}
