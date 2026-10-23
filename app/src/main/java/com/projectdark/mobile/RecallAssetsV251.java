package com.projectdark.mobile;
import android.content.Context;import android.graphics.*;import org.json.*;import java.util.*;
/** All screenshot recalls are admitted as assets; only four existing town IDs are granted. */
final class RecallAssetsV251 {
 static final String[] LIVE={"IT_RECALL_MILLES","IT_RECALL_ABEL","IT_RECALL_PIET","IT_RECALL_RUCESION"};
 private final Context context;private final Map<String,JSONObject> rows=new LinkedHashMap<>();private final Map<String,Bitmap> cache=new HashMap<>();
 RecallAssetsV251(Context c){context=c;try{JSONArray a=new JSONObject(new String(PresentationAssetBytes.read(c,"recalls-v251/manifest.json"),java.nio.charset.StandardCharsets.UTF_8)).getJSONArray("recalls");for(int i=0;i<a.length();i++){JSONObject r=a.getJSONObject(i);r.put("assetPath",r.getString("path")).put("identityMatch","USER_SCREENSHOT_V251");rows.put(r.getString("id"),r);}}catch(Exception e){throw new IllegalStateException("Recall source manifest",e);}}
 JSONObject receipt(String id){return rows.get(RpgProgressionState.canonicalItemId(id));}
 Bitmap get(String id){JSONObject r=receipt(id);if(r==null)return null;String key=r.optString("id");Bitmap b=cache.get(key);if(b==null)try(java.io.InputStream in=context.getAssets().open(r.getString("path"))){BitmapFactory.Options o=new BitmapFactory.Options();o.inScaled=false;b=BitmapFactory.decodeStream(in,null,o);if(b==null)throw new IllegalStateException(key);cache.put(key,b);}catch(Exception e){throw new IllegalStateException(key,e);}return b;}
}
