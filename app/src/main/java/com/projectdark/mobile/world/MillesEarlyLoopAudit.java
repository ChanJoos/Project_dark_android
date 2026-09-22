package com.projectdark.mobile.world;

import com.projectdark.mobile.RuntimeState;

/** Executable B0 regression: the current production fixture can support the whole early-town loop. */
public final class MillesEarlyLoopAudit {
  private MillesEarlyLoopAudit(){}

  public static boolean verify(){
    final RuntimeState state=new RuntimeState();
    boolean identities=MillesEarlyLoopContract.verify(new MillesEarlyLoopContract.RuntimeStateView(){
      @Override public boolean hasNpc(String id){for(RuntimeState.Npc n:state.npcs())if(id.equals(n.id))return true;return false;}
      @Override public boolean hasMonster(String id){for(RuntimeState.Monster m:state.monsters())if(id.equals(m.id))return true;return false;}
    });
    if(!identities)return false;
    MillesEarlyLoopContract.Stop spawn=MillesEarlyLoopContract.byRole(MillesEarlyLoopContract.Role.SPAWN);
    MillesEarlyLoopContract.Stop quest=MillesEarlyLoopContract.byRole(MillesEarlyLoopContract.Role.QUEST_NPC);
    MillesEarlyLoopContract.Stop hunt=MillesEarlyLoopContract.byRole(MillesEarlyLoopContract.Role.HUNTING);
    MillesEarlyLoopContract.Stop supply=MillesEarlyLoopContract.byRole(MillesEarlyLoopContract.Role.SUPPLY);
    MillesEarlyLoopContract.Stop back=MillesEarlyLoopContract.byRole(MillesEarlyLoopContract.Role.RETURN_NPC);
    return spawn!=null&&quest!=null&&hunt!=null&&supply!=null&&back!=null
        &&distance(spawn,quest)>0f&&distance(quest,hunt)>0f&&distance(hunt,supply)>0f
        &&distance(supply,back)>0f;
  }

  private static float distance(MillesEarlyLoopContract.Stop a,MillesEarlyLoopContract.Stop b){
    float dx=a.x-b.x,dy=a.y-b.y;return(float)Math.sqrt(dx*dx+dy*dy);
  }

  public static void main(String[] args){if(!verify())throw new IllegalStateException("Milles early-loop audit failed");System.out.println("Milles early-loop audit OK");}
}
