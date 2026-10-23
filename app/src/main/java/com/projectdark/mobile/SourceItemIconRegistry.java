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

 private final ItemIconRepairsV212 repairs;
 private JSONObject full;
 private ItemAtlasV210 atlas;private JSONObject additions;
 SourceItemIconRegistry(Context c){context=c;repairs=new ItemIconRepairsV212(c);try(InputStream in=c.getAssets().open("item-icons/manifest.json")){full=new JSONObject(new String(read(in),StandardCharsets.UTF_8));}catch(Exception e){throw new IllegalStateException("Full original item artwork manifest unavailable",e);}}

 private static byte[] read(InputStream in)throws java.io.IOException{java.io.ByteArrayOutputStream out=new java.io.ByteArrayOutputStream();byte[] bytes=new byte[4096];int n;while((n=in.read(bytes))!=-1)out.write(bytes,0,n);return out.toByteArray();}
 private ItemAtlasV210 atlas(){if(atlas==null)atlas=new ItemAtlasV210(context);return atlas;}
 JSONObject receipt(String id){JSONObject row=full.optJSONObject("items").optJSONObject(id);if(row!=null)return row;try{if(additions==null)additions=new JSONObject(new String(PresentationAssetBytes.read(context,"item-atlas-v210/new-sources.json"),StandardCharsets.UTF_8));return additions.optJSONObject(id);}catch(Exception e){throw new IllegalStateException("New item sources unavailable",e);}}
 boolean pending(String id){JSONObject row=receipt(id);return row!=null&&"PENDING_REPLACEMENT_SOURCE".equals(row.optString("identityMatch"));}
 static void drawPending(Canvas c,RectF target){
  UiTheme.center(c,"이미지",target.centerX(),target.centerY()-1,8,UiTheme.MUTED,false);
  UiTheme.center(c,"준비 중",target.centerX(),target.centerY()+10,8,UiTheme.MUTED,false);
 }
 Bitmap get(String id){
  Bitmap repaired=repairs.get(id);if(repaired!=null)return repaired;
  if(pending(id))return null;
  JSONObject row=receipt(id);if(row==null)return null;String path=row.optString("assetPath",null);
  if(row.optString("projection").equals("V210_PACKED_NAMED_NATIVE_PREVIEW"))return atlas().get(id);
  if(path==null)return null;
  String cacheKey="IT_REAGENT_CURUM".equals(id)?id:path;
  if(cache.containsKey(cacheKey))return cache.get(cacheKey);
  if("IT_REAGENT_CURUM".equals(id)){
    Bitmap base=get("IT_REAGENT_CURANUM");
    if(base==null){base=cache.get(path);}
    if(base!=null){Bitmap orange=orangeCurum(base);cache.put(cacheKey,orange);return orange;}
  }
  if("USER_ITEM_20261018_R05062".equals(id)||"USER_ITEM_20261018_R04290".equals(id)){Bitmap nativeIcon=get("IT_LEGGING_LEATHER");if(nativeIcon!=null){cache.put(cacheKey,nativeIcon);return nativeIcon;}}
  Bitmap front=InventoryArtworkV199.front(row,context);if(front!=null){cache.put(cacheKey,front);return front;}
  Bitmap bitmap=null;try(InputStream in=context.getAssets().open(path)){BitmapFactory.Options options=new BitmapFactory.Options();options.inScaled=false;bitmap=BitmapFactory.decodeStream(in,null,options);}catch(Exception e){throw new IllegalStateException("Missing original item artwork: "+id,e);}
  if("IT_REAGENT_CURUM".equals(id))bitmap=orangeCurum(bitmap);
  if((row.optString("projection").contains("BACKGROUND_PRESERVED")||row.optString("projection").equals("ORIGINAL_ICON_SOCKET_BYTES"))&&bitmap!=null)bitmap=InventoryArtworkV199.removeTileBackground(bitmap);
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
  paint.setFilterBitmap(scale<1f);paint.setAntiAlias(false);
  canvas.drawBitmap(bitmap,null,new RectF(Math.round(target.centerX()-w/2),Math.round(target.centerY()-h/2),Math.round(target.centerX()-w/2)+w,Math.round(target.centerY()-h/2)+h),paint);
 }
 // Equipment-only projection; retained source bytes and inventory rendering stay unchanged.
 private static final Map<Bitmap,android.graphics.Rect> equipmentBounds=Collections.synchronizedMap(new WeakHashMap<>());
 static android.graphics.Rect visibleEquipmentBounds(Bitmap bitmap){
  android.graphics.Rect cached=equipmentBounds.get(bitmap);if(cached!=null)return cached;
  int w=bitmap.getWidth(),h=bitmap.getHeight(),left=w,top=h,right=-1,bottom=-1;int[] pixels=new int[w*h];bitmap.getPixels(pixels,0,w,0,0,w,h);
  for(int y=0;y<h;y++)for(int x=0;x<w;x++)if(android.graphics.Color.alpha(pixels[y*w+x])>=16){left=Math.min(left,x);right=Math.max(right,x);top=Math.min(top,y);bottom=Math.max(bottom,y);}
  android.graphics.Rect b=right<left?new android.graphics.Rect():new android.graphics.Rect(left,top,right+1,bottom+1);equipmentBounds.put(bitmap,b);return b;
 }
 // V178 phone feedback: both gloves use the V177 right-slot direction; all shoes flip from V177.
 static boolean equipmentMirrored(String slot){
  return "모자".equals(slot)||"갑옷".equals(slot);
 }
 static float equipmentSize(String slot,String itemId){
  String base=RpgProgressionState.baseSlot(slot);
  if("귀걸이".equals(base))return .85f*.8f; // 80% of the delivered V177 size, for every earring.
  if("반지".equals(base))return .8f; // Both slots, every ring.
  return "모자".equals(base)||RpgProgressionState.STARTER_SHIRT_ITEM_ID.equals(itemId)?.85f:1f;
 }
 private static final Map<Bitmap,android.graphics.PointF> equipmentCenters=Collections.synchronizedMap(new WeakHashMap<>());
 static android.graphics.PointF equipmentOpticalCenter(Bitmap bitmap){
  android.graphics.PointF cached=equipmentCenters.get(bitmap);if(cached!=null)return cached;
  android.graphics.Rect src=visibleEquipmentBounds(bitmap);double mass=0,sx=0,sy=0;
  for(int y=src.top;y<src.bottom;y++)for(int x=src.left;x<src.right;x++){
   int alpha=android.graphics.Color.alpha(bitmap.getPixel(x,y));if(alpha<16)continue;
   mass+=alpha;sx+=(x+.5)*alpha;sy+=(y+.5)*alpha;
  }
  android.graphics.PointF center=mass==0?new android.graphics.PointF(src.exactCenterX(),src.exactCenterY()):new android.graphics.PointF((float)(sx/mass),(float)(sy/mass));
  equipmentCenters.put(bitmap,center);return center;
 }
 static void drawEquipmentForSlot(Canvas canvas,Bitmap bitmap,RectF target,Paint paint,String slot,String itemId){
  if("무기".equals(RpgProgressionState.baseSlot(slot))){draw(canvas,bitmap,target,paint);return;}
  RectF fitted=new RectF(target);float size=equipmentSize(slot,itemId);fitted.inset(target.width()*(1-size)/2,target.height()*(1-size)/2);
  drawEquipment(canvas,bitmap,fitted,paint,equipmentMirrored(slot),"목걸이".equals(slot)||"갑옷".equals(slot));
 }
 static void drawEquipment(Canvas canvas,Bitmap bitmap,RectF target,Paint paint,boolean mirror){drawEquipment(canvas,bitmap,target,paint,mirror,false);}
 private static void drawEquipment(Canvas canvas,Bitmap bitmap,RectF target,Paint paint,boolean mirror,boolean optical){
  if(bitmap==null)return;android.graphics.Rect src=visibleEquipmentBounds(bitmap);if(src.isEmpty())return;
  android.graphics.PointF center=optical?equipmentOpticalCenter(bitmap):new android.graphics.PointF(src.exactCenterX(),src.exactCenterY());
  // Fit both extents about the visible centroid, so optical alignment never clips the source.
  float halfW=Math.max(center.x-src.left,src.right-center.x),halfH=Math.max(center.y-src.top,src.bottom-center.y);
  float scale=Math.min(target.width()/(2*halfW),target.height()/(2*halfH));
  float left=target.centerX()-(center.x-src.left)*scale,top=target.centerY()-(center.y-src.top)*scale;
  RectF dst=new RectF(left,top,left+src.width()*scale,top+src.height()*scale);
  paint.setFilterBitmap(false);paint.setAntiAlias(false);int save=canvas.save();canvas.clipRect(target);
  if(mirror)canvas.scale(-1,1,target.centerX(),target.centerY());canvas.drawBitmap(bitmap,src,dst,paint);canvas.restoreToCount(save);
 }
 Bitmap get(RpgProgressionState.ItemDefinition d){
  if(d==null)return null;Bitmap repaired=repairs.get(d.itemId);if(repaired!=null)return repaired;Bitmap source="IT_REAGENT_CURUM".equals(d.itemId)?get(d.itemId):atlas().get(d.itemId);if(source==null)source=get(d.itemId);if(source==null||!"무기".equals(d.equipSlot))return source;
  JSONObject row=receipt(d.itemId),cell=atlas().entry(d.itemId);String key="V210_DIAGONAL:"+(cell!=null?cell.optString("texture"):row==null?d.itemId:row.optString("assetPath",d.itemId));
  Bitmap icon=cache.get(key);if(icon==null){icon=WeaponInventoryArtworkV209.diagonal(source);cache.put(key,icon);}return icon;
 }
}
