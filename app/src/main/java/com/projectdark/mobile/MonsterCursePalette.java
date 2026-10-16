package com.projectdark.mobile;

import android.graphics.ColorMatrixColorFilter;
import android.graphics.Bitmap;
import android.graphics.Color;

/** User-confirmed curse fading. RGB only: alpha, source pixels and combat authority stay intact. */
public final class MonsterCursePalette {
  // A modest desaturation and cool lift, matching the normal/faded pair in the supplied recording.
  // Vector fallback only. Scaled raster actors use exact-alpha prewarmed variants below.
  public static final ColorMatrixColorFilter FADED=new ColorMatrixColorFilter(new float[]{
      .615f,.300f,.085f,0,9,
      .165f,.750f,.085f,0,12,
      .165f,.300f,.535f,0,22,
      0,0,0,1,0});
  public static int fadedColor(int color){
    int a=color>>>24;if(a==0)return color;
    int r=(color>>>16)&255,g=(color>>>8)&255,b=color&255;
    int nr=Math.min(255,Math.round(.615f*r+.300f*g+.085f*b+9));
    int ng=Math.min(255,Math.round(.165f*r+.750f*g+.085f*b+12));
    int nb=Math.min(255,Math.round(.165f*r+.300f*g+.535f*b+22));
    return (a<<24)|(nr<<16)|(ng<<8)|nb;
  }
  /** Prewarm only. A color-filtered scaled bitmap can use a different native sampling path. */
  public static Bitmap fadedBitmap(Bitmap source){return fadedBitmap(source,false);}
  public static Bitmap fadedBitmap(Bitmap source,boolean pong){
    if(!pong)return variant(source,true,false);
    int w=source.getWidth(),h=source.getHeight();int[] colors=new int[w*h];source.getPixels(colors,0,w,0,0,w,h);float[] hsv=new float[3];
    for(int i=0;i<colors.length;i++){
      int color=colors[i],a=color>>>24;if(a==0)continue;Color.colorToHSV(color,hsv);
      // Independent video-pair material medians: violet91/37/121 -> blue77.5/60.5/153;
      // warm193/139/91 ->212/157/96. Apply only to Pong, not unobserved species palettes.
      if(hsv[0]>240&&hsv[0]<315&&hsv[1]>.18f&&hsv[2]>.16f){
        hsv[0]-=30;hsv[1]*=.82f;hsv[2]=Math.min(1f,hsv[2]*1.26f);colors[i]=Color.HSVToColor(a,hsv);
      }else if(hsv[0]>20&&hsv[0]<55&&hsv[1]>.18f&&hsv[2]>.16f){
        hsv[0]+=2;hsv[1]=Math.min(1f,hsv[1]*1.04f);hsv[2]=Math.min(1f,hsv[2]*1.10f);colors[i]=Color.HSVToColor(a,hsv);
      }else colors[i]=fadedColor(color);
    }
    return Bitmap.createBitmap(colors,w,h,Bitmap.Config.ARGB_8888);
  }
  public static Bitmap variant(Bitmap source,boolean curse,boolean hit){
    int w=source.getWidth(),h=source.getHeight();int[] colors=new int[w*h];source.getPixels(colors,0,w,0,0,w,h);
    for(int i=0;i<colors.length;i++){
      int color=colors[i];if((color>>>24)==0)continue;
      if(hit)color=(color&0xff000000)|0xe9d6b1;
      colors[i]=curse?fadedColor(color):color;
    }
    return Bitmap.createBitmap(colors,w,h,Bitmap.Config.ARGB_8888);
  }
  public static boolean active(SkillEffectState effects,String actor){
    if(effects==null)return false;
    SkillEffectState.Effect curse=effects.get(actor,"CURSE");
    return curse!=null&&curse.remaining>0f;
  }
  private MonsterCursePalette(){}
}
