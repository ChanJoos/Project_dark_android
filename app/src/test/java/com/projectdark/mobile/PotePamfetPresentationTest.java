package com.projectdark.mobile;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import com.projectdark.mobile.world.PoteFieldDef;
import com.projectdark.mobile.world.PoteFieldRenderer;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.GraphicsMode;
import java.util.HashSet;
import java.util.Set;

@RunWith(RobolectricTestRunner.class)
@Config(sdk=34,manifest=Config.NONE)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public final class PotePamfetPresentationTest {
  @Test public void everyShowcaseMonsterLoadsAllTwelveDirectionalPoseAssets() throws Exception {
    PoteFieldRenderer renderer=new PoteFieldRenderer();
    CharacterRenderer.Direction[] directions={
        CharacterRenderer.Direction.NW,CharacterRenderer.Direction.NE,
        CharacterRenderer.Direction.SW,CharacterRenderer.Direction.SE};
    String[] states={"idle","walk","attack"};
    assertEquals(16,PoteForestMonsterShowcase.monsterIds().size());
    for(String id:PoteForestMonsterShowcase.monsterIds())for(String state:states)for(CharacterRenderer.Direction direction:directions){
      Bitmap cell=Bitmap.createBitmap(128,128,Bitmap.Config.ARGB_8888);
      renderer.drawMonsterTestPose(new Canvas(cell),id,state,direction,.5f,.4f,64f,112f);
      assertTrue("missing/blank concept sprite "+id+" "+state+" "+direction,countOpaque(cell,new Rect(0,0,128,128))>100);
      cell.recycle();
    }
  }

  @Test public void runtimePlacesEveryMasterRosterIdentityOnADistinctWalkableTile(){
    RuntimeState state=new RuntimeState(RuntimeState.BootMode.POTE_01_PROTOTYPE,true);
    assertEquals(16,state.monsters().size());
    Set<String> ids=new HashSet<>();Set<String> locations=new HashSet<>();
    for(RuntimeState.Monster monster:state.monsters()){
      assertTrue(ids.add(monster.id));
      assertTrue(PoteFieldDef.isNavigationCenter(monster.x,monster.y));
      assertTrue(locations.add(monster.x+":"+monster.y));
      assertSame("visible test sprite must be tappable above its ground anchor",monster,state.hitMonster(monster.x,monster.y-55f,1f));
    }
    assertEquals(PoteForestMonsterShowcase.monsterIds().size(),ids.size());
    assertTrue("canonical spirit reward must not be farmable from the test fixture",ids.contains("POTE_SPIRIT_TEST_B"));
  }

  @Test public void purplePamfetIsPlacedOnTheEntryRouteWithoutChangingItsStats(){
    PotePrototypeWorldDef.Spawn spawn=PotePrototypeWorldDef.primarySpawn();
    assertEquals("POTE_PURPLE",spawn.monsterId);
    assertTrue(PoteFieldDef.navigationTiles().stream().anyMatch(t->
        Math.abs(t.x-spawn.x)<.01f&&Math.abs(t.y-spawn.y)<.01f));
    assertTrue("default Pamfet should be visible near the field arrival",
        Math.hypot(spawn.x-PoteFieldDef.ENTRY_X,spawn.y-PoteFieldDef.ENTRY_Y)<150f);
  }

  private static int countOpaque(Bitmap bitmap,Rect bounds){
    int count=0;for(int y=bounds.top;y<bounds.bottom;y++)for(int x=bounds.left;x<bounds.right;x++)
      if((bitmap.getPixel(x,y)>>>24)>0)count++;
    return count;
  }
}
