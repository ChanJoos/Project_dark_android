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
@RunWith(RobolectricTestRunner.class) @Config(sdk=34,manifest=Config.NONE) @GraphicsMode(GraphicsMode.Mode.NATIVE)
public class BasicRollbackV261Test {
 Context c;final FacingNumbersV260Test helper=new FacingNumbersV260Test();
 @Before public void setup(){helper.setup();c=RuntimeEnvironment.getApplication();}
 @Test public void allLearnedLinkedSkillsAndPreviewNeverCreateAFourthBasicHit()throws Exception{
  for(String[] row:new String[][]{{"WARRIOR","SK_전사_002","SK_전사_005","SK_전사_020"},{"ROGUE","SK_도적_008","SK_도적_024","SK_도적_031"},{"MARTIAL_ARTIST","SK_무도가_005","SK_무도가_027","SK_무도가_035"}}){
   c.getSharedPreferences("project_dark_skill_book_v1",0).edit().clear().commit();SkillBook b=SkillBook.load(c);b.bindJob(()->row[0]);assertEquals(1,b.basicHits());assertTrue(b.learn(row[1],100));assertEquals(2,b.basicHits());assertTrue(b.learn(row[2],100));assertEquals(3,b.basicHits());b.learn(row[3],100);assertEquals("other learned skill cannot add a fourth contact",3,b.basicHits());
   b.setTestAccess(true);assertEquals("all-skills preview is also capped",3,b.basicHits());SkillBook restored=SkillBook.load(c);restored.bindJob(()->row[0]);assertTrue(restored.restore(b.snapshot()));assertEquals(3,restored.basicHits());
  }
 }
 @Test public void oneSubmittedActionHasExactlyOneTwoOrThreeAtomicContactsAndNoLateFourth()throws Exception{
  for(int count=1;count<=3;count++)for(boolean auto:new boolean[]{false,true}){
   RuntimeState s=helper.clean();RuntimeState.Monster m=helper.add(s,1000000);RuntimeCombatSession x=new RuntimeCombatSession(s,(a,t)->true,(a,id)->true,a->true);final int total=count;x.setBasicHits(()->total);
   if(auto){CombatActionOrchestrator actions=helper.field(x,"actions");assertTrue(actions.submitAuto("player",m.id,RuntimeCombatSession.playerAttackActionId(0)).accepted());}else assertTrue(x.submitPlayerAttack(m.id,0).accepted());
   String action=RuntimeCombatSession.playerAttackActionId(0);assertTrue(x.tick(x.effectiveCastSeconds(action)-.001f).events.stream().noneMatch(e->e.type==CombatResolver.EventType.EFFECT_APPLIED));
   RuntimeCombatSession.FrameResult frame=x.tick(.002f);List<CombatResolver.Event> hits=new ArrayList<>();for(CombatResolver.Event e:frame.events)if(e.type==CombatResolver.EventType.EFFECT_APPLIED&&e.actorId.equals("player"))hits.add(e);
   assertEquals(count,hits.size());assertEquals(count,m.damageNumbers.hits().size());int damage=hits.get(0).amount;assertEquals(damage*count,1000000-m.hp);for(CombatResolver.Event e:hits)assertEquals(hits.get(0).actionSequence,e.actionSequence);
   assertTrue(x.tick(2).events.stream().noneMatch(e->e.type==CombatResolver.EventType.EFFECT_APPLIED));assertFalse(x.playerActionActive());
  }
 }
 @Test public void idleCooldownNeverAppliesTheRemovedOpponentFacingCorrection()throws Exception{
  for(CharacterRenderer.Direction facing:CharacterRenderer.Direction.values()){
   RuntimeState s=new RuntimeState();RuntimeState.Monster m=s.monsters().get(0);m.attackCooldown=10;m.provoked=true;m.visualFacing.setLocomotion(facing);s.player().x=m.x+32;s.player().y=m.y+16;float x=m.x,y=m.y;
   new MonsterAIController().tick(s,.016f);assertEquals(facing,m.visualFacing.presentation());assertEquals(x,m.x,0);assertEquals(y,m.y,0);
  }
 }
 @Test public void threeNumbersShareAnImpactThenFanIndependentlyWithoutPushingOldNumbers()throws Exception{
  HitNumbersV260 n=new HitNumbersV260();n.add(1433,0,0);n.add(1444,0,0);n.add(1421,0,0);assertEquals(3,n.hits().size());long group=n.hits().get(0).group;
  for(HitNumbersV260.Hit h:n.hits()){assertEquals(group,h.group);assertEquals(0,h.offsetX(),0);assertEquals(0,h.offsetY(),0);assertEquals(3,h.count);}
  n.tick(3*HitNumbersV260.STEP);assertTrue(n.hits().get(0).offsetY()<n.hits().get(1).offsetY());assertTrue(n.hits().get(1).offsetY()<n.hits().get(2).offsetY());float y=n.hits().get(0).offsetY();n.add(999,0,0);assertEquals(y,n.hits().get(0).offsetY(),0);
  n.tick(HitNumbersV260.LIFE);assertTrue(n.hits().isEmpty());
 }
 @Test public void measuredReferenceArcAndFadeHaveTheObservedRangeAndCadence()throws Exception{
  // Video 5.750..6.350s: first1433 template displacement from its first exposed frame.
  float[][] measured={{3,0,0},{4,9,-14},{5,18,-26},{6,28,-35},{7,37,-42},{8,46,-47},{9,56,-49},{10,65,-49},{11,74,-47},{12,84,-42}};
  HitNumbersV260 n=new HitNumbersV260();n.add(1433,0,0);n.add(1444,0,0);n.add(1421,0,0);HitNumbersV260.Hit h=n.hits().get(0);h.age=3*HitNumbersV260.STEP;float x=h.offsetX(),y=h.offsetY();
  for(float[] r:measured){h.age=r[0]*HitNumbersV260.STEP;assertEquals("source horizontal displacement",r[1],h.offsetX()-x,1.1f);assertEquals("source first-number arc",r[2],h.offsetY()-y,2.1f);}
  h.age=9*HitNumbersV260.STEP;assertEquals(1,h.alpha(),0);h.age=12*HitNumbersV260.STEP;assertTrue(h.alpha()<.3f);assertTrue(h.offsetX()>100);
 }
 @Test public void nativeReferenceFanRendersSeparateSourceTrackedTrajectories()throws Exception{
  OriginalDamageDigitsV260 digits=new OriginalDamageDigitsV260(c);Bitmap im=Bitmap.createBitmap(5*260,240,Bitmap.Config.ARGB_8888);Canvas canvas=new Canvas(im);canvas.drawColor(0xff24322d);HitNumbersV260 n=new HitNumbersV260();n.add(1433,0,0);n.add(1444,0,0);n.add(1421,0,0);
  int[] ages={0,3,6,9,12};for(int col=0;col<ages.length;col++)for(HitNumbersV260.Hit h:n.hits()){h.age=ages[col]*HitNumbersV260.STEP;digits.draw(canvas,h.text(),col*260+70+h.offsetX(),190+h.offsetY(),h.alpha());}
  helper.save(im,"v261-source-fan.png");
 }
}
