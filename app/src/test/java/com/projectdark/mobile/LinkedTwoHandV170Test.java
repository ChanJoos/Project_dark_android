package com.projectdark.mobile;
import static org.junit.Assert.*;
import android.content.Context;
import java.util.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.*;
@RunWith(RobolectricTestRunner.class) @Config(sdk=34,manifest=Config.NONE)
public class LinkedTwoHandV170Test {
 Context c;
 @Before public void setup(){c=RuntimeEnvironment.getApplication();}
 SkillBook book(){SkillBook b=SkillBook.load(c);b.bindJob(()->"WARRIOR");return b;}
 RuntimeState state(){RuntimeState s=new RuntimeState();s.player().x=100;s.player().y=100;RuntimeState.Monster m=s.monsters().get(0);m.x=132;m.y=116;m.hp=100000;m.alive=true;return s;}
 RuntimeCombatSession session(RuntimeState s,SkillBook b){RuntimeCombatSession x=new RuntimeCombatSession(s,(a,t)->true,(a,id)->b.learned(id),a->true);x.setBasicHits(b::basicHits);return x;}
 List<CombatResolver.Event> damage(RuntimeCombatSession.FrameResult f){List<CombatResolver.Event> out=new ArrayList<>();for(CombatResolver.Event e:f.events)if(e.type==CombatResolver.EventType.EFFECT_APPLIED&&e.actorId.equals("player"))out.add(e);return out;}
 @Test public void learnedDoubleAndTripleApplySeparateFullBasicHitsAndNeverStackToFive(){
  for(int hits=1;hits<=3;hits++){SkillBook b=book();if(hits>=2)assertTrue(b.learn("SK_전사_002",0));if(hits==3)assertTrue(b.learn("SK_전사_005",0));assertEquals(hits,b.basicHits());RuntimeState s=state();RuntimeCombatSession x=session(s,b);RuntimeState.Monster m=s.monsters().get(0);assertTrue(x.submitPlayerBasicAttack(m.id).combat.accepted());List<CombatResolver.Event> all=new ArrayList<>();assertTrue(damage(x.tick(.14f)).isEmpty());for(int i=0;i<50;i++)all.addAll(damage(x.tick(.01f)));assertEquals(hits,all.size());int per=all.get(0).amount;assertTrue(per>0);for(CombatResolver.Event e:all){assertEquals(per,e.amount);assertEquals(all.get(0).actionSequence,e.actionSequence);}assertEquals(100000-per*hits,m.hp);assertFalse(x.playerActionActive());}
 }
 @Test public void coarseTickStillResolvesEachHitAndLethalFirstHitStopsTheRest(){
  SkillBook b=book();b.learn("SK_전사_005",0);RuntimeState s=state();RuntimeCombatSession x=session(s,b);RuntimeState.Monster m=s.monsters().get(0);assertTrue(x.submitPlayerBasicAttack(m.id).combat.accepted());assertEquals(3,damage(x.tick(.6f)).size());
  s=state();x=session(s,b);m=s.monsters().get(0);m.hp=1;assertTrue(x.submitPlayerBasicAttack(m.id).combat.accepted());assertEquals(1,damage(x.tick(.6f)).size());assertFalse(m.alive);for(CombatResolver.Event e:x.tick(.01f).events)assertNotEquals(CombatResolver.EventType.ACTION_CANCELLED,e.type);assertEquals(0,damage(x.tick(.6f)).size());assertFalse(x.playerActionActive());
 }
 @Test public void losingRangeBetweenHitsCancelsRemainingDamage(){SkillBook b=book();b.learn("SK_전사_005",0);RuntimeState s=state();RuntimeCombatSession x=session(s,b);RuntimeState.Monster m=s.monsters().get(0);x.submitPlayerBasicAttack(m.id);assertEquals(1,damage(x.tick(.15f)).size());int hp=m.hp;m.x+=1000;assertEquals(0,damage(x.tick(.3f)).size());assertEquals(hp,m.hp);assertFalse(x.playerActionActive());}
 @Test public void twoHandSkillIsRequiredEvenInEquipmentProfileAndFailedEquipPreservesShield(){
  RpgProgressionState r=new RpgProgressionState();r.enableEquipmentSandbox(true);SkillBook b=book();r.bindTwoHandLearning(()->b.owned("SK_전사_008"));r.equip("IT_ADAPTED_STARTER_SHIELD");Map<String,String> before=new LinkedHashMap<>(r.equipment());assertEquals(RpgProgressionState.EquipResult.TWO_HAND_SKILL_REQUIRED,r.equip("IT_WARDROBE_MW016"));assertEquals(before,r.equipment());b.setTestAccess(true);assertEquals(RpgProgressionState.EquipResult.TWO_HAND_SKILL_REQUIRED,r.equip("IT_WARDROBE_MW016"));b.setTestAccess(false);b.learn("SK_전사_008",0);assertEquals(RpgProgressionState.EquipResult.EQUIPPED,r.equip("IT_WARDROBE_MW016"));assertNull(r.equippedDefinition("방패"));assertEquals(RpgProgressionState.EquipResult.TWO_HAND_CONFLICT,r.equip("IT_ADAPTED_STARTER_SHIELD"));
 }
 @Test public void twoHandContactAndCooldownShareVideoAdaptedClock(){SkillBook b=book();b.learn("SK_전사_005",0);b.learn("SK_전사_008",0);RuntimeState s=state();s.rpg().enableEquipmentSandbox(true);s.rpg().bindTwoHandLearning(()->b.owned("SK_전사_008"));s.rpg().equip("IT_WARDROBE_MW016");RuntimeCombatSession x=session(s,b);String target=s.monsters().get(0).id;assertTrue(x.submitPlayerBasicAttack(target).combat.accepted());assertTrue(damage(x.tick(.29f)).isEmpty());assertEquals(1,damage(x.tick(.011f)).size());assertEquals(1,damage(x.tick(.065f)).size());assertEquals(1,damage(x.tick(.065f)).size());assertFalse(x.submitPlayerBasicAttack(target).combat.accepted());x.tick(.38f);assertTrue(x.submitPlayerBasicAttack(target).combat.accepted());assertEquals(.8f,BasicAttackTiming.cycle(s.rpg(),.45f),0);}
 @Test public void oldEquippedTwoHandWithoutLearningIsRemovedWithoutLosingOwnership(){RpgProgressionState r=new RpgProgressionState();r.enableEquipmentSandbox(true);r.bindTwoHandLearning(()->true);r.equip("IT_WARDROBE_MW016");int owned=r.inventory().get("IT_WARDROBE_MW016");r.bindTwoHandLearning(()->false);r.enforceTwoHandLearning();assertNull(r.equippedDefinition("무기"));assertEquals(owned,(int)r.inventory().get("IT_WARDROBE_MW016"));}
 @Test public void linkedLearningPersistsAndJobRestrictionRemains(){SkillBook b=book();b.learn("SK_전사_002",0);b.learn("SK_전사_005",0);b.learn("SK_전사_008",0);SkillBook restored=book();assertTrue(restored.restore(b.snapshot()));assertEquals(3,restored.basicHits());assertTrue(restored.owned("SK_전사_008"));restored.bindJob(()->"COMMONER");assertEquals(1,restored.basicHits());}
}
