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
 private JSONObject manifest,full;
 SourceItemIconRegistry(Context c){context=c;try(InputStream in=c.getAssets().open("item-icons/manifest.json")){full=new JSONObject(new String(read(in),StandardCharsets.UTF_8));}catch(Exception e){throw new IllegalStateException("Full original item artwork manifest unavailable",e);}
 try(InputStream in=c.getAssets().open("equipment-icons/manifest.json")){manifest=new JSONObject(new String(read(in),StandardCharsets.UTF_8));}catch(Exception e){manifest=new JSONObject();}}
 private static byte[] read(InputStream in)throws java.io.IOException{java.io.ByteArrayOutputStream out=new java.io.ByteArrayOutputStream();byte[] bytes=new byte[4096];int n;while((n=in.read(bytes))!=-1)out.write(bytes,0,n);return out.toByteArray();}
 JSONObject receipt(String id){return full.optJSONObject("items").optJSONObject(id);}
 Bitmap get(String id){
  JSONObject row=receipt(id);if(row==null)return null;String path=row.optString("assetPath",null);
  if(path==null)return null;if(cache.containsKey(path))return cache.get(path);
  Bitmap bitmap=null;try(InputStream in=context.getAssets().open(path)){BitmapFactory.Options options=new BitmapFactory.Options();options.inScaled=false;bitmap=BitmapFactory.decodeStream(in,null,options);}catch(Exception e){throw new IllegalStateException("Missing original item artwork: "+id,e);}
  cache.put(path,bitmap);return bitmap;
 }
 Bitmap get(RpgProgressionState.ItemDefinition d){return d==null?null:get(d.itemId);}
}
