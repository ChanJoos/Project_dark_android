package com.projectdark.mobile;
import com.projectdark.mobile.world.*;
import java.util.*;

/** Source-referenced coast encounters; mobile counts and placements remain adapted. */
public final class AbelCoastMonsters {
 public static final List<String> ALL=Collections.unmodifiableList(Arrays.asList("ABEL_TURTLE","ABEL_ARKPUS","ABEL_PONG","ABEL_CATERPILLAR","ABEL_GRAB","ABEL_OBS_SLATE","ABEL_OCTOPUS","ABEL_OBS_PURPLE_EGG","ABEL_GAS","ABEL_OBS_PURPLE_MAW","ABEL_BERSERKER","ABEL_KINGARK_FAKE","ABEL_KINGARK"));
 public static String name(String id){switch(id){case "ABEL_TURTLE":return "터틀";case "ABEL_CATERPILLAR":return "애벌레";case "ABEL_PONG":return "퐁퐁이";case "ABEL_ARKPUS":return "아크퍼스";case "ABEL_GRAB":return "그래브";case "ABEL_OBS_SLATE":return "회청색 몬스터";case "ABEL_OCTOPUS":return "문어";case "ABEL_OBS_PURPLE_EGG":return "보라";case "ABEL_GAS":return "가스";case "ABEL_OBS_PURPLE_MAW":return "튜터";case "ABEL_BERSERKER":return "광폭자";case "ABEL_KINGARK_FAKE":return "가짜 킹아크퍼스";case "ABEL_KINGARK":return "킹아크퍼스";default:return null;}}
 /** Observed zone families; unknown proper names remain provisional in source receipts. */
 public static List<String> roster(String map){
  switch(AbelCoastGeometry.index(map)){
   case 1:case 2:return Arrays.asList("ABEL_TURTLE","ABEL_ARKPUS","ABEL_PONG","ABEL_CATERPILLAR");
   case 3:return Arrays.asList("ABEL_TURTLE","ABEL_GRAB","ABEL_OBS_SLATE","ABEL_CATERPILLAR","ABEL_PONG");
   case 4:return Arrays.asList("ABEL_OCTOPUS","ABEL_OBS_PURPLE_EGG","ABEL_GAS","ABEL_OBS_PURPLE_MAW","ABEL_CATERPILLAR");
   case 5:return Arrays.asList("ABEL_OCTOPUS","ABEL_OBS_PURPLE_MAW","ABEL_GAS","ABEL_BERSERKER","ABEL_KINGARK_FAKE","ABEL_KINGARK");
   default:return Collections.emptyList();
  }
 }
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
   List<String> families=roster(map);
   String species=families.get((i+i/12)%families.size());
   // Final coast keeps 108 actors: three blue elites and one of each event
   // identity replace five ordinary slots. Fake/real share one source art set.
   if(area==5)species=i==count-1?"ABEL_KINGARK":i==count-2?"ABEL_KINGARK_FAKE":i>=count-5?"ABEL_BERSERKER":families.get((i+i/12)%3);
   int hp=species.startsWith("ABEL_KINGARK")?6000:species.equals("ABEL_BERSERKER")?3000:850+area*90;
   RuntimeState.Monster m=new RuntimeState.Monster(species+"#"+i,name(species),at.x,at.y,hp,"B/ADAPTED_SOURCE_REFERENCED_CANDIDATE");m.campaignRewardProfileId=species;m.respawnSeconds=species.startsWith("ABEL_KINGARK")?60:species.equals("ABEL_BERSERKER")?30:7+i%3;out.add(m);
  }
  return out;
 }
 private AbelCoastMonsters(){}
}
