package com.projectdark.mobile;

import android.graphics.*;
import java.util.*;

/** Town travel presentation and shared hit geometry; destination authority stays in TownTravel. */
final class TownTravelWindow {
 static final String FOREST="MAP_POTE_01";
 static final RectF PANEL=new RectF(250,135,710,427);
 private static final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
 static List<String> destinations(String current){
  List<String> out=new ArrayList<>();
  TownTravel.Destination here=TownTravel.forMap(current);
  for(TownTravel.Destination d:TownTravel.TOWNS)if(here==null||!d.map.equals(here.map))out.add(d.map);
  if(!com.projectdark.mobile.world.CampaignWorld.PIET.equals(current))out.add(FOREST);return out;
 }
 static RectF card(int i){float x=270,y=193+i*70;return new RectF(x,y,x+420,y+60);}
 static RectF cardFor(String current,String map){int i=destinations(current).indexOf(map);return i<0?null:card(i);}
 static boolean close(float x,float y){return !PANEL.contains(x,y)||new RectF(661,144,697,180).contains(x,y);}
 static String hit(String current,float x,float y){List<String> ds=destinations(current);for(int i=0;i<ds.size();i++)if(card(i).contains(x,y))return ds.get(i);return null;}
 static void draw(Canvas c,String current,String guide){
  c.drawColor(0x73100f13);
  p.setColor(0xff29251f);c.drawRoundRect(PANEL,8,8,p);
  p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1);p.setColor(0xffa68b5f);c.drawRoundRect(PANEL,8,8,p);p.setStyle(Paint.Style.FILL);
  UiTheme.text(c,"마을 이동",272,170,20,0xfff7ecd7,true);
  List<String> ds=destinations(current);
  for(int i=0;i<ds.size();i++){
   String map=ds.get(i);boolean forest=FOREST.equals(map);RectF r=card(i);
   p.setColor(0xff3b352c);c.drawRoundRect(r,5,5,p);
   p.setStyle(Paint.Style.STROKE);p.setColor(0xff74624c);c.drawRoundRect(r,5,5,p);p.setStyle(Paint.Style.FILL);
   UiTheme.text(c,title(map),r.left+18,r.top+(forest?25:36),17,0xfff4e8d3,true);
   if(forest)UiTheme.text(c,description(map),r.left+18,r.top+46,11,0xffbdb39f,false);
   UiTheme.right(c,"›",r.right-17,r.centerY()+7,24,0xffdfc28f,false);
  }
  UiTheme.close(c,679,162);
 }
 static String title(String map){return FOREST.equals(map)?"포테의 숲":TownTravel.forMap(map).title;}
 static String description(String map){return FOREST.equals(map)?"2써클 사냥터 (Lv 11~Lv 40)":"";}
 private TownTravelWindow(){}
}
