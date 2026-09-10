package com.projectdark.mobile.world;

/** Deterministic, Android-free integration audit for movement, camera and portal ordering. */
public final class WorldNavigationSessionAudit {
  private static final class Fixture implements WorldMoveTargetController.NavigationWorld,WorldMoveTargetController.Walker {
    float x=32f,y=32f;
    public float minX(){return 0f;} public float maxX(){return 320f;} public float minY(){return 0f;} public float maxY(){return 240f;}
    public boolean canPlayerOccupy(float x,float y){return x>=0&&x<=320&&y>=0&&y<=240;}
    public float worldX(){return x;} public float worldY(){return y;}
    public boolean tryWalkStep(float dx,float dy){if(!canPlayerOccupy(x+dx,y+dy))return false;x+=dx;y+=dy;return true;}
  }
  private static final class Sink implements WorldPortalTransitionController.TransitionSink {
    int requests; long lastId;
    public boolean requestTransition(WorldPortalTransitionController.Request request){requests++;lastId=request.requestId;return true;}
  }
  public static void main(String[] args){run();System.out.println("WorldNavigationSessionAudit PASS");}
  public static void run(){
    Fixture f=new Fixture();Sink sink=new Sink();
    WorldCameraTransform camera=new WorldCameraTransform(0,320,0,240,120,100);
    WorldNavigationSession session=new WorldNavigationSession(f,f,camera,sink);

    session.requestGroundMove(240,160);
    WorldNavigationSession.Snapshot s=null;
    for(int i=0;i<500;i++){s=session.tick(1f/60f,null);if(s.moveTarget.status!=WorldMoveTargetController.Status.MOVING)break;}
    require(s!=null&&s.moveTarget.status==WorldMoveTargetController.Status.REACHED,"ground target must be reached by WALK ticks");
    require(s.cameraX>0&&s.cameraY>0,"camera must follow the moved player");
    WorldCameraTransform.Point roundTrip=session.screenToWorld(s.playerScreenX,s.playerScreenY);
    require(close(roundTrip.x,s.playerWorldX)&&close(roundTrip.y,s.playerWorldY),"screen/world transform must round-trip after follow");

    WorldMoveTargetController.Snapshot first=session.requestGroundMove(32,32);
    WorldMoveTargetController.Snapshot npc=session.requestNpcApproach("npc_service",80,64,28);
    require(npc.kind==WorldMoveTargetController.RequestKind.NPC_APPROACH&&npc.replacedRequestId==first.requestId,"NPC approach must replace generic ground move explicitly");

    session.requestGroundMove(300,220);
    WorldPortalTransitionController.Portal ready=new WorldPortalTransitionController.Portal(
        "test_exit","map_a","map_b","spawn_b",f.x,f.y,20,WorldPortalTransitionController.PortalReadiness.READY);
    s=session.tick(0,ready);
    require(s.portal.status==WorldPortalTransitionController.Status.REQUESTED&&sink.requests==1,"ready portal must request once");
    require(s.moveTarget.status==WorldMoveTargetController.Status.CANCELLED&&s.moveTarget.cancelReason==WorldMoveTargetController.CancelReason.PORTAL_TRANSITION,"accepted portal must expose portal-specific move cancellation");
    s=session.tick(0,ready);
    require(sink.requests==1&&s.portal.status==WorldPortalTransitionController.Status.PENDING,"remaining inside portal must not duplicate request");
    require(session.completeTransition(sink.lastId,true,"OK").status==WorldPortalTransitionController.Status.COMPLETED,"matching completion must finish transition");

    session.tick(0,null);
    session.requestGroundMove(288,208);
    WorldPortalTransitionController.Portal pending=new WorldPortalTransitionController.Portal(
        "pending_exit","map_a",null,null,f.x,f.y,20,WorldPortalTransitionController.PortalReadiness.TARGET_PENDING);
    s=session.tick(0,pending);
    require(s.portal.status==WorldPortalTransitionController.Status.BLOCKED_TARGET_PENDING,"unverified target must remain blocked");
    require(s.moveTarget.status==WorldMoveTargetController.Status.MOVING,"blocked portal must not cancel movement");
  }
  private static boolean close(float a,float b){return Math.abs(a-b)<.01f;}
  private static void require(boolean condition,String message){if(!condition)throw new IllegalStateException(message);}
}
