package com.projectdark.mobile;

import android.graphics.*;
import com.projectdark.mobile.world.*;
import java.io.*;
import java.lang.reflect.Field;
import java.util.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.*;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class) @Config(sdk=34,manifest=Config.NONE) @GraphicsMode(GraphicsMode.Mode.NATIVE)
public class MinePolishV206Test {
 @Test public void treeKeepsNativeLeavesAndTrunkButNeverCarriesSourceRiverIntoTown()throws Exception {
  android.content.Context c=RuntimeEnvironment.getApplication();Bitmap original=BitmapFactory.decodeStream(c.getAssets().open("mine-v205/tree.png"));
  int w=original.getWidth(),h=original.getHeight(),removed=0,kept=0;int[] before=new int[w*h];original.getPixels(before,0,w,0,0,w,h);
  MineTownRenderer renderer=new MineTownRenderer(c);renderer.prepare();Field f=MineTownRenderer.class.getDeclaredField("art");f.setAccessible(true);Bitmap result=((Map<String,Bitmap>)f.get(renderer)).get("tree");
  assertEquals(w,result.getWidth());assertEquals(h,result.getHeight());
  for(int y=0;y<h;y++)for(int x=0;x<w;x++) {int p=before[y*w+x],q=result.getPixel(x,y);
   if(Color.alpha(p)>0&&Color.blue(p)>Color.red(p)+12&&Color.blue(p)>Color.green(p)+2){assertEquals(0,q);removed++;}
   else if(Color.alpha(p)>0){assertEquals("native leaf/trunk pixel",p,q);kept++;}
  }
  assertTrue("reproduce blue contamination",removed>30);assertTrue("retain most of the tree",kept>2500);
 }
 @Test public void actualSceneIncludesCleanTreeLawnVariationAndBothStableServicePaths()throws Exception {
  MineTownV203Test fixture=new MineTownV203Test();fixture.reset();GameView view=fixture.town();RuntimeState state=fixture.field(view,"state");WorldRuntimeAdapter world=fixture.field(view,"poteFieldAdapter");
  for(int[] uv:new int[][]{{16,24},{27,20},{35,19},{42,23},{42,50},{21,32},{45,32}}){
   WorldMoveTargetController.TileCenter t=CampaignWorld.nearestConnectedTile(MineTownGeometry.ID,MineTownGeometry.x(uv[0],uv[1]),MineTownGeometry.y(uv[0],uv[1]));state.player().x=t.x;state.player().y=t.y;world.snapCameraToPlayer();fixture.shot(view,"v206-"+uv[0]+"-"+uv[1]);
  }
  view.layout(0,0,1280,540);fixture.shot(view,"v206-wide");
  for(MineTownGeometry.Building b:MineTownGeometry.BUILDINGS)assertTrue("unchanged service approach",CampaignNavigationTest.walk(world,world.requestGroundWorld(b.doorX(),b.doorY())));
 }
}
