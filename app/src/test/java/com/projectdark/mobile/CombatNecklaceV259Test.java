package com.projectdark.mobile;
import static org.junit.Assert.*;
import android.content.Context;
import android.graphics.*;
import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.nio.file.*;
import org.json.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.*;
@RunWith(RobolectricTestRunner.class) @Config(sdk=34,manifest=Config.NONE) @GraphicsMode(GraphicsMode.Mode.NATIVE)
public class CombatNecklaceV259Test {
 Context c;final MartialHudAutoRegressionTest h=new MartialHudAutoRegressionTest();
 @Before public void setup(){h.setup();c=RuntimeEnvironment.getApplication();reset();}
 void reset(){for(String p:new String[]{"project_dark_f5m_v1",QuickItemBarV254.PREFS,"project_dark_skill_book_v1","project_dark_skill_test_v1"})c.getSharedPreferences(p,0).edit().clear().commit();F5mSaveStore.install(c);}
 @SuppressWarnings("unchecked") <T>T field(Object o,String name)throws Exception{Field f=o.getClass().getDeclaredField(name);f.setAccessible(true);return(T)f.get(o);}
 Object call(GameView v,String name,Class<?> type,Object value)throws Exception{Method m=GameView.class.getDeclaredMethod(name,type);m.setAccessible(true);return m.invoke(v,value);}
 GameView view(){GameView v=new GameView(c);v.layout(0,0,960,540);return v;}
 RuntimeState state()throws Exception{RuntimeState s=new RuntimeState();((List<?>)field(s,"monsters")).clear();s.player().x=s.player().y=0;s.rpg().restoreBaseResources(100000,100000);s.applyDerivedGrowth();s.player().hp=s.player().maxHp;return s;}
 RuntimeState.Monster monster(RuntimeState s)throws Exception{RuntimeState.Monster m=new RuntimeState.Monster("pos","position",32,16,1000000,"B");((List<RuntimeState.Monster>)field(s,"monsters")).add(m);m.defenseElement="FIRE";m.attackElement="WATER";return m;}
 RuntimeCombatSession session(RuntimeState s){return new RuntimeCombatSession(s,(a,t)->true,(a,id)->true,a->true);}
 int cycle(RuntimeState s,RuntimeState.Monster m,SkillBook b,boolean automatic)throws Exception{RuntimeCombatSession session=session(s);session.setBasicHits(b::basicHits);int before=m.hp;if(automatic){CombatActionOrchestrator a=field(session,"actions");assertTrue(a.submitAuto("player",m.id,RuntimeCombatSession.playerAttackActionId(0)).accepted());}else assertTrue(session.submitPlayerAttack(m.id,0).accepted());session.tick(2);return before-m.hp;}
 @Test public void learnedDoubleAndTripleHaveEqualDamageForEveryDefenderFacingInManualAndAuto()throws Exception{
  for(String[] ids:new String[][]{{"WARRIOR","SK_전사_002","SK_전사_005"},{"ROGUE","SK_도적_008","SK_도적_024"},{"MARTIAL_ARTIST","SK_무도가_005","SK_무도가_027"}}){
   RuntimeState s=state();RuntimeState.Monster m=monster(s);SkillBook b=SkillBook.load(c);b.bindJob(()->ids[0]);m.visualFacing.setLocomotion(CharacterRenderer.Direction.NW);int front=cycle(s,m,b,false);assertTrue(front>0);
   for(int hits=1;hits<=3;hits++){if(hits==2)assertTrue(b.learn(ids[1],100));if(hits==3)assertTrue(b.learn(ids[2],100));assertEquals(hits,b.basicHits());for(boolean auto:new boolean[]{false,true}){m.visualFacing.setLocomotion(CharacterRenderer.Direction.NW);assertEquals(front*hits,cycle(s,m,b,auto));m.visualFacing.setLocomotion(CharacterRenderer.Direction.NE);assertEquals(front*hits,cycle(s,m,b,auto));m.visualFacing.setLocomotion(CharacterRenderer.Direction.SE);assertEquals(front*hits,cycle(s,m,b,auto));}}
  }
 }
 @Test public void directionDoesNotMultiplyPhysicalSkillsOrIncomingMonsterHits()throws Exception{
  RuntimeState s=state();RuntimeState.Monster m=monster(s);RuntimeCombatPortAdapter p=new RuntimeCombatPortAdapter(s,(a,t)->true,(a,id)->true,a->true);
  m.visualFacing.setLocomotion(CharacterRenderer.Direction.NW);p.prepareAction("player","SK_전사_001");int before=m.hp;p.applyDamage("player",m.id,"SK_전사_001",100);int front=before-m.hp;p.finishAction("player","SK_전사_001");m.visualFacing.setLocomotion(CharacterRenderer.Direction.SE);p.prepareAction("player","SK_전사_001");before=m.hp;p.applyDamage("player",m.id,"SK_전사_001",100);assertEquals(front,before-m.hp);
  s.player().hp=s.player().maxHp;m.visualFacing.setLocomotion(CharacterRenderer.Direction.NW);before=s.player().hp;p.applyDamage(m.id,"player",RuntimeCombatSession.MONSTER_BASIC_ACTION_ID,100);int hit=before-s.player().hp;m.visualFacing.setLocomotion(CharacterRenderer.Direction.SE);before=s.player().hp;p.applyDamage(m.id,"player",RuntimeCombatSession.MONSTER_BASIC_ACTION_ID,100);assertEquals(hit,before-s.player().hp);
 }
 @Test public void everyInventoryNecklaceHasAnAuditedElementOrOneExplicitUnresolvedCashSource()throws Exception{
  GameView v=view();RuntimeState s=field(v,"state");int verified=0,pending=0;Set<String> seen=new HashSet<>();
  for(String id:s.rpg().inventory().keySet()){RpgProgressionState.ItemDefinition d=s.rpg().itemDefinitions().get(id);if(!"목걸이".equals(d.equipSlot)||!seen.add(RpgProgressionState.canonicalItemId(id)))continue;if(d.attackElement==null){pending++;assertEquals("쥬얼십자가목걸이FX",d.name);}else{verified++;assertTrue(d.name,ElementalDamageFormula.index(d.attackElement)>=0);}}
  assertTrue("inventory has verified necklaces",verified>0);assertEquals(1,pending);assertEquals("WATER",ElementalDamageFormula.normalize(s.rpg().itemDefinitions().get(StarterNecklacesV259.id(StarterNecklacesV259.WATER)).attackElement));
  assertEquals("EARTH",ElementalDamageFormula.normalize(s.rpg().itemDefinitions().get("USER_ITEM_20261018_R01306").attackElement));assertEquals("WATER",ElementalDamageFormula.normalize(s.rpg().itemDefinitions().get("USER_ITEM_20261018_R03895").attackElement));
  File report=new File("build/reports/horror-v179/v259/necklace-coverage.json");report.getParentFile().mkdirs();try(FileWriter out=new FileWriter(report)){out.write(new JSONObject().put("verifiedInventoryNecklaces",verified).put("unresolved",pending).toString());}
 }
 @Test public void newGameStartsWaterCrystalAndFireEarthWindShortcutsAndPersistsWithoutResettingChoices()throws Exception{
  GameView v=view();RuntimeState s=field(v,"state");QuickItemBarV254 bar=field(v,"quickItems");String water=StarterNecklacesV259.id(StarterNecklacesV259.WATER),earth=StarterNecklacesV259.id(StarterNecklacesV259.EARTH);
  assertEquals(water,s.rpg().equipment().get("목걸이"));assertEquals(StarterNecklacesV259.id(StarterNecklacesV259.FIRE),bar.slot(0));assertEquals(earth,bar.slot(1));assertEquals(StarterNecklacesV259.id(StarterNecklacesV259.WIND),bar.slot(2));assertNull(bar.slot(3));assertEquals(Integer.valueOf(11),s.rpg().itemDefinitions().get(water).requiredLevel);
  assertTrue((Boolean)call(v,"useQuickItem",String.class,earth));assertTrue(bar.assign(0,null,s.rpg()));v.pause();F5mSaveStore.install(c);GameView next=view();RuntimeState copy=field(next,"state");QuickItemBarV254 restored=field(next,"quickItems");assertEquals(earth,copy.rpg().equipment().get("목걸이"));assertNull(restored.slot(0));assertEquals(RpgProgressionState.RequirementResult.MET,copy.rpg().currentRequirements(water));assertTrue((Boolean)call(next,"useQuickItem",String.class,water));assertEquals(water,copy.rpg().equipment().get("목걸이"));
  Bitmap image=Bitmap.createBitmap(960,540,Bitmap.Config.ARGB_8888);next.draw(new Canvas(image));File file=new File("build/reports/horror-v179/v259/starter-hud.png");file.getParentFile().mkdirs();try(FileOutputStream out=new FileOutputStream(file)){assertTrue(image.compress(Bitmap.CompressFormat.PNG,100,out));}
 }
 GameView senseView(String defense)throws Exception{
  reset();GameView v=h.directed(CharacterRenderer.Direction.SE);v.setSkillTestMode(false);RuntimeState s=field(v,"state");s.rpg().restoreProgression(11,0);assertTrue(s.rpg().restoreJobCode("ROGUE"));s.player().mp=s.player().maxMp;RuntimeState.Monster m=((CombatController)field(v,"combat")).target();m.defenseElement=defense;SkillBook b=field(v,"skillBook");assertTrue(b.learn(RogueSkillPolicy.SENSE,100));for(int i=0;i<12;i++)b.clearSlot(i);assertTrue(b.assign(0,RogueSkillPolicy.SENSE));return v;
 }
 @Test public void successfulManualSenseUsesRealNoticeAndPicksEarthAgainstWaterButKeepsWaterAgainstFire()throws Exception{
  for(String defense:new String[]{"WATER","FIRE"}){GameView v=senseView(defense);SkillBook b=field(v,"skillBook");call(v,"useBookSkill",SkillBook.Entry.class,b.get(RogueSkillPolicy.SENSE));h.advance(v,1f);RuntimeState s=field(v,"state");assertEquals(StarterNecklacesV259.id(defense.equals("WATER")?StarterNecklacesV259.EARTH:StarterNecklacesV259.WATER),s.rpg().equipment().get("목걸이"));assertTrue(((SenseMonsterWindow)field(v,"senseMonsterWindow")).visible());}
 }
 @Test public void autoSenseUsesTheSameEquipPathAndRejectedOutOfRangeSenseNeverSwaps()throws Exception{
  GameView v=senseView("WATER");HudTestControlsV267.auto(v);h.advance(v,1f);RuntimeState s=field(v,"state");assertEquals(StarterNecklacesV259.id(StarterNecklacesV259.EARTH),s.rpg().equipment().get("목걸이"));
  v=senseView("WATER");s=field(v,"state");RuntimeState.Monster m=((CombatController)field(v,"combat")).target();m.x=s.player().x+96;m.y=s.player().y+48;SkillBook b=field(v,"skillBook");call(v,"useBookSkill",SkillBook.Entry.class,b.get(RogueSkillPolicy.SENSE));h.advance(v,.3f);assertEquals(StarterNecklacesV259.id(StarterNecklacesV259.WATER),s.rpg().equipment().get("목걸이"));
 }
 @Test public void waterDefenseFallbackPriorityIsEarthThenWindThenFireAndCurrentWaterNeverUnequips()throws Exception{
  GameView v=view();RuntimeState s=field(v,"state");QuickItemBarV254 bar=field(v,"quickItems");RuntimeState.Monster m=s.monsters().get(0);m.defenseElement="WATER";
  for(String alias:new String[]{StarterNecklacesV259.EARTH,StarterNecklacesV259.WIND,StarterNecklacesV259.FIRE}){String id=StarterNecklacesV259.id(alias);assertEquals(id,SenseNecklaceV259.best(s,m,bar));Map<String,Integer> owned=new LinkedHashMap<>(s.rpg().inventory());owned.remove(id);assertTrue(s.rpg().restoreOwnedItems(owned,s.rpg().equipment()));}
  assertNull(SenseNecklaceV259.best(s,m,bar));assertEquals(StarterNecklacesV259.id(StarterNecklacesV259.WATER),s.rpg().equipment().get("목걸이"));
 }
 @Test public void emptyNonNecklaceUnslottedUnknownReadOnlyAndDeadTargetsNeverCreateAnAutoEquip()throws Exception{
  GameView v=view();RuntimeState s=field(v,"state");QuickItemBarV254 bar=field(v,"quickItems");RuntimeState.Monster m=s.monsters().get(0);m.defenseElement="WATER";for(int i=0;i<4;i++)assertTrue(bar.assign(i,null,s.rpg()));assertNull(SenseNecklaceV259.best(s,m,bar));assertTrue(bar.assign(0,RpgProgressionState.PLAYTEST_WEAPON_ITEM_ID,s.rpg()));assertNull(SenseNecklaceV259.best(s,m,bar));
  String cash=RpgProgressionState.canonicalItemId("IT_SOURCE_MINIMOB_5");assertTrue(bar.assign(1,cash,s.rpg()));s.rpg().enableEquipmentSandbox(true);assertNull(SenseNecklaceV259.best(s,m,bar));
  assertTrue(bar.assign(0,StarterNecklacesV259.id(StarterNecklacesV259.EARTH),s.rpg()));m.alive=false;assertNull(SenseNecklaceV259.best(s,m,bar));m.alive=true;c.getSharedPreferences("project_dark_f5m_v1",0).edit().putInt("save_schema",999).commit();F5mSaveStore.install(c);assertNull(SenseNecklaceV259.best(s,m,bar));
 }
 @Test public void temporaryDefenseChangeUsesSameEffectiveNatureInSenseAndNecklaceRanking()throws Exception{
  GameView v=view();RuntimeState s=field(v,"state");RuntimeState.Monster m=s.monsters().get(0);m.defenseElement="WATER";QuickItemBarV254 bar=field(v,"quickItems");assertEquals(StarterNecklacesV259.id(StarterNecklacesV259.EARTH),SenseNecklaceV259.best(s,m,bar));s.skillEffects().put(m.id,"qa","CHANGE_ELEMENT",1,10);assertEquals("FIRE",MonsterElementsV258.effectiveDefense(s,m));assertNull(SenseNecklaceV259.best(s,m,bar));SenseMonsterWindow w=field(v,"senseMonsterWindow");w.show(s,m,false);String[] lines=field(w,"lines");assertEquals("DEFENSE NATURE: 불",lines[4]);
 }
}
