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

/** Story-driven Milles pass: grass-dominant terrain, dirt roads, central well, and restrained landmarks. */
public final class AdaptedMillesMapRenderer {
  public static final String STATUS="MILLES_V10_LOCKED_GRASS_DIRT_MAPPING";
  private static final float TILE_W=AdaptedMillesIsometricTileLayer.TILE_WIDTH,TILE_H=AdaptedMillesIsometricTileLayer.TILE_HEIGHT;
  private static final float SEAM_GUARD=1f;
  private static final String GRASS_TILE="terrain/OBJ_ground_01.png";
  private static final String DIRT_TILE="terrain/OBJ_ground_02.png";
  private static final String PLAZA_TILE="terrain/OBJ_stone_01.png";
  private final Paint outsidePaint=new Paint(),pixelPaint=new Paint();
  private final Map<String,Bitmap> bitmapCache=new LinkedHashMap<>();
  private final Map<String,Rect> opaqueBoundsCache=new LinkedHashMap<>();
  private final AssetManager assets;
  public AdaptedMillesMapRenderer(){configureFill(outsidePaint,0xff1f2a22);pixelPaint.setAntiAlias(false);pixelPaint.setFilterBitmap(false);pixelPaint.setDither(false);assets=findAssets();}

  public void draw(Canvas canvas,WorldRuntimeAdapter world){
    if(canvas==null||world==null)return;
    canvas.drawRect(0,0,canvas.getWidth(),canvas.getHeight(),outsidePaint);
    for(AdaptedMillesIsometricTileLayer.Tile t:world.map().tiles())drawTerrainTile(canvas,world,terrainFor(t),t.centerX,t.centerY);
    drawFoot(canvas,world,"street/OBJ_well.png",AdaptedMillesIsometricTileLayer.PLAZA_CENTER_X,AdaptedMillesIsometricTileLayer.PLAZA_CENTER_Y,.50f);
    drawFoot(canvas,world,"landmarks/BLD_011_church.png",1320f,455f,.42f);
    drawFoot(canvas,world,"street/OBJ_noticeboard.png",690f,720f,.38f);
    drawFoot(canvas,world,"street/OBJ_bench.png",850f,650f,.38f);
    drawFoot(canvas,world,"street/OBJ_lamp_01.png",720f,535f,.42f);
    drawFoot(canvas,world,"street/OBJ_lamp_02.png",830f,535f,.42f);
  }

  /**
   * Visual contract for the current Milles foundation:
   * GROUND is always the authored green grass tile; ROAD/GATE is always the authored brown dirt tile;
   * PLAZA is always stone. Variants are intentionally disabled so semantic tile kinds can never
   * reintroduce the grass/dirt checkerboard regression.
   */
  private static String terrainFor(AdaptedMillesIsometricTileLayer.Tile t){
    if(t.kind==AdaptedMillesIsometricTileLayer.TileKind.ROAD||t.kind==AdaptedMillesIsometricTileLayer.TileKind.GATE)return DIRT_TILE;
    if(t.kind==AdaptedMillesIsometricTileLayer.TileKind.PLAZA)return PLAZA_TILE;
    return GRASS_TILE;
  }

  private void drawTerrainTile(Canvas c,WorldRuntimeAdapter w,String path,float wx,float wy){
    Bitmap b=bitmap(path);if(b==null)return;Rect src=opaqueBounds(path,b);if(src==null)return;
    WorldCameraTransform.Point p=w.worldToScreen(wx,wy);float hw=TILE_W*.5f+SEAM_GUARD,hh=TILE_H*.5f+SEAM_GUARD;
    RectF dst=new RectF((float)Math.floor(p.x-hw),(float)Math.floor(p.y-hh),(float)Math.ceil(p.x+hw),(float)Math.ceil(p.y+hh));
    if(dst.right<0||dst.left>c.getWidth()||dst.bottom<0||dst.top>c.getHeight())return;c.drawBitmap(b,src,dst,pixelPaint);
  }
  private Rect opaqueBounds(String path,Bitmap b){if(opaqueBoundsCache.containsKey(path))return opaqueBoundsCache.get(path);int minX=b.getWidth(),minY=b.getHeight(),maxX=-1,maxY=-1;for(int y=0;y<b.getHeight();y++)for(int x=0;x<b.getWidth();x++)if((b.getPixel(x,y)>>>24)!=0){if(x<minX)minX=x;if(x>maxX)maxX=x;if(y<minY)minY=y;if(y>maxY)maxY=y;}Rect r=maxX>=minX?new Rect(minX,minY,maxX+1,maxY+1):null;opaqueBoundsCache.put(path,r);return r;}
  private void drawFoot(Canvas c,WorldRuntimeAdapter w,String path,float wx,float wy,float scale){Bitmap b=bitmap(path);if(b==null)return;WorldCameraTransform.Point p=w.worldToScreen(wx,wy);float ww=b.getWidth()*scale,hh=b.getHeight()*scale;RectF d=new RectF(Math.round(p.x-ww*.5f),Math.round(p.y-hh),Math.round(p.x+ww*.5f),Math.round(p.y));if(d.right<0||d.left>c.getWidth()||d.bottom<0||d.top>c.getHeight())return;c.drawBitmap(b,null,d,pixelPaint);}
  private Bitmap bitmap(String path){if(bitmapCache.containsKey(path))return bitmapCache.get(path);Bitmap b=null;if(assets!=null)try(InputStream in=assets.open(path)){BitmapFactory.Options o=new BitmapFactory.Options();o.inScaled=false;b=BitmapFactory.decodeStream(in,null,o);}catch(Throwable ignored){}bitmapCache.put(path,b);return b;}
  private static AssetManager findAssets(){try{Class<?> c=Class.forName("android.app.ActivityThread");Method m=c.getDeclaredMethod("currentApplication");Object a=m.invoke(null);return a instanceof Context?((Context)a).getAssets():null;}catch(Throwable ignored){return null;}}
  private static void configureFill(Paint p,int color){p.setAntiAlias(false);p.setDither(false);p.setColor(color);p.setStyle(Paint.Style.FILL);}
}
