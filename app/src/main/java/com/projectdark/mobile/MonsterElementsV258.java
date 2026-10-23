package com.projectdark.mobile;
import java.util.concurrent.ThreadLocalRandom;
import org.json.*;
/** One attack/defense pair per living instance; U natural policy, source-backed mine exceptions. */
public final class MonsterElementsV258 {
 private static final String[] NATURAL={"WATER","EARTH","WIND","FIRE"};
 private MonsterElementsV258(){}
 public static boolean darkMine(String id){if(id==null||!id.startsWith("MINE_")||id.length()<7)return false;try{int t=Integer.parseInt(id.substring(5,7));return t>=4&&t<=9;}catch(NumberFormatException e){return false;}}
 public static boolean natural(String e){return "WATER".equals(e)||"EARTH".equals(e)||"WIND".equals(e)||"FIRE".equals(e);}
 static void spawn(RuntimeState.Monster m){assign(m,ThreadLocalRandom.current().nextInt(4),ThreadLocalRandom.current().nextInt(4));}
 static void assign(RuntimeState.Monster m,int attackRoll,int defenseRoll){
  if(darkMine(m.id)){m.attackElement="DARK";m.defenseElement="DARK";}
  else{m.attackElement=NATURAL[Math.floorMod(attackRoll,4)];m.defenseElement=NATURAL[Math.floorMod(defenseRoll,4)];}
 }
 static JSONObject snapshot(RuntimeState s){
  try{JSONArray rows=new JSONArray();for(RuntimeState.Monster m:s.monsters())rows.put(new JSONObject().put("id",m.id).put("attack",m.attackElement).put("defense",m.defenseElement));
   return new JSONObject().put("version",1).put("map",s.currentMapId()).put("actors",rows);
  }catch(JSONException e){throw new IllegalStateException(e);}
 }
 static void restore(RuntimeState s,JSONObject data){
  if(data==null||data.optInt("version")!=1||!s.currentMapId().equals(data.optString("map")))return;
  JSONArray rows=data.optJSONArray("actors");if(rows==null)return;
  java.util.Map<String,RuntimeState.Monster> actors=new java.util.HashMap<>();for(RuntimeState.Monster m:s.monsters())actors.put(m.id,m);
  for(int i=0;i<rows.length();i++){JSONObject row=rows.optJSONObject(i);if(row==null)continue;RuntimeState.Monster m=actors.get(row.optString("id"));if(m==null)continue;
   String a=row.optString("attack"),d=row.optString("defense");
   if(!darkMine(m.id)&&natural(a)&&natural(d)){m.attackElement=a;m.defenseElement=d;}
  }
 }
}
