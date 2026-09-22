package com.projectdark.mobile;

import static org.junit.Assert.*;
import android.graphics.*;
import java.io.File;
import java.io.FileOutputStream;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.GraphicsMode;

/** Draw the shipped source bitmaps through the real Android Canvas renderer, not mocked geometry. */
@RunWith(RobolectricTestRunner.class)
@Config(sdk=34,manifest=Config.NONE)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public class ShirtRenderingRegressionTest {
  private File root(){File f=new File("src/main/res/drawable-nodpi");return f.isDirectory()?f:new File("app/src/main/res/drawable-nodpi");}
  private Bitmap load(String name){Bitmap b=BitmapFactory.decodeFile(new File(root(),name+".webp").getPath());assertNotNull(name,b);return b;}
  private CharacterRenderer renderer(){Bitmap[] bodies=new Bitmap[4],robes=new Bitmap[4];for(int i=0;i<4;i++){bodies[i]=load("player_body_mm001_action02_"+i);robes[i]=load("player_robe_mu0000058_action02_"+i);}return new CharacterRenderer(load(CharacterRenderer.IDLE_WALK_RESOURCE),load(CharacterRenderer.STARTER_SHIRT_RESOURCE),load(CharacterRenderer.LUERS_ROBE_RESOURCE),load(CharacterRenderer.MOKDO_RESOURCE),bodies,robes);}
  private Bitmap draw(CharacterRenderer r,CharacterRenderer.Direction d,CharacterRenderer.State state,float clock,String garment){Bitmap b=Bitmap.createBitmap(80,88,Bitmap.Config.ARGB_8888);r.draw(new Canvas(b),new CharacterRenderer.Pose(40,76,d,state,0,clock,1,false,garment,"mw001",null,CharacterRenderer.EffectFamily.NONE,AnimationAction.SWING));return b;}
  private int differences(Bitmap a,Bitmap b){int n=0;for(int y=0;y<a.getHeight();y++)for(int x=0;x<a.getWidth();x++)if(a.getPixel(x,y)!=b.getPixel(x,y))n++;return n;}
  @Test public void shirtStaysAttachedAcrossEveryWalkAndContactFrame()throws Exception{
    CharacterRenderer renderer=renderer();assertTrue(renderer.sourceActionActive());
    Bitmap sheet=Bitmap.createBitmap(8*80,4*88,Bitmap.Config.ARGB_8888);Canvas canvas=new Canvas(sheet);canvas.drawColor(0xff656565);
    for(CharacterRenderer.Direction d:CharacterRenderer.Direction.values()){
      Bitmap idle=null;
      for(int col=0;col<8;col++){
        CharacterRenderer.setPresentationWalkClock(Math.max(0,col-1)*CharacterRenderer.WALK_FRAME_SECONDS);
        CharacterRenderer.State state=col==0?CharacterRenderer.State.IDLE:col<5?CharacterRenderer.State.WALK:col==7?CharacterRenderer.State.CAST:CharacterRenderer.State.ATTACK;
        Bitmap dressed=draw(renderer,d,state,col==5?.4f:.9f,"mu0000001"),bare=draw(renderer,d,state,col==5?.4f:.9f,null);
        assertTrue(d+"/"+col+" garment missing",differences(dressed,bare)>20);
        int green=0,minY=88,maxY=0,minX=80,maxX=0;
        for(int y=0;y<88;y++)for(int x=0;x<80;x++){int p=dressed.getPixel(x,y);if(Color.alpha(p)>0&&Color.green(p)>Color.red(p)*1.3&&Color.green(p)>Color.blue(p)*1.3&&Color.green(p)>45){green++;minY=Math.min(minY,y);maxY=Math.max(maxY,y);minX=Math.min(minX,x);maxX=Math.max(maxX,x);}}
        assertTrue(d+"/"+col+" shirt is absent",green>12);
        assertTrue(d+"/"+col+" shirt detached vertically: "+minY+".."+maxY,minY>=30&&minY<50&&maxY<63);
        assertTrue(d+"/"+col+" shirt detached horizontally",minX>=22&&maxX<=58);
        if(col==0)idle=dressed;if(col==5)assertTrue("contact silently fell back to idle",differences(idle,dressed)>40);
        canvas.drawBitmap(dressed,col*80,d.ordinal()*88,null);
      }
      assertTrue("full body garment disappeared",differences(draw(renderer,d,CharacterRenderer.State.IDLE,0,"mu0000058"),draw(renderer,d,CharacterRenderer.State.IDLE,0,null))>30);
    }
    File output=new File("build/reports/device-review/character-matrix.png");output.getParentFile().mkdirs();try(FileOutputStream stream=new FileOutputStream(output)){sheet.compress(Bitmap.CompressFormat.PNG,100,stream);}
  }
}
