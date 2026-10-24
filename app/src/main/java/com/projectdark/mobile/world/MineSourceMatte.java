package com.projectdark.mobile.world;

import android.graphics.Bitmap;
import android.graphics.Color;

/** One-time source-background separation at scene preparation; never per-frame shading. */
public final class MineSourceMatte {
 private MineSourceMatte() {}
 public static Bitmap prepare(String name, Bitmap source) {
  boolean waterKey=name.equals("tree")||name.equals("house")||name.equals("lamp");
  boolean patch=name.startsWith("meadow");
  boolean flowers=name.equals("flowers");
  if(!waterKey&&!patch&&!flowers)return source;
  int w=source.getWidth(),h=source.getHeight();int[] pixels=new int[w*h];source.getPixels(pixels,0,w,0,0,w,h);
  for(int y=0;y<h;y++)for(int x=0;x<w;x++){
   int i=y*w+x,p=pixels[i],a=Color.alpha(p),r=Color.red(p),g=Color.green(p),b=Color.blue(p);
   // Leaves and wood are warm/green; source river and its compressed cyan fringe are not.
   if(waterKey&&b>r+12&&b>g+2)a=0;
   if(patch){double dx=(x-(w-1)*.5)/(w*.5),dy=(y-(h-1)*.5)/(h*.5);double edge=(1-Math.sqrt(dx*dx+dy*dy))/.32;a=(int)(a*Math.max(0,Math.min(1,edge)));}
   if(flowers){
    // Keep the source flowers/leaves/pot; discard only the surrounding lawn.
    float center=(w-1)*.5f,half=y<h*.66f?w*.46f:w*(.36f-(y/(float)h-.66f)*.43f);
    if(Math.abs(x-center)>half||y>h-4)a=0;
    if(y<h*.72f&&g>r*1.2f&&g>b*1.4f&&Math.abs(x-center)>w*.26f)a=0;
   }
   pixels[i]=a==0?0:Color.argb(a,r,g,b);
  }
  Bitmap result=Bitmap.createBitmap(pixels,w,h,Bitmap.Config.ARGB_8888);source.recycle();return result;
 }
}
