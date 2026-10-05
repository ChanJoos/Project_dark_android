package com.projectdark.mobile;
import static org.junit.Assert.*;
import android.content.Context;
import android.graphics.*;
import java.io.*;
import java.lang.reflect.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import org.json.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.*;
/** Source sigil, real damage onset, recipient registration and animation lifetime. */
@RunWith(RobolectricTestRunner.class) @Config(sdk=34,manifest=Config.NONE) @GraphicsMode(GraphicsMode.Mode.NATIVE)
public class RagnarokSourceCaptureTest {
 Context c;static final String ID="SK_마법사_037";
 @Before public void setup(){c=RuntimeEnvironment.getApplication();c.getSharedPreferences("project_dark_f5m_v1",0).edit().clear().commit();F5mSaveStore.install(c);}
 @Test public void registeredFourCapturesHaveVisibleOnsetFlashAndTransparentScene()throws Exception{
  ClassicSkillReference fx=new ClassicSkillReference(c);assertNull(fx.channel(ID,true));ClassicSkillReference.Channel channel=fx.channel(ID,false);assertNotNull(channel);CapturedSkillFx.Sequence s=channel.sequence;
  JSONObject doc=new JSONObject(new String(PresentationAssetBytes.read(c,"skill-presentation/classic/manifest.json"),StandardCharsets.UTF_8));JSONObject j=doc.getJSONObject("skills").getJSONObject(ID).getJSONObject("channels").getJSONObject("RECIPIENT_CONTACT");
  assertEquals(4,j.getJSONArray("sourceFrames").length());assertTrue(j.getString("sourceTiming").startsWith("PROJECT_"));assertTrue(s.visualCenter);assertTrue(s.normalBlend);assertTrue(s.filterBitmap);assertFalse(channel.casterAnchor);assertEquals(.30f,s.scale,0);assertEquals(14,s.durations.length);
  long first=0,peak=0;int time=0;
  for(int frame=0;frame<s.durations.length;frame++){
   Bitmap b=Bitmap.createBitmap(200,200,Bitmap.Config.ARGB_8888);fx.draw(new Canvas(b),channel,time/1000f+.0001f,100,100);
   int[] box={200,200,-1,-1};long energy=0;
   for(int y=0;y<200;y++)for(int x=0;x<200;x++){
    int pixel=b.getPixel(x,y),a=Color.alpha(pixel);energy+=a;
    assertFalse("No baked green HP bar",a>70&&Color.green(pixel)>Color.red(pixel)*1.35&&Color.green(pixel)>Color.blue(pixel)*1.2);
    if(a>12){box[0]=Math.min(x,box[0]);box[1]=Math.min(y,box[1]);box[2]=Math.max(x,box[2]);box[3]=Math.max(y,box[3]);}
   }
   if(frame==0){first=energy;assertTrue("Source rings visible at age zero",first>10000);assertTrue(box[2]-box[0]>70);assertEquals(100,(box[0]+box[2])/2,2);assertEquals(100,(box[1]+box[3])/2,2);}
   peak=Math.max(peak,energy);assertEquals(0,Color.alpha(b.getPixel(0,0)));
   Bitmap review=Bitmap.createBitmap(200,200,Bitmap.Config.ARGB_8888);Canvas canvas=new Canvas(review);canvas.drawColor(0xff0f1117);canvas.drawBitmap(b,0,0,null);write(review,"v81-ragnarok-phase-"+frame+".png");b.recycle();review.recycle();time+=s.durations[frame];
  }
  assertTrue("Flash grows beyond onset rings",peak>first*2);assertEquals(.81f,s.duration,.001f);
 }
 @Test public void productionInputEmitsSourceAtDamageOnEveryHitRecipient()throws Exception{
  GameView view=new GameView(c);view.layout(0,0,960,540);view.setSkillTestMode(true);RuntimeState state=field(view,"state");RuntimeState.Monster m=state.monsters().get(0);m.hp=200;state.player().x=m.x-32;state.player().y=m.y-16;
  ((CombatController)field(view,"combat")).selectTarget(m);((com.projectdark.mobile.world.WorldRuntimeAdapter)field(view,"worldAdapter")).snapCameraToPlayer();Method use=GameView.class.getDeclaredMethod("useBookSkill",SkillBook.Entry.class);use.setAccessible(true);use.invoke(view,((SkillBook)field(view,"skillBook")).get(ID));
  assertEquals("CLASSIC_CAPTURE",((SkillPresentationCatalog)field(view,"skillPresentation")).get(ID).target);
  Method tick=GameView.class.getDeclaredMethod("tickSkillCombat",float.class);tick.setAccessible(true);int hp=m.hp;tick.invoke(view,.23f);assertEquals(hp,m.hp);SkillVfxRenderer fx=field(view,"skillVfx");assertFalse(fx.pulses.stream().anyMatch(p->p.id.equals(ID)&&p.sheet.equals("classic")));
  tick.invoke(view,.011f);assertTrue(m.hp<hp);Set<String> recipients=new HashSet<>();
  for(SkillVfxRenderer.Pulse pulse:fx.pulses)if(pulse.id.equals(ID)&&pulse.sheet.equals("classic")){assertFalse(pulse.caster);assertEquals(0,pulse.age,0);assertFalse(pulse.anchor.equals("player"));assertTrue(state.monsters().stream().anyMatch(n->n.id.equals(pulse.anchor)));assertTrue(recipients.add(pulse.anchor));}
  assertTrue(recipients.contains(m.id));save(view,"contact");float elapsed=0;
  for(float target:new float[]{.05f,.10f,.20f,.35f,.50f,.70f,.82f}){tick.invoke(view,target-elapsed);elapsed=target;save(view,"age-"+target);}
  assertFalse(fx.pulses.stream().anyMatch(p->p.id.equals(ID)));
 }
 private static void save(GameView v,String name)throws Exception{Bitmap b=Bitmap.createBitmap(960,540,Bitmap.Config.ARGB_8888);v.draw(new Canvas(b));write(b,"v81-ragnarok-live-"+name+".png");b.recycle();}
 private static void write(Bitmap b,String name)throws Exception{File f=new File("build/reports/device-review/"+name);f.getParentFile().mkdirs();try(FileOutputStream out=new FileOutputStream(f)){b.compress(Bitmap.CompressFormat.PNG,100,out);}}
 @SuppressWarnings("unchecked") private static <T>T field(Object o,String n)throws Exception{Field f=o.getClass().getDeclaredField(n);f.setAccessible(true);return(T)f.get(o);}
}
