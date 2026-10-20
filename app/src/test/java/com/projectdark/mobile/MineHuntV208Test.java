package com.projectdark.mobile;
import android.content.Context;
import android.graphics.*;
import com.projectdark.mobile.world.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.*;
import java.util.*;
import java.io.*;
import java.lang.reflect.*;
import org.json.*;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class) @Config(sdk=34,manifest=Config.NONE) @GraphicsMode(GraphicsMode.Mode.NATIVE)
public class MineHuntV208Test {
 private MineTownV203Test f;private Context context;
 @Before public void reset(){f=new MineTownV203Test();f.reset();context=RuntimeEnvironment.getApplication();}
 private GameView enter()throws Exception{GameView v=f.town();RuntimeState s=f.field(v,"state");s.rpg().restoreProgression(99,0);f.approach(v,"mine_town_travel");RectF card=TownTravelWindow.cardFor(MineTownGeometry.ID,MineHuntGeometry.ID);assertNotNull(card);f.shot(v,"v208-mine-destination");f.tap(v,card.centerX(),card.centerY());assertEquals(MineHuntGeometry.ID,s.currentMapId());return v;}
 @Test public void realMineManagerCardEntersTenSpeciesAndSaveRestoresActualHunt()throws Exception{
  GameView v=enter();RuntimeState s=f.field(v,"state");assertEquals(100,s.monsters().size());assertTrue(s.npcs().isEmpty());Set<Integer> forms=new HashSet<>();for(RuntimeState.Monster m:s.monsters()){forms.add(MineHuntMonsters.type(m.id));assertTrue(MineHuntGeometry.inside(m.x,m.y));assertEquals(MineHuntMonsters.HP[MineHuntMonsters.type(m.id)],m.maxHp);}
  assertEquals(10,forms.size());f.shot(v,"v208-entry");assertTrue(F5mSaveStore.checkpointActive());float x=s.player().x,y=s.player().y;F5mSaveStore.install(context);GameView restored=new GameView(context);restored.layout(0,0,960,540);RuntimeState saved=f.field(restored,"state");assertEquals(MineHuntGeometry.ID,saved.currentMapId());assertEquals(x,saved.player().x,0);assertEquals(y,saved.player().y,0);assertEquals(100,saved.monsters().size());
 }
 @Test public void originalCellsRetainRecordedCartWallRegionsAndConnectedNavigation()throws Exception{
  assertEquals(2102,MineHuntGeometry.grid().sourceId);assertEquals(150,MineHuntGeometry.grid().width);assertEquals(102,MineHuntGeometry.grid().height);assertEquals(9468,CampaignWorld.connectedTiles(MineHuntGeometry.ID).size());
  RuntimeState s=new RuntimeState();s.enterCampaignMap(MineHuntGeometry.ID,false);WorldRuntimeAdapter w=CampaignNavigationTest.adapter(s);
  // Terrain-only path fixture: live-actor collision is checked separately in shared regression.
  for(RuntimeState.Monster m:s.monsters())m.alive=false;
  for(int[] uv:new int[][]{{87,37},{35,76},{78,82},{102,76},{112,78}}){WorldMoveTargetController.TileCenter t=CampaignWorld.nearestConnectedTile(MineHuntGeometry.ID,MineHuntGeometry.x(uv[0],uv[1]),MineHuntGeometry.y(uv[0],uv[1]));assertTrue(CampaignNavigationTest.walk(w,w.requestGroundWorld(t.x,t.y)));}
  assertFalse(MineHuntGeometry.inside(-100,-100));assertFalse(MineHuntGeometry.inside(MineHuntGeometry.x(0,0),MineHuntGeometry.y(0,0)));assertEquals(.60f,w.tileStepSeconds(),0);
 }
 @Test public void everyProfileUsesDistinctCombatAndHigherRewardsThanEarlierHunts(){
  AdaptedCampaignRewardCatalog rewards=new AdaptedCampaignRewardCatalog();long priorGold=0;int priorExp=0;for(AdaptedCampaignRewardCatalog.Reward r:rewards.entries().values())if(!MineHuntMonsters.actor(r.monsterId)){priorGold=Math.max(priorGold,r.gold);priorExp=Math.max(priorExp,r.exp);}
  Set<Integer> hp=new HashSet<>(),damage=new HashSet<>();Set<String> spells=new HashSet<>();for(AbelCombatProfile p:MineHuntMonsters.all()){assertSame(p,AbelCombatProfile.forId(p.id+"#life"));hp.add(p.hp);damage.add(p.damage);spells.add(p.spell);assertTrue(p.exp>priorExp);assertTrue(p.gold>priorGold);assertEquals(p.exp,rewards.find(p.id).exp);}assertTrue(hp.size()>=8);assertEquals(10,damage.size());assertTrue(spells.size()>=6);
 }
 @Test public void allFiveArmorDropsUseRealDefeatReceiptsDirectInventoryAndNoReplay()throws Exception{
  GameView v=enter();RuntimeState s=f.field(v,"state");long gold=s.rpg().gold();Set<String> gained=new HashSet<>();
  for(int t=5;t<10;t++){RuntimeState.Monster m=s.monsters().get(t);String item=MineHuntMonsters.armor(m.id);assertNotNull(s.rpg().itemDefinitions().get(item));int start=s.rpg().inventory().getOrDefault(item,0);for(int k=0;k<400&&s.rpg().inventory().getOrDefault(item,0)==start;k++){m.alive=true;m.hp=m.maxHp;s.damage(m,m.maxHp);s.rpg().consumeCombat(s.ledger().snapshot(),s);}assertEquals(start+1,(int)s.rpg().inventory().get(item));gained.add(item);}
  assertEquals(5,gained.size());assertTrue(s.rpg().gold()>gold);Map<String,Integer> before=new LinkedHashMap<>(s.rpg().inventory());long earned=s.rpg().gold();s.rpg().consumeCombat(s.ledger().snapshot(),s);assertEquals(before,s.rpg().inventory());assertEquals(earned,s.rpg().gold().longValue());
  assertTrue(F5mSaveStore.checkpointActive());F5mSaveStore.install(context);GameView restored=new GameView(context);RuntimeState saved=f.field(restored,"state");assertEquals(before,saved.rpg().inventory());assertEquals(earned,saved.rpg().gold().longValue());assertEquals(MineHuntGeometry.ID,saved.currentMapId());
 }
 @Test public void monsterPhysicalAndMagicUseSharedResolverResourcesAndPositiveDamage(){
  RuntimeState s=new RuntimeState();s.enterCampaignMap(MineHuntGeometry.ID,false);RuntimeCombatSession combat=new RuntimeCombatSession(s,(a,b)->true,(a,b)->true,a->true);RuntimeState.Monster m=s.monsters().get(8);WorldMoveTargetController.TileCenter e=MineHuntGeometry.entry();m.x=e.x+32;m.y=e.y+16;m.mp=240;s.player().hp=s.player().maxHp;int hp=s.player().hp;MonsterAutoCombatBridge.Result hit=combat.monsterAutoBridge().submit(m.id,"player","MONSTER_FIRE");assertEquals(MonsterAutoCombatBridge.Outcome.ACCEPTED,hit.outcome);combat.tick(1f);assertTrue(s.player().hp<hp);assertTrue(m.mp<240);
 }
 @Test public void everySourcePoseFacesFourWaysAndAllVariantAlphaBytesStayExact()throws Exception{
  MineHuntRenderer renderer=new MineHuntRenderer(context);renderer.prepare();assertTrue(renderer.atlasBytes()<=35389440L);Field field=MineHuntRenderer.class.getDeclaredField("sheets");field.setAccessible(true);Bitmap[][] sheets=(Bitmap[][])field.get(renderer);
  for(int t=0;t<10;t++){Bitmap source=sheets[0][t];for(int variant=1;variant<4;variant++){Bitmap other=sheets[variant][t];int[] a=new int[source.getWidth()*source.getHeight()],b=new int[a.length];source.getPixels(a,0,source.getWidth(),0,0,source.getWidth(),source.getHeight());other.getPixels(b,0,source.getWidth(),0,0,source.getWidth(),source.getHeight());for(int i=0;i<a.length;i++)assertEquals(a[i]>>>24,b[i]>>>24);}
   RuntimeState.Monster m=new RuntimeState.Monster(MineHuntMonsters.species(t)+"#view",MineHuntMonsters.NAMES[t],160,160,MineHuntMonsters.HP[t],"SOURCE");for(CharacterRenderer.Direction dir:CharacterRenderer.Direction.values())for(int pose=0;pose<3;pose++){m.visualFacing.setLocomotion(dir);m.isMoving=pose==1;m.attackVisualRemaining=pose==2?.3f:0;Bitmap bitmap=Bitmap.createBitmap(320,230,Bitmap.Config.ARGB_8888);renderer.monster(new Canvas(bitmap),m,new SkillEffectState());int opaque=0;for(int y=0;y<230;y++)for(int x=0;x<320;x++)if((bitmap.getPixel(x,y)>>>24)>0)opaque++;assertTrue("real original body "+t+dir+pose,opaque>120);save(bitmap,"v208-actor-"+t+"-"+dir+"-"+pose);bitmap.recycle();}}
 }
 @Test public void actualSourceLandmarksAndTwelveRoundTripsKeepWarmFloorBounded()throws Exception{
  GameView v=enter();RuntimeState s=f.field(v,"state");WorldRuntimeAdapter w=f.field(v,"poteFieldAdapter");MineHuntRenderer renderer=f.field(v,"mineHuntRenderer");
  for(int[] uv:new int[][]{{87,37},{35,76},{78,82},{102,76},{112,78}}){WorldMoveTargetController.TileCenter t=CampaignWorld.nearestConnectedTile(MineHuntGeometry.ID,MineHuntGeometry.x(uv[0],uv[1]),MineHuntGeometry.y(uv[0],uv[1]));s.player().x=t.x;s.player().y=t.y;w.snapCameraToPlayer();f.shot(v,"v208-original-"+uv[0]+"-"+uv[1]);}
  Bitmap frame=Bitmap.createBitmap(960,540,Bitmap.Config.ARGB_8888);for(int i=0;i<12;i++){v.draw(new Canvas(frame));int builds=renderer.floorBuilds();v.draw(new Canvas(frame));assertEquals(builds,renderer.floorBuilds());assertTrue(renderer.floorEntries()<=32);assertTrue(renderer.floorBytes()<=8652800L);f.call(v,"travelTown",new Class<?>[]{String.class},MineTownGeometry.ID);assertEquals(0,renderer.floorEntries());assertEquals(0,renderer.atlasBytes());f.call(v,"changeCampaignMap",new Class<?>[]{String.class,boolean.class},MineHuntGeometry.ID,false);renderer=f.field(v,"mineHuntRenderer");}frame.recycle();
 }
 private void save(Bitmap b,String name)throws Exception{File dir=new File("build/reports/horror-v179");dir.mkdirs();try(FileOutputStream out=new FileOutputStream(new File(dir,name+".png"))){assertTrue(b.compress(Bitmap.CompressFormat.PNG,100,out));}}
}
