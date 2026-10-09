package com.projectdark.mobile;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.util.*;
import java.io.InputStream;
import org.json.JSONObject;
import java.nio.charset.StandardCharsets;

/** Original Master thumbnails and labelled Cafe captures, shared by inventory and shops. */
final class SourceItemIconRegistry {
 private final Context context;
 private final Map<String,Bitmap> cache=new HashMap<>();
 private JSONObject manifest;
 SourceItemIconRegistry(Context c){context=c;try(InputStream in=c.getAssets().open("equipment-icons/manifest.json")){manifest=new JSONObject(new String(read(in),StandardCharsets.UTF_8));}catch(Exception e){manifest=new JSONObject();}}
 private static byte[] read(InputStream in)throws java.io.IOException{java.io.ByteArrayOutputStream out=new java.io.ByteArrayOutputStream();byte[] bytes=new byte[4096];int n;while((n=in.read(bytes))!=-1)out.write(bytes,0,n);return out.toByteArray();}
 Bitmap get(RpgProgressionState.ItemDefinition d){
  if(d==null)return null;
  String file=null;
  JSONObject items=manifest.optJSONObject("items");JSONObject item=items==null?null:items.optJSONObject(d.itemId);
  if(item!=null)file=item.optString("asset",null);
  if(file==null&&d.appearanceId!=null&&!d.appearanceId.isEmpty())file=d.appearanceId+".png";
  if(RpgProgressionState.CHUNGRYONG_ITEM_ID.equals(d.itemId))file="../weapons/chungryong/icon.png";
  if(file==null)return null;if(cache.containsKey(file))return cache.get(file);
  Bitmap b=null;String path=file.startsWith("../")?file.substring(3):"equipment-icons/"+file;
  try(InputStream in=context.getAssets().open(path)){BitmapFactory.Options o=new BitmapFactory.Options();o.inScaled=false;b=BitmapFactory.decodeStream(in,null,o);}catch(Exception e){}
  cache.put(file,b);return b;
 }
}
