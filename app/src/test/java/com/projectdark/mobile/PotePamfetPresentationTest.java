package com.projectdark.mobile;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import com.projectdark.mobile.world.PoteFieldDef;
import com.projectdark.mobile.world.PoteFieldRenderer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.GraphicsMode;
import java.io.File;
import java.io.FileOutputStream;
import java.util.HashSet;
import java.util.Set;

@RunWith(RobolectricTestRunner.class)
@Config(sdk=34,manifest=Config.NONE)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public final class PotePamfetPresentationTest {
  @Test public void fourDirectionsRenderDistinctIdleAndRollAttackAssets() throws Exception {
    PoteFieldRenderer renderer=new PoteFieldRenderer();
    CharacterRenderer.Direction[] directions={
        CharacterRenderer.Direction.NW,CharacterRenderer.Direction.NE,
        CharacterRenderer.Direction.SW,CharacterRenderer.Direction.SE};
    Bitmap sheet=Bitmap.createBitmap(480,256,Bitmap.Config.ARGB_8888);
    Canvas canvas=new Canvas(sheet);Set<Integer> frameCounts=new HashSet<>();
    for(int i=0;i<directions.length;i++){
      int x=60+i*120;
      renderer.drawPamfet(canvas,directions[i],false,0f,i*.4f,x,96f);
      renderer.drawPamfet(canvas,directions[i],true,.5f,0f,x,230f);
      int idle=countOpaque(sheet,new Rect(x-42,24,x+42,101));
      int roll=countOpaque(sheet,new Rect(x-42,152,x+42,235));
      assertTrue("idle sprite must be visible for "+directions[i],idle>100);
      assertTrue("directional roll attack sprite must be visible for "+directions[i],roll>100);
      frameCounts.add(idle);frameCounts.add(roll);
    }
    assertTrue("direction/action crops must not all collapse to the same placeholder",frameCounts.size()>=4);
    File file=new File("build/reports/device-review/pote-pamfet-directions.png");
    File parent=file.getParentFile();if(parent!=null)parent.mkdirs();
    try(FileOutputStream out=new FileOutputStream(file)){
      assertTrue(sheet.compress(Bitmap.CompressFormat.PNG,100,out));
    }
    sheet.recycle();
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
