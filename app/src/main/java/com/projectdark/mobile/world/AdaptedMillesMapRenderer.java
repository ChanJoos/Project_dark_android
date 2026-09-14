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

/** Milles vertical slice reconstructed against the user-supplied reference video. */
public final class AdaptedMillesMapRenderer {
  public static final String STATUS="MILLES_VIDEO_REFERENCE_V6_NATURAL_PATH_SPLIT";
  public static final String REFERENCE_COMPOSITION="grass-first village; branching diagonal paths; central fountain; fenced tree gardens; water-side landmark";
  public static final String ROAD_SURFACE_STATUS="ADAPTED_SOURCE_GROUND_VARIANT_PENDING_TRUE_DIRT_SOURCE";
  public static final float LOGICAL_GROUND_WIDTH=AdaptedMillesIsometricTileLayer.TILE_WIDTH;
  public static final float LOGICAL_GROUND_HEIGHT=AdaptedMillesIsometricTileLayer.TILE_HEIGHT;
  private final Paint pixel=new Paint();private final Paint backdrop=new Paint();
  private final Map<String,Bitmap> cache=new LinkedHashMap<>();private AssetManager assets;
  public AdaptedMillesMapRenderer(){pixel.setAntiAlias(false);pixel.setFilterBitmap(false);pixel.setDither(false);backdrop.setAntiAlias(false);backdrop.setColor(0xff26382b);assets=findAssets();}

  public void draw(Canvas canvas,WorldRuntimeAdapter world){
    if(canvas==null||world==null)return;canvas.drawRect(0,0,canvas.getWidth(),canvas.getHeight(),backdrop);WorldMapProjection.Spawn s=world.map().spawn();
    for(AdaptedMillesIsometricTileLayer.Tile tile:world.map().tiles())drawGround(canvas,world,terrainFor(tile.kind),tile.centerX,tile.centerY,1f);

    // Reference hierarchy: green village first, buildings form the outer frame rather than a central wall.
    drawFoot(canvas,world,"landmarks/BLD_011_church.png",s.x+270f,s.y-150f,.72f);
    drawFoot(canvas,world,"buildings/BLD_002_potion_shop.png",s.x-285f,s.y-128f,.64f);
    drawFoot(canvas,world,"buildings/BLD_003_weapon_shop.png",s.x-128f,s.y-206f,.62f);
    drawFoot(canvas,world,"buildings/BLD_005_general_shop.png",s.x+86f,s.y-220f,.62f);
    drawFoot(canvas,world,"buildings/BLD_006_inn.png",s.x+330f,s.y-38f,.62f);

    // Central fountain/plaza and street furniture visible through most camera positions.
    drawFoot(canvas,world,"street/OBJ_well.png",s.x+12f,s.y+34f,1.02f);
    drawFoot(canvas,world,"street/OBJ_bench.png",s.x-96f,s.y+58f,.92f);
    drawFoot(canvas,world,"street/OBJ_bench.png",s.x+118f,s.y+72f,.92f);
    drawFoot(canvas,world,"street/OBJ_lamp_01.png",s.x-72f,s.y-12f,.94f);
    drawFoot(canvas,world,"street/OBJ_lamp_01.png",s.x+92f,s.y-18f,.94f);
    drawFoot(canvas,world,"street/OBJ_noticeboard.png",s.x-176f,s.y-6f,.90f);
    drawFoot(canvas,world,"street/OBJ_signpost.png",s.x+174f,s.y+4f,.92f);

    // Circular/clustered tree gardens from the Milles reference, fenced instead of random isolated props.
    drawFoot(canvas,world,"vegetation/trees/OBJ_tree_01.png",s.x-205f,s.y+76f,.88f);
    drawFoot(canvas,world,"vegetation/trees/OBJ_tree_02.png",s.x-258f,s.y+132f,.86f);
    drawFoot(canvas,world,"vegetation/trees/OBJ_tree_03.png",s.x-172f,s.y+166f,.84f);
    drawFoot(canvas,world,"vegetation/bushes/OBJ_bush_01.png",s.x-224f,s.y+184f,.94f);
    drawFoot(canvas,world,"vegetation/bushes/OBJ_bush_02.png",s.x-126f,s.y+126f,.94f);
    for(int i=0;i<4;i++)drawFoot(canvas,world,"structures/fences/OBJ_fence_01.png",s.x-316f+i*62f,s.y+224f,.86f);
    drawFoot(canvas,world,"structures/fences/OBJ_fence_02.png",s.x-330f,s.y+162f,.86f);

    drawFoot(canvas,world,"vegetation/trees/OBJ_tree_01.png",s.x+206f,s.y+82f,.88f);
    drawFoot(canvas,world,"vegetation/trees/OBJ_tree_02.png",s.x+270f,s.y+126f,.86f);
    drawFoot(canvas,world,"vegetation/trees/OBJ_tree_03.png",s.x+178f,s.y+170f,.84f);
    drawFoot(canvas,world,"vegetation/bushes/OBJ_bush_01.png",s.x+236f,s.y+186f,.94f);
    for(int i=0;i<4;i++)drawFoot(canvas,world,"structures/fences/OBJ_fence_02.png",s.x+126f+i*62f,s.y+226f,.86f);

    // Water-side district: the reference repeatedly uses water/stone edges as a strong navigation landmark.
    drawFoot(canvas,world,"water/lakes/OBJ_lake_main.png",s.x+318f,s.y+252f,.82f);
    drawFoot(canvas,world,"street/OBJ_lamp_01.png",s.x+248f,s.y+206f,.90f);
    drawFoot(canvas,world,"street/OBJ_signpost.png",s.x+286f,s.y+218f,.88f);
    drawFoot(canvas,world,"vegetation/trees/OBJ_tree_01.png",s.x+382f,s.y+170f,.80f);

    // Market occupies a side street, not the plaza center.
    drawFoot(canvas,world,"market/OBJ_stall_01.png",s.x-330f,s.y+286f,.80f);
    drawFoot(canvas,world,"market/OBJ_cart.png",s.x-230f,s.y+302f,.86f);
  }

