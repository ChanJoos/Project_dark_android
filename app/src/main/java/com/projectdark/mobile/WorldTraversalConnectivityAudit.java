package com.projectdark.mobile;

import android.graphics.RectF;
import com.projectdark.mobile.world.AdaptedMillesVillageLayout;
import com.projectdark.mobile.world.WorldSpatialLayout;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Regression gate for the [ADAPTED]/[B] Milles prototype village.
 *
 * This audit consumes the same WorldDef collision rectangles used by runtime and the same named
 * spatial anchors exposed by AdaptedMillesVillageLayout.  It fails when a road/plaza/gate anchor
 * becomes unreachable at the player collision radius, preventing a visually authored route from
 * silently becoming non-traversable.
 */
public final class WorldTraversalConnectivityAudit {
  private static final float CELL=16f;
  private static final float PLAYER_RADIUS=RuntimeState.PLAYER_RADIUS;

  private WorldTraversalConnectivityAudit(){}

  public static boolean verify(){
    WorldDef world=new WorldDef();
    WorldSpatialLayout layout=AdaptedMillesVillageLayout.create();
    WorldSpatialLayout.Anchor spawn=require(layout,"spawn");
    if(spawn==null)return false;

    Set<Long> reachable=flood(world,spawn.x,spawn.y);
    String[] required={
        "spawn",
        "plaza_center",
        "north_cross",
        "service_npc_approach",
        "west_lane",
        "east_lane",
        "south_gate_approach",
        "south_portal"
    };
    for(String id:required){
      WorldSpatialLayout.Anchor anchor=require(layout,id);
      if(anchor==null||!canOccupy(world,anchor.x,anchor.y)||!reachable.contains(key(cellX(anchor.x),cellY(anchor.y))))return false;
    }

    // The target-pending portal must remain physically approachable even though transition is disabled.
    if(world.portalSpawns().isEmpty())return false;
    WorldDef.PortalSpawn portal=world.portalSpawns().get(0);
    if(!"PROTOTYPE_DISABLED_TARGET_PENDING".equals(portal.status))return false;
    WorldSpatialLayout.Anchor portalAnchor=layout.anchor("south_portal");
    if(portalAnchor==null||distance(portal.x,portal.y,portalAnchor.x,portalAnchor.y)>CELL)return false;

    // At least one alternate lower-lane circulation choice must remain open.
    if(!connected(reachable,layout.anchor("west_lane"),layout.anchor("east_lane")))return false;
    return true;
  }

  private static Set<Long> flood(WorldDef world,float sx,float sy){
    int startX=cellX(sx),startY=cellY(sy);
    ArrayDeque<int[]> queue=new ArrayDeque<>();
    Set<Long> seen=new HashSet<>();
    if(!canOccupy(world,worldX(startX),worldY(startY)))return seen;
    queue.add(new int[]{startX,startY});
    seen.add(key(startX,startY));
    final int[] ox={1,-1,0,0},oy={0,0,1,-1};
    while(!queue.isEmpty()){
      int[] c=queue.removeFirst();
      for(int i=0;i<4;i++){
        int nx=c[0]+ox[i],ny=c[1]+oy[i];
        long k=key(nx,ny);
        float wx=worldX(nx),wy=worldY(ny);
        if(seen.contains(k)||wx<WorldDef.MIN_X||wx>WorldDef.MAX_X||wy<WorldDef.MIN_Y||wy>WorldDef.MAX_Y)continue;
        if(!canOccupy(world,wx,wy))continue;
        seen.add(k);
        queue.addLast(new int[]{nx,ny});
      }
    }
    return seen;
  }

  private static boolean canOccupy(WorldDef world,float x,float y){
    List<RectF> blockers=world.blockers();
    for(RectF r:blockers){
      if(x+PLAYER_RADIUS>r.left&&x-PLAYER_RADIUS<r.right&&y+PLAYER_RADIUS>r.top&&y-PLAYER_RADIUS<r.bottom)return false;
    }
    return x>=WorldDef.MIN_X&&x<=WorldDef.MAX_X&&y>=WorldDef.MIN_Y&&y<=WorldDef.MAX_Y;
  }

  private static boolean connected(Set<Long> reachable,WorldSpatialLayout.Anchor a,WorldSpatialLayout.Anchor b){
    return a!=null&&b!=null&&reachable.contains(key(cellX(a.x),cellY(a.y)))&&reachable.contains(key(cellX(b.x),cellY(b.y)));
  }

  private static WorldSpatialLayout.Anchor require(WorldSpatialLayout layout,String id){return layout.anchor(id);}
  private static int cellX(float x){return Math.round((x-WorldDef.MIN_X)/CELL);}
  private static int cellY(float y){return Math.round((y-WorldDef.MIN_Y)/CELL);}
  private static float worldX(int x){return WorldDef.MIN_X+x*CELL;}
  private static float worldY(int y){return WorldDef.MIN_Y+y*CELL;}
  private static long key(int x,int y){return((long)x<<32)^(y&0xffffffffL);}
  private static float distance(float ax,float ay,float bx,float by){float dx=ax-bx,dy=ay-by;return(float)Math.sqrt(dx*dx+dy*dy);}

  public static void main(String[] args){
    if(!verify())throw new AssertionError("World traversal connectivity audit failed");
    System.out.println("WorldTraversalConnectivityAudit PASS");
  }
}
