package com.projectdark.mobile;
import static org.junit.Assert.*;
import android.content.Context;import android.graphics.*;import android.view.MotionEvent;
import com.projectdark.mobile.world.*;import java.io.*;import java.lang.reflect.*;import java.util.*;
import org.junit.*;import org.junit.runner.RunWith;import org.robolectric.*;import org.robolectric.annotation.*;
@RunWith(RobolectricTestRunner.class) @Config(sdk=34,manifest=Config.NONE) @GraphicsMode(GraphicsMode.Mode.NATIVE)
public class MartialHudAutoRegressionTest {
 Context c;
 @Before public void setup(){c=RuntimeEnvironment.getApplication();reset();}
 void reset(){c.getSharedPreferences("project_dark_f5m_v1",0).edit().clear().commit();F5mSaveStore.install(c);}
 @SuppressWarnings("unchecked") static <T>T field(Object o,String n)throws Exception{Field f=o.getClass().getDeclaredField(n);f.setAccessible(true);return (T)f.get(o);}
 static void set(Object o,String n,Object value)throws Exception{Field f=o.getClass().getDeclaredField(n);f.setAccessible(true);f.set(o,value);}
 static Object call(GameView v,String name,Class<?> type,Object value)throws Exception{Method m=GameView.class.getDeclaredMethod(name,type);m.setAccessible(true);return m.invoke(v,value);}
 static void tap(GameView v,float x,float y){MotionEvent e=MotionEvent.obtain(0,0,MotionEvent.ACTION_DOWN,x,y,0);v.onTouchEvent(e);e.recycle();}
 GameView directed(CharacterRenderer.Direction direction)throws Exception{
  reset();GameView v=new GameView(c);v.layout(0,0,960,540);v.setSkillTestMode(true);RuntimeState s=field(v,"state");
  s.rpg().restoreBaseResources(20000,20000);s.applyDerivedGrowth();s.player().hp=10000;s.player().mp=20000;
  RuntimeState.Monster m=s.monsters().get(0);m.hp=20000;for(RuntimeState.Monster other:s.monsters()){if(other!=m)other.alive=false;other.attackCooldown=100; s.skillEffects().put(other.id,"qa","ROOT",1,60);}
  float dx=direction==CharacterRenderer.Direction.NW||direction==CharacterRenderer.Direction.SW?-32:32;
  float dy=direction==CharacterRenderer.Direction.NW||direction==CharacterRenderer.Direction.NE?-16:16;
  s.player().x=m.x-dx;s.player().y=m.y-dy;((CombatController)field(v,"combat")).selectTarget(m);
  ((CanonicalActorFacing)field(v,"playerFacing")).setLocomotion(direction);((WorldRuntimeAdapter)field(v,"worldAdapter")).snapCameraToPlayer();return v;
 }
 @Test public void everyActiveMartialSkillHasCorrectLiveDirectionTimingAndRecipientAnchors()throws Exception{
  SkillPresentationCatalog catalog=new SkillPresentationCatalog(c);int reviewed=0,active=0;
  for(SkillPresentationCatalog.Entry entry:catalog.entries.values())if(entry.id.startsWith("SK_무도가_")){
   reviewed++;SkillActionContract.Rule rule=SkillActionContract.get(entry.id);assertNotNull(rule);
   if(!rule.presentationAllowed())continue;active++;
   Bitmap sheet=Bitmap.createBitmap(960,630,Bitmap.Config.ARGB_8888);Canvas out=new Canvas(sheet);Paint label=new Paint();label.setColor(Color.WHITE);label.setTextSize(11);
   int column=0;
   for(CharacterRenderer.Direction direction:CharacterRenderer.Direction.values()){
    GameView v=directed(direction);RuntimeState s=field(v,"state");RuntimeState.Monster target=s.monsters().get(0);int hp=target.hp;
    call(v,"useBookSkill",SkillBook.Entry.class,((SkillBook)field(v,"skillBook")).get(entry.id));assertEquals(entry.id,field(v,"activeSkillVisualId"));
    assertEquals(direction,((CanonicalActorFacing)field(v,"playerFacing")).presentation());
    advance(v,Math.max(0,rule.contact-.01f));assertEquals(entry.id+" no early enemy hit",hp,target.hp);
    SkillVfxRenderer fx=field(v,"skillVfx");assertFalse(entry.id+" no early recipient",fx.pulses.stream().anyMatch(p->!p.caster));
    advance(v,.011f);assertEquals(direction,((CanonicalActorFacing)field(v,"playerFacing")).presentation());
    if(rule.damage())assertTrue(entry.id+" actual target contact",target.hp<hp);
    for(SkillVfxRenderer.Pulse pulse:fx.pulses){
     if(pulse.sheet.equals("impact"))assertEquals("actual damage recipient",target.id,pulse.anchor);
     if(pulse.sheet.equals("classic")){ClassicSkillReference.Channel channel=new ClassicSkillReference(c).channel(entry.id,pulse.caster);assertEquals(entry.id+" declared channel anchor",pulse.caster||rule.selfAnchored()?"player":target.id,pulse.anchor);}
    }
    crop(v,out,column*240,20);advance(v,.12f);crop(v,out,column*240,220);for(SkillVfxRenderer.Pulse pulse:fx.pulses)pulse.age=SkillFxAuditTest.sourcePeakAge(fx,pulse);crop(v,out,column*240,420);out.drawText(direction+" contact / +120ms / FX peak",column*240+8,14,label);column++;
   }
   save(sheet,"v89-martial-"+entry.id+".png");sheet.recycle();
  }
  assertEquals(34,reviewed);assertEquals(30,active);
 }
 static void advance(GameView view,float seconds)throws Exception{while(seconds>.05f){call(view,"update",float.class,.05f);seconds-=.05f;}if(seconds>0)call(view,"update",float.class,seconds);}
 static void crop(GameView v,Canvas out,int x,int y)throws Exception{
  Bitmap full=Bitmap.createBitmap(960,540,Bitmap.Config.ARGB_8888);v.draw(new Canvas(full));WorldRuntimeAdapter world=field(v,"worldAdapter");RuntimeState s=field(v,"state");WorldCameraTransform.Point p=world.worldToScreen(s.player().x,s.player().y);
  int left=Math.max(0,Math.min(720,Math.round(p.x)-120)),top=Math.max(0,Math.min(340,Math.round(p.y)-140));out.drawBitmap(full,new Rect(left,top,left+240,top+200),new RectF(x,y,x+240,y+200),null);full.recycle();
 }
 @Test public void movingTargetCannotTurnAnAcceptedFrontAttackBehindTheActor()throws Exception{
  for(String id:new String[]{"SK_무도가_001","SK_무도가_002","SK_무도가_006","SK_무도가_007","SK_무도가_009","SK_무도가_010","SK_무도가_018","SK_무도가_023","SK_무도가_024","SK_무도가_028"})for(float[] dir:new float[][]{{32,16},{-32,16},{32,-16},{-32,-16}}){
   RuntimeState s=new RuntimeState();s.player().x=0;s.player().y=0;RuntimeState.Monster m=s.monsters().get(0);m.x=dir[0];m.y=dir[1];m.hp=20000;
   RuntimeCombatSession session=new RuntimeCombatSession(s,(a,t)->true,(a,skill)->true,a->true);session.setSkillTestMode(true);
   assertTrue(id,session.submitPlayer(m.id,id).accepted());m.x=-dir[0];m.y=-dir[1];int before=m.hp;
   List<CombatResolver.Event> events=session.tick(SkillActionContract.get(id).contact+.001f).events;assertEquals(id+" no hit behind original direction",before,m.hp);assertFalse(events.stream().anyMatch(e->e.type==CombatResolver.EventType.HIT_FEEDBACK&&e.targetId.equals(m.id)));
  }
 }
 @Test public void autoUsesQuickslotsFairlyAndSkipsCoolingUnlearnedAndFullHealthHeal()throws Exception{
  GameView v=directed(CharacterRenderer.Direction.SE);SkillBook book=field(v,"skillBook");book.restoreTestSlots(new String[]{"SK_무도가_002","SK_무도가_007",null,null,null,null,null,null});
  tap(v,842,495);advance(v,.05f);assertTrue((Boolean)field(v,"autoAttackEnabled"));assertEquals("SK_무도가_002",field(v,"activeSkillVisualId"));
  advance(v,.55f);assertTrue((Boolean)field(v,"autoAttackEnabled"));assertEquals("SK_무도가_007",field(v,"activeSkillVisualId"));
  Bitmap screenshot=Bitmap.createBitmap(960,540,Bitmap.Config.ARGB_8888);v.draw(new Canvas(screenshot));save(screenshot,"v89-auto-quickslots.png");
  advance(v,.55f);assertTrue((Boolean)field(v,"autoAttackEnabled"));assertNotEquals("Cooling slot is skipped","SK_무도가_007",field(v,"activeSkillVisualId"));
  GameView real=directed(CharacterRenderer.Direction.SE);real.setSkillTestMode(false);SkillBook actual=field(real,"skillBook");actual.learn("SK_무도가_002",100);actual.assign(0,"SK_무도가_002");actual.bindJob(()->"MARTIAL_ARTIST");actual.learn("SK_무도가_017",100);actual.assign(0,"SK_무도가_017");actual.assign(1,"SK_무도가_002");
  actual.learn("SK_무도가_016",100);actual.assign(2,"SK_무도가_016");RuntimeState live=field(real,"state");live.player().mp=0;live.player().hp=live.player().maxHp;tap(real,842,495);advance(real,.05f);assertTrue((Boolean)field(real,"autoAttackEnabled"));assertEquals("Full-health heal is skipped","SK_무도가_002",field(real,"activeSkillVisualId"));
  GameView unlearned=directed(CharacterRenderer.Direction.SE);unlearned.setSkillTestMode(false);SkillBook empty=field(unlearned,"skillBook");empty.bindJob(()->"MARTIAL_ARTIST");assertFalse(empty.assign(0,"SK_무도가_007"));tap(unlearned,842,495);advance(unlearned,.05f);assertNull(field(unlearned,"activeSkillVisualId"));
 }
 @Test public void allEightQuickslotIconsStayInsideTheirSingleHudFramesAndNpcNamesFollowTheirHeads()throws Exception{
  GameView v=directed(CharacterRenderer.Direction.SE);SkillBook book=field(v,"skillBook");book.restoreTestSlots(new String[]{"SK_무도가_002","SK_무도가_007","SK_무도가_014","SK_무도가_020","SK_무도가_021","SK_무도가_023","SK_무도가_017","SK_무도가_008"});
  SkillWindow window=field(v,"skillWindow");SkillWindow.Actions actions=field(v,"skillActions");RectF slot=new RectF(10,10,52,52);Bitmap b=Bitmap.createBitmap(70,70,Bitmap.Config.ARGB_8888);
  for(int i=0;i<8;i++){b.eraseColor(Color.TRANSPARENT);window.drawSlot(new Canvas(b),slot,i,actions);int visible=0;for(int y=0;y<70;y++)for(int x=0;x<70;x++)if(Color.alpha(b.getPixel(x,y))>0){visible++;assertTrue("Icon respects common inset",x>=16&&x<46&&y>=16&&y<46);}assertTrue(visible>100);}
  RuntimeState state=field(v,"state");WorldRuntimeAdapter world=field(v,"worldAdapter");RuntimeState.Npc npc=state.npcs().get(0);state.player().x=npc.x-64;state.player().y=npc.y-32;world.snapCameraToPlayer();
  Bitmap full=Bitmap.createBitmap(960,540,Bitmap.Config.ARGB_8888);v.draw(new Canvas(full));save(full,"v89-hud-npc.png");
  v.layout(0,0,1536,709);Bitmap wide=Bitmap.createBitmap(1536,709,Bitmap.Config.ARGB_8888);v.draw(new Canvas(wide));save(wide,"v89-hud-wide-npc.png");wide.recycle();v.layout(0,0,960,540);
  for(TownInteriorDef d:TownInteriorDef.ALL){Method enter=GameView.class.getDeclaredMethod("enterTownInterior",TownInteriorDef.class);enter.setAccessible(true);enter.invoke(v,d);v.draw(new Canvas(full));save(full,"v89-town-"+d.kind.toString().toLowerCase()+".png");}
 }

