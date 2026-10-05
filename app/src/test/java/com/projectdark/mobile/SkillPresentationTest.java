package com.projectdark.mobile;
import static org.junit.Assert.*;
import android.content.*;
import android.graphics.*;
import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.*;
@RunWith(RobolectricTestRunner.class)
@Config(sdk=34,manifest=Config.NONE)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public class SkillPresentationTest {
  Context c;
  @Before public void setup(){c=RuntimeEnvironment.getApplication();c.getSharedPreferences("project_dark_f5m_v1",0).edit().clear().commit();F5mSaveStore.install(c);}
  @Test public void allCatalogIdsAndBothBodiesHaveBoundedDirectionalFrames()throws Exception{
    SkillPresentationCatalog s=new SkillPresentationCatalog(c);assertEquals(221,s.entries.size());assertEquals(268,s.frames.length());SkillBook b=SkillBook.load(c);
    for(SkillPresentationCatalog.Entry e:s.entries.values()){assertNotNull(b.get(e.id));for(String body:new String[]{"mm001","wm001"})for(CharacterRenderer.Direction d:CharacterRenderer.Direction.values())for(float phase:new float[]{0,.4f,.9f})assertNotNull(e.id+" "+body+" "+d,s.frames.optJSONObject(s.frameKey(body,e.motion,d,phase)));}
    assertEquals("FRONT_KICK",s.get("SK_무도가_002").motion);assertEquals("SIDE_KICK",s.get("SK_무도가_007").motion);assertEquals("WATER",s.get("SK_마법사_001").target);assertEquals("FIRE",s.get("SK_마법사_005").target);
    assertEquals("THROW",s.get("SK_도적_003").motion);assertEquals("SWING",s.get("SK_전사_021").motion);assertEquals("CLASSIC_CAPTURE",s.get("SK_무도가_016").target);assertEquals("QI_BLAST",s.get("SK_무도가_028").target);assertEquals("CLASSIC_CAPTURE",s.get("SK_성직자_007").target);
  }
  @Test public void originalFramesAndCurrentEquipmentRenderForEachGenderDirectionPose()throws Exception{
    SkillPresentationCatalog s=new SkillPresentationCatalog(c);SkillBodyRenderer r=new SkillBodyRenderer(c,s);String[] motions={"CAST","PUNCH","FRONT_KICK","SIDE_KICK","THRUST","RAISE","THROW"};
    for(String body:new String[]{"mm001","wm001"}){
      Bitmap image=Bitmap.createBitmap(1080,540,Bitmap.Config.ARGB_8888);Canvas canvas=new Canvas(image);canvas.drawColor(0xff282e24);Paint p=new Paint();p.setColor(Color.WHITE);p.setTextSize(12);
      for(int i=0;i<motions.length;i++)for(CharacterRenderer.Direction d:CharacterRenderer.Direction.values()){
        float x=60+i*150,y=95+d.ordinal()*120;
        CharacterRenderer.Pose pose=new CharacterRenderer.Pose(x,y,d,CharacterRenderer.State.SKILL,0,.2f,.5f,false,"mu0000001,mh172,ml228,ms001","mw001",null,CharacterRenderer.EffectFamily.SKILL);
        assertTrue(r.draw(canvas,pose,body,motions[i],.4f));canvas.drawText(motions[i]+" "+d,x-40,y+25,p);
      }save(image,"skill-motion-"+body+".png");
    }
  }
  @Test public void twoChannelsAreSeparatedDeduplicatedAndCancelled()throws Exception{
    SkillPresentationCatalog s=new SkillPresentationCatalog(c);SkillVfxRenderer r=new SkillVfxRenderer(c,s);SkillVfxRenderer.Anchors a=new SkillVfxRenderer.Anchors(){public float x(String id){return id.equals("player")?200:350;}public float y(String id){return 200;}};
    CombatResolver.Definition def=new CombatResolver.Definition("SK_성직자_003",CombatResolver.ActionKind.MAGIC,CombatResolver.ActionState.MAGIC,CombatResolver.EffectType.MAGIC_HIT,true,8,1,150,.24f,12);
    CombatResolver.Event start=new CombatResolver.Event(1,8,CombatResolver.EventType.ACTION_STARTED,"player","m",def,CombatResolver.InputMode.MANUAL,null,null,0);
    r.consume(Arrays.asList(start,start),a);assertEquals(1,r.pulses.size());assertTrue(r.pulses.get(0).caster);assertEquals("player",r.pulses.get(0).anchor);
    CombatResolver.Event miss=new CombatResolver.Event(2,8,CombatResolver.EventType.HIT_FEEDBACK,"player","m",def,CombatResolver.InputMode.MANUAL,null,CombatResolver.HitSemantic.MISS,0);r.consume(Collections.singletonList(miss),a);assertEquals(1,r.pulses.size());
    CombatResolver.Event hit=new CombatResolver.Event(3,9,CombatResolver.EventType.HIT_FEEDBACK,"player","m",def,CombatResolver.InputMode.MANUAL,null,CombatResolver.HitSemantic.DAMAGE,12);r.consume(Arrays.asList(hit,hit),a);assertEquals(3,r.pulses.size());SkillVfxRenderer.Pulse spell=r.pulses.stream().filter(f->!f.caster&&!f.sheet.equals("impact")).findFirst().get();assertEquals("m",spell.anchor);assertEquals("classic",spell.sheet);assertFalse(spell.caster);assertEquals(1,r.pulses.stream().filter(f->f.sheet.equals("impact")).count());
    r.consume(Collections.singletonList(new CombatResolver.Event(4,8,CombatResolver.EventType.ACTION_CANCELLED,"player","m",def,CombatResolver.InputMode.MANUAL,null,null,0)),a);assertEquals(2,r.pulses.size());r.tick(1);assertTrue(r.pulses.isEmpty());
  }
  @Test public void realFieldSkillInputDrivesCasterThenHitAndHealRecipient()throws Exception{
    for(String id:new String[]{"SK_마법사_005","SK_무도가_002","SK_성직자_005"}){
      c.getSharedPreferences("project_dark_f5m_v1",0).edit().clear().commit();F5mSaveStore.install(c);GameView v=new GameView(c);v.layout(0,0,960,540);SkillBook book=field(v,"skillBook");RuntimeState state=field(v,"state");CombatController combat=field(v,"combat");SkillVfxRenderer fx=field(v,"skillVfx");
      Field job=RpgProgressionState.class.getDeclaredField("currentJobCode");job.setAccessible(true);job.set(state.rpg(),id.contains("마법사")?"MAGE":id.contains("무도가")?"MARTIAL_ARTIST":"CLERIC");book.learn(id,0);state.player().mp=50;state.player().hp=5;RuntimeState.Monster m=state.monsters().get(0);state.player().x=m.x-32;state.player().y=m.y-16;combat.selectTarget(m);
      com.projectdark.mobile.world.WorldRuntimeAdapter world=field(v,"worldAdapter");world.snapCameraToPlayer();
      Method use=GameView.class.getDeclaredMethod("useBookSkill",SkillBook.Entry.class);use.setAccessible(true);use.invoke(v,book.get(id));Method tick=GameView.class.getDeclaredMethod("tickSkillCombat",float.class);tick.setAccessible(true);tick.invoke(v,.01f);if(id.equals("SK_무도가_002")||id.equals("SK_마법사_005")){assertTrue("Retained source has only a recipient contact burst",fx.pulses.isEmpty());}else{assertEquals(1,fx.pulses.size());assertTrue(fx.pulses.get(0).caster);}
      tick.invoke(v,.10f);Field actionTime=GameView.class.getDeclaredField("actionClock");actionTime.setAccessible(true);actionTime.setFloat(v,.11f);Bitmap casterImage=Bitmap.createBitmap(960,540,Bitmap.Config.ARGB_8888);v.draw(new Canvas(casterImage));save(casterImage,"skill-vfx-caster-"+id+".png");
      tick.invoke(v,.15f);assertTrue(fx.pulses.stream().anyMatch(f->!f.caster));SkillVfxRenderer.Pulse target=fx.pulses.stream().filter(f->!f.caster).findFirst().get();assertEquals(SkillRuntimeCatalog.healing(id)?"player":m.id,target.anchor);
      int count=fx.pulses.size(),mp=state.player().mp;use.invoke(v,book.get(id));tick.invoke(v,.01f);assertEquals(mp,state.player().mp);assertEquals(count,fx.pulses.size());
      Method clock=GameView.class.getDeclaredMethod("drawCharacter",Canvas.class);clock.setAccessible(true);Field t=GameView.class.getDeclaredField("actionClock");t.setAccessible(true);t.setFloat(v,.2f);
      tick.invoke(v,.05f);Bitmap image=Bitmap.createBitmap(960,540,Bitmap.Config.ARGB_8888);v.draw(new Canvas(image));save(image,"skill-vfx-live-"+id+".png");
    }
  }
  @Test public void unverifiedQiBlastAndBloodTransferUseExplicitDirectionalReconstructions()throws Exception{
    SkillPresentationCatalog s=new SkillPresentationCatalog(c);SkillVfxRenderer r=new SkillVfxRenderer(c,s);
    SkillVfxRenderer.Anchors a=new SkillVfxRenderer.Anchors(){public float x(String id){return id.equals("player")?180:360;}public float y(String id){return 220;}};
    r.preview(51,"SK_무도가_028",false,"target",a);r.preview(52,"SK_성직자_002",true,"player",a);
    assertEquals(2,r.pulses.size());SkillVfxRenderer.Pulse qi=r.pulses.get(0),blood=r.pulses.get(1);
    assertEquals("QI_BLAST",s.get(qi.id).target);assertEquals("reconstruction",blood.sheet);assertTrue(blood.caster);
    Bitmap image=Bitmap.createBitmap(500,360,Bitmap.Config.ARGB_8888);Canvas canvas=new Canvas(image);r.draw(canvas,a);
    int colored=0;for(int yy=0;yy<image.getHeight();yy++)for(int xx=0;xx<image.getWidth();xx++)if(Color.alpha(image.getPixel(xx,yy))>0)colored++;
    assertTrue("directional QI impact and caster transfer must draw visible pixels",colored>20);
  }
  private void save(Bitmap b,String name)throws Exception{File dir=new File("build/reports/device-review");dir.mkdirs();try(FileOutputStream out=new FileOutputStream(new File(dir,name))){b.compress(Bitmap.CompressFormat.PNG,100,out);}}
  @SuppressWarnings("unchecked") private static <T>T field(Object o,String name)throws Exception{Field f=o.getClass().getDeclaredField(name);f.setAccessible(true);return(T)f.get(o);}
}
