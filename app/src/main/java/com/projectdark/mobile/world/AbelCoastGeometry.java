package com.projectdark.mobile.world;

import android.graphics.RectF;
import java.util.*;

/** Source-referenced coast palette; map dimensions, terrain and encounters are ADAPTED. */
public final class AbelCoastGeometry {
 public static final String ENTRY="MAP_ABEL_COAST";
 public static final String[] ROUTE={ENTRY,"MAP_ABEL_COAST_1A","MAP_ABEL_COAST_1B","MAP_ABEL_COAST_2A","MAP_ABEL_COAST_3B","MAP_ABEL_COAST_4C"};
 private static final String[] TITLES={"아벨해안 · 조사 거점","아벨해안 1-A · 모래 해변","아벨해안 1-B · 암초 지대","아벨해안 2-A · 야자수 해안","아벨해안 3-B · 숨겨진 해변","아벨해안 4-C · 바위 암초"};
 public static boolean contains(String id){return Arrays.asList(ROUTE).contains(id);}
 public static int index(String id){return Arrays.asList(ROUTE).indexOf(id);}
 public static String title(String id){return TITLES[index(id)];}
 public static float width(String id){return ENTRY.equals(id)?2304:4096;}
 public static float height(String id){return ENTRY.equals(id)?1536:2048;}
 public static final class Prop {
  public final String asset;public final float x,y;public final RectF footprint;public final boolean solid,canopy;public final float scale;
  Prop(String asset,float x,float y,boolean solid,boolean canopy,float scale){this.asset=asset;this.x=x;this.y=y;this.solid=solid;this.canopy=canopy;this.scale=scale;float rx=asset.startsWith("reef")?48:asset.equals("barrels")?24:14,ry=asset.startsWith("reef")?24:10;footprint=new RectF(x-rx,y-ry,x+rx,y+ry);}
 }
 private static final Map<String,List<Prop>> PROPS=new HashMap<>();
 private static final Map<String,List<RectF>> WATER=new HashMap<>();
 /** One sampled coastline owns both the visible water boundary and its static collision. */
 public static float shoreY(String id,float x){float phase=index(id)*.57f;return Math.max(995f,1010f+(float)Math.sin((x-384)/430f+phase)*76f+(float)Math.sin(x/173f+phase)*20f);}
 public static List<RectF> water(String id){return WATER.computeIfAbsent(id,k->{List<RectF> out=new ArrayList<>();for(float x=64;x<width(k);x+=32){float y=Math.min(shoreY(k,x),shoreY(k,x+32));out.add(new RectF(x,y,Math.min(width(k),x+32),height(k)));}return Collections.unmodifiableList(out);});}
 /** Groves shelter the inland edge; reefs mark coves; debris follows the high-tide line. */
 public static List<Prop> props(String id){return PROPS.computeIfAbsent(id,k->{List<Prop> out=new ArrayList<>();int phase=index(k);
  int groves=new int[]{6,7,5,12,10,4}[phase],reefs=new int[]{5,4,10,5,5,12}[phase];
  for(int g=0;g<groves;g++){float cx=260+g*(width(k)-520)/(groves-1f),cy=740+(g%3)*60+(phase%3)*16;
   for(int j=0;j<7;j++){float angle=j*2.4f+g,r=35+(j%3)*44,x=cx+(float)Math.cos(angle)*r*1.5f,y=cy+(float)Math.sin(angle)*r;
    String a=j==0?"palm_curve":j==1?"palm_fan":j==2?"banana":"fern";add(out,k,a,x,y,j<3,j<3,1f);}
  }
  for(int g=0;g<reefs;g++){float x=620+g*(width(k)-900)/(reefs-1f),y=shoreY(k,x)-80-(g%2)*70;
   add(out,k,g%2==0?"reef_low":"reef_tall",x,y,true,false,1f);
   add(out,k,"boulders",x-66,y+22,true,false,.8f);add(out,k,"fern",x+56,y-12,false,false,.8f);
  }
  for(int j=0;j<38;j++){float x=250+j*(width(k)-520)/37f,y=shoreY(k,x)-40-(j%4)*22;String a=j%5==0?"driftwood":j%3==0?"bones":"shells";add(out,k,a,x,y,false,false,1f);}
  add(out,k,"barrels",640,840,true,false,1f);add(out,k,"banana",700,850,true,true,1f);add(out,k,"fern",675,880,false,false,1f);
  out.sort(Comparator.comparingDouble(p->p.y));return Collections.unmodifiableList(out);});}
 private static void add(List<Prop> out,String map,String asset,float x,float y,boolean solid,boolean canopy,float scale){
  if(x<96||x>width(map)-96||y<120||y>shoreY(map,x)-30)return;
  if(solid&&(Math.hypot(x-384,y-864)<180||Math.hypot(x-192,y-944)<160||Math.hypot(x-(width(map)-192),y-512)<170||Math.hypot(x-480,y-816)<100))return;
  out.add(new Prop(asset,x,y,solid,canopy,scale));
 }
 public static List<RectF> obstacles(String id){List<RectF> out=new ArrayList<>(water(id));for(Prop p:props(id))if(p.solid)out.add(new RectF(p.footprint));return Collections.unmodifiableList(out);}
 private AbelCoastGeometry(){}
}
