package com.projectdark.mobile;
import com.projectdark.mobile.world.*;
/** NPC admission and map gate latch; no inherited Horror/party/ability restrictions. */
public final class CountessCastleTravel {
 public static final String GUARD="countess_entry_guard",ENVOY="countess_envoy";
 private String map;private boolean latched=true;
 public static String destination(String map,String npc){if(DeathVillageGeometry.LOBBY.equals(map)&&GUARD.equals(npc))return CountessCastleGeometry.EXTERIOR;if(CountessCastleGeometry.EXTERIOR.equals(map)&&ENVOY.equals(npc))return CountessCastleGeometry.UPPER;return null;}
 public void arrived(String id){map=id;latched=true;}
 public CountessCastleGeometry.Gate at(RuntimeState s){String id=s.currentMapId();if(!CountessCastleGeometry.contains(id))return null;if(!id.equals(map)){arrived(id);return null;}CountessCastleGeometry.Gate hit=null;for(CountessCastleGeometry.Gate g:CountessCastleGeometry.gates(id))if(Math.hypot(s.player().x-g.x,s.player().y-g.y)<22){hit=g;break;}if(hit==null){latched=false;return null;}if(latched)return null;latched=true;return hit;}
 private CountessCastleTravel(boolean unused){}public CountessCastleTravel(){}
}
