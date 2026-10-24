package com.projectdark.mobile.world;
import android.graphics.RectF;
import java.util.*;
/** Ten original-source scenes with static navigation and live actor collision through the shared World adapter. */
public final class DeathVillageGeometry {
 public static final String LOBBY="MAP_DEATH_LOBBY",FIELD="MAP_DEATH_FIELD";
 public static boolean contains(String id){return DeathVillageData.grid(id)!=null;}
 public static boolean under(String id){return id!=null&&id.startsWith("MAP_DEATH_UNDER_");}
 public static boolean house(String id){return id!=null&&id.startsWith("MAP_DEATH_HOUSE_");}
 public static float x(int u,int v){return 3400+(u-v)*32;}public static float y(int u,int v){return 160+(u+v)*16;}
 public static int u(float x,float y){return Math.round((y-160)/32+(x-3400)/64);}public static int v(float x,float y){return Math.round((y-160)/32-(x-3400)/64);}
 public static WorldMoveTargetController.TileCenter at(int u,int v){return new WorldMoveTargetController.TileCenter(x(u,v),y(u,v));}
 public static WorldMoveTargetController.TileCenter entry(String id){int[] p=DeathVillageData.spawn(id);return at(p[0],p[1]);}
 public static float portalX(DeathVillageData.Gate gate){return gate.doorSide==0?x(gate.u,gate.v):x(gate.doorU,gate.doorV)+(gate.doorSide==1?-16:16);}
 public static float portalY(DeathVillageData.Gate gate){return gate.doorSide==0?y(gate.u,gate.v):y(gate.doorU,gate.doorV)+16;}
 public static String title(String id){if(LOBBY.equals(id))return "죽음의마을 대기실";if(FIELD.equals(id))return "죽음의마을";if(house(id))return "죽음의마을 집안 "+id.substring(id.length()-1);return "죽음의마을 지하 "+id.substring(id.length()-1);}
 public static boolean inside(String id,float x,float y){return DeathVillageData.grid(id).walkable(u(x,y),v(x,y));}
 private static final Map<String,List<WorldMoveTargetController.TileCenter>> NAV=new HashMap<>(),GROUND=new HashMap<>();private static final Map<String,List<RectF>> OBSTACLES=new HashMap<>();
 public static List<WorldMoveTargetController.TileCenter> tiles(String id,boolean navigation){Map<String,List<WorldMoveTargetController.TileCenter>> cache=navigation?NAV:GROUND;List<WorldMoveTargetController.TileCenter> result=cache.get(id);if(result!=null)return result;HorrorCastleOriginalData.Grid g=DeathVillageData.grid(id);List<WorldMoveTargetController.TileCenter> out=new ArrayList<>();for(int v=0;v<g.height;v++)for(int u=0;u<g.width;u++)if(navigation?g.walkable(u,v):g.value(u,v,0)>0)out.add(at(u,v));result=Collections.unmodifiableList(out);cache.put(id,result);return result;}
 public static List<RectF> obstacles(String id){List<RectF> cached=OBSTACLES.get(id);if(cached!=null)return cached;HorrorCastleOriginalData.Grid g=DeathVillageData.grid(id);List<RectF> out=new ArrayList<>();for(int v=0;v<g.height;v++)for(int u=0;u<g.width;u++)if(!g.walkable(u,v))out.add(new RectF(x(u,v)-13,y(u,v)-6,x(u,v)+13,y(u,v)+6));cached=Collections.unmodifiableList(out);OBSTACLES.put(id,cached);return cached;}
 private static final Map<String,List<DeathVillageData.Gate>> GATES=new HashMap<>();
 public static List<DeathVillageData.Gate> gates(String id){List<DeathVillageData.Gate> cached=GATES.get(id);if(cached!=null)return cached;List<DeathVillageData.Gate> out=new ArrayList<>();for(DeathVillageData.Gate g:DeathVillageData.GATES)if(g.map.equals(id))out.add(g);cached=Collections.unmodifiableList(out);GATES.put(id,cached);return cached;}
 public static float minX(String id){return 3400-DeathVillageData.grid(id).height*32-64;}public static float maxX(String id){return 3400+DeathVillageData.grid(id).width*32+64;}public static float maxY(String id){HorrorCastleOriginalData.Grid g=DeathVillageData.grid(id);return 240+(g.width+g.height)*16;}
 private DeathVillageGeometry(){}
}
