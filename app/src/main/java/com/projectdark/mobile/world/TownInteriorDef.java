package com.projectdark.mobile.world;
import android.graphics.RectF;
import java.util.*;
/** Authored isometric rooms; service anchors face the customer across one counter tile. */
public final class TownInteriorDef {
 public enum Kind { REAGENT,EQUIPMENT,BANK,CHURCH }
 public static final float MIN_X=64,MAX_X=896,MIN_Y=0,MAX_Y=540;
 public static final class Prop {public final String asset;public final int u,v;public final float width;public Prop(String a,int u,int v,float w){asset=a;this.u=u;this.v=v;width=w;}public float x(){return TownInteriorDef.x(u,v);}public float y(){return TownInteriorDef.y(u,v);}}
 public final Kind kind;public final String mapId,title,npcName,outfit,doorId;
 public final List<Prop> props;
 public final int columns,rows,npcU,npcV,customerU,customerV,counterV,counterStart,counterEnd;
 private TownInteriorDef(Kind k,String id,String title,String npc,String outfit,String door){
  kind=k;mapId=id;this.title=title;npcName=npc;this.outfit=outfit;doorId=door;
  columns=k==Kind.EQUIPMENT?14:k==Kind.BANK?10:12;rows=k==Kind.EQUIPMENT?10:12;
  npcU=k==Kind.CHURCH?6:k==Kind.EQUIPMENT?8:7;npcV=k==Kind.BANK?4:k==Kind.EQUIPMENT?2:3;
  counterV=npcV+1;customerU=npcU;customerV=npcV+2;
  counterStart=k==Kind.BANK?4:3;counterEnd=k==Kind.EQUIPMENT?11:k==Kind.BANK?8:10;
  List<Prop> p=new ArrayList<>();
  if(k!=Kind.CHURCH)for(int u=counterStart;u<=counterEnd;u++)p.add(new Prop("joined_counter",u,counterV,64));
  if(k==Kind.REAGENT){p.add(new Prop("potion_cabinet",3,1,80));p.add(new Prop("potion_cabinet",9,1,80));p.add(new Prop("potion_table",2,3,56));p.add(new Prop("potion_table",10,2,52));p.add(new Prop("potion_table",1,8,56));}
  if(k==Kind.EQUIPMENT){p.add(new Prop("weapon_rack",3,1,84));p.add(new Prop("weapon_rack",11,1,84));p.add(new Prop("shield_rack",1,5,80));p.add(new Prop("shield_rack",1,8,64));p.add(new Prop("sword_barrel",12,2,44));p.add(new Prop("sword_barrel",3,2,38));}
  if(k==Kind.BANK){p.add(new Prop("safe",2,1,80));p.add(new Prop("safe",5,1,80));p.add(new Prop("ledger",8,2,50));p.add(new Prop("ledger",1,7,52));}
  if(k==Kind.CHURCH){p.add(new Prop("altar",6,1,96));for(int v=6;v<=10;v+=2){p.add(new Prop("pew",3,v,72));p.add(new Prop("pew",9,v,72));}}
  props=Collections.unmodifiableList(p);
 }
 public static final List<TownInteriorDef> ALL=Collections.unmodifiableList(Arrays.asList(
  new TownInteriorDef(Kind.REAGENT,"milles_interior_potion_shop","시약상점","멀린","mu0000020,mh172,ml230","potion_shop_door"),
  new TownInteriorDef(Kind.EQUIPMENT,"milles_interior_weapon_shop","장비상점","로건","mu0000016,mh001,ml230","weapon_shop_door"),
  new TownInteriorDef(Kind.BANK,"milles_interior_bank","밀레스 은행","은행원","mu0000026,mh004,ml230","general_shop_door"),
  new TownInteriorDef(Kind.CHURCH,"milles_interior_church","밀레스 성당","사제","mu0000015,mh003,ml230","church_door")));
 public static TownInteriorDef forMap(String id){for(TownInteriorDef d:ALL)if(d.mapId.equals(id))return d;return null;}
 public static TownInteriorDef forDoor(String id){for(TownInteriorDef d:ALL)if(d.doorId.equals(id))return d;return null;}
 public static float x(int u,int v){return 480+32*(u-v);}public static float y(int u,int v){return 132+16*(u+v);}
 private int entryU(){return kind==Kind.BANK?5:6;}
 public float spawnX(){return x(entryU(),rows-2);}public float spawnY(){return y(entryU(),rows-2);}public float exitX(){return x(entryU(),rows-1);}public float exitY(){return y(entryU(),rows-1);}
 public float npcX(){return x(npcU,npcV);}public float npcY(){return y(npcU,npcV);}public float customerX(){return x(customerU,customerV);}public float customerY(){return y(customerU,customerV);}
 public boolean blocked(int u,int v){if(u<=0||v<=0||u>=columns||v>=rows)return true;if(u==npcU&&v==npcV)return true;if(kind!=Kind.CHURCH&&v==counterV&&u>=counterStart&&u<=counterEnd)return true;for(Prop p:props)if(p.u==u&&p.v==v)return true;return false;}
 public List<WorldMoveTargetController.TileCenter> navigationTiles(){List<WorldMoveTargetController.TileCenter> t=new ArrayList<>();for(int u=1;u<columns;u++)for(int v=1;v<rows;v++)if(!blocked(u,v))t.add(new WorldMoveTargetController.TileCenter(x(u,v),y(u,v)));return Collections.unmodifiableList(t);}
 public List<RectF> obstacles(){List<RectF> a=new ArrayList<>();for(int u=0;u<columns;u++)for(int v=0;v<rows;v++)if(blocked(u,v))a.add(new RectF(x(u,v)-3,y(u,v)-3,x(u,v)+3,y(u,v)+3));return a;}
 public int floor(int u,int v){return kind==Kind.REAGENT?1:kind==Kind.CHURCH?(u>=5&&u<=7?3:2):0;}
}
