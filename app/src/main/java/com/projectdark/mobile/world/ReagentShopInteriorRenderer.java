package com.projectdark.mobile.world;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import java.io.InputStream;

/** Asset-backed reagent-shop interior. The scene is one reviewed pixel-art asset, never Canvas placeholder geometry. */
public final class ReagentShopInteriorRenderer {
  public static final String SCENE_ASSET="interiors/reagent_shop/reagent_shop_classic.webp";
  private final Paint pixel=new Paint();
  private final Bitmap scene;
  public ReagentShopInteriorRenderer(Context context){
    pixel.setAntiAlias(false);pixel.setFilterBitmap(false);pixel.setDither(false);
    scene=load(context==null?null:context.getAssets(),SCENE_ASSET);
  }
  public boolean ready(){return scene!=null;}
  public void draw(Canvas c){
    if(c==null||scene==null)return;
    float maxW=c.getWidth()*.90f,maxH=c.getHeight()*.86f;
    float scale=Math.min(maxW/scene.getWidth(),maxH/scene.getHeight());
    float w=scene.getWidth()*scale,h=scene.getHeight()*scale,l=(c.getWidth()-w)*.5f,t=(c.getHeight()-h)*.5f;
    c.drawBitmap(scene,null,new RectF(l,t,l+w,t+h),pixel);
  }
  private static Bitmap load(AssetManager a,String path){
    if(a==null)return null;try(InputStream in=a.open(path)){BitmapFactory.Options o=new BitmapFactory.Options();o.inScaled=false;return BitmapFactory.decodeStream(in,null,o);}catch(Throwable ignored){return null;}
  }
}