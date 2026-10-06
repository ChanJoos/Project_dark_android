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
import java.util.Arrays;
import java.io.File;
import java.io.FileOutputStream;

@RunWith(RobolectricTestRunner.class)
@Config(sdk=34,manifest=Config.NONE)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public final class PotePamfetPresentationTest {
  @Test public void everyCurrentCandidateLoadsAllTwelveDirectionalPoseAssets() throws Exception {
    PoteFieldRenderer renderer=new PoteFieldRenderer();
    CharacterRenderer.Direction[] directions={
        CharacterRenderer.Direction.NW,CharacterRenderer.Direction.NE,
        CharacterRenderer.Direction.SW,CharacterRenderer.Direction.SE};
    String[] states={"idle","walk","attack"};
    assertEquals(Arrays.asList("POTE_PURPLE","POTE_RED","POTE_GREEN","POTE_SILVER","POTE_LYCAN"),PoteForestMonsterShowcase.monsterIds());
    for(String id:PoteForestMonsterShowcase.monsterIds())for(String state:states)for(CharacterRenderer.Direction direction:directions){
      Bitmap cell=Bitmap.createBitmap(128,128,Bitmap.Config.ARGB_8888);
      renderer.drawMonsterTestPose(new Canvas(cell),id,state,direction,.5f,.4f,64f,112f);
      assertTrue("missing/blank generated sprite "+id+" "+state+" "+direction,countOpaque(cell,new Rect(0,0,128,128))>100);
      Rect ink=opaqueBounds(cell,new Rect(0,0,128,128));
      if("POTE_LYCAN".equals(id))assertTrue("Lycan must approach player scale",ink.height()>=40&&ink.height()<=52);
      else assertTrue("Pamfets retain their small scale",ink.height()<=34);
      File file=new File("build/reports/device-review/monster-candidates/"+id+"_"+state+"_"+direction.name().toLowerCase()+".png");
      File parent=file.getParentFile();if(parent!=null)parent.mkdirs();
      try(FileOutputStream out=new FileOutputStream(file)){assertTrue(cell.compress(Bitmap.CompressFormat.PNG,100,out));}
      cell.recycle();
    }
    assertEquals("unknown species must not be registered",null,PoteForestMonsterShowcase.assetPath("POTE_UNKNOWN","attack",CharacterRenderer.Direction.NE));
    for(String id:new String[]{"POTE_TREANT","POTE_ANTLION","POTE_GNOLL","POTE_WOLFRIDER","POTE_ANTGIANT","POTE_SILVERWOLF"})for(String state:states)for(CharacterRenderer.Direction direction:directions){
      Bitmap cell=Bitmap.createBitmap(128,128,Bitmap.Config.ARGB_8888);
      renderer.drawMonsterTestPose(new Canvas(cell),id,state,direction,.5f,.4f,64f,112f);
      assertTrue("complete new campaign sprite "+id+" "+state+" "+direction,countOpaque(cell,new Rect(0,0,128,128))>100);
      cell.recycle();
    }
  }

  @Test public void runtimePlacesEveryMasterRosterIdentityOnADistinctWalkableTile(){
    RuntimeState state=new RuntimeState(RuntimeState.BootMode.POTE_01_PROTOTYPE,true);
    assertEquals(5,state.monsters().size());
    state.enterPoteField();
    Set<String> ids=new HashSet<>();Set<String> locations=new HashSet<>();
    for(RuntimeState.Monster monster:state.monsters()){
      assertTrue(ids.add(monster.id));
      assertTrue(PoteFieldDef.isNavigationCenter(monster.x,monster.y));
      double entryDistance=Math.hypot(monster.x-PoteFieldDef.ENTRY_X,monster.y-PoteFieldDef.ENTRY_Y);
      assertTrue("all five test actors must be in the entrance view/aggro pocket",entryDistance>=72&&entryDistance<=168);
      assertTrue(locations.add(monster.x+":"+monster.y));
      assertSame("visible test sprite must be tappable above its ground anchor",monster,state.hitMonster(monster.x,monster.y-24f,1f));
    }
    assertEquals(PoteForestMonsterShowcase.monsterIds().size(),ids.size());
    assertEquals("the playable art contract has four diagonal directions",4,CharacterRenderer.Direction.values().length);
    assertTrue("strong variants stay outside this art candidate run",java.util.Collections.disjoint(ids,Arrays.asList("POTE_STRONG_GNOLL","POTE_STRONG_WOLFRIDER","POTE_STRONG_TREANT")));
  }

  @Test public void mantisFinalEncounterLoadsAllFourDirectionsAndCombatPoses() throws Exception {
    PoteFieldRenderer renderer=new PoteFieldRenderer();
    for(String state:new String[]{"idle","walk","attack"})for(CharacterRenderer.Direction direction:new CharacterRenderer.Direction[]{
        CharacterRenderer.Direction.NW,CharacterRenderer.Direction.NE,CharacterRenderer.Direction.SW,CharacterRenderer.Direction.SE}){
      Bitmap frame=Bitmap.createBitmap(128,128,Bitmap.Config.ARGB_8888);
      renderer.drawMonsterTestPose(new Canvas(frame),"POTE_MANTIS#0",state,direction,.5f,.4f,64f,112f);
      Rect bounds=opaqueBounds(frame,new Rect(0,0,128,128));
      assertTrue("mantis final boss pose is present: "+state+" "+direction,countOpaque(frame,new Rect(0,0,128,128))>100);
      assertTrue("mantis is rendered at boss scale: "+bounds.height(),bounds.height()>=48&&bounds.height()<=72);
      File file=new File("build/reports/device-review/monster-candidates/POTE_MANTIS_"+state+"_"+direction.name().toLowerCase()+".png");
      File parent=file.getParentFile();if(parent!=null)parent.mkdirs();
      try(FileOutputStream out=new FileOutputStream(file)){assertTrue(frame.compress(Bitmap.CompressFormat.PNG,100,out));}
      frame.recycle();
    }
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
  private static Rect opaqueBounds(Bitmap bitmap,Rect bounds){
    Rect out=new Rect(bounds.right,bounds.bottom,bounds.left,bounds.top);
    for(int y=bounds.top;y<bounds.bottom;y++)for(int x=bounds.left;x<bounds.right;x++)
      if((bitmap.getPixel(x,y)>>>24)>0){out.left=Math.min(out.left,x);out.top=Math.min(out.top,y);out.right=Math.max(out.right,x+1);out.bottom=Math.max(out.bottom,y+1);}
    return out;
  }
}
