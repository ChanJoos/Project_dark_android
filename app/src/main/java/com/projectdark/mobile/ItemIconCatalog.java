package com.projectdark.mobile;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

/**
 * Small source-independent pixel symbols for Master items without a verified original
 * inventory crop. Original captured icons always take precedence in their own registry.
 */
final class ItemIconCatalog {
  private static final java.util.Map<String,Bitmap> CACHE=new java.util.HashMap<>();
  private ItemIconCatalog() {}
  static Bitmap reagent(String id){if(id==null)return null;if(CACHE.containsKey(id))return CACHE.get(id);if(!id.equals("IT_REAGENT_CURUM")&&!id.equals("IT_REAGENT_HOLYWATER"))return null;Bitmap b=Bitmap.createBitmap(24,24,Bitmap.Config.ARGB_8888);Canvas c=new Canvas(b);Paint p=new Paint();p.setAntiAlias(false);int edge=0xff3c3027,glass=id.equals("IT_REAGENT_CURUM")?0xff5c964e:0xff6da9c7,light=id.equals("IT_REAGENT_CURUM")?0xffb8df79:0xffc8f0eb,dark=id.equals("IT_REAGENT_CURUM")?0xff315b39:0xff416d8a;rect(c,p,9,3,15,6,edge);rect(c,p,8,6,16,8,light);rect(c,p,6,8,18,20,edge);rect(c,p,7,9,17,19,glass);rect(c,p,8,9,10,15,light);rect(c,p,11,16,16,18,dark);CACHE.put(id,b);return b;}
  static Bitmap get(String id,RpgProgressionState.ItemDefinition item){
    if(item==null||item.appearanceId!=null&&!item.appearanceId.isEmpty())return null;
    if(CACHE.containsKey(id))return CACHE.get(id);
    String slot=item.equipSlot;if(slot==null)return null;
    Bitmap b=Bitmap.createBitmap(24,24,Bitmap.Config.ARGB_8888);Canvas c=new Canvas(b);Paint p=new Paint();p.setAntiAlias(false);
    int edge=0xff39271d,base=0xffb48b57,light=0xffe4c992,dark=0xff725137;
    if(id.contains("REDJADE")){base=0xffad3841;light=0xffed8582;dark=0xff6c242d;}
    else if(id.contains("GREENJADE")){base=0xff428b62;light=0xff8bd291;dark=0xff28553c;}
    else if(id.contains("GORU")){base=0xff8e69a6;light=0xffd0a6e8;dark=0xff513b65;}
    else if(id.contains("SILVER")||id.contains("EARRING")){base=0xffaab6c0;light=0xffeef2e9;dark=0xff65727c;}
    else if(id.contains("LEATHER")){base=0xffa56d42;light=0xffd09b60;dark=0xff68452e;}
    if(slot.equals("장갑")){rect(c,p,6,9,11,18,edge);rect(c,p,13,8,18,18,edge);rect(c,p,7,10,10,16,base);rect(c,p,14,9,17,16,base);rect(c,p,8,10,9,12,light);rect(c,p,15,9,16,11,light);rect(c,p,6,17,11,20,dark);rect(c,p,13,17,18,20,dark);}
    else if(slot.equals("각반")){rect(c,p,4,5,19,9,edge);rect(c,p,5,6,18,8,base);rect(c,p,6,9,11,19,edge);rect(c,p,13,9,18,19,edge);rect(c,p,7,10,10,17,base);rect(c,p,14,10,17,17,base);rect(c,p,7,10,8,14,light);rect(c,p,14,10,15,14,light);rect(c,p,6,18,11,20,dark);rect(c,p,13,18,18,20,dark);}
    else if(slot.equals("귀걸이")){ring(c,p,7,10,edge,base);ring(c,p,16,10,edge,base);rect(c,p,6,5,8,8,light);rect(c,p,15,5,17,8,light);}
    else if(slot.equals("반지")){ring(c,p,12,13,edge,light);rect(c,p,10,4,15,10,edge);rect(c,p,11,5,14,8,base);color(p,light);c.drawPoint(12,6,p);}
    else if(slot.equals("목걸이")){ring(c,p,12,11,edge,base);rect(c,p,10,14,15,20,edge);rect(c,p,11,15,14,18,light);}
    else if(slot.equals("벨트")){rect(c,p,3,9,20,15,edge);rect(c,p,4,10,19,14,base);rect(c,p,10,9,14,15,light);}
    else return null;
    CACHE.put(id,b);return b;
  }
  private static int color(Paint p,int c){p.setColor(c);return c;}
  private static void rect(Canvas c,Paint p,int l,int t,int r,int b,int color){p.setColor(color);c.drawRect(l,t,r+1,b+1,p);}
  private static void ring(Canvas c,Paint p,int x,int y,int outer,int inner){p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2);p.setColor(outer);c.drawOval(new RectF(x-3,y-4,x+3,y+3),p);p.setStrokeWidth(1);p.setColor(inner);c.drawOval(new RectF(x-2,y-3,x+2,y+2),p);p.setStyle(Paint.Style.FILL);}
}
