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
  public static final String STATUS="MILLES_PRODUCTION_VERTICAL_SLICE_V1";
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

    // Ground/road field around the current playable spawn. These are real production terrain sprites.
    for(int gy=-4;gy<=4;gy++){
      for(int gx=-7;gx<=7;gx++){
        float wx=s.x+gx*64f, wy=s.y+gy*32f;
        String terrain=(Math.abs(gx)<=1||Math.abs(gy)<=1)?"terrain/OBJ_stone_01.png":"terrain/OBJ_ground_01.png";
        drawGround(canvas,world,terrain,wx,wy,1f);
      }
    }

    // Back row: landmark + shops. Positions are authored around the existing playable spawn.
    drawFoot(canvas,world,"landmarks/BLD_011_church.png",s.x+250f,s.y-120f,.72f);
    drawFoot(canvas,world,"buildings/BLD_002_potion_shop.png",s.x-285f,s.y-105f,.62f);
    drawFoot(canvas,world,"buildings/BLD_003_weapon_shop.png",s.x-90f,s.y-165f,.60f);
    drawFoot(canvas,world,"buildings/BLD_005_general_shop.png",s.x+105f,s.y-170f,.60f);
    drawFoot(canvas,world,"buildings/BLD_006_inn.png",s.x+390f,s.y-70f,.60f);

    // Lake/nature zone gives this first slice a strong Milles landmark read.
    drawFoot(canvas,world,"water/lakes/OBJ_lake_main.png",s.x+325f,s.y+210f,.72f);
    drawFoot(canvas,world,"vegetation/trees/OBJ_tree_01.png",s.x-330f,s.y+45f,.82f);
    drawFoot(canvas,world,"vegetation/trees/OBJ_tree_02.png",s.x-270f,s.y+125f,.82f);
    drawFoot(canvas,world,"vegetation/trees/OBJ_tree_03.png",s.x+455f,s.y+110f,.82f);
    drawFoot(canvas,world,"vegetation/bushes/OBJ_bush_01.png",s.x+175f,s.y+145f,.90f);
    drawFoot(canvas,world,"vegetation/bushes/OBJ_bush_02.png",s.x-175f,s.y+155f,.90f);

    // Street-life props around the central road/plaza.
    drawFoot(canvas,world,"street/OBJ_well.png",s.x+35f,s.y+70f,.88f);
    drawFoot(canvas,world,"street/OBJ_noticeboard.png",s.x-145f,s.y+35f,.86f);
    drawFoot(canvas,world,"street/OBJ_bench.png",s.x+145f,s.y+40f,.88f);
    drawFoot(canvas,world,"market/OBJ_stall_01.png",s.x-225f,s.y+205f,.78f);
    drawFoot(canvas,world,"market/OBJ_cart.png",s.x-80f,s.y+225f,.82f);
    drawFoot(canvas,world,"street/OBJ_lamp_01.png",s.x+205f,s.y+25f,.88f);
    drawFoot(canvas,world,"street/OBJ_signpost.png",s.x-35f,s.y-55f,.88f);

    // Fences frame the slice without turning it into a corridor.
    for(int i=0;i<4;i++){
      drawFoot(canvas,world,"structures/fences/OBJ_fence_01.png",s.x-390f+i*72f,s.y+245f,.82f);
      drawFoot(canvas,world,"structures/fences/OBJ_fence_02.png",s.x+245f+i*72f,s.y+300f,.82f);
    }
  }

  private void drawGround(Canvas c,WorldRuntimeAdapter world,String path,float wx,float wy,float scale){
    Bitmap b=bitmap(path);if(b==null)return;
    WorldCameraTransform.Point p=world.worldToScreen(wx,wy);
    float w=b.getWidth()*scale,h=b.getHeight()*scale;
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
