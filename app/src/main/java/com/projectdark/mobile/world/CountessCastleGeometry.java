package com.projectdark.mobile.world;
import android.graphics.RectF;
import java.util.*;
/** Native map layers; mobile connections and safe arrival cells are explicitly ADAPTED. */
public final class CountessCastleGeometry {
 public static final String EXTERIOR="MAP_COUNTESS_EXTERIOR",LOBBY="MAP_COUNTESS_LOBBY",UPPER="MAP_COUNTESS_UPPER",BOSS="MAP_COUNTESS_BOSS";
 public static boolean contains(String id){return CountessCastleData.grid(id)!=null;}
 public static int zone(String id){if(id==null||!id.matches("MAP_COUNTESS_ZONE_(0[1-9]|1[0-2])"))return 0;return Integer.parseInt(id.substring(id.length()-2));}
 public static String zoneId(int n){if(n<1||n>12)throw new IllegalArgumentException("zone");return String.format(Locale.ROOT,"MAP_COUNTESS_ZONE_%02d",n);}
 public static String title(String id){return EXTERIOR.equals(id)?"백작부인의별장 입구":LOBBY.equals(id)?"백작부인의별장 응접실1":UPPER.equals(id)?"백작부인의별장 응접실2":BOSS.equals(id)?"블러드백작의 결계":"[유리드] 백작부인별장 "+zone(id)+"존";}
 public static float x(int u,int v){return 2000+(u-v)*32;}public static float y(int u,int v){return 160+(u+v)*16;}
 public static int u(float x,float y){return Math.round((y-160)/32+(x-2000)/64);}public static int v(float x,float y){return Math.round((y-160)/32-(x-2000)/64);}
 public static WorldMoveTargetController.TileCenter at(int u,int v){return new WorldMoveTargetController.TileCenter(x(u,v),y(u,v));}
 private static final Map<String,List<WorldMoveTargetController.TileCenter>> NAV=new HashMap<>(),GROUND=new HashMap<>();private static final Map<String,List<RectF>> BLOCKS=new HashMap<>();
 public static List<WorldMoveTargetController.TileCenter> tiles(String id,boolean nav){Map<String,List<WorldMoveTargetController.TileCenter>> cache=nav?NAV:GROUND;List<WorldMoveTargetController.TileCenter> found=cache.get(id);if(found!=null)return found;HorrorCastleOriginalData.Grid g=CountessCastleData.grid(id);List<WorldMoveTargetController.TileCenter> out=new ArrayList<>();
  if(nav){Set<Integer> seen=new HashSet<>();List<Integer> largest=new ArrayList<>();for(int v=0;v<g.height;v++)for(int u=0;u<g.width;u++){int key=v*g.width+u;if(!g.walkable(u,v)||seen.contains(key))continue;List<Integer> part=new ArrayList<>();ArrayDeque<Integer> q=new ArrayDeque<>();q.add(key);seen.add(key);while(!q.isEmpty()){int k=q.remove();part.add(k);int xx=k%g.width,yy=k/g.width;for(int[] d:new int[][]{{1,0},{-1,0},{0,1},{0,-1}}){int nx=xx+d[0],ny=yy+d[1],nk=ny*g.width+nx;if(g.walkable(nx,ny)&&seen.add(nk))q.add(nk);}}if(part.size()>largest.size())largest=part;}Collections.sort(largest);for(int k:largest)out.add(at(k%g.width,k/g.width));}
  else for(int v=0;v<g.height;v++)for(int u=0;u<g.width;u++)if(g.value(u,v,0)>0)out.add(at(u,v));
  found=Collections.unmodifiableList(out);cache.put(id,found);return found;
 }
 public static WorldMoveTargetController.TileCenter near(String id,int u,int v){WorldMoveTargetController.TileCenter p=at(u,v),best=null;float score=Float.MAX_VALUE;for(WorldMoveTargetController.TileCenter t:tiles(id,true)){float d=(t.x-p.x)*(t.x-p.x)+(t.y-p.y)*(t.y-p.y);if(d<score){score=d;best=t;}}if(best==null)throw new IllegalStateException("Countess connected floor "+id);return best;}
 public static WorldMoveTargetController.TileCenter entry(String id){return EXTERIOR.equals(id)?near(id,25,43):LOBBY.equals(id)?near(id,12,20):near(id,6,21);}
 public static boolean inside(String id,float x,float y){return CountessCastleData.grid(id).walkable(u(x,y),v(x,y));}
 public static List<RectF> obstacles(String id){return BLOCKS.computeIfAbsent(id,k->{List<RectF> a=new ArrayList<>();HorrorCastleOriginalData.Grid g=CountessCastleData.grid(id);for(int v=0;v<g.height;v++)for(int u=0;u<g.width;u++)if(!g.walkable(u,v))a.add(new RectF(x(u,v)-13,y(u,v)-6,x(u,v)+13,y(u,v)+6));return Collections.unmodifiableList(a);});}
 public static float minX(String id){return 2000-CountessCastleData.grid(id).height*32-64;}public static float maxX(String id){return 2000+CountessCastleData.grid(id).width*32+64;}public static float maxY(String id){HorrorCastleOriginalData.Grid g=CountessCastleData.grid(id);return 240+(g.width+g.height)*16;}
 public static final class Gate {public final float x,y;public final String to,label;Gate(String from,int u,int v,String to,String label){WorldMoveTargetController.TileCenter p=near(from,u,v);x=p.x;y=p.y;this.to=to;this.label=label;}}
 private static final Map<String,List<Gate>> GATES=new HashMap<>();
 public static List<Gate> gates(String id){return GATES.computeIfAbsent(id,k->{List<Gate> a=new ArrayList<>();int z=zone(id);if(EXTERIOR.equals(id))a.add(new Gate(id,25,47,DeathVillageGeometry.LOBBY,"죽음의마을 대기실"));else if(LOBBY.equals(id)){a.add(new Gate(id,2,13,zoneId(1),"유리드 1존"));a.add(new Gate(id,21,2,UPPER,"응접실2"));a.add(new Gate(id,15,24,EXTERIOR,"별장 입구"));}else if(UPPER.equals(id)){a.add(new Gate(id,2,20,LOBBY,"응접실1"));a.add(new Gate(id,27,2,BOSS,"블러드백작의 결계"));}else if(BOSS.equals(id))a.add(new Gate(id,2,20,UPPER,"응접실2"));else if(z>0){a.add(new Gate(id,2,20,z==1?LOBBY:zoneId(z-1),z==1?"응접실1":"이전 존"));a.add(new Gate(id,27,2,z==12?UPPER:zoneId(z+1),z==12?"응접실2":"다음 존"));}return Collections.unmodifiableList(a);});}
 private CountessCastleGeometry(){}
}
