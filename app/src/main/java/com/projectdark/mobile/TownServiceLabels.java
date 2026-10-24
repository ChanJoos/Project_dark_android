package com.projectdark.mobile;
import android.graphics.*;
import com.projectdark.mobile.world.*;
import java.util.*;
/** Shared exterior signs at existing service entrances. */
final class TownServiceLabels {
 static final class Sign {final String name;final float x,y;Sign(String name,float x,float y){this.name=name;this.x=x;this.y=y;}}
 private static final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
 private static final Map<String,List<Sign>> cache=new HashMap<>();
 static String name(TownInteriorDef d){return d.title.replace("밀레스 ","").replace("아벨 ","").replace("뤼케시온 ","").replace("마인 ","");}
 static List<Sign> signs(String map){List<Sign> saved=cache.get(map);if(saved!=null)return saved;List<Sign> out=new ArrayList<>();
  if(WorldDef.ID.equals(map)){out.add(new Sign("밀레스 신전",TempleEntranceRenderer.X,TempleEntranceRenderer.Y+20));for(MillesDoorAnchors.Door door:MillesDoorAnchors.ALL){TownInteriorDef d=TownInteriorDef.forDoor(door.id);if(d!=null)out.add(new Sign(name(d),door.x,door.y));}}
  else if(AbelTownGeometry.contains(map)){for(AbelTownGeometry.Building b:AbelTownGeometry.BUILDINGS)out.add(new Sign(name(TownInteriorDef.forMap(b.interior)),b.doorX(),b.doorY()));}
  else if(MineTownGeometry.contains(map)){for(MineTownGeometry.Building b:MineTownGeometry.BUILDINGS)out.add(new Sign(name(TownInteriorDef.forMap(b.interior)),b.doorX(),b.doorY()));}
  else if(RucesionTownGeometry.contains(map)){for(RucesionTownGeometry.Building b:RucesionTownGeometry.BUILDINGS)out.add(new Sign(name(TownInteriorDef.forMap(b.interior)),b.doorX(),b.doorY()));}
  List<Sign> result=Collections.unmodifiableList(out);if(!out.isEmpty())cache.put(map,result);return result;
 }
 static void draw(Canvas c,String map,float ox,float oy,float vw,float vh){p.setTextSize(12);p.setTextAlign(Paint.Align.CENTER);p.setTypeface(UiTheme.font(true));for(Sign s:signs(map)){float x=s.x-ox,y=s.y-oy+24;if(x<0||x>vw||y<0||y>vh)continue;float half=p.measureText(s.name)/2+7;p.setColor(0xd9292721);c.drawRoundRect(x-half,y-13,x+half,y+4,4,4,p);p.setColor(0xffffe4a0);c.drawText(s.name,x,y,p);}}
 private TownServiceLabels(){}
}

