package com.projectdark.mobile.world;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Milles production vertical-slice renderer.
 *
 * Source pixels come directly from assets/milles/production via the app assets sourceSet.
 * No generated replacement art is used here. Runtime scaling uses nearest-neighbour only.
 */
public final class AdaptedMillesMapRenderer {
  public static final String STATUS="MILLES_PRODUCTION_VERTICAL_SLICE_V4_DENSE_TOWN_COMPOSITION";
  public static final float LOGICAL_GROUND_WIDTH=AdaptedMillesIsometricTileLayer.TILE_WIDTH;
  public static final float LOGICAL_GROUND_HEIGHT=AdaptedMillesIsometricTileLayer.TILE_HEIGHT;
  private final Paint pixel=new Paint();
  private final Paint backdrop=new Paint();
  private final Map<String,Bitmap> cache=new LinkedHashMap<>();
  private AssetManager assets;

  public AdaptedMillesMapRenderer(){
    pixel.setAntiAlias(false);pixel.setFilterBitmap(false);pixel.setDither(false);
    backdrop.setAntiAlias(false);backdrop.setColor(0xff26382b);
    assets=findAssets();
  }

  public void draw(Canvas canvas,WorldRuntimeAdapter world){
    if(canvas==null||world==null)return;
    canvas.drawRect(0,0,canvas.getWidth(),canvas.getHeight(),backdrop);
    WorldMapProjection.Spawn s=world.map().spawn();

    // Ground remains owned by the canonical 64x32 navigation grid.
    for(AdaptedMillesIsometricTileLayer.Tile tile:world.map().tiles()){
      drawGround(canvas,world,terrainFor(tile.kind),tile.centerX,tile.centerY,1f);
    }

    // Dense town ring: major buildings stay within roughly 2-4 authored tiles of the playable center.
    // This prevents the camera from exposing a large empty checkerboard between landmarks.
    drawFoot(canvas,world,"landmarks/BLD_011_church.png",s.x+190f,s.y-92f,.74f);
    drawFoot(canvas,world,"buildings/BLD_002_potion_shop.png",s.x-205f,s.y-78f,.66f);
    drawFoot(canvas,world,"buildings/BLD_003_weapon_shop.png",s.x-78f,s.y-132f,.64f);
    drawFoot(canvas,world,"buildings/BLD_005_general_shop.png",s.x+72f,s.y-142f,.64f);
    drawFoot(canvas,world,"buildings/BLD_006_inn.png",s.x+248f,s.y-28f,.64f);

    // Lake/nature district is closer to the plaza and balanced by vegetation on the opposite side.
    drawFoot(canvas,world,"water/lakes/OBJ_lake_main.png",s.x+245f,s.y+172f,.76f);
    drawFoot(canvas,world,"vegetation/trees/OBJ_tree_01.png",s.x-250f,s.y+44f,.86f);
    drawFoot(canvas,world,"vegetation/trees/OBJ_tree_02.png",s.x-205f,s.y+128f,.86f);
    drawFoot(canvas,world,"vegetation/trees/OBJ_tree_03.png",s.x+330f,s.y+92f,.86f);
    drawFoot(canvas,world,"vegetation/trees/OBJ_tree_01.png",s.x+355f,s.y+195f,.78f);
    drawFoot(canvas,world,"vegetation/trees/OBJ_tree_02.png",s.x-315f,s.y+190f,.78f);
    drawFoot(canvas,world,"vegetation/bushes/OBJ_bush_01.png",s.x+145f,s.y+128f,.94f);
    drawFoot(canvas,world,"vegetation/bushes/OBJ_bush_02.png",s.x-145f,s.y+142f,.94f);
    drawFoot(canvas,world,"vegetation/bushes/OBJ_bush_01.png",s.x+292f,s.y+48f,.88f);
    drawFoot(canvas,world,"vegetation/bushes/OBJ_bush_02.png",s.x-278f,s.y+92f,.88f);

    // Plaza/street-life: enough foreground anchors that the center reads as a town rather than a test floor.
    drawFoot(canvas,world,"street/OBJ_well.png",s.x+22f,s.y+58f,.92f);
    drawFoot(canvas,world,"street/OBJ_noticeboard.png",s.x-112f,s.y+24f,.90f);
    drawFoot(canvas,world,"street/OBJ_bench.png",s.x+112f,s.y+30f,.92f);
    drawFoot(canvas,world,"street/OBJ_lamp_01.png",s.x+164f,s.y-4f,.92f);
    drawFoot(canvas,world,"street/OBJ_lamp_01.png",s.x-164f,s.y+4f,.92f);
    drawFoot(canvas,world,"street/OBJ_signpost.png",s.x-24f,s.y-44f,.92f);

    // Market district now occupies the lower-left instead of sitting outside the common camera frame.
    drawFoot(canvas,world,"market/OBJ_stall_01.png",s.x-176f,s.y+176f,.84f);
    drawFoot(canvas,world,"market/OBJ_cart.png",s.x-72f,s.y+198f,.88f);
    drawFoot(canvas,world,"market/OBJ_stall_01.png",s.x-280f,s.y+232f,.74f);

    // Fence segments frame both lower quadrants and visually break up long repeated-tile runs.
    for(int i=0;i<4;i++){
      drawFoot(canvas,world,"structures/fences/OBJ_fence_01.png",s.x-300f+i*64f,s.y+238f,.86f);
      drawFoot(canvas,world,"structures/fences/OBJ_fence_02.png",s.x+132f+i*64f,s.y+248f,.86f);
    }
  }

