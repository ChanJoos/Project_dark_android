package com.projectdark.mobile;

import static org.junit.Assert.*;
import android.content.Context;
import android.graphics.*;
import com.projectdark.mobile.world.*;
import java.io.*;
import java.util.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.*;

@RunWith(RobolectricTestRunner.class) @Config(sdk=34,manifest=Config.NONE)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public class InnWorldObjectTest {
  private Context context;
  @Before public void setup(){context=RuntimeEnvironment.getApplication();context.getSharedPreferences("project_dark_f5m_v1",0).edit().clear().commit();F5mSaveStore.install(context);}
  private GameView start() throws Exception {GameView v=new GameView(context);v.layout(0,0,960,540);TownInteriorTest.enter(v,TownInteriorDef.forMap("milles_interior_inn"));return v;}
  private void capture(GameView v,String name)throws Exception {
    Bitmap b=Bitmap.createBitmap(960,540,Bitmap.Config.ARGB_8888);v.draw(new Canvas(b));
    File f=new File("build/reports/device-review/v92-inn-"+name+".png");f.getParentFile().mkdirs();try(FileOutputStream out=new FileOutputStream(f)){assertTrue(b.compress(Bitmap.CompressFormat.PNG,100,out));}b.recycle();
  }
  @Test public void everyFurnitureCellIsBlockedAndNoDiningClusterRemains() throws Exception {
    GameView v=start();TownInteriorDef d=TownInteriorDef.forMap("milles_interior_inn");WorldRuntimeAdapter w=TownInteriorTest.field(v,"reagentShopAdapter");
    TownInteriorRenderer renderer=new TownInteriorRenderer(context);Set<String> occupied=new HashSet<>();int tables=0,chairs=0,counters=0;
    for(TownInteriorDef.Prop o:d.props){
      assertFalse("no screenshot counter/dining group",Arrays.asList("inn_counter","inn_table","inn_table_food").contains(o.asset));assertTrue(renderer.hasProp(o.asset));
      if(o.asset.equals("inn_world_table")){tables++;assertEquals(2,o.cellsU);assertEquals(1,o.cellsV);assertEquals(o.y()+16,o.depthY(),.01);}
      if(o.asset.startsWith("inn_world_chair_"))chairs++;
      if(o.asset.startsWith("joined_counter"))counters++;
      if(o.blocking)for(int u=o.u;u<o.u+o.cellsU;u++)for(int t=o.v;t<o.v+o.cellsV;t++){
        assertTrue("unique occupied footprint "+u+","+t,occupied.add(u+","+t));assertTrue(d.blocked(u,t));
        assertFalse("player cannot stand inside a rendered solid cell",w.canPlayerOccupy(TownInteriorDef.x(u,t),TownInteriorDef.y(u,t)));
      }
    }
    assertEquals(3,tables);assertEquals(12,chairs);assertEquals(11,counters);
    assertTrue("rug corner is walkable ground",w.canPlayerOccupy(TownInteriorDef.x(3,6),TownInteriorDef.y(3,6)));
    capture(v,"entry-world-objects");
  }
  @Test public void liveMovementRoutesAroundAllTableAndChairFootprints()throws Exception {
    GameView v=start();RuntimeState state=TownInteriorTest.field(v,"state");TownInteriorDef d=TownInteriorDef.forMap(state.currentMapId());WorldRuntimeAdapter w=TownInteriorTest.field(v,"reagentShopAdapter");
    for(int[] destination:new int[][]{{6,8},{5,6},{3,6},{8,10},{6,11}}){
      assertNotEquals(WorldMoveTargetController.Status.BLOCKED,w.requestGroundWorld(TownInteriorDef.x(destination[0],destination[1]),TownInteriorDef.y(destination[0],destination[1])).status);
      for(int n=0;n<350;n++){TownInteriorTest.tick(v,1);assertFalse("applied player feet never enter furniture",d.props.stream().anyMatch(o->o.blocking&&atFootprint(o,state.player().x,state.player().y)));}
      assertEquals(TownInteriorDef.x(destination[0],destination[1]),state.player().x,.01);assertEquals(TownInteriorDef.y(destination[0],destination[1]),state.player().y,.01);
      capture(v,"walk-"+destination[0]+"-"+destination[1]);
    }
  }
  private boolean atFootprint(TownInteriorDef.Prop o,float x,float y){for(int u=o.u;u<o.u+o.cellsU;u++)for(int v=o.v;v<o.v+o.cellsV;v++)if(Math.abs(x-TownInteriorDef.x(u,v))<.5&&Math.abs(y-TownInteriorDef.y(u,v))<.5)return true;return false;}
  @Test public void rearAndFrontViewsUseSharedWorldProjectionAndGroundDepth()throws Exception {
    GameView v=start();RuntimeState state=TownInteriorTest.field(v,"state");WorldRuntimeAdapter w=TownInteriorTest.field(v,"reagentShopAdapter");
    for(int[] position:new int[][]{{5,6},{6,8},{8,7},{11,9}}){
      assertTrue(w.canPlayerOccupy(TownInteriorDef.x(position[0],position[1]),TownInteriorDef.y(position[0],position[1])));
      state.player().x=TownInteriorDef.x(position[0],position[1]);state.player().y=TownInteriorDef.y(position[0],position[1]);w.snapCameraToPlayer();
      WorldCameraTransform.Point a=w.worldToScreen(TownInteriorDef.x(4,7),TownInteriorDef.y(4,7)),b=w.worldToScreen(TownInteriorDef.x(5,7),TownInteriorDef.y(5,7));
      assertEquals("same isometric tile projection as shop",32,b.x-a.x,.001);assertEquals(16,b.y-a.y,.001);
      capture(v,"depth-"+position[0]+"-"+position[1]);
    }
  }
}
