package com.projectdark.mobile;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Map;

/** Source-backed classic reagent/recall icons. No placeholder drawing is permitted here. */
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
    else if(RpgProgressionState.RECALL_MILLES_ITEM_ID.equals(itemId))file="it_recall_milles.webp";
    // Curanum deliberately has no fabricated visual mapping until its source icon is confirmed.
    Bitmap out=null;
    if(file!=null)try(InputStream in=context.getAssets().open("assets/items/consumable/"+file)){
      BitmapFactory.Options o=new BitmapFactory.Options();o.inScaled=false;
      out=BitmapFactory.decodeStream(in,null,o);
    }catch(Throwable ignored){}
    cache.put(itemId,out);return out;
  }
  boolean sourceBacked(String itemId){return get(itemId)!=null;}
}