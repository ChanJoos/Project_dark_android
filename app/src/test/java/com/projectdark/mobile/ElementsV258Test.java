package com.projectdark.mobile;
import static org.junit.Assert.*;
import android.content.Context;
import android.graphics.*;
import com.projectdark.mobile.world.*;
import java.lang.reflect.*;
import java.nio.file.*;
import java.util.*;
import java.io.*;
import org.json.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.*;
@RunWith(RobolectricTestRunner.class) @Config(sdk=34,manifest=Config.NONE) @GraphicsMode(GraphicsMode.Mode.NATIVE)
public class ElementsV258Test {
 Context context;
 @Before public void setup(){context=RuntimeEnvironment.getApplication();context.getSharedPreferences("project_dark_f5m_v1",0).edit().clear().commit();F5mSaveStore.install(context);}
 @SuppressWarnings("unchecked") <T>T field(Object o,String name)throws Exception{Field f=o.getClass().getDeclaredField(name);f.setAccessible(true);return(T)f.get(o);}
 RuntimeState clean()throws Exception{RuntimeState s=new RuntimeState();((List<?>)field(s,"monsters")).clear();s.player().x=0;s.player().y=0;s.rpg().restoreBaseResources(100000,100000);s.applyDerivedGrowth();s.player().hp=s.player().maxHp;s.player().mp=s.player().maxMp;return s;}
 RuntimeState.Monster add(RuntimeState s,String id)throws Exception{RuntimeState.Monster m=new RuntimeState.Monster(id,"element test",32,16,1000000,"B");((List<RuntimeState.Monster>)field(s,"monsters")).add(m);return m;}
 RuntimeCombatSession session(RuntimeState s){return new RuntimeCombatSession(s,(a,t)->true,(a,id)->true,a->true);}
 void equip(RuntimeState s,String alias){s.rpg().enableEquipmentSandbox(true);String id=RpgProgressionState.canonicalItemId(alias);assertNotNull(s.rpg().itemDefinitions().get(id));assertEquals(RpgProgressionState.AutoLootResult.LOOTED,s.rpg().autoLootResolvedItem(id,1));assertEquals(RpgProgressionState.EquipResult.EQUIPPED,s.rpg().equip(id));}
 @Test public void everySourceCellUsesDefenseRowsAttackColumnsAndExactDamageReference()throws Exception{
  JSONObject source=new JSONObject(new String(Files.readAllBytes(Paths.get("../master/canonical/Element_Reference_V257.json")),java.nio.charset.StandardCharsets.UTF_8));JSONArray order=source.getJSONObject("orientation").getJSONArray("order"),matrix=source.getJSONArray("tables").getJSONObject(0).getJSONArray("values");
  for(int d=0;d<9;d++)for(int a=0;a<9;a++)assertEquals("def "+order.getString(d)+" atk "+order.getString(a),matrix.getJSONArray(d).getInt(a),ElementalDamageFormula.apply(18300,order.getString(a),order.getString(d)));
  assertEquals(13000,ElementalDamageFormula.apply(18300,"WOOD","METAL"));assertEquals(16000,ElementalDamageFormula.apply(18300,"LIFE","DARK"));assertEquals(16000,ElementalDamageFormula.apply(18300,"DARK","LIFE"));assertTrue(CombatV3PipelineAudit.verify());
 }
 @Test public void aliasesUnknownNonPositiveAndOverflowHaveExplicitSafeBehavior(){
  assertEquals(18300,ElementalDamageFormula.apply(18300,"수","화"));assertEquals(16000,ElementalDamageFormula.apply(18300,"빛","암흑"));assertEquals(20000,ElementalDamageFormula.apply(18300,"숲","대지"));assertEquals(24500,ElementalDamageFormula.apply(18300,"강철",null));assertEquals(73,ElementalDamageFormula.apply(73,"UNRESOLVED","WATER"));assertEquals(0,ElementalDamageFormula.apply(-1,"WATER","FIRE"));assertEquals(Integer.MAX_VALUE,ElementalDamageFormula.apply(Integer.MAX_VALUE,"WATER","NONE"));
 }
 @Test public void independentAttackAndDefenseRollsCoverAllSixteenNaturalPairsAndStayStableWhileAlive()throws Exception{
  RuntimeState s=clean();RuntimeState.Monster m=add(s,"plain");Set<String> pairs=new HashSet<>();
  for(int a=0;a<4;a++)for(int d=0;d<4;d++){MonsterElementsV258.assign(m,a,d);String attack=m.attackElement,defense=m.defenseElement;pairs.add(attack+"/"+defense);s.tick(.02f);assertEquals(attack,m.attackElement);assertEquals(defense,m.defenseElement);}
  assertEquals(16,pairs.size());
 }
 @Test public void allExistingHuntMapsAndPrototypeActorsHaveNaturalPairsExceptSixtyDarkMineActors()throws Exception{
  RuntimeState s=new RuntimeState();int maps=0,actors=0,dark=0;List<String> list=new ArrayList<>(Arrays.asList(CampaignWorld.ROUTE));list.add("MAP_POTE_04");list.addAll(Arrays.asList(RucesionCoastGeometry.ROUTE));list.add(DeathVillageGeometry.FIELD);for(char suffix='A';suffix<='E';suffix++)list.add("MAP_DEATH_UNDER_"+suffix);list.add(MineHuntGeometry.ID);for(int i=1;i<=13;i++)list.add(HorrorCastleGeometry.roomId(i));
  for(RuntimeState.Monster m:s.monsters()){assertTrue(MonsterElementsV258.natural(m.attackElement));assertTrue(MonsterElementsV258.natural(m.defenseElement));}
  for(String map:new LinkedHashSet<>(list)){s.enterCampaignMap(map,false);maps++;for(RuntimeState.Monster m:s.monsters()){actors++;if(MonsterElementsV258.darkMine(m.id)){dark++;assertEquals("DARK",m.attackElement);assertEquals("DARK",m.defenseElement);}else{assertTrue(map+" "+m.id,MonsterElementsV258.natural(m.attackElement));assertTrue(map+" "+m.id,MonsterElementsV258.natural(m.defenseElement));}}}
  assertTrue(maps>=30);assertTrue(actors>1000);assertEquals(60,dark);assertTrue(MonsterElementsV258.natural(new RuntimeState(RuntimeState.BootMode.POTE_01_PROTOTYPE).monsters().get(0).defenseElement));
  File report=new File("build/reports/horror-v179/v258/coverage.json");report.getParentFile().mkdirs();Files.write(report.toPath(),new JSONObject().put("maps",maps).put("actors",actors).put("darkMineActors",dark).toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));
 }
 @Test public void successfulRespawnDrawsANewPairAndBlockedRespawnDoesNotDraw()throws Exception{
  RuntimeState s=new RuntimeState();s.enterCampaignMap("MAP_ABEL_COAST_3B",false);RuntimeState.Monster m=s.monsters().get(0);MonsterElementsV258.assign(m,0,1);String before=m.attackElement+"/"+m.defenseElement;s.damage(m,m.hp);s.player().x=m.spawnX;s.player().y=m.spawnY;s.tick(m.respawnSeconds+1);assertFalse(m.alive);assertEquals(before,m.attackElement+"/"+m.defenseElement);Set<String> seen=new HashSet<>();seen.add(before);s.player().x=-1000;s.player().y=-1000;
  for(int i=0;i<32;i++){s.tick(m.respawnSeconds+1);assertTrue(m.alive);assertTrue(MonsterElementsV258.natural(m.attackElement));assertTrue(MonsterElementsV258.natural(m.defenseElement));seen.add(m.attackElement+"/"+m.defenseElement);s.damage(m,m.hp);}
  assertTrue("respawn is rerolled, not fixed by species",seen.size()>1);
 }
 @Test public void realNecklaceSwapChangesDamageAndManualAutoUseIdenticalCurrentInstanceDefense()throws Exception{
  RuntimeState s=clean();RuntimeState.Monster m=add(s,"plain");m.attackElement="WIND";m.defenseElement="FIRE";equip(s,"IT_NECK_WATER_PEARL");RuntimeCombatSession manual=session(s);
  int before=m.hp;assertTrue(manual.submitPlayerAttack(m.id,0).accepted());manual.tick(1);int water=before-m.hp;assertTrue(water>0);
  RuntimeCombatSession automatic=session(s);CombatActionOrchestrator actions=field(automatic,"actions");before=m.hp;assertTrue(actions.submitAuto("player",m.id,RuntimeCombatSession.playerAttackActionId(0)).accepted());automatic.tick(1);assertEquals(water,before-m.hp);
  equip(s,"IT_NECK_EARTH_PEARL");RuntimeCombatSession next=session(s);before=m.hp;assertTrue(next.submitPlayerAttack(m.id,0).accepted());next.tick(1);int earth=before-m.hp;assertTrue("water necklace beats earth against fire: "+water+"/"+earth,water>earth);assertEquals("FIRE",m.defenseElement);
 }
 @Test public void monsterBasicUsesIndependentAttackAndRealPlayerBeltWhileExplicitSpellsKeepTheirElement()throws Exception{
  RuntimeState s=clean();RuntimeState.Monster m=add(s,"MINE_04#test");equip(s,"IT_BELT_FIRE_LEATHER");assertEquals("FIRE",ElementalDamageFormula.normalize(s.rpg().defenseElement()));RuntimeCombatPortAdapter port=new RuntimeCombatPortAdapter(s,(a,t)->true,(a,id)->true,a->true);
  m.attackElement="WATER";int before=s.player().hp;port.applyDamage(m.id,"player",RuntimeCombatSession.MONSTER_BASIC_ACTION_ID,100);int water=before-s.player().hp;
  m.attackElement="FIRE";before=s.player().hp;port.applyDamage(m.id,"player",RuntimeCombatSession.MONSTER_BASIC_ACTION_ID,100);int fire=before-s.player().hp;assertTrue(water>fire);
  m.attackElement="EARTH";before=s.player().hp;port.applyDamage(m.id,"player","MONSTER_FIRE",100);int spellEarth=before-s.player().hp;m.attackElement="WATER";before=s.player().hp;port.applyDamage(m.id,"player","MONSTER_FIRE",100);assertEquals(spellEarth,before-s.player().hp);
 }
 @Test public void elementalMagicOverridesNecklaceAndStillUsesTheTargetsCurrentDefense()throws Exception{
  RuntimeState s=clean();RuntimeState.Monster m=add(s,"plain");m.defenseElement="FIRE";RuntimeCombatPortAdapter port=new RuntimeCombatPortAdapter(s,(a,t)->true,(a,id)->true,a->true);
  equip(s,"IT_NECK_WATER_PEARL");int before=m.hp;port.applyDamage("player",m.id,"SK_마법사_001",100);int water=before-m.hp;
  equip(s,"IT_NECK_EARTH_PEARL");before=m.hp;port.applyDamage("player",m.id,"SK_마법사_001",100);assertEquals(water,before-m.hp);m.defenseElement="WATER";before=m.hp;port.applyDamage("player",m.id,"SK_마법사_001",100);assertTrue(water>before-m.hp);
 }
 @Test public void actualSaveRestartPreservesPairsAndOldCorruptOrForeignRowsKeepValidSpawn()throws Exception{
  GameView v=new GameView(context);v.layout(0,0,960,540);RuntimeState s=field(v,"state");RuntimeState.Monster m=s.monsters().get(0);MonsterElementsV258.assign(m,1,3);String id=m.id;v.pause();F5mSaveStore.install(context);GameView next=new GameView(context);RuntimeState restored=field(next,"state");RuntimeState.Monster same=restored.monsters().stream().filter(x->x.id.equals(id)).findFirst().get();assertEquals("EARTH",same.attackElement);assertEquals("FIRE",same.defenseElement);
  JSONObject bad=new JSONObject().put("version",1).put("map",restored.currentMapId()).put("actors",new JSONArray().put(new JSONObject().put("id",id).put("attack","LIFE").put("defense","DARK")));MonsterElementsV258.restore(restored,bad);assertEquals("EARTH",same.attackElement);
  bad.put("map","foreign");MonsterElementsV258.restore(restored,bad);assertEquals("FIRE",same.defenseElement);context.getSharedPreferences("project_dark_f5m_v1",0).edit().remove("monster_elements_v258").commit();F5mSaveStore.install(context);RuntimeState old=field(new GameView(context),"state");assertTrue(F5mSaveStore.writable());assertTrue(MonsterElementsV258.natural(old.monsters().get(0).attackElement));
 }
 @Test public void senseDisplaysTheSameTwoIndependentElementsUsedByCombatAndSavesNativePreview()throws Exception{
  GameView v=new GameView(context);v.layout(0,0,960,540);RuntimeState s=field(v,"state");RuntimeState.Monster m=s.monsters().get(0);MonsterElementsV258.assign(m,0,3);SenseMonsterWindow window=field(v,"senseMonsterWindow");window.show(s,m,false);String[] lines=field(window,"lines");assertEquals("ATTACK NATURE: 물",lines[3]);assertEquals("DEFENSE NATURE: 불",lines[4]);Bitmap image=Bitmap.createBitmap(960,540,Bitmap.Config.ARGB_8888);v.draw(new Canvas(image));File file=new File("build/reports/horror-v179/v258/sense-elements.png");file.getParentFile().mkdirs();try(FileOutputStream out=new FileOutputStream(file)){assertTrue(image.compress(Bitmap.CompressFormat.PNG,100,out));}
 }
}
