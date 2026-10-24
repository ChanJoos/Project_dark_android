package com.projectdark.mobile;
import static org.junit.Assert.*;
import android.graphics.*;import org.json.*;import org.junit.*;import org.junit.runner.RunWith;import org.robolectric.*;import org.robolectric.annotation.*;
@RunWith(RobolectricTestRunner.class) @Config(sdk=34,manifest=Config.NONE) @GraphicsMode(GraphicsMode.Mode.NATIVE)
public class MageVideoV238Test {
 final MageSourcesV235Test m=new MageSourcesV235Test();
 @Before public void setup(){m.setup();}
 @Test public void videoSequencesHaveRealChangingFramesAndScreenAliasesShareTheirSingleSource()throws Exception{
  ClassicSkillReference r=new ClassicSkillReference(m.h.c);Bitmap grid=Bitmap.createBitmap(900,700,Bitmap.Config.ARGB_8888);Canvas c=new Canvas(grid);c.drawColor(0xff555951);
  int row=0;for(int id:new int[]{26,27,28,29,46}){ClassicSkillReference.Channel ch=r.channel(m.mage(id),false);assertTrue(ch.sequence.durations.length>=5);assertTrue(ch.sequence.duration>.2f);assertTrue(ch.sequence.duration<1.6f);assertTrue(ch.sequence.atlas.getWidth()>ch.sequence.width);java.util.Set<Integer> frameHashes=new java.util.HashSet<>();int[] pixels=new int[ch.sequence.width*ch.sequence.height];int visible=0;
   for(int f=0;f<ch.sequence.durations.length;f++){ch.sequence.atlas.getPixels(pixels,0,ch.sequence.width,f%ch.sequence.columns*ch.sequence.width,f/ch.sequence.columns*ch.sequence.height,ch.sequence.width,ch.sequence.height);frameHashes.add(java.util.Arrays.hashCode(pixels));for(int p:pixels)if(Color.alpha(p)>0)visible++;}
   assertTrue(m.mage(id)+" observed changing source frames",frameHashes.size()>=4);assertTrue(visible>100);
   for(int f=0;f<8;f++)r.draw(c,ch,ch.sequence.duration*f/8,80+f*110,110+row*135);
   if(id<30)assertSame(ch,r.channel(m.mage(id+4),false));row++;
  }RogueRepairV232Test.save(grid,"v238-five-source-sequences");
 }
 @Test public void fourSingleElementQuickslotsKeepOneRecipientAndSingleCostWithVideoFrames()throws Exception{
  for(int n=26;n<=29;n++){final String id=m.mage(n);m.setup();GameView v=m.actor(m.mage(n));RuntimeState s=m.h.field(v,"state");RuntimeState.Monster first=s.monsters().get(0),extra=m.add(s,"not-target",s.player().x-32,s.player().y+16);int mp=s.player().mp;m.tap(v);RuntimeCombatSession session=m.h.field(v,"combatSession");int cost=session.effectiveMpCost(m.mage(n));m.contact(v);assertTrue(first.hp<first.maxHp);assertEquals(extra.maxHp,extra.hp);assertEquals(mp-cost,s.player().mp);SkillVfxRenderer fx=m.h.field(v,"skillVfx");assertEquals(1,fx.pulses.stream().filter(p->p.id.equals(id)&&!p.caster).count());m.h.capture(v,"v238-single-"+n);}
 }
 @Test public void screenFourUseSameVideoOnEachVisibleRecipientWithoutRepeatedMpCost()throws Exception{m.fourScreenElementsUseNormalQuickslotsHitEveryVisibleLiveMonsterOnceAndEmitThirdTierPerRecipient();m.screenElementRecipientsAreRecheckedAtContactWithoutOffscreenEffects();}
 @Test public void darkMarkUsesVideoAndRetainsSeventyCurseWithActualQuickslotContact()throws Exception{
  GameView v=m.actor(m.mage(46));RuntimeState s=m.h.field(v,"state");m.tap(v);m.contact(v);assertEquals(70,s.skillEffects().power(s.monsters().get(0).id,"CURSE"));SkillVfxRenderer fx=m.h.field(v,"skillVfx");for(SkillVfxRenderer.Pulse p:fx.pulses)if(p.id.equals(m.mage(46)))p.age=.65f;m.h.capture(v,"v238-dark-mark-normal-contact");
 }
 @Test public void sharedCombatAuditsRunWithAndroidRuntimeInsteadOfStubJar(){java.util.List<String> failures=new java.util.ArrayList<>();
   if(!CombatV3PipelineAudit.verify())failures.add("CombatV3PipelineAudit");
   if(!RuntimeCombatPortAdapterAudit.verify())failures.add("RuntimeCombatPortAdapterAudit");
   if(!MonsterAutoCombatBridgeAudit.verify())failures.add("MonsterAutoCombatBridgeAudit");
   if(!CombatConcurrentInterruptionAudit.verify())failures.add("CombatConcurrentInterruptionAudit");
   if(!MonsterDefeatIdempotencyAudit.verify())failures.add("MonsterDefeatIdempotencyAudit");
   assertTrue(failures.toString(),failures.isEmpty());}
}
