package com.projectdark.mobile;
import static org.junit.Assert.*;
import android.content.Context;
import android.graphics.*;
import android.view.MotionEvent;
import java.lang.reflect.*;
import java.io.*;
import java.util.*;
import org.json.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.*;
@RunWith(RobolectricTestRunner.class) @Config(sdk=34,manifest=Config.NONE) @GraphicsMode(GraphicsMode.Mode.NATIVE)
public class SkillAbilityRuntimeTest {
 Context context;
 @Before public void before(){context=RuntimeEnvironment.getApplication();context.getSharedPreferences("project_dark_f5m_v1",0).edit().clear().commit();F5mSaveStore.install(context);}
 RuntimeState state()throws Exception{RuntimeState s=new RuntimeState();s.rpg().restoreStats(50,50,50,50,50,0);s.rpg().restoreProgression(80,0);s.rpg().restoreBaseResources(20000,20000);s.applyDerivedGrowth();s.player().hp=20000;s.player().mp=20000;s.player().x=s.player().y=0;List<RuntimeState.Monster> list=field(s,"monsters");list.clear();int i=0;for(float[] d:new float[][]{{32,16},{-32,-16},{32,-16},{-32,16},{64,0}})list.add(new RuntimeState.Monster("m"+i++,"fixture",d[0],d[1],1000000,"ADAPTED_TEST"));return s;}
 RuntimeCombatSession session(RuntimeState s){return new RuntimeCombatSession(s,(a,t)->true,(a,id)->true,a->true);}
 @Test public void everyCatalogIdHasAnExplicitAbilityAndDamageScalesByRealStats()throws Exception{
  assertEquals(221,SkillAbilityCatalog.all().size());RuntimeState s=state();FinalStats low=s.rpg().finalStats();s.rpg().restoreStats(99,99,99,99,99,0);FinalStats high=s.rpg().finalStats();int offensive=0,heals=0;
  for(SkillAbilityCatalog.Ability a:SkillAbilityCatalog.all()){
   assertNotNull(a.id,a.evidence);assertFalse(a.id,a.description.isEmpty());
   if(a.damage()||a.heal()){
    int n=SkillAbilityCatalog.amount(a,new SkillAbilityCatalog.Snapshot(low,20000,20000,80,100,false));
    int h=SkillAbilityCatalog.amount(a,new SkillAbilityCatalog.Snapshot(high,40000,40000,99,100,false));
    assertTrue(a.id+" positive",n>0);assertTrue(a.id+" monotonic",h>=n);if(a.damage())offensive++;else heals++;
   }
  }assertEquals(77,offensive);assertEquals(16,heals);
  assertTrue(ordinary("SK_무도가_007",low)>ordinary("SK_무도가_002",low));assertTrue(ordinary("SK_마법사_026",low)>ordinary("SK_마법사_001",low));
 }
 int ordinary(String id,FinalStats s){return SkillAbilityCatalog.amount(SkillAbilityCatalog.get(id),new SkillAbilityCatalog.Snapshot(s,20000,20000,80,100,false));}
 @Test public void suppliedClassicAnchorsAreExactAndThresholdsNeverBecomeMinimumOneDamage()throws Exception{
  RuntimeState s=state();FinalStats stats=s.rpg().finalStats();SkillAbilityCatalog.Snapshot x=new SkillAbilityCatalog.Snapshot(stats,16000,7000,99,100,false);
  assertEquals(72873,SkillAbilityCatalog.amount(SkillAbilityCatalog.get("SK_무도가_020"),x));assertEquals(3178,SkillAbilityCatalog.amount(SkillAbilityCatalog.get("SK_마법사_037"),x));
  assertEquals(6824,SkillAbilityCatalog.amount(SkillAbilityCatalog.get("SK_마법사_034"),x));assertEquals(4800,SkillAbilityCatalog.amount(SkillAbilityCatalog.get("SK_무도가_023"),x));
  assertEquals(0,SkillAbilityCatalog.amount(SkillAbilityCatalog.get("SK_마법사_043"),x));assertEquals(67600,ordinary("SK_전사_015",stats));
  assertEquals(0,CombatStatPipeline.resolveSkill(0,CombatStatPipeline.Channel.PHYSICAL,stats,stats).applied);
 }
 @Test public void all77DamageActionsActuallyReachTheResolverWithTheirOwnFormulas()throws Exception{
  Set<Integer> distinct=new HashSet<>();
  for(SkillAbilityCatalog.Ability a:SkillAbilityCatalog.all())if(a.damage()){
   RuntimeState s=state();RuntimeCombatSession c=session(s);SkillActionContract.Rule r=SkillActionContract.get(a.id);
   if(a.formula.equals("CRASH"))s.player().hp=1;
   s.skillEffects().put("player","SK_도적_011","STEALTH",1,30);
   if(a.status.equals("DRAIN"))s.monsters().get(0).hp=100;
   if(r.minReach>1){s.monsters().get(0).x=32*r.minReach;s.monsters().get(0).y=16*r.minReach;}
   String target=r.selfAnchored()?"player":"m0";assertTrue(a.id,c.submitPlayer(target,a.id).accepted());
   List<CombatResolver.Event> events=c.tick(r.contact+.001f).events;
   CombatResolver.Event hit=events.stream().filter(e->e.type==CombatResolver.EventType.HIT_FEEDBACK&&e.amount>0).findFirst().orElse(null);assertNotNull(a.id,hit);distinct.add(hit.amount);
  }assertTrue("skills no longer share a flat fallback",distinct.size()>35);
 }
 @Test public void spinQuickslotStartsWithoutSelectionFacingOrApproachAndHitsOnlyFourNeighbors()throws Exception{
  for(CharacterRenderer.Direction direction:CharacterRenderer.Direction.values()){
   before();GameView view=new GameView(context);view.layout(0,0,960,540);view.setSkillTestMode(true);RuntimeState s=field(view,"state");SkillBook book=field(view,"skillBook");
   RuntimeState.Monster original=s.monsters().get(0);float x=original.x-32,y=original.y-16;s.player().x=x;s.player().y=y;
   List<RuntimeState.Monster> list=field(s,"monsters");list.clear();int i=0;for(float[] d:new float[][]{{32,16},{-32,-16},{32,-16},{-32,16},{64,0},{320,160}})list.add(new RuntimeState.Monster("spin"+i++,"fixture",x+d[0],y+d[1],10000,"B"));
   ((CanonicalActorFacing)field(view,"playerFacing")).setLocomotion(direction);CombatController combat=field(view,"combat");if(direction.ordinal()%2==0)combat.selectTarget(list.get(5));else combat.clearTarget();
   ((com.projectdark.mobile.world.WorldRuntimeAdapter)field(view,"worldAdapter")).snapCameraToPlayer();book.assign(0,"SK_무도가_014");
   Method slot=GameView.class.getDeclaredMethod("slotRect",int.class);slot.setAccessible(true);RectF button=(RectF)slot.invoke(view,0);MotionEvent input=MotionEvent.obtain(0,0,MotionEvent.ACTION_DOWN,button.centerX(),button.centerY(),0);view.onTouchEvent(input);input.recycle();
   assertNull(((SkillApproachController)field(view,"skillApproach")).skillId);assertEquals("SK_무도가_014",field(view,"activeSkillVisualId"));assertEquals(direction,((CanonicalActorFacing)field(view,"playerFacing")).locomotion());
   tick(view,.139f);for(RuntimeState.Monster m:list)assertEquals(10000,m.hp);tick(view,.002f);for(i=0;i<list.size();i++)assertEquals("four axes "+direction+" "+i,i<4,list.get(i).hp<10000);
   Bitmap image=Bitmap.createBitmap(960,540,Bitmap.Config.ARGB_8888);view.draw(new Canvas(image));File f=new File("build/reports/device-review/v83-spin-four-neighbors-"+direction+".png");f.getParentFile().mkdirs();try(FileOutputStream out=new FileOutputStream(f)){image.compress(Bitmap.CompressFormat.PNG,100,out);}
  }
 }
 @Test public void areaUltimateChargesOnceUsesOneResourceSnapshotAndNoTargetCostsNothing()throws Exception{
  RuntimeState s=state();RuntimeCombatSession c=session(s);assertTrue(c.submitPlayer("player","SK_마법사_037").accepted());assertEquals(20000,s.player().mp);List<CombatResolver.Event> events=c.tick(.25f).events;
  List<Integer> amounts=new ArrayList<>();for(CombatResolver.Event e:events)if(e.type==CombatResolver.EventType.HIT_FEEDBACK)amounts.add(e.amount);
  assertEquals(5,amounts.size());assertEquals(1,new HashSet<>(amounts).size());assertEquals(9080,(int)amounts.get(0));assertEquals(0,s.player().mp);
  RuntimeState empty=state();((List<?>)field(empty,"monsters")).clear();RuntimeCombatSession none=session(empty);assertFalse(none.submitPlayer("player","SK_마법사_037").accepted());assertEquals(20000,empty.player().mp);assertEquals(0,none.cooldownRemaining("player","SK_마법사_037"),0);
 }
 @Test public void daraWaitsThenUsesReleaseResourcesOnceAndCancellationDoesNotDrain()throws Exception{
  RuntimeState s=state();RuntimeCombatSession c=session(s);assertTrue(c.submitPlayer("m0","SK_무도가_020").accepted());c.tick(2.99f);assertEquals(20000,s.player().hp);assertEquals(20000,s.player().mp);s.player().hp=16000;s.player().mp=7000;
  List<CombatResolver.Event> e=c.tick(.02f).events;assertEquals(1,s.player().hp);assertEquals(1,s.player().mp);assertTrue(e.stream().anyMatch(x->x.type==CombatResolver.EventType.HIT_FEEDBACK&&x.amount==72873));
  RuntimeState cancelled=state();RuntimeCombatSession no=session(cancelled);assertTrue(no.submitPlayer("m0","SK_무도가_020").accepted());cancelled.monsters().get(0).alive=false;assertTrue(no.tick(3.01f).events.stream().anyMatch(x->x.type==CombatResolver.EventType.ACTION_CANCELLED));assertEquals(20000,cancelled.player().hp);assertEquals(20000,cancelled.player().mp);
 }
 @Test public void completeDefenseBlocksPhysicalButMagicStillHitsAndDalmaIgnoresDefense()throws Exception{
  RuntimeState s=state();RuntimeCombatSession c=session(s);assertTrue(c.submitPlayer("player","SK_전사_014").accepted());c.tick(.15f);assertTrue(s.skillEffects().has("player","PHYSICAL_GUARD"));
  RuntimeCombatPortAdapter p=new RuntimeCombatPortAdapter(s,(a,t)->true,(a,id)->true);int hp=s.player().hp;assertEquals(0,p.applyDamage("m0","player",RuntimeCombatSession.MONSTER_BASIC_ACTION_ID,100).appliedAmount);assertEquals(hp,s.player().hp);
  assertTrue(p.applyDamage("m0","player","cast_proto",100).appliedAmount>0);
  s.skillEffects().put("m0","SK_무도가_016","INVINCIBLE",1,9);s.skillEffects().put("m0","SK_무도가_013","ARMOR",99,30);s.player().hp=20000;
  assertTrue(c.submitPlayer("m0","SK_무도가_023").accepted());List<CombatResolver.Event> events=c.tick(.15f).events;assertTrue(events.stream().anyMatch(x->x.type==CombatResolver.EventType.HIT_FEEDBACK&&x.amount==6000));assertEquals(8000,s.player().hp);
 }
 @Test public void cursesBuffsFreezePoisonDispelAndExpiryChangeRealCombat()throws Exception{
  RuntimeState s=state();RuntimeCombatSession c=session(s);assertTrue(c.submitPlayer("m0","SK_마법사_011").accepted());c.tick(.25f);assertEquals(35,s.skillEffects().power("m0","CURSE"));
  assertTrue(c.submitPlayer("m0","SK_무도가_018").accepted());c.tick(.15f);assertTrue(s.skillEffects().disabled("m0"));assertFalse(s.monsters().get(0).attackPrimed);
  assertTrue(c.submitPlayer("m0","SK_마법사_012").accepted());c.tick(.25f);int hp=s.monsters().get(0).hp;s.tick(1);assertTrue(s.monsters().get(0).hp<hp);
  s.skillEffects().put("player","SK_마법사_012","POISON",20,10);assertTrue(c.submitPlayer("player","SK_무도가_031").accepted());c.tick(.25f);assertFalse(s.skillEffects().has("player","POISON"));
  assertTrue(c.submitPlayer("player","SK_전사_009").accepted());c.tick(.15f);int base=s.rpg().finalStats().dam;assertEquals(base+4,s.rpg().finalStats().withEffects(s.skillEffects(),"player").dam);assertEquals(base,s.rpg().finalStats().dam);
  s.tick(60);assertTrue(s.skillEffects().playerEffects().isEmpty());assertFalse(s.skillEffects().disabled("m0"));
 }
 @Test public void statusRoundTripRetainsRemainingDurationWithoutMutatingBaseStats()throws Exception{
  RuntimeState s=state();s.skillEffects().put("player","SK_전사_009","DRAGON",4,14);s.tick(.4f);JSONObject saved=s.skillEffects().snapshot();SkillEffectState restored=new SkillEffectState();assertTrue(restored.restore(saved));assertEquals(13.6f,restored.get("player","DRAGON").remaining,.001f);assertFalse(restored.restore(new JSONObject().put("version",99)));assertTrue(restored.has("player","DRAGON"));
  SkillBook b=SkillBook.load(context);b.learn("SK_전사_009",50);b.assign(0,"SK_전사_009");b.learn("SK_전사_010",0);assertFalse(b.learned("SK_전사_009"));assertEquals("SK_전사_010",b.slot(0));
 }
 @Test public void allSupportedStatusesAreReachableThroughProductionEffectDispatch()throws Exception{
  for(SkillAbilityCatalog.Ability a:SkillAbilityCatalog.all())if(a.kind.equals("STATUS")){
   RuntimeState s=state();RuntimeCombatSession c=session(s);SkillActionContract.Rule r=SkillActionContract.get(a.id);
   if(a.status.equals("BASIC"))continue;
   if(a.status.equals("TRANSFER")){assertFalse(c.submitPlayer("player",a.id).accepted());continue;}
   if(r.presentationAllowed()){
    String target=r.selfAnchored()?"player":"m0";assertTrue(a.id,c.submitPlayer(target,a.id).accepted());assertTrue(a.id,c.tick(r.contact+.01f).events.stream().anyMatch(e->e.type==CombatResolver.EventType.EFFECT_APPLIED));
   }else assertTrue(a.id,c.useUtility(a.id));
  }
 }
 static void tick(GameView v,float dt)throws Exception{Method m=GameView.class.getDeclaredMethod("tickSkillCombat",float.class);m.setAccessible(true);m.invoke(v,dt);}
 @SuppressWarnings("unchecked") static <T>T field(Object o,String n)throws Exception{Field f=o.getClass().getDeclaredField(n);f.setAccessible(true);return(T)f.get(o);}
}
