package com.projectdark.mobile;

import android.content.Context;
import android.graphics.*;
import java.util.*;

/** Event-only visual projection: accepted start => caster, resolved non-miss => recipient. */
final class SkillVfxRenderer {
  interface Anchors {float x(String id);float y(String id);default float centerX(String id){return x(id);}default float centerY(String id){return y(id)-25f;}}
  static final class Pulse {
    final long actionSequence;final String id,sheet,anchor;final int row;final boolean caster;final float duration,x,y;float age;float directionX=1,directionY;
    Pulse(long seq,String id,String sheet,int row,String anchor,boolean caster,float duration,float x,float y){actionSequence=seq;this.id=id;this.sheet=sheet;this.row=row;this.anchor=anchor;this.caster=caster;this.duration=duration;this.x=x;this.y=y;}
  }
  private final CapturedSkillFx captured;private final ClassicSkillReference classic,rogue,warrior,shared;private final SkillPresentationCatalog catalog;private final Map<String,Bitmap> sheets=new HashMap<>();
  boolean testAccess;
  final List<Pulse> pulses=new ArrayList<>();private final Set<String> seen=new HashSet<>();private final Deque<String> history=new ArrayDeque<>();private final Paint p=new Paint();
  SkillVfxRenderer(Context c,SkillPresentationCatalog catalog){this.catalog=catalog;captured=new CapturedSkillFx(c);classic=new ClassicSkillReference(c);rogue=new ClassicSkillReference(c,"rogue");warrior=new ClassicSkillReference(c,"warrior");shared=new ClassicSkillReference(c,"shared");p.setFilterBitmap(false);for(String name:new String[]{"caster","target","status","finisher-v65"})try{BitmapFactory.Options options=new BitmapFactory.Options();options.inSampleSize=name.equals("finisher-v65")?1:2;sheets.put(name,BitmapFactory.decodeStream(c.getAssets().open("skill-presentation/"+name+".png"),null,options));}catch(Exception ignored){}}
  void tick(float dt){float safe=Math.max(0,dt);for(Pulse f:pulses)f.age+=safe;pulses.removeIf(f->f.age>=f.duration);}
  void clear(){pulses.clear();}
  /** Explicit test-only visual emission, independent from real Resolver hit feedback. */
  void preview(long seq,String id,boolean caster,String anchor,Anchors a){SkillPresentationCatalog.Entry v=catalog.get(id);if(v==null||anchor==null)return;int row=caster?casterRow(v.caster):v.targetRow;if(row<0)return;float x=a.x(anchor),y=a.y(anchor);if(!Float.isFinite(x)||!Float.isFinite(y))return;pulses.add(new Pulse(seq,id,caster?"caster":v.targetSheet,row,anchor,caster,caster?.30f:.48f,x,y));if(pulses.size()>64)pulses.remove(0);}
  void consume(List<CombatResolver.Event> events,Anchors a){for(CombatResolver.Event e:events){
    if(!RuntimeCombatSession.PLAYER_ID.equals(e.actorId)){
      if(e.type==CombatResolver.EventType.HIT_FEEDBACK&&e.amount>0&&(e.hitSemantic==CombatResolver.HitSemantic.DAMAGE||e.hitSemantic==CombatResolver.HitSemantic.CRIT)){
        String key=e.actionSequence+":monster-contact:"+e.targetId;float x=a.x(e.targetId),y=a.y(e.targetId);
        if(Float.isFinite(x)&&Float.isFinite(y)&&seen.add(key)){history.add(key);while(history.size()>256)seen.remove(history.removeFirst());pulses.add(new Pulse(e.actionSequence,e.actionId,"impact",0,e.targetId,false,warrior.damageImpact.sequence.duration,x,y));if(pulses.size()>64)pulses.remove(0);}
      }
      continue;
    }
    String id=e.actionId.startsWith("attack_proto_")?"SK_공통_001":e.actionId;SkillPresentationCatalog.Entry v=catalog.get(id);if(v==null||id.equals("SK_공통_001"))continue;
    if(e.type==CombatResolver.EventType.ACTION_CANCELLED){pulses.removeIf(f->f.actionSequence==e.actionSequence);continue;}
    // Test access bypasses Dara's MP prerequisite but retains its real damage formula.
    // A low-resource test release therefore reports zero/MISS. Show the source release
    // on its resolved recipient without inventing damage, an impact, or an early cast.
    boolean daraTestRelease=testAccess&&id.equals("SK_무도가_020")&&e.amount==0;
    boolean caster=e.type==CombatResolver.EventType.ACTION_STARTED,target=e.type==CombatResolver.EventType.HIT_FEEDBACK&&(e.hitSemantic!=CombatResolver.HitSemantic.MISS||daraTestRelease);
    if(!caster&&!target)continue;
    // A real resolved damaging contact has its own recipient-only visual channel.
    // Preview (amount zero), misses and healing never pretend the monster took damage.
    if(target&&e.amount>0&&(e.hitSemantic==CombatResolver.HitSemantic.DAMAGE||e.hitSemantic==CombatResolver.HitSemantic.CRIT)&&!e.actorId.equals(e.targetId)){
      String impactKey=e.actionSequence+":recipient-damage:"+e.targetId;float ix=a.x(e.targetId),iy=a.y(e.targetId);
      if(Float.isFinite(ix)&&Float.isFinite(iy)&&seen.add(impactKey)){history.add(impactKey);while(history.size()>256)seen.remove(history.removeFirst());pulses.add(new Pulse(e.actionSequence,id,"impact",0,e.targetId,false,warrior.damageImpact.sequence.duration,ix,iy));if(pulses.size()>64)pulses.remove(0);}
    }
    if(rogue.has(id)||classic.has(id)||warrior.has(id)||shared.has(id)){
      ClassicSkillReference reference=rogue.has(id)?rogue:classic.has(id)?classic:warrior.has(id)?warrior:shared;String sourceSheet=rogue.has(id)?"rogue":classic.has(id)?"classic":warrior.has(id)?"warrior":"shared";
      ClassicSkillReference.Channel channel=reference.channel(id,caster);if(channel==null)continue;
      // Capture anchor records the example's pivot, not who receives a live attack.
      SkillActionContract.Rule liveRule=SkillActionContract.get(id);
      boolean martialRecipient=sourceSheet.equals("classic")&&id.startsWith("SK_무도가_")&&!caster&&liveRule!=null&&MartialCaptureRegistration.recipient(id,caster);
      String anchor=martialRecipient?e.targetId:channel.casterAnchor?e.actorId:e.targetId,key=e.actionSequence+":classic:"+channel.key+":"+anchor;
      if(!seen.add(key))continue;history.add(key);while(history.size()>256)seen.remove(history.removeFirst());
      float x=a.x(anchor),y=a.y(anchor);if(!Float.isFinite(x)||!Float.isFinite(y))continue;
      Pulse sourcePulse=new Pulse(e.actionSequence,id,sourceSheet,0,anchor,caster,channel.sequence.duration,x,y);sourcePulse.directionX=a.x(e.targetId)-a.x(e.actorId);sourcePulse.directionY=a.y(e.targetId)-a.y(e.actorId);pulses.add(sourcePulse);if(pulses.size()>64)pulses.remove(0);continue;
    }
boolean captureContact=target&&captured.get(id)!=null;String key=e.actionSequence+":"+(caster?"caster":captureContact?"capture":"target:"+e.targetId);if(!seen.add(key))continue;history.add(key);while(history.size()>256)seen.remove(history.removeFirst());
    int row=caster?casterRow(v.caster):v.targetRow;if(caster&&row<0||!caster&&v.target.equals("NONE"))continue;String anchor=caster||(captureContact&&captured.get(id).directional)?e.actorId:e.targetId;
    CapturedSkillFx.Sequence sequence=caster?null:captured.get(id);String sheet=sequence!=null?"capture":caster?"caster":v.targetSheet;float x=a.x(anchor),y=a.y(anchor);if(!Float.isFinite(x)||!Float.isFinite(y))continue;
    Pulse pulse=new Pulse(e.actionSequence,id,sheet,row,anchor,caster,sequence!=null?sequence.duration:caster?.30f:row<0||sheet.equals("finisher-v65")?.75f:.48f,x,y);pulse.directionX=a.x(e.targetId)-a.x(e.actorId);pulse.directionY=a.y(e.targetId)-a.y(e.actorId);pulses.add(pulse);if(pulses.size()>64)pulses.remove(0);
  }}
  static int casterRow(String name){switch(name){case "SLASH":return 0;case "MARTIAL":return 1;case "ARCANE":return 2;case "HEAL":return 3;default:return -1;}}
  void draw(Canvas c,Anchors a){for(Pulse f:pulses){
    if(f.sheet.equals("classic")||f.sheet.equals("rogue")||f.sheet.equals("warrior")||f.sheet.equals("shared")){ClassicSkillReference reference=f.sheet.equals("rogue")?rogue:f.sheet.equals("warrior")?warrior:f.sheet.equals("shared")?shared:classic;float x=a.x(f.anchor),y=a.y(f.anchor);ClassicSkillReference.Channel channel=reference.channel(f.id,f.caster);float cy=a.centerY(f.anchor),cx=a.centerX(f.anchor);
      if(f.sheet.equals("classic")&&f.id.startsWith("SK_무도가_")&&!channel.sequence.visualCenter){
        float[] foot=MartialCaptureRegistration.foot(f.id,f.caster);float px=foot[0],py=foot[1];
        x=Float.isFinite(cx)?cx:f.x;y=Float.isFinite(y)?y:f.y;
        boolean recipient=MartialCaptureRegistration.recipient(f.id,f.caster);
        if(recipient&&!f.id.equals("SK_무도가_018")){py-=26f;y=Float.isFinite(cy)?cy:y-23f;}
        if(f.id.equals("SK_무도가_010")){py=foot[1]-48f;float floor=a.y(f.anchor);y=Float.isFinite(cy)&&Float.isFinite(floor)?2*cy-floor:y-20f;}
        // The yellow horizontal HP cursor in these two captures is UI, above the observed head.
        int crop=f.id.equals("SK_무도가_015")||f.id.equals("SK_무도가_023")?63:0;
        reference.drawRegistered(c,channel,f.age,x,y,px,py,recipient?f.directionX:0,recipient?f.directionY:0,MartialCaptureRegistration.projectile(f.id,f.caster),crop);continue;
      }
      if(Float.isFinite(channel.sequence.footOffsetY)){x=Float.isFinite(cx)?cx:f.x;y=(Float.isFinite(y)?y:f.y)+channel.sequence.footOffsetY;}else if(channel.sequence.visualCenter){x=Float.isFinite(cx)?cx:f.x;y=Float.isFinite(cy)?cy:f.y-25f;}else if(!channel.casterAnchor){float sourceLift=f.sheet.equals("classic")?16f:25f;y=(Float.isFinite(cy)?cy:f.y-25f)+sourceLift*channel.sequence.scale;x=Float.isFinite(cx)?cx:f.x;}
      reference.draw(c,channel,f.age,Float.isFinite(x)?x:f.x,Float.isFinite(y)?y:f.y);continue;}
    if(f.sheet.equals("impact")){float x=a.x(f.anchor),y=a.y(f.anchor);float cy=a.centerY(f.anchor);warrior.draw(c,warrior.damageImpact,f.age,Float.isFinite(x)?x:f.x,(Float.isFinite(cy)?cy:f.y-25f)+25f*warrior.damageImpact.sequence.scale);continue;}
    if(f.sheet.equals("capture")){float x=a.x(f.anchor),y=a.y(f.anchor);CapturedSkillFx.Sequence sequence=captured.get(f.id);if(sequence.contactMidpoint){float px=a.centerX("player"),py=a.centerY("player"),tx=a.centerX(f.anchor),ty=a.centerY(f.anchor);x=Float.isFinite(px)&&Float.isFinite(tx)?(px+tx)/2:f.x;y=Float.isFinite(py)&&Float.isFinite(ty)?(py+ty)/2:f.y;}else if(!sequence.directional){float cy=a.centerY(f.anchor);y=(Float.isFinite(cy)?cy:f.y-25f)+sequence.directionPivotLift*sequence.scale;}captured.drawDirected(c,p,f.id,f.age,Float.isFinite(x)?x:f.x,Float.isFinite(y)?y:f.y,f.directionX,f.directionY);continue;}
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
