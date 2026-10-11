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
  public final String asset;public final float x,y;public final RectF footprint;
  Prop(String asset,float x,float y){this.asset=asset;this.x=x;this.y=y;float rx="reef".equals(asset)?38:15,ry="reef".equals(asset)?20:10;footprint=new RectF(x-rx,y-ry,x+rx,y+ry);}
 }
 private static final Map<String,List<Prop>> PROPS=new HashMap<>();
 private static final Map<String,List<RectF>> WATER=new HashMap<>();
 /** One sampled coastline owns both the visible water boundary and its static collision. */
 public static float shoreY(String id,float x){float h=height(id),phase=index(id)*.73f;return h*.73f+(float)Math.sin(x/370f+phase)*95f+(float)Math.sin(x/137f+phase*.5f)*24f;}
 public static List<RectF> water(String id){return WATER.computeIfAbsent(id,k->{List<RectF> out=new ArrayList<>();for(float x=64;x<width(k);x+=32){float y=Math.min(shoreY(k,x),shoreY(k,x+32));out.add(new RectF(x,y,Math.min(width(k),x+32),height(k)));}return Collections.unmodifiableList(out);});}
 public static List<Prop> props(String id){return PROPS.computeIfAbsent(id,k->{List<Prop> out=new ArrayList<>();int phase=index(k);for(int i=0;i<28;i++){float x=200+Math.floorMod(i*479+phase*157,(int)width(k)-420),y=200+Math.floorMod(i*271+phase*89,(int)(height(k)*.58f)-260);if(Math.hypot(x-384,y-864)<190||Math.hypot(x-192,y-944)<145||Math.hypot(x-(width(k)-192),y-512)<150)continue;out.add(new Prop(i%4==0?"reef":"palm",x,y));}if(phase==5){out.add(new Prop("reef",1792,880));out.add(new Prop("reef",1920,960));out.add(new Prop("reef",2016,864));}out.sort(Comparator.comparingDouble(p->p.y));return Collections.unmodifiableList(out);});}
 public static List<RectF> obstacles(String id){List<RectF> out=new ArrayList<>(water(id));for(Prop p:props(id))out.add(new RectF(p.footprint));return Collections.unmodifiableList(out);}
 private AbelCoastGeometry(){}
}
