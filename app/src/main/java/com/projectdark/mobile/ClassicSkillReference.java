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
  private final Map<Channel,Bitmap> mageClean=new IdentityHashMap<>();
  private final PorterDuffXfermode screen=new PorterDuffXfermode(PorterDuff.Mode.SCREEN);
  ClassicSkillReference(Context c){this(c,"classic");}
  ClassicSkillReference(Context c,String directory){try{
    JSONObject doc=read(c,directory+"/manifest.json"),entries=doc.getJSONObject("skills");
    if(doc.has("damageImpact"))damageImpact=new Channel(c,"RECIPIENT_DAMAGE",doc.getJSONObject("damageImpact"));
    Iterator<String> ids=entries.keys();while(ids.hasNext()){String id=ids.next();JSONObject channels=entries.getJSONObject(id).getJSONObject("channels");Map<String,Channel> result=new LinkedHashMap<>();Iterator<String> keys=channels.keys();while(keys.hasNext()){String key=keys.next();result.put(key,new Channel(c,key,channels.getJSONObject(key)));}effects.put(id,result);}
    JSONArray rows=read(c,directory+"/references.json").getJSONArray("rows");for(int i=0;i<rows.length();i++){JSONObject row=rows.getJSONObject(i);if(!row.isNull("id"))references.put(row.getString("id"),row);}
    if(directory.equals("classic")){JSONObject extra=read(c,"user-v228/manifest.json").getJSONObject("skills");Iterator<String> newIds=extra.keys();while(newIds.hasNext()){String id=newIds.next();JSONObject raw=extra.getJSONObject(id).getJSONObject("channels");Map<String,Channel> channels=new LinkedHashMap<>();Iterator<String> keys=raw.keys();while(keys.hasNext()){String key=keys.next();channels.put(key,new Channel(c,key,raw.getJSONObject(key)));}effects.put(id,channels);}}
    if(directory.equals("classic")){prepareMageV237();loadMageVideoV238(c);loadMageVideoV239(c);}prewarmCenters();prepareBaekbo();
  }catch(Exception e){throw new IllegalStateException("Classic skill reference",e);}}
  private static JSONObject read(Context c,String path)throws Exception{return new JSONObject(new String(PresentationAssetBytes.read(c,"skill-presentation/"+path),StandardCharsets.UTF_8));}
  boolean has(String id){return effects.containsKey(SkillPolicyV227.effectSource(id));}
  Channel channel(String id,boolean start){Map<String,Channel> channels=effects.get(SkillPolicyV227.effectSource(id));return channels==null?null:channels.get(start?"CASTER_START":"RECIPIENT_CONTACT");}
  void drawRegistered(Canvas canvas,Channel channel,float age,float x,float y,float pivotX,float pivotY,float dx,float dy,boolean projectile,int topCrop){
    CapturedSkillFx.Sequence s=channel.sequence;int frame=s.frame(age),col=frame%s.columns,row=frame/s.columns;
    canvas.save();
    if(projectile&&Math.abs(dx)+Math.abs(dy)>.001f)canvas.rotate((float)Math.toDegrees(Math.atan2(dy,dx)-Math.atan2(13,28)),x,y);
    else if(dx<0)canvas.scale(-1,1,x,y);
    paint.setFilterBitmap(s.filterBitmap);paint.setXfermode(s.normalBlend?null:screen);
    canvas.drawBitmap(mageClean.containsKey(channel)?mageClean.get(channel):s.atlas,new Rect(col*s.width,row*s.height+topCrop,(col+1)*s.width,(row+1)*s.height),new RectF(x-pivotX*s.scale,y+(topCrop-pivotY)*s.scale,x+(s.width-pivotX)*s.scale,y+(s.height-pivotY)*s.scale),paint);
    paint.setXfermode(null);canvas.restore();
  }
  private void prepareMageV237(){
    for(String n:new String[]{"039","040","041","042","044","045","051","052"}){
      Channel ch=channel("SK_마법사_"+n,false);if(ch==null)continue;CapturedSkillFx.Sequence q=ch.sequence;Bitmap b=q.atlas.copy(Bitmap.Config.ARGB_8888,true);int w=b.getWidth(),h=b.getHeight();int[] pixels=new int[w*h];b.getPixels(pixels,0,w,0,0,w,h);
      int[][] masks=(n.equals("044"))?new int[][]{{31,20,62,64}}:(n.equals("045"))?new int[][]{{35,0,62,46}}:(n.equals("051")||n.equals("052"))?new int[][]{{31,28,62,79}}:new int[][]{{49,64,73,115},{48,54,79,63}};
      // Explicit occluded actor/HP areas. No invented fill through hidden source FX.
      for(int yy=0;yy<h;yy++)for(int xx=0;xx<w;xx++){int x=xx%q.width,y=yy%q.height;for(int[] r:masks)if(x>=r[0]&&x<r[2]&&y>=r[1]&&y<r[3]){pixels[yy*w+xx]=Color.TRANSPARENT;break;}}
      int[] clean=pixels.clone();for(int yy=0;yy<h;yy++)for(int xx=0;xx<w;xx++){int k=yy*w+xx;if(Color.alpha(pixels[k])==0)continue;int neighbors=0;for(int dy=-1;dy<=1;dy++)for(int dx=-1;dx<=1;dx++){int nx=xx+dx,ny=yy+dy;if(nx>=0&&nx<w&&ny>=0&&ny<h&&nx/q.width==xx/q.width&&ny/q.height==yy/q.height&&Color.alpha(pixels[ny*w+nx])>0)neighbors++;}if(neighbors<3)clean[k]=Color.TRANSPARENT;}
      b.setPixels(clean,0,w,0,0,w,h);b.prepareToDraw();mageClean.put(ch,b);
    }
  }
  Bitmap mageDisplayAtlas(Channel ch){return mageClean.get(ch);}
  private void loadMageVideoV238(Context c)throws Exception{
    JSONObject skills=read(c,"mage-v238/manifest.json").getJSONObject("skills");
    Iterator<String> ids=skills.keys();while(ids.hasNext()){String id=ids.next();JSONObject channels=skills.getJSONObject(id).getJSONObject("channels");Map<String,Channel> result=new LinkedHashMap<>();Iterator<String> keys=channels.keys();while(keys.hasNext()){String key=keys.next();result.put(key,new Channel(c,key,channels.getJSONObject(key)));}effects.put(id,result);}
  }
  private void loadMageVideoV239(Context c)throws Exception{
    JSONObject skills=read(c,"mage-v239/manifest.json").getJSONObject("skills");
    Iterator<String> ids=skills.keys();while(ids.hasNext()){String id=ids.next();JSONObject channels=skills.getJSONObject(id).getJSONObject("channels");Map<String,Channel> result=effects.computeIfAbsent(id,k->new LinkedHashMap<>());Iterator<String> keys=channels.keys();while(keys.hasNext()){String key=keys.next();result.put(key,new Channel(c,key,channels.getJSONObject(key)));}}
  }
  private Bitmap baekbo;private final java.util.List<Rect> baekboFrames=new ArrayList<>();
  /** Retain cyan burst pixels only; source actor/HP UI residue is never projected onto recipients. */
  private void prepareBaekbo(){Channel ch=channel(MartialSkillPolicy.LINE,false);if(ch==null)return;
    CapturedSkillFx.Sequence s=ch.sequence;baekbo=s.atlas.copy(Bitmap.Config.ARGB_8888,true);
    int[] pixels=new int[baekbo.getWidth()*baekbo.getHeight()];baekbo.getPixels(pixels,0,baekbo.getWidth(),0,0,baekbo.getWidth(),baekbo.getHeight());
    for(int n=0;n<pixels.length;n++){int color=pixels[n],r=Color.red(color),g=Color.green(color),b=Color.blue(color);if(g<100||b<100||r>=Math.min(g,b)*.7f)pixels[n]=Color.TRANSPARENT;}
    baekbo.setPixels(pixels,0,baekbo.getWidth(),0,0,baekbo.getWidth(),baekbo.getHeight());
    for(int frame=0;frame<s.durations.length;frame++){int ox=frame%s.columns*s.width,oy=frame/s.columns*s.height,l=s.width,t=s.height,r=-1,b=-1,count=0;
      for(int y=0;y<s.height;y++)for(int x=0;x<s.width;x++)if(Color.alpha(pixels[(oy+y)*baekbo.getWidth()+ox+x])>0){count++;l=Math.min(l,x);t=Math.min(t,y);r=Math.max(r,x);b=Math.max(b,y);}
      if(count>=300)baekboFrames.add(new Rect(ox+l,oy+t,ox+r+1,oy+b+1));
    }baekbo.prepareToDraw();
  }
  void drawBaekboContact(Canvas c,float age,float x,float y){if(baekbo==null||baekboFrames.isEmpty())return;Rect r=baekboFrames.get(Math.min(baekboFrames.size()-1,(int)(age/.32f*baekboFrames.size())));paint.setFilterBitmap(false);c.drawBitmap(baekbo,r,new RectF(x-r.width()/2f,y-r.height()/2f,x+r.width()/2f,y+r.height()/2f),paint);}
  private final Map<Channel,float[]> centers=new IdentityHashMap<>();
  private void prewarmCenters(){for(Map.Entry<String,Map<String,Channel>> entry:effects.entrySet()){
    if(!SkillVfxRenderer.recipientFinisher(entry.getKey()))continue;
    Channel channel=entry.getValue().get("RECIPIENT_CONTACT");if(channel==null)continue;
    CapturedSkillFx.Sequence s=channel.sequence;int l=s.width,t=s.height,r=0,b=0,w=s.atlas.getWidth(),h=s.atlas.getHeight();int[] pixels=new int[w*h];s.atlas.getPixels(pixels,0,w,0,0,w,h);
    for(int yy=0;yy<h;yy++)for(int xx=0;xx<w;xx++)if(Color.alpha(pixels[yy*w+xx])>24){int x=xx%s.width,y=yy%s.height;l=Math.min(l,x);r=Math.max(r,x+1);t=Math.min(t,y);b=Math.max(b,y+1);}
    centers.put(channel,l<r?new float[]{(l+r)*.5f,(t+b)*.5f}:new float[]{s.pivotX,s.pivotY});
  }}
  void drawCentered(Canvas canvas,Channel channel,float age,float x,float y){float[] p=centers.get(channel);drawRegistered(canvas,channel,age,x,y,p[0],p[1],0,0,false,0);}
  void draw(Canvas canvas,Channel channel,float age,float x,float y){
    CapturedSkillFx.Sequence s=channel.sequence;int frame=s.frame(age),col=frame%s.columns,row=frame/s.columns;paint.setFilterBitmap(s.filterBitmap);paint.setXfermode(s.normalBlend?null:screen);canvas.drawBitmap(mageClean.containsKey(channel)?mageClean.get(channel):s.atlas,new Rect(col*s.width,row*s.height,(col+1)*s.width,(row+1)*s.height),new RectF(x-s.pivotX*s.scale,y-s.pivotY*s.scale,x+(s.width-s.pivotX)*s.scale,y+(s.height-s.pivotY)*s.scale),paint);paint.setXfermode(null);}
}
