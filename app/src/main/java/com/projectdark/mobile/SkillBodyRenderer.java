package com.projectdark.mobile;

import android.content.Context;
import android.graphics.*;
import org.json.*;
import java.util.*;
import java.nio.charset.StandardCharsets;

/** Both original bodies, fixed source root, shared layer transform and source-group equipment. */
final class SkillBodyRenderer {
  private final Context context;private final SkillPresentationCatalog catalog;private final EquipmentVisualRegistry gear;
  private final Map<String,Bitmap> bitmaps=new HashMap<>();private final Map<String,JSONObject> registrations=new HashMap<>();
  private final Paint paint=new Paint();private final ChungryongWeaponRenderer chungryong;
  static final float SCALE=CharacterRenderer.SOURCE_PRESENTATION_SCALE*43f/51f;
  SkillBodyRenderer(Context c,SkillPresentationCatalog s){context=c;catalog=s;gear=new EquipmentVisualRegistry(c);chungryong=new ChungryongWeaponRenderer(c);paint.setFilterBitmap(false);}
  boolean drawChungryong(Canvas c,CharacterRenderer.Pose p,String body,String motion,float phase){
    if(!ChungryongWeaponRenderer.equipped(p.weaponVisualRef))return false;
    boolean back=ChungryongWeaponRenderer.back(p.direction);int n=ChungryongWeaponRenderer.frame(motion,phase);
    String group=ChungryongWeaponRenderer.group(motion);int index=ChungryongWeaponRenderer.bodyIndex(motion,back,n);
    JSONObject f=catalog.frames.optJSONObject(body+"/"+group+"/"+index);if(f==null)return false;Bitmap b=bitmap(f.optString("path"));if(b==null)return false;
    float x=p.x,y=p.y+8*SCALE;
    float[] hand=ChungryongWeaponRenderer.hand(group,index,b.getWidth(),b.getHeight());
    float hx=x+(hand[0]-(float)f.optDouble("pivotX"))*SCALE;
    float hy=y+(hand[1]-b.getHeight()-(float)f.optDouble("offsetY"))*SCALE;
    c.save();if(ChungryongWeaponRenderer.west(p.direction))c.scale(-1,1,x,y);
    if(back)chungryong.layer(c,motion,true,n,hx,hy,SCALE);
    float left=x-(float)f.optDouble("pivotX")*SCALE,top=y-(b.getHeight()+(float)f.optDouble("offsetY"))*SCALE;
    c.drawBitmap(b,null,new RectF(left,top,left+b.getWidth()*SCALE,top+b.getHeight()*SCALE),paint);
    for(String id:p.equipmentVisualRef==null?new String[0]:p.equipmentVisualRef.split(","))drawGear(c,id,group,index,p.direction,x,y);
    if(!back)chungryong.layer(c,motion,false,n,hx,hy,SCALE);
    c.restore();return true;
  }
  boolean draw(Canvas c,CharacterRenderer.Pose p,String body,String motion,float phase){
    if(p.direction==null)return false;String key=catalog.frameKey(body,motion,p.direction,phase);JSONObject f=key==null?null:catalog.frames.optJSONObject(key);if(f==null)return false;
    Bitmap b=bitmap(f.optString("path"));if(b==null)return false;
    String[] parts=key.split("/");String group=parts[1];int index=Integer.parseInt(parts[2]);
    boolean mirror=p.direction==CharacterRenderer.Direction.NW||p.direction==CharacterRenderer.Direction.SW;
    float rootX=p.x,rootY=p.y+8f*SCALE;
    if("JUMP".equals(motion)){paint.setColor(0x50000000);c.drawOval(new RectF(p.x-7,p.y-2,p.x+7,p.y+2),paint);paint.setColor(Color.WHITE);rootY-=jumpLift(phase);}
    c.save();if(mirror)c.scale(-1,1,rootX,rootY);
    boolean weapon=motion.equals("THRUST")||motion.equals("SWING");
    if(weapon){if(ChungryongWeaponRenderer.equipped(p.weaponVisualRef))chungryong.layer(c,"basic",ChungryongWeaponRenderer.back(p.direction),0,rootX+6*SCALE,rootY-29*SCALE,SCALE);else drawGear(c,p.weaponVisualRef,group,index,p.direction,rootX,rootY);}
    float left=rootX-(float)f.optDouble("pivotX")*SCALE,top=rootY-(b.getHeight()+(float)f.optDouble("offsetY"))*SCALE;
    c.drawBitmap(b,null,new RectF(left,top,left+b.getWidth()*SCALE,top+b.getHeight()*SCALE),paint);
    for(String id:p.equipmentVisualRef==null?new String[0]:p.equipmentVisualRef.split(","))drawGear(c,id,group,index,p.direction,rootX,rootY);
    c.restore();return true;
  }
  // Presentation-only takeoff/contact/landing; world position and accepted sphere stay fixed.
  static float jumpLift(float phase){float q=Math.max(0,Math.min(1,phase));return q<1f/3f?12f*q*3:12f*(1-q)*1.5f;}
  private void drawGear(Canvas c,String id,String group,int index,CharacterRenderer.Direction direction,float x,float y){
    if(id==null||id.isEmpty())return;EquipmentVisualRegistry.Visual v=gear.get(id);if(v==null||v.atlas==null)return;
    JSONObject j=registration(id);if(j==null)return;JSONObject sprites=j.optJSONObject("sprites");if(sprites==null)return;
    JSONArray a=sprites.optJSONArray(group);JSONObject f=a!=null&&index<a.length()?a.optJSONObject(index):null;
    // Source outfits with no such group retain their real standing cloth layer. No naked fallback.
    // This is an explicit ADAPTED registration, not a claimed original outfit animation.
    if(f==null){a=sprites.optJSONArray("01");int n=direction==CharacterRenderer.Direction.NW||direction==CharacterRenderer.Direction.NE?0:1;f=a!=null&&n<a.length()?a.optJSONObject(n):null;}
    if(f==null)return;int w=f.optInt("w"),h=f.optInt("h"),sx=f.optInt("sx"),sy=f.optInt("sy");if(w<=0||h<=0||sx+w>v.atlas.getWidth()||sy+h>v.atlas.getHeight())return;
    float left=x-(float)f.optDouble("px")*w*SCALE,top=y-(h-(float)f.optDouble("py")*h)*SCALE;
    c.drawBitmap(v.atlas,new Rect(sx,sy,sx+w,sy+h),new RectF(left,top,left+w*SCALE,top+h*SCALE),paint);
  }
  private JSONObject registration(String id){if(registrations.containsKey(id))return registrations.get(id);JSONObject j=null;try{j=new JSONObject(new String(PresentationAssetBytes.read(context,"source-registration/"+id+".json"),StandardCharsets.UTF_8));}catch(Exception ignored){}registrations.put(id,j);return j;}
  private Bitmap bitmap(String path){if(bitmaps.containsKey(path))return bitmaps.get(path);Bitmap b=null;try{b=BitmapFactory.decodeStream(context.getAssets().open("skill-presentation/"+path));}catch(Exception ignored){}bitmaps.put(path,b);return b;}
}
