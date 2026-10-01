package com.projectdark.mobile;

import android.content.Context;
import android.graphics.*;
import java.util.*;
import org.json.*;

/** User-selected screenshot pixels, kept separate from historical skill source art. */
final class SkillWindowSkin {
  private final Map<String,Bitmap> images=new HashMap<>();
  private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG|Paint.FILTER_BITMAP_FLAG);
  SkillWindowSkin(Context context){try{
    JSONObject manifest=new JSONObject(new String(PresentationAssetBytes.read(context,"skill-window/manifest.json"),java.nio.charset.StandardCharsets.UTF_8));
    JSONObject crops=manifest.getJSONObject("crops");Iterator<String> keys=crops.keys();while(keys.hasNext()){
      String key=keys.next();BitmapFactory.Options options=new BitmapFactory.Options();options.inScaled=false;
      Bitmap image=BitmapFactory.decodeStream(context.getAssets().open(crops.getJSONObject(key).getString("path")),null,options);
      if(image==null)throw new IllegalStateException("Invalid window crop "+key);images.put(key,image);
    }
  }catch(Exception e){throw new IllegalStateException("Skill window reference skin unavailable",e);}}
  boolean draw(Canvas c,String key,RectF rect){Bitmap image=images.get(key);if(image==null)return false;c.drawBitmap(image,null,rect,paint);return true;}
  void panel(Canvas c,RectF r,boolean detail){
    draw(c,detail?"detail-paper":"paper",r);
    draw(c,detail?"detail-header":"list-header",new RectF(r.left,r.top,r.right,r.top+31));
    draw(c,detail?"detail-left":"left-edge",new RectF(r.left,r.top+31,r.left+4,r.bottom));
    draw(c,detail?"detail-right":"right-edge",new RectF(r.right-4,r.top+31,r.right,r.bottom));
    draw(c,detail?"detail-bottom":"bottom-edge",new RectF(r.left,r.bottom-4,r.right,r.bottom));
  }
}
