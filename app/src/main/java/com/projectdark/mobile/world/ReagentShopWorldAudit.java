package com.projectdark.mobile.world;

import com.projectdark.mobile.RuntimeState;

/** CI-safe contract audit for the Milles reagent-shop map. */
public final class ReagentShopWorldAudit {
  private ReagentShopWorldAudit(){}
  public static boolean verify(){
    if(ReagentShopMapDef.TILE_W!=64f||ReagentShopMapDef.TILE_H!=32f)return false;
    if(ReagentShopMapDef.tiles().isEmpty())return false;
    if(!ReagentShopMapDef.canOccupy(ReagentShopMapDef.ENTRY_X,ReagentShopMapDef.ENTRY_Y,RuntimeState.PLAYER_RADIUS))return false;
    if(ReagentShopMapDef.canOccupy(480f,240f,RuntimeState.PLAYER_RADIUS))return false;
    WorldPortalTransitionController.Portal exit=ReagentShopMapDef.exitPortal();
    if(exit.readiness!=WorldPortalTransitionController.PortalReadiness.READY)return false;
    if(!ReagentShopMapDef.MILLES_MAP_ID.equals(exit.targetMapId))return false;
    RuntimeState state=new RuntimeState();
    ReagentShopWorldRuntime world=new ReagentShopWorldRuntime(state);
    if(world.navigationTiles().size()!=ReagentShopMapDef.tiles().size())return false;
    float x=state.player().x,y=state.player().y;
    WorldMoveTargetController.Snapshot s=world.step(WorldMoveTargetController.Direction.NW);
    if(s==null)return false;
    if(state.player().x==x&&state.player().y==y)return false;
    return true;
  }
}
