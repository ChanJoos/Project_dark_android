package com.projectdark.mobile.world;
import java.util.*;
/** Mobile route labels/placement are adapted; original temple room tiles stay intact. */
public final class TempleRoutes {
 public static final class Gate {
  public final float x,y;public final String label,target;public final boolean exit;
  Gate(int u,int v,String label,String target,boolean exit){x=TownInteriorDef.x(u,v);y=TownInteriorDef.y(u,v);this.label=label;this.target=target;this.exit=exit;}
 }
 public static List<Gate> forRoom(TownInteriorDef d){
  if(!d.isTemple())return Collections.emptyList();
  List<Gate> gates=new ArrayList<>();
  if(d.mapId.endsWith("_lobby"))gates.add(new Gate(9,5,"신들의 세계","milles_interior_temple_hub",false));
  if(d.mapId.endsWith("_hub"))gates.add(new Gate(25,23,"세오신전 · 체력/마력","milles_interior_temple_seo",false));
  gates.add(new Gate(d.original().exitU,d.original().exitV,d.mapId.endsWith("_lobby")?"밀레스 마을":d.mapId.endsWith("_hub")?"신전대기실":"신들의 세계",null,true));
  return Collections.unmodifiableList(gates);
 }
 private TempleRoutes(){}
}
