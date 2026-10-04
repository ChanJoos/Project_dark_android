package com.projectdark.mobile.world;

import android.content.res.AssetManager;
import android.graphics.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import org.json.*;

/** Presentation-only source palette. Source sprites and world/domain geometry stay immutable. */
final class MillesSourceStyle {
  private final Map<String,int[]> palettes=new HashMap<>();
  private static final float[][] ROOF={{.29f,.03f},{.89f,.23f},{.70f,.61f},{.16f,.32f}};
  MillesSourceStyle(AssetManager assets){
    if(assets==null)return;
    try(InputStream in=assets.open("maps/milles_source_style.json")){
      ByteArrayOutputStream bytes=new ByteArrayOutputStream();byte[] buffer=new byte[4096];int read;while((read=in.read(buffer))!=-1)bytes.write(buffer,0,read);
      JSONObject p=new JSONObject(new String(bytes.toByteArray(),StandardCharsets.UTF_8)).getJSONObject("palettes");
      for(String name:Arrays.asList("wood","roof","foliage","stone")){
        JSONArray rows=p.getJSONArray(name);int[] colors=new int[rows.length()];
        for(int i=0;i<colors.length;i++){JSONArray c=rows.getJSONArray(i);colors[i]=Color.rgb(c.getInt(0),c.getInt(1),c.getInt(2));}
        palettes.put(name,colors);
      }
    }catch(Exception e){throw new IllegalStateException("Milles source palette unavailable",e);}
  }
  static String completeBenchSource(String path){
    // 03/04 have already clipped/lost RGB below the seat. Use the complete source in
    // the same orientation, rather than inventing feet or shifting the entire bench.
    return path.replace("bench_video_cutout_03","bench_video_cutout_01")
        .replace("bench_video_cutout_04","bench_video_cutout_02");
  }
  static Bitmap bench(Bitmap raw,String path){
    Bitmap result=Bitmap.createBitmap(raw.getWidth(),raw.getHeight(),Bitmap.Config.ARGB_8888);
    boolean first=path.contains("_01");
    float[][][] feet=first?new float[][][]{
      {{17,81},{32,82},{28,94},{15,99},{10,94}},
      {{119,99},{138,101},{129,122},{120,130},{109,128},{108,122}}}
      :new float[][][]{
      {{27,95},{43,100},{47,119},{37,123},{29,112}},
      {{145,77},{160,73},{160,95},{150,99},{144,91}}};
    int width=raw.getWidth(),height=raw.getHeight();int[] pixels=new int[width*height];
    raw.getPixels(pixels,0,width,0,0,width,height);
    for(int y=0;y<height;y++)for(int x=0;x<width;x++){
      int c=pixels[y*width+x],r=c>>16&255,g=c>>8&255,b=c&255;
      int max=Math.max(r,Math.max(g,b)),delta=max-Math.min(r,Math.min(g,b));float hue=hue(r,g,b,max,delta);
      boolean support=inside(x,y,feet[0])||inside(x,y,feet[1]);
      // Restore the retained source RGB of the iron legs and arms. The earlier key
      // erased every low-luminance pixel, including the metal and its foot contacts.
      boolean metal=support&&max<115;
      boolean grass=hue>65&&hue<170&&delta>max*.22f;
      int a=c>>>24;
      if(metal)a=255;
      else if(grass)a=0;
      pixels[y*width+x]=(a<<24)|(r<<16)|(g<<8)|b;
    }
    result.setPixels(pixels,0,width,0,0,width,height);
    return result;
  }
  Bitmap scenery(Bitmap original,String path){
    if(path.startsWith("video_reference/")||path.startsWith("assets/")||palettes.isEmpty())return original;
    int w=original.getWidth(),h=original.getHeight();
    // One cached native sprite, no canvas-wide blur or shader over the player/UI.
    int step=path.contains("bridge")?6:2;
    Bitmap small=Bitmap.createScaledBitmap(original,Math.max(1,w/step),Math.max(1,h/step),false);
    Bitmap result=Bitmap.createScaledBitmap(small,w,h,false);if(small!=result)small.recycle();
    result=result.copy(Bitmap.Config.ARGB_8888,true);
    // Match each material's light/shadow distribution before palette selection.
    // Direct nearest-RGB would collapse the brighter generated leaves into one flat green.
    int[] pixels=new int[w*h];result.getPixels(pixels,0,w,0,0,w,h);
    Map<String,double[]> stats=new HashMap<>();
    for(String f:palettes.keySet())stats.put(f,new double[3]);
    for(int y=0;y<h;y++)for(int x=0;x<w;x++){
      int c=pixels[y*w+x];if((c>>>24)==0)continue;
      String f=family(c,path,(float)x/w,(float)y/h);if(f==null)continue;
      double l=luma(c);double[] a=stats.get(f);a[0]+=l;a[1]+=l*l;a[2]++;
    }
    Map<String,double[]> transforms=new HashMap<>();
    for(String f:palettes.keySet()){
      double[] a=stats.get(f);if(a[2]==0)continue;
      double mean=a[0]/a[2],sd=Math.sqrt(Math.max(1,a[1]/a[2]-mean*mean));
      double sum=0,sq=0;for(int color:palettes.get(f)){double l=luma(color);sum+=l;sq+=l*l;}
      int n=palettes.get(f).length;double target=sum/n;
      transforms.put(f,new double[]{mean,sd,target,Math.sqrt(Math.max(1,sq/n-target*target))});
    }
    Map<String,int[]> tables=new HashMap<>();
    for(String f:transforms.keySet()){
      double[] t=transforms.get(f);int[] table=new int[256];
      for(int level=0;level<256;level++){
        double brightness=t[2]+(level-t[0])*t[3]/t[1],distance=Double.MAX_VALUE;int best=0;
        for(int color:palettes.get(f)){double d=Math.abs(luma(color)-brightness);if(d<distance){distance=d;best=color;}}
        table[level]=best;
      }
      tables.put(f,table);
    }
    for(int y=0;y<h;y++)for(int x=0;x<w;x++){
      int c=pixels[y*w+x];if((c>>>24)==0)continue;
      String f=family(c,path,(float)x/w,(float)y/h);if(f==null)continue;
      int best=tables.get(f)[(int)Math.round(luma(c))];
      pixels[y*w+x]=(c&0xff000000)|(best&0xffffff);
    }
    result.setPixels(pixels,0,w,0,0,w,h);
    return result;
  }
  private static double luma(int c){return (c>>16&255)*.30+(c>>8&255)*.59+(c&255)*.11;}
  private static float hue(int r,int g,int b,int max,int delta){
    if(delta==0)return 0;float hue=max==r?(float)(g-b)/delta:max==g?2f+(float)(b-r)/delta:4f+(float)(r-g)/delta;
    hue*=60f;return hue<0?hue+360:hue;
  }
  private static String family(int c,String path,float x,float y){
    int r=c>>16&255,g=c>>8&255,b=c&255,max=Math.max(r,Math.max(g,b)),delta=max-Math.min(r,Math.min(g,b));
    float hue=hue(r,g,b,max,delta),saturation=max==0?0:(float)delta/max,value=max/255f;
    boolean building=path.startsWith("buildings/"),vegetation=path.startsWith("vegetation/");
    boolean coloredRoof=y<.62f&&(hue<12||hue>330||hue>175&&hue<315)&&saturation>.28f;
    boolean chimney=x>.70f&&x<.85f&&y<.31f&&saturation<.25f;
    if(building&&!chimney&&(inside(x,y,ROOF)||coloredRoof)&&value>.10f)return "roof";
    if(vegetation&&hue>45&&hue<180&&saturation>.20f)return "foliage";
    if(hue>12&&hue<63&&saturation>.24f)return "wood";
    if(saturation<.23f)return "stone";
    if(path.startsWith("landmarks/")&&hue>175&&hue<265&&value<.8f)return "roof";
    return null;
  }
  static boolean inside(float x,float y,float[][] polygon){
    boolean inside=false;
    for(int i=0,j=polygon.length-1;i<polygon.length;j=i++){
      float[] a=polygon[i],b=polygon[j];
      if((a[1]>y)!=(b[1]>y)&&x<(b[0]-a[0])*(y-a[1])/(b[1]-a[1])+a[0])inside=!inside;
    }
    return inside;
  }
}
