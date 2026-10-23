package com.projectdark.mobile;

import android.graphics.*;
import java.util.*;

/** Town travel presentation and shared hit geometry; destination authority stays in TownTravel. */
final class TownTravelWindow {
 static final String FOREST="MAP_POTE_01";
 static final RectF PANEL=new RectF(36,80,946,530);
 private static SourceTravelSkin skin;private static int rows=6;
 static void install(android.content.Context c){skin=new SourceTravelSkin(c);}
 private static final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
 static List<String> destinations(String current){
  List<String> out=new ArrayList<>();if(com.projectdark.mobile.world.HorrorCastleGeometry.contains(current)&&!com.projectdark.mobile.world.HorrorCastleGeometry.LOBBY.equals(current)){out.add(com.projectdark.mobile.world.HorrorCastleGeometry.LOBBY);return out;}
  TownTravel.Destination here=TownTravel.forMap(current);
  for(TownTravel.Destination d:TownTravel.TOWNS)if((here==null||!d.map.equals(here.map))&&TownTravel.offers(current,d.map))out.add(d.map);
  if(!com.projectdark.mobile.world.CampaignWorld.PIET.equals(current))out.add(hunting(current));if(WorldDef.ID.equals(current))out.add(com.projectdark.mobile.world.HorrorCastleGeometry.LOBBY);return out;
 }
 static String hunting(String current){
  if(com.projectdark.mobile.world.MineTownGeometry.contains(current))return com.projectdark.mobile.world.MineHuntGeometry.ID;
  if(com.projectdark.mobile.world.AbelTownGeometry.contains(current))return com.projectdark.mobile.world.AbelCoastGeometry.ENTRY;
  if(com.projectdark.mobile.world.RucesionTownGeometry.contains(current))return com.projectdark.mobile.world.RucesionCoastGeometry.ENTRY;
  return FOREST;
 }
 static boolean huntingMap(String map){return com.projectdark.mobile.world.MineHuntGeometry.ID.equals(map)||FOREST.equals(map)||com.projectdark.mobile.world.AbelCoastGeometry.ENTRY.equals(map)||com.projectdark.mobile.world.RucesionCoastGeometry.ENTRY.equals(map);}
 static RectF card(int i){float y=400-rows*43+i*43;return new RectF(686,y,942,y+37);}
 static RectF cardFor(String current,String map){List<String> ds=destinations(current);rows=ds.size();int i=ds.indexOf(map);return i<0?null:card(i);}
 static boolean close(float x,float y){return !PANEL.contains(x,y)||new RectF(42,502,230,536).contains(x,y);}
 static String hit(String current,float x,float y){List<String> ds=destinations(current);rows=ds.size();for(int i=0;i<ds.size();i++)if(card(i).contains(x,y))return ds.get(i);return null;}
 static void draw(Canvas c,String current,String guide){
  c.drawColor(0x99000000);List<String> ds=destinations(current);rows=ds.size();
  for(int i=0;i<ds.size();i++){RectF r=card(i);if(skin!=null)skin.frame(c,i==0?"choice-selected":"choice-idle",r);UiTheme.fit(c,title(ds.get(i)),r.left+13,r.top+24,r.width()-26,14,0xffeeeae2,false);}
  if(skin!=null)skin.portrait(c);UiTheme.text(c,"백원만",166,410,18,0xff70e29a,true);UiTheme.text(c,"이동을 원하시는 곳을 골라주세요!",166,440,15,0xfff2f0e8,false);UiTheme.text(c,"이동 요금은 100골드입니다.",166,464,14,0xffe5dfce,false);UiTheme.text(c,"〈  대화 끝내기",48,522,13,0xffe2d8c1,false);
 }

 static String title(String map){if(com.projectdark.mobile.world.MineHuntGeometry.ID.equals(map))return "카스마늄 광산";if(com.projectdark.mobile.world.HorrorCastleGeometry.LOBBY.equals(map))return "호러캐슬";return FOREST.equals(map)?"포테의 숲":com.projectdark.mobile.world.AbelCoastGeometry.ENTRY.equals(map)?"아벨해안":com.projectdark.mobile.world.RucesionCoastGeometry.ENTRY.equals(map)?"뤼케시온해안":TownTravel.forMap(map).title;}
 static String description(String map){if(com.projectdark.mobile.world.MineHuntGeometry.ID.equals(map))return "Lv99 · 3갱도 · 헬 장비 사냥";if(com.projectdark.mobile.world.HorrorCastleGeometry.LOBBY.equals(map))return "로비 · 1층/2층 복도 · 01~13 순차 사냥";return FOREST.equals(map)?"2써클 사냥터 (Lv 11~Lv 40)":com.projectdark.mobile.world.AbelCoastGeometry.ENTRY.equals(map)?"Lv 41 이상 · 포테 조사 완료":com.projectdark.mobile.world.RucesionCoastGeometry.ENTRY.equals(map)?"Lv 71 이상":"";}
 private TownTravelWindow(){}
}

