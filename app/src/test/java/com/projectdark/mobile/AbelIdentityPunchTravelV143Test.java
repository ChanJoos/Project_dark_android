package com.projectdark.mobile;

import android.content.Context;
import android.graphics.*;
import com.projectdark.mobile.world.*;
import java.lang.reflect.*;
import java.nio.file.*;
import java.nio.file.Path;
import java.util.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.*;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class) @Config(sdk=34,manifest=Config.NONE) @GraphicsMode(GraphicsMode.Mode.NATIVE)
public class AbelIdentityPunchTravelV143Test {
 Context context;
 @Before public void reset(){context=RuntimeEnvironment.getApplication();context.getSharedPreferences("project_dark_f5m_v1",0).edit().clear().commit();F5mSaveStore.install(context);}
 static Object field(Object o,String name)throws Exception{return ForestPixelCacheTest.field(o,name);}
 static void set(Object o,String name,Object value)throws Exception{Field f=o.getClass().getDeclaredField(name);f.setAccessible(true);f.set(o,value);}
 static Object call(Object o,String name,Class<?>[] types,Object... values)throws Exception{Method m=o.getClass().getDeclaredMethod(name,types);m.setAccessible(true);return m.invoke(o,values);}
 static void save(Bitmap b,String name)throws Exception{Path p=Path.of("build/reports/abel-v139",name);Files.createDirectories(p.getParent());try(java.io.OutputStream out=Files.newOutputStream(p)){b.compress(Bitmap.CompressFormat.PNG,100,out);}}
 @Test public void userNamesBindToCorrectArtWithoutDoubleCountingCrabOrGas(){
  assertEquals("애벌레",AbelCoastMonsters.name("ABEL_CATERPILLAR"));assertEquals("보라",AbelCoastMonsters.name("ABEL_OBS_PURPLE_EGG"));assertEquals("튜터",AbelCoastMonsters.name("ABEL_OBS_PURPLE_MAW"));assertEquals("광폭자",AbelCoastMonsters.name("ABEL_BERSERKER"));
  assertEquals("abel_v161/caterpillar",PoteForestMonsterShowcase.artKey("ABEL_CATERPILLAR"));assertEquals("abel_v142/orange_float",PoteForestMonsterShowcase.artKey("ABEL_GAS"));assertEquals("abel_v142/armored",PoteForestMonsterShowcase.artKey("ABEL_GRAB"));assertEquals("abel_v161/slate",PoteForestMonsterShowcase.artKey("ABEL_OBS_SLATE"));
  assertFalse(AbelCoastMonsters.ALL.contains("ABEL_CRAB"));assertFalse(AbelCoastMonsters.ALL.contains("ABEL_OBS_ORANGE_FLOAT"));assertEquals(13,new HashSet<>(AbelCoastMonsters.ALL).size());assertTrue("retained adapted giant remains much larger than ordinary octopus",PoteForestMonsterShowcase.bodyHeight("ABEL_KINGARK")>PoteForestMonsterShowcase.bodyHeight("ABEL_OCTOPUS")*2);
  for(String map:AbelCoastGeometry.ROUTE)for(RuntimeState.Monster m:AbelCoastMonsters.spawn(map))assertEquals(AbelCoastMonsters.name(m.campaignRewardProfileId),m.name);
 }
 private Bitmap actor(GameView v)throws Exception{Bitmap b=Bitmap.createBitmap(240,200,Bitmap.Config.ARGB_8888);Canvas c=new Canvas(b);c.translate(120-v.renderedPlayerWorldX(),150-v.renderedPlayerWorldY());call(v,"drawCharacter",new Class<?>[]{Canvas.class},c);return b;}
 private static int different(Bitmap a,Bitmap b){int n=0;for(int y=0;y<a.getHeight();y++)for(int x=0;x<a.getWidth();x++)if(a.getPixel(x,y)!=b.getPixel(x,y))n++;return n;}
 @Test public void normalUnlearnedBasicAttackRendersOriginalPunchForBothBodiesAndAllDirections()throws Exception{
  Bitmap sheet=Bitmap.createBitmap(1920,400,Bitmap.Config.ARGB_8888);Canvas out=new Canvas(sheet);out.drawColor(0xffc9a166);int row=0;
  for(String body:new String[]{"mm001","wm001"}){int col=0;for(WorldMoveTargetController.Direction direction:WorldMoveTargetController.Direction.values()){
   reset();GameView v=new GameView(context);v.setSkillTestMode(false);v.layout(0,0,960,540);set(v,"characterBodyIdentity",body);RuntimeState s=(RuntimeState)field(v,"state");s.rpg().grantAdaptedReward(22800,0);assertTrue(s.rpg().chooseInitialJob("MARTIAL_ARTIST"));s.applyDerivedGrowth();CampaignResources.freeRest(s);
   assertEquals(AnimationAction.PUNCH,new EquipmentActionResolver().resolveBasicAttack(s.rpg()).animationAction);RuntimeState.Monster target=s.monsters().get(0);target.hp=2000;s.player().x=target.x-direction.dx;s.player().y=target.y-direction.dy;((WorldRuntimeAdapter)field(v,"worldAdapter")).snapCameraToPlayer();((CombatController)field(v,"combat")).selectTarget(target);
   float ax=((Number)field(v,"ATK_X")).floatValue()+((Number)field(v,"hudRightOffset")).floatValue(),ay=((Number)field(v,"ATK_Y")).floatValue();QuestJournalTest.gesture(v,ax,ay);assertEquals(body+direction,"PUNCH",field(v,"action").toString());assertNull(field(v,"activeSkillVisualId"));
   Bitmap before=actor(v);int hp=target.hp;float contact=CombatResolver.attackPrototype(((CombatController)field(v,"combat")).attackDef(),((CombatController)field(v,"combat")).attackMode()).hitTime;call(v,"tickSkillCombat",new Class<?>[]{float.class},contact+.005f);set(v,"actionClock",contact+.005f);Bitmap punch=actor(v);
   assertTrue(body+direction+" source punch must differ from preparation",different(before,punch)>20);assertTrue("same actual basic action contacts target",target.hp<hp);assertTrue(((SkillVfxRenderer)field(v,"skillVfx")).pulses.isEmpty());out.drawBitmap(before,col*480,row*200,null);out.drawBitmap(punch,col*480+240,row*200,null);col++;
  }row++;}save(sheet,"v143-basic-punch-both-bodies.png");
 }
 private RuntimeState.Npc noah(RuntimeState s){for(RuntimeState.Npc n:s.npcs())if(n.id.equals("pote_travel_guide"))return n;throw new AssertionError("Noah missing");}
 @Test public void qualifiedNormalPlayerWalksToNoahAndTouchesAbelTravelWithoutTestModeOrRewardGrant()throws Exception{
  GameView v=new GameView(context);v.setSkillTestMode(false);v.layout(0,0,960,540);RuntimeState s=(RuntimeState)field(v,"state");AbelCoastV139Test.restoreD04(s.rpg(),"MARTIAL_ARTIST");RuntimeState.Npc n=noah(s);assertTrue(n.name.contains("마을 이동"));assertTrue("Noah no longer overlaps initial spawn",Math.hypot(n.x-WorldDef.PLAYER_SPAWN_X,n.y-WorldDef.PLAYER_SPAWN_Y)>70);
  WorldRuntimeAdapter w=(WorldRuntimeAdapter)field(v,"worldAdapter");assertTrue(CampaignNavigationTest.walk(w,w.requestNpcApproach(n.id)));assertEquals(InteractionController.TickResult.DIALOG_OPENED,((InteractionController)field(v,"interaction")).request(s,n));Bitmap shot=Bitmap.createBitmap(960,540,Bitmap.Config.ARGB_8888);v.draw(new Canvas(shot));save(shot,"v143-noah-abel-dialogue.png");long exp=CampaignProgress.earnedExp(s.rpg()),gold=s.rpg().gold();Map<String,Integer> items=new LinkedHashMap<>(s.rpg().inventory());QuestJournalTest.gesture(v,645,400);assertEquals(AbelTownGeometry.ID,s.currentMapId());assertEquals(exp,CampaignProgress.earnedExp(s.rpg()));assertEquals(gold,s.rpg().gold().longValue());assertEquals(items,s.rpg().inventory());assertFalse(((SkillBook)field(v,"skillBook")).testAccess());assertTrue(F5mSaveStore.checkpointActive());
 }
 @Test public void newPlayerCanVisitAbelTownButCannotEnterHuntOrGainRewards()throws Exception{
  GameView v=new GameView(context);v.setSkillTestMode(false);v.layout(0,0,960,540);RuntimeState s=(RuntimeState)field(v,"state");RuntimeState.Npc n=noah(s);WorldRuntimeAdapter w=(WorldRuntimeAdapter)field(v,"worldAdapter");assertTrue(CampaignNavigationTest.walk(w,w.requestNpcApproach(n.id)));((InteractionController)field(v,"interaction")).request(s,n);assertFalse((Boolean)call(v,"canTravelAbel",new Class<?>[]{}));Bitmap shot=Bitmap.createBitmap(960,540,Bitmap.Config.ARGB_8888);v.draw(new Canvas(shot));save(shot,"v143-noah-abel-locked-conditions.png");QuestJournalTest.gesture(v,645,400);assertEquals(AbelTownGeometry.ID,s.currentMapId());assertEquals(Integer.valueOf(1),s.rpg().normalLevel());assertFalse(CampaignWorld.canEnter(AbelCoastGeometry.ENTRY,1,false));
 }
}
