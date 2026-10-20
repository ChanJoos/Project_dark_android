package com.projectdark.mobile;
import static org.junit.Assert.*;
import android.graphics.*;
import com.projectdark.mobile.world.*;
import java.util.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.*;
@RunWith(RobolectricTestRunner.class) @Config(sdk=34,manifest=Config.NONE) @GraphicsMode(GraphicsMode.Mode.NATIVE)
public class ScreenMagicV204Test {
 final ScreenMagicV201Test h=new ScreenMagicV201Test();
 @Before public void setup(){h.setup();}
 @Test public void inverseGridAxesAreOneTileAndCameraIndependentOnEveryWorld()throws Exception{
  GameView v=new GameView(h.c);v.layout(0,0,960,540);WorldRuntimeAdapter w=h.field(v,"worldAdapter");
  for(String id:new String[]{WorldDef.ID,MineTownGeometry.ID,HorrorCastleGeometry.roomId(1),"MAP_POTE_01"}){
   if(!id.equals(WorldDef.ID)){java.lang.reflect.Field in=GameView.class.getDeclaredField("inPoteField");in.setAccessible(true);in.setBoolean(v,true);h.call(v,"changeCampaignMap",new Class<?>[]{String.class,boolean.class},id,false);w=h.field(v,"poteFieldAdapter");}
   for(WorldMoveTargetController.TileCenter t:w.navigationTiles()){
    int x=w.tileCoordinateX(t.x,t.y),y=w.tileCoordinateY(t.x,t.y);
    assertEquals(x+1,w.tileCoordinateX(t.x+32,t.y-16));assertEquals(y,w.tileCoordinateY(t.x+32,t.y-16));
    assertEquals(x,w.tileCoordinateX(t.x+32,t.y+16));assertEquals(y+1,w.tileCoordinateY(t.x+32,t.y+16));
    assertEquals(x-1,w.tileCoordinateX(t.x-32,t.y+16));assertEquals(y,w.tileCoordinateY(t.x-32,t.y+16));
    assertEquals(x,w.tileCoordinateX(t.x-32,t.y-16));assertEquals(y-1,w.tileCoordinateY(t.x-32,t.y-16));
   }
  }
 }
 @Test public void originalFixedCostFormulasUsePreChargeMpWithoutProficiencyPenalty(){
  FinalStats stats=new RuntimeState().rpg().finalStats();
  for(int mp:new int[]{0,12960,12961,20000,50000})for(String id:new String[]{"SK_마법사_043","SK_성직자_040"}){
   SkillAbilityCatalog.Ability a=SkillAbilityCatalog.get(id);assertEquals(12960,a.mpCost);assertFalse(a.allMp);
   int expected=(int)Math.round(Math.max(0,mp-12960)*(id.contains("마법사")?1.5:1.8));
   assertEquals(expected,SkillAbilityCatalog.amount(a,new SkillAbilityCatalog.Snapshot(stats,100,mp,99,0,false)));
   assertEquals(expected,SkillAbilityCatalog.amount(a,new SkillAbilityCatalog.Snapshot(stats,100,mp,99,100,false)));
  }
 }
 @Test public void restoredTestModeCastsRagnarokHolyDragonAndMeteorFromZeroMpThroughRealQuickslots()throws Exception{
  for(String id:new String[]{"SK_마법사_037","SK_성직자_040","SK_마법사_043"}){
   h.setup();GameView v=new GameView(h.c,true);v.layout(0,0,960,540);
   java.lang.reflect.Field in=GameView.class.getDeclaredField("inPoteField");in.setAccessible(true);in.setBoolean(v,true);
   h.call(v,"changeCampaignMap",new Class<?>[]{String.class,boolean.class},HorrorCastleGeometry.roomId(1),false);
   RuntimeState s=h.field(v,"state");SkillBook b=h.field(v,"skillBook");assertTrue(b.testAccess());b.restoreTestSlots(new String[]{id,null,null,null,null,null,null,null,null,null});s.player().mp=0;
   Map<String,Integer> before=new HashMap<>();for(RuntimeState.Monster m:s.monsters())before.put(m.id,m.hp);
   RectF slot=(RectF)h.call(v,"slotRect",new Class<?>[]{int.class},0);h.tap(v,slot.centerX(),slot.centerY());
   RuntimeCombatSession session=h.field(v,"combatSession");assertTrue(id,session.playerActionActive());
   h.call(v,"tickSkillCombat",new Class<?>[]{float.class},session.effectiveCastSeconds(id)+.001f);
   assertTrue(id,s.monsters().stream().anyMatch(m->m.hp<before.get(m.id)));assertEquals(0,s.player().mp);
   SkillVfxRenderer fx=h.field(v,"skillVfx");assertTrue(id,fx.pulses.stream().anyMatch(p->p.id.equals(id)&&!p.caster));
   h.capture(v,"v204-zero-mp-"+id);
  }
 }
 @Test public void normalMeteorRejectsLowMpButTestSnapshotDoesNotChangeActualResources()throws Exception{
  RuntimeState s=h.fixture();s.player().mp=12959;RuntimeCombatSession session=new RuntimeCombatSession(s,(a,t)->true,(a,id)->true,a->true);
  assertEquals(CombatResolver.RejectReason.RESOURCE,session.submitPlayer("player","SK_마법사_043").rejectReason);assertEquals(12959,s.player().mp);
  assertEquals(50000,SkillAbilityExecutor.previewMp(SkillAbilityCatalog.get("SK_마법사_043"),0,true));assertEquals(0,SkillAbilityExecutor.previewMp(SkillAbilityCatalog.get("SK_마법사_043"),0,false));
 }
 @Test public void curseHasOriginalTallSilhouetteAndSharedCleanSourceFrames()throws Exception{
  for(String name:new String[]{"blue","yellow","pink","red"}){
   org.json.JSONObject j=new org.json.JSONObject(new String(PresentationAssetBytes.read(h.c,"skill-presentation/screen-magic-v204/curse-"+name+".json"),java.nio.charset.StandardCharsets.UTF_8));
   CapturedSkillFx.Sequence seq=new CapturedSkillFx.Sequence(h.c,j);assertEquals(90,seq.width);assertEquals(120,seq.height);assertEquals(4,seq.durations.length);
   assertEquals(0,Color.alpha(seq.atlas.getPixel(0,0)));assertTrue(seq.atlas.getAllocationByteCount()<=700000);
  }
 }
 @Test public void meteorUsesCompleteOriginalAnimationAndScreenshotIcon()throws Exception{
  SkillIconCatalog icons=new SkillIconCatalog(h.c);Bitmap b=Bitmap.createBitmap(40,40,Bitmap.Config.ARGB_8888);assertTrue(icons.drawSquare(new Canvas(b),"SK_마법사_043",new RectF(0,0,40,40)));
  org.json.JSONObject j=new org.json.JSONObject(new String(PresentationAssetBytes.read(h.c,"skill-presentation/screen-magic-v204/meteor.json"),java.nio.charset.StandardCharsets.UTF_8));CapturedSkillFx.Sequence seq=new CapturedSkillFx.Sequence(h.c,j);assertEquals(7,seq.durations.length);assertEquals(294,seq.width);assertEquals(235,seq.height);
 }
}
