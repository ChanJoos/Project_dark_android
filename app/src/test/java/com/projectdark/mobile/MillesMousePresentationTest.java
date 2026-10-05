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
  @Test public void everyEarlyFieldEncounterUsesCapturedMouseAndKeepsItsQuestIdentity() throws Exception {
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
      assertTrue("captured rat is rendered in the production field actor path",visible>30);
      assertTrue("rat silhouette is readable at the field camera scale",right-left>=40);
      assertTrue("rat remains grounded in its sprite bounds",bottom<=mouse.y&&top<mouse.y-8);
      frame.recycle();
    }
  }

  private static void invokeDrawMonster(GameView view,Canvas canvas,RuntimeState.Monster monster)throws Exception{
    java.lang.reflect.Method method=GameView.class.getDeclaredMethod("drawMonster",Canvas.class,RuntimeState.Monster.class);
    method.setAccessible(true);method.invoke(view,canvas,monster);
  }
  @SuppressWarnings("unchecked") private static <T>T field(Object owner,String name)throws Exception{
    java.lang.reflect.Field f=owner.getClass().getDeclaredField(name);f.setAccessible(true);return (T)f.get(owner);
  }
}
