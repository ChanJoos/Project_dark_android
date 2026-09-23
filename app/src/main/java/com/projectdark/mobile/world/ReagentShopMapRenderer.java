package com.projectdark.mobile.world;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;

/** Lightweight interior renderer using the same 64x32 world/camera contract as Milles. */
public final class ReagentShopMapRenderer {
  private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
  public void draw(Canvas c,ReagentShopWorldRuntime w){
    p.setStyle(Paint.Style.FILL);p.setColor(0xff16130f);c.drawRect(0,0,c.getWidth(),c.getHeight(),p);
    for(ReagentShopMapDef.Tile t:ReagentShopMapDef.tiles())tile(c,w,t.centerX,t.centerY);
    fixture(c,w,304f,224f,656f,264f,0xff5b3d27);
    fixture(c,w,160f,128f,224f,288f,0xff493326);
    fixture(c,w,736f,128f,800f,288f,0xff493326);
    fixture(c,w,160f,96f,800f,128f,0xff34251d);
    WorldCameraTransform.Point npc=w.worldToScreen(ReagentShopMapDef.MERLIN_X,ReagentShopMapDef.MERLIN_Y);
    p.setColor(0xffd7c49a);c.drawCircle(npc.x,npc.y-22f,7f,p);p.setColor(0xff765d42);c.drawRect(npc.x-7,npc.y-15,npc.x+7,npc.y,p);
  }
  private void tile(Canvas c,ReagentShopWorldRuntime w,float x,float y){
    WorldCameraTransform.Point q=w.worldToScreen(x,y);Path d=new Path();d.moveTo(q.x,q.y-16f);d.lineTo(q.x+32f,q.y);d.lineTo(q.x,q.y+16f);d.lineTo(q.x-32f,q.y);d.close();
    p.setColor(0xff6b5a43);p.setStyle(Paint.Style.FILL);c.drawPath(d,p);p.setColor(0xff3d3327);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1f);c.drawPath(d,p);p.setStyle(Paint.Style.FILL);
  }
  private void fixture(Canvas c,ReagentShopWorldRuntime w,float l,float t,float r,float b,int color){
    WorldCameraTransform.Point a=w.worldToScreen(l,t),z=w.worldToScreen(r,b);p.setColor(color);c.drawRect(new RectF(a.x,a.y,z.x,z.y),p);
  }
}