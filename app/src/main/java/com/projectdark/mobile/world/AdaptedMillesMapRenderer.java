package com.projectdark.mobile.world;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.BitmapShader;
import android.graphics.Path;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;

/** Story-driven Milles pass: grass-dominant terrain, dirt roads, central well, and restrained landmarks. */
public final class AdaptedMillesMapRenderer {
  public static final String STATUS="MILLES_B5_PLAYABLE_HUB_VISIBLE_BUILDINGS";
  public static final float B1_VISIBLE_BUILDING_SCALE=1.0f; // Production PNGs are already native large assets; do not raster-upscale.
  private static final float TILE_W=AdaptedMillesIsometricTileLayer.TILE_WIDTH,TILE_H=AdaptedMillesIsometricTileLayer.TILE_HEIGHT;
  private static final float SEAM_GUARD=1f;
  private static final String[] GRASS_TILES={"video_reference/terrain/grass_tile_01.png","video_reference/terrain/grass_tile_02.png","video_reference/terrain/grass_tile_03.png"};
  private static final String DIRT_TEXTURE="video_reference/terrain/dirt_path_fill_texture.png";
  private static final String PLAZA_TILE="terrain/OBJ_stone_01.png";
  private final Paint outsidePaint=new Paint(),pixelPaint=new Paint(),roadEdgePaint=new Paint(),roadPaint=new Paint();
  private final Map<String,Bitmap> bitmapCache=new LinkedHashMap<>();
  private final Map<String,Rect> opaqueBoundsCache=new LinkedHashMap<>();
  private final AssetManager assets;
  public AdaptedMillesMapRenderer(){configureFill(outsidePaint,0xff4f6928);pixelPaint.setAntiAlias(false);pixelPaint.setFilterBitmap(false);pixelPaint.setDither(false);configureStroke(roadEdgePaint,0xff79501f,110f);configureStroke(roadPaint,0xff895616,94f);assets=findAssets();}

