package com.projectdark.mobile.world;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.*;
import java.io.InputStream;

/** Classic reagent-shop presentation on the canonical WorldRuntimeAdapter camera/grid. */
public final class ReagentShopMapRenderer {
  private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG),px=new Paint();
  private final Bitmap classic;
  public ReagentShopMapRenderer(){px.setAntiAlias(false);px.setFilterBitmap(false);px.setDither(false);Context c=context();classic=load(c==null?null:c.getAssets(),"interiors/reagent_shop/reagent_shop_classic.webp");}
  public void draw(Canvas c,WorldRuntimeAdapter w){
    p.setStyle(Paint.Style.FILL);p.setColor(0xff120d09);c.drawRect(0,0,c.getWidth(),c.getHeight(),p);
    if(classic!=null){c.drawBitmap(classic,null,new RectF(160f,92f,800f,476f),px);return;}
    for(ReagentShopMapDef.Tile t:ReagentShopMapDef.tiles())tile(c,w,t.centerX,t.centerY);
    fixture(c,w,304f,224f,656f,264f,0xff5b3d27);fixture(c,w,160f,128f,224f,288f,0xff493326);fixture(c,w,736f,128f,800f,288f,0xff493326);fixture(c,w,160f,96f,800f,128f,0xff34251d);
  }
  private void tile(Canvas c,WorldRuntimeAdapter w,float x,float y){WorldCameraTransform.Point q=w.worldToScreen(x,y);Path d=new Path();d.moveTo(q.x,q.y-16f);d.lineTo(q.x+32f,q.y);d.lineTo(q.x,q.y+16f);d.lineTo(q.x-32f,q.y);d.close();p.setColor(0xff6b5a43);p.setStyle(Paint.Style.FILL);c.drawPath(d,p);p.setColor(0xff3d3327);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1f);c.drawPath(d,p);p.setStyle(Paint.Style.FILL);}
  private void fixture(Canvas c,WorldRuntimeAdapter w,float l,float t,float r,float b,int color){WorldCameraTransform.Point a=w.worldToScreen(l,t),z=w.worldToScreen(r,b);p.setColor(color);c.drawRect(new RectF(a.x,a.y,z.x,z.y),p);}
  private static Bitmap load(AssetManager a,String path){if(a==null)return null;try(InputStream in=a.open(path)){BitmapFactory.Options o=new BitmapFactory.Options();o.inScaled=false;return BitmapFactory.decodeStream(in,null,o);}catch(Throwable t){return null;}}
  private static Context context(){try{Class<?> at=Class.forName("android.app.ActivityThread");Object app=at.getMethod("currentApplication").invoke(null);return app instanceof Context?(Context)app:null;}catch(Throwable t){return null;}}
}