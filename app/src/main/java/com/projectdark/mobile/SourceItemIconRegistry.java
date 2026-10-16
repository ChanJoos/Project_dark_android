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
  if(path==null)return null;
  String cacheKey="IT_REAGENT_CURUM".equals(id)?id:path;
  if(cache.containsKey(cacheKey))return cache.get(cacheKey);
  if("IT_REAGENT_CURUM".equals(id)){
    Bitmap base=get("IT_REAGENT_CURANUM");
    if(base==null){base=cache.get(path);}
    if(base!=null){Bitmap orange=orangeCurum(base);cache.put(cacheKey,orange);return orange;}
  }
  Bitmap bitmap=null;try(InputStream in=context.getAssets().open(path)){BitmapFactory.Options options=new BitmapFactory.Options();options.inScaled=false;bitmap=BitmapFactory.decodeStream(in,null,options);}catch(Exception e){throw new IllegalStateException("Missing original item artwork: "+id,e);}
  if("IT_REAGENT_CURUM".equals(id))bitmap=orangeCurum(bitmap);
  cache.put(cacheKey,bitmap);return bitmap;
 }
 /** USER_CONFIRMED_ORANGE / ADAPTED: original Curum identity is not recovered.
     Reuse retained bottle silhouette; recolor violet liquid only, preserve alpha/cork/glass. */
 static Bitmap orangeCurum(Bitmap source){
  int w=source.getWidth(),h=source.getHeight();int[] pixels=new int[w*h];source.getPixels(pixels,0,w,0,0,w,h);float[] hsv=new float[3];
  for(int i=0;i<pixels.length;i++){int p=pixels[i];android.graphics.Color.colorToHSV(p,hsv);
   if(android.graphics.Color.alpha(p)>0&&hsv[1]>.3f&&hsv[0]>=250&&hsv[0]<=335){hsv[0]=28;hsv[1]=Math.max(.65f,hsv[1]);hsv[2]=Math.min(1,hsv[2]*1.65f);pixels[i]=android.graphics.Color.HSVToColor(android.graphics.Color.alpha(p),hsv);}}
  return Bitmap.createBitmap(pixels,w,h,Bitmap.Config.ARGB_8888);
 }
 /** Center the original bitmap at native size; never enlarge small artwork to fill a socket. */
 static void draw(Canvas canvas,Bitmap bitmap,RectF target,Paint paint){
  if(bitmap==null)return;
  float scale=Math.min(1f,Math.min(target.width()/bitmap.getWidth(),target.height()/bitmap.getHeight()));
  float w=bitmap.getWidth()*scale,h=bitmap.getHeight()*scale;
  paint.setFilterBitmap(false);paint.setAntiAlias(false);
  canvas.drawBitmap(bitmap,null,new RectF(Math.round(target.centerX()-w/2),Math.round(target.centerY()-h/2),Math.round(target.centerX()-w/2)+w,Math.round(target.centerY()-h/2)+h),paint);
 }
 // Equipment-only: fit visible alpha bounds. Native orientation, except paired right glove.
 private static final Map<Bitmap,android.graphics.Rect> equipmentBounds=Collections.synchronizedMap(new WeakHashMap<>());
 static android.graphics.Rect visibleEquipmentBounds(Bitmap bitmap){
  android.graphics.Rect cached=equipmentBounds.get(bitmap);if(cached!=null)return cached;
  int w=bitmap.getWidth(),h=bitmap.getHeight(),left=w,top=h,right=-1,bottom=-1;int[] pixels=new int[w*h];bitmap.getPixels(pixels,0,w,0,0,w,h);
  for(int y=0;y<h;y++)for(int x=0;x<w;x++)if(android.graphics.Color.alpha(pixels[y*w+x])>=16){left=Math.min(left,x);right=Math.max(right,x);top=Math.min(top,y);bottom=Math.max(bottom,y);}
  android.graphics.Rect b=right<left?new android.graphics.Rect():new android.graphics.Rect(left,top,right+1,bottom+1);equipmentBounds.put(bitmap,b);return b;
 }
 static void drawEquipment(Canvas canvas,Bitmap bitmap,RectF target,Paint paint,boolean mirrorHand){
  if(bitmap==null)return;android.graphics.Rect src=visibleEquipmentBounds(bitmap);if(src.isEmpty())return;
  float scale=Math.min(target.width()/src.width(),target.height()/src.height()),w=src.width()*scale,h=src.height()*scale;
  RectF dst=new RectF(target.centerX()-w/2,target.centerY()-h/2,target.centerX()+w/2,target.centerY()+h/2);
  paint.setFilterBitmap(false);paint.setAntiAlias(false);int save=canvas.save();canvas.clipRect(target);
  if(mirrorHand)canvas.scale(-1,1,target.centerX(),target.centerY());canvas.drawBitmap(bitmap,src,dst,paint);canvas.restoreToCount(save);
 }
 Bitmap get(RpgProgressionState.ItemDefinition d){return d==null?null:get(d.itemId);}
}

