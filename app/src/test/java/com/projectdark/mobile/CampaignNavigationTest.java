package com.projectdark.mobile;
import static org.junit.Assert.*;
import com.projectdark.mobile.world.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
/** All spatial targets use actual paths and actor collision on the shared World stack. */
@RunWith(RobolectricTestRunner.class) @Config(sdk=34,manifest=Config.NONE)
public class CampaignNavigationTest {
 @Test public void forestAndSafeCampSupportBothGatesAllNpcsAndAllSpecies(){
  for(String map:CampaignWorld.ROUTE){RuntimeState s=new RuntimeState();s.enterCampaignMap(map,false);WorldRuntimeAdapter w=adapter(s);assertTrue(PoteFieldDef.isNavigationCenter(s.player().x,s.player().y));assertFalse(s.blocked(s.player().x,s.player().y));
   if(map.equals(CampaignWorld.PIET))assertTrue(s.monsters().isEmpty());
   for(RuntimeState.Npc n:s.npcs()){assertFalse(map+" NPC terrain "+n.id,s.blocked(n.x,n.y));assertTrue(map+" NPC path "+n.id,walk(w,w.requestNpcApproach(n.id)));}
   for(RuntimeState.Monster m:s.monsters()){assertNotNull(new AdaptedCampaignRewardCatalog().find(m.campaignRewardProfileId));assertTrue(PoteForestMonsterShowcase.containsMonster(m.id));assertTrue(map+" monster path "+m.id,walk(w,w.requestMonsterApproach(m.id,CanonicalMeleeTileContract.REACH_DISTANCE)));}
   assertTrue(map+" forward",walk(w,w.requestGroundWorld(CampaignWorld.forward().x,CampaignWorld.forward().y)));assertTrue(map+" backward",walk(w,w.requestGroundWorld(CampaignWorld.backward().x,CampaignWorld.backward().y)));
   s.enterCampaignMap(map,true);assertTrue(Math.hypot(s.player().x-CampaignWorld.forward().x,s.player().y-CampaignWorld.forward().y)>70);
  }
 }
 @Test public void newTownMentorsAndCounselorHaveReachableApproachTiles(){RuntimeState s=new RuntimeState();WorldRuntimeAdapter w=new WorldRuntimeAdapter(s,960,540);for(RuntimeState.Npc n:s.npcs())if(n.id.startsWith("mentor_")||n.id.equals("milles_job_counselor"))assertTrue(n.id,walk(w,w.requestNpcApproach(n.id)));}
 static WorldRuntimeAdapter adapter(RuntimeState s){return new WorldRuntimeAdapter(s,960,540,PoteFieldDef.MIN_X,PoteFieldDef.MAX_X,PoteFieldDef.MIN_Y,PoteFieldDef.MAX_Y,PoteFieldDef.navigationTiles(),PoteFieldDef.obstacles(),true);}
 static boolean walk(WorldRuntimeAdapter w,WorldMoveTargetController.Snapshot first){WorldMoveTargetController.Status status=first.status;for(int i=0;i<2000&&status==WorldMoveTargetController.Status.MOVING;i++)status=w.tickNavigation(.18f).movement.status;if(status!=WorldMoveTargetController.Status.REACHED)System.out.println("PATH_FAIL "+first.targetEntityId+" status="+status+" goal="+first.targetX+","+first.targetY+" player="+w.runtime().player().x+","+w.runtime().player().y);return status==WorldMoveTargetController.Status.REACHED;}
}
