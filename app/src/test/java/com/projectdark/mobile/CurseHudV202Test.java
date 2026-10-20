package com.projectdark.mobile;
import static org.junit.Assert.*;
import android.content.*;import android.graphics.*;import android.view.MotionEvent;
import com.projectdark.mobile.world.*;
import java.util.*;import java.io.*;import java.lang.reflect.*;
import org.junit.*;import org.junit.runner.RunWith;import org.robolectric.*;import org.robolectric.annotation.*;
@RunWith(RobolectricTestRunner.class) @Config(sdk=34,manifest=Config.NONE) @GraphicsMode(GraphicsMode.Mode.NATIVE)
public class CurseHudV202Test {
 Context c;
 @Before public void setup(){c=RuntimeEnvironment.getApplication();for(String n:new String[]{"project_dark_f5m_v1","project_dark_skill_test_v1","project_dark_skill_book_v1"})c.getSharedPreferences(n,0).edit().clear().commit();F5mSaveStore.install(c);UiTheme.install(c);}
 @SuppressWarnings("unchecked") <T>T field(Object o,String n)throws Exception{Field f=o.getClass().getDeclaredField(n);f.setAccessible(true);return(T)f.get(o);}
 Object call(GameView v,String n,Class<?>[] types,Object...args)throws Exception{Method m=GameView.class.getDeclaredMethod(n,types);m.setAccessible(true);return m.invoke(v,args);}
 void save(Bitmap b,String name)throws Exception{File f=new File("build/reports/horror-v179/v202/"+name+".png");f.getParentFile().mkdirs();try(FileOutputStream o=new FileOutputStream(f)){assertTrue(b.compress(Bitmap.CompressFormat.PNG,100,o));}}
 void capture(GameView v,String name)throws Exception{Bitmap b=Bitmap.createBitmap(v.getWidth(),v.getHeight(),Bitmap.Config.ARGB_8888);v.draw(new Canvas(b));save(b,name);b.recycle();}
 RuntimeState fixture(int count)throws Exception{RuntimeState s=new RuntimeState();s.rpg().restoreStats(50,50,50,50,50,0);s.rpg().restoreProgression(99,0);s.rpg().restoreBaseResources(100000,100000);s.applyDerivedGrowth();s.player().mp=50000;s.player().x=s.player().y=0;List<RuntimeState.Monster> ms=field(s,"monsters");ms.clear();for(int i=0;i<count;i++)ms.add(new RuntimeState.Monster("m"+i,"fixture",i*20,100,100000,"B"));return s;}
 SkillVfxRenderer.Anchors anchors(RuntimeState s){return new SkillVfxRenderer.Anchors(){public float x(String id){if(id.equals("player"))return s.player().x;for(RuntimeState.Monster m:s.monsters())if(m.id.equals(id))return m.x;return Float.NaN;}public float y(String id){if(id.equals("player"))return s.player().y;for(RuntimeState.Monster m:s.monsters())if(m.id.equals(id))return m.y;return Float.NaN;}};}
 @Test public void fourWholeCursesKeepAll108RecipientsAndTheirOwnColors()throws Exception{
  String[] ids={"SK_마법사_047","SK_마법사_048","SK_마법사_049","SK_마법사_050"};
  for(int k=0;k<ids.length;k++){
   RuntimeState s=fixture(110);s.monsters().get(109).alive=false;RuntimeCombatSession session=new RuntimeCombatSession(s,(a,t)->false,(a,id)->true,a->true);session.setSkillVisibility(id->!id.equals("m108"));SkillVfxRenderer fx=new SkillVfxRenderer(c,new SkillPresentationCatalog(c));SkillVfxRenderer.Anchors a=anchors(s);
   assertTrue(session.submitPlayer("player",ids[k]).accepted());List<CombatResolver.Event> events=session.tick(session.effectiveCastSeconds(ids[k])+.001f).events;fx.consume(events,a);fx.consume(events,a);
   Set<String> actual=new HashSet<>();for(SkillVfxRenderer.Pulse p:fx.pulses)if(p.sheet.equals("curse-skull")){assertEquals(ids[k],p.id);assertEquals(k,CurseSkullFx.variant(p.id));assertFalse(p.caster);assertTrue(actual.add(p.anchor));}
   assertEquals(108,actual.size());for(int i=0;i<108;i++){assertTrue(actual.contains("m"+i));assertTrue(s.skillEffects().has("m"+i,"CURSE"));assertEquals(100000,s.monsters().get(i).hp);}assertFalse(actual.contains("m108"));assertFalse(actual.contains("m109"));
   fx.tick(CurseSkullFx.DURATION+.01f);assertTrue(fx.pulses.isEmpty());
  }
 }
 @Test public void singleCursesMatchWholeVariantsAndDoNotChangeDispelOrNonCurse(){
  String[] ids={"SK_마법사_002","SK_마법사_011","SK_마법사_024","SK_마법사_053"};for(int i=0;i<ids.length;i++)assertEquals(i,CurseSkullFx.variant(ids[i]));assertEquals(1,CurseSkullFx.variant("SK_성직자_012"));for(String id:new String[]{"SK_성직자_003","SK_성직자_041","SK_마법사_006","SK_마법사_046"})assertEquals(-1,CurseSkullFx.variant(id));
 }
 @Test public void prewarmedSkullsHaveDistinctColorsAnimatedFramesAndBoundedSharedMemory()throws Exception{
  CurseSkullFx fx=new CurseSkullFx();assertTrue(CurseSkullFx.allocationBytes()<=4_000_000);long bytes=CurseSkullFx.allocationBytes();new CurseSkullFx();assertEquals(bytes,CurseSkullFx.allocationBytes());
  Bitmap contact=Bitmap.createBitmap(640,440,Bitmap.Config.ARGB_8888);Canvas canvas=new Canvas(contact);canvas.drawColor(0xff292526);String[] ids={"SK_마법사_053","SK_마법사_002","SK_마법사_024","SK_마법사_011"};
  Set<Integer> hashes=new HashSet<>();for(int k=0;k<4;k++){for(int frame=0;frame<3;frame++){float age=frame*.27f;fx.draw(canvas,ids[k],age,80+k*160,80+frame*130);}Bitmap tile=Bitmap.createBitmap(128,160,Bitmap.Config.ARGB_8888);fx.draw(new Canvas(tile),ids[k],.25f,64,80);int[] px=new int[128*160];tile.getPixels(px,0,128,0,0,128,160);int visible=0;for(int v:px)if(Color.alpha(v)>20)visible++;assertTrue(visible>1800);hashes.add(Arrays.hashCode(px));tile.recycle();}assertEquals(4,hashes.size());save(contact,"four-colors-three-phases");
 }
 @Test public void liveMillesLabelFollowsActualWalkingMidpointAndMapSwitch()throws Exception{
  GameView v=new GameView(c);v.layout(0,0,960,540);WorldRuntimeAdapter w=field(v,"worldAdapter");RuntimeState s=field(v,"state");String before=(String)call(v,"currentMapLocation",new Class<?>[]{});assertEquals("밀레스 마을 ("+Math.round(w.presentationPlayerX())+","+Math.round(w.presentationPlayerY())+")",before);capture(v,"milles-before");
  boolean moved=false;for(WorldMoveTargetController.Direction d:WorldMoveTargetController.Direction.values()){float x=s.player().x,y=s.player().y;w.step(d);if(x!=s.player().x||y!=s.player().y){moved=true;break;}}assertTrue(moved);w.tickNavigation(.3f);String middle=(String)call(v,"currentMapLocation",new Class<?>[]{});assertNotEquals(before,middle);assertTrue(middle.endsWith("("+Math.round(w.presentationPlayerX())+","+Math.round(w.presentationPlayerY())+")"));capture(v,"milles-moving");w.tickNavigation(.4f);assertNotEquals(middle,call(v,"currentMapLocation",new Class<?>[]{}));
  Field inside=GameView.class.getDeclaredField("inPoteField");inside.setAccessible(true);inside.setBoolean(v,true);call(v,"changeCampaignMap",new Class<?>[]{String.class,boolean.class},HorrorCastleGeometry.roomId(1),false);WorldRuntimeAdapter h=field(v,"poteFieldAdapter");assertEquals(CampaignWorld.title(s.currentMapId())+" ("+Math.round(h.presentationPlayerX())+","+Math.round(h.presentationPlayerY())+")",call(v,"currentMapLocation",new Class<?>[]{}));v.layout(0,0,1280,540);capture(v,"horror-wide-coordinates");
 }
 GameView blockedHorror(String id)throws Exception{setup();GameView v=new GameView(c);v.layout(0,0,960,540);Field inside=GameView.class.getDeclaredField("inPoteField");inside.setAccessible(true);inside.setBoolean(v,true);call(v,"changeCampaignMap",new Class<?>[]{String.class,boolean.class},HorrorCastleGeometry.roomId(1),false);RuntimeState s=field(v,"state");s.rpg().restoreProgression(99,0);assertTrue(s.rpg().restoreJobCode(id.startsWith("SK_성직자")?"CLERIC":"MAGE"));s.rpg().restoreStats(50,50,50,50,50,0);s.rpg().restoreBaseResources(100000,100000);s.applyDerivedGrowth();s.player().hp=100000;s.player().mp=50000;SkillBook book=field(v,"skillBook");assertFalse(book.testAccess());assertTrue(id,book.learn(id,50));for(int i=0;i<SkillBook.SLOT_COUNT;i++)book.clearSlot(i);assertTrue(book.assign(0,id));return v;}
 RuntimeState.Monster blockedTarget(GameView v,boolean onlyOne)throws Exception{RuntimeState s=field(v,"state");WorldRuntimeAdapter w=field(v,"poteFieldAdapter");RuntimeState.Monster target=s.monsters().get(0);if(onlyOne)for(RuntimeState.Monster m:s.monsters())if(m!=target)m.alive=false;
  for(RuntimeState.Monster m:s.monsters())if(m.alive)for(WorldMoveTargetController.TileCenter t:w.navigationTiles()){double d=Math.hypot(t.x-m.x,t.y-m.y);if(d<80||d>320||!w.canPlayerOccupy(t.x,t.y)||w.hasLineOfSight(t.x,t.y,m.x,m.y))continue;s.player().x=t.x;s.player().y=t.y;w.snapCameraToPlayer();if((Boolean)call(v,"skillTargetVisible",new Class<?>[]{String.class},m.id)&&!w.hasCombatLineOfSight("player",m.id))return m;}throw new AssertionError("source furniture must supply a visible blocked ray");
 }
 void tap(GameView v,float x,float y){MotionEvent e=MotionEvent.obtain(0,0,MotionEvent.ACTION_DOWN,x,y,0);v.onTouchEvent(e);e.recycle();}

