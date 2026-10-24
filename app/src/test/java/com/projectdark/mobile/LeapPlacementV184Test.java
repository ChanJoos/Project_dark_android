package com.projectdark.mobile;
import static org.junit.Assert.*;
import android.graphics.RectF;
import com.projectdark.mobile.world.*;
import java.util.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.*;
@RunWith(RobolectricTestRunner.class) @Config(sdk=34,manifest=Config.NONE)
public class LeapPlacementV184Test {
 @Test public void legalTileSnapUsesStaticIndexAndBlockedOrOffGridPositionsStillRelocate() throws Exception {
  RuntimeState s=new RuntimeState();s.enterCampaignMap(HorrorCastleGeometry.roomId(1),false);
  List<WorldMoveTargetController.TileCenter> tiles=HorrorCastleGeometry.tiles(s.currentMapId(),true);
  WorldRuntimeAdapter w=new WorldRuntimeAdapter(s,960,540,HorrorCastleGeometry.minX(s.currentMapId()),HorrorCastleGeometry.maxX(s.currentMapId()),0,HorrorCastleGeometry.maxY(s.currentMapId()),tiles,HorrorCastleGeometry.obstacles(s.currentMapId()),true);
  WorldMoveTargetController.TileCenter safe=tiles.stream().filter(t->w.canPlayerOccupy(t.x,t.y)).findFirst().get();s.player().x=safe.x;s.player().y=safe.y;
  java.lang.reflect.Field field=WorldRuntimeAdapter.class.getDeclaredField("navigationTiles");field.setAccessible(true);Object original=field.get(w);
  // An indexed, legal tile must not enumerate the map when a leap resets its camera.
  field.set(w,new AbstractList<WorldMoveTargetController.TileCenter>(){public int size(){throw new AssertionError("full map scan during legal landing");}public WorldMoveTargetController.TileCenter get(int n){throw new AssertionError("full map scan");}});
  w.snapCameraToPlayer();assertEquals(safe.x,s.player().x,0);assertEquals(safe.y,s.player().y,0);
  field.set(w,original);s.player().x=-5000;s.player().y=-5000;w.snapCameraToPlayer();assertTrue(tiles.stream().anyMatch(t->t.x==s.player().x&&t.y==s.player().y));assertTrue(w.canPlayerOccupy(s.player().x,s.player().y));
  RuntimeState.Monster m=s.monsters().stream().filter(a->a.alive&&!a.isMoving).findFirst().get();s.player().x=m.x;s.player().y=m.y;w.snapCameraToPlayer();assertFalse(s.player().x==m.x&&s.player().y==m.y);assertTrue(w.canPlayerOccupy(s.player().x,s.player().y));
 }
}
