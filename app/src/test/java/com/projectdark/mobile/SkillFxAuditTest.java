package com.projectdark.mobile;
import static org.junit.Assert.*;
import android.content.*;import android.graphics.*;import android.view.MotionEvent;
import java.util.*;import java.io.*;import java.lang.reflect.*;
import org.junit.*;import org.junit.runner.RunWith;import org.robolectric.*;import org.robolectric.annotation.*;
@RunWith(RobolectricTestRunner.class) @Config(sdk=34,manifest=Config.NONE) @GraphicsMode(GraphicsMode.Mode.NATIVE)
public class SkillFxAuditTest {
 Context c;
 @Before public void setup(){c=RuntimeEnvironment.getApplication();c.getSharedPreferences("project_dark_f5m_v1",0).edit().clear().commit();F5mSaveStore.install(c);}
 @Test public void all221RuntimeRoutesAndFourDirectionsRenderWithoutBorrowingSources()throws Exception{
  SkillPresentationCatalog catalog=new SkillPresentationCatalog(c);SkillVfxRenderer fx=new SkillVfxRenderer(c,catalog);String[] dirs={"rogue","classic","warrior","shared"};Map<String,ClassicSkillReference> refs=new LinkedHashMap<>();for(String d:dirs)refs.put(d,new ClassicSkillReference(c,d));CapturedSkillFx capture=new CapturedSkillFx(c);Map<String,List<SkillPresentationCatalog.Entry>> jobs=new LinkedHashMap<>();for(SkillPresentationCatalog.Entry e:catalog.entries.values())jobs.computeIfAbsent(e.id.split("_")[1],k->new ArrayList<>()).add(e);long seq=100000;
  for(Map.Entry<String,List<SkillPresentationCatalog.Entry>> job:jobs.entrySet()){
   Bitmap b=Bitmap.createBitmap(960,job.getValue().size()*140,Bitmap.Config.ARGB_8888);Canvas canvas=new Canvas(b);canvas.drawColor(0xff203128);Paint p=new Paint();p.setColor(Color.WHITE);p.setTextSize(11);
   for(int i=0;i<job.getValue().size();i++){SkillPresentationCatalog.Entry e=job.getValue().get(i);String branch=null;ClassicSkillReference reference=null;for(String d:dirs)if(refs.get(d).has(e.id)){branch=d;reference=refs.get(d);break;}String label=e.id+" "+e.name+" "+(branch==null?capture.get(e.id)==null?"ADAPTED":"capture":branch);canvas.drawText(label,6,i*140+14,p);
    for(int face=0;face<4;face++){final float ax=65+face*240,ay=i*140+86,dx=face%2==0?32:-32,dy=face<2?16:-16;SkillVfxRenderer.Anchors a=new SkillVfxRenderer.Anchors(){public float x(String id){return "player".equals(id)?ax:ax+dx;}public float y(String id){return "player".equals(id)?ay:ay+dy;}};
     fx.clear();CombatResolver.Definition def=new CombatResolver.Definition(e.id,CombatResolver.ActionKind.MAGIC,CombatResolver.ActionState.MAGIC,CombatResolver.EffectType.MAGIC_HIT,true,0,1,150,.24f,0);long action=++seq;CombatResolver.Event start=new CombatResolver.Event(++seq,action,CombatResolver.EventType.ACTION_STARTED,"player","monster",def,CombatResolver.InputMode.MANUAL,null,null,0),contact=new CombatResolver.Event(++seq,action,CombatResolver.EventType.HIT_FEEDBACK,"player","monster",def,CombatResolver.InputMode.MANUAL,null,CombatResolver.HitSemantic.DAMAGE,0);fx.consume(Arrays.asList(start,contact,contact),a);
     if(reference!=null){int expected=(reference.channel(e.id,true)==null?0:1)+(reference.channel(e.id,false)==null?0:1);assertEquals(e.id,expected,fx.pulses.size());for(SkillVfxRenderer.Pulse pulse:fx.pulses){assertEquals(e.id,branch,pulse.sheet);ClassicSkillReference.Channel ch=reference.channel(e.id,pulse.caster);assertEquals(ch.casterAnchor?"player":"monster",pulse.anchor);}}
     else if(capture.get(e.id)!=null){assertEquals(e.id,1,fx.pulses.stream().filter(q->q.sheet.equals("capture")).count());}
     for(SkillVfxRenderer.Pulse pulse:fx.pulses)pulse.age=Math.min(pulse.duration*.6f,pulse.duration-.001f);p.setColor(0xffd0ba7b);canvas.drawCircle(ax,ay,3,p);p.setColor(0xffef7777);canvas.drawCircle(ax+dx,ay+dy,3,p);fx.draw(canvas,a);p.setColor(Color.WHITE);canvas.drawText(""+face,ax-30,ay+43,p);
    }
   }save(b,"fx-audit-v75-"+job.getKey()+".png");b.recycle();
  }assertEquals(221,catalog.entries.size());
 }
 @Test public void madSoulBurstFacesEveryContactTargetAndDirectionSnapshotIsStable()throws Exception{
  CapturedSkillFx fx=new CapturedSkillFx(c);CapturedSkillFx.Sequence s=fx.get("SK_전사_013");assertTrue(s.directional);for(float dx:new float[]{32,-32})for(float dy:new float[]{16,-16}){
   Bitmap b=Bitmap.createBitmap(320,320,Bitmap.Config.ARGB_8888);fx.drawDirected(new Canvas(b),new Paint(),"SK_전사_013",.5f,160,160,dx,dy);double sum=0,x=0,y=0;for(int py=0;py<320;py++)for(int px=0;px<320;px++){int a=Color.alpha(b.getPixel(px,py));sum+=a;x+=a*px;y+=a*py;}assertTrue(sum>0);double cx=x/sum-160,cy=y/sum-(160-s.directionPivotLift*s.scale);assertTrue("forward energy "+dx+","+dy,cx*dx+cy*dy>0);b.recycle();
  }
 }
 @Test public void fiveReportedSkillsUseActualQuickslotInputInAllFourDirections()throws Exception{
  String[] ids={"SK_전사_015","SK_전사_014","SK_전사_013","SK_무도가_002","SK_무도가_007"};int face=0;
  for(String id:ids)for(float dx:new float[]{32,-32})for(float dy:new float[]{16,-16}){
   F5mSaveStore.install(c);GameView v=new GameView(c);v.layout(0,0,960,540);v.setSkillTestMode(true);RuntimeState s=field(v,"state");RuntimeState.Monster m=s.monsters().get(0);s.player().x=m.x-dx;s.player().y=m.y-dy;s.player().mp=0;m.hp=200;
   ((CombatController)field(v,"combat")).selectTarget(m);((com.projectdark.mobile.world.WorldRuntimeAdapter)field(v,"worldAdapter")).snapCameraToPlayer();((SkillBook)field(v,"skillBook")).restoreTestSlots(new String[]{id,null,null,null,null,null,null,null});MotionEvent ev=MotionEvent.obtain(0,0,MotionEvent.ACTION_DOWN,671,395,0);v.onTouchEvent(ev);ev.recycle();assertEquals(id,field(v,"activeSkillVisualId"));
   Method tick=GameView.class.getDeclaredMethod("tickSkillCombat",float.class);tick.setAccessible(true);tick.invoke(v,SkillActionContract.get(id).contact+.001f);SkillVfxRenderer fx=field(v,"skillVfx");assertFalse(id,fx.pulses.isEmpty());for(SkillVfxRenderer.Pulse q:fx.pulses){q.age=q.sheet.equals("capture")?(id.equals("SK_전사_015")?.22f:Math.min(.5f,q.duration*.75f)):q.duration*.45f;if(id.equals("SK_전사_013")&&q.sheet.equals("capture")){assertEquals(dx,q.directionX,.01f);assertEquals(dy,q.directionY,.01f);}}
   Bitmap b=Bitmap.createBitmap(960,540,Bitmap.Config.ARGB_8888);v.draw(new Canvas(b));save(b,"fx-live-v75-"+id+"-"+(face++%4)+".png");b.recycle();
  }
  ClassicSkillReference classic=new ClassicSkillReference(c);assertNull(classic.channel("SK_무도가_002",true));assertTrue(classic.channel("SK_무도가_002",false).sequence.atlas.getWidth()>0);ClassicSkillReference warrior=new ClassicSkillReference(c,"warrior");assertTrue(warrior.channel("SK_전사_014",false).sequence.normalBlend);
 }
 @Test public void defenseRingKeepsBrightSourceRimAndNormalColourPixels()throws Exception{
  ClassicSkillReference warrior=new ClassicSkillReference(c,"warrior");ClassicSkillReference.Channel channel=warrior.channel("SK_전사_014",false);CapturedSkillFx.Sequence s=channel.sequence;assertTrue(s.normalBlend);Bitmap b=Bitmap.createBitmap(s.width,s.height,Bitmap.Config.ARGB_8888);warrior.draw(new Canvas(b),channel,.08f,s.pivotX,s.pivotY);int bright=0;for(int y=0;y<s.height;y++)for(int x=0;x<s.width;x++){int expected=s.atlas.getPixel(x+s.width,y);assertEquals(expected,b.getPixel(x,y));if(Color.alpha(expected)>200&&Color.red(expected)>170&&Color.green(expected)>170&&Color.blue(expected)>170)bright++;}assertTrue("source white rim was not removed",bright>20);b.recycle();
 }
 static void save(Bitmap b,String name)throws Exception{File f=new File("build/reports/device-review/"+name);f.getParentFile().mkdirs();try(FileOutputStream o=new FileOutputStream(f)){b.compress(Bitmap.CompressFormat.PNG,100,o);}}
 @SuppressWarnings("unchecked")static <T>T field(Object o,String n)throws Exception{Field f=o.getClass().getDeclaredField(n);f.setAccessible(true);return(T)f.get(o);}
}
