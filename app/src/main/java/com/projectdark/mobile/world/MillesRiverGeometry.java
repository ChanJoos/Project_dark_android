package com.projectdark.mobile.world;

import android.graphics.Path;
import java.util.List;

/** V102 adapted river. One geometry supplies both the visible water and water collision. */
public final class MillesRiverGeometry {
  public static final float HALF_WIDTH=64f, BANK_WIDTH=73f;
  public static final float BRIDGE_X=1880f,BRIDGE_Y=1050f;
  private static final float[][] CENTER={{1824,32},{1808,352},{1808,640},{1760,970},{1880,1050},{2100,1160},{2210,1320},{2200,1460},{2320,1632}};
  private MillesRiverGeometry(){}
  public static Path centerline(){Path p=new Path();p.moveTo(CENTER[0][0],CENTER[0][1]);for(int i=1;i<CENTER.length;i++)p.lineTo(CENTER[i][0],CENTER[i][1]);return p;}
  public static float distance(float x,float y){float best=Float.MAX_VALUE;for(int i=1;i<CENTER.length;i++){float[] a=CENTER[i-1],b=CENTER[i];float dx=b[0]-a[0],dy=b[1]-a[1],t=Math.max(0,Math.min(1,((x-a[0])*dx+(y-a[1])*dy)/(dx*dx+dy*dy)));best=Math.min(best,(float)Math.hypot(x-a[0]-t*dx,y-a[1]-t*dy));}return best;}
  /** Same SW-NE axis as the existing visible wooden deck, with no invisible bypass. */
  public static boolean bridgeDeck(float x,float y){return x>=1680&&x<=2096&&Math.abs((y-BRIDGE_Y)+.5f*(x-BRIDGE_X))<34;}
  public static void addWaterFootprints(List<MillesProductionCollision.Footprint> out){
    // Contiguous 8px ground contacts follow the water instead of the former sparse pond dots.
    // A whole 64x32 diagonal walk lane is cut out only beneath the visible bridge.
    for(int y=48;y<=1600;y+=8){
      int start=-1;
      for(int x=1632;x<=2312;x+=8){
        boolean water=x<=2304&&distance(x,y)<=HALF_WIDTH-4&&!bridgeDeck(x,y);
        if(water&&start<0)start=x;
        if(!water&&start>=0){out.add(new MillesProductionCollision.Footprint("river_"+start+"_"+y,MillesProductionCollision.Kind.LAKE,start-4,y-4,x-4,y+4));start=-1;}
      }
    }
  }
}
