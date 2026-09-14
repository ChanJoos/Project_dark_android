package com.projectdark.mobile.world;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Milles authored-terrain renderer.
 *
 * Visible terrain is rendered from the production Milles tile art, but its centers are NEVER
 * generated independently. The exact authored tile centers consumed by WorldRuntimeAdapter /
 * WorldMoveTargetController are also the centers used to draw the floor. This preserves the core
 * contract: player feet sit on a tile center and every NW/NE/SW/SE step is exactly one 64x32
 * adjacent tile.
 *
 * Production terrain PNGs were isolated with transparent safety margins. Those margins must not
 * participate in tiling or they create visible gaps even when destination rectangles overlap.
 * Terrain drawing therefore crops each bitmap to its real non-transparent bounds once, caches that
 * crop, and draws the cropped diamond with a small destination overlap guard.
 */
public final class AdaptedMillesMapRenderer {
  public static final String STATUS="MILLES_V7_CANONICAL_TILE_CENTERS_ALPHA_CROPPED";
  private static final float TILE_W=AdaptedMillesIsometricTileLayer.TILE_WIDTH;
  private static final float TILE_H=AdaptedMillesIsometricTileLayer.TILE_HEIGHT;
  private static final float SEAM_GUARD=1f;

  private final Paint outsidePaint=new Paint();
  private final Paint wetBankPaint=new Paint();
  private final Paint waterPaint=new Paint();
  private final Paint crossingUnderPaint=new Paint();
  private final Paint crossingPaint=new Paint();
  private final Paint pixelPaint=new Paint();
  private final Map<String,Bitmap> bitmapCache=new LinkedHashMap<>();
  private final Map<String,Rect> opaqueBoundsCache=new LinkedHashMap<>();
  private final AssetManager assets;

  public AdaptedMillesMapRenderer(){
    configureFill(outsidePaint,0xff1f2a22);
    configureFill(wetBankPaint,0xff536f49);
    configureFill(waterPaint,0xff486f76);
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

    // CRITICAL: draw the exact same authored tile objects that navigation consumes. No second grid.
    drawAuthoredTerrain(canvas,world);

    for(AdaptedMillesMapLayer.WaterBody water:AdaptedMillesMapLayer.waterBodies()){
      drawWorldPolygon(canvas,world,water.bankPoints,wetBankPaint);
      drawWorldPolygon(canvas,world,water.waterPoints,waterPaint);
    }

    for(AdaptedMillesMapLayer.Crossing crossing:AdaptedMillesMapLayer.crossings()){
      drawWorldLine(canvas,world,crossing.points,crossing.width+14f,crossingUnderPaint);
      drawWorldLine(canvas,world,crossing.points,crossing.width,crossingPaint);
    }

    // Stable vertical-asset placements from the current Milles rebuild line.
    drawFoot(canvas,world,"buildings/BLD_006_inn.png",665f,340f,.46f);
    drawFoot(canvas,world,"buildings/BLD_002_potion_shop.png",875f,390f,.43f);
    drawFoot(canvas,world,"landmarks/BLD_011_church.png",1370f,605f,.56f);
    drawFoot(canvas,world,"buildings/BLD_003_weapon_shop.png",260f,690f,.44f);
    drawFoot(canvas,world,"buildings/BLD_004_armor_shop.png",445f,760f,.42f);
    drawFoot(canvas,world,"buildings/BLD_005_general_shop.png",780f,1035f,.43f);
  }

  private void drawAuthoredTerrain(Canvas canvas,WorldRuntimeAdapter world){
    for(AdaptedMillesIsometricTileLayer.Tile tile:world.map().tiles()){
      String path=terrainFor(tile);
      drawTerrainTile(canvas,world,path,tile.centerX,tile.centerY);
    }
  }

  private static String terrainFor(AdaptedMillesIsometricTileLayer.Tile tile){
    boolean stone=tile.kind==AdaptedMillesIsometricTileLayer.TileKind.ROAD||
        tile.kind==AdaptedMillesIsometricTileLayer.TileKind.PLAZA||
        tile.kind==AdaptedMillesIsometricTileLayer.TileKind.GATE;
    boolean alt=(tile.variant&1)==1;
    if(stone)return alt?"terrain/OBJ_stone_02.png":"terrain/OBJ_stone_01.png";
    return alt?"terrain/OBJ_ground_02.png":"terrain/OBJ_ground_01.png";
  }

  /**
   * Alpha-crop the source safety margin, then place the actual diamond on the canonical 64x32
   * footprint. A one-pixel destination overlap is intentional: it closes raster rounding seams,
   * while the logical/navigation centers remain unchanged.
   */
  private void drawTerrainTile(Canvas canvas,WorldRuntimeAdapter world,String path,float wx,float wy){
    Bitmap bitmap=bitmap(path);
    if(bitmap==null)return;
    Rect src=opaqueBounds(path,bitmap);
    if(src==null||src.width()<=0||src.height()<=0)return;

    WorldCameraTransform.Point p=world.worldToScreen(wx,wy);
    float hw=TILE_W*.5f+SEAM_GUARD;
    float hh=TILE_H*.5f+SEAM_GUARD;
    RectF dst=new RectF(
        (float)Math.floor(p.x-hw),(float)Math.floor(p.y-hh),
        (float)Math.ceil(p.x+hw),(float)Math.ceil(p.y+hh));
    if(dst.right<0f||dst.left>canvas.getWidth()||dst.bottom<0f||dst.top>canvas.getHeight())return;
    canvas.drawBitmap(bitmap,src,dst,pixelPaint);
  }

  private Rect opaqueBounds(String path,Bitmap bitmap){
    if(opaqueBoundsCache.containsKey(path))return opaqueBoundsCache.get(path);
    int minX=bitmap.getWidth(),minY=bitmap.getHeight(),maxX=-1,maxY=-1;
    for(int y=0;y<bitmap.getHeight();y++){
      for(int x=0;x<bitmap.getWidth();x++){
        if((bitmap.getPixel(x,y)>>>24)==0)continue;
        if(x<minX)minX=x;if(x>maxX)maxX=x;
        if(y<minY)minY=y;if(y>maxY)maxY=y;
      }
    }
    Rect result=maxX>=minX&&maxY>=minY?new Rect(minX,minY,maxX+1,maxY+1):null;
    opaqueBoundsCache.put(path,result);
    return result;
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
