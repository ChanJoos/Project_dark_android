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
public class ClassicSkillReferenceTest {
 Context c;
 @Before public void setup(){c=RuntimeEnvironment.getApplication();c.getSharedPreferences("project_dark_f5m_v1",0).edit().clear().commit();F5mSaveStore.install(c);}
 @Test public void allLabelledReferencesAreRetainedAndCorrectedWithoutResurrectingExcludedSkills()throws Exception{
  JSONObject references=new JSONObject(new String(PresentationAssetBytes.read(c,"skill-presentation/classic/references.json"),StandardCharsets.UTF_8));assertEquals(85,references.getJSONArray("rows").length());
  SkillBook b=SkillBook.load(c);assertNull(b.get("SK_성직자_010"));assertEquals("경신공법",b.get("SK_무도가_033").name);assertEquals("아지토",b.get("SK_무도가_034").name);
  assertEquals(53,b.learningPolicy("SK_무도가_020").getJSONObject("stats").getInt("INT"));assertTrue(b.summary("SK_성직자_007").contains("그룹"));assertEquals(SkillActionContract.Pattern.GROUP,SkillActionContract.get("SK_성직자_007").pattern);
  assertEquals(SkillActionContract.Pattern.SELF,SkillActionContract.get("SK_성직자_017").pattern);assertEquals(3,SkillActionContract.get("SK_무도가_003").reach);
  assertFalse(SkillActionContract.get("SK_무도가_003").presentationAllowed());assertFalse(SkillActionContract.get("SK_성직자_025").presentationAllowed());
  assertEquals("마법",b.get("SK_무도가_020").kind);assertEquals("마법",b.get("SK_무도가_011").kind);
 }
 @Test public void everyCaptureChannelHasVerifiedSourceTimingPixelsAndTransparentFloor()throws Exception{
  ClassicSkillReference fx=new ClassicSkillReference(c);assertEquals(102,fx.effects.size());
  JSONObject doc=new JSONObject(new String(PresentationAssetBytes.read(c,"skill-presentation/classic/manifest.json"),StandardCharsets.UTF_8));assertFalse(doc.getBoolean("nativeArchivePixels"));
  JSONObject skills=doc.getJSONObject("skills");int channels=0;
  for(String id:fx.effects.keySet())for(ClassicSkillReference.Channel channel:fx.effects.get(id).values()){
   channels++;JSONObject j=skills.getJSONObject(id).getJSONObject("channels").getJSONObject(channel.key);CapturedSkillFx.Sequence s=channel.sequence;assertTrue(j.getString("path"),j.getString("path").matches("[A-Za-z0-9_./-]+"));
   assertEquals(j.getString("sourceSha256"),sha(java.nio.file.Files.readAllBytes(new File("../"+j.getString("sourceGif")).toPath())));assertEquals(j.getString("atlasSha256"),sha(PresentationAssetBytes.read(c,"skill-presentation/"+j.getString("path"))));
   if(j.has("sourceFrames")){
    JSONArray originals=j.getJSONArray("sourceFrames");assertTrue(originals.length()>0);assertTrue(s.visualCenter);
    for(int i=0;i<originals.length();i++){JSONObject original=originals.getJSONObject(i);assertEquals(original.getString("sha256"),sha(java.nio.file.Files.readAllBytes(new File("../"+original.getString("path")).toPath())));}
    assertTrue(j.getString("sourceTiming").startsWith("PROJECT_"));
   }
   assertEquals(0,s.frame(0));assertEquals(s.durations.length-1,s.frame(s.duration));assertTrue("No opaque floor corner: "+id,Color.alpha(s.atlas.getPixel(s.width-1,s.height-1))<32);assertTrue(s.duration>0);
  }assertEquals(143,channels);
 }
 @Test public void mageSourceMasksExcludeObservedCursorAndUnchangedFloor()throws Exception{
  ClassicSkillReference reference=new ClassicSkillReference(c);
  CapturedSkillFx.Sequence pravo=reference.channel("SK_마법사_053",false).sequence;
  for(int frame=0;frame<pravo.durations.length;frame++)for(int y=65;y<88;y++)for(int x=77;x<90;x++)
   assertEquals("Observed cursor must not enter any held frame",0,Color.alpha(pravo.atlas.getPixel(frame*pravo.width+x,y)));
  CapturedSkillFx.Sequence mark=reference.channel("SK_마법사_046",false).sequence;
  assertEquals("Brown floor corner is not dark sigil ink",0,Color.alpha(mark.atlas.getPixel(mark.width-1,mark.height-1)));
 }
 @Test public void productionInputSeparatesCasterAndRecipientChannelsAndNeverHealsEnemies()throws Exception{
  ClassicSkillReference reference=new ClassicSkillReference(c);
  for(String id:reference.effects.keySet()){
   SkillActionContract.Rule rule=SkillActionContract.get(id);if(!rule.presentationAllowed())continue;
   F5mSaveStore.install(c);GameView v=new GameView(c);v.layout(0,0,960,540);v.setSkillTestMode(true);RuntimeState s=field(v,"state");RuntimeState.Monster m=s.monsters().get(0);s.player().x=m.x-32;s.player().y=m.y-16;s.player().hp=5;s.player().mp=0;int before=m.hp;
   ((CombatController)field(v,"combat")).selectTarget(m);((com.projectdark.mobile.world.WorldRuntimeAdapter)field(v,"worldAdapter")).snapCameraToPlayer();
   Method use=GameView.class.getDeclaredMethod("useBookSkill",SkillBook.Entry.class);use.setAccessible(true);use.invoke(v,((SkillBook)field(v,"skillBook")).get(id));assertEquals(id,field(v,"activeSkillVisualId"));
   // Production drains queued ACTION_STARTED events in its frame tick, not in submitPlayer.
   Method tick=GameView.class.getDeclaredMethod("tickSkillCombat",float.class);tick.setAccessible(true);tick.invoke(v,0f);
   SkillVfxRenderer renderer=field(v,"skillVfx");ClassicSkillReference.Channel start=reference.channel(id,true);assertEquals(id,start==null?0:1,renderer.pulses.size());
   for(SkillVfxRenderer.Pulse pulse:renderer.pulses){assertTrue(pulse.caster);assertEquals("classic",pulse.sheet);assertEquals("player",pulse.anchor);}
   tick.invoke(v,rule.contact+.001f);
   ClassicSkillReference.Channel contact=reference.channel(id,false);List<SkillVfxRenderer.Pulse> targets=new ArrayList<>();for(SkillVfxRenderer.Pulse pulse:renderer.pulses)if(!pulse.caster&&!pulse.sheet.equals("impact"))targets.add(pulse);
   if(rule.pattern==SkillActionContract.Pattern.SCREEN&&contact!=null)assertTrue(id,targets.size()>=1);else assertEquals(id,contact==null?0:1,targets.size());for(SkillVfxRenderer.Pulse pulse:targets){assertEquals("classic",pulse.sheet);if(rule.pattern==SkillActionContract.Pattern.SCREEN)assertTrue(s.monsters().stream().anyMatch(monster->monster.id.equals(pulse.anchor)));else assertEquals(id,contact.casterAnchor||rule.selfAnchored()?"player":m.id,pulse.anchor);pulse.age=SkillFxAuditTest.sourcePeakAge(renderer,pulse);}
   if(rule.heal())assertEquals(id,before,m.hp);
   if(id.equals("SK_성직자_040")||id.startsWith("SK_마법사_")||id.equals("SK_성직자_013")||id.equals("SK_무도가_020")||id.equals("SK_성직자_003")||id.equals("SK_무도가_002")||id.equals("SK_성직자_005")){
    Bitmap image=Bitmap.createBitmap(960,540,Bitmap.Config.ARGB_8888);v.draw(new Canvas(image));File out=new File("build/reports/device-review/classic-"+id+".png");out.getParentFile().mkdirs();try(FileOutputStream f=new FileOutputStream(out)){image.compress(Bitmap.CompressFormat.PNG,100,f);}
   }
   renderer.tick(30f);assertTrue(renderer.pulses.isEmpty());
  }
 }
 private static String sha(byte[] b)throws Exception{StringBuilder s=new StringBuilder();for(byte v:MessageDigest.getInstance("SHA-256").digest(b))s.append(String.format(Locale.ROOT,"%02x",v&255));return s.toString();}
 @SuppressWarnings("unchecked") private static <T>T field(Object o,String n)throws Exception{Field f=o.getClass().getDeclaredField(n);f.setAccessible(true);return(T)f.get(o);}
}
