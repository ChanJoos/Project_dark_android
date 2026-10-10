package com.projectdark.mobile;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
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
 SourceItemIconRegistry(Context c){context=c;try(InputStream in=c.getAssets().open("item-icons/manifest.json")){full=new JSONObject(new String(read(in),StandardCharsets.UTF_8));}catch(Exception e){throw new IllegalStateException("Full original item artwork manifest unavailable",e);}}

 private static byte[] read(InputStream in)throws java.io.IOException{java.io.ByteArrayOutputStream out=new java.io.ByteArrayOutputStream();byte[] bytes=new byte[4096];int n;while((n=in.read(bytes))!=-1)out.write(bytes,0,n);return out.toByteArray();}
 JSONObject receipt(String id){return full.optJSONObject("items").optJSONObject(id);}
 boolean pending(String id){JSONObject row=receipt(id);return row!=null&&"PENDING_REPLACEMENT_SOURCE".equals(row.optString("identityMatch"));}
 static void drawPending(Canvas c,RectF target){
  UiTheme.center(c,"이미지",target.centerX(),target.centerY()-1,8,UiTheme.MUTED,false);
  UiTheme.center(c,"준비 중",target.centerX(),target.centerY()+10,8,UiTheme.MUTED,false);
 }
 Bitmap get(String id){
  if(pending(id))return null;
  JSONObject row=receipt(id);if(row==null)return null;String path=row.optString("assetPath",null);
  if(path==null)return null;if(cache.containsKey(path))return cache.get(path);
  Bitmap bitmap=null;try(InputStream in=context.getAssets().open(path)){BitmapFactory.Options options=new BitmapFactory.Options();options.inScaled=false;bitmap=BitmapFactory.decodeStream(in,null,options);}catch(Exception e){throw new IllegalStateException("Missing original item artwork: "+id,e);}
  cache.put(path,bitmap);return bitmap;
 }
 /** Center the original bitmap at native size; never enlarge small artwork to fill a socket. */
 static void draw(Canvas canvas,Bitmap bitmap,RectF target,Paint paint){
  if(bitmap==null)return;
  float scale=Math.min(1f,Math.min(target.width()/bitmap.getWidth(),target.height()/bitmap.getHeight()));
  float w=bitmap.getWidth()*scale,h=bitmap.getHeight()*scale;
  paint.setFilterBitmap(false);paint.setAntiAlias(false);
  canvas.drawBitmap(bitmap,null,new RectF(Math.round(target.centerX()-w/2),Math.round(target.centerY()-h/2),Math.round(target.centerX()-w/2)+w,Math.round(target.centerY()-h/2)+h),paint);
 }
 Bitmap get(RpgProgressionState.ItemDefinition d){return d==null?null:get(d.itemId);}
}
