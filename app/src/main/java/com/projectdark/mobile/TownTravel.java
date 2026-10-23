package com.projectdark.mobile;
import com.projectdark.mobile.world.*;
import java.util.*;
/** User-authorized mobile town network. Town visits do not unlock hunting gates or quests. */
public final class TownTravel {
 public static final class Destination {
  public final String map,npc,title;
  Destination(String map,String npc,String title){this.map=map;this.npc=npc;this.title=title;}
 }
 public static final List<Destination> TOWNS=Collections.unmodifiableList(Arrays.asList(
  new Destination(WorldDef.ID,"pote_travel_guide","밀레스 마을"),
  new Destination(AbelTownGeometry.ID,"abel_town_travel","아벨 마을"),
  new Destination(RucesionTownGeometry.ID,"rucesion_town_travel","뤼케시온 마을"),
  new Destination(MineTownGeometry.ID,"mine_town_travel","마인 마을"),
  new Destination(DeathVillageGeometry.LOBBY,"death_village_travel","죽음의마을 대기실")));
 private static final Destination LEGACY_PIET=new Destination(CampaignWorld.PIET,"piet_town_travel","피에트 거점");
 public static boolean isManager(String npc){if("abel_coast_departure".equals(npc)||"rucesion_beach_keeper".equals(npc)||HorrorCastleTravel.EXIT_NPC.equals(npc))return true;if(LEGACY_PIET.npc.equals(npc))return true;for(Destination d:TOWNS)if(d.npc.equals(npc))return true;return false;}
 public static boolean isTown(String map){return forMap(map)!=null;}
 public static Destination forMap(String map){map=AbelTownGeometry.canonical(map);if(LEGACY_PIET.map.equals(map))return LEGACY_PIET;for(Destination d:TOWNS)if(d.map.equals(map))return d;return null;}
 /** Death Village entry is offered by the existing Mine town manager only. */
 public static boolean offers(String from,String to){return !DeathVillageGeometry.LOBBY.equals(to)||MineTownGeometry.ID.equals(from);}
 public static boolean canDepart(String map,String npc){if(HorrorCastleTravel.EXIT_NPC.equals(npc))return HorrorCastleGeometry.contains(map);if("abel_coast_departure".equals(npc))return AbelTownGeometry.contains(map);if("rucesion_beach_keeper".equals(npc))return RucesionTownGeometry.contains(map);Destination d=forMap(map);return d!=null&&d.npc.equals(npc);}
 public static WorldMoveTargetController.TileCenter center(String map){
  if(WorldDef.ID.equals(map))return new WorldMoveTargetController.TileCenter(512,544);
  float x=800,y=608;
  if(DeathVillageGeometry.LOBBY.equals(map)){x=DeathVillageGeometry.x(10,10);y=DeathVillageGeometry.y(10,10);}
  if(MineTownGeometry.contains(map)){x=MineTownGeometry.x(37,25);y=MineTownGeometry.y(37,25);}
  if(AbelTownGeometry.contains(map)){AbelTownGeometry.Building shop=AbelTownGeometry.forInterior("abel_interior_reagent");x=shop.doorX()+96;y=shop.doorY()+48;}
  if(RucesionTownGeometry.contains(map)){RucesionTownGeometry.Building shop=RucesionTownGeometry.forInterior("rucesion_interior_reagent");x=shop.doorX()+96;y=shop.doorY()+48;}
  return CampaignWorld.nearestConnectedTile(map,x,y);
 }
 /** Shared arrival for recall and NPC town travel; never the occupied manager tile. */
 public static WorldMoveTargetController.TileCenter arrival(String map){
  if(WorldDef.ID.equals(map))return new WorldMoveTargetController.TileCenter(WorldDef.PLAYER_SPAWN_X,WorldDef.PLAYER_SPAWN_Y);
  return CampaignWorld.arrival(map,false);
 }
 /** Resolve only actual town recall names, including imported catalog IDs. */
 public static String recallDestination(String name){
  if(name==null)return null;String n=name.replace(" ","").replace("마을","");
  if(!n.endsWith("리콜"))return null;
  if(n.equals("밀레스리콜"))return WorldDef.ID;
  if(n.equals("아벨리콜"))return AbelTownGeometry.ID;
  if(n.equals("뤼케시온리콜"))return RucesionTownGeometry.ID;
  if(n.equals("마인리콜"))return MineTownGeometry.ID;
  if(n.equals("피에트리콜"))return CampaignWorld.PIET;
  if(n.equals("죽음의리콜"))return DeathVillageGeometry.LOBBY;
  return null;
 }
 private TownTravel(){}
}


