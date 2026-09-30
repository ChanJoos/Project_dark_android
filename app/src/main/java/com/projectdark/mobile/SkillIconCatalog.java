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
  private final Map<String,Bitmap> presentation=new HashMap<>();
  private final Paint paint=new Paint();
  SkillIconCatalog(Context context){
    try(InputStream image=context.getAssets().open("skills/source_icons.png");InputStream meta=context.getAssets().open("skills/source_icons.json")){
      BitmapFactory.Options options=new BitmapFactory.Options();options.inScaled=false;
      atlas=BitmapFactory.decodeStream(image,null,options);
      ByteArrayOutputStream bytes=new ByteArrayOutputStream();byte[] buffer=new byte[4096];int n;while((n=meta.read(buffer))!=-1)bytes.write(buffer,0,n);
      JSONObject rows=new JSONObject(new String(bytes.toByteArray(),StandardCharsets.UTF_8)).getJSONObject("icons");
      Iterator<String> keys=rows.keys();while(keys.hasNext()){String id=keys.next();index.put(id,rows.getInt(id));}
      if(atlas==null)throw new IOException("Invalid skill atlas");
      for(Map.Entry<String,Integer> row:index.entrySet()){
        int i=row.getValue();Bitmap icon=Bitmap.createBitmap(atlas,(i%10)*40,(i/10)*40,40,40).copy(Bitmap.Config.ARGB_8888,true);
        // Remove only capture-background cyan in the four corner regions; preserve original atlas bytes.
        for(int y=0;y<40;y++)for(int x=0;x<40;x++){
          int corner=Math.min(x,39-x)+Math.min(y,39-y),color=icon.getPixel(x,y);
          if(corner<18&&Color.green(color)>Color.red(color)+8&&Color.blue(color)>Color.red(color)+8)icon.setPixel(x,y,Color.TRANSPARENT);
        }
        presentation.put(row.getKey(),icon);
      }
      for(String directory:new String[]{"classic","rogue"}){
      JSONObject refs=new JSONObject(new String(PresentationAssetBytes.read(context,"skill-presentation/"+directory+"/references.json"),StandardCharsets.UTF_8));JSONArray list=refs.getJSONArray("rows");
      for(int rowIndex=0;rowIndex<list.length();rowIndex++){JSONObject row=list.getJSONObject(rowIndex);if(row.isNull("id"))continue;String id=row.getString("id"),path=row.getString("iconAssetPath");Bitmap icon=BitmapFactory.decodeStream(context.getAssets().open("skill-presentation/"+directory+"/"+path));if(icon==null)throw new IOException("Invalid classic icon: "+path);presentation.put(id,icon);index.putIfAbsent(id,-1);}
      }
    }catch(Exception e){throw new IllegalStateException("Skill source icons unavailable",e);}
  }
  boolean has(String id){return index.containsKey(id);}
  boolean draw(Canvas canvas,String id,RectF dest){
    Integer i=index.get(id);if(i==null)return false;
    int x=(i%10)*40,y=(i/10)*40;paint.setFilterBitmap(false);
    Path mask=new Path();mask.addRoundRect(dest,dest.width()*.20f,dest.height()*.20f,Path.Direction.CW);
    canvas.save();canvas.clipPath(mask);
    Bitmap image=presentation.get(id);canvas.drawBitmap(image,new Rect(1,1,image.getWidth()-1,image.getHeight()-1),dest,paint);canvas.restore();return true;
  }
}
