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
public class SkillFeedbackV231Test {
 final ScreenMagicV201Test h=new ScreenMagicV201Test();
 @Before public void setup(){h.setup();}
 GameView actor(String id,boolean automatic)throws Exception{
  GameView v=new GameView(h.c);v.layout(0,0,960,540);RuntimeState s=h.field(v,"state");s.rpg().restoreProgression(99,0);s.rpg().restoreJobCode("ROGUE");s.rpg().restoreStats(100,100,100,100,100,0);s.rpg().restoreBaseResources(100000,100000);s.applyDerivedGrowth();s.player().hp=100000;s.player().mp=50000;List<RuntimeState.Monster> ms=h.field(s,"monsters");ms.clear();WorldRuntimeAdapter w=h.field(v,"worldAdapter");List<WorldMoveTargetController.TileCenter> tiles=new ArrayList<>(w.navigationTiles());tiles.sort(Comparator.comparingDouble(t->Math.hypot(t.x-WorldDef.PLAYER_SPAWN_X,t.y-WorldDef.PLAYER_SPAWN_Y)));int distance=Math.max(1,SkillActionContract.get(id).minReach);boolean placed=false;
  for(WorldMoveTargetController.TileCenter t:tiles){s.player().x=t.x;s.player().y=t.y;if(w.canPlayerOccupy(t.x,t.y)&&w.canPlayerOccupy(t.x+32*distance,t.y+16*distance)&&w.hasLineOfSight(t.x,t.y,t.x+32*distance,t.y+16*distance)){placed=true;break;}}assertTrue(placed);
  RuntimeState.Monster m=new RuntimeState.Monster("LYK_ELECT_KOATL#v231","일렉코아틀",s.player().x+32*distance,s.player().y+16*distance,1000000,"B");m.level=98;ms.add(m);w.snapCameraToPlayer();SkillBook b=h.field(v,"skillBook");assertTrue(b.learn(id,100));assertTrue(b.assign(0,id));((CombatController)h.field(v,"combat")).selectTarget(m);java.lang.reflect.Field f=GameView.class.getDeclaredField("autoAttackEnabled");f.setAccessible(true);f.setBoolean(v,automatic);RectF slot=(RectF)h.call(v,"slotRect",new Class<?>[]{int.class},0);h.tap(v,slot.centerX(),slot.centerY());return v;
 }
 @Test public void senseUsesOriginalFiveRowsAndAutoExpiresAfterThreeTenthsWithoutStoppingAttack()throws Exception{
  GameView v=actor("SK_도적_002",true);RuntimeCombatSession session=h.field(v,"combatSession");h.call(v,"tickSkillCombat",new Class<?>[]{float.class},session.effectiveCastSeconds("SK_도적_002")+.001f);SenseMonsterWindow window=h.field(v,"senseMonsterWindow");assertTrue(window.visible());assertTrue((Boolean)h.field(v,"autoAttackEnabled"));String[] rows=h.field(window,"lines");assertEquals(5,rows.length);assertTrue(rows[0].contains("EXP: 248570"));assertEquals("HP: 1000000",rows[1]);assertEquals("Lev: 98",rows[2]);assertEquals("ATTACK NATURE: 땅",rows[3]);assertEquals("DEFENSE NATURE: 물",rows[4]);h.capture(v,"v231-sense-auto");h.call(v,"update",new Class<?>[]{float.class},.299f);assertTrue(window.visible());h.call(v,"update",new Class<?>[]{float.class},.002f);assertFalse(window.visible());assertTrue((Boolean)h.field(v,"autoAttackEnabled"));
 }
 @Test public void manualSenseWaitsForConfirmationAndDoesNotLeakAcrossMaps()throws Exception{
  GameView v=actor("SK_도적_002",false);RuntimeCombatSession session=h.field(v,"combatSession");h.call(v,"tickSkillCombat",new Class<?>[]{float.class},session.effectiveCastSeconds("SK_도적_002")+.001f);SenseMonsterWindow window=h.field(v,"senseMonsterWindow");assertTrue(window.visible());RuntimeState s=h.field(v,"state");window.tick(2,s);assertTrue(window.visible());h.capture(v,"v231-sense-manual");h.tap(v,480,348);assertFalse(window.visible());window.show(s,s.monsters().get(0),false);s.player().alive=false;window.tick(.01f,s);assertFalse(window.visible());
 }
 @Test public void shurikenNormalInputThrowsAndDamagesAtContactWithoutAnyParticle()throws Exception{
  GameView v=actor("SK_도적_003",false);RuntimeState s=h.field(v,"state");RuntimeState.Monster m=s.monsters().get(0);RuntimeCombatSession session=h.field(v,"combatSession");assertTrue(session.playerActionActive());assertEquals("ROGUE_THROW",new SkillPresentationCatalog(h.c).get("SK_도적_003").motion);h.call(v,"tickSkillCombat",new Class<?>[]{float.class},session.effectiveCastSeconds("SK_도적_003")-.01f);assertEquals(m.maxHp,m.hp);h.call(v,"tickSkillCombat",new Class<?>[]{float.class},.011f);assertTrue(m.hp<m.maxHp);SkillVfxRenderer fx=h.field(v,"skillVfx");assertTrue(fx.pulses.isEmpty());h.capture(v,"v231-shuriken-contact");
 }
 @Test public void protectionIncludesOriginalUpperGlowAndSphereExceedsBodyWithLargerAssassinRing()throws Exception{
  ClassicSkillReference ref=new ClassicSkillReference(h.c);CapturedSkillFx.Sequence shield=ref.channel("SK_무도가_029",false).sequence;int upper=0;for(int n=0;n<shield.durations.length;n++)for(int y=8;y<=35;y++)for(int x=0;x<shield.width;x++)if(Color.alpha(shield.atlas.getPixel(n%shield.columns*shield.width+x,n/shield.columns*shield.height+y))>24)upper++;assertTrue("restored upper arc above old35px cut",upper>20);
  CapturedSkillFx.Sequence sphere=ref.channel("SK_무도가_028",false).sequence;assertEquals(1.9,sphere.scale,.001);assertTrue("29px source sphere wider than 43px body",29*sphere.scale>43);CapturedSkillFx captures=new CapturedSkillFx(h.c);assertEquals(.416,captures.get("SK_도적_020").scale,.001);
  for(String id:new String[]{"SK_무도가_029","SK_무도가_028"}){Bitmap b=Bitmap.createBitmap(240,200,Bitmap.Config.ARGB_8888);Canvas c=new Canvas(b);c.drawColor(0xff283730);ref.draw(c,ref.channel(id,false),id.endsWith("029")?.31f:.2f,120,140);java.io.File f=new java.io.File("build/reports/device-review/v231-"+id+".png");f.getParentFile().mkdirs();try(java.io.FileOutputStream out=new java.io.FileOutputStream(f)){b.compress(Bitmap.CompressFormat.PNG,100,out);}}
 }
 @Test public void stabAliasesHwiBigiAndRequestedIconsUseDoubleStabBrownFamily()throws Exception{
  SkillIconCatalog icons=new SkillIconCatalog(h.c);Bitmap a=Bitmap.createBitmap(40,40,Bitmap.Config.ARGB_8888),b=a.copy(Bitmap.Config.ARGB_8888,true);icons.drawSquare(new Canvas(a),"SK_도적_001",new RectF(0,0,40,40));icons.drawSquare(new Canvas(b),"SK_도적_007",new RectF(0,0,40,40));assertTrue(a.sameAs(b));
  for(String n:new String[]{"015","020","021","025","027"}){a.eraseColor(Color.TRANSPARENT);icons.drawSquare(new Canvas(a),"SK_도적_"+n,new RectF(0,0,40,40));int warm=0,cold=0;for(int y=4;y<36;y++)for(int x=4;x<36;x++){int c=a.getPixel(x,y);if(Color.red(c)>Color.blue(c)+15)warm++;if(Color.blue(c)>Color.red(c)+15)cold++;}assertTrue(n+" warm source background",warm>400);assertEquals(0,cold);}
 }
}
