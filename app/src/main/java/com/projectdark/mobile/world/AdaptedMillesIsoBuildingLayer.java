package com.projectdark.mobile.world;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** [ADAPTED]/[B] buildings authored directly in the live 64x32 isometric projection. */
public final class AdaptedMillesIsoBuildingLayer {
  public static final class Building {
    public final String structureId;
    public final float centerX,centerY,halfWidth,halfDepth,wallHeight,roofRise,roofOverhang;
    public final float collisionLeft,collisionTop,collisionRight,collisionBottom;
    public final float doorFootX,doorFootY,approachX,approachY;
    public final String evidence,status,assetRef;
    Building(String structureId,float centerX,float centerY,float halfWidth,float halfDepth,float wallHeight,float roofRise,float roofOverhang,String assetRef){
      this.structureId=structureId;this.centerX=centerX;this.centerY=centerY;this.halfWidth=halfWidth;this.halfDepth=halfDepth;
      this.wallHeight=wallHeight;this.roofRise=roofRise;this.roofOverhang=roofOverhang;
      collisionLeft=centerX-halfWidth;collisionTop=centerY-halfDepth;collisionRight=centerX+halfWidth;collisionBottom=centerY+halfDepth;
      // Production v2 house/shop/inn all place the visible door at the south/base vertex.
      doorFootX=centerX;doorFootY=centerY+halfDepth;
      approachX=centerX+halfWidth*.5f;approachY=centerY+halfDepth*1.5f;
      evidence="ADAPTED/B";status="COHERENT_ISOMETRIC_VILLAGE_BUILDING";this.assetRef=assetRef;
    }
    public boolean footprintContains(float x,float y){return Math.abs(x-centerX)/halfWidth+Math.abs(y-centerY)/halfDepth<=1f;}
    /** Conservative circle-vs-isometric-footprint test used by player navigation. */
    public boolean blocksPlayer(float x,float y,float radius){
      float dx=Math.max(0f,Math.abs(x-centerX)-Math.max(0f,radius));
      float dy=Math.max(0f,Math.abs(y-centerY)-Math.max(0f,radius));
      return dx/halfWidth+dy/halfDepth<=1f;
    }
    public boolean ownsCollisionBounds(float left,float top,float right,float bottom){
      return close(left,collisionLeft)&&close(top,collisionTop)&&close(right,collisionRight)&&close(bottom,collisionBottom);
    }
  }

  private static final Building WEST_HOUSE=new Building("west_house",320f,560f,64f,32f,54f,30f,8f,"PENDING_CROP/milles/building/west_house_iso");
  private static final Building PLAZA_SHOP=new Building("plaza_landmark_a",736f,512f,64f,32f,58f,34f,8f,"PENDING_CROP/milles/building/plaza_shop_iso");
  private static final Building PLAZA_HOUSE=new Building("plaza_landmark_b",864f,640f,64f,32f,50f,28f,7f,"PENDING_CROP/milles/building/plaza_house_iso");
  private static final List<Building> BUILDINGS=Collections.unmodifiableList(Arrays.asList(WEST_HOUSE,PLAZA_SHOP,PLAZA_HOUSE));
  private AdaptedMillesIsoBuildingLayer(){}
  public static List<Building> buildings(){return BUILDINGS;}
  public static Building byStructureId(String id){for(Building building:BUILDINGS)if(building.structureId.equals(id))return building;return null;}
  public static Building byCollisionBounds(float left,float top,float right,float bottom){
    for(Building building:BUILDINGS)if(building.ownsCollisionBounds(left,top,right,bottom))return building;
    return null;
  }
  private static boolean close(float a,float b){return Math.abs(a-b)<.01f;}
}
