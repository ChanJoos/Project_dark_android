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
  public static final String PRESENTATION_PROFILE="USER_CONCEPT_20260910_CHIBI_DIAGONAL_V2";

  /** User-approved fixed mobile presentation scale from latest main/user concept. */
  public static final float PLAYER_RENDER_SCALE=1.50f;
  public static final float SHADOW_RENDER_SCALE=0.72f;
  public static final float LOGICAL_FOOT_ANCHOR_Y=0f;
  /** Concept target: compact 2.5-3 head chibi body with a readable diagonal silhouette. */
  public static final float BASE_HEIGHT=32f;
  public static final float HEAD_TO_BODY_RATIO=0.34f;
  public static final float HEAD_WIDTH=11.2f;
  public static final float SHOULDER_WIDTH=8.6f;

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

  private static final class ArmPose {
    final float sx,sy,ex,ey,hx,hy;
    ArmPose(float sx,float sy,float ex,float ey,float hx,float hy){this.sx=sx;this.sy=sy;this.ex=ex;this.ey=ey;this.hx=hx;this.hy=hy;}
  }

  private static final boolean CONTRACT_VALID=CharacterRendererAudit.passes();
  private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);

  public CharacterRenderer(){if(!CONTRACT_VALID)throw new IllegalStateException("CharacterRenderer contract audit failed: "+CharacterRendererAudit.summary());}

  public void draw(Canvas c,Pose pose){
    if(pose==null||pose.direction==null||pose.state==null)throw new IllegalArgumentException("Character pose requires direction and state");
    int frame=pose.state==State.WALK?((int)(pose.walkClock*7f)&3):0;
    boolean left=isLeft(pose.direction),down=isDown(pose.direction);
    float sx=left?-1f:1f,q=phase(pose);
    float walkContact=(frame==1||frame==3)?1f:0f;
    float bob=pose.state==State.WALK?-walkContact*.72f:pose.state==State.IDLE?(float)Math.sin(pose.stateClock*3.2f)*.18f:0f;
    float weight=pose.state==State.WALK?(frame==1?-.38f:frame==3?.38f:0f):0f;
    float recoil=pose.state==State.HIT?-3.2f*sx:0f;
    float actionLean=(pose.state==State.ATTACK||pose.state==State.SKILL)?(float)Math.sin(Math.PI*q)*1.0f*sx:0f;
    float anchorY=pose.y+LOGICAL_FOOT_ANCHOR_Y;

    float sw=12.5f*SHADOW_RENDER_SCALE*(pose.state==State.DEAD?1.42f:1f+walkContact*.032f);
    float sh=3.4f*SHADOW_RENDER_SCALE*(pose.state==State.DEAD?.62f:1f-walkContact*.045f);
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
    case EQUIPMENT:drawEquipment(c,pose,frame);break; case WEAPON:drawWeapon(c,pose,frame);break;
    case EFFECT:drawEffect(c,pose);break;}}

  /** Cohesive compact body: head/neck/torso/pelvis and limbs share one continuous diagonal rig. */
  private void drawBody(Canvas c,Pose pose,int frame){
    boolean left=isLeft(pose.direction),down=isDown(pose.direction);float side=left?-1f:1f;
    int step=pose.state==State.WALK?(frame==1?2:frame==3?-2:0):0;
    float torsoX=side*1.05f,headX=side*1.55f,depthY=down?.25f:-.2f;
    int outline=0xff171311,skin=pose.hitFlash?0xffffded0:0xffe7aa78;
    int pants=0xff26313b,boot=0xff5a3927,undershirt=0xffd8c7aa;

    // Neck bridge eliminates the detached-head read at runtime scale.
    rect(c,outline,7.0f+torsoX,9.2f+depthY,11.3f+torsoX,13.0f+depthY);
    rect(c,skin,7.7f+torsoX,9.4f+depthY,10.7f+torsoX,12.7f+depthY);

    // Far leg first; both legs originate from a shared pelvis block rather than floating below torso.
    rect(c,outline,5.2f+torsoX,20.2f,12.8f+torsoX,24.0f);
    rect(c,pants,5.9f+torsoX,20.4f,12.1f+torsoX,23.6f);
    float far=left?10.4f:5.2f,near=left?5.2f:10.4f;
    rect(c,outline,far-step*.42f,22.2f,far+3.1f-step*.42f,29.1f);rect(c,pants,far+.55f-step*.42f,22.4f,far+2.5f-step*.42f,27.5f);rect(c,boot,far-.35f-step*.42f,27.0f,far+3.6f-step*.42f,30.2f);
    rect(c,outline,near+step*.42f,21.7f,near+3.3f+step*.42f,29.5f);rect(c,pants,near+.55f+step*.42f,22.0f,near+2.65f+step*.42f,27.6f);rect(c,boot,near-.45f+step*.42f,27.0f,near+3.9f+step*.42f,30.4f);

    Path torso=new Path();
    torso.moveTo(5.0f+torsoX,11.2f);torso.lineTo(12.9f+torsoX,11.2f);
    torso.lineTo(13.7f+torsoX,19.7f);torso.lineTo(11.7f+torsoX,22.5f);
    torso.lineTo(6.1f+torsoX,22.5f);torso.lineTo(4.2f+torsoX,19.2f);
    torso.lineTo(4.4f+torsoX,14.0f);torso.close();
    p.setColor(outline);c.drawPath(torso,p);
    p.setColor(undershirt);c.drawRoundRect(new RectF(5.2f+torsoX,12.0f,12.4f+torsoX,20.7f),1.4f,1.4f,p);

    // Arms are true shoulder -> elbow -> hand chains. Weapon renderer reuses the same near-hand endpoint.
    ArmPose farArm=farArmPose(pose,frame);
    ArmPose nearArm=nearArmPose(pose,frame);
    drawJointedArm(c,farArm,outline,skin,false);
    drawJointedArm(c,nearArm,outline,skin,true);

    // Head is deliberately wider than torso and overlaps the neck/shoulder line like the reference board.
    p.setColor(outline);c.drawOval(new RectF(3.4f+headX,.2f,14.6f+headX,11.8f),p);
    p.setColor(skin);c.drawOval(new RectF(4.25f+headX,1.05f,13.75f+headX,10.85f),p);
    if(down){
      float eyeNear=left?6.0f:11.45f,eyeFar=left?9.1f:8.55f;
      rect(c,0xff241d1b,eyeNear+headX,6.0f,eyeNear+1.0f+headX,7.2f);
      rect(c,0xff53372d,eyeFar+headX,6.25f,eyeFar+.65f+headX,7.0f);
      rect(c,0xffb96f58,(left?7.1f:9.9f)+headX,8.0f,(left?7.8f:10.6f)+headX,8.45f);
    }
  }

  private ArmPose nearArmPose(Pose pose,int frame){
    boolean left=isLeft(pose.direction),down=isDown(pose.direction);float side=left?-1f:1f;
    int step=pose.state==State.WALK?(frame==1?2:frame==3?-2:0):0;
    float swing=pose.state==State.WALK?step*.45f:0f;
    float attack=(pose.state==State.ATTACK||pose.state==State.SKILL)?(float)Math.sin(Math.PI*phase(pose)):0f;
    float cast=pose.state==State.CAST?1f:0f;
    float shoulderX=left?5.0f:13.0f,shoulderY=13.0f+(down?.25f:-.1f);
    float elbowX=shoulderX+side*(2.2f+2.4f*attack);
    float elbowY=shoulderY+3.5f+swing-4.1f*cast-1.0f*attack;
    float handX=elbowX+side*(1.9f+2.0f*attack);
    float handY=elbowY+2.7f-2.0f*cast-1.5f*attack;
    return new ArmPose(shoulderX,shoulderY,elbowX,elbowY,handX,handY);
  }

  private ArmPose farArmPose(Pose pose,int frame){
    boolean left=isLeft(pose.direction),down=isDown(pose.direction);float side=left?-1f:1f;
    int step=pose.state==State.WALK?(frame==1?2:frame==3?-2:0):0;
    float swing=pose.state==State.WALK?-step*.38f:0f;
    float shoulderX=left?12.4f:5.6f,shoulderY=13.4f+(down?.15f:-.25f);
    float elbowX=shoulderX-side*1.45f,elbowY=shoulderY+3.0f+swing;
    float handX=elbowX-side*.85f,handY=elbowY+2.55f;
    return new ArmPose(shoulderX,shoulderY,elbowX,elbowY,handX,handY);
  }

  private void drawJointedArm(Canvas c,ArmPose a,int outline,int skin,boolean near){
    p.setStrokeCap(Paint.Cap.ROUND);
    p.setColor(outline);p.setStrokeWidth(near?4.1f:3.6f);c.drawLine(a.sx,a.sy,a.ex,a.ey,p);c.drawLine(a.ex,a.ey,a.hx,a.hy,p);
    p.setColor(skin);p.setStrokeWidth(near?2.05f:1.75f);c.drawLine(a.sx,a.sy+.15f,a.ex,a.ey,p);c.drawLine(a.ex,a.ey,a.hx,a.hy,p);
    p.setColor(outline);c.drawCircle(a.ex,a.ey,near?1.8f:1.55f,p);p.setColor(skin);c.drawCircle(a.ex,a.ey,near?.9f:.72f,p);
    p.setColor(outline);c.drawCircle(a.hx,a.hy,near?1.8f:1.55f,p);p.setColor(skin);c.drawCircle(a.hx,a.hy,near?1.0f:.8f,p);
    p.setStrokeCap(Paint.Cap.BUTT);
  }

  private void drawHair(Canvas c,Pose pose){
    boolean left=isLeft(pose.direction),down=isDown(pose.direction);float side=left?-1f:1f,hs=side*1.55f;
    int dark=0xff342017,mid=0xff674028,light=0xff9b653e;
    // Rounded skull wrap plus stepped fringe replaces the previous rectangular-cap read.
    p.setColor(dark);c.drawOval(new RectF(2.65f+hs,-1.0f,15.35f+hs,6.4f),p);
    p.setColor(mid);c.drawOval(new RectF(3.45f+hs,-.25f,14.55f+hs,4.65f),p);
    rect(c,light,5.0f+hs,.0f,10.2f+hs,1.25f);
    float backX=left?11.25f:2.65f;rect(c,dark,backX+hs,2.3f,backX+3.6f+hs,9.55f);
    if(down){
      float fringe=left?4.05f:10.0f;rect(c,mid,fringe+hs,2.45f,fringe+2.3f+hs,6.25f);
      rect(c,dark,(left?3.4f:12.25f)+hs,4.1f,(left?5.0f:13.65f)+hs,7.0f);
    }else{
      rect(c,dark,(left?10.1f:3.0f)+hs,4.0f,(left?14.0f:6.9f)+hs,9.8f);
    }
  }

  private void drawEquipment(Canvas c,Pose pose,int frame){
    boolean left=isLeft(pose.direction),down=isDown(pose.direction);float side=left?-1f:1f,tx=side*1.05f;
    int navy=pose.hitFlash?0xffd99086:0xff243a53,blue=0xff365f86,gold=0xffc09045,steel=0xff9faeb5;
    rect(c,navy,5.0f+tx,12.0f,12.8f+tx,20.7f);rect(c,blue,5.8f+tx,12.7f,12.0f+tx,19.8f);
    rect(c,gold,5.25f+tx,18.9f,12.55f+tx,20.35f);
    float shoulderX=left?4.05f:11.55f;
    p.setColor(steel);c.drawOval(new RectF(shoulderX+tx,11.0f,shoulderX+3.0f+tx,14.5f),p);
    p.setColor(gold);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(.75f);c.drawOval(new RectF(shoulderX+.25f+tx,11.35f,shoulderX+2.75f+tx,14.15f),p);p.setStyle(Paint.Style.FILL);

    // Shield center is tied to the far-hand endpoint, so it moves with the arm instead of floating beside the body.
    ArmPose off=farArmPose(pose,frame);
    float shieldCx=off.hx-side*.45f,shieldCy=off.hy+.75f+(down?.35f:-.15f);
    p.setColor(0xff202a33);c.drawOval(new RectF(shieldCx-2.8f,shieldCy-4.3f,shieldCx+2.8f,shieldCy+4.3f),p);
    p.setColor(0xff355777);c.drawOval(new RectF(shieldCx-2.2f,shieldCy-3.7f,shieldCx+2.2f,shieldCy+3.7f),p);
    p.setColor(gold);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(.9f);c.drawOval(new RectF(shieldCx-1.9f,shieldCy-3.35f,shieldCx+1.9f,shieldCy+3.35f),p);p.setStyle(Paint.Style.FILL);
    p.setColor(gold);c.drawCircle(shieldCx,shieldCy,1.0f,p);
  }

  private void drawWeapon(Canvas c,Pose pose,int frame){
    if(pose.effectFamily==EffectFamily.PUNCH||pose.effectFamily==EffectFamily.KICK)return;
    boolean left=isLeft(pose.direction),down=isDown(pose.direction);float sx=left?-1f:1f,sy=down?1f:-1f,q=phase(pose);
    ArmPose arm=nearArmPose(pose,frame);
    float attack=(pose.state==State.ATTACK||pose.state==State.SKILL)?(float)Math.sin(Math.PI*q):0f;
    float reach=8.5f+5.8f*attack;
    float bladeAngleY=pose.state==State.IDLE||pose.state==State.WALK?(down?.76f:-.65f):(down?.45f:-.52f);
    float ex=arm.hx+sx*reach,ey=arm.hy+sy*reach*Math.abs(bladeAngleY);

    // Grip starts exactly at the near-hand endpoint; guard sits directly beyond the fist.
    p.setStrokeCap(Paint.Cap.ROUND);p.setStrokeWidth(2.5f);p.setColor(0xff8e5f2e);
    c.drawLine(arm.hx-sx*1.7f,arm.hy-sy*1.0f,arm.hx+sx*1.25f,arm.hy+sy*.75f,p);
    p.setStrokeCap(Paint.Cap.BUTT);p.setStrokeWidth(2.2f);p.setColor(0xffc9a24c);
    c.drawLine(arm.hx-sy*1.8f,arm.hy+sx*1.8f,arm.hx+sy*1.8f,arm.hy-sx*1.8f,p);
    p.setStrokeWidth(3.0f);p.setColor(0xffaebbc0);c.drawLine(arm.hx+sx*1.0f,arm.hy+sy*.65f,ex,ey,p);
    p.setStrokeWidth(1.2f);p.setColor(0xfff4fbff);c.drawLine(arm.hx+sx*1.5f,arm.hy+sy*.9f,ex-sx*.3f,ey-sy*.2f,p);
    // Small point makes the sword silhouette read as a blade rather than a stick.
    Path tip=new Path();tip.moveTo(ex,ey);tip.lineTo(ex-sx*2.1f-sy*.7f,ey-sy*1.5f+sx*.7f);tip.lineTo(ex-sx*1.3f+sy*.7f,ey-sy*.9f-sx*.7f);tip.close();p.setColor(0xffdce6e9);c.drawPath(tip,p);
  }

  private void drawEffect(Canvas c,Pose pose){
    EffectFamily family=pose.hitFlash?EffectFamily.HIT:pose.effectFamily;if(family==EffectFamily.NONE)return;
    float q=phase(pose);boolean left=isLeft(pose.direction),down=isDown(pose.direction);float sx=left?-1f:1f,sy=down?1f:-1f;
    p.setStyle(Paint.Style.STROKE);
    switch(family){
      case CAST:case MAGIC:
        p.setStrokeWidth(1.8f);p.setColor(0xcc78c8ff);c.drawCircle(9+sx*4f,5+sy*5f,4+8*q,p);c.drawCircle(9+sx*4f,5+sy*5f,10-3*q,p);break;
      case SKILL:
        // Broader crescent with an inner highlight, matching the board's readable blue slash mass.
        p.setStrokeWidth(4.0f);p.setColor(0xcf70bff0);float ax=9+sx*8f,ay=15+sy*4f;
        c.drawArc(new RectF(ax-16,ay-13,ax+16,ay+13),left?26:190,152,false,p);
        p.setStrokeWidth(2.0f);p.setColor(0xe7c7efff);c.drawArc(new RectF(ax-13,ay-10.5f,ax+13,ay+10.5f),left?29:193,147,false,p);
        p.setStrokeWidth(.9f);p.setColor(0xb6ffffff);c.drawArc(new RectF(ax-18,ay-14.5f,ax+18,ay+14.5f),left?24:188,156,false,p);break;
      case HIT:
        p.setStrokeWidth(1.8f);p.setColor(0xd9ff7b61);c.drawCircle(9-sx*3f,10-sy*2f,4+6*q,p);break;
      case THROW:
        p.setStyle(Paint.Style.FILL);p.setColor(0xffffd36a);c.drawCircle(9+sx*26*q,14+sy*17*q,2f,p);break;
      default:break;
    }
    p.setStyle(Paint.Style.FILL);
  }

  private boolean isLeft(Direction d){return d==Direction.NW||d==Direction.SW;}
  private boolean isDown(Direction d){return d==Direction.SW||d==Direction.SE;}
  private float phase(Pose pose){return pose.stateDuration<=0?0:Math.max(0,Math.min(1,pose.stateClock/pose.stateDuration));}
  private void rect(Canvas c,int color,float l,float t,float r,float b){p.setColor(color);c.drawRect(l,t,r,b,p);}
  public boolean hasRequiredStateContract(){return CharacterRendererAudit.passes();}
  public String contractAuditSummary(){return CharacterRendererAudit.summary();}
  public boolean ownsPlayerLocalEffects(){return true;}
  public float playerRenderScale(){return PLAYER_RENDER_SCALE;}
}
