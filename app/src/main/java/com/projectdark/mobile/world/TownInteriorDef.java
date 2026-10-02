package com.projectdark.mobile.world;
import android.graphics.RectF;
import java.util.*;
/** Authored isometric rooms; service anchors face the customer across one counter tile. */
public final class TownInteriorDef {
 public enum Kind { REAGENT,EQUIPMENT,BANK,CHURCH }
 public static final float MIN_X=64,MAX_X=896,MIN_Y=0,MAX_Y=540;
 public static final class Prop {
  public final String asset;public final int u,v;public final float width,lift;public final boolean blocking;
  public Prop(String a,int u,int v,float w){this(a,u,v,w,0,true);}
  public Prop(String a,int u,int v,float w,float lift,boolean blocking){asset=a;this.u=u;this.v=v;width=w;this.lift=lift;this.blocking=blocking;}
  public float x(){return TownInteriorDef.x(u,v);}public float y(){return TownInteriorDef.y(u,v);}
 }

 public final Kind kind;public final String mapId,title,npcName,outfit,doorId;
 public final List<Prop> props;
 public final int columns,rows,npcU,npcV,customerU,customerV,counterV,counterStart,counterEnd;
 private TownInteriorDef(Kind k,String id,String title,String npc,String outfit,String door){
  kind=k;mapId=id;this.title=title;npcName=npc;this.outfit=outfit;doorId=door;
  columns=k==Kind.EQUIPMENT?14:k==Kind.BANK?10:12;rows=k==Kind.EQUIPMENT?10:12;
  npcU=k==Kind.CHURCH?6:k==Kind.EQUIPMENT?8:7;npcV=k==Kind.BANK?3:k==Kind.EQUIPMENT?1:2;
  counterV=npcV+2;customerU=npcU;customerV=counterV+1;
  counterStart=k==Kind.BANK?4:3;counterEnd=k==Kind.EQUIPMENT?11:k==Kind.BANK?8:10;
  List<Prop> p=new ArrayList<>();
  if(k!=Kind.CHURCH)for(int u=counterStart;u<=counterEnd;u++)p.add(new Prop("joined_counter",u,counterV,64));
  if(k==Kind.REAGENT){
   p.add(new Prop("potion_cabinet",2,1,76));p.add(new Prop("potion_cabinet",5,1,76));p.add(new Prop("potion_cabinet",10,1,76));
   p.add(new Prop("potion_table",2,3,54));p.add(new Prop("potion_table",10,2,50));p.add(new Prop("potion_table",1,8,54));
   p.add(new Prop("storage_crate",1,5,36));p.add(new Prop("storage_sack",1,6,30));p.add(new Prop("storage_barrel",10,3,30));
   p.add(new Prop("herb_planter",10,8,38));
   p.add(new Prop("bottle_tray",4,counterV,32,42,false));p.add(new Prop("bottle_tray",9,counterV,32,42,false));
   p.add(new Prop("open_book",6,counterV,23,42,false));p.add(new Prop("brass_scale",8,counterV,24,42,false));
  }
  if(k==Kind.EQUIPMENT){
   p.add(new Prop("weapon_rack",3,1,76));p.add(new Prop("weapon_rack",6,1,76));p.add(new Prop("weapon_rack",11,1,76));
   p.add(new Prop("shield_rack",1,3,72));p.add(new Prop("shield_rack",1,6,64));p.add(new Prop("sword_barrel",12,2,42));p.add(new Prop("sword_barrel",4,2,38));
   p.add(new Prop("armor_stand",2,8,42));p.add(new Prop("storage_crate",12,6,38));p.add(new Prop("storage_barrel",12,7,30));
   p.add(new Prop("weapon_tray",4,counterV,32,42,false));p.add(new Prop("weapon_tray",10,counterV,32,42,false));
   p.add(new Prop("open_book",7,counterV,23,42,false));
  }
  if(k==Kind.BANK){
   p.add(new Prop("safe",2,1,76));p.add(new Prop("safe",5,1,76));p.add(new Prop("safe",8,1,70));
   p.add(new Prop("ledger",8,2,48));p.add(new Prop("ledger",1,7,50));p.add(new Prop("archive_shelf",1,3,52));
   p.add(new Prop("storage_crate",8,8,34));p.add(new Prop("herb_planter",1,9,38));
   p.add(new Prop("coin_tray",5,counterV,27,42,false));p.add(new Prop("open_book",7,counterV,25,42,false));
   p.add(new Prop("brass_scale",8,counterV,24,42,false));
  }
  if(k==Kind.CHURCH){
   p.add(new Prop("altar",6,1,96));for(int v=6;v<=10;v+=2){p.add(new Prop("pew",3,v,72));p.add(new Prop("pew",9,v,72));}
   p.add(new Prop("votive_stand",2,3,42));p.add(new Prop("votive_stand",10,3,42));
   p.add(new Prop("holy_font",1,9,40));p.add(new Prop("herb_planter",1,5,36));p.add(new Prop("herb_planter",10,5,36));
   p.add(new Prop("open_book",6,1,20,45,false));
  }
  props=Collections.unmodifiableList(p);
 }
 public static final List<TownInteriorDef> ALL=Collections.unmodifiableList(Arrays.asList(
  new TownInteriorDef(Kind.REAGENT,"milles_interior_potion_shop","시약상점","멀린","mu0000020,mh172,ml230","potion_shop_door"),
  new TownInteriorDef(Kind.EQUIPMENT,"milles_interior_weapon_shop","장비상점","로건","mu0000016,mh001,ml230","weapon_shop_door"),
  new TownInteriorDef(Kind.BANK,"milles_interior_bank","밀레스 은행","은행원","mu0000026,mh004,ml230","general_shop_door"),
  new TownInteriorDef(Kind.CHURCH,"milles_interior_church","밀레스 성당","사제","mu0000015,mh003,ml230","church_door")));
 public static TownInteriorDef forMap(String id){for(TownInteriorDef d:ALL)if(d.mapId.equals(id))return d;return null;}
 public static TownInteriorDef forDoor(String id){if(id!=null&&id.endsWith("_2"))id=id.substring(0,id.length()-2);for(TownInteriorDef d:ALL)if(d.doorId.equals(id))return d;return null;}
 public static float x(int u,int v){return 480+32*(u-v);}public static float y(int u,int v){return 132+16*(u+v);}
 private int entryU(){return kind==Kind.BANK?5:6;}
 public float spawnX(){return x(entryU(),rows-2);}public float spawnY(){return y(entryU(),rows-2);}public float exitX(){return x(entryU(),rows-1);}public float exitY(){return y(entryU(),rows-1);}
 public float npcX(){return x(npcU,npcV);}public float npcY(){return y(npcU,npcV);}public float customerX(){return x(customerU,customerV);}public float customerY(){return y(customerU,customerV);}
 public boolean blocked(int u,int v){if(u<=0||v<=0||u>=columns||v>=rows)return true;if(u==npcU&&v==npcV)return true;if(kind!=Kind.CHURCH&&v==counterV&&u>=counterStart&&u<=counterEnd)return true;for(Prop p:props)if(p.blocking&&p.u==u&&p.v==v)return true;return false;}
 public List<WorldMoveTargetController.TileCenter> navigationTiles(){List<WorldMoveTargetController.TileCenter> t=new ArrayList<>();for(int u=1;u<columns;u++)for(int v=1;v<rows;v++)if(!blocked(u,v))t.add(new WorldMoveTargetController.TileCenter(x(u,v),y(u,v)));return Collections.unmodifiableList(t);}
 public List<RectF> obstacles(){List<RectF> a=new ArrayList<>();for(int u=0;u<columns;u++)for(int v=0;v<rows;v++)if(blocked(u,v))a.add(new RectF(x(u,v)-3,y(u,v)-3,x(u,v)+3,y(u,v)+3));return a;}
 public int floor(int u,int v){return kind==Kind.REAGENT?1:kind==Kind.CHURCH?(u>=5&&u<=7?3:2):0;}
}
