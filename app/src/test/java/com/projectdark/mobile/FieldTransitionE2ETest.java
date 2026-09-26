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
    WorldRuntimeAdapter field=new WorldRuntimeAdapter(state,960f,540f,PoteFieldDef.MIN_X,PoteFieldDef.MAX_X,PoteFieldDef.MIN_Y,PoteFieldDef.MAX_Y,PoteFieldDef.navigationTiles(),PoteFieldDef.obstacles(),true);
    assertEquals(PoteFieldDef.ENTRY_X,state.player().x,.01f);
    assertEquals(PoteFieldDef.ENTRY_Y,state.player().y,.01f);
    WorldMoveTargetController.Snapshot move=field.requestGroundWorld(PoteFieldDef.EXIT_X,PoteFieldDef.EXIT_Y);
    for(int i=0;i<64&&move.status==WorldMoveTargetController.Status.MOVING;i++)move=field.tickNavigation(WorldMoveTargetController.TILE_STEP_SECONDS).movement;
    assertTrue(PoteFieldDef.atExit(state.player().x,state.player().y));
    state.enterMillesFromField(790f,1510f);
    assertEquals(WorldDef.ID,state.currentMapId());
    assertTrue(state.npcs().size()>=1);
    assertEquals(3,state.monsters().size());
  }
}
