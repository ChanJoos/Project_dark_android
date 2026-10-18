package com.projectdark.mobile.world;
import android.graphics.RectF;
import java.util.*;
/** Original cells for recovered maps. Legacy V179 rooms 12/13 remain unverified, never a fidelity claim. */
public final class HorrorCastleGeometry {
 public static final String LOBBY="MAP_HORROR_LOBBY",FIRST="MAP_HORROR_CORRIDOR_1",SECOND="MAP_HORROR_CORRIDOR_2";
 public static boolean contains(String id){return LOBBY.equals(id)||FIRST.equals(id)||SECOND.equals(id)||room(id)>0;}
 public static int room(String id){if(id==null||!id.matches("MAP_HORROR_ROOM_(0[1-9]|1[0-3])"))return 0;return Integer.parseInt(id.substring(id.length()-2));}
 public static String roomId(int n){if(n<1||n>13)throw new IllegalArgumentException("room");return String.format(java.util.Locale.ROOT,"MAP_HORROR_ROOM_%02d",n);}
 public static HorrorCastleOriginalData.Grid original(String id){return HorrorCastleOriginalData.get(id);}
 public static int columns(String id){HorrorCastleOriginalData.Grid g=original(id);if(g!=null)return g.width;return LOBBY.equals(id)?24:room(id)>0?18:30;}
 public static int rows(String id){HorrorCastleOriginalData.Grid g=original(id);if(g!=null)return g.height;return LOBBY.equals(id)?18:room(id)>0?16:7;}
 public static float x(int u,int v){return 800+(u-v)*32;}
 public static float y(int u,int v){return 160+(u+v)*16;}
 public static WorldMoveTargetController.TileCenter at(int u,int v){return new WorldMoveTargetController.TileCenter(x(u,v),y(u,v));}
 public static WorldMoveTargetController.TileCenter entry(String id){if(original(id)!=null)return nearest(id,LOBBY.equals(id)?24:room(id)>0?columns(id)/2:columns(id)/2,LOBBY.equals(id)?17:room(id)>0?3:rows(id)-3);return at(2,rows(id)-3);}
 private static WorldMoveTargetController.TileCenter nearest(String id,int u,int v){HorrorCastleOriginalData.Grid g=original(id);int bu=-1,bv=-1,best=Integer.MAX_VALUE;for(int yy=0;yy<g.height;yy++)for(int xx=0;xx<g.width;xx++)if(g.walkable(xx,yy)){int d=(xx-u)*(xx-u)+(yy-v)*(yy-v);if(d<best){best=d;bu=xx;bv=yy;}}if(bu<0)throw new IllegalStateException("No original walkable cells "+id);return at(bu,bv);}
 public static float minX(String id){return original(id)==null?128:800-(rows(id)-1)*32-64;}
 public static float maxX(String id){return original(id)==null?1856:800+(columns(id)-1)*32+64;}
 public static float maxY(String id){return original(id)==null?960:160+(columns(id)+rows(id))*16+64;}
 public static String title(String id){return LOBBY.equals(id)?"호러캐슬 로비":FIRST.equals(id)?"호러캐슬 1층 복도":SECOND.equals(id)?"호러캐슬 2층 복도":String.format(java.util.Locale.ROOT,"호러캐슬 %02d",room(id));}
 public static boolean inside(String id,float x,float y,float radius){float a=(y-160)/32+(x-800)/64,b=(y-160)/32-(x-800)/64;HorrorCastleOriginalData.Grid g=original(id);if(g!=null)return g.walkable(Math.round(a),Math.round(b));float inset=radius/28f;return a>=inset&&b>=inset&&a<=columns(id)-1-inset&&b<=rows(id)-1-inset;}
 public static final class Door {public final float x,y;public final String destination,label;Door(float xx,float yy,String d,String l){x=xx;y=yy;destination=d;label=l;}Door(int u,int v,String d,String l){x=HorrorCastleGeometry.x(u,v);y=HorrorCastleGeometry.y(u,v);destination=d;label=l;}}
 private static Door originalDoor(String id,int u,int v,String destination,String label){WorldMoveTargetController.TileCenter p=nearest(id,u,v);return new Door(p.x,p.y,destination,label);}
 public static List<Door> doors(String id){if(original(id)!=null){List<Door> d=new ArrayList<>();if(LOBBY.equals(id)){d.add(originalDoor(id,24,18,"MILLES","밀레스"));d.add(originalDoor(id,46,2,FIRST,"1층 복도"));d.add(originalDoor(id,18,2,SECOND,"2층 복도"));}else if(room(id)>0){d.add(originalDoor(id,columns(id)/2,1,LOBBY,"로비로"));}else{d.add(originalDoor(id,columns(id)/2-1,rows(id)-2,LOBBY,"로비로"));if(FIRST.equals(id)){for(int v:new int[]{7,13,19,25,31})d.add(originalDoor(id,1,v,roomId(1),"방 입구"));}else d.add(originalDoor(id,8,2,roomId(1),"방 입구"));}return Collections.unmodifiableList(d);}List<Door> out=new ArrayList<>();out.add(new Door(1,rows(id)-3,LOBBY.equals(id)?"MILLES":LOBBY,LOBBY.equals(id)?"밀레스":"로비로"));if(LOBBY.equals(id)){out.add(new Door(7,1,FIRST,"1층 복도"));out.add(new Door(19,1,SECOND,"2층 복도"));}else if(room(id)==0){for(int i=0;i<6;i++)out.add(new Door(4+i*4,1,roomId(1),"방 입구 "+(i+1)));}return Collections.unmodifiableList(out);}
 public static final class Prop {public final String art;public final int u,v;public final float width;public Prop(String a,int u,int v,float w){art=a;this.u=u;this.v=v;width=w;}public float x(){return HorrorCastleGeometry.x(u,v);}public float y(){return HorrorCastleGeometry.y(u,v);}}
 public static List<Prop> props(String id){if(original(id)!=null)return Collections.emptyList();List<Prop> p=new ArrayList<>();if(LOBBY.equals(id)){for(int u=3;u<23;u+=5)p.add(new Prop("pillar",u,2,42));p.add(new Prop("stairs",11,3,330));return p;}if(room(id)==0){for(int u=2;u<30;u+=4)p.add(new Prop("pillar",u,0,32));return p;}
  int theme=(room(id)-1)%5;
  if(theme==0){p.add(new Prop("table",9,8,135));p.add(new Prop("sofa",6,7,88));p.add(new Prop("sofa",11,10,88));p.add(new Prop("tiger",13,5,100));p.add(new Prop("chair",5,4,38));}
  if(theme==1){for(int u=2;u<17;u+=3)p.add(new Prop("bookcase",u,0,88));p.add(new Prop("table",10,6,130));p.add(new Prop("chair",7,5,40));}
  if(theme==2){p.add(new Prop("table",8,7,150));p.add(new Prop("table",12,10,150));for(int u=4;u<15;u+=4)p.add(new Prop("chair",u,4,40));}
  if(theme==3){p.add(new Prop("tiger",12,7,130));for(int u=3;u<17;u+=4)p.add(new Prop("sofa",u,1,86));p.add(new Prop("chair",7,8,38));}
  if(theme==4){for(int u=3;u<17;u+=4)p.add(new Prop("bookcase",u,0,84));p.add(new Prop("table",10,9,110));p.add(new Prop("chair",8,7,40));}
  return p;
 }
 public static List<RectF> obstacles(String id){List<RectF> out=new ArrayList<>();HorrorCastleOriginalData.Grid g=original(id);if(g!=null){for(int v=0;v<g.height;v++)for(int u=0;u<g.width;u++)if(!g.walkable(u,v))out.add(new RectF(x(u,v)-13,y(u,v)-6,x(u,v)+13,y(u,v)+6));return Collections.unmodifiableList(out);}for(Prop p:props(id)){float w=p.art.equals("stairs")?135:p.art.equals("table")?42:p.art.equals("bookcase")?22:p.art.equals("pillar")?10:18;float h=p.art.equals("stairs")?32:p.art.equals("table")?22:10;out.add(new RectF(p.x()-w,p.y()-h,p.x()+w,p.y()+h));}return Collections.unmodifiableList(out);}
 public static List<WorldMoveTargetController.TileCenter> tiles(String id,boolean nav){List<WorldMoveTargetController.TileCenter> out=new ArrayList<>();HorrorCastleOriginalData.Grid g=original(id);if(g!=null){for(int v=0;v<g.height;v++)for(int u=0;u<g.width;u++)if(!nav||g.walkable(u,v))out.add(at(u,v));return Collections.unmodifiableList(out);}List<RectF> obstacles=obstacles(id);for(int u=nav?1:0;u<columns(id)-(nav?1:0);u++)for(int v=nav?1:0;v<rows(id)-(nav?1:0);v++){WorldMoveTargetController.TileCenter t=at(u,v);boolean blocked=false;if(nav)for(RectF r:obstacles)if(t.x>=r.left-12&&t.x<=r.right+12&&t.y>=r.top-12&&t.y<=r.bottom+12){blocked=true;break;}if(!blocked)out.add(t);}return Collections.unmodifiableList(out);}
 private HorrorCastleGeometry(){}
}
