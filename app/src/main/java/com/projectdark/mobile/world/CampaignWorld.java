package com.projectdark.mobile.world;
import java.util.*;
/** Separate ADAPTED forest instances and a safe expedition camp, using the existing World lattice. */
public final class CampaignWorld {
 public static final String PIET="MAP_PIET_CAMP";
 public static final String BOSS_D="MAP_POTE_D_BOSS";
 public static final String[] ROUTE={"MAP_POTE_01","MAP_POTE_02",PIET,"MAP_POTE_03",BOSS_D,AbelTownGeometry.ID,AbelCoastGeometry.ENTRY,"MAP_ABEL_COAST_1A","MAP_ABEL_COAST_2A","MAP_ABEL_COAST_3B","MAP_ABEL_COAST_4C"};
 public static boolean contains(String id){return RucesionCoastGeometry.contains(id)||RucesionTownGeometry.contains(id)||Arrays.asList(ROUTE).contains(AbelCoastGeometry.canonical(id))||AbelTownGeometry.EAST_ID.equals(id)||"MAP_POTE_04".equals(id);}
 public static int zone(String id){if(RucesionCoastGeometry.contains(id))return 40+RucesionCoastGeometry.index(id);if(RucesionTownGeometry.contains(id))return 30;if(AbelTownGeometry.contains(id))return 9;if(AbelCoastGeometry.contains(id))return 10+AbelCoastGeometry.index(id);if(BOSS_D.equals(id))return 4;if("MAP_POTE_04".equals(id))return 3;if(id!=null&&id.startsWith("MAP_POTE_")&&id.length()==11)return Integer.parseInt(id.substring(9));return 0;}
 public static boolean canEnter(String id,int level,boolean forestComplete){return RucesionCoastGeometry.contains(id)?!RucesionCoastGeometry.BOSS.equals(id)&&level>=71:!AbelCoastGeometry.contains(id)||(level>=41&&forestComplete);}
 public static String title(String id){return PoteCampaignMapDef.forId(id).title;}
 public static String previous(String id){id=AbelCoastGeometry.canonical(AbelTownGeometry.canonical(id));if(RucesionCoastGeometry.contains(id)){int r=RucesionCoastGeometry.index(id);return r==0?RucesionTownGeometry.ID:RucesionCoastGeometry.ROUTE[r-1];}if(AbelTownGeometry.ID.equals(id))return null;int i=Arrays.asList(ROUTE).indexOf(id);return i<=0?null:ROUTE[i-1];}
 public static String next(String id){id=AbelCoastGeometry.canonical(AbelTownGeometry.canonical(id));if(RucesionCoastGeometry.contains(id)){int r=RucesionCoastGeometry.index(id);return r+1<RucesionCoastGeometry.ROUTE.length?RucesionCoastGeometry.ROUTE[r+1]:null;}if(BOSS_D.equals(id))return null;int i=Arrays.asList(ROUTE).indexOf(id);return i<0||i+1==ROUTE.length?null:ROUTE[i+1];}
 public static WorldMoveTargetController.TileCenter forward(){return forward(PoteFieldDef.MAP_ID);}
 private static final Map<String,WorldMoveTargetController.TileCenter> GATES=new HashMap<>();
 public static WorldMoveTargetController.TileCenter forward(String mapId){String key=mapId+":forward";if(GATES.containsKey(key))return GATES.get(key);PoteCampaignMapDef d=PoteCampaignMapDef.forId(mapId);WorldMoveTargetController.TileCenter t=nearestConnectedTile(mapId,d.zone==1||d.zone==0?1056:d.nextX,d.zone==1||d.zone==0?240:d.nextY);GATES.put(key,t);return t;}
 public static WorldMoveTargetController.TileCenter backward(){return backward(PoteFieldDef.MAP_ID);}
 public static WorldMoveTargetController.TileCenter backward(String mapId){PoteCampaignMapDef d=PoteCampaignMapDef.forId(mapId);String key=mapId+":back";return GATES.computeIfAbsent(key,k->nearestConnectedTile(mapId,d.backX,d.backY));}
 public static WorldMoveTargetController.TileCenter arrival(boolean fromNext){return arrival(PoteFieldDef.MAP_ID,fromNext);}
 public static WorldMoveTargetController.TileCenter arrival(String mapId,boolean fromNext){if(RucesionTownGeometry.contains(mapId))return nearestConnectedTile(mapId,RucesionTownGeometry.x(41,41),RucesionTownGeometry.y(41,41));if(AbelTownGeometry.contains(mapId))return townArrival(mapId);PoteCampaignMapDef d=PoteCampaignMapDef.forId(mapId);WorldMoveTargetController.TileCenter gate=fromNext?forward(mapId):backward(mapId);WorldMoveTargetController.TileCenter best=null;float distance=Float.MAX_VALUE;for(WorldMoveTargetController.TileCenter t:connectedTiles(mapId)){float dg=(float)Math.hypot(t.x-gate.x,t.y-gate.y);if(dg<75||dg>110)continue;float q=(float)Math.hypot(t.x-d.entryX,t.y-d.entryY);if(q<distance){distance=q;best=t;}}return best==null?nearestConnectedTile(mapId,d.entryX,d.entryY):best;}
 private static WorldMoveTargetController.TileCenter townArrival(String id){
  WorldMoveTargetController.TileCenter manager=com.projectdark.mobile.TownTravel.center(id),best=null;float distance=Float.MAX_VALUE;
  for(WorldMoveTargetController.TileCenter t:connectedTiles(id)){float separation=(float)Math.hypot(t.x-manager.x,t.y-manager.y);if(separation<64||separation>120)continue;float q=(t.x-manager.x-64)*(t.x-manager.x-64)+(t.y-manager.y-32)*(t.y-manager.y-32);if(q<distance){distance=q;best=t;}}
  if(best==null)throw new IllegalStateException("No legal town arrival near manager "+id);return best;
 }
 public static WorldMoveTargetController.TileCenter nearestConnectedTile(String mapId,float x,float y){WorldMoveTargetController.TileCenter best=null;float bd=Float.MAX_VALUE;for(WorldMoveTargetController.TileCenter t:connectedTiles(mapId)){float dx=t.x-x,dy=t.y-y,q=dx*dx+dy*dy;if(q<bd){bd=q;best=t;}}return best;}
 private static final Map<String,List<WorldMoveTargetController.TileCenter>> CONNECTED=new HashMap<>();
 /** Terrain-only connected component; prevents spawn/gate anchors on isolated creek banks. */
 public static List<WorldMoveTargetController.TileCenter> connectedTiles(){
  return connectedTiles(PoteFieldDef.MAP_ID);
 }
 public static List<WorldMoveTargetController.TileCenter> connectedTiles(String mapId){synchronized(CONNECTED){List<WorldMoveTargetController.TileCenter> cached=CONNECTED.get(mapId);if(cached!=null)return cached;PoteCampaignMapDef def=PoteCampaignMapDef.forId(mapId);Map<String,WorldMoveTargetController.TileCenter> all=new HashMap<>();for(WorldMoveTargetController.TileCenter t:def.navigationTiles())all.put(key(t.x,t.y),t);List<WorldMoveTargetController.TileCenter> out=new ArrayList<>();Set<String> seen=new HashSet<>();ArrayDeque<WorldMoveTargetController.TileCenter> queue=new ArrayDeque<>();WorldMoveTargetController.TileCenter root=def.nearest(def.entryX,def.entryY);queue.add(root);seen.add(key(root.x,root.y));while(!queue.isEmpty()){WorldMoveTargetController.TileCenter a=queue.remove();out.add(a);for(WorldMoveTargetController.Direction d:WorldMoveTargetController.Direction.values()){WorldMoveTargetController.TileCenter b=all.get(key(a.x+d.dx,a.y+d.dy));if(b==null||seen.contains(key(b.x,b.y))||!terrainEdge(a,b,def))continue;seen.add(key(b.x,b.y));queue.add(b);}}List<WorldMoveTargetController.TileCenter> result=Collections.unmodifiableList(out);CONNECTED.put(mapId,result);return result;}}
 private static String key(float x,float y){return Math.round(x)+":"+Math.round(y);}
 private static boolean terrainEdge(WorldMoveTargetController.TileCenter a,WorldMoveTargetController.TileCenter b,PoteCampaignMapDef def){for(int i=0;i<=12;i++){float x=a.x+(b.x-a.x)*i/12f,y=a.y+(b.y-a.y)*i/12f;if(def.terrainBlocked(x,y,10))return false;}return true;}
 private CampaignWorld(){}
}

