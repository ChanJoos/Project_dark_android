package com.projectdark.mobile;
import android.content.Context;
import android.graphics.Bitmap;
/** The HUD/shop use the same original inventory art, including alpha separation. */
final class ReagentItemVisualRegistry {
 private final SourceItemIconRegistry icons;
 ReagentItemVisualRegistry(Context context){icons=new SourceItemIconRegistry(context);}
 Bitmap get(String itemId){return itemId==null?null:icons.get(itemId);}
 boolean sourceBacked(String itemId){return !"IT_REAGENT_CURUM".equals(itemId)&&!"IT_REAGENT_HOLYWATER".equals(itemId)&&get(itemId)!=null;}
}
