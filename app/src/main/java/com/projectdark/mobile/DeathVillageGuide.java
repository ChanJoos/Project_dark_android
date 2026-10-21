package com.projectdark.mobile;
import android.graphics.*;
import com.projectdark.mobile.world.*;
import java.util.List;
/** Visible destinations use the normal collision-aware walking route and real entrance triggers. */
final class DeathVillageGuide {
 private final RectF bounds=new RectF();
 static String label(DeathVillageData.Gate gate){
  if(DeathVillageGeometry.FIELD.equals(gate.to))return DeathVillageGeometry.LOBBY.equals(gate.map)?"야외 입장":"야외로 나가기";
  if(DeathVillageGeometry.LOBBY.equals(gate.to))return "대기실";
  if(gate.to.startsWith("MAP_DEATH_HOUSE_"))return "건물 "+gate.to.substring(gate.to.length()-1);
  return "지하 "+gate.to.substring(gate.to.length()-1);
 }
 RectF button(int index,int count,float offset){float width=360f/count,left=300+offset+index*width;bounds.set(left+2,80,left+width-2,116);return bounds;}
 void draw(Canvas canvas,String map,float offset){
  List<DeathVillageData.Gate> gates=DeathVillageGeometry.gates(map);
  UiTheme.center(canvas,DeathVillageGeometry.LOBBY.equals(map)?"죽음의마을 · 아래 버튼으로 야외 입구까지 이동":"목적지 터치 · 문과 계단까지 걸어서 이동",480+offset,72,10,UiTheme.GOLD,true);
  for(int i=0;i<gates.size();i++)UiTheme.button(canvas,button(i,gates.size(),offset),label(gates.get(i)),true,false);
 }
 DeathVillageData.Gate hit(String map,float x,float y,float offset){List<DeathVillageData.Gate> gates=DeathVillageGeometry.gates(map);for(int i=0;i<gates.size();i++)if(button(i,gates.size(),offset).contains(x,y))return gates.get(i);return null;}
}
