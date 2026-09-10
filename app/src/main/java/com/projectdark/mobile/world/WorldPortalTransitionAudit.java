package com.projectdark.mobile.world;

/** Android-free deterministic audit for portal safety and exactly-once entry semantics. */
public final class WorldPortalTransitionAudit {
  private WorldPortalTransitionAudit(){}

  private static final class Fixture implements WorldPortalTransitionController.TransitionSink,
      WorldPortalTransitionController.MoveTargetCanceller {
    int requests,cancels;
    boolean accept=true;
    public boolean requestTransition(WorldPortalTransitionController.Request request){requests++;return accept;}
    public void cancelMoveTargetForPortal(){cancels++;}
  }

  public static boolean verify(){
    Fixture fixture=new Fixture();
    WorldPortalTransitionController controller=new WorldPortalTransitionController(fixture,fixture);
    WorldPortalTransitionController.Portal pending=new WorldPortalTransitionController.Portal(
        "milles_south_exit_proto","milles_runtime_proto",null,null,590f,842f,22f,
        WorldPortalTransitionController.PortalReadiness.TARGET_PENDING);
    if(controller.observe(pending,590f,842f).status!=WorldPortalTransitionController.Status.BLOCKED_TARGET_PENDING)return false;
    if(fixture.requests!=0||fixture.cancels!=0)return false;
    if(controller.observe(pending,590f,842f).status!=WorldPortalTransitionController.Status.LATCHED)return false;

    controller.observeOutside();
    WorldPortalTransitionController.Portal ready=new WorldPortalTransitionController.Portal(
        "fixture_exit","fixture_a","fixture_b","entry_north",10f,10f,4f,
        WorldPortalTransitionController.PortalReadiness.READY);
    WorldPortalTransitionController.Snapshot requested=controller.observe(ready,11f,10f);
    if(requested.status!=WorldPortalTransitionController.Status.REQUESTED)return false;
    if(fixture.requests!=1||fixture.cancels!=1||!controller.hasPendingRequest())return false;
    if(controller.observe(ready,10f,10f).status!=WorldPortalTransitionController.Status.PENDING)return false;
    if(fixture.requests!=1||fixture.cancels!=1)return false;
    if(controller.complete(requested.requestId+1,true,null).status!=WorldPortalTransitionController.Status.STALE_COMPLETION)return false;
    if(controller.complete(requested.requestId,true,"OK").status!=WorldPortalTransitionController.Status.COMPLETED)return false;
    if(controller.observe(ready,10f,10f).status!=WorldPortalTransitionController.Status.LATCHED)return false;

    controller.observeOutside();
    fixture.accept=false;
    if(controller.observe(ready,10f,10f).status!=WorldPortalTransitionController.Status.REJECTED)return false;
    return fixture.requests==2&&fixture.cancels==1&&!controller.hasPendingRequest();
  }

  public static void main(String[] args){
    if(!verify())throw new AssertionError("WorldPortalTransitionAudit failed");
    System.out.println("WorldPortalTransitionAudit PASS");
  }
}
