package com.projectdark.mobile;
import static org.junit.Assert.*;
import android.graphics.*;import org.junit.*;import org.junit.runner.RunWith;import org.robolectric.*;import org.robolectric.annotation.*;
@RunWith(RobolectricTestRunner.class) @Config(sdk=34,manifest=Config.NONE) @GraphicsMode(GraphicsMode.Mode.NATIVE)
public class MageBasicV240Test {
 final MageSourcesV235Test m=new MageSourcesV235Test();
 @Before public void setup(){m.setup();}
 @Test public void basicFourReachNormalSingleContactWithChangingRecordedFramesAndOneMpCharge()throws Exception{
  ClassicSkillReference r=new ClassicSkillReference(m.h.c);Bitmap strip=Bitmap.createBitmap(1000,600,Bitmap.Config.ARGB_8888);Canvas canvas=new Canvas(strip);canvas.drawColor(0xff454b48);int row=0;long decoded=0;
  for(int n:new int[]{1,3,4,5}){
   m.setup();String id=m.mage(n);ClassicSkillReference.Channel ch=r.channel(id,false);assertNotNull(ch);assertTrue(ch.sequence.durations.length>=10);assertEquals(0f,ch.sequence.footOffsetY,0f);decoded+=ch.sequence.atlas.getAllocationByteCount();java.util.Set<Integer> hashes=new java.util.HashSet<>();int[] pixels=new int[ch.sequence.width*ch.sequence.height];
   for(int f=0;f<ch.sequence.durations.length;f++){ch.sequence.atlas.getPixels(pixels,0,ch.sequence.width,f%ch.sequence.columns*ch.sequence.width,f/ch.sequence.columns*ch.sequence.height,ch.sequence.width,ch.sequence.height);hashes.add(java.util.Arrays.hashCode(pixels));}assertTrue(id,hashes.size()>=7);
   for(int f=0;f<8;f++)r.draw(canvas,ch,ch.sequence.duration*f/8,70+f*120,130+row*145);row++;
   GameView v=m.actor(id);RuntimeState s=m.h.field(v,"state");RuntimeState.Monster first=s.monsters().get(0),other=m.add(s,"not-target",s.player().x-32,s.player().y+16);int mp=s.player().mp;m.tap(v);RuntimeCombatSession session=m.h.field(v,"combatSession");int cost=session.effectiveMpCost(id);assertEquals(mp-cost,s.player().mp);m.contact(v);assertTrue(first.hp<first.maxHp);assertEquals(other.maxHp,other.hp);assertEquals(mp-cost,s.player().mp);SkillVfxRenderer fx=m.h.field(v,"skillVfx");assertEquals(1,fx.pulses.stream().filter(p->!p.caster&&p.id.equals(id)).count());for(SkillVfxRenderer.Pulse p:fx.pulses)if(!p.caster&&p.id.equals(id))p.age=ch.sequence.duration*.5f;m.h.capture(v,"v240-basic-normal-"+n);
  }assertTrue(decoded<4000000);RogueRepairV232Test.save(strip,"v240-basic4-recorded-frames");
 }
 @Test public void userMappingSharesCircleThreeWithScreenFourAndRestoresIndependentCircleFourSingles(){
  ClassicSkillReference r=new ClassicSkillReference(m.h.c);
  for(int i=0;i<4;i++){assertEquals(m.mage(13+i),SkillPolicyV227.effectSource(m.mage(30+i)));assertSame(r.channel(m.mage(13+i),false),r.channel(m.mage(30+i),false));assertNotSame(r.channel(m.mage(13+i),false),r.channel(m.mage(26+i),false));assertEquals(m.mage(26+i),SkillPolicyV227.effectSource(m.mage(26+i)));}
 }
}
