package com.projectdark.mobile.world;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

/** Lightweight authored presentation for the first field slice; geometry is explicitly ADAPTED/B. */
public final class PoteFieldRenderer {
  private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
  public void draw(Canvas c,WorldRuntimeAdapter w){
    if(c==null||w==null)return;
    c.drawColor(0xff315f2c);
    // grass checker gives the field readable tile rhythm without pretending to be original art.
    for(float y=96;y<=480;y+=32)for(float x=128;x<=832;x+=64){
      WorldCameraTransform.Point q=w.worldToScreen(x,y);
      p.setColor((((int)(x/64)+(int)(y/32))&1)==0?0xff4f7d39:0xff477335);
      c.drawOval(new RectF(q.x-34,q.y-16,q.x+34,q.y+16),p);
    }
    // main return trail: field interior -> Milles gate.
    p.setColor(0xff9b743b);
    for(float y=288;y<=480;y+=32){WorldCameraTransform.Point q=w.worldToScreen(480,y);c.drawOval(new RectF(q.x-42,q.y-18,q.x+42,q.y+18),p);}
    // boundary groves correspond to collision blocks.
    p.setColor(0xff244b27);
    for(RectF r:PoteFieldDef.obstacles()){
      WorldCameraTransform.Point a=w.worldToScreen(r.left,r.top),b=w.worldToScreen(r.right,r.bottom);
      c.drawOval(new RectF(Math.min(a.x,b.x)-20,Math.min(a.y,b.y)-18,Math.max(a.x,b.x)+20,Math.max(a.y,b.y)+18),p);
    }
    WorldCameraTransform.Point exit=w.worldToScreen(PoteFieldDef.EXIT_X,PoteFieldDef.EXIT_Y);
    p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(3f);p.setColor(0xffffc86b);c.drawOval(new RectF(exit.x-24,exit.y-11,exit.x+24,exit.y+11),p);p.setStyle(Paint.Style.FILL);
    p.setColor(Color.WHITE);p.setTextSize(11f);c.drawText("밀레스",exit.x-17,exit.y+30,p);
  }
}
