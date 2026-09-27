package com.projectdark.mobile;

import static org.junit.Assert.*;
import android.view.MotionEvent;
import java.lang.reflect.Field;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import com.projectdark.mobile.world.WorldCameraTransform;
import com.projectdark.mobile.world.WorldRuntimeAdapter;

@RunWith(RobolectricTestRunner.class)
@Config(sdk=34,manifest=Config.NONE)
public final class NpcTouchAcceptanceTest {
  private static Object field(Object target,String name)throws Exception{Field f=target.getClass().getDeclaredField(name);f.setAccessible(true);return f.get(target);}
  private static float floatField(Object target,String name)throws Exception{Field f=target.getClass().getDeclaredField(name);f.setAccessible(true);return f.getFloat(target);}

  @Test public void tappingMillesTravelGuidesVisibleNameplateOpensItsDialogue() throws Exception {
    GameView view=new GameView(RuntimeEnvironment.getApplication());view.layout(0,0,1536,704);
    RuntimeState state=(RuntimeState)field(view,"state");
    RuntimeState.Npc npc=state.npcs().stream().filter(n->"pote_travel_guide".equals(n.id)).findFirst().orElseThrow();
    WorldRuntimeAdapter world=(WorldRuntimeAdapter)field(view,"worldAdapter");
    WorldCameraTransform.Point label=world.worldToScreen(npc.x,npc.y-41f);
    float scale=floatField(view,"scale"),ox=floatField(view,"ox"),oy=floatField(view,"oy");
    float screenX=label.x*scale+ox,screenY=label.y*scale+oy;
    MotionEvent tap=MotionEvent.obtain(0,0,MotionEvent.ACTION_DOWN,screenX,screenY,0);
    try{assertTrue(view.onTouchEvent(tap));}finally{tap.recycle();}
    WorldMoveTargetController.Snapshot movement=world.movement().snapshot();
    assertEquals(WorldMoveTargetController.RequestKind.NPC_APPROACH,movement.kind);
    assertEquals("pote_travel_guide",movement.targetEntityId);
    java.lang.reflect.Method update=GameView.class.getDeclaredMethod("update",float.class);update.setAccessible(true);update.invoke(view,.05f);
    InteractionController interaction=(InteractionController)field(view,"interaction");
    assertTrue("NPC body/nameplate tap should reach and open dialogue",interaction.dialogOpen());
    assertEquals("pote_travel_guide",interaction.dialogNpc().id);
  }
}
