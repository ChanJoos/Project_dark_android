package com.projectdark.mobile;

import android.content.Context;
import android.graphics.*;
import org.json.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** Labelled classic-server captures. Never grants unresolved mechanics or excluded skills. */
final class ClassicSkillReference {
  static final class Channel {
    final String key;final boolean casterAnchor;final CapturedSkillFx.Sequence sequence;
    Channel(Context c,String key,JSONObject j)throws Exception{this.key=key;casterAnchor="CASTER".equals(j.getString("anchor"));sequence=new CapturedSkillFx.Sequence(c,j);}
  }
  Channel damageImpact;
  final Map<String,Map<String,Channel>> effects=new LinkedHashMap<>();
  final Map<String,JSONObject> references=new LinkedHashMap<>();
  private final Paint paint=new Paint();
  private final PorterDuffXfermode screen=new PorterDuffXfermode(PorterDuff.Mode.SCREEN);
  ClassicSkillReference(Context c){this(c,"classic");}
  ClassicSkillReference(Context c,String directory){try{
    JSONObject doc=read(c,directory+"/manifest.json"),entries=doc.getJSONObject("skills");
    if(doc.has("damageImpact"))damageImpact=new Channel(c,"RECIPIENT_DAMAGE",doc.getJSONObject("damageImpact"));
    Iterator<String> ids=entries.keys();while(ids.hasNext()){String id=ids.next();JSONObject channels=entries.getJSONObject(id).getJSONObject("channels");Map<String,Channel> result=new LinkedHashMap<>();Iterator<String> keys=channels.keys();while(keys.hasNext()){String key=keys.next();result.put(key,new Channel(c,key,channels.getJSONObject(key)));}effects.put(id,result);}
    JSONArray rows=read(c,directory+"/references.json").getJSONArray("rows");for(int i=0;i<rows.length();i++){JSONObject row=rows.getJSONObject(i);if(!row.isNull("id"))references.put(row.getString("id"),row);}
  }catch(Exception e){throw new IllegalStateException("Classic skill reference",e);}}
  private static JSONObject read(Context c,String path)throws Exception{return new JSONObject(new String(PresentationAssetBytes.read(c,"skill-presentation/"+path),StandardCharsets.UTF_8));}
  boolean has(String id){return effects.containsKey(id);}
  Channel channel(String id,boolean start){Map<String,Channel> channels=effects.get(id);return channels==null?null:channels.get(start?"CASTER_START":"RECIPIENT_CONTACT");}
  void drawRegistered(Canvas canvas,Channel channel,float age,float x,float y,float pivotX,float pivotY,float dx,float dy,boolean projectile,int topCrop){
    CapturedSkillFx.Sequence s=channel.sequence;int frame=s.frame(age),col=frame%s.columns,row=frame/s.columns;
    canvas.save();
    if(projectile&&Math.abs(dx)+Math.abs(dy)>.001f)canvas.rotate((float)Math.toDegrees(Math.atan2(dy,dx)-Math.atan2(13,28)),x,y);
    else if(dx<0)canvas.scale(-1,1,x,y);
    paint.setFilterBitmap(s.filterBitmap);paint.setXfermode(s.normalBlend?null:screen);
    canvas.drawBitmap(s.atlas,new Rect(col*s.width,row*s.height+topCrop,(col+1)*s.width,(row+1)*s.height),new RectF(x-pivotX*s.scale,y+(topCrop-pivotY)*s.scale,x+(s.width-pivotX)*s.scale,y+(s.height-pivotY)*s.scale),paint);
    paint.setXfermode(null);canvas.restore();
  }
  void draw(Canvas canvas,Channel channel,float age,float x,float y){CapturedSkillFx.Sequence s=channel.sequence;int frame=s.frame(age),col=frame%s.columns,row=frame/s.columns;paint.setFilterBitmap(s.filterBitmap);paint.setXfermode(s.normalBlend?null:screen);canvas.drawBitmap(s.atlas,new Rect(col*s.width,row*s.height,(col+1)*s.width,(row+1)*s.height),new RectF(x-s.pivotX*s.scale,y-s.pivotY*s.scale,x+(s.width-s.pivotX)*s.scale,y+(s.height-s.pivotY)*s.scale),paint);paint.setXfermode(null);}
}
