package com.projectdark.mobile;

import static org.junit.Assert.*;
import android.content.Context;
import com.projectdark.mobile.world.*;
import java.nio.file.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.*;

@RunWith(RobolectricTestRunner.class) @Config(sdk=34,manifest=Config.NONE)
public class QuickQuestLatencyTest {
  @Test public void firstAndRepeatedRealQuickQuestTouchesStayBelowFreezeBudget() throws Exception {
    Context c=RuntimeEnvironment.getApplication();
    for(String name:new String[]{"project_dark_f5m_v1","project_dark_journal_v1","project_dark_skill_test_v1"})c.getSharedPreferences(name,0).edit().clear().commit();
    F5mSaveStore.install(c);GameView v=new GameView(c);v.layout(0,0,960,540);
    long[] timings=new long[6];
    for(int i=0;i<timings.length;i++){
      long start=System.nanoTime();QuestJournalTest.gesture(v,80,150);timings[i]=(System.nanoTime()-start)/1000000;
      WorldRuntimeAdapter world=TownInteriorTest.field(v,"worldAdapter");
      assertEquals(WorldMoveTargetController.Status.MOVING,world.movement().snapshot().status);
      assertFalse((Boolean)TownInteriorTest.field(v,"questJournalOpen"));
      assertTrue("quick quest touch took "+timings[i]+"ms",timings[i]<750);
    }
    Path out=Paths.get("build/reports/device-review/v107-quickquest-timing.txt");Files.createDirectories(out.getParent());
    Files.write(out,("Real GameView DOWN+UP callback milliseconds: "+java.util.Arrays.toString(timings)+"\nRobolectric JVM timing; physical-phone acceptance pending.\n").getBytes(java.nio.charset.StandardCharsets.UTF_8));
  }
}
