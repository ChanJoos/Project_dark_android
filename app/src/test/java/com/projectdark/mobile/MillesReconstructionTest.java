package com.projectdark.mobile;

import static org.junit.Assert.*;
import android.content.Context;
import android.graphics.*;
import com.projectdark.mobile.world.*;
import java.io.*;
import java.util.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.*;

/** V100: actual scene depth, landmark access and whole-map production renders. */
@RunWith(RobolectricTestRunner.class)
@Config(sdk=34,manifest=Config.NONE)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public final class MillesReconstructionTest {
  private GameView fresh(){Context c=RuntimeEnvironment.getApplication();c.getSharedPreferences("project_dark_f5m_v1",0).edit().clear().commit();F5mSaveStore.install(c);GameView v=new GameView(c);v.layout(0,0,1920,1080);return v;}

  @Test public void standingSceneryActuallyOccludesAnActorBehindButNotInFront() throws Exception {
    GameView v=fresh();WorldRuntimeAdapter world=TownInteriorTest.field(v,"worldAdapter");
    world.camera().snapTo(768,592);AdaptedMillesMapRenderer renderer=new AdaptedMillesMapRenderer();
    WorldCameraTransform.Point q=world.worldToScreen(768,562);Paint actor=new Paint();actor.setColor(0xffff00ff);
    Bitmap behind=Bitmap.createBitmap(960,540,Bitmap.Config.ARGB_8888);Canvas cb=new Canvas(behind);
    renderer.draw(cb,world,Collections.singletonList(new AdaptedMillesMapRenderer.DepthDraw(591,()->cb.drawRect(q.x-8,q.y-8,q.x+8,q.y+8,actor))));
    Bitmap front=Bitmap.createBitmap(960,540,Bitmap.Config.ARGB_8888);Canvas cf=new Canvas(front);
    renderer.draw(cf,world,Collections.singletonList(new AdaptedMillesMapRenderer.DepthDraw(593,()->cf.drawRect(q.x-8,q.y-8,q.x+8,q.y+8,actor))));
    assertEquals("actor in front owns overlap pixels",0xffff00ff,front.getPixel(Math.round(q.x),Math.round(q.y)));
    assertNotEquals("fountain hides the actor behind it",0xffff00ff,behind.getPixel(Math.round(q.x),Math.round(q.y)));
    write(behind,"depth-behind");write(front,"depth-front");behind.recycle();front.recycle();
  }

  @Test public void allServiceDoorsAndDistrictsRemainReachableWithSceneryCollision() throws Exception {
    GameView v=fresh();WorldRuntimeAdapter world=TownInteriorTest.field(v,"worldAdapter");
    List<float[]> targets=new ArrayList<>();
    for(MillesDoorAnchors.Door door:MillesDoorAnchors.ALL)targets.add(new float[]{door.approachX,door.approachY});
    targets.addAll(Arrays.asList(new float[]{992,576},new float[]{256,800},new float[]{800,1136},new float[]{1696,1152},new float[]{1888,1056},new float[]{2080,960},new float[]{800,1536},new float[]{620,560}));
    for(float[] target:targets){
      WorldMoveTargetController.Snapshot request=world.requestGroundWorld(target[0],target[1]);
      assertNotEquals("district request "+Arrays.toString(target),WorldMoveTargetController.Status.BLOCKED,request.status);
      for(int i=0;i<3000&&world.movement().snapshot().status==WorldMoveTargetController.Status.MOVING;i++)world.tickNavigation(.1f);
      assertEquals("actual navigation finishes "+Arrays.toString(target),WorldMoveTargetController.Status.REACHED,world.movement().snapshot().status);
      assertTrue(world.canPlayerOccupy(world.runtime().player().x,world.runtime().player().y));
    }
  }

  @Test public void waterBlocksSwimmingAndBridgeDeckRemainsWalkable(){
    boolean water=false,tree=false,bench=false,fountain=false;
    for(MillesProductionCollision.Footprint f:MillesProductionCollision.blockers()){
      water|=f.kind==MillesProductionCollision.Kind.LAKE;tree|=f.kind==MillesProductionCollision.Kind.TREE;
      bench|=f.kind==MillesProductionCollision.Kind.BENCH;fountain|=f.kind==MillesProductionCollision.Kind.FOUNTAIN;
    }
    assertTrue(water&&tree&&bench&&fountain);
    assertTrue("pond beside deck blocks standing in water",MillesProductionCollision.blocked(1888,1120,9));
    assertFalse("bridge center remains navigable",MillesProductionCollision.blocked(1888,1056,9));
    assertFalse("initial spawn remains clear",MillesProductionCollision.blocked(620,560,9));
  }

  @Test public void renderEveryDistrictAndPlayerOnBothSidesOfScenery() throws Exception {
    GameView v=fresh();RuntimeState state=TownInteriorTest.field(v,"state");WorldRuntimeAdapter world=TownInteriorTest.field(v,"worldAdapter");
    float[][] points={{620,560},{768,592},{320,448},{1120,480},{1540,520},{2032,784},{1888,1056},{400,1136},{1088,1264},{800,1456},{512,480},{512,544}};
    String[] names={"spawn","fountain-park","west-services","bank-garden","church-east","inn","waterside-bridge","south-west","south-garden","south-gate","tree-behind","tree-front"};
    for(int i=0;i<points.length;i++){
      state.player().x=points[i][0];state.player().y=points[i][1];world.snapCameraToPlayer();
      Bitmap b=Bitmap.createBitmap(1920,1080,Bitmap.Config.ARGB_8888);v.draw(new Canvas(b));write(b,names[i]);b.recycle();
    }
    v.layout(0,0,2340,1080);world.snapCameraToPlayer();Bitmap wide=Bitmap.createBitmap(2340,1080,Bitmap.Config.ARGB_8888);v.draw(new Canvas(wide));write(wide,"wide-tree");wide.recycle();
    WorldRuntimeAdapter overview=new WorldRuntimeAdapter(state,2816,1552);
    Bitmap map=Bitmap.createBitmap(2816,1552,Bitmap.Config.ARGB_8888);
    new AdaptedMillesMapRenderer().draw(new Canvas(map),overview);write(map,"overview");map.recycle();
  }
  private static void write(Bitmap b,String name)throws IOException{
    File f=new File("build/reports/device-review/v100-"+name+".png");f.getParentFile().mkdirs();
    ByteArrayOutputStream encoded=new ByteArrayOutputStream();assertTrue(b.compress(Bitmap.CompressFormat.PNG,100,encoded));
    byte[] bytes=encoded.toByteArray(),end={0,0,0,0,73,69,78,68,(byte)174,66,96,(byte)130};
    assertTrue("complete PNG stream",bytes.length>end.length);
    assertArrayEquals("PNG IEND is intact",end,Arrays.copyOfRange(bytes,bytes.length-end.length,bytes.length));
    java.nio.file.Path temporary=java.nio.file.Files.createTempFile(f.getParentFile().toPath(),"milles-render-",".tmp");
    java.nio.file.Files.write(temporary,bytes);java.nio.file.Files.move(temporary,f.toPath(),java.nio.file.StandardCopyOption.REPLACE_EXISTING,java.nio.file.StandardCopyOption.ATOMIC_MOVE);
    assertArrayEquals("saved review bytes equal complete native encoding",bytes,java.nio.file.Files.readAllBytes(f.toPath()));
  }
}