  private static String terrainFor(AdaptedMillesIsometricTileLayer.TileKind kind){
    switch(kind){
      case ROAD:return "terrain/OBJ_ground_02.png";
      case PLAZA:
      case GATE:return "terrain/OBJ_stone_01.png";
      default:return "terrain/OBJ_ground_01.png";
    }
  }
  private void drawGround(Canvas c,WorldRuntimeAdapter world,String path,float wx,float wy,float scale){Bitmap b=bitmap(path);if(b==null)return;WorldCameraTransform.Point p=world.worldToScreen(wx,wy);float w=LOGICAL_GROUND_WIDTH*scale,h=LOGICAL_GROUND_HEIGHT*scale;RectF dst=new RectF(Math.round(p.x-w*.5f),Math.round(p.y-h*.5f),Math.round(p.x+w*.5f),Math.round(p.y+h*.5f));if(dst.right<0||dst.left>c.getWidth()||dst.bottom<0||dst.top>c.getHeight())return;c.drawBitmap(b,null,dst,pixel);}
  private void drawFoot(Canvas c,WorldRuntimeAdapter world,String path,float wx,float wy,float scale){Bitmap b=bitmap(path);if(b==null)return;WorldCameraTransform.Point p=world.worldToScreen(wx,wy);float w=b.getWidth()*scale,h=b.getHeight()*scale;RectF dst=new RectF(Math.round(p.x-w*.5f),Math.round(p.y-h),Math.round(p.x+w*.5f),Math.round(p.y));if(dst.right<0||dst.left>c.getWidth()||dst.bottom<0||dst.top>c.getHeight())return;c.drawBitmap(b,null,dst,pixel);}
  private Bitmap bitmap(String path){if(cache.containsKey(path))return cache.get(path);Bitmap b=null;if(assets!=null){try(InputStream in=assets.open(path)){BitmapFactory.Options o=new BitmapFactory.Options();o.inScaled=false;b=BitmapFactory.decodeStream(in,null,o);}catch(Throwable ignored){}}cache.put(path,b);return b;}
  private AssetManager findAssets(){try{Class<?> at=Class.forName("android.app.ActivityThread");Method m=at.getDeclaredMethod("currentApplication");Object app=m.invoke(null);return app instanceof Context?((Context)app).getAssets():null;}catch(Throwable ignored){return null;}}
}
