package com.projectdark.mobile.world;
import android.graphics.RectF;
import java.util.*;
/** Authored isometric rooms; service anchors face the customer across one counter tile. */
public final class TownInteriorDef {
 public enum Kind { REAGENT,EQUIPMENT,BANK,CHURCH,INN }
 public static final float MIN_X=64,MAX_X=896,MIN_Y=0,MAX_Y=540;
 public static final class Prop {
  public final String asset;public final int u,v;public final float width,lift,sourceGroundX,sourceGroundY;public final boolean blocking;public final int cellsU,cellsV;
  public Prop(String a,int u,int v,float w){this(a,u,v,w,0,true);}
  public Prop(String a,int u,int v,float w,float lift,boolean blocking){this(a,u,v,w,lift,blocking,1,1);}
  public Prop(String a,int u,int v,float w,float lift,boolean blocking,int cellsU,int cellsV){this(a,u,v,w,lift,blocking,cellsU,cellsV,Float.NaN,Float.NaN);}
  public Prop(String a,int u,int v,float w,float lift,boolean blocking,int cellsU,int cellsV,float sx,float sy){sourceGroundX=sx;sourceGroundY=sy;asset=a;this.u=u;this.v=v;width=w;this.lift=lift;this.blocking=blocking;this.cellsU=cellsU;this.cellsV=cellsV;}
  public boolean occupies(int tu,int tv){return blocking&&tu>=u&&tu<u+cellsU&&tv>=v&&tv<v+cellsV;}
  public float depthY(){return y()+(Float.isNaN(sourceGroundY)?16*(cellsU+cellsV-2):0);}
  public float x(){return Float.isNaN(sourceGroundX)?TownInteriorDef.x(u,v):sourceGroundX;}public float y(){return Float.isNaN(sourceGroundY)?TownInteriorDef.y(u,v):sourceGroundY;}
 }

