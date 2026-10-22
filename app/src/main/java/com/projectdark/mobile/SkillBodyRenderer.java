package com.projectdark.mobile;

import android.content.Context;
import android.graphics.*;
import org.json.*;
import java.util.*;
import java.nio.charset.StandardCharsets;

/** Both original bodies, fixed source root, shared layer transform and source-group equipment. */
final class SkillBodyRenderer {
  private final Context context;private final SkillPresentationCatalog catalog;private final EquipmentVisualRegistry gear;
  private final Map<String,Bitmap> bitmaps=new HashMap<>();private final Map<String,JSONObject> registrations=new LinkedHashMap<String,JSONObject>(24,.75f,true){@Override protected boolean removeEldestEntry(Map.Entry<String,JSONObject> e){return size()>24;}};
  private final Map<String,CharacterSemanticRig.Anchors> rigs=new HashMap<>();
  private int rigBuilds,bitmapLoads;
  int rigBuildCount(){return rigBuilds;}
  int bitmapLoadCount(){return bitmapLoads;}
  private CharacterSemanticRig.Anchors rig(String key,Bitmap b,CharacterRenderer.Direction d){
    CharacterSemanticRig.Anchors r=rigs.get(key);if(r==null){r=CharacterSemanticRig.derive(b,new Rect(0,0,b.getWidth(),b.getHeight()),d);rigs.put(key,r);rigBuilds++;}return r;
  }
  private final Paint paint=new Paint();private final ChungryongWeaponRenderer chungryong;
  static final float SCALE=CharacterRenderer.SOURCE_PRESENTATION_SCALE*43f/51f;
  SkillBodyRenderer(Context c,SkillPresentationCatalog s){context=c;catalog=s;gear=new EquipmentVisualRegistry(c);chungryong=new ChungryongWeaponRenderer(c);paint.setFilterBitmap(false);
    // Predecode retained BODY clips off the draw path. Equipment is prepared on state changes.
    for(Iterator<String> it=catalog.frames.keys();it.hasNext();){JSONObject f=catalog.frames.optJSONObject(it.next());if(f!=null)bitmap(f.optString("path"));}
    for(String body:new String[]{"mm001","wm001"})for(int n:new int[]{1,3}){JSONObject f=catalog.frames.optJSONObject(body+"/02/"+n);if(f!=null){Bitmap b=bitmap(f.optString("path"));if(b!=null)rig(body+"/02/"+n,b,n==1?CharacterRenderer.Direction.NE:CharacterRenderer.Direction.SE);}}
  }
  private String preparedEquipment,preparedWeapon;
  void prepare(String equipment,String weapon){
    if(Objects.equals(preparedEquipment,equipment)&&Objects.equals(preparedWeapon,weapon))return;
    for(String id:equipment==null?new String[0]:equipment.split(",")){gear.get(id);registration(id);if(RapidHairclipV199.source(id)){String female=RapidHairclipV199.gender(id,"wm001");gear.get(female);registration(female);}}
    gear.get(weapon);registration(weapon);preparedEquipment=equipment;preparedWeapon=weapon;
  }
  boolean drawChungryong(Canvas c,CharacterRenderer.Pose p,String body,String motion,float phase){
    if(!ChungryongWeaponRenderer.equipped(p.weaponVisualRef))return false;
    if("twohand".equals(motion)&&(phase<.12f||phase>=.68f)){motion="basic";phase=0;}
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
    drawRapid(c,p,body,group,index,x,y,true);for(String id:p.equipmentVisualRef==null?new String[0]:p.equipmentVisualRef.split(","))if(WingShieldV215.layer(id)&&!WingShieldV215.inFront(id,p.direction))drawGear(c,id,body,group,index,p.direction,x,y);
    c.drawBitmap(b,null,new RectF(left,top,left+b.getWidth()*SCALE,top+b.getHeight()*SCALE),paint);
    for(String id:p.equipmentVisualRef==null?new String[0]:p.equipmentVisualRef.split(","))if(!RapidHairclipV199.source(id)&&(!WingShieldV215.layer(id)||WingShieldV215.inFront(id,p.direction)))drawGear(c,id,body,group,index,p.direction,x,y);
    if(!back)chungryong.layer(c,motion,false,n,hx,hy,SCALE);
    drawRapid(c,p,body,group,index,x,y,false);
    c.restore();return true;
  }
  boolean draw(Canvas c,CharacterRenderer.Pose p,String body,String motion,float phase){
    if(p.direction==null)return false;
    // group02 is an authored directional contact pose, not a complete temporal clip.
    // Its real source standing preparation/recovery keeps the blade and clothes registered.
    if(("SWING".equals(motion)||"EQUIPPED_BASIC".equals(motion))&&(phase<1f/3f||phase>=.8f)||"TWO_HAND".equals(motion)&&(phase<.12f||phase>=.68f))motion="IDLE";
    String key=catalog.frameKey(body,motion,p.direction,phase);JSONObject f=key==null?null:catalog.frames.optJSONObject(key);if(f==null)return false;
    Bitmap b=bitmap(f.optString("path"));if(b==null)return false;
    String[] parts=key.split("/");String group=parts[1];int index=Integer.parseInt(parts[2]);
    boolean mirror=!"02".equals(group)&&(p.direction==CharacterRenderer.Direction.NW||p.direction==CharacterRenderer.Direction.SW);
    float rootX=p.x,rootY=p.y+8f*SCALE;
    if("JUMP_KICK".equals(motion))rootY-=jumpLift(phase);
    if("JUMP".equals(motion)){paint.setColor(0x50000000);c.drawOval(new RectF(p.x-7,p.y-2,p.x+7,p.y+2),paint);paint.setColor(Color.WHITE);rootY-=jumpLift(phase);}
    c.save();if(mirror)c.scale(-1,1,rootX,rootY);
    boolean jumpWeapon="JUMP".equals(motion)&&ChungryongWeaponRenderer.equipped(p.weaponVisualRef);
    boolean back=ChungryongWeaponRenderer.back(p.direction);
    float jumpHandX=rootX+(13f*b.getWidth()/17f-(float)f.optDouble("pivotX"))*SCALE;
    float jumpHandY=rootY+(24-b.getHeight()-(float)f.optDouble("offsetY"))*SCALE;
    if(jumpWeapon&&back)chungryong.layer(c,"basic",true,0,jumpHandX,jumpHandY,SCALE);
    boolean weapon=motion.equals("ROGUE_STAB")||motion.equals("THRUST")||motion.equals("SWING")||motion.equals("TWO_HAND")||motion.equals("IDLE")||motion.equals("WALK");
    if(weapon&&back){if(ChungryongWeaponRenderer.equipped(p.weaponVisualRef))chungryong.layer(c,"basic",true,0,rootX+6*SCALE,rootY-29*SCALE,SCALE);else drawHeldWeapon(c,p.weaponVisualRef,body,group,index,p.direction,rootX,rootY,b,f);}
    float left=rootX-(float)f.optDouble("pivotX")*SCALE,top=rootY-(b.getHeight()+(float)f.optDouble("offsetY"))*SCALE;
    drawRapid(c,p,body,group,index,rootX,rootY,true);for(String id:p.equipmentVisualRef==null?new String[0]:p.equipmentVisualRef.split(","))if(WingShieldV215.layer(id)&&!WingShieldV215.inFront(id,p.direction))drawGear(c,id,body,group,index,p.direction,rootX,rootY);
    c.drawBitmap(b,null,new RectF(left,top,left+b.getWidth()*SCALE,top+b.getHeight()*SCALE),paint);
    for(String id:p.equipmentVisualRef==null?new String[0]:p.equipmentVisualRef.split(","))if(!RapidHairclipV199.source(id)&&(!WingShieldV215.layer(id)||WingShieldV215.inFront(id,p.direction)))drawGear(c,id,body,group,index,p.direction,rootX,rootY);
    if(weapon&&!back){if(ChungryongWeaponRenderer.equipped(p.weaponVisualRef))chungryong.layer(c,"basic",false,0,rootX+6*SCALE,rootY-29*SCALE,SCALE);else drawHeldWeapon(c,p.weaponVisualRef,body,group,index,p.direction,rootX,rootY,b,f);}
    if(jumpWeapon&&!back)chungryong.layer(c,"basic",false,0,jumpHandX,jumpHandY,SCALE);
    drawRapid(c,p,body,group,index,rootX,rootY,false);
    c.restore();return true;
  }
  // Presentation-only takeoff/contact/landing; world position and accepted sphere stay fixed.
  static float jumpLift(float phase){float q=Math.max(0,Math.min(1,phase));return q<1f/3f?12f*q*3:12f*(1-q)*1.5f;}
  /** Preserve source blade pixels; translate its registered source grip to the current BODY hand. */
  private void drawHeldWeapon(Canvas c,String id,String body,String group,int index,CharacterRenderer.Direction direction,float x,float y,Bitmap current,JSONObject currentFrame){
    JSONObject own=registration(id);
    JSONObject sprites=own==null?null:own.optJSONObject("sprites");
    JSONArray exact=sprites==null?null:sprites.optJSONArray(group);
    if(exact!=null){drawGear(c,id,body,group,index,direction,x,y);return;}
    EquipmentVisualRegistry.Visual v=gear.get(id);JSONObject registration=registration(id);if(v==null||v.atlas==null||registration==null)return;
    boolean back=ChungryongWeaponRenderer.back(direction);int sourceIndex=back?1:3;
    JSONArray frames=registration.optJSONObject("sprites").optJSONArray("02");JSONObject f=SourceEquipmentRegistration.sourceFrame(frames,sourceIndex);
    JSONObject baseFrame=catalog.frames.optJSONObject(body+"/02/"+sourceIndex);if(f==null||baseFrame==null){drawGear(c,id,body,group,index,direction,x,y);return;}
    Bitmap base=bitmap(baseFrame.optString("path"));if(base==null)return;
    CharacterRenderer.Direction sourceDirection=back?CharacterRenderer.Direction.NE:CharacterRenderer.Direction.SE;
    CharacterSemanticRig.Anchors baseRig=rig(body+"/02/"+sourceIndex,base,sourceDirection);
    float hx,hy;
    if("e".equals(group)&&index>=16&&index<=23){float[] hand=ChungryongWeaponRenderer.hand(group,index,current.getWidth(),current.getHeight());hx=hand[0];hy=hand[1];}
    else if("c".equals(group)&&index<14){float[] hand=ChungryongWeaponRenderer.hand(group,index,current.getWidth(),current.getHeight());hx=hand[0];hy=hand[1];}
    else{CharacterSemanticRig.Anchors a=rig(body+"/"+group+"/"+index,current,sourceDirection);hx=a.dominantHand.x;hy=a.dominantHand.y;}
    float dx=hx-(float)currentFrame.optDouble("pivotX")-(baseRig.dominantHand.x-(float)baseFrame.optDouble("pivotX"));
    float dy=hy-current.getHeight()-(float)currentFrame.optDouble("offsetY")-(baseRig.dominantHand.y-base.getHeight()-(float)baseFrame.optDouble("offsetY"));
    int w=f.optInt("w"),h=f.optInt("h"),sx=f.optInt("sx"),sy=f.optInt("sy");if(w<=0||h<=0||sx+w>v.atlas.getWidth()||sy+h>v.atlas.getHeight())return;
    float left=x+(dx-(float)f.optDouble("px")*w)*SCALE,top=y+(dy-h+(float)f.optDouble("py")*h)*SCALE;
    c.drawBitmap(v.atlas,new Rect(sx,sy,sx+w,sy+h),new RectF(left,top,left+w*SCALE,top+h*SCALE),paint);
  }
  private void drawRapid(Canvas c,CharacterRenderer.Pose p,String body,String group,int index,float x,float y,boolean behind){
    for(String id:RapidHairclipV199.layers(p.equipmentVisualRef,body,p.direction,behind))drawGear(c,id,body,group,index,p.direction,x,y);
  }
  private void drawGear(Canvas c,String id,String body,String group,int index,CharacterRenderer.Direction direction,float x,float y){
    if(id==null||id.isEmpty())return;EquipmentVisualRegistry.Visual v=gear.get(id);if(v==null||v.atlas==null)return;
    JSONObject j=registration(id);if(j==null)return;JSONObject sprites=j.optJSONObject("sprites");if(sprites==null)return;
    JSONArray a=sprites.optJSONArray(group);int gearIndex="02".equals(group)?WingShieldV215.actionFrame(id,index):index;JSONObject f=SourceEquipmentRegistration.sourceFrame(a,gearIndex);
    // Explicit source group omissions have no layer; do not substitute a later/standing pose.
    if(a!=null&&f==null)return;
    boolean adapted=f==null;
    // Missing action clips are attached to the current BODY rig, never left at standing coordinates.
    if(f==null){a=sprites.optJSONArray("01");int n=direction==CharacterRenderer.Direction.NW||direction==CharacterRenderer.Direction.NE?0:1;f=SourceEquipmentRegistration.sourceFrame(a,n);}
    if(f==null)return;int w=f.optInt("w"),h=f.optInt("h"),sx=f.optInt("sx"),sy=f.optInt("sy");if(w<=0||h<=0||sx+w>v.atlas.getWidth()||sy+h>v.atlas.getHeight())return;
    float left=x-(float)f.optDouble("px")*w*SCALE,top=y-(h-(float)f.optDouble("py")*h)*SCALE;
    c.save();if(adapted){Matrix attachment=attachment(id,body,group,index,direction);c.translate(x,y);c.scale(SCALE,SCALE);c.concat(attachment);c.scale(1/SCALE,1/SCALE);c.translate(-x,-y);}
    c.drawBitmap(v.atlas,new Rect(sx,sy,sx+w,sy+h),new RectF(left,top,left+w*SCALE,top+h*SCALE),paint);c.restore();
  }
  private final Map<String,Matrix> attachments=new HashMap<>();
  int attachmentBuildCount(){return attachments.size();}
  /** ADAPTED: source pixels remain intact; cached BODY shoulder/pelvis skinning follows crouches. */
  private Matrix attachment(String id,String body,String group,int index,CharacterRenderer.Direction direction){
    boolean back=direction==CharacterRenderer.Direction.NW||direction==CharacterRenderer.Direction.NE;
    String key=id+"/"+body+"/"+group+"/"+index+"/"+back;
    Matrix m=attachments.get(key);if(m!=null)return m;m=new Matrix();attachments.put(key,m);
    JSONObject from=catalog.frames.optJSONObject(body+"/01/"+(back?0:1)),to=catalog.frames.optJSONObject(body+"/"+group+"/"+index);
    if(from==null||to==null)return m;Bitmap a=bitmap(from.optString("path")),b=bitmap(to.optString("path"));if(a==null||b==null)return m;
    CharacterRenderer.Direction d=back?CharacterRenderer.Direction.NE:CharacterRenderer.Direction.SE;
    CharacterSemanticRig.Anchors ar=rig(body+"/01/"+(back?0:1),a,d),br=rig(body+"/"+group+"/"+index,b,d);
    if(id.startsWith("ml")||id.startsWith("wl")){translateAnchor(m,ar.foot,br.foot,from,to,a,b);return m;}
    if(RapidHairclipV199.source(id)||id.startsWith("mh")||id.startsWith("wh")){translateAnchor(m,new CharacterSemanticRig.Point(ar.shoulder.x,0),new CharacterSemanticRig.Point(br.shoulder.x,0),from,to,a,b);return m;}
    if(id.startsWith("ms")||id.startsWith("ws")){translateAnchor(m,ar.dominantHand,br.dominantHand,from,to,a,b);return m;}
    float[] src={localX(ar.shoulder,from),localY(ar.shoulder,from,a),localX(ar.pelvis,from),localY(ar.pelvis,from,a)};
    float[] dst={localX(br.shoulder,to),localY(br.shoulder,to,b),localX(br.pelvis,to),localY(br.pelvis,to,b)};
    m.setPolyToPoly(src,0,dst,0,2);return m;
  }
  private static float localX(CharacterSemanticRig.Point p,JSONObject f){return p.x-(float)f.optDouble("pivotX");}
  private static float localY(CharacterSemanticRig.Point p,JSONObject f,Bitmap b){return p.y-b.getHeight()-(float)f.optDouble("offsetY");}
  private static void translateAnchor(Matrix m,CharacterSemanticRig.Point a,CharacterSemanticRig.Point b,JSONObject from,JSONObject to,Bitmap src,Bitmap dst){m.setTranslate(localX(b,to)-localX(a,from),localY(b,to,dst)-localY(a,from,src));}
  private JSONObject registration(String id){if(registrations.containsKey(id))return registrations.get(id);JSONObject j=null;try{j=new JSONObject(new String(PresentationAssetBytes.read(context,"extended-wearables-v215/"+id+".json"),StandardCharsets.UTF_8));}catch(Exception extendedMissing){try{j=new JSONObject(new String(PresentationAssetBytes.read(context,"wearables-v212/registrations/"+id+".json"),StandardCharsets.UTF_8));}catch(Exception missing){try{j=new JSONObject(new String(PresentationAssetBytes.read(context,"source-registration/"+id+".json"),StandardCharsets.UTF_8));}catch(Exception ignored){}}}registrations.put(id,j);return j;}
  private Bitmap bitmap(String path){if(bitmaps.containsKey(path))return bitmaps.get(path);Bitmap b=null;try(java.io.InputStream in=context.getAssets().open("skill-presentation/"+path)){b=BitmapFactory.decodeStream(in);if(b!=null){b.prepareToDraw();bitmapLoads++;}}catch(Exception ignored){}bitmaps.put(path,b);return b;}
}
