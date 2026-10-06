package com.projectdark.mobile;

import static org.junit.Assert.*;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.content.Context;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.GraphicsMode;

@RunWith(RobolectricTestRunner.class)
@Config(sdk=34,manifest=Config.NONE)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public final class MillesMousePresentationTest {
  @Test public void everyEarlyFieldEncounterUsesReadableAdaptedMouseAndKeepsItsQuestIdentity() throws Exception {
    WorldDef world=new WorldDef();
    assertEquals(3,world.monsterSpawns().size());
    for(WorldDef.MonsterSpawn spawn:world.monsterSpawns()){
      assertTrue(spawn.id,MillesMousePresentation.isEarlyFieldMouse(spawn.id));
      assertEquals(spawn.id,MillesMousePresentation.fieldName(spawn.id),spawn.name);
      assertTrue(spawn.name.startsWith("들쥐"));
    }
    CampaignProgress.Def opening=CampaignProgress.find("M04");
    assertNotNull(opening);
    for(String id:opening.targets.split(","))assertTrue("quest IDs stay stable",MillesMousePresentation.isEarlyFieldMouse(id));
    String objective=new CampaignProgress().objective(opening);
    assertTrue("quickquest calls the adapted field monsters mice",objective.contains("들쥐"));
    assertFalse(objective.contains("훈련 몬스터"));

    Context context=RuntimeEnvironment.getApplication();
    GameView view=new GameView(context);RuntimeState state=field(view,"state");
    for(RuntimeState.Monster mouse:state.monsters()){
      assertTrue(mouse.id,MillesMousePresentation.isEarlyFieldMouse(mouse.id));
      mouse.x=80;mouse.y=90;
      Bitmap frame=Bitmap.createBitmap(112,120,Bitmap.Config.ARGB_8888);
      invokeDrawMonster(view,new Canvas(frame),mouse);
      int left=frame.getWidth(),right=-1,top=frame.getHeight(),bottom=-1,visible=0;
      for(int y=0;y<frame.getHeight();y++)for(int x=0;x<frame.getWidth();x++){
        if(Color.alpha(frame.getPixel(x,y))==0)continue;
        visible++;left=Math.min(left,x);right=Math.max(right,x);top=Math.min(top,y);bottom=Math.max(bottom,y);
      }
      assertTrue("adapted mouse is rendered in the production field actor path",visible>300);
      assertTrue("mouse remains recognizable at the field camera scale",right-left>=30&&right-left<=36);
      assertTrue("mouse remains grounded in its sprite bounds",bottom<=mouse.y&&top<mouse.y-24);
      frame.recycle();
    }
  }

  @Test public void allFourFacingsUseNormalizedIdleWalkAndAttackFramesInTheProductionActorPath() throws Exception {
    GameView view=new GameView(RuntimeEnvironment.getApplication());
    RuntimeState state=field(view,"state");RuntimeState.Monster mouse=state.monsters().get(0);mouse.x=80;mouse.y=96;
    assertTrue("test actor uses the production mouse rendering branch",MillesMousePresentation.isMouse(mouse.id));
    String[] activities={"idle","walk_a","walk_b","attack"};
    Bitmap[][] rendered=new Bitmap[4][4];Bitmap review=Bitmap.createBitmap(4*96,4*96,Bitmap.Config.ARGB_8888);Canvas sheet=new Canvas(review);sheet.drawColor(0xff5f4429);
    int directionIndex=0;
    for(CharacterRenderer.Direction direction:CharacterRenderer.Direction.values()){
      mouse.visualFacing.setLocomotion(direction);int activityIndex=0;
      for(String activity:activities){
        mouse.isMoving=activity.startsWith("walk");mouse.animationClock="walk_b".equals(activity)?.2f:0f;
        mouse.attackPrimed="attack".equals(activity);mouse.attackVisualRemaining=0f;
        int expected=activity.equals("idle")?0:activity.equals("walk_a")?1:activity.equals("walk_b")?2:3;
        assertEquals(direction+"/"+activity,expected,InnMouseRenderer.activityFrame(mouse));
        Bitmap frame=Bitmap.createBitmap(160,128,Bitmap.Config.ARGB_8888);
        invokeDrawMonster(view,new Canvas(frame),mouse);
        Bitmap actor=Bitmap.createBitmap(frame,48,72,64,24);frame.recycle();
        int left=actor.getWidth(),right=-1,top=actor.getHeight(),bottom=-1,pixels=0;
        for(int y=0;y<actor.getHeight();y++)for(int x=0;x<actor.getWidth();x++)if(Color.alpha(actor.getPixel(x,y))>0){pixels++;left=Math.min(left,x);right=Math.max(right,x);top=Math.min(top,y);bottom=Math.max(bottom,y);}
        assertTrue(direction+"/"+activity+" has the live mouse sprite",pixels>300);
        assertTrue(direction+"/"+activity+" is smaller than the 48px player, width="+(right-left+1),right-left+1>=31&&right-left+1<=33);
        assertEquals(direction+"/"+activity+" preserves common ground anchor",23,bottom);
        if(activityIndex>0)assertFalse(direction+" uses different idle/walk/attack poses",rendered[directionIndex][activityIndex-1].sameAs(actor));
        rendered[directionIndex][activityIndex]=actor.copy(Bitmap.Config.ARGB_8888,false);
        sheet.drawBitmap(actor,directionIndex*96+16,activityIndex*96+16,null);actor.recycle();activityIndex++;
      }
      if(directionIndex>0)assertFalse("each compass direction has its own silhouette",rendered[directionIndex][0].sameAs(rendered[0][0]));directionIndex++;
    }
    for(Bitmap[] direction:rendered)for(Bitmap frame:direction)frame.recycle();
    java.io.File file=new java.io.File("build/reports/device-review/v109-small-mouse-animation-grid.png");file.getParentFile().mkdirs();
    try(java.io.FileOutputStream out=new java.io.FileOutputStream(file)){assertTrue(review.compress(Bitmap.CompressFormat.PNG,100,out));}
    review.recycle();
  }

  @Test public void innStoryMouseUsesTheSameWalkAndAttackAnimationFrames() throws Exception {
    GameView view=new GameView(RuntimeEnvironment.getApplication());
    RuntimeState state=field(view,"state");state.enterTownInterior(com.projectdark.mobile.world.TownInteriorDef.ALL.get(4));
    RuntimeState.Monster mouse=state.ensureAdaptedMillesMouse();assertNotNull(mouse);assertEquals("milles_mouse_proto",mouse.id);mouse.x=80;mouse.y=96;
    Bitmap[] poses=new Bitmap[3];String[] activities={"idle","walk_a","attack"};
    for(int i=0;i<activities.length;i++){
      mouse.isMoving="walk_a".equals(activities[i]);mouse.animationClock=.05f;mouse.attackPrimed="attack".equals(activities[i]);
      Bitmap frame=Bitmap.createBitmap(160,128,Bitmap.Config.ARGB_8888);invokeDrawMonster(view,new Canvas(frame),mouse);
      poses[i]=Bitmap.createBitmap(frame,48,72,64,24);frame.recycle();
      assertTrue("inn mouse " + activities[i] + " is visibly rendered",nonTransparent(poses[i])>300);
      assertEquals("inn mouse selects matching live state",i==0?0:i==1?1:3,InnMouseRenderer.activityFrame(mouse));
    }
    assertFalse("walking pose changes the inn idle pose",poses[0].sameAs(poses[1]));
    assertFalse("attack pose changes the inn idle pose",poses[0].sameAs(poses[2]));
    for(Bitmap pose:poses)pose.recycle();
  }

  private static int nonTransparent(Bitmap b){int n=0;for(int y=0;y<b.getHeight();y++)for(int x=0;x<b.getWidth();x++)if(Color.alpha(b.getPixel(x,y))>0)n++;return n;}

  private static void invokeDrawMonster(GameView view,Canvas canvas,RuntimeState.Monster monster)throws Exception{
    java.lang.reflect.Method method=GameView.class.getDeclaredMethod("drawMonster",Canvas.class,RuntimeState.Monster.class);
    method.setAccessible(true);method.invoke(view,canvas,monster);
  }
  @SuppressWarnings("unchecked") private static <T>T field(Object owner,String name)throws Exception{
    java.lang.reflect.Field f=owner.getClass().getDeclaredField(name);f.setAccessible(true);return (T)f.get(owner);
  }
}
