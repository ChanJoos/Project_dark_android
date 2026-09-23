package com.projectdark.mobile.world;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.*;
import java.io.InputStream;

/** Reagent-shop renderer on the canonical World camera/grid. All visible geometry is asset-backed. */
public final class ReagentShopInteriorRenderer {
  private final Paint px=new Paint(); private final AssetManager assets;
  private Bitmap floor,wall,counter,barrel,crate,portal;
  public ReagentShopInteriorRenderer(Context c){
    px.setAntiAlias(false);px.setFilterBitmap(false);px.setDither(false);assets=c==null?null:c.getAssets();
    floor=load("interiors/reagent_shop/OBJ_reagent_wood_floor.webp");
    wall=load("interiors/reagent_shop/OBJ_reagent_wall.webp");
    counter=load("interiors/reagent_shop/OBJ_reagent_counter.webp");
    barrel=load("storage/OBJ_barrel.png");crate=load("storage/OBJ_crate.png");
    portal=load("assets/world/portal/portal_reagent_shop.webp");
  }
  public boolean ready(){return floor!=null&&wall!=null&&counter!=null&&portal!=null;}
  public void draw(Canvas c,WorldRuntimeAdapter world){
    if(c==null||world==null)return;c.drawColor(0xff17100b);
    for(WorldMoveTargetController.TileCenter t:ReagentShopInteriorDef.navigationTiles())drawTile(c,world,floor,t.x,t.y);
    // Rear wall: asset panels aligned to the same world coordinates.
    for(float x=224f;x<=736f;x+=64f)drawFoot(c,world,wall,x,256f,1f);
    // Straight counter separating Merlin from the customer floor.
    for(float x=288f;x<=672f;x+=64f)drawFoot(c,world,counter,x,288f,1f);
    drawFoot(c,world,crate,240f,286f,.70f);drawFoot(c,world,barrel,720f,286f,.72f);
    drawPortal(c,world,ReagentShopInteriorDef.EXIT_X,ReagentShopInteriorDef.EXIT_Y);
  }
  private void drawTile(Canvas c,WorldRuntimeAdapter w,Bitmap b,float wx,float wy){
    if(b==null)return;WorldCameraTransform.Point q=w.worldToScreen(wx,wy);
    c.drawBitmap(b,null,new RectF(Math.round(q.x-32),Math.round(q.y-16),Math.round(q.x+32),Math.round(q.y+16)),px);
  }
  private void drawPortal(Canvas c,WorldRuntimeAdapter w,float wx,float wy){
    if(portal==null)return;WorldCameraTransform.Point q=w.worldToScreen(wx,wy);
    c.drawBitmap(portal,null,new RectF(Math.round(q.x-32),Math.round(q.y-16),Math.round(q.x+32),Math.round(q.y+16)),px);
  }
  private void drawFoot(Canvas c,WorldRuntimeAdapter w,Bitmap b,float wx,float wy,float scale){
    if(b==null)return;WorldCameraTransform.Point q=w.worldToScreen(wx,wy);float ww=b.getWidth()*scale,hh=b.getHeight()*scale;
    c.drawBitmap(b,null,new RectF(Math.round(q.x-ww/2),Math.round(q.y-hh),Math.round(q.x+ww/2),Math.round(q.y)),px);
  }
  private Bitmap load(String p){if(assets==null)return null;try(InputStream in=assets.open(p)){BitmapFactory.Options o=new BitmapFactory.Options();o.inScaled=false;return BitmapFactory.decodeStream(in,null,o);}catch(Throwable t){return null;}}
}