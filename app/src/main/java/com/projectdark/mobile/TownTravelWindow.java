package com.projectdark.mobile;

import android.graphics.*;
import java.util.*;

/** Town travel presentation and shared hit geometry; destination authority stays in TownTravel. */
final class TownTravelWindow {
 static final String FOREST="MAP_POTE_01";
 static final RectF PANEL=new RectF(180,126,780,454);
 private static final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
 static List<String> destinations(String current){
  List<String> out=new ArrayList<>();
  TownTravel.Destination here=TownTravel.forMap(current);
  for(TownTravel.Destination d:TownTravel.TOWNS)if(here==null||!d.map.equals(here.map))out.add(d.map);
  out.add(FOREST);return out;
 }
 static RectF card(int i){float x=206+(i%2)*280,y=235+(i/2)*88;return new RectF(x,y,x+268,y+76);}
 static RectF cardFor(String current,String map){int i=destinations(current).indexOf(map);return i<0?null:card(i);}
 static boolean close(float x,float y){return !PANEL.contains(x,y)||new RectF(732,138,768,174).contains(x,y);}
 static String hit(String current,float x,float y){List<String> ds=destinations(current);for(int i=0;i<ds.size();i++)if(card(i).contains(x,y))return ds.get(i);return null;}
 static void draw(Canvas c,String current,String guide){
  c.drawColor(0x73100f13);
  p.setColor(0x70000000);c.drawRoundRect(new RectF(183,133,783,461),12,12,p);
  p.setShader(new LinearGradient(0,126,0,454,0xff303238,0xff1c1e24,Shader.TileMode.CLAMP));c.drawRoundRect(PANEL,10,10,p);p.setShader(null);
  p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1);p.setColor(0xffa68b5f);c.drawRoundRect(PANEL,10,10,p);p.setStyle(Paint.Style.FILL);
  UiTheme.text(c,"마을 이동",207,163,22,0xfff7ecd7,true);
  UiTheme.text(c,guide,207,185,11,0xffbdaf96,false);
  UiTheme.line(c,206,200,754,200,0xff5c5345);
  UiTheme.text(c,"어디로 떠나시겠어요?",207,220,12,0xffddd6c9,false);
  UiTheme.right(c,"이동 비용  무료",752,220,11,0xffe1c187,true);
  List<String> ds=destinations(current);
  for(int i=0;i<ds.size();i++){
   String map=ds.get(i);boolean forest=FOREST.equals(map);TownTravel.Destination d=TownTravel.forMap(map);RectF r=card(i);
   p.setShader(new LinearGradient(0,r.top,0,r.bottom,0xff3c3c3e,0xff292b30,Shader.TileMode.CLAMP));c.drawRoundRect(r,7,7,p);p.setShader(null);
   p.setStyle(Paint.Style.STROKE);p.setColor(0xff74624c);c.drawRoundRect(r,7,7,p);p.setStyle(Paint.Style.FILL);
   p.setColor(forest?0xff394b3e:0xff4b4540);c.drawCircle(r.left+31,r.centerY(),19,p);
   UiTheme.center(c,String.format(Locale.ROOT,"%02d",i+1),r.left+31,r.centerY()+5,13,0xffdfc28f,true);
   UiTheme.text(c,forest?"포테의 숲":d.title,r.left+60,r.top+31,17,0xfff4e8d3,true);
   String detail=forest?"숲길 · 사냥터":WorldDef.ID.equals(map)?"모험의 시작 마을":map.equals("MAP_PIET_CAMP")?"모험가 거점":"항구 마을";
   UiTheme.text(c,detail,r.left+60,r.top+52,10,0xffb5b2aa,false);
   p.setColor(0xffdfc28f);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1.7f);Path arrow=new Path();arrow.moveTo(r.right-24,r.centerY()-5);arrow.lineTo(r.right-19,r.centerY());arrow.lineTo(r.right-24,r.centerY()+5);c.drawPath(arrow,p);p.setStyle(Paint.Style.FILL);
  }
  UiTheme.center(c,"목적지를 누르면 바로 이동합니다",480,433,10,0xffaaa69e,false);
  UiTheme.close(c,750,156);
 }
 private TownTravelWindow(){}
}
