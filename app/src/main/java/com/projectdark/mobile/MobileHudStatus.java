package com.projectdark.mobile;
import android.graphics.*;
/** Approved B floating resource bars. */
final class MobileHudStatus {
 private static final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
 private static final Path path=new Path();
 static void draw(Canvas c,RuntimeState state,float offset){
  float d=offset*.5f,x=376+d,y=497;
  path.reset();path.moveTo(x,y-20);path.lineTo(x+19,y-10);path.lineTo(x+22,y+7);path.lineTo(x,y+23);path.lineTo(x-22,y+7);path.lineTo(x-19,y-10);path.close();
  p.setStyle(Paint.Style.FILL);p.setShader(new LinearGradient(x-18,y-20,x+18,y+23,0xe52e3432,0xef12191b,Shader.TileMode.CLAMP));c.drawPath(path,p);p.setShader(null);
  p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(.9f);p.setColor(0xffb7aa7f);c.drawPath(path,p);p.setStyle(Paint.Style.FILL);
  UiTheme.center(c,"LV",x,y-5,6.5f,0xffc7bb92,true);Integer lv=state.rpg().normalLevel();UiTheme.center(c,lv==null?"?":lv.toString(),x,y+12,15,UiTheme.TEXT,true);
  resource(c,new RectF(405+d,480,602+d,493),state.player().hp,state.player().maxHp,true);
  resource(c,new RectF(405+d,499,602+d,512),state.player().mp,state.player().maxMp,false);
  Float ratio=PostF5mHudPresentation.expRatio(state.rpg());float q=ratio==null?0:Math.max(0,Math.min(1,ratio));
  UiTheme.text(c,"EXP",405+d,527,7,UiTheme.GOLD,true);
  RectF r=new RectF(429+d,520,571+d,526);p.setColor(0xdd131b1b);c.drawRoundRect(r,3,3,p);
  if(q>0){p.setShader(new LinearGradient(r.left,r.top,r.left,r.bottom,0xffffdf84,0xffb48a3f,Shader.TileMode.CLAMP));c.drawRoundRect(r.left,r.top,r.left+r.width()*q,r.bottom,3,3,p);p.setShader(null);}
  p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(.6f);p.setColor(0xff938773);c.drawRoundRect(r,3,3,p);p.setStyle(Paint.Style.FILL);
  UiTheme.right(c,Math.round(q*100)+"%",602+d,527,7.5f,UiTheme.GOLD,true);
 }
 private static void resource(Canvas c,RectF r,int value,int max,boolean hp){
  path.reset();path.moveTo(r.left+6,r.top);path.lineTo(r.right-6,r.top);path.lineTo(r.right,r.centerY());path.lineTo(r.right-6,r.bottom);path.lineTo(r.left+6,r.bottom);path.lineTo(r.left,r.centerY());path.close();
  p.setShader(null);p.setStyle(Paint.Style.FILL);p.setColor(0xf0121b20);c.drawPath(path,p);
  c.save();c.clipPath(path);float q=Math.max(0,Math.min(1,value/(float)Math.max(1,max)));
  p.setShader(new LinearGradient(0,r.top,0,r.bottom,hp?new int[]{0xffee898c,0xffa53842,0xff6c202b}:new int[]{0xff9bd5ef,0xff397bc4,0xff254778},new float[]{0,.45f,1},Shader.TileMode.CLAMP));c.drawRect(r.left+2,r.top+2,r.left+2+(r.width()-4)*q,r.bottom-2,p);p.setShader(null);c.restore();
  p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(.9f);p.setColor(0xffb0aa94);c.drawPath(path,p);p.setStrokeWidth(.45f);p.setColor(0x99f1e5c0);c.drawLine(r.left+7,r.top+1,r.right-7,r.top+1,p);p.setStyle(Paint.Style.FILL);
  p.setShadowLayer(1.2f,0,1,0xff000000);p.setColor(0xfff9f4e4);p.setTextSize(8);p.setTypeface(UiTheme.font(true));String label=value+" / "+max;c.drawText(label,r.centerX()-p.measureText(label)/2,r.centerY()+3,p);p.clearShadowLayer();
 }
}
