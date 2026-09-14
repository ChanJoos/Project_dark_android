package com.projectdark.mobile.world;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import com.projectdark.mobile.WorldDef;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Milles floor + first audited vertical-asset pass.
 *
 * The floor is world-space, grass-first and deliberately independent of the navigation-cell grid.
 * Visible circulation, waterside terrain, landmark footprints and selected production sprites use
 * stable absolute world coordinates. Production sprites are candidate source assets and remain
 * pending exact-device visual acceptance.
 */
public final class AdaptedMillesMapRenderer {
  public static final String STATUS="MILLES_V5_STABLE_VERTICAL_ASSET_REINTRODUCTION";

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
  private final Paint pixelPaint=new Paint();
  private final Map<String,Bitmap> bitmapCache=new LinkedHashMap<>();
  private final AssetManager assets;

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
    pixelPaint.setAntiAlias(false);
    pixelPaint.setFilterBitmap(false);
    pixelPaint.setDither(false);
    assets=findAssets();
  }

  public void draw(Canvas canvas,WorldRuntimeAdapter world){
    if(canvas==null||world==null)return;

    canvas.drawRect(0f,0f,canvas.getWidth(),canvas.getHeight(),outsidePaint);
    drawWorldRect(canvas,world,WorldDef.MIN_X,WorldDef.MIN_Y,WorldDef.MAX_X,WorldDef.MAX_Y,grassPaint);

    for(AdaptedMillesMapLayer.DistrictPad pad:AdaptedMillesMapLayer.districtPads()){
      Paint paint;
      switch(pad.kind){
        case CHURCH_QUIET:
        case CIVIC_STONE: paint=churchQuietPaint; break;
        case CRAFT_YARD: paint=craftYardPaint; break;
        case MARKET_COMMONS: paint=marketCommonsPaint; break;
        case SERVICE_GARDEN:
        default: paint=serviceGardenPaint; break;
      }
      drawWorldPolygon(canvas,world,pad.points,paint);
    }

    for(AdaptedMillesMapLayer.WaterBody water:AdaptedMillesMapLayer.waterBodies()){
      drawWorldPolygon(canvas,world,water.bankPoints,wetBankPaint);
      drawWorldPolygon(canvas,world,water.waterPoints,waterPaint);
    }

    for(AdaptedMillesMapLayer.Route route:AdaptedMillesMapLayer.routes()){
      switch(route.kind){
        case PRIMARY: drawWorldRoute(canvas,world,route,primaryRoadPaint); break;
        case SECONDARY: drawWorldRoute(canvas,world,route,secondaryRoadPaint); break;
        case QUIET:
        default: drawWorldRoute(canvas,world,route,quietRoadPaint); break;
      }
    }

    for(AdaptedMillesMapLayer.Surface surface:AdaptedMillesMapLayer.surfaces()){
      if(surface.kind==AdaptedMillesMapLayer.SurfaceKind.PLAZA){
        drawWorldRect(canvas,world,surface.left,surface.top,surface.right,surface.bottom,plazaPaint);
      }else if(surface.kind==AdaptedMillesMapLayer.SurfaceKind.GATE){
        drawWorldRect(canvas,world,surface.left,surface.top,surface.right,surface.bottom,gatePaint);
      }
    }

    for(AdaptedMillesMapLayer.Crossing crossing:AdaptedMillesMapLayer.crossings()){
      drawWorldLine(canvas,world,crossing.points,crossing.width+14f,crossingUnderPaint);
      drawWorldLine(canvas,world,crossing.points,crossing.width,crossingPaint);
    }

    // First vertical-asset pass. These assets were already present in assets/milles/production and
    // passed repository visual-isolation QA; their district placement/scale is [ADAPTED] pending
    // exact-device evidence. Critically, placement is absolute world-space, never spawn-relative.
    // Draw roughly back-to-front by ground-contact Y.
    drawFoot(canvas,world,"buildings/BLD_006_inn.png",665f,340f,.46f);
    drawFoot(canvas,world,"buildings/BLD_002_potion_shop.png",875f,390f,.43f);
    drawFoot(canvas,world,"landmarks/BLD_011_church.png",1370f,605f,.56f);
    drawFoot(canvas,world,"buildings/BLD_003_weapon_shop.png",260f,690f,.44f);
    drawFoot(canvas,world,"buildings/BLD_004_armor_shop.png",445f,760f,.42f);
    drawFoot(canvas,world,"buildings/BLD_005_general_shop.png",780f,1035f,.43f);
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

  /** Bottom-center ground anchor; source pixels remain nearest-neighbour rendered. */
  private void drawFoot(Canvas canvas,WorldRuntimeAdapter world,String path,float wx,float wy,float scale){
    Bitmap bitmap=bitmap(path);
    if(bitmap==null)return;
    WorldCameraTransform.Point p=world.worldToScreen(wx,wy);
    float w=bitmap.getWidth()*scale;
    float h=bitmap.getHeight()*scale;
    RectF dst=new RectF(Math.round(p.x-w*.5f),Math.round(p.y-h),Math.round(p.x+w*.5f),Math.round(p.y));
    if(dst.right<0f||dst.left>canvas.getWidth()||dst.bottom<0f||dst.top>canvas.getHeight())return;
    canvas.drawBitmap(bitmap,null,dst,pixelPaint);
  }

  private Bitmap bitmap(String path){
    if(bitmapCache.containsKey(path))return bitmapCache.get(path);
    Bitmap bitmap=null;
    if(assets!=null){
      try(InputStream in=assets.open(path)){
        BitmapFactory.Options options=new BitmapFactory.Options();
        options.inScaled=false;
        bitmap=BitmapFactory.decodeStream(in,null,options);
      }catch(Throwable ignored){}
    }
    bitmapCache.put(path,bitmap);
    return bitmap;
  }

  private static AssetManager findAssets(){
    try{
      Class<?> activityThread=Class.forName("android.app.ActivityThread");
      Method currentApplication=activityThread.getDeclaredMethod("currentApplication");
      Object app=currentApplication.invoke(null);
      return app instanceof Context?((Context)app).getAssets():null;
    }catch(Throwable ignored){return null;}
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
