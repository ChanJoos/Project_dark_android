package com.projectdark.mobile;
import android.content.Context;
import android.graphics.*;
import com.projectdark.mobile.world.*;
import java.util.*;
import java.lang.reflect.*;
import java.nio.file.Files;
import java.nio.file.Path;
import org.json.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.*;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class) @Config(sdk=34,manifest=Config.NONE) @GraphicsMode(GraphicsMode.Mode.NATIVE)
public class AbelCoastV139Test {
 Context context;
 @Before public void reset(){context=RuntimeEnvironment.getApplication();context.getSharedPreferences("project_dark_f5m_v1",0).edit().clear().commit();F5mSaveStore.install(context);}
 static void restoreD04(RpgProgressionState r,String job)throws Exception{
  long exp=0;for(int i=1;i<41;i++)exp+=LevelExpCurve.requiredForNext(i);r.grantAdaptedReward((int)exp,10000);assertTrue(r.chooseInitialJob(job));
  JSONArray complete=new JSONArray();for(CampaignProgress.Def d:r.campaign().route(job))if(!d.id.startsWith("E"))complete.put(d.id);
  assertTrue(r.campaign().restore(r.campaign().snapshot().put("complete",complete)));
 }
 @Test public void coastIncludesEveryVideoFamilyAndShorePropsRespectTideAndProtectedReturnCorridor(){
  Set<String> species=new HashSet<>(),objects=new HashSet<>();
  for(String map:AbelCoastGeometry.ROUTE){
   for(RuntimeState.Monster m:AbelCoastMonsters.spawn(map)){species.add(m.campaignRewardProfileId);assertTrue(m.y<AbelCoastGeometry.shoreY(map,m.x)-20);assertTrue(m.x>=448);}
   assertTrue("coast stays inside its square bounds",AbelCoastGeometry.shoreY(map,384)<AbelCoastGeometry.height(map));
   for(AbelCoastGeometry.Prop p:AbelCoastGeometry.props(map)){objects.add(p.asset);if(p.solid)assertTrue("return portal corridor",Math.hypot(p.x-PoteCampaignMapDef.forId(map).backX,p.y-PoteCampaignMapDef.forId(map).backY)>=160);}
  }
  assertEquals(new HashSet<>(AbelCoastMonsters.ALL),species);
  assertFalse(PoteForestMonsterShowcase.containsMonster("ADAPTED_COAST_RED_OCTOPUS"));
  for(String map:AbelCoastGeometry.ROUTE){Set<String> art=new HashSet<>();for(RuntimeState.Monster m:AbelCoastMonsters.spawn(map))art.add(PoteForestMonsterShowcase.artKey(m.id));assertTrue(map+" all-pose/hit prewarm bound",art.size()<=5);}
  assertTrue(objects.containsAll(Arrays.asList("palm_curve","palm_fan","banana","fern","reef_low","reef_tall","barrels","shells","bones","driftwood")));
 }
 @Test public void oldD04ProgressOpensCoastWithoutRegrantingAnythingAndNormalEntryStaysGated()throws Exception{
  RuntimeState s=new RuntimeState();restoreD04(s.rpg(),"WARRIOR");Map<String,Integer> inv=new LinkedHashMap<>(s.rpg().inventory());long exp=CampaignProgress.earnedExp(s.rpg()),gold=s.rpg().gold();
  JSONObject old=s.rpg().campaign().snapshot();assertTrue(s.rpg().campaign().restore(old));assertEquals(inv,s.rpg().inventory());assertEquals(exp,CampaignProgress.earnedExp(s.rpg()));assertEquals(gold,s.rpg().gold().longValue());assertEquals("E01",s.rpg().campaign().next(s.rpg()).id);assertFalse(s.rpg().campaign().finished());
  assertFalse(CampaignWorld.canEnter(AbelCoastGeometry.ENTRY,40,true));assertFalse(CampaignWorld.canEnter(AbelCoastGeometry.ENTRY,41,false));assertTrue(CampaignWorld.canEnter(AbelCoastGeometry.ENTRY,41,true));assertFalse(s.rpg().campaign().claim("D03",s.rpg()));
 }
 @Test public void normalFiveJobsCompleteCoastThroughResolverAndOneTimeSavedTurnIns()throws Exception{
  JSONObject report=new JSONObject();for(String job:CampaignProgress.JOBS){reset();RuntimeState s=new RuntimeState();restoreD04(s.rpg(),job);SkillBook b=SkillBook.load(context);b.bindJob(s.rpg()::currentJobCode);RuntimeCombatSession session=new RuntimeCombatSession(s,(a,t)->true,(a,id)->b.usable(id),a->true);
   F5mSaveStore.bindRuntime(s,F5mAdaptedPrologueQuest.openingFixture(),new GrowthQuest2());F5mSaveStore.restoreAndBindSkillsActive(b);int kills=0;
   for(int n=1;n<=8;n++){CampaignProgress.Def d=s.rpg().campaign().next(s.rpg());assertEquals(String.format(Locale.ROOT,"E%02d",n),d.id);CampaignProgressTest.enter(s,d.map);CampaignProgressTest.allocate(s.rpg(),job);s.applyDerivedGrowth();assertTrue(job+" accept "+d.id,s.rpg().campaign().accept(d.id,s.rpg(),b));
    while(s.rpg().campaign().status(d,s.rpg())!=CampaignProgress.Status.REPORT){RuntimeState.Monster target=null;for(RuntimeState.Monster m:s.monsters())if(m.alive&&s.rpg().campaign().wanted(d,m.campaignRewardProfileId)){target=m;break;}assertNotNull(d.id,target);CampaignProgressTest.rest(s);CampaignProgressTest.kill(s,session,b,target,null);kills++;assertTrue(kills<100);}
    assertTrue(F5mSaveStore.transactActive(s.rpg(),b,()->s.rpg().campaign().claim(d.id,s.rpg())));long before=CampaignProgress.earnedExp(s.rpg()),gold=s.rpg().gold();assertFalse(s.rpg().campaign().claim(d.id,s.rpg()));assertEquals(before,CampaignProgress.earnedExp(s.rpg()));assertEquals(gold,s.rpg().gold().longValue());
    assertTrue(F5mSaveStore.checkpointActive());RpgProgressionState restored=new RpgProgressionState();F5mSaveStore.restoreRewardsActive(restored);assertEquals(s.rpg().campaign().snapshot().getJSONArray("complete").toString(),restored.campaign().snapshot().getJSONArray("complete").toString());assertEquals(s.rpg().inventory(),restored.inventory());assertEquals(s.rpg().normalLevel(),restored.normalLevel());
   }
   assertFalse(s.rpg().campaign().finished());assertEquals("E09",s.rpg().campaign().next(s.rpg()).id);assertTrue(s.rpg().normalLevel()>=55);assertFalse(b.testAccess());assertTrue(s.rpg().gold()>0);assertTrue(s.rpg().inventory().getOrDefault("IT_RING_THREELINEGOLD",0)==0);
   report.put(job,new JSONObject().put("level",s.rpg().normalLevel()).put("kills",kills).put("gold",s.rpg().gold()).put("scope","D04 fixture; actual Resolver kills/transactions/checkpoint restore; no handset FPS claim"));
  }Files.createDirectories(Path.of("build/reports/abel-v139"));Files.write(Path.of("build/reports/abel-v139/five-jobs.json"),report.toString(2).getBytes(java.nio.charset.StandardCharsets.UTF_8));
 }
 @Test public void everyMapGateNpcAndEveryActorHasConnectedSharedLiveCollisionRoute()throws Exception{
  for(String map:AbelCoastGeometry.ROUTE){RuntimeState s=new RuntimeState();s.enterCampaignMap(map,false);WorldRuntimeAdapter w=CampaignNavigationTest.adapter(s);PoteCampaignMapDef d=PoteCampaignMapDef.forId(map);assertSame(d.navigationTiles(),d.navigationTiles());assertSame(CampaignWorld.connectedTiles(map),CampaignWorld.connectedTiles(map));
   assertEquals(map,s.currentMapId());assertFalse(s.blocked(s.player().x,s.player().y));assertEquals(map.equals(AbelCoastGeometry.ENTRY)?0:AbelCoastGeometry.index(map)==1?72:AbelCoastGeometry.index(map)==2?96:108,s.monsters().size());
   for(RuntimeState.Npc npc:s.npcs())assertTrue(map+" "+npc.id,CampaignNavigationTest.walk(w,w.requestNpcApproach(npc.id)));
   for(RuntimeState.Monster m:s.monsters()){assertTrue(PoteForestMonsterShowcase.containsMonster(m.id));assertNotNull(new AdaptedCampaignRewardCatalog().find(m.campaignRewardProfileId));assertTrue(map+" "+m.id,CampaignNavigationTest.walk(w,w.requestMonsterApproach(m.id,CanonicalMeleeTileContract.REACH_DISTANCE)));}
   assertTrue(map+" forward",CampaignNavigationTest.walk(w,w.requestGroundWorld(CampaignWorld.forward(map).x,CampaignWorld.forward(map).y)));assertTrue(map+" back",CampaignNavigationTest.walk(w,w.requestGroundWorld(CampaignWorld.backward(map).x,CampaignWorld.backward(map).y)));s.enterCampaignMap(map,true);assertTrue(Math.hypot(s.player().x-CampaignWorld.forward(map).x,s.player().y-CampaignWorld.forward(map).y)>70);
   for(RectF water:AbelCoastGeometry.water(map))assertTrue(d.terrainBlocked(water.centerX(),water.centerY(),0));for(AbelCoastGeometry.Prop prop:AbelCoastGeometry.props(map))if(prop.solid)assertTrue(d.terrainBlocked(prop.x,prop.y,0));
  }
 }
 @Test public void actualTravelDialogueOffersTestPreviewWithoutGrantingLevelOrCompletingD04()throws Exception{
  GameView v=new GameView(context);v.setSkillTestMode(true);v.layout(0,0,960,540);RuntimeState s=(RuntimeState)ForestPixelCacheTest.field(v,"state");SkillBook b=(SkillBook)ForestPixelCacheTest.field(v,"skillBook");assertTrue(b.testAccess());RuntimeState.Npc npc=null;for(RuntimeState.Npc n:s.npcs())if(n.id.equals("pote_travel_guide"))npc=n;assertNotNull(npc);s.player().x=npc.x;s.player().y=npc.y;
  InteractionController interaction=(InteractionController)ForestPixelCacheTest.field(v,"interaction");interaction.request(s,npc);int level=s.rpg().normalLevel();long exp=CampaignProgress.earnedExp(s.rpg());QuestJournalTest.gesture(v,682f,426f);assertEquals(AbelTownGeometry.ID,s.currentMapId());assertEquals(level,s.rpg().normalLevel().intValue());assertEquals(exp,CampaignProgress.earnedExp(s.rpg()));assertFalse(s.rpg().campaign().forestFinished());
  Method gate=GameView.class.getDeclaredMethod("checkCampaignGate");gate.setAccessible(true);WorldMoveTargetController.TileCenter exit=CampaignWorld.forward(AbelTownGeometry.ID);s.player().x=exit.x;s.player().y=exit.y;assertEquals(false,gate.invoke(v));RuntimeState.Npc departure=s.npcs().stream().filter(n->n.id.equals("abel_coast_departure")).findFirst().get();WorldRuntimeAdapter travel=(WorldRuntimeAdapter)ForestPixelCacheTest.field(v,"poteFieldAdapter");assertTrue(CampaignNavigationTest.walk(travel,travel.requestNpcApproach(departure.id)));assertEquals(InteractionController.TickResult.DIALOG_OPENED,interaction.request(s,departure));QuestJournalTest.gesture(v,667,429);assertEquals(AbelCoastGeometry.ENTRY,s.currentMapId());for(int i=0;i<AbelCoastGeometry.ROUTE.length-1;i++){WorldMoveTargetController.TileCenter at=CampaignWorld.forward(s.currentMapId());s.player().x=at.x;s.player().y=at.y;assertEquals(true,gate.invoke(v));assertEquals(AbelCoastGeometry.ROUTE[i+1],s.currentMapId());}assertEquals(level,s.rpg().normalLevel().intValue());assertEquals(exp,CampaignProgress.earnedExp(s.rpg()));
 }
 @Test public void actualQuickQuestTravelsToAbelHuntAndKillsItsSpecies()throws Exception{
  GameView v=new GameView(context);v.layout(0,0,960,540);RuntimeState s=(RuntimeState)ForestPixelCacheTest.field(v,"state");restoreD04(s.rpg(),"WARRIOR");v.setSkillTestMode(false);((F5mAdaptedPrologueQuest)ForestPixelCacheTest.field(v,"f5mQuest")).restore(F5mAdaptedPrologueQuest.State.COMPLETED,1);((GrowthQuest2)ForestPixelCacheTest.field(v,"quest2")).restore(GrowthQuest2.State.COMPLETED,3);
  JSONObject saved=s.rpg().campaign().snapshot();saved.getJSONArray("complete").put("E01");saved.put("active","E02");assertTrue(s.rpg().campaign().restore(saved));CampaignProgressTest.allocate(s.rpg(),"WARRIOR");s.applyDerivedGrowth();CampaignResources.freeRest(s);TownInteriorTest.call(v,"enterPoteField");Method change=GameView.class.getDeclaredMethod("changeCampaignMap",String.class,boolean.class);change.setAccessible(true);change.invoke(v,AbelCoastGeometry.ENTRY,false);QuestJournalTest.gesture(v,790,104);assertTrue((Boolean)ForestPixelCacheTest.field(v,"autoAttackEnabled"));
  Method update=GameView.class.getDeclaredMethod("update",float.class);update.setAccessible(true);CombatController combat=(CombatController)ForestPixelCacheTest.field(v,"combat");boolean selected=false;for(int i=0;i<5000&&s.rpg().campaign().count(CampaignProgress.find("E02"),s.rpg())==0;i++){update.invoke(v,.05f);if(selected)assertEquals("hunt must not accidentally cross the return portal","MAP_ABEL_COAST_1A",s.currentMapId());if(combat.target()!=null){assertEquals("MAP_ABEL_COAST_1A",s.currentMapId());assertEquals("ABEL_TURTLE",combat.target().campaignRewardProfileId);combat.target().hp=1;selected=true;}}
  assertTrue("live quick quest selects an Abel turtle",selected);assertTrue("real AUTO attack records the kill; alive="+s.player().alive+" hp="+s.player().hp+" position="+s.player().x+","+s.player().y+" auto="+ForestPixelCacheTest.field(v,"autoAttackEnabled")+" map="+s.currentMapId()+" feedback="+ForestPixelCacheTest.field(v,"feedback")+" movement="+((WorldRuntimeAdapter)ForestPixelCacheTest.field(v,"poteFieldAdapter")).movement().snapshot().status,s.rpg().campaign().count(CampaignProgress.find("E02"),s.rpg())>0);
 }
 @Test public void restartRestoresEveryCoastMapAndExactPosition()throws Exception{
  GameView v=new GameView(context);v.layout(0,0,960,540);RuntimeState state=(RuntimeState)ForestPixelCacheTest.field(v,"state");restoreD04(state.rpg(),"WARRIOR");TownInteriorTest.call(v,"enterPoteField");
  Method change=GameView.class.getDeclaredMethod("changeCampaignMap",String.class,boolean.class);change.setAccessible(true);
  for(String map:AbelCoastGeometry.ROUTE){change.invoke(v,map,false);float x=state.player().x,y=state.player().y;assertTrue(F5mSaveStore.checkpointActive());F5mSaveStore.install(context);GameView restored=new GameView(context);RuntimeState other=(RuntimeState)ForestPixelCacheTest.field(restored,"state");assertEquals(map,other.currentMapId());assertEquals(x,other.player().x,.01f);assertEquals(y,other.player().y,.01f);assertEquals("E01",other.rpg().campaign().next(other.rpg()).id);assertEquals(state.rpg().gold(),other.rpg().gold());F5mSaveStore.bindRuntime(state,(F5mAdaptedPrologueQuest)ForestPixelCacheTest.field(v,"f5mQuest"),(GrowthQuest2)ForestPixelCacheTest.field(v,"quest2"));}
 }
 @Test public void actualGameViewCoastTouchesAndSourceScenesAreProfiled()throws Exception{
  GameView v=new GameView(context);v.layout(0,0,960,540);RuntimeState s=(RuntimeState)ForestPixelCacheTest.field(v,"state");restoreD04(s.rpg(),"WARRIOR");v.setSkillTestMode(false);((F5mAdaptedPrologueQuest)ForestPixelCacheTest.field(v,"f5mQuest")).restore(F5mAdaptedPrologueQuest.State.COMPLETED,1);((GrowthQuest2)ForestPixelCacheTest.field(v,"quest2")).restore(GrowthQuest2.State.COMPLETED,3);CampaignProgressTest.allocate(s.rpg(),"WARRIOR");s.applyDerivedGrowth();CampaignResources.freeRest(s);TownInteriorTest.call(v,"enterPoteField");
  Method change=GameView.class.getDeclaredMethod("changeCampaignMap",String.class,boolean.class),hud=GameView.class.getDeclaredMethod("isHudSurface",float.class,float.class);change.setAccessible(true);hud.setAccessible(true);List<Double> timings=new ArrayList<>();Path folder=Path.of("build/reports/abel-v139");Files.createDirectories(folder);
  for(String map:AbelCoastGeometry.ROUTE){change.invoke(v,map,false);WorldRuntimeAdapter w=(WorldRuntimeAdapter)ForestPixelCacheTest.field(v,"poteFieldAdapter");List<WorldMoveTargetController.TileCenter> cells=new ArrayList<>();
   for(WorldMoveTargetController.TileCenter t:w.navigationTiles()){WorldCameraTransform.Point p=w.worldToScreen(t.x,t.y);if(p.x>320&&p.x<620&&p.y>170&&p.y<340&&!(Boolean)hud.invoke(v,p.x,p.y)&&s.hitNpc(t.x,t.y,34)==null&&s.hitMonster(t.x,t.y,34)==null&&w.canPlayerOccupy(t.x,t.y))cells.add(t);}assertTrue(map+" empty touch cells",cells.size()>8);
   for(int i=0;i<8;i++){WorldMoveTargetController.TileCenter t=cells.get(i*(cells.size()-1)/7);WorldCameraTransform.Point p=w.worldToScreen(t.x,t.y);long before=w.movement().snapshot().requestId,n=System.nanoTime();QuestJournalTest.gesture(v,p.x,p.y);timings.add((System.nanoTime()-n)/1e6);assertTrue(map+" real callback request",w.movement().snapshot().requestId>before);}
   Bitmap frame=Bitmap.createBitmap(960,540,Bitmap.Config.ARGB_8888);v.draw(new Canvas(frame));try(java.io.OutputStream out=Files.newOutputStream(folder.resolve("gameview-"+map+".png"))){frame.compress(Bitmap.CompressFormat.PNG,100,out);}frame.recycle();
   if(map.equals("MAP_ABEL_COAST_4C")){
    float[][] review={{1250,800},{2350,810},{3650,800}};
    for(int i=0;i<review.length;i++){
     WorldMoveTargetController.TileCenter at=null;float best=Float.MAX_VALUE;
     for(WorldMoveTargetController.TileCenter t:w.navigationTiles())if(w.canPlayerOccupy(t.x,t.y)){float q=(t.x-review[i][0])*(t.x-review[i][0])+(t.y-review[i][1])*(t.y-review[i][1]);if(q<best){best=q;at=t;}}
     assertNotNull(at);s.player().x=at.x;s.player().y=at.y;w.cancel();w.snapCameraToPlayer();
     Bitmap shot=Bitmap.createBitmap(960,540,Bitmap.Config.ARGB_8888);v.draw(new Canvas(shot));try(java.io.OutputStream out=Files.newOutputStream(folder.resolve("gameview-coast-detail-"+i+".png"))){shot.compress(Bitmap.CompressFormat.PNG,100,out);}shot.recycle();
    }
   }
  }
  Collections.sort(timings);JSONObject report=new JSONObject().put("scope","Robolectric real DOWN/UP callbacks with live actors, not handset FPS").put("taps",timings.size()).put("p95Ms",timings.get((int)Math.ceil(timings.size()*.95)-1)).put("p99Ms",timings.get((int)Math.ceil(timings.size()*.99)-1)).put("maxMs",timings.get(timings.size()-1));Files.write(folder.resolve("touch-profile.json"),report.toString(2).getBytes(java.nio.charset.StandardCharsets.UTF_8));
 }
 @Test public void nativeScenesFullPoseSetsAndTwelveMapRoundTripsKeepCacheBounded()throws Exception{
  Path folder=Path.of("build/reports/abel-v139");Files.createDirectories(folder);PoteFieldRenderer r=new PoteFieldRenderer();int maxPairs=0;
  for(int trip=0;trip<12;trip++){String map=trip%2==0?AbelCoastGeometry.ROUTE[1+(trip/2)%(AbelCoastGeometry.ROUTE.length-1)]:"MAP_POTE_03";RuntimeState s=new RuntimeState();s.enterCampaignMap(map,false);PoteCampaignMapDef d=PoteCampaignMapDef.forId(map);WorldRuntimeAdapter w=CampaignNavigationTest.adapter(s);r.prepareMonsters(s);int pairs=((Map<?,?>)ForestPixelCacheTest.field(r,"hitCache")).size();maxPairs=Math.max(maxPairs,pairs);assertTrue(pairs<=60);
   for(int spot=0;spot<6;spot++){w.camera().snapTo(500+spot*550,spot%2==0?880:760);Bitmap frame=Bitmap.createBitmap(960,540,Bitmap.Config.ARGB_8888);Canvas c=new Canvas(frame);List<PoteFieldRenderer.ActorDraw> actors=new ArrayList<>();for(RuntimeState.Monster m:s.monsters()){WorldCameraTransform.Point p=w.worldToScreen(m.x,m.y);actors.add(new PoteFieldRenderer.ActorDraw(m.x,m.y,72,50,()->r.drawMonster(c,m,p.x,p.y)));}r.drawScene(c,w,actors);assertTrue(((Map<?,?>)ForestPixelCacheTest.field(r,"floorChunks")).size()<=32);if(trip==0){try(java.io.OutputStream out=Files.newOutputStream(folder.resolve("coast-spot-"+spot+".png"))){frame.compress(Bitmap.CompressFormat.PNG,100,out);}}frame.recycle();}
  }
  Bitmap sheet=Bitmap.createBitmap(1000,AbelCoastMonsters.ALL.size()*420,Bitmap.Config.ARGB_8888);Canvas c=new Canvas(sheet);c.drawColor(0xffc9a166);int species=0;for(String id:AbelCoastMonsters.ALL){int pose=0;for(String state:new String[]{"idle","walk","attack"}){int dir=0;for(CharacterRenderer.Direction direction:CharacterRenderer.Direction.values()){String path=PoteForestMonsterShowcase.assetPath(id,state,direction);try(java.io.InputStream in=context.getAssets().open(path)){Bitmap b=BitmapFactory.decodeStream(in);assertEquals(192,b.getWidth());assertEquals(192,b.getHeight());assertEquals(0,b.getPixel(0,0)>>>24);}r.drawMonsterTestPose(c,id,state,direction,.6f,1f,120+dir++*245,species*420+pose*130+125);}pose++;}species++;}try(java.io.OutputStream out=Files.newOutputStream(folder.resolve("all-156-poses.png"))){sheet.compress(Bitmap.CompressFormat.PNG,100,out);}System.out.println("ABEL_MAX_MONSTER_SOURCE_HIT_PAIRS="+maxPairs+"; FLOOR_PIXEL_LIMIT="+(32*260*260*4)+"; ROUND_TRIPS=12");
 }
 @Test public void everyAdmittedCoastIdentityUsesRealResolverExactlyOnceAndRestoresRewards()throws Exception{
  for(String species:AbelCoastMonsters.ALL){reset();RuntimeState s=new RuntimeState();restoreD04(s.rpg(),"WARRIOR");String map=null;for(String id:AbelCoastGeometry.ROUTE)if(AbelCoastMonsters.roster(id).contains(species)){map=id;break;}assertNotNull(species,map);s.enterCampaignMap(map,false);CampaignProgressTest.allocate(s.rpg(),"WARRIOR");s.applyDerivedGrowth();CampaignResources.freeRest(s);
   SkillBook b=SkillBook.load(context);b.bindJob(s.rpg()::currentJobCode);RuntimeCombatSession session=new RuntimeCombatSession(s,(a,t)->true,(a,id)->b.usable(id),a->true);F5mSaveStore.bindRuntime(s,F5mAdaptedPrologueQuest.openingFixture(),new GrowthQuest2());F5mSaveStore.restoreAndBindSkillsActive(b);
   RuntimeState.Monster actor=null;for(RuntimeState.Monster m:s.monsters())if(species.equals(m.campaignRewardProfileId)){actor=m;break;}assertNotNull(species,actor);long before=CampaignProgress.earnedExp(s.rpg());int receipts=s.rpg().rewardHistory().size();CampaignProgressTest.kill(s,session,b,actor,null);AdaptedCampaignRewardCatalog.Reward reward=new AdaptedCampaignRewardCatalog().find(species);assertEquals(before+reward.exp,CampaignProgress.earnedExp(s.rpg()));assertEquals(receipts+1,s.rpg().rewardHistory().size());s.rpg().consumeCombat(s.ledger().snapshot(),s);assertEquals(receipts+1,s.rpg().rewardHistory().size());assertTrue(F5mSaveStore.checkpointActive());RpgProgressionState restored=new RpgProgressionState();F5mSaveStore.restoreRewardsActive(restored);assertEquals(CampaignProgress.earnedExp(s.rpg()),CampaignProgress.earnedExp(restored));assertEquals(s.rpg().gold(),restored.gold());assertEquals(s.rpg().inventory(),restored.inventory());
  }
 }
}
