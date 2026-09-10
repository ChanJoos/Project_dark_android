package com.projectdark.mobile.world;

import java.util.Collections;
import java.util.List;

/** First [ADAPTED]/[B] building authored directly in the live 64x32 isometric projection. */
public final class AdaptedMillesIsoBuildingLayer {
  public static final class Building {
    public final String structureId;
    public final float centerX,centerY,halfWidth,halfDepth,wallHeight,roofRise,roofOverhang;
    public final float collisionLeft,collisionTop,collisionRight,collisionBottom;
    public final float doorFootX,doorFootY,approachX,approachY;
    public final String evidence,status,assetRef;
    Building(String structureId,float centerX,float centerY,float halfWidth,float halfDepth,float wallHeight,float roofRise,float roofOverhang){
      this.structureId=structureId;this.centerX=centerX;this.centerY=centerY;this.halfWidth=halfWidth;this.halfDepth=halfDepth;
      this.wallHeight=wallHeight;this.roofRise=roofRise;this.roofOverhang=roofOverhang;
      collisionLeft=centerX-halfWidth;collisionTop=centerY-halfDepth;collisionRight=centerX+halfWidth;collisionBottom=centerY+halfDepth;
      doorFootX=centerX+halfWidth*.5f;doorFootY=centerY+halfDepth*.5f;
      approachX=centerX+halfWidth*.5f;approachY=centerY+halfDepth*1.5f;
      evidence="ADAPTED/B";status="FIRST_COHERENT_ISOMETRIC_BUILDING";assetRef="PENDING_CROP/milles/building/plaza_shop_iso";
    }
    public boolean footprintContains(float x,float y){return Math.abs(x-centerX)/halfWidth+Math.abs(y-centerY)/halfDepth<=1f;}
  }

  private static final Building PLAZA_SHOP=new Building("plaza_landmark_a",736f,512f,64f,32f,58f,34f,8f);
  private static final List<Building> BUILDINGS=Collections.singletonList(PLAZA_SHOP);
  private AdaptedMillesIsoBuildingLayer(){}
  public static List<Building> buildings(){return BUILDINGS;}
  public static Building byStructureId(String id){return PLAZA_SHOP.structureId.equals(id)?PLAZA_SHOP:null;}
}
