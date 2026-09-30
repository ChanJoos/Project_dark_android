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
public class RogueSkillReferenceTest {
 Context c;
 @Before public void setup(){c=RuntimeEnvironment.getApplication();c.getSharedPreferences("project_dark_f5m_v1",0).edit().clear().commit();F5mSaveStore.install(c);}
 @Test public void correctedMagicUsesOverheadBodyForBothSexesAndEveryFacing()throws Exception{
  SkillBook book=SkillBook.load(c);SkillPresentationCatalog catalog=new SkillPresentationCatalog(c);
  for(String id:new String[]{"SK_도적_011","SK_도적_012","SK_도적_013","SK_도적_016","SK_도적_017","SK_도적_026"}){
   assertEquals(id,"마법",book.get(id).kind);assertEquals(id,"CAST",catalog.get(id).motion);
   for(String body:new String[]{"mm001","wm001"})for(CharacterRenderer.Direction direction:CharacterRenderer.Direction.values()){
    String key=catalog.frameKey(body,"CAST",direction,.4f);assertTrue(key,key.contains("/f/"));assertTrue(key,catalog.frames.has(key));
   }
  }
  assertTrue(book.summary("SK_도적_026").contains("다른 유저"));
  assertFalse(SkillActionContract.get("SK_도적_012").presentationAllowed());
 }
 @Test public void everyCaptureChannelHasVerifiedSourceTimingPixelsAndTransparentFloor()throws Exception{
  ClassicSkillReference fx=new ClassicSkillReference(c,"rogue");assertEquals(15,fx.effects.size());
  JSONObject doc=new JSONObject(new String(PresentationAssetBytes.read(c,"skill-presentation/rogue/manifest.json"),StandardCharsets.UTF_8));assertFalse(doc.getBoolean("nativeArchivePixels"));
  JSONObject skills=doc.getJSONObject("skills");int channels=0;
  for(String id:fx.effects.keySet())for(ClassicSkillReference.Channel channel:fx.effects.get(id).values()){
   channels++;JSONObject j=skills.getJSONObject(id).getJSONObject("channels").getJSONObject(channel.key);CapturedSkillFx.Sequence s=channel.sequence;assertTrue(j.getString("path"),j.getString("path").matches("[A-Za-z0-9_./-]+"));
   assertEquals(j.getString("sourceSha256"),sha(java.nio.file.Files.readAllBytes(new File("../"+j.getString("sourceGif")).toPath())));assertEquals(j.getString("atlasSha256"),sha(PresentationAssetBytes.read(c,"skill-presentation/"+j.getString("path"))));
   assertEquals(0,s.frame(0));assertEquals(s.durations.length-1,s.frame(s.duration));assertEquals(0,Color.alpha(s.atlas.getPixel(s.width-1,s.height-1)));assertTrue(s.duration>0);
  }assertEquals(9,channels);
 }
 @Test public void productionInputSeparatesCasterAndRecipientChannelsAndNeverHealsEnemies()throws Exception{
  ClassicSkillReference reference=new ClassicSkillReference(c,"rogue");
  for(String id:reference.effects.keySet()){
   SkillActionContract.Rule rule=SkillActionContract.get(id);if(!rule.presentationAllowed())continue;
   F5mSaveStore.install(c);GameView v=new GameView(c);v.layout(0,0,960,540);v.setSkillTestMode(true);RuntimeState s=field(v,"state");RuntimeState.Monster m=s.monsters().get(0);s.player().x=m.x-32;s.player().y=m.y-16;s.player().hp=5;s.player().mp=0;int before=m.hp;
   ((CombatController)field(v,"combat")).selectTarget(m);((com.projectdark.mobile.world.WorldRuntimeAdapter)field(v,"worldAdapter")).snapCameraToPlayer();
   Method use=GameView.class.getDeclaredMethod("useBookSkill",SkillBook.Entry.class);use.setAccessible(true);use.invoke(v,((SkillBook)field(v,"skillBook")).get(id));assertEquals(id,field(v,"activeSkillVisualId"));
   // Production drains queued ACTION_STARTED events in its frame tick, not in submitPlayer.
   Method tick=GameView.class.getDeclaredMethod("tickSkillCombat",float.class);tick.setAccessible(true);tick.invoke(v,0f);
   SkillVfxRenderer renderer=field(v,"skillVfx");ClassicSkillReference.Channel start=reference.channel(id,true);assertEquals(id,start==null?0:1,renderer.pulses.size());
   for(SkillVfxRenderer.Pulse pulse:renderer.pulses){assertTrue(pulse.caster);assertEquals("rogue",pulse.sheet);assertEquals("player",pulse.anchor);}
   tick.invoke(v,rule.contact+.001f);
   ClassicSkillReference.Channel contact=reference.channel(id,false);List<SkillVfxRenderer.Pulse> targets=new ArrayList<>();for(SkillVfxRenderer.Pulse pulse:renderer.pulses)if(!pulse.caster)targets.add(pulse);
   assertEquals(id,contact==null?0:1,targets.size());for(SkillVfxRenderer.Pulse pulse:targets){assertEquals("rogue",pulse.sheet);assertEquals(id,contact.casterAnchor||rule.selfAnchored()?"player":m.id,pulse.anchor);pulse.age=Math.min(.16f,pulse.duration/2);}
   if(rule.heal())assertEquals(id,before,m.hp);
   if(contact!=null){
    Bitmap image=Bitmap.createBitmap(960,540,Bitmap.Config.ARGB_8888);v.draw(new Canvas(image));File out=new File("build/reports/device-review/rogue-"+id+".png");out.getParentFile().mkdirs();try(FileOutputStream f=new FileOutputStream(out)){image.compress(Bitmap.CompressFormat.PNG,100,f);}
   }
   renderer.tick(30f);assertTrue(renderer.pulses.isEmpty());
  }
 }
 private static String sha(byte[] b)throws Exception{StringBuilder s=new StringBuilder();for(byte v:MessageDigest.getInstance("SHA-256").digest(b))s.append(String.format(Locale.ROOT,"%02x",v&255));return s.toString();}
 @SuppressWarnings("unchecked") private static <T>T field(Object o,String n)throws Exception{Field f=o.getClass().getDeclaredField(n);f.setAccessible(true);return(T)f.get(o);}
}
