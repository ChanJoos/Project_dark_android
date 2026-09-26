package com.projectdark.mobile;

import com.projectdark.mobile.world.PoteFieldDef;
import com.projectdark.mobile.world.WorldMoveTargetController;
import com.projectdark.mobile.world.WorldRuntimeAdapter;

/** End-to-end audit for Milles -> first hunting field -> combat spawn -> Milles return. */
public final class FieldTransitionE2EAudit {
  private FieldTransitionE2EAudit(){}
  public static boolean verify(){
    RuntimeState state=new RuntimeState();
    if(!WorldDef.ID.equals(state.currentMapId()))return false;
    state.enterPoteField();
    if(!PotePrototypeWorldDef.MAP_ID.equals(state.currentMapId()))return false;
    if(state.monsters().size()!=1||!"POTE_PURPLE".equals(state.monsters().get(0).id))return false;
    WorldRuntimeAdapter field=new WorldRuntimeAdapter(state,960f,540f,PoteFieldDef.MIN_X,PoteFieldDef.MAX_X,PoteFieldDef.MIN_Y,PoteFieldDef.MAX_Y,PoteFieldDef.navigationTiles(),PoteFieldDef.obstacles(),true);
    if(Math.abs(state.player().x-PoteFieldDef.ENTRY_X)>.01f||Math.abs(state.player().y-PoteFieldDef.ENTRY_Y)>.01f)return false;
    WorldMoveTargetController.Snapshot move=field.requestGroundWorld(PoteFieldDef.EXIT_X,PoteFieldDef.EXIT_Y);
    for(int i=0;i<64&&move.status==WorldMoveTargetController.Status.MOVING;i++)move=field.tickNavigation(WorldMoveTargetController.TILE_STEP_SECONDS).movement;
    if(!PoteFieldDef.atExit(state.player().x,state.player().y))return false;
    state.enterMillesFromField(790f,1510f);
    return WorldDef.ID.equals(state.currentMapId())&&state.npcs().size()>=1&&state.monsters().size()==3;
  }
  public static void main(String[] args){if(!verify())throw new IllegalStateException("Field transition E2E failed");System.out.println("FIELD_TRANSITION_E2E_PASS");}
}
