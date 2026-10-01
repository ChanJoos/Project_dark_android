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

/** Review evolving BODY frames and real contact events, not only a particle peak. */
@RunWith(RobolectricTestRunner.class) @Config(sdk=34,manifest=Config.NONE) @GraphicsMode(GraphicsMode.Mode.NATIVE)
public class MartialDefenseStarRegressionTest {
 Context c;
 @Before public void setup(){c=RuntimeEnvironment.getApplication();c.getSharedPreferences("project_dark_f5m_v1",0).edit().clear().commit();F5mSaveStore.install(c);}
 @Test public void all34MartialSelectionsRenderBothOriginalBodiesInFourDirections()throws Exception{
  SkillPresentationCatalog catalog=new SkillPresentationCatalog(c);SkillBodyRenderer renderer=new SkillBodyRenderer(c,catalog);int count=0;
  for(SkillPresentationCatalog.Entry entry:catalog.entries.values())if(entry.id.startsWith("SK_무도가_")){
   count++;SkillActionContract.Rule rule=SkillActionContract.get(entry.id);float duration=Math.max(.5f,rule.contact+.28f);
   for(String body:new String[]{"mm001","wm001"}){
    Bitmap image=Bitmap.createBitmap(600,480,Bitmap.Config.ARGB_8888);Canvas canvas=new Canvas(image);canvas.drawColor(0xff263029);Paint label=new Paint();label.setColor(Color.WHITE);label.setTextSize(10);
    float[] phases={0,.2f,1f/3f,.65f,.9f};
    for(CharacterRenderer.Direction d:CharacterRenderer.Direction.values())for(int i=0;i<phases.length;i++){
     float x=60+i*120,y=85+d.ordinal()*115;
     CharacterRenderer.Pose pose=new CharacterRenderer.Pose(x,y,d,CharacterRenderer.State.SKILL,0,0,duration,false,null,null,null,CharacterRenderer.EffectFamily.SKILL);
     assertTrue(entry.id+" "+body+" "+d,renderer.draw(canvas,pose,body,entry.motion,phases[i]));
     assertNotNull(catalog.frames.optJSONObject(catalog.frameKey(body,entry.motion,d,phases[i])));
     canvas.drawText(d+" "+phases[i],x-40,y+20,label);
    }
    canvas.drawText(entry.id+" "+entry.motion+" (selection; original cadence not certified)",5,475,label);
    write(image,"v80-martial-"+entry.id+"-"+body+".png");image.recycle();
   }
  }
  assertEquals(34,count);
  assertEquals("PUNCH",catalog.get("SK_무도가_015").motion);assertEquals("MARTIAL_CAST",catalog.get("SK_무도가_021").motion);
  assertEquals("IDLE",catalog.get("SK_무도가_011").motion);assertEquals("CHARGE_CAST",catalog.get("SK_무도가_020").motion);
  assertEquals("TRIPLE_PUNCH",catalog.get("SK_무도가_027").motion);
 }
 @Test public void realKickClockReachesFullExtensionWhenDamageResolves()throws Exception{
  for(String id:new String[]{"SK_무도가_002","SK_무도가_007","SK_무도가_014"}){
   GameView view=fresh();RuntimeState state=field(view,"state");RuntimeState.Monster m=state.monsters().get(0);m.hp=200;
   state.player().x=m.x-32;state.player().y=m.y-16;((CombatController)field(view,"combat")).selectTarget(m);
   use(view,id);SkillActionContract.Rule rule=SkillActionContract.get(id);int hp=m.hp;
   Method update=GameView.class.getDeclaredMethod("update",float.class);update.setAccessible(true);
   update.invoke(view,rule.contact-.01f);assertEquals(hp,m.hp);float before=(Float)field(view,"actionClock");assertEquals(rule.contact-.01f,before,.001f);
   save(view,id+"-before");update.invoke(view,.011f);assertTrue(id,m.hp<hp);
   float clock=(Float)field(view,"actionClock");float phase=GameView.skillPosePhase(clock,.5f,rule.contact);
   SkillPresentationCatalog catalog=field(view,"skillPresentation");String motion=catalog.get(id).motion;
   for(String body:new String[]{"mm001","wm001"})for(CharacterRenderer.Direction d:CharacterRenderer.Direction.values()){
    boolean back=d==CharacterRenderer.Direction.NE||d==CharacterRenderer.Direction.NW;
    int expected=id.endsWith("002")?(back?2:5):(back?12:16);
    assertEquals(body+"/d/"+expected,catalog.frameKey(body,motion,d,phase));
   }
   save(view,id+"-contact");update.invoke(view,.22f);save(view,id+"-recovery");
  }
 }
 @Test public void v82EquippedKickInputUsesMartialLegSpritesAndSpinTurns()throws Exception{
  for(String id:new String[]{"SK_무도가_002","SK_무도가_007","SK_무도가_014"})for(String body:new String[]{"mm001","wm001"})for(CharacterRenderer.Direction d:CharacterRenderer.Direction.values()){
   GameView view=directed(body,d);use(view,id);assertEquals(id,field(view,"activeSkillVisualId"));
   SkillActionContract.Rule rule=SkillActionContract.get(id);Method update=GameView.class.getDeclaredMethod("update",float.class);update.setAccessible(true);
   save82(view,id+"-"+body+"-"+d+"-startup");update.invoke(view,rule.contact+.001f);
   RuntimeState state=field(view,"state");assertTrue(state.monsters().get(0).hp<20000);
   SkillPresentationCatalog catalog=field(view,"skillPresentation");String motion=catalog.get(id).motion;
   assertEquals("d",catalog.profiles.getJSONObject(motion).getString("group"));
   boolean back=d==CharacterRenderer.Direction.NW||d==CharacterRenderer.Direction.NE;
   int contactFrame=id.endsWith("002")?(back?2:5):(back?12:16);
   float clock=(Float)field(view,"actionClock");Method duration=GameView.class.getDeclaredMethod("duration",field(view,"action").getClass());duration.setAccessible(true);
   float total=(Float)duration.invoke(view,field(view,"action"));
   assertEquals(body+"/d/"+contactFrame,catalog.frameKey(body,motion,d,GameView.skillPosePhase(clock,total,rule.contact)));
   save82(view,id+"-"+body+"-"+d+"-contact");
   if(id.endsWith("014")){
    Set<String> seen=new LinkedHashSet<>();for(float q:new float[]{.34f,.44f,.53f,.61f,.69f,.77f})seen.add(catalog.frameKey(body,motion,d,q));
    assertTrue("Full turn has multiple facing source poses",seen.size()>=6);
    update.invoke(view,.16f);save82(view,id+"-"+body+"-"+d+"-turn");
   }
   update.invoke(view,.3f);save82(view,id+"-"+body+"-"+d+"-recovery");
  }
 }
 @Test public void v82DaraRemainsStandingUntilDamageReleaseThenRaisesArms()throws Exception{
  for(String body:new String[]{"mm001","wm001"})for(CharacterRenderer.Direction d:CharacterRenderer.Direction.values()){
   GameView view=directed(body,d);use(view,"SK_무도가_020");assertEquals("SK_무도가_020",field(view,"activeSkillVisualId"));
   RuntimeState state=field(view,"state");RuntimeState.Monster m=state.monsters().get(0);int hp=m.hp;
   Method update=GameView.class.getDeclaredMethod("update",float.class);update.setAccessible(true);
   Bitmap idle=actor82(view);save82(view,"dara-"+body+"-"+d+"-0000ms");float previous=0;
   for(float time:new float[]{.30f,1.50f,2.99f}){
    float advance=time-previous;while(advance>.05f){update.invoke(view,.05f);advance-=.05f;}update.invoke(view,advance);previous=time;
    assertEquals("No damage during preparation",hp,m.hp);
    assertFalse(((SkillVfxRenderer)field(view,"skillVfx")).pulses.stream().anyMatch(p->p.id.equals("SK_무도가_020")&&!p.caster));
    Bitmap waiting=actor82(view);assertTrue("Equipped idle remains pixel-identical until release "+body+" "+d+" "+time,idle.sameAs(waiting));waiting.recycle();
    save82(view,"dara-"+body+"-"+d+"-"+Math.round(time*1000)+"ms");
   }
   update.invoke(view,.012f);assertTrue("Damage at release",m.hp<hp);assertEquals("SK_무도가_020",field(view,"activeSkillVisualId"));
   assertTrue(((SkillVfxRenderer)field(view,"skillVfx")).pulses.stream().anyMatch(p->p.id.equals("SK_무도가_020")&&!p.caster&&p.anchor.equals(m.id)));
   Bitmap raised=actor82(view);assertFalse("Arms change only after release",idle.sameAs(raised));raised.recycle();
   SkillPresentationCatalog catalog=field(view,"skillPresentation");boolean back=d==CharacterRenderer.Direction.NW||d==CharacterRenderer.Direction.NE;
   float clock=(Float)field(view,"actionClock");assertEquals(body+"/f/"+(back?1:3),catalog.frameKey(body,"CHARGE_CAST",d,GameView.skillPosePhase(clock,3.35f,3f)));
   save82(view,"dara-"+body+"-"+d+"-release");update.invoke(view,.27f);save82(view,"dara-"+body+"-"+d+"-lower-arms");
   update.invoke(view,.1f);assertNull(field(view,"activeSkillVisualId"));save82(view,"dara-"+body+"-"+d+"-end");idle.recycle();
  }
 }
 private GameView directed(String body,CharacterRenderer.Direction d)throws Exception{
  GameView v=fresh();Field identity=GameView.class.getDeclaredField("characterBodyIdentity");identity.setAccessible(true);identity.set(v,body);
  RuntimeState s=field(v,"state");RuntimeState.Monster m=s.monsters().get(0);m.hp=20000;s.player().hp=20000;for(RuntimeState.Monster other:s.monsters())other.attackCooldown=100f;
  float dx=d==CharacterRenderer.Direction.NW||d==CharacterRenderer.Direction.SW?-32:32,dy=d==CharacterRenderer.Direction.NW||d==CharacterRenderer.Direction.NE?-16:16;
  s.player().x=m.x-dx;s.player().y=m.y-dy;((CombatController)field(v,"combat")).selectTarget(m);
  ((com.projectdark.mobile.world.WorldRuntimeAdapter)field(v,"worldAdapter")).snapCameraToPlayer();return v;
 }
 private static Bitmap actor82(GameView view)throws Exception{
  Bitmap b=Bitmap.createBitmap(240,200,Bitmap.Config.ARGB_8888);Canvas canvas=new Canvas(b);canvas.translate(120-view.renderedPlayerWorldX(),150-view.renderedPlayerWorldY());
  Method draw=GameView.class.getDeclaredMethod("drawCharacter",Canvas.class);draw.setAccessible(true);draw.invoke(view,canvas);return b;
 }
 private static void save82(GameView view,String name)throws Exception{
  Bitmap b=Bitmap.createBitmap(960,540,Bitmap.Config.ARGB_8888);view.draw(new Canvas(b));write(b,"v82-live-"+name+".png");b.recycle();
  Bitmap actor=actor82(view);write(actor,"v82-actor-"+name+".png");actor.recycle();
 }
 @Test public void retainedDefenseSphereContainsHeadAndFeetForBothBodies()throws Exception{
  SkillPresentationCatalog catalog=new SkillPresentationCatalog(c);SkillBodyRenderer renderer=new SkillBodyRenderer(c,catalog);
  ClassicSkillReference reference=new ClassicSkillReference(c,"warrior");ClassicSkillReference.Channel channel=reference.channel("SK_전사_014",false);assertNotNull(channel);
  assertEquals(-35,channel.sequence.footOffsetY,0);assertEquals(.31f,channel.sequence.scale,0);
  for(String body:new String[]{"mm001","wm001"})for(CharacterRenderer.Direction d:CharacterRenderer.Direction.values()){
   Bitmap actor=Bitmap.createBitmap(240,220,Bitmap.Config.ARGB_8888);Canvas actorCanvas=new Canvas(actor);
   CharacterRenderer.Pose pose=new CharacterRenderer.Pose(120,150,d,CharacterRenderer.State.SKILL,0,.2f,.5f,false,body.equals("mm001")?"mu0000001,mh172,ml228":null,null,null,CharacterRenderer.EffectFamily.SKILL);
   assertTrue(renderer.draw(actorCanvas,pose,body,"RAISE",.4f));
   Bitmap shell=Bitmap.createBitmap(240,220,Bitmap.Config.ARGB_8888);reference.draw(new Canvas(shell),channel,.2f,120,150+channel.sequence.footOffsetY);
   for(int y=0;y<actor.getHeight();y++)for(int x=0;x<actor.getWidth();x++)if(Color.alpha(actor.getPixel(x,y))>12){
    float dx=(x-120)/(110*.31f),dy=(y-(150-35))/(118*.31f);
    assertTrue("Inside source ellipse "+body+" "+d+" pixel="+x+","+y,dx*dx+dy*dy<=1.04f);
   }
   int[] a=bounds(actor),s=bounds(shell);assertTrue("head "+body+" "+d+" actor="+Arrays.toString(a)+" sphere="+Arrays.toString(s),a[1]>=s[1]);
   assertTrue("feet "+body+" "+d,a[3]<=s[3]+2);
   Bitmap review=Bitmap.createBitmap(240,220,Bitmap.Config.ARGB_8888);Canvas canvas=new Canvas(review);canvas.drawColor(0xff20291f);canvas.drawBitmap(actor,0,0,null);canvas.drawBitmap(shell,0,0,null);
   write(review,"v80-defense-"+body+"-"+d+".png");actor.recycle();shell.recycle();review.recycle();
  }
 }
 @Test public void remainingFifthCircleStarsAppearOnEachRecipientAtRealDamageContact()throws Exception{
  for(String id:new String[]{"SK_마법사_034","SK_마법사_035","SK_마법사_036"}){
   GameView view=fresh();RuntimeState state=field(view,"state");RuntimeState.Monster m=state.monsters().get(0);m.hp=200;
   state.player().x=m.x-32;state.player().y=m.y-16;((CombatController)field(view,"combat")).selectTarget(m);use(view,id);
   Method tick=GameView.class.getDeclaredMethod("tickSkillCombat",float.class);tick.setAccessible(true);int hp=m.hp;
   tick.invoke(view,.23f);assertEquals(hp,m.hp);tick.invoke(view,.011f);assertTrue(id,m.hp<hp);
   SkillPresentationCatalog catalog=field(view,"skillPresentation");assertEquals("DARK_STAR",catalog.get(id).target);
   SkillVfxRenderer fx=field(view,"skillVfx");List<SkillVfxRenderer.Pulse> stars=new ArrayList<>();
   for(SkillVfxRenderer.Pulse pulse:fx.pulses)if(pulse.id.equals(id)&&!pulse.caster&&!pulse.sheet.equals("impact"))stars.add(pulse);
   assertFalse(id,stars.isEmpty());assertTrue(stars.stream().anyMatch(p->p.anchor.equals(m.id)));
   for(SkillVfxRenderer.Pulse pulse:stars){assertEquals(0,pulse.age,0);assertFalse("never caster",pulse.anchor.equals("player"));}
   save(view,id+"-contact");tick.invoke(view,.8f);assertFalse(fx.pulses.stream().anyMatch(p->p.id.equals(id)));
  }
  Bitmap b=Bitmap.createBitmap(240,220,Bitmap.Config.ARGB_8888);SkillEffectShapes.draw(new Canvas(b),new Paint(),"DARK_STAR",120,134,0);
  int[] box=bounds(b);assertTrue(box[2]-box[0]>45);assertTrue(box[3]-box[1]>45);assertEquals(120,(box[0]+box[2])/2,2);
  write(b,"v80-star-visible-at-zero.png");
 }
 private GameView fresh(){c.getSharedPreferences("project_dark_f5m_v1",0).edit().clear().commit();F5mSaveStore.install(c);GameView v=new GameView(c);v.layout(0,0,960,540);v.setSkillTestMode(true);return v;}
 private static int[] bounds(Bitmap b){int[] r={b.getWidth(),b.getHeight(),-1,-1};for(int y=0;y<b.getHeight();y++)for(int x=0;x<b.getWidth();x++)if(Color.alpha(b.getPixel(x,y))>12){r[0]=Math.min(r[0],x);r[1]=Math.min(r[1],y);r[2]=Math.max(r[2],x);r[3]=Math.max(r[3],y);}assertTrue(r[2]>=0);return r;}
 private static void use(GameView v,String id)throws Exception{Method m=GameView.class.getDeclaredMethod("useBookSkill",SkillBook.Entry.class);m.setAccessible(true);((com.projectdark.mobile.world.WorldRuntimeAdapter)field(v,"worldAdapter")).snapCameraToPlayer();m.invoke(v,((SkillBook)field(v,"skillBook")).get(id));}
 private static void save(GameView v,String name)throws Exception{Bitmap b=Bitmap.createBitmap(960,540,Bitmap.Config.ARGB_8888);v.draw(new Canvas(b));write(b,"v80-live-"+name+".png");b.recycle();}
 private static void write(Bitmap b,String name)throws Exception{File f=new File("build/reports/device-review/"+name);f.getParentFile().mkdirs();try(FileOutputStream out=new FileOutputStream(f)){b.compress(Bitmap.CompressFormat.PNG,100,out);}}
 @SuppressWarnings("unchecked")private static <T>T field(Object o,String n)throws Exception{Field f=o.getClass().getDeclaredField(n);f.setAccessible(true);return(T)f.get(o);}
}
