package com.projectdark.mobile.world;
import android.graphics.RectF;import java.util.*;
/** User-video materials; unseen joins and service assignments are explicitly ADAPTED. */
public final class MineTownGeometry {
 public static final String ID="MAP_MINE_TOWN";public static final int SIZE=64;
 public static final float SCALE=8f/7f;
 public static boolean contains(String id){return ID.equals(id);}
 public static float x(int u,int v){return 2048+(u-v)*32;}public static float y(int u,int v){return 64+(u+v)*16;}
 public static int tile(int u,int v){if(u<8||v<8||u>55||v>55)return 0;if(v>=28&&v<=36)return bridge(u,v)?2:0; if(u>=29&&u<=33&&v>=8&&v<=18)return 0; if(u>=29&&u<=33&&v>=46&&v<=55)return 0; return 1;}
 public static boolean bridge(int u,int v){return v>=27&&v<=37&&(u>=19&&u<=23||u>=43&&u<=47);}
 public static final class Prop {
  public final String asset;public final int u,v;public final float x,y,anchorX,anchorY,width,height,depthY;public final boolean blocking;
  Prop(String a,int u,int v,float ax,float ay,float w,float h,boolean block){asset=a;this.u=u;this.v=v;x=MineTownGeometry.x(u,v);y=MineTownGeometry.y(u,v);anchorX=ax*SCALE;anchorY=ay*SCALE;width=w*SCALE;height=h*SCALE;depthY=y+Math.max(0,height-anchorY);blocking=block;}
 }
 public static final class Building {public final String interior,title;public final int u,v;public final Prop prop;
  Building(String id,String title,int u,int v){interior=id;this.title=title;this.u=u;this.v=v;prop=new Prop("house",u,v,76,242,322,268,true);}
  public float doorX(){return x(u,v);}public float doorY(){return y(u,v);}public float returnX(){return x(u,v+2);}public float returnY(){return y(u,v+2);}
 }
 public static final List<Building> BUILDINGS=Collections.unmodifiableList(Arrays.asList(new Building("mine_interior_inn","마인 여관",42,20),new Building("mine_interior_reagent","마인 시약점",42,47)));
 public static Building forInterior(String id){for(Building b:BUILDINGS)if(b.interior.equals(id))return b;return null;}
 public static Building doorAt(float x,float y){for(Building b:BUILDINGS)if(Math.hypot(x-b.doorX(),y-b.doorY())<12)return b;return null;}
 public static final List<Prop> PROPS=props();
 private static List<Prop> props(){List<Prop> p=new ArrayList<>();for(Building b:BUILDINGS)p.add(b.prop);
  p.add(new Prop("house",20,18,76,242,322,268,true));p.add(new Prop("house",20,47,76,242,322,268,true));
  // Shore fronts carry native irregular rock groups; do not stamp a closed paving outline.
  for(int u=8;u<=55;u++)for(int v=8;v<=55;v++)if(tile(u,v)==1&&!bridge(u,v)){
   boolean shore=false;for(int[] d:new int[][]{{1,0},{-1,0},{0,1},{0,-1}})if(tile(u+d[0],v+d[1])==0)shore=true;
   if(shore){int n=Math.floorMod(u*17+v*31,3);boolean front=tile(u+1,v)==0||tile(u,v+1)==0; p.add(new Prop(front?"shore"+n:"rock"+n,u,v,22,front?34:n==2?43:38,front?(n==1?50:n==2?47:49):44,front?53:n==2?49:44,true));}
  }
  // Bridge rail collision is registered separately; the restored source bridge is drawn as a whole.
  for(int v=18;v<=25;v+=2)p.add(new Prop("cliff",10,v,90,130,175,141,true));
  p.add(new Prop("cave",13,23,110,285,398,317,true));
  p.add(new Prop("mine_hatch",14,23,50,50,115,63,true));
  for(int[] uv:new int[][]{{16,20},{12,26},{27,18},{34,17},{27,48},{35,48},{16,46},{47,15}})p.add(new Prop("tree",uv[0],uv[1],30,128,62,131,true));
  for(int[] uv:new int[][]{{17,25},{25,24},{36,24},{49,23},{17,40},{28,42},{37,51},{50,47},{24,51},{49,13},{32,14},{13,49},{15,12}})p.add(new Prop("lamp",uv[0],uv[1],13,116,61,118,true));
  for(int[] uv:new int[][]{{35,21},{36,46},{16,16},{24,46},{15,28}})p.add(new Prop("bench",uv[0],uv[1],25,40,51,44,true));
  int i=0;for(int[] uv:new int[][]{{16,25},{19,25},{23,25},{26,24},{30,24},{34,24},{38,23},{42,23},{46,24},{49,23},{21,40},{24,41},{27,42},{31,42},{35,43},{39,46},{42,50},{46,50},{49,48},{16,44},{16,48},{23,50},{36,17},{39,18},{17,17},{23,20},{16,27}}){int n=i++%3;p.add(new Prop("paving"+n,uv[0],uv[1],n==0?34:n==1?35:26,n==0?20:n==1?23:17,n==0?69:n==1?71:52,n==0?40:n==1?46:34,false));}
  i=0;for(int[] uv:new int[][]{{12,30},{16,34},{26,31},{30,34},{35,30},{39,33},{50,31},{53,35},{13,6},{24,6},{38,6},{47,5},{57,16},{58,25},{58,42},{55,57},{43,58},{35,57},{22,58},{13,57},{6,47},{6,38},{6,26},{6,14},{31,11},{31,52}}){int n=i++%3;if(tile(uv[0],uv[1])==0)p.add(new Prop("riverrock"+n,uv[0],uv[1],n==0?24:19,24,n==0?49:n==1?38:41,n==0?38:n==1?31:30,false));}
  return Collections.unmodifiableList(p);
 }
 private static boolean houseSolid(Prop p,int u,int v){return u>=p.u-2&&u<=p.u+3&&v>=p.v-5&&v<=p.v;}
 private static final boolean[][] SOLID=solids();
 private static boolean[][] solids(){boolean[][] b=new boolean[SIZE][SIZE];for(int u=0;u<SIZE;u++)for(int v=0;v<SIZE;v++)b[u][v]=tile(u,v)==0;
  for(Prop p:PROPS)if(p.blocking){if(p.asset.equals("house")){for(int u=p.u-2;u<=p.u+3;u++)for(int v=p.v-5;v<=p.v;v++)if(u>=0&&v>=0&&u<SIZE&&v<SIZE)b[u][v]=true;}else if(p.asset.equals("cliff")){for(int u=p.u-1;u<=p.u+1;u++)for(int v=p.v-1;v<=p.v+1;v++)b[u][v]=true;}else b[p.u][p.v]=true;}
  for(int u:new int[]{19,23,43,47})for(int v=27;v<=37;v++)b[u][v]=true;
  for(int u=8;u<=12;u++)for(int v=16;v<=25;v++)b[u][v]=true;
  // Only doorway thresholds and two exterior approach tiles are open; no wall tunnels.
  for(Building a:BUILDINGS)for(int v=a.v;v<=a.v+2;v++)b[a.u][v]=false;
  return b;
 }
 public static boolean blocked(int u,int v){return u<0||v<0||u>=SIZE||v>=SIZE||SOLID[u][v];}
 private static final List<WorldMoveTargetController.TileCenter> NAV=buildNav();private static List<WorldMoveTargetController.TileCenter> buildNav(){List<WorldMoveTargetController.TileCenter>a=new ArrayList<>();for(int u=0;u<SIZE;u++)for(int v=0;v<SIZE;v++)if(!blocked(u,v))a.add(new WorldMoveTargetController.TileCenter(x(u,v),y(u,v)));return Collections.unmodifiableList(a);}
 public static List<WorldMoveTargetController.TileCenter> navigation(){return NAV;}
 private static final List<RectF> WALLS=walls();private static List<RectF> walls(){List<RectF>a=new ArrayList<>();for(int u=0;u<SIZE;u++)for(int v=0;v<SIZE;v++)if(blocked(u,v))a.add(new RectF(x(u,v)-12,y(u,v)-6,x(u,v)+12,y(u,v)+6));return Collections.unmodifiableList(a);}
 public static List<RectF> obstacles(){return WALLS;}
 private MineTownGeometry(){}
}
