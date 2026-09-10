package com.projectdark.mobile;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * [B]/[ADAPTED] Layered four-direction character renderer scaffold.
 * Authenticated original directional frames remain PENDING_CROP; procedural geometry exists only
 * to preserve the canonical NW/NE/SW/SE side-diagonal presentation contract until they are mapped.
 */
public final class CharacterRenderer {
  public static final String EVIDENCE="B+ADAPTED";
  public static final String ASSET_STATUS="PENDING_CROP";

  /** User runtime-approved baseline restored from c149434f; logical/world coordinates stay unchanged. */
  public static final float PLAYER_RENDER_SCALE=1.35f;
  public static final float SHADOW_RENDER_SCALE=0.72f;
  public static final float LOGICAL_FOOT_ANCHOR_Y=0f;

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
    public final Direction direction;
    public final State state;
    public final boolean hitFlash;
    public final String equipmentVisualRef,weaponVisualRef,effectVisualRef;
    public final EffectFamily effectFamily;

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
    int frame=pose.state==State.WALK?((int)(pose.walkClock*8f)%4):0;
    float bob=(frame==1||frame==3)?-1.5f:0f;
    boolean left=isLeft(pose.direction),down=isDown(pose.direction);
    float facingX=left?-1f:1f,facingY=down?1f:-1f;
    float recoilX=pose.state==State.HIT?-facingX*3.5f:0f;
    float recoilY=pose.state==State.HIT?-facingY*1.4f:0f;
    float anchorY=pose.y+LOGICAL_FOOT_ANCHOR_Y;

    float sw=13f*SHADOW_RENDER_SCALE*(pose.state==State.DEAD?1.32f:1f);
    float sh=4f*SHADOW_RENDER_SCALE*(pose.state==State.DEAD?0.72f:1f);
    p.setColor(pose.hitFlash?0x99ff7766:0x66000000);
    c.drawOval(new RectF(pose.x-sw,anchorY-sh,pose.x+sw,anchorY+sh),p);

