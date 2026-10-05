package com.projectdark.mobile.world;
import java.util.*;
/** Separate ADAPTED forest instances and a safe expedition camp, using the existing World lattice. */
public final class CampaignWorld {
 public static final String PIET="MAP_PIET_CAMP";
 public static final String[] ROUTE={"MAP_POTE_01","MAP_POTE_02",PIET,"MAP_POTE_03","MAP_POTE_04"};
 public static boolean contains(String id){return Arrays.asList(ROUTE).contains(id);}
 public static int zone(String id){return id!=null&&id.startsWith("MAP_POTE_")?Integer.parseInt(id.substring(9)):0;}
 public static String title(String id){return PIET.equals(id)?"피에트 조사 거점":"포테의 숲 · "+zone(id)+"구역";}
 public static String previous(String id){int i=Arrays.asList(ROUTE).indexOf(id);return i<=0?null:ROUTE[i-1];}
 public static String next(String id){int i=Arrays.asList(ROUTE).indexOf(id);return i<0||i+1==ROUTE.length?null:ROUTE[i+1];}
 public static WorldMoveTargetController.TileCenter forward(){return PoteFieldDef.nearestNavigationCenter(1056,240);}
 public static WorldMoveTargetController.TileCenter backward(){return PoteFieldDef.nearestNavigationCenter(PoteFieldDef.EXIT_X,PoteFieldDef.EXIT_Y);}
 public static WorldMoveTargetController.TileCenter arrival(boolean fromNext){WorldMoveTargetController.TileCenter gate=fromNext?forward():backward();WorldMoveTargetController.TileCenter best=null;float distance=Float.MAX_VALUE;for(WorldMoveTargetController.TileCenter t:PoteFieldDef.navigationTiles()){float dg=(float)Math.hypot(t.x-gate.x,t.y-gate.y);if(dg<75||dg>110)continue;float d=(float)Math.hypot(t.x-PoteFieldDef.ENTRY_X,t.y-PoteFieldDef.ENTRY_Y);if(d<distance){distance=d;best=t;}}return best==null?PoteFieldDef.nearestNavigationCenter(PoteFieldDef.ENTRY_X,PoteFieldDef.ENTRY_Y):best;}
 private static List<WorldMoveTargetController.TileCenter> connected;
 /** Terrain-only connected component; prevents spawn/gate anchors on isolated creek banks. */
 public static List<WorldMoveTargetController.TileCenter> connectedTiles(){
  if(connected!=null)return connected;
  Map<String,WorldMoveTargetController.TileCenter> all=new HashMap<>();for(WorldMoveTargetController.TileCenter t:PoteFieldDef.navigationTiles())all.put(key(t.x,t.y),t);
  List<WorldMoveTargetController.TileCenter> out=new ArrayList<>();Set<String> seen=new HashSet<>();ArrayDeque<WorldMoveTargetController.TileCenter> queue=new ArrayDeque<>();WorldMoveTargetController.TileCenter root=PoteFieldDef.nearestNavigationCenter(PoteFieldDef.ENTRY_X,PoteFieldDef.ENTRY_Y);queue.add(root);seen.add(key(root.x,root.y));
  while(!queue.isEmpty()){WorldMoveTargetController.TileCenter a=queue.remove();out.add(a);for(WorldMoveTargetController.Direction d:WorldMoveTargetController.Direction.values()){WorldMoveTargetController.TileCenter b=all.get(key(a.x+d.dx,a.y+d.dy));if(b==null||seen.contains(key(b.x,b.y))||!terrainEdge(a,b))continue;seen.add(key(b.x,b.y));queue.add(b);}}
  connected=Collections.unmodifiableList(out);return connected;
 }
 private static String key(float x,float y){return Math.round(x)+":"+Math.round(y);}
 private static boolean terrainEdge(WorldMoveTargetController.TileCenter a,WorldMoveTargetController.TileCenter b){for(int i=0;i<=12;i++){float x=a.x+(b.x-a.x)*i/12f,y=a.y+(b.y-a.y)*i/12f;for(android.graphics.RectF r:PoteFieldDef.obstacles())if(x+10>r.left&&x-10<r.right&&y+10>r.top&&y-10<r.bottom)return false;}return true;}
 private CampaignWorld(){}
}
