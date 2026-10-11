package com.projectdark.mobile;
import com.projectdark.mobile.world.*;
import java.util.*;

/** First coast admission: observed turtle and red octopus candidates, not an invented full canon roster. */
public final class AbelCoastMonsters {
 public static String name(String id){return "ABEL_TURTLE".equals(id)?"터틀":"ADAPTED_COAST_RED_OCTOPUS".equals(id)?"붉은 문어":null;}
 public static List<RuntimeState.Monster> spawn(String map){
  List<RuntimeState.Monster> out=new ArrayList<>();int area=AbelCoastGeometry.index(map);if(area<=0)return out;
  PoteCampaignMapDef d=PoteCampaignMapDef.forId(map);List<WorldMoveTargetController.TileCenter> clear=new ArrayList<>();
  for(WorldMoveTargetController.TileCenter t:CampaignWorld.connectedTiles(map)){
   if(t.x<448||Math.hypot(t.x-d.backX,t.y-d.backY)<260||Math.hypot(t.x-d.entryX,t.y-d.entryY)<210||Math.hypot(t.x-(d.entryX+96),t.y-(d.entryY-48))<100||Math.hypot(t.x-d.nextX,t.y-d.nextY)<120)continue;
   boolean open=true;for(WorldMoveTargetController.Direction step:WorldMoveTargetController.Direction.values())if(d.exactCenter(t.x+step.dx,t.y+step.dy)==null){open=false;break;}if(open)clear.add(t);
  }
  int count=area==1?72:area==2?96:108;
  for(int i=0;i<count;i++){
   float tx=280+(i%12)*(d.maxX-560)/11f,ty=200+(i/12)*(d.maxY*.58f-300)/(count/12-1f),best=Float.MAX_VALUE;WorldMoveTargetController.TileCenter at=null;
   for(WorldMoveTargetController.TileCenter t:clear){boolean close=false;for(RuntimeState.Monster m:out)if(Math.hypot(t.x-m.x,t.y-m.y)<90){close=true;break;}if(close)continue;float q=(t.x-tx)*(t.x-tx)+(t.y-ty)*(t.y-ty);if(q<best){best=q;at=t;}}
   if(at==null)throw new IllegalStateException("Abel spawn clearance "+map+" "+i);
   String species=i%2==0?"ABEL_TURTLE":"ADAPTED_COAST_RED_OCTOPUS";
   RuntimeState.Monster m=new RuntimeState.Monster(species+"#"+i,name(species),at.x,at.y,850+area*90,"B/ADAPTED_SOURCE_REFERENCED_CANDIDATE");m.campaignRewardProfileId=species;m.respawnSeconds=7+i%3;out.add(m);
  }
  return out;
 }
 private AbelCoastMonsters(){}
}
