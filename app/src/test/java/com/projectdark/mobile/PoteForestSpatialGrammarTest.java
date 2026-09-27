package com.projectdark.mobile;

import com.projectdark.mobile.world.PoteFieldDef;
import com.projectdark.mobile.world.PoteFieldRenderer;
import org.junit.Test;
import static org.junit.Assert.*;
import java.util.*;
import com.projectdark.mobile.world.WorldMoveTargetController;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(sdk=34,manifest=Config.NONE)
public final class PoteForestSpatialGrammarTest {
  @Test public void forestUsesDenseAuthoredAssetPlacements(){
    PoteFieldRenderer renderer=new PoteFieldRenderer();
    assertEquals("POTE_FOREST_REFERENCE_GROUND_V10",PoteFieldRenderer.STATUS);
    assertTrue("forest should read as connected canopy and understory around clearings",renderer.placementCount()>=180);
  }

  @Test public void navigationRetainsCorridorsAroundDenseGroves(){
    assertTrue(PoteFieldDef.navigationTiles().size()>250);
    // Entry and exit must remain represented by nearby walkable navigation tiles after visual/collision redesign.
    assertTrue(PoteFieldDef.navigationTiles().stream().anyMatch(t -> Math.abs(t.x-PoteFieldDef.ENTRY_X)<=64f && Math.abs(t.y-PoteFieldDef.ENTRY_Y)<=32f));
    assertTrue(PoteFieldDef.navigationTiles().stream().anyMatch(t -> Math.abs(t.x-PoteFieldDef.EXIT_X)<=64f && Math.abs(t.y-PoteFieldDef.EXIT_Y)<=32f));
    assertFalse(PoteFieldDef.atExit(PoteFieldDef.ENTRY_X,PoteFieldDef.ENTRY_Y));
    assertTrue(PoteFieldDef.atExit(PoteFieldDef.EXIT_X,PoteFieldDef.EXIT_Y));
  }

  @Test public void forestGroundCellsUseTheMillesisometricTileLattice(){
    List<WorldMoveTargetController.TileCenter> ground=PoteFieldDef.groundTiles();
    assertTrue("the entire map floor must be tiled, not sprinkled",ground.size()>500);
    WorldMoveTargetController.TileCenter center=ground.stream().filter(t->Math.abs(t.x-672f)<.1f&&Math.abs(t.y-496f)<.1f).findFirst().orElseThrow();
    int adjacent=0;
    for(WorldMoveTargetController.TileCenter t:ground)if(PoteFieldDef.areAdjacentGroundTiles(center.x,center.y,t.x,t.y))adjacent++;
    assertEquals("a fully joined interior diamond has four walkable neighbours",4,adjacent);
    for(WorldMoveTargetController.TileCenter t:ground)
      assertTrue("floor cells must remain on the 32x16 movement lattice",(Math.round(t.x/32f)+Math.round(t.y/16f))%2==0);
  }

  @Test public void entryCanReachNortheastWithoutCrossingAuthoredCollision(){
    List<WorldMoveTargetController.TileCenter> tiles=PoteFieldDef.navigationTiles();
    assertTrue("redesigned forest must retain a connected route", hasPath(tiles,PoteFieldDef.ENTRY_X,PoteFieldDef.ENTRY_Y,1120f,224f));
  }

  private static boolean hasPath(List<WorldMoveTargetController.TileCenter> tiles,float sx,float sy,float gx,float gy){
    int start=-1,goal=-1; float sd=Float.MAX_VALUE,gd=Float.MAX_VALUE;
    for(int i=0;i<tiles.size();i++){WorldMoveTargetController.TileCenter t=tiles.get(i);
      float a=(t.x-sx)*(t.x-sx)+(t.y-sy)*(t.y-sy), b=(t.x-gx)*(t.x-gx)+(t.y-gy)*(t.y-gy);
      if(a<sd){sd=a;start=i;} if(b<gd){gd=b;goal=i;}
    }
    if(start<0||goal<0)return false;
    boolean[] seen=new boolean[tiles.size()]; ArrayDeque<Integer> q=new ArrayDeque<>(); q.add(start);seen[start]=true;
    while(!q.isEmpty()){int i=q.remove(); if(i==goal)return true; WorldMoveTargetController.TileCenter a=tiles.get(i);
      for(int j=0;j<tiles.size();j++){if(seen[j])continue; WorldMoveTargetController.TileCenter b=tiles.get(j);
        float dx=Math.abs(a.x-b.x),dy=Math.abs(a.y-b.y);
        // Match the four runtime directions exactly; loose proximity made disconnected
        // navigation lattices appear connected in the earlier prototype test.
        if(Math.abs(dx-32f)<.1f&&Math.abs(dy-16f)<.1f){seen[j]=true;q.add(j);}
      }
    } return false;
  }

  @Test public void fieldIsMateriallyLargerThanOldPrototypeShell(){
    assertTrue(PoteFieldDef.MAX_X-PoteFieldDef.MIN_X>=1600f);
    assertTrue(PoteFieldDef.MAX_Y-PoteFieldDef.MIN_Y>=760f);
  }

  @Test public void windingCreekCrossesTheArrivalTrailOnlyAtTheBridge(){
    List<WorldMoveTargetController.TileCenter> tiles=PoteFieldDef.navigationTiles();
    assertEquals("arrival should sit near the map centre",896f,PoteFieldDef.ENTRY_X,160f);
    assertEquals("arrival should sit near the map centre",480f,PoteFieldDef.ENTRY_Y,80f);
    assertTrue("the trail should lead to the creek crossing",hasPath(tiles,PoteFieldDef.ENTRY_X,PoteFieldDef.ENTRY_Y,PoteFieldDef.BRIDGE_X,PoteFieldDef.BRIDGE_Y));
    assertTrue("the far bank should be reachable from field entry",hasPath(tiles,PoteFieldDef.ENTRY_X,PoteFieldDef.ENTRY_Y,1648f,480f));
    assertTrue("bridge deck gap must be unblocked",tiles.stream().anyMatch(t->Math.abs(t.x-PoteFieldDef.BRIDGE_X)<=32f&&Math.abs(t.y-PoteFieldDef.BRIDGE_Y)<=16f));
    assertTrue("winding creek away from the bridge must remain blocked",PoteFieldDef.obstacles().stream().anyMatch(r->r.contains(900f,820f)));
    float[][] creek=PoteFieldDef.creekCenterline();boolean turned=false;
    for(int i=2;i<creek.length;i++)if((creek[i][0]-creek[i-1][0])*(creek[i-1][0]-creek[i-2][0])<0)turned=true;
    assertTrue("the creek should change direction across the map",turned);
    List<WorldMoveTargetController.TileCenter> withoutDeck=new ArrayList<>();
    for(WorldMoveTargetController.TileCenter t:tiles)
      if(Math.abs(t.x-PoteFieldDef.BRIDGE_X)>80f||Math.abs(t.y-PoteFieldDef.BRIDGE_Y)>64f)withoutDeck.add(t);
    assertFalse("the far bank must not be reachable by walking around the creek ends",
        hasPath(withoutDeck,PoteFieldDef.ENTRY_X,PoteFieldDef.ENTRY_Y,1648f,480f));
  }
}
