package com.projectdark.mobile.world;

/**
 * [ADAPTED] World-owned camera projection contract.
 *
 * Logical movement/collision stays in world coordinates. Rendering/input may use this transform to
 * project world<->screen using a following camera with a configurable dead-zone and map clamp.
 * HUD remains screen-space and must not be passed through this transform.
 */
public final class WorldCameraTransform {
  public static final class Point {
    public final float x,y;
    public Point(float x,float y){this.x=x;this.y=y;}
  }

  private final float worldMinX,worldMaxX,worldMinY,worldMaxY;
  private final float viewportWidth,viewportHeight;
  private final float anchorX,anchorY;
  private final float deadZoneHalfWidth,deadZoneHalfHeight;
  private float cameraX,cameraY;

  public WorldCameraTransform(float worldMinX,float worldMaxX,float worldMinY,float worldMaxY,
      float viewportWidth,float viewportHeight){
    this(worldMinX,worldMaxX,worldMinY,worldMaxY,viewportWidth,viewportHeight,
        viewportWidth*.5f,viewportHeight*.48f,viewportWidth*.10f,viewportHeight*.08f);
  }

  public WorldCameraTransform(float worldMinX,float worldMaxX,float worldMinY,float worldMaxY,
      float viewportWidth,float viewportHeight,float anchorX,float anchorY,
      float deadZoneHalfWidth,float deadZoneHalfHeight){
    if(worldMaxX<worldMinX||worldMaxY<worldMinY)throw new IllegalArgumentException("invalid world bounds");
    if(viewportWidth<=0f||viewportHeight<=0f)throw new IllegalArgumentException("viewport must be positive");
    if(deadZoneHalfWidth<0f||deadZoneHalfHeight<0f)throw new IllegalArgumentException("dead-zone must be non-negative");
    this.worldMinX=worldMinX;this.worldMaxX=worldMaxX;this.worldMinY=worldMinY;this.worldMaxY=worldMaxY;
    this.viewportWidth=viewportWidth;this.viewportHeight=viewportHeight;
    this.anchorX=anchorX;this.anchorY=anchorY;
    this.deadZoneHalfWidth=deadZoneHalfWidth;this.deadZoneHalfHeight=deadZoneHalfHeight;
    cameraX=clampCameraX(worldMinX-anchorX);
    cameraY=clampCameraY(worldMinY-anchorY);
  }

  /** Snap camera to place the supplied world point at the configured anchor, then clamp to map bounds. */
  public void snapTo(float worldX,float worldY){
    cameraX=clampCameraX(worldX-anchorX);
    cameraY=clampCameraY(worldY-anchorY);
  }

  /**
   * Follow only when the player leaves the dead-zone. This avoids micro-jitter while preserving
   * world-scroll presentation. Call once after logical movement; never feed camera offsets back into collision.
   */
  public void follow(float playerWorldX,float playerWorldY){
    float screenX=playerWorldX-cameraX;
    float screenY=playerWorldY-cameraY;
    float minScreenX=anchorX-deadZoneHalfWidth,maxScreenX=anchorX+deadZoneHalfWidth;
    float minScreenY=anchorY-deadZoneHalfHeight,maxScreenY=anchorY+deadZoneHalfHeight;
    if(screenX<minScreenX)cameraX=clampCameraX(playerWorldX-minScreenX);
    else if(screenX>maxScreenX)cameraX=clampCameraX(playerWorldX-maxScreenX);
    if(screenY<minScreenY)cameraY=clampCameraY(playerWorldY-minScreenY);
    else if(screenY>maxScreenY)cameraY=clampCameraY(playerWorldY-maxScreenY);
  }

  public Point worldToScreen(float worldX,float worldY){return new Point(worldX-cameraX,worldY-cameraY);}
  public Point screenToWorld(float screenX,float screenY){return new Point(screenX+cameraX,screenY+cameraY);}

  public float cameraX(){return cameraX;}
  public float cameraY(){return cameraY;}
  public float anchorX(){return anchorX;}
  public float anchorY(){return anchorY;}

  private float clampCameraX(float value){
    float max=worldMaxX-viewportWidth;
    if(max<=worldMinX)return (worldMinX+worldMaxX-viewportWidth)*.5f;
    return clamp(value,worldMinX,max);
  }
  private float clampCameraY(float value){
    float max=worldMaxY-viewportHeight;
    if(max<=worldMinY)return (worldMinY+worldMaxY-viewportHeight)*.5f;
    return clamp(value,worldMinY,max);
  }
  private static float clamp(float v,float min,float max){return Math.max(min,Math.min(max,v));}
}
