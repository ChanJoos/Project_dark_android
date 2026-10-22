package com.projectdark.mobile;
import static org.junit.Assert.*;
import android.graphics.*;import org.json.*;import org.junit.*;import org.junit.runner.RunWith;import org.robolectric.*;import org.robolectric.annotation.*;import java.util.*;
@RunWith(RobolectricTestRunner.class) @Config(sdk=34,manifest=Config.NONE) @GraphicsMode(GraphicsMode.Mode.NATIVE)
public class MageCircle4V242Test {
 final MageSourcesV235Test m=new MageSourcesV235Test();
 @Before public void setup(){m.setup();}
 @Test public void allEfaFramesUseOriginalIntervalsFullEnvelopeAndShareOneAtlasWithScreen()throws Exception{
  ClassicSkillReference r=new ClassicSkillReference(m.h.c);int[] counts={9,12,5,14},intervals={70,52,85,65};long bytes=0;Bitmap grid=Bitmap.createBitmap(1600,800,Bitmap.Config.ARGB_8888);Canvas c=new Canvas(grid);c.drawColor(0xff4f554e);
  for(int e=0;e<4;e++){
   String id=m.mage(26+e);ClassicSkillReference.Channel ch=r.channel(id,false);assertSame(ch,r.channel(m.mage(30+e),false));CapturedSkillFx.Sequence q=ch.sequence;assertFalse(q.normalBlend);assertEquals(1f,q.scale,0);assertEquals(0f,q.footOffsetY,0);assertNull(r.mageDisplayAtlas(ch));assertEquals(counts[e],q.durations.length);bytes+=q.atlas.getAllocationByteCount();int time=0;boolean beyondTile=false;
   Bitmap rendered=Bitmap.createBitmap(600,600,Bitmap.Config.ARGB_8888);Set<Integer> hashes=new HashSet<>();
   for(int f=0;f<q.durations.length;f++){
    assertEquals(intervals[e],q.durations[f]);rendered.eraseColor(Color.TRANSPARENT);r.draw(new Canvas(rendered),ch,(time+1)/1000f,300,300);int sx=f%q.columns*q.width,sy=f/q.columns*q.height,dx=300-(int)q.pivotX,dy=300-(int)q.pivotY;int[] pixels=new int[q.width*q.height];q.atlas.getPixels(pixels,0,q.width,sx,sy,q.width,q.height);hashes.add(Arrays.hashCode(pixels));
    for(int y=0;y<q.height;y++)for(int x=0;x<q.width;x++){
     int source=pixels[y*q.width+x];assertEquals(id+" frame"+f+" completeRGBA",source,rendered.getPixel(dx+x,dy+y));
     if(Color.alpha(source)>0&&(Math.abs(dx+x-300)>32||Math.abs(dy+y-300)>16))beyondTile=true;
     if(x<2||y<2||x>=q.width-2||y>=q.height-2)assertEquals(0,Color.alpha(source));
    }time+=q.durations[f];
   }assertEquals(counts[e],hashes.size());assertTrue(id+" actual pixels extend outsideone tile",beyondTile);
   for(int f=0;f<8;f++)r.draw(c,ch,q.duration*f/8f,100+f*200,180+e*195);
  }assertTrue(bytes<10000000L);RogueRepairV232Test.save(grid,"v242-four-original-circle4-sequences");
 }
 private void seekAndCapture(GameView v,String id,String name)throws Exception{
  ClassicSkillReference.Channel ch=new ClassicSkillReference(m.h.c).channel(id,false);SkillVfxRenderer fx=m.h.field(v,"skillVfx");for(SkillVfxRenderer.Pulse p:fx.pulses)if(!p.caster&&p.id.equals(id))p.age=ch.sequence.duration*.4f;m.h.capture(v,name);
 }
 @Test public void eightNormalQuickslotsKeepSingleVersusAllVisibleRecipientsOneMpChargeAndSharedOriginalFx()throws Exception{
  for(int e=0;e<4;e++)for(boolean screen:new boolean[]{false,true}){
   m.setup();String id=m.mage((screen?30:26)+e);GameView v=m.actor(id);RuntimeState s=m.h.field(v,"state");RuntimeState.Monster first=s.monsters().get(0),second=m.add(s,"second",s.player().x-32,s.player().y+16),third=m.add(s,"third",s.player().x+96,s.player().y+48),outside=m.add(s,"outside",s.player().x+10000,s.player().y),dead=m.add(s,"dead",s.player().x+64,s.player().y+32);dead.alive=false;int mp=s.player().mp;m.tap(v);RuntimeCombatSession session=m.h.field(v,"combatSession");int cost=session.effectiveMpCost(id);assertEquals(mp-cost,s.player().mp);m.contact(v);
   assertTrue(first.hp<first.maxHp);assertEquals(outside.maxHp,outside.hp);assertEquals(dead.maxHp,dead.hp);assertEquals(mp-cost,s.player().mp);
   if(screen){assertTrue(second.hp<second.maxHp);assertTrue(third.hp<third.maxHp);}else{assertEquals(second.maxHp,second.hp);assertEquals(third.maxHp,third.hp);}
   SkillVfxRenderer fx=m.h.field(v,"skillVfx");Set<String> actual=new HashSet<>();int pulses=0;for(SkillVfxRenderer.Pulse p:fx.pulses)if(!p.caster&&p.id.equals(id)){assertEquals("classic",p.sheet);assertTrue(actual.add(p.anchor));pulses++;}assertEquals(screen?3:1,pulses);assertEquals(screen?new HashSet<>(Arrays.asList(first.id,second.id,third.id)):Collections.singleton(first.id),actual);seekAndCapture(v,id,"v242-"+(screen?"screen":"single")+"-"+e);
  }
 }
 @Test public void allFourScreenRecipientsRecheckDeathAndVisibilityAtActualContact()throws Exception{
  for(int e=0;e<4;e++){
   m.setup();String id=m.mage(30+e);GameView v=m.actor(id);RuntimeState s=m.h.field(v,"state");RuntimeState.Monster moved=s.monsters().get(0),live=m.add(s,"live",s.player().x-32,s.player().y+16),died=m.add(s,"died",s.player().x+64,s.player().y+32);m.tap(v);moved.x+=10000;died.alive=false;m.contact(v);assertEquals(moved.maxHp,moved.hp);assertEquals(died.maxHp,died.hp);assertTrue(live.hp<live.maxHp);SkillVfxRenderer fx=m.h.field(v,"skillVfx");List<SkillVfxRenderer.Pulse> pulses=new ArrayList<>();for(SkillVfxRenderer.Pulse p:fx.pulses)if(!p.caster&&p.id.equals(id))pulses.add(p);assertEquals(1,pulses.size());assertEquals(live.id,pulses.get(0).anchor);
  }
 }
}
