package com.projectdark.mobile;
import static org.junit.Assert.*;
import android.content.Context;
import android.graphics.*;
import java.io.*;
import java.lang.reflect.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import org.json.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.*;
@RunWith(RobolectricTestRunner.class) @Config(sdk=34,manifest=Config.NONE) @GraphicsMode(GraphicsMode.Mode.NATIVE)
public class CapturedSkillFxTest {
  private Context c;
  private static final String[] IDS={"SK_전사_015","SK_전사_013","SK_전사_023","SK_전사_022","SK_도적_020","SK_도적_027","SK_무도가_020","SK_무도가_021","SK_무도가_023"};
  @Before public void setup(){c=RuntimeEnvironment.getApplication();c.getSharedPreferences("project_dark_f5m_v1",0).edit().clear().commit();F5mSaveStore.install(c);}
  @Test public void nineExplicitIdsUseRetainedSourceBytesAndNonuniformFrameTimes()throws Exception{
    CapturedSkillFx fx=new CapturedSkillFx(c);assertEquals(9,fx.sequences.size());
    JSONObject manifest=new JSONObject(new String(PresentationAssetBytes.read(c,"skill-presentation/captured/manifest.json"),StandardCharsets.UTF_8));assertFalse(manifest.getBoolean("nativeArchivePixels"));
    JSONObject entries=manifest.getJSONObject("skills");
    for(String id:IDS){JSONObject entry=entries.getJSONObject(id);CapturedSkillFx.Sequence sequence=fx.get(id);assertNotNull(id,sequence);assertEquals("SCREEN",entry.getString("blend"));
      assertEquals(entry.getString("sourceSha256"),sha(java.nio.file.Files.readAllBytes(new File("../"+entry.getString("sourceGif")).toPath())));
      assertEquals(entry.getString("atlasSha256"),sha(PresentationAssetBytes.read(c,"skill-presentation/"+entry.getString("path"))));
      assertEquals(0,sequence.frame(0));assertEquals(1,sequence.frame(sequence.durations[0]/1000f+.001f));assertEquals(sequence.durations.length-1,sequence.frame(sequence.duration));
      // Fully transparent corner: no floor rectangle or opaque captured character layer.
      assertEquals(0,Color.alpha(sequence.atlas.getPixel(sequence.width-1,sequence.height-1)));
    }
    assertEquals(8,fx.get("SK_전사_015").durations.length);assertEquals(31,fx.get("SK_도적_020").durations.length);
  }
  @Test public void actualGameSubmissionUsesCaptureOnlyAtContactForLegacyWarriorAndRogue()throws Exception{
    for(String id:Arrays.copyOf(IDS,6)){F5mSaveStore.install(c);GameView v=new GameView(c);v.layout(0,0,960,540);v.setSkillTestMode(true);
      RuntimeState s=field(v,"state");RuntimeState.Monster m=s.monsters().get(0);s.player().x=m.x-32;s.player().y=m.y-16;s.player().mp=0;
      ((CombatController)field(v,"combat")).selectTarget(m);((com.projectdark.mobile.world.WorldRuntimeAdapter)field(v,"worldAdapter")).snapCameraToPlayer();
      Method use=GameView.class.getDeclaredMethod("useBookSkill",SkillBook.Entry.class);use.setAccessible(true);use.invoke(v,((SkillBook)field(v,"skillBook")).get(id));
      assertEquals(id,field(v,"activeSkillVisualId"));SkillVfxRenderer renderer=field(v,"skillVfx");assertTrue("no invented caster pulse",renderer.pulses.isEmpty());
      Method tick=GameView.class.getDeclaredMethod("tickSkillCombat",float.class);tick.setAccessible(true);tick.invoke(v,SkillActionContract.get(id).contact+.001f);
      assertEquals(1,renderer.pulses.size());SkillVfxRenderer.Pulse pulse=renderer.pulses.get(0);assertEquals("capture",pulse.sheet);assertEquals(RuntimeCombatSession.PLAYER_ID,pulse.anchor);assertFalse(pulse.caster);
      pulse.age=.16f;Bitmap image=Bitmap.createBitmap(960,540,Bitmap.Config.ARGB_8888);v.draw(new Canvas(image));File out=new File("build/reports/device-review/captured-"+id+".png");out.getParentFile().mkdirs();try(FileOutputStream stream=new FileOutputStream(out)){image.compress(Bitmap.CompressFormat.PNG,100,stream);}
      renderer.tick(pulse.duration);assertTrue(renderer.pulses.isEmpty());
    }
  }
  private static String sha(byte[] b)throws Exception{StringBuilder s=new StringBuilder();for(byte v:MessageDigest.getInstance("SHA-256").digest(b))s.append(String.format(Locale.ROOT,"%02x",v&255));return s.toString();}
  @SuppressWarnings("unchecked") private static <T>T field(Object o,String n)throws Exception{Field f=o.getClass().getDeclaredField(n);f.setAccessible(true);return(T)f.get(o);}
}
