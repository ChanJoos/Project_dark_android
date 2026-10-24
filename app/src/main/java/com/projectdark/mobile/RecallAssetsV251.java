package com.projectdark.mobile;
import android.content.Context;import android.graphics.*;import org.json.*;import java.util.*;
/** All screenshot recalls are admitted as assets; only four existing town IDs are granted. */
final class RecallAssetsV251 {
 static final String[] LIVE={"IT_RECALL_MILLES","IT_RECALL_ABEL","IT_RECALL_PIET","IT_RECALL_RUCESION"};
 private final Context context;private final Map<String,JSONObject> rows=new LinkedHashMap<>();private final Map<String,Bitmap> cache=new HashMap<>();
 RecallAssetsV251(Context c){context=c;try{JSONArray a=new JSONObject(new String(PresentationAssetBytes.read(c,"recalls-v251/manifest.json"),java.nio.charset.StandardCharsets.UTF_8)).getJSONArray("recalls");for(int i=0;i<a.length();i++){JSONObject r=a.getJSONObject(i);r.put("assetPath",r.getString("path")).put("identityMatch","USER_SCREENSHOT_V251");rows.put(r.getString("id"),r);}}catch(Exception e){throw new IllegalStateException("Recall source manifest",e);}}
 JSONObject receipt(String id){return rows.get(RpgProgressionState.canonicalItemId(id));}
 Bitmap get(String id){JSONObject r=receipt(id);if(r==null)return null;String key=r.optString("id");Bitmap b=cache.get(key);if(b==null)try(java.io.InputStream in=context.getAssets().open(r.getString("path"))){BitmapFactory.Options o=new BitmapFactory.Options();o.inScaled=false;b=BitmapFactory.decodeStream(in,null,o);if(b==null)throw new IllegalStateException(key);b=transparentBackground(b);cache.put(key,b);}catch(Exception e){throw new IllegalStateException(key,e);}return b;}
 /** Display-only matte: flood the near-black screenshot background from its perimeter.
     Keep enclosed dark artwork and all foreground RGB; immutable source PNGs stay intact. */
 static Bitmap transparentBackground(Bitmap source){
  int w=source.getWidth(),h=source.getHeight();int[] pixels=new int[w*h];source.getPixels(pixels,0,w,0,0,w,h);
  boolean[] seen=new boolean[pixels.length];int[] queue=new int[pixels.length];int end=0,start=0;
  for(int i=0;i<pixels.length;i++){int x=i%w,y=i/w;if((x==0||y==0||x==w-1||y==h-1)&&background(pixels[i])){seen[i]=true;queue[end++]=i;}}
  while(start<end){int n=queue[start++],x=n%w,y=n/w;pixels[n]=0;
   for(int k=0;k<4;k++){int xx=x+(k==0?-1:k==1?1:0),yy=y+(k==2?-1:k==3?1:0);if(xx<0||xx>=w||yy<0||yy>=h)continue;int next=yy*w+xx;if(!seen[next]&&background(pixels[next])){seen[next]=true;queue[end++]=next;}}
  }
  return Bitmap.createBitmap(pixels,w,h,Bitmap.Config.ARGB_8888);
 }
 private static boolean background(int p){return Color.alpha(p)==0||Math.max(Color.red(p),Math.max(Color.green(p),Color.blue(p)))<=24;}
}