 @Test public void everyMartialDamageShapeHitsOnlyItsDeclaredRecipients()throws Exception{
  for(SkillActionContract.Rule rule:SkillActionContract.all())if(rule.id.startsWith("SK_무도가_")&&rule.presentationAllowed()&&rule.damage())for(float[] direction:new float[][]{{32,16},{-32,16},{32,-16},{-32,-16}}){
   RuntimeState s=new RuntimeState();s.player().x=0;s.player().y=0;s.player().hp=20000;s.player().mp=20000;
   int i=0;for(RuntimeState.Monster m:s.monsters()){int u=i%7-3,v=i/7-1;m.x=32*(u-v);m.y=16*(u+v);m.hp=20000;i++;}
   RuntimeState.Monster selected=s.monsters().get(0);selected.x=direction[0];selected.y=direction[1];
   String target=rule.selfAnchored()?"player":selected.id;float tx=rule.selfAnchored()?0:selected.x,ty=rule.selfAnchored()?0:selected.y;
   Set<String> expected=new HashSet<>();for(RuntimeState.Monster m:s.monsters())if(SkillActionContract.includes(rule,0,0,tx,ty,m.x,m.y,true))expected.add(m.id);
   RuntimeCombatSession session=new RuntimeCombatSession(s,(a,t)->true,(a,id)->true,a->true);session.setSkillTestMode(true);assertTrue(rule.id,session.submitPlayer(target,rule.id).accepted());
   Set<String> actual=new HashSet<>();for(CombatResolver.Event e:session.tick(rule.contact+.001f).events)if(e.type==CombatResolver.EventType.HIT_FEEDBACK&&e.amount>0)actual.add(e.targetId);
   assertEquals(rule.id+" declared tile membership",expected,actual);
  }
 }

