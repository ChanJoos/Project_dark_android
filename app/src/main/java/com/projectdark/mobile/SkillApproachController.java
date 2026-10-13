package com.projectdark.mobile;

import com.projectdark.mobile.world.WorldMoveTargetController;
import com.projectdark.mobile.world.WorldRuntimeAdapter;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.function.Predicate;

/** Pending one-shot intent only. World owns the route; Resolver owns charging and the hit. */
final class SkillApproachController {
  String skillId,targetId;float age;float plannedX=Float.NaN,plannedY=Float.NaN;
  void cancel(){skillId=targetId=null;age=0;plannedX=plannedY=Float.NaN;}
  void begin(String skill,String target){cancel();skillId=skill;targetId=target;}
  static boolean legal(SkillActionContract.Rule r,float x,float y,float tx,float ty){
    if(r.selfAnchored())return SkillActionContract.includes(r,x,y,x,y,tx,ty,true);
    return SkillActionContract.canStart(r,x,y,tx,ty,true);
  }
  static List<WorldMoveTargetController.TileCenter> path(WorldRuntimeAdapter world,SkillActionContract.Rule rule,RuntimeState.Monster target,float width,float height){
    float tx=target.isMoving?target.moveTargetX:target.x,ty=target.isMoving?target.moveTargetY:target.y;
    return world.movement().skillApproachPath(tile->Math.abs(tx-tile.x)<=width/2-48&&Math.abs(ty-tile.y)<=height/2-48&&legal(rule,tile.x,tile.y,tx,ty)&&world.hasLineOfSight(tile.x,tile.y,tx,ty));
  }
  /** Ranged hunting must find a legal casting approach even if the target has no melee approach. */
  static RuntimeState.Monster reachableTarget(WorldRuntimeAdapter world,SkillActionContract.Rule rule,Predicate<RuntimeState.Monster> filter,float width,float height){
    List<RuntimeState.Monster> candidates=new ArrayList<>();RuntimeState s=world.runtime();
    for(RuntimeState.Monster m:s.monsters())if(m.alive&&filter.test(m))candidates.add(m);
    candidates.sort(Comparator.comparingDouble(m->Math.hypot(m.x-s.player().x,m.y-s.player().y)));
    for(RuntimeState.Monster m:candidates)if(path(world,rule,m,width,height)!=null)return m;
    return null;
  }

}
