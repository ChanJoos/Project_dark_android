package com.projectdark.mobile.world;

import java.util.ArrayList;
import java.util.List;

/** Regression audit: entity navigation must optimize route cost, not nearest endpoint distance. */
public final class QuickQuestApproachPathAudit {
  private static final class Grid implements WorldMoveTargetController.NavigationWorld, WorldMoveTargetController.Walker {
    final List<WorldMoveTargetController.TileCenter> tiles=new ArrayList<>();
    float x=0f,y=0f;
    Grid(){for(int row=-3;row<=3;row++)for(int col=-3;col<=3;col++)if(((row+col)&1)==0)tiles.add(new WorldMoveTargetController.TileCenter(col*32f,row*16f));}
    public List<WorldMoveTargetController.TileCenter> navigationTiles(){return tiles;}
    public boolean canPlayerOccupy(float wx,float wy){
      // Block the direct east-side endpoint near the entity, leaving a short west-side endpoint
      // and a longer route toward the geometrically closest alternative.
      return !(close(wx,32f)&&close(wy,-16f));
    }
    public float worldX(){return x;} public float worldY(){return y;}
    public boolean moveToAdjacentTile(float nx,float ny,WorldMoveTargetController.Direction d){x=nx;y=ny;return true;}
  }
  public static void main(String[] args){
    Grid g=new Grid();
    WorldMoveTargetController nav=new WorldMoveTargetController(g,g);
    WorldMoveTargetController.Snapshot s=nav.requestNpcApproach("guide",64f,0f,80f);
    if(s.status!=WorldMoveTargetController.Status.MOVING&&s.status!=WorldMoveTargetController.Status.REACHED)throw new AssertionError("approach must be reachable");
    int planned=s.remainingWaypoints;
    while(s.status==WorldMoveTargetController.Status.MOVING)s=nav.tick(WorldMoveTargetController.TILE_STEP_SECONDS);
    if(s.status!=WorldMoveTargetController.Status.REACHED)throw new AssertionError("approach must reach interaction tile");
    if(planned>2)throw new AssertionError("route should select a shortest interaction endpoint; steps="+planned);
    float d=(float)Math.sqrt((g.x-64f)*(g.x-64f)+g.y*g.y);
    if(d>80f)throw new AssertionError("endpoint outside interaction tolerance");
    System.out.println("QUICK_QUEST_APPROACH_PATH_AUDIT=PASS steps="+planned+" endpoint="+g.x+","+g.y);
  }
  private static boolean close(float a,float b){return Math.abs(a-b)<.01f;}
}