 public final Kind kind;public final String mapId,title,npcName,npcId,outfit,doorId;
 public final List<Prop> props;
 public final int columns,rows,npcU,npcV,customerU,customerV,counterV,counterStart,counterEnd;
 private TownInteriorDef(Kind k,String id,String title,String npc,String outfit,String door){
  kind=k;mapId=id;this.title=title;npcId=com.projectdark.mobile.NpcIdentity.interiorKey(id,k.name());com.projectdark.mobile.NpcIdentity.Profile profile=com.projectdark.mobile.NpcIdentity.forId(npcId);npcName=profile.label();this.outfit=profile.outfit;doorId=door;
  columns=k==Kind.INN&&id.startsWith("abel_")?17:k==Kind.EQUIPMENT?14:k==Kind.BANK?10:k==Kind.INN?14:12;rows=k==Kind.INN&&id.startsWith("abel_")?16:k==Kind.EQUIPMENT?10:k==Kind.INN?13:12;
  npcU=k==Kind.INN&&id.startsWith("abel_")?10:k==Kind.CHURCH?6:k==Kind.EQUIPMENT?8:k==Kind.INN?5:7;npcV=k==Kind.INN&&id.startsWith("abel_")?4:k==Kind.BANK?3:k==Kind.EQUIPMENT?1:2;
  counterV=npcV+2;customerU=npcU;customerV=counterV+1;
  counterStart=k==Kind.BANK?4:3;counterEnd=k==Kind.EQUIPMENT?11:k==Kind.BANK?8:10;
  List<Prop> p=new ArrayList<>();
  if(k==Kind.REAGENT||k==Kind.EQUIPMENT||k==Kind.BANK)for(int u=counterStart;u<=counterEnd;u++)p.add(new Prop("joined_counter",u,counterV,64));
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
   p.add(new Prop("shield_rack",2,8,48));p.add(new Prop("storage_crate",12,6,38));p.add(new Prop("storage_barrel",12,7,30));
   p.add(new Prop("weapon_tray",4,counterV,32,42,false));p.add(new Prop("weapon_tray",10,counterV,32,42,false));
   p.add(new Prop("open_book",7,counterV,23,42,false));
  }
  if(k==Kind.BANK){
   p.add(new Prop("safe",2,1,76));p.add(new Prop("safe",5,1,76));p.add(new Prop("safe",8,1,70));
   p.add(new Prop("ledger",8,2,48));p.add(new Prop("ledger",1,7,50));p.add(new Prop("safe",1,3,52));
   p.add(new Prop("storage_crate",8,8,34));p.add(new Prop("herb_planter",1,9,38));
   p.add(new Prop("coin_tray",5,counterV,27,42,false));p.add(new Prop("open_book",7,counterV,25,42,false));
   p.add(new Prop("brass_scale",8,counterV,24,42,false));
  }
  if(k==Kind.CHURCH){
   p.add(new Prop("altar",6,1,96));for(int v=6;v<=10;v+=2){p.add(new Prop("pew",3,v,72));p.add(new Prop("pew",9,v,72));}
   p.add(new Prop("altar",2,3,42));p.add(new Prop("altar",10,3,42));
   p.add(new Prop("holy_font",1,9,40));p.add(new Prop("herb_planter",1,5,36));p.add(new Prop("herb_planter",10,5,36));
   p.add(new Prop("open_book",6,1,20,45,false));
  }
  if(k==Kind.INN){
   // Same independently placed counter modules as the existing shops.
   for(int u=3;u<=10;u++)p.add(new Prop("joined_counter",u,4,64));
   for(int v=2;v<4;v++){p.add(new Prop("joined_counter_sw",3,v,64));if(v==3)p.add(new Prop("joined_counter_sw",10,v,64));}
   p.add(new Prop("storage_barrel",1,2,35));p.add(new Prop("storage_barrel",2,2,35));
   dining(p,4,7,true);dining(p,9,8,true);dining(p,2,10,false);
   p.add(new Prop("inn_world_hearth",1,4,96,0,true,1,2));p.add(new Prop("inn_world_hearth",11,1,96,0,true,2,1));
   p.add(new Prop("herb_planter",1,6,32));p.add(new Prop("herb_planter",10,1,32));
   p.add(new Prop("storage_sack",11,10,25));p.add(new Prop("storage_sack",12,10,25));p.add(new Prop("storage_crate",12,6,32));
   p.add(new Prop("open_book",5,4,23,42,false));p.add(new Prop("bottle_tray",8,4,28,42,false));
  }
if(k==Kind.INN&&id.startsWith("abel_interior_")){p.clear();
 // A complete, explicitly adapted L counter encloses the keeper; discarded screenshot sliver.
 for(int u=7;u<=13;u++)p.add(new Prop("joined_counter",u,6,64));
 for(int v=2;v<6;v++)p.add(new Prop("joined_counter_sw",7,v,64));
 for(int i=0;i<AbelVideoLayout.INN_ASSETS.length;i++){String asset=AbelVideoLayout.INN_ASSETS[i];if(asset.equals("inn_counter_segment"))continue;float wx=480+(AbelVideoLayout.INN_PROPS[i][0]-310)*AbelVideoLayout.SCALE,wy=132+(AbelVideoLayout.INN_PROPS[i][1]+100)*AbelVideoLayout.SCALE;int tu=Math.max(1,Math.min(columns-2,Math.round(((wx-480)/32+(wy-132)/16)/2))),tv=Math.max(1,Math.min(rows-2,Math.round(((wy-132)/16-(wx-480)/32)/2)));boolean table=asset.startsWith("inn_table");p.add(new Prop("abel_video:"+i+":"+asset,table?tu-1:tu,table?tv-1:tv,table?120:32,0,true,table?2:1,table?2:1,wx,wy));}
 p.add(new Prop("inn_world_hearth",1,4,96,0,true,1,2));p.add(new Prop("storage_barrel",2,1,35));
 // Remaining observed dining-table locations reuse the supplied table family, not new art.
 p.add(new Prop("open_book",9,6,23,42,false));p.add(new Prop("bottle_tray",12,6,28,42,false));}
  props=Collections.unmodifiableList(p);
 }
 private static void dining(List<Prop> p,int u,int v,boolean food){
  p.add(new Prop("inn_world_table",u,v,96,0,true,2,1));
  p.add(new Prop("inn_world_chair_se",u-1,v,32));p.add(new Prop("inn_world_chair_nw",u+2,v,32));
  p.add(new Prop("inn_world_chair_sw",u,v-1,32));p.add(new Prop("inn_world_chair_ne",u+1,v+1,32));
  if(food)p.add(new Prop("bottle_tray",u,v,23,32,false,2,1));
 }
 public static final List<TownInteriorDef> ALL=Collections.unmodifiableList(Arrays.asList(
  new TownInteriorDef(Kind.REAGENT,"milles_interior_potion_shop","시약상점","멀린","mu0000020,mh172,ml230","potion_shop_door"),
  new TownInteriorDef(Kind.EQUIPMENT,"milles_interior_weapon_shop","장비상점","로건","mu0000016,mh001,ml230","weapon_shop_door"),
  new TownInteriorDef(Kind.BANK,"milles_interior_bank","밀레스 은행","은행원","mu0000026,mh004,ml230","general_shop_door"),
  new TownInteriorDef(Kind.CHURCH,"milles_interior_church","밀레스 성당","사제","mu0000015,mh003,ml230","church_door"),
  new TownInteriorDef(Kind.INN,"milles_interior_inn","밀레스 여관","메리 · 여관 주인","mu0000026,mh004,ml230","inn_door")));
    public static final List<TownInteriorDef> ABEL = Collections.unmodifiableList(Arrays.asList(new TownInteriorDef(Kind.REAGENT, "abel_interior_reagent", "아벨 시약상점", "시약상", "", "abel_reagent"), new TownInteriorDef(Kind.EQUIPMENT, "abel_interior_equipment", "아벨 무기상점", "무기상", "", "abel_equipment"), new TownInteriorDef(Kind.BANK, "abel_interior_bank", "아벨 은행", "은행원", "", "abel_bank"), new TownInteriorDef(Kind.CHURCH, "abel_interior_church", "아벨 교회", "사제", "", "abel_church"), new TownInteriorDef(Kind.INN, "abel_interior_inn", "아벨 여관", "여관 주인", "", "abel_inn")));
 public boolean isAbel(){return mapId.startsWith("abel_interior_");}
 public static TownInteriorDef forMap(String id){for(TownInteriorDef d:ALL)if(d.mapId.equals(id))return d;for(TownInteriorDef d:ABEL)if(d.mapId.equals(id))return d;return null;}
 public static TownInteriorDef forDoor(String id){if(id!=null&&id.endsWith("_2"))id=id.substring(0,id.length()-2);for(TownInteriorDef d:ALL)if(d.doorId.equals(id))return d;return null;}
 public static float x(int u,int v){return 480+32*(u-v);}public static float y(int u,int v){return 132+16*(u+v);}
 public float minX(){return isAbel()&&kind==Kind.INN?-64:MIN_X;}public float maxX(){return isAbel()&&kind==Kind.INN?1056:MAX_X;}public float maxY(){return isAbel()&&kind==Kind.INN?680:MAX_Y;}
 private int entryU(){return isAbel()&&kind==Kind.INN?13:kind==Kind.BANK?5:6;}
 public float spawnX(){return x(entryU(),rows-2);}public float spawnY(){return y(entryU(),rows-2);}public float exitX(){return x(entryU(),rows-1);}public float exitY(){return y(entryU(),rows-1);}
 public float npcX(){return x(npcU,npcV)-12;}public float npcY(){return y(npcU,npcV)-6;}public float customerX(){return x(customerU,customerV);}public float customerY(){return y(customerU,customerV);}
 public boolean blocked(int u,int v){if(u<=0||v<=0||u>=columns||v>=rows)return true;if(u==npcU&&v==npcV)return true;if((kind==Kind.REAGENT||kind==Kind.EQUIPMENT||kind==Kind.BANK)&&v==counterV&&u>=counterStart&&u<=counterEnd)return true;for(Prop p:props)if(p.occupies(u,v))return true;return false;}
 public List<WorldMoveTargetController.TileCenter> navigationTiles(){List<WorldMoveTargetController.TileCenter> t=new ArrayList<>();for(int u=1;u<columns;u++)for(int v=1;v<rows;v++)if(!blocked(u,v))t.add(new WorldMoveTargetController.TileCenter(x(u,v),y(u,v)));return Collections.unmodifiableList(t);}
 public List<RectF> obstacles(){List<RectF> a=new ArrayList<>();for(int u=0;u<columns;u++)for(int v=0;v<rows;v++)if(blocked(u,v))a.add(new RectF(x(u,v)-3,y(u,v)-3,x(u,v)+3,y(u,v)+3));return a;}
 public int floor(int u,int v){return kind==Kind.REAGENT?1:kind==Kind.CHURCH?(u>=5&&u<=7?3:2):0;}
}
