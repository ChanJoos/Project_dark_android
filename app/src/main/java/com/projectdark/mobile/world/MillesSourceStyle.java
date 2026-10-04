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
  private final Map<String,Map<Integer,Integer>> lookup=new HashMap<>();
  MillesSourceStyle(AssetManager assets){
    if(assets==null)return;
    try(InputStream in=assets.open("maps/milles_source_style.json")){
      ByteArrayOutputStream bytes=new ByteArrayOutputStream();byte[] buffer=new byte[4096];int read;while((read=in.read(buffer))!=-1)bytes.write(buffer,0,read);
      JSONObject p=new JSONObject(new String(bytes.toByteArray(),StandardCharsets.UTF_8)).getJSONObject("palettes");
      for(String name:Arrays.asList("wood","roof","foliage","stone")){
        JSONArray rows=p.getJSONArray(name);int[] colors=new int[rows.length()];
        for(int i=0;i<colors.length;i++){JSONArray c=rows.getJSONArray(i);colors[i]=Color.rgb(c.getInt(0),c.getInt(1),c.getInt(2));}
        palettes.put(name,colors);lookup.put(name,new HashMap<>());
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
    float[] hsv=new float[3];
    for(int y=0;y<raw.getHeight();y++)for(int x=0;x<raw.getWidth();x++){
      int c=raw.getPixel(x,y),r=Color.red(c),g=Color.green(c),b=Color.blue(c);
      Color.colorToHSV(c,hsv);
      boolean support=inside(x,y,feet[0])||inside(x,y,feet[1]);
      // Restore the retained source RGB of the iron legs and arms. The earlier key
      // erased every low-luminance pixel, including the metal and its foot contacts.
      boolean metal=support&&hsv[2]<.45f;
      boolean grass=hsv[0]>65&&hsv[0]<170&&hsv[1]>.22f;
      int a=Color.alpha(c);
      if(metal)a=255;
      else if(grass)a=0;
      result.setPixel(x,y,Color.argb(a,r,g,b));
    }
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
    float[] hsv=new float[3];
    boolean building=path.startsWith("buildings/"),vegetation=path.startsWith("vegetation/");
    float[][] roof={{.31f,.03f},{.82f,.20f},{.70f,.53f},{.19f,.34f}};
    for(int y=0;y<h;y++)for(int x=0;x<w;x++){
      int c=result.getPixel(x,y);if(Color.alpha(c)==0)continue;
      Color.colorToHSV(c,hsv);String family=null;
      if(building&&inside((float)x/w,(float)y/h,roof)&&hsv[2]>.10f)family="roof";
      else if(vegetation&&hsv[0]>45&&hsv[0]<180&&hsv[1]>.20f)family="foliage";
      else if(hsv[0]>12&&hsv[0]<63&&hsv[1]>.24f)family="wood";
      else if(hsv[1]<.23f)family="stone";
      else if(path.startsWith("landmarks/")&&hsv[0]>175&&hsv[0]<265&&hsv[2]<.8f)family="roof";
      if(family!=null){int color=nearest(c,family);result.setPixel(x,y,(c&0xff000000)|(color&0xffffff));}
    }
    return result;
  }
  private int nearest(int c,String family){
    Map<Integer,Integer> cache=lookup.get(family);
    int key=(Color.red(c)/8<<10)|(Color.green(c)/8<<5)|Color.blue(c)/8;
    Integer found=cache.get(key);if(found!=null)return found;
    int best=0;double distance=Double.MAX_VALUE;
    for(int p:palettes.get(family)){
      int dr=Color.red(c)-Color.red(p),dg=Color.green(c)-Color.green(p),db=Color.blue(c)-Color.blue(p);
      double d=dr*dr*.30+dg*dg*.59+db*db*.11;
      if(d<distance){distance=d;best=p;}
    }
    cache.put(key,best);return best;
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