  public void draw(Canvas canvas,WorldRuntimeAdapter world){
    if(canvas==null||world==null)return;
    canvas.drawRect(0,0,canvas.getWidth(),canvas.getHeight(),outsidePaint);
    for(AdaptedMillesIsometricTileLayer.Tile t:world.map().tiles()){
      if(t.kind==AdaptedMillesIsometricTileLayer.TileKind.PLAZA)continue;
      drawTerrainTile(canvas,world,GRASS_TILES[Math.floorMod(t.variant,GRASS_TILES.length)],t.centerX,t.centerY);
      drawGrassTuft(canvas,world,t);
    }
    drawRoadRibbons(canvas,world);
    for(AdaptedMillesIsometricTileLayer.Tile t:world.map().tiles())if(t.kind==AdaptedMillesIsometricTileLayer.TileKind.PLAZA)drawTerrainTile(canvas,world,PLAZA_TILE,t.centerX,t.centerY);
    // Existing candidate tree assets add scale and depth to the hub. Coordinates are adapted
    // composition markers; their placement still needs device playtest approval.
    drawFoot(canvas,world,"vegetation/trees/OBJ_tree_01.png",245f,520f,.46f);
    drawFoot(canvas,world,"vegetation/trees/OBJ_tree_03.png",470f,745f,.48f);
    drawFoot(canvas,world,"vegetation/trees/OBJ_tree_02.png",1010f,420f,.46f);
    drawFoot(canvas,world,"vegetation/trees/OBJ_tree_01.png",1360f,690f,.45f);
    drawFoot(canvas,world,"vegetation/trees/OBJ_tree_03.png",1810f,505f,.48f);
    drawFoot(canvas,world,"vegetation/trees/OBJ_tree_02.png",2230f,720f,.43f);
    // Adapted grove and park edges: keep the central walk legible while breaking up the open lawn.
    drawFootMany(canvas,world,"vegetation/trees/OBJ_tree_01.png",.43f,new float[][]{{150,330},{160,930},{370,1190},{500,1370},{1090,900},{1250,900},{1760,1120},{2180,1190}});
    drawFootMany(canvas,world,"vegetation/trees/OBJ_tree_03.png",.44f,new float[][]{{300,310},{220,1260},{1380,1110},{1960,1180},{2280,940}});
    drawFoot(canvas,world,"vegetation/bushes/OBJ_bush_01.png",395f,470f,.52f);
    drawFoot(canvas,world,"vegetation/bushes/OBJ_bush_03.png",910f,735f,.48f);
    drawFoot(canvas,world,"vegetation/bushes/OBJ_bush_02.png",1735f,710f,.50f);
    drawFootMany(canvas,world,"vegetation/bushes/OBJ_bush_02.png",.43f,new float[][]{{245,360},{360,1150},{610,1300},{1015,875},{1320,900},{1490,1130},{1840,1090},{2110,1160}});
    drawFootMany(canvas,world,"terrain/OBJ_ground_02.png",.20f,new float[][]{{280,640},{530,360},{580,850},{420,1030},{900,920},{990,1110},{1430,910},{1580,1010},{1650,1250},{1930,960},{2100,580},{2310,1330}});
    drawFoot(canvas,world,"street/OBJ_well.png",AdaptedMillesIsometricTileLayer.PLAZA_CENTER_X,AdaptedMillesIsometricTileLayer.PLAZA_CENTER_Y,.50f);
    // Small lawn enclosure and resting places define a grove room off the market walk.
    drawFootMany(canvas,world,"structures/fences/OBJ_fence_01.png",.43f,new float[][]{{1060,825},{1125,805},{1190,805},{1255,825},{1300,875},{1295,935},{1230,970},{1160,975},{1090,945},{1050,890}});
    // B1/B5: render the actual APK building assets at a character-readable village scale.
    // The previous APK only drew the church at 0.42, so changing planning geometry had no visible effect.
    drawFoot(canvas,world,"buildings/BLD_002_potion_shop.png",320f,420f,B1_VISIBLE_BUILDING_SCALE);
    // Visible walk-in target: same foot coordinate as WorldDef potion_shop_door trigger.
    drawPortalTile(canvas,world,"assets/world/portal/portal_reagent_shop.webp",320f,459f);
    drawFoot(canvas,world,"buildings/BLD_003_weapon_shop.png",760f,300f,B1_VISIBLE_BUILDING_SCALE);
    drawFoot(canvas,world,"buildings/BLD_005_general_shop.png",1120f,360f,B1_VISIBLE_BUILDING_SCALE);
    drawFoot(canvas,world,"landmarks/BLD_011_church.png",1540f,455f,1.35f);
    drawFoot(canvas,world,"buildings/BLD_006_inn.png",1980f,650f,B1_VISIBLE_BUILDING_SCALE);
    drawFoot(canvas,world,"street/OBJ_noticeboard.png",690f,720f,.38f);
    drawVideoBenchSet(canvas,world,new float[][]{{850,650},{980,705},{1360,650},{1690,760},{780,950},{1230,1030}});
    drawFoot(canvas,world,"street/OBJ_lamp_01.png",720f,535f,.42f);
    drawFoot(canvas,world,"street/OBJ_lamp_02.png",830f,535f,.42f);
    drawFootMany(canvas,world,"street/OBJ_lamp_01.png",.38f,new float[][]{{590,520},{945,480},{1135,590},{1460,630},{1790,760},{750,1100},{790,1320}});
  }

  /**
   * Video-derived surface contract:
   * GROUND -> cropped outdoor grass diamond variants
   * ROAD/GATE -> cropped outdoor dirt-path diamond variants
   * PLAZA -> OBJ_stone_01
   * Only a small set of recorded variants is selected; no colors are synthesized at runtime.
   */
  private void drawGrassTuft(Canvas c,WorldRuntimeAdapter w,AdaptedMillesIsometricTileLayer.Tile t){
    if(t.kind!=AdaptedMillesIsometricTileLayer.TileKind.GROUND)return;
    int hash=t.row*73856093^t.column*19349663;
    if(Math.floorMod(hash,6)==0)drawFoot(c,w,"terrain/OBJ_ground_02.png",t.centerX,t.centerY+5f,.12f);
  }

  /** Navigation remains tile-based; the visible dirt follows hand-authored curved paths. */
  private void drawRoadRibbons(Canvas c,WorldRuntimeAdapter w){
    Bitmap texture=bitmap(DIRT_TEXTURE);
    if(texture!=null)roadPaint.setShader(new BitmapShader(texture,Shader.TileMode.REPEAT,Shader.TileMode.REPEAT));
    c.save();c.translate(-w.camera().cameraX(),-w.camera().cameraY());
    for(float[][] points:AdaptedMillesIsometricTileLayer.roadPaths()){
      Path path=new Path();path.moveTo(points[0][0],points[0][1]);
      for(int i=1;i<points.length;i++)path.lineTo(points[i][0],points[i][1]);
      c.drawPath(path,roadEdgePaint);c.drawPath(path,roadPaint);
    }
    c.restore();
    roadPaint.setShader(null);
  }

