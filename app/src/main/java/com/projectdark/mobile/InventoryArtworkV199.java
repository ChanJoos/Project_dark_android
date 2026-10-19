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
 static Bitmap removeTileBackground(Bitmap source){
  int w=source.getWidth(),h=source.getHeight();if(w<3||h<3)return source;
  int[] p=new int[w*h];source.getPixels(p,0,w,0,0,w,h);
  int[] corners={p[0],p[w-1],p[(h-1)*w],p[w*h-1]};
  for(int v:corners)if(Color.alpha(v)<16)return source;
  boolean[] seen=new boolean[p.length];int[] queue=new int[p.length];int end=0,start=0;
  for(int x=0;x<w;x++){if(background(p[x],corners)){seen[x]=true;queue[end++]=x;}int n=(h-1)*w+x;if(!seen[n]&&background(p[n],corners)){seen[n]=true;queue[end++]=n;}}
  for(int y=1;y<h-1;y++)for(int x:new int[]{0,w-1}){int n=y*w+x;if(!seen[n]&&background(p[n],corners)){seen[n]=true;queue[end++]=n;}}
  while(start<end){int n=queue[start++],x=n%w,y=n/w;for(int next:new int[]{x>0?n-1:-1,x<w-1?n+1:-1,y>0?n-w:-1,y<h-1?n+w:-1})if(next>=0&&!seen[next]&&background(p[next],corners)){seen[next]=true;queue[end++]=next;}}
  if(end==0||end==p.length)return source;
  for(int i=0;i<p.length;i++)if(seen[i])p[i]=0;
  return Bitmap.createBitmap(p,w,h,Bitmap.Config.ARGB_8888);
 }
 private static boolean background(int p,int[] corners){
  if(Color.alpha(p)<16)return true;
  for(int c:corners)if(Math.max(Math.abs(Color.red(p)-Color.red(c)),Math.max(Math.abs(Color.green(p)-Color.green(c)),Math.abs(Color.blue(p)-Color.blue(c))))<=38)return true;
  return false;
 }
}
