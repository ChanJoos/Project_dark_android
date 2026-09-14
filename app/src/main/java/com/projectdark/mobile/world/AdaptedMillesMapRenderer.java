package com.projectdark.mobile.world;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import com.projectdark.mobile.WorldDef;

/**
 * Milles floor-foundation renderer.
 *
 * This pass intentionally removes the old "asset cluster on a tiled test board" presentation.
 * Buildings, lake, fences, trees and street props are not rendered here yet. The renderer now owns
 * only the continuous world-space floor so camera movement can be validated before vertical content
 * is reintroduced.
 *
 * Geometry remains [ADAPTED/B] until verified Milles source geometry is calibrated. The important
 * invariant for this pass is that floor coverage follows the actual world bounds and moves with the
 * camera; it must never be a screen-sized backdrop and must never expose logical 64x32 navigation
 * cells as visible checker/diamond tiles.
 */
public final class AdaptedMillesMapRenderer {
  public static final String STATUS="MILLES_FLOOR_FOUNDATION_V1_WORLD_SPACE";

  private final Paint outsidePaint=new Paint();
  private final Paint grassPaint=new Paint();
  private final Paint roadPaint=new Paint();
  private final Paint plazaPaint=new Paint();
  private final Paint gatePaint=new Paint();

  public AdaptedMillesMapRenderer(){
    configure(outsidePaint,0xff1f2a22);
    // [ADAPTED] presentation colors only; not claimed as original Milles palette.
    configure(grassPaint,0xff66884d);
    configure(roadPaint,0xffbca77b);
    configure(plazaPaint,0xffc8b58b);
    configure(gatePaint,0xffae986f);
  }

  public void draw(Canvas canvas,WorldRuntimeAdapter world){
    if(canvas==null||world==null)return;

    // Outside the playable map is deliberately distinct. This makes camera/bounds defects visible
    // instead of hiding them behind a screen-sized grass fill.
    canvas.drawRect(0f,0f,canvas.getWidth(),canvas.getHeight(),outsidePaint);

    // One continuous ground slab in WORLD SPACE. Camera scrolling therefore reveals different parts
    // of the same map instead of repainting the viewport as if the world were attached to the screen.
    drawWorldRect(canvas,world,WorldDef.MIN_X,WorldDef.MIN_Y,WorldDef.MAX_X,WorldDef.MAX_Y,grassPaint);

    // Surface composition is also world-space and continuous. No per-navigation-cell diamonds.
    // GROUND is already covered by the base slab; only semantic road/plaza/gate surfaces overlay it.
    for(AdaptedMillesMapLayer.Surface surface:AdaptedMillesMapLayer.surfaces()){
      switch(surface.kind){
        case ROAD:
          drawWorldRect(canvas,world,surface.left,surface.top,surface.right,surface.bottom,roadPaint);
          break;
        case PLAZA:
          drawWorldRect(canvas,world,surface.left,surface.top,surface.right,surface.bottom,plazaPaint);
          break;
        case GATE:
          drawWorldRect(canvas,world,surface.left,surface.top,surface.right,surface.bottom,gatePaint);
          break;
        case GROUND:
        default:
          break;
      }
    }
  }

  private static void drawWorldRect(Canvas canvas,WorldRuntimeAdapter world,
      float left,float top,float right,float bottom,Paint paint){
    WorldCameraTransform.Point p0=world.worldToScreen(left,top);
    WorldCameraTransform.Point p1=world.worldToScreen(right,bottom);
    RectF dst=new RectF(Math.min(p0.x,p1.x),Math.min(p0.y,p1.y),Math.max(p0.x,p1.x),Math.max(p0.y,p1.y));
    if(dst.right<0f||dst.left>canvas.getWidth()||dst.bottom<0f||dst.top>canvas.getHeight())return;
    canvas.drawRect(dst,paint);
  }

  private static void configure(Paint paint,int color){
    paint.setAntiAlias(false);
    paint.setDither(false);
    paint.setColor(color);
    paint.setStyle(Paint.Style.FILL);
  }
}
