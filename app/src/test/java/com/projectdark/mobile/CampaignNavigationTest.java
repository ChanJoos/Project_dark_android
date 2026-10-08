package com.projectdark.mobile;
import static org.junit.Assert.*;
import com.projectdark.mobile.world.*;
import java.util.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
/** All spatial targets use actual paths and actor collision on the shared World stack. */
@RunWith(RobolectricTestRunner.class) @Config(sdk=34,manifest=Config.NONE)
public class CampaignNavigationTest {
 @Test public void lv11to41CampaignUsesOrdinaryMonsterPoolsAndSingleBoss(){
  Set<String> spawned=new HashSet<>();
  for(int zone=1;zone<=4;zone++)for(RuntimeState.Monster m:CampaignMonsters.spawn(zone)){
   assertFalse("strong variants are outside the selected forest campaign",m.id.contains("POTE_STRONG_"));
   assertFalse("the old elite gnoll is outside the selected forest campaign",m.id.contains("POTE_CAMPAIGN_ELITE_GNOLL"));
   spawned.add(m.campaignRewardProfileId);
  }
  assertEquals(new HashSet<>(Arrays.asList("POTE_RED","POTE_GREEN","POTE_PURPLE","POTE_SILVER")),species(1));
  assertEquals(new HashSet<>(Arrays.asList("POTE_TREANT","POTE_ANTLION","POTE_GNOLL","POTE_LYCAN")),species(2));
  assertEquals(new HashSet<>(Arrays.asList("POTE_WOLFRIDER","POTE_ANTGIANT","POTE_SILVERWOLF","POTE_SPIRIT")),species(3));
  AdaptedCampaignRewardCatalog rewards=new AdaptedCampaignRewardCatalog();for(String id:species(1))assertEquals("A reward metadata follows its assigned pool","포테 A · 입구 숲",rewards.find(id).zone);for(String id:species(2))assertEquals("B reward metadata follows its assigned pool","포테 B · 무리 숲",rewards.find(id).zone);for(String id:species(3))if(!"POTE_SPIRIT".equals(id))assertEquals("C reward metadata follows its assigned pool","포테 C · 깊은 숲",rewards.find(id).zone);
  assertEquals("D is a single-boss encounter",1,CampaignMonsters.spawn(4).size());assertEquals("POTE_MANTIS",CampaignMonsters.spawn(4).get(0).campaignRewardProfileId);assertTrue("final encounter has four-direction idle/walk/attack art",PoteForestMonsterShowcase.containsMonster("POTE_MANTIS#0"));
  for(CampaignProgress.Def d:CampaignProgress.definitions()){
   if(!d.kind.equals("KILL")&&!d.kind.equals("PAIR"))continue;
   for(String target:d.targets.split(","))if(target.startsWith("POTE_")){
    assertFalse("quest "+d.id+" must not require a strong monster",target.startsWith("POTE_STRONG_"));
    assertFalse("quest "+d.id+" must not require the old elite gnoll",target.equals("POTE_CAMPAIGN_ELITE_GNOLL"));
    assertTrue("quest "+d.id+" target must spawn in a campaign map: "+target,spawned.contains(target));
   }
  }
 }
 private Set<String> species(int zone){Set<String> ids=new HashSet<>();for(RuntimeState.Monster m:CampaignMonsters.spawn(zone))ids.add(m.campaignRewardProfileId);return ids;}
 @Test public void forestAndSafeCampSupportBothGatesAllNpcsAndAllSpecies(){
  for(String map:CampaignWorld.ROUTE){RuntimeState s=new RuntimeState();s.enterCampaignMap(map,false);PoteCampaignMapDef def=PoteCampaignMapDef.forId(map);WorldRuntimeAdapter w=adapter(s);WorldMoveTargetController.TileCenter start=def.nearest(s.player().x,s.player().y);assertNotNull(start);assertEquals(start.x,s.player().x,.01f);assertEquals(start.y,s.player().y,.01f);assertFalse(s.blocked(s.player().x,s.player().y));
   if(map.equals(CampaignWorld.PIET))assertTrue(s.monsters().isEmpty());
   for(RuntimeState.Npc n:s.npcs()){assertFalse(map+" NPC terrain "+n.id,s.blocked(n.x,n.y));assertTrue(map+" NPC path "+n.id,walk(w,w.requestNpcApproach(n.id)));}
   for(RuntimeState.Monster m:s.monsters()){assertTrue("registered reward profile "+m.id,new AdaptedCampaignRewardCatalog().find(m.campaignRewardProfileId)!=null||new CanonicalMonsterRewardCatalog().find(m.campaignRewardProfileId)!=null);assertTrue(map+" art "+m.id,PoteForestMonsterShowcase.containsMonster(m.id));assertTrue(map+" monster path "+m.id,walk(w,w.requestMonsterApproach(m.id,CanonicalMeleeTileContract.REACH_DISTANCE)));}
   assertTrue(map+" forward",walk(w,w.requestGroundWorld(CampaignWorld.forward(map).x,CampaignWorld.forward(map).y)));assertTrue(map+" backward",walk(w,w.requestGroundWorld(CampaignWorld.backward(map).x,CampaignWorld.backward(map).y)));
   s.enterCampaignMap(map,true);assertTrue(Math.hypot(s.player().x-CampaignWorld.forward(map).x,s.player().y-CampaignWorld.forward(map).y)>70);
  }
 }
 @Test public void previousMap04SaveDoesNotBecomeTheNewBossMap(){assertTrue(CampaignWorld.contains("MAP_POTE_04"));assertFalse(Arrays.asList(CampaignWorld.ROUTE).contains("MAP_POTE_04"));assertNotEquals(CampaignWorld.title("MAP_POTE_04"),CampaignWorld.title(CampaignWorld.BOSS_D));assertFalse(CampaignMonsters.spawnForMap("MAP_POTE_04").stream().anyMatch(m->"POTE_MANTIS".equals(m.campaignRewardProfileId)));assertEquals("POTE_MANTIS",CampaignMonsters.spawnForMap(CampaignWorld.BOSS_D).get(0).campaignRewardProfileId);}
 @Test public void newTownMentorsAndCounselorHaveReachableApproachTiles(){RuntimeState s=new RuntimeState();WorldRuntimeAdapter w=new WorldRuntimeAdapter(s,960,540);for(RuntimeState.Npc n:s.npcs())if(n.id.startsWith("mentor_")||n.id.equals("milles_job_counselor"))assertTrue(n.id,walk(w,w.requestNpcApproach(n.id)));}
 static WorldRuntimeAdapter adapter(RuntimeState s){PoteCampaignMapDef d=PoteCampaignMapDef.forId(s.currentMapId());return new WorldRuntimeAdapter(s,960,540,d.minX,d.maxX,d.minY,d.maxY,d.navigationTiles(),d.obstacles(),true);}
 static boolean walk(WorldRuntimeAdapter w,WorldMoveTargetController.Snapshot first){WorldMoveTargetController.Status status=first.status;for(int i=0;i<2000&&status==WorldMoveTargetController.Status.MOVING;i++)status=w.tickNavigation(.18f).movement.status;if(status!=WorldMoveTargetController.Status.REACHED)System.out.println("PATH_FAIL "+first.targetEntityId+" status="+status+" goal="+first.targetX+","+first.targetY+" player="+w.runtime().player().x+","+w.runtime().player().y);return status==WorldMoveTargetController.Status.REACHED;}
}
