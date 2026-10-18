package com.projectdark.mobile;
import com.projectdark.mobile.world.*;
import java.util.*;
/** Horror-only portal latch and collision-checked one-cell rejection. */
public final class HorrorCastleTravel {
 public static final String EXIT_NPC="horror_exit_keeper";
 private String map;private boolean latched;
 public void reset(){map=null;latched=false;}
 public HorrorCastleGeometry.Door at(RuntimeState s){if(!s.currentMapId().equals(map)){map=s.currentMapId();latched=false;}HorrorCastleGeometry.Door hit=null;
  for(HorrorCastleGeometry.Door d:HorrorCastleGeometry.doors(map))if(Math.hypot(s.player().x-d.x,s.player().y-d.y)<20){hit=d;break;}
  if(hit==null){latched=false;return null;}if(latched)return null;latched=true;return hit;
 }
 public boolean reject(WorldRuntimeAdapter w,HorrorCastleGeometry.Door d){WorldMoveTargetController.Direction approach=w.movement().snapshot().lastStepDirection;float px=w.runtime().player().x,py=w.runtime().player().y;
  List<WorldMoveTargetController.Direction> choices=new ArrayList<>(Arrays.asList(WorldMoveTargetController.Direction.values()));
  final float backX=approach==null?px-d.x:-approach.dx,backY=approach==null?py-d.y:-approach.dy;
  choices.sort(Comparator.comparingDouble(a->-(a.dx*backX+a.dy*backY)));
  w.cancelForAction();for(WorldMoveTargetController.Direction step:choices){float x=d.x+step.dx,y=d.y+step.dy;
   if(!w.canPlayerTraverse(px,py,x,y))continue;w.runtime().player().x=x;w.runtime().player().y=y;w.cancelForAction();return true;
  }return false;
 }
}
