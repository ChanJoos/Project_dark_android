package com.projectdark.mobile;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

/** Captured original core icons; Curum/holy-water icons are documented project reconstruction. */
final class ReagentItemVisualRegistry {
  private final Context context;
  private final Map<String,Bitmap> cache=new LinkedHashMap<>();
  ReagentItemVisualRegistry(Context context){this.context=context;}
  Bitmap get(String itemId){
    if(itemId==null||context==null)return null;
    if(cache.containsKey(itemId))return cache.get(itemId);
    String file=null;
    if(RpgProgressionState.REAGENT_KOMADIUM_ITEM_ID.equals(itemId))file="it_reagent_komadium.webp";
    else if(RpgProgressionState.REAGENT_DIBENOMUM_ITEM_ID.equals(itemId))file="it_reagent_dibenomum.webp";
    else if(RpgProgressionState.REAGENT_CURANUM_ITEM_ID.equals(itemId))file="it_reagent_curanum.webp";
    else if(RpgProgressionState.RECALL_MILLES_ITEM_ID.equals(itemId))file="it_recall_milles.webp";
    else if("IT_REAGENT_EXCURANUM".equals(itemId))file="it_reagent_excuranum.webp";
    Bitmap out=null;
    if(file!=null)try(InputStream in=context.getAssets().open("assets/items/consumable/"+file)){
      BitmapFactory.Options o=new BitmapFactory.Options();o.inScaled=false;
      out=cleanCapturedBackground(BitmapFactory.decodeStream(in,null,o));
    }catch(Throwable ignored){}
    if(out==null&&("IT_REAGENT_CURUM".equals(itemId)||"IT_REAGENT_HOLYWATER".equals(itemId)))try(InputStream in=context.getAssets().open("interiors/v85/icons.webp")){Bitmap atlas=BitmapFactory.decodeStream(in);int w=atlas.getWidth()/4,h=atlas.getHeight()/2;out=trimTransparent(Bitmap.createBitmap(atlas,"IT_REAGENT_CURUM".equals(itemId)?0:w,0,w,h));}catch(Exception ignored){}
    cache.put(itemId,out);return out;
  }
  private static Bitmap cleanCapturedBackground(Bitmap src){
    if(src==null||src.getWidth()<2||src.getHeight()<2)return src;
    int w=src.getWidth(),h=src.getHeight(),bg=src.getPixel(0,0);int br=Color.red(bg),bgc=Color.green(bg),bb=Color.blue(bg);
    Bitmap out=src.copy(Bitmap.Config.ARGB_8888,true);boolean[] seen=new boolean[w*h];int[] q=new int[w*h];int head=0,tail=0;
    int[] seeds={0,w-1,(h-1)*w,h*w-1};for(int z:seeds){if(!seen[z]){seen[z]=true;q[tail++]=z;}}
    while(head<tail){int z=q[head++],x=z%w,y=z/w,c=out.getPixel(x,y);int d=Math.abs(Color.red(c)-br)+Math.abs(Color.green(c)-bgc)+Math.abs(Color.blue(c)-bb);if(d>42)continue;out.setPixel(x,y,Color.TRANSPARENT);if(x>0&&!seen[z-1]){seen[z-1]=true;q[tail++]=z-1;}if(x+1<w&&!seen[z+1]){seen[z+1]=true;q[tail++]=z+1;}if(y>0&&!seen[z-w]){seen[z-w]=true;q[tail++]=z-w;}if(y+1<h&&!seen[z+w]){seen[z+w]=true;q[tail++]=z+w;}}
    return trimTransparent(out);
  }
  private static Bitmap trimTransparent(Bitmap b){int l=b.getWidth(),t=b.getHeight(),r=-1,bt=-1;for(int y=0;y<b.getHeight();y++)for(int x=0;x<b.getWidth();x++)if((b.getPixel(x,y)>>>24)!=0){if(x<l)l=x;if(x>r)r=x;if(y<t)t=y;if(y>bt)bt=y;}return r>=l?Bitmap.createBitmap(b,l,t,r-l+1,bt-t+1):b;}
  boolean sourceBacked(String itemId){return !"IT_REAGENT_CURUM".equals(itemId)&&!"IT_REAGENT_HOLYWATER".equals(itemId)&&get(itemId)!=null;}
}