package com.projectdark.mobile.world;
import android.graphics.RectF;import java.util.*;
/** V159 source-observed coast motifs, individually authored unseen extents; no mirrored arenas. */
public final class CoastalHuntLayout {
 public static float size(int zone){return zone==0?768:zone==4?2304:2560;}
 public static float entryX(int z){return z==0?288:352;}public static float entryY(int z){return z==0?432:size(z)-448;}
 public static float backX(int z){return z==0?128:192;}public static float backY(int z){return z==0?512:size(z)-352;}
 public static float nextX(int z){return size(z)-(z==0?256:224);}public static float nextY(int z){return z==0?448:512;}
 private static final Map<Integer,float[][]> LINES=new HashMap<>();
 private static final Map<Integer,List<RectF>> WATER=new HashMap<>();
 private static float[][] soften(float[][] points,int axis){List<float[]> out=new ArrayList<>();for(int i=1;i<points.length;i++){float[] a=points[i-1],b=points[i];int steps=Math.max(1,(int)Math.ceil((b[axis]-a[axis])/24));for(int k=0;k<steps;k++){float t=k/(float)steps,e=t*t*(3-2*t);float[] q=new float[2];q[axis]=a[axis]+(b[axis]-a[axis])*t;q[1-axis]=a[1-axis]+(b[1-axis]-a[1-axis])*e;out.add(q);}}out.add(points[points.length-1]);return out.toArray(new float[0][]);}
 public static float[][] shoreLine(int z,boolean r){return LINES.computeIfAbsent(z*2+(r?1:0),k->createShoreLine(z,r));}
 private static float[][] createShoreLine(int z,boolean r){
  if(z==0)return soften(r?new float[][]{{64,620},{200,685},{380,700},{580,640},{768,665}}:new float[][]{{64,660},{230,695},{420,665},{590,620},{768,648}},0);
  float[][] line;
  if(r){switch(z){
   case 1:line=new float[][]{{64,2290},{430,2430},{810,2390},{1140,2210},{1510,1970},{1760,1680},{2070,1690},{2300,1910},{2560,2190}};break;
   case 2:line=new float[][]{{64,2350},{420,2380},{860,2310},{1090,2060},{1320,1960},{1630,2180},{2070,2390},{2560,2340}};break;
   case 3:line=new float[][]{{64,2330},{450,2440},{820,2320},{1030,2090},{1280,1840},{1500,1890},{1800,2150},{2190,2210},{2560,2060}};break;
   default:line=new float[][]{{64,2420},{450,2390},{840,2180},{1080,2020},{1380,2110},{1780,2390},{2120,2450},{2560,2300}};}}
  else{switch(z){
   case 1:line=new float[][]{{64,2360},{480,2390},{860,2240},{1140,1930},{1390,1710},{1580,1740},{1900,2160},{2200,2380},{2560,2440}};break;
   case 2:line=new float[][]{{64,2320},{480,2370},{980,2440},{1390,2350},{1710,2050},{1960,1720},{2280,1840},{2560,2140}};break;
   case 3:line=new float[][]{{64,2350},{480,2440},{860,2370},{1100,2150},{1380,2190},{1740,2420},{2190,2410},{2560,2250}};break;
   default:line=new float[][]{{64,2400},{520,2450},{940,2290},{1240,2210},{1530,2330},{1950,2390},{2250,2120},{2560,1940}};}}
  float factor=size(z)/2560;for(float[] p:line){p[0]*=factor;p[1]*=factor;}line[0][0]=64;return soften(line,0);
 }
 public static float shore(int z,boolean r,float x){float[][] a=shoreLine(z,r);for(int i=1;i<a.length;i++)if(x<=a[i][0])return a[i-1][1]+(a[i][1]-a[i-1][1])*(x-a[i-1][0])/(a[i][0]-a[i-1][0]);return a[a.length-1][1];}
 /** Source-like west coves instead of putting water only along the bottom of every map. */
 public static float[][] inlet(int z,boolean r){
  if(z==0)return new float[0][];
  float[][] a=r&&z==2?new float[][]{{64,290},{340,440},{550,720},{510,1080},{320,1350},{64,1480}}:
   !r&&z==3?new float[][]{{64,650},{330,790},{450,1080},{280,1260},{64,1390}}:
   r&&z==4?new float[][]{{64,230},{430,330},{650,580},{490,820},{64,960}}:new float[0][];
  float factor=size(z)/2560;for(float[] p:a){p[0]=p[0]==64?64:p[0]*factor;p[1]*=factor;}return soften(a,1);
 }
 public static List<RectF> water(int z,boolean r){return WATER.computeIfAbsent(z*2+(r?1:0),k->createWater(z,r));}
 private static List<RectF> createWater(int z,boolean r){List<RectF> out=new ArrayList<>();float n=size(z);for(float x=64;x<n;x+=16)out.add(new RectF(x,Math.min(shore(z,r,x),shore(z,r,x+16)),Math.min(n,x+16),n));float[][] a=inlet(z,r);if(a.length>0)for(float y=a[0][1];y<a[a.length-1][1];y+=16){float edge=64;for(int i=1;i<a.length;i++)if(y>=a[i-1][1]&&y<=a[i][1]){edge=a[i-1][0]+(a[i][0]-a[i-1][0])*(y-a[i-1][1])/(a[i][1]-a[i-1][1]);break;}out.add(new RectF(64,y,edge+8,y+16));}return Collections.unmodifiableList(out);}
 public static final class Prop {public final String asset;public final float x,y,scale;public final boolean solid,canopy;public final RectF footprint;
  Prop(String a,float x,float y,float s,boolean solid,boolean canopy){asset=a;this.x=x;this.y=y;scale=s;this.solid=solid;this.canopy=canopy;float rx=a.startsWith("reef")?48:a.equals("boulders")?24:a.equals("barrels")?24:14,ry=a.startsWith("reef")?24:a.equals("boulders")?14:10;footprint=new RectF(x-rx*s,y-ry*s,x+rx*s,y+ry*s);}}
 private static float distance(float x,float y,float ax,float ay,float bx,float by){float dx=bx-ax,dy=by-ay,t=Math.max(0,Math.min(1,((x-ax)*dx+(y-ay)*dy)/(dx*dx+dy*dy)));return (float)Math.hypot(x-ax-t*dx,y-ay-t*dy);}
 private static void add(List<Prop> out,int z,boolean r,String a,float x,float y,float s,boolean solid,boolean canopy){float n=size(z);if(x<100||x>n-100||y<160||y>shore(z,r,x)-30)return;Prop p=new Prop(a,x,y,s,solid,canopy);for(RectF water:water(z,r))if(RectF.intersects(p.footprint,water))return;if(solid&&(distance(x,y,backX(z),backY(z),entryX(z),entryY(z))<160||distance(x,y,entryX(z),entryY(z),nextX(z),nextY(z))<110||Math.hypot(x-nextX(z),y-nextY(z))<170))return;out.add(p);}
 // Uneven overlapping columns/base rocks, as seen in source cameras. No three-rock pyramid stamp.
 private static void reef(List<Prop> o,int z,boolean r,float x,float y,float s){int k=(int)(x+y)/37%3;add(o,z,r,"reef_tall",x,y,s,true,false);add(o,z,r,"reef_low",x-100*s,y+25*s,s*.9f,true,false);if(k!=0)add(o,z,r,"reef_tall",x+68*s,y+70*s,s*.67f,true,false);add(o,z,r,"reef_low",x+125*s,y+115*s,s*.72f,true,false);if(k==1)add(o,z,r,"boulders",x-140*s,y+100*s,.9f*s,true,false);add(o,z,r,"fern",x-80*s,y+90*s,1.1f,false,false);}
 private static void grove(List<Prop> o,int z,boolean r,float x,float y,float s){int k=(int)(x+y)/53%3;add(o,z,r,k==1?"palm_fan":"palm_curve",x,y,s,true,true);add(o,z,r,k==2?"palm_curve":"banana",x-110,y+85,.82f*s,true,true);add(o,z,r,k==0?"palm_fan":"fern",x+85,y+135,.8f*s,k==0,k==0);add(o,z,r,"fern",x-145,y+110,1.2f,false,false);}
 private static void rocks(List<Prop> o,int z,boolean r,float[][] plan){float f=size(z)/2560;for(float[] p:plan)reef(o,z,r,p[0]*f,p[1]*f,p[2]);}
 private static void trees(List<Prop> o,int z,boolean r,float[][] plan){float f=size(z)/2560;for(float[] p:plan)grove(o,z,r,p[0]*f,p[1]*f,p[2]);}
 public static List<Prop> props(int z,boolean r){List<Prop> o=new ArrayList<>();float n=size(z);
  if(z==0){if(r){grove(o,z,r,650,230,.9f);add(o,z,r,"palm_curve",170,275,.8f,true,true);reef(o,z,r,390,170,.45f);}else{grove(o,z,r,215,200,.8f);reef(o,z,r,600,250,.7f);}add(o,z,r,"barrels",660,560,.8f,true,false);add(o,z,r,"shells",400,610,.8f,false,false);}
  else if(r){switch(z){
   case 1:rocks(o,z,r,new float[][]{{570,450,2.5f},{400,790,1.3f},{2030,490,1.8f}});trees(o,z,r,new float[][]{{1330,570,1.8f},{1530,730,1.5f},{1700,950,1.7f},{940,1350,1.4f},{1150,1580,1.7f},{450,1770,1.25f}});break;
   case 2:rocks(o,z,r,new float[][]{{730,780,2.3f},{1040,990,1.9f},{1830,1380,1.5f}});trees(o,z,r,new float[][]{{470,300,1.6f},{670,460,1.4f},{1870,540,1.9f},{2110,710,1.5f},{1530,1660,1.7f},{810,1930,1.4f}});break;
   case 3:rocks(o,z,r,new float[][]{{640,460,2.9f},{780,750,2.7f},{480,1100,2.1f},{1600,870,3.0f},{1910,1100,2.2f},{1570,1480,1.6f}});trees(o,z,r,new float[][]{{1160,330,1.4f},{2070,430,1.7f},{620,1780,1.4f},{2150,1780,1.3f}});break;
   default:rocks(o,z,r,new float[][]{{790,900,1.7f},{1900,450,2.4f},{1770,730,1.8f}});trees(o,z,r,new float[][]{{800,350,1.7f},{510,1230,1.9f},{1940,1750,1.4f},{2180,1530,1.7f}});}}
  else{switch(z){
   case 1:rocks(o,z,r,new float[][]{{540,450,2.2f},{2140,860,1.5f}});trees(o,z,r,new float[][]{{710,650,1.5f},{470,1050,1.6f},{2100,480,1.5f},{1890,700,1.4f},{2070,1550,1.7f},{2190,1790,1.3f},{910,1840,1.5f}});break;
   case 2:rocks(o,z,r,new float[][]{{660,670,2.8f},{910,910,2.3f},{1140,1140,2.7f},{800,1320,1.4f}});trees(o,z,r,new float[][]{{1640,400,1.5f},{1960,540,1.9f},{2180,780,1.6f},{1430,1620,1.4f},{470,1840,1.5f}});break;
   case 3:rocks(o,z,r,new float[][]{{1860,480,2.3f},{2030,720,1.35f},{950,1760,1.3f}});trees(o,z,r,new float[][]{{540,380,1.8f},{810,480,1.6f},{990,720,1.8f},{770,1020,1.5f},{1210,980,1.5f},{1510,1320,1.8f},{1840,1610,1.6f},{2140,1840,1.4f}});break;
   default:rocks(o,z,r,new float[][]{{530,430,2.8f},{790,610,2.4f},{1170,400,2.2f},{1740,1180,2.9f},{1980,1440,2.0f},{1590,1680,2.2f}});trees(o,z,r,new float[][]{{1960,380,1.5f},{2100,650,1.4f},{520,1540,1.6f},{700,1730,1.4f}});}}
  // Small weathered debris stays near actual tide/reef margins instead of covering the map in a grid.
  if(z>0)for(int i=0;i<42;i++){float x=160+(i*431+(r?117:0)+z*97)%(int)(n-360);float y=shore(z,r,x)-45-(i%5)*35;add(o,z,r,i%4==0?"shells":i%4==1?"driftwood":i%4==2?"bones":"boulders",x,y,i%4==3?.5f:.7f,false,false);}
  o.sort(Comparator.comparingDouble(p->p.y));return Collections.unmodifiableList(o);
 }
 private CoastalHuntLayout(){}
}
