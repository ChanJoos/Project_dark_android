package com.projectdark.mobile;
import android.content.Context;
import android.graphics.*;
import org.json.*;
import java.util.*;
/** State-owned auras. No queued pulses, per-frame bitmap decode or damage from drawing. */
final class PersistentStatusV249 {
 RuntimeState state;
 final Map<String,CapturedSkillFx.Sequence> sequences=new LinkedHashMap<>();
 final Paint paint=new Paint();
 final NarcoliV253 narcoli=new NarcoliV253();
 final PorterDuffXfermode add=new PorterDuffXfermode(PorterDuff.Mode.ADD),screen=new PorterDuffXfermode(PorterDuff.Mode.SCREEN);
 PersistentStatusV249(Context c){try{JSONObject j=new JSONObject(new String(PresentationAssetBytes.read(c,"skill-presentation/status-v249/manifest.json"),java.nio.charset.StandardCharsets.UTF_8)).getJSONObject("persistent");for(String key:new String[]{"SLEEP","POISON"})sequences.put(key,new CapturedSkillFx.Sequence(c,j.getJSONObject(key)));sequences.put("SLEEP",new CapturedSkillFx.Sequence(c,new JSONObject(new String(PresentationAssetBytes.read(c,"skill-presentation/narcoli-v253/manifest.json"),java.nio.charset.StandardCharsets.UTF_8))));}catch(Exception e){throw new IllegalStateException("V249 status source",e);}paint.setFilterBitmap(false);}
 void draw(Canvas c,SkillVfxRenderer.Anchors a){if(state==null)return;for(RuntimeState.Monster m:state.monsters())if(m.alive)drawActor(c,a,m.id);if(state.player().alive)drawActor(c,a,"player");CompanionParty.Member ally=state.party().active();if(ally!=null&&ally.body.alive&&state.currentMapId().equals(ally.map))drawActor(c,a,ally.id);}
 boolean active(String actor,String kind){SkillEffectState.Effect e=state==null?null:state.skillEffects().get(actor,kind);return e!=null&&e.remaining>0&&(!kind.equals("POISON")||e.source.equals("SK_마법사_012"));}
 void drawActor(Canvas c,SkillVfxRenderer.Anchors a,String actor){for(String kind:sequences.keySet())if(active(actor,kind)){
  float x=a.centerX(actor),foot=a.y(actor),center=a.centerY(actor);if(!Float.isFinite(x)||!Float.isFinite(foot)||!Float.isFinite(center))continue;
  SkillEffectState.Effect e=state.skillEffects().get(actor,kind);SkillAbilityCatalog.Ability ability=SkillAbilityCatalog.get(e.source);float age=Math.max(0,(ability==null?e.source.equals("MONSTER_NARCOLI")?2.5f:e.remaining:ability.duration)-e.remaining);CapturedSkillFx.Sequence q=sequences.get(kind);
  // Register the full original Z envelope above the current body top.
  if(kind.equals("SLEEP"))narcoli.draw(c,x,foot,center,age);
  float y=kind.equals("SLEEP")?2*center-foot+q.pivotY-q.height-6:foot;
  int frame=q.frame(age%q.duration),sx=frame%q.columns*q.width,sy=frame/q.columns*q.height;
  paint.setXfermode(q.normalBlend?null:q.additiveBlend?add:screen);
  c.drawBitmap(q.atlas,new Rect(sx,sy,sx+q.width,sy+q.height),new RectF(x-q.pivotX,y-q.pivotY,x+q.width-q.pivotX,y+q.height-q.pivotY),paint);paint.setXfermode(null);
 }}
}
