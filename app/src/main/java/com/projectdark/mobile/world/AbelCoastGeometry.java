package com.projectdark.mobile.world;
import android.graphics.RectF;import java.util.*;
/** User-authored V158 square coast layout; stable IDs retain legacy compatibility. */
public final class AbelCoastGeometry {
 public static final String ENTRY="MAP_ABEL_COAST",BOSS="MAP_ABEL_COAST_4C";public static final String[] ROUTE={"MAP_ABEL_COAST","MAP_ABEL_COAST_1A","MAP_ABEL_COAST_2A","MAP_ABEL_COAST_3B","MAP_ABEL_COAST_4C"};
 private static final String[] NAMES={"아벨 해안 입구","아벨 해안 1","아벨 해안 2","아벨 해안 3","아벨 해안 보스방"};
 public static String canonical(String id){return "MAP_ABEL_COAST_1B".equals(id)?"MAP_ABEL_COAST_1A":id;}public static boolean contains(String id){return Arrays.asList(ROUTE).contains(canonical(id));}public static int index(String id){return Arrays.asList(ROUTE).indexOf(canonical(id));}public static String title(String id){return NAMES[index(id)];}
 public static float width(String id){return CoastalHuntLayout.size(index(id));}public static float height(String id){return width(id);}public static float shoreY(String id,float x){return CoastalHuntLayout.shore(index(id),false,x);}
 public static final class Prop {public final String asset;public final float x,y,scale;public final boolean solid,canopy;public final RectF footprint;Prop(CoastalHuntLayout.Prop p){asset=p.asset;x=p.x;y=p.y;scale=p.scale;solid=p.solid;canopy=p.canopy;footprint=new RectF(p.footprint);}}
 private static final Map<String,List<Prop>> PROPS=new HashMap<>();private static final Map<String,List<RectF>> BLOCKERS=new HashMap<>();
 public static List<Prop> props(String map){return PROPS.computeIfAbsent(canonical(map),k->{List<Prop> out=new ArrayList<>();for(CoastalHuntLayout.Prop p:CoastalHuntLayout.props(index(k),false))out.add(new Prop(p));return Collections.unmodifiableList(out);});}
 public static List<RectF> water(String map){List<RectF> out=new ArrayList<>();for(float x=64;x<width(map);x+=32)out.add(new RectF(x,Math.min(shoreY(map,x),shoreY(map,x+32)),Math.min(width(map),x+32),height(map)));return Collections.unmodifiableList(out);}
 public static List<RectF> obstacles(String map){return BLOCKERS.computeIfAbsent(canonical(map),k->{List<RectF> out=new ArrayList<>(water(k));for(Prop p:props(k))if(p.solid)out.add(new RectF(p.footprint));return Collections.unmodifiableList(out);});}
 private AbelCoastGeometry(){}
}
