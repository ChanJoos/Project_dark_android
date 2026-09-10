package com.projectdark.mobile.world;

/** Android-free regression gate for transient vs persistent WALK rejection semantics. */
public final class WorldMoveTargetDynamicReplanAudit {
  private WorldMoveTargetDynamicReplanAudit(){}

  private static final class OpenWorld implements WorldMoveTargetController.NavigationWorld {
    public float minX(){return 0f;} public float maxX(){return 640f;}
    public float minY(){return 0f;} public float maxY(){return 320f;}
    public boolean canPlayerOccupy(float x,float y){return x>=0f&&x<=640f&&y>=0f&&y<=320f;}
  }

  private static final class TestWalker implements WorldMoveTargetController.Walker {
    float x=32f,y=160f; int calls=0; final int[] transientRejectCalls; final boolean rejectForever;
    TestWalker(int[] transientRejectCalls,boolean rejectForever){this.transientRejectCalls=transientRejectCalls;this.rejectForever=rejectForever;}
    public float worldX(){return x;} public float worldY(){return y;}
    public boolean tryWalkStep(float dx,float dy){
      calls++;
      if(rejectForever||contains(transientRejectCalls,calls))return false;
      x+=dx;y+=dy;return true;
    }
    private static boolean contains(int[] values,int value){if(values==null)return false;for(int v:values)if(v==value)return true;return false;}
  }

  public static boolean verify(){
    // Three non-consecutive runtime rejections across one long route must not consume a lifetime budget.
    TestWalker transientWalker=new TestWalker(new int[]{3,9,15},false);
    WorldMoveTargetController transientController=new WorldMoveTargetController(new OpenWorld(),transientWalker);
    WorldMoveTargetController.Snapshot s=transientController.requestGroundMove(560f,160f);
    if(s.status!=WorldMoveTargetController.Status.MOVING)return false;
    for(int i=0;i<500&&s.status==WorldMoveTargetController.Status.MOVING;i++)s=transientController.tick(1f/30f);
    if(s.status!=WorldMoveTargetController.Status.REACHED)return false;
    if(Math.abs(transientWalker.x-560f)>WorldMoveTargetController.DEFAULT_GROUND_TOLERANCE)return false;

    // A genuinely persistent runtime rejection must still fail closed instead of looping forever.
    TestWalker blockedWalker=new TestWalker(null,true);
    WorldMoveTargetController blockedController=new WorldMoveTargetController(new OpenWorld(),blockedWalker);
    s=blockedController.requestGroundMove(560f,160f);
    for(int i=0;i<20&&s.status==WorldMoveTargetController.Status.MOVING;i++)s=blockedController.tick(1f/30f);
    if(s.status!=WorldMoveTargetController.Status.BLOCKED)return false;
    if(blockedWalker.calls!=3)return false;

    // Replacement and direct-input cancellation contracts must remain unchanged after replan hardening.
    TestWalker replacementWalker=new TestWalker(null,false);
    WorldMoveTargetController replacementController=new WorldMoveTargetController(new OpenWorld(),replacementWalker);
    WorldMoveTargetController.Snapshot first=replacementController.requestGroundMove(400f,160f);
    WorldMoveTargetController.Snapshot second=replacementController.requestGroundMove(240f,160f);
    if(second.replacedRequestId!=first.requestId)return false;
    if(replacementController.cancelForDirectInput().cancelReason!=WorldMoveTargetController.CancelReason.DIRECT_INPUT)return false;
    return true;
  }

  public static void main(String[] args){
    if(!verify())throw new AssertionError("WorldMoveTargetDynamicReplanAudit failed");
    System.out.println("WorldMoveTargetDynamicReplanAudit PASS");
  }
}
