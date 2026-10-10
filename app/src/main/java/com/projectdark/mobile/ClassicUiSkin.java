package com.projectdark.mobile;
import android.content.Context;
import android.graphics.*;
import java.util.*;

/** Original-client screenshot crops, cached once; items never contain this skin. */
final class ClassicUiSkin {
 private static final Map<String,Bitmap> art=new HashMap<>();
 private static final Paint pixel=new Paint();
 private static BitmapShader paper,header;
 static void install(Context context){
  if(paper!=null)return;
  try{
   for(String key:new String[]{"paper","header","top","bottom","left","right","corner_tl","corner_tr","corner_bl","corner_br"}){
    BitmapFactory.Options o=new BitmapFactory.Options();o.inScaled=false;
    Bitmap b=BitmapFactory.decodeStream(context.getAssets().open("classic-ui/"+key+".png"),null,o);
    if(b==null)throw new IllegalStateException(key);art.put(key,b);
   }
   paper=new BitmapShader(art.get("paper"),Shader.TileMode.REPEAT,Shader.TileMode.REPEAT);
   header=new BitmapShader(art.get("header"),Shader.TileMode.REPEAT,Shader.TileMode.REPEAT);
  }catch(Exception e){throw new IllegalStateException("Original classic UI skin unavailable",e);}
 }
 static void texture(Canvas c,RectF r,boolean title){
  pixel.setShader(null);pixel.setColor(title?UiTheme.BG:UiTheme.SURFACE);pixel.setAlpha(255);c.drawRect(r,pixel);
  pixel.setFilterBitmap(false);pixel.setShader(title?header:paper);pixel.setStyle(Paint.Style.FILL);pixel.setAlpha(title?70:28);c.drawRect(r,pixel);pixel.setShader(null);pixel.setAlpha(255);
 }
 private static void part(Canvas c,String key,RectF r){pixel.setShader(null);pixel.setFilterBitmap(false);c.drawBitmap(art.get(key),null,r,pixel);}
 static void frame(Canvas c,RectF r){
  float e=4;
  part(c,"top",new RectF(r.left+e,r.top,r.right-e,r.top+e));
  part(c,"bottom",new RectF(r.left+e,r.bottom-e,r.right-e,r.bottom));
  part(c,"left",new RectF(r.left,r.top+e,r.left+e,r.bottom-e));
  part(c,"right",new RectF(r.right-e,r.top+e,r.right,r.bottom-e));
  part(c,"corner_tl",new RectF(r.left,r.top,r.left+e,r.top+e));
  part(c,"corner_tr",new RectF(r.right-e,r.top,r.right,r.top+e));
  part(c,"corner_bl",new RectF(r.left,r.bottom-e,r.left+e,r.bottom));
  part(c,"corner_br",new RectF(r.right-e,r.bottom-e,r.right,r.bottom));
 }
}
