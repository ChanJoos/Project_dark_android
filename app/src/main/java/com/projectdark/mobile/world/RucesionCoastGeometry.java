package com.projectdark.mobile.world;
import android.graphics.RectF;import java.util.*;
/** Observed video scenery; unseen dimensions and joins remain ADAPTED. */
public final class RucesionCoastGeometry {
 public static final String ENTRY="MAP_RUCESION_COAST",BOSS="MAP_RUCESION_ARIEL_ROOM";
 public static final String[] ROUTE={ENTRY,"MAP_RUCESION_COAST_1C","MAP_RUCESION_COAST_2C","MAP_RUCESION_COAST_3B",BOSS};
 private static final String[] NAMES={"뤼케시온해안 · 입구","뤼케시온해안 1-C","뤼케시온해안 2-C","뤼케시온해안 3-B","뤼케시온해안 · 아리엘 미리보기"};
 public static boolean contains(String id){return Arrays.asList(ROUTE).contains(id);}public static int index(String id){return Arrays.asList(ROUTE).indexOf(id);}public static String title(String id){return NAMES[index(id)];}
 public static float width(String id){return index(id)==0||index(id)==4?2304:4096;}public static float height(String id){return index(id)==4?1536:2432;}
 public static float shoreY(String id,float x){return index(id)==0||index(id)==4?1120:1910+(float)Math.sin(x/380+index(id)*.7)*90+(float)Math.sin(x/137)*18;}
 public static final class Prop {
  public final String asset;public final float x,y,scale;public final boolean solid,canopy;public final RectF footprint;
  Prop(String a,float x,float y,float scale,boolean solid,boolean canopy){asset=a;this.x=x;this.y=y;this.scale=scale;this.solid=solid;this.canopy=canopy;float rx=a.startsWith("reef")?68*scale:a.equals("boulders")?28*scale:16,ry=a.startsWith("reef")?30*scale:a.equals("boulders")?16*scale:10;footprint=new RectF(x-rx,y-ry,x+rx,y+ry);}
 }
 private static final Map<String,List<Prop>> PROPS=new HashMap<>();private static final Map<String,List<RectF>> BLOCKERS=new HashMap<>();
 private static void add(List<Prop> out,String map,String a,float x,float y,float scale,boolean solid,boolean canopy){if(x<100||x>width(map)-100||y<160||y>shoreY(map,x)-80)return;if(solid&&(Math.hypot(x-384,y-864)<200||Math.hypot(x-192,y-944)<180||Math.hypot(x-(width(map)-192),y-512)<180))return;out.add(new Prop(a,x,y,scale,solid,canopy));}
 public static List<Prop> props(String map){return PROPS.computeIfAbsent(map,k->{List<Prop> out=new ArrayList<>();int z=index(k);
  if(z==4){for(float[] p:new float[][]{{650,600},{840,890},{1730,620},{1870,920},{1500,1070}})add(out,k,"palm_curve",p[0],p[1],1.25f,true,true);}
  else{int groups=z==3?7:z==1?4:2;for(int g=0;g<groups;g++){float x=900+g*(width(k)-1700)/Math.max(1,groups-1),y=460+g%3*440;add(out,k,"reef_tall",x,y,z==3?1.9f:1.4f,true,false);add(out,k,"reef_low",x-100,y+70,1.35f,true,false);add(out,k,"reef_low",x+112,y+82,1.35f,true,false);add(out,k,"boulders",x+185,y+120,1,true,false);if(z!=3){add(out,k,"banana",x-165,y+70,1.3f,true,true);add(out,k,"palm_fan",x+140,y-70,1.1f,true,true);}add(out,k,"fern",x-80,y+115,.8f,false,false);add(out,k,"shells",x+190,y+170,.9f,false,false);}
   int palms=z==3?5:z==1?12:9;for(int i=0;i<palms;i++){float x=610+i*(width(k)-1100)/Math.max(1,palms-1),y=shoreY(k,x)-180-i%3*90;add(out,k,"palm_curve",x,y,1.35f,true,true);if(i%3==0)add(out,k,"palm_fan",x+110,y-90,1.1f,true,true);}}
  for(int i=0;i<64;i++){float x=520+i*(width(k)-900)/63f,y=260+i*113%1450;add(out,k,i%4==0?"bones":i%4==1?"driftwood":i%4==2?"shells":"boulders",x,y,i%4==3?.45f:.8f,false,false);}out.sort(Comparator.comparingDouble(p->p.y));return Collections.unmodifiableList(out);});}
 public static List<RectF> obstacles(String map){return BLOCKERS.computeIfAbsent(map,k->{List<RectF> out=new ArrayList<>();for(float x=64;x<width(k);x+=32)out.add(new RectF(x,Math.min(shoreY(k,x),shoreY(k,x+32)),Math.min(width(k),x+32),height(k)));for(Prop p:props(k))if(p.solid)out.add(new RectF(p.footprint));return Collections.unmodifiableList(out);});}
 private RucesionCoastGeometry(){}
}
