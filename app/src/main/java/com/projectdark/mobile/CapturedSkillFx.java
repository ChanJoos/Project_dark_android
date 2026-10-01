package com.projectdark.mobile;

import android.content.Context;
import android.graphics.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import org.json.*;

/** Actual capture frames with source timing. Matting/occlusion remain explicit approximations. */
final class CapturedSkillFx {
  static final class Sequence {
    final boolean directional,normalBlend,visualCenter,filterBitmap;final float directionPivotLift,sourceForwardDistance;final Bitmap atlas;final int width,height,columns;final int[] durations;final float duration,pivotX,pivotY,scale;
    Sequence(Context c,JSONObject j)throws Exception{
      atlas=BitmapFactory.decodeStream(c.getAssets().open("skill-presentation/"+j.getString("path")));
      if(atlas==null)throw new IllegalStateException("missing capture atlas");width=j.getInt("width");height=j.getInt("height");columns=j.getInt("columns");
      JSONArray times=j.getJSONArray("durationsMs");durations=new int[times.length()];int total=0;
      for(int i=0;i<durations.length;i++){durations[i]=times.getInt(i);if(durations[i]<=0)throw new IllegalStateException("capture timing");total+=durations[i];}
      filterBitmap=j.optBoolean("filterBitmap",false);visualCenter="VISUAL_CENTER".equals(j.optString("registration"));directional=j.optBoolean("directional",false);normalBlend="NORMAL".equals(j.optString("blend"));directionPivotLift=(float)j.optDouble("directionPivotLift",0);sourceForwardDistance=(float)j.optDouble("sourceForwardDistance",1);
      duration=total/1000f;pivotX=(float)j.getDouble("pivotX");pivotY=(float)j.getDouble("pivotY");scale=(float)j.getDouble("scale");
    }
    int frame(float age){int elapsed=Math.max(0,(int)(age*1000));for(int i=0;i<durations.length;i++){if(elapsed<durations[i])return i;elapsed-=durations[i];}return durations.length-1;}
  }
  final Map<String,Sequence> sequences=new LinkedHashMap<>();
  private final PorterDuffXfermode screen=new PorterDuffXfermode(PorterDuff.Mode.SCREEN);
  CapturedSkillFx(Context c){try{
    JSONObject doc=new JSONObject(new String(PresentationAssetBytes.read(c,"skill-presentation/captured/manifest.json"),StandardCharsets.UTF_8));JSONObject entries=doc.getJSONObject("skills");
    Iterator<String> ids=entries.keys();while(ids.hasNext()){String id=ids.next();sequences.put(id,new Sequence(c,entries.getJSONObject(id)));}
  }catch(Exception e){throw new IllegalStateException("capture manifest",e);}}
  Sequence get(String id){return sequences.get(id);}
  void draw(Canvas canvas,Paint paint,String id,float age,float x,float y){Sequence s=get(id);if(s==null)return;int frame=s.frame(age),col=frame%s.columns,row=frame/s.columns;
    paint.setAlpha(255);paint.setFilterBitmap(false);paint.setColorFilter(null);paint.setXfermode(s.normalBlend?null:screen);
    canvas.drawBitmap(s.atlas,new Rect(col*s.width,row*s.height,(col+1)*s.width,(row+1)*s.height),new RectF(x-s.pivotX*s.scale,y-s.pivotY*s.scale,x+(s.width-s.pivotX)*s.scale,y+(s.height-s.pivotY)*s.scale),paint);
    paint.setXfermode(null);
  }
  void drawDirected(Canvas c,Paint p,String id,float age,float x,float y,float dx,float dy){Sequence s=get(id);if(s==null)return;if(!s.directional){draw(c,p,id,age,x,y);return;}float lift=s.directionPivotLift*s.scale,angle=(float)Math.toDegrees(Math.atan2(dy,dx)),reach=(float)Math.hypot(dx,dy)/(s.sourceForwardDistance*s.scale);reach=Math.max(.5f,Math.min(2f,reach));c.save();c.translate(x,y-lift);c.rotate(angle);c.scale(reach,1);c.translate(-x,-(y-lift));draw(c,p,id,age,x,y);c.restore();}

}
