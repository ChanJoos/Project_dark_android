package com.projectdark.mobile.world;
import android.graphics.RectF;import java.util.*;
/** User-authored V158 square coast layout; stable IDs retain legacy compatibility. */
public final class RucesionCoastGeometry {
 public static final String ENTRY="MAP_RUCESION_COAST",BOSS="MAP_RUCESION_ARIEL_ROOM";public static final String[] ROUTE={"MAP_RUCESION_COAST","MAP_RUCESION_COAST_1C","MAP_RUCESION_COAST_2C","MAP_RUCESION_COAST_3B","MAP_RUCESION_ARIEL_ROOM"};
 private static final String[] NAMES={"뤼케시온 해안 입구","뤼케시온 해안 1","뤼케시온 해안 2","뤼케시온 해안 3","뤼케시온 해안 보스방"};
 public static String canonical(String id){return id;}public static boolean contains(String id){return Arrays.asList(ROUTE).contains(canonical(id));}public static int index(String id){return Arrays.asList(ROUTE).indexOf(canonical(id));}public static String title(String id){return NAMES[index(id)];}
 public static float width(String id){return CoastalHuntLayout.size(index(id));}public static float height(String id){return width(id);}public static float shoreY(String id,float x){return CoastalHuntLayout.shore(index(id),true,x);}
 public static final class Prop {public final String asset;public final float x,y,scale;public final boolean solid,canopy;public final RectF footprint;Prop(CoastalHuntLayout.Prop p){asset=p.asset;x=p.x;y=p.y;scale=p.scale;solid=p.solid;canopy=p.canopy;footprint=new RectF(p.footprint);}}
 private static final Map<String,List<Prop>> PROPS=new HashMap<>();private static final Map<String,List<RectF>> BLOCKERS=new HashMap<>();
 public static List<Prop> props(String map){return PROPS.computeIfAbsent(canonical(map),k->{List<Prop> out=new ArrayList<>();for(CoastalHuntLayout.Prop p:CoastalHuntLayout.props(index(k),true))out.add(new Prop(p));return Collections.unmodifiableList(out);});}
 public static List<RectF> water(String map){return CoastalHuntLayout.water(index(map),true);}
 public static List<RectF> obstacles(String map){return BLOCKERS.computeIfAbsent(canonical(map),k->{List<RectF> out=new ArrayList<>(water(k));for(Prop p:props(k))if(p.solid)out.add(new RectF(p.footprint));return Collections.unmodifiableList(out);});}
 private RucesionCoastGeometry(){}
}
