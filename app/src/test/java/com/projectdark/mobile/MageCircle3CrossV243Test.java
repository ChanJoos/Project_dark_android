package com.projectdark.mobile;
import static org.junit.Assert.*;
import android.graphics.*;import com.projectdark.mobile.world.*;import org.junit.*;import org.junit.runner.RunWith;import org.robolectric.*;import org.robolectric.annotation.*;import java.util.*;
@RunWith(RobolectricTestRunner.class) @Config(sdk=34,manifest=Config.NONE) @GraphicsMode(GraphicsMode.Mode.NATIVE)
public class MageCircle3CrossV243Test {
 final MageSourcesV235Test m=new MageSourcesV235Test();
 static final int[] BASIC={1,3,4,5};static final int[][] DIR={{32,16},{-32,-16},{-32,16},{32,-16}};
 @Before public void setup(){m.setup();}
 private void capture(GameView v,String id,String name)throws Exception{
  SkillVfxRenderer fx=m.h.field(v,"skillVfx");ClassicSkillReference.Channel ch=new ClassicSkillReference(m.h.c).channel(id,false);for(SkillVfxRenderer.Pulse p:fx.pulses)if(!p.caster&&p.id.equals(id))p.age=ch.sequence.duration*.45f;m.h.capture(v,name);
 }
 private Set<String> recipients(GameView v,String id)throws Exception{
  SkillVfxRenderer fx=m.h.field(v,"skillVfx");Set<String> actual=new HashSet<>();for(SkillVfxRenderer.Pulse p:fx.pulses)if(!p.caster&&p.id.equals(id)){assertEquals("classic",p.sheet);assertTrue("One pulse per recipient",actual.add(p.anchor));}return actual;
 }
 private int singleDamage(int sid)throws Exception{
  m.setup();String id=m.mage(sid);GameView v=m.actor(id);RuntimeState s=m.h.field(v,"state");RuntimeState.Monster t=s.monsters().get(0);m.tap(v);m.contact(v);assertTrue(t.hp<t.maxHp);return t.maxHp-t.hp;
 }
 private void openCenter(GameView v)throws Exception{
  RuntimeState s=m.h.field(v,"state");WorldRuntimeAdapter w=m.h.field(v,"worldAdapter");boolean placed=false;
  List<WorldMoveTargetController.TileCenter> tiles=new ArrayList<>(w.navigationTiles());tiles.sort(Comparator.comparingDouble(t->Math.hypot(t.x-s.player().x,t.y-s.player().y)));
  for(WorldMoveTargetController.TileCenter t:tiles){
   boolean ok=w.canPlayerOccupy(t.x,t.y);for(int[] d:DIR)ok&=w.canPlayerOccupy(t.x+d[0],t.y+d[1])&&w.hasLineOfSight(t.x,t.y,t.x+d[0],t.y+d[1]);
   if(ok){s.player().x=t.x;s.player().y=t.y;placed=true;break;}
  }assertTrue("Four legal adjacent LOS tiles",placed);w.snapCameraToPlayer();((List<?>)m.h.field(s,"monsters")).clear();((CombatController)m.h.field(v,"combat")).clearTarget();
 }
 @Test public void originalFramesKeepEveryPixelHeaderTimingFootBoundsAndActualAdditiveBlending()throws Exception{
  ClassicSkillReference ref=new ClassicSkillReference(m.h.c);int[] counts={9,12,4,11},ms={80,65,110,75};long bytes=0;Bitmap grid=Bitmap.createBitmap(1600,800,Bitmap.Config.ARGB_8888);Canvas gridCanvas=new Canvas(grid);gridCanvas.drawColor(0xff273447);
  for(int e=0;e<4;e++){
   String id=m.mage(13+e);ClassicSkillReference.Channel ch=ref.channel(id,false);CapturedSkillFx.Sequence q=ch.sequence;assertSame(ch,ref.channel(m.mage(18+e),false));assertEquals(counts[e],q.durations.length);assertFalse(q.normalBlend);assertEquals(e==2,q.additiveBlend);assertNull(ref.mageDisplayAtlas(ch));assertEquals(0,q.footOffsetY,0);assertEquals(1,q.scale,0);bytes+=q.atlas.getAllocationByteCount();int time=0;boolean outside=false;Set<Integer> hashes=new HashSet<>();
   Bitmap rendered=Bitmap.createBitmap(600,600,Bitmap.Config.ARGB_8888);
   for(int f=0;f<counts[e];f++){
    assertEquals(ms[e],q.durations[f]);int sx=f%q.columns*q.width,sy=f/q.columns*q.height;int[] source=new int[q.width*q.height];q.atlas.getPixels(source,0,q.width,sx,sy,q.width,q.height);hashes.add(Arrays.hashCode(source));rendered.eraseColor(Color.TRANSPARENT);ref.draw(new Canvas(rendered),ch,(time+1)/1000f,300,300);int dx=300-(int)q.pivotX,dy=300-(int)q.pivotY;
    for(int y=0;y<q.height;y++)for(int x=0;x<q.width;x++){
     int p=source[y*q.width+x];assertEquals(id+" frame"+f+" full originalRGBA",p,rendered.getPixel(dx+x,dy+y));if(Color.alpha(p)>0&&(Math.abs(dx+x-300)>32||Math.abs(dy+y-300)>16))outside=true;
     if(x<2||y<2||x>=q.width-2||y>=q.height-2)assertEquals(0,Color.alpha(p));
    }
    rendered.eraseColor(0xff3c5064);ref.draw(new Canvas(rendered),ch,(time+1)/1000f,300,300);
    for(int y=0;y<q.height;y++)for(int x=0;x<q.width;x++){
     int src=source[y*q.width+x];int[] a={Color.red(src),Color.green(src),Color.blue(src)},b={60,80,100};int dst=rendered.getPixel(dx+x,dy+y);int[] got={Color.red(dst),Color.green(dst),Color.blue(dst)};
     for(int k=0;k<3;k++){int expected=q.additiveBlend?Math.min(255,a[k]+b[k]):255-(255-a[k])*(255-b[k])/255;assertEquals(id+" correct destination-dependent blend",expected,got[k],1);}
    }time+=q.durations[f];
   }assertEquals(counts[e],hashes.size());assertTrue("Full original visible outside one tile",outside);
   for(int j=0;j<8;j++)ref.draw(gridCanvas,ch,q.duration*j/8f,100+j*200,180+e*195);
  }assertTrue(bytes<8000000L);RogueRepairV232Test.save(grid,"v243-four-original-circle3-sequences");
 }
 @Test public void fourNormalSingleQuickslotsHitOnlySelectedRecipientAndConsumeMpOnce()throws Exception{
  for(int e=0;e<4;e++){
   m.setup();String id=m.mage(13+e);GameView v=m.actor(id);RuntimeState s=m.h.field(v,"state");RuntimeState.Monster selected=s.monsters().get(0),other=m.add(s,"other",s.player().x-32,s.player().y+16);int mp=s.player().mp;RuntimeCombatSession c=m.h.field(v,"combatSession");m.tap(v);assertTrue(c.playerActionActive());assertEquals(mp-c.effectiveMpCost(id),s.player().mp);assertEquals(selected.maxHp,selected.hp);assertTrue(recipients(v,id).isEmpty());m.contact(v);assertTrue(selected.hp<selected.maxHp);assertEquals(selected.lastDamage,selected.maxHp-selected.hp);assertEquals(other.maxHp,other.hp);assertEquals(Collections.singleton(selected.id),recipients(v,id));assertEquals(mp-c.effectiveMpCost(id),s.player().mp);capture(v,id,"v243-single-"+e);
  }
 }
 @Test public void eightNormalCrossQuickslotsNeedNoTargetHitExactlyFourNeighborsAndMatchSinglePower()throws Exception{
  ClassicSkillReference ref=new ClassicSkillReference(m.h.c);
  for(int circle=2;circle<=3;circle++)for(int e=0;e<4;e++){
   int single=circle==2?BASIC[e]:13+e,expectedDamage=singleDamage(single);m.setup();String id=m.mage((circle==2?7:18)+e);GameView v=m.actor(id);openCenter(v);RuntimeState s=m.h.field(v,"state");SkillActionContract.Rule r=SkillActionContract.get(id);assertEquals(SkillActionContract.Pattern.CROSS,r.pattern);assertTrue(r.selfAnchored());assertFalse(r.needsSelection());assertEquals(1,r.reach);assertSame(ref.channel(m.mage(single),false),ref.channel(id,false));assertEquals(SkillAbilityCatalog.get(m.mage(single)).coefficient,SkillAbilityCatalog.get(id).coefficient,0);
   List<RuntimeState.Monster> targets=new ArrayList<>();for(int d=0;d<4;d++)targets.add(m.add(s,"neighbor-"+d,s.player().x+DIR[d][0],s.player().y+DIR[d][1]));
   RuntimeState.Monster diagonal=m.add(s,"diagonal",s.player().x+64,s.player().y),two=m.add(s,"two",s.player().x+64,s.player().y+32),center=m.add(s,"center",s.player().x,s.player().y),dead=m.add(s,"dead",s.player().x+32,s.player().y+16);dead.alive=false;
   int mp=s.player().mp,hp=s.player().hp;RuntimeCombatSession c=m.h.field(v,"combatSession");m.tap(v);assertTrue(id+" no target required",c.playerActionActive());assertEquals(mp-c.effectiveMpCost(id),s.player().mp);for(RuntimeState.Monster t:targets)assertEquals(t.maxHp,t.hp);assertTrue(recipients(v,id).isEmpty());m.contact(v);Set<String> expected=new HashSet<>();for(RuntimeState.Monster t:targets){expected.add(t.id);assertEquals(id+" same circle single final damage",expectedDamage,t.maxHp-t.hp);assertEquals(expectedDamage,t.lastDamage);}assertEquals(expected,recipients(v,id));for(RuntimeState.Monster t:new RuntimeState.Monster[]{diagonal,two,center,dead})assertEquals(t.maxHp,t.hp);assertEquals(hp,s.player().hp);assertEquals(mp-c.effectiveMpCost(id),s.player().mp);capture(v,id,"v243-circle"+circle+"-cross-"+e);
  }
 }
 @Test public void crossesIgnoreRemoteSelectionAndRecheckLiveShapeDeathAndVisibilityAtContact()throws Exception{
  for(int start:new int[]{7,18})for(int e=0;e<4;e++){
   m.setup();String id=m.mage(start+e);GameView v=m.actor(id);openCenter(v);RuntimeState s=m.h.field(v,"state");RuntimeState.Monster moved=m.add(s,"moved",s.player().x+32,s.player().y+16),died=m.add(s,"died",s.player().x-32,s.player().y-16),invisible=m.add(s,"invisible",s.player().x-32,s.player().y+16),entered=m.add(s,"entered",s.player().x+64,s.player().y+32),remote=m.add(s,"remote-selected",s.player().x+128,s.player().y+64),remoteNeighbor=m.add(s,"remote-neighbor",s.player().x+160,s.player().y+80);((CombatController)m.h.field(v,"combat")).selectTarget(remote);RuntimeCombatSession c=m.h.field(v,"combatSession");c.setSkillVisibility(t->!t.equals(invisible.id));m.tap(v);moved.x+=32;moved.y+=16;died.alive=false;entered.x=s.player().x+32;entered.y=s.player().y-16;m.contact(v);
   assertTrue(entered.hp<entered.maxHp);assertEquals(Collections.singleton(entered.id),recipients(v,id));for(RuntimeState.Monster t:new RuntimeState.Monster[]{moved,died,invisible,remote,remoteNeighbor})assertEquals(t.maxHp,t.hp);
  }
 }
 @Test public void eightEmptyCrossCastsNeverDamageCasterAndInsufficientMpRejectsBeforeAnyEffect()throws Exception{
  for(int start:new int[]{7,18})for(int e=0;e<4;e++){
   m.setup();String id=m.mage(start+e);GameView v=m.actor(id);openCenter(v);RuntimeState s=m.h.field(v,"state");int hp=s.player().hp,mp=s.player().mp;RuntimeCombatSession c=m.h.field(v,"combatSession");m.tap(v);assertTrue(c.playerActionActive());m.contact(v);assertEquals(hp,s.player().hp);assertEquals(mp-c.effectiveMpCost(id),s.player().mp);assertTrue(recipients(v,id).isEmpty());
   m.setup();v=m.actor(id);s=m.h.field(v,"state");s.player().mp=0;RuntimeState.Monster t=s.monsters().get(0);m.tap(v);c=m.h.field(v,"combatSession");assertFalse(c.playerActionActive());assertEquals(0,s.player().mp);assertEquals(t.maxHp,t.hp);assertTrue(recipients(v,id).isEmpty());
  }
 }
}
