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
  new Destination(MineTownGeometry.ID,"mine_town_travel","마인 마을")));
 private static final Destination LEGACY_PIET=new Destination(CampaignWorld.PIET,"piet_town_travel","피에트 거점");
 public static boolean isManager(String npc){if(LEGACY_PIET.npc.equals(npc))return true;for(Destination d:TOWNS)if(d.npc.equals(npc))return true;return false;}
 public static boolean isTown(String map){return forMap(map)!=null;}
 public static Destination forMap(String map){map=AbelTownGeometry.canonical(map);if(LEGACY_PIET.map.equals(map))return LEGACY_PIET;for(Destination d:TOWNS)if(d.map.equals(map))return d;return null;}
 public static boolean canDepart(String map,String npc){Destination d=forMap(map);return d!=null&&d.npc.equals(npc);}
 public static WorldMoveTargetController.TileCenter center(String map){
  if(WorldDef.ID.equals(map))return new WorldMoveTargetController.TileCenter(512,544);
  float x=800,y=608;
  if(MineTownGeometry.contains(map)){x=MineTownGeometry.x(37,25);y=MineTownGeometry.y(37,25);}
  if(AbelTownGeometry.contains(map)){AbelTownGeometry.Building shop=AbelTownGeometry.forInterior("abel_interior_reagent");x=shop.doorX()+96;y=shop.doorY()+48;}
  if(RucesionTownGeometry.contains(map)){RucesionTownGeometry.Building shop=RucesionTownGeometry.forInterior("rucesion_interior_reagent");x=shop.doorX()+96;y=shop.doorY()+48;}
  return CampaignWorld.nearestConnectedTile(map,x,y);
 }
 private TownTravel(){}
}


