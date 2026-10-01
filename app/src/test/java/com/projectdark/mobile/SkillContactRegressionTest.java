package com.projectdark.mobile;
import static org.junit.Assert.*;
import android.content.Context;
import android.graphics.*;
import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.*;
/** Contact-onset regressions: advance real input clocks, never seek to a peak. */
@RunWith(RobolectricTestRunner.class) @Config(sdk=34,manifest=Config.NONE) @GraphicsMode(GraphicsMode.Mode.NATIVE)
public class SkillContactRegressionTest {
 Context c;
 @Before public void setup(){c=RuntimeEnvironment.getApplication();c.getSharedPreferences("project_dark_f5m_v1",0).edit().clear().commit();F5mSaveStore.install(c);}
 @Test public void damageAndVisibleRecipientEffectStartTogetherIncludingLethalDragon()throws Exception{
  for(String id:new String[]{"SK_무도가_020","SK_성직자_040","SK_공통_001","SK_전사_015","SK_전사_023"}){
   F5mSaveStore.install(c);GameView view=new GameView(c);view.layout(0,0,960,540);view.setSkillTestMode(true);
   RuntimeState state=field(view,"state");RuntimeState.Monster m=state.monsters().get(0);SkillActionContract.Rule rule=SkillActionContract.get(id);
   state.player().x=m.x-32*Math.max(1,rule.minReach);state.player().y=m.y-16*Math.max(1,rule.minReach);
   if(id.equals("SK_성직자_040"))m.hp=1;int before=m.hp;
   ((CombatController)field(view,"combat")).selectTarget(m);((com.projectdark.mobile.world.WorldRuntimeAdapter)field(view,"worldAdapter")).snapCameraToPlayer();
   Method use=GameView.class.getDeclaredMethod("useBookSkill",SkillBook.Entry.class);use.setAccessible(true);use.invoke(view,((SkillBook)field(view,"skillBook")).get(id));
   Method tick=GameView.class.getDeclaredMethod("tickSkillCombat",float.class);tick.setAccessible(true);
   float contact=id.equals("SK_공통_001")?CombatResolver.attackPrototype(((CombatController)field(view,"combat")).attackDef(),0).hitTime:rule.contact;
   tick.invoke(view,Math.max(0,contact-.01f));assertEquals(id+" must not damage before contact",before,m.hp);
   SkillVfxRenderer fx=field(view,"skillVfx");assertFalse(id+" no recipient before hit",fx.pulses.stream().anyMatch(p->!p.caster));save(view,id,"before");
   tick.invoke(view,.011f);assertTrue(id+" resolves damage at contact",m.hp<before);
   if(id.equals("SK_공통_001"))assertTrue("Basic is BODY-only",fx.pulses.isEmpty());
   else {
    SkillVfxRenderer.Pulse pulse=fx.pulses.stream().filter(p->!p.caster&&!p.sheet.equals("impact")&&p.anchor.equals(m.id)).findFirst().orElse(null);assertNotNull(id+" recipient at the same hit",pulse);assertEquals(0,pulse.age,0);
    if(id.equals("SK_성직자_040")||id.equals("SK_무도가_020")){
     ClassicSkillReference.Channel channel=new ClassicSkillReference(c).channel(id,false);assertFalse(channel.casterAnchor);
     assertTrue(id+" first frame must be visible",alphaPixels(channel.sequence,0)>100);
    }
   }
   save(view,id,"contact");float previous=0;
   for(float elapsed:new float[]{.04f,.08f,.16f,.32f,.60f,1.10f}){tick.invoke(view,elapsed-previous);previous=elapsed;save(view,id,String.format(Locale.ROOT,"%04dms",Math.round(elapsed*1000)));}
   if(id.equals("SK_성직자_040")){assertFalse(m.alive);assertFalse("No delayed dragon over a corpse",fx.pulses.stream().anyMatch(p->p.id.equals(id)&&p.sheet.equals("classic")));}
  }
 }
 @Test public void bothCrashersHavePeakEnergyBetweenActorsInEveryDirection()throws Exception{
  CapturedSkillFx captured=new CapturedSkillFx(c);long n=9000;
  for(String id:new String[]{"SK_전사_015","SK_전사_023"})for(float[] offset:new float[][]{{160,80},{-160,80},{160,-80},{-160,-80}}){
   SkillVfxRenderer fx=new SkillVfxRenderer(c,new SkillPresentationCatalog(c));
   SkillVfxRenderer.Anchors a=new SkillVfxRenderer.Anchors(){public float x(String actor){return actor.equals("player")?240:240+offset[0];}public float y(String actor){return actor.equals("player")?200:200+offset[1];}};
   CombatResolver.Definition d=new CombatResolver.Definition(id,CombatResolver.ActionKind.MAGIC,CombatResolver.ActionState.MAGIC,CombatResolver.EffectType.MAGIC_HIT,true,0,1,150,.24f,12);
   fx.consume(Collections.singletonList(new CombatResolver.Event(++n,n,CombatResolver.EventType.HIT_FEEDBACK,"player","monster",d,CombatResolver.InputMode.MANUAL,null,CombatResolver.HitSemantic.DAMAGE,0)),a);
   assertEquals(1,fx.pulses.size());SkillVfxRenderer.Pulse pulse=fx.pulses.get(0);pulse.age=peakAge(captured.get(id));
   Bitmap image=Bitmap.createBitmap(640,400,Bitmap.Config.ARGB_8888);fx.draw(new Canvas(image),a);double weight=0,x=0,y=0;
   for(int iy=0;iy<image.getHeight();iy++)for(int ix=0;ix<image.getWidth();ix++){int alpha=Color.alpha(image.getPixel(ix,iy));weight+=alpha;x+=ix*alpha;y+=iy*alpha;}
   assertTrue(weight>100);assertEquals((a.centerX("player")+a.centerX("monster"))/2,x/weight,2);assertEquals((a.centerY("player")+a.centerY("monster"))/2,y/weight,2);
   assertTrue(captured.get(id).contactMidpoint);write(image,"v79-midpoint-"+id+"-"+Math.round(offset[0])+"-"+Math.round(offset[1])+".png");
  }
 }
 @Test public void fourCurseCrownsAreIncludedAndRenderWithoutTheOldCutoff()throws Exception{
  ClassicSkillReference r=new ClassicSkillReference(c);
  for(String id:new String[]{"SK_마법사_002","SK_마법사_011","SK_마법사_024","SK_마법사_053"}){
   ClassicSkillReference.Channel ch=r.channel(id,false);CapturedSkillFx.Sequence s=ch.sequence;assertTrue("Full source crown crop",s.height>=88);
   int top=0;for(int y=0;y<15;y++)for(int x=0;x<s.width;x++)if(Color.alpha(s.atlas.getPixel(s.width+x,y))>20)top++;assertTrue(id+" crown exists above old crop",top>10);
   Bitmap image=Bitmap.createBitmap(240,200,Bitmap.Config.ARGB_8888);Canvas canvas=new Canvas(image);canvas.drawColor(0xff19231e);r.draw(canvas,ch,.081f,120,100);write(image,"v79-curse-"+id+".png");
  }
 }
 private static float peakAge(CapturedSkillFx.Sequence s){long best=-1;int elapsed=0,start=0,hold=0;for(int frame=0;frame<s.durations.length;frame++){long energy=0;for(int y=0;y<s.height;y++)for(int x=0;x<s.width;x++)energy+=Color.alpha(s.atlas.getPixel(frame%s.columns*s.width+x,frame/s.columns*s.height+y));if(energy>best){best=energy;start=elapsed;hold=s.durations[frame];}elapsed+=s.durations[frame];}return (start+hold*.5f)/1000f;}
 private static int alphaPixels(CapturedSkillFx.Sequence s,int frame){int count=0;for(int y=0;y<s.height;y++)for(int x=0;x<s.width;x++)if(Color.alpha(s.atlas.getPixel(frame%s.columns*s.width+x,frame/s.columns*s.height+y))>0)count++;return count;}
 private static void save(GameView view,String id,String moment)throws Exception{Bitmap b=Bitmap.createBitmap(960,540,Bitmap.Config.ARGB_8888);view.draw(new Canvas(b));write(b,"v79-contact-"+id+"-"+moment+".png");}
 private static void write(Bitmap b,String name)throws Exception{File f=new File("build/reports/device-review/"+name);f.getParentFile().mkdirs();try(FileOutputStream out=new FileOutputStream(f)){b.compress(Bitmap.CompressFormat.PNG,100,out);}}
 @SuppressWarnings("unchecked")private static <T>T field(Object o,String name)throws Exception{Field f=o.getClass().getDeclaredField(name);f.setAccessible(true);return(T)f.get(o);}
}
