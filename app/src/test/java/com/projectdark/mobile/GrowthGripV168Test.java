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
import com.projectdark.mobile.world.*;
@RunWith(RobolectricTestRunner.class) @Config(sdk=34,manifest=Config.NONE) @GraphicsMode(GraphicsMode.Mode.NATIVE)
public class GrowthGripV168Test {
 Context c;
 @Before public void setup(){c=RuntimeEnvironment.getApplication();for(String p:new String[]{"project_dark_f5m_v1","project_dark_skill_test_v1","project_dark_skill_book_v1"})c.getSharedPreferences(p,0).edit().clear().commit();F5mSaveStore.install(c);UiTheme.install(c);}
 @Test public void actualLevelUpsRollTheUserExamplesAndNeverRecalculatePastGrowth(){
  for(int stat:new int[]{3,60}){RuntimeState state=new RuntimeState();RpgProgressionState r=state.rpg();r.restoreStats(3,3,stat,stat,3,0);r.restoreGrowthRandomState(7123456789L);Set<Integer> hpRolls=new HashSet<>(),mpRolls=new HashSet<>();
   int previous=r.baseMaxHp();r.restoreStats(3,3,stat,stat,3,0);assertEquals(previous,r.baseMaxHp());
   for(int n=0;n<40;n++){int hp=r.baseMaxHp(),mp=r.baseMaxMp();assertEquals(1,r.grantAdaptedReward(LevelExpCurve.requiredForNext(r.normalLevel()),0));state.applyDerivedGrowth();int dh=r.baseMaxHp()-hp,dm=r.baseMaxMp()-mp;hpRolls.add(dh);mpRolls.add(dm);assertTrue(dh>=LevelGrowthPolicy.minimum(stat)&&dh<=LevelGrowthPolicy.maximum(stat));assertTrue(dm>=LevelGrowthPolicy.minimum(stat)&&dm<=LevelGrowthPolicy.maximum(stat));assertEquals(r.baseMaxHp(),state.player().maxHp);assertEquals(r.baseMaxMp(),state.player().maxMp);}
   assertTrue(hpRolls.size()>4);assertTrue(mpRolls.size()>4);assertEquals(stat==3?"45~55":"130~150",r.nextHpGainRange());
  }
 }
 @Test public void nextGrowthRollSurvivesSaveReloadAndPreviewDoesNotConsumeIt(){
  RpgProgressionState r=new RpgProgressionState();r.restoreStats(3,3,60,60,3,0);r.restoreGrowthRandomState(1234567L);long seed=r.growthRandomState();for(int i=0;i<20;i++){r.nextHpGain();r.nextHpGainRange();r.nextMpGainRange();}assertEquals(seed,r.growthRandomState());F5mSaveStore.saveRewardsActive(r);F5mSaveStore.install(c);RpgProgressionState restored=new RpgProgressionState();F5mSaveStore.restoreRewardsActive(restored);assertEquals(seed,restored.growthRandomState());r.grantAdaptedReward(LevelExpCurve.requiredForNext(1),0);restored.grantAdaptedReward(LevelExpCurve.requiredForNext(1),0);assertEquals(r.baseMaxHp(),restored.baseMaxHp());assertEquals(r.baseMaxMp(),restored.baseMaxMp());assertEquals(r.growthRandomState(),restored.growthRandomState());
 }
 @Test public void rejectedRewardTransactionRestoresGrowthAndRandomState(){
  RpgProgressionState r=new RpgProgressionState();long seed=r.growthRandomState();int hp=r.baseMaxHp();assertFalse(F5mSaveStore.transactActive(r,null,()->{r.grantAdaptedReward(LevelExpCurve.requiredForNext(1),0);return false;}));assertEquals(1,(int)r.normalLevel());assertEquals(hp,r.baseMaxHp());assertEquals(seed,r.growthRandomState());
 }
 @Test public void shieldIsRejectedWithoutChangingWeaponInventoryOrStatsButTwoHandReplacesShield(){
  for(String weapon:new String[]{"IT_WARDROBE_MW016",ChungryongWeaponRenderer.ITEM}){RpgProgressionState r=new RpgProgressionState();r.enableEquipmentSandbox(true);r.autoLootResolvedItem(weapon,1);r.equip("IT_ADAPTED_STARTER_SHIELD");assertEquals(RpgProgressionState.EquipResult.EQUIPPED,r.equip(weapon));assertNull(r.equippedDefinition("방패"));Map<String,String> before=new LinkedHashMap<>(r.equipment());Map<String,Integer> owned=new LinkedHashMap<>(r.inventory());assertEquals(RpgProgressionState.EquipResult.TWO_HAND_CONFLICT,r.equip("IT_ADAPTED_STARTER_SHIELD"));assertEquals(before,r.equipment());assertEquals(owned,r.inventory());assertEquals("TWO_HAND",WeaponCombatProfile.motion(r,"EQUIPPED_BASIC"));assertEquals("TWO_HAND",WeaponCombatProfile.motion(r,"THRUST"));assertEquals("CAST",WeaponCombatProfile.motion(r,"CAST"));}
 }
 @Test public void actualTownNpcsAndArrivalAreAtReagentShopsWithoutOverlap()throws Exception{
  for(String map:new String[]{AbelTownGeometry.ID,RucesionTownGeometry.ID}){GameView v=new GameView(c);v.layout(0,0,960,540);v.setSkillTestMode(true);TownQualityV164Test h=new TownQualityV164Test();h.c=c;h.travel(v,map);RuntimeState state=ItemWindowReferenceTest.field(v,"state");TownTravel.Destination d=TownTravel.forMap(map);RuntimeState.Npc npc=null;for(RuntimeState.Npc n:state.npcs())if(d.npc.equals(n.id))npc=n;assertNotNull(npc);float x,y;if(AbelTownGeometry.contains(map)){AbelTownGeometry.Building b=AbelTownGeometry.forInterior("abel_interior_reagent");x=b.doorX();y=b.doorY();}else{RucesionTownGeometry.Building b=RucesionTownGeometry.forInterior("rucesion_interior_reagent");x=b.doorX();y=b.doorY();}assertTrue(Math.hypot(npc.x-x,npc.y-y)<180);assertTrue(Math.hypot(state.player().x-npc.x,state.player().y-npc.y)>=64);assertTrue(Math.hypot(state.player().x-npc.x,state.player().y-npc.y)<=120);capture(v,"arrival-"+map);}
 }
 @Test public void hudUsesSharedCurumAndMaradiumAndCurumIsOrangeWithExactSourceAlpha()throws Exception{
  SourceItemIconRegistry icons=new SourceItemIconRegistry(c);Bitmap curum=icons.get("IT_REAGENT_CURUM"),curanum=icons.get("IT_REAGENT_CURANUM"),mp=icons.get("IT_B_MP_POTION");assertNotNull(curum);assertNotNull(mp);assertFalse(curum.sameAs(curanum));int orange=0;
  for(int y=0;y<32;y++)for(int x=0;x<32;x++){int a=curum.getPixel(x,y),b=curanum.getPixel(x,y);assertEquals(Color.alpha(b),Color.alpha(a));float[] hsv=new float[3];Color.colorToHSV(a,hsv);if(Color.alpha(a)>100&&hsv[0]>15&&hsv[0]<45&&hsv[1]>.5f)orange++;}assertTrue(orange>40);assertTrue(curum.sameAs(new ReagentItemVisualRegistry(c).get("IT_REAGENT_CURUM")));
  MobileHudArtwork.install(c);Bitmap hud=Bitmap.createBitmap(150,64,Bitmap.Config.ARGB_8888);Canvas canvas=new Canvas(hud);assertTrue(MobileHudArtwork.potion(canvas,32,32,true));assertTrue(MobileHudArtwork.potion(canvas,96,32,false));assertTrue(Bitmap.createBitmap(hud,16,16,32,32).sameAs(curum));assertTrue(Bitmap.createBitmap(hud,80,16,32,32).sameAs(mp));save(hud,"hud-potions-native");
 }
 @Test public void liveBasicTwoHandAndGrowthReceiptRender()throws Exception{
  GameView v=new GameView(c);v.layout(0,0,960,540);RuntimeState state=ItemWindowReferenceTest.field(v,"state");state.rpg().enableEquipmentSandbox(true);state.rpg().autoLootResolvedItem(ChungryongWeaponRenderer.ITEM,1);state.rpg().equip(ChungryongWeaponRenderer.ITEM);
  Field f=GameView.class.getDeclaredField("action");f.setAccessible(true);f.set(v,Enum.valueOf((Class)f.getType(),"SWING"));Field clock=GameView.class.getDeclaredField("actionClock");clock.setAccessible(true);clock.setFloat(v,.18f);capture(v,"twohand-live-chungryong");
  Method notice=GameView.class.getDeclaredMethod("observeLevelGrowth");notice.setAccessible(true);notice.invoke(v);int hp=state.rpg().baseMaxHp(),mp=state.rpg().baseMaxMp();state.rpg().grantAdaptedReward(LevelExpCurve.requiredForNext(1),0);state.applyDerivedGrowth();notice.invoke(v);String banner=ItemWindowReferenceTest.field(v,"rewardBanner");assertTrue(banner.contains("HP +"+(state.rpg().baseMaxHp()-hp)));assertTrue(banner.contains("MP +"+(state.rpg().baseMaxMp()-mp)));capture(v,"growth-receipt");
 }
 void capture(GameView view,String name)throws Exception{Bitmap b=Bitmap.createBitmap(960,540,Bitmap.Config.ARGB_8888);view.draw(new Canvas(b));save(b,name);b.recycle();}
 void save(Bitmap b,String name)throws Exception{File d=new File("build/reports/growth-grip-v168");d.mkdirs();try(FileOutputStream out=new FileOutputStream(new File(d,name+".png"))){assertTrue(b.compress(Bitmap.CompressFormat.PNG,100,out));}}
}
