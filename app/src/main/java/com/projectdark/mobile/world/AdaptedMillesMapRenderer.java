package com.projectdark.mobile.world;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import com.projectdark.mobile.WorldDef;

/**
 * Milles floor-foundation renderer.
 *
 * The floor is world-space, grass-first and deliberately independent of the navigation-cell grid.
 * Visible village circulation is rendered from connected route polylines that converge on a central
 * civic square; this replaces the prior rectangular road strips that still read like a prototype
 * board when the camera moved.
 */
public final class AdaptedMillesMapRenderer {
  public static final String STATUS="MILLES_FLOOR_FOUNDATION_V2_ROAD_HIERARCHY";

  private final Paint outsidePaint=new Paint();
  private final Paint grassPaint=new Paint();
  private final Paint primaryRoadPaint=new Paint();
  private final Paint secondaryRoadPaint=new Paint();
  private final Paint quietRoadPaint=new Paint();
  private final Paint plazaPaint=new Paint();
  private final Paint gatePaint=new Paint();

  public AdaptedMillesMapRenderer(){
    configureFill(outsidePaint,0xff1f2a22);
    // [ADAPTED] presentation colors only; not claimed as original Milles palette.
    configureFill(grassPaint,0xff66884d);
    configureRoute(primaryRoadPaint,0xffb8a276);
    configureRoute(secondaryRoadPaint,0xffad9a72);
    configureRoute(quietRoadPaint,0xff9e9270);
    configureFill(plazaPaint,0xffc8b58b);
    configureFill(gatePaint,0xffae986f);
  }

  public void draw(Canvas canvas,WorldRuntimeAdapter world){
    if(canvas==null||world==null)return;

    // Outside the playable map is deliberately distinct. This makes camera/bounds defects visible
    // instead of hiding them behind a screen-sized grass fill.
    canvas.drawRect(0f,0f,canvas.getWidth(),canvas.getHeight(),outsidePaint);

    // One continuous ground slab in WORLD SPACE. Camera scrolling reveals different parts of the
    // same village instead of repainting a viewport-fixed backdrop.
    drawWorldRect(canvas,world,WorldDef.MIN_X,WorldDef.MIN_Y,WorldDef.MAX_X,WorldDef.MAX_Y,grassPaint);

    // Draw circulation first so every route visually terminates beneath the civic square rather than
    // producing stacked rectangle seams. Logical ROAD rectangles remain in MapLayer for navigation
    // classification, but they are not exposed as visible geometry.
    for(AdaptedMillesMapLayer.Route route:AdaptedMillesMapLayer.routes()){
      switch(route.kind){
        case PRIMARY:
          drawWorldRoute(canvas,world,route,primaryRoadPaint);
          break;
        case SECONDARY:
          drawWorldRoute(canvas,world,route,secondaryRoadPaint);
          break;
        case QUIET:
        default:
          drawWorldRoute(canvas,world,route,quietRoadPaint);
          break;
      }
    }

    // Plaza/gate are the only rectangular semantic surfaces deliberately rendered in this pass.
    // The square is a civic destination; the gate is a broad transition pad at the future field axis.
    for(AdaptedMillesMapLayer.Surface surface:AdaptedMillesMapLayer.surfaces()){
      if(surface.kind==AdaptedMillesMapLayer.SurfaceKind.PLAZA){
        drawWorldRect(canvas,world,surface.left,surface.top,surface.right,surface.bottom,plazaPaint);
      }else if(surface.kind==AdaptedMillesMapLayer.SurfaceKind.GATE){
        drawWorldRect(canvas,world,surface.left,surface.top,surface.right,surface.bottom,gatePaint);
      }
    }
  }

  private static void drawWorldRoute(Canvas canvas,WorldRuntimeAdapter world,
      AdaptedMillesMapLayer.Route route,Paint paint){
    Path path=new Path();
    boolean started=false;
    for(int i=0;i<route.points.length;i+=2){
      WorldCameraTransform.Point p=world.worldToScreen(route.points[i],route.points[i+1]);
      if(!started){path.moveTo(p.x,p.y);started=true;}else path.lineTo(p.x,p.y);
    }
    paint.setStrokeWidth(route.width);
    canvas.drawPath(path,paint);
  }

  private static void drawWorldRect(Canvas canvas,WorldRuntimeAdapter world,
      float left,float top,float right,float bottom,Paint paint){
    WorldCameraTransform.Point p0=world.worldToScreen(left,top);
    WorldCameraTransform.Point p1=world.worldToScreen(right,bottom);
    RectF dst=new RectF(Math.min(p0.x,p1.x),Math.min(p0.y,p1.y),Math.max(p0.x,p1.x),Math.max(p0.y,p1.y));
    if(dst.right<0f||dst.left>canvas.getWidth()||dst.bottom<0f||dst.top>canvas.getHeight())return;
    canvas.drawRect(dst,paint);
  }

  private static void configureFill(Paint paint,int color){
    paint.setAntiAlias(false);
    paint.setDither(false);
    paint.setColor(color);
    paint.setStyle(Paint.Style.FILL);
  }

  private static void configureRoute(Paint paint,int color){
    paint.setAntiAlias(false);
    paint.setDither(false);
    paint.setColor(color);
    paint.setStyle(Paint.Style.STROKE);
    paint.setStrokeCap(Paint.Cap.ROUND);
    paint.setStrokeJoin(Paint.Join.ROUND);
  }
}
