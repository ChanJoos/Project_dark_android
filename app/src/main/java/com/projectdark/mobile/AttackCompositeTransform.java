package com.projectdark.mobile;

/**
 * Final screen-space normalization for the authored single CONTACT attack pose.
 * The transform is intentionally source-agnostic: callers provide visible alpha geometry from
 * the accepted idle frame and the authored action BODY. BODY, robe and weapon must consume the
 * same returned scale/origin; no layer may independently rescale or re-anchor.
 */
public final class AttackCompositeTransform {
  public static final float MAX_FOOT_ERROR_PX = 1f;
  public static final float MAX_HEIGHT_ERROR_PX = 1f;

  public static final class Result {
    public final float scale;
    public final float bodyLeft;
    public final float bodyTop;
    public final float targetFootY;
    public final float targetCenterX;
    Result(float scale,float bodyLeft,float bodyTop,float targetFootY,float targetCenterX){
      this.scale=scale;this.bodyLeft=bodyLeft;this.bodyTop=bodyTop;
      this.targetFootY=targetFootY;this.targetCenterX=targetCenterX;
    }
    public float screenX(float localX){return bodyLeft+localX*scale;}
    public float screenY(float localY){return bodyTop+localY*scale;}
  }

  private AttackCompositeTransform(){}

  /**
   * Match CONTACT apparent BODY height to the accepted idle alpha height, then align the visible
   * feet and alpha centers in final screen space. This preserves the authored action silhouette;
   * it performs no crop and fabricates no temporal frame.
   */
  public static Result normalize(
      float actorX,float logicalAnchorY,float baseScale,float sourceFootAnchorY,float sourceCenterX,
      int idleTop,int idleBottom,float idleCenterX,
      int actionTop,int actionBottom,float actionCenterX){
    int idleHeight=idleBottom-idleTop+1;
    int actionHeight=actionBottom-actionTop+1;
    if(baseScale<=0f||idleHeight<=0||actionHeight<=0)
      return new Result(baseScale,actorX-sourceCenterX*baseScale,
          logicalAnchorY-sourceFootAnchorY*baseScale,logicalAnchorY,actorX);
    float scale=baseScale*(idleHeight/(float)actionHeight);
    float idleFrameTop=logicalAnchorY-sourceFootAnchorY*baseScale;
    float targetFootY=idleFrameTop+idleBottom*baseScale;
    float targetCenterX=actorX+(idleCenterX-sourceCenterX)*baseScale;
    float bodyLeft=targetCenterX-actionCenterX*scale;
    float bodyTop=targetFootY-actionBottom*scale;
    return new Result(scale,bodyLeft,bodyTop,targetFootY,targetCenterX);
  }

  public static boolean finalGeometryWithinTolerance(Result r,int actionTop,int actionBottom,float actionCenterX,
      float expectedHeight,float expectedFootY,float expectedCenterX){
    if(r==null)return false;
    float height=(actionBottom-actionTop+1)*r.scale;
    float foot=r.screenY(actionBottom);
    float center=r.screenX(actionCenterX);
    return Math.abs(height-expectedHeight)<=MAX_HEIGHT_ERROR_PX
        && Math.abs(foot-expectedFootY)<=MAX_FOOT_ERROR_PX
        && Math.abs(center-expectedCenterX)<=1f;
  }
}
