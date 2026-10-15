package com.projectdark.mobile;

import android.graphics.ColorMatrixColorFilter;
import android.graphics.Bitmap;

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
  public static Bitmap fadedBitmap(Bitmap source){
    return variant(source,true,false);
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
