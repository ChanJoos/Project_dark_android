package com.projectdark.mobile;
import com.projectdark.mobile.world.*;
import java.util.*;
/** Stable per-spawn IDs and admitted species profiles. New HP/placement values are ADAPTED. */
public final class CampaignMonsters {
 private static final String[][] POOLS={{"POTE_RED","POTE_GREEN"},{"POTE_PURPLE","POTE_SILVER","POTE_TREANT","POTE_ANTLION"},{"POTE_GNOLL","POTE_LYCAN","POTE_WOLFRIDER","POTE_ANTGIANT","POTE_STRONG_GNOLL","POTE_STRONG_TREANT"},{"POTE_SILVERWOLF","POTE_STRONG_GNOLL","POTE_STRONG_TREANT","POTE_STRONG_WOLFRIDER","POTE_CAMPAIGN_ELITE_GNOLL"}};
 public static List<RuntimeState.Monster> spawn(int zone){List<RuntimeState.Monster> out=new ArrayList<>();if(zone<1||zone>4)return out;PoteMonsterRoster roster=new PoteMonsterRoster();List<WorldMoveTargetController.TileCenter> tiles=CampaignWorld.connectedTiles();Set<String> centers=new HashSet<>();for(WorldMoveTargetController.TileCenter t:tiles)centers.add(Math.round(t.x)+":"+Math.round(t.y));String[] ids=POOLS[zone-1];
  for(int i=0;i<ids.length*2;i++){String species=ids[i%ids.length];WorldMoveTargetController.TileCenter pick=null;float best=Float.MAX_VALUE;float tx=600+(i%3)*150,ty=390+(i/3)*100;for(WorldMoveTargetController.TileCenter t:tiles){if(t.x>1056||t.y>768)continue;boolean open=true;for(WorldMoveTargetController.Direction direction:WorldMoveTargetController.Direction.values())if(!centers.contains(Math.round(t.x+direction.dx)+":"+Math.round(t.y+direction.dy))){open=false;break;}if(!open)continue;if(Math.hypot(t.x-PoteFieldDef.ENTRY_X,t.y-PoteFieldDef.ENTRY_Y)<100)continue;boolean near=false;for(RuntimeState.Monster m:out)if(Math.hypot(t.x-m.x,t.y-m.y)<90){near=true;break;}if(near)continue;float d=(float)Math.hypot(t.x-tx,t.y-ty);if(d<best){best=d;pick=t;}}if(pick==null)throw new IllegalStateException("Campaign spawn clearance");PoteMonsterRoster.Entry e=roster.find(species);String name=e==null?"정예 강력한 놀":e.name;int hp=new int[]{240,450,800,1200}[zone-1];if(species.equals("POTE_CAMPAIGN_ELITE_GNOLL"))hp=2200;RuntimeState.Monster m=new RuntimeState.Monster(species+"#"+i,name,pick.x,pick.y,hp,"B/ADAPTED_CAMPAIGN; retained candidate sprite");m.campaignRewardProfileId=species;m.respawnSeconds=24;out.add(m);}
  return out;
 }
 private CampaignMonsters(){}
}
