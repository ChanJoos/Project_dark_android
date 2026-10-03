package com.projectdark.mobile;

import static org.junit.Assert.*;
import android.content.Context;
import android.graphics.*;
import com.projectdark.mobile.world.*;
import java.io.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.*;

@RunWith(RobolectricTestRunner.class) @Config(sdk=34,manifest=Config.NONE)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public class InnSourceRepairTest {
  private Context c;
  @Before public void setup(){c=RuntimeEnvironment.getApplication();c.getSharedPreferences("project_dark_f5m_v1",0).edit().clear().commit();F5mSaveStore.install(c);}
  private GameView start() throws Exception {
    GameView v=new GameView(c);v.layout(0,0,960,540);
    F5mAdaptedPrologueQuest q=TownInteriorTest.field(v,"f5mQuest");assertEquals(F5mAdaptedPrologueQuest.AcceptResult.ACTIVATED,q.accept());
    RuntimeState s=TownInteriorTest.field(v,"state");assertNull(s.ensureAdaptedMillesMouse());
    TownInteriorTest.enter(v,TownInteriorDef.forMap("milles_interior_inn"));return v;
  }
  private RuntimeState.Monster mouse(RuntimeState s){for(RuntimeState.Monster m:s.monsters())if(F5mAdaptedPrologueQuest.OPENING_MONSTER_ID.equals(m.id))return m;return null;}
  private void capture(GameView v,String name) throws Exception {
    Bitmap b=Bitmap.createBitmap(960,540,Bitmap.Config.ARGB_8888);v.draw(new Canvas(b));
    File f=new File("build/reports/device-review/v91-inn-"+name+".png");f.getParentFile().mkdirs();try(FileOutputStream out=new FileOutputStream(f)){assertTrue(b.compress(Bitmap.CompressFormat.PNG,100,out));}b.recycle();
  }
  @Test public void actualInnMouseCanBeSelectedChasedAttackedTurnedInAndRestarted() throws Exception {
    GameView v=start();RuntimeState s=TownInteriorTest.field(v,"state");RuntimeState.Monster rat=mouse(s);
    assertNotNull(rat);assertEquals(1,s.monsters().size());assertTrue(s.isMonsterTileCenter(rat.x,rat.y));
    WorldRuntimeAdapter w=TownInteriorTest.field(v,"reagentShopAdapter");assertFalse(w.canPlayerOccupy(rat.x,rat.y));
    capture(v,"entry");
    float x=rat.x,y=rat.y;TownInteriorTest.tick(v,30);assertTrue("shared AI actually pursues inside the inn",rat.isMoving||Math.hypot(rat.x-x,rat.y-y)>1);
    capture(v,"chase");WorldCameraTransform.Point p=w.worldToScreen(rat.x,rat.y);TownInteriorTest.tap(v,p.x,p.y-3);
    CombatController combat=TownInteriorTest.field(v,"combat");assertSame("actual screen tap selects the indoor mouse",rat,combat.target());
    TownInteriorTest.tap(v,826,502); // existing AUTO control
    F5mAdaptedPrologueQuest q=TownInteriorTest.field(v,"f5mQuest");
    for(int i=0;i<800&&q.state()==F5mAdaptedPrologueQuest.State.ACTIVE;i++)TownInteriorTest.tick(v,1);
    assertEquals("real shared resolver defeats the actual indoor target",F5mAdaptedPrologueQuest.State.RETURN_READY,q.state());assertFalse(rat.alive);assertEquals(1,q.currentCount());
    capture(v,"defeated");
    TownInteriorDef d=TownInteriorDef.forMap(s.currentMapId());p=w.worldToScreen(d.npcX(),d.npcY());TownInteriorTest.tap(v,p.x,p.y-24);TownInteriorTest.tick(v,600);
    assertTrue("Mary can be reached around real furniture",(Boolean)TownInteriorTest.field(v,"innDialogueOpen"));capture(v,"mary-report");
    long gold=s.rpg().gold();TownInteriorTest.tap(v,570,426);assertEquals(F5mAdaptedPrologueQuest.State.COMPLETED,q.state());assertEquals(gold+100,s.rpg().gold().longValue());
    assertTrue(F5mSaveStore.checkpointActive());F5mSaveStore.install(c);GameView restored=new GameView(c);restored.layout(0,0,960,540);RuntimeState r=TownInteriorTest.field(restored,"state");
    assertEquals(d.mapId,r.currentMapId());assertEquals(F5mAdaptedPrologueQuest.State.COMPLETED,((F5mAdaptedPrologueQuest)TownInteriorTest.field(restored,"f5mQuest")).state());assertNull("claimed mouse cannot return on restart",mouse(r));assertEquals(s.rpg().gold(),r.rpg().gold());
    capture(restored,"completed-restart");TownInteriorTest.call(restored,"leaveReagentShop");assertEquals(WorldDef.ID,r.currentMapId());assertNull(mouse(r));assertEquals("exterior training actors are restored",3,r.monsters().size());
  }
  @Test public void activeMouseHpAndIndoorMapSurviveCheckpoint() throws Exception {
    GameView v=start();RuntimeState s=TownInteriorTest.field(v,"state");RuntimeState.Monster rat=mouse(s);s.damage(rat,3);
    assertTrue(F5mSaveStore.checkpointActive());F5mSaveStore.install(c);GameView restored=new GameView(c);RuntimeState r=TownInteriorTest.field(restored,"state");
    assertEquals(s.currentMapId(),r.currentMapId());assertEquals(rat.hp,mouse(r).hp);assertEquals(rat.x,mouse(r).x,.001f);assertEquals(rat.y,mouse(r).y,.001f);
  }
  @Test public void sourceRatAndMaryPixelsAreReadableAtTheirActualWorldScale() throws Exception {
    GameView v=start();RuntimeState s=TownInteriorTest.field(v,"state");Bitmap sheet=Bitmap.createBitmap(360,150,Bitmap.Config.ARGB_8888);Canvas canvas=new Canvas(sheet);canvas.drawColor(0xff5f4429);
    InnMouseRenderer renderer=new InnMouseRenderer(c);int i=0;
    for(CharacterRenderer.Direction d:CharacterRenderer.Direction.values()){RuntimeState.Monster rat=mouse(s);rat.visualFacing.setLocomotion(d);renderer.draw(canvas,rat,45+i*85,120);i++;}
    TownNpcRenderer npc=new TownNpcRenderer(c);npc.draw(canvas,TownInteriorDef.forMap(s.currentMapId()),180,75);
    File f=new File("build/reports/device-review/v91-inn-actors.png");f.getParentFile().mkdirs();try(FileOutputStream out=new FileOutputStream(f)){sheet.compress(Bitmap.CompressFormat.PNG,100,out);}sheet.recycle();
    // Contract is three observed stills, not falsely claimed 12 original poses.
    for(String pose:new String[]{"mouse_nw","mouse_ne","mouse_se"})try(InputStream in=c.getAssets().open("interiors/v91/"+pose+".png")){
      Bitmap b=BitmapFactory.decodeStream(in);int occupied=0;for(int y=0;y<b.getHeight();y++)for(int x=0;x<b.getWidth();x++)if(Color.alpha(b.getPixel(x,y))>0)occupied++;
      assertTrue("source crop contains fur",occupied>15);assertTrue("brown recording floor was removed",occupied<b.getWidth()*b.getHeight()/2);b.recycle();
    }
  }
  @Test public void earlyPurpleRoofAndMeasuredDoorAreTheLiveExterior() throws Exception {
    MillesDoorAnchors.Door door=MillesDoorAnchors.forId("inn_door");assertEquals(2016,door.x,.01);assertEquals(736,door.y,.01);
    try(InputStream in=c.getAssets().open("buildings/BLD_006_inn_early.png")){Bitmap b=BitmapFactory.decodeStream(in);assertEquals(416,b.getWidth());assertEquals(346,b.getHeight());b.recycle();}
    GameView v=new GameView(c);v.layout(0,0,960,540);RuntimeState s=TownInteriorTest.field(v,"state");WorldRuntimeAdapter w=TownInteriorTest.field(v,"worldAdapter");
    assertTrue("measured entrance is a legal exterior cell",w.canPlayerOccupy(2016,736));s.player().x=2016;s.player().y=736;w.snapCameraToPlayer();capture(v,"exterior");
    TownInteriorTest.tick(v,1);assertEquals("touch/movement doorway enters the actual inn map","milles_interior_inn",s.currentMapId());
  }
}