  private void drawTerrainTile(Canvas c,WorldRuntimeAdapter w,String path,float wx,float wy){
    WorldCameraTransform.Point p=w.worldToScreen(wx,wy);float hw=TILE_W*.5f+SEAM_GUARD,hh=TILE_H*.5f+SEAM_GUARD;
    RectF dst=new RectF((float)Math.floor(p.x-hw),(float)Math.floor(p.y-hh),(float)Math.ceil(p.x+hw),(float)Math.ceil(p.y+hh));
    if(dst.right<0||dst.left>c.getWidth()||dst.bottom<0||dst.top>c.getHeight())return;
    Bitmap b=bitmap(path);if(b==null)return;Rect src=opaqueBounds(path,b);if(src==null)return;c.drawBitmap(b,src,dst,pixelPaint);
  }
  private Rect opaqueBounds(String path,Bitmap b){if(opaqueBoundsCache.containsKey(path))return opaqueBoundsCache.get(path);int minX=b.getWidth(),minY=b.getHeight(),maxX=-1,maxY=-1;for(int y=0;y<b.getHeight();y++)for(int x=0;x<b.getWidth();x++)if((b.getPixel(x,y)>>>24)!=0){if(x<minX)minX=x;if(x>maxX)maxX=x;if(y<minY)minY=y;if(y>maxY)maxY=y;}Rect r=maxX>=minX?new Rect(minX,minY,maxX+1,maxY+1):null;opaqueBoundsCache.put(path,r);return r;}
  private void drawPortalTile(Canvas c,WorldRuntimeAdapter w,String path,float wx,float wy){Bitmap b=bitmap(path);if(b==null)return;WorldCameraTransform.Point p=w.worldToScreen(wx,wy);RectF d=new RectF(Math.round(p.x-TILE_W*.5f),Math.round(p.y-TILE_H*.5f),Math.round(p.x+TILE_W*.5f),Math.round(p.y+TILE_H*.5f));if(d.right<0||d.left>c.getWidth()||d.bottom<0||d.top>c.getHeight())return;c.drawBitmap(b,null,d,pixelPaint);}
  private void drawFoot(Canvas c,WorldRuntimeAdapter w,String path,float wx,float wy,float scale){Bitmap b=bitmap(path);if(b==null)return;WorldCameraTransform.Point p=w.worldToScreen(wx,wy);float ww=b.getWidth()*scale,hh=b.getHeight()*scale;RectF d=new RectF(Math.round(p.x-ww*.5f),Math.round(p.y-hh),Math.round(p.x+ww*.5f),Math.round(p.y));if(d.right<0||d.left>c.getWidth()||d.bottom<0||d.top>c.getHeight())return;c.drawBitmap(b,null,d,pixelPaint);}
  private void drawFootMany(Canvas c,WorldRuntimeAdapter w,String path,float scale,float[][] points){for(float[] point:points)drawFoot(c,w,path,point[0],point[1],scale);}
  private void drawVideoBenchSet(Canvas c,WorldRuntimeAdapter w,float[][] points){for(int i=0;i<points.length;i++){String path="video_reference/objects/bench_video_cutout_"+String.format(java.util.Locale.ROOT,"%02d",(i%4)+1)+".png";drawFoot(c,w,path,points[i][0],points[i][1],.38f);}}
  private Bitmap bitmap(String path){if(bitmapCache.containsKey(path))return bitmapCache.get(path);Bitmap b=null;if(assets!=null)try(InputStream in=assets.open(path)){BitmapFactory.Options o=new BitmapFactory.Options();o.inScaled=false;b=BitmapFactory.decodeStream(in,null,o);}catch(Throwable ignored){}bitmapCache.put(path,b);return b;}
  private static AssetManager findAssets(){try{Class<?> c=Class.forName("android.app.ActivityThread");Method m=c.getDeclaredMethod("currentApplication");Object a=m.invoke(null);return a instanceof Context?((Context)a).getAssets():null;}catch(Throwable ignored){return null;}}
  private static void configureFill(Paint p,int color){p.setAntiAlias(false);p.setDither(false);p.setColor(color);p.setStyle(Paint.Style.FILL);}
  private static void configureStroke(Paint p,int color,float width){p.setAntiAlias(true);p.setDither(false);p.setColor(color);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(width);p.setStrokeCap(Paint.Cap.ROUND);p.setStrokeJoin(Paint.Join.ROUND);}
}
