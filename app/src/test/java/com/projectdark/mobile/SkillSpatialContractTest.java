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

@RunWith(RobolectricTestRunner.class) @Config(sdk=34,manifest=Config.NONE) @GraphicsMode(GraphicsMode.Mode.NATIVE)
public class SkillSpatialContractTest {
  Context c;
  @Before public void setup(){c=RuntimeEnvironment.getApplication();c.getSharedPreferences("project_dark_f5m_v1",0).edit().clear().commit();F5mSaveStore.install(c);}
  @Test public void originalTileOraclesCoverAllFourDirectionsAndEveryReviewedId(){
    assertEquals(219,SkillActionContract.all().size());
    for(float[] direction:new float[][]{{32,16},{32,-16},{-32,16},{-32,-16}}){
      SkillActionContract.Rule line=SkillActionContract.get("SK_전사_003");
      for(int n=1;n<=3;n++)assertTrue(SkillActionContract.canStart(line,0,0,direction[0]*n,direction[1]*n,true));
      assertFalse(SkillActionContract.canStart(line,0,0,direction[0]*4,direction[1]*4,true));
      assertFalse(SkillActionContract.canStart(line,0,0,64,0,true));
      assertFalse(SkillActionContract.includes(line,0,0,direction[0],direction[1],-direction[0],-direction[1],true));
      assertTrue(SkillActionContract.includes(SkillActionContract.get("SK_무도가_009"),0,0,direction[0],direction[1],direction[0]*3,direction[1]*3,true));
    }
    assertFalse(SkillActionContract.includes(SkillActionContract.get("SK_전사_006"),0,0,0,0,64,0,true));
    assertTrue(SkillActionContract.includes(SkillActionContract.get("SK_무도가_025"),0,0,0,0,64,0,true));
    SkillActionContract.Rule ranged=SkillActionContract.get("SK_도적_003");
    assertFalse(SkillActionContract.canStart(ranged,0,0,32,16,true));assertTrue(SkillActionContract.canStart(ranged,0,0,64,32,true));assertTrue(SkillActionContract.canStart(ranged,0,0,128,64,true));assertFalse(SkillActionContract.canStart(ranged,0,0,160,80,true));
    assertFalse(SkillActionContract.canStart(SkillActionContract.get("SK_도적_005"),0,0,32,16,true));
    assertEquals(SkillActionContract.Pattern.SCREEN,SkillActionContract.get("SK_마법사_050").pattern);
    assertEquals(3f,SkillActionContract.get("SK_무도가_020").contact,.001f);
    for(SkillActionContract.Rule r:SkillActionContract.all()){
      assertNotNull(r.id,r.evidence);assertNotNull(r.id,r.note);
      if(!r.presentationAllowed())assertFalse(r.id,SkillActionContract.canStart(r,0,0,0,0,true));
      if(r.needsSelection())assertFalse(r.id,SkillActionContract.canStart(r,0,0,32,16,false));
    }
  }
  @Test public void actualResolverHitsOnlyExpectedRecipientsForEveryCombatClass(){
    String[] ids={"SK_전사_006","SK_도적_030","SK_무도가_025","SK_마법사_018","SK_마법사_043","SK_성직자_040"};
    int[][] expected={{0,1,2,3},{0,1},{0,1,2,3,4},{0,4,5},{0,1,2,3,4,5},{0,1,2,3,4,5}};
    for(int k=0;k<ids.length;k++){
      RuntimeState s=scene();RuntimeCombatSession session=new RuntimeCombatSession(s,(a,t)->true,(a,id)->true,a->true);
      session.setSkillVisibility(id->!id.equals("m6"));SkillActionContract.Rule rule=SkillActionContract.get(ids[k]);
      assertTrue(ids[k],session.submitPlayer(rule.selfAnchored()?"player":"m0",ids[k]).accepted());
      List<CombatResolver.Event> events=session.tick(.3f).events;Set<String> affected=new HashSet<>();
      for(CombatResolver.Event e:events)if(e.type==CombatResolver.EventType.HIT_FEEDBACK){assertTrue(e.amount>0);affected.add(e.targetId);}
      Set<String> want=new HashSet<>();for(int n:expected[k])want.add("m"+n);assertEquals(ids[k],want,affected);
      for(RuntimeState.Monster m:s.monsters())assertEquals(ids[k]+" "+m.id,want.contains(m.id),m.hp<100);
      SkillVfxRenderer fx=new SkillVfxRenderer(c,new SkillPresentationCatalog(c));fx.consume(events,anchors(s));fx.consume(events,anchors(s));assertEquals(ids[k],want.size(),fx.pulses.stream().filter(p->!p.caster).count());
    }
  }
  @Test public void targetMovesWallsAndViewportAreRecheckedAtContact(){
    RuntimeState s=scene();RuntimeCombatSession session=new RuntimeCombatSession(s,(a,t)->!t.equals("m2"),(a,id)->true,a->true);session.setSkillVisibility(id->!id.equals("m3"));
    assertTrue(session.submitPlayer("player","SK_전사_006").accepted());s.monsters().get(0).x=160;s.monsters().get(0).y=80;
    List<CombatResolver.Event> e=session.tick(.2f).events;Set<String> ids=new HashSet<>();for(CombatResolver.Event v:e)if(v.type==CombatResolver.EventType.HIT_FEEDBACK)ids.add(v.targetId);assertEquals(Collections.singleton("m1"),ids);
    RuntimeState other=scene();RuntimeCombatSession single=new RuntimeCombatSession(other,(a,t)->true,(a,id)->true,a->true);assertTrue(single.submitPlayer("m0","SK_무도가_002").accepted());other.monsters().get(0).x=64;other.monsters().get(0).y=32;assertTrue(single.tick(.2f).events.stream().anyMatch(v->v.type==CombatResolver.EventType.ACTION_CANCELLED));assertEquals(100,other.monsters().get(0).hp);
    assertFalse(single.submitPlayer("m0","SK_무도가_002").accepted());
  }
  @Test public void passivesUtilitiesUnknownEffectsDoNotPlayMonsterAttackAndLinkedSkillsAffectBasic()throws Exception{
    GameView v=new GameView(c);v.setSkillTestMode(true);SkillBook b=field(v,"skillBook");
    for(String id:new String[]{"SK_전사_008","SK_전사_002","SK_도적_025","SK_성직자_060","SK_마법사_054","SK_무도가_026"}){use(v,b.get(id));assertNull(id,field(v,"activeSkillVisualId"));assertFalse(((RuntimeCombatSession)field(v,"combatSession")).playerActionActive());assertTrue(((SkillVfxRenderer)field(v,"skillVfx")).pulses.isEmpty());}
    assertEquals(5,b.basicHits());v.setSkillTestMode(false);assertEquals(1,b.basicHits());
    b.bindJob(()->"WARRIOR");b.learn("SK_전사_002",0);assertEquals(2,b.basicHits());b.learn("SK_전사_005",0);assertEquals(3,b.basicHits());b.learn("SK_전사_020",0);assertEquals(4,b.basicHits());
  }
  @Test public void liveGameInputRendersDistinctFinisherAndSpellEffects()throws Exception{
    for(String id:new String[]{"SK_전사_015","SK_전사_023","SK_도적_020","SK_무도가_020","SK_마법사_043","SK_성직자_040"}){
      F5mSaveStore.install(c);GameView v=new GameView(c);v.layout(0,0,960,540);v.setSkillTestMode(true);SkillBook b=field(v,"skillBook");RuntimeState s=field(v,"state");RuntimeState.Monster m=s.monsters().get(0);s.player().x=m.x-32;s.player().y=m.y-16;s.player().mp=0;((CombatController)field(v,"combat")).selectTarget(m);((com.projectdark.mobile.world.WorldRuntimeAdapter)field(v,"worldAdapter")).snapCameraToPlayer();
      use(v,b.get(id));assertEquals(id,field(v,"activeSkillVisualId"));SkillActionContract.Rule r=SkillActionContract.get(id);tick(v,r.contact+.01f);tick(v,.25f);assertTrue(id,((SkillVfxRenderer)field(v,"skillVfx")).pulses.stream().anyMatch(p->!p.caster));assertEquals(0,s.player().mp);time(v,r.contact+.26f);render(v,"original-skill-"+id+".png");
    }
  }
  @SuppressWarnings("unchecked") private RuntimeState scene(){try{RuntimeState s=new RuntimeState();s.player().x=s.player().y=0;Field f=RuntimeState.class.getDeclaredField("monsters");f.setAccessible(true);List<RuntimeState.Monster> list=(List<RuntimeState.Monster>)f.get(s);list.clear();float[][] points={{32,16},{-32,-16},{32,-16},{-32,16},{64,0},{64,32},{2000,1000}};for(int i=0;i<points.length;i++)list.add(new RuntimeState.Monster("m"+i,"fixture",points[i][0],points[i][1],100,"B"));return s;}catch(Exception e){throw new AssertionError(e);}}
  private SkillVfxRenderer.Anchors anchors(RuntimeState s){return new SkillVfxRenderer.Anchors(){public float x(String id){for(RuntimeState.Monster m:s.monsters())if(m.id.equals(id))return m.x;return s.player().x;}public float y(String id){for(RuntimeState.Monster m:s.monsters())if(m.id.equals(id))return m.y;return s.player().y;}};}
  private static void use(GameView v,SkillBook.Entry e)throws Exception{Method m=GameView.class.getDeclaredMethod("useBookSkill",SkillBook.Entry.class);m.setAccessible(true);m.invoke(v,e);}
  private static void tick(GameView v,float dt)throws Exception{Method m=GameView.class.getDeclaredMethod("tickSkillCombat",float.class);m.setAccessible(true);m.invoke(v,dt);}
  private static void time(GameView v,float t)throws Exception{Field f=GameView.class.getDeclaredField("actionClock");f.setAccessible(true);f.setFloat(v,t);}
  @SuppressWarnings("unchecked") private static <T>T field(Object o,String n)throws Exception{Field f=o.getClass().getDeclaredField(n);f.setAccessible(true);return(T)f.get(o);}
  private static void render(GameView v,String name)throws Exception{Bitmap b=Bitmap.createBitmap(960,540,Bitmap.Config.ARGB_8888);v.draw(new Canvas(b));File d=new File("build/reports/device-review");d.mkdirs();try(FileOutputStream out=new FileOutputStream(new File(d,name))){b.compress(Bitmap.CompressFormat.PNG,100,out);}}
}
