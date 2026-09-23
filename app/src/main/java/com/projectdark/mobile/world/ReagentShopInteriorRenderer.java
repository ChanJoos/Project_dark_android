package com.projectdark.mobile.world;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.*;
import java.io.InputStream;

/** Tile/object renderer for the reagent shop. No full-screen scene bitmap: floor, walls and objects share world coordinates. */
public final class ReagentShopInteriorRenderer {
  private final Paint px=new Paint();
  private final AssetManager assets;
  private Bitmap wood,barrel,crate,portal;
  public ReagentShopInteriorRenderer(Context c){
    px.setAntiAlias(false);px.setFilterBitmap(false);px.setDither(false);assets=c==null?null:c.getAssets();
    wood=load("terrain/OBJ_ground_01.png"); barrel=load("storage/OBJ_barrel.png"); crate=load("storage/OBJ_crate.png");
    portal=load("assets/world/portal/portal_reagent_shop.webp");
  }
  public boolean ready(){return wood!=null;}
  public void draw(Canvas c){
    if(c==null)return;
    c.drawColor(0xff17100b);
    // 64x32 authored floor tiles: visual tile centers are the same centers used by navigation.
    for(float y=272f;y<=464f;y+=16f){
      int row=Math.round((y-272f)/16f);float off=(row&1)==0?0f:32f;
      for(float x=224f+off;x<=736f;x+=64f)tile(c,wood,x,y);
    }
    // rear timber wall and straight counter; these are world geometry, not a stretched background image.
    Paint q=px;q.setColor(0xff4b2f1d);c.drawRect(192,118,768,240,q);
    q.setColor(0xff765033);for(int x=200;x<768;x+=32)c.drawRect(x,122,x+3,238,q);
    q.setColor(0xff2c1a10);c.drawRect(205,238,755,250,q);
    q.setColor(0xff8a603c);c.drawRect(210,248,750,270,q);
    q.setColor(0xff3a2416);c.drawRect(210,267,750,274,q);
    object(c,crate,244,230,.75f);object(c,barrel,710,235,.78f);
    object(c,crate,292,228,.65f);object(c,barrel,665,236,.68f);
    // Exact exit tile. Player must occupy this tile to leave.
    if(portal!=null)c.drawBitmap(portal,null,new RectF(ReagentShopInteriorDef.EXIT_X-32,ReagentShopInteriorDef.EXIT_Y-16,ReagentShopInteriorDef.EXIT_X+32,ReagentShopInteriorDef.EXIT_Y+16),px);
  }
  private void tile(Canvas c,Bitmap b,float x,float y){if(b==null){px.setColor(0xff5d3b25);Path p=new Path();p.moveTo(x,y-16);p.lineTo(x+32,y);p.lineTo(x,y+16);p.lineTo(x-32,y);p.close();c.drawPath(p,px);return;}c.drawBitmap(b,null,new RectF(x-32,y-16,x+32,y+16),px);}
  private void object(Canvas c,Bitmap b,float x,float foot,float scale){if(b==null)return;float w=b.getWidth()*scale,h=b.getHeight()*scale;c.drawBitmap(b,null,new RectF(x-w/2,foot-h,x+w/2,foot),px);}
  private Bitmap load(String p){if(assets==null)return null;try(InputStream in=assets.open(p)){BitmapFactory.Options o=new BitmapFactory.Options();o.inScaled=false;return BitmapFactory.decodeStream(in,null,o);}catch(Throwable t){return null;}}
}