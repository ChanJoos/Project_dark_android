package com.projectdark.mobile.world;
import android.graphics.RectF;
import java.util.*;
/** Authored tile graph and independent, foot-anchored props. All four scenes share World movement. */
public final class TownInteriorDef {
 public enum Kind { REAGENT,EQUIPMENT,BANK,CHURCH }
 public static final float MIN_X=64,MAX_X=896,MIN_Y=0,MAX_Y=540;
 public static final class Prop {public final String asset;public final int u,v;public final float width;public Prop(String a,int u,int v,float w){asset=a;this.u=u;this.v=v;width=w;}public float x(){return TownInteriorDef.x(u,v);}public float y(){return TownInteriorDef.y(u,v);}}
 public final Kind kind; public final String mapId,title,npcName,outfit,doorId;
 public final List<Prop> props;
 public final int npcU=7,npcV=2,customerU=7,customerV=5;
 private TownInteriorDef(Kind k,String id,String title,String npc,String outfit,String door){this.kind=k;mapId=id;this.title=title;npcName=npc;this.outfit=outfit;doorId=door;List<Prop> p=new ArrayList<>();
  for(int a=1;a<12;a+=2){p.add(new Prop(k==Kind.CHURCH?"church_wall_sw":"wall_sw",0,a,128));p.add(new Prop(k==Kind.CHURCH?"church_wall_se":k==Kind.EQUIPMENT?"wood_wall_se":"wall_se",a,0,128));}
  if(k!=Kind.CHURCH){for(int u=3;u<=9;u+=2)p.add(new Prop("counter_se",u,4,128));p.add(new Prop("counter_sw",2,2,128));p.add(new Prop("counter_corner",2,4,96));}
  if(k==Kind.REAGENT){p.add(new Prop("potion_cabinet",3,1,96));p.add(new Prop("potion_cabinet",9,1,96));p.add(new Prop("potion_table",2,6,96));p.add(new Prop("potion_table",10,2,96));}
  if(k==Kind.EQUIPMENT){p.add(new Prop("weapon_rack",3,1,100));p.add(new Prop("weapon_rack",9,1,100));p.add(new Prop("shield_rack",2,7,96));p.add(new Prop("sword_barrel",10,3,54));}
  if(k==Kind.BANK){p.add(new Prop("safe",3,1,96));p.add(new Prop("safe",10,1,96));p.add(new Prop("ledger",4,2,72));p.add(new Prop("ledger",10,6,72));}
  if(k==Kind.CHURCH){p.add(new Prop("altar",5,2,112));for(int v=5;v<=9;v+=2){p.add(new Prop("pew",3,v,96));p.add(new Prop("pew",9,v,96));}}
  props=Collections.unmodifiableList(p);
 }
 public static final List<TownInteriorDef> ALL=Collections.unmodifiableList(Arrays.asList(
  new TownInteriorDef(Kind.REAGENT,"milles_interior_potion_shop","시약상점","노비스","mu0000020,mh172,ml228","potion_shop_door"),
  new TownInteriorDef(Kind.EQUIPMENT,"milles_interior_weapon_shop","장비상점","로건","mu0000016,ml228","weapon_shop_door"),
  new TownInteriorDef(Kind.BANK,"milles_interior_bank","밀레스 은행","은행원","mu0000026,ml228","general_shop_door"),
  new TownInteriorDef(Kind.CHURCH,"milles_interior_church","밀레스 성당","사제","mu0000018,ml228","church_door")));
 public static TownInteriorDef forMap(String id){for(TownInteriorDef d:ALL)if(d.mapId.equals(id))return d;return null;}
 public static TownInteriorDef forDoor(String id){for(TownInteriorDef d:ALL)if(d.doorId.equals(id))return d;return null;}
 public static float x(int u,int v){return 480+32*(u-v);}public static float y(int u,int v){return 132+16*(u+v);}
 public float spawnX(){return x(6,10);}public float spawnY(){return y(6,10);}public float exitX(){return x(6,11);}public float exitY(){return y(6,11);}
 public float npcX(){return x(npcU,npcV);}public float npcY(){return y(npcU,npcV);}public float customerX(){return x(customerU,customerV);}public float customerY(){return y(customerU,customerV);}
 public boolean blocked(int u,int v){if(u==0||v==0)return true;if(u==npcU&&v==npcV)return true;if(kind!=Kind.CHURCH&&((v==4&&u>=2&&u<=10)||(u==2&&v<=4)))return true;for(Prop p:props)if(p.u==u&&p.v==v)return true;return false;}
 public List<WorldMoveTargetController.TileCenter> navigationTiles(){List<WorldMoveTargetController.TileCenter> t=new ArrayList<>();for(int u=1;u<12;u++)for(int v=1;v<12;v++)if(!blocked(u,v))t.add(new WorldMoveTargetController.TileCenter(x(u,v),y(u,v)));return Collections.unmodifiableList(t);}
 public List<RectF> obstacles(){List<RectF> a=new ArrayList<>();for(int u=0;u<12;u++)for(int v=0;v<12;v++)if(blocked(u,v))a.add(new RectF(x(u,v)-3,y(u,v)-3,x(u,v)+3,y(u,v)+3));return a;}
 public int floor(int u,int v){return kind==Kind.REAGENT?1:kind==Kind.CHURCH?(u>=5&&u<=7?3:2):0;}
}
