package com.projectdark.mobile;

import static org.junit.Assert.*;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.MotionEvent;
import com.projectdark.mobile.world.TownInteriorDef;
import com.projectdark.mobile.world.WorldMoveTargetController;
import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.GraphicsMode;

/** Story fixtures stay explicitly adapted; mouse behavior is compared against the shared AI path. */
@RunWith(RobolectricTestRunner.class)
@Config(sdk=34,manifest=Config.NONE)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public final class MillesStoryQuestTest {
  @Test public void innIsAReachableDistinctInteriorAndMouseIsExplicitlyAdapted(){
    TownInteriorDef inn=TownInteriorDef.forMap("milles_interior_inn");
    assertNotNull("the exterior inn portal now resolves to a real room",inn);
    assertEquals(TownInteriorDef.Kind.INN,inn.kind);
    assertEquals("Benjamin · 여관 주인",inn.npcName);
    assertTrue(inn.props.stream().anyMatch(p->p.asset.equals("inn_world_table")));
    assertTrue(inn.props.stream().anyMatch(p->p.asset.equals("inn_world_hearth")));
    assertTrue(inn.props.stream().anyMatch(p->p.asset.equals("joined_counter")));
    assertEquals(F5mAdaptedPrologueQuest.OPENING_MONSTER_ID,"milles_mouse_proto");
    MonsterDefinition mouse=new MonsterDefinitionRegistry().resolve("milles_mouse_proto");
    assertEquals(MonsterDefinition.Status.PROTOTYPE_PENDING,mouse.status);
    assertEquals(MonsterDefinition.Evidence.B,mouse.evidence);
    assertFalse(mouse.hasCanonicalReward());
  }

  @Test public void mouseAppearsOnlyAfterTheAdaptedQuestIsAccepted(){
    RuntimeState state=new RuntimeState();
    assertNull(find(state,"milles_mouse_proto"));
    F5mAdaptedPrologueQuest quest=new F5mAdaptedPrologueQuest(F5mAdaptedPrologueQuest.OPENING_MONSTER_ID);
    assertEquals(F5mAdaptedPrologueQuest.AcceptResult.ACTIVATED,quest.accept());
    assertNull("mouse cannot be spawned in the outdoor map",state.ensureAdaptedMillesMouse());
    state.enterTownInterior(TownInteriorDef.forMap("milles_interior_inn"));
    RuntimeState.Monster mouse=state.ensureAdaptedMillesMouse();
    assertNotNull(mouse);
    assertSame(mouse,state.ensureAdaptedMillesMouse());
    assertEquals("milles_mouse_proto",mouse.id);
  }

  @Test public void mouseAndControlMonsterUseIdenticalSharedChaseAndAttackFlow(){
    RuntimeState mouseState=new RuntimeState(),controlState=new RuntimeState();
    mouseState.enterTownInterior(TownInteriorDef.forMap("milles_interior_inn"));
    controlState.enterTownInterior(TownInteriorDef.forMap("milles_interior_inn"));
    addControl(controlState,new RuntimeState.Monster("combat_dummy_01","control",TownInteriorDef.x(10,11),TownInteriorDef.y(10,11),24,"test"));
    mouseState.ensureAdaptedMillesMouse();
    RuntimeState.Monster mouse=find(mouseState,"milles_mouse_proto");
    RuntimeState.Monster control=find(controlState,"combat_dummy_01");
    assertNotNull(mouse);assertNotNull(control);
    silence(mouseState,mouse);silence(controlState,control);
    List<WorldMoveTargetController.TileCenter> tiles=mouseState.monsterNavigationTiles();
    WorldMoveTargetController.TileCenter start=null,target=null;
    outer: for(WorldMoveTargetController.TileCenter from:tiles){
      for(WorldMoveTargetController.TileCenter to:tiles){
        float d=(float)Math.hypot(to.x-from.x,to.y-from.y);
        if(d<48||d>=180f)continue;
        if(mouseState.nextMonsterChaseStep(new RuntimeState.Monster("probe","probe",from.x,from.y,1,"test"),to.x,to.y)!=null){start=from;target=to;break outer;}
      }
    }
    assertNotNull("Milles navigation exposes a reachable chase pair",start);assertNotNull(target);
    mouse.x=control.x=start.x;mouse.y=control.y=start.y;
    mouseState.player().x=controlState.player().x=target.x;mouseState.player().y=controlState.player().y=target.y;
    MonsterAIController mouseAi=new MonsterAIController(),controlAi=new MonsterAIController();
    mouseAi.tick(mouseState,MonsterAIController.MONSTER_STEP_SECONDS_B);
    controlAi.tick(controlState,MonsterAIController.MONSTER_STEP_SECONDS_B);
    assertTrue("mouse enters the same tile-walk state",mouse.isMoving);
    assertEquals(control.isMoving,mouse.isMoving);
    assertEquals(control.moveTargetX,mouse.moveTargetX,.001f);
    assertEquals(control.moveTargetY,mouse.moveTargetY,.001f);
    assertEquals(control.state,mouse.state);

    WorldMoveTargetController.TileCenter adjacent=null,playerTile=null;
    for(WorldMoveTargetController.TileCenter a:tiles)for(WorldMoveTargetController.TileCenter b:tiles){
      if(CanonicalMeleeTileContract.reachable(a.x,a.y,b.x,b.y)){adjacent=a;playerTile=b;break;}
    }
    assertNotNull(adjacent);assertNotNull(playerTile);
    mouse.isMoving=control.isMoving=false;mouse.attackCooldown=control.attackCooldown=0;
    mouse.x=control.x=adjacent.x;mouse.y=control.y=adjacent.y;
    mouseState.player().x=controlState.player().x=playerTile.x;mouseState.player().y=controlState.player().y=playerTile.y;
    RuntimeCombatSession mouseCombat=new RuntimeCombatSession(mouseState,(a,t)->true,RuntimeCombatSession.startingCommonerLearnedActions(),a->true);
    RuntimeCombatSession controlCombat=new RuntimeCombatSession(controlState,(a,t)->true,RuntimeCombatSession.startingCommonerLearnedActions(),a->true);
    mouseAi=new MonsterAIController(new MonsterAIController.SharedResolverAttackRouter(mouseCombat.monsterAutoBridge()));
    controlAi=new MonsterAIController(new MonsterAIController.SharedResolverAttackRouter(controlCombat.monsterAutoBridge()));
    int mouseHp=mouseState.player().hp,controlHp=controlState.player().hp;
    mouseAi.tick(mouseState,0);controlAi.tick(controlState,0);
    assertTrue(mouse.attackPrimed);assertTrue(control.attackPrimed);
    mouseState.tick(.25f);controlState.tick(.25f);mouseAi.tick(mouseState,0);controlAi.tick(controlState,0);
    assertEquals(MonsterAIController.AttackRoute.SHARED_RESOLVER,mouseAi.lastAttackSubmission().route);
    assertEquals(MonsterAIController.SubmissionOutcome.ACCEPTED,mouseAi.lastAttackSubmission().outcome);
    assertEquals(controlAi.lastAttackSubmission().outcome,mouseAi.lastAttackSubmission().outcome);
    mouseCombat.tick(.24f);controlCombat.tick(.24f);
    assertEquals("mouse uses the same resolver damage as the control monster",controlHp-controlState.player().hp,mouseHp-mouseState.player().hp);
  }

  @Test public void innkeeperUsesRegisteredWearablesWithoutAnOpaqueApron() throws Exception {
    Context context=RuntimeEnvironment.getApplication();TownNpcRenderer renderer=new TownNpcRenderer(context);
    Bitmap inn=Bitmap.createBitmap(128,128,Bitmap.Config.ARGB_8888);
    renderer.draw(new Canvas(inn),TownInteriorDef.forMap("milles_interior_inn"),64,112);
    int occupied=0;for(int y=60;y<112;y++)for(int x=42;x<85;x++)if(android.graphics.Color.alpha(inn.getPixel(x,y))>0)occupied++;
    assertTrue("Mary loads the detailed BODY/wearable sprite",occupied>100);assertTrue("no full opaque rectangular apron covering the character",occupied<900);
    inn.recycle();
  }
  @SuppressWarnings("unchecked") private static void addControl(RuntimeState state,RuntimeState.Monster m) throws RuntimeException {
    try{java.lang.reflect.Field f=RuntimeState.class.getDeclaredField("monsters");f.setAccessible(true);((java.util.List<RuntimeState.Monster>)f.get(state)).add(m);}catch(Exception e){throw new RuntimeException(e);}
  }

  @Test public void questJournalButtonOpensAnActionableReviewCapture() throws Exception {
    GameView view=new GameView(RuntimeEnvironment.getApplication());view.layout(0,0,960,540);
    tap(view,808,28);assertTrue(field(view,"questJournalOpen"));
    Bitmap frame=Bitmap.createBitmap(960,540,Bitmap.Config.ARGB_8888);view.draw(new Canvas(frame));
    File file=new File("build/reports/device-review/v90-milles-story-quest-journal.png");file.getParentFile().mkdirs();
    try(FileOutputStream out=new FileOutputStream(file)){assertTrue(frame.compress(Bitmap.CompressFormat.PNG,100,out));}
    assertTrue(file.isFile()&&file.length()>0);frame.recycle();tap(view,818,74);assertFalse(field(view,"questJournalOpen"));
  }

  private static Bitmap render(WorldEntityPresentationRenderer r,String visual){
    return render(r,visual,CharacterRenderer.Direction.SE);
  }
  private static Bitmap render(WorldEntityPresentationRenderer r,String visual,CharacterRenderer.Direction direction){
    Bitmap b=Bitmap.createBitmap(48,48,Bitmap.Config.ARGB_8888);Canvas c=new Canvas(b);
    r.draw(c,new WorldEntityPresentationRenderer.Pose(WorldEntityPresentationRenderer.Kind.MONSTER,24,42,
        direction,CharacterRenderer.State.IDLE,0,0,1,CharacterRenderer.EffectFamily.NONE,false,false,visual,null));return b;
  }
  private static int differentPixels(Bitmap a,Bitmap b){int count=0;for(int y=0;y<a.getHeight();y++)for(int x=0;x<a.getWidth();x++)if(a.getPixel(x,y)!=b.getPixel(x,y))count++;return count;}
  private static boolean field(GameView view,String name)throws Exception{java.lang.reflect.Field f=GameView.class.getDeclaredField(name);f.setAccessible(true);return f.getBoolean(view);}
  private static void tap(GameView view,float x,float y){MotionEvent e=MotionEvent.obtain(0,1,MotionEvent.ACTION_DOWN,x,y,0);view.onTouchEvent(e);e.recycle();}
  private static RuntimeState.Monster find(RuntimeState state,String id){for(RuntimeState.Monster m:state.monsters())if(id.equals(m.id))return m;return null;}
  private static void silence(RuntimeState state,RuntimeState.Monster keep){for(RuntimeState.Monster m:state.monsters())m.alive=m==keep;}
}
