package com.projectdark.mobile.world;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;

/** Milles terrain validation renderer: canonical grass field + two-lane diagonal roads + center landmark. */
public final class AdaptedMillesMapRenderer {
  public static final String STATUS="MILLES_V8_GRASS_TWO_LANE_DIAGONAL_ROADS";
  private static final float TILE_W=AdaptedMillesIsometricTileLayer.TILE_WIDTH;
  private static final float TILE_H=AdaptedMillesIsometricTileLayer.TILE_HEIGHT;
  private static final float SEAM_GUARD=1f;
  private final Paint outsidePaint=new Paint();
  private final Paint pixelPaint=new Paint();
  private final Map<String,Bitmap> bitmapCache=new LinkedHashMap<>();
  private final Map<String,Rect> opaqueBoundsCache=new LinkedHashMap<>();
  private final AssetManager assets;

  public AdaptedMillesMapRenderer(){
    configureFill(outsidePaint,0xff1f2a22);
    pixelPaint.setAntiAlias(false);pixelPaint.setFilterBitmap(false);pixelPaint.setDither(false);
    assets=findAssets();
  }

  public void draw(Canvas canvas,WorldRuntimeAdapter world){
    if(canvas==null||world==null)return;
    canvas.drawRect(0f,0f,canvas.getWidth(),canvas.getHeight(),outsidePaint);
    drawAuthoredTerrain(canvas,world);
    // No generated art. The repository has no fountain PNG yet, so use the existing well as the
    // central landmark placeholder for this layout/device pass. It is anchored to the exact plaza tile.
    drawFoot(canvas,world,"street/OBJ_well.png",
        AdaptedMillesIsometricTileLayer.PLAZA_CENTER_X,
        AdaptedMillesIsometricTileLayer.PLAZA_CENTER_Y,.50f);
  }

  private void drawAuthoredTerrain(Canvas canvas,WorldRuntimeAdapter world){
    for(AdaptedMillesIsometricTileLayer.Tile tile:world.map().tiles())
      drawTerrainTile(canvas,world,terrainFor(tile),tile.centerX,tile.centerY);
  }

  private static String terrainFor(AdaptedMillesIsometricTileLayer.Tile tile){
    boolean road=tile.kind==AdaptedMillesIsometricTileLayer.TileKind.ROAD||
        tile.kind==AdaptedMillesIsometricTileLayer.TileKind.PLAZA||
        tile.kind==AdaptedMillesIsometricTileLayer.TileKind.GATE;
    boolean alt=(tile.variant&1)==1;
    if(road)return alt?"terrain/OBJ_stone_02.png":"terrain/OBJ_stone_01.png";
    return alt?"terrain/OBJ_ground_02.png":"terrain/OBJ_ground_01.png";
  }

  private void drawTerrainTile(Canvas canvas,WorldRuntimeAdapter world,String path,float wx,float wy){
    Bitmap bitmap=bitmap(path);if(bitmap==null)return;
    Rect src=opaqueBounds(path,bitmap);if(src==null||src.width()<=0||src.height()<=0)return;
    WorldCameraTransform.Point p=world.worldToScreen(wx,wy);
    float hw=TILE_W*.5f+SEAM_GUARD,hh=TILE_H*.5f+SEAM_GUARD;
    RectF dst=new RectF((float)Math.floor(p.x-hw),(float)Math.floor(p.y-hh),
        (float)Math.ceil(p.x+hw),(float)Math.ceil(p.y+hh));
    if(dst.right<0f||dst.left>canvas.getWidth()||dst.bottom<0f||dst.top>canvas.getHeight())return;
    canvas.drawBitmap(bitmap,src,dst,pixelPaint);
  }

  private Rect opaqueBounds(String path,Bitmap bitmap){
    if(opaqueBoundsCache.containsKey(path))return opaqueBoundsCache.get(path);
    int minX=bitmap.getWidth(),minY=bitmap.getHeight(),maxX=-1,maxY=-1;
    for(int y=0;y<bitmap.getHeight();y++)for(int x=0;x<bitmap.getWidth();x++){
      if((bitmap.getPixel(x,y)>>>24)==0)continue;
      if(x<minX)minX=x;if(x>maxX)maxX=x;if(y<minY)minY=y;if(y>maxY)maxY=y;
    }
    Rect result=maxX>=minX&&maxY>=minY?new Rect(minX,minY,maxX+1,maxY+1):null;
    opaqueBoundsCache.put(path,result);return result;
  }

  private void drawFoot(Canvas canvas,WorldRuntimeAdapter world,String path,float wx,float wy,float scale){
    Bitmap bitmap=bitmap(path);if(bitmap==null)return;
    WorldCameraTransform.Point p=world.worldToScreen(wx,wy);
    float w=bitmap.getWidth()*scale,h=bitmap.getHeight()*scale;
    RectF dst=new RectF(Math.round(p.x-w*.5f),Math.round(p.y-h),Math.round(p.x+w*.5f),Math.round(p.y));
    if(dst.right<0f||dst.left>canvas.getWidth()||dst.bottom<0f||dst.top>canvas.getHeight())return;
    canvas.drawBitmap(bitmap,null,dst,pixelPaint);
  }

  private Bitmap bitmap(String path){
    if(bitmapCache.containsKey(path))return bitmapCache.get(path);
    Bitmap bitmap=null;
    if(assets!=null)try(InputStream in=assets.open(path)){
      BitmapFactory.Options options=new BitmapFactory.Options();options.inScaled=false;
      bitmap=BitmapFactory.decodeStream(in,null,options);
    }catch(Throwable ignored){}
    bitmapCache.put(path,bitmap);return bitmap;
  }

  private static AssetManager findAssets(){
    try{Class<?> c=Class.forName("android.app.ActivityThread");Method m=c.getDeclaredMethod("currentApplication");
      Object app=m.invoke(null);return app instanceof Context?((Context)app).getAssets():null;
    }catch(Throwable ignored){return null;}
  }
  private static void configureFill(Paint paint,int color){
    paint.setAntiAlias(false);paint.setDither(false);paint.setColor(color);paint.setStyle(Paint.Style.FILL);
  }
}
