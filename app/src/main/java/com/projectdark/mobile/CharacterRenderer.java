package com.projectdark.mobile;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * [ADAPTED] Four-direction player renderer rebuilt against the user-approved 2026-09-10
 * character sprite concept. The concept is presentation authority for silhouette/proportion;
 * it is not claimed as extracted original Nexon sprite pixels. Verified originals remain PENDING_CROP.
 */
public final class CharacterRenderer {
  public static final String EVIDENCE="USER_APPROVED_CONCEPT+ADAPTED";
  public static final String ASSET_STATUS="PENDING_CROP";
  public static final String PRESENTATION_PROFILE="USER_CONCEPT_20260910_CHIBI_DIAGONAL";

  /** User-approved fixed mobile presentation scale from latest main/user concept. */
  public static final float PLAYER_RENDER_SCALE=1.50f;
  public static final float SHADOW_RENDER_SCALE=0.72f;
  public static final float LOGICAL_FOOT_ANCHOR_Y=0f;
  /** Concept target: compact chibi body, large readable head, narrow torso and short legs. */
  public static final float BASE_HEIGHT=32f;
  public static final float HEAD_TO_BODY_RATIO=0.34f;

  public enum Direction { NW, NE, SW, SE }
  public enum State { IDLE, WALK, CAST, ATTACK, SKILL, HIT, DEAD }
  public enum Layer { BODY, HAIR, EQUIPMENT, WEAPON, EFFECT }
  public enum EffectFamily { NONE, CAST, MAGIC, THROW, PUNCH, KICK, SKILL, HIT }

  public static final List<Layer> DRAW_ORDER=Collections.unmodifiableList(Arrays.asList(
      Layer.BODY,Layer.HAIR,Layer.EQUIPMENT,Layer.WEAPON,Layer.EFFECT));

  public static final class DirectionalVisualSet {
    public final String nw,ne,sw,se;
    public DirectionalVisualSet(String nw,String ne,String sw,String se){this.nw=nw;this.ne=ne;this.sw=sw;this.se=se;}
    public String forDirection(Direction d){if(d==null)return null;switch(d){case NW:return nw;case NE:return ne;case SW:return sw;case SE:return se;default:return null;}}
    public boolean unresolved(){return nw==null&&ne==null&&sw==null&&se==null;}
  }

  public static final class Pose {
    public final float x,y,walkClock,stateClock,stateDuration;
    public final Direction direction; public final State state; public final boolean hitFlash;
    public final String equipmentVisualRef,weaponVisualRef,effectVisualRef; public final EffectFamily effectFamily;
    public Pose(float x,float y,Direction direction,State state,float walkClock,float stateClock,
        float stateDuration,boolean hitFlash,String equipmentVisualRef,String weaponVisualRef,
        String effectVisualRef,EffectFamily effectFamily){
      this.x=x;this.y=y;this.direction=direction;this.state=state;this.walkClock=walkClock;
      this.stateClock=stateClock;this.stateDuration=stateDuration;this.hitFlash=hitFlash;
      this.equipmentVisualRef=equipmentVisualRef;this.weaponVisualRef=weaponVisualRef;
      this.effectVisualRef=effectVisualRef;this.effectFamily=effectFamily==null?EffectFamily.NONE:effectFamily;
    }
  }

