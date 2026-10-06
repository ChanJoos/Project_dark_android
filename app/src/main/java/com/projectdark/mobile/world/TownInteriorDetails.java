package com.projectdark.mobile.world;
import android.graphics.*;
/** Small pixel-scale set dressing. Authored stand-ins, never represented as original client art. */
public final class TownInteriorDetails {
 private final Paint p=new Paint();
 private void poly(Canvas c,int color,float... xy){p.setColor(color);Path a=new Path();a.moveTo(xy[0],xy[1]);for(int i=2;i<xy.length;i+=2)a.lineTo(xy[i],xy[i+1]);a.close();c.drawPath(a,p);}
 private void line(Canvas c,int color,float width,float x,float y,float xx,float yy){p.setColor(color);p.setStrokeWidth(width);c.drawLine(x,y,xx,yy,p);}
 private void rect(Canvas c,int color,float l,float t,float r,float b){p.setColor(color);c.drawRect(l,t,r,b,p);}
 private void oval(Canvas c,int color,float l,float t,float r,float b){p.setColor(color);c.drawOval(l,t,r,b,p);}
 private void slab(Canvas c,int top,int front,int side,float x,float y,float w,float h){
  poly(c,front,x-w,y,x,y+w*.45f,x,y+w*.45f-h,x-w,y-h);
  poly(c,side,x,y+w*.45f,x+w,y,x+w,y-h,x,y+w*.45f-h);
  poly(c,top,x-w,y-h,x,y-w*.45f-h,x+w,y-h,x,y+w*.45f-h);
 }
 private void book(Canvas c,float x,float y){
  slab(c,0xffded0a6,0xff776447,0xff5a4731,x,y,14,3);
  poly(c,0xffeee0b7,x-13,y-4,x,y-10,x,y+3,x-13,y-3);
  poly(c,0xffc9ba94,x,y-10,x+13,y-4,x+13,y-3,x,y+3);
  line(c,0xff796044,1,x,y-10,x,y+3);
  for(int i=0;i<4;i++){line(c,0xff948361,1,x-10,y-5+i*2,x-2,y-1+i*2);line(c,0xff827452,1,x+2,y-1+i*2,x+10,y-5+i*2);}
  line(c,0xffa33f34,1,x+3,y+1,x+3,y+8);
 }
 private void bottle(Canvas c,float x,float y,int color,int h){
  rect(c,0xff262b23,x-3,y-h+4,x+4,y);rect(c,color,x-2,y-h+4,x+3,y-1);
  rect(c,0xffbba379,x-1,y-h,x+2,y-h+3);rect(c,0xff453328,x-1,y-h-1,x+2,y-h);
  rect(c,0xffded9b2,x-2,y-7,x+3,y-4);rect(c,0xffc8e5cd,x-1,y-h+5,x,y-8);
  line(c,0x70531915,1,x-2,y,x+3,y);
 }
 private void candle(Canvas c,float x,float y,int height){
  oval(c,0xff372619,x-4,y-2,x+5,y+2);oval(c,0xffbd9551,x-3,y-3,x+4,y);rect(c,0xffac864a,x,y-7,x+2,y-2);
  rect(c,0xffaa9367,x-2,y-height,x+3,y-7);rect(c,0xffeddeb2,x-1,y-height,x+1,y-7);
  poly(c,0xffc3712c,x,y-height-7,x+2,y-height-3,x,y-height,x-2,y-height-3);
  rect(c,0xffffe0a0,x,y-height-5,x+1,y-height-2);
 }
 private void scale(Canvas c){
  slab(c,0xffc2a56a,0xff79633d,0xff58492e,0,-1,12,4);
  rect(c,0xff57482d,-2,-32,3,-7);rect(c,0xffc7ad71,-1,-31,1,-8);
  line(c,0xff675232,3,-16,-30,17,-26);line(c,0xffd6b97d,1,-16,-31,17,-27);
  for(int sign:new int[]{-1,1}){float x=sign*13,y=sign<0?-29:-26;
   line(c,0xffb59d65,1,x,y,x-6,y+15);line(c,0xffb59d65,1,x,y,x+6,y+15);
   poly(c,0xffc2a263,x-8,y+14,x+8,y+14,x+4,y+18,x-4,y+18);line(c,0xffe1c78a,1,x-8,y+14,x+8,y+14);
  }
 }
 public static boolean supports(String kind){return ArraysHolder.TYPES.contains(kind);}
 public boolean draw(Canvas c,String kind,float x,float y,float width){
  if(!ArraysHolder.TYPES.contains(kind))return false;
  c.save();c.translate(x,y);c.scale(width/48,width/48);
  switch(kind){
   case "open_book":book(c,0,-3);break;
   case "brass_scale":scale(c);break;
   case "bottle_tray":
    slab(c,0xff695039,0xff35291d,0xff433021,0,-1,22,3);
    bottle(c,-13,-4,0xff4f7469,18);bottle(c,-3,-1,0xff6c557c,21);bottle(c,8,-4,0xff7b893b,17);bottle(c,16,-7,0xff526e7e,20);break;
   case "weapon_tray":
    slab(c,0xff574338,0xff362c23,0xff403123,0,-1,22,3);
    for(int i=0;i<3;i++){float yy=-5-i*4;line(c,0xff28343a,4,-16,yy,11,yy-12);line(c,0xffa9b2a7,2,-14,yy,11,yy-12);line(c,0xffb49753,2,-12,yy-7,-7,yy+1);line(c,0xff65402a,3,-18,yy+2,-12,yy-1);}break;
   case "coin_tray":
    slab(c,0xff655036,0xff3c2d1c,0xff48341e,0,-1,20,3);
    for(int i=0;i<12;i++){float xx=-12+(i%4)*7,yy=-3-(i/4)*4;oval(c,0xff5a4420,xx-3,yy-2,xx+4,yy+3);oval(c,0xffbea153,xx-3,yy-3,xx+3,yy+1);line(c,0xffe2c882,1,xx-2,yy-2,xx+1,yy-2);}break;
   case "inn_table":
    slab(c,0xff9a6338,0xff58371f,0xff714725,0,-1,25,8);
    rect(c,0xff39271b,-17,-9,-14,15);rect(c,0xff39271b,14,-9,17,15);
    rect(c,0xffbd8b52,-18,-12,18,-9);break;
   case "inn_stool":
    slab(c,0xffa97845,0xff50341f,0xff684426,0,-1,13,5);
    rect(c,0xff49301e,-8,-2,-6,12);rect(c,0xff49301e,6,-2,8,12);break;
   case "inn_hearth":
    poly(c,0xff777064,-29,0,-22,-15,22,-15,29,0,22,6,-22,6);
    rect(c,0xff302a25,-20,-31,20,-8);rect(c,0xff9a8e78,-24,-34,24,-29);
    poly(c,0xffc27b34,0,-29,7,-18,1,-10,-5,-19);poly(c,0xffffce66,0,-25,4,-18,0,-13,-3,-19);break;
   case "inn_bed":
    slab(c,0xff845b3d,0xff49301f,0xff5e3d27,0,-2,24,6);
    poly(c,0xffd1c4a6,-20,-9,0,-18,20,-9,0,1);poly(c,0xffeee1c5,-18,-10,-8,-15,1,-10,-10,-5);
    rect(c,0xff69472d,-24,-14,-20,4);rect(c,0xff69472d,20,-14,24,4);break;

  }
  c.restore();return true;
 }
 private static final class ArraysHolder {static final java.util.Set<String>TYPES=new java.util.HashSet<>(java.util.Arrays.asList("open_book","brass_scale","bottle_tray","weapon_tray","coin_tray","inn_table","inn_stool","inn_hearth","inn_bed"));}
}
