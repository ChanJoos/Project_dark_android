package com.projectdark.mobile;

import com.projectdark.mobile.world.PoteFieldDef;
import com.projectdark.mobile.world.PoteFieldRenderer;
import org.junit.Test;
import static org.junit.Assert.*;
import java.util.*;
import com.projectdark.mobile.world.WorldMoveTargetController;

public final class PoteForestSpatialGrammarTest {
  @Test public void forestUsesDenseAuthoredAssetPlacements(){
    PoteFieldRenderer renderer=new PoteFieldRenderer();
    assertEquals("POTE_FOREST_MASS_V3",PoteFieldRenderer.STATUS);
    assertTrue("forest should be grouped into authored groves, not rendered as scattered props",renderer.placementCount()>=70);
  }

  @Test public void navigationRetainsCorridorsAroundDenseGroves(){
    assertTrue(PoteFieldDef.navigationTiles().size()>250);
    // Entry and exit must remain represented by nearby walkable navigation tiles after visual/collision redesign.
    assertTrue(PoteFieldDef.navigationTiles().stream().anyMatch(t -> Math.abs(t.x-PoteFieldDef.ENTRY_X)<=64f && Math.abs(t.y-PoteFieldDef.ENTRY_Y)<=32f));
    assertTrue(PoteFieldDef.navigationTiles().stream().anyMatch(t -> Math.abs(t.x-PoteFieldDef.EXIT_X)<=64f && Math.abs(t.y-PoteFieldDef.EXIT_Y)<=32f));
    assertFalse(PoteFieldDef.atExit(PoteFieldDef.ENTRY_X,PoteFieldDef.ENTRY_Y));
    assertTrue(PoteFieldDef.atExit(PoteFieldDef.EXIT_X,PoteFieldDef.EXIT_Y));
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
    assertTrue(PoteFieldDef.MAX_X-PoteFieldDef.MIN_X>=1200f);
    assertTrue(PoteFieldDef.MAX_Y-PoteFieldDef.MIN_Y>=760f);
  }
}
