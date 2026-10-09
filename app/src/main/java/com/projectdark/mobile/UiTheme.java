package com.projectdark.mobile;

import android.content.Context;
import android.graphics.*;

/** Shared presentation-only system. No domain, inventory or combat authority. */
final class UiTheme {
  static final int BG=0xff352416, SURFACE=0xff523921, RAISED=0xff694b2c, LINE=0xff8a623b;
  static final int TEXT=0xffeee0bd, MUTED=0xffc2aa83, GOLD=0xffe0b66d, ACCENT=0xfff0cc78, GOOD=0xffaaca78, BAD=0xffef9676;
  private static Typeface regular=Typeface.create("sans-serif",0), semibold=Typeface.create("sans-serif-medium",0);
  private static final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
  static void install(Context c){regular=Typeface.createFromAsset(c.getAssets(),"ui-fonts/Pretendard-Regular.otf");semibold=Typeface.createFromAsset(c.getAssets(),"ui-fonts/Pretendard-SemiBold.otf");ClassicUiSkin.install(c);}
  static Typeface font(boolean bold){return bold?semibold:regular;}
  static void scrim(Canvas c){c.drawColor(0x7d1b120b);}
  static void surface(Canvas c,RectF r,int fill,int border,float radius){p.setShader(null);p.setStyle(Paint.Style.FILL);if(fill==BG||fill==SURFACE||fill==RAISED)ClassicUiSkin.texture(c,r,fill==BG);else{p.setColor(fill);c.drawRect(r,p);}if(border!=0){line(c,r.left,r.top,r.right,r.top,border);line(c,r.left,r.top,r.left,r.bottom,border);line(c,r.right,r.top,r.right,r.bottom,0xff291b10);line(c,r.left,r.bottom,r.right,r.bottom,0xff291b10);}}
  static void panel(Canvas c,RectF r,String title){ClassicUiSkin.texture(c,r,false);ClassicUiSkin.frame(c,r);RectF head=new RectF(r.left+7,r.top+7,r.right-7,r.top+32);ClassicUiSkin.texture(c,head,true);line(c,r.left+7,r.top+32,r.right-7,r.top+32,LINE);text(c,title,r.left+16,r.top+25,16,GOLD,false);}
  static void text(Canvas c,String s,float x,float y,float size,int color,boolean bold){p.setShader(null);p.setStyle(Paint.Style.FILL);p.setTextAlign(Paint.Align.LEFT);p.setTypeface(font(bold));p.setTextSize(size);p.setColor(color);c.drawText(s,x,y,p);}
  static void center(Canvas c,String s,float x,float y,float size,int color,boolean bold){p.setTypeface(font(bold));p.setTextSize(size);text(c,s,x-p.measureText(s)/2,y,size,color,bold);}
  static void right(Canvas c,String s,float x,float y,float size,int color,boolean bold){p.setTypeface(font(bold));p.setTextSize(size);text(c,s,x-p.measureText(s),y,size,color,bold);}
  static void fit(Canvas c,String s,float x,float y,float width,float size,int color,boolean bold){p.setTypeface(font(bold));p.setTextSize(size);if(p.measureText(s)>width){int n=p.breakText(s,true,Math.max(0,width-p.measureText("…")),null);s=s.substring(0,Math.max(0,n))+"…";}text(c,s,x,y,size,color,bold);}
  static void button(Canvas c,RectF r,String s,boolean active,boolean primary){surface(c,r,active?RAISED:BG,active&&primary?GOLD:LINE,0);if(active){line(c,r.left+1,r.top+1,r.right-1,r.top+1,0xffbd925b);line(c,r.left+1,r.top+1,r.left+1,r.bottom-1,0xffbd925b);}center(c,s,r.centerX(),r.centerY()+4.3f,12,active?TEXT:MUTED,false);}
  static void tab(Canvas c,RectF r,String s,boolean selected){surface(c,r,selected?RAISED:BG,LINE,0);center(c,s,r.centerX(),r.centerY()+4,12,selected?GOLD:MUTED,false);if(selected)line(c,r.left+2,r.bottom-2,r.right-2,r.bottom-2,ACCENT);}
  static void line(Canvas c,float x,float y,float xx,float yy,int color){p.setShader(null);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1);p.setColor(color);c.drawLine(x,y,xx,yy,p);p.setStyle(Paint.Style.FILL);}
  static void slot(Canvas c,RectF r,boolean selected,boolean equipped){ClassicUiSkin.texture(c,r,false);line(c,r.left,r.top,r.right,r.top,selected?ACCENT:0xff6b4f30);line(c,r.left,r.top,r.left,r.bottom,selected?ACCENT:0xff6b4f30);line(c,r.right,r.top,r.right,r.bottom,0xff3e2c1a);line(c,r.left,r.bottom,r.right,r.bottom,0xff3e2c1a);if(selected){p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1);p.setColor(ACCENT);c.drawRect(new RectF(r.left+1,r.top+1,r.right-1,r.bottom-1),p);p.setStyle(Paint.Style.FILL);}if(equipped){p.setColor(GOOD);c.drawCircle(r.right-7,r.top+7,2,p);}}
  static void close(Canvas c,float x,float y){button(c,new RectF(x-12,y-11,x+12,y+11),"×",true,false);}
  static void utility(Canvas c,float x,float y,String icon,boolean active,float radius){surface(c,new RectF(x-radius,y-radius,x+radius,y+radius),active?0xed694b2c:0xeb352416,active?ACCENT:LINE,10);glyph(c,icon,x,y,9,active?ACCENT:GOLD);}
  /** Empty equipment sockets use centered silhouettes, never text labels. */
  static void equipmentGlyph(Canvas c,String slot,float x,float y,float size,int color){
    c.save();c.translate(x,y);c.scale(size/10,size/10);
    p.setShader(null);p.setColor(color);p.setStyle(Paint.Style.STROKE);
    p.setStrokeWidth(1.25f);p.setStrokeCap(Paint.Cap.ROUND);p.setStrokeJoin(Paint.Join.ROUND);
    Path q=new Path();
    switch(slot){
      case "귀걸이":
        c.drawCircle(0,-7,1.4f,p);c.drawOval(new RectF(-5,-4,5,8),p);break;
      case "목걸이":
        q.moveTo(-7,-8);q.cubicTo(-9,-1,-5,4,0,5);q.cubicTo(5,4,9,-1,7,-8);c.drawPath(q,p);
        q.reset();q.moveTo(0,3);q.lineTo(3,7);q.lineTo(0,10);q.lineTo(-3,7);q.close();c.drawPath(q,p);break;
      case "갑옷":
        q.moveTo(-4,-8);q.lineTo(-8,-5);q.lineTo(-6,0);q.lineTo(-4,-1);q.lineTo(-5,8);q.lineTo(5,8);q.lineTo(4,-1);q.lineTo(6,0);q.lineTo(8,-5);q.lineTo(4,-8);q.quadTo(0,-3,-4,-8);q.close();c.drawPath(q,p);
        c.drawLine(-4,4,4,4,p);break;
      case "모자":
        q.moveTo(-7,5);q.lineTo(-7,-1);q.cubicTo(-7,-10,7,-10,7,-1);q.lineTo(7,5);q.lineTo(3,8);q.lineTo(2,2);q.lineTo(-2,2);q.lineTo(-3,8);q.close();c.drawPath(q,p);
        c.drawLine(0,-7,0,-1,p);break;
      case "날개":
        q.moveTo(-7,8);q.cubicTo(-10,-1,-5,-8,7,-8);q.quadTo(4,-5,1,-3);q.lineTo(6,-3);q.lineTo(0,2);q.lineTo(4,2);q.lineTo(-3,7);q.close();c.drawPath(q,p);
        c.drawLine(-7,8,2,-4,p);break;
      case "무기":
        q.moveTo(-5,5);q.lineTo(4,-7);q.lineTo(8,-9);q.lineTo(7,-4);q.lineTo(-3,7);q.close();c.drawPath(q,p);
        c.drawLine(-7,2,1,8,p);c.drawLine(-5,5,-8,9,p);break;
      case "방패":
        q.moveTo(0,-9);q.lineTo(7,-6);q.lineTo(6,2);q.quadTo(5,6,0,9);q.quadTo(-5,6,-6,2);q.lineTo(-7,-6);q.close();c.drawPath(q,p);
        c.drawLine(0,-5,0,5,p);c.drawLine(-4,0,4,0,p);break;
      case "장갑":
        q.moveTo(-5,8);q.lineTo(-6,1);q.lineTo(-9,-3);q.quadTo(-9,-6,-6,-4);q.lineTo(-4,-1);q.lineTo(-4,-7);q.quadTo(-3,-10,-2,-7);q.lineTo(-2,-2);q.lineTo(-2,-8);q.quadTo(-1,-11,0,-8);q.lineTo(0,-2);q.lineTo(0,-7);q.quadTo(1,-10,2,-7);q.lineTo(2,-1);q.lineTo(2,-5);q.quadTo(4,-8,5,-5);q.lineTo(5,3);q.lineTo(3,8);q.close();c.drawPath(q,p);
        c.drawLine(-5,5,4,5,p);break;
      case "벨트":
        c.drawRoundRect(new RectF(-9,-3,9,4),2,2,p);c.drawRoundRect(new RectF(-3,-4,3,5),1,1,p);c.drawLine(-1,0,2,0,p);break;
      case "각반":
        q.moveTo(-7,-8);q.lineTo(7,-8);q.lineTo(6,8);q.lineTo(1,8);q.lineTo(0,-1);q.lineTo(-1,8);q.lineTo(-6,8);q.close();c.drawPath(q,p);
        c.drawLine(-6,-4,6,-4,p);break;
      case "신발":
        q.moveTo(-5,-8);q.lineTo(3,-8);q.lineTo(3,1);q.lineTo(8,5);q.quadTo(10,8,7,9);q.lineTo(-7,9);q.lineTo(-7,3);q.lineTo(-5,1);q.close();c.drawPath(q,p);
        c.drawLine(-7,6,7,6,p);c.drawLine(-5,-4,3,-4,p);break;
    }
    p.setStyle(Paint.Style.FILL);p.setStrokeCap(Paint.Cap.BUTT);c.restore();
  }
  /** Consistent 1.5px line illustration, rendered at native resolution. */
  static void glyph(Canvas c,String kind,float x,float y,float size,int color){c.save();c.translate(x,y);c.scale(size/10,size/10);p.setShader(null);p.setColor(color);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1.5f);p.setStrokeCap(Paint.Cap.ROUND);p.setStrokeJoin(Paint.Join.ROUND);Path q=new Path();switch(kind){
    case "bag":c.drawRoundRect(new RectF(-7,-5,7,8),2,2,p);c.drawArc(new RectF(-4,-10,4,-2),180,180,false,p);c.drawLine(-3,1,3,1,p);break;
    case "stats":c.drawCircle(0,-5,3,p);c.drawArc(new RectF(-7,-1,7,12),180,180,false,p);c.drawLine(-7,5,7,5,p);break;
    case "gear":q.moveTo(-4,-9);q.lineTo(-8,-5);q.lineTo(-5,0);q.lineTo(-4,-1);q.lineTo(-4,8);q.lineTo(4,8);q.lineTo(4,-1);q.lineTo(5,0);q.lineTo(8,-5);q.lineTo(4,-9);q.lineTo(0,-6);q.close();c.drawPath(q,p);break;
    case "skill":q.moveTo(-7,-6);q.lineTo(-1,-8);q.lineTo(0,-5);q.lineTo(1,-8);q.lineTo(7,-6);q.lineTo(7,6);q.lineTo(1,8);q.lineTo(0,6);q.lineTo(-1,8);q.lineTo(-7,6);q.close();c.drawPath(q,p);c.drawLine(0,-5,0,6,p);break;
    case "quest":c.drawRoundRect(new RectF(-6,-9,6,9),2,2,p);for(int i=0;i<3;i++)c.drawLine(-2,-4+i*5,3,-4+i*5,p);break;
    case "potion":c.drawRoundRect(new RectF(-3,-9,3,-5),1,1,p);q.moveTo(-3,-5);q.lineTo(-3,-2);q.cubicTo(-9,2,-6,9,0,9);q.cubicTo(6,9,9,2,3,-2);q.lineTo(3,-5);c.drawPath(q,p);c.drawLine(-4,3,4,3,p);break;
    case "attack":q.moveTo(-6,7);q.lineTo(5,-7);q.lineTo(8,-9);q.lineTo(7,-5);q.lineTo(-4,9);c.drawPath(q,p);c.drawLine(-6,2,1,8,p);break;
    case "auto":c.drawArc(new RectF(-8,-8,8,8),-35,285,false,p);q.moveTo(5,-8);q.lineTo(8,-4);q.lineTo(3,-3);c.drawPath(q,p);break;
    default:c.drawCircle(-4,0,1,p);c.drawCircle(0,0,1,p);c.drawCircle(4,0,1,p);
  }p.setStyle(Paint.Style.FILL);p.setStrokeCap(Paint.Cap.BUTT);c.restore();}
  static int ink(int old){if(old==LINE||old==TEXT||old==MUTED||old==GOLD||old==ACCENT||old==GOOD||old==BAD)return old;int rgb=old&0xffffff;if(rgb==0xcfc0a9||rgb==0xcfbda1||rgb==0xc7b59a||rgb==0xbcae98)return MUTED;if(rgb==0x6dda59||rgb==0xa9c99b||rgb==0x9acbae)return GOOD;if(rgb==0xed8074||rgb==0xdc9b87)return BAD;int rr=(rgb>>16)&255,gg=(rgb>>8)&255,bb=rgb&255;if(rr>=210&&gg>=180)return TEXT;if(rr>gg+12&&gg>bb+12&&rr>=180)return GOLD;return MUTED;}
}
