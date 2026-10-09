package com.projectdark.mobile;

import android.graphics.*;
import com.projectdark.mobile.world.*;
import java.io.*;
import java.lang.reflect.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.*;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class) @Config(sdk=34,manifest=Config.NONE)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public class PoteMonsterVisualRepairV122Test {
  static void save(Bitmap b,String name)throws Exception{
    File f=new File("build/reports/monster-visual-v122/"+name+".png");f.getParentFile().mkdirs();
    try(FileOutputStream out=new FileOutputStream(f)){assertTrue(b.compress(Bitmap.CompressFormat.PNG,100,out));}
  }
  @Test public void spiritHoovesRemainStableAcrossGaitAndFractionalScreenPositions()throws Exception{
    PoteFieldRenderer r=new PoteFieldRenderer();
    for(String id:new String[]{"POTE_SPIRIT#0","POTE_SPIRIT#1"}){
      Bitmap sheet=Bitmap.createBitmap(8*160,4*160,Bitmap.Config.ARGB_8888);Canvas c=new Canvas(sheet);
      for(CharacterRenderer.Direction d:CharacterRenderer.Direction.values()){
        int[] reference=null;
        for(int i=0;i<16;i++){
          Bitmap frame=Bitmap.createBitmap(160,160,Bitmap.Config.ARGB_8888);
          r.drawMonsterTestPose(new Canvas(frame),id,"walk",d,0,i*CharacterRenderer.WALK_CYCLE_SECONDS/16f,80.1f+(i%3)*.1f,112.1f+(i%3)*.1f);
          int[] feet=new int[160*12];frame.getPixels(feet,0,160,0,104,160,12);
          if(reference==null){reference=feet;int ink=0;for(int color:feet)if((color>>>24)>0)ink++;assertTrue("feet actually present "+id+" "+d,ink>0);}
          else assertArrayEquals("stable hoof pixels "+id+" "+d,reference,feet);
          if(i%2==0)c.drawBitmap(frame,(i/2)*160,d.ordinal()*160,null);frame.recycle();
        }
      }
      save(sheet,id.replace('#','-')+"-feet");sheet.recycle();
    }
  }
  @Test public void backIdleAndWalkAgreeAndAntlionHeadsFaceCanonicalDiagonal()throws Exception{
    PoteFieldRenderer r=new PoteFieldRenderer();
    Bitmap sheet=Bitmap.createBitmap(8*128,3*128,Bitmap.Config.ARGB_8888);Canvas c=new Canvas(sheet);
    String[] ids={"POTE_TREANT","POTE_ANTGIANT","POTE_ANTLION"};
    Method load=PoteFieldRenderer.class.getDeclaredMethod("bitmap",String.class);load.setAccessible(true);
    for(int row=0;row<ids.length;row++)for(CharacterRenderer.Direction d:CharacterRenderer.Direction.values()){
      Bitmap idle=Bitmap.createBitmap(128,128,Bitmap.Config.ARGB_8888),walk=Bitmap.createBitmap(128,128,Bitmap.Config.ARGB_8888);
      r.drawMonsterTestPose(new Canvas(idle),ids[row],"idle",d,0,0,64,112);
      r.drawMonsterTestPose(new Canvas(walk),ids[row],"walk",d,0,0,64,112);
      if(row<2&&(d==CharacterRenderer.Direction.NW||d==CharacterRenderer.Direction.NE))assertTrue("consistent back silhouette "+ids[row]+" "+d,idle.sameAs(walk));
      c.drawBitmap(idle,d.ordinal()*256,row*128,null);c.drawBitmap(walk,d.ordinal()*256+128,row*128,null);
      if(row==2&&(d==CharacterRenderer.Direction.NW||d==CharacterRenderer.Direction.NE))for(String pose:new String[]{"idle","walk","attack"}){
        Bitmap b=(Bitmap)load.invoke(r,PoteForestMonsterShowcase.assetPath(ids[row],pose,d));
        long xs=0;int count=0;
        for(int y=0;y<b.getHeight();y++)for(int x=0;x<b.getWidth();x++){
          int color=b.getPixel(x,y),red=(color>>16)&255,g=(color>>8)&255,blue=color&255;
          if((color>>>24)>128&&red>70&&red>g*1.8f&&red>blue*1.5f){xs+=x;count++;}
        }
        assertTrue("red head present",count>10);float head=xs/(float)count;
        assertTrue("head faces "+d+" "+pose+" centroid="+head,d==CharacterRenderer.Direction.NW?head<96:head>96);
      }
    }
    save(sheet,"all-ant-idle-walk-facing");
  }
  @Test public void enlargedBossIsSelectableAboveOldBodyAndKeepsCombatFootprint()throws Exception{
    RuntimeState s=new RuntimeState();s.enterCampaignMap(CampaignWorld.BOSS_D,false);
    RuntimeState.Monster m=s.monsters().get(0);assertEquals("POTE_MANTIS",PoteForestMonsterShowcase.species(m.id));
    assertEquals(180f,PoteForestMonsterShowcase.bodyHeight(m.id),0f);
    assertSame("large upper body is tappable",m,s.hitMonster(m.x,m.y-140f,1f));
    Method radius=RuntimeState.class.getDeclaredMethod("monsterCollisionRadius",RuntimeState.Monster.class);radius.setAccessible(true);
    assertEquals("combat footprint is unchanged",18f,(Float)radius.invoke(s,m),0f);
  }
  @Test public void actualBossMapShowsEnlargedBossWithPlayerForScale()throws Exception{
    GameView v=new GameView(RuntimeEnvironment.getApplication());v.layout(0,0,1536,864);
    Method enter=GameView.class.getDeclaredMethod("enterPoteField"),change=GameView.class.getDeclaredMethod("changeCampaignMap",String.class,boolean.class);
    enter.setAccessible(true);change.setAccessible(true);enter.invoke(v);change.invoke(v,CampaignWorld.BOSS_D,false);
    RuntimeState s=TownInteriorTest.field(v,"state");RuntimeState.Monster m=s.monsters().get(0);
    WorldRuntimeAdapter w=TownInteriorTest.field(v,"poteFieldAdapter");s.player().x=m.x-96;s.player().y=m.y-48;w.snapCameraToPlayer();
    Bitmap b=Bitmap.createBitmap(1536,864,Bitmap.Config.ARGB_8888);v.draw(new Canvas(b));save(b,"live-enlarged-boss");
  }
}
