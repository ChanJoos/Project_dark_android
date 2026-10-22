package com.projectdark.mobile;
import android.graphics.*;
/** User V237: actor-free rebuild from article254766 circular hexagram; authored timing, not recovered footage. */
final class MageDarkMarkV237 {
 private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);private final Path star=new Path();private final RectF ring=new RectF();
 void draw(Canvas c,float age,float duration,float x,float y){
  if(age<0||age>=duration)return;float t=age/duration,alpha=Math.min(1,t/.16f)*Math.min(1,(1-t)/.22f);float scale=.86f+.14f*Math.min(1,t/.3f);c.save();c.translate(x,y-25);c.scale(scale,scale*.86f);p.setStyle(Paint.Style.STROKE);p.setColor(Color.argb((int)(235*alpha),47,36,64));p.setStrokeWidth(1.5f);
  c.drawCircle(0,0,43,p);c.drawCircle(0,0,38,p);
  for(int tri=0;tri<2;tri++){star.reset();for(int i=0;i<3;i++){double a=Math.toRadians(-90+tri*60+i*120);float px=(float)Math.cos(a)*34,py=(float)Math.sin(a)*34;if(i==0)star.moveTo(px,py);else star.lineTo(px,py);}star.close();c.drawPath(star,p);}
  c.drawCircle(0,0,18,p);p.setStrokeWidth(1);
  for(int i=0;i<12;i++){double a=Math.PI*i/6;float cx=(float)Math.cos(a)*40.5f,cy=(float)Math.sin(a)*40.5f;float dx=(float)Math.cos(a),dy=(float)Math.sin(a);c.drawLine(cx-dy*1.2f,cy+dx*1.2f,cx+dy*1.2f,cy-dx*1.2f,p);}
  p.setColor(Color.argb((int)(205*alpha),129,93,158));p.setStrokeWidth(.8f);ring.set(-35,-35,35,35);c.drawArc(ring,-90,Math.min(360,t*600),false,p);
  // The central sigil is geometry only: never a captured person, HP bar, skull or text.
  c.drawLine(0,-13,0,13,p);c.drawLine(-10,-6,10,6,p);c.drawLine(-10,6,10,-6,p);c.drawCircle(0,0,5,p);c.restore();
 }
}
