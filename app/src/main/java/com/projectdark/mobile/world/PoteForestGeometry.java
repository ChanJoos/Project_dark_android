package com.projectdark.mobile.world;

/** Immutable authored creek and crossing coordinates shared by Pote rendering and collision. */
public final class PoteForestGeometry {
  public static final float BRIDGE_X=1510f,BRIDGE_Y=516f;
  private static final float[][] CREEK_CENTERLINE={
    {1460,896},{1490,820},{1465,744},{1500,668},{1470,592},{1510,516},
    {1475,440},{1515,364},{1485,288},{1525,212},{1495,136},{1510,64}
  };
  private PoteForestGeometry(){}

  public static float[][] creekCenterline(){
    float[][] copy=new float[CREEK_CENTERLINE.length][2];
    for(int i=0;i<CREEK_CENTERLINE.length;i++)copy[i]=CREEK_CENTERLINE[i].clone();
    return copy;
  }
}
