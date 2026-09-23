package com.projectdark.mobile.world;

/** Deterministic B4 audit for potion-shop enter/move/exit semantics. */
public final class BuildingInteriorRuntimeAudit {
  private BuildingInteriorRuntimeAudit(){}
  public static boolean verify(){
    BuildingInteriorRuntime r=new BuildingInteriorRuntime();
    if(r.active()||r.exit())return false;
    if(!r.enterPotionShop(320f,530f)||!r.active())return false;
    if(!BuildingInteriorRuntime.POTION_SHOP_MAP_ID.equals(r.mapId()))return false;
    if(Math.abs(r.exteriorReturnX()-320f)>.01f||Math.abs(r.exteriorReturnY()-530f)>.01f)return false;
    if(!r.move(1f,-1f,64f))return false;
    for(int i=0;i<20;i++)r.move(0f,1f,32f);
    if(!r.atExit())return false;
    if(!r.exit()||r.active())return false;
    return true;
  }
  public static void main(String[] args){
    if(!verify())throw new AssertionError("BuildingInteriorRuntimeAudit failed");
    System.out.println("BuildingInteriorRuntimeAudit PASS");
  }
}
