package com.projectdark.mobile.world;

/** Deterministic regression audit for replacement, cancellation, collision detour and NPC separation. */
public final class WorldMoveTargetAudit {
  private WorldMoveTargetAudit(){}

  private static final class Fixture implements WorldMoveTargetController.NavigationWorld,WorldMoveTargetController.Walker {
    float x=8f,y=8f;
    public float minX(){return 0f;} public float maxX(){return 96f;}
    public float minY(){return 0f;} public float maxY(){return 96f;}
    public float worldX(){return x;} public float worldY(){return y;}
    public boolean canPlayerOccupy(float wx,float wy){return !(wx>=32f&&wx<=48f&&wy<64f);}
    public boolean tryWalkStep(float dx,float dy){
      float nx=x+dx,ny=y+dy;
      if(!canPlayerOccupy(nx,ny))return false;
      x=nx;y=ny;return true;
    }
  }

  public static boolean verify(){
    Fixture f=new Fixture();
    WorldMoveTargetController c=new WorldMoveTargetController(f,f,8f,64f);
    WorldMoveTargetController.Snapshot first=c.requestGroundMove(88f,8f);
    if(first.status!=WorldMoveTargetController.Status.MOVING||first.kind!=WorldMoveTargetController.RequestKind.GROUND)return false;
    long firstId=first.requestId;
    WorldMoveTargetController.Snapshot npc=c.requestNpcApproach("milles_guide_proto",80f,80f,16f);
    if(npc.requestId<=firstId||npc.kind!=WorldMoveTargetController.RequestKind.NPC_APPROACH)return false;
    if(!"milles_guide_proto".equals(npc.targetEntityId))return false;
    for(int i=0;i<300&&c.snapshot().status==WorldMoveTargetController.Status.MOVING;i++)c.tick(.05f);
    if(c.snapshot().status!=WorldMoveTargetController.Status.REACHED)return false;
    WorldMoveTargetController.Snapshot moving=c.requestGroundMove(8f,88f);
    if(moving.status!=WorldMoveTargetController.Status.MOVING)return false;
    WorldMoveTargetController.Snapshot cancelled=c.cancelForDirectInput();
    if(cancelled.status!=WorldMoveTargetController.Status.CANCELLED||cancelled.cancelReason!=WorldMoveTargetController.CancelReason.DIRECT_INPUT)return false;
    WorldMoveTargetController.Snapshot blocked=c.requestGroundMove(120f,8f);
    return blocked.status==WorldMoveTargetController.Status.BLOCKED;
  }

  public static void main(String[] args){
    if(!verify())throw new IllegalStateException("World move-target contract audit failed");
  }
}
