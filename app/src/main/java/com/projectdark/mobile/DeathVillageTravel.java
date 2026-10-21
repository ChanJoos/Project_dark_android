package com.projectdark.mobile;
import com.projectdark.mobile.world.*;
/** Arrival gate remains latched until the visible foot leaves it; continuous movement can activate gates. */
final class DeathVillageTravel {
 private String map;private DeathVillageData.Gate blocked;
 void choose(DeathVillageData.Gate gate){if(!gate.map.equals(map))arrived(gate.map,null);if(blocked==gate)blocked=null;}
 void arrived(String id,DeathVillageData.Gate exit){map=id;blocked=null;for(DeathVillageData.Gate g:DeathVillageGeometry.gates(id))if(exit!=null&&g.u==exit.arrivalU&&g.v==exit.arrivalV)blocked=g;}
 DeathVillageData.Gate at(String id,float x,float y){if(!id.equals(map)){arrived(id,null);for(DeathVillageData.Gate g:DeathVillageGeometry.gates(id))if(Math.hypot(x-DeathVillageGeometry.x(g.u,g.v),y-DeathVillageGeometry.y(g.u,g.v))<60){blocked=g;break;}return null;}if(blocked!=null&&Math.hypot(x-DeathVillageGeometry.x(blocked.u,blocked.v),y-DeathVillageGeometry.y(blocked.u,blocked.v))>60)blocked=null;for(DeathVillageData.Gate g:DeathVillageGeometry.gates(id))if(g!=blocked&&Math.hypot(x-DeathVillageGeometry.x(g.u,g.v),y-DeathVillageGeometry.y(g.u,g.v))<22)return g;return null;}
}
