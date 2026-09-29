package com.projectdark.mobile;

import android.content.Context;
import android.graphics.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import org.json.*;

/** Exact job/name source crops. Missing images never borrow another skill's art. */
final class SkillIconCatalog {
  private final Bitmap atlas;
  private final Map<String,Integer> index=new HashMap<>();
  private final Paint paint=new Paint();
  SkillIconCatalog(Context context){
    try(InputStream image=context.getAssets().open("skills/source_icons.png");InputStream meta=context.getAssets().open("skills/source_icons.json")){
      BitmapFactory.Options options=new BitmapFactory.Options();options.inScaled=false;
      atlas=BitmapFactory.decodeStream(image,null,options);
      ByteArrayOutputStream bytes=new ByteArrayOutputStream();byte[] buffer=new byte[4096];int n;while((n=meta.read(buffer))!=-1)bytes.write(buffer,0,n);
      JSONObject rows=new JSONObject(new String(bytes.toByteArray(),StandardCharsets.UTF_8)).getJSONObject("icons");
      Iterator<String> keys=rows.keys();while(keys.hasNext()){String id=keys.next();index.put(id,rows.getInt(id));}
      if(atlas==null)throw new IOException("Invalid skill atlas");
    }catch(Exception e){throw new IllegalStateException("Skill source icons unavailable",e);}
  }
  boolean has(String id){return index.containsKey(id);}
  boolean draw(Canvas canvas,String id,RectF dest){
    Integer i=index.get(id);if(i==null)return false;
    int x=(i%10)*40,y=(i/10)*40;paint.setFilterBitmap(false);
    canvas.drawBitmap(atlas,new Rect(x,y,x+40,y+40),dest,paint);return true;
  }
}
