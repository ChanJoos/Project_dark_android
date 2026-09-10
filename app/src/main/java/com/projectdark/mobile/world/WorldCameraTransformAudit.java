package com.projectdark.mobile.world;

/** Deterministic, Android-free audit for the world camera contract. */
public final class WorldCameraTransformAudit {
  private WorldCameraTransformAudit(){}

  public static boolean verify(){
    WorldCameraTransform camera=new WorldCameraTransform(96f,1184f,64f,864f,360f,240f,180f,120f,36f,24f);
    camera.snapTo(500f,470f);

    // Round-trip conversion must stay exact enough for tap-to-move world targeting.
    WorldCameraTransform.Point s=camera.worldToScreen(620f,510f);
    WorldCameraTransform.Point w=camera.screenToWorld(s.x,s.y);
    if(Math.abs(w.x-620f)>.001f||Math.abs(w.y-510f)>.001f)return false;

    // Player motion within dead-zone should not jitter the camera.
    float cx=camera.cameraX(),cy=camera.cameraY();
    WorldCameraTransform.Point playerScreen=camera.worldToScreen(500f,470f);
    camera.follow(500f+18f,470f+12f);
    if(Math.abs(camera.cameraX()-cx)>.001f||Math.abs(camera.cameraY()-cy)>.001f)return false;

    // Motion outside dead-zone should move camera so the world scrolls opposite the player motion.
    camera.follow(700f,470f);
    if(camera.cameraX()<=cx)return false;
    WorldCameraTransform.Point after=camera.worldToScreen(500f,470f);
    if(after.x>=playerScreen.x)return false;

    // Clamp at world edges.
    camera.snapTo(-1000f,-1000f);
    if(camera.cameraX()<96f||camera.cameraY()<64f)return false;
    camera.snapTo(5000f,5000f);
    if(camera.cameraX()>824.001f||camera.cameraY()>624.001f)return false;

    return true;
  }

  public static void main(String[] args){
    if(!verify())throw new AssertionError("WorldCameraTransform audit failed");
    System.out.println("WorldCameraTransformAudit PASS");
  }
}
