package com.projectdark.mobile;
import android.content.Context;
import android.graphics.*;
import java.util.*;
import org.json.*;
/** Packed inventory textures only. Original item identity is never derived from image equality. */
final class ItemAtlasV210 {
 private final Context context;final JSONObject manifest;
 private final LinkedHashMap<Integer,Bitmap> pages=new LinkedHashMap<>(4,.75f,true);
 private final Map<String,Bitmap> icons=new HashMap<>();
 ItemAtlasV210(Context c){context=c;try{manifest=new JSONObject(new String(PresentationAssetBytes.read(c,"item-atlas-v210/manifest.json"),java.nio.charset.StandardCharsets.UTF_8));}catch(Exception e){throw new IllegalStateException("Item atlas unavailable",e);}}
 JSONObject entry(String id){return manifest.optJSONObject("items").optJSONObject(RpgProgressionState.canonicalItemId(id));}
 Bitmap get(String id){
  JSONObject e=entry(id);if(e==null)return null;String key=e.optString("texture");Bitmap icon=icons.get(key);if(icon!=null)return icon;
  try{int n=e.getInt("page");Bitmap page=pages.get(n);if(page==null){BitmapFactory.Options o=new BitmapFactory.Options();o.inScaled=false;try(java.io.InputStream in=context.getAssets().open(String.format(java.util.Locale.ROOT,"item-atlas-v210/page-%02d.png",n))){page=BitmapFactory.decodeStream(in,null,o);}if(page==null)throw new IllegalStateException("Invalid item page");pages.put(n,page);if(pages.size()>2)pages.remove(pages.keySet().iterator().next());}
   icon=Bitmap.createBitmap(page,e.getInt("x"),e.getInt("y"),e.getInt("w"),e.getInt("h"));icons.put(key,icon);return icon;
  }catch(Exception bad){throw new IllegalStateException("Invalid packed item "+id,bad);}
 }
 int pageCacheCount(){return pages.size();}
}
