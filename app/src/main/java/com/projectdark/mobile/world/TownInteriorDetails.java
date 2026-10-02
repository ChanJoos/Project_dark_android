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
 private void plant(Canvas c){
  slab(c,0xff98704b,0xff705138,0xff493b2c,0,-1,12,17);
  oval(c,0xff352d20,-11,-23,12,-13);oval(c,0xffb38a5b,-12,-25,12,-18);oval(c,0xff302b1e,-10,-23,10,-18);
  for(int i=0;i<9;i++){
   float x=(i%3-1)*7,y=-31-(i*7%20),lean=(i%2==0?-1:1)*7;
   line(c,0xff41603c,2,0,-21,x,y);
   poly(c,i%2==0?0xff385b3e:0xff547247,x,y,x+lean,y-6,x+lean*1.6f,y-4,x+lean*.6f,y+2);
   line(c,0xff779067,1,x,y,x+lean,y-4);
  }
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
 private void armor(Canvas c){
  slab(c,0xff7b5a36,0xff493522,0xff34291c,0,-1,12,5);rect(c,0xff67523b,-2,-15,3,-5);
  poly(c,0xff252e32,-10,-47,11,-47,16,-36,10,-19,0,-14,-11,-21,-16,-36);
  poly(c,0xff858e8a,-9,-45,1,-43,1,-17,-9,-23,-13,-36);
  poly(c,0xff4c5c60,1,-43,10,-45,13,-34,8,-21,1,-17);
  line(c,0xffc1c7b6,1,-10,-41,0,-37);line(c,0xff99aaa5,1,1,-34,8,-40);line(c,0xff2e3738,2,-8,-25,9,-25);
  poly(c,0xff5e6c6b,-14,-48,-20,-40,-13,-33,-9,-43);poly(c,0xff758581,12,-48,20,-40,13,-33,8,-43);
  oval(c,0xff283236,-8,-61,9,-44);oval(c,0xff929b90,-7,-60,7,-48);rect(c,0xff273438,-6,-52,8,-48);rect(c,0xffbfc6b4,-1,-59,1,-47);
 }
 public static boolean supports(String kind){return ArraysHolder.TYPES.contains(kind);}
 public boolean draw(Canvas c,String kind,float x,float y,float width){
  if(!ArraysHolder.TYPES.contains(kind))return false;
  c.save();c.translate(x,y);c.scale(width/48,width/48);
  switch(kind){
   case "herb_planter":plant(c);break;
   case "armor_stand":armor(c);break;
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
   case "votive_stand":
    slab(c,0xff928877,0xff70675c,0xff494941,0,-1,18,5);slab(c,0xffb5aa92,0xff887d69,0xff686354,0,-7,12,23);slab(c,0xffbcb094,0xff81745d,0xff625b4b,0,-30,21,5);
    candle(c,-12,-31,18);candle(c,0,-28,22);candle(c,12,-32,17);break;
   case "holy_font":
    slab(c,0xff8f897c,0xff746e63,0xff4f5048,0,-1,15,5);slab(c,0xffb2a994,0xff897f6e,0xff696454,0,-6,8,20);
    oval(c,0xff625d51,-22,-40,22,-24);oval(c,0xffb3ac94,-22,-43,22,-28);oval(c,0xff464e4c,-17,-40,17,-30);oval(c,0xff567d80,-15,-38,15,-30);line(c,0xffa7bbb0,1,-8,-35,8,-35);break;
   case "archive_shelf":
    slab(c,0xff785737,0xff513b27,0xff362d21,0,-1,21,65);
    for(int row=0;row<3;row++){
     poly(c,0xff231d16,-17,-11-row*18,-2,-4-row*18,-2,-18-row*18,-17,-25-row*18);
     for(int col=0;col<4;col++){float xx=-15+col*3,yy=-12-row*18+col*1.4f;rect(c,col%2==0?0xff766341:0xff6a4738,xx,yy-10,xx+2,yy);line(c,0xffb19a66,1,xx,yy-7,xx+2,yy-7);}
     line(c,0xffb4915d,2,-19,-9-row*18,0,-1-row*18);
    }break;
  }
  c.restore();return true;
 }
 private static final class ArraysHolder {static final java.util.Set<String>TYPES=new java.util.HashSet<>(java.util.Arrays.asList("herb_planter","armor_stand","open_book","brass_scale","bottle_tray","weapon_tray","coin_tray","votive_stand","holy_font","archive_shelf"));}
}
