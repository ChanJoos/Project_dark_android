package com.projectdark.mobile;
import android.content.Context;import com.projectdark.mobile.world.*;import java.util.*;
import org.junit.*;import org.junit.runner.RunWith;import org.robolectric.*;import org.robolectric.annotation.*;import static org.junit.Assert.*;
@RunWith(RobolectricTestRunner.class) @Config(sdk=34,manifest=Config.NONE) @GraphicsMode(GraphicsMode.Mode.NATIVE)
public class DeathRepairV222Test {
 @Test public void everyOriginalUndergroundDoorHasExactlyOneAlignedVisibleAndWalkablePortal()throws Exception{
  MineTownV203Test f=new MineTownV203Test();f.reset();Context c=RuntimeEnvironment.getApplication();GameView v=new GameView(c);v.layout(0,0,960,540);f.call(v,"travelTown",new Class<?>[]{String.class},DeathVillageGeometry.LOBBY);int doors=0;
  for(String id:DeathVillageData.ids())if(DeathVillageGeometry.under(id)){
   HorrorCastleOriginalData.Grid grid=DeathVillageData.grid(id);int nativeDoors=0,linked=0;
   for(int u=0;u<grid.width;u++)for(int y=0;y<grid.height;y++)for(int side=1;side<=2;side++)if(grid.value(u,y,side)==6633||grid.value(u,y,side)==6639){final int du=u,dv=y,ds=side;nativeDoors++;assertEquals(id+" original door "+u+","+y,1,DeathVillageGeometry.gates(id).stream().filter(g->g.doorU==du&&g.doorV==dv&&g.doorSide==ds).count());}
   f.call(v,"changeCampaignMap",new Class<?>[]{String.class,boolean.class},id,false);RuntimeState state=f.field(v,"state");WorldRuntimeAdapter w=f.field(v,"poteFieldAdapter");
   for(DeathVillageData.Gate g:DeathVillageGeometry.gates(id)){
    assertTrue(grid.walkable(g.u,g.v));assertTrue(DeathVillageData.grid(g.to).walkable(g.arrivalU,g.arrivalV));
    if(g.doorSide>0){linked++;int object=grid.value(g.doorU,g.doorV,g.doorSide);assertTrue(object==6633||object==6639);assertTrue(Math.hypot(DeathVillageGeometry.portalX(g)-DeathVillageGeometry.x(g.u,g.v),DeathVillageGeometry.portalY(g)-DeathVillageGeometry.y(g.u,g.v))<=36);}
    else if(DeathVillageGeometry.under(id)){assertEquals("explicit adapted junction/house stairs", "stairs",g.style);assertEquals(0,grid.value(g.u,g.v,1));assertEquals(0,grid.value(g.u,g.v,2));}
    state.player().x=DeathVillageGeometry.x(g.u,g.v);state.player().y=DeathVillageGeometry.y(g.u,g.v);w.snapCameraToPlayer();f.shot(v,"v222-door-"+id+"-"+g.u+"-"+g.v);
   }
   assertEquals(nativeDoors,linked);doors+=linked;
  }
  assertEquals("all ten original door openings",10,doors);
 }
 @Test public void requestedWereRatHpIsComparableWithoutInventingOriginalStats(){assertEquals(180000,DeathVillageMonsters.profile("DEATH_02").hp);assertEquals(750000,DeathVillageMonsters.profile("DEATH_02").exp);for(RuntimeState.Monster m:DeathVillageMonsters.spawn("MAP_DEATH_UNDER_C"))if(DeathVillageMonsters.type(m.id)==2)assertEquals(180000,m.maxHp);}
}
