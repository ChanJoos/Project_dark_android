package com.projectdark.mobile;
import android.content.Context;
import android.graphics.*;
import org.json.*;
import java.util.*;
/** Complete native inventory sprites. Never rotate or recolor already authored icon pixels. */
final class OriginalInventoryV252 {
 private final Context context;
 final JSONObject entries;
 private Bitmap sheet;
 private final Map<String,Bitmap> cache=new HashMap<>();
 OriginalInventoryV252(Context c){context=c;try{entries=new JSONObject(new String(PresentationAssetBytes.read(c,"inventory-v252/manifest.json"),java.nio.charset.StandardCharsets.UTF_8));}catch(Exception e){throw new IllegalStateException("Original inventory manifest unavailable",e);}}
 JSONObject receipt(String id){return entries.optJSONObject(RpgProgressionState.canonicalItemId(id));}
 Bitmap get(String id){
  String key=RpgProgressionState.canonicalItemId(id);JSONObject row=entries.optJSONObject(key);if(row==null)return null;
  Bitmap b=cache.get(key);if(b!=null)return b;
  try{if(sheet==null){BitmapFactory.Options o=new BitmapFactory.Options();o.inScaled=false;try(java.io.InputStream in=context.getAssets().open("inventory-v252/icons.png")){sheet=BitmapFactory.decodeStream(in,null,o);}if(sheet==null)throw new IllegalStateException("Invalid original inventory atlas");}
   b=Bitmap.createBitmap(sheet,row.getInt("x"),row.getInt("y"),row.getInt("w"),row.getInt("h"));cache.put(key,b);return b;
  }catch(Exception e){throw new IllegalStateException("Invalid original inventory icon "+key,e);}
 }
}
