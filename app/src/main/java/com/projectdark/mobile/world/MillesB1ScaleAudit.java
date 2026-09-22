package com.projectdark.mobile.world;

/** Bundle B1 acceptance gate for character-relative building scale and readable town circulation. */
public final class MillesB1ScaleAudit {
  private static final float MIN_SCALE=4f; // B1 CI trigger: scale contract is intentional and device-visible.
  private MillesB1ScaleAudit(){}

  public static boolean verify(){
    if(AdaptedMillesIsoBuildingLayer.B1_BUILDING_SCALE<MIN_SCALE)return false;
    if(AdaptedMillesIsoBuildingLayer.buildings().size()<3)return false;
    for(AdaptedMillesIsoBuildingLayer.Building b:AdaptedMillesIsoBuildingLayer.buildings()){
      if(b.halfWidth<180f||b.wallHeight<190f)return false;
      if(b.approachY<=b.collisionBottom)return false;
      if(insideCoreLane(b.collisionLeft,b.collisionTop,b.collisionRight,b.collisionBottom))return false;
    }
    // Core B0 gameplay actors must remain outside the enlarged building masses.
    float[][] actors={{620f,560f},{665f,615f},{1315f,715f},{1900f,820f}};
    for(float[] a:actors)for(AdaptedMillesIsoBuildingLayer.Building b:AdaptedMillesIsoBuildingLayer.buildings())
      if(a[0]>=b.collisionLeft&&a[0]<=b.collisionRight&&a[1]>=b.collisionTop&&a[1]<=b.collisionBottom)return false;
    return true;
  }

  private static boolean insideCoreLane(float l,float t,float r,float b){
    // Protect the spawn/quest plaza and its south/east circulation spine from 4x structures.
    return overlaps(l,t,r,b,500f,480f,830f,730f)
        ||overlaps(l,t,r,b,540f,690f,760f,1510f)
        ||overlaps(l,t,r,b,780f,430f,980f,660f);
  }
  private static boolean overlaps(float l,float t,float r,float b,float al,float at,float ar,float ab){
    return r>al&&l<ar&&b>at&&t<ab;
  }
  public static void main(String[] args){if(!verify())throw new AssertionError("MillesB1ScaleAudit failed");System.out.println("Milles B1 scale audit OK");}
}
