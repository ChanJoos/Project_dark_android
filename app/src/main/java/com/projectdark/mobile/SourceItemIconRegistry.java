package com.projectdark.mobile;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Paint;
import java.util.*;
import java.io.InputStream;
import org.json.JSONObject;
import java.nio.charset.StandardCharsets;

/** Every registered item has an explicit original-art receipt shared by all windows. */
final class SourceItemIconRegistry {
 private final Context context;
 private final Map<String,Bitmap> cache=new HashMap<>();
 private JSONObject full;
 private static final Map<Bitmap,Rect> foreground=new WeakHashMap<>();
 SourceItemIconRegistry(Context c){context=c;try(InputStream in=c.getAssets().open("item-icons/manifest.json")){full=new JSONObject(new String(read(in),StandardCharsets.UTF_8));}catch(Exception e){throw new IllegalStateException("Full original item artwork manifest unavailable",e);}}

 private static byte[] read(InputStream in)throws java.io.IOException{java.io.ByteArrayOutputStream out=new java.io.ByteArrayOutputStream();byte[] bytes=new byte[4096];int n;while((n=in.read(bytes))!=-1)out.write(bytes,0,n);return out.toByteArray();}
 JSONObject receipt(String id){return full.optJSONObject("items").optJSONObject(id);}
 Bitmap get(String id){
  JSONObject row=receipt(id);if(row==null)return null;String path=row.optString("assetPath",null);
  if(path==null)return null;if(cache.containsKey(path))return cache.get(path);
  Bitmap bitmap=null;try(InputStream in=context.getAssets().open(path)){BitmapFactory.Options options=new BitmapFactory.Options();options.inScaled=false;bitmap=BitmapFactory.decodeStream(in,null,options);}catch(Exception e){throw new IllegalStateException("Missing original item artwork: "+id,e);}
  cache.put(path,bitmap);return bitmap;
 }
 /** Ignore capture padding when fitting a socket; retained pixels are never repainted. */
 static void draw(Canvas canvas,Bitmap bitmap,RectF target,Paint paint){
  Rect bounds=foreground.get(bitmap);
  if(bounds==null){int left=bitmap.getWidth(),top=bitmap.getHeight(),right=-1,bottom=-1;
   for(int y=0;y<bitmap.getHeight();y++)for(int x=0;x<bitmap.getWidth();x++)if((bitmap.getPixel(x,y)>>>24)>0){left=Math.min(left,x);top=Math.min(top,y);right=Math.max(right,x);bottom=Math.max(bottom,y);}
   if(right<left)return;bounds=new Rect(left,top,right+1,bottom+1);foreground.put(bitmap,bounds);
  }
  float scale=Math.min(2.5f,Math.min(target.width()/bounds.width(),target.height()/bounds.height()));float w=bounds.width()*scale,h=bounds.height()*scale;
  paint.setFilterBitmap(false);canvas.drawBitmap(bitmap,bounds,new RectF(target.centerX()-w/2,target.centerY()-h/2,target.centerX()+w/2,target.centerY()+h/2),paint);
 }
 Bitmap get(RpgProgressionState.ItemDefinition d){return d==null?null:get(d.itemId);}
}
