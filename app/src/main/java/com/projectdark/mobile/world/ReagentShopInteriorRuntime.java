package com.projectdark.mobile.world;

import java.util.List;

/** World-owned runtime for the reagent-shop interior; reuses the same 64x32 tile-step controller as Milles. */
public final class ReagentShopInteriorRuntime implements WorldMoveTargetController.NavigationWorld,WorldMoveTargetController.Walker {
  private final List<WorldMoveTargetController.TileCenter> tiles=ReagentShopInteriorDef.navigationTiles();
  private final WorldMoveTargetController movement=new WorldMoveTargetController(this,this);
  private float x=ReagentShopInteriorDef.SPAWN_X,y=ReagentShopInteriorDef.SPAWN_Y;
  public float x(){return x;} public float y(){return y;} public WorldMoveTargetController movement(){return movement;} public boolean atSpawn(){return Math.abs(x-ReagentShopInteriorDef.SPAWN_X)<.01f&&Math.abs(y-ReagentShopInteriorDef.SPAWN_Y)<.01f;}
  public void resetAtEntry(){movement.cancel();WorldMoveTargetController.TileCenter t=nearest(ReagentShopInteriorDef.SPAWN_X,ReagentShopInteriorDef.SPAWN_Y);x=t.x;y=t.y;}
  public WorldMoveTargetController.Snapshot requestGround(float x,float y){return movement.requestGroundMove(x,y);}
  public WorldMoveTargetController.Snapshot step(WorldMoveTargetController.Direction d){return movement.step(d);}
  public WorldMoveTargetController.Snapshot tick(float dt){return movement.tick(dt);}
  public boolean atExit(){return ReagentShopInteriorDef.exitContains(x,y);}
  public boolean merlinHit(float wx,float wy){float dx=wx-ReagentShopInteriorDef.MERLIN_X,dy=wy-ReagentShopInteriorDef.MERLIN_Y;return dx*dx+dy*dy<=40f*40f;}
  public List<WorldMoveTargetController.TileCenter> navigationTiles(){return tiles;}
  public boolean canPlayerOccupy(float wx,float wy){return ReagentShopInteriorDef.inside(wx,wy);}
  public float worldX(){return x;}public float worldY(){return y;}
  public boolean moveToAdjacentTile(float dx,float dy,WorldMoveTargetController.Direction d){
    if(WorldMoveTargetController.Direction.between(x,y,dx,dy)!=d||!canPlayerOccupy(dx,dy))return false;x=dx;y=dy;return true;
  }
  private WorldMoveTargetController.TileCenter nearest(float wx,float wy){WorldMoveTargetController.TileCenter best=null;float bd=Float.MAX_VALUE;for(WorldMoveTargetController.TileCenter t:tiles){float dx=t.x-wx,dy=t.y-wy,d=dx*dx+dy*dy;if(d<bd){bd=d;best=t;}}return best;}
}