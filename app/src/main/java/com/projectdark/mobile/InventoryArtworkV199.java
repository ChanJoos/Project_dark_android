package com.projectdark.mobile;
import android.graphics.*;
import android.content.Context;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import org.json.JSONObject;
import java.util.*;
/** Display projections only; immutable source files are not repainted. */
final class InventoryArtworkV199 {
 static Bitmap front(JSONObject row,Context context){
  String id=row.optString("appearanceId","").toLowerCase(),projection=row.optString("projection","");
  if(!(id.startsWith("mu")||id.startsWith("wu"))||!projection.contains("IDLE"))return null;
  try(InputStream pixels=context.getAssets().open("wearable-atlases/"+id+".webp")){
   JSONObject j=new JSONObject(new String(PresentationAssetBytes.read(context,"source-registration/"+id+".json"),StandardCharsets.UTF_8));JSONObject frame=SourceEquipmentRegistration.sourceFrame(j.getJSONObject("sprites").getJSONArray("01"),1);if(frame==null)return null;
   BitmapFactory.Options opts=new BitmapFactory.Options();opts.inScaled=false;Bitmap atlas=BitmapFactory.decodeStream(pixels,null,opts);if(atlas==null)return null;
   int x=frame.getInt("sx"),y=frame.getInt("sy"),w=frame.getInt("w"),h=frame.getInt("h");if(x<0||y<0||x+w>atlas.getWidth()||y+h>atlas.getHeight())return null;
   // Only the small front crop is retained by the icon cache, not another full wearable atlas.
   return Bitmap.createBitmap(atlas,x,y,w,h);
  }catch(Exception ignored){return null;}
 }
 private static final int[] WOOD={0x291908,0x312110,0x291910,0x312119,0x211008,0x311910,0x100800,0x191008,0x311908,0x101008,0x100808,0x191010};
 private static final int[] FRAME={0xa58463,0x9c7b5a,0x736352,0x3a2100,0x3a2108,0x6b5242,0x211000,0x291000,0x311000,0x846329,0x94735a,0x947b63,0xa5846b,0xad8c73,0x6b3108,0x734a19,0x846b52,0x3a1900,0x523119,0x8c735a,0xad947b,0x634a3a,0x422910,0x6b4229,0x422919,0x524231,0x523a29};
 private static boolean near(int pixel,int[] palette){for(int c:palette)if(Math.max(Math.abs(Color.red(pixel)-Color.red(c)),Math.max(Math.abs(Color.green(pixel)-Color.green(c)),Math.abs(Color.blue(pixel)-Color.blue(c))))<=10)return true;return false;}
 static Bitmap removeTileBackground(Bitmap source){
  int w=source.getWidth(),h=source.getHeight();if(w<3||h<3)return source;
  int[] pixels=new int[w*h];source.getPixels(pixels,0,w,0,0,w,h);
  for(int y=0;y<h;y++)for(int x=0;x<w;x++){int i=y*w+x;boolean edge=x<5||x>=w-5||y<5||y>=h-5;if(near(pixels[i],WOOD)||edge&&near(pixels[i],FRAME))pixels[i]=0;}
  // Remove only tiny dark wood remnants, retaining bright isolated item highlights.
  boolean[] seen=new boolean[pixels.length];int[] queue=new int[pixels.length];
  for(int seed=0;seed<pixels.length;seed++)if(!seen[seed]&&Color.alpha(pixels[seed])>0){int end=1,start=0;queue[0]=seed;seen[seed]=true;boolean wood=true;
   while(start<end){int n=queue[start++],x=n%w,y=n/w,p=pixels[n];if(Math.max(Color.red(p),Math.max(Color.green(p),Color.blue(p)))>=130||Color.red(p)<Color.green(p)||Color.green(p)<Color.blue(p))wood=false;
    for(int yy=Math.max(0,y-1);yy<=Math.min(h-1,y+1);yy++)for(int xx=Math.max(0,x-1);xx<=Math.min(w-1,x+1);xx++){int next=yy*w+xx;if(!seen[next]&&Color.alpha(pixels[next])>0){seen[next]=true;queue[end++]=next;}}
   }if(end<=3&&wood)for(int n=0;n<end;n++)pixels[queue[n]]=0;
  }
  return Bitmap.createBitmap(pixels,w,h,Bitmap.Config.ARGB_8888);
 }
}
