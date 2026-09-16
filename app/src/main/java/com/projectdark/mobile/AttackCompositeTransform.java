package com.projectdark.mobile;

/**
 * Final screen-space normalization for the authored single CONTACT attack pose.
 * BODY, robe and weapon consume one immutable transform. No layer may independently rescale,
 * re-anchor or reinterpret the locked attack facing.
 */
public final class AttackCompositeTransform {
  public static final float MAX_FOOT_ERROR_PX=1f;
  public static final float MAX_HEIGHT_ERROR_PX=1f;
  public static final float MAX_CENTER_ERROR_PX=1f;
  public static final float MAX_HANDLE_ERROR_PX=1f;

  public static final class Result {
    public final float scale,bodyLeft,bodyTop,targetFootY,targetCenterX,mirrorPivotX;
    Result(float scale,float bodyLeft,float bodyTop,float targetFootY,float targetCenterX,float mirrorPivotX){
      this.scale=scale;this.bodyLeft=bodyLeft;this.bodyTop=bodyTop;
      this.targetFootY=targetFootY;this.targetCenterX=targetCenterX;this.mirrorPivotX=mirrorPivotX;
    }
    public float screenX(float localX){return bodyLeft+localX*scale;}
    public float screenY(float localY){return bodyTop+localY*scale;}
    public float mirroredScreenX(float localX,boolean mirrored){
      float x=screenX(localX);return mirrored?2f*mirrorPivotX-x:x;
    }
    /** Place a detached authored layer in the same source-global coordinate system as BODY. */
    public float layerLeft(float layerAuthoredOffsetX,float bodyAuthoredOffsetX,float residualLocalX){
      return bodyLeft+(layerAuthoredOffsetX-bodyAuthoredOffsetX+residualLocalX)*scale;
    }
    public float layerTop(float layerAuthoredOffsetY,float bodyAuthoredOffsetY,float residualLocalY){
      return bodyTop+(layerAuthoredOffsetY-bodyAuthoredOffsetY+residualLocalY)*scale;
    }
    /** Final screen-space attachment point for a BODY-local semantic anchor. */
    public float handleX(float localX,boolean mirrored){return mirroredScreenX(localX,mirrored);}
    public float handleY(float localY){return screenY(localY);}
  }

  private AttackCompositeTransform(){}

  /**
   * Match CONTACT apparent BODY height to the accepted idle alpha height, then align visible foot
   * and alpha center in final screen space. The authored action silhouette is preserved: no crop,
   * no fabricated temporal frame. actorX is also the single mirror pivot for the whole composite.
   */
  public static Result normalize(
      float actorX,float logicalAnchorY,float baseScale,float sourceFootAnchorY,float sourceCenterX,
      int idleTop,int idleBottom,float idleCenterX,
      int actionTop,int actionBottom,float actionCenterX){
    int idleHeight=idleBottom-idleTop+1,actionHeight=actionBottom-actionTop+1;
    if(baseScale<=0f||idleHeight<=0||actionHeight<=0)
      return new Result(baseScale,actorX-sourceCenterX*baseScale,
          logicalAnchorY-sourceFootAnchorY*baseScale,logicalAnchorY,actorX,actorX);
    float scale=baseScale*(idleHeight/(float)actionHeight);
    float idleFrameTop=logicalAnchorY-sourceFootAnchorY*baseScale;
    float targetFootY=idleFrameTop+idleBottom*baseScale;
    float targetCenterX=actorX+(idleCenterX-sourceCenterX)*baseScale;
    float bodyLeft=targetCenterX-actionCenterX*scale;
    float bodyTop=targetFootY-actionBottom*scale;
    return new Result(scale,bodyLeft,bodyTop,targetFootY,targetCenterX,actorX);
  }

  public static boolean finalGeometryWithinTolerance(Result r,int actionTop,int actionBottom,float actionCenterX,
      float expectedHeight,float expectedFootY,float expectedCenterX){
    if(r==null)return false;
    float height=(actionBottom-actionTop+1)*r.scale;
    float foot=r.screenY(actionBottom),center=r.screenX(actionCenterX);
    return Math.abs(height-expectedHeight)<=MAX_HEIGHT_ERROR_PX
        && Math.abs(foot-expectedFootY)<=MAX_FOOT_ERROR_PX
        && Math.abs(center-expectedCenterX)<=MAX_CENTER_ERROR_PX;
  }

  /** Regression for the atomic composition rule: every layer uses exactly the BODY transform. */
  public static boolean coherentLayerTransform(Result r,
      float bodyOffsetX,float bodyOffsetY,float robeOffsetX,float robeOffsetY,
      float residualX,float residualY,float handLocalX,float handLocalY,boolean mirrored){
    if(r==null||r.scale<=0f)return false;
    float robeLeft=r.layerLeft(robeOffsetX,bodyOffsetX,residualX);
    float robeTop=r.layerTop(robeOffsetY,bodyOffsetY,residualY);
    float expectedRobeLeft=r.bodyLeft+(robeOffsetX-bodyOffsetX+residualX)*r.scale;
    float expectedRobeTop=r.bodyTop+(robeOffsetY-bodyOffsetY+residualY)*r.scale;
    float handleX=r.handleX(handLocalX,mirrored),handleY=r.handleY(handLocalY);
    float rawHandleX=r.bodyLeft+handLocalX*r.scale;
    float expectedHandleX=mirrored?2f*r.mirrorPivotX-rawHandleX:rawHandleX;
    float expectedHandleY=r.bodyTop+handLocalY*r.scale;
    return Math.abs(robeLeft-expectedRobeLeft)<=MAX_CENTER_ERROR_PX
        && Math.abs(robeTop-expectedRobeTop)<=MAX_FOOT_ERROR_PX
        && Math.abs(handleX-expectedHandleX)<=MAX_HANDLE_ERROR_PX
        && Math.abs(handleY-expectedHandleY)<=MAX_HANDLE_ERROR_PX;
  }
}
