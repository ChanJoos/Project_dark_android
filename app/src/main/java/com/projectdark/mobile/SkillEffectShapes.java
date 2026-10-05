package com.projectdark.mobile;

import android.graphics.*;

/** Reconstructed pixel effects. Source-described shape/color is separate from unverified original pixels. */
final class SkillEffectShapes {
  static void drawDirected(Canvas c,Paint p,String name,float x,float y,float t,float dx,float dy){
    if(!"QI_BLAST".equals(name)){draw(c,p,name,x,y,t);return;}
    c.save();c.translate(Math.round(x),Math.round(y-24));c.rotate((float)Math.toDegrees(Math.atan2(dy,dx)));
    int alpha=Math.round(255*Math.min(1,(1-t)*4));float travel=10+18*Math.min(1,t*2);
    p.setStyle(Paint.Style.STROKE);p.setStrokeCap(Paint.Cap.SQUARE);p.setColor(0xff45d9ff);p.setAlpha(alpha/2);p.setStrokeWidth(10);c.drawLine(-travel-18,0,-5,0,p);
    p.setColor(0xffa9f5ff);p.setAlpha(alpha);p.setStrokeWidth(4);c.drawLine(-travel-12,0,-3,0,p);p.setStyle(Paint.Style.FILL);p.setStrokeCap(Paint.Cap.BUTT);
    Path diamond=new Path();diamond.moveTo(8,0);diamond.lineTo(-1,-7);diamond.lineTo(-7,0);diamond.lineTo(-1,7);diamond.close();p.setColor(0xffdffcff);p.setAlpha(alpha);c.drawPath(diamond,p);
    float r=8+12*Math.min(1,t*2);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2);p.setColor(0xff45d9ff);p.setAlpha(alpha*3/4);c.drawCircle(0,0,r,p);p.setStyle(Paint.Style.FILL);
    c.restore();p.setAlpha(255);p.setStyle(Paint.Style.FILL);
  }
  static void drawCasterStream(Canvas c,Paint p,float x,float y,float t,float dx,float dy){
    c.save();c.translate(Math.round(x),Math.round(y-24));c.rotate((float)Math.toDegrees(Math.atan2(dy,dx)));
    int alpha=Math.round(255*Math.min(1,(1-t)*4));float phase=t*42f;
    p.setStyle(Paint.Style.STROKE);p.setStrokeCap(Paint.Cap.ROUND);p.setColor(0xffe24961);p.setAlpha(alpha/2);p.setStrokeWidth(7);c.drawLine(4,0,37,0,p);
    p.setColor(0xffff8792);p.setAlpha(alpha);p.setStrokeWidth(2);c.drawLine(4,0,37,0,p);p.setStyle(Paint.Style.FILL);p.setStrokeCap(Paint.Cap.BUTT);
    for(int i=0;i<5;i++){float px=8+((i*9+phase)%32),py=(float)Math.sin(i*2.1+t*9)*3;pixel(c,p,px,py,3,i%2==0?0xffff526e:0xffffd4d8,alpha);}
    pixel(c,p,3,0,4,0xffffd4d8,alpha);c.restore();p.setAlpha(255);p.setStyle(Paint.Style.FILL);
  }
  static void draw(Canvas c,Paint p,String name,float x,float y,float t){
    c.save();c.translate(Math.round(x),Math.round(y-24));
    p.setStyle(Paint.Style.FILL);int alpha=Math.round(255*Math.min(1,(1-t)*4));
    if(name.startsWith("BLADE_")){
      int color=name.endsWith("GREEN")?0xff8af05e:name.endsWith("RED")?0xffff5353:0xffeefbff;
      for(int i=0;i<18;i++){float a=(i/17f*2.5f+t*.7f);float r=20+t*12;pixel(c,p,(float)Math.cos(a)*r,(float)Math.sin(a)*r*.65f-4,3,color,alpha);}
    }else if(name.equals("DEFENSE_SPHERE")){
      // Thin translucent spherical shell observed in the supplied original combat recording.
      float r=22f*(.75f+.25f*Math.min(1,t*5));
      p.setShader(new RadialGradient(0,0,r,new int[]{0x0047a8ef,0x0847a8ef,0x385ba9f4,0x887ad9ff},new float[]{0,.7f,.93f,1},Shader.TileMode.CLAMP));
      p.setAlpha(alpha);c.drawOval(new RectF(-r,-r*1.16f,r,r*1.16f),p);p.setShader(null);
      p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1f);p.setColor(0xff7ecaff);p.setAlpha(alpha*3/4);
      c.drawOval(new RectF(-r,-r*1.16f,r,r*1.16f),p);
      p.setStrokeWidth(2f);p.setColor(0xffe9fcff);p.setAlpha(alpha);
      c.drawArc(new RectF(-r+1,-r*1.16f+1,r-1,r*1.16f-1),205,42,false,p);
      p.setStyle(Paint.Style.FILL);
    }else if(name.startsWith("CRASHER_")){
      // Three hooked cyan/red strokes around recipient; screenshot-backed adapted animation.
      int color=name.endsWith("RED")?0xfffa3967:0xff35caff;
      float growth=Math.min(1,t*4),r=29*growth;
      c.save();c.rotate(t*115);
      for(int arm=0;arm<3;arm++){
        c.save();c.rotate(arm*120);Path path=new Path();
        path.moveTo(-r*.38f,-r*.88f);path.cubicTo(r*.45f,-r*1.14f,r*.95f,-r*.12f,r*.55f,r*.55f);
        path.cubicTo(r*.25f,r*.94f,-r*.12f,r*.7f,-r*.1f,r*.43f);
        p.setStyle(Paint.Style.STROKE);p.setStrokeCap(Paint.Cap.ROUND);p.setColor(color);p.setAlpha(alpha/3);p.setStrokeWidth(7);c.drawPath(path,p);
        p.setAlpha(alpha);p.setStrokeWidth(3);c.drawPath(path,p);
        p.setColor(0xffedfbff);p.setStrokeWidth(1);c.drawPath(path,p);c.restore();
      }
      c.restore();p.setStrokeCap(Paint.Cap.BUTT);p.setStyle(Paint.Style.FILL);
    }else if(name.equals("DARK_STAR")){
      // User confirms the original star motif; geometry is reconstructed, not recovered pixels.
      // Visible at contact (t=0), centered on each damage recipient, without a late buildup.
      float radius=27+5*(float)Math.sin(Math.min(1,t)*Math.PI);
      Path star=new Path();
      for(int i=0;i<10;i++){
        double angle=-Math.PI/2+i*Math.PI/5;float r=i%2==0?radius:radius*.42f;
        float sx=(float)Math.cos(angle)*r,sy=(float)Math.sin(angle)*r;
        if(i==0)star.moveTo(sx,sy);else star.lineTo(sx,sy);
      }
      star.close();p.setColor(0xffb787ef);p.setAlpha(alpha/4);c.drawPath(star,p);
      p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(5);p.setAlpha(alpha/3);c.drawPath(star,p);
      p.setColor(0xffe6d5ff);p.setStrokeWidth(2);p.setAlpha(alpha);c.drawPath(star,p);
      p.setStyle(Paint.Style.FILL);
    }else if(name.equals("METEOR")){
      float f=Math.min(1,t*2),mx=-36*(1-f),my=-75*(1-f);
      for(int i=0;i<8;i++)pixel(c,p,mx-i*3,my-i*6,6-i*.5f,0xffed8132,alpha);
      pixel(c,p,mx,my,9,0xfffce399,alpha);
      if(t>.45f)burst(c,p,28*(t-.45f)*2,0xffef7b2c,alpha,0);
    }else if(name.equals("FREEZE")){
      for(int k=-2;k<=2;k++)for(int j=0;j<12;j++){float xx=k*6+j*.35f,yy=15-j*3;pixel(c,p,xx,yy,3,0xff88daf5,alpha);}
    }else if(name.equals("DARK_DRAGON")){
      // Newly reconstructed silhouette; not an extracted or authenticated original dragon sprite.
      for(int i=0;i<38;i++){float yy=-46+i*1.7f,xx=(float)Math.sin(i*.22f+t*8)*13;pixel(c,p,xx,yy,5,0xff8f4abd,alpha);}
      pixel(c,p,4,-44,9,0xffbb8edf,alpha);pixel(c,p,8,-46,2,0xffeee8ff,alpha);
      for(int i=0;i<9;i++){pixel(c,p,-10-i*3,-25+i,3,0xff754498,alpha);pixel(c,p,10+i*3,-25+i,3,0xff754498,alpha);}
    }else if(name.equals("AMBUSH")||name.equals("BACKSTAB")||name.equals("ASSASSIN")){
      int count=name.equals("AMBUSH")?1:name.equals("BACKSTAB")?2:3;
      int color=name.equals("ASSASSIN")?0xffec6175:name.equals("BACKSTAB")?0xffb1a8f2:0xffe7e9ff;
      for(int k=0;k<count;k++)for(int i=0;i<20;i++){float v=i-10;pixel(c,p,v*2,v*(k%2==0?-1.1f:1.1f)-k*5,3,color,alpha);}
    }else if(name.equals("CONFUSION")){
      for(int i=0;i<6;i++){float a=i*1.05f+t*8;pixel(c,p,(float)Math.cos(a)*13,-23+(float)Math.sin(a)*5,3,0xfff0d874,alpha);}
    }else{
      int color=name.equals("DARK_BURST")?0xff914ebb:name.equals("SOUL_BURST")?0xffb862dc:name.equals("DARA")?0xfff4cc5d:0xffb1e8ef;
      burst(c,p,8+22*t,color,alpha,name.equals("DARA")?t*8:0);
    }
    c.restore();p.setAlpha(255);p.setStyle(Paint.Style.FILL);
  }
  private static void burst(Canvas c,Paint p,float r,int color,int alpha,float spin){for(int i=0;i<12;i++){float a=i*.5236f+spin;for(int j=0;j<4;j++)pixel(c,p,(float)Math.cos(a)*(r+j*2),(float)Math.sin(a)*(r+j*2)*.85f,3-j*.4f,color,alpha);}pixel(c,p,0,0,5,0xfff0e9dc,alpha);}
  private static void pixel(Canvas c,Paint p,float x,float y,float size,int color,int alpha){p.setColor(color);p.setAlpha(alpha);x=Math.round(x);y=Math.round(y);c.drawRect(x,y,x+size,y+size,p);}
}