  private static final boolean CONTRACT_VALID=CharacterRendererAudit.passes();
  private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);

  public CharacterRenderer(){if(!CONTRACT_VALID)throw new IllegalStateException("CharacterRenderer contract audit failed: "+CharacterRendererAudit.summary());}

  public void draw(Canvas c,Pose pose){
    if(pose==null||pose.direction==null||pose.state==null)throw new IllegalArgumentException("Character pose requires direction and state");
    int frame=pose.state==State.WALK?((int)(pose.walkClock*7f)&3):0;
    boolean left=isLeft(pose.direction),down=isDown(pose.direction);
    float sx=left?-1f:1f,sy=down?1f:-1f,q=phase(pose);
    float walkContact=(frame==1||frame==3)?1f:0f;
    float bob=pose.state==State.WALK?-walkContact*.75f:pose.state==State.IDLE?(float)Math.sin(pose.stateClock*3.2f)*.22f:0f;
    float weight=pose.state==State.WALK?(frame==1?-0.45f:frame==3?.45f:0f):0f;
    float recoil=pose.state==State.HIT?-3.2f*sx:0f;
    float actionLean=(pose.state==State.ATTACK||pose.state==State.SKILL)?(float)Math.sin(Math.PI*q)*1.2f*sx:0f;
    float anchorY=pose.y+LOGICAL_FOOT_ANCHOR_Y;

    float sw=12.5f*SHADOW_RENDER_SCALE*(pose.state==State.DEAD?1.42f:1f+walkContact*.035f);
    float sh=3.4f*SHADOW_RENDER_SCALE*(pose.state==State.DEAD?.62f:1f-walkContact*.05f);
    p.setStyle(Paint.Style.FILL);p.setColor(pose.hitFlash?0x88ff705f:0x52000000);
    c.drawOval(new RectF(pose.x-sw,anchorY-sh,pose.x+sw,anchorY+sh),p);

    c.save();
    c.translate(pose.x+recoil+weight*sx+actionLean,anchorY-BASE_HEIGHT*PLAYER_RENDER_SCALE+bob*PLAYER_RENDER_SCALE);
    c.scale(PLAYER_RENDER_SCALE,PLAYER_RENDER_SCALE);c.translate(-9f,0f);
    if(pose.state==State.DEAD){c.rotate(left?-76f:76f,9f,29f);c.scale(1.04f,.82f,9f,29f);}
    for(Layer layer:DRAW_ORDER)drawLayer(c,pose,layer,frame);
    c.restore();
  }

  private void drawLayer(Canvas c,Pose pose,Layer layer,int frame){switch(layer){
    case BODY:drawBody(c,pose,frame);break; case HAIR:drawHair(c,pose);break;
    case EQUIPMENT:drawEquipment(c,pose);break; case WEAPON:drawWeapon(c,pose);break;
    case EFFECT:drawEffect(c,pose);break;}}

  /** Compact chibi diagonal body: ~11px head, ~11px torso, short separated legs. */
  private void drawBody(Canvas c,Pose pose,int frame){
    boolean left=isLeft(pose.direction),down=isDown(pose.direction);float side=left?-1f:1f;
    int step=pose.state==State.WALK?(frame==1?2:frame==3?-2:0):0;
    float torsoX=side*1.15f,headX=side*1.65f;
    int outline=0xff171311,skin=pose.hitFlash?0xffffded0:0xffe7aa78;
    int pants=0xff26313b,boot=0xff5a3927,undershirt=0xffd8c7aa;

    // far leg first, then near leg: this overlap is what makes each diagonal read as a side view.
    float far=left?11f:5f,near=left?5f:11f;
    rect(c,outline,far-step*.45f,22,far+3f-step*.45f,29);rect(c,pants,far+.6f-step*.45f,22.5f,far+2.4f-step*.45f,27.5f);rect(c,boot,far-.3f-step*.45f,27,far+3.5f-step*.45f,30);
    rect(c,outline,near+step*.45f,21.5f,near+3.2f+step*.45f,29.5f);rect(c,pants,near+.6f+step*.45f,22,near+2.5f+step*.45f,27.5f);rect(c,boot,near-.4f+step*.45f,27,near+3.8f+step*.45f,30.3f);

    // narrow armored torso, unlike the old rectangular mannequin.
    Path torso=new Path();torso.moveTo(5f+torsoX,11f);torso.lineTo(12.8f+torsoX,11f);torso.lineTo(13.7f+torsoX,20.8f);torso.lineTo(10.7f+torsoX,23f);torso.lineTo(6f+torsoX,22.3f);torso.lineTo(4.3f+torsoX,15f);torso.close();
    p.setColor(outline);c.drawPath(torso,p);p.setColor(undershirt);c.drawRect(5.6f+torsoX,12f,12.2f+torsoX,19.8f,p);

    // arms sit around the torso, not as long dangling sticks.
    float swing=pose.state==State.WALK?step*.55f:0f;
    float farArm=left?13f:4f,nearArm=left?3.8f:13.2f;
    limb(c,farArm,13f-swing,farArm+side*1.4f,20f-swing,outline,skin);
    float action=pose.state==State.ATTACK||pose.state==State.SKILL?3.5f*side*(float)Math.sin(Math.PI*phase(pose)):0f;
    float cast=pose.state==State.CAST?-5.5f:0f;
    limb(c,nearArm,13f+swing,nearArm+side*(1.5f+Math.abs(action)),20f+cast+swing,outline,skin);

    // large face ellipse, shifted toward facing direction.
    p.setColor(outline);c.drawOval(new RectF(3.4f+headX,.2f,14.6f+headX,11.8f),p);
    p.setColor(skin);c.drawOval(new RectF(4.3f+headX,1.1f,13.8f+headX,10.8f),p);
    if(down){
      float eyeNear=left?6.1f:11.6f,eyeFar=left?9.2f:8.5f;
      rect(c,0xff2a211e,eyeNear+headX,6.2f,eyeNear+.9f+headX,7.2f);
      rect(c,0xff4b352c,eyeFar+headX,6.4f,eyeFar+.65f+headX,7.05f);
    }
  }

  private void drawHair(Canvas c,Pose pose){
    boolean left=isLeft(pose.direction),down=isDown(pose.direction);float side=left?-1f:1f,hs=side*1.65f;
    int dark=0xff3a2419,mid=0xff69412b,light=0xff95613d;
    // layered brown fringe/back volume matching the approved concept vocabulary.
    p.setColor(dark);c.drawOval(new RectF(2.7f+hs,-.8f,15.2f+hs,6.2f),p);
    rect(c,mid,3.4f+hs,.2f,14.4f+hs,3.5f);rect(c,light,5.1f+hs,.1f,10.4f+hs,1.4f);
    float backX=left?11.5f:2.4f;rect(c,dark,backX+hs,2.2f,backX+3.4f+hs,9.5f);
    if(down){float fringeX=left?4.2f:10.2f;rect(c,mid,fringeX+hs,2.5f,fringeX+2.4f+hs,6.3f);}
  }

  private void drawEquipment(Canvas c,Pose pose){
    boolean left=isLeft(pose.direction),down=isDown(pose.direction);float side=left?-1f:1f,tx=side*1.15f;
    int navy=pose.hitFlash?0xffd99086:0xff263d57,blue=0xff365d80,gold=0xffb9863d,steel=0xff9da9ae;
    // blue warrior tunic/pauldron profile from the approved concept.
    rect(c,navy,5.1f+tx,12f,12.7f+tx,20.6f);rect(c,blue,6f+tx,12.8f,11.9f+tx,19.8f);
    rect(c,gold,5.4f+tx,19.1f,12.4f+tx,20.4f);rect(c,steel,(left?3.7f:11.4f)+tx,11.5f,(left?6.2f:13.9f)+tx,14.4f);
    // shield remains on the off-hand and swaps depth with facing.
    float shieldX=left?11.2f:3.0f;if(!down)shieldX+=side*.8f;
    p.setColor(0xff202a33);c.drawOval(new RectF(shieldX,14f,shieldX+5.2f,22.3f),p);
    p.setColor(0xff35516d);c.drawOval(new RectF(shieldX+.6f,14.8f,shieldX+4.6f,21.4f),p);
    p.setColor(gold);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(.9f);c.drawOval(new RectF(shieldX+.8f,15f,shieldX+4.4f,21.1f),p);p.setStyle(Paint.Style.FILL);
  }

  private void drawWeapon(Canvas c,Pose pose){
    if(pose.effectFamily==EffectFamily.PUNCH||pose.effectFamily==EffectFamily.KICK)return;
    boolean left=isLeft(pose.direction),down=isDown(pose.direction);float sx=left?-1f:1f,sy=down?1f:-1f,q=phase(pose);
    float handX=left?4.2f:13.8f,handY=down?16.8f:14.5f;
    float attack=(pose.state==State.ATTACK||pose.state==State.SKILL)?(float)Math.sin(Math.PI*q):0f;
    float reach=pose.state==State.IDLE||pose.state==State.WALK?8.2f:8.2f+5.5f*attack;
    float ex=handX+sx*reach,ey=handY+sy*reach*.72f;
    p.setStrokeWidth(2.1f);p.setColor(0xffdce2e1);c.drawLine(handX,handY,ex,ey,p);
    p.setStrokeWidth(1f);p.setColor(0xffffffff);c.drawLine(handX+sx*1.2f,handY+sy*.8f,ex,ey,p);
    p.setStrokeWidth(2.2f);p.setColor(0xff9b6a31);c.drawLine(handX-sx*2f,handY-sy*1.4f,handX+sx*1.3f,handY+sy*.9f,p);
  }

  private void drawEffect(Canvas c,Pose pose){
    EffectFamily family=pose.hitFlash?EffectFamily.HIT:pose.effectFamily;if(family==EffectFamily.NONE)return;
    float q=phase(pose);boolean left=isLeft(pose.direction),down=isDown(pose.direction);float sx=left?-1f:1f,sy=down?1f:-1f;
    p.setStyle(Paint.Style.STROKE);
    switch(family){
      case CAST:case MAGIC:
        p.setStrokeWidth(1.8f);p.setColor(0xcc78c8ff);c.drawCircle(9+sx*4f,5+sy*5f,4+8*q,p);c.drawCircle(9+sx*4f,5+sy*5f,10-3*q,p);break;
      case SKILL:
        p.setStrokeWidth(3.1f);p.setColor(0xd58bd8ff);float ax=9+sx*7f,ay=15+sy*4f;c.drawArc(new RectF(ax-14,ay-12,ax+14,ay+12),left?30:195,145,false,p);p.setStrokeWidth(1.2f);p.setColor(0xb8e5f8ff);c.drawArc(new RectF(ax-17,ay-14,ax+17,ay+14),left?25:190,150,false,p);break;
      case HIT:
        p.setStrokeWidth(1.8f);p.setColor(0xd9ff7b61);c.drawCircle(9-sx*3f,10-sy*2f,4+6*q,p);break;
      case THROW:
        p.setStyle(Paint.Style.FILL);p.setColor(0xffffd36a);c.drawCircle(9+sx*26*q,14+sy*17*q,2f,p);break;
      default:break;
    }
    p.setStyle(Paint.Style.FILL);
  }

  private void limb(Canvas c,float x1,float y1,float x2,float y2,int outline,int skin){p.setStrokeCap(Paint.Cap.ROUND);p.setStrokeWidth(3.4f);p.setColor(outline);c.drawLine(x1,y1,x2,y2,p);p.setStrokeWidth(1.7f);p.setColor(skin);c.drawLine(x1,y1+.5f,x2,y2,p);p.setStrokeCap(Paint.Cap.BUTT);}
  private boolean isLeft(Direction d){return d==Direction.NW||d==Direction.SW;}
  private boolean isDown(Direction d){return d==Direction.SW||d==Direction.SE;}
  private float phase(Pose pose){return pose.stateDuration<=0?0:Math.max(0,Math.min(1,pose.stateClock/pose.stateDuration));}
  private void rect(Canvas c,int color,float l,float t,float r,float b){p.setColor(color);c.drawRect(l,t,r,b,p);}
  public boolean hasRequiredStateContract(){return CharacterRendererAudit.passes();}
  public String contractAuditSummary(){return CharacterRendererAudit.summary();}
  public boolean ownsPlayerLocalEffects(){return true;}
  public float playerRenderScale(){return PLAYER_RENDER_SCALE;}
}
