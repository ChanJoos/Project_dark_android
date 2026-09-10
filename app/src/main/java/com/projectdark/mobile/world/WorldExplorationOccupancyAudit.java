package com.projectdark.mobile.world;

import android.graphics.RectF;
import com.projectdark.mobile.RuntimeState;
import com.projectdark.mobile.WorldDef;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;

/** Regression gate for [ADAPTED]/[B] Milles exploration targets under current runtime occupancy rules. */
public final class WorldExplorationOccupancyAudit {
  private static final float GRID=16f;
  private static final float TARGET_TOLERANCE=20f;
  private WorldExplorationOccupancyAudit(){}

  public static boolean verify(){
    WorldDef world=new WorldDef();
    for(WorldExplorationContract.Anchor a:WorldExplorationContract.millesPrototype())if(!occupiable(world,a.x,a.y))return false;
    WorldExplorationContract.Anchor spawn=WorldExplorationContract.byId("spawn");if(spawn==null)return false;
    int cols=(int)Math.floor((WorldDef.MAX_X-WorldDef.MIN_X)/GRID)+1;
    int rows=(int)Math.floor((WorldDef.MAX_Y-WorldDef.MIN_Y)/GRID)+1;
    int sx=nearestCellX(spawn.x),sy=nearestCellY(spawn.y);
    if(!occupiable(world,cellX(sx),cellY(sy)))return false;
    ArrayDeque<Long> q=new ArrayDeque<>();Set<Long> seen=new HashSet<>();long start=key(sx,sy);seen.add(start);q.add(start);
    int[] dx={1,-1,0,0},dy={0,0,1,-1};
    while(!q.isEmpty()){
      long cur=q.removeFirst();int cx=(int)(cur>>32),cy=(int)cur;
      for(int i=0;i<4;i++){
        int nx=cx+dx[i],ny=cy+dy[i];if(nx<0||ny<0||nx>=cols||ny>=rows)continue;
        long nk=key(nx,ny);if(seen.contains(nk)||!occupiable(world,cellX(nx),cellY(ny)))continue;
        seen.add(nk);q.addLast(nk);
      }
    }
    for(WorldExplorationContract.Anchor a:WorldExplorationContract.millesPrototype())if(!reachableNear(seen,a))return false;
    return true;
  }

  private static boolean occupiable(WorldDef world,float x,float y){
    float r=RuntimeState.PLAYER_RADIUS;
    if(x-r<WorldDef.MIN_X||x+r>WorldDef.MAX_X||y-r<WorldDef.MIN_Y||y+r>WorldDef.MAX_Y)return false;
    for(RectF b:world.blockers())if(x+r>b.left&&x-r<b.right&&y+r>b.top&&y-r<b.bottom)return false;
    for(WorldDef.NpcSpawn n:world.npcSpawns()){float min=r+RuntimeState.NPC_RADIUS+2f;if(distance(x,y,n.x,n.y)<min)return false;}
    for(WorldDef.MonsterSpawn m:world.monsterSpawns()){float min=r+RuntimeState.MONSTER_RADIUS+3f;if(distance(x,y,m.x,m.y)<min)return false;}
    return true;
  }

  private static boolean reachableNear(Set<Long> seen,WorldExplorationContract.Anchor a){
    int cx=nearestCellX(a.x),cy=nearestCellY(a.y),range=(int)Math.ceil(TARGET_TOLERANCE/GRID)+1;
    for(int x=cx-range;x<=cx+range;x++)for(int y=cy-range;y<=cy+range;y++)if(seen.contains(key(x,y))&&distance(cellX(x),cellY(y),a.x,a.y)<=TARGET_TOLERANCE)return true;
    return false;
  }
  private static int nearestCellX(float x){return Math.round((x-WorldDef.MIN_X)/GRID);} private static int nearestCellY(float y){return Math.round((y-WorldDef.MIN_Y)/GRID);}
  private static float cellX(int x){return WorldDef.MIN_X+x*GRID;} private static float cellY(int y){return WorldDef.MIN_Y+y*GRID;}
  private static long key(int x,int y){return ((long)x<<32)|(y&0xffffffffL);} private static float distance(float ax,float ay,float bx,float by){float dx=ax-bx,dy=ay-by;return(float)Math.sqrt(dx*dx+dy*dy);}
  public static void main(String[] args){if(!verify())throw new AssertionError("World exploration occupancy audit failed");System.out.println("WorldExplorationOccupancyAudit PASS");}
}