 @Test public void allFourNormalLearnedHorrorQuickslotsRenderEveryLiveRecipientSkull()throws Exception{
  for(String id:new String[]{"SK_마법사_047","SK_마법사_048","SK_마법사_049","SK_마법사_050"}){
   GameView v=blockedHorror(id);blockedTarget(v,false);RuntimeState s=field(v,"state");RuntimeCombatSession session=field(v,"combatSession");Set<String> wanted=new HashSet<>();for(RuntimeState.Monster m:s.monsters())if(m.alive&&(Boolean)call(v,"skillTargetVisible",new Class<?>[]{String.class},m.id))wanted.add(m.id);
   RectF slot=(RectF)call(v,"slotRect",new Class<?>[]{int.class},0);tap(v,slot.centerX(),slot.centerY());assertTrue(session.playerActionActive());call(v,"tickSkillCombat",new Class<?>[]{float.class},session.effectiveCastSeconds(id)+.001f);
   SkillVfxRenderer fx=field(v,"skillVfx");Set<String> actual=new HashSet<>();for(SkillVfxRenderer.Pulse p:fx.pulses)if(p.sheet.equals("curse-skull")){assertEquals(id,p.id);assertTrue(actual.add(p.anchor));}
   assertEquals(wanted,actual);assertTrue(actual.size()>1);capture(v,id+"-all-recipients");
  }
 }

 @Test public void coordinateCacheRefreshesNegativeAndRoundedValuesWithoutStaleMap(){HudMapLocation l=new HudMapLocation();String a=l.label("A",-12.2f,23.1f);assertEquals("A (-12,23)",a);assertSame(a,l.label("A",-12.1f,23.2f));assertEquals("A (-11,24)",l.label("A",-11.4f,23.6f));assertEquals("B (1,2)",l.label("B",1,2));}
}
