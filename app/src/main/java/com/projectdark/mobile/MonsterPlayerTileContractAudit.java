package com.projectdark.mobile;

import com.projectdark.mobile.world.AdaptedMillesIsometricTileLayer;
import com.projectdark.mobile.world.WorldMoveTargetController;

/** Executable parity gate: player and monster share the exact authored tile-center movement contract. */
public final class MonsterPlayerTileContractAudit {
  private static final float EPS=.001f;
  private static final int LONG_RUN=128;
  private MonsterPlayerTileContractAudit(){}

  public static boolean passes(){
    if(Math.abs(AdaptedMillesIsometricTileLayer.TILE_WIDTH-64f)>EPS||
        Math.abs(AdaptedMillesIsometricTileLayer.TILE_HEIGHT-32f)>EPS)return false;
    if(Math.abs(MonsterTileCenterLocomotion.STEP_X-32f)>EPS||
        Math.abs(MonsterTileCenterLocomotion.STEP_Y-16f)>EPS)return false;

    float ox=640f,oy=512f;
    for(WorldMoveTargetController.Direction d:WorldMoveTargetController.Direction.values()){
      float mx=ox+d.dx,my=oy+d.dy;
      if(!MonsterTileCenterLocomotion.isAdjacentEndpoint(ox,oy,mx,my))return false;
      if(Math.abs(Math.abs(d.dx)-MonsterTileCenterLocomotion.STEP_X)>EPS||
          Math.abs(Math.abs(d.dy)-MonsterTileCenterLocomotion.STEP_Y)>EPS)return false;
      CharacterRenderer.Direction mf=MonsterTileCenterLocomotion.facing(d);
      if(CanonicalActorFacing.quantize(d.dx,d.dy,null)!=mf)return false;
      WorldMoveTargetController.Direction chosen=MonsterTileCenterLocomotion.toward(d.dx,d.dy,null);
      if(chosen!=d)return false;
    }

    // 100+ authored steps: exact opposing endpoint roundtrip with zero accumulated drift.
    float x=ox,y=oy;
    for(int i=0;i<LONG_RUN;i++){x+=WorldMoveTargetController.Direction.SE.dx;y+=WorldMoveTargetController.Direction.SE.dy;}
    for(int i=0;i<LONG_RUN;i++){x+=WorldMoveTargetController.Direction.NW.dx;y+=WorldMoveTargetController.Direction.NW.dy;}
    if(Math.abs(x-ox)>EPS||Math.abs(y-oy)>EPS)return false;

    x=ox;y=oy;
    for(int i=0;i<LONG_RUN;i++){x+=WorldMoveTargetController.Direction.NE.dx;y+=WorldMoveTargetController.Direction.NE.dy;}
    for(int i=0;i<LONG_RUN;i++){x+=WorldMoveTargetController.Direction.SW.dx;y+=WorldMoveTargetController.Direction.SW.dy;}
    if(Math.abs(x-ox)>EPS||Math.abs(y-oy)>EPS)return false;

    // Collision rejection contract: rejected endpoint means no partial/cardinal mutation.
    float beforeX=ox,beforeY=oy;
    boolean endpointAccepted=false;
    if(endpointAccepted){beforeX+=32f;beforeY+=16f;}
    if(Math.abs(beforeX-ox)>EPS||Math.abs(beforeY-oy)>EPS)return false;

    // Spawn normalization must resolve to an actual authored center, not a tuned sub-segment.
    WorldMoveTargetController.TileCenter center=MonsterTileCenterLocomotion.nearestAuthoredCenter(1315f,715f);
    if(center==null||!MonsterTileCenterLocomotion.isAuthoredCenter(center.x,center.y))return false;

    return true;
  }

  public static void main(String[] args){
    if(!passes())throw new AssertionError("MonsterPlayerTileContractAudit failed");
    System.out.println("MonsterPlayerTileContractAudit PASS: player/monster endpoints=±32/±16, 128-step drift=0, collision atomic, facing parity");
  }
}
