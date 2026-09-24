package com.projectdark.mobile.world;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Draws the Milles world from reusable ground tiles and independently anchored object assets. */
public final class AdaptedMillesMapRenderer {
  public static final String STATUS="MILLE_MAP_ASSEMBLED_FROM_REUSABLE_ASSETS";
  public static final float B1_VISIBLE_BUILDING_SCALE=1.0f;
  private static final String MAP_LAYOUT="maps/milles_garden.json";
  private static final float TILE_W=AdaptedMillesIsometricTileLayer.TILE_WIDTH;
  private static final float TILE_H=AdaptedMillesIsometricTileLayer.TILE_HEIGHT;
  private static final float SEAM_GUARD=1f;

  private static final class SpritePlacement {
    final String id,asset,drawMode,district;
    final float x,y,scale;
    SpritePlacement(JSONObject value){
      id=value.optString("id","");asset=value.optString("asset","");
      drawMode=value.optString("draw","foot");district=value.optString("district","");
      x=(float)value.optDouble("x",0);y=(float)value.optDouble("y",0);scale=(float)value.optDouble("scale",1);
    }
  }

  private final Paint outsidePaint=new Paint(),pixelPaint=new Paint();
  private final Map<String,Bitmap> bitmapCache=new LinkedHashMap<>();
  private final Map<String,Rect> opaqueBoundsCache=new LinkedHashMap<>();
  private final AssetManager assets;
  private final List<SpritePlacement> placements;

  public AdaptedMillesMapRenderer(){
    configureFill(outsidePaint,0xff355127);
    pixelPaint.setAntiAlias(false);pixelPaint.setFilterBitmap(false);pixelPaint.setDither(false);
    assets=findAssets();placements=loadPlacements();
  }

  public int placementCount(){return placements.size();}

  public void draw(Canvas canvas,WorldRuntimeAdapter world){
    if(canvas==null||world==null||world.map()==null)return;
    canvas.drawRect(0,0,canvas.getWidth(),canvas.getHeight(),outsidePaint);
    for(AdaptedMillesIsometricTileLayer.Tile tile:world.map().tiles())
      drawTerrainTile(canvas,world,tile.assetRef,tile.centerX,tile.centerY);
    for(SpritePlacement placement:placements)drawPlacement(canvas,world,placement);
  }

  private List<SpritePlacement> loadPlacements(){
    if(assets==null)return Collections.emptyList();
    try(InputStream in=assets.open(MAP_LAYOUT);ByteArrayOutputStream out=new ByteArrayOutputStream()){
      byte[] buffer=new byte[4096];int read;while((read=in.read(buffer))!=-1)out.write(buffer,0,read);
      JSONObject root=new JSONObject(new String(out.toByteArray(),StandardCharsets.UTF_8));
      JSONArray entries=root.getJSONArray("objects");List<SpritePlacement> result=new ArrayList<>();
      for(int i=0;i<entries.length();i++){
        JSONObject entry=entries.getJSONObject(i);
        if(entry.optString("id").isEmpty()||entry.optString("asset").isEmpty()||entry.optDouble("scale",0)<=0)continue;
        result.add(new SpritePlacement(entry));
      }
      result.sort(Comparator.comparingDouble((SpritePlacement value)->value.y).thenComparing(value->value.id));
      return Collections.unmodifiableList(result);
    }catch(Throwable ignored){return Collections.emptyList();}
  }

  private void drawPlacement(Canvas canvas,WorldRuntimeAdapter world,SpritePlacement placement){
    Bitmap image=bitmap(placement.asset);if(image==null)return;
    WorldCameraTransform.Point foot=world.worldToScreen(placement.x,placement.y);
    if("tile".equals(placement.drawMode)){
      RectF dst=new RectF(Math.round(foot.x-TILE_W*.5f),Math.round(foot.y-TILE_H*.5f),
          Math.round(foot.x+TILE_W*.5f),Math.round(foot.y+TILE_H*.5f));
      if(visible(dst,canvas))canvas.drawBitmap(image,null,dst,pixelPaint);
      return;
    }
    float width=image.getWidth()*placement.scale,height=image.getHeight()*placement.scale;
    RectF dst=new RectF(Math.round(foot.x-width*.5f),Math.round(foot.y-height),
        Math.round(foot.x+width*.5f),Math.round(foot.y));
    if(visible(dst,canvas))canvas.drawBitmap(image,null,dst,pixelPaint);
  }

  private void drawTerrainTile(Canvas canvas,WorldRuntimeAdapter world,String asset,float wx,float wy){
    WorldCameraTransform.Point point=world.worldToScreen(wx,wy);
    float halfW=TILE_W*.5f+SEAM_GUARD,halfH=TILE_H*.5f+SEAM_GUARD;
    RectF dst=new RectF((float)Math.floor(point.x-halfW),(float)Math.floor(point.y-halfH),
        (float)Math.ceil(point.x+halfW),(float)Math.ceil(point.y+halfH));
    if(!visible(dst,canvas))return;
    Bitmap image=bitmap(asset);if(image==null)return;Rect src=opaqueBounds(asset,image);
    if(src!=null)canvas.drawBitmap(image,src,dst,pixelPaint);
  }

  private static boolean visible(RectF rect,Canvas canvas){
    return rect.right>=0&&rect.left<=canvas.getWidth()&&rect.bottom>=0&&rect.top<=canvas.getHeight();
  }
  private Rect opaqueBounds(String path,Bitmap image){
    if(opaqueBoundsCache.containsKey(path))return opaqueBoundsCache.get(path);
    int minX=image.getWidth(),minY=image.getHeight(),maxX=-1,maxY=-1;
    for(int y=0;y<image.getHeight();y++)for(int x=0;x<image.getWidth();x++)
      if((image.getPixel(x,y)>>>24)!=0){minX=Math.min(minX,x);maxX=Math.max(maxX,x);minY=Math.min(minY,y);maxY=Math.max(maxY,y);}
    Rect result=maxX>=minX?new Rect(minX,minY,maxX+1,maxY+1):null;
    opaqueBoundsCache.put(path,result);return result;
  }
  private Bitmap bitmap(String path){
    if(bitmapCache.containsKey(path))return bitmapCache.get(path);
    Bitmap image=null;
    if(assets!=null)try(InputStream in=assets.open(path)){BitmapFactory.Options options=new BitmapFactory.Options();options.inScaled=false;image=BitmapFactory.decodeStream(in,null,options);}catch(Throwable ignored){}
    bitmapCache.put(path,image);return image;
  }
  private static AssetManager findAssets(){
    try{Class<?> type=Class.forName("android.app.ActivityThread");Method method=type.getDeclaredMethod("currentApplication");Object app=method.invoke(null);return app instanceof Context?((Context)app).getAssets():null;}catch(Throwable ignored){return null;}
  }
  private static void configureFill(Paint paint,int color){paint.setAntiAlias(false);paint.setDither(false);paint.setColor(color);paint.setStyle(Paint.Style.FILL);}
}
