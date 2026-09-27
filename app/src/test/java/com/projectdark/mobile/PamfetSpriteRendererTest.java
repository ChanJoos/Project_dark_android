package com.projectdark.mobile;

import static org.junit.Assert.*;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.GraphicsMode;

@RunWith(RobolectricTestRunner.class)
@Config(sdk=34,manifest=Config.NONE)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public final class PamfetSpriteRendererTest {
  @Test public void allEightDraftSpritesLoadAndRenderByMotionAndDiagonal(){
    PamfetSpriteRenderer renderer=new PamfetSpriteRenderer(RuntimeEnvironment.getApplication());
    assertTrue("all 4 idle and 4 roll frames must decode from packaged assets",renderer.assetsReady());
    CharacterRenderer.Direction[] directions={CharacterRenderer.Direction.NW,CharacterRenderer.Direction.NE,
        CharacterRenderer.Direction.SW,CharacterRenderer.Direction.SE};
    Bitmap sheet=Bitmap.createBitmap(512,256,Bitmap.Config.ARGB_8888);
    Canvas canvas=new Canvas(sheet);
    for(int motion=0;motion<2;motion++)for(int direction=0;direction<4;direction++){
      Bitmap frame=renderer.frame(motion==1,directions[direction]);
      assertNotNull(frame);assertEquals(128,frame.getWidth());assertEquals(128,frame.getHeight());
      assertTrue(frame.hasAlpha());
      canvas.drawBitmap(frame,direction*128,(motion==0?0:128),null);
    }
    int visible=0;
    for(int y=0;y<sheet.getHeight();y++)for(int x=0;x<sheet.getWidth();x++)
      if(Color.alpha(sheet.getPixel(x,y))>0)visible++;
    assertTrue("contact sheet must contain visible pixels for the 8 runtime frame slots",visible>12000);
    assertEquals("monsters/pamfet/purple/idle_lower_left.png",PamfetSpriteRenderer.assetPath(false,2));
    assertEquals("monsters/pamfet/purple/attack_roll_lower_right.png",PamfetSpriteRenderer.assetPath(true,3));
    assertEquals("B/ADAPTED/PAMFET_DRAFT_V3",PamfetSpriteRenderer.ASSET_STATUS);
  }

  @Test public void onlyPurplePamfetUsesThisDraft(){
    PamfetSpriteRenderer renderer=new PamfetSpriteRenderer(RuntimeEnvironment.getApplication());
    assertTrue(renderer.canRender("POTE_PURPLE"));
    assertFalse(renderer.canRender("POTE_SILVER"));
    assertFalse(renderer.canRender("combat_dummy_01"));
  }
}
