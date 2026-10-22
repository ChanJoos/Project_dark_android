package com.projectdark.mobile;
import static org.junit.Assert.*;
import android.graphics.*;
import com.projectdark.mobile.world.*;
import java.io.*;
import java.util.*;
import org.json.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.*;
@RunWith(RobolectricTestRunner.class) @Config(sdk=34,manifest=Config.NONE) @GraphicsMode(GraphicsMode.Mode.NATIVE)
public class RogueReviewV229Test {
 final ScreenMagicV201Test h=new ScreenMagicV201Test();
 @Before public void setup(){h.setup();}
 @Test public void all25ArticleIconsWinOnBookAndQuickslotsWithoutChangingOtherJobs()throws Exception{
  ClassicSkillReference refs=new ClassicSkillReference(h.c,"rogue");SkillIconCatalog icons=new SkillIconCatalog(h.c);assertEquals(25,refs.references.size());
  for(JSONObject row:refs.references.values()){
   String id=row.getString("id");Bitmap source=BitmapFactory.decodeStream(h.c.getAssets().open("skill-presentation/rogue/"+row.getString("iconAssetPath")));
   Bitmap actual=Bitmap.createBitmap(40,40,Bitmap.Config.ARGB_8888),expected=actual.copy(Bitmap.Config.ARGB_8888,true);Paint p=new Paint();p.setFilterBitmap(true);new Canvas(expected).drawBitmap(source,null,new RectF(0,0,40,40),p);assertTrue(id,icons.drawSquare(new Canvas(actual),id,new RectF(0,0,40,40)));assertTrue(id+" must use exact article icon",expected.sameAs(actual));
   actual.eraseColor(Color.TRANSPARENT);expected.eraseColor(Color.TRANSPARENT);Canvas ec=new Canvas(expected);Path clip=new Path();clip.addRoundRect(new RectF(0,0,40,40),3,3,Path.Direction.CW);ec.clipPath(clip);int inset=Math.max(1,Math.round(Math.min(source.getWidth(),source.getHeight())*.12f));p.setFilterBitmap(false);ec.drawBitmap(source,new Rect(inset,inset,source.getWidth()-inset,source.getHeight()-inset),new RectF(0,0,40,40),p);assertTrue(icons.drawQuickslot(new Canvas(actual),id,new RectF(0,0,40,40)));assertTrue(id+" quickslot must use article glyph",expected.sameAs(actual));
  }
  assertEquals(22,refs.references.get("SK_도적_015").getJSONObject("sourceOrdinal").getInt("imageIndex"));assertEquals(30,refs.references.get("SK_도적_023").getJSONObject("sourceOrdinal").getInt("imageIndex"));assertEquals(40,refs.references.get("SK_도적_030").getJSONObject("sourceOrdinal").getInt("imageIndex"));assertEquals(42,refs.references.get("SK_도적_029").getJSONObject("sourceOrdinal").getInt("imageIndex"));
 }
 @Test public void seupgyeokAndGiseupRetainDistinctIconsDescriptionsLevelsSlotsAndDurableIds()throws Exception{
  SkillBook b=SkillBook.load(h.c);assertEquals("습격",b.get("SK_도적_015").name);assertEquals("기습",b.get("SK_도적_023").name);assertNotEquals(b.get("SK_도적_015").circle,b.get("SK_도적_023").circle);assertFalse(b.summary("SK_도적_023").contains("습격을 강화"));assertTrue(b.summary("SK_도적_023").contains("방어력"));assertTrue(b.learn("SK_도적_015",17));assertTrue(b.learn("SK_도적_023",63));assertTrue(b.assign(0,"SK_도적_015"));assertTrue(b.assign(1,"SK_도적_023"));
  F5mSaveStore.bindRuntime(h.fixture(),F5mAdaptedPrologueQuest.openingFixture(),new GrowthQuest2());F5mSaveStore.restoreAndBindSkillsActive(b);assertTrue(F5mSaveStore.checkpointActive());F5mSaveStore.install(h.c);SkillBook restored=SkillBook.load(h.c);F5mSaveStore.restoreAndBindSkillsActive(restored);assertEquals("SK_도적_015",restored.slot(0));assertEquals("SK_도적_023",restored.slot(1));assertEquals(17,restored.proficiency("SK_도적_015"));assertEquals(63,restored.proficiency("SK_도적_023"));
 }
 @Test public void throwsNeverUseKickGroupAndStabsHaveEquippedLungeInBothBodiesAllFacings()throws Exception{
  SkillPresentationCatalog cat=new SkillPresentationCatalog(h.c);SkillBodyRenderer renderer=new SkillBodyRenderer(h.c,cat);renderer.prepare("mu0000001,mh172,ml228,ms001","mw002");Bitmap montage=Bitmap.createBitmap(960,640,Bitmap.Config.ARGB_8888);Canvas out=new Canvas(montage);out.drawColor(0xff283528);int col=0;
  for(String motion:new String[]{"ROGUE_THROW","ROGUE_STAB"})for(String body:new String[]{"mm001","wm001"})for(CharacterRenderer.Direction d:CharacterRenderer.Direction.values()){
   String key=cat.frameKey(body,motion,d,1f/3f);assertTrue(key,key.contains(motion.equals("ROGUE_THROW")?"/e/":"/c/"));assertNotEquals(cat.frameKey(body,motion,d,0),key);
   CharacterRenderer.Pose pose=new CharacterRenderer.Pose(100,150,d,CharacterRenderer.State.SKILL,0,.2f,.5f,false,"mu0000001,mh172,ml228,ms001","mw002",null,CharacterRenderer.EffectFamily.SKILL);Bitmap image=Bitmap.createBitmap(200,200,Bitmap.Config.ARGB_8888);assertTrue(renderer.draw(new Canvas(image),pose,body,motion,1f/3f));boolean drawn=false;for(int y=0;y<200;y++)for(int x=0;x<200;x++)if(Color.alpha(image.getPixel(x,y))>0)drawn=true;assertTrue(key,drawn);out.drawBitmap(image,null,new Rect(col%8*120,col/8*320,col%8*120+120,col/8*320+160),null);col++;
  }
  File p=new File("build/reports/device-review/v229-source-motion.png");p.getParentFile().mkdirs();try(FileOutputStream f=new FileOutputStream(p)){montage.compress(Bitmap.CompressFormat.PNG,100,f);}
 }
 @Test public void reviewedActiveDaggerAndThrowMotionsReachNormalQuickslotContactAndNativeDraw()throws Exception{
  for(String n:new String[]{"003","005","006","007","014","015","020","021","022","023","027","028","029","030"}){
   h.setup();String id="SK_도적_"+n;GameView v=new GameView(h.c);v.layout(0,0,960,540);RuntimeState s=h.field(v,"state");s.rpg().restoreProgression(99,0);assertTrue(s.rpg().restoreJobCode("ROGUE"));s.rpg().restoreStats(100,100,100,100,100,0);s.rpg().restoreBaseResources(100000,100000);s.applyDerivedGrowth();s.player().hp=100000;s.player().mp=50000;List<RuntimeState.Monster> ms=h.field(s,"monsters");ms.clear();WorldRuntimeAdapter w=h.field(v,"worldAdapter");List<WorldMoveTargetController.TileCenter> tiles=new ArrayList<>(w.navigationTiles());tiles.sort(Comparator.comparingDouble(t->Math.hypot(t.x-WorldDef.PLAYER_SPAWN_X,t.y-WorldDef.PLAYER_SPAWN_Y)));int distance=Math.max(1,SkillActionContract.get(id).minReach);boolean placed=false;
   for(WorldMoveTargetController.TileCenter t:tiles){s.player().x=t.x;s.player().y=t.y;if(w.canPlayerOccupy(t.x,t.y)&&w.canPlayerOccupy(t.x+32*distance,t.y+16*distance)&&w.hasLineOfSight(t.x,t.y,t.x+32*distance,t.y+16*distance)){placed=true;break;}}assertTrue(id+" fixture",placed);RuntimeState.Monster m=new RuntimeState.Monster("v229-target","V229 source review",s.player().x+32*distance,s.player().y+16*distance,1000000,"B");ms.add(m);w.snapCameraToPlayer();s.skillEffects().put("player","SK_도적_011","STEALTH",1,30);SkillBook b=h.field(v,"skillBook");assertTrue(b.learn(id,50));assertTrue(b.assign(0,id));((CombatController)h.field(v,"combat")).selectTarget(m);RectF slot=(RectF)h.call(v,"slotRect",new Class<?>[]{int.class},0);h.tap(v,slot.centerX(),slot.centerY());RuntimeCombatSession session=h.field(v,"combatSession");assertTrue(id+" normal input accepted",session.playerActionActive());assertEquals(id,h.field(v,"activeSkillVisualId"));h.call(v,"tickSkillCombat",new Class<?>[]{float.class},session.effectiveCastSeconds(id)+.001f);h.capture(v,"v229-"+n);
  }
 }
}
