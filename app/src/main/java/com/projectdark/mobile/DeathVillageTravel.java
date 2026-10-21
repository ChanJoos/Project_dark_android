package com.projectdark.mobile;
import com.projectdark.mobile.world.*;
/** Adjacent portal tiles share one arrival latch; leave the whole entrance before returning. */
final class DeathVillageTravel {
 private String map,blockedTo;
 void select(String id){map=id;blockedTo=null;}
 void arrived(String id,DeathVillageData.Gate exit){map=id;blockedTo=null;if(exit!=null)for(DeathVillageData.Gate g:DeathVillageGeometry.gates(id))if(g.u==exit.arrivalU&&g.v==exit.arrivalV){blockedTo=g.to;break;}}
 private float distance(DeathVillageData.Gate g,float x,float y){return (float)Math.hypot(x-DeathVillageGeometry.x(g.u,g.v),y-DeathVillageGeometry.y(g.u,g.v));}
 DeathVillageData.Gate at(String id,float x,float y){if(!id.equals(map)){arrived(id,null);for(DeathVillageData.Gate g:DeathVillageGeometry.gates(id))if(distance(g,x,y)<60){blockedTo=g.to;break;}return null;}if(blockedTo!=null){boolean clear=true;for(DeathVillageData.Gate g:DeathVillageGeometry.gates(id))if(g.to.equals(blockedTo)&&distance(g,x,y)<=60){clear=false;break;}if(clear)blockedTo=null;}for(DeathVillageData.Gate g:DeathVillageGeometry.gates(id))if(!g.to.equals(blockedTo)&&distance(g,x,y)<22)return g;return null;}
}
