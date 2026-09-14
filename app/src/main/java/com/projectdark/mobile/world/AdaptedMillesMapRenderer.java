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
 * Visible village circulation, waterside terrain, landmark footprints and crossings are stable
 * world-space surfaces rather than repeated navigation cells or screen-fixed decoration.
 */
public final class AdaptedMillesMapRenderer {
  public static final String STATUS="MILLES_FLOOR_FOUNDATION_V4_DISTRICT_LANDMARK_FOOTPRINTS";

  private final Paint outsidePaint=new Paint();
  private final Paint grassPaint=new Paint();
  private final Paint serviceGardenPaint=new Paint();
  private final Paint craftYardPaint=new Paint();
  private final Paint churchQuietPaint=new Paint();
  private final Paint marketCommonsPaint=new Paint();
  private final Paint wetBankPaint=new Paint();
  private final Paint waterPaint=new Paint();
  private final Paint primaryRoadPaint=new Paint();
  private final Paint secondaryRoadPaint=new Paint();
  private final Paint quietRoadPaint=new Paint();
  private final Paint plazaPaint=new Paint();
  private final Paint gatePaint=new Paint();
  private final Paint crossingUnderPaint=new Paint();
  private final Paint crossingPaint=new Paint();

  public AdaptedMillesMapRenderer(){
    configureFill(outsidePaint,0xff1f2a22);
    // [ADAPTED] presentation colors only; not claimed as original Milles palette.
    configureFill(grassPaint,0xff66884d);
    configureFill(serviceGardenPaint,0xff718e57);
    configureFill(craftYardPaint,0xff827b5b);
    configureFill(churchQuietPaint,0xff9c9878);
    configureFill(marketCommonsPaint,0xff7b8457);
    configureFill(wetBankPaint,0xff536f49);
    configureFill(waterPaint,0xff486f76);
    configureRoute(primaryRoadPaint,0xffb8a276);
    configureRoute(secondaryRoadPaint,0xffad9a72);
    configureRoute(quietRoadPaint,0xff9e9270);
    configureFill(plazaPaint,0xffc8b58b);
    configureFill(gatePaint,0xffae986f);
    configureRoute(crossingUnderPaint,0xff5c4936);
    configureRoute(crossingPaint,0xffa98c61);
  }

  public void draw(Canvas canvas,WorldRuntimeAdapter world){
    if(canvas==null||world==null)return;

    // Outside the playable map is deliberately distinct. This makes camera/bounds defects visible
    // instead of hiding them behind a screen-sized grass fill.
    canvas.drawRect(0f,0f,canvas.getWidth(),canvas.getHeight(),outsidePaint);

    // One continuous ground slab in WORLD SPACE. Camera scrolling reveals different parts of the
    // same village instead of repainting a viewport-fixed backdrop.
    drawWorldRect(canvas,world,WorldDef.MIN_X,WorldDef.MIN_Y,WorldDef.MAX_X,WorldDef.MAX_Y,grassPaint);

    // District pads reserve stable landmark breathing room before vertical assets are reintroduced.
    // Their irregular polygons create readable district transitions without exposing navigation cells.
    for(AdaptedMillesMapLayer.DistrictPad pad:AdaptedMillesMapLayer.districtPads()){
      Paint paint;
      switch(pad.kind){
        case CHURCH_QUIET:
        case CIVIC_STONE:
          paint=churchQuietPaint;
          break;
        case CRAFT_YARD:
          paint=craftYardPaint;
          break;
        case MARKET_COMMONS:
          paint=marketCommonsPaint;
          break;
        case SERVICE_GARDEN:
        default:
          paint=serviceGardenPaint;
          break;
      }
      drawWorldPolygon(canvas,world,pad.points,paint);
    }

    // Water is a floor-layer feature, so bank/wet transition and water are drawn before routes and
    // crossings. The wider bank polygon prevents a hard water-to-default-grass cut.
    for(AdaptedMillesMapLayer.WaterBody water:AdaptedMillesMapLayer.waterBodies()){
      drawWorldPolygon(canvas,world,water.bankPoints,wetBankPaint);
      drawWorldPolygon(canvas,world,water.waterPoints,waterPaint);
    }

    // Draw circulation over district ground/wet terrain. Logical ROAD rectangles remain classification-only.
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

    // Plaza/gate remain broad semantic surfaces within this floor pass.
    for(AdaptedMillesMapLayer.Surface surface:AdaptedMillesMapLayer.surfaces()){
      if(surface.kind==AdaptedMillesMapLayer.SurfaceKind.PLAZA){
        drawWorldRect(canvas,world,surface.left,surface.top,surface.right,surface.bottom,plazaPaint);
      }else if(surface.kind==AdaptedMillesMapLayer.SurfaceKind.GATE){
        drawWorldRect(canvas,world,surface.left,surface.top,surface.right,surface.bottom,gatePaint);
      }
    }

    // Crossing is intentionally floor-layer presentation only for now. A dark under-stroke makes
    // the bridge/causeway edge readable while preserving the explicit phase-9 collision deferral.
    for(AdaptedMillesMapLayer.Crossing crossing:AdaptedMillesMapLayer.crossings()){
      drawWorldLine(canvas,world,crossing.points,crossing.width+14f,crossingUnderPaint);
      drawWorldLine(canvas,world,crossing.points,crossing.width,crossingPaint);
    }
  }

  private static void drawWorldRoute(Canvas canvas,WorldRuntimeAdapter world,
      AdaptedMillesMapLayer.Route route,Paint paint){
    drawWorldLine(canvas,world,route.points,route.width,paint);
  }

  private static void drawWorldLine(Canvas canvas,WorldRuntimeAdapter world,
      float[] points,float width,Paint paint){
    Path path=new Path();
    boolean started=false;
    for(int i=0;i<points.length;i+=2){
      WorldCameraTransform.Point p=world.worldToScreen(points[i],points[i+1]);
      if(!started){path.moveTo(p.x,p.y);started=true;}else path.lineTo(p.x,p.y);
    }
    paint.setStrokeWidth(width);
    canvas.drawPath(path,paint);
  }

  private static void drawWorldPolygon(Canvas canvas,WorldRuntimeAdapter world,float[] points,Paint paint){
    if(points==null||points.length<6)return;
    Path path=new Path();
    for(int i=0;i<points.length;i+=2){
      WorldCameraTransform.Point p=world.worldToScreen(points[i],points[i+1]);
      if(i==0)path.moveTo(p.x,p.y);else path.lineTo(p.x,p.y);
    }
    path.close();
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
