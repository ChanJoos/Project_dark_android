package com.projectdark.mobile;

import android.content.Context;
import android.graphics.Rect;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/** Exact dress-up registration from recovered source manifests. No semantic-anchor fallback. */
final class SourceEquipmentRegistration {
  static final class Frame {
    final Rect src; final float dx,dy;
    Frame(Rect src,float dx,float dy){this.src=src;this.dx=dx;this.dy=dy;}
  }
  private final JSONObject body,gear;
  private SourceEquipmentRegistration(JSONObject body,JSONObject gear){this.body=body;this.gear=gear;}
  static SourceEquipmentRegistration load(Context context,String identity){
    if(context==null||identity==null)return null;
    try{
      JSONObject b=new JSONObject(read(context,"source-registration/mm001.json"));
      JSONObject g=new JSONObject(read(context,"source-registration/"+identity+".json"));
      return new SourceEquipmentRegistration(b,g);
    }catch(Throwable ignored){return null;}
  }
  Frame idle(CharacterRenderer.Direction direction,int column){
    int frame=ShirtSourceRegistration.idleFrame(direction,column);
    return frame("01",frame);
  }
  Frame action(int sourceIndex){return frame("02",sourceIndex);}
  Frame idleByIndex(int index){return frame("01",index);}
  private Frame frame(String group,int index){
    try{
      JSONArray bs=body.getJSONObject("sprites").getJSONArray(group);
      JSONArray gs=gear.getJSONObject("sprites").getJSONArray(group);
      if(index<0||index>=bs.length()||index>=gs.length())return null;
      JSONObject b=bs.getJSONObject(index),g=gs.getJSONObject(index);
      int bw=b.getInt("w"),bh=b.getInt("h"),gw=g.getInt("w"),gh=g.getInt("h");
      float bx=(float)b.getDouble("px")*bw, by=-(float)b.getDouble("py")*bh;
      float gx=(float)g.getDouble("px")*gw, gy=-(float)g.getDouble("py")*gh;
      float dx=bx-gx;
      float dy=bh+by-gh-gy;
      return new Frame(new Rect(g.getInt("sx"),g.getInt("sy"),g.getInt("sx")+gw,g.getInt("sy")+gh),dx,dy);
    }catch(Throwable ignored){return null;}
  }
  private static String read(Context c,String path)throws Exception{
    InputStream in=c.getAssets().open(path);byte[] buf=new byte[8192];StringBuilder out=new StringBuilder();int n;
    while((n=in.read(buf))>0)out.append(new String(buf,0,n,StandardCharsets.UTF_8));in.close();return out.toString();
  }
}