    c.save();c.translate(pose.x+recoilX,anchorY-(30f*PLAYER_RENDER_SCALE)+bob*PLAYER_RENDER_SCALE+recoilY);
    c.scale(PLAYER_RENDER_SCALE,PLAYER_RENDER_SCALE);c.translate(-8,0);
    if(pose.state==State.DEAD){c.rotate(left?-72f:72f,8f,29f);c.scale(1f,0.84f,8f,29f);}
    for(Layer layer:DRAW_ORDER)drawLayer(c,pose,layer,frame);c.restore();
  }

  private void drawLayer(Canvas c,Pose pose,Layer layer,int frame){switch(layer){case BODY:drawBody(c,pose,frame);break;case HAIR:drawHair(c,pose);break;case EQUIPMENT:drawEquipment(c,pose);break;case WEAPON:drawWeapon(c,pose);break;case EFFECT:drawEffect(c,pose);break;}}

  private void drawBody(Canvas c,Pose pose,int frame){
    int outline=0xff171313,skin=pose.hitFlash?0xffffe0d0:0xffffc68f,pants=0xff393a3a,shoe=0xff6a4526;
    boolean left=isLeft(pose.direction),down=isDown(pose.direction);float side=left?-1f:1f;
    int step=pose.state==State.WALK?(frame==1?1:frame==3?-1:0):0;int depth=down?step:-step;
    float torsoShift=side*1.2f,headShift=side*1.8f;
    float nearLegX=left?5f:11f,farLegX=left?10.5f:5.5f,nearArmX=left?3f:13f,farArmX=left?13f:3f;
    float castLift=pose.state==State.CAST?-6f:0f,skillLift=pose.state==State.SKILL?-2.5f:0f;
    float attackReach=pose.state==State.ATTACK?3f*side*phase(pose):0f;
    float kick=pose.effectFamily==EffectFamily.KICK&&phase(pose)>.22f&&phase(pose)<.78f?5f*side:0f;

    rect(c,outline,farLegX-step,20-depth,farLegX+2.2f-step,29-depth);rect(c,pants,farLegX+.4f-step,20-depth,farLegX+1.8f-step,27-depth);rect(c,shoe,farLegX-.5f-step,27-depth,farLegX+2.8f-step,30-depth);
    rect(c,outline,nearLegX+step+kick,20+depth,nearLegX+2.4f+step+kick,29+depth);rect(c,pants,nearLegX+.4f+step+kick,20+depth,nearLegX+2f+step+kick,27+depth);rect(c,shoe,nearLegX-.5f+step+kick,27+depth,nearLegX+3f+step+kick,30+depth);
    rect(c,outline,4.7f+torsoShift,9f,12.3f+torsoShift,22f);rect(c,skin,5.6f+torsoShift,10f,11.4f+torsoShift,16.5f);
    float farSwing=pose.state==State.WALK?-step*1.7f:0f,nearSwing=-farSwing;
    rect(c,outline,farArmX-1f,11f+farSwing,farArmX+1f,20f+farSwing);rect(c,skin,farArmX-.5f,12f+farSwing,farArmX+.5f,19f+farSwing);
    rect(c,outline,nearArmX-1f+attackReach,11f+castLift+skillLift+nearSwing,nearArmX+1f+attackReach,20f+nearSwing);rect(c,skin,nearArmX-.5f+attackReach,12f+castLift+skillLift+nearSwing,nearArmX+.5f+attackReach,19f+nearSwing);
    p.setColor(outline);c.drawOval(new RectF(4.2f+headShift,0f,13.0f+headShift,10f),p);p.setColor(skin);c.drawOval(new RectF(5.0f+headShift,1f,12.2f+headShift,9f),p);
    if(pose.state==State.DEAD){p.setColor(0x44000000);c.drawRect(2,8,15,27,p);}
  }

  private void drawHair(Canvas c,Pose pose){
    int hair=0xff3b251b;boolean left=isLeft(pose.direction),back=!isDown(pose.direction);float shift=left?-1.5f:1.5f;
    rect(c,hair,3f+shift,0,14f+shift,4);rect(c,hair,(left?2f:11f)+shift,2,(left?6f:15f)+shift,9);rect(c,hair,5f+shift,-1,12f+shift,2);
    if(back)rect(c,hair,(left?10f:3f)+shift,3,(left?14f:7f)+shift,9);else{int eye=0xff252020;float eyeX=left?6f:11f;rect(c,eye,eyeX+shift,6,eyeX+1f+shift,7);}
  }

  private void drawEquipment(Canvas c,Pose pose){if(ASSET_STATUS.equals(pose.equipmentVisualRef))return;boolean left=isLeft(pose.direction);float shift=(left?-1f:1f)*1.2f;int shirt=pose.hitFlash?0xffffd0ca:0xffeee5d3;rect(c,0xff171313,4.7f+shift,9,12.3f+shift,21);rect(c,shirt,5.5f+shift,10,11.5f+shift,18.5f);rect(c,0xff3c6382,5.2f+shift,16,11.8f+shift,21);}

  private void drawWeapon(Canvas c,Pose pose){if(pose.state!=State.ATTACK||pose.effectFamily==EffectFamily.PUNCH)return;boolean left=isLeft(pose.direction),down=isDown(pose.direction);float sx=left?-1f:1f,sy=down?1f:-1f;float q=phase(pose),reach=5f+7f*(q<.5f?q*2f:(1f-q)*2f);float handX=left?3f:14f,handY=down?15f:12f;p.setColor(0xffd7d2c5);p.setStrokeWidth(2);c.drawLine(handX,handY,handX+sx*reach,handY+sy*reach*.72f,p);}

  private void drawEffect(Canvas c,Pose pose){
    EffectFamily family=pose.hitFlash?EffectFamily.HIT:pose.effectFamily;if(family==EffectFamily.NONE)return;float q=phase(pose);boolean left=isLeft(pose.direction),down=isDown(pose.direction);float sx=left?-1f:1f,sy=down?1f:-1f;
    switch(family){
      case CAST:p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1.5f);p.setColor(0xaa78b9ff);c.drawCircle(8+sx*2,-7+sy*1.5f,5+9*q,p);c.drawCircle(8+sx*2,-7+sy*1.5f,12-4*q,p);p.setStyle(Paint.Style.FILL);break;
      case MAGIC:p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2f);p.setColor(0xaa9d7cff);float mx=8+sx*5,my=6+sy*8;c.drawCircle(mx,my,4+7*q,p);c.drawLine(mx-sx*8,my-sy*8,mx+sx*8,my+sy*8,p);p.setStyle(Paint.Style.FILL);break;
      case THROW:p.setColor(0xffffd76b);c.drawCircle(8+sx*28*q,13+sy*18*q,2,p);break;
      case PUNCH:case KICK:p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1.7f);p.setColor(0xaaffefb0);float cx=8+sx*11,cy=13+sy*5;c.drawArc(new RectF(cx-8,cy-8,cx+8,cy+8),left?55:205,105,false,p);p.setStyle(Paint.Style.FILL);break;
      case SKILL:p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2.6f);p.setColor(0xaa7fffa8);float ax=8+sx*10,ay=12+sy*7;c.drawArc(new RectF(ax-12-q*4,ay-9-q*3,ax+12+q*4,ay+9+q*3),left?40:190,130,false,p);p.setStyle(Paint.Style.FILL);break;
      case HIT:p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1.5f);p.setColor(0xaaff7755);c.drawCircle(8-sx*4,9-sy*3,5+7*q,p);p.setStyle(Paint.Style.FILL);break;
      default:break;
    }
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
