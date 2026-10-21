package com.projectdark.mobile.world;
import java.util.*;
/** Mobile route labels/placement are adapted; original temple room tiles stay intact. */
public final class TempleRoutes {
 public static final class Gate {
  public final float x,y;public final String label,target;public final boolean exit;
  Gate(int u,int v,String label,String target,boolean exit){x=TownInteriorDef.x(u,v);y=TownInteriorDef.y(u,v);this.label=label;this.target=target;this.exit=exit;}
 }
 private static final Map<String,List<Gate>> CACHE=new HashMap<>();
 public static List<Gate> forRoom(TownInteriorDef d){if(!d.isTemple())return Collections.emptyList();List<Gate> gates=CACHE.get(d.mapId);if(gates==null){gates=create(d);CACHE.put(d.mapId,gates);}return gates;}
 private static List<Gate> create(TownInteriorDef d){
  if(!d.isTemple())return Collections.emptyList();
  List<Gate> gates=new ArrayList<>();
  // Lobby forward travel is exclusively the guide NPC.
  if(d.mapId.endsWith("_hub"))gates.add(new Gate(17,23,"세오신전 · 체력/마력","milles_interior_temple_seo",false));
  gates.add(new Gate(exitU(d),exitV(d),d.mapId.endsWith("_lobby")?"밀레스 마을":d.mapId.endsWith("_hub")?"신전대기실":"신들의 세계",null,true));
  return Collections.unmodifiableList(gates);
 }
 public static int exitU(TownInteriorDef d){return d.mapId.endsWith("_temple_hub")?27:d.mapId.endsWith("_temple_seo")?9:d.original().exitU;}
 public static int exitV(TownInteriorDef d){return d.mapId.endsWith("_temple_hub")?26:d.mapId.endsWith("_temple_seo")?7:d.original().exitV;}
 private TempleRoutes(){}
}
