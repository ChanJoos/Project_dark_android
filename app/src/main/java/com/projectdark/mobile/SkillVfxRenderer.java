package com.projectdark.mobile;

import android.content.Context;
import android.graphics.*;
import java.util.*;

/** Event-only visual projection: accepted start => caster, resolved non-miss => recipient. */
final class SkillVfxRenderer {
  interface Anchors {float x(String id);float y(String id);default float facingX(String id){return 32;}default float facingY(String id){return 16;}default float centerX(String id){return x(id);}default float centerY(String id){return y(id)-25f;}}
  static final class Pulse {
    final long actionSequence;final String id,sheet,anchor;final int row;final boolean caster;final float duration,x,y;float age;float directionX=1,directionY;
    Pulse(long seq,String id,String sheet,int row,String anchor,boolean caster,float duration,float x,float y){actionSequence=seq;this.id=id;this.sheet=sheet;this.row=row;this.anchor=anchor;this.caster=caster;this.duration=duration;this.x=x;this.y=y;}
  }
  private final CapturedSkillFx captured;private final ClassicSkillReference classic,rogue,warrior,shared;private final SkillPresentationCatalog catalog;private final Map<String,Bitmap> sheets=new HashMap<>();
  private final CurseSkullFx curseSkull;
  final PersistentStatusV249 statusFx;
  private final CapturedSkillFx.Sequence meteor;
  private static final int MAX_PULSES=1024;
  private static final float[][] SEMELIA_OFFSETS={{32,16},{-32,16},{32,-16},{-32,-16}};
  boolean testAccess;java.util.function.BooleanSupplier martialTriple=()->false;
  final List<Pulse> pulses=new ArrayList<>();private final Set<String> seen=new HashSet<>();private final Deque<String> history=new ArrayDeque<>();private final Paint p=new Paint();
  SkillVfxRenderer(Context c,SkillPresentationCatalog catalog){this.catalog=catalog;statusFx=new PersistentStatusV249(c);try{meteor=new CapturedSkillFx.Sequence(c,new org.json.JSONObject(new String(PresentationAssetBytes.read(c,"skill-presentation/screen-magic-v204/meteor.json"),java.nio.charset.StandardCharsets.UTF_8)));}catch(Exception ex){throw new IllegalStateException("Meteor original frames",ex);}captured=new CapturedSkillFx(c);classic=new ClassicSkillReference(c);curseSkull=new CurseSkullFx(c);rogue=new ClassicSkillReference(c,"rogue");warrior=new ClassicSkillReference(c,"warrior");shared=new ClassicSkillReference(c,"shared");p.setFilterBitmap(false);for(String name:new String[]{"caster","target","status","finisher-v65"})try{BitmapFactory.Options options=new BitmapFactory.Options();options.inSampleSize=name.equals("finisher-v65")?1:2;sheets.put(name,BitmapFactory.decodeStream(c.getAssets().open("skill-presentation/"+name+".png"),null,options));}catch(Exception ignored){}}
  void tick(float dt){float safe=Math.max(0,dt);for(Pulse f:pulses)f.age+=safe;pulses.removeIf(f->f.age>=f.duration);}
  private final Map<Long,float[]> actionDirections=new LinkedHashMap<>();
  private final Map<Long,String> directionTargets=new LinkedHashMap<>();private final Set<Long> releasedDirections=new HashSet<>();
  void clear(){pulses.clear();actionDirections.clear();directionTargets.clear();releasedDirections.clear();}
  private void lockDirection(Pulse p){float[] d=actionDirections.get(p.actionSequence);if(d!=null){p.directionX=d[0];p.directionY=d[1];}}
  /** Explicit test-only visual emission, independent from real Resolver hit feedback. */
  void preview(long seq,String id,boolean caster,String anchor,Anchors a){SkillPresentationCatalog.Entry v=catalog.get(id);if("SK_도적_003".equals(id)||v==null||SkillPolicyV227.retired(id)||anchor==null||MartialSkillPolicy.LEAP.equals(id))return;int row=caster?casterRow(v.caster):v.targetRow;if(row<0)return;float x=a.x(anchor),y=a.y(anchor);if(!Float.isFinite(x)||!Float.isFinite(y))return;pulses.add(new Pulse(seq,id,caster?"caster":v.targetSheet,row,anchor,caster,caster?.30f:.48f,x,y));if(pulses.size()>MAX_PULSES)pulses.remove(0);}
  void consume(List<CombatResolver.Event> events,Anchors a){for(CombatResolver.Event e:events){
    if(!RuntimeCombatSession.PLAYER_ID.equals(e.actorId)&&!e.actorId.startsWith("companion_")){
      if(e.type==CombatResolver.EventType.HIT_FEEDBACK&&e.amount>0&&(e.hitSemantic==CombatResolver.HitSemantic.DAMAGE||e.hitSemantic==CombatResolver.HitSemantic.CRIT)){
        String key=e.actionSequence+":monster-contact:"+e.targetId;float x=a.x(e.targetId),y=a.y(e.targetId);
        if(Float.isFinite(x)&&Float.isFinite(y)&&seen.add(key)){history.add(key);while(history.size()>256)seen.remove(history.removeFirst());pulses.add(new Pulse(e.actionSequence,e.actionId,"impact",0,e.targetId,false,warrior.damageImpact.sequence.duration,x,y));if(pulses.size()>MAX_PULSES)pulses.remove(0);}
      }
      continue;
    }
    if(e.type==CombatResolver.EventType.ACTION_STARTED){float dx=a.x(e.targetId)-a.x(e.actorId),dy=a.y(e.targetId)-a.y(e.actorId);
      if(!Float.isFinite(dx)||!Float.isFinite(dy)||Math.abs(dx)+Math.abs(dy)<.001f){dx=a.facingX(e.actorId);dy=a.facingY(e.actorId);}
      actionDirections.put(e.actionSequence,new float[]{dx,dy});directionTargets.put(e.actionSequence,e.targetId);while(actionDirections.size()>64){Long old=actionDirections.keySet().iterator().next();actionDirections.remove(old);directionTargets.remove(old);releasedDirections.remove(old);}
    }
    SkillActionContract.Rule directionRule=SkillActionContract.get(e.actionId);
    if(e.type==CombatResolver.EventType.HIT_FEEDBACK&&e.targetId.equals(directionTargets.get(e.actionSequence))&&!releasedDirections.contains(e.actionSequence)&&directionRule!=null&&directionRule.pattern==SkillActionContract.Pattern.SINGLE&&directionRule.damage()){
      float dx=a.x(e.targetId)-a.x(e.actorId),dy=a.y(e.targetId)-a.y(e.actorId);
      if(Float.isFinite(dx)&&Float.isFinite(dy)&&Math.abs(dx)+Math.abs(dy)>.001f){actionDirections.put(e.actionSequence,new float[]{dx,dy});for(Pulse p:pulses)if(p.actionSequence==e.actionSequence)lockDirection(p);}
      releasedDirections.add(e.actionSequence);
    }
    String id=e.actionId.startsWith("attack_proto_")?(martialTriple.getAsBoolean()?MartialSourceV228.TRIPLE:"SK_공통_001"):e.actionId;SkillPresentationCatalog.Entry v=catalog.get(id);if("SK_도적_003".equals(id)||v==null||SkillPolicyV227.retired(id)||id.equals("SK_공통_001")||MartialSkillPolicy.LEAP.equals(id))continue;
    if(e.type==CombatResolver.EventType.ACTION_CANCELLED){pulses.removeIf(f->f.actionSequence==e.actionSequence);continue;}
    boolean caster=e.type==CombatResolver.EventType.ACTION_STARTED,target=e.type==CombatResolver.EventType.HIT_FEEDBACK&&(e.hitSemantic!=CombatResolver.HitSemantic.MISS);
    if(!caster&&!target)continue;
    if(MartialSkillPolicy.LINE.equals(id)){
      if(target){String key=e.actionSequence+":martial-line:"+e.targetId;if(seen.add(key)){history.add(key);while(history.size()>256)seen.remove(history.removeFirst());pulses.add(new Pulse(e.actionSequence,id,"martial-line",0,e.targetId,false,.32f,a.x(e.targetId),a.y(e.targetId)));if(pulses.size()>MAX_PULSES)pulses.remove(0);}}continue;
    }
    // A real resolved damaging contact has its own recipient-only visual channel.
    // Preview (amount zero), misses and healing never pretend the monster took damage.
    if(target&&!semeliaCross(id)&&!sourceOriginalDamage(id)&&!MartialSourceV228.TRIPLE.equals(id)&&e.amount>0&&(e.hitSemantic==CombatResolver.HitSemantic.DAMAGE||e.hitSemantic==CombatResolver.HitSemantic.CRIT)&&!e.actorId.equals(e.targetId)){
      String impactKey=e.actionSequence+":recipient-damage:"+e.targetId;float ix=a.x(e.targetId),iy=a.y(e.targetId);
      if(Float.isFinite(ix)&&Float.isFinite(iy)&&seen.add(impactKey)){history.add(impactKey);while(history.size()>256)seen.remove(history.removeFirst());pulses.add(new Pulse(e.actionSequence,id,"impact",0,e.targetId,false,warrior.damageImpact.sequence.duration,ix,iy));if(pulses.size()>MAX_PULSES)pulses.remove(0);}
    }
    if(semeliaCross(id)){
      if(target){String key=e.actionSequence+":semelia-cross";if(seen.add(key)){history.add(key);while(history.size()>256)seen.remove(history.removeFirst());String center=id.equals("SK_마법사_035")?e.actorId:directionTargets.get(e.actionSequence);if(center==null)center=e.targetId;ClassicSkillReference.Channel ch=classic.channel(SkillPolicyV227.SEMELIA,false);pulses.add(new Pulse(e.actionSequence,id,"semelia-cross",0,center,false,ch.sequence.duration,a.centerX(center),a.centerY(center)));if(pulses.size()>MAX_PULSES)pulses.remove(0);}}continue;
    }
    if(id.equals("SK_마법사_017")||id.equals("SK_마법사_012"))continue; // Persistent status owns its sole visual channel.
    if(target&&id.equals("SK_마법사_043")){
      String key=e.actionSequence+":meteor-source:"+e.targetId;if(seen.add(key)){history.add(key);while(history.size()>256)seen.remove(history.removeFirst());pulses.add(new Pulse(e.actionSequence,id,"meteor-source",0,e.targetId,false,meteor.duration,a.x(e.targetId),a.y(e.targetId)));if(pulses.size()>MAX_PULSES)pulses.remove(0);}continue;
    }
    // A real curse contact gets one identical skull per recipient. Bypass captured actor/scene pixels.
    if(target&&CurseSkullFx.variant(id)>=0){
      String key=e.actionSequence+":curse-skull:"+e.targetId;
      float x=a.x(e.targetId),y=a.y(e.targetId);
      if(Float.isFinite(x)&&Float.isFinite(y)&&seen.add(key)){history.add(key);while(history.size()>256)seen.remove(history.removeFirst());pulses.add(new Pulse(e.actionSequence,id,"curse-skull",0,e.targetId,false,CurseSkullFx.DURATION,x,y));if(pulses.size()>MAX_PULSES)pulses.remove(0);}continue;
    }
    if(rogue.has(id)||classic.has(id)||warrior.has(id)||shared.has(id)){
      ClassicSkillReference reference=rogue.has(id)?rogue:classic.has(id)?classic:warrior.has(id)?warrior:shared;String sourceSheet=rogue.has(id)?"rogue":classic.has(id)?"classic":warrior.has(id)?"warrior":"shared";
      ClassicSkillReference.Channel channel=reference.channel(id,caster);if(channel==null)continue;
      // Capture anchor records the example's pivot, not who receives a live attack.
      SkillActionContract.Rule liveRule=SkillActionContract.get(id);
      boolean martialRecipient=sourceSheet.equals("classic")&&SkillPolicyV227.effectSource(id).startsWith("SK_무도가_")&&!caster&&liveRule!=null&&MartialCaptureRegistration.recipient(SkillPolicyV227.effectSource(id),caster);
      String anchor=(martialRecipient||!caster&&recipientFinisher(id))?e.targetId:channel.casterAnchor?e.actorId:e.targetId,key=e.actionSequence+":classic:"+channel.key+":"+anchor+((MartialSourceV228.MUYEONG.equals(id)||MartialSourceV228.TRIPLE.equals(id))?":"+e.sequence:"");
      if(!seen.add(key))continue;history.add(key);while(history.size()>256)seen.remove(history.removeFirst());
      float x=a.x(anchor),y=a.y(anchor);if(!Float.isFinite(x)||!Float.isFinite(y))continue;
      Pulse sourcePulse=new Pulse(e.actionSequence,id,sourceSheet,0,anchor,caster,channel.sequence.duration,x,y);sourcePulse.directionX=a.x(e.targetId)-a.x(e.actorId);sourcePulse.directionY=a.y(e.targetId)-a.y(e.actorId);lockDirection(sourcePulse);pulses.add(sourcePulse);if(pulses.size()>MAX_PULSES)pulses.remove(0);continue;
    }
boolean captureContact=target&&captured.get(id)!=null;String key=e.actionSequence+":"+(caster?"caster":captureContact?"capture":"target:"+e.targetId);if(!seen.add(key))continue;history.add(key);while(history.size()>256)seen.remove(history.removeFirst());
    int row=caster?casterRow(v.caster):v.targetRow;if(caster&&row<0||!caster&&v.target.equals("NONE"))continue;String anchor=caster||(captureContact&&captured.get(id).directional&&!recipientFinisher(id))?e.actorId:e.targetId;
    CapturedSkillFx.Sequence sequence=caster?null:captured.get(id);String sheet=sequence!=null?"capture":caster?"caster":v.targetSheet;float x=a.x(anchor),y=a.y(anchor);if(!Float.isFinite(x)||!Float.isFinite(y))continue;
    Pulse pulse=new Pulse(e.actionSequence,id,sheet,row,anchor,caster,sequence!=null?sequence.duration:caster?.30f:row<0||sheet.equals("finisher-v65")?.75f:.48f,x,y);pulse.directionX=a.x(e.targetId)-a.x(e.actorId);pulse.directionY=a.y(e.targetId)-a.y(e.actorId);lockDirection(pulse);pulses.add(pulse);if(pulses.size()>MAX_PULSES)pulses.remove(0);
  }}
  private boolean sourceOriginalDamage(String id){return id.startsWith("SK_도적_")&&(rogue.has(id)||captured.get(id)!=null)||id.startsWith("SK_마법사_")&&classic.has(id);}
  static boolean recipientFinisher(String id){SkillAbilityCatalog.Ability a=SkillAbilityCatalog.get(id);return a!=null&&a.damage()&&(a.resourceBurst()||a.formula.equals("METEOR")||a.formula.equals("HOLY_DRAGON"))&&!"CRASH".equals(a.formula);}
  static boolean semeliaCross(String id){return "SK_마법사_035".equals(id)||"SK_마법사_036".equals(id);}
  static int casterRow(String name){switch(name){case "SLASH":return 0;case "MARTIAL":return 1;case "ARCANE":return 2;case "HEAL":return 3;default:return -1;}}
  void draw(Canvas c,Anchors a){statusFx.draw(c,a);for(Pulse f:pulses){
    if(f.sheet.equals("semelia-cross")){float x=a.centerX(f.anchor),y=a.centerY(f.anchor);if(!Float.isFinite(x)||!Float.isFinite(y)){x=f.x;y=f.y;}ClassicSkillReference.Channel ch=classic.channel(SkillPolicyV227.SEMELIA,false);for(float[] offset:SEMELIA_OFFSETS)classic.drawCentered(c,ch,f.age,x+offset[0],y+offset[1]);continue;}
    if(f.sheet.equals("meteor-source")){float x=a.x(f.anchor),y=a.y(f.anchor)-8;int frame=meteor.frame(f.age),col=frame%meteor.columns,row=frame/meteor.columns;p.setAlpha(255);p.setColorFilter(null);p.setFilterBitmap(false);c.drawBitmap(meteor.atlas,new Rect(col*meteor.width,row*meteor.height,(col+1)*meteor.width,(row+1)*meteor.height),new RectF(x-meteor.pivotX,y-meteor.pivotY,x+meteor.width-meteor.pivotX,y+meteor.height-meteor.pivotY),p);continue;}
    if(f.sheet.equals("curse-skull")){float x=a.centerX(f.anchor),y=a.centerY(f.anchor);curseSkull.draw(c,f.id,f.age,Float.isFinite(x)?x:f.x,Float.isFinite(y)?y:f.y-25);continue;}
    if(f.sheet.equals("martial-line")){classic.drawBaekboContact(c,f.age,a.centerX(f.anchor),a.centerY(f.anchor));continue;}
    if(f.sheet.equals("classic")||f.sheet.equals("rogue")||f.sheet.equals("warrior")||f.sheet.equals("shared")){ClassicSkillReference reference=f.sheet.equals("rogue")?rogue:f.sheet.equals("warrior")?warrior:f.sheet.equals("shared")?shared:classic;float x=a.x(f.anchor),y=a.y(f.anchor);ClassicSkillReference.Channel channel=reference.channel(f.id,f.caster);float cy=a.centerY(f.anchor),cx=a.centerX(f.anchor);
      if(!f.caster&&recipientFinisher(f.id)){reference.drawCentered(c,channel,f.age,Float.isFinite(cx)?cx:f.x,Float.isFinite(cy)?cy:f.y-25);continue;}
      String registeredId=SkillPolicyV227.effectSource(f.id);
      if(f.sheet.equals("classic")&&registeredId.startsWith("SK_무도가_")&&!channel.sequence.visualCenter&&!Float.isFinite(channel.sequence.footOffsetY)){
        float[] foot=MartialCaptureRegistration.foot(registeredId,f.caster);float px=foot[0],py=foot[1];
        x=Float.isFinite(cx)?cx:f.x;y=Float.isFinite(y)?y:f.y;
        boolean recipient=MartialCaptureRegistration.recipient(registeredId,f.caster);
        if(recipient&&!registeredId.equals("SK_무도가_018")){py-=26f;y=Float.isFinite(cy)?cy:y-23f;}
        if(registeredId.equals("SK_무도가_010")){py=foot[1]-48f;float floor=a.y(f.anchor);y=Float.isFinite(cy)&&Float.isFinite(floor)?2*cy-floor:y-20f;}
        // The yellow horizontal HP cursor in these two captures is UI, above the observed head.
        int crop=registeredId.equals("SK_무도가_015")||registeredId.equals("SK_무도가_023")?63:0;
        reference.drawRegistered(c,channel,f.age,x,y,px,py,recipient?f.directionX:0,recipient?f.directionY:0,MartialCaptureRegistration.projectile(registeredId,f.caster),crop);continue;
      }
      if(f.id.equals("SK_마법사_006")){y=Float.isFinite(cy)&&Float.isFinite(y)?2*cy-y+46:f.y;reference.draw(c,channel,f.age,Float.isFinite(cx)?cx:f.x,y);continue;}
      if(Float.isFinite(channel.sequence.footOffsetY)){x=Float.isFinite(cx)?cx:f.x;y=(Float.isFinite(y)?y:f.y)+channel.sequence.footOffsetY;}else if(channel.sequence.visualCenter){x=Float.isFinite(cx)?cx:f.x;y=Float.isFinite(cy)?cy:f.y-25f;}else if(!channel.casterAnchor){float sourceLift=f.sheet.equals("classic")?16f:25f;y=(Float.isFinite(cy)?cy:f.y-25f)+sourceLift*channel.sequence.scale;x=Float.isFinite(cx)?cx:f.x;}
      reference.draw(c,channel,f.age,Float.isFinite(x)?x:f.x,Float.isFinite(y)?y:f.y);continue;}
    if(f.sheet.equals("impact")){float x=a.x(f.anchor),y=a.y(f.anchor);float cy=a.centerY(f.anchor);warrior.draw(c,warrior.damageImpact,f.age,Float.isFinite(x)?x:f.x,(Float.isFinite(cy)?cy:f.y-25f)+25f*warrior.damageImpact.sequence.scale);continue;}
    if(f.sheet.equals("capture")){float x=a.x(f.anchor),y=a.y(f.anchor);CapturedSkillFx.Sequence sequence=captured.get(f.id);if(recipientFinisher(f.id)){captured.drawCentered(c,p,f.id,f.age,a.centerX(f.anchor),a.centerY(f.anchor));continue;}if(sequence.contactMidpoint){float px=a.centerX("player"),py=a.centerY("player"),tx=a.centerX(f.anchor),ty=a.centerY(f.anchor);x=Float.isFinite(px)&&Float.isFinite(tx)?(px+tx)/2:f.x;y=Float.isFinite(py)&&Float.isFinite(ty)?(py+ty)/2:f.y;}else if(!sequence.directional){float cy=a.centerY(f.anchor);y=(Float.isFinite(cy)?cy:f.y-25f)+sequence.directionPivotLift*sequence.scale;}captured.drawDirected(c,p,f.id,f.age,Float.isFinite(x)?x:f.x,Float.isFinite(y)?y:f.y,f.directionX,f.directionY);continue;}
    if(!f.caster&&f.row<0){SkillPresentationCatalog.Entry v=catalog.get(f.id);if(v!=null){float x=a.x(f.anchor),y=a.y(f.anchor);SkillEffectShapes.draw(c,p,v.target,Float.isFinite(x)?x:f.x,(Float.isFinite(a.centerY(f.anchor))?a.centerY(f.anchor):f.y-25f)+24f,f.age/f.duration);}continue;}
    Bitmap b=sheets.get(f.sheet);if(b==null)continue;int rows=f.sheet.equals("caster")?4:8,cw=b.getWidth()/6,ch=b.getHeight()/rows;int col=Math.min(5,(int)(f.age/f.duration*6));
    float x=a.x(f.anchor),y=a.y(f.anchor);if(!Float.isFinite(x)||!Float.isFinite(y)){x=f.x;y=f.y;}
    float size=f.caster?58:f.sheet.equals("finisher-v65")?82:68,height=size*ch/(float)cw;float centerY=f.caster?y-23:(Float.isFinite(a.centerY(f.anchor))?a.centerY(f.anchor):y-25);p.setAlpha(Math.round(255*Math.min(1,(f.duration-f.age)/.10f)));
    if(f.sheet.equals("finisher-v65")&&f.row==1){ColorMatrix white=new ColorMatrix();white.setSaturation(0);p.setColorFilter(new ColorMatrixColorFilter(white));}
    boolean martialCast=f.caster&&f.id.startsWith("SK_무도가_")&&Math.abs(f.directionX)+Math.abs(f.directionY)>.001f;
    // The retained MARTIAL crescent opens SW; rotate from that observed source facing.
    if(martialCast){c.save();c.rotate((float)Math.toDegrees(Math.atan2(f.directionY,f.directionX)-Math.atan2(16,-32)),x,centerY);}
    c.drawBitmap(b,new Rect(col*b.getWidth()/6,f.row*b.getHeight()/rows,(col+1)*b.getWidth()/6,(f.row+1)*b.getHeight()/rows),new RectF(x-size/2,centerY-height/2,x+size/2,centerY+height/2),p);
    if(martialCast)c.restore();p.setColorFilter(null);
  }p.setAlpha(255);}
}