  private static String terrainFor(AdaptedMillesIsometricTileLayer.TileKind kind){
    switch(kind){
      case ROAD:
      case PLAZA:
      case GATE:return "terrain/OBJ_stone_01.png";
      default:return "terrain/OBJ_ground_01.png";
    }
  }

  /**
   * Terrain source crops keep their original extraction dimensions, but their runtime footprint is
   * one logical isometric cell. Keeping those concerns separate makes the visible ground cadence
   * agree with the 64x32 navigation grid instead of inheriting arbitrary source-crop dimensions.
   */
  private void drawGround(Canvas c,WorldRuntimeAdapter world,String path,float wx,float wy,float scale){
    Bitmap b=bitmap(path);if(b==null)return;
    WorldCameraTransform.Point p=world.worldToScreen(wx,wy);
    float w=LOGICAL_GROUND_WIDTH*scale,h=LOGICAL_GROUND_HEIGHT*scale;
    RectF dst=new RectF(Math.round(p.x-w*.5f),Math.round(p.y-h*.5f),Math.round(p.x+w*.5f),Math.round(p.y+h*.5f));
    if(dst.right<0||dst.left>c.getWidth()||dst.bottom<0||dst.top>c.getHeight())return;
    c.drawBitmap(b,null,dst,pixel);
  }

  /** Anchor production sprites by their bottom-center ground contact. */
  private void drawFoot(Canvas c,WorldRuntimeAdapter world,String path,float wx,float wy,float scale){
    Bitmap b=bitmap(path);if(b==null)return;
    WorldCameraTransform.Point p=world.worldToScreen(wx,wy);
    float w=b.getWidth()*scale,h=b.getHeight()*scale;
    RectF dst=new RectF(Math.round(p.x-w*.5f),Math.round(p.y-h),Math.round(p.x+w*.5f),Math.round(p.y));
    if(dst.right<0||dst.left>c.getWidth()||dst.bottom<0||dst.top>c.getHeight())return;
    c.drawBitmap(b,null,dst,pixel);
  }

  private Bitmap bitmap(String path){
    if(cache.containsKey(path))return cache.get(path);
    Bitmap b=null;
    if(assets!=null){try(InputStream in=assets.open(path)){BitmapFactory.Options o=new BitmapFactory.Options();o.inScaled=false;b=BitmapFactory.decodeStream(in,null,o);}catch(Throwable ignored){}}
    cache.put(path,b);return b;
  }

  private AssetManager findAssets(){
    try{
      Class<?> at=Class.forName("android.app.ActivityThread");
      Method m=at.getDeclaredMethod("currentApplication");
      Object app=m.invoke(null);
      return app instanceof Context?((Context)app).getAssets():null;
    }catch(Throwable ignored){return null;}
  }
}