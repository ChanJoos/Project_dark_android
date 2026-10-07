package com.projectdark.mobile.world;

/** Immutable authored path, creek and crossing coordinates shared by rendering and collision. */
public final class PoteForestGeometry {
  public static final float BRIDGE_X=1200f,BRIDGE_Y=440f;
  private static final float[][] TRAIL_CENTERLINE={
    {700,492},{790,510},{880,482},{975,505},{1060,474},{1135,458},
    {1200,440},{1290,466},{1380,438},{1485,455},{1605,420},{2048,600},{2464,816},{2816,1088},{3296,1376},{3712,1696},{3968,1888}
  };
  private static final float[][] CREEK_CENTERLINE={
    {576,2048},{680,1776},{720,1504},{800,1248},{800,896},{900,820},{980,744},{1075,668},{1120,592},{1120,516},
    {1200,440},{1285,364},{1360,288},{1440,212},{1405,136},{1510,64}
  };
  private static final float[][] B_TRAIL={{128,2048},{320,1984},{700,1760},{1100,1520},{1568,1248},{2112,1024},{2656,768},{3200,512},{3776,288},{4224,160}};
  private static final float[][] B_CREEK={{64,640},{600,800},{1100,1040},{1568,1248},{2048,1504},{2688,1680},{3328,1776},{3856,1888},{4352,1984}};
  private static final float[][] C_TRAIL={{128,2304},{320,2240},{896,1952},{1536,1616},{2240,1248},{2816,928},{3392,608},{4032,384},{4736,144}};
  private static final float[][] C_CREEK={{1856,64},{1968,448},{2096,864},{2240,1248},{2528,1584},{2912,1920},{3328,2176},{3712,2432}};
  private static final float[][] D_TRAIL={{128,768},{256,768},{640,688},{1024,560},{1536,512},{2048,560},{2560,688},{2944,768}};
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
  public static float bridgeX(String mapId){return "MAP_POTE_02".equals(mapId)?1568f:"MAP_POTE_03".equals(mapId)?2240f:BRIDGE_X;}
  public static float bridgeY(String mapId){return "MAP_POTE_02".equals(mapId)?1248f:"MAP_POTE_03".equals(mapId)?1248f:BRIDGE_Y;}
  public static float[][] channel(String mapId){
    float[][] line=creekCenterline(mapId);java.util.List<float[]> out=new java.util.ArrayList<>();float length=0;
    for(int i=0;i<line.length-1;i++){
      float[] p0=line[Math.max(0,i-1)],p1=line[i],p2=line[i+1],p3=line[Math.min(line.length-1,i+2)];
      int count=Math.max(2,(int)Math.ceil(Math.hypot(p2[0]-p1[0],p2[1]-p1[1])/14));
      for(int j=0;j<count;j++){float t=j/(float)count,t2=t*t,t3=t2*t;
        float x=.5f*(2*p1[0]+(-p0[0]+p2[0])*t+(2*p0[0]-5*p1[0]+4*p2[0]-p3[0])*t2+(-p0[0]+3*p1[0]-3*p2[0]+p3[0])*t3);
        float y=.5f*(2*p1[1]+(-p0[1]+p2[1])*t+(2*p0[1]-5*p1[1]+4*p2[1]-p3[1])*t2+(-p0[1]+3*p1[1]-3*p2[1]+p3[1])*t3);
        if(!out.isEmpty()){float[] prev=out.get(out.size()-1);length+=Math.hypot(x-prev[0],y-prev[1]);}
        float width=29+5*(float)Math.sin(length*.018)+3*(float)Math.sin(length*.047);out.add(new float[]{x,y,width});
      }
    }
    if(line.length>0){float[] last=line[line.length-1];out.add(new float[]{last[0],last[1],29});}
    return out.toArray(new float[0][]);
  }
  public static java.util.List<android.graphics.RectF> waterObstacles(String map){
    java.util.List<android.graphics.RectF> out=new java.util.ArrayList<>();
    for(float[] a:channel(map)){
      // Footbridge is longer than the water width; both banks remain connected at its centre only.
      if(Math.hypot(a[0]-bridgeX(map),a[1]-bridgeY(map))<82)continue;
      float half=a[2]-3;out.add(new android.graphics.RectF(a[0]-half,a[1]-half,a[0]+half,a[1]+half));
    }
    return out;
  }
  private static float[][] copy(float[][] points){float[][] out=new float[points.length][2];for(int i=0;i<points.length;i++)out[i]=points[i].clone();return out;}
}
