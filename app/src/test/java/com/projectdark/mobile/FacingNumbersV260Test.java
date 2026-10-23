package com.projectdark.mobile;
import static org.junit.Assert.*;
import android.content.Context;
import android.graphics.*;
import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.*;
@RunWith(RobolectricTestRunner.class) @Config(sdk=34,manifest=Config.NONE) @GraphicsMode(GraphicsMode.Mode.NATIVE)
public class FacingNumbersV260Test {
 Context c;final MartialHudAutoRegressionTest h=new MartialHudAutoRegressionTest();
 @Before public void setup(){h.setup();c=RuntimeEnvironment.getApplication();for(String p:new String[]{QuickItemBarV254.PREFS,"project_dark_skill_book_v1","project_dark_skill_test_v1"})c.getSharedPreferences(p,0).edit().clear().commit();}
 @SuppressWarnings("unchecked") <T>T field(Object o,String key)throws Exception{Field f=o.getClass().getDeclaredField(key);f.setAccessible(true);return(T)f.get(o);}
 @Test public void idleCooldownFacesItsOpponentInAllFourDirectionsWithoutMovingOrPausingCombat()throws Exception{
  for(CharacterRenderer.Direction d:CharacterRenderer.Direction.values()){
   GameView v=h.directed(d);RuntimeState s=field(v,"state");RuntimeState.Monster m=s.monsters().get(0);
   m.visualFacing.endAttack();m.visualFacing.setLocomotion(d);m.attackCooldown=10;float x=m.x,y=m.y;
   MonsterAIController ai=field(v,"monsterAi");ai.tick(s,.016f);
   assertEquals("idle cooldown tracks the actual opponent",100,BasicPositionDamageV259.percent(s.player(),m));
   assertEquals(x,m.x,0);assertEquals(y,m.y,0);assertFalse(m.isMoving);
   s.skillEffects().clear(m.id);m.attackCooldown=0;
   ai.tick(s,.016f);assertTrue("ordinary fight still attacks",m.attackPrimed||m.attackVisualRemaining>0);
   for(int i=0;i<45;i++){h.advance(v,.016f);assertEquals("stationary one-on-one stays front",100,BasicPositionDamageV259.percent(s.player(),m));}
  }
 }
 @Test public void movementAttackLocksDisabledHiddenAndAggroResetNeverTurnForPositionBonus()throws Exception{
  RuntimeState s=new RuntimeState();RuntimeState.Monster m=s.monsters().get(0);m.x=m.y=0;s.player().x=-32;s.player().y=-16;m.visualFacing.setLocomotion(CharacterRenderer.Direction.SE);
  m.isMoving=true;MonsterCombatFacingV260.track(s,m);assertEquals(CharacterRenderer.Direction.SE,m.visualFacing.presentation());m.isMoving=false;
  m.visualFacing.beginAttack(CharacterRenderer.Direction.SE);MonsterCombatFacingV260.track(s,m);assertEquals(CharacterRenderer.Direction.SE,m.visualFacing.presentation());m.visualFacing.endAttack();
  for(String effect:new String[]{"SLEEP","AGGRO_RESET"}){s.skillEffects().put(m.id,"qa",effect,1,10);MonsterCombatFacingV260.track(s,m);assertEquals(effect,CharacterRenderer.Direction.SE,m.visualFacing.presentation());s.skillEffects().clear(m.id);}
  s.skillEffects().put("player","qa","STEALTH",1,10);assertTrue(s.skillEffects().hidden());MonsterCombatFacingV260.track(s,m);assertEquals(CharacterRenderer.Direction.SE,m.visualFacing.presentation());s.skillEffects().clear("player");
  assertEquals(200,BasicPositionDamageV259.percent(s.player(),m));assertEquals(CharacterRenderer.Direction.SE,m.visualFacing.presentation());
  s.player().x=32;s.player().y=-16;assertEquals(150,BasicPositionDamageV259.percent(s.player(),m));assertEquals(CharacterRenderer.Direction.SE,m.visualFacing.presentation());
 }
 RuntimeState clean()throws Exception{RuntimeState s=new RuntimeState();((List<?>)field(s,"monsters")).clear();s.player().x=s.player().y=0;return s;}
 RuntimeState.Monster add(RuntimeState s,int hp)throws Exception{RuntimeState.Monster m=new RuntimeState.Monster("popup","popup",32,16,hp,"B/TEST");m.defenseElement="FIRE";m.visualFacing.setLocomotion(CharacterRenderer.Direction.NW);((List<RuntimeState.Monster>)field(s,"monsters")).add(m);return m;}
 @Test public void learnedDoubleAndTripleApplyTwoAndThreeEqualNumbersWithTripleTakingPriorityInManualAndAuto()throws Exception{
  for(String[] row:new String[][]{{"WARRIOR","SK_전사_002","SK_전사_005"},{"ROGUE","SK_도적_008","SK_도적_024"},{"MARTIAL_ARTIST","SK_무도가_005","SK_무도가_027"}})for(boolean auto:new boolean[]{false,true}){
   RuntimeState s=clean();RuntimeState.Monster m=add(s,1000000);SkillBook b=SkillBook.load(c);b.bindJob(()->row[0]);assertTrue(b.learn(row[1],100));assertEquals(2,b.basicHits());
   for(int count=2;count<=3;count++){
    if(count==3){assertTrue(b.learn(row[2],100));assertTrue(b.learned(row[1]));assertEquals("triple wins, never2+3",3,b.basicHits());}
    m.damageNumbers.clear();RuntimeCombatSession session=new RuntimeCombatSession(s,(a,t)->true,(a,id)->true,a->true);session.setBasicHits(b::basicHits);String id=RuntimeCombatSession.playerAttackActionId(0);int hp=m.hp;
    if(auto){CombatActionOrchestrator actions=field(session,"actions");assertTrue(actions.submitAuto("player",m.id,id).accepted());}else assertTrue(session.submitPlayerAttack(m.id,0).accepted());
    session.tick(session.effectiveCastSeconds(id)+.001f);
    int unit=hp-m.hp;assertTrue(unit>0);assertEquals(1,m.damageNumbers.hits().size());
    for(int n=2;n<=count;n++){session.tick(BasicAttackTiming.HIT_SPACING);assertEquals(unit*n,hp-m.hp);assertEquals(n,m.damageNumbers.hits().size());}
    for(HitNumbersV260.Hit hit:m.damageNumbers.hits()){assertEquals(unit,hit.amount);assertEquals(""+unit,hit.text());}
    assertFalse(session.playerActionActive());
   }
  }
 }
 @Test public void lowFrameRateKeepsEveryPopupAndTheirTotalEqualToActualHpLoss()throws Exception{
  RuntimeState s=clean();RuntimeState.Monster m=add(s,1000000);RuntimeCombatSession session=new RuntimeCombatSession(s,(a,t)->true,(a,id)->true,a->true);session.setBasicHits(()->3);int hp=m.hp;assertTrue(session.submitPlayerAttack(m.id,0).accepted());session.tick(1);
  assertEquals(3,m.damageNumbers.hits().size());int total=0;float prior=-Float.MAX_VALUE;for(HitNumbersV260.Hit hit:m.damageNumbers.hits()){total+=hit.amount;assertTrue(hit.offsetY()>prior);prior=hit.offsetY();}
  assertEquals(hp-m.hp,total);
 }
 @Test public void lethalHitStillHasItsNumberWhileFurtherHitsCannotDuplicateDefeatOrPopup()throws Exception{
  RuntimeState s=clean();RuntimeState.Monster m=add(s,1);RuntimeCombatSession session=new RuntimeCombatSession(s,(a,t)->true,(a,id)->true,a->true);session.setBasicHits(()->3);assertTrue(session.submitPlayerAttack(m.id,0).accepted());session.tick(1);assertFalse(m.alive);assertEquals(1,m.damageNumbers.hits().size());
  assertEquals(1,s.ledger().snapshot().stream().filter(e->e.type==CombatLedger.Type.MONSTER_DEFEATED).count());s.damage(m,10);assertEquals(1,m.damageNumbers.hits().size());m.damageNumbers.tick(HitNumbersV260.LIFE);assertTrue(m.damageNumbers.hits().isEmpty());
 }
 @Test public void popupLifetimesStackingAndCapacityAreBoundedWithoutChangingDamage()throws Exception{
  HitNumbersV260 n=new HitNumbersV260();n.add(0,0,0);assertTrue(n.hits().isEmpty());n.add(10,1,2);HitNumbersV260.Hit first=n.hits().get(0);n.tick(.1f);float y=first.offsetY();n.add(20,1,2);assertTrue(first.offsetY()<y);assertEquals(10,first.amount);assertEquals(20,n.hits().get(1).amount);assertEquals(1,first.alpha(),0);
  for(int i=0;i<100;i++)n.add(i+1,1,2);assertEquals(HitNumbersV260.LIMIT,n.hits().size());n.tick(1);assertTrue(n.hits().isEmpty());
 }
 @Test public void nativeGameViewShowsThreeIndependentNumbersAndKeepsTheKillingNumber()throws Exception{
  GameView v=h.directed(CharacterRenderer.Direction.SE);v.setSkillTestMode(false);RuntimeState s=field(v,"state");s.rpg().restoreProgression(11,0);assertTrue(s.rpg().restoreJobCode("WARRIOR"));SkillBook b=field(v,"skillBook");assertTrue(b.learn("SK_전사_002",100));assertTrue(b.learn("SK_전사_005",100));assertTrue(b.learn("SK_공통_001",100));
  RuntimeState.Monster m=s.monsters().get(0);m.visualFacing.endAttack();MonsterCombatFacingV260.track(s,m);h.call(v,"useBookSkill",SkillBook.Entry.class,b.get("SK_공통_001"));
  boolean sawThree=false;for(int i=0;i<24;i++){h.advance(v,.04f);save(v,"combo-"+String.format(java.util.Locale.ROOT,"%02d",i));sawThree|=m.damageNumbers.hits().size()==3;}assertTrue("actual basic contact produces three independent numbers",sawThree);
  m.hp=1;s.damage(m,20);Bitmap with=frame(v);m.damageNumbers.clear();Bitmap without=frame(v);assertFalse("dead target keeps its final floating number",with.sameAs(without));save(with,"lethal.png");
 }
 @Test public void originalPixelFontRendersStableDigitsWithoutSystemFontFallback()throws Exception{
  OriginalDamageDigitsV260 digits=new OriginalDamageDigitsV260(c);Bitmap im=Bitmap.createBitmap(480,100,Bitmap.Config.ARGB_8888);Canvas out=new Canvas(im);out.drawColor(0xff24322d);digits.draw(out,"0123456789",240,65,1);save(im,"digits.png");
  int changed=0;for(int y=0;y<100;y++)for(int x=0;x<480;x++)if(im.getPixel(x,y)!=0xff24322d)changed++;assertTrue(changed>150);assertTrue(changed<3000);
 }
 Bitmap frame(GameView v){Bitmap im=Bitmap.createBitmap(960,540,Bitmap.Config.ARGB_8888);v.draw(new Canvas(im));return im;}
 void save(GameView v,String name)throws Exception{save(frame(v),name+".png");}
 void save(Bitmap im,String name)throws Exception{File f=new File("build/reports/horror-v179/v260/"+name);f.getParentFile().mkdirs();try(FileOutputStream out=new FileOutputStream(f)){assertTrue(im.compress(Bitmap.CompressFormat.PNG,100,out));}}
}
