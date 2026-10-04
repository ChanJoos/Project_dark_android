package com.projectdark.mobile.world;

import android.content.res.AssetManager;
import android.graphics.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import org.json.*;

/** Presentation-only source palette. Source sprites and world/domain geometry stay immutable. */
final class MillesSourceStyle {
  private final AssetManager assets;
  private final Map<String,int[]> palettes=new HashMap<>();
  private static final float[][] ROOF={{.29f,.03f},{.89f,.23f},{.70f,.61f},{.16f,.32f}};
  MillesSourceStyle(AssetManager assets){
    this.assets=assets;
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
  /** One independent authored cabin is intentionally reused; not counted as many unique assets. */
  Bitmap replacement(Bitmap old,String path){
    // The single-tower V102 substitute removed the church's established identity.
    if(path.equals("landmarks/BLD_011_church.png"))return old;
    if(path.contains("bench_video_cutout"))return completeBench(path);
    boolean house=path.startsWith("buildings/");boolean tree=path.startsWith("vegetation/trees/");boolean church=path.equals("landmarks/BLD_011_church.png");
    if(!house&&!tree&&!church)return old;
    String asset=tree?"style_v102/willow.png":church?"style_v102/church.png":"style_v102/log_cabin.png";
    Bitmap raw;try(InputStream in=assets.open(asset)){raw=BitmapFactory.decodeStream(in);}catch(Exception e){throw new IllegalStateException("Missing V102 production sprite "+asset,e);}
    int rw=raw.getWidth(),rh=raw.getHeight();int[] pixels=new int[rw*rh];raw.getPixels(pixels,0,rw,0,0,rw,rh);
    int left=rw,top=rh,right=0,bottom=0;
    for(int y=0;y<rh;y++)for(int x=0;x<rw;x++){
      int i=y*rw+x,c=pixels[i];
      // Generated semi-transparent colored glows are presentation artifacts, not ground shadows.
      if((c>>>24)<220)pixels[i]=0;else{pixels[i]=0xff000000|(c&0xffffff);left=Math.min(left,x);top=Math.min(top,y);right=Math.max(right,x+1);bottom=Math.max(bottom,y+1);}
    }
    Bitmap clean=Bitmap.createBitmap(rw,rh,Bitmap.Config.ARGB_8888);clean.setPixels(pixels,0,rw,0,0,rw,rh);raw.recycle();
    int w=old.getWidth(),h=old.getHeight();float doorX=w*.29f,doorY=h*.82f,sourceX=500,sourceY=878;
    if(tree){w=path.contains("tree_01")?256:180;h=path.contains("tree_01")?200:140;doorX=w*.5f;doorY=h-1;sourceX=840;sourceY=960;}
    else if(church){doorX=85;doorY=281;sourceX=458;sourceY=914;}
    else if(path.contains("potion")){doorX=98;doorY=216;}
    else if(path.contains("weapon")){doorX=97;doorY=216;}
    else if(path.contains("general")){doorX=91;doorY=216;}
    else if(path.contains("inn")){doorX=184;doorY=308;}
    float sx=w*.90f/(right-left),sy=h*.90f/(bottom-top);
    float dx=doorX-(sourceX-left)*sx,dy=doorY-(sourceY-top)*sy;
    Bitmap result=Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888);Paint p=new Paint();p.setFilterBitmap(false);
    new Canvas(result).drawBitmap(clean,new Rect(left,top,right,bottom),new RectF(dx,dy,dx+(right-left)*sx,dy+(bottom-top)*sy),p);clean.recycle();
    // Fractional Canvas registration can create edge coverage even with nearest sampling.
    // Snap that coverage to an actual native pixel silhouette after registration.
    int[] registered=new int[w*h];result.getPixels(registered,0,w,0,0,w,h);
    for(int i=0;i<registered.length;i++)registered[i]=(registered[i]>>>24)<128?0:0xff000000|(registered[i]&0xffffff);
    result.setPixels(registered,0,w,0,0,w,h);
    return result;
  }
  /** One complete authored support structure, explicitly reused in mirrored orientation. */
  Bitmap completeBench(String path){
    Bitmap raw;
    try(InputStream in=assets.open("style_v103/bench_complete.png")){raw=BitmapFactory.decodeStream(in);}
    catch(Exception e){throw new IllegalStateException("Complete bench unavailable",e);}
    int w=raw.getWidth(),h=raw.getHeight(),left=w,top=h,right=0,bottom=0;
    int[] pixels=new int[w*h];raw.getPixels(pixels,0,w,0,0,w,h);
    for(int y=0;y<h;y++)for(int x=0;x<w;x++){
      int k=y*w+x,c=pixels[k];
      if((c>>>24)<220)pixels[k]=0;
      else{pixels[k]=0xff000000|(c&0xffffff);left=Math.min(left,x);top=Math.min(top,y);right=Math.max(right,x+1);bottom=Math.max(bottom,y+1);}
    }
    raw.setPixels(pixels,0,w,0,0,w,h);
    Bitmap result=Bitmap.createBitmap(180,180,Bitmap.Config.ARGB_8888);
    float scale=Math.min(176f/(right-left),164f/(bottom-top));
    float width=(right-left)*scale,height=(bottom-top)*scale;
    Canvas canvas=new Canvas(result);Paint pixel=new Paint();pixel.setFilterBitmap(false);
    if(path.contains("_02")||path.contains("_04")){canvas.translate(180,0);canvas.scale(-1,1);}
    canvas.drawBitmap(raw,new Rect(left,top,right,bottom),new RectF(90-width/2,172-height,90+width/2,172),pixel);
    raw.recycle();
    int[] registered=new int[180*180];result.getPixels(registered,0,180,0,0,180,180);
    for(int i=0;i<registered.length;i++)registered[i]=(registered[i]>>>24)<128?0:0xff000000|(registered[i]&0xffffff);
    result.setPixels(registered,0,180,0,0,180,180);return result;
  }
  static Bitmap quietGrass(Bitmap raw){
    Bitmap result=Bitmap.createBitmap(raw.getWidth(),raw.getHeight(),Bitmap.Config.ARGB_8888);Paint p=new Paint();
    p.setColorFilter(new ColorMatrixColorFilter(new float[]{.62f,0,0,0,22, 0,.62f,0,0,35, 0,0,.62f,0,17, 0,0,0,1,0}));
    new Canvas(result).drawBitmap(raw,0,0,p);return result;
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
      boolean metal=support&&max<115&&!(g>r*1.55f&&g>b*1.45f);
      boolean grass=hue>65&&hue<170&&delta>max*.22f;
      int a=c>>>24;
      if(metal)a=255;
      else if(grass)a=0;
      // Remove pale keying fringe only at the silhouette edge, not silver interior pixels.
      if(a>0&&!support&&max>180&&delta<34&&(x==0||y==0||x==width-1||y==height-1||((raw.getPixel(Math.max(0,x-1),y)>>>24)==0)||((raw.getPixel(Math.min(width-1,x+1),y)>>>24)==0)))a=0;
      pixels[y*width+x]=(a<<24)|(r<<16)|(g<<8)|b;
    }
    boolean[] seen=new boolean[pixels.length];int[] queue=new int[pixels.length];
    for(int i=0;i<pixels.length;i++){
      if(seen[i]||(pixels[i]>>>24)==0)continue;int n=1;queue[0]=i;seen[i]=true;
      for(int q=0;q<n;q++){int v=queue[q],vx=v%width,vy=v/width;for(int yy=Math.max(0,vy-1);yy<=Math.min(height-1,vy+1);yy++)for(int xx=Math.max(0,vx-1);xx<=Math.min(width-1,vx+1);xx++){int k=yy*width+xx;if(!seen[k]&&(pixels[k]>>>24)>0){seen[k]=true;queue[n++]=k;}}}
      if(n<7)for(int q=0;q<n;q++)pixels[queue[q]]=0;
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
      if("roof".equals(f)&&path.startsWith("buildings/"))best=roofVariant(best,path);
      pixels[y*w+x]=(c&0xff000000)|(best&0xffffff);
    }
    result.setPixels(pixels,0,w,0,0,w,h);
    return result;
  }
  /** Observed muted roof families in the supplied north-village overview; adapted assignment. */
  private static int roofVariant(int color,String path){
    int r=color>>16&255,g=color>>8&255,b=color&255;double l=luma(color);
    if(path.contains("general")||path.contains("guild"))return Color.rgb((int)(l*.93),(int)(l*1.04),(int)(l*.84));
    if(path.contains("weapon")||path.contains("library"))return Color.rgb((int)(l*.86),(int)(l*.97),(int)(l*1.08));
    if(path.contains("armor"))return Color.rgb((int)(l*.80),(int)(l*.83),(int)(l*.85));
    if(path.contains("flower"))return Color.rgb((int)(l*1.09),(int)(l*.84),(int)(l*.78));
    if(path.contains("inn"))return Color.rgb((int)(r*.95),(int)(g*.94),(int)(b*.92));
    return color;
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
    boolean coloredRoof=y<.62f&&(hue<12||hue>330||hue>175&&hue<315)&&saturation>.08f;
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
