package com.projectdark.mobile.world;

/** Immutable authored path, creek and crossing coordinates shared by rendering and collision. */
public final class PoteForestGeometry {
  public static final float BRIDGE_X=1200f,BRIDGE_Y=440f;
  private static final float[][] TRAIL_CENTERLINE={
    {700,492},{790,510},{880,482},{975,505},{1060,474},{1135,458},
    {1200,440},{1290,466},{1380,438},{1485,455},{1605,420}
  };
  private static final float[][] CREEK_CENTERLINE={
    {800,896},{900,820},{980,744},{1075,668},{1120,592},{1120,516},
    {1200,440},{1285,364},{1360,288},{1440,212},{1405,136},{1510,64}
  };
  private PoteForestGeometry(){}

  public static float[][] creekCenterline(){
    float[][] copy=new float[CREEK_CENTERLINE.length][2];
    for(int i=0;i<CREEK_CENTERLINE.length;i++)copy[i]=CREEK_CENTERLINE[i].clone();
    return copy;
  }
  public static float[][] trailCenterline(){
    float[][] copy=new float[TRAIL_CENTERLINE.length][2];
    for(int i=0;i<TRAIL_CENTERLINE.length;i++)copy[i]=TRAIL_CENTERLINE[i].clone();
    return copy;
  }
}
