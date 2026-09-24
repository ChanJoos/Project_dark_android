package com.projectdark.mobile.world;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import com.projectdark.mobile.WorldDef;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;

/** Renders the recording-derived Milles garden scene over the existing navigable world grid. */
public final class AdaptedMillesMapRenderer {
  public static final String STATUS="MILLES_VIDEO_REFERENCE_GARDEN_SCENE";
  public static final float B1_VISIBLE_BUILDING_SCALE=1.0f; // Production PNGs are already native large assets; do not raster-upscale.
  private static final float TILE_W=AdaptedMillesIsometricTileLayer.TILE_WIDTH,TILE_H=AdaptedMillesIsometricTileLayer.TILE_HEIGHT;
  private static final float SEAM_GUARD=1f;
  private static final String[] GRASS_TILES={"video_reference/terrain/grass_tile_01.png","video_reference/terrain/grass_tile_02.png","video_reference/terrain/grass_tile_03.png"};
  private static final String GARDEN_SCENE="video_reference/scenes/garden_route_scene.webp";
  private final Paint outsidePaint=new Paint(),pixelPaint=new Paint();
  private final Map<String,Bitmap> bitmapCache=new LinkedHashMap<>();
  private final Map<String,Rect> opaqueBoundsCache=new LinkedHashMap<>();
  private final AssetManager assets;
  public AdaptedMillesMapRenderer(){configureFill(outsidePaint,0xff4f6928);pixelPaint.setAntiAlias(false);pixelPaint.setFilterBitmap(false);pixelPaint.setDither(false);assets=findAssets();}

  public void draw(Canvas canvas,WorldRuntimeAdapter world){
    if(canvas==null||world==null)return;
    canvas.drawRect(0,0,canvas.getWidth(),canvas.getHeight(),outsidePaint);
    for(AdaptedMillesIsometricTileLayer.Tile t:world.map().tiles()){
      if(t.kind==AdaptedMillesIsometricTileLayer.TileKind.PLAZA)continue;
      drawTerrainTile(canvas,world,GRASS_TILES[Math.floorMod(t.variant,GRASS_TILES.length)],t.centerX,t.centerY);
    }
    drawGardenRouteScene(canvas,world);
  }

  /** A recorded viewport is one compact scene slice; map movement and collision remain independent. */
  private void drawGardenRouteScene(Canvas c,WorldRuntimeAdapter w){
    Bitmap scene=bitmap(GARDEN_SCENE);if(scene==null)return;
    float left=WorldDef.PLAYER_SPAWN_X-w.camera().anchorX();
    float top=WorldDef.PLAYER_SPAWN_Y-w.camera().anchorY();
    WorldCameraTransform.Point p=w.worldToScreen(left,top);
    RectF dst=new RectF(p.x,p.y,p.x+w.camera().viewportWidth(),p.y+w.camera().viewportHeight());
    c.drawBitmap(scene,null,dst,pixelPaint);
  }

  private void drawTerrainTile(Canvas c,WorldRuntimeAdapter w,String path,float wx,float wy){
    WorldCameraTransform.Point p=w.worldToScreen(wx,wy);float hw=TILE_W*.5f+SEAM_GUARD,hh=TILE_H*.5f+SEAM_GUARD;
    RectF dst=new RectF((float)Math.floor(p.x-hw),(float)Math.floor(p.y-hh),(float)Math.ceil(p.x+hw),(float)Math.ceil(p.y+hh));
    if(dst.right<0||dst.left>c.getWidth()||dst.bottom<0||dst.top>c.getHeight())return;
    Bitmap b=bitmap(path);if(b==null)return;Rect src=opaqueBounds(path,b);if(src==null)return;c.drawBitmap(b,src,dst,pixelPaint);
  }
  private Rect opaqueBounds(String path,Bitmap b){if(opaqueBoundsCache.containsKey(path))return opaqueBoundsCache.get(path);int minX=b.getWidth(),minY=b.getHeight(),maxX=-1,maxY=-1;for(int y=0;y<b.getHeight();y++)for(int x=0;x<b.getWidth();x++)if((b.getPixel(x,y)>>>24)!=0){if(x<minX)minX=x;if(x>maxX)maxX=x;if(y<minY)minY=y;if(y>maxY)maxY=y;}Rect r=maxX>=minX?new Rect(minX,minY,maxX+1,maxY+1):null;opaqueBoundsCache.put(path,r);return r;}
  private Bitmap bitmap(String path){if(bitmapCache.containsKey(path))return bitmapCache.get(path);Bitmap b=null;if(assets!=null)try(InputStream in=assets.open(path)){BitmapFactory.Options o=new BitmapFactory.Options();o.inScaled=false;b=BitmapFactory.decodeStream(in,null,o);}catch(Throwable ignored){}bitmapCache.put(path,b);return b;}
  private static AssetManager findAssets(){try{Class<?> c=Class.forName("android.app.ActivityThread");Method m=c.getDeclaredMethod("currentApplication");Object a=m.invoke(null);return a instanceof Context?((Context)a).getAssets():null;}catch(Throwable ignored){return null;}}
  private static void configureFill(Paint p,int color){p.setAntiAlias(false);p.setDither(false);p.setColor(color);p.setStyle(Paint.Style.FILL);}
}
