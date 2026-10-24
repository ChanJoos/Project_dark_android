package com.projectdark.mobile.world;

import android.graphics.Bitmap;
import android.graphics.Color;

/** Same raster/alpha/pose for each color pair. Material edits happen only at map prewarm. */
public final class CoastSpritePalette {
  private static final String ROOT="pote_monsters_generated_v1/sprites/rucesion_v157/";
  public static String sourcePath(String path){
    if(!path.startsWith(ROOT))return path;
    if(path.contains("/koatl/"))return path.replace("/koatl/","/elect_koatl/");
    if(path.contains("/poison_kakapo/"))return path.replace("/poison_kakapo/","/kakapo/");
    if(path.contains("/red_hacon/"))return path.replace("/red_hacon/","/blue_hacon/");
    return path;
  }
  public static Bitmap apply(String path,Bitmap source){
    boolean pong=path.contains("/abel_v161/pong/");
    if(source==null||(!pong&&sourcePath(path).equals(path)))return source;
    int[] pixels=new int[source.getWidth()*source.getHeight()];
    source.getPixels(pixels,0,source.getWidth(),0,0,source.getWidth(),source.getHeight());
    float[] hsv=new float[3];
    for(int i=0;i<pixels.length;i++){
      int p=pixels[i],a=Color.alpha(p);if(a==0)continue;
      int r=Color.red(p),g=Color.green(p),b=Color.blue(p);
      if(pong){
        // Normal violet/gold, not the blue-faded cursed actor formerly used as reference.
        // 20261011 recording 9.5s normal / 16s faded crops: master/source/abel_v162.
        // Material projection retains every authored pose/alpha/anchor and all dark outlines.
        Color.colorToHSV(p,hsv);
        if(hsv[2]>.16f&&hsv[1]>.18f){
          if(hsv[0]>245&&hsv[0]<315){
            hsv[0]=hsv[0]*.25f+282f*.75f;
            hsv[1]=Math.min(1f,hsv[1]*.85f);hsv[2]=Math.min(1f,hsv[2]*.90f);
          }else if(hsv[0]>20&&hsv[0]<55){
            hsv[1]=Math.min(1f,hsv[1]*.98f);hsv[2]=Math.min(1f,hsv[2]*.96f);
          }
          pixels[i]=Color.HSVToColor(a,hsv);
        }
      }else if(path.contains("/koatl/")){
        // Silver ribs only. Navy membrane, body and tail remain byte-identical.
        int max=Math.max(r,Math.max(g,b)),min=Math.min(r,Math.min(g,b));
        if(max>38&&max-min<max*.19f&&r>=b*.94f){
          pixels[i]=Color.argb(a,Math.min(255,(int)(max*1.05f)),(int)(max*.61f),(int)(max*.34f));
        }
      }else if(path.contains("/poison_kakapo/")){
        Color.colorToHSV(p,hsv);
        // Orange shell panels; neutral metal, red tentacles and eyes are preserved.
        if(hsv[0]>13&&hsv[0]<47&&hsv[1]>.48f){hsv[0]=119;pixels[i]=Color.HSVToColor(a,hsv);}
      }else{
        if(b>r*1.25f&&b>g*1.08f){
          Color.colorToHSV(p,hsv);hsv[0]=8;
          if(hsv[2]>.8f)hsv[1]*=.62f;
          pixels[i]=Color.HSVToColor(a,hsv);
        }else if(b>r*1.35f&&g>r*1.25f){
          Color.colorToHSV(p,hsv);hsv[0]=348;pixels[i]=Color.HSVToColor(a,hsv);
        }
      }
    }
    Bitmap out=Bitmap.createBitmap(pixels,source.getWidth(),source.getHeight(),Bitmap.Config.ARGB_8888);
    source.recycle();return out;
  }
  private CoastSpritePalette(){}
}
