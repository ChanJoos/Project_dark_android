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
 * Milles authored-terrain renderer.
 *
 * The visible floor uses the production terrain art under assets/milles/production/terrain.
 * Logical 64x32 navigation geometry is NOT drawn as debug polygons. Instead, authored diamond
 * terrain tiles are laid on a 64x32 staggered lattice and expanded by a 1px seam guard on every
 * edge so camera movement / float rounding cannot expose gaps between adjacent tiles.
 */
public final class AdaptedMillesMapRenderer {
  public static final String STATUS="MILLES_V6_AUTHORED_TERRAIN_SEAM_SAFE";
  private static final float TILE_W=64f;
  private static final float TILE_H=32f;
  private static final float HALF_W=32f;
  private static final float HALF_H=16f;
  private static final float SEAM_GUARD=1f;

  private final Paint outsidePaint=new Paint();
  private final Paint wetBankPaint=new Paint();
  private final Paint waterPaint=new Paint();
  private final Paint crossingUnderPaint=new Paint();
  private final Paint crossingPaint=new Paint();
  private final Paint pixelPaint=new Paint();
  private final Map<String,Bitmap> bitmapCache=new LinkedHashMap<>();
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

    // Authored terrain first. No flat grass/road/plaza debug polygons are used for the visible floor.
    drawAuthoredTerrain(canvas,world);

    // Water remains an explicit world-space feature in this pass; authored water sprites are audited
    // separately. Bank/water geometry stays below vertical assets and above terrain.
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

  /**
   * Tile centers form the canonical staggered 64x32 lattice:
   * row height 16, alternating rows shifted by 32, horizontal repeat 64.
   * Destination size is 66x34 (1px guard each edge), deliberately overlapping immediate neighbors
   * enough to hide sub-pixel/rounding seams while preserving nearest-neighbour source pixels.
   */
  private void drawAuthoredTerrain(Canvas canvas,WorldRuntimeAdapter world){
    int firstRow=(int)Math.floor(WorldDef.MIN_Y/HALF_H)-2;
    int lastRow=(int)Math.ceil(WorldDef.MAX_Y/HALF_H)+2;
    int firstCol=(int)Math.floor(WorldDef.MIN_X/TILE_W)-2;
    int lastCol=(int)Math.ceil(WorldDef.MAX_X/TILE_W)+2;

    for(int row=firstRow;row<=lastRow;row++){
      float wy=row*HALF_H;
      float shift=((row&1)==0)?0f:HALF_W;
      for(int col=firstCol;col<=lastCol;col++){
        float wx=col*TILE_W+shift;
        if(wx<WorldDef.MIN_X-HALF_W||wx>WorldDef.MAX_X+HALF_W||
            wy<WorldDef.MIN_Y-HALF_H||wy>WorldDef.MAX_Y+HALF_H)continue;

        boolean stone=isStoneTerrain(wx,wy);
        // Deterministic visual variation without exposing navigation/debug state.
        boolean alt=((row*31+col*17)&3)==0;
        String path=stone
            ?(alt?"terrain/OBJ_stone_02.png":"terrain/OBJ_stone_01.png")
            :(alt?"terrain/OBJ_ground_02.png":"terrain/OBJ_ground_01.png");
        drawTerrainTile(canvas,world,path,wx,wy);
      }
    }
  }

  private boolean isStoneTerrain(float x,float y){
    for(AdaptedMillesMapLayer.Surface surface:AdaptedMillesMapLayer.surfaces()){
      if((surface.kind==AdaptedMillesMapLayer.SurfaceKind.PLAZA||
          surface.kind==AdaptedMillesMapLayer.SurfaceKind.GATE)&&
          x>=surface.left&&x<=surface.right&&y>=surface.top&&y<=surface.bottom){
        return true;
      }
    }
    for(AdaptedMillesMapLayer.Route route:AdaptedMillesMapLayer.routes()){
      float radius=route.width*.5f+6f;
      if(distanceToPolyline(x,y,route.points)<=radius)return true;
    }
    return false;
  }

  private static float distanceToPolyline(float x,float y,float[] points){
    if(points==null||points.length<4)return Float.MAX_VALUE;
    float best=Float.MAX_VALUE;
    for(int i=0;i+3<points.length;i+=2){
      float ax=points[i],ay=points[i+1],bx=points[i+2],by=points[i+3];
      float dx=bx-ax,dy=by-ay;
      float denom=dx*dx+dy*dy;
      float t=denom<=0f?0f:((x-ax)*dx+(y-ay)*dy)/denom;
      if(t<0f)t=0f;else if(t>1f)t=1f;
      float px=ax+t*dx,py=ay+t*dy;
      float ex=x-px,ey=y-py;
      float d=(float)Math.sqrt(ex*ex+ey*ey);
      if(d<best)best=d;
    }
    return best;
  }

  private void drawTerrainTile(Canvas canvas,WorldRuntimeAdapter world,String path,float wx,float wy){
    Bitmap bitmap=bitmap(path);
    if(bitmap==null)return;
    WorldCameraTransform.Point p=world.worldToScreen(wx,wy);
    float hw=TILE_W*.5f+SEAM_GUARD;
    float hh=TILE_H*.5f+SEAM_GUARD;
    RectF dst=new RectF(
        (float)Math.floor(p.x-hw),(float)Math.floor(p.y-hh),
        (float)Math.ceil(p.x+hw),(float)Math.ceil(p.y+hh));
    if(dst.right<0f||dst.left>canvas.getWidth()||dst.bottom<0f||dst.top>canvas.getHeight())return;
    canvas.drawBitmap(bitmap,null,dst,pixelPaint);
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
