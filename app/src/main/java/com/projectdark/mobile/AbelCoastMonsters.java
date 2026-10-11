package com.projectdark.mobile;
import com.projectdark.mobile.world.*;
import java.util.*;

/** Source-referenced coast encounters; mobile counts and placements remain adapted. */
public final class AbelCoastMonsters {
 public static String name(String id){switch(id){case "ABEL_TURTLE":return "터틀";case "ABEL_GAS":return "가스";case "ABEL_PONG":return "퐁퐁이";case "ABEL_ARKPUS":return "아크퍼스";default:return null;}}
 public static List<RuntimeState.Monster> spawn(String map){
  List<RuntimeState.Monster> out=new ArrayList<>();int area=AbelCoastGeometry.index(map);if(area<=0)return out;
  PoteCampaignMapDef d=PoteCampaignMapDef.forId(map);List<WorldMoveTargetController.TileCenter> clear=new ArrayList<>();
  for(WorldMoveTargetController.TileCenter t:CampaignWorld.connectedTiles(map)){
   if(t.x<448||Math.hypot(t.x-d.backX,t.y-d.backY)<260||Math.hypot(t.x-d.entryX,t.y-d.entryY)<210||Math.hypot(t.x-(d.entryX+96),t.y-(d.entryY-48))<100||Math.hypot(t.x-d.nextX,t.y-d.nextY)<120)continue;
   boolean open=true;for(WorldMoveTargetController.Direction step:WorldMoveTargetController.Direction.values())if(d.exactCenter(t.x+step.dx,t.y+step.dy)==null){open=false;break;}if(open)clear.add(t);
  }
  int count=area==1?72:area==2?96:108;
  Random scatter=new Random(14000L+area*137L);
  for(int i=0;i<count;i++){
   float tx=280+(i%12)*(d.maxX-560)/11f+scatter.nextInt(177)-88,ty=240+(i/12)*620/(count/12-1f)+scatter.nextInt(101)-50,best=Float.MAX_VALUE;WorldMoveTargetController.TileCenter at=null;
   for(WorldMoveTargetController.TileCenter t:clear){boolean close=false;for(RuntimeState.Monster m:out)if(Math.hypot(t.x-m.x,t.y-m.y)<90){close=true;break;}if(close)continue;float q=(t.x-tx)*(t.x-tx)+(t.y-ty)*(t.y-ty);if(q<best){best=q;at=t;}}
   if(at==null)throw new IllegalStateException("Abel spawn clearance "+map+" "+i);
   String[] roster={"ABEL_TURTLE","ABEL_ARKPUS","ABEL_PONG","ABEL_GAS"};
   String species=roster[(i+i/12)%4];
   RuntimeState.Monster m=new RuntimeState.Monster(species+"#"+i,name(species),at.x,at.y,(850+area*90),"B/ADAPTED_SOURCE_REFERENCED_CANDIDATE");m.campaignRewardProfileId=species;m.respawnSeconds=7+i%3;out.add(m);
  }
  return out;
 }
 private AbelCoastMonsters(){}
}
