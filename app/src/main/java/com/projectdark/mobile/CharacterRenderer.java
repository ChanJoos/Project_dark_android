package com.projectdark.mobile;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * [O+B] Layered character presentation scaffold.
 * Base-avatar silhouette/proportion and creation choices are grounded in Nexon's official
 * character-creation screenshot while exact directional sprite crops remain PENDING_CROP.
 */
public final class CharacterRenderer {
  public static final String EVIDENCE="O+B";
  public static final String ASSET_STATUS="PENDING_CROP";
  public static final float PLAYER_RENDER_SCALE=0.92f;
  public static final float SHADOW_RENDER_SCALE=0.58f;
  public static final float LOGICAL_FOOT_ANCHOR_Y=0f;
  public static final String BASE_AVATAR_PROPORTION_SOURCE=CharacterVisualSourceManifest.CHARACTER_CREATION_SCREENSHOT_URL;

  public enum Direction { NW, NE, SW, SE }
  public enum State { IDLE, WALK, CAST, ATTACK, SKILL, HIT, DEAD }
  public enum Layer { BODY, HAIR, EQUIPMENT, WEAPON, EFFECT }
  public enum EffectFamily { NONE, CAST, MAGIC, THROW, PUNCH, KICK, SKILL, HIT }

  public static final List<Layer> DRAW_ORDER=Collections.unmodifiableList(Arrays.asList(
      Layer.BODY,Layer.HAIR,Layer.EQUIPMENT,Layer.WEAPON,Layer.EFFECT));

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
      this.effectVisualRef=effectVisualRef;
      this.effectFamily=effectFamily==null?EffectFamily.NONE:effectFamily;
    }
  }

  private static final boolean CONTRACT_VALID=CharacterRendererAudit.passes();
  private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);

  public CharacterRenderer(){
    if(!CONTRACT_VALID)throw new IllegalStateException("CharacterRenderer contract audit failed: "+CharacterRendererAudit.summary());
  }

  /** Backward-compatible runtime entry: current GameView automatically receives the guide-backed male appearance. */
  public void draw(Canvas c,Pose pose){draw(c,pose,CharacterAppearance.defaultGuideMale());}

  /** Appearance-aware renderer API for later character-creation wiring without changing gameplay semantics. */
  public void draw(Canvas c,Pose pose,CharacterAppearance appearance){
    if(pose==null||pose.direction==null||pose.state==null)throw new IllegalArgumentException("Character pose requires direction and state");
    if(appearance==null)appearance=CharacterAppearance.defaultGuideMale();
    int frame=pose.state==State.WALK?((int)(pose.walkClock*8f)%4):0; // [B] prototype cadence
    float bob=(frame==1||frame==3)?-2f:0f;
    boolean left=pose.direction==Direction.NW||pose.direction==Direction.SW;
    boolean down=pose.direction==Direction.SW||pose.direction==Direction.SE;
    float facingX=left?-1f:1f,facingY=down?1f:-1f;
    float recoilX=pose.state==State.HIT?-facingX*3.5f:0f;
    float recoilY=pose.state==State.HIT?-facingY*1.5f:0f;
    float anchorY=pose.y+LOGICAL_FOOT_ANCHOR_Y;
    float shadowHalfWidth=13f*SHADOW_RENDER_SCALE*(pose.state==State.DEAD?1.35f:1f);
    float shadowHalfHeight=4f*SHADOW_RENDER_SCALE*(pose.state==State.DEAD?0.72f:1f);
    p.setColor(pose.hitFlash?0x99ff7766:0x66000000);
    c.drawOval(new RectF(pose.x-shadowHalfWidth,anchorY-shadowHalfHeight,pose.x+shadowHalfWidth,anchorY+shadowHalfHeight),p);

    c.save();
    c.translate(pose.x+recoilX,anchorY-(30f*PLAYER_RENDER_SCALE)+bob*PLAYER_RENDER_SCALE+recoilY);
    c.scale(PLAYER_RENDER_SCALE,PLAYER_RENDER_SCALE);
    c.translate(-8,0);
    if(pose.state==State.DEAD){c.rotate(left?-76f:76f,8f,29f);c.scale(1f,0.86f,8f,29f);}
    for(Layer layer:DRAW_ORDER)drawLayer(c,pose,appearance,layer,frame);
    c.restore();
  }

  private void drawLayer(Canvas c,Pose pose,CharacterAppearance appearance,Layer layer,int frame){
    switch(layer){
      case BODY:drawBody(c,pose,appearance,frame);break;
      case HAIR:drawHair(c,pose,appearance);break;
      case EQUIPMENT:drawEquipment(c,pose);break;
      case WEAPON:drawWeapon(c,pose);break;
      case EFFECT:drawEffect(c,pose);break;
    }
  }

  private void drawBody(Canvas c,Pose pose,CharacterAppearance appearance,int frame){
    int outline=0xff2a1b17,skin=pose.hitFlash?0xffffe0d0:0xffefb58b,baseGarment=0xff5f4439,foot=0xff7f5b44;
    boolean left=pose.direction==Direction.NW||pose.direction==Direction.SW;
    boolean down=pose.direction==Direction.SW||pose.direction==Direction.SE;
    int step=frame==1?1:frame==3?-1:0,depthStep=down?step:-step;
    int kick=pose.effectFamily==EffectFamily.KICK&&phase(pose)>.22f&&phase(pose)<.78f?5:0;

    rect(c,outline,5+step-kick,20+depthStep,7+step,29+depthStep);
    rect(c,outline,10-step,20-depthStep,12-step+kick,29-depthStep);
    rect(c,skin,5.5f+step-kick,20+depthStep,6.5f+step,27+depthStep);
    rect(c,skin,10.5f-step,20-depthStep,11.5f-step+kick,27-depthStep);
    rect(c,foot,4.5f+step-kick,27+depthStep,7.5f+step,30+depthStep);
    rect(c,foot,9.5f-step,27-depthStep,12.5f-step+kick,30-depthStep);
    rect(c,outline,4,15,13,22);rect(c,baseGarment,5,16,12,21);

    int walkSwing=pose.state==State.WALK?step*2:0;
    int armLift=pose.state==State.CAST?-7:pose.state==State.SKILL?-3:0;
    int nearSwing=left?walkSwing:-walkSwing,farSwing=-nearSwing,verticalBias=down?1:-1;
    rect(c,outline,5,8,12,17);rect(c,skin,6,9,11,16);
    rect(c,outline,2,10+armLift+farSwing*verticalBias,4,20+farSwing*verticalBias);
    rect(c,skin,2.5f,11+armLift+farSwing*verticalBias,3.5f,19+farSwing*verticalBias);
    rect(c,outline,13,10+armLift+nearSwing*verticalBias,15,20+nearSwing*verticalBias);
    rect(c,skin,13.5f,11+armLift+nearSwing*verticalBias,14.5f,19+nearSwing*verticalBias);
    p.setColor(outline);c.drawOval(new RectF(3.5f,0f,13.5f,10f),p);
    p.setColor(skin);c.drawOval(new RectF(4.5f,1f,12.5f,9f),p);
    if(appearance.gender==CharacterAppearance.Gender.FEMALE){
      // Gender selection is official; exact female body pixels are still PENDING_CROP, so do not fabricate a new anatomy.
      p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(.7f);p.setColor(0x55ffffff);c.drawOval(new RectF(4.2f,.2f,13.2f,9.8f),p);p.setStyle(Paint.Style.FILL);
    }
    if(pose.state==State.DEAD){p.setColor(0x44000000);c.drawRect(2,8,15,26,p);}
  }

  private void drawHair(Canvas c,Pose pose,CharacterAppearance appearance){
    int hair=appearance.previewHairColor();
    int s=appearance.hairStyleIndex;
    // [O+B] Eighteen selectable silhouettes are visible in the official creation UI. Exact pixels remain adapted.
    switch(s){
      case 0: rect(c,hair,3,0,14,4);rect(c,hair,2,2,5,8);rect(c,hair,12,2,15,7);rect(c,hair,5,-1,12,2);break;
      case 1: rect(c,hair,4,-1,13,3);rect(c,hair,2,1,6,6);rect(c,hair,11,2,15,8);rect(c,hair,3,5,5,10);break;
      case 2: rect(c,hair,2,0,15,3);rect(c,hair,2,2,4,9);rect(c,hair,13,2,15,9);rect(c,hair,6,3,8,7);break;
      case 3: rect(c,hair,4,-1,14,4);rect(c,hair,1,2,6,6);rect(c,hair,11,3,15,6);rect(c,hair,2,6,4,9);break;
      case 4: rect(c,hair,2,0,15,4);rect(c,hair,1,2,4,8);rect(c,hair,13,2,16,8);rect(c,hair,5,3,7,8);rect(c,hair,10,3,12,8);break;
      case 5: rect(c,hair,3,-1,14,3);rect(c,hair,2,2,5,7);rect(c,hair,12,2,15,10);rect(c,hair,5,3,7,5);break;
      case 6: rect(c,hair,2,0,15,4);rect(c,hair,2,3,4,9);rect(c,hair,13,3,15,9);rect(c,hair,7,3,10,7);break;
      case 7: rect(c,hair,3,-1,14,3);rect(c,hair,1,2,5,7);rect(c,hair,12,1,16,6);rect(c,hair,4,6,6,9);break;
      case 8: rect(c,hair,2,0,15,3);rect(c,hair,1,2,4,10);rect(c,hair,13,2,16,10);rect(c,hair,5,-2,8,2);break;
      case 9: rect(c,hair,4,-2,13,3);rect(c,hair,2,1,5,8);rect(c,hair,12,1,15,7);rect(c,hair,6,3,9,6);break;
      case 10: rect(c,hair,2,-1,15,3);rect(c,hair,2,2,5,8);rect(c,hair,12,2,15,8);rect(c,hair,4,4,7,6);rect(c,hair,10,4,13,6);break;
      case 11: rect(c,hair,3,-1,14,2);rect(c,hair,1,1,5,6);rect(c,hair,12,3,16,9);rect(c,hair,4,5,6,10);break;
      case 12: rect(c,hair,2,0,15,4);rect(c,hair,1,3,4,9);rect(c,hair,13,3,16,9);rect(c,hair,5,4,12,6);break;
      case 13: rect(c,hair,3,-2,14,3);rect(c,hair,2,1,5,6);rect(c,hair,12,1,15,6);rect(c,hair,7,3,10,8);break;
      case 14: rect(c,hair,2,-1,15,3);rect(c,hair,1,2,5,9);rect(c,hair,12,2,16,9);rect(c,hair,5,-3,7,1);rect(c,hair,10,-3,12,1);break;
      case 15: rect(c,hair,4,-1,13,3);rect(c,hair,2,2,6,8);rect(c,hair,11,2,15,8);rect(c,hair,3,7,5,11);rect(c,hair,12,7,14,11);break;
      case 16: rect(c,hair,2,0,15,3);rect(c,hair,1,2,4,8);rect(c,hair,13,2,16,8);rect(c,hair,6,3,11,9);break;
      default: rect(c,hair,3,-1,14,4);rect(c,hair,1,2,5,7);rect(c,hair,12,2,16,7);rect(c,hair,4,5,6,10);rect(c,hair,11,5,13,10);break;
    }
    boolean back=pose.direction==Direction.NW||pose.direction==Direction.NE;
    if(!back){int eye=0xff2a2020;boolean left=pose.direction==Direction.SW;rect(c,eye,left?5:7,6,left?6:8,7);rect(c,eye,left?9:11,6,left?10:12,7);}
  }

  private void drawEquipment(Canvas c,Pose pose){
    if(pose.equipmentVisualRef==null||ASSET_STATUS.equals(pose.equipmentVisualRef))return;
    int shirt=pose.hitFlash?0xffffd0ca:0xffd8c7aa;
    rect(c,0xff2a1b17,4,8,13,18);rect(c,shirt,5,9,12,16);rect(c,0xff4e657c,5,14,12,18);
  }

  private void drawWeapon(Canvas c,Pose pose){
    if(pose.state!=State.ATTACK||pose.effectFamily==EffectFamily.PUNCH)return;
    boolean left=pose.direction==Direction.NW||pose.direction==Direction.SW;
    boolean down=pose.direction==Direction.SW||pose.direction==Direction.SE;
    float sx=left?-1f:1f,sy=down?1f:-1f;
    p.setColor(0xffd7d2c5);p.setStrokeWidth(2);
    float handX=left?2f:15f,handY=down?15f:12f;
    c.drawLine(handX,handY,handX+sx*9f,handY+sy*7f,p);
  }

  private void drawEffect(Canvas c,Pose pose){
    EffectFamily family=pose.hitFlash?EffectFamily.HIT:pose.effectFamily;
    float q=phase(pose);boolean left=pose.direction==Direction.NW||pose.direction==Direction.SW;
    float sx=left?-1f:1f,sy=(pose.direction==Direction.SW||pose.direction==Direction.SE)?1f:-1f;
    if(family==EffectFamily.NONE&&pose.state==State.ATTACK){p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1.8f);p.setColor(0xaad7d2c5);float leadX=8+sx*(10+5*q),leadY=12+sy*(4+3*q),start=left?(sy>0?35f:205f):(sy>0?25f:215f);c.drawArc(new RectF(leadX-12,leadY-11,leadX+12,leadY+11),start,115,false,p);p.setStyle(Paint.Style.FILL);return;}
    if(family==EffectFamily.NONE)return;
    switch(family){
      case CAST:p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1.5f);p.setColor(0xaa78b9ff);c.drawCircle(8,-7,5+9*q,p);c.drawCircle(8,-7,12-4*q,p);p.setStyle(Paint.Style.FILL);break;
      case MAGIC:p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2f);p.setColor(0xaa9d7cff);c.drawCircle(8,-7,4+7*q,p);c.drawLine(8,-16-(5*q),8,-1+(3*q),p);c.drawLine(-1+(4*q),-7,17-(4*q),-7,p);p.setStyle(Paint.Style.FILL);break;
      case THROW:p.setColor(0xffffd76b);c.drawCircle(8+sx*27.5f*q,13+sy*17.5f*q,2,p);break;
      case PUNCH:case KICK:case SKILL:p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(family==EffectFamily.SKILL?2.5f:1.5f);p.setColor(family==EffectFamily.SKILL?0xaa7fffa8:0xaaffefb0);float cx=8+sx*9f,cy=12+sy*3f;c.drawArc(new RectF(cx-9,cy-9,cx+9,cy+9),left?(sy>0?25f:205f):(sy>0?15f:215f),130,false,p);p.setStyle(Paint.Style.FILL);break;
      case HIT:p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1.5f);p.setColor(0xaaff7755);c.drawCircle(8,8,5+7*q,p);c.drawLine(8-sx*2f,8-sy*2f,8+sx*8f,8+sy*8f,p);p.setStyle(Paint.Style.FILL);break;
      default:break;
    }
  }

  private float phase(Pose pose){return pose.stateDuration<=0?0:Math.max(0,Math.min(1,pose.stateClock/pose.stateDuration));}
  private void rect(Canvas c,int color,float l,float t,float r,float b){p.setColor(color);c.drawRect(l,t,r,b,p);}
  public boolean hasRequiredStateContract(){return CharacterRendererAudit.passes();}
  public String contractAuditSummary(){return CharacterRendererAudit.summary();}
  public boolean ownsPlayerLocalEffects(){return true;}
  public float playerRenderScale(){return PLAYER_RENDER_SCALE;}
}