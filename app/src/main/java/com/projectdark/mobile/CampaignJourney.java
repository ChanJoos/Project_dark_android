package com.projectdark.mobile;
import com.projectdark.mobile.world.*;
import java.util.*;
/** Navigation plan only: actual movement, manager interactions and hunting gates remain in GameView. */
final class CampaignJourney {
 static String next(String from,String to){
  from=AbelTownGeometry.canonical(AbelCoastGeometry.canonical(from));to=AbelTownGeometry.canonical(AbelCoastGeometry.canonical(to));
  Map<String,List<String>> graph=new LinkedHashMap<>();
  for(String map:CampaignWorld.ROUTE){String previous=CampaignWorld.previous(map);if(previous!=null)edge(graph,map,previous);}
  edge(graph,WorldDef.ID,"MAP_POTE_01");edge(graph,AbelTownGeometry.ID,AbelCoastGeometry.ENTRY);
  for(String map:RucesionCoastGeometry.ROUTE){if(RucesionCoastGeometry.BOSS.equals(map))continue;edge(graph,map,CampaignWorld.previous(map));}
  for(TownTravel.Destination a:TownTravel.TOWNS)for(TownTravel.Destination z:TownTravel.TOWNS)if(a!=z)edge(graph,a.map,z.map);
  ArrayDeque<String> queue=new ArrayDeque<>();Map<String,String> seen=new HashMap<>();queue.add(from);seen.put(from,null);
  while(!queue.isEmpty()){String at=queue.remove();if(at.equals(to)){if(at.equals(from))return at;while(!from.equals(seen.get(at)))at=seen.get(at);return at;}for(String n:graph.getOrDefault(at,Collections.emptyList()))if(!seen.containsKey(n)){seen.put(n,at);queue.add(n);}}
  return null;
 }
 private static void edge(Map<String,List<String>> g,String a,String z){if(a==null||z==null)return;g.computeIfAbsent(a,k->new ArrayList<>()).add(z);g.computeIfAbsent(z,k->new ArrayList<>()).add(a);}
 private CampaignJourney(){}
}
