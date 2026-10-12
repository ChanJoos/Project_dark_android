package com.projectdark.mobile.world;
import android.graphics.RectF;
import java.util.*;
/** Registered source-visible geometry. Service names and unobserved joins are adaptations. */
public final class AbelTownGeometry {
 public static final String ID="MAP_ABEL_TOWN",EAST_ID="MAP_ABEL_EAST_TOWN";
 public static final int SIZE=112;public static final int EAST_U=28,EAST_V=4;public static final float EAST_DX=768,EAST_DY=512;
 public static String canonical(String id){return EAST_ID.equals(id)?ID:id;}
 public static float x(int u,int v){return 2560+(u-v)*32;}
 public static float y(int u,int v){return 64+(u+v)*16;}
 public static boolean contains(String id){return ID.equals(id)||EAST_ID.equals(id);}
 public static final class Prop {
  public final String asset;public final float x,y,width,height,anchorX,anchorY,depthY;public final int u,v;public final boolean mirror=false;
  Prop(String a,float sx,float sy,float ax,float ay,float w,float h){asset=a;x=2560+sx*AbelVideoLayout.SCALE;y=64+sy*AbelVideoLayout.SCALE;anchorX=ax*AbelVideoLayout.SCALE;anchorY=ay*AbelVideoLayout.SCALE;width=w*AbelVideoLayout.SCALE;height=h*AbelVideoLayout.SCALE;u=Math.round((sx/28+sy/14)/2);v=Math.round((sy/14-sx/28)/2);depthY=y+Math.max(0,height-anchorY);}
 }
 public static final List<Prop> PROPS=AbelUnifiedLayout.props(),EAST_PROPS=PROPS;
 public static List<Prop> props(String id){return PROPS;}
 public static int tile(String id,int u,int v){if(u<0||v<0||u>=SIZE||v>=SIZE)return 0;return AbelUnifiedLayout.TILES[u].charAt(v)-'0';}
 public static final class Building {
  public final String id,asset,title,interior;public final int u,v,du=4,dv=4;public final float width;public final Prop prop;
  Building(String id,String interior,Prop p){this.id=id;this.interior=interior;asset=p.asset;title="아벨 건물";u=p.u-1;v=p.v-3;prop=p;width=p.width;}
  public float x(){return prop.x;}public float y(){return prop.y;}public float doorX(){return AbelTownGeometry.x(prop.u,prop.v);}public float doorY(){return AbelTownGeometry.y(prop.u,prop.v);}
  boolean occupies(int a,int b){return a>=u&&a<u+du&&b>=v&&b<v+dv&&!(a==prop.u&&b>=prop.v-1);}
 }
 public static final List<Building> BUILDINGS;
 private static Prop facade(String asset,float sx,float sy){Prop best=null;float dist=Float.MAX_VALUE;for(Prop p:PROPS)if(p.asset.equals(asset)){float x=(p.x-2560)/AbelVideoLayout.SCALE,y=(p.y-64)/AbelVideoLayout.SCALE,d=(x-sx)*(x-sx)+(y-sy)*(y-sy);if(d<dist){dist=d;best=p;}}if(best==null||dist>36)throw new IllegalStateException("Missing registered service facade "+asset);return best;}
 static {BUILDINGS=Collections.unmodifiableList(Arrays.asList(
 new Building("abel_church","abel_interior_church",facade("building_two_storey",181.48f,314.74f)),
 new Building("abel_reagent","abel_interior_reagent",facade("building_awning_corner",-74.51f,404.72f)),
 new Building("abel_bank","abel_interior_bank",facade("building_two_storey",461.5f,454.75f)),
 new Building("abel_equipment","abel_interior_equipment",facade("building_awning_long",-547.51f,1030.57f)),
 new Building("abel_inn","abel_interior_inn",facade("building_awning_corner",1353f,1007f))));}

 public static Building forInterior(String id){for(Building b:BUILDINGS)if(Objects.equals(id,b.interior))return b;return null;}
 public static Building doorAt(float px,float py){for(Building b:BUILDINGS)if(Math.hypot(px-b.doorX(),py-b.doorY())<12)return b;return null;}
 public static boolean pool(int u,int v){return tile(EAST_ID,u,v)==2;}
 public static boolean channel(int u,int v){return tile(ID,u,v)==2||tile(ID,u,v)==4;}
 public static boolean garden(int u,int v){return tile(ID,u,v)==5;}
 public static boolean road(int u,int v){return tile(ID,u,v)==1;}
 public static boolean waterCell(int u,int v){return pool(u,v)||channel(u,v);}
 public static boolean benchCell(int u,int v){for(Prop p:PROPS)if(p.asset.startsWith("bench_")&&p.u==u&&p.v==v)return true;return false;}
 public static boolean blocked(int u,int v){return blocked(ID,u,v);}
 public static boolean blocked(String id,int u,int v){if(u<1||v<1||u>=SIZE-1||v>=SIZE-1||AbelUnifiedLayout.ACTIVE[u].charAt(v)!='1')return true;for(Building b:BUILDINGS)if(u==b.prop.u&&v>=b.prop.v-1&&v<=b.prop.v+2)return false;int kind=tile(id,u,v);if(kind==2||kind==4||kind==5)return true;
  for(Building b:BUILDINGS)if(b.occupies(u,v))return true;
  for(Prop p:props(id))if(!p.asset.startsWith("building_")&&(p.asset.startsWith("tree_")||p.asset.startsWith("bench_")||p.asset.startsWith("lamp"))&&p.u==u&&p.v==v)return true;
  return false;
 }
 public static List<RectF> obstacles(){return obstacles(ID);}
 public static List<RectF> obstacles(String id){List<RectF> a=new ArrayList<>();for(int u=0;u<SIZE;u++)for(int v=0;v<SIZE;v++)if(blocked(id,u,v))a.add(new RectF(x(u,v)-3,y(u,v)-3,x(u,v)+3,y(u,v)+3));return Collections.unmodifiableList(a);}
 public static List<WorldMoveTargetController.TileCenter> navigation(){return navigation(ID);}
 public static List<WorldMoveTargetController.TileCenter> navigation(String id){List<WorldMoveTargetController.TileCenter> a=new ArrayList<>();for(int u=1;u<SIZE-1;u++)for(int v=1;v<SIZE-1;v++)if(!blocked(id,u,v))a.add(new WorldMoveTargetController.TileCenter(x(u,v),y(u,v)));return Collections.unmodifiableList(a);}
 private AbelTownGeometry(){}
}