 @Test public void martialImpactsStayUprightAboveTheFootPivotWhenFacingNorthOrWest()throws Exception{
  ClassicSkillReference reference=new ClassicSkillReference(c);
  for(String id:reference.effects.keySet())if(id.startsWith("SK_무도가_")&&!SkillActionContract.get(id).selfAnchored()){
   ClassicSkillReference.Channel channel=reference.channel(id,false);if(channel==null||channel.sequence.visualCenter)continue;
   Bitmap east=Bitmap.createBitmap(400,400,Bitmap.Config.ARGB_8888),north=Bitmap.createBitmap(400,400,Bitmap.Config.ARGB_8888),west=Bitmap.createBitmap(400,400,Bitmap.Config.ARGB_8888);
   float age=channel.sequence.duration/2;reference.drawDirected(new Canvas(east),channel,age,200,200,32,16);reference.drawDirected(new Canvas(north),channel,age,200,200,32,-16);reference.drawDirected(new Canvas(west),channel,age,200,200,-32,-16);
   int[] pixels=new int[160000],n=new int[160000],w=new int[160000];east.getPixels(pixels,0,400,0,0,400,400);north.getPixels(n,0,400,0,0,400,400);west.getPixels(w,0,400,0,0,400,400);
   assertArrayEquals(id+" upright north impact",pixels,n);for(int y=0;y<400;y++)for(int x=0;x<400;x++)assertEquals(id+" west preserves vertical foot registration",pixels[y*400+x],w[y*400+399-x]);east.recycle();north.recycle();west.recycle();
  }
 }
 static void save(Bitmap b,String name)throws Exception{File f=new File("build/reports/device-review/"+name);f.getParentFile().mkdirs();try(FileOutputStream out=new FileOutputStream(f)){assertTrue(b.compress(Bitmap.CompressFormat.PNG,100,out));}}
}
