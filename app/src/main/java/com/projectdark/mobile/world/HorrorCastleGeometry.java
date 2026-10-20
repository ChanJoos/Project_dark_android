package com.projectdark.mobile.world;
import android.graphics.RectF;
import java.util.*;
/** Original cells for recovered maps. Rooms12–15 use explicit screenshot-guided native-tile adaptations. */
public final class HorrorCastleGeometry {
 public static final int ROOM_COUNT=15;
 public static final String LOBBY="MAP_HORROR_LOBBY",FIRST="MAP_HORROR_CORRIDOR_1",SECOND="MAP_HORROR_CORRIDOR_2";
 public static boolean contains(String id){return LOBBY.equals(id)||FIRST.equals(id)||SECOND.equals(id)||room(id)>0;}
 public static int room(String id){if(id==null||!id.matches("MAP_HORROR_ROOM_(0[1-9]|1[0-5])"))return 0;return Integer.parseInt(id.substring(id.length()-2));}
 public static String roomId(int n){if(n<1||n>ROOM_COUNT)throw new IllegalArgumentException("room");return String.format(java.util.Locale.ROOT,"MAP_HORROR_ROOM_%02d",n);}
 public static HorrorCastleOriginalData.Grid original(String id){HorrorCastleOriginalData.Grid g=HorrorCastleOriginalData.get(id);return g!=null?g:HorrorCastleReferenceRooms.grid(id);}
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
 public static final class Door {public final float x,y,markerX,markerY;public final String destination,label;public final boolean visiblePortal;
  Door(float xx,float yy,String d,String l){this(xx,yy,xx,yy,d,l);}
  Door(float xx,float yy,float mx,float my,String d,String l){x=xx;y=yy;markerX=mx;markerY=my;destination=d;label=l;visiblePortal=!"방 입구".equals(l)&&!"로비로 돌아가기".equals(l);}
  Door(int u,int v,String d,String l){this(HorrorCastleGeometry.x(u,v),HorrorCastleGeometry.y(u,v),d,l);}
 }
 private static Door originalDoor(String id,int u,int v,String destination,String label){WorldMoveTargetController.TileCenter p=nearest(id,u,v);return new Door(p.x,p.y,destination,label);}
 private static final Map<String,List<Door>> DOORS=new HashMap<>();
 public static List<Door> doors(String id){return DOORS.computeIfAbsent(id,HorrorCastleGeometry::buildDoors);}
 private static List<Door> buildDoors(String id){List<Door> d=new ArrayList<>();HorrorCastleOriginalData.Grid g=original(id);
  if(LOBBY.equals(id)){
   // User's device capture: the carpet under the central arches is the west entrance.
   for(int u=22;u<=26;u++)d.add(originalDoor(id,u,10,FIRST,"1층 서쪽 복도"));
   // Keep the marker on the same reachable floor cell as its activation.
   d.add(originalDoor(id,46,3,FIRST,"1층 동쪽 복도"));
   d.add(originalDoor(id,18,1,SECOND,"2층 중앙 복도"));
   // User-marked five-cell opening between the exterior pillars (u21 and u27).
   for(int u=22;u<=26;u++)d.add(new Door(u,19,"MILLES","밀레스"));
  }else if(FIRST.equals(id)){
   for(int u=3;u<=6;u++)d.add(originalDoor(id,u,rows(id)-1,LOBBY,"로비로"));
   for(int v:new int[]{6,12,18,24,30})d.add(originalDoor(id,0,v,roomId(1),"방 입구"));
   for(int v:new int[]{6,12,18,24,30})d.add(originalDoor(id,9,v,roomId(1),"방 입구"));
  }else if(SECOND.equals(id)){
   for(int u=4;u<=7;u++)d.add(originalDoor(id,u,rows(id)-1,LOBBY,"로비로"));
   d.add(originalDoor(id,8,2,roomId(1),"방 입구"));
  }else if(room(id)>0){
   String next=room(id)==ROOM_COUNT?roomId(1):roomId(room(id)+1);
   if(g!=null)for(int v=0;v<g.height;v++)for(int u=0;u<g.width;u++)for(int side=1;side<=2;side++){
    int f=g.value(u,v,side);Door door=null;
    // Registered native door panels only. Cracked wall panels 11365/11366 are not doors.
    if(f==11082||f==11367)door=originalDoor(id,u,v,next,"다음 방");
    else if(f==11298)door=originalDoor(id,u,v,next,"다음 방");
    else if(f==11398||f==11162)door=originalDoor(id,u,v,next,"다음 방");
    if(door!=null&&!sameDoor(d,door))d.add(door);
   }
   // Kitchen has an open wall threshold, and 12/13 still use their explicit legacy fixture.
   if(d.isEmpty()){if(g!=null)d.add(originalDoor(id,Math.min(6,g.width-2),1,next,"다음 방"));else d.add(new Door(columns(id)/2,2,next,"다음 방"));}
  }
  if(room(id)>0){List<Door> returns=new ArrayList<>();
   // Return through the native doorway itself, never an adjacent +u/+v floor portal.
   for(Door old:d){Door threshold=new Door(old.x,old.y,old.x,old.y,LOBBY,"로비로 돌아가기");if(!sameDoor(returns,threshold))returns.add(threshold);
   }d=returns;
  }
  return Collections.unmodifiableList(d);
 }
 private static boolean sameDoor(List<Door> list,Door candidate){for(Door d:list)if(d.x==candidate.x&&d.y==candidate.y)return true;return false;}
 public static WorldMoveTargetController.TileCenter exitNpc(String id){WorldMoveTargetController.TileCenter e=entry(id),best=null;float score=Float.MAX_VALUE;
  for(WorldMoveTargetController.TileCenter t:tiles(id,true)){float dist=(float)Math.hypot(t.x-e.x,t.y-e.y);if(dist<60||dist>80)continue;boolean close=false;for(Door d:doors(id))if(Math.hypot(t.x-d.x,t.y-d.y)<64)close=true;if(close)continue;float q=(t.x-e.x-64)*(t.x-e.x-64)+(t.y-e.y-32)*(t.y-e.y-32);if(q<score){score=q;best=t;}}
  if(best==null)throw new IllegalStateException("No safe Horror exit NPC "+id);return best;
 }
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
