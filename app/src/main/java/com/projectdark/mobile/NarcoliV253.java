package com.projectdark.mobile;
import android.graphics.*;

/** ADAPTED trace of the supplied 213626 video, not original-client aura pixels.
 * The separate Z sequence retains original EPF pixels. No game state is changed here. */
final class NarcoliV253 {
 private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
 private final Path wave=new Path();
 void draw(Canvas c,float x,float foot,float center,float age){
  float height=Math.max(30,2*(foot-center)),width=Math.max(22,height*.48f);
  c.save();c.translate(x,foot);c.scale(width/28f,height/50f);
  p.setStyle(Paint.Style.STROKE);p.setStrokeCap(Paint.Cap.ROUND);p.setStrokeJoin(Paint.Join.ROUND);
  // Three rising, unbranched S wisps and low flattened rings are visible in the
  // video. Geometry and phase are a reconstruction; they are not archive frames.
  float phase=age%1f;
  for(int i=0;i<3;i++){
   float side=i==0?-1:1,dx=i==0?-29:i==1?24:33;
   float sway=(float)Math.sin((phase+i*.22f)*Math.PI*2)*1.6f;
   wave.reset();wave.moveTo(dx,-12-i*3);
   wave.cubicTo(dx+side*5,-21,dx-side*7+sway,-25,dx-side*2,-31);
   wave.cubicTo(dx+side*6+sway,-37,dx-side*5,-41,dx-side*3,-49-i*3);
   p.setStrokeWidth(3);p.setColor(0x245c285d);c.drawPath(wave,p);
   p.setStrokeWidth(1.25f);p.setColor(0x99bd7aaf);c.drawPath(wave,p);
  }
  for(int i=0;i<3;i++){
   float pulse=(phase+i/3f)%1f,r=20+pulse*13,cy=-1+pulse*5;
   p.setStrokeWidth(1.2f);p.setColor(Color.argb((int)(105*(1-pulse)),181,112,166));
   c.drawOval(new RectF(-r,cy-r*.20f,r,cy+r*.20f),p);
  }
  c.restore();
 }
}
