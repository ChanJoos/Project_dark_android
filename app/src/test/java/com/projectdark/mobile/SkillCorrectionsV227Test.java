package com.projectdark.mobile;
import static org.junit.Assert.*;
import android.graphics.*;
import java.util.*;
import org.json.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.*;
@RunWith(RobolectricTestRunner.class) @Config(sdk=34,manifest=Config.NONE) @GraphicsMode(GraphicsMode.Mode.NATIVE)
public class SkillCorrectionsV227Test {
 final ScreenMagicV201Test h=new ScreenMagicV201Test();
 @Before public void setup(){h.setup();}
 @Test public void holyBlowCannotBeLearnedAssignedPreviewedOrExecuted()throws Exception{
  String id=SkillPolicyV227.HOLY_BLOW;SkillBook b=SkillBook.load(h.c);assertEquals(220,b.catalogSize());
  assertFalse(b.list(true,false).stream().anyMatch(e->id.equals(e.id)));assertFalse(b.learn(id,0));assertFalse(b.assign(0,id));
  b.setTestAccess(true);assertFalse(b.learned(id));assertFalse(b.previewable(id));assertFalse(b.usable(id));b.restoreTestSlots(new String[]{id,"SK_성직자_013"});assertNull(b.slot(0));assertEquals("SK_성직자_013",b.slot(1));
  RuntimeState s=h.fixture();long gold=s.rpg().gold();assertFalse(new SkillAcquisition(b).learn(id,s.rpg()));assertEquals(gold,s.rpg().gold());
  RuntimeCombatSession session=new RuntimeCombatSession(s,(a,t)->true,(a,key)->true,a->true);int mp=s.player().mp;
  assertFalse(session.submitPlayer("area0",id).accepted());assertEquals(mp,s.player().mp);assertEquals(1000000,s.monsters().get(0).hp);
  SkillVfxRenderer fx=new SkillVfxRenderer(h.c,new SkillPresentationCatalog(h.c));fx.preview(1,id,false,"area0",anchors(s));assertTrue(fx.pulses.isEmpty());
 }
 @Test public void oldSaveDropsOnlyRetiredSkillAndKeepsTransactionalValidationAcrossRestart()throws Exception{
  String keep="SK_성직자_013",gone=SkillPolicyV227.HOLY_BLOW;SkillBook b=SkillBook.load(h.c);assertTrue(b.learn(keep,42));assertTrue(b.assign(1,keep));
  JSONObject old=b.snapshot();old.getJSONObject("learned").put(gone,99);old.getJSONArray("slots").put(0,gone);
  h.c.getSharedPreferences("project_dark_f5m_v1",0).edit().putString("skill_book_v1",old.toString()).commit();
  F5mSaveStore.install(h.c);F5mSaveStore.bindRuntime(new RuntimeState(),F5mAdaptedPrologueQuest.openingFixture(),new GrowthQuest2());SkillBook restored=SkillBook.load(h.c);F5mSaveStore.restoreAndBindSkillsActive(restored);
  assertTrue(F5mSaveStore.writable());assertFalse(restored.owned(gone));assertNull(restored.slot(0));assertEquals(keep,restored.slot(1));assertEquals(42,restored.proficiency(keep));assertTrue(F5mSaveStore.checkpointActive());
  F5mSaveStore.install(h.c);SkillBook twice=SkillBook.load(h.c);F5mSaveStore.restoreAndBindSkillsActive(twice);assertNull(twice.slot(0));assertEquals(keep,twice.slot(1));assertFalse(twice.snapshot().getJSONObject("learned").has(gone));
  JSONObject bad=twice.snapshot();bad.getJSONArray("slots").put(2,"unknown_removed_skill");assertFalse(twice.restore(bad));assertEquals(keep,twice.slot(1));
 }
 SkillVfxRenderer.Anchors anchors(RuntimeState s){return new SkillVfxRenderer.Anchors(){public float x(String id){return id.equals("player")?s.player().x:s.monsters().stream().filter(m->m.id.equals(id)).findFirst().get().x;}public float y(String id){return id.equals("player")?s.player().y:s.monsters().stream().filter(m->m.id.equals(id)).findFirst().get().y;}};}
 @Test public void semeliaUsesSameOriginalAtlasOnOneRecipientWhileRagnarokHitsEveryVisibleMonster()throws Exception{
  ClassicSkillReference original=new ClassicSkillReference(h.c);assertSame(original.channel(SkillPolicyV227.RAGNAROK,false),original.channel(SkillPolicyV227.SEMELIA,false));assertNotNull(original.channel(SkillPolicyV227.SEMELIA,false));
  for(String id:new String[]{SkillPolicyV227.SEMELIA,SkillPolicyV227.RAGNAROK}){
   RuntimeState s=h.fixture();RuntimeCombatSession session=new RuntimeCombatSession(s,(a,t)->true,(a,key)->true,a->true);session.setSkillVisibility(key->!key.equals("area6"));
   assertTrue(id,session.submitPlayer(id.equals(SkillPolicyV227.SEMELIA)?"area2":"player",id).accepted());List<CombatResolver.Event> events=session.tick(session.effectiveCastSeconds(id)+.001f).events;
   Set<String> damaged=new HashSet<>();for(RuntimeState.Monster m:s.monsters())if(m.hp<m.maxHp)damaged.add(m.id);
   Set<String> expected=id.equals(SkillPolicyV227.SEMELIA)?new HashSet<>(Arrays.asList("area2")):new HashSet<>(Arrays.asList("area0","area1","area2","area3","area4","area5"));assertEquals(id,expected,damaged);assertEquals(0,s.player().mp);
   SkillVfxRenderer fx=new SkillVfxRenderer(h.c,new SkillPresentationCatalog(h.c));fx.consume(events,anchors(s));Set<String> shown=new HashSet<>();long count=0;for(SkillVfxRenderer.Pulse p:fx.pulses)if(p.id.equals(id)&&p.sheet.equals("classic")&&!p.caster){shown.add(p.anchor);count++;}assertEquals(id,expected,shown);assertEquals(expected.size(),count);
   Bitmap frame=Bitmap.createBitmap(960,540,Bitmap.Config.ARGB_8888);fx.draw(new Canvas(frame),anchors(s));assertTrue(fx.pulses.stream().allMatch(p->Float.isFinite(p.x)&&Float.isFinite(p.y)));
  }
 }
 @Test public void screenshotIconsRenderInBookAndQuickslots(){SkillIconCatalog icons=new SkillIconCatalog(h.c);for(String id:new String[]{"SK_성직자_051","SK_성직자_002"}){assertTrue(icons.has(id));Bitmap b=Bitmap.createBitmap(80,40,Bitmap.Config.ARGB_8888);Canvas c=new Canvas(b);assertTrue(icons.drawSquare(c,id,new RectF(0,0,40,40)));assertTrue(icons.drawQuickslot(c,id,new RectF(40,0,80,40)));assertTrue(Color.alpha(b.getPixel(20,20))>0);assertTrue(Color.alpha(b.getPixel(60,20))>0);}}
}
