package com.projectdark.mobile.world;
import android.graphics.RectF;import java.util.*;
/** User-requested compact camps and square arenas; authored mobile layout, not original topology. */
public final class CoastalHuntLayout {
 public static float size(int zone){return zone==0?768:zone==4?2304:2560;}
 public static float entryX(int zone){return zone==0?288:352;}public static float entryY(int zone){return zone==0?432:size(zone)-448;}
 public static float backX(int zone){return zone==0?128:192;}public static float backY(int zone){return zone==0?512:size(zone)-352;}
 public static float nextX(int zone){return size(zone)-(zone==0?256:224);}public static float nextY(int zone){return zone==0?448:512;}
 public static float shore(int zone,boolean rucesion,float x){float n=size(zone);return zone==0?656:zone==4?n-176:n-230+(float)Math.sin(x/270+zone+(rucesion?.8:0))*52;}
 public static final class Prop {public final String asset;public final float x,y,scale;public final boolean solid,canopy;public final RectF footprint;
  Prop(String a,float x,float y,float s,boolean solid,boolean canopy){asset=a;this.x=x;this.y=y;scale=s;this.solid=solid;this.canopy=canopy;float rx=a.startsWith("reef")?48:a.equals("boulders")?24:a.equals("barrels")?24:14,ry=a.startsWith("reef")?24:a.equals("boulders")?14:10;footprint=new RectF(x-rx*s,y-ry*s,x+rx*s,y+ry*s);}}
 private static float distance(float x,float y,float ax,float ay,float bx,float by){float dx=bx-ax,dy=by-ay,t=Math.max(0,Math.min(1,((x-ax)*dx+(y-ay)*dy)/(dx*dx+dy*dy)));return (float)Math.hypot(x-ax-t*dx,y-ay-t*dy);}
 private static void add(List<Prop> out,int z,boolean r,String a,float x,float y,float s,boolean solid,boolean canopy){float n=size(z);if(x<100||x>n-100||y<160||y>shore(z,r,x)-70)return;if(solid&&(distance(x,y,backX(z),backY(z),entryX(z),entryY(z))<160||distance(x,y,entryX(z),entryY(z),nextX(z),nextY(z))<110||Math.hypot(x-nextX(z),y-nextY(z))<170))return;out.add(new Prop(a,x,y,s,solid,canopy));}
 private static void reef(List<Prop> out,int z,boolean r,float x,float y,float s){add(out,z,r,"reef_tall",x,y,s,true,false);add(out,z,r,"reef_low",x-85*s,y+42*s,s,true,false);add(out,z,r,"reef_low",x+75*s,y+55*s,s,true,false);add(out,z,r,"boulders",x+120*s,y+95*s,.9f*s,true,false);add(out,z,r,"fern",x-95*s,y+110*s,.9f,false,false);}
 private static void grove(List<Prop> out,int z,boolean r,float x,float y,float s){add(out,z,r,"palm_curve",x,y,s,true,true);add(out,z,r,"banana",x+100,y+75,.9f*s,true,true);add(out,z,r,"palm_fan",x-75,y+110,s,true,true);add(out,z,r,"fern",x+85,y+140,1,false,false);}
 public static List<Prop> props(int z,boolean r){List<Prop> out=new ArrayList<>();float n=size(z);
  if(z==0){grove(out,z,r,260,210,.85f);reef(out,z,r,550,210,.65f);add(out,z,r,"barrels",460,590,.8f,true,false);add(out,z,r,"driftwood",280,630,.8f,false,false);}
  else if(z==1){for(float[] p:new float[][]{{.25f,.28f},{.27f,.58f},{.72f,.78f}})reef(out,z,r,n*p[0],n*p[1],1.8f);grove(out,z,r,n*.69f,n*.25f,1.5f);grove(out,z,r,n*.79f,n*.48f,1.2f);}
  else if(z==2){for(int i=0;i<4;i++){reef(out,z,r,n*.37f,420+i*460,1.65f);reef(out,z,r,n*.73f,650+i*400,1.35f);}grove(out,z,r,650,n*.18f,1.45f);grove(out,z,r,n*.53f,n*.82f,1.3f);}
  else if(z==3){for(float[] p:new float[][]{{.45f,.40f},{.53f,.48f},{.42f,.60f},{.66f,.69f},{.79f,.22f}})reef(out,z,r,n*p[0],n*p[1],2.35f);grove(out,z,r,600,500,1.45f);grove(out,z,r,n*.80f,n*.80f,1.4f);}
  else{reef(out,z,r,550,460,1.9f);reef(out,z,r,n-530,n-470,1.9f);grove(out,z,r,n-500,430,1.5f);grove(out,z,r,480,n-550,1.5f);}
  if(z>0){int count=r?7:10;for(int i=0;i<count;i++){float x=420+i*(n-760)/Math.max(1,count-1);add(out,z,r,"palm_curve",x,shore(z,r,x)-110-(i%3)*80,1.2f,true,true);}for(int i=0;i<64;i++){float x=340+(i*173)%(int)(n-650),y=250+(i*317)%(int)(n-500);add(out,z,r,i%4==0?"bones":i%4==1?"driftwood":i%4==2?"shells":"boulders",x,y,i%4==3?.55f:.8f,false,false);}}
  out.sort(Comparator.comparingDouble(p->p.y));return Collections.unmodifiableList(out);}
 private CoastalHuntLayout(){}
}
