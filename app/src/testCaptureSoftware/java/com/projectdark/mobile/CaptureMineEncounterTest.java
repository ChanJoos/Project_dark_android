package com.projectdark.mobile;
import com.projectdark.mobile.world.*;
import org.junit.*;import org.junit.runner.RunWith;import org.robolectric.*;import org.robolectric.annotation.*;
import java.util.*;import java.io.*;import static org.junit.Assert.*;
@RunWith(RobolectricTestRunner.class) @Config(sdk=34,manifest=Config.NONE) @GraphicsMode(GraphicsMode.Mode.NATIVE)
public class CaptureMineEncounterTest {
  FilmingProfileExportTest f;
  @Before public void setup(){f=new FilmingProfileExportTest();f.clean();}
  @Test public void fifteenExistingProfilesAreAliveVisibleUnmodifiedAndCollisionValid()throws Exception{
    GameView v=f.setup("MAGE");RuntimeState s=CaptureMineEncounter.state(v);Map<String,String> gear=new LinkedHashMap<>(s.rpg().equipment());
    String report=CaptureMineEncounter.stage(v).toString(2);assertEquals(15,s.monsters().size());
    WorldRuntimeAdapter world=(WorldRuntimeAdapter)CaptureMineEncounter.field(v,"poteFieldAdapter");
    MineHuntRenderer renderer=(MineHuntRenderer)CaptureMineEncounter.field(v,"mineHuntRenderer");
    List<RuntimeState.Monster> normal=MineHuntMonsters.spawn();
    for(int i=0;i<15;i++){
      RuntimeState.Monster m=s.monsters().get(i),before=normal.get(i);assertTrue(m.alive);assertEquals(before.maxHp,m.hp);assertEquals(before.id,m.id);assertEquals(before.level,m.level);assertEquals(before.campaignRewardProfileId,m.campaignRewardProfileId);assertEquals(before.respawnSeconds,m.respawnSeconds,0);assertEquals(before.defenseElement,m.defenseElement);assertEquals(before.attackElement,m.attackElement);assertFalse(m.provoked);
      assertTrue(MineHuntGeometry.inside(m.x,m.y));assertFalse(s.blocked(m.x,m.y));assertTrue(Math.hypot(m.x-s.player().x,m.y-s.player().y)>=27);
      for(RuntimeState.Monster other:s.monsters())if(other!=m)assertTrue(Math.hypot(m.x-other.x,m.y-other.y)>=29);
      WorldCameraTransform.Point q=world.worldToScreen(m.x,m.y);assertTrue("horizontal "+m.id,q.x>=50&&q.x<=910);assertTrue("body top "+m.id+" y="+q.y+" height="+renderer.bodyHeight(m.id),q.y-renderer.bodyHeight(m.id)>=32);assertTrue("ground visible",q.y<400);
    }
    assertEquals(gear,s.rpg().equipment());assertEquals(255,s.rpg().str());assertEquals(255,s.rpg().intel());assertEquals(255,s.rpg().wis());assertEquals(255,s.rpg().con());assertEquals(255,s.rpg().dex());assertEquals(1000000,s.player().hp);assertEquals(1000000,s.player().mp);assertEquals(1000000,s.player().maxHp);assertEquals(1000000,s.player().maxMp);assertEquals(99,s.rpg().normalLevel().intValue());assertTrue(s.rpg().firstAdvanced());
    f.f.shot(v,"capture-fifteen-paused-initial");File file=new File("build/reports/horror-v179/capture-encounter.json");file.getParentFile().mkdirs();try(FileWriter out=new FileWriter(file)){out.write(report);}
    // Normal ticking remains active when started; no artificial immobilization or invulnerability.
    float[] positions=new float[30];for(int i=0;i<15;i++){positions[i*2]=s.monsters().get(i).x;positions[i*2+1]=s.monsters().get(i).y;}
    MonsterAIController ai=new MonsterAIController();for(int i=0;i<30;i++){s.tick(.05f);ai.tick(s,.05f);}
    boolean moved=false;for(int i=0;i<15;i++)if(s.monsters().get(i).x!=positions[i*2]||s.monsters().get(i).y!=positions[i*2+1])moved=true;assertTrue("real AI runs",moved);
  }
  @Test public void ordinaryMineEntryStillHasHundredAndOriginalArrival(){RuntimeState s=new RuntimeState();s.enterCampaignMap(MineHuntGeometry.ID,false);assertEquals(100,s.monsters().size());assertEquals(118,MineHuntGeometry.sourceU(s.player().x,s.player().y));assertEquals(4,MineHuntGeometry.sourceV(s.player().x,s.player().y));}
  @Test public void rogueCannotAccidentallyActivateMageEncounter()throws Exception{GameView v=f.setup("ROGUE");RuntimeState s=CaptureMineEncounter.state(v);try{CaptureMineEncounter.stage(v);fail("rogue should reject");}catch(IllegalStateException expected){}assertEquals(MineTownGeometry.ID,s.currentMapId());assertEquals(0,s.monsters().size());}
}
