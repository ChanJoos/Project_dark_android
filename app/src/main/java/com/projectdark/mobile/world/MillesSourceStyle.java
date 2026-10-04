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
    // Match each material's light/shadow distribution before palette selection.
    // Direct nearest-RGB would collapse the brighter generated leaves into one flat green.
    float[] hsv=new float[3];
    Map<String,double[]> stats=new HashMap<>();
    for(String f:palettes.keySet())stats.put(f,new double[3]);
    for(int y=0;y<h;y++)for(int x=0;x<w;x++){
      int c=result.getPixel(x,y);if(Color.alpha(c)==0)continue;
      String f=family(c,path,(float)x/w,(float)y/h,hsv);if(f==null)continue;
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
      int c=result.getPixel(x,y);if(Color.alpha(c)==0)continue;
      String f=family(c,path,(float)x/w,(float)y/h,hsv);if(f==null)continue;
      int best=tables.get(f)[(int)Math.round(luma(c))];
      result.setPixel(x,y,(c&0xff000000)|(best&0xffffff));
    }
    return result;
  }
  private static double luma(int c){return Color.red(c)*.30+Color.green(c)*.59+Color.blue(c)*.11;}
  private static String family(int c,String path,float x,float y,float[] hsv){
    Color.colorToHSV(c,hsv);
    boolean building=path.startsWith("buildings/"),vegetation=path.startsWith("vegetation/");
    boolean coloredRoof=y<.62f&&(hsv[0]<12||hsv[0]>330||hsv[0]>175&&hsv[0]<315)&&hsv[1]>.28f;
    boolean chimney=x>.70f&&x<.85f&&y<.31f&&hsv[1]<.25f;
    if(building&&!chimney&&(inside(x,y,ROOF)||coloredRoof)&&hsv[2]>.10f)return "roof";
    if(vegetation&&hsv[0]>45&&hsv[0]<180&&hsv[1]>.20f)return "foliage";
    if(hsv[0]>12&&hsv[0]<63&&hsv[1]>.24f)return "wood";
    if(hsv[1]<.23f)return "stone";
    if(path.startsWith("landmarks/")&&hsv[0]>175&&hsv[0]<265&&hsv[2]<.8f)return "roof";
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
