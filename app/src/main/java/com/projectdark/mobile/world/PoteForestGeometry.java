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
  private static final float[][] B_TRAIL={{320,928},{510,850},{700,900},{890,785},{1080,710},{1260,650},{1450,535},{1640,430},{1840,330},{2030,250},{2208,160}};
  private static final float[][] B_CREEK={{64,500},{300,560},{520,650},{760,710},{980,700},{1160,680},{1320,650},{1500,690},{1720,760},{1950,740},{2200,660},{2304,600}};
  private static final float[][] C_TRAIL={{320,1312},{500,1200},{680,1120},{880,1060},{1080,980},{1280,900},{1480,820},{1680,700},{1900,580},{2120,460},{2360,340},{2560,240},{2704,144}};
  private static final float[][] C_CREEK={{1200,64},{1260,220},{1340,380},{1440,540},{1520,680},{1600,780},{1710,900},{1820,1040},{1940,1190},{2080,1320},{2200,1472}};
  private static final float[][] D_TRAIL={{256,512},{420,460},{580,390},{740,340},{900,300},{1080,310},{1240,360},{1400,420},{1536,512}};
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
  public static float[][] trailCenterline(String mapId){return copy("MAP_POTE_02".equals(mapId)?B_TRAIL:"MAP_POTE_03".equals(mapId)?C_TRAIL:com.projectdark.mobile.world.CampaignWorld.BOSS_D.equals(mapId)?D_TRAIL:TRAIL_CENTERLINE);}
  public static float[][] creekCenterline(String mapId){return copy("MAP_POTE_02".equals(mapId)?B_CREEK:"MAP_POTE_03".equals(mapId)?C_CREEK:com.projectdark.mobile.world.CampaignWorld.BOSS_D.equals(mapId)?new float[0][2]:CREEK_CENTERLINE);}
  public static float bridgeX(String mapId){return "MAP_POTE_02".equals(mapId)?1320f:"MAP_POTE_03".equals(mapId)?1600f:BRIDGE_X;}
  public static float bridgeY(String mapId){return "MAP_POTE_02".equals(mapId)?650f:"MAP_POTE_03".equals(mapId)?780f:BRIDGE_Y;}
  private static float[][] copy(float[][] points){float[][] out=new float[points.length][2];for(int i=0;i<points.length;i++)out[i]=points[i].clone();return out;}
}
