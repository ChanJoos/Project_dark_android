package com.projectdark.mobile;
import android.content.Context;import android.graphics.*;import org.json.*;import java.util.*;
/** Reviewed source-local alpha repairs shared by inventory and equipment windows. */
final class ItemIconRepairsV212 {
 private final Context context;private final JSONObject entries;private Bitmap sheet;private final Map<String,Bitmap> cache=new HashMap<>();
 ItemIconRepairsV212(Context c){context=c;try{entries=new JSONObject(new String(PresentationAssetBytes.read(c,"item-repairs-v212/manifest.json"),java.nio.charset.StandardCharsets.UTF_8));}catch(Exception e){throw new IllegalStateException(e);}}
 Bitmap get(String id){String key=RpgProgressionState.canonicalItemId(id);JSONObject e=entries.optJSONObject(key);if(e==null)return null;Bitmap b=cache.get(key);if(b!=null)return b;try{if(sheet==null)try(java.io.InputStream in=context.getAssets().open("item-repairs-v212/icons.png")){BitmapFactory.Options opts=new BitmapFactory.Options();opts.inScaled=false;sheet=BitmapFactory.decodeStream(in,null,opts);}b=Bitmap.createBitmap(sheet,e.getInt("x"),e.getInt("y"),e.getInt("w"),e.getInt("h"));cache.put(key,b);return b;}catch(Exception ex){throw new IllegalStateException("Repaired icon "+id,ex);}}
}
