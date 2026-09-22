package com.projectdark.mobile.world;

/** Executable B0 regression for the authored production early-town loop contract. */
public final class MillesEarlyLoopAudit {
  private MillesEarlyLoopAudit(){}

  public static boolean verify(){
    boolean identities=MillesEarlyLoopContract.verify(new MillesEarlyLoopContract.RuntimeStateView(){
      @Override public boolean hasNpc(String id){return "milles_guide_proto".equals(id)||"milles_market_proto".equals(id);}
      @Override public boolean hasMonster(String id){return "combat_dummy_01".equals(id);}
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
