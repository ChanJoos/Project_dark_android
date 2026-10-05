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
      assertTrue("mouse remains recognizable at the field camera scale",right-left>=40);
      assertTrue("mouse remains grounded in its sprite bounds",bottom<=mouse.y&&top<mouse.y-24);
      frame.recycle();
    }
  }

  @Test public void allFourMouseFacingsLoadDistinctFramesFromTheProductionRenderer() throws Exception {
    GameView view=new GameView(RuntimeEnvironment.getApplication());
    RuntimeState state=field(view,"state");RuntimeState.Monster mouse=state.monsters().get(0);
    InnMouseRenderer renderer=new InnMouseRenderer(RuntimeEnvironment.getApplication());
    Bitmap previous=null;int i=0;
    Bitmap review=Bitmap.createBitmap(4*96,112,Bitmap.Config.ARGB_8888);Canvas sheet=new Canvas(review);sheet.drawColor(0xff5f4429);
    for(CharacterRenderer.Direction direction:CharacterRenderer.Direction.values()){
      mouse.visualFacing.setLocomotion(direction);
      Bitmap frame=Bitmap.createBitmap(96,112,Bitmap.Config.ARGB_8888);
      renderer.draw(new Canvas(frame),mouse,48,88);
      int left=frame.getWidth(),right=-1,top=frame.getHeight(),bottom=-1,pixels=0;
      for(int y=0;y<frame.getHeight();y++)for(int x=0;x<frame.getWidth();x++)if(Color.alpha(frame.getPixel(x,y))>0){pixels++;left=Math.min(left,x);right=Math.max(right,x);top=Math.min(top,y);bottom=Math.max(bottom,y);}
      assertTrue("each direction has visible mouse pixels: "+direction,pixels>300);
      assertTrue("each mouse has ears, body and tail at world scale: "+direction,right-left>=32&&bottom-top>=24);
      if(previous!=null)assertFalse("facing selects a distinct source still: "+direction,previous.sameAs(frame));
      sheet.drawBitmap(frame,i*96,0,null);if(previous!=null)previous.recycle();previous=frame.copy(frame.getConfig(),false);frame.recycle();i++;
    }
    if(previous!=null)previous.recycle();
    java.io.File file=new java.io.File("build/reports/device-review/v108-adapted-mouse-directions.png");file.getParentFile().mkdirs();
    try(java.io.FileOutputStream out=new java.io.FileOutputStream(file)){assertTrue(review.compress(Bitmap.CompressFormat.PNG,100,out));}
    review.recycle();
  }

  private static void invokeDrawMonster(GameView view,Canvas canvas,RuntimeState.Monster monster)throws Exception{
    java.lang.reflect.Method method=GameView.class.getDeclaredMethod("drawMonster",Canvas.class,RuntimeState.Monster.class);
    method.setAccessible(true);method.invoke(view,canvas,monster);
  }
  @SuppressWarnings("unchecked") private static <T>T field(Object owner,String name)throws Exception{
    java.lang.reflect.Field f=owner.getClass().getDeclaredField(name);f.setAccessible(true);return (T)f.get(owner);
  }
}
