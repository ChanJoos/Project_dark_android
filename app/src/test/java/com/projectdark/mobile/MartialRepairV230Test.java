package com.projectdark.mobile;
import static org.junit.Assert.*;
import android.graphics.*;
import java.util.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.*;
@RunWith(RobolectricTestRunner.class) @Config(sdk=34,manifest=Config.NONE) @GraphicsMode(GraphicsMode.Mode.NATIVE)
public class MartialRepairV230Test {
 final MartialSevenV228Test h=new MartialSevenV228Test();
 @Before public void setup(){h.setup();}
 SkillAbilityExecutor.Movement movement(RuntimeState s,java.util.function.BiPredicate<Float,Float> ok){return new SkillAbilityExecutor.Movement(){public boolean canLeapPlayer(float x,float y){return ok.test(x,y);}public boolean leapPlayer(float x,float y){if(!canLeapPlayer(x,y))return false;s.player().x=x;s.player().y=y;return true;}public boolean movePlayer(float x,float y){return false;}public boolean moveMonster(RuntimeState.Monster m,float x,float y){return false;}};}
 @Test public void forwardMonsterAndNpcAtEveryDistanceUseTargetRelativeLandingAndFaceBackEvenAfterKill()throws Exception{
  for(CharacterRenderer.Direction d:CharacterRenderer.Direction.values())for(int distance=1;distance<=3;distance++)for(boolean npc:new boolean[]{false,true}){
   final int range=distance;RuntimeState s=h.state();List<RuntimeState.Npc> ns=h.h.field(s,"npcs");ns.clear();float[] v=MartialSkillPolicy.step(d);String id="target";
   if(npc)ns.add(new RuntimeState.Npc(id,"NPC",v[0]*distance,v[1]*distance,"","B"));else h.add(s,id,v[0]*distance,v[1]*distance).hp=1;
   RuntimeCombatSession c=h.session(s,d);c.setSkillMovement(movement(s,(x,y)->x==v[0]*(range+1)&&y==v[1]*(range+1)));
   assertTrue(d+" "+distance+" npc="+npc,c.submitFacingMartial(MartialSourceV228.ADVANCE).accepted());c.tick(.15f);assertEquals(v[0]*(distance+1),s.player().x,0);assertEquals(v[1]*(distance+1),s.player().y,0);float[] facing=c.takeAdvanceFacing();assertNotNull(facing);assertEquals(-v[0],facing[0],0);assertEquals(-v[1],facing[1],0);if(!npc)assertFalse(s.monsters().get(0).alive);
  }
 }
 @Test public void missingForwardTargetBlockedLandingAndSideTargetDoNotSpendOrMove()throws Exception{
  RuntimeState s=h.state();RuntimeCombatSession c=h.session(s,CharacterRenderer.Direction.SE);c.setSkillMovement(movement(s,(x,y)->true));h.add(s,"side",-32,16);assertFalse(c.submitFacingMartial(MartialSourceV228.ADVANCE).accepted());h.add(s,"far",128,64);assertFalse(c.submitFacingMartial(MartialSourceV228.ADVANCE).accepted());h.add(s,"front",96,48);c.setSkillMovement(movement(s,(x,y)->false));assertFalse(c.submitFacingMartial(MartialSourceV228.ADVANCE).accepted());assertEquals(50000,s.player().mp);assertEquals(0,s.player().x,0);assertEquals(0,c.cooldownRemaining("player",MartialSourceV228.ADVANCE),0);
  c.setSkillMovement(movement(s,(x,y)->x==64&&y==64));assertTrue(c.submitFacingMartial(MartialSourceV228.ADVANCE).accepted());c.tick(.15f);assertEquals(64,s.player().x,0);assertEquals(64,s.player().y,0);
 }
 @Test public void normalQuickslotCrossesUnselectedNpcOrLethalMonsterWithAttackPoseAndFinalFacing()throws Exception{
  for(boolean npc:new boolean[]{false,true}){
   h.setup();GameView v=new GameView(h.h.c);v.layout(0,0,960,540);RuntimeState state=h.h.field(v,"state");((List<?>)h.h.field(state,"monsters")).clear();List<RuntimeState.Npc> ns=h.h.field(state,"npcs");ns.clear();state.rpg().restoreProgression(99,0);assertTrue(state.rpg().restoreJobCode("MARTIAL_ARTIST"));state.rpg().restoreStats(50,50,50,50,50,0);state.rpg().restoreBaseResources(100000,100000);state.applyDerivedGrowth();state.player().hp=100000;state.player().mp=50000;
   com.projectdark.mobile.world.WorldRuntimeAdapter w=h.h.field(v,"worldAdapter");boolean found=false;List<com.projectdark.mobile.world.WorldMoveTargetController.TileCenter> tiles=new ArrayList<>(w.navigationTiles());tiles.sort(java.util.Comparator.comparingDouble(t->Math.hypot(t.x-WorldDef.PLAYER_SPAWN_X,t.y-WorldDef.PLAYER_SPAWN_Y)));for(com.projectdark.mobile.world.WorldMoveTargetController.TileCenter t:tiles){state.player().x=t.x;state.player().y=t.y;if(w.canPlayerLeapTo(t.x+128,t.y+64)){found=true;break;}}assertTrue(found);float x=state.player().x,y=state.player().y;
   if(npc)ns.add(new RuntimeState.Npc("target","NPC",x+96,y+48,"","B"));else h.add(state,"target",x+96,y+48).hp=1;w.snapCameraToPlayer();assertTrue(w.worldToScreen(state.player().x,state.player().y).x>160);assertTrue(w.worldToScreen(state.player().x,state.player().y).x<800);CanonicalActorFacing facing=h.h.field(v,"playerFacing");facing.setLocomotion(CharacterRenderer.Direction.SE);SkillBook book=h.h.field(v,"skillBook");assertTrue(book.learn(MartialSourceV228.ADVANCE,50));assertTrue(book.assign(0,MartialSourceV228.ADVANCE));RectF rect=(RectF)h.h.call(v,"slotRect",new Class<?>[]{int.class},0);h.h.tap(v,rect.centerX(),rect.centerY());assertEquals(MartialSourceV228.ADVANCE,h.h.field(v,"activeSkillVisualId"));assertNull(((SkillApproachController)h.h.field(v,"skillApproach")).skillId);
   h.h.call(v,"update",new Class<?>[]{float.class},.15f);assertEquals(x+128,state.player().x,0);assertEquals(y+64,state.player().y,0);assertEquals(CharacterRenderer.Direction.NW,facing.presentation());h.h.call(v,"update",new Class<?>[]{float.class},.1f);h.h.capture(v,"v230-advance-"+(npc?"npc":"lethal"));h.h.call(v,"update",new Class<?>[]{float.class},.6f);assertEquals(CharacterRenderer.Direction.NW,facing.presentation());if(!npc)assertFalse(state.monsters().get(0).alive);
  }
 }
 @Test public void advanceSharesRandomSideFallbackAndRevalidatesMovedTargetAtContact()throws Exception{
  Set<String> landings=new HashSet<>();for(double roll:new double[]{0,.999}){RuntimeState s=h.state();h.add(s,"m",32,16);RuntimeCombatSession c=h.session(s,CharacterRenderer.Direction.SE);RuntimeCombatPortAdapter port=h.h.field(c,"port");port.abilities.random=()->roll;c.setSkillMovement(movement(s,(x,y)->x!=64||y!=32));assertTrue(c.submitFacingMartial(MartialSourceV228.ADVANCE).accepted());c.tick(.15f);landings.add(s.player().x+","+s.player().y);}assertEquals(new HashSet<>(Arrays.asList("0.0,32.0","64.0,0.0")),landings);
  RuntimeState s=h.state();RuntimeState.Monster m=h.add(s,"moving",32,16);RuntimeCombatSession c=h.session(s,CharacterRenderer.Direction.SE);c.setSkillMovement(movement(s,(x,y)->true));assertTrue(c.submitFacingMartial(MartialSourceV228.ADVANCE).accepted());m.x=96;m.y=48;c.tick(.15f);assertEquals(128,s.player().x,0);assertEquals(64,s.player().y,0);assertTrue(m.hp<m.maxHp);
 }
 @Test public void pacheonHitsOnlyFourAdjacentDirectionsAndBungsinUsesBunggakProfile()throws Exception{
  RuntimeState s=h.state();for(CharacterRenderer.Direction d:CharacterRenderer.Direction.values()){float[] v=MartialSkillPolicy.step(d);h.add(s,d.name(),v[0],v[1]);h.add(s,"far"+d.name(),v[0]*2,v[1]*2);}h.add(s,"diagonal",64,0);
  RuntimeCombatSession c=h.session(s,CharacterRenderer.Direction.SE);assertTrue(c.submitPlayer("player",MartialSourceV228.PACHEON).accepted());List<CombatResolver.Event> es=c.tick(.15f).events;for(CharacterRenderer.Direction d:CharacterRenderer.Direction.values()){assertEquals(1,h.hits(es,d.name()));assertEquals(0,h.hits(es,"far"+d.name()));}assertEquals(0,h.hits(es,"diagonal"));
  SkillPresentationCatalog cat=new SkillPresentationCatalog(h.h.c);assertEquals(cat.get("SK_무도가_007").motion,cat.get("SK_무도가_025").motion);
 }
 @Test public void exactSourceIconsAndCompleteSourceFxRenderAndFade()throws Exception{
  SkillIconCatalog icons=new SkillIconCatalog(h.h.c);ClassicSkillReference ref=new ClassicSkillReference(h.h.c);for(String n:new String[]{"035","027","024","025","026","028","029"}){
   String id="SK_무도가_"+n;Bitmap expected=BitmapFactory.decodeStream(h.h.c.getAssets().open("skill-presentation/user-v228/icon-"+n+".png"));assertEquals(31,expected.getWidth());Bitmap actual=Bitmap.createBitmap(31,31,Bitmap.Config.ARGB_8888);assertTrue(icons.drawSquare(new Canvas(actual),id,new RectF(0,0,31,31)));assertTrue(id,actual.sameAs(expected));
  }
  java.io.File dir=new java.io.File("build/reports/horror-v179/v230");dir.mkdirs();
  for(String n:new String[]{"026","028","029"}){ClassicSkillReference.Channel ch=ref.channel("SK_무도가_"+n,false);CapturedSkillFx.Sequence seq=ch.sequence;Bitmap sheet=Bitmap.createBitmap(240*seq.durations.length,180,Bitmap.Config.ARGB_8888);Canvas cv=new Canvas(sheet);cv.drawColor(Color.rgb(40,55,48));float time=0;for(int i=0;i<seq.durations.length;i++){cv.save();cv.translate(240*i,0);ref.draw(cv,ch,time+.001f,120,135);cv.restore();time+=seq.durations[i]/1000f;}try(java.io.FileOutputStream out=new java.io.FileOutputStream(new java.io.File(dir,n+"-sequence.png"))){sheet.compress(Bitmap.CompressFormat.PNG,100,out);}int last=seq.durations.length-1;for(int y=0;y<seq.height;y++)for(int x=0;x<seq.width;x++)assertEquals(0,Color.alpha(seq.atlas.getPixel(last%seq.columns*seq.width+x,last/seq.columns*seq.height+y)));}
 }
}
