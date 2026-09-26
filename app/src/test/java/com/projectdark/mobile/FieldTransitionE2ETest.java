package com.projectdark.mobile;

import static org.junit.Assert.*;
import com.projectdark.mobile.world.PoteFieldDef;
import com.projectdark.mobile.world.WorldMoveTargetController;
import com.projectdark.mobile.world.WorldRuntimeAdapter;
import org.junit.Test;

public final class FieldTransitionE2ETest {
  @Test public void millesFieldRoundTripKeepsOneRuntimeAndRestoresTownActors(){
    RuntimeState state=new RuntimeState();
    assertEquals(WorldDef.ID,state.currentMapId());
    state.enterPoteField();
    assertEquals(PotePrototypeWorldDef.MAP_ID,state.currentMapId());
    assertEquals(1,state.monsters().size());
    assertEquals("POTE_PURPLE",state.monsters().get(0).id);
    assertEquals(PoteFieldDef.ENTRY_X,state.player().x,.01f);
    assertEquals(PoteFieldDef.ENTRY_Y,state.player().y,.01f);
    // Navigation geometry is audited separately; keep this transition test free of android.graphics.Matrix JVM stubs.
    state.player().x=PoteFieldDef.EXIT_X;state.player().y=PoteFieldDef.EXIT_Y;
    assertTrue(PoteFieldDef.atExit(state.player().x,state.player().y));
    state.enterMillesFromField(790f,1510f);
    assertEquals(WorldDef.ID,state.currentMapId());
    assertTrue(state.npcs().size()>=1);
    assertEquals(3,state.monsters().size());
  }
}
